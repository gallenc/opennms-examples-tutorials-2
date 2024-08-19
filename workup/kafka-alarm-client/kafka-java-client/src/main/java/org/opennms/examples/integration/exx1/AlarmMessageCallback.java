package org.opennms.examples.integration.exx1;

import org.opennms.features.kafka.producer.model.OpennmsModelProtos;

public interface AlarmMessageCallback {
	
	public void onAlarmEvent(OpennmsModelProtos.Alarm alarmEvent);
	

}
