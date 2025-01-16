package gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.*;
import java.awt.event.*;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Properties;
import java.util.Vector;
import java.awt.event.FocusListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;

import log.*;
import utils.*;

/*
* This is the main class for the Backup Utility GUI
*/

public class Configurator implements ActionListener, FocusListener
{
    private final int NO_ACTION = 0;
    private final int DECIDE_BY_DATE = 1;
    private final int DECIDE_BY_SIZE = 2;

    private final int KEEP_NEWEST = 0;
    private final int KEEP_OLDEST = 1;
    private final int DATE_CUSTOM = 2;

    private final int KEEP_BIGGEST = 0;
    private final int KEEP_SMALLEST = 1;
    private final int SIZE_CUSTOM = 2;

    private final int NO_SCHEDULE = 0;
    private final int DAILY = 1;
    private final int WEEKLY = 2;
    private final int MONTHLY = 3;

    private JFrame frame;

    // Location of the DMG Installation
    public static String _dmghome;
    // Properties section
    public static DMGProps _vars;
    // DataMirror Generator
    public static DMGGenerator _dataMirror;
    // Scheduler
    public static DMGScheduler _scheduleModule;
    // Report table model
    public static DMGTableModel _reportTableModel;

    // File Chooser
    private JFileChooser _fChooser;

    // Directory Locations
    private JTextField _src;
    private JTextField _dest;

    // Action radio buttons
    private JRadioButton _chooseByNone;
    private JRadioButton _chooseByDate;
    private JRadioButton _chooseBySize;

    private JRadioButton _dateNewest;
    private JRadioButton _dateOldest;
    private JRadioButton _dateCustom;

    private JRadioButton _sizeBiggest;
    private JRadioButton _sizeSmalest;
    private JRadioButton _sizeCustom;

    // Custom actions - condition values
    private JComboBox _modifyOptions;

    private JLabel _labelIf_1;
    private JComboBox _val_11;
    private JComboBox _cond_1;
    private JComboBox _val_12;
    private JLabel _labelThen_1;
    private JLabel _labelDate;
    private JComboBox _res_1;
    private JTextField _fDate;

    private JLabel _labelIf_2;
    private JComboBox _val_21;
    private JComboBox _cond_2;
    private JComboBox _val_22;
    private JLabel _labelThen_2;
    private JLabel _labelkB;
    private JComboBox _res_2;
    private JTextField _fSize;

    // Schedulers options
    private JRadioButton _chooseNone;
    private JRadioButton _chooseDaily;
    private JRadioButton _chooseWeekly;
    private JRadioButton _chooseMonthly;

    // Report Tab
    private JComboBox _reportLevel;
    private JButton _clearReportButton;
    private JCheckBox _reportToFile;

    private JLabel _labelEvery;
    private static JComboBox _weekDays;
    private JTextField _daysOfMonth;
    private JLabel _labelAt;
    private JComboBox _timeIntervalSelector;

    // Start/Pause/Stop button
    public static JButton _startButton;

    // Status bar
    public static JProgressBar _progressBar;
    public static JTextField _numFiles;
    public static JTextField _dataSize;
    public static JTextField _timeSec;

    // Recurse Tickbox
    private JCheckBox _recurseBox;

    // Activate and deactivate GUI actions
    private boolean enableActions = false;

    // Create the Main Frame for the Event Generator GUI
    //public Configurator(DMGProps vars, DMGGenerator dataMirror, DMGScheduler scheduleModule, String dmghome)
    public Configurator()
    {
        _vars = new DMGProps();

        // If the properties file does not exist,
        // the GUI is launched in order to create such file.
        if (!_vars.getProps(_dmghome)) {

            _vars.setDefaultProperties();

            LogManager.write("Datamirror Utility   - Properites not defined. Starting GUI.", LogHandle.ALL,LogManager.DEBUG);
            createConfigurator();

            // Create the DataMirror Generator
            _dataMirror = new DMGGenerator(_dmghome, _reportTableModel, _progressBar, _numFiles, _dataSize, _timeSec);
            // Create the scheduler
            _scheduleModule = new DMGScheduler(_dmghome, _dataMirror);

            launchConfigurator();
        } else if (_vars.getSchedulePeriod() == 0) {

            LogManager.write("Datamirror Utility   - No scheduled tasks. Starting GUI.", LogHandle.ALL,LogManager.DEBUG);
            createConfigurator();

            // Create the DataMirror Generator
            _dataMirror = new DMGGenerator(_dmghome, _reportTableModel, _progressBar, _numFiles, _dataSize, _timeSec);
            // Create the scheduler
            _scheduleModule = new DMGScheduler(_dmghome, _dataMirror);

            launchConfigurator();
        } else {
            Object[] userInput = { "Configure", "Continue", "Exit" };
            int userChoice = DMGOptionPane.showOptionDialog(new JFrame(), "Scheduled tasks found.\n\n" +
                    "\tPress 'Configure' if you wish to reconfigure your settings.\n" +
                    "\tPress 'Continue' to start the application in a background.\n" +
                    "\tPress 'Exit' if you wish to exit the application.\n\n", " Datamirror Utility.  ",
                    DMGOptionPane.YES_NO_CANCEL_OPTION, DMGOptionPane.QUESTION_MESSAGE, null, userInput, userInput[2]);

            switch (userChoice) {
                case 0:
                    createConfigurator();

                    // Create the DataMirror Generator
                    _dataMirror = new DMGGenerator(_dmghome, _reportTableModel, _progressBar, _numFiles, _dataSize, _timeSec);
                    // Create the scheduler
                    _scheduleModule = new DMGScheduler(_dmghome, _dataMirror);
                    _scheduleModule.startScheduler();

                    launchConfigurator();
                    break;
                case 1:
                    createConfigurator();

                    // Create the DataMirror Generator
                    _dataMirror = new DMGGenerator(_dmghome, _reportTableModel, _progressBar, _numFiles, _dataSize, _timeSec);
                    // Create the scheduler
                    _scheduleModule = new DMGScheduler(_dmghome, _dataMirror);
                    _scheduleModule.startScheduler();
                    break;
                case 2:
                    System.exit(0);
                    break;
                default:
                    System.exit(0);
                    break;
            }
        }
    }

    private void createConfigurator()
    {
        LogManager.write("Application Started  - GUI \"Datamirror Utility v.1.0\".", LogHandle.ALL,LogManager.DEBUG);

        // Apply the user defined settings from the previous session to the GUI
        //_vars = vars;
        //_dmghome = dmghome;
        //_dataMirror = dataMirror;
        //_scheduleModule = scheduleModule;

        String title = "  Datamirror Utility";
        frame = new JFrame(title);
        frame.setIconImage(new ImageIcon(_dmghome + "images/generator.gif").getImage());
        frame.setSize(500, 377);
        frame.setResizable(false);

        LogManager.write("Configurator         - Creating Tabbed frames.", LogHandle.ALL,LogManager.DEBUG);
        //frame.setContentPane(addTabbedFrames());
        frame.getContentPane().add(addTabbedFrames(), BorderLayout.NORTH);
        frame.getContentPane().add(statusBar(), BorderLayout.SOUTH);

        centerFrame(500, 377, frame);
        enableMenus();
        enableActions = true;

        frame.setVisible(false);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {

                LogManager.write("Configurator         - Hiding Configurator.", LogHandle.ALL,LogManager.DEBUG);
                saveVars();
                frame.setVisible(false);
            }
        });

        LogManager.write("Configurator         - GUI \"Datamirror Utility v.1.0\" created.", LogHandle.ALL,LogManager.DEBUG);
    }

    protected void launchConfigurator()
    {
        LogManager.write("Configurator         - Launching Configurator.", LogHandle.ALL,LogManager.DEBUG);
        frame.pack();
        frame.setVisible(true);
    }

    // Create the Tabbed panel.
    private JTabbedPane addTabbedFrames()
    {
        // Tabbed Frames Panel
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab(" Config", new ImageIcon(_dmghome + "images/config.gif"), ConfigFrame());
        tabbedPane.addTab(" Report", new ImageIcon(_dmghome + "images/events.gif"), ReportFrame());

        return tabbedPane;
    }

    // Create the Configuration Panel.
    private JPanel ConfigFrame()
    {
        LogManager.write("Configurator         - Creating: \"Config\" tab.", LogHandle.ALL,LogManager.DEBUG);

        //ImagePanel config = new ImagePanel(new ImageIcon(_dmghome+"images/dmgbkgnd.png").getImage());
        //config.setOpaque(true);
        JPanel config = new JPanel();

        // Create panel layout
        config.setLayout(new GridBagLayout());
        GridBagConstraints constr = new GridBagConstraints();
        constr.fill = GridBagConstraints.HORIZONTAL;

        // Create File Chooser
        _fChooser = new JFileChooser();
        _fChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // Create Start button
        Action start = new AbstractAction("Start"){
            public void actionPerformed(ActionEvent e){
                if (_startButton.getText().equalsIgnoreCase("Start"))
                {
                    // Save properties
                    _vars.saveProps(_dmghome);
                    // Start Datamirroring
                    _startButton.setText("Stop");
                    _dataMirror.startDMG(_startButton);
                }
                else
                {
                    // Stop Datamirroring
                    _startButton.setText("Start");
                    _dataMirror.stopDMG();
                    if (_dataMirror.testGenerator())
                    {
                        _startButton.setText("Stop");
                    }
                }
            }
        };
        _startButton = new JButton(start);
        _startButton.setFont(new Font("Dialog", Font.BOLD, 11));
        _startButton.setBorder(BorderFactory.createRaisedBevelBorder());
        Dimension size1 = new Dimension(55,23);
        _startButton.setMinimumSize(size1);
        _startButton.setPreferredSize(size1);
        _startButton.setMaximumSize(size1);

        // Create button for directory recursion
        _recurseBox = new JCheckBox("  Recurse");
        _recurseBox.setSelected(_vars.getRecurse());
        Dimension size2 = new Dimension(70,30);
        _recurseBox.setMinimumSize(size2);
        _recurseBox.setPreferredSize(size2);
        _recurseBox.setMaximumSize(size2);
        _recurseBox.addActionListener(this);

        // Add subpanels to the main panel
        // *******************************

        // Add Source directory
        constr.weightx = 0.5;
        constr.gridx = 0;
        constr.gridy = 0;
        config.add(sourcePanel(), constr);

        // Add Start button
        constr.gridx = 1;
        constr.gridy = 0;
        config.add(_startButton, constr);

        // Add Destination directory
        constr.gridx = 0;
        constr.gridy = 1;
        config.add(destPanel(), constr);

        // Add Recurse button
        constr.gridx = 1;
        constr.gridy = 1;
        config.add(_recurseBox, constr);

        // Add panel for handling duplicate files
        constr.gridx = 0;
        constr.gridy = 2;
        config.add(duplHandlingPanel(), constr);

        // Add panel for scheduled tasks
        constr.gridx = 1;
        constr.gridy = 2;
        config.add(schedulePanel(), constr);

        frame.getContentPane().add(config);

        return config;
    }

    // Function to position a window in the middle of the screen.
    public void centerFrame(int w, int h, JFrame frame)
    {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds((d.width-w)/2, (d.height-h)/2, w, h);
    }

    private JPanel sourcePanel()
    {
        JPanel srcPanel = new JPanel();
        srcPanel.setLayout(new BoxLayout(srcPanel, BoxLayout.X_AXIS));
        srcPanel.setMinimumSize(new Dimension(300, 30));
        srcPanel.setPreferredSize(new Dimension(300,30));
        srcPanel.setMaximumSize(new Dimension(300,30));

        JLabel src_name = new JLabel("  Src:  ");
        src_name.setForeground(Color.black);
        //src_name.setBounds(2, 5, 50, 20);

        _src = new JTextField(_vars.getSourceDir());
        _src.setFont(new Font("Helvetica", Font.PLAIN, 11));
        _src.setMinimumSize(new Dimension(280, 20));
        _src.setPreferredSize(new Dimension(280, 20));
        _src.setMaximumSize(new Dimension(280, 20));
        _src.addFocusListener(this);

        // Create File Chooser button
        Action fileOpen = new AbstractAction(){
            public void actionPerformed(ActionEvent e){
                int returnVal = _fChooser.showOpenDialog(frame);
                if (returnVal == JFileChooser.APPROVE_OPTION)
                {
                    String location = _fChooser.getSelectedFile().getAbsolutePath();
                    _src.setText(location);
                    _vars.setSourceDir(location);
                }
            }
        };
        ImageIcon folder = new ImageIcon(_dmghome + "images/folder.png");
        JButton fOpenButton = new JButton(fileOpen);
        fOpenButton.setIcon(folder);
        fOpenButton.setMinimumSize(new Dimension(25, 18));
        fOpenButton.setPreferredSize(new Dimension(25, 18));
        fOpenButton.setMaximumSize(new Dimension(25, 18));

        srcPanel.add(src_name);
        srcPanel.add(_src);
        srcPanel.add(Box.createRigidArea(new Dimension(8,8)));
        srcPanel.add(fOpenButton);

        return srcPanel;
    }

    private JPanel destPanel()
    {
        JPanel dstPanel = new JPanel();
        dstPanel.setLayout(new BoxLayout(dstPanel, BoxLayout.X_AXIS));
        dstPanel.setMinimumSize(new Dimension(300, 40));
        dstPanel.setPreferredSize(new Dimension(300,40));
        dstPanel.setMaximumSize(new Dimension(300,40));

        JLabel dst_name = new JLabel("  Dst:  ");
        dst_name.setForeground(Color.black);

        _dest = new JTextField(_vars.getDestinationDir());
        _dest.setFont(new Font("Helvetica", Font.PLAIN, 11));
        _dest.setMinimumSize(new Dimension(280, 20));
        _dest.setPreferredSize(new Dimension(280, 20));
        _dest.setMaximumSize(new Dimension(280, 20));
        _dest.addFocusListener(this);

        // Create File Chooser button
        Action fileOpen = new AbstractAction(){
            public void actionPerformed(ActionEvent e){
                int returnVal = _fChooser.showOpenDialog(frame);
                if (returnVal == JFileChooser.APPROVE_OPTION)
                {
                    String location = _fChooser.getSelectedFile().getAbsolutePath();
                    _dest.setText(location);
                    _vars.setDestinationDir(location);
                }
            }
        };
        ImageIcon folder = new ImageIcon(_dmghome + "images/folder.png");
        JButton fOpenButton = new JButton(fileOpen);
        fOpenButton.setIcon(folder);
        fOpenButton.setMinimumSize(new Dimension(25, 18));
        fOpenButton.setPreferredSize(new Dimension(25, 18));
        fOpenButton.setMaximumSize(new Dimension(25, 18));

        dstPanel.add(dst_name);
        dstPanel.add(_dest);
        dstPanel.add(Box.createRigidArea(new Dimension(8,8)));
        dstPanel.add(fOpenButton);

        return dstPanel;
    }

    // Create File Handling Field.
    private JPanel duplHandlingPanel()
    {
        LogManager.write("Configurator         - Creating: \"Config\" -> \"Control Panel\".", LogHandle.ALL,LogManager.DEBUG);

        JPanel fHandling = new JPanel();
        fHandling.setLayout(new BoxLayout(fHandling, BoxLayout.X_AXIS));
        fHandling.setMinimumSize(new Dimension(360, 255));
        fHandling.setPreferredSize(new Dimension(360, 255));
        fHandling.setMaximumSize(new Dimension(360, 255));

        TitledBorder titledborder = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)),"   Handling Duplicate Files:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
        fHandling.setBorder(titledborder);

        fHandling.add(actionSelector());
        fHandling.add(conditionTool());

        return fHandling;
    }

    // Create "Action Selector" sub-panel for Duplicate File Handling
    private JPanel actionSelector()
    {
        LogManager.write("Configurator         - Creating: \"Config\" -> \"Control Panel\" -> \"Duplicate File Handling\".", LogHandle.ALL,LogManager.DEBUG);

        JPanel selector = new JPanel();
        selector.setLayout(null);
        selector.setMinimumSize(new Dimension(135, 215));
        selector.setPreferredSize(new Dimension(135, 215));
        selector.setMaximumSize(new Dimension(135, 215));

        _chooseByNone = new JRadioButton("  No Action");
        _chooseByDate = new JRadioButton("  Decide by Date");
        _chooseBySize = new JRadioButton("  Decide by Size");

        _dateNewest = new JRadioButton("  Keep Newest");
        _dateOldest = new JRadioButton("  Keep Oldest");
        _dateCustom = new JRadioButton("  Custom");

        _sizeBiggest = new JRadioButton("  Keep Biggest");
        _sizeSmalest = new JRadioButton("  Keep Smallest");
        _sizeCustom = new JRadioButton("  Custom");

        // Add actions to the radio buttons
        _chooseByNone.addActionListener(this);
        _chooseByDate.addActionListener(this);
        _chooseBySize.addActionListener(this);
        _dateNewest.addActionListener(this);
        _dateOldest.addActionListener(this);
        _dateCustom.addActionListener(this);
        _sizeBiggest.addActionListener(this);
        _sizeSmalest.addActionListener(this);
        _sizeCustom.addActionListener(this);

        ButtonGroup chooseAction = new ButtonGroup();
        chooseAction.add(_chooseByNone);
        chooseAction.add(_chooseByDate);
        chooseAction.add(_chooseBySize);
        switch(_vars.getAction())
        {
            case 0:  _chooseByNone.setSelected(true); break;
            case 1:  _chooseByDate.setSelected(true); break;
            case 2:  _chooseBySize.setSelected(true); break;
            default: _chooseByNone.setSelected(true); break;
        }

        ButtonGroup compareDates = new ButtonGroup();
        compareDates.add(_dateNewest);
        compareDates.add(_dateOldest);
        compareDates.add(_dateCustom);
        switch(_vars.getActionDate())
        {
            case 0:  _dateNewest.setSelected(true); break;
            case 1:  _dateOldest.setSelected(true); break;
            case 2:  _dateCustom.setSelected(true); break;
            default: _dateNewest.setSelected(true); break;
        }

        ButtonGroup compareSizes = new ButtonGroup();
        compareSizes.add(_sizeBiggest);
        compareSizes.add(_sizeSmalest);
        compareSizes.add(_sizeCustom);
        switch(_vars.getActionSize())
        {
            case 0:  _sizeBiggest.setSelected(true); break;
            case 1:  _sizeSmalest.setSelected(true); break;
            case 2:  _sizeCustom.setSelected(true); break;
            default: _sizeBiggest.setSelected(true); break;
        }

        // Position the fields in the panel
        Insets insets = selector.getInsets();
        Dimension size;
        size = _chooseByNone.getPreferredSize();
        _chooseByNone.setBounds(insets.left, insets.top, size.width, size.height);
        size = _chooseByDate.getPreferredSize();
        _chooseByDate.setBounds(insets.left, 24 + insets.top, size.width, size.height);
        size = _dateNewest.getPreferredSize();
        _dateNewest.setBounds(25 + insets.left, 48 + insets.top, size.width, size.height);
        size = _dateOldest.getPreferredSize();
        _dateOldest.setBounds(25 + insets.left, 72 + insets.top, size.width, size.height);
        size = _dateCustom.getPreferredSize();
        _dateCustom.setBounds(25 + insets.left, 96 + insets.top, size.width, size.height);
        size = _chooseBySize.getPreferredSize();
        _chooseBySize.setBounds(insets.left, 120 + insets.top, size.width, size.height);
        size = _sizeBiggest.getPreferredSize();
        _sizeBiggest.setBounds(25 + insets.left, 144 + insets.top, size.width, size.height);
        size = _sizeSmalest.getPreferredSize();
        _sizeSmalest.setBounds(25 + insets.left, 168 + insets.top, size.width, size.height);
        size = _sizeCustom.getPreferredSize();
        _sizeCustom.setBounds(25 + insets.left, 192 + insets.top, size.width, size.height);

        // Add items to the panel
        selector.add(_chooseByNone);
        selector.add(_chooseByDate);
        selector.add(_dateNewest);
        selector.add(_dateOldest);
        selector.add(_dateCustom);
        selector.add(_chooseBySize);
        selector.add(_sizeBiggest);
        selector.add(_sizeSmalest);
        selector.add(_sizeCustom);

        return selector;
    }

    // Create "Condition Writing Tool" sub-panel for Duplicate File Handling
    private JPanel conditionTool()
    {
        LogManager.write("Configurator         - Creating: \"Config\" -> \"Control Panel\" -> \"Duplicate File Handling\" -> \"Condition Writing Tool\".", LogHandle.ALL,LogManager.DEBUG);

        JPanel conditions = new JPanel();
        conditions.setLayout(null);
        conditions.setMinimumSize(new Dimension(210, 215));
        conditions.setPreferredSize(new Dimension(210, 215));
        conditions.setMaximumSize(new Dimension(210, 215));

        String[] modifyOptStr = { " Modify Source (SRC) only", " Modify Destination (DST) only", " Modify All Files (SRC and DST)" };
        _modifyOptions = new JComboBox(modifyOptStr);
        _modifyOptions.addActionListener(this);
        _modifyOptions.setMinimumSize(new Dimension(185, 20));
        _modifyOptions.setPreferredSize(new Dimension(185, 20));
        _modifyOptions.setMaximumSize(new Dimension(185, 20));
        _modifyOptions.setForeground(Color.black);
        _modifyOptions.setBackground(Color.white);
        _modifyOptions.setFont(new Font("Dialog", Font.PLAIN, 11));
        _modifyOptions.setBorder(BorderFactory.createCompoundBorder());
        _modifyOptions.setSelectedIndex(_vars.getModifyDir());

        _labelIf_1 = new JLabel("If:");

        String[] val_11str = { " Src", " Dst" };
        _val_11 = new JComboBox(val_11str);
        _val_11.addActionListener(this);
        _val_11.setMinimumSize(new Dimension(50, 20));
        _val_11.setPreferredSize(new Dimension(50, 20));
        _val_11.setMaximumSize(new Dimension(50, 20));
        _val_11.setForeground(Color.black);
        _val_11.setBackground(Color.white);
        _val_11.setFont(new Font("Dialog", Font.PLAIN, 11));
        _val_11.setBorder(BorderFactory.createCompoundBorder());
        _val_11.setSelectedIndex(_vars.getDateValue01());

        String[] condStr1 = { " Older", "Newer", " Same", " Diff." };
        _cond_1 = new JComboBox(condStr1);
        _cond_1.addActionListener(this);
        _cond_1.setMinimumSize(new Dimension(60, 20));
        _cond_1.setPreferredSize(new Dimension(60, 20));
        _cond_1.setMaximumSize(new Dimension(60, 20));
        _cond_1.setForeground(Color.black);
        _cond_1.setBackground(Color.white);
        _cond_1.setFont(new Font("Dialog", Font.PLAIN, 11));
        _cond_1.setBorder(BorderFactory.createCompoundBorder());
        _cond_1.setSelectedIndex(_vars.getDateCondition());

        String[] val_12str = { " Src", " Dst", " Date", " Hour", " Day", " Week", "Month", " Year" };
        _val_12 = new JComboBox(val_12str);
        _val_12.addActionListener(this);
        _val_12.setMinimumSize(new Dimension(55, 20));
        _val_12.setPreferredSize(new Dimension(55, 20));
        _val_12.setMaximumSize(new Dimension(55, 20));
        _val_12.setForeground(Color.black);
        _val_12.setBackground(Color.white);
        _val_12.setFont(new Font("Dialog", Font.PLAIN, 11));
        _val_12.setBorder(BorderFactory.createCompoundBorder());
        _val_12.setSelectedIndex(_vars.getDateValue02());

        _labelThen_1 = new JLabel("Then:");

        String[] res1_str = { " Overwrite Src", " Overwrite Dst", " Delete Src", " Delete Dst", " Delete Both" };
        _res_1 = new JComboBox(res1_str);
        _res_1.addActionListener(this);
        _res_1.setMinimumSize(new Dimension(120, 20));
        _res_1.setPreferredSize(new Dimension(120, 20));
        _res_1.setMaximumSize(new Dimension(120, 20));
        _res_1.setForeground(Color.black);
        _res_1.setBackground(Color.white);
        _res_1.setFont(new Font("Dialog", Font.PLAIN, 11));
        _res_1.setBorder(BorderFactory.createCompoundBorder());
        _res_1.setSelectedIndex(_vars.getDateModifyDir());

        _labelDate = new JLabel("Date:");

        _fDate = new JTextField(_vars.getFileSize());
        _fDate.addFocusListener(this);
        _fDate.setFont(new Font("Helvetica", Font.PLAIN, 11));
        _fDate.setMinimumSize(new Dimension(55, 20));
        _fDate.setPreferredSize(new Dimension(55, 20));
        _fDate.setMaximumSize(new Dimension(55, 20));
        _fDate.setText(_vars.getDateSet());
        _fDate.setToolTipText("DD/MM/YY");

        _labelIf_2 = new JLabel("If:");

        String[] val_21str = { " Src", " Dst" };
        _val_21 = new JComboBox(val_21str);
        _val_21.addActionListener(this);
        _val_21.setMinimumSize(new Dimension(50, 20));
        _val_21.setPreferredSize(new Dimension(50, 20));
        _val_21.setMaximumSize(new Dimension(50, 20));
        _val_21.setForeground(Color.black);
        _val_21.setBackground(Color.white);
        _val_21.setFont(new Font("Dialog", Font.PLAIN, 11));
        _val_21.setBorder(BorderFactory.createCompoundBorder());
        _val_21.setSelectedIndex(_vars.getSizeValue01());

        String[] condStr2 = { "    >", "    >=", "    <", "    <=", "    ><", "    ==" };
        _cond_2 = new JComboBox(condStr2);
        _cond_2.addActionListener(this);
        _cond_2.setMinimumSize(new Dimension(60, 20));
        _cond_2.setPreferredSize(new Dimension(60, 20));
        _cond_2.setMaximumSize(new Dimension(60, 20));
        _cond_2.setForeground(Color.black);
        _cond_2.setBackground(Color.white);
        _cond_2.setFont(new Font("Dialog", Font.PLAIN, 11));
        _cond_2.setBorder(BorderFactory.createCompoundBorder());
        _cond_2.setSelectedIndex(_vars.getSizeCondition());

        String[] val_22str = { " Src", " Dst", " Size" };
        _val_22 = new JComboBox(val_22str);
        _val_22.addActionListener(this);
        _val_22.setMinimumSize(new Dimension(55, 20));
        _val_22.setPreferredSize(new Dimension(55, 20));
        _val_22.setMaximumSize(new Dimension(55, 20));
        _val_22.setForeground(Color.black);
        _val_22.setBackground(Color.white);
        _val_22.setFont(new Font("Dialog", Font.PLAIN, 11));
        _val_22.setBorder(BorderFactory.createCompoundBorder());
        _val_22.setSelectedIndex(_vars.getSizeValue02());

        _labelThen_2 = new JLabel("Then:");

        _labelkB = new JLabel("Bytes:");

        String[] res2_str = { " Overwrite Src", " Overwrite Dst", " Delete Src", " Delete Dst", " Delete Both" };
        _res_2 = new JComboBox(res2_str);
        _res_2.addActionListener(this);
        _res_2.setMinimumSize(new Dimension(120, 20));
        _res_2.setPreferredSize(new Dimension(120, 20));
        _res_2.setMaximumSize(new Dimension(120, 20));
        _res_2.setForeground(Color.black);
        _res_2.setBackground(Color.white);
        _res_2.setFont(new Font("Dialog", Font.PLAIN, 11));
        _res_2.setBorder(BorderFactory.createCompoundBorder());
        _res_2.setSelectedIndex(_vars.getSizeModifyDir());

        _fSize = new JTextField(_vars.getFileSize());
        _fSize.addFocusListener(this);
        _fSize.setFont(new Font("Helvetica", Font.PLAIN, 11));
        _fSize.setMinimumSize(new Dimension(55, 20));
        _fSize.setPreferredSize(new Dimension(55, 20));
        _fSize.setMaximumSize(new Dimension(55, 20));
        _fSize.setText(_vars.getFileSize());

        // Position the fields in the panel
        Insets insets = conditions.getInsets();
        Dimension size;
        size = _modifyOptions.getPreferredSize();
        _modifyOptions.setBounds(15 + insets.left, insets.top, size.width, size.height);
        size = _labelIf_1.getPreferredSize();
        _labelIf_1.setBounds(15 + insets.left, 28 + insets.top, size.width, size.height);
        size = _val_11.getPreferredSize();
        _val_11.setBounds(15 + insets.left, 50 + insets.top, size.width, size.height);
        size = _cond_1.getPreferredSize();
        _cond_1.setBounds(75 + insets.left, 50 + insets.top, size.width, size.height);
        size = _val_12.getPreferredSize();
        _val_12.setBounds(145 + insets.left, 50 + insets.top, size.width, size.height);
        size = _labelThen_1.getPreferredSize();
        _labelThen_1.setBounds(15 + insets.left, 76 + insets.top, size.width, size.height);
        size = _labelDate.getPreferredSize();
        _labelDate.setBounds(145 + insets.left, 76 + insets.top, size.width, size.height);
        size = _res_1.getPreferredSize();
        _res_1.setBounds(15 + insets.left, 98 + insets.top, size.width, size.height);
        size = _fDate.getPreferredSize();
        _fDate.setBounds(145 + insets.left, 98 + insets.top, size.width, size.height);
        size = _labelIf_2.getPreferredSize();
        _labelIf_2.setBounds(15 + insets.left, 124 + insets.top, size.width, size.height);
        size = _val_21.getPreferredSize();
        _val_21.setBounds(15 + insets.left, 146 + insets.top, size.width, size.height);
        size = _cond_2.getPreferredSize();
        _cond_2.setBounds(75 + insets.left, 146 + insets.top, size.width, size.height);
        size = _val_22.getPreferredSize();
        _val_22.setBounds(145 + insets.left, 146 + insets.top, size.width, size.height);
        size = _labelThen_2.getPreferredSize();
        _labelThen_2.setBounds(15 + insets.left, 172 + insets.top, size.width, size.height);
        size = _labelkB.getPreferredSize();
        _labelkB.setBounds(145 + insets.left, 172 + insets.top, size.width, size.height);
        size = _res_2.getPreferredSize();
        _res_2.setBounds(15 + insets.left, 194 + insets.top, size.width, size.height);
        size = _fSize.getPreferredSize();
        _fSize.setBounds(145 + insets.left, 194 + insets.top, size.width, size.height);

        conditions.add(_modifyOptions);
        conditions.add(_labelIf_1);
        conditions.add(_val_11);
        conditions.add(_cond_1);
        conditions.add(_val_12);
        conditions.add(_labelThen_1);
        conditions.add(_labelDate);
        conditions.add(_res_1);
        conditions.add(_fDate);
        conditions.add(_labelIf_2);
        conditions.add(_val_21);
        conditions.add(_cond_2);
        conditions.add(_val_22);
        conditions.add(_labelThen_2);
        conditions.add(_labelkB);
        conditions.add(_res_2);
        conditions.add(_fSize);

        return conditions;
    }

    class GlassPane extends JComponent {

        public GlassPane() {


        }
    }

    // Create Panel for the Event Table list and buttons
    private JPanel schedulePanel()
    {
        LogManager.write("Configurator         - Creating: \"Config\" -> \"Scheduler\".", LogHandle.ALL,LogManager.DEBUG);

        JPanel scheduler = new JPanel();
        scheduler.setLayout(null);
        scheduler.setMinimumSize(new Dimension(120, 255));
        scheduler.setPreferredSize(new Dimension(120, 255));
        scheduler.setMaximumSize(new Dimension(120, 255));

        TitledBorder titledborder = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Scheduler:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
        scheduler.setBorder(titledborder);

        _chooseNone = new JRadioButton("  None");
        _chooseDaily = new JRadioButton("  Daily");
        _chooseWeekly = new JRadioButton("  Weekly");
        _chooseMonthly = new JRadioButton("  Monthly");

        _chooseNone.addActionListener(this);
        _chooseDaily.addActionListener(this);
        _chooseWeekly.addActionListener(this);
        _chooseMonthly.addActionListener(this);

        switch(_vars.getSchedulePeriod())
        {
            case 0:  _chooseNone.setSelected(true); break;
            case 1:  _chooseDaily.setSelected(true); break;
            case 2:  _chooseWeekly.setSelected(true); break;
            case 3:  _chooseMonthly.setSelected(true); break;
            default: _chooseNone.setSelected(true); break;
        }

        ButtonGroup period = new ButtonGroup();
        period.add(_chooseNone);
        period.add(_chooseDaily);
        period.add(_chooseWeekly);
        period.add(_chooseMonthly);

        _labelEvery = new JLabel("Every:");

        String[] daysOfWeek = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
        _weekDays = new JComboBox(daysOfWeek);
        _weekDays.addActionListener(this);
        _weekDays.setMinimumSize(new Dimension(90, 20));
        _weekDays.setPreferredSize(new Dimension(90, 20));
        _weekDays.setMaximumSize(new Dimension(90, 20));
        _weekDays.setForeground(Color.black);
        _weekDays.setBackground(Color.white);
        _weekDays.setFont(new Font("Dialog", Font.PLAIN, 11));
        _weekDays.setBorder(BorderFactory.createCompoundBorder());
        _weekDays.setSelectedIndex(_vars.getWeekDay());

        _daysOfMonth = new JTextField(_vars.getMonthDay());
        _daysOfMonth.addFocusListener(this);
        _daysOfMonth.setFont(new Font("Helvetica", Font.PLAIN, 11));
        _daysOfMonth.setMinimumSize(new Dimension(90, 20));
        _daysOfMonth.setPreferredSize(new Dimension(90, 20));
        _daysOfMonth.setMaximumSize(new Dimension(90, 20));

        _labelAt = new JLabel("At:");

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
                "23:00", "23:15", "23:30", "23:45" };
        _timeIntervalSelector = new JComboBox(timeIntervals);
        _timeIntervalSelector.addActionListener(this);
        _timeIntervalSelector.setMinimumSize(new Dimension(60, 20));
        _timeIntervalSelector.setPreferredSize(new Dimension(60, 20));
        _timeIntervalSelector.setMaximumSize(new Dimension(60, 20));
        _timeIntervalSelector.setForeground(Color.black);
        _timeIntervalSelector.setBackground(Color.white);
        _timeIntervalSelector.setFont(new Font("Dialog", Font.PLAIN, 11));
        _timeIntervalSelector.setBorder(BorderFactory.createCompoundBorder());
        _timeIntervalSelector.setSelectedIndex(_vars.getRunAtTime());

        // Position the fields in the panel
        Insets insets = scheduler.getInsets();
        Dimension size;
        size = _chooseNone.getPreferredSize();
        _chooseNone.setBounds(5 + insets.left, insets.top, size.width, size.height);
        size = _chooseNone.getPreferredSize();
        _chooseDaily.setBounds(5 + insets.left, 25 + insets.top, size.width, size.height);
        size = _chooseWeekly.getPreferredSize();
        _chooseWeekly.setBounds(5 + insets.left, 50 + insets.top, size.width, size.height);
        size = _chooseMonthly.getPreferredSize();
        _chooseMonthly.setBounds(5 + insets.left, 75 + insets.top, size.width, size.height);
        size = _labelEvery.getPreferredSize();
        _labelEvery.setBounds(5 + insets.left, 106 + insets.top, size.width, size.height);
        size = _weekDays.getPreferredSize();
        _weekDays.setBounds(5 + insets.left, 126 + insets.top, size.width, size.height);
        size = _daysOfMonth.getPreferredSize();
        _daysOfMonth.setBounds(5 + insets.left, 150 + insets.top, size.width, size.height);
        size = _labelAt.getPreferredSize();
        _labelAt.setBounds(5 + insets.left, 176 + insets.top, size.width, size.height);
        size = _timeIntervalSelector.getPreferredSize();
        _timeIntervalSelector.setBounds(5 + insets.left, 194 + insets.top, size.width, size.height);

        scheduler.add(_chooseNone);
        scheduler.add(_chooseDaily);
        scheduler.add(_chooseWeekly);
        scheduler.add(_chooseMonthly);
        scheduler.add(_labelEvery);
        scheduler.add(_weekDays);
        scheduler.add(_daysOfMonth);
        scheduler.add(_labelAt);
        scheduler.add(_timeIntervalSelector);

        return scheduler;
    }

    // Create Report Frame.
    private JPanel ReportFrame()
    {
        LogManager.write("Configurator         - Creating: \"Report\" tab.", LogHandle.ALL,LogManager.DEBUG);

        JPanel reportPanel = new JPanel();
        reportPanel.setBounds(10, 150, 492, 200);
        reportPanel.setLayout(new BoxLayout(reportPanel, BoxLayout.Y_AXIS));
        reportPanel.add(reportButtonsPanel());
        reportPanel.add(reportTablePane());

        return reportPanel;
    }

    private JPanel reportButtonsPanel()
    {
        JPanel reportButtons = new JPanel();
        reportButtons.setLayout(new BoxLayout(reportButtons, BoxLayout.LINE_AXIS));
        reportButtons.setBounds(0, 0, 481, 30);
        //reportButtons.setBackground(Color.WHITE);
        reportButtons.setBorder(new EtchedBorder(0, Color.darkGray, Color.white));

        JLabel labelLog = new JLabel("Log Level:");

        String[] logLevelStr = { " Info", " Update", " Error" };
        _reportLevel = new JComboBox(logLevelStr);
        _reportLevel.addActionListener(this);
        _reportLevel.setMinimumSize(new Dimension(80, 20));
        _reportLevel.setPreferredSize(new Dimension(80, 20));
        _reportLevel.setMaximumSize(new Dimension(80, 20));
        _reportLevel.setForeground(Color.black);
        _reportLevel.setBackground(Color.white);
        _reportLevel.setFont(new Font("Dialog", Font.PLAIN, 11));
        _reportLevel.setBorder(BorderFactory.createCompoundBorder());
        _reportLevel.setSelectedIndex(_vars.getReportLevel());

        _clearReportButton = new JButton(new ImageIcon(_dmghome + "images/stat_clear.gif"));
        //_clearReportButton.setBorder(BorderFactory.createEtchedBorder());
        _clearReportButton.setMinimumSize(new Dimension(20, 20));
        _clearReportButton.setPreferredSize(new Dimension(20, 20));
        _clearReportButton.setMaximumSize(new Dimension(20, 20));
        _clearReportButton.setToolTipText("Clear statistic.");
        _clearReportButton.addActionListener(this);

        _reportToFile = new JCheckBox(" Report to File.");
        _reportToFile.addActionListener(this);
        _reportToFile.setSelected(_vars.getReportToFile());

        reportButtons.add(Box.createRigidArea(new Dimension(12,25)));
        reportButtons.add(labelLog);
        reportButtons.add(Box.createRigidArea(new Dimension(10,25)));
        reportButtons.add(_reportLevel);
        reportButtons.add(Box.createHorizontalStrut(10));
        reportButtons.add(new JLabel(new ImageIcon(_dmghome + "images/sep.gif")));
        reportButtons.add(Box.createHorizontalStrut(10));
        reportButtons.add(_clearReportButton);
        reportButtons.add(Box.createHorizontalStrut(10));
        reportButtons.add(new JLabel(new ImageIcon(_dmghome + "images/sep.gif")));
        reportButtons.add(Box.createHorizontalStrut(10));
        reportButtons.add(_reportToFile);
        //reportButtons.add(Box.createHorizontalStrut(5));
        //reportButtons.add(new JSeparator(SwingConstants.VERTICAL));
        reportButtons.add(Box.createHorizontalGlue());

        return reportButtons;
    }

    private JPanel reportTablePane()
    {
        JPanel reportTableFrame = new JPanel();
        reportTableFrame.setLayout(new BorderLayout());
        reportTableFrame.setBounds(0, 30, 481, 100);
        reportTableFrame.setBackground(Color.WHITE);

        Vector colNames = new Vector();
        colNames.removeAllElements();
        colNames.addElement("Filename");
        colNames.addElement("Action");
        colNames.addElement("Location");
        colNames.addElement("Status");

        Vector reportData = new Vector();

        _reportTableModel = new DMGTableModel(reportData, colNames);
        JTable reportTable = new JTable(_reportTableModel);

        reportTable.getColumnModel().getColumn(1).setMinWidth(90);
        reportTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        reportTable.getColumnModel().getColumn(1).setMaxWidth(90);
        reportTable.getColumnModel().getColumn(1).setWidth(90);
        reportTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component myself = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                return myself;
            }
        });

        reportTable.getColumnModel().getColumn(2).setMinWidth(90);
        reportTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        reportTable.getColumnModel().getColumn(2).setMaxWidth(90);
        reportTable.getColumnModel().getColumn(2).setWidth(90);
        reportTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component myself = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                return myself;
            }
        });

        reportTable.getColumnModel().getColumn(3).setMinWidth(70);
        reportTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        reportTable.getColumnModel().getColumn(3).setMaxWidth(70);
        reportTable.getColumnModel().getColumn(3).setWidth(70);
        reportTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component myself = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                return myself;
            }
        });

        JScrollPane scrollTable = new JScrollPane(reportTable);
        scrollTable.setPreferredSize(new Dimension(481, 205));

        reportTableFrame.add(scrollTable);

        return reportTableFrame;
    }

    private JPanel statusBar() {

        JPanel statBar = new JPanel();
        statBar.setLayout(new BoxLayout(statBar, BoxLayout.LINE_AXIS));
        statBar.setBounds(0, 0, 481, 20);

        _progressBar = new JProgressBar(0, 100);
        _progressBar.setValue(0);
        _progressBar.setStringPainted(true);
        _progressBar.setMaximumSize(new Dimension(145, 15));

        JLabel labelFiles = new JLabel("Files: ");
        labelFiles.setEnabled(false);
        _numFiles = new JTextField("");
        _numFiles.setBackground(statBar.getBackground());
        _numFiles.setMaximumSize(new Dimension(70,18));
        _numFiles.setEditable(false);
        _numFiles.setBorder(null);

        JLabel labelSize = new JLabel("Size: ");
        labelSize.setEnabled(false);
        _dataSize = new JTextField("");
        _dataSize.setBackground(statBar.getBackground());
        _dataSize.setMaximumSize(new Dimension(70,18));
        _dataSize.setEditable(false);
        _dataSize.setBorder(null);

        JLabel labelTime = new JLabel("Time: ");
        labelTime.setEnabled(false);
        _timeSec = new JTextField("");
        _timeSec.setBackground(statBar.getBackground());
        _timeSec.setMaximumSize(new Dimension(70,18));
        _timeSec.setEditable(false);
        _timeSec.setBorder(null);

        statBar.add(Box.createRigidArea(new Dimension(10,20)));
        statBar.add(_progressBar);
        statBar.add(Box.createRigidArea(new Dimension(15,20)));
        statBar.add(labelFiles);
        statBar.add(_numFiles);
        statBar.add(Box.createRigidArea(new Dimension(8,20)));
        statBar.add(labelSize);
        statBar.add(_dataSize);
        statBar.add(Box.createRigidArea(new Dimension(8,20)));
        statBar.add(labelTime);
        statBar.add(_timeSec);

        return statBar;
    }

    private void saveVars()
    {
        _vars.setSourceDir(_src.getText());
        _vars.setDestinationDir(_dest.getText());
        _vars.setMonthDay(_daysOfMonth.getText());
        _vars.setDateSet(_fDate.getText());
        _vars.setFileSize(_fSize.getText());

        _vars.saveProps(_dmghome);
    }

    private void enableMenus()
    {

        if (_chooseByNone.isSelected())
        {
            _modifyOptions.setEnabled(true);

            _dateNewest.setEnabled(false);
            _dateOldest.setEnabled(false);
            _dateCustom.setEnabled(false);
            _sizeBiggest.setEnabled(false);
            _sizeSmalest.setEnabled(false);
            _sizeCustom.setEnabled(false);

            _labelIf_1.setEnabled(false);
            _val_11.setEnabled(false);
            _cond_1.setEnabled(false);
            _val_12.setEnabled(false);
            _labelThen_1.setEnabled(false);
            _res_1.setEnabled(false);
            _labelDate.setEnabled(false);
            _fDate.setEnabled(false);

            _labelIf_2.setEnabled(false);
            _val_21.setEnabled(false);
            _cond_2.setEnabled(false);
            _val_22.setEnabled(false);
            _labelThen_2.setEnabled(false);
            _res_2.setEnabled(false);
            _labelkB.setEnabled(false);
            _fSize.setEnabled(false);
        }
        else if (_chooseByDate.isSelected())
        {
            _dateNewest.setEnabled(true);
            _dateOldest.setEnabled(true);
            _dateCustom.setEnabled(true);
            _sizeBiggest.setEnabled(false);
            _sizeSmalest.setEnabled(false);
            _sizeCustom.setEnabled(false);
            _labelDate.setEnabled(false);
            _fDate.setEnabled(false);

            if (_dateCustom.isSelected())
            {
                //_modifyOptions.setEnabled(false);

                _labelIf_1.setEnabled(true);
                _val_11.setEnabled(true);
                _cond_1.setEnabled(true);
                _val_12.setEnabled(true);
                _labelThen_1.setEnabled(true);
                _res_1.setEnabled(true);

                if (_val_12.getSelectedIndex() == 2)
                {
                    _labelDate.setEnabled(true);
                    _fDate.setEnabled(true);
                }
                else
                {
                    _labelDate.setEnabled(false);
                    _fDate.setEnabled(false);
                }
            }
            else
            {
                //_modifyOptions.setEnabled(true);

                _labelIf_1.setEnabled(false);
                _val_11.setEnabled(false);
                _cond_1.setEnabled(false);
                _val_12.setEnabled(false);
                _labelThen_1.setEnabled(false);
                _res_1.setEnabled(false);
                _labelDate.setEnabled(false);
                _fDate.setEnabled(false);
            }

            _labelIf_2.setEnabled(false);
            _val_21.setEnabled(false);
            _cond_2.setEnabled(false);
            _val_22.setEnabled(false);
            _labelThen_2.setEnabled(false);
            _res_2.setEnabled(false);
            _labelkB.setEnabled(false);
            _fSize.setEnabled(false);
        }
        else if (_chooseBySize.isSelected())
        {
            _dateNewest.setEnabled(false);
            _dateOldest.setEnabled(false);
            _dateCustom.setEnabled(false);
            _sizeBiggest.setEnabled(true);
            _sizeSmalest.setEnabled(true);
            _sizeCustom.setEnabled(true);

            _labelIf_1.setEnabled(false);
            _val_11.setEnabled(false);
            _cond_1.setEnabled(false);
            _val_12.setEnabled(false);
            _labelThen_1.setEnabled(false);
            _res_1.setEnabled(false);
            _labelDate.setEnabled(false);
            _fDate.setEnabled(false);

            if (_sizeCustom.isSelected())
            {
                //_modifyOptions.setEnabled(false);

                _labelIf_2.setEnabled(true);
                _val_21.setEnabled(true);
                _cond_2.setEnabled(true);
                _val_22.setEnabled(true);
                _labelThen_2.setEnabled(true);
                _res_2.setEnabled(true);

                if (_val_22.getSelectedIndex() == 2)
                {
                    _labelkB.setEnabled(true);
                    _fSize.setEnabled(true);
                }
                else
                {
                    _labelkB.setEnabled(false);
                    _fSize.setEnabled(false);
                }
            }
            else
            {
                //_modifyOptions.setEnabled(true);

                _labelIf_2.setEnabled(false);
                _val_21.setEnabled(false);
                _cond_2.setEnabled(false);
                _val_22.setEnabled(false);
                _labelThen_2.setEnabled(false);
                _res_2.setEnabled(false);
                _labelkB.setEnabled(false);
                _fSize.setEnabled(false);

            }
        }

        if (_chooseNone.isSelected())
        {
            _labelEvery.setEnabled(false);
            _weekDays.setEnabled(false);
            _daysOfMonth.setEnabled(false);
            _labelAt.setEnabled(false);
            _timeIntervalSelector.setEnabled(false);
        }
        else if (_chooseDaily.isSelected())
        {
            _labelEvery.setEnabled(false);
            _weekDays.setEnabled(false);
            _daysOfMonth.setEnabled(false);
            _labelAt.setEnabled(true);
            _timeIntervalSelector.setEnabled(true);
        }
        else if (_chooseWeekly.isSelected())
        {
            _labelEvery.setEnabled(true);
            _weekDays.setEnabled(true);
            _daysOfMonth.setEnabled(false);
            _labelAt.setEnabled(true);
            _timeIntervalSelector.setEnabled(true);
        }
        else if (_chooseMonthly.isSelected())
        {
            _labelEvery.setEnabled(true);
            _weekDays.setEnabled(false);
            _daysOfMonth.setEnabled(true);
            _labelAt.setEnabled(true);
            _timeIntervalSelector.setEnabled(true);
        }

        // Modify the menu depending on the conditions
        if (_cond_1.getSelectedIndex() < 2)
        {
            String[] val_12str = { " Src", " Dst", " Date", " Hour", " Day", " Week", "Month", " Year" };
            _val_12.removeAllItems();
            for (int i=0; i < val_12str.length; i++)
            {
                _val_12.addItem(val_12str[i]);
            }
        }
        else
        {
            String[] val_12str = { " Src", " Dst" };
            _val_12.removeAllItems();
            for (int i=0; i < val_12str.length; i++)
            {
                _val_12.addItem(val_12str[i]);
            }
        }
        _val_12.setSelectedIndex(_vars.getDateValue02());
    }

    // Create Actions
    public void actionPerformed(ActionEvent e)
    {
        // Verify if actions are allowed
        if (!enableActions)
            return;

        Object source = e.getSource();

        // Buttons and Menu items Section
        if (source == _recurseBox)
        {
            _vars.setRecurse(_recurseBox.isSelected());
        }
        else if (source == _chooseByNone)
        {
            _vars.setAction(NO_ACTION);

            //_modifyOptions.setEnabled(true);

            _dateNewest.setEnabled(false);
            _dateOldest.setEnabled(false);
            _dateCustom.setEnabled(false);
            _sizeBiggest.setEnabled(false);
            _sizeSmalest.setEnabled(false);
            _sizeCustom.setEnabled(false);

            _labelIf_1.setEnabled(false);
            _val_11.setEnabled(false);
            _cond_1.setEnabled(false);
            _val_12.setEnabled(false);
            _labelThen_1.setEnabled(false);
            _res_1.setEnabled(false);
            _labelDate.setEnabled(false);
            _fDate.setEnabled(false);

            _labelIf_2.setEnabled(false);
            _val_21.setEnabled(false);
            _cond_2.setEnabled(false);
            _val_22.setEnabled(false);
            _labelThen_2.setEnabled(false);
            _res_2.setEnabled(false);
            _labelkB.setEnabled(false);
            _fSize.setEnabled(false);
        }
        else if (source == _chooseByDate)
        {
            _vars.setAction(DECIDE_BY_DATE);

            _dateNewest.setEnabled(true);
            _dateOldest.setEnabled(true);
            _dateCustom.setEnabled(true);
            _sizeBiggest.setEnabled(false);
            _sizeSmalest.setEnabled(false);
            _sizeCustom.setEnabled(false);

            if (_dateCustom.isSelected())
            {
                //_modifyOptions.setEnabled(false);

                _labelIf_1.setEnabled(true);
                _val_11.setEnabled(true);
                _cond_1.setEnabled(true);
                _val_12.setEnabled(true);
                _labelThen_1.setEnabled(true);
                _res_1.setEnabled(true);

                if (_val_12.getSelectedIndex() == 2)
                {
                    _labelDate.setEnabled(true);
                    _fDate.setEnabled(true);
                }
                else
                {
                    _labelDate.setEnabled(false);
                    _fDate.setEnabled(false);
                }
            }
            else
            {
                //_modifyOptions.setEnabled(true);

                _labelIf_1.setEnabled(false);
                _val_11.setEnabled(false);
                _cond_1.setEnabled(false);
                _val_12.setEnabled(false);
                _labelThen_1.setEnabled(false);
                _res_1.setEnabled(false);
                _labelDate.setEnabled(false);
                _fDate.setEnabled(false);
            }

            _labelIf_2.setEnabled(false);
            _val_21.setEnabled(false);
            _cond_2.setEnabled(false);
            _val_22.setEnabled(false);
            _labelThen_2.setEnabled(false);
            _res_2.setEnabled(false);
            _labelkB.setEnabled(false);
            _fSize.setEnabled(false);
        }
        else if (source == _chooseBySize)
        {
            _vars.setAction(DECIDE_BY_SIZE);

            _dateNewest.setEnabled(false);
            _dateOldest.setEnabled(false);
            _dateCustom.setEnabled(false);
            _sizeBiggest.setEnabled(true);
            _sizeSmalest.setEnabled(true);
            _sizeCustom.setEnabled(true);

            _labelIf_1.setEnabled(false);
            _val_11.setEnabled(false);
            _cond_1.setEnabled(false);
            _val_12.setEnabled(false);
            _labelThen_1.setEnabled(false);
            _res_1.setEnabled(false);
            _labelDate.setEnabled(false);
            _fDate.setEnabled(false);

            if (_sizeCustom.isSelected())
            {
                //_modifyOptions.setEnabled(false);

                _labelIf_2.setEnabled(true);
                _val_21.setEnabled(true);
                _cond_2.setEnabled(true);
                _val_22.setEnabled(true);
                _labelThen_2.setEnabled(true);
                _res_2.setEnabled(true);

                if (_val_22.getSelectedIndex() == 2)
                {
                    _labelkB.setEnabled(true);
                    _fSize.setEnabled(true);
                }
                else
                {
                    _labelkB.setEnabled(false);
                    _fSize.setEnabled(false);
                }
            }
            else
            {
                //_modifyOptions.setEnabled(true);

                _labelIf_2.setEnabled(false);
                _val_21.setEnabled(false);
                _cond_2.setEnabled(false);
                _val_22.setEnabled(false);
                _labelThen_2.setEnabled(false);
                _res_2.setEnabled(false);
                _labelkB.setEnabled(false);
                _fSize.setEnabled(false);
            }
        }
        else if (source == _dateNewest)
        {
            _vars.setActionDate(KEEP_NEWEST);

            //_modifyOptions.setEnabled(true);

            _labelIf_1.setEnabled(false);
            _val_11.setEnabled(false);
            _cond_1.setEnabled(false);
            _val_12.setEnabled(false);
            _labelThen_1.setEnabled(false);
            _res_1.setEnabled(false);
            _labelDate.setEnabled(false);
            _fDate.setEnabled(false);

        }
        else if (source == _dateOldest)
        {
            _vars.setActionDate(KEEP_OLDEST);

            //_modifyOptions.setEnabled(true);

            _labelIf_1.setEnabled(false);
            _val_11.setEnabled(false);
            _cond_1.setEnabled(false);
            _val_12.setEnabled(false);
            _labelThen_1.setEnabled(false);
            _res_1.setEnabled(false);
            _labelDate.setEnabled(false);
            _fDate.setEnabled(false);
        }
        else if (source == _dateCustom)
        {
            _vars.setActionDate(DATE_CUSTOM);

            //_modifyOptions.setEnabled(false);

            _labelIf_1.setEnabled(true);
            _val_11.setEnabled(true);
            _cond_1.setEnabled(true);
            _val_12.setEnabled(true);
            _labelThen_1.setEnabled(true);
            _res_1.setEnabled(true);

            if (_val_12.getSelectedIndex() == 2)
            {
                _labelDate.setEnabled(true);
                _fDate.setEnabled(true);
            }
            else
            {
                _labelDate.setEnabled(false);
                _fDate.setEnabled(false);
            }
        }
        else if (source == _sizeBiggest)
        {
            _vars.setActionSize(KEEP_BIGGEST);

            //_modifyOptions.setEnabled(true);

            _labelIf_2.setEnabled(false);
            _val_21.setEnabled(false);
            _cond_2.setEnabled(false);
            _val_22.setEnabled(false);
            _labelThen_2.setEnabled(false);
            _res_2.setEnabled(false);
            _labelkB.setEnabled(false);
            _fSize.setEnabled(false);
        }
        else if (source == _sizeSmalest)
        {
            _vars.setActionSize(KEEP_SMALLEST);

            //_modifyOptions.setEnabled(true);

            _labelIf_2.setEnabled(false);
            _val_21.setEnabled(false);
            _cond_2.setEnabled(false);
            _val_22.setEnabled(false);
            _labelThen_2.setEnabled(false);
            _res_2.setEnabled(false);
            _labelkB.setEnabled(false);
            _fSize.setEnabled(false);
        }
        else if (source == _sizeCustom)
        {
            _vars.setActionSize(SIZE_CUSTOM);

            //_modifyOptions.setEnabled(false);

            _labelIf_2.setEnabled(true);
            _val_21.setEnabled(true);
            _cond_2.setEnabled(true);
            _val_22.setEnabled(true);
            _labelThen_2.setEnabled(true);
            _res_2.setEnabled(true);
            if (_val_22.getSelectedIndex() == 2)
            {
                _labelkB.setEnabled(true);
                _fSize.setEnabled(true);
            }
            else
            {
                _labelkB.setEnabled(false);
                _fSize.setEnabled(false);
            }
        }
        else if (source == _chooseNone)
        {
            _vars.setSchedulePeriod(NO_SCHEDULE);

            _scheduleModule.stopScheduler();

            _labelEvery.setEnabled(false);
            _weekDays.setEnabled(false);
            _daysOfMonth.setEnabled(false);
            _labelAt.setEnabled(false);
            _timeIntervalSelector.setEnabled(false);
        }
        else if (source == _chooseDaily)
        {
            _vars.setSchedulePeriod(DAILY);

            if (!_scheduleModule.isRunning()) {
                _scheduleModule.startScheduler();
            }

            _labelEvery.setEnabled(false);
            _weekDays.setEnabled(false);
            _daysOfMonth.setEnabled(false);
            _labelAt.setEnabled(true);
            _timeIntervalSelector.setEnabled(true);
        }
        else if (source == _chooseWeekly)
        {
            _vars.setSchedulePeriod(WEEKLY);

            if (!_scheduleModule.isRunning()) {
                _scheduleModule.startScheduler();
            }

            _labelEvery.setEnabled(true);
            _weekDays.setEnabled(true);
            _daysOfMonth.setEnabled(false);
            _labelAt.setEnabled(true);
            _timeIntervalSelector.setEnabled(true);
        }
        else if (source == _chooseMonthly)
        {
            _vars.setSchedulePeriod(MONTHLY);

            if (!_scheduleModule.isRunning()) {
                _scheduleModule.startScheduler();
            }

            _labelEvery.setEnabled(true);
            _weekDays.setEnabled(false);
            _daysOfMonth.setEnabled(true);
            _labelAt.setEnabled(true);
            _timeIntervalSelector.setEnabled(true);
        }
        else if (source == _modifyOptions)
        {
            _vars.setModifyDir(_modifyOptions.getSelectedIndex());
        }
        else if (source == _val_11)
        {
            _vars.setDateValue01(_val_11.getSelectedIndex());
        }
        else if (source == _cond_1)
        {
            _vars.setDateCondition(_cond_1.getSelectedIndex());
            //LogManager.write("Configurator         - Index changed to: "+_cond_1.getSelectedIndex()+".", LogHandle.ALL,LogManager.DEBUG);

            // Manipulation for changing the drop-down menu depending on the users choice of condition.

            // Remember users old choice
            int pos = _val_12.getSelectedIndex();

            // Modify the menu
            if (_cond_1.getSelectedIndex() < 2)
            {
                String[] val_12str = { " Src", " Dst", " Date", " Hour", " Day", " Week", "Month", " Year" };
                _val_12.removeAllItems();
                for (int i=0; i < val_12str.length; i++)
                {
                    _val_12.addItem(val_12str[i]);
                }
            }
            else
            {
                String[] val_12str = { " Src", " Dst" };
                _val_12.removeAllItems();
                for (int i=0; i < val_12str.length; i++)
                {
                    _val_12.addItem(val_12str[i]);
                }
            }

            // Set the users old choice
            if (pos <= _val_12.getItemCount())
            {
                _val_12.setSelectedIndex(pos);
            } else {
                _val_12.setSelectedIndex(1);
            }
        }
        else if (source == _val_12)
        {
            _vars.setDateValue02(_val_12.getSelectedIndex());

            if (_val_12.getSelectedIndex() == 2)
            {
                _labelDate.setEnabled(true);
                _fDate.setEnabled(true);
            }
            else
            {
                _labelDate.setEnabled(false);
                _fDate.setEnabled(false);
            }
        }
        else if (source == _res_1)
        {
            _vars.setDateModifyDir(_res_1.getSelectedIndex());
        }
        else if (source == _val_21)
        {
            _vars.setSizeValue01(_val_21.getSelectedIndex());
        }
        else if (source == _cond_2)
        {
            _vars.setSizeCondition(_cond_2.getSelectedIndex());
        }
        else if (source == _val_22)
        {
            _vars.setSizeValue02(_val_22.getSelectedIndex());

            if (_val_22.getSelectedIndex() == 2)
            {
                _labelkB.setEnabled(true);
                _fSize.setEnabled(true);
            }
            else
            {
                _labelkB.setEnabled(false);
                _fSize.setEnabled(false);
            }
        }
        else if (source == _res_2)
        {
            _vars.setSizeModifyDir(_res_2.getSelectedIndex());
        }
        else if (source == _weekDays)
        {
            _vars.setWeekDay(_weekDays.getSelectedIndex());
        }
        else if (source == _timeIntervalSelector)
        {
            _vars.setRunAtTime(_timeIntervalSelector.getSelectedIndex());
        }
        else if (source == _reportLevel)
        {
            _vars.setReportLevel(_reportLevel.getSelectedIndex());
        }
        else if (source == _clearReportButton)
        {
            _reportTableModel.clearTable();
            _numFiles.setText("");
            _dataSize.setText("");
            _timeSec.setText("");
        }
        else if (source == _reportToFile)
        {
            _vars.setReportToFile(_reportToFile.isSelected());
        }
    }

    public void focusGained(FocusEvent e){
        Object source = e.getSource();

        // Text fields values
        if (source == _src)
        {
            if (_src.getText().equals("Select Source directory.")) {
                _src.setText("");
            }
        }
        else if (source == _dest)
        {
            if (_dest.getText().equals("Select Destination directory.")) {
                _dest.setText("");
            }
        }
    }

    public void focusLost(FocusEvent e){
        Object source = e.getSource();

        // Text fields values
        if (source == _src)
        {
            if (_src.getText().trim().equals("")) {
                _src.setText("Select Source directory.");
            }
            _vars.setSourceDir(_src.getText());
        }
        else if (source == _dest)
        {
            if (_dest.getText().equals("")) {
                _dest.setText("Select Destination directory.");
            }
            _vars.setDestinationDir(_dest.getText());
        }
        else if (source == _daysOfMonth)
        {
            _vars.setMonthDay(_daysOfMonth.getText());
        }
        else if (source == _fDate)
        {
            if (!_fDate.getText().trim().equals("")) {

                // Notify the user that he set the time in milliseconds
                long testVar = -1;

                try {
                    testVar = Integer.valueOf(_fDate.getText().trim()).intValue();
                } catch (Exception exptn) {
                    // Date is set in calendar format, or not set correctly
                    LogManager.write("Configurator         - File Size not set as time in milliseconds.", LogHandle.ALL,LogManager.DEBUG);
                }

                //if (testVar >= 0 && testVar < 949363200765) {
                if (testVar >= 0) {
                    DMGOptionPane.showMessageDialog(frame, "Oddly old date specified: ["+_vars.dateMillToString(testVar)+"]." +
                            "\n", " Datamirror Utility.", DMGOptionPane.WARNING_MESSAGE);
                }

                // Date is set in calendar format, or not set correctly
                if (_vars.dateStringToMill(_fDate.getText()) > 0) {
                    _vars.setDateSet(_fDate.getText());
                } else {
                    DMGOptionPane.showMessageDialog(frame, "Illegal date format: ["+_fDate.getText()+"]. Valid formats are:\n\n" +
                            "\t[DD:MM:YYYY HH:MM:SS] or [DD/MM/YYYY HH:MM:SS] or\n" +
                            "\t[DD/MM/YYYY HH/MM/SS] or [DD/MM/YYYY HH/MM] or\n" +
                            "\t[DD/MM/YYYY HH] or [DD/MM/YYYY].\n\n", " Datamirror Utility.", DMGOptionPane.WARNING_MESSAGE);
                    _fDate.setText("");
                }
            } else {
                _fDate.setText("");
            }
            _vars.setDateSet(_fDate.getText());
        }
        else if (source == _fSize)
        {
            if (!_fSize.getText().trim().equals("")) {

                int testVar = -1;

                try {
                    testVar = Integer.valueOf(_fSize.getText().trim()).intValue();
                } catch (Exception exptn) {
                    LogManager.write("Configurator         - Illegal value defined for File Size.", LogHandle.ALL,LogManager.DEBUG);
                }

                if (testVar < 0) {
                    DMGOptionPane.showMessageDialog(frame, "Illegal file size.\n\n", " Datamirror Utility.", DMGOptionPane.WARNING_MESSAGE);
                    _fSize.setText("");
                }
            }
            _vars.setFileSize(_fSize.getText().trim());
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
            System.out.println("*  Datamirror generator GUI -  v1.0   *");
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
            System.out.println("*  Datamirror generator GUI -  v1.0   *");
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
                System.out.println("Configurator         - Home Directory not set. Exit.\n\n");
                System.exit(-1);
            }
            File home = new File(_dmghome);
            if (!home.exists())
            {
                System.out.println("Configurator         - Home Directory ["+_dmghome+"] does not exist. Exit.\n\n");
                System.exit(-1);
            }

            FileInputStream fStream = new FileInputStream(_dmghome + "props/dmgutil.prp");
            props.load(fStream);
            fStream.close();
        }
        catch(IOException e)
        {
            System.out.println("Configurator         - Can't find the property file \"" +_dmghome+ "props/dmgutil.prp\": Exit.\n\n");
            System.exit(-1);
        }
//        File startup = new File(home, "Generator.exe");
//        if (!startup.exists())
//        {
//            System.out.println("SMS   Manager - Home Directory ["+dmghome+"] is wrong. Exit.");
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

        //new Configurator(_vars, _dataMirror, _scheduleModule, _dmghome);
        new Configurator();
    }
}