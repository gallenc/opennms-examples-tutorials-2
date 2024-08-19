package org.opennms.examples.integration.exx1.manual;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.opennms.examples.integration.exx1.KtableAlarmClient;
import org.opennms.features.kafka.producer.model.OpennmsModelProtos.Alarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class KtableAlarmClientTest {
	private static final Logger LOG = LoggerFactory.getLogger(KtableAlarmClientTest.class);

	@Test
	void test() throws IOException {
		LOG.debug("Starting test");
		KtableAlarmClient kclient = new KtableAlarmClient();
		
		kclient.setAlarmTopic("alarms");
		kclient.init();
		LOG.debug("finished initilisation - waiting ");
		
		int i = 0;
		while (i<10 ) {
			i++;
			if (kclient.isReady()) {
				break;
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				LOG.info("waiting for kafka to be ready.");
				return;
			}
		}
		
		LOG.debug("  kclient is ready="+kclient.isReady());
		
		// now poll for alarms
		while (i<100 ) {
			i++;

			Map<String, Alarm> alarmList = kclient.getAlarms();
			LOG.debug("  alarms: "+alarmList.size() );
			for(String reductionKey :alarmList.keySet()) {
				LOG.debug("  "+ reductionKey+ " "+alarmList.get(reductionKey).getDescription());
			}

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				LOG.info("waiting before polling for alarms.");
				return;
			}
		}
		
		
		
		
		kclient.destroy();
	}

}
