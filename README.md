# Generic SNMP
## Tags : plugin   snmp   networking  

## Category: plugin

##Version Compatibility<br/>Module Name</th><th>up.time Monitoring Station Version</th>


  
    * Generic SNMP 1.0 - 7.2
  


### Description: This plug-in allows user to monitor any SNMP OID without having to add the MIB's to up.time. It also allows users to include filters so only certain data is included/excluded.

### Supported Monitoring Stations: 7.2
### Supported Agents: None; no agent required
### Installation Notes: <p><a href="https://github.com/uptimesoftware/uptime-plugin-manager">Install using the up.time Plugin Manager</a></p>

### Dependencies: <p>n/a</p>

### Input Variables: * SNMP Version - SNMP version (1/2/3)* SNMP Port - the port SNMP is listening on* SNMP Action - SNMP Walk/Get* SNMP Data Type - integer/string* SNMP OID - the SNMP OID to get. This is used for both SNMP Walk and Get.* SNMP Table Index OID (Walk) - specify the SNMP OID to use for the index of SNMP Walk data* SNMP Table Index Filter - if there is specific index that one wants to include, provide regex meeting the criteria.* Community String(v1/v2) - SNMP Community String for SNMP V1 or V2* Agent Username (v3)* Authentication Type (v3)* Authentication Passphrase (v3)* Privacy Type (v3)* Privacy Passphrase (v3)
### Output Variables: * Returned Data (Integer)* Returned Data (String)
### Languages Used: * PHP

