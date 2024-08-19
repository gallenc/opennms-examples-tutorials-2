
# event passing collection 

see https://docs.opennms.com/horizon/30/operation/events/perf-data.html

snmptrap -v1 -c public  horizon:1162 .1.3.6.1.4.1.22222.2.4.3.12.2.2 '127.0.0.1' 6 4 100 .1.3.6.1.4.1.22222.2.4.3.12.2.21 s "Time=200 Value=300 Status=secondary Tag=Text"