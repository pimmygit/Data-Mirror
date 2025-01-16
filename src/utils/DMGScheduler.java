package utils;

import java.util.*;
import javax.swing.*;
import gui.DMGOptionPane;
import log.LogManager;
import log.LogHandle;

/**
 * Created by IntelliJ IDEA.
 * User: Pimmy
 * Date: 04-Oct-2005
 * Time: 15:25:06
 * To change this template use File | Settings | File Templates.
 */
public class DMGScheduler implements Runnable
{
    //private final int NO_SCHEDULE = 0;
    private final int DAILY = 1;
    private final int WEEKLY = 2;
    private final int MONTHLY = 3;

    private boolean _run;
    private DMGProps _vars;
    private String _dmghome;
    public DMGGenerator _dataMirror;

    private Thread scheduler = null;

    public DMGScheduler(String dmghome, DMGGenerator dataMirror)
    {
        _run = false;
        _dmghome = dmghome;
        _vars = new DMGProps();
        _vars.getProps(_dmghome);
        _dataMirror = dataMirror;
    }

    public void startScheduler()
    {
        _vars.getProps(_dmghome);

        if (scheduler == null)
        {
            scheduler = new Thread(this);
            scheduler.start();
        }
    }

    public void stopScheduler()
    {
        _run = false;
        try{Thread.sleep(1000);}catch(Exception e){
            LogManager.write("Datamirror scheduler - Timer Exception.", LogHandle.ALL,LogManager.DEBUG);
        }
        scheduler = null;
    }

    public void run()
    {
        _run = true;

        LogManager.write("Datamirror scheduler - Starting Scheduler.", LogHandle.ALL,LogManager.DEBUG);

        GregorianCalendar cal=new GregorianCalendar();

        String[] days={"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
        String[] timeIntervals = {  "00:00", "00:15", "00:30", "00:45",
                                    "01:00", "01:15", "01:30", "01:45",
                                    "02:00", "02:15", "02:30", "02:45",
                                    "03:00", "03:15", "03:30", "03:45",
                                    "04:00", "04:15", "04:30", "04:45",
                                    "05:00", "05:15", "05:30", "05:45",
                                    "06:00", "06:15", "06:30", "06:45",
                                    "07:00", "07:15", "07:30", "07:45",
                                    "08:00", "08:15", "08:30", "08:45",
                                    "09:00", "09:15", "09:30", "09:45",
                                    "10:00", "10:15", "10:30", "10:45",
                                    "11:00", "11:15", "11:30", "11:45",
                                    "12:00", "12:15", "12:30", "12:45",
                                    "13:00", "13:15", "13:30", "13:45",
                                    "14:00", "14:15", "14:30", "14:45",
                                    "15:00", "15:15", "15:30", "15:45",
                                    "16:00", "16:15", "16:30", "16:45",
                                    "17:00", "17:15", "17:30", "17:45",
                                    "18:00", "18:15", "18:30", "18:45",
                                    "19:00", "19:15", "19:30", "19:45",
                                    "20:00", "20:15", "20:30", "20:45",
                                    "21:00", "21:15", "21:30", "21:45",
                                    "22:00", "22:15", "22:30", "22:45",
                                    "23:00", "23:15", "23:30", "23:45", };

        String runAtTime = timeIntervals[_vars.getRunAtTime()];
        String runWeekDay = days[_vars.getWeekDay()];
        String runMonthDay = _vars.getMonthDay();

        // Determin the log format
        if (_vars.getSchedulePeriod() == DAILY)
        {
            LogManager.write("Datamirror scheduler - Run every day at [" +runAtTime+ "].", LogHandle.ALL,LogManager.DEBUG);
        }
        else if (_vars.getSchedulePeriod() == WEEKLY)
        {
            LogManager.write("Datamirror scheduler - Run weekly on every [" +runWeekDay+ "] at [" +runAtTime+ "].", LogHandle.ALL,LogManager.DEBUG);
        }
        else if (_vars.getSchedulePeriod() == MONTHLY)
        {
            LogManager.write("Datamirror scheduler - Run monthly on every [" +runMonthDay+ "] at [" +runAtTime+ "].", LogHandle.ALL,LogManager.DEBUG);
        }
        else
        {
            LogManager.write("Datamirror scheduler - No Scheduling defined: Month/Week/Time - [" +runMonthDay+ "]/[" +runWeekDay+ "]/[" +runAtTime+ "].", LogHandle.ALL,LogManager.DEBUG);
        }

        String nowTime;
        String nowWeekDay;
        String nowMonthDay;

        while (_run)
        {
            cal.setTime(new Date(System.currentTimeMillis()));

            // Create the timestamp
            StringBuffer currTime = new StringBuffer();
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

            nowTime = currTime.toString();
            nowWeekDay = days[cal.get(Calendar.DAY_OF_WEEK)-1];
            nowMonthDay = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
            if (nowMonthDay.length() == 1)
            {
                nowMonthDay = "0"+nowMonthDay;
            }

            //LogManager.write("Datamirror generator - Time now: ["+nowTime+" "+nowWeekDay+" "+nowMonthDay+"].", LogHandle.ALL,LogManager.DEBUG);
            //LogManager.write("Datamirror generator - Schedule: ["+runAtTime+" "+runWeekDay+" "+runMonthDay+"].", LogHandle.ALL,LogManager.DEBUG);

            switch(_vars.getSchedulePeriod())
            {
                case 1:
                    if (nowTime.equalsIgnoreCase(runAtTime))
                    {
                        LogManager.write("Datamirror scheduler - Starting daily mirroring: ["+runAtTime+"].", LogHandle.ALL,LogManager.DEBUG);
                        startMirroring();
                    }
                    break;
                case 2:
                    if (nowWeekDay.equalsIgnoreCase(runWeekDay) && nowTime.equalsIgnoreCase(runAtTime))
                    {
                        LogManager.write("Datamirror scheduler - Starting weekly mirroring: ["+runAtTime+" "+runWeekDay+"].", LogHandle.ALL,LogManager.DEBUG);
                        startMirroring();
                    }
                    break;
                case 3:
                    if (runMonthDay.contains(nowMonthDay.subSequence(0, nowMonthDay.length())) && nowTime.equalsIgnoreCase(runAtTime))
                    {
                        LogManager.write("Datamirror scheduler - Starting monthly mirroring: ["+runAtTime+" "+runMonthDay+"].", LogHandle.ALL,LogManager.DEBUG);
                        startMirroring();
                    }
                    break;
                default:
                    //LogManager.write("Datamirror generator - No scheduling defined.", LogHandle.ALL,LogManager.DEBUG);
                    break;
            }
            sleepAminute();
        }
        LogManager.write("Datamirror scheduler - Scheduler stopped.", LogHandle.ALL,LogManager.DEBUG);
    }

    private void sleepAminute()
    {
        for (int sec=0; sec<60; sec++)
        {
            if (_run)
            {
                try{Thread.sleep(1000);}catch(Exception e){
                    LogManager.write("Datamirror scheduler - Timer sleep exception.", LogHandle.ALL,LogManager.DEBUG);
                }
            } else {
                return;
            }
        }
    }

    private void startMirroring()
    {
        _vars.getProps(_dmghome);
        String source = _vars.getSourceDir();
        String destination = _vars.getDestinationDir();

        int action = _vars.getAction();
        int actionDate = _vars.getActionDate();
        int actionSize = _vars.getActionSize();

        int modifyOption = _vars.getModifyDir();
        int dateModifyOption = _vars.getDateModifyDir();
        int sizeModifyOption = _vars.getSizeModifyDir();

        int userChoice = 0;

        // Define the user message depending on the settings
        if (action == 0)
        {
            if (modifyOption == 0)
            {
                userChoice = DMGOptionPane.showConfirmDialog(new JFrame(), "Datamirror is about to start copying all missing files\n" +
                        "from:    '" +source+ "'\n" +
                        "to:          '" +destination+ "'\n\n" +
                        "Do you wish to continue?\n", " Datamirror generator.  ", DMGOptionPane.YES_NO_OPTION);
            }
            else if (modifyOption == 1)
            {
                userChoice = DMGOptionPane.showConfirmDialog(new JFrame(), "Datamirror is about to start copying all missing files\n" +
                        "from:    '" +destination+ "'\n" +
                        "to:          '" +source+ "'\n\n" +
                        "Do you wish to continue?\n", " Datamirror generator.  ", DMGOptionPane.YES_NO_OPTION);

            }
            else if (modifyOption == 2)
            {
                userChoice = DMGOptionPane.showConfirmDialog(new JFrame(), "Datamirror is about to start equalizing all files in\n" +
                        "          - '" +source+ "'\n" +
                        "          - '" +destination+ "'\n\n" +
                        "All duplicate files will be skipped.\n\n" +
                        "Do you wish to continue?\n", " Datamirror generator.  ", DMGOptionPane.YES_NO_OPTION);
            }
        }
        else if (action == 1)
        {
            if (actionDate == 0)
            {
                userChoice = DMGOptionPane.showConfirmDialog(new JFrame(), "Datamirror is about to start equalizing all files in\n" +
                        "          - '" +source+ "'\n" +
                        "          - '" +destination+ "'\n\n" +
                        "Older files will be overwritten by Newer.\n\n" +
                        "Do you wish to continue?\n", " Datamirror generator.  ", DMGOptionPane.YES_NO_OPTION);
            }
            else if (actionDate == 1)
            {
                userChoice = DMGOptionPane.showConfirmDialog(new JFrame(), "Datamirror is about to start equalizing all files in\n" +
                        "          - '" +source+ "'\n" +
                        "          - '" +destination+ "'\n\n" +
                        "Newer files will be overwritten by Older.\n\n" +
                        "Do you wish to continue?\n", " Datamirror generator.  ", DMGOptionPane.YES_NO_OPTION);
            }
            else if (actionDate == 2)
            {
                if (dateModifyOption == 0)
                {
                userChoice = DMGOptionPane.showConfirmDialog(new JFrame(), "Datamirror is about to start equalizing all files in\n" +
                        "        Src: '" +source+ "'\n" +
                        "        Dst: '" +destination+ "'\n\n" +
                        "Depending on condition, Src files will be overwritten.\n\n" +
                        "Do you wish to continue?\n", " Datamirror generator.  ", DMGOptionPane.YES_NO_OPTION);
                }
                else if (dateModifyOption == 1)
                {
                userChoice = DMGOptionPane.showConfirmDialog(new JFrame(), "Datamirror is about to start equalizing all files in\n" +
                        "        Src: '" +source+ "'\n" +
                        "        Dst: '" +destination+ "'\n\n" +
                        "Depending on condition, Dst files will be overwritten.\n\n" +
                        "Do you wish to continue?\n", " Datamirror generator.  ", DMGOptionPane.YES_NO_OPTION);
                }
                else if (dateModifyOption == 2)
                {
                userChoice = DMGOptionPane.showConfirmDialog(new JFrame(), "Datamirror is about to start equalizing all files in\n" +
                        "        Src: '" +source+ "'\n" +
                        "        Dst: '" +destination+ "'\n\n" +
                        "Depending on condition, Src files will be deleted.\n\n" +
                        "Do you wish to continue?\n", " Datamirror generator.  ", DMGOptionPane.YES_NO_OPTION);
                }
                else if (dateModifyOption == 3)
                {
                userChoice = DMGOptionPane.showConfirmDialog(new JFrame(), "Datamirror is about to start equalizing all files in\n" +
                        "        Src: '" +source+ "'\n" +
                        "        Dst: '" +destination+ "'\n\n" +
                        "Depending on condition, Dst files will be deleted.\n\n" +
                        "Do you wish to continue?\n", " Datamirror generator.  ", DMGOptionPane.YES_NO_OPTION);
                }
                else if (dateModifyOption == 4)
                {
                userChoice = DMGOptionPane.showConfirmDialog(new JFrame(), "Datamirror is about to start equalizing all files in\n" +
                        "        Src: '" +source+ "'\n" +
                        "        Dst: '" +destination+ "'\n\n" +
                        "Depending on condition, Src and Dst files will be deleted.\n\n" +
                        "Do you wish to continue?\n", " Datamirror generator.  ", DMGOptionPane.YES_NO_OPTION);
                }
            }
        }
        else if (action == 2)
        {
            if (actionSize == 0)
            {
                userChoice = DMGOptionPane.showConfirmDialog(new JFrame(), "Datamirror is about to start equalizing all files in\n" +
                        "          - '" +source+ "'\n" +
                        "          - '" +destination+ "'\n\n" +
                        "Smaller files will be overwritten by Bigger.\n\n" +
                        "Do you wish to continue?\n", " Datamirror generator.  ", DMGOptionPane.YES_NO_OPTION);
            }
            else if (actionSize == 1)
            {
                userChoice = DMGOptionPane.showConfirmDialog(new JFrame(), "Datamirror is about to start equalizing all files in\n" +
                        "          - '" +source+ "'\n" +
                        "          - '" +destination+ "'\n\n" +
                        "Bigger files will be overwritten by Smaller.\n\n" +
                        "Do you wish to continue?\n", " Datamirror generator.  ", DMGOptionPane.YES_NO_OPTION);
            }
            else if (actionSize == 2)
            {
                if (sizeModifyOption == 0)
                {
                userChoice = DMGOptionPane.showConfirmDialog(new JFrame(), "Datamirror is about to start equalizing all files in\n" +
                        "        Src: '" +source+ "'\n" +
                        "        Dst: '" +destination+ "'\n\n" +
                        "Depending on condition, Src files will be overwritten.\n\n" +
                        "Do you wish to continue?\n", " Datamirror generator.  ", DMGOptionPane.YES_NO_OPTION);
                }
                else if (sizeModifyOption == 1)
                {
                userChoice = DMGOptionPane.showConfirmDialog(new JFrame(), "Datamirror is about to start equalizing all files in\n" +
                        "        Src: '" +source+ "'\n" +
                        "        Dst: '" +destination+ "'\n\n" +
                        "Depending on condition, Dst files will be overwritten.\n\n" +
                        "Do you wish to continue?\n", " Datamirror generator.  ", DMGOptionPane.YES_NO_OPTION);
                }
                else if (sizeModifyOption == 2)
                {
                userChoice = DMGOptionPane.showConfirmDialog(new JFrame(), "Datamirror is about to start equalizing all files in\n" +
                        "        Src: '" +source+ "'\n" +
                        "        Dst: '" +destination+ "'\n\n" +
                        "Depending on condition, Src files will be deleted.\n\n" +
                        "Do you wish to continue?\n", " Datamirror generator.  ", DMGOptionPane.YES_NO_OPTION);
                }
                else if (sizeModifyOption == 3)
                {
                userChoice = DMGOptionPane.showConfirmDialog(new JFrame(), "Datamirror is about to start equalizing all files in\n" +
                        "        Src: '" +source+ "'\n" +
                        "        Dst: '" +destination+ "'\n\n" +
                        "Depending on condition, Dst files will be deleted.\n\n" +
                        "Do you wish to continue?\n", " Datamirror generator.  ", DMGOptionPane.YES_NO_OPTION);
                }
                else if (sizeModifyOption == 4)
                {
                userChoice = DMGOptionPane.showConfirmDialog(new JFrame(), "Datamirror is about to start equalizing all files in\n" +
                        "        Src: '" +source+ "'\n" +
                        "        Dst: '" +destination+ "'\n\n" +
                        "Depending on condition, Src and Dst files will be deleted.\n\n" +
                        "Do you wish to continue?\n", " Datamirror generator.  ", DMGOptionPane.YES_NO_OPTION);
                }
            }
        }

        if (userChoice == DMGOptionPane.YES_OPTION)
        {
            //DMGOptionPane.showMessageDialog(new JFrame(), "You selected YES.", "  Datamirror generator.  ", DMGOptionPane.INFORMATION_MESSAGE);
            LogManager.write("Datamirror scheduler - Starting datamirroring.", LogHandle.ALL,LogManager.DEBUG);
            _dataMirror.startDMG(new JButton());
        }
    }

    public boolean isRunning()
    {
        return _run;
    }

    public static void main(String[] args)
    {

    }
}
