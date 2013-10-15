# IBM AS400 Monitor
## Tags : plugin   as400  

## Category: plugin

##Version Compatibility<br/>Module Name</th><th>up.time Monitoring Station Version</th>


  
    * IBM AS400 Monitor 2.2 - 7.2, 7.1
  

  
    * IBM AS400 Monitor 2.1 - 7.0
  

  
    * IBM AS400 Monitor 2.0 - 6.0
  

  
    * IBM AS400 Monitor 1.1 - 5.5
  


### Description: Monitor CPU/Memory performance on an IBM (iSeries) AS/400 server.

### Supported Monitoring Stations: 7.2, 7.1
### Supported Agents: None; no agent required
### Installation Notes: <p><a href="https://github.com/uptimesoftware/uptime-plugin-manager">Install using the up.time Plugin Manager</a>
1. Currently there is a limitation with the max upload size in Apache (&lt;2MB) so we'll have to increase it first.</p>

<p>Edit the php.ini file and modify the options below.</p>

<p>Windows Monitoring Station location: [uptime_dir]\apache\php\php.ini
Linux/Solaris Monitoring Station location: [uptime_dir]/apache/conf/php.ini</p>

<p>; Maximum size of POST data that PHP will accept.
post_max_size = 100M
; Maximum allowed size for uploaded files.
upload_max_filesize = 100M</p>

<ol>
<li>Restart the up.time Web Server for the changes to go into effect.</li>
</ol>


### Dependencies: <p>n/a</p>

### Input Variables: * AS/400 user name* AS/400 password
### Output Variables: * Basic Monitor (MonitorAS400.xml)* CPU Utilization* Disk Free* Number of Jobs* CURSTG (memory)* PTF Monitor (MonitorAS400ptf.xml)* Message Count (number of messages)
### Languages Used: * Java

