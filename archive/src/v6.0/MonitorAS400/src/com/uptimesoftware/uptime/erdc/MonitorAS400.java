/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uptimesoftware.uptime.erdc;

import java.util.*;
import java.lang.Thread.*;
import com.ibm.as400.access.*;
import com.uptimesoftware.uptime.erdc.baseclass.*;
import com.uptimesoftware.uptime.base.util.Parameters;

/**
 *
 * @author chris
 */
public class MonitorAS400 extends MonitorWithMonitorVariables {

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
        int checkPos = 0;

        try {
            AS400 as400 = new AS400(hostname);
            as400.setUserId(username);
            as400.setPassword(password);

            QSYSObjectPathName programName = new QSYSObjectPathName("QSYS", "QWCRSSTS", "PGM");

            ProgramCall getSystemStatus = new ProgramCall(as400);
            ProgramParameter[] parameterList = new ProgramParameter[5];

            parameterList[0] = new ProgramParameter(64);

            AS400Bin4 as400Bin4 = new AS400Bin4();
            Integer intStatusLength = new Integer(64);
            byte[] statusLength = as400Bin4.toBytes(intStatusLength);
            parameterList[1] = new ProgramParameter(statusLength);

            AS400Text as400Text = new AS400Text(8, as400);
            byte[] statusFormat = as400Text.toBytes("SSTS0200");
            parameterList[2] = new ProgramParameter(statusFormat);

            AS400Text as400Text3 = new AS400Text(10, as400);
            byte[] resetStats = as400Text3.toBytes("*NO       ");
            parameterList[3] = new ProgramParameter(resetStats);
            byte[] errorInfo = new byte[32];
            parameterList[4] = new ProgramParameter(errorInfo, 0);

            getSystemStatus.setProgram(programName.getPath(), parameterList);

            getSystemStatus.run();
            Thread.sleep(5000);

            if (getSystemStatus.run() != true) {

                AS400Message[] msgList = getSystemStatus.getMessageList();
                message = "Unable to run program";

                for (int i = 0; i < msgList.length; i++) {
                    message += "\r" + msgList[i].getText();
                }

                setMessage(message);
                setState(ErdcTransientState.CRIT);

            } 
            else {

                AS400Bin4 as400Int = new AS400Bin4();

                byte[] as400Data = parameterList[0].getOutputData();

                ArrayList<String> checks = new ArrayList<String>();
                checks.add("CPU");
                checks.add("DISK");
                checks.add("JOBS");
                checks.add("CURSTG");

                for (String check : checks) {
                    if (check.equalsIgnoreCase("CPU")) {
                        checkPos = 32;
                    } else if (check.equalsIgnoreCase("DISK")) {
                        checkPos = 52;
                    } else if (check.equalsIgnoreCase("JOBS")) {
                        checkPos = 36;
                    } else if (check.equalsIgnoreCase("CURSTG")) {
                        checkPos = 60;
                    }

                    metricValue = (Integer) as400Int.toObject(as400Data, checkPos);

                    if (check.equalsIgnoreCase("CPU")) {
                        metricValue = metricValue / 10;
                        addVariable(check.toLowerCase(), metricValue);
                        message += check.toLowerCase() + ": " + metricValue + "% ";
                        
                    } else if (check.equalsIgnoreCase("DISK")) {
                        metricValue = metricValue / 10000;
                        addVariable(check.toLowerCase(), metricValue);                        
                        message += check.toLowerCase() + ": " + metricValue + "% ";
                        
                    } else {
                        addVariable(check.toLowerCase(), metricValue);
                        message += check.toLowerCase() + ": " + metricValue + " ";
                    }   
                }
            }
            as400.disconnectAllServices();
            
            setMessage(message);
            setState(ErdcTransientState.OK);

        } catch (Exception e) {

            setMessage("MonitorAS400 failed \r" + e.getMessage());
            setState(ErdcTransientState.CRIT);
        }
    }
}
