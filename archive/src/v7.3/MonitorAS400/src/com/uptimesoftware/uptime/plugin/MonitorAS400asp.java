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
public class MonitorAS400asp extends Plugin {

    public MonitorAS400asp(PluginWrapper wrapper) {
        super(wrapper);
    }
	
	@Extension
	public static class UptimeMonitorAS400 extends PluginMonitor {

		private String username = "";
		private String password = "";
		private String hostname = "";
		private String check = "";
		private String message = "";

		@Override
		public void setParameters(Parameters params) {

			username = params.getString("username");
			password = params.getString("password");
			//hostname = getHostname();
			hostname = params.getString("hostname");
			check = params.getString("check");

		}

		@Override
		public void monitor() {
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
					setState(MonitorState.OK);

				} else {
					message = "Failed to run command " + commandString;
					setState(MonitorState.CRIT);
				}

				as400.disconnectAllServices();
				setMessage(message);
				
			} catch (Exception e) {
				setMessage("MonitorAS400 failed \r" + e.getMessage());
				setState(MonitorState.CRIT);
			}
		}
	}
}
