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
public class MonitorAS400ptf extends MonitorWithMonitorVariables {

    private String username = "";
    private String password = "";
    private String hostname = "";
    private String message = "";
    static String strMsgs = "CPIEF1D";

    @Override
    public void setParameters(Parameters params, Long instanceId) {
        super.setParameters(params, instanceId);

        username = parameters.getStringParameter("username");
        password = parameters.getStringParameter("password");
        hostname = getHostname();

    }

    protected void monitor() {
        {
            try {
                AS400 as400 = new AS400(hostname);
                as400.setUserId(username);
                as400.setPassword(password);
                RMessageQueue messageQueue = new RMessageQueue(as400, "/QSYS.LIB/QSYSOPR.MSGQ");
                Integer intSeverity = new Integer(40);
                messageQueue.setSelectionValue(RMessageQueue.SEVERITY_CRITERIA, intSeverity);
                messageQueue.open();
                messageQueue.waitForComplete();
                StringBuffer messageText = new StringBuffer();
                int returnStatus = 0;
                
                long numberOfMessages = messageQueue.getListLength();
                for (long i = 0; i < numberOfMessages; ++i) {
                    RQueuedMessage queuedMessage = (RQueuedMessage) messageQueue.resourceAt(i);
                    int messageInString = queuedMessage.toString().indexOf(strMsgs);
                    int messageInMessageText = messageText.toString().indexOf(queuedMessage.getAttributeValue(RQueuedMessage.MESSAGE_TEXT).toString());
                    if (messageInString > -1 && messageInMessageText == -1) {
                        if (messageInString > -1) {
                            messageText.append(queuedMessage.getAttributeValue(RQueuedMessage.MESSAGE_TEXT));
                            returnStatus = 1;
                        }
                    }
                }

                addVariable("messageCount", numberOfMessages);

                messageQueue.close();
                if (returnStatus == 1) {
                    message = "PTFs to install. \r" + messageText;
                    setState(ErdcTransientState.WARN);
                    setMessage(message);
                }
                if (returnStatus == 0) {
                    message = "No PTFs to install.";
                    setState(ErdcTransientState.OK);
                    setMessage(message);
                }
                
                as400.disconnectAllServices();
                
                
            } catch (Exception e) {
                setMessage("MonitorAS400ptf failed \r" + e.getMessage());
                setState(ErdcTransientState.CRIT);
            }
        }
    }
}
