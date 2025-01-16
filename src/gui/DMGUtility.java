package gui;

import utils.*;

import org.jdesktop.jdic.tray.*;

import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;
import java.awt.event.*;

import log.LogManager;
import log.LogHandle;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pimmy
 * Date: 20-Apr-2006
 * Time: 17:13:42
 * To change this template use File | Settings | File Templates.
 */
public class DMGUtility extends Configurator implements ActionListener, ItemListener, MouseListener
{
    // System Tray
    private SystemTray tray = SystemTray.getDefaultSystemTray();
    // Tray menu
    private JPopupMenu _tBarMenu;
    // DataMirror Scheduler
    //public static DMGScheduler _scheduleModule;
    // DataMirror Generator
    //public static DMGGenerator _dataMirror;

    // User defined software configuration
    //public static DMGProps _vars;

    // Location of the DMG Install
   // public static String _dmghome;

    public DMGUtility()
    {
        LogManager.write("Datamirror Utility   - Starting DMG controller.", LogHandle.ALL,LogManager.DEBUG);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        _tBarMenu = new JPopupMenu("DMG Menu");

        ImageIcon icon_start = new ImageIcon(_dmghome +"images/start.gif");
        Action startDMG = new AbstractAction("Start", icon_start){
            public void actionPerformed(ActionEvent e){
                LogManager.write("Datamirror Utility   - Starting data mirroring.", LogHandle.ALL,LogManager.DEBUG);
                if (_dataMirror.testGenerator())
                {
                    LogManager.write("Datamirror Utility   - Data-mirroring is in progress.", LogHandle.ALL,LogManager.MINOR);
                }
                else
                {
                    _dataMirror.startDMG(_startButton);
                }
            }
        };
        _tBarMenu.add(startDMG);

        ImageIcon icon_stop = new ImageIcon(_dmghome +"images/stop.gif");
        Action stopDMG = new AbstractAction("Stop", icon_stop){
            public void actionPerformed(ActionEvent e){
                _dataMirror.stopDMG();
                LogManager.write("Datamirror Utility   - Datamirroring stopped.", LogHandle.ALL,LogManager.DEBUG);
            }
        };
        _tBarMenu.add(stopDMG);

        ImageIcon icon_conf = new ImageIcon(_dmghome +"images/config.gif");
        Action confDMG = new AbstractAction("Configure", icon_conf){
            public void actionPerformed(ActionEvent e){
                launchConfigurator();
            }
        };
        _tBarMenu.add(confDMG);

        ImageIcon icon_exit = new ImageIcon(_dmghome +"images/exit.gif");
        Action exitDMG = new AbstractAction("Exit", icon_exit){
            public void actionPerformed(ActionEvent e){
                LogManager.write("Datamirror Utility   - Exiting DataMirror.", LogHandle.ALL,LogManager.DEBUG);
                if (_dataMirror.testGenerator())
                {
                    // Pause the mirroring while the user makes his choice
                    _dataMirror.setPauseGenerator(true);
                    int question = DMGOptionPane.showConfirmDialog(_tBarMenu, "Data mirroring in progress. Terminating the program now may result in\n" +
                            "non-complete data mirror. Exit the program now?" +
                            "\n", " Datamirror Utility.", DMGOptionPane.YES_NO_OPTION);
                    if (question == 0) {
                        _dataMirror.stopDMG();
                        LogManager.write("Datamirror Utility   - Datamirroring stopped.", LogHandle.ALL,LogManager.DEBUG);
                        System.exit(1);
                    } else {
                        _dataMirror.setPauseGenerator(false);
                    }
                }
                else
                {
                    LogManager.write("Application Stopped  - Datamirror Utility.", LogHandle.ALL,LogManager.DEBUG);
                    System.exit(1);
                }
            }
        };
        _tBarMenu.add(exitDMG);

        _tBarMenu.addMouseListener(this);


        ImageIcon i = new ImageIcon(_dmghome +"images/generator.gif");
        TrayIcon ti = new TrayIcon(i, "DataMirror Utility", _tBarMenu);
        ti.setIconAutoSize(true);
        //ti.addActionListener(new ActionListener() {
        //    public void actionPerformed(ActionEvent e) {
        //          JOptionPane.showMessageDialog(null, "DataMirror Utility", "About", JOptionPane.INFORMATION_MESSAGE);
        //    }
        //});
        tray.addTrayIcon(ti);

        // Monitors the change of the properties and if the user changes the properties,
        // the props file and the scheduler gets updated
        DMGPropsMonitor propsMonitor = new DMGPropsMonitor(_dmghome, _vars, _scheduleModule);
        propsMonitor.startMonitor();
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
       LogManager.write("Datamirror Utility   - CLICK.", LogHandle.ALL,LogManager.DEBUG);
       if (e.getClickCount() > 1)
       {
           LogManager.write("Datamirror Utility   - Launching Configurator.", LogHandle.ALL,LogManager.DEBUG);
           launchConfigurator();
       }
    }

    // Returns just the class name -- no package info.
    protected String getClassName(Object o) {
        String classString = o.getClass().getName();
        int dotIndex = classString.lastIndexOf(".");

        return classString.substring(dotIndex + 1);
    }

    public void itemStateChanged(ItemEvent e) {
        JMenuItem source = (JMenuItem) (e.getSource());
        String s = "Item event detected." + "\n" + "    Event source: "
                + source.getText() + " (an instance of " + getClassName(source)
                + ")" + "\n" + "    New state: "
                + ((e.getStateChange() == ItemEvent.SELECTED)
                        ? "selected"
                        : "unselected");

        System.out.println(s);
    }

    public boolean stopScheduler()
    {
        return true;
    }

    public static void main(String[] args)
    {
        // Command line argument
        String argument;
        // Runtime properties
        Properties props = new Properties();
        // Level of message logging
        String messagelevel;
        // Location of the logging info
        String log_file;

        if (args.length > 1)
        {
            System.out.println("***************************************");
            System.out.println("*  Datamirror RMI Server  -  v1.0     *");
            System.out.println("***************************************");
            System.out.println("***************************************");
            System.out.println("*  Not more than one argment accepted.*");
            System.out.println("***************************************");
            System.out.println("*  Available options:                 *");
            System.out.println("*     -v   -Version                   *");
            System.out.println("*     -go  -Start Server              *");
            System.out.println("***************************************\n\n");
            System.exit(-1);
        }
        else
        {
            // If nothing is specified, we assume that the user wants to run the application.
            if (args.length == 0) {
                argument = "go";
            } else {
                argument = args[0];
            }


            System.out.println("\n");
            if (argument.equalsIgnoreCase("-v") || argument.matches("-version"))
            {
                System.out.println("***************************************");
                System.out.println("*  Datamirror RMI Server  -  v1.0      *");
                System.out.println("***************************************\n\n");
                System.exit(1);
            }

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

//        File startup = new File(home, "Generator.exe");
//        if (!startup.exists())
//        {
//            System.out.println("Datamirror Utility   - Home Directory ["+_dmghome+"] is wrong. Exit.");
//            System.exit(-1);
//        }

            //Setting logging level.
            messagelevel = props.getProperty("messagelevel");

            //Setting the default logging value
            if (messagelevel == null)
            {
                messagelevel = "CRITICAL";
            }

            int message_level;
            if(messagelevel.equals("debug") || messagelevel.equals("DEBUG"))
            {
                message_level=LogManager.DEBUG;
            }
            else if(messagelevel.equals("minor") || messagelevel.equals("MINOR"))
            {
                message_level=LogManager.MINOR;
            }
            else if(messagelevel.equals("major") || messagelevel.equals("MAJOR"))
            {
                message_level=LogManager.MAJOR;
            }
            else if(messagelevel.equals("critical") || messagelevel.equals("CRITICAL"))
            {
                message_level=LogManager.CRITICAL;
            }
            else
            {
                message_level=LogManager.CRITICAL;
            }

            // Set the logfile
            log_file=props.getProperty("messagelog");
            int sink_type;

            //If messagelog is not set into the generator._props,
            //set the default value to generator.log.
            if (log_file == null)
            {
                log_file = "dmgutil.log";
            }

            if(log_file.equals("stdout") || log_file.equals("STDOUT"))
            {
                sink_type=LogManager.STDOUT;
            }
            else
            {
                sink_type=LogManager.FILE;
                try
                {
                    log_file = _dmghome + "log/" + log_file;
                    LogManager.setLog(LogHandle.ALL,log_file);
                }
                catch(Exception exit_log)
                {
                    System.out.println("Datamirror Utility   - ERROR: Failed to set the log file - ["+log_file+"]");
                    System.exit(-1);
                }
            }
            LogManager.setSinkType(sink_type);
            LogManager.setLogLevel(message_level);

            LogManager.write("", LogHandle.ALL,LogManager.CRITICAL);
            LogManager.write("Application Started  - Datamirror Utility.", LogHandle.ALL,LogManager.DEBUG);
            LogManager.write("Datamirror Utility   - Home Directory set to: " +_dmghome, LogHandle.ALL,LogManager.DEBUG);

            new DMGUtility();
        }
    }
}
