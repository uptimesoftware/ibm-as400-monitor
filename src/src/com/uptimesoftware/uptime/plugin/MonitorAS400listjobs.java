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
public class MonitorAS400listjobs extends Plugin {

    public MonitorAS400listjobs(PluginWrapper wrapper) {
        super(wrapper);
    }

	@Extension
    public static class UptimeMonitorAS400 extends PluginMonitor {
	
		static int metricValue = new Integer(0);
		private String username = "";
		private String password = "";
		private String hostname = "";
		private String message = "";

		@Override
		public void setParameters(Parameters params) {
			username = params.getString("username");
			password = params.getString("password");
			hostname = params.getString("hostname");
			//hostname = getHostname();

		}

		@Override
		public void monitor() {
			try {
				AS400 as400 = new AS400(hostname);
				as400.setUserId(username);
				as400.setPassword(password);

				as400.disconnectAllServices();
				
				setMessage(message);
				setState(MonitorState.OK);

			} catch (Exception e) {

				setMessage("MonitorAS400 failed \r" + e.getMessage());
				setState(MonitorState.CRIT);
			}
		}
	}
}
