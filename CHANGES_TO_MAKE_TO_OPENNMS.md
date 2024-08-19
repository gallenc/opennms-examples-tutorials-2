## CHANGES TO MAKE TO OPENNMS

# syslog

1. allow mask to use param name instead of just parm position

https://github.com/OpenNMS/opennms/blob/develop/opennms-config-model/src/main/java/org/opennms/netmgt/eventd/datablock/EventKey.java  line 423
https://github.com/OpenNMS/opennms/blob/develop/features/events/api/src/main/java/org/opennms/netmgt/xml/event/Parm.java#L50

varbind number is used rather than the varbind name - need both options
see also documentation eventconf.xsd 'The Mask for event configuration: The mask contains one
      or more 'maskelements' which uniquely identify an event. XXX need to add information about varbind'
      

2. add back minimal perl into containers to allow sendevent to work

3. allow system variables to be used in parameter substitution for metadata - this would allow general values to be set for multiple alarms

4. syslog severity - alow severity ot be explicitly set for event rathe han mapping to seperate eui values.
this would simplify the parsing and event translatino of events

5. allow syslog parsing to add a parameter which is a constant not just substitute values from regex
parameter-assignment syslog.xsd

6. syslogd to use nodename to look up interface rather than always use dns lookup ( avoids need for hosts file)

7. grok parser setting arbitrary params in grok # note setting arbitrary params is not supported yet %{STRING:paramCalexHeader} SyslogMessage.java 393 (32.0.4) 386 (develop)

8. create a plugin which allows replay of syslogs with adjusted timing

9. drools - make failure to load drules message part of event - have to restart every time at present

## ticketing

1. allow ticketing-drools rule to run on update as well as initial creation of ticket

2. expand states of ticket in OpenNMS - not just 3 states but match ticket states used in ni2 and other ticket engines

3. fix why cxf cannot work in OSGi



