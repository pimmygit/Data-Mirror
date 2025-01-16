package utils;

import log.LogManager;
import log.LogHandle;

import java.util.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.FileOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Pimmy
 * Date: 29-Sep-2005
 * Time: 17:10:48
 * To change this template use File | Settings | File Templates.
 */
public class DMGProps
{
    //private final int MODIFY_SRC = 0;
    private final int MODIFY_DST = 1;
    //private final int MODIFY_BOTH = 2;

    private final int NO_ACTION = 0;
    //private final int DECIDE_BY_DATE = 1;
    //private final int DECIDE_BY_SIZE = 2;

    private final int KEEP_NEWEST = 0;
    //private final int KEEP_OLDEST = 1;
    //private final int DATE_CUSTOM = 2;

    private final int KEEP_BIGGEST = 0;
    //private final int KEEP_SMALLEST = 1;
    //private final int SIZE_CUSTOM = 2;

    private final int NO_SCHEDULE = 0;
    //private final int DAILY = 1;
    //private final int WEEKLY = 2;
    //private final int MONTHLY = 3;

    private final int VALUE_SRC = 0;
    private final int VALUE_DST = 1;

    private final int COND_DATE_OLDER = 0;
    //private final int COND_DATE_NEWER = 1;
    //private final int COND_DATE_SAME = 2;
    //private final int COND_DATE_DIFF = 3;

    private final int COND_SIZE_BIGGER = 0;
    //private final int COND_SIZE_BIG_EQ = 1;
    //private final int COND_SIZE_SMALLER = 2;
    //private final int COND_SIZE_SMALL_EQ = 3;
    //private final int COND_SIZE_DIFF = 4;
    //private final int COND_SIZE_EQUAL = 5;

    private final int SUNDAY = 0;
    //private final int MONDAY = 1;
    //private final int TUESDAY = 2;
    //private final int WEDNESDAY = 3;
    //private final int THURSDAY = 4;
    //private final int FRIDAY = 5;
    //private final int SATURDAY = 6;

    //private final int INFO = 0;
    private final int UPDATE = 1;
    //private final int ERROR = 2;

    // Locations of the Source and Destination
    private String _src;
    private String _dst;

    // Choose to which directory files to be added.
    // Choices: SRC, DST or BOTH
    private int _modifyDir;

    // Define if the program should recurse into subfolders
    private boolean _recurse;

    // Choose type of action for duplicate files
    // Choices: NO ACTION, DECIDE BY DATE, DECIDE BY SIZE
    private int _action;

    // Choose type of actions
    // Choices: Keep Oldest, Keep Newest, Custom
    private int _actionDate;
    // Choices: Keep Biggest, Keep Smallest, Custom
    private int _actionSize;

    // Custom action for Date
    private int _dateVal_1;
    private int _dateCond;
    private int _dateVal_2;
    private int _dateDest;
    private long _dateSet = 0;

    // Custom action for Size
    private int _sizeVal_1;
    private int _sizeCond;
    private int _sizeVal_2;
    private int _sizeDest;
    private String _fileSize;

    // Scheduling
    private int _schedulePeriod;
    private int _weekDay;
    private String _monthDay;
    private int _runAtTime;

    // Reporting
    private int _reportLevel;
    private boolean _reportToFile;


    public DMGProps()
    {
        setDefaultProperties();
    }

    public boolean getProps(String dmghome)
    {
        LogManager.write("DMG Props Manager    - Loading properties from: " + dmghome + "props/conf.prp", LogHandle.ALL,LogManager.DEBUG);

        Properties bkpProps = new Properties();
        try
        {
            FileInputStream fStream = new FileInputStream(dmghome +"props/conf.prp");
            bkpProps.load(fStream);
            fStream.close();
        }
        catch (Exception exptn)
        {
            LogManager.write("DMG Props Manager    - Properties file does not exist.", LogHandle.ALL,LogManager.MAJOR);
            return false;
        }

        LogManager.write("DMG Props Manager    - Read properties file: " + dmghome + "props/conf.prp", LogHandle.ALL,LogManager.DEBUG);

        try
        {
            setSourceDir(bkpProps.getProperty("Source"));
            setDestinationDir(bkpProps.getProperty("Destination"));
            setModifyDir(Integer.valueOf(bkpProps.getProperty("ModifyDir")).intValue());
            setRecurse(Boolean.valueOf(bkpProps.getProperty("Recurse")).booleanValue());
            setAction(Integer.valueOf(bkpProps.getProperty("Action")).intValue());
            setActionDate(Integer.valueOf(bkpProps.getProperty("DateAction")).intValue());
            setActionSize(Integer.valueOf(bkpProps.getProperty("SizeAction")).intValue());
            setDateValue01(Integer.valueOf(bkpProps.getProperty("DateValue01")).intValue());
            setDateCondition(Integer.valueOf(bkpProps.getProperty("DateCondition")).intValue());
            setDateValue02(Integer.valueOf(bkpProps.getProperty("DateValue02")).intValue());
            setDateModifyDir(Integer.valueOf(bkpProps.getProperty("DateModifyDir")).intValue());
            setDateSet(bkpProps.getProperty("DateSet"));
            setSizeValue01(Integer.valueOf(bkpProps.getProperty("SizeValue01")).intValue());
            setSizeCondition(Integer.valueOf(bkpProps.getProperty("SizeCondition")).intValue());
            setSizeValue02(Integer.valueOf(bkpProps.getProperty("SizeValue02")).intValue());
            setSizeModifyDir(Integer.valueOf(bkpProps.getProperty("SizeModifyDir")).intValue());
            setFileSize(bkpProps.getProperty("FileSize"));
            setSchedulePeriod(Integer.valueOf(bkpProps.getProperty("SchedulePeriod")).intValue());
            setWeekDay(Integer.valueOf(bkpProps.getProperty("WeekDay")).intValue());
            setMonthDay(bkpProps.getProperty("MonthDay"));
            setRunAtTime(Integer.valueOf(bkpProps.getProperty("RunAtTime")).intValue());
            setReportLevel(Integer.valueOf(bkpProps.getProperty("ReportLevel")).intValue());
            setReportToFile(Boolean.valueOf(bkpProps.getProperty("ReportToFile")).booleanValue());
        }
        catch (Exception excptn)
        {
            LogManager.write("DMG Props Manager    - Error in properties file. Setting default properties.", LogHandle.ALL,LogManager.MAJOR);
            setDefaultProperties();
        }

        LogManager.write("DMG Props Manager    - Properties read successfully.", LogHandle.ALL,LogManager.DEBUG);
        
        return true;
    }

    public void saveProps(String dmghome)
    {
        LogManager.write("DMG Props Manager    - Saving properties to: " + dmghome + "props/conf.prp", LogHandle.ALL,LogManager.DEBUG);

        Properties bkpProps = new Properties();
        bkpProps.setProperty("Source", _src);
        bkpProps.setProperty("Destination", _dst);
        bkpProps.setProperty("ModifyDir", Integer.toString(_modifyDir));
        bkpProps.setProperty("Recurse", Boolean.toString(_recurse));
        bkpProps.setProperty("Action", Integer.toString(_action));
        bkpProps.setProperty("DateAction", Integer.toString(_actionDate));
        bkpProps.setProperty("SizeAction", Integer.toString(_actionSize));
        bkpProps.setProperty("DateValue01", Integer.toString(_dateVal_1));
        bkpProps.setProperty("DateCondition", Integer.toString(_dateCond));
        bkpProps.setProperty("DateValue02", Integer.toString(_dateVal_2));
        bkpProps.setProperty("DateModifyDir", Integer.toString(_dateDest));
        bkpProps.setProperty("DateSet", Long.toString(_dateSet));
        bkpProps.setProperty("SizeValue01", Integer.toString(_sizeVal_1));
        bkpProps.setProperty("SizeCondition", Integer.toString(_sizeCond));
        bkpProps.setProperty("SizeValue02", Integer.toString(_sizeVal_2));
        bkpProps.setProperty("SizeModifyDir", Integer.toString(_sizeDest));
        bkpProps.setProperty("FileSize", _fileSize);
        bkpProps.setProperty("SchedulePeriod", Integer.toString(_schedulePeriod));
        bkpProps.setProperty("WeekDay", Integer.toString(_weekDay));
        bkpProps.setProperty("MonthDay", _monthDay);
        bkpProps.setProperty("RunAtTime", Integer.toString(_runAtTime));
        bkpProps.setProperty("ReportLevel", Integer.toString(_reportLevel));
        bkpProps.setProperty("ReportToFile", Boolean.toString(_reportToFile));

        try
        {
            FileOutputStream stream=new FileOutputStream(dmghome +"props/conf.prp");
            bkpProps.store(new PrintStream(stream), "Backup Utility Properties. Warning - Do not edit this file!!!");
            stream.close();

            LogManager.write("DMG Props Manager    - Properties saved.", LogHandle.ALL,LogManager.DEBUG);
        }
        catch (IOException e)
        {
            LogManager.write("DMG Props Manager    - Saving properties failed.", LogHandle.ALL,LogManager.MAJOR);
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
    }

    public void setSourceDir(String src)
    {
        _src = src;
    }

    public String getSourceDir()
    {
        return _src;
    }

    public void setDestinationDir(String dst)
    {
        _dst = dst;
    }

    public String getDestinationDir()
    {
        return _dst;
    }

    public void setModifyDir(int dir)
    {
        _modifyDir = dir;
    }

    public int getModifyDir()
    {
        return _modifyDir;
    }

    public void setRecurse(boolean recurse)
    {
        _recurse = recurse;
    }

    public boolean getRecurse()
    {
        return _recurse;
    }

    public void setAction(int action)
    {
        _action = action;
    }

    public int getAction()
    {
        return _action;
    }

    public void setActionDate(int actionDate)
    {
        _actionDate = actionDate;
    }

    public int getActionDate()
    {
        return _actionDate;
    }

    public void setActionSize(int actionSize)
    {
        _actionSize = actionSize;
    }

    public int getActionSize()
    {
        return _actionSize;
    }

    public void setDateValue01(int dateVal_1)
    {
        _dateVal_1 = dateVal_1;
    }

    public int getDateValue01()
    {
        return _dateVal_1;
    }

    public void setDateCondition(int condition)
    {
        _dateCond = condition;
    }

    public int getDateCondition()
    {
        return _dateCond;
    }

    public void setDateValue02(int dateVal_2)
    {
        _dateVal_2 = dateVal_2;
    }

    public int getDateValue02()
    {
        return _dateVal_2;
    }

    public void setDateModifyDir(int dateDest)
    {
        _dateDest = dateDest;
    }

    public int getDateModifyDir()
    {
        return _dateDest;
    }

    public void setDateSet(String dateSet)
    {
        if (dateSet.trim().equals("") || dateSet.trim().equals("0")) {
            _dateSet = 0;
        } else {
            _dateSet = dateStringToMill(dateSet);
        }
    }

    public String getDateSet()
    {
        if (_dateSet == 0){
            return "";
        } else {
            return dateMillToString(_dateSet);
        }
    }

    // Input: Date in milliseconds
    // Output: Formatted (DD/MM/YYYY hh:mm:ss) string
    public String dateMillToString(long date)
    {
        GregorianCalendar cal=new GregorianCalendar();

        cal.setTime(new Date(date));

        // Create the timestamp
        StringBuffer currTime = new StringBuffer();

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

        return currTime.toString();
    }

    // Input: Formatted date (DD/MM/YYYY HH:MM:SS) or Date in milliseconds as a String
    // Output: Date in milliseconds as long
    // Error: Returns NULL
    public long dateStringToMill(String dateString) {

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date(System.currentTimeMillis()));

        // If none of the time elements are set, we define them as ZERO (Beginning of today)
        int dateTokens[] = {cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH), cal.get(Calendar.YEAR), 0, 0, 0};

        // Check if the date is set as UNIX Time
        try
        {
            long timeInMilliseconds = Long.parseLong(dateString);
            if (timeInMilliseconds > System.currentTimeMillis()) {
                LogManager.write("DMG Props Manager    - Custom date: Invalid UNIX time.", LogHandle.ALL,LogManager.MINOR);
                return 0;
            } else {
                LogManager.write("DMG Props Manager    - Custom date: Time set in milliseconds.", LogHandle.ALL,LogManager.DEBUG);
                return timeInMilliseconds;
            }
        }
        catch (NumberFormatException nfe)
        {
            LogManager.write("DMG Props Manager    - Custom date: Not set as UNIX time.", LogHandle.ALL,LogManager.DEBUG);
        }

        // If not set as UNIX Time, check if it is set as a string
        // Valid formats are:
        // DD/MM/YYYY HH/MM/SS
        // DD/MM/YYYY HH/MM
        // DD/MM/YYYY HH
        // DD/MM/YYYY
        // DD:MM:YYYY HH:MM:SS
        // DD/MM/YYYY HH:MM:SS
        // DD.MM.YYYY HH.MM.SS
        int elementValue;
        int elementNum = 0;
        String element;
        StringTokenizer dateElements = new StringTokenizer(dateString, (".:/\\ "));
        //String[] dateElements = dateString.split("/:");

        LogManager.write("DMG Props Manager    - Getting date from string: [" + dateString + "]", LogHandle.ALL,LogManager.DEBUG);
        // The date can have maximum 6 elements
        //for (int elementNum = 0; elementNum <= 5; elementNum++) {
        while (elementNum <= 5) {

            element = "00";

            if (dateElements.hasMoreTokens()) {
                element = dateElements.nextToken();
            }

            switch (elementNum) {
                case 0:
                    // Day of the month
                    //LogManager.write("DMG Props Manager    - DD: [" + element + "]", LogHandle.ALL,LogManager.DEBUG);
                    try {
                        elementValue = Integer.valueOf(element).intValue();
                    } catch (Exception e) {
                        LogManager.write("DMG Props Manager    - Illegal date format: ["+dateString+"]. Valid format is: [DD/MM/YYY hh:mm:ss]", LogHandle.ALL,LogManager.MINOR);
                        return 0;
                    }
                    if (elementValue < 1 || elementValue > 31)
                    {
                        LogManager.write("DMG Props Manager    - Error: Invalid day of the month - Value must be [1-31].", LogHandle.ALL,LogManager.MINOR);
                        return 0;
                    } else {
                        dateTokens[elementNum] = elementValue;
                    }
                    break;
                case 1:
                    // Month of the year
                    //LogManager.write("DMG Props Manager    - MM: [" + element + "]", LogHandle.ALL,LogManager.DEBUG);
                    try {
                        elementValue = Integer.valueOf(element).intValue();
                    } catch (Exception e) {
                        LogManager.write("DMG Props Manager    - Illegal date format: ["+dateString+"]. Valid format is: [DD/MM/YYY hh:mm:ss]", LogHandle.ALL,LogManager.MINOR);
                        return 0;
                    }
                    if (elementValue < 1 || elementValue > 12)
                    {
                        LogManager.write("DMG Props Manager    - Error: Invalid month - Value must be [1-12].", LogHandle.ALL,LogManager.MINOR);
                        return 0;
                    } else {
                        dateTokens[elementNum] = elementValue;
                    }
                    break;
                case 2:
                    // Year
                    //LogManager.write("DMG Props Manager    - YY: [" + element + "]", LogHandle.ALL,LogManager.DEBUG);
                    try {
                        elementValue = Integer.valueOf(element).intValue();
                    } catch (Exception e) {
                        LogManager.write("DMG Props Manager    - Illegal date format: ["+dateString+"]. Valid format is: [DD/MM/YYY hh:mm:ss]", LogHandle.ALL,LogManager.MINOR);
                        return 0;
                    }
                    if (elementValue < 1970 || elementValue > dateTokens[2])
                    {
                        LogManager.write("DMG Props Manager    - Error: Invalid year - Value must be [1970-"+dateTokens[2]+"].", LogHandle.ALL,LogManager.MINOR);
                        return 0;
                    } else {
                        dateTokens[elementNum] = elementValue;
                    }
                    break;
                case 3:
                    // Hour
                    //LogManager.write("DMG Props Manager    - hh: [" + element + "]", LogHandle.ALL,LogManager.DEBUG);
                    try {
                        elementValue = Integer.valueOf(element).intValue();
                    } catch (Exception e) {
                        LogManager.write("DMG Props Manager    - Hour not specified: ["+dateString+"]. Setting to [00].", LogHandle.ALL,LogManager.DEBUG);
                        elementValue = 0;
                    }
                    if (elementValue < 0 || elementValue > 23)
                    {
                        LogManager.write("DMG Props Manager    - Error: Illegal hour - Value must be [0-23].", LogHandle.ALL,LogManager.MINOR);
                        return 0;
                    } else {
                        dateTokens[elementNum] = elementValue;
                    }
                    break;
                case 4:
                    // Minutes
                    //LogManager.write("DMG Props Manager    - mm: [" + element + "]", LogHandle.ALL,LogManager.DEBUG);
                    try {
                        elementValue = Integer.valueOf(element).intValue();
                    } catch (Exception e) {
                        LogManager.write("DMG Props Manager    - Minutes not specified: ["+dateString+"]. Setting to [00].", LogHandle.ALL,LogManager.DEBUG);
                        elementValue = 0;
                    }
                    if (elementValue < 0 || elementValue > 59)
                    {
                        LogManager.write("DMG Props Manager    - Error: Illegal minutes - Value must be [0-59].", LogHandle.ALL,LogManager.MINOR);
                        return 0;
                    } else {
                        dateTokens[elementNum] = elementValue;
                    }
                    break;
                case 5:
                    // Seconds
                    //LogManager.write("DMG Props Manager    - ss: [" + element + "]", LogHandle.ALL,LogManager.DEBUG);
                    try {
                        elementValue = Integer.valueOf(element).intValue();
                    } catch (Exception e) {
                        LogManager.write("DMG Props Manager    - Seconds not specified: ["+dateString+"]. Setting to [00].", LogHandle.ALL,LogManager.DEBUG);
                        elementValue = 0;
                    }
                    if (elementValue < 0 || elementValue > 59)
                    {
                        LogManager.write("DMG Props Manager    - Error: Illegal hour - Value must be [0-59].", LogHandle.ALL,LogManager.MINOR);
                        return 0;
                    } else {
                        dateTokens[elementNum] = elementValue;
                    }
                    break;
                default:
                    // Leave it as the beginning of today
                    break;
            }
            elementNum++;
        }

        // SetTime(YYYY, MM, DD, hh, mm, ss)
        cal.set(dateTokens[2], dateTokens[1], dateTokens[0], dateTokens[3], dateTokens[4], dateTokens[5]);

        LogManager.write("DMG Props Manager    - Specified time in milliseconds: ["+cal.getTimeInMillis()+"].", LogHandle.ALL,LogManager.DEBUG);

        return cal.getTimeInMillis();
    }

    public void setSizeValue01(int sizeVal_1)
    {
        _sizeVal_1 = sizeVal_1;
    }

    public int getSizeValue01()
    {
        return _sizeVal_1;
    }

    public void setSizeCondition(int condition)
    {
        _sizeCond = condition;
    }

    public int getSizeCondition()
    {
        return _sizeCond;
    }

    public void setSizeValue02(int sizeVal_2)
    {
        _sizeVal_2 = sizeVal_2;
    }

    public int getSizeValue02()
    {
        return _sizeVal_2;
    }

    public void setSizeModifyDir(int sizeDest)
    {
        _sizeDest = sizeDest;
    }

    public int getSizeModifyDir()
    {
        return _sizeDest;
    }

    public void setFileSize(String fileSize)
    {
        _fileSize = fileSize;
    }

    public String getFileSize()
    {
        return _fileSize;
    }

    public void setSchedulePeriod(int schedulePeriod)
    {
        _schedulePeriod = schedulePeriod;
    }

    public int getSchedulePeriod()
    {
        return _schedulePeriod;
    }

    public void setWeekDay(int weekDay)
    {
        _weekDay = weekDay;
    }

    public int getWeekDay()
    {
        return _weekDay;
    }

    // Before setting the value we have to format the string which the user entered
    public void setMonthDay(String monthDay)
    {
        String token;
        StringBuffer buffer = new StringBuffer();
        StringTokenizer st = new StringTokenizer(monthDay, ",.;:/\\ ");

        while (st.hasMoreTokens()) {
            token = st.nextToken();

            if (Integer.valueOf(token).intValue() > 31)
            {
                LogManager.write("DMG Props Manager    - Error: Unrecognized day of the month - Value must be [1-31].", LogHandle.ALL,LogManager.MAJOR);
                continue;
            }

            if (token.length() == 1)
            {
                buffer.append("0");
                buffer.append(token);
                buffer.append(", ");
            }
            else
            {
                buffer.append(token);
                buffer.append(", ");
            }
        }
        monthDay = buffer.toString();

        if (monthDay.length() > 2)
        {
            _monthDay = monthDay.substring(0, monthDay.length()-2);
        }
        else
        {
            _monthDay = monthDay;
        }
    }

    public String getMonthDay()
    {
        return _monthDay;
    }

    public void setRunAtTime(int runAt)
    {
        _runAtTime = runAt;
    }

    public int getRunAtTime()
    {
        return _runAtTime;
    }

    public void setReportLevel(int reportLevel) {
        _reportLevel = reportLevel;
    }

    public int getReportLevel() {
        return _reportLevel;
    }

    public void setReportToFile(boolean reportToFile)
    {
        _reportToFile = reportToFile;
    }

    public boolean getReportToFile()
    {
        return _reportToFile;
    }

    public void setDefaultProperties()
    {
        _src = "Select Source directory.";
        _dst = "Select Destination directory.";
        _modifyDir = MODIFY_DST;

        _recurse = false;

        _action = NO_ACTION;
        _actionDate = KEEP_NEWEST;
        _actionSize = KEEP_BIGGEST;
        _dateVal_1 = VALUE_SRC;
        _dateCond  = COND_DATE_OLDER;
        _dateVal_2 = VALUE_DST;
        _dateSet = 0;
        _sizeVal_1 = VALUE_SRC;
        _sizeCond = COND_SIZE_BIGGER;
        _sizeVal_2 = VALUE_DST;
        _fileSize = "";

        _schedulePeriod = NO_SCHEDULE;
        _weekDay = SUNDAY;
        _monthDay = "";
        _runAtTime = 32;        // Index '0' is 00:00 o'clock
        // Every 15 min is ONE Index (01:00 is Index '4')
        // So Index '32' is 08.00

        _reportLevel = UPDATE;
        _reportToFile = false;
    }

    public void printProps()
    {
        System.out.println("\nBackup Utility Properties\n-----------------------------");
        System.out.println("Source Directory        [_src]            -> "+_src);
        System.out.println("Destination Directory   [_dst]            -> "+_dst);
        System.out.println("Directory to modify     [_modifyDir]      -> "+_modifyDir);
        System.out.println("Directory Recursion     [_recurse]        -> "+Boolean.toString(_recurse));
        System.out.println("Duplicate Files Action  [_action]         -> "+_action);
        System.out.println("Decide by Date Action   [_actionDate]     -> "+_actionDate);
        System.out.println("Decide by Size Action   [_actionSize]     -> "+_actionSize);
        System.out.println("Date Value 1            [_dateVal_1]      -> "+_dateVal_1);
        System.out.println("Date Condition          [_dateCond]       -> "+_dateCond);
        System.out.println("Date Value 2            [_dateVal_2]      -> "+_dateVal_2);
        System.out.println("Modify Action by Date   [_dateDest]       -> "+_dateDest);
        System.out.println("File Date               [_dateSet]        -> "+_dateSet);
        System.out.println("Size Value 1            [_sizeVal_1]      -> "+_sizeVal_1);
        System.out.println("Size Condition          [_sizeCond]       -> "+_sizeCond);
        System.out.println("Size Value 2            [_sizeVal_2]      -> "+_sizeVal_2);
        System.out.println("Modify Action by Size   [_sizeDest]       -> "+_sizeDest);
        System.out.println("Size of file            [_fileSize]       -> "+_fileSize);
        System.out.println("Schedule Period         [_schedulePeriod] -> "+_schedulePeriod);
        System.out.println("Scheduled days of Week  [_weekDay]        -> "+_weekDay);
        System.out.println("Scheduled days of Month [_monthDay]       -> "+_monthDay);
        System.out.println("Scheduled Time          [_runAtTime]      -> "+_runAtTime);
        System.out.println("Report Level            [_reportLevel]    -> "+_reportLevel);
        System.out.println("Report to File          [_reportToFile]   -> "+Boolean.toString(_reportToFile));
    }
}
