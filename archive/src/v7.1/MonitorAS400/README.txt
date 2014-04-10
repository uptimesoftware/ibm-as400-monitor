-----------------------------------------------
IBM AS400 Plugin Monitor - uptime software
-----------------------------------------------


Installation
-----------------------------------------------
1. Copy the jar files in zip into the uptime/core/as400 directory

2. Copy the XML file into the uptime directory

3. Run the following commands on the command line:
> cd uptime_dir
> scripts\erdcloader –x MonitorAS400.xml

4. Add the following line to the following file: uptime/wrapper.conf:
wrapper.java.classpath.2=%UPTIMEROOT%/core/as400/*.jar

5. Restart the "up.time Data Collector" (core) service

6. Add your AS400 systems to up.time as Nodes and assign the monitors to the systems.
