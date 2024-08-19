package org.opennms.examples.integration.exx1;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.InvalidProtocolBufferException;

import org.opennms.features.kafka.producer.model.OpennmsModelProtos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class AlarmMessageClient implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(AlarmMessageClient.class);

	private final AtomicBoolean closed = new AtomicBoolean(true);

	private ScheduledExecutorService scheduler;

	private Consumer<String, byte[]> alarmConsumer;

	private String alarmTopic;

	private Properties clientProperties;

	// Create the set by newKeySet() method of ConcurrentHashMap
	Set<AlarmMessageCallback> clientSet = ConcurrentHashMap.newKeySet();

	public void subscribe(AlarmMessageCallback alarmMessageCallback) {
		if (clientSet.contains(alarmMessageCallback)) {
			LOG.warn("trying to resubscribe alarmEventCallback. ignoring" + alarmMessageCallback.toString());
		} else {
			clientSet.add(alarmMessageCallback);
			LOG.info("subscribed alarmEventCallback." + alarmMessageCallback.toString());
		}
	}

	public void unsubscribe(AlarmMessageCallback alarmMessageCallback) {
		LOG.info("unsubscribing " + alarmMessageCallback.toString());
		clientSet.remove(alarmMessageCallback);
	}

	public void destroy() {
		LOG.info("shutting down alarm message consumer");

		closed.set(true);
		if (scheduler != null) {
			scheduler.shutdown();
			try {
				scheduler.awaitTermination(2, TimeUnit.MINUTES);
			} catch (final InterruptedException e) {
				LOG.warn("Failed to shut down the alarm message consumer.", e);
			}
		}

		alarmConsumer.close();

	}

	public void init() throws IOException {

		// clientProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
		// BOOTSTRAP_SERVERS);
		clientProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaExampleConsumer");
		clientProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		clientProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());

		alarmConsumer = new KafkaConsumer<String, byte[]>(clientProperties);
		// Defer startup to another thread

		scheduler = Executors.newScheduledThreadPool(1,
				new ThreadFactoryBuilder().setNameFormat("kafka-client-alarm-event-%d").build());
		closed.set(false);
		scheduler.execute(this);
	}

	@Override
	public void run() {
		if (alarmConsumer == null)
			throw new IllegalStateException("alarmConsumer must be set before invoking run");

		try {
			// Subscribe to the topic.
			alarmConsumer.subscribe(Collections.singletonList(alarmTopic));
		} catch (Exception ex) {
			LOG.error("Faild to subscribe to alarm topic.", ex);
		}
		
		LOG.info("Alarm consumer subscribed");

		while (!closed.get()) {

			final ConsumerRecords<String, byte[]> consumerRecords = alarmConsumer.poll(Duration.ofMillis(1000));

			if(consumerRecords.count()!=0) LOG.debug("received " + consumerRecords.count() + " alarmRecords");
			
			consumerRecords.forEach(record -> {
				String reductionKey = record.key();
				final byte[] alarmBytes = record.value();
				if (alarmBytes==null) {
					LOG.info("\n*****************************"
							+ "\n clear alarm (record is null) for reduction key " + reductionKey
							+"\n*****************************\n"
							);
				} else 	try {
					OpennmsModelProtos.Alarm alarm = OpennmsModelProtos.Alarm.parseFrom(alarmBytes);
					LOG.debug("Alarm Event key: " + record.key() + " id:" + alarm.getId() + " message:"
							+ alarm.getLogMessage());
					sendAlarmMessage(alarm);
				} catch (Exception e) {
					LOG.error("Failed to parse alarm for bytes at reduction key " + reductionKey, e);
				}

			});

			alarmConsumer.commitAsync();
		}

	}

	private void sendAlarmMessage(OpennmsModelProtos.Alarm alarmEvent) {
		LOG.debug("sending alarm message to "+clientSet.size()+" clients");
		for(AlarmMessageCallback client :clientSet) {
			try {
				client.onAlarmEvent(alarmEvent);
			} catch (Exception e) {
				LOG.error("failure calling AlarmMessageCallback client callback client.class="+client.getClass()
						+ " Removing subscription",e  );
				clientSet.remove(client);
			}
		}
		
	}

	public void setAlarmConsumer(Consumer<String, byte[]> alarmConsumer) {
		this.alarmConsumer = alarmConsumer;

	}

	public void setAlarmTopic(String alarmTopic) {
		this.alarmTopic = alarmTopic;
	}

	public void setClientProperties(Properties clientProperties) {
		this.clientProperties = clientProperties;
	}

}
