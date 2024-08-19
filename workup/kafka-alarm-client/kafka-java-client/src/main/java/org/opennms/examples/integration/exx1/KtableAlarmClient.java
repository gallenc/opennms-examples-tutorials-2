/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.examples.integration.exx1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.errors.StreamsException;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import org.opennms.features.kafka.producer.model.OpennmsModelProtos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.InvalidProtocolBufferException;

public class KtableAlarmClient implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(KtableAlarmClient.class);

	private static final String ALARM_STORE_NAME = "alarm_store";

	private final AtomicBoolean closed = new AtomicBoolean(true);

	private String alarmTopic;

	private boolean startWithCleanState = false;

	private KafkaStreams streams;
	private ScheduledExecutorService scheduler;
	private KTable<String, byte[]> alarmBytesKtable;
	private KTable<String, OpennmsModelProtos.Alarm> alarmKtable;


	private Path kafkaDir;

	public void setAlarmTopic(String alarmTopic) {
		this.alarmTopic = alarmTopic;
	}

	// @Override
	public void setStartWithCleanState(boolean startWithCleanState) {
		this.startWithCleanState = startWithCleanState;
	}

	/**
	 * This method initializes the stream client
	 *
	 * @throws IOException when an error occurs in loading/parsing the Kafka
	 *                     client/stream configuration
	 */
	public void init() throws IOException {

		final Properties streamProperties = loadStreamsProperties();

		// create kafka ktable

		final StreamsBuilder builder = new StreamsBuilder();

		final GlobalKTable<String, byte[]> alarmBytesKtable = builder.globalTable(alarmTopic,
				Consumed.with(Serdes.String(), Serdes.ByteArray()), Materialized.as(ALARM_STORE_NAME));

		final Topology topology = builder.build();

		streams = new KafkaStreams(topology, streamProperties);

		streams.setUncaughtExceptionHandler(
				(t, e) -> LOG.error(String.format("Stream error on thread: %s", t.getName()), e));

		// Defer startup to another thread
		scheduler = Executors.newScheduledThreadPool(1,
				new ThreadFactoryBuilder().setNameFormat("kafka-client-alarm-%d").build());
		closed.set(false);
		scheduler.execute(this);
	}

	@Override
	public void run() {

		try {
			if (startWithCleanState) {
				LOG.info("Performing stream state cleanup.");
				streams.cleanUp();
			}
			LOG.info("Starting alarm client stream.");
			streams.start();
			LOG.info("Starting alarm client started.");
		} catch (StreamsException | IllegalStateException e) {
			LOG.error("Failed to start alarm client stream. Synchronization will not be performed.", e);
		}

		LOG.info("Waiting for alarm data store to be ready.");
		while (!closed.get()) {
			if (isReady()) {
				break;
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				LOG.info("Interrupted while waiting for store to be ready. Synchronization will not be performed.");
				return;
			}
		}
		LOG.info("Alarm data store is ready!");
	}

	public void destroy() {
		closed.set(true);
		if (scheduler != null) {
			scheduler.shutdown();
			try {
				scheduler.awaitTermination(2, TimeUnit.MINUTES);
			} catch (final InterruptedException e) {
				LOG.warn("Failed to shut down the alarm data sync scheduler.", e);
			}
		}


		if (streams != null) {
			streams.close(Duration.ofMinutes(2));
		}

		try {
			kafkaDir.toFile().delete();
		} catch (Exception ex) {
			LOG.error("problem deleting tempory karaf data", ex);
		}
	}

	public Properties loadStreamsProperties() throws IOException {
		
		final Properties kafkaClientPoperties = LoadProperties.load();


		final Properties streamsProperties = new Properties();
		// Default values
		streamsProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, "alarm-datasync");

		kafkaDir = Files.createTempDirectory("kafka-client" + UUID.randomUUID());

		streamsProperties.put(StreamsConfig.STATE_DIR_CONFIG, kafkaDir.toString());

		// Copy kafka server info from client properties
		// Add all of the stream properties, overriding the bootstrap servers if set)

		for (Object key : kafkaClientPoperties.keySet()) {
			streamsProperties.put(key, kafkaClientPoperties.get(key));
		}

		// Override the deserializers unconditionally
		streamsProperties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
		streamsProperties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass());

		if (LOG.isDebugEnabled()) {
			String msg = "Kafka Client Starting configuration :\n";
			for (String key : streamsProperties.stringPropertyNames()) {
				msg += "   " + key.toString() + "=" + streamsProperties.getProperty(key) + "\n";
			}
			LOG.debug(msg);
		}
		return streamsProperties;
	}

	private ReadOnlyKeyValueStore<String, byte[]> getAlarmTableNow() throws InvalidStateStoreException {
		return streams
				.store(StoreQueryParameters.fromNameAndType(ALARM_STORE_NAME, QueryableStoreTypes.keyValueStore()));
	}

	// @Override
	public boolean isReady() {
		try {
			getAlarmTableNow();
			return true;
		} catch (InvalidStateStoreException ignored) {
			// Store is not yet ready for querying
			return false;
		}
	}

	// @Override
	public Map<String, OpennmsModelProtos.Alarm> getAlarms() {
		final Map<String, OpennmsModelProtos.Alarm> alarmsByReductionKey = new LinkedHashMap<>();
		getAlarmTableNow().all().forEachRemaining(kv -> {
			try {
				alarmsByReductionKey.put(kv.key,
						kv.value != null ? OpennmsModelProtos.Alarm.parseFrom(kv.value) : null);
			} catch (InvalidProtocolBufferException e) {
				LOG.error("Failed to parse alarm for bytes at reduction key '{}'. Alarm will be empty in map.", kv.key);
				alarmsByReductionKey.put(kv.key, null);
			}
		});
		return alarmsByReductionKey;
	}

	// @Override
	public OpennmsModelProtos.Alarm getAlarm(String reductionKey) {
		final byte[] alarmBytes = getAlarmTableNow().get(reductionKey);
		try {
			return OpennmsModelProtos.Alarm.parseFrom(alarmBytes);
		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException("Failed to parse alarm for bytes at reduction key " + reductionKey, e);
		}
	}

}
