package utils;

import log.LogManager;
import log.LogHandle;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: GB031042
 * Date: 02-Oct-2006
 * Time: 14:32:16
 * To change this template use File | Settings | File Templates.
 */
public class ReportCSV {

    // Install location
    private String _dmghome;
    // Print writer
    PrintWriter _reporter;


    public ReportCSV(String dmghome) {

        _dmghome = dmghome;
    }

    public void createReport() {

        // Create the filename
        GregorianCalendar cal=new GregorianCalendar();
        cal.setTime(new Date(System.currentTimeMillis()));

        StringBuffer currTime = new StringBuffer();

        if (Integer.toString(cal.get(Calendar.YEAR)).length()==1)
        {
            currTime.append("0");
        }
        currTime.append(Integer.toString(cal.get(Calendar.YEAR)));

        if (Integer.toString(cal.get(Calendar.MONTH)).length()==1)
        {
            currTime.append("0");
        }
        currTime.append(Integer.toString(cal.get(Calendar.MONTH)));

        if (Integer.toString(cal.get(Calendar.DAY_OF_MONTH)).length()==1)
        {
            currTime.append("0");
        }
        currTime.append(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));

        if (Integer.toString(cal.get(Calendar.HOUR_OF_DAY)).length()==1)
        {
            currTime.append("0");
        }
        currTime.append(Integer.toString(cal.get(Calendar.HOUR_OF_DAY)));

        if (Integer.toString(cal.get(Calendar.MINUTE)).length()==1)
        {
            currTime.append("0");
        }
        currTime.append(Integer.toString(cal.get(Calendar.MINUTE)));

        if (Integer.toString(cal.get(Calendar.SECOND)).length()==1)
        {
            currTime.append("0");
        }
        currTime.append(Integer.toString(cal.get(Calendar.SECOND)));

        try
        {
            _reporter = new PrintWriter(new BufferedWriter(new FileWriter(_dmghome +"log/report_"+currTime.toString()+".log",true)));
        }
        catch (IOException e)
        {
            LogManager.write("Report File Log      - Failed to create report file.", LogHandle.ALL,LogManager.MAJOR);
            //e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }


        // Create the time string for the log file
        currTime = new StringBuffer();

        if (Integer.toString(cal.get(Calendar.DAY_OF_MONTH)).length()==1)
        {
            currTime.append("0");
        }
        currTime.append(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
        currTime.append("/");

        if (Integer.toString(cal.get(Calendar.MONTH)).length()==1)
        {
            currTime.append("0");
        }
        currTime.append(Integer.toString(cal.get(Calendar.MONTH)));
        currTime.append("/");

        if (Integer.toString(cal.get(Calendar.YEAR)).length()==1)
        {
            currTime.append("0");
        }
        currTime.append(Integer.toString(cal.get(Calendar.YEAR)));
        currTime.append(" ");

        if (Integer.toString(cal.get(Calendar.HOUR_OF_DAY)).length()==1)
        {
            currTime.append("0");
        }
        currTime.append(Integer.toString(cal.get(Calendar.HOUR_OF_DAY)));
        currTime.append(":");

        if (Integer.toString(cal.get(Calendar.MINUTE)).length()==1)
        {
            currTime.append("0");
        }
        currTime.append(Integer.toString(cal.get(Calendar.MINUTE)));
        currTime.append(":");

        if (Integer.toString(cal.get(Calendar.SECOND)).length()==1)
        {
            currTime.append("0");
        }
        currTime.append(Integer.toString(cal.get(Calendar.SECOND)));

        String header = "--------------------------------------------------------------------------------------\n"+
                        "- Data mirror generator v1.0     -     Datamirroring started at: "+currTime.toString()+" -\n"+
                        "--------------------------------------------------------------------------------------\n";

        _reporter.println(header);
    }

    public void closeReport() {
        if (_reporter != null) {
            _reporter.close();
        }
    }

    public void write(String src, long srcFileSize, long srcFileTime, String dst, long dstFileSize, long dstFileTime, String action, String direction, String status) {

        // Time of the action, Source, Source size, Source timestamp, Destination, Destination size, Destination timestamp, Action, Direction, Status
        _reporter.println(System.currentTimeMillis()+","+src+","+srcFileSize+","+srcFileTime+","+dst+","+dstFileSize+","+dstFileTime+","+action+","+direction+","+status);
    }
}
