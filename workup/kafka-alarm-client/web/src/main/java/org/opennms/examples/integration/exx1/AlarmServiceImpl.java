package org.opennms.examples.integration.exx1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opennms.features.kafka.producer.model.OpennmsModelProtos.Alarm;
import org.springframework.stereotype.Component;

@Component
public class AlarmServiceImpl implements AlarmService{
	
	KtableAlarmClient kclient;
	
	public AlarmServiceImpl(){
		kclient = new KtableAlarmClient();
		kclient.setAlarmTopic("alarms");
		try {
			kclient.init();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	


	public Map<String, Alarm> getAlarms() {
		Map<String, Alarm> alarmList = kclient.getAlarms();
		return alarmList;
	}
	
}
