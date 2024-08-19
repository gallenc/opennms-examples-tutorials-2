

package org.opennms.examples.integration.exx1;

import java.util.HashMap;
import java.util.Map;

import org.opennms.features.kafka.producer.model.OpennmsModelProtos.Alarm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class AlarmListController {
	
    private final AlarmService alarmService;

    public AlarmListController(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    @GetMapping("/viewAlarms")
    public String viewBooks(Model model) {
    	
    	Map<String, Alarm> alarmMap = alarmService.getAlarms();
    	
        model.addAttribute("alarms", alarmMap); // 
        model.addAttribute("alarmsSize", alarmMap.size());
        return "view-alarms";
    }
}