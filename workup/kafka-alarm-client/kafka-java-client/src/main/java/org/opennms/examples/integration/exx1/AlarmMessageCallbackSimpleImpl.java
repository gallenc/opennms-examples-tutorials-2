package org.opennms.examples.integration.exx1;

import org.opennms.features.kafka.producer.model.OpennmsModelProtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlarmMessageCallbackSimpleImpl implements AlarmMessageCallback {
	private static final Logger LOG = LoggerFactory.getLogger(AlarmMessageCallbackSimpleImpl.class);

	@Override
	public void onAlarmEvent(OpennmsModelProtos.Alarm alarmEvent) {
		LOG.info("*****************************\n"
				+ "callback received alarm message:\n" 
				+ alarmEvent
				+"\n*****************************");

	}

}
