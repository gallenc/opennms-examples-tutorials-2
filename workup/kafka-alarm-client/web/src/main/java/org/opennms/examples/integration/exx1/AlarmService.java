package org.opennms.examples.integration.exx1;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opennms.features.kafka.producer.model.OpennmsModelProtos.Alarm;

public interface AlarmService {

	public Map<String, Alarm> getAlarms();

}
