package org.opennms.examples.integration.exx1;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.opennms.features.kafka.producer.model.OpennmsModelProtos;
import org.opennms.features.kafka.producer.model.OpennmsModelProtos.Alarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlarmMessageCallbackShortImpl implements AlarmMessageCallback {
	private static final Logger LOG = LoggerFactory.getLogger(AlarmMessageCallbackShortImpl.class);
	
	

	@Override
	public void onAlarmEvent(OpennmsModelProtos.Alarm alarmEvent) {
		
		 DateFormat fmt = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		
		String relatedAlarms = "related alarms count: "+alarmEvent.getRelatedAlarmCount()+" relatedAlarms: ";
		if(alarmEvent.getRelatedAlarmList() !=null) {
			for(Alarm a: alarmEvent.getRelatedAlarmList()) {
				relatedAlarms = relatedAlarms +" "+a.getId()+" "+a.getSeverity().toString()+" |";
			}
		}
		
		
		LOG.info("\n***************************** callback received alarm message:"
				+ "\nId: " + 
				alarmEvent.getId()+
				"\nlast event time: " + 
				alarmEvent.getLastEventTime()+" "+  fmt.format(new Date(alarmEvent.getLastEventTime()))+
				"\nlast update time: " + 
				alarmEvent.getLastUpdateTime()+" "+  fmt.format(new Date(alarmEvent.getLastUpdateTime()))+
				"\nseverity: " + 
				alarmEvent.getSeverity()+
				"\nuei: "+
				alarmEvent.getUei()+
				"\nlog: "+
				alarmEvent.getLogMessage()+
				"\nreductionkey: "+
				alarmEvent.getReductionKey()+
				"\nnode: "+
				alarmEvent.getNodeCriteria()+
				relatedAlarms
				+"\n*****************************");

	}

}
