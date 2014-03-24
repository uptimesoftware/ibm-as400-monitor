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
import com.uptimesoftware.uptime.erdc.baseclass.*;
import com.uptimesoftware.uptime.base.util.Parameters;

/**
 *
 * @author chris
 */
public class MonitorAS400asp extends MonitorWithMonitorVariables {

    private String username = "";
    private String password = "";
    private String hostname = "";
    private String check = "";
    private String message = "";

    @Override
    public void setParameters(Parameters params, Long instanceId) {
        super.setParameters(params, instanceId);

        username = parameters.getStringParameter("username");
        password = parameters.getStringParameter("password");
        hostname = getHostname();
        check = parameters.getStringParameter("check");

    }

    @Override
    protected void monitor() {
        AS400 as400 = new AS400(hostname);
        try {
            as400.setUserId(username);
            as400.setPassword(password);

            CommandCall command = new CommandCall(as400);
            String commandString = ("DRIFT/RTVASPPERC " + check);

            if (command.run(commandString)) {

                QSYSObjectPathName objectPathName = new QSYSObjectPathName("QGPL", "PERCUSE", "DTAARA");
                DecimalDataArea dataArea = new DecimalDataArea(as400, objectPathName.getPath());

                BigDecimal as400Data = dataArea.read();
                int aspIntValue = as400Data.intValue();
                dataArea.delete();

                message = check + " is " + as400Data;
                addVariable("asp", aspIntValue);
                setState(ErdcTransientState.OK);

            } else {
                message = "Failed to run command " + commandString;
                setState(ErdcTransientState.CRIT);
            }

            as400.disconnectAllServices();
            setMessage(message);
            
        } catch (Exception e) {
            setMessage("MonitorAS400 failed \r" + e.getMessage());
            setState(ErdcTransientState.CRIT);
        }
    }
}
