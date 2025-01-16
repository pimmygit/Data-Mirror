package utils;

import log.LogManager;
import log.LogHandle;

/**
 * Created by IntelliJ IDEA.
 * User: pimmy
 * Date: 25-Apr-2006
 * Time: 10:23:12
 * Purpose:
 *      Monitors the DMG Properties for changes. If change ocurres,
 *      the affected processes gets restarted.
 */
public class DMGPropsMonitor implements Runnable
{
    String _dmghome;
    private static DMGProps _vars;
    private static DMGScheduler _scheduleModule;
    private boolean _run = false;
    private DMGProps _props;

    private Thread propsMonitor = null;

    public DMGPropsMonitor(String dmghome, DMGProps vars, DMGScheduler scheduleModule)
    {
        _dmghome = dmghome;
        _scheduleModule = scheduleModule;
        _vars = vars;
        _props = new DMGProps();
        _props.getProps(dmghome);
    }

    public void startMonitor()
    {
        _run = true;
        if (propsMonitor == null)
        {
            LogManager.write("Properties monitor   - Monitoring started.", LogHandle.ALL,LogManager.DEBUG);
            propsMonitor = new Thread(this);
            propsMonitor.start();
        }
    }

    public void stopMonitor()
    {
        LogManager.write("Properties monitor   - Monitoring stopped.", LogHandle.ALL,LogManager.DEBUG);
        _run = false;
        try{Thread.sleep(3000);}catch(Exception e){
            LogManager.write("Properties monitor   - Timer exception.", LogHandle.ALL,LogManager.MINOR);
        }
        propsMonitor = null;
    }

    public void run()
    {
        while(_run)
        {
            // Test if any of the settings has changed
            if ((_props.getAction() != _vars.getAction())
            || (_props.getActionDate() != _vars.getActionDate())
            || (_props.getActionSize() != _vars.getActionSize())
            || (_props.getDateCondition() != _vars.getDateCondition())
            || (_props.getDateModifyDir() != _vars.getDateModifyDir())
            || (_props.getDateValue01() != _vars.getDateValue01())
            || (_props.getDateValue02() != _vars.getDateValue02())
            || (!_props.getDateSet().equals(_props.getDateSet()))
            || (!_props.getDestinationDir().equals(_vars.getDestinationDir()))
            || (!_props.getFileSize().equals(_vars.getFileSize()))
            || (_props.getModifyDir() != _vars.getModifyDir())
            || (!_props.getMonthDay().equals(_vars.getMonthDay()))
            || (_props.getRecurse() != _vars.getRecurse())
            || (_props.getRunAtTime() != _vars.getRunAtTime())
            || (_props.getSchedulePeriod() != _vars.getSchedulePeriod())
            || (_props.getSizeCondition() != _vars.getSizeCondition())
            || (_props.getSizeModifyDir() != _vars.getSizeModifyDir())
            || (_props.getSizeValue01() != _vars.getSizeValue01())
            || (_props.getSizeValue02() != _vars.getSizeValue02())
            || (!_props.getSourceDir().equals(_vars.getSourceDir()))
            || (_props.getWeekDay() != _vars.getWeekDay())
            || (_props.getReportLevel() != _vars.getReportLevel())
            || (_props.getReportToFile() != _vars.getReportToFile())){
                LogManager.write("Properties monitor   - Properies changed.", LogHandle.ALL,LogManager.DEBUG);

                _vars.saveProps(_dmghome);
                _props.getProps(_dmghome);

                // Restart the processes which depend on the settings.
                if(_scheduleModule.isRunning())
                {
                    LogManager.write("Properties monitor   - Restarting Scheduler..", LogHandle.ALL,LogManager.DEBUG);

                    _scheduleModule.stopScheduler();
                    try{Thread.sleep(100);}catch(Exception e){
                        LogManager.write("Properties monitor   - Timer exception.", LogHandle.ALL,LogManager.MINOR);
                    }
                    _scheduleModule.startScheduler();
                }
            }
            // Execute every 3 seconds
            try{Thread.sleep(3000);}catch(Exception e){
                LogManager.write("Properties monitor   - Timer exception.", LogHandle.ALL,LogManager.MINOR);
            }
        }
    }

    private void printProps() {
        LogManager.write("Properties monitor   - Property getAction:         ["+_props.getAction()+"] = ["+_vars.getAction()+"].", LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Properties monitor   - Property getActionDate:     ["+_props.getActionDate()+"] = ["+_vars.getActionDate()+"].", LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Properties monitor   - Property getActionSize:     ["+_props.getActionSize()+"] = ["+_vars.getActionSize()+"].", LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Properties monitor   - Property getDateCondition:  ["+_props.getDateCondition()+"] = ["+_vars.getDateCondition()+"].", LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Properties monitor   - Property getDateModifyDir:  ["+_props.getDateModifyDir()+"] = ["+_vars.getDateModifyDir()+"].", LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Properties monitor   - Property getDateValue01:    ["+_props.getDateValue01()+"] = ["+_vars.getDateValue01()+"].", LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Properties monitor   - Property getDateValue02:    ["+_props.getDateValue02()+"] = ["+_vars.getDateValue02()+"].", LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Properties monitor   - Property getDateSet:        ["+_props.getDateSet()+"] = ["+_props.getDateSet()+"].", LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Properties monitor   - Property getDestinationDir: ["+_props.getDestinationDir()+"] = ["+_vars.getDestinationDir()+"].", LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Properties monitor   - Property getFileSize:       ["+_props.getFileSize()+"] = ["+_vars.getFileSize()+"].", LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Properties monitor   - Property getModifyDir:      ["+_props.getModifyDir()+"] = ["+_vars.getModifyDir()+"].", LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Properties monitor   - Property getMonthDay:       ["+_props.getMonthDay()+"] = ["+_vars.getMonthDay()+"].", LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Properties monitor   - Property getRecurse:        ["+_props.getRecurse()+"] = ["+_vars.getRecurse()+"].", LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Properties monitor   - Property getRunAtTime:      ["+_props.getRunAtTime()+"] = ["+_vars.getRunAtTime()+"].", LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Properties monitor   - Property getSchedulePeriod: ["+_props.getSchedulePeriod()+"] = ["+_vars.getSchedulePeriod()+"].", LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Properties monitor   - Property getSizeCondition:  ["+_props.getSizeCondition()+"] = ["+_vars.getSizeCondition()+"].", LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Properties monitor   - Property getSizeModifyDir:  ["+_props.getSizeModifyDir()+"] = ["+_vars.getSizeModifyDir()+"].", LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Properties monitor   - Property getSizeValue01:    ["+_props.getSizeValue01()+"] = ["+_vars.getSizeValue01()+"].", LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Properties monitor   - Property getSizeValue02:    ["+_props.getSizeValue02()+"] = ["+_vars.getSizeValue02()+"].", LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Properties monitor   - Property getSourceDir:      ["+_props.getSourceDir()+"] = ["+_vars.getSourceDir()+"].", LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Properties monitor   - Property getWeekDay:        ["+_props.getWeekDay()+"] = ["+_vars.getWeekDay()+"].", LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Properties monitor   - Property getReportLevel:    ["+_props.getReportLevel()+"] = ["+_vars.getReportLevel()+"].", LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Properties monitor   - Property getReportToFile:   ["+_props.getReportToFile()+"] = ["+_vars.getReportToFile()+"].", LogHandle.ALL,LogManager.DEBUG);
    }
}
