package org.opennms.examples.integration.exx1;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {

		boolean shortmessages = Arrays.asList(args).contains("--shortmessages");

		AlarmMessageClient aeclient = null;

		CountDownLatch doneSignal = new CountDownLatch(1);

		Runtime.getRuntime().addShutdownHook(new Thread() {

			/** This handler will be called on Control-C pressed */
			@Override
			public void run() {
				// Decrement counter.
				// It will became 0 and main thread who waits for this barrier could continue
				// run (and fulfill all proper shutdown steps)
				doneSignal.countDown();
			}
		});

		try {
			LOG.debug("Starting async client");
			aeclient = new AlarmMessageClient();

			Properties clientProperties = LoadProperties.load();
			aeclient.setClientProperties(clientProperties);

			aeclient.setAlarmTopic("alarms");

			if (shortmessages) {
				LOG.debug("printing short messages");
				aeclient.subscribe(new AlarmMessageCallbackShortImpl());
			} else {
				LOG.debug("printing full messages");
				aeclient.subscribe(new AlarmMessageCallbackSimpleImpl());
			}

			aeclient.init();
			LOG.debug("finished initialisation - waiting for alarm events ");

			// now wait for events
			// Here we enter wait state until control-c will be pressed
			doneSignal.await();

		} catch (InterruptedException e) {
			LOG.info("client shutdown.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (aeclient != null)
				aeclient.destroy();
		}

	}

}
