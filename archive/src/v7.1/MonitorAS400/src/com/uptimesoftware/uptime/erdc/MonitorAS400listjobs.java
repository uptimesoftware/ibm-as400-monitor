/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uptimesoftware.uptime.erdc;

import java.io.*;
import java.util.*;
import java.math.*;
import java.lang.Thread.*;
import com.ibm.as400.access.*;
import com.ibm.as400.resource.*;
import com.uptimesoftware.uptime.erdc.baseclass.*;
import com.uptimesoftware.uptime.base.util.Parameters;

/**
 *
 * @author chris
 */
public class MonitorAS400listjobs extends MonitorWithMonitorVariables {

    static int metricValue = new Integer(0);
    private String username = "";
    private String password = "";
    private String hostname = "";
    private String message = "";

    @Override
    public void setParameters(Parameters params, Long instanceId) {
        super.setParameters(params, instanceId);

        username = parameters.getStringParameter("username");
        password = parameters.getStringParameter("password");
        hostname = getHostname();

    }

    @Override
    protected void monitor() {
        try {
            AS400 as400 = new AS400(hostname);
            as400.setUserId(username);
            as400.setPassword(password);

            as400.disconnectAllServices();
            
            setMessage(message);
            setState(ErdcTransientState.OK);

        } catch (Exception e) {

            setMessage("MonitorAS400 failed \r" + e.getMessage());
            setState(ErdcTransientState.CRIT);
        }
    }
}
