package utils;

import gui.DMGOptionPane;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import log.LogManager;
import log.LogHandle;

/**
 * Created by IntelliJ IDEA.
 * User: Pimmy
 * Date: 14-Oct-2005
 * Time: 10:37:49
 * To change this template use File | Settings | File Templates.
 */
public class DMGGenerator implements Runnable
{
    private static String _dmghome;
    private static DMGProps _vars;
    private static JButton _startButton;
    private static DMGTableModel _reportTableModel;

    private static JProgressBar _progress;
    private static JTextField _numFiles;
    private static JTextField _dataSize;
    private static JTextField _time;

    private int _totalFiles;
    private int _jobDone; // = _totalFiles, and is used to determine when the update has completed
    private long _totalSize;

    private boolean _running;
    private boolean _stopped;
    private boolean _paused;
    private FileManipulator _fileMan;

    private File _f_src;
    private File _f_dst;

    private Thread dmgGenerator = null;

    private ReportCSV _reporter = null;

    public DMGGenerator(String dmghome, DMGTableModel reportTableModel, JProgressBar progress, JTextField numFiles, JTextField dataSize, JTextField time)
    {
        _dmghome = dmghome;
        _reportTableModel = reportTableModel;
        _progress = progress;
        _numFiles = numFiles;
        _dataSize = dataSize;
        _time = time;

        _vars = new DMGProps();
        _vars.getProps(_dmghome);
        _running = false;
        _stopped = true;
        _paused = false;
        _fileMan = new FileManipulator();
    }

    public boolean generateDMG(JButton startButton) {
    	
        LogManager.write("Datamirror generator - Retrieving latest properties..", LogHandle.ALL,LogManager.DEBUG);
    	
        // Load the latest properties
        _vars.getProps(_dmghome);
        // Get the start button
        _startButton = startButton;

        String src = _vars.getSourceDir();
        String dst = _vars.getDestinationDir();

        _f_src = new File(src);
        _f_dst = new File(dst);

        LogManager.write("Datamirror generator - Verifying locations src[" + src + "], dst[" + dst + "]", LogHandle.ALL,LogManager.DEBUG);

        if (!verifyLocations(_f_src, _f_dst)) {
            _running = false;
            _stopped = true;
            return false;
        }

        if (dmgGenerator == null)
        {
            LogManager.write("Datamirror generator - Starting the generator thread.", LogHandle.ALL,LogManager.DEBUG);

            dmgGenerator = new Thread(this);
            dmgGenerator.start();
        } else {
            LogManager.write("Datamirror generator - The generator thread is already running!", LogHandle.ALL,LogManager.MINOR);
        }

        return true;
    }

    public void run()
    {
        String header = "--------------------------------------------------------------------------------\n"+
                        "- User configuration:\n"+
                        "--------------------------------------------------------------------------------\n"+
                        "- "+
                        "- Source Directory        [_src]            -> "+_vars.getSourceDir()+"\n"+
                        "- Destination Directory   [_dst]            -> "+_vars.getDestinationDir()+"\n"+
                        "- Directory to modify     [_modifyDir]      -> "+_vars.getModifyDir()+"\n"+
                        "- Directory Recursion     [_recurse]        -> "+_vars.getRecurse()+"\n"+
                        "- Duplicate Files Action  [_action]         -> "+_vars.getAction()+"\n"+
                        "- Decide by Date Action   [_actionDate]     -> "+_vars.getActionDate()+"\n"+
                        "- Decide by Size Action   [_actionSize]     -> "+_vars.getActionSize()+"\n"+
                        "- Date Value 1            [_dateVal_1]      -> "+_vars.getDateValue01()+"\n"+
                        "- Date Condition          [_dateCond]       -> "+_vars.getDateCondition()+"\n"+
                        "- Date Value 2            [_dateVal_2]      -> "+_vars.getDateValue02()+"\n"+
                        "- Modify Action by Date   [_dateDest]       -> "+_vars.getDateModifyDir()+"\n"+
                        "- File Date               [_dateSet]        -> "+_vars.getDateSet()+"\n"+
                        "- Size Value 1            [_sizeVal_1]      -> "+_vars.getSizeValue01()+"\n"+
                        "- Size Condition          [_sizeCond]       -> "+_vars.getSizeCondition()+"\n"+
                        "- Size Value 2            [_sizeVal_2]      -> "+_vars.getSizeValue02()+"\n"+
                        "- Modify Action by Size   [_sizeDest]       -> "+_vars.getSizeModifyDir()+"\n"+
                        "- Size of file            [_fileSize]       -> "+_vars.getFileSize()+"\n"+
                        "- Schedule Period         [_schedulePeriod] -> "+_vars.getSchedulePeriod()+"\n"+
                        "- Scheduled days of Week  [_weekDay]        -> "+_vars.getWeekDay()+"\n"+
                        "- Scheduled days of Month [_monthDay]       -> "+_vars.getMonthDay()+"\n"+
                        "- Scheduled Time          [_runAtTime]      -> "+_vars.getRunAtTime()+"\n"+
                        "- Report Level            [_reportLevel]    -> "+_vars.getReportLevel()+"\n"+
                        "- Report to File          [_reportToFile]   -> "+_vars.getReportToFile()+"\n"+
                        "----------------------------------------------------------------------------------------------------";
        LogManager.write("Datamirror generator - User defined settings:\n"+header, LogHandle.ALL,LogManager.DEBUG);

        if (_vars.getReportToFile()) {
            _reporter = new ReportCSV(_dmghome);
            _reporter.createReport();
            LogManager.write("Datamirror generator - Report to file: Enabled", LogHandle.ALL,LogManager.DEBUG);
        } else {
            LogManager.write("Datamirror generator - Report to file: Disabled", LogHandle.ALL,LogManager.DEBUG);
        }

        // Count the time estimated to complete the task
        //Chrono seconds = new Chrono();
        //seconds.startChrono();

        _progress.setIndeterminate(true);
        
        if (_vars.getModifyDir() == 0)
        {
            // Get the total number of files required for the progress bar
            _totalFiles = 0;
            getNumFiles(_f_dst);
            _jobDone = _totalFiles;
            // Start the progress bar
            _progress.setMaximum(_totalFiles);
            _progress.setMinimum(0);
            _progress.setIndeterminate(false);
            // Null the number of files
            _totalFiles = 0;

            LogManager.write("Datamirror generator - Start to MODIFY SRC.", LogHandle.ALL,LogManager.DEBUG);
            drillModifSrc(_f_src, _f_dst);
        }
        else if (_vars.getModifyDir() == 1)
        {
            // Get the total number of files required for the progress bar
            _totalFiles = 0;
            getNumFiles(_f_src);
            _jobDone = _totalFiles;
            // Start the progress bar
            _progress.setMaximum(_totalFiles);
            _progress.setMinimum(0);
            _progress.setIndeterminate(false);
            // Null the number of files
            _totalFiles = 0;

            LogManager.write("Datamirror generator - Start to MODIFY DST.", LogHandle.ALL,LogManager.DEBUG);
            drillModifDst(_f_src, _f_dst);
        }
        else
        {
            // Get the total number of files required for the progress bar
            _totalFiles = 0;
            getNumFiles(_f_dst);
            int tmpFiles = _totalFiles;
            getNumFiles(_f_src);
            _jobDone = _totalFiles;
            // Add the difference
            if ((_totalFiles - tmpFiles) > tmpFiles) {

            }
            // Start the progress bar
            _progress.setMaximum(_totalFiles);
            _progress.setMinimum(0);
            _progress.setIndeterminate(false);
            // Null the number of files
            _totalFiles = 0;

            LogManager.write("Datamirror generator - Start to MODIFY SRC.", LogHandle.ALL,LogManager.DEBUG);
            drillModifSrc(_f_src, _f_dst);
            LogManager.write("Datamirror generator - Start to MODIFY DST.", LogHandle.ALL,LogManager.DEBUG);
            drillModifDst(_f_src, _f_dst);
        }

        if (_running) {
            LogManager.write("Datamirror generator - Data-mirroring completed.", LogHandle.ALL,LogManager.DEBUG);
        }

        if (_reporter != null) {
            _reporter.closeReport();
            _reporter = null;
        }

        _progress.setIndeterminate(false);

        dmgGenerator = null;
        _running = false;
        _stopped = true;
        _paused = false;
        _startButton.setText("Start");
    }

    private void drillModifSrc(File srcFolder, File dstFolder)
    {
        LogManager.write("Datamirror generator - Scanning DST directory: ["+dstFolder.getAbsolutePath()+"].", LogHandle.ALL,LogManager.DEBUG);

        // Go trough each file in SRC directory
        File[] dstList = dstFolder.listFiles();
        LogManager.write("Datamirror generator - DST directory: ["+dstFolder.getName()+"]  has: ["+dstList.length+"] files.", LogHandle.ALL,LogManager.DEBUG);

        for (int i=0; i < dstList.length; i++)
        {
            while (_paused && _running) {
                try{Thread.sleep(1000);}catch (Exception se) {
                    LogManager.write("Datamirror generator - Timer exception.", LogHandle.ALL,LogManager.MAJOR);
                }
            }

            if (!_running)
            {
                _stopped = true;
                return;
            }

            // Increment the number of files count
            _totalFiles++;

            // Get the mirrored filename
            String srcMirror = srcFolder.getAbsolutePath().concat("\\");
            srcMirror = srcMirror.concat(dstList[i].getName());
            File f_srcMirror = new File(srcMirror);

            LogManager.write("Datamirror generator - TARGET: ["+dstList[i].getAbsolutePath()+"], Size: ["+dstList[i].length()+"], Date: ["+convertTime(dstList[i].lastModified())+"].", LogHandle.ALL,LogManager.DEBUG);
            LogManager.write("Datamirror generator - DESTIN: ["+f_srcMirror.getAbsolutePath()+"], Size: ["+f_srcMirror.length()+"], Date: ["+convertTime(f_srcMirror.lastModified())+"].", LogHandle.ALL,LogManager.DEBUG);
            // If this is a dir, we make sure that the mirror dir exist and then drill down.
            if (dstList[i].isDirectory())
            {
                if (!f_srcMirror.exists())
                {
                    // If directory does not exist, we check if the user wants us to create it.
                    // If selected to modify SRC or BOTH we proceed with the dir creation
                    if (_vars.getModifyDir() != 1)
                    {
                        LogManager.write("Datamirror generator - Creating directory: ["+f_srcMirror.getAbsolutePath()+"].", LogHandle.ALL,LogManager.DEBUG);
                        if (f_srcMirror.mkdir()) {
                            if (_vars.getReportLevel() < 2) {
                                // Info = 0
                                // Update = 1
                                // Error = 2
                                if (_vars.getReportToFile()) {
                                    _reporter.write(f_srcMirror.getAbsolutePath(), f_srcMirror.length(), f_srcMirror.lastModified(), dstList[i].getAbsolutePath(), dstList[i].length(), dstList[i].lastModified(), "MKDIR", "SRC", "OK");
                                }
                                _reportTableModel.addElement(f_srcMirror.getName(), "MKDIR", "SRC", "OK");
                            }
                        } else {
                            if (_vars.getReportToFile()) {
                                _reporter.write(f_srcMirror.getAbsolutePath(), f_srcMirror.length(), f_srcMirror.lastModified(), dstList[i].getAbsolutePath(), dstList[i].length(), dstList[i].lastModified(), "MKDIR", "SRC", "Fail");
                            }
                            _reportTableModel.addElement(f_srcMirror.getName(), "MKDIR", "SRC", "Fail");
                            LogManager.write("Datamirror generator - Warning: Failed to create directory: ["+f_srcMirror.getAbsolutePath()+"]. Directory will be skipped.", LogHandle.ALL,LogManager.MAJOR);
                            continue;
                        }
                    }
                }
                else if (f_srcMirror.isFile())
                {
                    // Bad coincidence where instead of a directory we have a file with the same name.
                    if (_vars.getReportToFile()) {
                        _reporter.write(f_srcMirror.getAbsolutePath(), f_srcMirror.length(), f_srcMirror.lastModified(), dstList[i].getAbsolutePath(), dstList[i].length(), dstList[i].lastModified(), "Dir -> File", "DST -> SRC", "Error");
                    }
                    _reportTableModel.addElement(f_srcMirror.getName(), "DIR -> File", "DST -> SRC", "Error");
                    LogManager.write("Datamirror generator - Warning: Mirror of directory: ["+dstList[i].getAbsolutePath()+"] is a file: ["+f_srcMirror.getAbsolutePath()+"]. Directory will be skipped.", LogHandle.ALL,LogManager.MAJOR);
                    continue;
                }

                if (f_srcMirror.isDirectory())
                {
                    // We have both directories, so DRILL DOWN if the User wants
                    if (_vars.getRecurse())
                    {
                        if (_vars.getReportLevel() < 1) {
                            // Info = 0
                            // Update = 1
                            // Error = 2
                            if (_vars.getReportToFile()) {
                                _reporter.write(f_srcMirror.getAbsolutePath(), f_srcMirror.length(), f_srcMirror.lastModified(), dstList[i].getAbsolutePath(), dstList[i].length(), dstList[i].lastModified(), "Drill Down", "SRC", "OK");
                            }
                            _reportTableModel.addElement(f_srcMirror.getName(), "Drill Down", "SRC", "OK");
                        }
                        drillModifSrc(f_srcMirror, dstList[i]);
                    }
                }
                else
                {
                    // Handling of any other errors I didn't think of.
                    if (_vars.getReportToFile()) {
                        _reporter.write(f_srcMirror.getAbsolutePath(), f_srcMirror.length(), f_srcMirror.lastModified(), dstList[i].getAbsolutePath(), dstList[i].length(), dstList[i].lastModified(), "Unknown", "DST -> SRC", "Error");
                    }
                    _reportTableModel.addElement(f_srcMirror.getName(), "Unknown", "SRC", "Error");
                    LogManager.write("Datamirror generator - Warning: Unknown error at: ["+f_srcMirror.getAbsolutePath()+"]. Skipped.", LogHandle.ALL,LogManager.MAJOR);
                }
            }
            else
            {
                // Here starts the file manipulation
                if (f_srcMirror.isFile())
                {
                    // Check what action the user defined
                    switch (_vars.getAction())
                    {
                        case 0:
                            // No action should be taken. Proceed with the next file.
                            if (_vars.getReportLevel() < 1) {
                                // Info = 0
                                // Update = 1
                                // Error = 2
                                if (_vars.getReportToFile()) {
                                    _reporter.write(f_srcMirror.getAbsolutePath(), f_srcMirror.length(), f_srcMirror.lastModified(), dstList[i].getAbsolutePath(), dstList[i].length(), dstList[i].lastModified(), "Skipped", "DST -> SRC", "OK");
                                }
                                _reportTableModel.addElement(f_srcMirror.getName(), "Skipped", "DST -> SRC", "OK");
                            }
                            break;
                        case 1:
                            // Decide by DATE
                            switch (_vars.getActionDate())
                            {
                                case 0:
                                    // Keep the newest file
                                    if (dstList[i].lastModified() > f_srcMirror.lastModified())
                                    {
                                        // If file matches the condition, we check if the user wants us to copy it.
                                        // If selected to modify SRC or BOTH we proceed with the dir creation
                                        if (_vars.getModifyDir() != 1)
                                        {
                                            LogManager.write("Datamirror generator - Deleting file: ["+f_srcMirror.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                                            if (f_srcMirror.delete())
                                            {
                                                if (_fileMan.copyFile(dstList[i].getAbsolutePath(), f_srcMirror.getAbsolutePath()))
                                                {
                                                    // Update Total amount of data copied
                                                    _totalSize = _totalSize + dstList[i].length();

                                                    // Log report information
                                                    if (_vars.getReportLevel() < 2) {
                                                        // Info = 0
                                                        // Update = 1
                                                        // Error = 2
                                                        if (_vars.getReportToFile()) {
                                                            _reporter.write(f_srcMirror.getAbsolutePath(), f_srcMirror.length(), f_srcMirror.lastModified(), dstList[i].getAbsolutePath(), dstList[i].length(), dstList[i].lastModified(), "Update", "DST -> SRC", "OK");
                                                        }
                                                        _reportTableModel.addElement(f_srcMirror.getName(), "Update", "DST -> SRC", "OK");
                                                    }

                                                    LogManager.write("Datamirror generator - Setting timestamp: ["+f_srcMirror.getAbsolutePath()+"] - ["+convertTime(dstList[i].lastModified())+"].", LogHandle.ALL,LogManager.DEBUG);
                                                    f_srcMirror.setLastModified(dstList[i].lastModified());
                                                }
                                                else
                                                {
                                                    if (_vars.getReportToFile()) {
                                                        _reporter.write(f_srcMirror.getAbsolutePath(), f_srcMirror.length(), f_srcMirror.lastModified(), dstList[i].getAbsolutePath(), dstList[i].length(), dstList[i].lastModified(), "Update", "DST -> SRC", "Fail");
                                                    }
                                                    _reportTableModel.addElement(f_srcMirror.getName(), "Update", "DST -> SRC", "Fail");
                                                    LogManager.write("Datamirror generator - Warning: Failed to copy From: ["+dstList[i].getAbsolutePath()+"] To: ["+f_srcMirror.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                                                }
                                            }
                                            else
                                            {
                                                if (_vars.getReportToFile()) {
                                                    _reporter.write(f_srcMirror.getAbsolutePath(), f_srcMirror.length(), f_srcMirror.lastModified(), dstList[i].getAbsolutePath(), dstList[i].length(), dstList[i].lastModified(), "Update", "DST -> SRC", "Fail");
                                                }
                                                _reportTableModel.addElement(f_srcMirror.getName(), "Remove", "SRC", "Fail");
                                                LogManager.write("Datamirror generator - Warning: Failed to delete file: ["+f_srcMirror.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                                            }
                                        }
                                    }
                                    break;
                                case 1:
                                    // Keep the oldest file
                                    if (dstList[i].lastModified() < f_srcMirror.lastModified())
                                    {
                                        // If file matches the condition, we check if the user wants us to copy it.
                                        // If selected to modify SRC or BOTH we proceed with the dir creation
                                        if (_vars.getModifyDir() != 0)
                                        {
                                            LogManager.write("Datamirror generator - Deleting file: ["+f_srcMirror.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                                            if (f_srcMirror.delete())
                                            {
                                                if (_fileMan.copyFile(dstList[i].getAbsolutePath(), f_srcMirror.getAbsolutePath()))
                                                {
                                                    // Update Total amount of data copied
                                                    _totalSize = _totalSize + dstList[i].length();

                                                    // Log report information
                                                    if (_vars.getReportLevel() < 2) {
                                                        // Info = 0
                                                        // Update = 1
                                                        // Error = 2
                                                        if (_vars.getReportToFile()) {
                                                            _reporter.write(f_srcMirror.getAbsolutePath(), f_srcMirror.length(), f_srcMirror.lastModified(), dstList[i].getAbsolutePath(), dstList[i].length(), dstList[i].lastModified(), "Update", "DST -> SRC", "OK");
                                                        }
                                                        _reportTableModel.addElement(f_srcMirror.getName(), "Update", "DST -> SRC", "OK");
                                                    }

                                                    LogManager.write("Datamirror generator - Setting timestamp: ["+f_srcMirror.getAbsolutePath()+"] - ["+convertTime(dstList[i].lastModified())+"].", LogHandle.ALL,LogManager.DEBUG);
                                                    f_srcMirror.setLastModified(dstList[i].lastModified());
                                                }
                                                else
                                                {
                                                    if (_vars.getReportToFile()) {
                                                        _reporter.write(f_srcMirror.getAbsolutePath(), f_srcMirror.length(), f_srcMirror.lastModified(), dstList[i].getAbsolutePath(), dstList[i].length(), dstList[i].lastModified(), "Update", "DST -> SRC", "Fail");
                                                    }
                                                    _reportTableModel.addElement(f_srcMirror.getName(), "Update", "DST -> SRC", "Fail");
                                                    LogManager.write("Datamirror generator - Warning: Failed to copy From: ["+dstList[i].getAbsolutePath()+"] To: ["+f_srcMirror.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                                                }
                                            }
                                            else
                                            {
                                                if (_vars.getReportToFile()) {
                                                    _reporter.write(f_srcMirror.getAbsolutePath(), f_srcMirror.length(), f_srcMirror.lastModified(), dstList[i].getAbsolutePath(), dstList[i].length(), dstList[i].lastModified(), "Update", "DST -> SRC", "Fail");
                                                }
                                                _reportTableModel.addElement(f_srcMirror.getName(), "Remove", "SRC", "Fail");
                                                LogManager.write("Datamirror generator - Warning: Failed to delete file: ["+f_srcMirror.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                                            }
                                        }
                                    }
                                    break;
                                case 2:
                                    // Custom settings

                                    // Set default values
                                    File srcName;
                                    File cmpName = f_srcMirror;

                                    // Timestamp of the file we are comparing
                                    long src_timestamp;
                                    // Timestamp against which we are comparing
                                    long cmp_timestamp = 0;

                                    // Get the file timestamps
                                    if (_vars.getDateValue01() == 0)
                                    {
                                        srcName = dstList[i];
                                        src_timestamp = srcName.lastModified();
                                    }
                                    else if (_vars.getDateValue01() == 1)
                                    {
                                        srcName = f_srcMirror;
                                        src_timestamp = srcName.lastModified();
                                    }
                                    else
                                    {
                                        LogManager.write("Datamirror generator - Warning: Unrecognized location: ["+_vars.getDateValue01()+"]. Possible Values are [0, 1].", LogHandle.ALL,LogManager.MAJOR);
                                        break;
                                    }

                                    if (_vars.getDateValue02() == 0)
                                    {
                                        cmpName = dstList[i];
                                        cmp_timestamp = cmpName.lastModified();
                                    }
                                    else if (_vars.getDateValue02() == 1)
                                    {
                                        cmpName = f_srcMirror;
                                        cmp_timestamp = cmpName.lastModified();
                                    }
                                    else if (_vars.getDateValue02() > 6)
                                    {
                                        LogManager.write("Datamirror generator - Warning: Unrecognized location: ["+_vars.getDateValue01()+"]. Possible Values are [0, 1].", LogHandle.ALL,LogManager.MAJOR);
                                        break;
                                    }

                                    // Make sure that the target and the destination are not the same file.
                                    if (_vars.getDateValue01() == _vars.getDateValue02())
                                    {
                                        LogManager.write("Datamirror generator - Custom Action: Target: ["+srcName.getAbsolutePath()+"] and Destination: ["+cmpName.getAbsolutePath()+"] are the same. Skipping.", LogHandle.ALL,LogManager.MINOR);
                                        break;
                                    }

                                    // Get the timestamp in seconds against which we will compare
                                    long now = System.currentTimeMillis();

                                    switch (_vars.getDateValue02())
                                    {
                                        case 0:
                                            // <src_path>
                                            // Alredy defined above
                                            break;
                                        case 1:
                                            // <dst_path>
                                            // Alredy defined above
                                            break;
                                        case 2:
                                            // <Specific timestamp (Date)>
                                            cmp_timestamp = now - 3600000;
                                            break;
                                        case 3:
                                            // <hour>
                                            cmp_timestamp = now - 3600000;
                                            break;
                                        case 4:
                                            // <day>
                                            cmp_timestamp = _vars.dateStringToMill(_vars.getDateSet());
                                            break;
                                        case 5:
                                            // <week>
                                            cmp_timestamp = now - 604800000;
                                            break;
                                        case 6:
                                            // <month> (assume the max length of the month - 31 days)
                                            cmp_timestamp = now - 2678400;
                                            cmp_timestamp = cmp_timestamp*1000;
                                            break;
                                        case 7:
                                            // <year>
                                            cmp_timestamp = now - 31536000;
                                            cmp_timestamp = cmp_timestamp*1000;
                                            break;
                                        default:
                                            cmp_timestamp = 0;
                                            LogManager.write("Datamirror generator - Warning: Unrecognized time period: ["+_vars.getDateValue01()+"]. Possible Values are [0, 6].", LogHandle.ALL,LogManager.MAJOR);
                                            break;
                                    }

                                    switch (_vars.getDateCondition())
                                    {
                                        case 0:
                                            // If <src_path>  OLDER_THAN ...
                                            if (src_timestamp < cmp_timestamp)
                                            {
                                                customAction(_vars.getDateModifyDir(), dstList[i], f_srcMirror);
                                            }
                                            break;
                                        case 1:
                                            // If <src_path>  NEWER_THAN ...
                                            if (src_timestamp > cmp_timestamp)
                                            {
                                                customAction(_vars.getDateModifyDir(), dstList[i], f_srcMirror);
                                            }
                                            break;
                                        case 2:
                                            // If <src_path>  SAME_AS ...
                                            if (src_timestamp == cmp_timestamp)
                                            {
                                                customAction(_vars.getDateModifyDir(), dstList[i], f_srcMirror);
                                            }
                                            break;
                                        case 3:
                                            // If <src_path>  DIFFERENT_THAN ...
                                            if (src_timestamp != cmp_timestamp)
                                            {
                                                customAction(_vars.getDateModifyDir(), dstList[i], f_srcMirror);
                                            }
                                            break;
                                        default:
                                            break;
                                    }
                                    break;
                                default:
                                    // Ignore this case
                                    break;
                            }
                            break;
                        case 2:
                            // Decide by SIZE
                            switch (_vars.getActionSize())
                            {
                                case 0:
                                    // Keeep the biggest file
                                    if (dstList[i].length() > f_srcMirror.length())
                                    {
                                        // If file matches the condition, we check if the user wants us to copy it.
                                        // If selected to modify SRC or BOTH we proceed with the dir creation
                                        if (_vars.getModifyDir() != 1)
                                        {
                                            LogManager.write("Datamirror generator - Deleting file: ["+f_srcMirror.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                                            if (f_srcMirror.delete())
                                            {
                                                if (_fileMan.copyFile(dstList[i].getAbsolutePath(), f_srcMirror.getAbsolutePath()))
                                                {
                                                    // Update Total amount of data copied
                                                    _totalSize = _totalSize + dstList[i].length();

                                                    // Log report information
                                                    if (_vars.getReportLevel() < 2) {
                                                        // Info = 0
                                                        // Update = 1
                                                        // Error = 2
                                                        if (_vars.getReportToFile()) {
                                                            _reporter.write(f_srcMirror.getAbsolutePath(), f_srcMirror.length(), f_srcMirror.lastModified(), dstList[i].getAbsolutePath(), dstList[i].length(), dstList[i].lastModified(), "Update", "DST -> SRC", "OK");
                                                        }
                                                        _reportTableModel.addElement(f_srcMirror.getName(), "Update", "DST -> SRC", "OK");
                                                    }

                                                    LogManager.write("Datamirror generator - Setting timestamp: ["+f_srcMirror.getAbsolutePath()+"] - ["+convertTime(dstList[i].lastModified())+"].", LogHandle.ALL,LogManager.DEBUG);
                                                    f_srcMirror.setLastModified(dstList[i].lastModified());
                                                }
                                                else
                                                {
                                                    if (_vars.getReportToFile()) {
                                                        _reporter.write(f_srcMirror.getAbsolutePath(), f_srcMirror.length(), f_srcMirror.lastModified(), dstList[i].getAbsolutePath(), dstList[i].length(), dstList[i].lastModified(), "Update", "DST -> SRC", "Fail");
                                                    }
                                                    _reportTableModel.addElement(f_srcMirror.getName(), "Update", "DST -> SRC", "Fail");
                                                    LogManager.write("Datamirror generator - Warning: Failed to copy From: ["+dstList[i].getAbsolutePath()+"] To: ["+f_srcMirror.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                                                }
                                            }
                                            else
                                            {
                                                if (_vars.getReportToFile()) {
                                                    _reporter.write(f_srcMirror.getAbsolutePath(), f_srcMirror.length(), f_srcMirror.lastModified(), dstList[i].getAbsolutePath(), dstList[i].length(), dstList[i].lastModified(), "Update", "DST -> SRC", "Fail");
                                                }
                                                _reportTableModel.addElement(f_srcMirror.getName(), "Remove", "SRC", "Fail");
                                                LogManager.write("Datamirror generator - Warning: Failed to delete file: ["+f_srcMirror.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                                            }
                                        }
                                    }
                                    break;
                                case 1:
                                    // Keeep the smallest file
                                    if (dstList[i].length() < f_srcMirror.length())
                                    {
                                        // If file matches the condition, we check if the user wants us to copy it.
                                        // If selected to modify SRC or BOTH we proceed with the dir creation
                                        if (_vars.getModifyDir() != 1)
                                        {
                                            LogManager.write("Datamirror generator - Deleting file: ["+f_srcMirror.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                                            if (f_srcMirror.delete())
                                            {
                                                if (_fileMan.copyFile(dstList[i].getAbsolutePath(), f_srcMirror.getAbsolutePath()))
                                                {
                                                    // Update Total amount of data copied
                                                    _totalSize = _totalSize + dstList[i].length();

                                                    // Log report information
                                                    if (_vars.getReportLevel() < 2) {
                                                        // Info = 0
                                                        // Update = 1
                                                        // Error = 2
                                                        if (_vars.getReportToFile()) {
                                                            _reporter.write(f_srcMirror.getAbsolutePath(), f_srcMirror.length(), f_srcMirror.lastModified(), dstList[i].getAbsolutePath(), dstList[i].length(), dstList[i].lastModified(), "Update", "DST -> SRC", "OK");
                                                        }
                                                        _reportTableModel.addElement(f_srcMirror.getName(), "Update", "DST -> SRC", "OK");
                                                    }

                                                    LogManager.write("Datamirror generator - Setting timestamp: ["+f_srcMirror.getAbsolutePath()+"] - ["+convertTime(dstList[i].lastModified())+"].", LogHandle.ALL,LogManager.DEBUG);
                                                    f_srcMirror.setLastModified(dstList[i].lastModified());
                                                }
                                                else
                                                {
                                                    if (_vars.getReportToFile()) {
                                                        _reporter.write(f_srcMirror.getAbsolutePath(), f_srcMirror.length(), f_srcMirror.lastModified(), dstList[i].getAbsolutePath(), dstList[i].length(), dstList[i].lastModified(), "Update", "DST -> SRC", "Fail");
                                                    }
                                                    _reportTableModel.addElement(f_srcMirror.getName(), "Update", "DST -> SRC", "Fail");
                                                    LogManager.write("Datamirror generator - Warning: Failed to copy From: ["+dstList[i].getAbsolutePath()+"] To: ["+f_srcMirror.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                                                }
                                            }
                                            else
                                            {
                                                if (_vars.getReportToFile()) {
                                                    _reporter.write(f_srcMirror.getAbsolutePath(), f_srcMirror.length(), f_srcMirror.lastModified(), dstList[i].getAbsolutePath(), dstList[i].length(), dstList[i].lastModified(), "Update", "DST -> SRC", "Fail");
                                                }
                                                _reportTableModel.addElement(f_srcMirror.getName(), "Remove", "SRC", "Fail");
                                                LogManager.write("Datamirror generator - Warning: Failed to delete file: ["+f_srcMirror.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                                            }
                                        }
                                    }
                                    break;
                                case 2:
                                    // Custom settings

                                    File srcName;
                                    File cmpName = f_srcMirror;

                                    // Size of the file we are comparing
                                    long srcSize;
                                    // Size against which we are comparing
                                    long cmpSize;

                                    // Get the file timestamp
                                    if (_vars.getSizeValue01() == 0)
                                    {
                                        srcName = dstList[i];
                                        srcSize = srcName.length();
                                    }
                                    else if (_vars.getSizeValue01() == 1)
                                    {
                                        srcName = f_srcMirror;
                                        srcSize = srcName.length();
                                    }
                                    else
                                    {
                                        LogManager.write("Datamirror generator - Warning: Unrecognized location: ["+_vars.getDateValue01()+"]. Possible Values are [0, 1].", LogHandle.ALL,LogManager.MAJOR);
                                        break;
                                    }

                                    if (_vars.getSizeValue02() == 0)
                                    {
                                        cmpName = dstList[i];
                                        cmpSize = cmpName.length();
                                    }
                                    else if (_vars.getSizeValue02() == 1)
                                    {
                                        cmpName = f_srcMirror;
                                        cmpSize = cmpName.length();
                                    }
                                    else if (_vars.getSizeValue02() == 2)
                                    {
                                        cmpSize = Long.valueOf(_vars.getFileSize()).longValue();
                                    }
                                    else
                                    {
                                        LogManager.write("Datamirror generator - Warning: Unrecognized location: ["+_vars.getDateValue01()+"]. Possible Values are [0, 1, or 2].", LogHandle.ALL,LogManager.MAJOR);
                                        break;
                                    }

                                    // Make sure that the target and the destination are not the same file.
                                    if (_vars.getSizeValue01() == _vars.getSizeValue02())
                                    {
                                        LogManager.write("Datamirror generator - Custom Action: Cannot Compare - Target: ["+srcName.getAbsolutePath()+"] and Destination: ["+cmpName.getAbsolutePath()+"] are the same. Skipping.", LogHandle.ALL,LogManager.MINOR);
                                        break;
                                    }

                                    switch (_vars.getSizeCondition())
                                    {
                                        case 0:
                                            // If <src_path>  BIGGER_THAN ...
                                            if (srcSize > cmpSize)
                                            {
                                                customAction(_vars.getSizeModifyDir(), dstList[i], f_srcMirror);
                                            }
                                            break;
                                        case 1:
                                            // If <src_path>  SMALLER_THAN ...
                                            if (srcSize >= cmpSize)
                                            {
                                                customAction(_vars.getSizeModifyDir(), dstList[i], f_srcMirror);
                                            }
                                            break;
                                        case 2:
                                            // If <src_path>  SAME_AS ...
                                            if (srcSize < cmpSize)
                                            {
                                                customAction(_vars.getSizeModifyDir(), dstList[i], f_srcMirror);
                                            }
                                            break;
                                        case 3:
                                            // If <src_path>  DIFFERENT_THAN ...
                                            if (srcSize <= cmpSize)
                                            {
                                                customAction(_vars.getSizeModifyDir(), dstList[i], f_srcMirror);
                                            }
                                            break;
                                        case 4:
                                            // If <src_path>  DIFFERENT_THAN ...
                                            if (srcSize != cmpSize)
                                            {
                                                customAction(_vars.getSizeModifyDir(), dstList[i], f_srcMirror);
                                            }
                                            break;
                                        case 5:
                                            // If <src_path>  DIFFERENT_THAN ...
                                            if (srcSize == cmpSize)
                                            {
                                                customAction(_vars.getSizeModifyDir(), srcName, f_srcMirror);
                                            }
                                            break;
                                        default:
                                            break;
                                    }
                                    break;
                                default:
                                    // Ignore this case
                                    break;
                            }
                            break;
                        default:
                            // Ignore this case
                            break;
                    }
                }
                else
                {
                    // If selected to modify SRC or BOTH we proceed
                    if (_vars.getModifyDir() != 1)
                    {
                        if (_fileMan.copyFile(dstList[i].getAbsolutePath(), f_srcMirror.getAbsolutePath()))
                        {
                            // Update Total amount of data copied
                            _totalSize = _totalSize + dstList[i].length();

                            // Log report information
                            if (_vars.getReportLevel() < 2) {
                                // Info = 0
                                // Update = 1
                                // Error = 2
                                if (_vars.getReportToFile()) {
                                    _reporter.write(f_srcMirror.getAbsolutePath(), f_srcMirror.length(), f_srcMirror.lastModified(), dstList[i].getAbsolutePath(), dstList[i].length(), dstList[i].lastModified(), "Copy", "DST -> SRC", "OK");
                                }
                                _reportTableModel.addElement(f_srcMirror.getName(), "Copy", "DST -> SRC", "OK");
                            }

                            LogManager.write("Datamirror generator - Setting timestamp: ["+f_srcMirror.getAbsolutePath()+"] - ["+convertTime(dstList[i].lastModified())+"].", LogHandle.ALL,LogManager.DEBUG);
                            f_srcMirror.setLastModified(dstList[i].lastModified());
                        }
                        else
                        {
                            if (_vars.getReportToFile()) {
                                _reporter.write(f_srcMirror.getAbsolutePath(), f_srcMirror.length(), f_srcMirror.lastModified(), dstList[i].getAbsolutePath(), dstList[i].length(), dstList[i].lastModified(), "Copy", "DST -> SRC", "Fail");
                            }
                            _reportTableModel.addElement(f_srcMirror.getName(), "Copy", "DST -> SRC", "Fail");
                            LogManager.write("Datamirror generator - Warning: Failed to copy From: ["+dstList[i].getAbsolutePath()+"] To: ["+f_srcMirror.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                        }
                    }
                }
            }
            if (_jobDone >= _totalFiles && !_progress.isIndeterminate()) {
                _progress.setValue(_totalFiles);
            } else {
                _progress.setIndeterminate(true);
            }
            _numFiles.setText(Integer.toString(_totalFiles));
            _dataSize.setText(getCopySize(_totalSize));
        }
    }

    private void drillModifDst(File srcFolder, File dstFolder)
    {
        LogManager.write("Datamirror generator - Scanning SRC directory: ["+srcFolder.getAbsolutePath()+"].", LogHandle.ALL,LogManager.DEBUG);
        // Go trough each file in SRC directory

        File[] srcList = srcFolder.listFiles();
        LogManager.write("Datamirror generator - SRC directory: ["+srcFolder.getName()+"] has: ["+srcList.length+"] files.", LogHandle.ALL,LogManager.DEBUG);

        for (int i=0; i < srcList.length; i++)
        {
            while (_paused && _running) {
                try{Thread.sleep(1000);}catch (Exception se) {
                    LogManager.write("Datamirror generator - Timer exception.", LogHandle.ALL,LogManager.MAJOR);
                }
            }

            if (!_running)
            {
                _stopped = true;
                return;
            }

            // Increment the number of files count
            _totalFiles++;

            // Get the mirrored filename
            String dstMirror = dstFolder.getAbsolutePath().concat("\\");
            dstMirror = dstMirror.concat(srcList[i].getName());
            File f_dstMirror = new File(dstMirror);

            LogManager.write("Datamirror generator - TARGET: ["+srcList[i].getAbsolutePath()+"], Size: ["+srcList[i].length()+"], Date: ["+convertTime(srcList[i].lastModified())+"].", LogHandle.ALL,LogManager.DEBUG);
            LogManager.write("Datamirror generator - DESTIN: ["+f_dstMirror.getAbsolutePath()+"], Size: ["+f_dstMirror.length()+"], Date: ["+convertTime(f_dstMirror.lastModified())+"].", LogHandle.ALL,LogManager.DEBUG);
            // If this is a dir, we make sure that the mirror dir exist and then drill down.
            if (srcList[i].isDirectory())
            {
                if (!f_dstMirror.exists())
                {
                    // If directory does not exist, we check if the user wants us to create it.
                    // If selected to modify DST or BOTH we proceed with the dir creation
                    if (_vars.getModifyDir() > 0)
                    {
                        LogManager.write("Datamirror generator - Creating directory: ["+f_dstMirror.getAbsolutePath()+"].", LogHandle.ALL,LogManager.DEBUG);
                        if (f_dstMirror.mkdir()) {
                            if (_vars.getReportLevel() < 2) {
                                // Info = 0
                                // Update = 1
                                // Error = 2
                                if (_vars.getReportToFile()) {
                                    _reporter.write(srcList[i].getAbsolutePath(), srcList[i].length(), srcList[i].lastModified(), f_dstMirror.getAbsolutePath(), f_dstMirror.length(), f_dstMirror.lastModified(), "MKDIR", "SRC -> DST", "OK");
                                }
                                _reportTableModel.addElement(f_dstMirror.getName(), "MKDIR", "DST", "OK");
                            }
                        } else {
                            if (_vars.getReportToFile()) {
                                _reporter.write(srcList[i].getAbsolutePath(), srcList[i].length(), srcList[i].lastModified(), f_dstMirror.getAbsolutePath(), f_dstMirror.length(), f_dstMirror.lastModified(), "MKDIR", "SRC -> DST", "Fail");
                            }
                            _reportTableModel.addElement(f_dstMirror.getName(), "MKDIR", "DST", "Fail");
                            LogManager.write("Datamirror generator - Warning: Failed to create directory: ["+f_dstMirror.getAbsolutePath()+"]. Directory will be skipped.", LogHandle.ALL,LogManager.MAJOR);
                            continue;
                        }
                    }
                }
                else if (f_dstMirror.isFile())
                {
                    // Bad coincidence where instead of a directory we have a file with the same name.
                    if (_vars.getReportToFile()) {
                        _reporter.write(srcList[i].getAbsolutePath(), srcList[i].length(), srcList[i].lastModified(), f_dstMirror.getAbsolutePath(), f_dstMirror.length(), f_dstMirror.lastModified(), "DIR -> File", "SRC -> DST", "Error");
                    }
                    _reportTableModel.addElement(f_dstMirror.getName(), "DIR -> File", "SRC -> DST", "Error");
                    LogManager.write("Datamirror generator - Warning: Mirror of directory: ["+srcList[i].getAbsolutePath()+"] is a file: ["+f_dstMirror.getAbsolutePath()+"]. Directory will be skipped.", LogHandle.ALL,LogManager.MAJOR);
                    continue;
                }

                if (f_dstMirror.isDirectory())
                {
                    // We have both directories, so DRILL DOWN if the User wants
                    if (_vars.getRecurse())
                    {
                        drillModifDst(srcList[i], f_dstMirror);
                    }
                }
                else
                {
                    // Handling of any other errors I didn't think of.
                    if (_vars.getReportToFile()) {
                        _reporter.write(srcList[i].getAbsolutePath(), srcList[i].length(), srcList[i].lastModified(), f_dstMirror.getAbsolutePath(), f_dstMirror.length(), f_dstMirror.lastModified(), "Unknown", "SRC -> DST", "Error");
                    }
                    _reportTableModel.addElement(f_dstMirror.getName(), "Unknown", "DST", "Error");
                    LogManager.write("Datamirror generator - Warning: Unknown error at: ["+f_dstMirror.getAbsolutePath()+"]. Skipped.", LogHandle.ALL,LogManager.MAJOR);
                }
            }
            else
            {
                // Here starts the file manipulation
                if (f_dstMirror.isFile())
                {
                    // Check what action the user defined
                    switch (_vars.getAction())
                    {
                        case 0:
                            // No action should be taken. Proceed with the next file.
                            if (_vars.getReportLevel() < 1) {
                                // Info = 0
                                // Update = 1
                                // Error = 2
                                if (_vars.getReportToFile()) {
                                    _reporter.write(srcList[i].getAbsolutePath(), srcList[i].length(), srcList[i].lastModified(), f_dstMirror.getAbsolutePath(), f_dstMirror.length(), f_dstMirror.lastModified(), "Skipped", "SRC -> DST", "OK");
                                }
                                _reportTableModel.addElement(f_dstMirror.getName(), "Skipped", "SRC -> DST", "OK");
                            }
                            break;
                        case 1:
                            // Decide by DATE
                            switch (_vars.getActionDate())
                            {
                                case 0:
                                    // Keep the newest file
                                    if (srcList[i].lastModified() > f_dstMirror.lastModified())
                                    {
                                        // If file matches the condition, we check if the user wants us to copy it.
                                        // If selected to modify DST or BOTH we proceed with the dir creation
                                        if (_vars.getModifyDir() > 0)
                                        {
                                            LogManager.write("Datamirror generator - Deleting file: ["+f_dstMirror.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                                            if (f_dstMirror.delete())
                                            {
                                                if (_fileMan.copyFile(srcList[i].getAbsolutePath(), f_dstMirror.getAbsolutePath()))
                                                {
                                                    // Update Total amount of data copied
                                                    _totalSize = _totalSize + srcList[i].length();

                                                    // Log report information
                                                    if (_vars.getReportLevel() < 2) {
                                                        // Info = 0
                                                        // Update = 1
                                                        // Error = 2
                                                        if (_vars.getReportToFile()) {
                                                            _reporter.write(srcList[i].getAbsolutePath(), srcList[i].length(), srcList[i].lastModified(), f_dstMirror.getAbsolutePath(), f_dstMirror.length(), f_dstMirror.lastModified(), "Update", "SRC -> DST", "OK");
                                                        }
                                                        _reportTableModel.addElement(f_dstMirror.getName(), "Update", "SRC -> DST", "OK");
                                                    }

                                                    LogManager.write("Datamirror generator - Setting timestamp: ["+f_dstMirror.getAbsolutePath()+"] - ["+convertTime(srcList[i].lastModified())+"].", LogHandle.ALL,LogManager.DEBUG);
                                                    f_dstMirror.setLastModified(srcList[i].lastModified());
                                                }
                                                else
                                                {
                                                    if (_vars.getReportToFile()) {
                                                        _reporter.write(srcList[i].getAbsolutePath(), srcList[i].length(), srcList[i].lastModified(), f_dstMirror.getAbsolutePath(), f_dstMirror.length(), f_dstMirror.lastModified(), "Update", "SRC -> DST", "Fail");
                                                    }
                                                    _reportTableModel.addElement(f_dstMirror.getName(), "Update", "SRC -> DST", "Fail");
                                                    LogManager.write("Datamirror generator - Warning: Failed to copy From: ["+srcList[i].getAbsolutePath()+"] To: ["+f_dstMirror.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                                                }
                                            }
                                            else
                                            {
                                                if (_vars.getReportToFile()) {
                                                    _reporter.write(srcList[i].getAbsolutePath(), srcList[i].length(), srcList[i].lastModified(), f_dstMirror.getAbsolutePath(), f_dstMirror.length(), f_dstMirror.lastModified(), "Update", "SRC -> DST", "Fail");
                                                }
                                                _reportTableModel.addElement(f_dstMirror.getName(), "Remove", "DST", "Fail");
                                                LogManager.write("Datamirror generator - Warning: Failed to delete file: ["+f_dstMirror.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                                            }
                                        }
                                    }
                                    break;
                                case 1:
                                    // Keep the oldest file
                                    if (srcList[i].lastModified() < f_dstMirror.lastModified())
                                    {
                                        // If file matches the condition, we check if the user wants us to copy it.
                                        // If selected to modify DST or BOTH we proceed with the dir creation
                                        if (_vars.getModifyDir() > 0)
                                        {
                                            LogManager.write("Datamirror generator - Deleting file: ["+f_dstMirror.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                                            if (f_dstMirror.delete())
                                            {
                                                if (_fileMan.copyFile(srcList[i].getAbsolutePath(), f_dstMirror.getAbsolutePath()))
                                                {
                                                    // Update Total amount of data copied
                                                    _totalSize = _totalSize + srcList[i].length();

                                                    // Log report information
                                                    if (_vars.getReportLevel() < 2) {
                                                        // Info = 0
                                                        // Update = 1
                                                        // Error = 2
                                                        if (_vars.getReportToFile()) {
                                                            _reporter.write(srcList[i].getAbsolutePath(), srcList[i].length(), srcList[i].lastModified(), f_dstMirror.getAbsolutePath(), f_dstMirror.length(), f_dstMirror.lastModified(), "Update", "SRC -> DST", "OK");
                                                        }
                                                        _reportTableModel.addElement(f_dstMirror.getName(), "Update", "SRC -> DST", "OK");
                                                    }

                                                    LogManager.write("Datamirror generator - Setting timestamp: ["+f_dstMirror.getAbsolutePath()+"] - ["+convertTime(srcList[i].lastModified())+"].", LogHandle.ALL,LogManager.DEBUG);
                                                    f_dstMirror.setLastModified(srcList[i].lastModified());
                                                }
                                                else
                                                {
                                                    if (_vars.getReportToFile()) {
                                                        _reporter.write(srcList[i].getAbsolutePath(), srcList[i].length(), srcList[i].lastModified(), f_dstMirror.getAbsolutePath(), f_dstMirror.length(), f_dstMirror.lastModified(), "Update", "SRC -> DST", "Fail");
                                                    }
                                                    _reportTableModel.addElement(f_dstMirror.getName(), "Update", "SRC -> DST", "Fail");
                                                    LogManager.write("Datamirror generator - Warning: Failed to copy From: ["+srcList[i].getAbsolutePath()+"] To: ["+f_dstMirror.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                                                }
                                            }
                                            else
                                            {
                                                if (_vars.getReportToFile()) {
                                                    _reporter.write(srcList[i].getAbsolutePath(), srcList[i].length(), srcList[i].lastModified(), f_dstMirror.getAbsolutePath(), f_dstMirror.length(), f_dstMirror.lastModified(), "Update", "SRC -> DST", "Fail");
                                                }
                                                _reportTableModel.addElement(f_dstMirror.getName(), "Remove", "DST", "Fail");
                                                LogManager.write("Datamirror generator - Warning: Failed to delete file: ["+f_dstMirror.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                                            }
                                        }
                                    }
                                    break;
                                case 2:
                                    // Custom settings

                                    // Set default values
                                    File srcName;
                                    File cmpName = f_dstMirror;

                                    // Timestamp of the file we are comparing
                                    long src_timestamp;
                                    // Timestamp against which we are comparing
                                    long cmp_timestamp = 0;

                                    // Get the file timestamps
                                    if (_vars.getDateValue01() == 0)
                                    {
                                        srcName = srcList[i];
                                        src_timestamp = srcName.lastModified();
                                    }
                                    else if (_vars.getDateValue01() == 1)
                                    {
                                        srcName = f_dstMirror;
                                        src_timestamp = srcName.lastModified();
                                    }
                                    else
                                    {
                                        LogManager.write("Datamirror generator - Warning: Unrecognized location: ["+_vars.getDateValue01()+"]. Possible Values are [0, 1].", LogHandle.ALL,LogManager.MAJOR);
                                        break;
                                    }

                                    if (_vars.getDateValue02() == 0)
                                    {
                                        cmpName = srcList[i];
                                        cmp_timestamp = cmpName.lastModified();
                                    }
                                    else if (_vars.getDateValue02() == 1)
                                    {
                                        cmpName = f_dstMirror;
                                        cmp_timestamp = cmpName.lastModified();
                                    }
                                    else if (_vars.getDateValue02() > 6)
                                    {
                                        LogManager.write("Datamirror generator - Warning: Unrecognized location: ["+_vars.getDateValue01()+"]. Possible Values are [0, 1].", LogHandle.ALL,LogManager.MAJOR);
                                        break;
                                    }

                                    // Make sure that the target and the destination are not the same file.
                                    if (_vars.getDateValue01() == _vars.getDateValue02())
                                    {
                                        LogManager.write("Datamirror generator - Custom Action: Cannot Compare - Target: ["+srcName.getAbsolutePath()+"] and Destination: ["+cmpName.getAbsolutePath()+"] are the same. Skipping.", LogHandle.ALL,LogManager.MINOR);
                                        break;
                                    }

                                    // Get the timestamp in seconds against which we will compare
                                    long now = System.currentTimeMillis();

                                    switch (_vars.getDateValue02())
                                    {
                                        case 0:
                                            // <src_path>
                                            // Alredy defined above
                                            break;
                                        case 1:
                                            // <dst_path>
                                            // Alredy defined above
                                            break;
                                        case 2:
                                            // <hour>
                                            cmp_timestamp = now - 3600000;
                                            break;
                                        case 3:
                                            // <day>
                                            cmp_timestamp = now - 86400000;
                                            break;
                                        case 4:
                                            // <week>
                                            cmp_timestamp = now - 604800000;
                                            break;
                                        case 5:
                                            // <month> (assume the max length of the month - 31 days)
                                            cmp_timestamp = now - 2678400;
                                            cmp_timestamp = cmp_timestamp*1000;
                                            break;
                                        case 6:
                                            // <year>
                                            cmp_timestamp = now - 31536000;
                                            cmp_timestamp = cmp_timestamp*1000;
                                            break;
                                        default:
                                            cmp_timestamp = 0;
                                            LogManager.write("Datamirror generator - Warning: Unrecognized time period: ["+_vars.getDateValue01()+"]. Possible Values are [0, 6].", LogHandle.ALL,LogManager.MAJOR);
                                            break;
                                    }

                                    switch (_vars.getDateCondition())
                                    {
                                        case 0:
                                            // If <src_path>  OLDER_THAN ...
                                            if (src_timestamp < cmp_timestamp)
                                            {
                                                customAction(_vars.getDateModifyDir(), srcList[i], f_dstMirror);
                                            }
                                            break;
                                        case 1:
                                            // If <src_path>  NEWER_THAN ...
                                            if (src_timestamp > cmp_timestamp)
                                            {
                                                customAction(_vars.getDateModifyDir(), srcList[i], f_dstMirror);
                                            }
                                            break;
                                        case 2:
                                            // If <src_path>  SAME_AS ...
                                            if (src_timestamp == cmp_timestamp)
                                            {
                                                customAction(_vars.getDateModifyDir(), srcList[i], f_dstMirror);
                                            }
                                            break;
                                        case 3:
                                            // If <src_path>  DIFFERENT_THAN ...
                                            if (src_timestamp != cmp_timestamp)
                                            {
                                                customAction(_vars.getDateModifyDir(), srcList[i], f_dstMirror);
                                            }
                                            break;
                                        default:
                                            break;
                                    }
                                    break;
                                default:
                                    // Ignore this case
                                    break;
                            }
                            break;
                        case 2:
                            // Decide by SIZE
                            switch (_vars.getActionSize())
                            {
                                case 0:
                                    // Keeep the biggest file
                                    if (srcList[i].length() > f_dstMirror.length())
                                    {
                                        // If file matches the condition, we check if the user wants us to copy it.
                                        // If selected to modify DST or BOTH we proceed with the dir creation
                                        if (_vars.getModifyDir() > 0)
                                        {
                                            LogManager.write("Datamirror generator - Deleting file: ["+f_dstMirror.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                                            if (f_dstMirror.delete())
                                            {
                                                if (_fileMan.copyFile(srcList[i].getAbsolutePath(), f_dstMirror.getAbsolutePath()))
                                                {
                                                    // Update Total amount of data copied
                                                    _totalSize = _totalSize + srcList[i].length();

                                                    // Log report information
                                                    if (_vars.getReportLevel() < 2) {
                                                        // Info = 0
                                                        // Update = 1
                                                        // Error = 2
                                                        if (_vars.getReportToFile()) {
                                                            _reporter.write(srcList[i].getAbsolutePath(), srcList[i].length(), srcList[i].lastModified(), f_dstMirror.getAbsolutePath(), f_dstMirror.length(), f_dstMirror.lastModified(), "Update", "SRC -> DST", "OK");
                                                        }
                                                        _reportTableModel.addElement(f_dstMirror.getName(), "Update", "SRC -> DST", "OK");
                                                    }

                                                    LogManager.write("Datamirror generator - Setting timestamp: ["+f_dstMirror.getAbsolutePath()+"] - ["+convertTime(srcList[i].lastModified())+"].", LogHandle.ALL,LogManager.DEBUG);
                                                    f_dstMirror.setLastModified(srcList[i].lastModified());
                                                }
                                                else
                                                {
                                                    if (_vars.getReportToFile()) {
                                                        _reporter.write(srcList[i].getAbsolutePath(), srcList[i].length(), srcList[i].lastModified(), f_dstMirror.getAbsolutePath(), f_dstMirror.length(), f_dstMirror.lastModified(), "Update", "SRC -> DST", "Fail");
                                                    }
                                                    _reportTableModel.addElement(f_dstMirror.getName(), "Update", "SRC -> DST", "Fail");
                                                    LogManager.write("Datamirror generator - Warning: Failed to copy From: ["+srcList[i].getAbsolutePath()+"] To: ["+f_dstMirror.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                                                }
                                            }
                                            else
                                            {
                                                if (_vars.getReportToFile()) {
                                                    _reporter.write(srcList[i].getAbsolutePath(), srcList[i].length(), srcList[i].lastModified(), f_dstMirror.getAbsolutePath(), f_dstMirror.length(), f_dstMirror.lastModified(), "Update", "SRC -> DST", "Fail");
                                                }
                                                _reportTableModel.addElement(f_dstMirror.getName(), "Remove", "DST", "Fail");
                                                LogManager.write("Datamirror generator - Warning: Failed to delete file: ["+f_dstMirror.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                                            }
                                        }
                                    }
                                    break;
                                case 1:
                                    // Keeep the smallest file
                                    if (srcList[i].length() < f_dstMirror.length())
                                    {
                                        // If file matches the condition, we check if the user wants us to copy it.
                                        // If selected to modify DST or BOTH we proceed with the dir creation
                                        if (_vars.getModifyDir() > 0)
                                        {
                                            LogManager.write("Datamirror generator - Deleting file: ["+f_dstMirror.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                                            if (f_dstMirror.delete())
                                            {
                                                if (_fileMan.copyFile(srcList[i].getAbsolutePath(), f_dstMirror.getAbsolutePath()))
                                                {
                                                    // Update Total amount of data copied
                                                    _totalSize = _totalSize + srcList[i].length();

                                                    // Log report information
                                                    if (_vars.getReportLevel() < 2) {
                                                        // Info = 0
                                                        // Update = 1
                                                        // Error = 2
                                                        if (_vars.getReportToFile()) {
                                                            _reporter.write(srcList[i].getAbsolutePath(), srcList[i].length(), srcList[i].lastModified(), f_dstMirror.getAbsolutePath(), f_dstMirror.length(), f_dstMirror.lastModified(), "Update", "SRC -> DST", "OK");
                                                        }
                                                        _reportTableModel.addElement(f_dstMirror.getName(), "Update", "SRC -> DST", "OK");
                                                    }

                                                    LogManager.write("Datamirror generator - Setting timestamp: ["+f_dstMirror.getAbsolutePath()+"] - ["+convertTime(srcList[i].lastModified())+"].", LogHandle.ALL,LogManager.DEBUG);
                                                    f_dstMirror.setLastModified(srcList[i].lastModified());
                                                }
                                                else
                                                {
                                                    if (_vars.getReportToFile()) {
                                                        _reporter.write(srcList[i].getAbsolutePath(), srcList[i].length(), srcList[i].lastModified(), f_dstMirror.getAbsolutePath(), f_dstMirror.length(), f_dstMirror.lastModified(), "Update", "SRC -> DST", "Fail");
                                                    }
                                                    _reportTableModel.addElement(f_dstMirror.getName(), "Update", "SRC -> DST", "Fail");
                                                    LogManager.write("Datamirror generator - Warning: Failed to copy From: ["+srcList[i].getAbsolutePath()+"] To: ["+f_dstMirror.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                                                }
                                            }
                                            else
                                            {
                                                if (_vars.getReportToFile()) {
                                                    _reporter.write(srcList[i].getAbsolutePath(), srcList[i].length(), srcList[i].lastModified(), f_dstMirror.getAbsolutePath(), f_dstMirror.length(), f_dstMirror.lastModified(), "Update", "SRC -> DST", "Fail");
                                                }
                                                _reportTableModel.addElement(f_dstMirror.getName(), "Remove", "DST", "Fail");
                                                LogManager.write("Datamirror generator - Warning: Failed to delete file: ["+f_dstMirror.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                                            }
                                        }
                                    }
                                    break;
                                case 2:
                                    // Custom settings

                                    File srcName;
                                    File cmpName = f_dstMirror;

                                    // Size of the file we are comparing
                                    long srcSize;
                                    // Size against which we are comparing
                                    long cmpSize;

                                    // Get the file timestamp
                                    if (_vars.getSizeValue01() == 0)
                                    {
                                        srcName = srcList[i];
                                        srcSize = srcName.length();
                                    }
                                    else if (_vars.getSizeValue01() == 1)
                                    {
                                        srcName = f_dstMirror;
                                        srcSize = srcName.length();
                                    }
                                    else
                                    {
                                        LogManager.write("Datamirror generator - Warning: Unrecognized location: ["+_vars.getDateValue01()+"]. Possible Values are [0, 1].", LogHandle.ALL,LogManager.MAJOR);
                                        break;
                                    }

                                    if (_vars.getSizeValue02() == 0)
                                    {
                                        cmpName = srcList[i];
                                        cmpSize = cmpName.length();
                                    }
                                    else if (_vars.getSizeValue02() == 1)
                                    {
                                        cmpName = f_dstMirror;
                                        cmpSize = cmpName.length();
                                    }
                                    else if (_vars.getSizeValue02() == 2)
                                    {
                                        cmpSize = Long.valueOf(_vars.getFileSize()).longValue();
                                    }
                                    else
                                    {
                                        LogManager.write("Datamirror generator - Warning: Unrecognized location: ["+_vars.getDateValue01()+"]. Possible Values are [0, 1, or 2].", LogHandle.ALL,LogManager.MAJOR);
                                        break;
                                    }

                                    // Make sure that the target and the destination are not the same file.
                                    if (_vars.getSizeValue01() == _vars.getSizeValue02())
                                    {
                                        LogManager.write("Datamirror generator - Custom Action: Cannot Compare - Target: ["+srcName.getAbsolutePath()+"] and Destination: ["+cmpName.getAbsolutePath()+"] are the same. Skipping.", LogHandle.ALL,LogManager.MINOR);
                                        break;
                                    }

                                    switch (_vars.getSizeCondition())
                                    {
                                        case 0:
                                            // If <src_path>  BIGGER_THAN ...
                                            if (srcSize > cmpSize)
                                            {
                                                customAction(_vars.getSizeModifyDir(), srcList[i], f_dstMirror);
                                            }
                                            break;
                                        case 1:
                                            // If <src_path>  SMALLER_THAN ...
                                            if (srcSize >= cmpSize)
                                            {
                                                customAction(_vars.getSizeModifyDir(), srcList[i], f_dstMirror);
                                            }
                                            break;
                                        case 2:
                                            // If <src_path>  SAME_AS ...
                                            if (srcSize < cmpSize)
                                            {
                                                customAction(_vars.getSizeModifyDir(), srcList[i], f_dstMirror);
                                            }
                                            break;
                                        case 3:
                                            // If <src_path>  DIFFERENT_THAN ...
                                            if (srcSize <= cmpSize)
                                            {
                                                customAction(_vars.getSizeModifyDir(), srcList[i], f_dstMirror);
                                            }
                                            break;
                                        case 4:
                                            // If <src_path>  DIFFERENT_THAN ...
                                            if (srcSize != cmpSize)
                                            {
                                                customAction(_vars.getSizeModifyDir(), srcList[i], f_dstMirror);
                                            }
                                            break;
                                        case 5:
                                            // If <src_path>  DIFFERENT_THAN ...
                                            if (srcSize == cmpSize)
                                            {
                                                customAction(_vars.getSizeModifyDir(), srcName, f_dstMirror);
                                            }
                                            break;
                                        default:
                                            break;
                                    }
                                    break;
                                default:
                                    // Ignore this case
                                    break;
                            }
                            break;
                        default:
                            // Ignore this case
                            break;
                    }
                }
                else
                {
                    // If selected to modify DST or BOTH we proceed
                    if (_vars.getModifyDir() > 0)
                    {
                        if (_fileMan.copyFile(srcList[i].getAbsolutePath(), f_dstMirror.getAbsolutePath()))
                        {
                            // Update Total amount of data copied
                            _totalSize = _totalSize + srcList[i].length();

                            // Log report information
                            if (_vars.getReportLevel() < 2) {
                                // Info = 0
                                // Update = 1
                                // Error = 2
                                if (_vars.getReportToFile()) {
                                    _reporter.write(srcList[i].getAbsolutePath(), srcList[i].length(), srcList[i].lastModified(), f_dstMirror.getAbsolutePath(), f_dstMirror.length(), f_dstMirror.lastModified(), "Copy", "SRC -> DST", "OK");
                                }
                                _reportTableModel.addElement(f_dstMirror.getName(), "Copy", "SRC -> DST", "OK");
                            }

                            LogManager.write("Datamirror generator - Setting timestamp: ["+f_dstMirror.getAbsolutePath()+"] - ["+convertTime(srcList[i].lastModified())+"].", LogHandle.ALL,LogManager.DEBUG);
                            f_dstMirror.setLastModified(srcList[i].lastModified());
                        }
                        else
                        {
                            if (_vars.getReportToFile()) {
                                _reporter.write(srcList[i].getAbsolutePath(), srcList[i].length(), srcList[i].lastModified(), f_dstMirror.getAbsolutePath(), f_dstMirror.length(), f_dstMirror.lastModified(), "Copy", "SRC -> DST", "Fail");
                            }
                            _reportTableModel.addElement(f_dstMirror.getName(), "Copy", "SRC -> DST", "Fail");
                            LogManager.write("Datamirror generator - Warning: Failed to copy From: ["+srcList[i].getAbsolutePath()+"] To: ["+f_dstMirror.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                        }
                    }
                }
            }
            if (_jobDone >= _totalFiles && !_progress.isIndeterminate()) {
                _progress.setValue(_totalFiles);
            } else {
                _progress.setIndeterminate(true);
            }
            _numFiles.setText(Integer.toString(_totalFiles));
            _dataSize.setText(getCopySize(_totalSize));
        }
    }

    // Define the users selected action
    private void customAction(int userAction, File s_File, File d_File)
    {
        switch (userAction)
        {
            case 0:
                // Overwrite SRC

                // If file matches the condition, we check if the user wants us to copy it.
                // If selected to modify SRC or BOTH we proceed with replacing the file
                if (_vars.getModifyDir() != 1)
                {
                    // Delete the old file
                    LogManager.write("Datamirror generator - Deleting file: ["+s_File.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                    if (s_File.delete())
                    {
                        if (_fileMan.copyFile(d_File.getAbsolutePath(), s_File.getAbsolutePath()))
                        {
                            // Update Total amount of data copied
                            _totalSize = _totalSize + d_File.length();

                            // Log report information
                            if (_vars.getReportLevel() < 2) {
                                // Info = 0
                                // Update = 1
                                // Error = 2
                                if (_vars.getReportToFile()) {
                                    _reporter.write(s_File.getAbsolutePath(), s_File.length(), s_File.lastModified(), d_File.getAbsolutePath(), d_File.length(), d_File.lastModified(), "Update", "DST -> SRC", "OK");
                                }
                                _reportTableModel.addElement(d_File.getName(), "Update", "DST -> SRC", "OK");
                            }

                            LogManager.write("Datamirror generator - Setting timestamp: ["+s_File.getAbsolutePath()+"] - ["+convertTime(d_File.lastModified())+"].", LogHandle.ALL,LogManager.DEBUG);
                            s_File.setLastModified(d_File.lastModified());
                        }
                        else
                        {
                            if (_vars.getReportToFile()) {
                                _reporter.write(s_File.getAbsolutePath(), s_File.length(), s_File.lastModified(), d_File.getAbsolutePath(), d_File.length(), d_File.lastModified(), "Update", "DST -> SRC", "Fail");
                            }
                            _reportTableModel.addElement(d_File.getName(), "Update", "DST -> SRC", "Fail");
                            LogManager.write("Datamirror generator - Warning: Failed to copy From: ["+d_File.getAbsolutePath()+"] To: ["+s_File.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                        }
                    }
                    else
                    {
                        if (_vars.getReportToFile()) {
                            _reporter.write(s_File.getAbsolutePath(), s_File.length(), s_File.lastModified(), d_File.getAbsolutePath(), d_File.length(), d_File.lastModified(), "Remove", "SRC", "Fail");
                        }
                        _reportTableModel.addElement(d_File.getName(), "Remove", "SRC", "Fail");
                        LogManager.write("Datamirror generator - Warning: Failed to remove file: ["+s_File.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                    }
                }
                break;
            case 1:
                // Overwrite DST

                // If file matches the condition, we check if the user wants us to copy it.
                // If selected to modify DST or BOTH we proceed with replacing the file
                if (_vars.getModifyDir() > 0)
                {
                    // Delete the old file
                    LogManager.write("Datamirror generator - Deleting file: ["+d_File.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                    if (d_File.delete())
                    {
                        if (_fileMan.copyFile(s_File.getAbsolutePath(), d_File.getAbsolutePath()))
                        {
                            // Update Total amount of data copied
                            _totalSize = _totalSize + s_File.length();

                            // Log report information
                            if (_vars.getReportLevel() < 2) {
                                // Info = 0
                                // Update = 1
                                // Error = 2
                                if (_vars.getReportToFile()) {
                                    _reporter.write(s_File.getAbsolutePath(), s_File.length(), s_File.lastModified(), d_File.getAbsolutePath(), d_File.length(), d_File.lastModified(), "Update", "SRC -> DST", "OK");
                                }
                                _reportTableModel.addElement(d_File.getName(), "Update", "SRC -> DST", "OK");
                            }

                            LogManager.write("Datamirror generator - Setting timestamp: ["+d_File.getAbsolutePath()+"] - ["+convertTime(s_File.lastModified())+"].", LogHandle.ALL,LogManager.DEBUG);
                            d_File.setLastModified(s_File.lastModified());
                        }
                        else
                        {
                            if (_vars.getReportToFile()) {
                                _reporter.write(s_File.getAbsolutePath(), s_File.length(), s_File.lastModified(), d_File.getAbsolutePath(), d_File.length(), d_File.lastModified(), "Update", "SRC -> DST", "Fail");
                            }
                            _reportTableModel.addElement(d_File.getName(), "Update", "SRC -> DST", "Fail");
                            LogManager.write("Datamirror generator - Warning: Failed to copy From: ["+s_File.getAbsolutePath()+"] To: ["+d_File.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                        }
                    }
                    else
                    {
                        if (_vars.getReportToFile()) {
                            _reporter.write(s_File.getAbsolutePath(), s_File.length(), s_File.lastModified(), d_File.getAbsolutePath(), d_File.length(), d_File.lastModified(), "Remove", "DST", "Fail");
                        }
                        _reportTableModel.addElement(d_File.getName(), "Remove", "DST", "Fail");
                        LogManager.write("Datamirror generator - Warning: Failed to remove file: ["+d_File.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                    }
                }
                break;
            case 2:
                // Delete SRC

                // If file matches the condition, we check if the user wants us to delete it
                // If selected to modify SRC or BOTH we proceed with delete
                if (_vars.getModifyDir() != 1)
                {
                    // Delete the file
                    LogManager.write("Datamirror generator - Deleting file: ["+s_File.getAbsolutePath()+"].", LogHandle.ALL,LogManager.DEBUG);
                    if (s_File.delete()) {
                        if (_vars.getReportLevel() < 2) {
                            // Info = 0
                            // Update = 1
                            // Error = 2
                            if (_vars.getReportToFile()) {
                                _reporter.write(s_File.getAbsolutePath(), s_File.length(), s_File.lastModified(), d_File.getAbsolutePath(), d_File.length(), d_File.lastModified(), "Delete", "SRC", "OK");
                            }
                            _reportTableModel.addElement(d_File.getName(), "Delete", "SRC", "OK");
                        }
                    } else {
                        if (_vars.getReportToFile()) {
                            _reporter.write(s_File.getAbsolutePath(), s_File.length(), s_File.lastModified(), d_File.getAbsolutePath(), d_File.length(), d_File.lastModified(), "Delete", "SRC", "Fail");
                        }
                        _reportTableModel.addElement(d_File.getName(), "Delete", "SRC", "Fail");
                        LogManager.write("Datamirror generator - Warning: Failed to remove file: ["+s_File.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                    }
                }
                break;
            case 3:
                // Delete DST

                // If file matches the condition, we check if the user wants us to delete it.
                // If selected to modify SRC or BOTH we proceed with delete
                if (_vars.getModifyDir() > 0)
                {
                    // Delete the file
                    LogManager.write("Datamirror generator - Deleting file: ["+d_File.getAbsolutePath()+"].", LogHandle.ALL,LogManager.DEBUG);
                    if (d_File.delete()) {
                        if (_vars.getReportLevel() < 2) {
                            // Info = 0
                            // Update = 1
                            // Error = 2
                            if (_vars.getReportToFile()) {
                                _reporter.write(s_File.getAbsolutePath(), s_File.length(), s_File.lastModified(), d_File.getAbsolutePath(), d_File.length(), d_File.lastModified(), "Delete", "DST", "OK");
                            }
                            _reportTableModel.addElement(d_File.getName(), "Delete", "DST", "OK");
                        }
                    } else {
                        if (_vars.getReportToFile()) {
                            _reporter.write(s_File.getAbsolutePath(), s_File.length(), s_File.lastModified(), d_File.getAbsolutePath(), d_File.length(), d_File.lastModified(), "Delete", "DST", "Fail");
                        }
                        _reportTableModel.addElement(d_File.getName(), "Delete", "DST", "Fail");
                        LogManager.write("Datamirror generator - Warning: Failed to remove file: ["+d_File.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                    }
                }
                break;
            case 4:
                // Delete BOTH

                // If file matches the condition, we check if the user wants us to delete it
                // If selected to modify BOTH we proceed with delete
                if (_vars.getModifyDir() > 1)
                {
                    // Delete the file
                    LogManager.write("Datamirror generator - Deleting file: ["+s_File.getAbsolutePath()+"].", LogHandle.ALL,LogManager.DEBUG);
                    if (s_File.delete()) {
                        if (_vars.getReportLevel() < 2) {
                            // Info = 0
                            // Update = 1
                            // Error = 2
                            if (_vars.getReportToFile()) {
                                _reporter.write(s_File.getAbsolutePath(), s_File.length(), s_File.lastModified(), d_File.getAbsolutePath(), d_File.length(), d_File.lastModified(), "Delete", "SRC", "OK");
                            }
                            _reportTableModel.addElement(d_File.getName(), "Delete", "SRC", "OK");
                        }
                    } else {
                        if (_vars.getReportToFile()) {
                            _reporter.write(s_File.getAbsolutePath(), s_File.length(), s_File.lastModified(), d_File.getAbsolutePath(), d_File.length(), d_File.lastModified(), "Delete", "SRC", "Fail");
                        }_reportTableModel.addElement(d_File.getName(), "Delete", "SRC", "Fail");
                        LogManager.write("Datamirror generator - Warning: Failed to remove file: ["+s_File.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                    }

                    LogManager.write("Datamirror generator - Deleting file: ["+d_File.getAbsolutePath()+"].", LogHandle.ALL,LogManager.DEBUG);
                    if (d_File.delete()) {
                        if (_vars.getReportLevel() < 2) {
                            // Info = 0
                            // Update = 1
                            // Error = 2
                            if (_vars.getReportToFile()) {
                                _reporter.write(s_File.getAbsolutePath(), s_File.length(), s_File.lastModified(), d_File.getAbsolutePath(), d_File.length(), d_File.lastModified(), "Delete", "DST", "OK");
                            }
                            _reportTableModel.addElement(d_File.getName(), "Delete", "DST", "OK");
                        }
                    } else {
                        if (_vars.getReportToFile()) {
                            _reporter.write(s_File.getAbsolutePath(), s_File.length(), s_File.lastModified(), d_File.getAbsolutePath(), d_File.length(), d_File.lastModified(), "Delete", "DST", "Fail");
                        }
                        _reportTableModel.addElement(d_File.getName(), "Delete", "DST", "Fail");
                        LogManager.write("Datamirror generator - Warning: Failed to remove file: ["+d_File.getAbsolutePath()+"].", LogHandle.ALL,LogManager.MAJOR);
                    }
                }
                break;
            default:
                break;
        }
    }

    public boolean  startDMG(JButton startButton)
    {
        _running = true;
        _stopped = false;
        return generateDMG(startButton);
    }

    public void stopDMG()
    {
        _running = false;
        int cntr = 0;

        // Apply timeout of ten seconds
        while (!_stopped && cntr<10)
        {
            cntr++;
            try{Thread.sleep(1000);}catch (Exception se) {
                LogManager.write("Datamirror generator - Timer exception.", LogHandle.ALL,LogManager.MINOR);
            }
        }

        if (!_stopped) {
            LogManager.write("Datamirror generator - Data-mirroring failed to stop gracefully. Terminating it.", LogHandle.ALL,LogManager.MAJOR);
            _running = false;
            dmgGenerator = null;
            _stopped = true;
        } else {
            LogManager.write("Datamirror generator - Data-mirroring stopped gracefully.", LogHandle.ALL,LogManager.DEBUG);
        }
    }

    public boolean getPauseGenerator()
    {
        return _paused;
    }

    public void setPauseGenerator(boolean pause)
    {
        _paused = pause;
    }

    public boolean testGenerator()
    {
        return !_stopped;
    }

    // Verifies both locations (src, dst) that they are directories and writable at the same time.
    // Also checks for available space and if it is enough to proceed with the data transfer.
    private boolean verifyLocations(File src, File dst)
    {
        boolean status = true;
        //long spaceAvailable;
        //long spaceRequired;

        if (src.getAbsolutePath().equals(dst.getAbsolutePath()))
        {
            DMGOptionPane.showMessageDialog(new JFrame(), "Locations:\n" +
                    " - "+src.getAbsolutePath()+"\n"+
                    " - "+dst.getAbsolutePath()+"\n"+
                    "are equal.", "  Datamirror generator.  ", DMGOptionPane.ERROR_MESSAGE);
            LogManager.write("Datamirror generator - Locations SRC:["+src.getAbsolutePath()+"], DST:["+dst.getAbsolutePath()+"] are equal.", LogHandle.ALL,LogManager.CRITICAL);
            status = false;
        } else {
            LogManager.write("Datamirror generator - Verified: Locations SRC:["+src.getAbsolutePath()+"], DST:["+dst.getAbsolutePath()+"] are not equal.", LogHandle.ALL,LogManager.DEBUG);
        }

        if (!src.exists())
        {
            DMGOptionPane.showMessageDialog(new JFrame(), "Location:\n" +
                    " - "+src.getAbsolutePath()+
                    "\ndoes not exist.", "  Datamirror generator.  ", DMGOptionPane.ERROR_MESSAGE);
            LogManager.write("Datamirror generator - Location ["+src.getAbsolutePath()+"] does not exist.", LogHandle.ALL,LogManager.CRITICAL);
            status = false;
        }
        else
        {
            LogManager.write("Datamirror generator - Verified: Location ["+src.getAbsolutePath()+"] exists.", LogHandle.ALL,LogManager.DEBUG);

            if (!src.isDirectory())
            {
                DMGOptionPane.showMessageDialog(new JFrame(), "Location:\n" +
                        " - "+src.getAbsolutePath()+
                        "\nis not a valid directory.", "  Datamirror generator.  ", DMGOptionPane.ERROR_MESSAGE);
                LogManager.write("Datamirror generator - Location ["+src.getAbsolutePath()+"] is not a valid directory.", LogHandle.ALL,LogManager.CRITICAL);
                status = false;
            } else {
                LogManager.write("Datamirror generator - Verified: Location ["+src.getAbsolutePath()+"] is a valid directory.", LogHandle.ALL,LogManager.DEBUG);
            }
            
            if (!src.canWrite())
            {
                DMGOptionPane.showMessageDialog(new JFrame(), "Location:\n" +
                        " - "+src.getAbsolutePath()+
                        "\nis not writable. Please check its permissions.", "  Datamirror generator.  ", DMGOptionPane.ERROR_MESSAGE);
                LogManager.write("Datamirror generator - Location ["+src.getAbsolutePath()+"] is not writable. Verify permissions.", LogHandle.ALL,LogManager.CRITICAL);
                status = false;
            } else {
                LogManager.write("Datamirror generator - Verified: Location ["+src.getAbsolutePath()+"] is writable.", LogHandle.ALL,LogManager.DEBUG);
            }
        }

        if (!dst.exists())
        {
            DMGOptionPane.showMessageDialog(new JFrame(), "Location:\n" +
                    " - "+dst.getAbsolutePath()+
                    "\ndoes not exist.", "  Datamirror generator.  ", DMGOptionPane.ERROR_MESSAGE);
            LogManager.write("Datamirror generator - Location ["+dst.getAbsolutePath()+"] does not exist.", LogHandle.ALL,LogManager.CRITICAL);
            status = false;
        }
        else
        {
            LogManager.write("Datamirror generator - Verified: Location ["+dst.getAbsolutePath()+"] exists.", LogHandle.ALL,LogManager.DEBUG);

            if (!dst.isDirectory())
            {
                DMGOptionPane.showMessageDialog(new JFrame(), "Location:\n" +
                        " - "+dst.getAbsolutePath()+
                        "\nis not a valid directory.", "  Datamirror generator.  ", DMGOptionPane.ERROR_MESSAGE);
                LogManager.write("Datamirror generator - Location ["+dst.getAbsolutePath()+"] is not a valid directory.", LogHandle.ALL,LogManager.CRITICAL);
                status = false;
            } else {
                LogManager.write("Datamirror generator - Verified: Location ["+dst.getAbsolutePath()+"] is a valid directory.", LogHandle.ALL,LogManager.DEBUG);
            }
            
            if (!dst.canWrite())
            {
                DMGOptionPane.showMessageDialog(new JFrame(), "Location:\n" +
                        " - "+dst.getAbsolutePath()+
                        "\nis not writable. Please check its permissions.", "  Datamirror generator.  ", DMGOptionPane.ERROR_MESSAGE);
                LogManager.write("Datamirror generator - Location ["+dst.getAbsolutePath()+"] is not writable. Verify permissions.", LogHandle.ALL,LogManager.CRITICAL);
                status = false;
            } else {
                LogManager.write("Datamirror generator - Verified: Location ["+dst.getAbsolutePath()+"] is writable.", LogHandle.ALL,LogManager.CRITICAL);
            }
        }

        /* Still not implemented
        //
        // The space required for the mirroring cannot exceed
        // the sum of the sizes of both directories.
        spaceRequired = _fileMan.getFileSize(src);
        spaceRequired = spaceRequired + _fileMan.getFileSize(dst);

        spaceAvailable = _fileMan.getFreeSpace(src);
        // Make sure that there are 10Mb in reserve
        spaceAvailable = spaceAvailable - 10485760;

        if (spaceRequired >= spaceAvailable)
        {
            DMGOptionPane.showMessageDialog(new JFrame(), "Not enough space on disk:\n" +
                    "Required - "+spaceRequired+"\n"+
                    "Available - "+spaceAvailable, "  Datamirror generator.  ", DMGOptionPane.ERROR_MESSAGE);
            LogManager.write("Datamirror generator - Not enough space on disk. Required: ["+spaceRequired+"], Available: ["+spaceAvailable+"].", LogHandle.ALL,LogManager.CRITICAL);
            status = false;
        }
		*/
        
        if (status)
        {
            LogManager.write("Datamirror generator - Location verification: OK", LogHandle.ALL,LogManager.DEBUG);
            return true;
        }
        else
        {
            LogManager.write("Datamirror generator - Location verification: FAIL", LogHandle.ALL,LogManager.CRITICAL);
            return false;
        }
    }

    private void getNumFiles(File location) {
    	
    	File[] locList = null;
    	
        LogManager.write("Datamirror generator - Determining the number of files in: " + location, LogHandle.ALL,LogManager.DEBUG);
        
        if (location.isDirectory() && location.canRead()) {
        	locList = location.listFiles();
        } else {
            LogManager.write("Datamirror generator - Skipping non-readable directory: " + location, LogHandle.ALL,LogManager.MINOR);
            return;
        }

        for (int i=0; i < locList.length; i++) {

            _totalFiles++;

            if (locList[i].isDirectory()) {
                LogManager.write("Datamirror generator - Recursing: [" + location + "] -> [" + locList[i] + "]", LogHandle.ALL,LogManager.DEBUG);
                getNumFiles(locList[i]);
            }
        }
        
        LogManager.write("Datamirror generator - Identified " + _totalFiles + " files.", LogHandle.ALL,LogManager.DEBUG);
    }

    private String getCopySize(long totalSize) {

        if (totalSize < 1024) {
            return Long.toString(totalSize) + "B";
        } else if (totalSize < 1048576) {
            return Long.toString(totalSize/1024) + "kB";
        } else if (totalSize < 1073741824) {
            return Long.toString(totalSize/1048576) + "MB";
        } else if (totalSize >= 1073741824) {
            return Long.toString(totalSize/1024) + "GB";
        } else {
            return ":-)";
        }
    }

    private String convertTime(long timeMilliseconds)
    {
        String tmp;
        GregorianCalendar cal=new GregorianCalendar();
        StringBuffer currTime = new StringBuffer();

        cal.setTime(new Date(timeMilliseconds));

        // Create the timestamp
        tmp = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
        if (tmp.length() == 1)
        {
            tmp = "0"+tmp;
        }
        currTime.append(tmp);
        currTime.append("/");

        tmp = Integer.toString(cal.get(Calendar.MONTH)+1);
        if (tmp.length() == 1)
        {
            tmp = "0"+tmp;
        }
        currTime.append(tmp);
        currTime.append("/");

        currTime.append(Integer.toString(cal.get(Calendar.YEAR)));
        currTime.append(" ");

        tmp = Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
        if (tmp.length() == 1)
        {
            tmp = "0"+tmp;
        }
        currTime.append(tmp);
        currTime.append(":");

        tmp = Integer.toString(cal.get(Calendar.MINUTE));
        if (tmp.length() == 1)
        {
            tmp = "0"+tmp;
        }
        currTime.append(tmp);
        currTime.append(":");

        tmp = Integer.toString(cal.get(Calendar.SECOND));
        if (tmp.length() == 1)
        {
            tmp = "0"+tmp;
        }
        currTime.append(tmp);


        //LogManager.write("Datamirror generator - Time now: ["+nowTime+" "+nowWeekDay+" "+nowMonthDay+"].", LogHandle.ALL,LogManager.DEBUG);

        return currTime.toString();
    }

    class Chrono extends Thread {

        public Chrono() {}

        public void startChrono() {
            Thread chronograph = new Thread(this);
            chronograph.start();
        }

        public void run() {

            int hh = 0;
            int mm = 0;
            int ss = 0;

            String hrs = "";
            String min = "";
            String sec = "";

            while (_running) {

                if (ss >= 60)
                {
                    ss = 0;
                    mm++;

                    if (mm >= 60)
                    {
                        mm = 0;
                        hh++;

                        if (hh >= 24)
                        {
                            hh = 0;
                        }
                    }
                }

                if (hh < 10 & mm >= 0)
                    hrs = "0"+hh+"h";
                else
                    hrs = hh+"h";

                if (mm < 10 && mm >= 0)
                    min = "0"+mm+"m";
                else
                    min = mm+"m";

                if (ss < 10 && ss >= 0)
                    sec = "0"+ss+"s";
                else
                    sec = ss+"s";

                if (hh == 0)
                    hrs = "";

                if (mm == 0 && hh == 0)
                    min = "";

                _time.setText(hrs+min+sec);

                try {sleep(1000);} catch (InterruptedException e) {LogManager.write("Datamirror generator - Cought exception", LogHandle.ALL,LogManager.MINOR);}

                ss++;
            }
        }
    }


    public static void main(String[] args)
    {
        Properties props;
        String argument;
        String messagelevel;
        String logFile;

        if (args.length > 1)
        {
            System.out.println("***************************************");
            System.out.println("*  Datamirror generator  -  v1.0      *");
            System.out.println("***************************************");
            System.out.println("***************************************");
            System.out.println("*  Not more than one argment accepted.*");
            System.out.println("***************************************");
            System.out.println("*  Available options:                 *");
            System.out.println("*     -v   -Version                   *");
            System.out.println("*                                     *");
            System.out.println("***************************************\n\n");
            System.exit(-1);
        }

        if (args.length == 0) {
            argument = "go";
        } else {
            argument = args[0];
        }

        System.out.println("\n");
        if (argument.equalsIgnoreCase("-v") || argument.matches("-version"))
        {
            System.out.println("***************************************");
            System.out.println("*  Datamirror generator  -  v1.0      *");
            System.out.println("***************************************\n\n");
            System.exit(1);
        }
        // Loading Event Generator variables
        props = new Properties();
        try
        {
            // Get the property (DMGHOME env var must be set)
            _dmghome = System.getProperty("DMGHOME");
            // Several checks to determine that the Program starts from the correct location
            if (_dmghome == null)
            {
                System.out.println("Datamirror Utility   - Home Directory not set. Exit.\n\n");
                System.exit(-1);
            }
            File home = new File(_dmghome);
            if (!home.exists())
            {
                System.out.println("Datamirror Utility   - Home Directory ["+_dmghome+"] does not exist. Exit.\n\n");
                System.exit(-1);
            }

            FileInputStream fStream = new FileInputStream(_dmghome + "props/dmgutil.prp");
            props.load(fStream);
            fStream.close();
        }
        catch(IOException e)
        {
            System.out.println("Datamirror Utility   - Can't find the property file \"" +_dmghome+ "props/dmgutil.prp\": Exit.\n\n");
            System.exit(-1);
        }

        //Setting logging level.
        messagelevel = props.getProperty("messagelevel");

        //Setting the default logging value
        if (messagelevel == null)
        {
            messagelevel = "CRITICAL";
        }

        int message_level;
        if(messagelevel.equalsIgnoreCase("debug"))
        {
            message_level=LogManager.DEBUG;
        }
        else if(messagelevel.equalsIgnoreCase("minor"))
        {
            message_level=LogManager.MINOR;
        }
        else if(messagelevel.equalsIgnoreCase("major"))
        {
            message_level=LogManager.MAJOR;
        }
        else if(messagelevel.equalsIgnoreCase("critical"))
        {
            message_level=LogManager.CRITICAL;
        }
        else
        {
            message_level=LogManager.CRITICAL;
        }

        // Set the logfile
        logFile=props.getProperty("messagelog");
        int sink_type;

        //If messagelog is not set into the generator.props,
        //set the default value to dmgutil.log.
        if (logFile == null)
        {
            logFile = "dmgutil.log";
        }

        if(logFile.equalsIgnoreCase("stdout"))
        {
            sink_type=LogManager.STDOUT;
        }
        else
        {
            sink_type=LogManager.FILE;
            try
            {
                logFile = _dmghome + "log/" + logFile;
                LogManager.setLog(LogHandle.ALL,logFile);
            }
            catch(Exception exit_log)
            {
                System.out.println("Configurator         - ERROR: Failed to set the log file - ["+logFile+"]");
                System.exit(-1);
            }
        }
        LogManager.setSinkType(sink_type);
        LogManager.setLogLevel(message_level);

        LogManager.write("", LogHandle.ALL,LogManager.CRITICAL);

        // Read all variables set at the previous session
        _vars = new DMGProps();
        if (!_vars.getProps(_dmghome)) {
            _vars.setDefaultProperties();
        }

        Vector reportData = new Vector();
        Vector colNames = new Vector();
        colNames.removeAllElements();
        colNames.addElement("Filename");
        colNames.addElement("Action");
        colNames.addElement("Location");
        colNames.addElement("Status");

        _reportTableModel = new DMGTableModel(reportData, colNames);

        DMGGenerator DMG = new DMGGenerator(_dmghome, _reportTableModel, null, null, null, null);
        DMG.startDMG(new JButton());
    }
}
