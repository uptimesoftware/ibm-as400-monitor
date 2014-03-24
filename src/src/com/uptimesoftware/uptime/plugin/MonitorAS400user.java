/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uptimesoftware.uptime.plugin;

import java.io.*;
import java.util.*;
import java.math.*;
import java.lang.Thread.*;
import com.ibm.as400.access.*;
import com.ibm.as400.resource.*;
import ro.fortsoft.pf4j.PluginWrapper;
import com.uptimesoftware.uptime.plugin.api.Extension;
import com.uptimesoftware.uptime.plugin.api.Plugin;
import com.uptimesoftware.uptime.plugin.api.PluginMonitor;
import com.uptimesoftware.uptime.plugin.monitor.MonitorState;
import com.uptimesoftware.uptime.plugin.monitor.Parameters;
/*
import com.uptimesoftware.uptime.erdc.baseclass.*;
import com.uptimesoftware.uptime.base.util.Parameters;
*/
/**
 *
 * @author chris
 */
public class MonitorAS400user extends Plugin {

    public MonitorAS400user(PluginWrapper wrapper) {
        super(wrapper);
    }

	@Extension
    public static class UptimeMonitorAS400 extends PluginMonitor {
	
		private String username = "";
		private String password = "";
		private String hostname = "";
		private String message = "";

		@Override
		public void setParameters(Parameters params) {
			username = params.getString("username");
			password = params.getString("password");
			hostname = params.getString("hostname");
		}

		@Override
        public void monitor() {

			int returnStatus = 0;
			String stringMessages = "CPF1393";
			
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
				long numberOfMessages = messageQueue.getListLength();
				
				for (long i = 0; i < numberOfMessages; ++i) {
					RQueuedMessage queuedMessage = (RQueuedMessage) messageQueue.resourceAt(i);
					int messageInString = queuedMessage.toString().indexOf(stringMessages);
					int messageInMessageText = messageText.toString().indexOf(queuedMessage.getAttributeValue(RQueuedMessage.MESSAGE_TEXT).toString());

					if (messageInString > -1 && messageInMessageText == -1) {
						if (messageInString > -1) {
							messageText.append(queuedMessage.getAttributeValue(RQueuedMessage.MESSAGE_TEXT));
							
							message = "User(s) disabled. \r" + messageText;
							setState(MonitorState.WARN);
						}
					}
				}

				messageQueue.close();
				
				if (returnStatus == 0) {
					
					message = "All users active";
					setState(MonitorState.OK);
				}
				
				as400.disconnectAllServices();
				setMessage(message);

			} catch (Exception e) {

				setMessage("MonitorAS400user failed \r" + e.getMessage());
				setState(MonitorState.CRIT);
			}
		}
    }
}
