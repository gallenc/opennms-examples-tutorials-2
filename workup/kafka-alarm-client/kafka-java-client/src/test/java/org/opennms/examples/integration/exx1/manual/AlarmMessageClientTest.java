package org.opennms.examples.integration.exx1.manual;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.opennms.examples.integration.exx1.AlarmMessageCallbackSimpleImpl;
import org.opennms.examples.integration.exx1.AlarmMessageClient;
import org.opennms.examples.integration.exx1.LoadProperties;
import org.opennms.features.kafka.producer.model.OpennmsModelProtos.Alarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AlarmMessageClientTest {
	private static final Logger LOG = LoggerFactory.getLogger(AlarmMessageClientTest.class);

	@Test
	void test() throws IOException {
		LOG.debug("Starting test");
		AlarmMessageClient aeclient = new AlarmMessageClient();
		
		Properties clientProperties = LoadProperties.load();
		aeclient.setClientProperties(clientProperties);

		aeclient.setAlarmTopic("alarms");
		
		aeclient.subscribe(new AlarmMessageCallbackSimpleImpl());
		
		aeclient.init();
		LOG.debug("finished initialisation - waiting for alarm events ");

		// now wait for events

		try {
			Thread.sleep(100000);
		} catch (InterruptedException e) {
			LOG.info("finisned test.");
			return;
		} finally {
			if(aeclient!=null) aeclient.destroy();
		}

		
	}

}
