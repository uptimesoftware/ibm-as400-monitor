/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uptimesoftware.uptime.erdc;

import java.io.*;
import java.util.*;
import java.lang.Thread.*;
import com.ibm.as400.access.*;
import com.uptimesoftware.uptime.erdc.baseclass.*;
import com.uptimesoftware.uptime.base.util.Parameters;

/**
 *
 * @author chris
 */
public class MonitorAS400jobq extends Monitor {

    private String username = "";
    private String password = "";
    private String hostname = "";
    private String message = "";
    private String queueFile = "";
    private int checkPos = 0;
    private int numjobInQOK = 0;
    private int numjobInQNOK = 0;
    private int numjobQOK = 0;
    private int numjobQNOK = 0;
    private String strOutTxtJobQ = "";
    private String strOutTxtInQ = "";
    ;

    @Override
    public void setParameters(Parameters params, Long instanceId) {
        super.setParameters(params, instanceId);

        username = parameters.getStringParameter("username");
        password = parameters.getStringParameter("password");
        queueFile = parameters.getStringParameter("queueFile");
        hostname = getHostname();

    }

    @Override
    protected void monitor() {

        try {
            AS400 as400 = new AS400(hostname);
            as400.setUserId(username);
            as400.setPassword(password);

            String systemName = hostname;
            String jobQFileName = queueFile;
            String strjobQLib = new String("          ");//null;
            String strjobQ = new String("          ");//null;
            String strjobQSts = null;
            String strjobQWarning = null;
            String strjobQCritical = null;
            int fileend = jobQFileName.lastIndexOf(".");
            int filestart = jobQFileName.lastIndexOf("/");
            String strsystemName = jobQFileName.substring(filestart + 1, fileend);

            QSYSObjectPathName programName = new QSYSObjectPathName("QSYS", "QSPRJOBQ", "PGM");

            ProgramCall getSystemStatus = new ProgramCall(as400);
            ProgramParameter[] parmlist = new ProgramParameter[5];
            parmlist[0] = new ProgramParameter(64);
            AS400Bin4 bin4 = new AS400Bin4();
            Integer iStatusLength = new Integer(64);
            byte[] statusLength = bin4.toBytes(iStatusLength);
            parmlist[1] = new ProgramParameter(statusLength);
            AS400Text text1 = new AS400Text(8, as400);
            byte[] jobQFormat = text1.toBytes("JOBQ0100");
            parmlist[2] = new ProgramParameter(jobQFormat);

            List<String> queues = getQueueList(queueFile);

            for (String queue : queues) {
                String[] splitJobq = queue.split("/");

                strjobQLib = splitJobq[0];
                strjobQ = splitJobq[1];
                strjobQSts = splitJobq[2];
                String strjobQWarn = splitJobq[3];
                String strjobQCrit = splitJobq[4];

                int intjobQWarn = Integer.parseInt(strjobQWarn);
                int intjobQCrit = Integer.parseInt(strjobQCrit);
                AS400Text text3 = new AS400Text(20, as400);
                byte[] qualjobName = text3.toBytes(strjobQ.concat("          ").substring(0, 10) +
                        strjobQLib.concat("          ").substring(0, 10));
                parmlist[3] = new ProgramParameter(qualjobName);
                byte[] errorInfo = new byte[32];
                parmlist[4] = new ProgramParameter(errorInfo, 0);
                getSystemStatus.setProgram(programName.getPath(), parmlist);

                getSystemStatus.run();
                Thread.sleep(5000);

                if (getSystemStatus.run() != true) {

                    AS400Message[] msgList = getSystemStatus.getMessageList();
                    message = "The program did not run";
                    for (int i = 0; i < msgList.length; i++) {
                        message += msgList[i].getText() + "\r";
                    }
                    setState(ErdcTransientState.UNKNOWN);
                    return;
                } else {

                    AS400Bin4 as400Int = new AS400Bin4();
                    AS400Text as400Text = new AS400Text(10);

                    byte[] as400Data = parmlist[0].getOutputData();
                    Integer numOfJobsInQ = new Integer(0);
                    String stsOfJobQ = new String();

                    numOfJobsInQ = (Integer) as400Int.toObject(as400Data, 48);
                    int intOfJobsInQ = numOfJobsInQ.intValue();

                    stsOfJobQ = (String) as400Text.toObject(as400Data, 52);

                    if (intjobQWarn == 0 && intjobQCrit == 0) //If no jobs should be checked. 0=no check!
                    {
                        if (strjobQSts.trim().equalsIgnoreCase(stsOfJobQ.trim())) {
                            ++numjobQOK;
                        } else {
                            ++numjobQNOK;
                            strOutTxtJobQ += strjobQLib + "/" + strjobQ;
                        }
                    } else {
                        if (strjobQSts.trim().equalsIgnoreCase(stsOfJobQ.trim())) {
                            ++numjobQOK;
                            if (intOfJobsInQ >= intjobQWarn && intOfJobsInQ < intjobQCrit) {
                                ++numjobInQNOK;
                                strOutTxtInQ += "WARNING - " + strjobQLib + "/" + strjobQ + " " + numOfJobsInQ + " job(s) \r";

                            } else if (intOfJobsInQ > intjobQWarn && intOfJobsInQ >= intjobQCrit) {
                                ++numjobInQNOK;
                                strOutTxtInQ += "CRITICAL - " + strjobQLib + "/" + strjobQ + " " + numOfJobsInQ + " job(s) \r";
                            } else if (intOfJobsInQ < intjobQWarn && intOfJobsInQ < intjobQCrit) {
                                ++numjobInQOK;
                            }
                        } else //JobQ Status NOT OK
                        {
                            ++numjobQNOK;
                            strOutTxtJobQ += strjobQLib + "/" + strjobQ + ".";
                            if (intOfJobsInQ >= intjobQWarn && intOfJobsInQ < intjobQCrit) {
                                ++numjobInQNOK;
                                strOutTxtInQ += "WARNING - " + strjobQLib + "/" + strjobQ + " " + numOfJobsInQ + " job(s) \r";

                            } else if (intOfJobsInQ > intjobQWarn && intOfJobsInQ >= intjobQCrit) {
                                ++numjobInQNOK;
                                strOutTxtInQ += "CRITICAL - " + strjobQLib + "/" + strjobQ + " " + numOfJobsInQ + " job(s) \r";

                            } else if (intOfJobsInQ < intjobQWarn && intOfJobsInQ < intjobQCrit) {
                                ++numjobInQOK;
                            }
                        }
                    }
                }
            }

            if (numjobQOK == 0 && numjobInQOK == 0) //If no jobq and no jobs are OK
            {
                setMessage("CRITICAL - Jobq(s) with wrong status: " + strOutTxtJobQ + " Too many jobs in jobq(s): " + strOutTxtInQ);
                setState(ErdcTransientState.CRIT);
            } else if (numjobQOK > 0 && numjobInQOK > 0 && numjobQNOK > 0 && numjobInQNOK > 0) //If some jobq and jobs are ok
            {
                setMessage("WARNING - Jobq(s) with wrong status: " + strOutTxtJobQ + " Too many jobs in jobq(s): " + strOutTxtInQ);
                setState(ErdcTransientState.WARN);
            } else if (numjobQNOK > 0 && numjobInQNOK == 0) //If some jobq not OK and all jobs ok
            {
                setMessage("WARNING - Jobq(s) with wrong status: " + strOutTxtJobQ + " "); // Too many jobs in jobq: " + strOutTxtInJobQ);
                setState(ErdcTransientState.WARN);
            } else if (numjobQNOK > 0 && numjobInQNOK > 0) //If jobq not OK and jobs not OK
            {
                setMessage("WARNING - Wrong status in jobq: " + strOutTxtJobQ + " Too many jobs in jobq(s): " + strOutTxtInQ);
                setState(ErdcTransientState.WARN);
            } else if (numjobQNOK == 0 && numjobInQNOK > 0) //if all jobq OK but all jobs not OK
            {
                setMessage("WARNING - Too many jobs in jobq(s): " + strOutTxtInQ);
                setState(ErdcTransientState.WARN);
            } else if (numjobQNOK == 0 && numjobInQNOK == 0) //If jobq and jobs OK
            {
                setMessage("OK - All jobq's in right status with number of jobs within limits.");
                setState(ErdcTransientState.OK);
            } else {
                setMessage("UNKNOWN - Monitor failed to complete task.");
                setState(ErdcTransientState.UNKNOWN);
            }

            as400.disconnectAllServices();

        } catch (Exception e) {
            setState(ErdcTransientState.UNKNOWN);
            setMessage("An exception occured running monitor. \r" + e.getMessage());
        }
    }

    public List<String> getQueueList(String fileName) {
        File file = new File(fileName);
        List<String> queueList = new ArrayList<String>();

        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                queueList.add(line);
            }
        } catch (IOException e) {
            System.err.println("Problem, class fileReader: " + e);
        }
        return queueList;
    }
}
