import java.util.concurrent.ScheduledExecutorService;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import javax.swing.border.LineBorder;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;
import java.security.MessageDigest;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.ArrayList;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.Scanner;
import java.awt.Desktop;
import java.awt.Cursor;
import java.util.Date;
import java.awt.Color;
import javax.swing.*;
import java.awt.Font;
import java.io.File;
import java.text.*;
import java.awt.*;
import java.io.*;

public class Punch implements ActionListener {
    //LinkedList objects used in the program
    private LinkedList<Users> Users = new LinkedList<>();
    private LinkedList<QuickTotals> QuickTotals = new LinkedList<>();
    private LinkedList<InOut> InOut = new LinkedList<>();
    private LinkedList<String> NegativeUsers = new LinkedList<>();
    private LinkedList<String> ForcedOutUsers = new LinkedList<>();
    private LinkedList<AD> ADS = new LinkedList<>();

    //Decimal format used to record time
    private DecimalFormat TwoDecPlace = new DecimalFormat("#.##");
    private DecimalFormat FourDecPlace = new DecimalFormat("#.####");

    //JFrame objects
    private JFrame PunchFrame;

    //colors
    private Color Navy = new Color(36,48,88);
    private Color gray = new Color(40,40,40);
    private Color c = new Color(40,40,40);

    //Username source
    private JTextField NameField;

    //Password source
    private JPasswordField PasswordField;

    //readers and writers
    private BufferedReader employeeReader;
    private BufferedWriter employeeWriter;

    //Main four Buttons
    private JButton PunchIn;
    private JButton PunchOut;
    private JButton AdminPanel;
    private JButton GetReport;

    //Informs a user of their punch binary
    private String AlreadyPunchedIn;

    //Test if user and password combo exists
    private String MatchName;
    private String MatchPassword;

    //Location read from file (what dir to store under)
    private String Location;

    //Used for switching out files when a new pay period is requested
    private String Rename;

    //used for JOptionPane messages
    private ImageIcon scaledDown = new ImageIcon(new ImageIcon("Logo.png").getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT));

    public static void main(String[] args) {
        //UI manager for tooltips
        UIManager.put("ToolTip.background", new Color(39,40,34));
        UIManager.put("ToolTip.border", Color.black);
        UIManager.put("ToolTip.font", new Font("Tahoma",Font.BOLD,15));
        UIManager.put("ToolTip.foreground", new Color(85,85,255));
        UIManager.put("ToolTip.border", new LineBorder(Color.BLACK,1));

        //Object to avoid static modifiers
        new Punch();
    }

    private Punch() {
        //Admin actions such as reset dates, name of program, auto reset dates, and close at times
        ActOnAdminActions();


        //If a folder to store data DNE, make it
        if (!new File(Location).exists()) {
            new File(Location).mkdirs();

            read();

            try {
                for (Users user : Users) {
                    write(user.getChangeName(), "false");
                    new File(Location + "\\" + user.getChangeName()).mkdirs();
                    new File(Location + "\\" + user.getChangeName() + "\\total.txt").createNewFile();
                    new File(Location + "\\" + user.getChangeName() + "\\all.txt").createNewFile();
                }
            }

            catch (Exception ex) {
                windowsFeel();

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);

                JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                    "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

                swingFeel();
            }
        }

        swingFeel();

        //Refresh users times
        RefreshTime();

        //GUI setsup until end of constructor
        PunchFrame = new JFrame();

        PunchFrame.setTitle(Location + " Punch Time Logger");

        PunchFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ImageIcon img = new ImageIcon("Logo.png");

        PunchFrame.setIconImage(img.getImage());

        JPanel ParentPanel = (JPanel) PunchFrame.getContentPane();

        ParentPanel.setLayout(new BoxLayout(ParentPanel,BoxLayout.Y_AXIS));

        ParentPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        JPanel NamePanel = new JPanel();

        JLabel EnterName = new JLabel("Employee Name:");

        NamePanel.add(EnterName);

        NameField = new JTextField(40);

        NameField.setToolTipText("Username");

        Font TextBoxFont = NameField.getFont().deriveFont(Font.BOLD, 15f);

        EnterName.setFont(TextBoxFont);

        NameField.setFont(TextBoxFont);

        NamePanel.add(NameField);

        ParentPanel.add(NamePanel);

        JPanel PasswordPanel = new JPanel();

        JLabel Password = new JLabel("Employee Password:");

        Password.setFont(TextBoxFont);

        PasswordPanel.add(Password);

        PasswordField = new JPasswordField(40);

        PasswordField.setToolTipText("Password");

        PasswordPanel.add(PasswordField);

        ParentPanel.add(PasswordPanel);

        JPanel ButtonPanel = new JPanel();

        PunchIn = new JButton("Punch In");

        PunchOut = new JButton("Punch Out");

        AdminPanel = new JButton("Admin Panel");

        GetReport = new JButton("Time Card Report");

        PunchIn.addActionListener(this);

        PunchOut.addActionListener(this);

        AdminPanel.addActionListener(this);

        GetReport.addActionListener(this);

        PunchIn.setFont(TextBoxFont);

        PunchOut.setFont(TextBoxFont);

        AdminPanel.setFont(TextBoxFont);

        GetReport.setFont(TextBoxFont);

        PunchIn.setBackground(new Color(50,87,188));

        PunchOut.setBackground(new Color(223,85,83));

        AdminPanel.setBackground(new Color(231,87,122));

        GetReport.setBackground(new Color(138,118,231));

        PunchIn.setToolTipText("Punch in");

        PunchOut.setToolTipText("Punch out");

        AdminPanel.setToolTipText("Admin features");

        GetReport.setToolTipText("Employee time log");

        PunchIn.setFocusPainted(false);

        PunchIn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        PunchOut.setFocusPainted(false);

        PunchOut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        AdminPanel.setFocusPainted(false);

        AdminPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        GetReport.setFocusPainted(false);

        GetReport.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        ButtonPanel.add(PunchIn);

        ButtonPanel.add(PunchOut);

        ButtonPanel.add(AdminPanel);

        ButtonPanel.add(GetReport);

        ParentPanel.add(ButtonPanel);

        PunchFrame.pack();

        PunchFrame.setVisible(true);

        PunchFrame.setLocationRelativeTo(null);

        PunchFrame.setResizable(false);

        PunchFrame.setAlwaysOnTop(true);

        PunchFrame.setAlwaysOnTop(false);

        if (Users.size() == 0) {
            PunchIn.setEnabled(false);
            PunchOut.setEnabled(false);
            GetReport.setEnabled(false);

            windowsFeel();

            JOptionPane.showMessageDialog(null, "Please note that there are no employees.\n" +
                    "You will need to add at least one employee before you can use the program.","No employees were found", JOptionPane.INFORMATION_MESSAGE,scaledDown );

            swingFeel();
        }

        AdminCheck();
    }

    //Action handler
    @Override
    public void actionPerformed(ActionEvent event) {
        //Strings for data from the input fields
        String CurrentUser;
        String Password;

        //get valid users
        read();

        //control source
        Object control = event.getSource();

        //this is where the current user and password are obtained
        CurrentUser = NameField.getText().trim();

        char[] PasswordPrime = PasswordField.getPassword();

        Password = new String(PasswordPrime);

        try {
            Password = toHexString(getSHA(CurrentUser +
                    "b91332ab9b07376173e11198629b8fb11081f8bb2a1fb555af4d8f44e75c9cac"
                    + Password));

        }

        catch (Exception ex) {
            windowsFeel();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                    "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

            swingFeel();
        }

        try {
            //read valid users from the employee list
            employeeReader = new  BufferedReader(new FileReader ("EmployeeList.txt"));

            String Line;

            Line = employeeReader.readLine();

            String[] MatchParts;

            while(Line != null) {
                MatchParts = Line.split(",");

                if (MatchParts.length == 3) {
                    MatchName = MatchParts[0];

                    MatchPassword = MatchParts[1];

                    AlreadyPunchedIn = MatchParts[2];

                    if (MatchName.equalsIgnoreCase(CurrentUser))
                    {
                        break;
                    }

                    else
                    {
                        Line = employeeReader.readLine();
                    }
                }
            }

            employeeReader.close();
        }

        catch (Exception ex) {
            windowsFeel();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                    "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

            swingFeel();
        }

        //default output
        if ((CurrentUser.equals("") || Password.equals("")) && control != AdminPanel) {
            windowsFeel();

            JOptionPane.showMessageDialog(null,"Failed Punch Action: you must enter a valid username and password.",
                    "Failed Punch Action", JOptionPane.INFORMATION_MESSAGE,scaledDown);

            swingFeel();

            PasswordField.setText("");
            NameField.setText("");
        }

        //Admin has forced a new period
        else if (control == AdminPanel) {
            if (AdminCheck(CurrentUser, Password)) {
                try {
                    AdminActions();
                }

                catch (Exception ex) {
                    windowsFeel();

                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    ex.printStackTrace(pw);

                    JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                            "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

                    swingFeel();
                }

            } else {
                windowsFeel();

                JOptionPane.showMessageDialog(null, "Failed Punch Action: this option is only available to system administrators.",
                        "Failed Punch Action", JOptionPane.INFORMATION_MESSAGE, scaledDown);

                swingFeel();

                PasswordField.setText("");
                NameField.setText("");
            }
        }

        //punch in or punch out
        else {
            //punch in
            if (control == PunchIn) {
                boolean NoCurrentUser = true;

                //Check for wrong time correction on users part
                for (String negativeUser : NegativeUsers) {
                    if (CurrentUser.equalsIgnoreCase(negativeUser)) {
                        NoCurrentUser = false;
                        windowsFeel();

                        JOptionPane.showMessageDialog(null, "Sorry " + negativeUser + " but one of your punch-out times"
                                + " is before its corresponding\npunch-in time. Please resolve this issue in order to punch in.", "", JOptionPane.ERROR_MESSAGE, scaledDown);

                        swingFeel();

                        break;
                    }
                }

                //Matching credentials so punchin
                if (CurrentUser.equalsIgnoreCase(MatchName) && Password.equals(MatchPassword) && AlreadyPunchedIn.equalsIgnoreCase("false") && NoCurrentUser) {
                    PunchIn(CurrentUser);
                }

                //Username invalid
                else if (!CurrentUser.equalsIgnoreCase(MatchName)) {
                    windowsFeel();
                    JOptionPane.showMessageDialog(null, "Unknown Username or Incorrect Password.","", JOptionPane.ERROR_MESSAGE,scaledDown );
                    swingFeel();
                }

                //Username valid yet incorrect password (we not not tell user that for security reasons)
                else if (CurrentUser.equalsIgnoreCase(MatchName) && !Password.equals(MatchPassword)) {
                    windowsFeel();
                    JOptionPane.showMessageDialog(null, "Unknown Username or Incorrect Password.","", JOptionPane.ERROR_MESSAGE,scaledDown );
                    swingFeel();
                }

                //User is already punched in, what an idiot
                else if (CurrentUser.equalsIgnoreCase(MatchName) && Password.equals(MatchPassword) && AlreadyPunchedIn.equalsIgnoreCase("true")) {
                    windowsFeel();
                    JOptionPane.showMessageDialog(null, "You are already punched in.","", JOptionPane.INFORMATION_MESSAGE,scaledDown );
                    swingFeel();
                }

                //reset credential fields
                PasswordField.setText("");
                NameField.setText("");
            }

            //punch out
            else if (control == PunchOut) {
                if (CurrentUser.equalsIgnoreCase(MatchName) && Password.equals(MatchPassword) && AlreadyPunchedIn.equalsIgnoreCase("true")) {
                    PunchOut(CurrentUser);
                }

                else if (!CurrentUser.equalsIgnoreCase(MatchName)) {
                    windowsFeel();
                    JOptionPane.showMessageDialog(null, "Unknown Username or Incorrect Password.","", JOptionPane.ERROR_MESSAGE,scaledDown );
                    swingFeel();
                }

                else if (CurrentUser.equalsIgnoreCase(MatchName) && !Password.equals(MatchPassword)) {
                    windowsFeel();
                    JOptionPane.showMessageDialog(null, "Unknown Username or Incorrect Password.","", JOptionPane.ERROR_MESSAGE,scaledDown );
                    swingFeel();
                }

                else if (CurrentUser.equalsIgnoreCase(MatchName) && Password.equals(MatchPassword) && AlreadyPunchedIn.equalsIgnoreCase("false")) {
                    windowsFeel();
                    JOptionPane.showMessageDialog(null, "You are already punched out.","", JOptionPane.INFORMATION_MESSAGE,scaledDown );
                    swingFeel();
                }

                PasswordField.setText("");
                NameField.setText("");
            }

            else if (control == GetReport) {
                if (CurrentUser.equalsIgnoreCase(MatchName) && Password.equals(MatchPassword)) {
                    TimeReport();

                    PunchFrame.dispose();

                    System.exit(0);
                }

                else if (!CurrentUser.equalsIgnoreCase(MatchName)) {
                    windowsFeel();
                    JOptionPane.showMessageDialog(null, "Unknown Username or Incorrect Password.","", JOptionPane.ERROR_MESSAGE,scaledDown );
                    swingFeel();
                }

                else if (CurrentUser.equalsIgnoreCase(MatchName) && !Password.equals(MatchPassword)) {
                    windowsFeel();
                    JOptionPane.showMessageDialog(null, "Unknown Username or Incorrect Password.","", JOptionPane.ERROR_MESSAGE,scaledDown );
                    swingFeel();
                }

                PasswordField.setText("");
                NameField.setText("");
            }
        }
    }

    //Punch a user in and writes the time to the users log file
    private void PunchIn(String UserName) {
        try {
            File TLocation = new File(Location + "\\" + MatchName);

            TLocation.mkdirs();


            File TotalFile = new File(Location + "\\" + MatchName + "\\total.txt");

            TotalFile.createNewFile();


            File AllFile = new File(Location + "\\" + MatchName + "\\all.txt");

            AllFile.createNewFile();
        }

        catch (Exception ex) {
            windowsFeel();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                    "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

            swingFeel();
        }

        windowsFeel();

        JOptionPane.showMessageDialog(null, MatchName + " has been punched in on \n" + PleasantTime(),
                "Successfull Punch In", JOptionPane.INFORMATION_MESSAGE,scaledDown );

        swingFeel();

        read();

        write(UserName,"true");

        String LocationWrite = Location + "\\" + MatchName + "\\all.txt";

        try {
            BufferedWriter TimeWriter = new BufferedWriter(new FileWriter(LocationWrite,true));

            TimeWriter.write("In:\r\n" + NiceTime());

            TimeWriter.flush();

            TimeWriter.close();
        }

        catch (Exception ex) {
            windowsFeel();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                    "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

            swingFeel();
        }

    }

    //Punches a user out and writes the punchout time to their log file
    private void PunchOut(String UserName) {
        windowsFeel();

        JOptionPane.showMessageDialog(null, MatchName + " has been punched out on \n" + PleasantTime(),
                "Successfull Punch Out", JOptionPane.INFORMATION_MESSAGE,scaledDown);

        swingFeel();

        read();

        write(UserName,"false");

        try {
            String StartTime = GetInTime(MatchName);

            WriteTotalTime(StartTime);

            String LocationWrite = Location + "\\" + MatchName + "\\all.txt";

            BufferedWriter TimeWriter = new BufferedWriter(new FileWriter(LocationWrite,true));

            TimeWriter.write("\r\n" + "\r\n" + "Out:\r\n" +  NiceTime() + "\r\n" + "\r\n" + "------------------------" + "\r\n" + "\r\n");

            TimeWriter.flush();

            TimeWriter.close();
        }

        catch (Exception ex) {
            windowsFeel();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                    "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

            swingFeel();
        }
    }

    //Opens up the time report for a specific user
    private void TimeReport() {
        File WhereItIs = new File(Location + "\\" + MatchName + "\\all.txt");

        try {
            Desktop.getDesktop().open(WhereItIs);
        }

        catch (Exception ex) {
            windowsFeel();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                    "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

            swingFeel();
        }
    }

    //Reads all users into the LinkedList Users
    private void read() {
        try {
            Users.clear();

            employeeReader = new BufferedReader(new FileReader("EmployeeList.txt"));

            String Line;

            Line = employeeReader.readLine();

            String[] currentUser;

            while(Line != null) {
                currentUser = Line.split(",");

                if (currentUser.length >= 3) {
                    Users.add(new Users(currentUser[0],currentUser[1],currentUser[2]));
                }

                Line = employeeReader.readLine();
            }
            employeeReader.close();
        }

        catch (Exception ex) {
            windowsFeel();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                    "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

            swingFeel();
        }
    }

    //Changes a users punch bin
    private void write(String UserName, String inout) {
        read();

        try {
            employeeWriter = new BufferedWriter(new FileWriter("EmployeeList.txt"));

            for (Users u: Users) {
                if (u.getChangeName().equalsIgnoreCase(UserName)) {
                    u.setChangeBinary(inout);
                }

                employeeWriter.write(u.getChangeName() + "," + u.getChangePass() + "," + u.getChangeBinary());

                employeeWriter.write("\r\n");

                employeeWriter.flush();
            }

            employeeWriter.close();
        }

        catch (Exception ex) {
            windowsFeel();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                    "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

            swingFeel();
        }
    }

    //Returns a date as a string
    private String NiceTime() {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");

        Date NiceDate = new Date();

        return dateFormat.format(NiceDate);
    }

    //Returns a date as a string
    private String PleasantTime() {
        DateFormat dateFormat = new SimpleDateFormat("EEEEEEE MM/dd/yyyy hh:mm aaa");

        Date PleasantDate = new Date();

        return dateFormat.format(PleasantDate);
    }

    //Used when punching out
    private void WriteTotalTime(String dateStart) {
        try {
            String LocationRead = Location + "\\" + MatchName + "\\total.txt";

            String dateStop = NiceTime();

            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm");

            Date DateStart;

            Date DateStop;

            DateStart = format.parse(dateStart);

            DateStop = format.parse(dateStop);

            long diff = DateStop.getTime() - DateStart.getTime();

            int Minutes = (int) TimeUnit.MILLISECONDS.toMinutes(diff);

            int OldMinutes;

            int TotalMinutes;

            BufferedReader NullCheck = new BufferedReader(new FileReader(LocationRead));

            if (NullCheck.readLine() == null) {
                OldMinutes = 0;
            }

            else {
                File OldTimeFile = new File(LocationRead);

                Scanner Time = new Scanner(OldTimeFile);

                OldMinutes = Time.nextInt();

                Time.close();
            }

            TotalMinutes = OldMinutes + Minutes;

            NullCheck.close();

            BufferedWriter OutputTime = new BufferedWriter(new FileWriter(LocationRead));

            OutputTime.write(Integer.toString(TotalMinutes));

            OutputTime.flush();

            OutputTime.close();
        }

        catch (Exception ex) {
            windowsFeel();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                    "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

            swingFeel();
        }
    }

    //Gets the most recent punched in time for the specified user
    private String GetInTime(String Username) {
        try {
            FileInputStream in = new FileInputStream(Location + "\\" + Username + "\\all.txt");

            BufferedReader LastReader = new BufferedReader(new InputStreamReader(in));

            String strLine = null, tmp;

            while ((tmp = LastReader.readLine()) != null) {
                strLine = tmp;
            }

            assert strLine != null;
            String lastLine = strLine.replace("!","");

            in.close();

            LastReader.close();

            return lastLine;
        }

        catch (Exception ex) {
            windowsFeel();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                    "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

            swingFeel();
        }

        return null;
    }

    //Checks to see if it is time to automatically force a new period
    private void CheckPeriod(String EonStart)  {
        try {
            DateFormat CheckMondayFormat = new SimpleDateFormat("u");

            DateFormat StartingFormat = new SimpleDateFormat("MM/dd/yy");

            DateFormat RenameFormat = new SimpleDateFormat("MM-dd-yyyy");

            Date CurrentDate = new Date();

            CheckMondayFormat.format(CurrentDate);

            Date EonStartDate = StartingFormat.parse(EonStart);

            int PastDays = (int)((CurrentDate.getTime() - EonStartDate.getTime()) / (1000 * 60 * 60 * 24));

            Rename = Location + " " + RenameFormat.format(CurrentDate);

            File RenameFile = new File(Rename);

            if ((PastDays % 14 == 0) && !Files.exists(RenameFile.toPath()) && NotInLog()) {
                RefreshTime();

                NewPeriod();
            }
        }

        catch (Exception ex) {
            windowsFeel();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                    "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

            swingFeel();
        }
    }

    //Method to revalidate all users punch in and punch out times and
    //add up the logged time in case the time was changed/altered within the file itself
    //and not from the program, which is the only way to do it
    @SuppressWarnings("UnusedAssignment")
    private void RefreshTime() {
        try {
            //init users list
            read();

            LinkedList<String> ChangedUsers = new LinkedList<>();

            //for all the users
            for (Users user : Users) {
                //date format times are stored in
                DateFormat AddingFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");

                //current users all file string
                String CurrentDir = Location + "\\" + user.getChangeName() + "\\" + "all.txt";

                //current users all file
                File CurrentAll = new File(CurrentDir);

                //if it even exists
                if (CurrentAll.exists()) {
                    //array to store the users punch in and out lines
                    ArrayList<String> LinesArray = new ArrayList<>();

                    //reader to read lines
                    employeeReader = new BufferedReader(new FileReader(CurrentDir));

                    //for the number of lines in the current all file
                    for (int j = 0; j < CurrentAll.length(); j++) {
                        //get line
                        String Line = employeeReader.readLine();

                        // if its not null, not a separator, and not an identifier, add it to the array
                        if (Line != null && Line.trim().length() != 0 && !Line.equals("------------------------")
                                && !Line.equals("In:") && !Line.equals("Out:")) {
                            LinesArray.add(Line);
                        }
                    }

                    //Uneven number of lines so a user is still punched in or some weird error might have occured so lets find out
                    if (LinesArray.size() % 2 != 0) {
                        //get the odd line
                        String OddOut = LinesArray.get(LinesArray.size() - 1);

                        //if the odd line is less than 16 chars (our date format char length)
                        if (OddOut.length() > 16) {
                            //its an error because its too short so it needs to be corrected before we can continue program execution
                            int dialogueButton = JOptionPane.YES_NO_OPTION;

                            windowsFeel();

                            int DialogResult = JOptionPane.showConfirmDialog(null, "An unfortunate error occured while attempting to refresh the time.\n"
                                    + "This is the result of a typo in " + user.getChangeName() + GetApostrophe(user.getChangeName()) + " \"all.txt\""
                                    + " file.\nWould you like to open the file"
                                    + " to make adjustments?", "", dialogueButton, JOptionPane.ERROR_MESSAGE, scaledDown);

                            swingFeel();

                            if (DialogResult == JOptionPane.YES_OPTION) {
                                File WhereItIs = new File(Location + "\\" + user.getChangeName() + "\\all.txt");

                                Desktop.getDesktop().open(WhereItIs);
                            }

                            System.exit(0);
                        }

                        //now that we know it is longer than or equal to 16
                        Date OddOutDate = new Date();

                        try {
                            OddOutDate = AddingFormat.parse(OddOut);
                        }

                        //error parsing the "date" so this must be corrected before we can continue program execution
                        catch (Exception exc) {
                            int dialogueButton = JOptionPane.YES_NO_OPTION;

                            windowsFeel();

                            int DialogResult = JOptionPane.showConfirmDialog(null, "An unfortunate error occured while attempting to refresh the time.\n"
                                    + "This is the result of a typo in " + user.getChangeName() + GetApostrophe(user.getChangeName()) + " \"all.txt\" "
                                    + "file.\nWould you like to open the file"
                                    + " to make adjustments?", "", dialogueButton, JOptionPane.ERROR_MESSAGE, scaledDown);

                            swingFeel();

                            if (DialogResult == JOptionPane.YES_OPTION) {
                                File WhereItIs = new File(Location + "\\" + user.getChangeName() + "\\all.txt");

                                Desktop.getDesktop().open(WhereItIs);
                            }

                            System.exit(0);
                        }

                        //If its not an error then they are still punched in so we will disregard this value when refreshing our time
                        LinesArray.remove(LinesArray.size() - 1);
                    }

                    //for all of our in and out lines for this user
                    for (int k = 0; k < LinesArray.size() / 2; k++) {
                        //put our lines into a custom object
                        InOut.add(new InOut(LinesArray.get(2 * k), LinesArray.get((2 * k) + 1)));
                    }

                    //init minutes total
                    int CurrentUserTotalMinutes = 0;

                    boolean UserAdded = false;

                    //for all the inout objects
                    for (InOut inOut : InOut) {
                        //get the in and out vals
                        String In = inOut.getIn();

                        String Out = inOut.getOut();

                        //make sure they are valid times otherwise its an error that needs to be corrected
                        if (In.length() > 16 || Out.length() > 16) {
                            int dialogueButton = JOptionPane.YES_NO_OPTION;

                            windowsFeel();

                            int DialogResult = JOptionPane.showConfirmDialog(null, "An unfortunate error occured while attempting to refresh the time.\n"
                                    + "This is the result of a typo in " + user.getChangeName() + GetApostrophe(user.getChangeName()) + " \"all.txt\" file.\nWould you like to open the file"
                                    + " to make adjustments?", "", dialogueButton, JOptionPane.ERROR_MESSAGE, scaledDown);

                            swingFeel();

                            if (DialogResult == JOptionPane.YES_OPTION) {
                                File WhereItIs = new File(Location + "\\" + user.getChangeName() + "\\all.txt");

                                Desktop.getDesktop().open(WhereItIs);
                            }

                            System.exit(0);
                        }

                        //Try to parse the dates, if it fails then an error needs to be corrected
                        Date InDate = new Date();

                        Date OutDate = new Date();

                        try {
                            InDate = AddingFormat.parse(In);

                            OutDate = AddingFormat.parse(Out);
                        } catch (Exception e) {
                            int dialogueButton = JOptionPane.YES_NO_OPTION;

                            windowsFeel();

                            int DialogResult = JOptionPane.showConfirmDialog(null, "An unfortunate error occured while attempting to refresh the time.\n"
                                    + "This is the result of a typo in " + user.getChangeName() + GetApostrophe(user.getChangeName()) + " \"all.txt\" file.\nWould you like to open the file"
                                    + " to make adjustments?", "", dialogueButton, JOptionPane.ERROR_MESSAGE, scaledDown);

                            swingFeel();

                            if (DialogResult == JOptionPane.YES_OPTION) {
                                File WhereItIs = new File(Location + "\\" + user.getChangeName() + "\\all.txt");

                                Desktop.getDesktop().open(WhereItIs);
                            }

                            System.exit(0);
                        }

                        //difference between in and out times
                        long diff = OutDate.getTime() - InDate.getTime();

                        //if the time is negative then we need to mark it in our negative users list
                        if (diff < 0 && !UserAdded) {
                            NegativeUsers.add(user.getChangeName());

                            UserAdded = true;
                        }

                        //add/sub this shift to our total time from total.txt

                        //took from here


                        CurrentUserTotalMinutes += TimeUnit.MILLISECONDS.toMinutes(diff);
                    }

                    double OldTotalMinutes = 0;

                    String LocationRead = Location + "\\" + user.getChangeName() + "\\total.txt";

                    if (new File(LocationRead).exists()) {
                        BufferedReader NullCheck = new BufferedReader(new FileReader(LocationRead));

                        if (NullCheck.readLine() == null) {
                            OldTotalMinutes = 0;
                        } else {
                            File TotalTime = new File(LocationRead);

                            Scanner Time = new Scanner(TotalTime);

                            OldTotalMinutes = Time.nextInt();

                            Time.close();
                        }

                        NullCheck.close();
                    }

                    double PrintTime = (double) CurrentUserTotalMinutes - OldTotalMinutes;

                    if (PrintTime != 0) {
                        ChangedUsers.add("Employee " + user.getChangeName() + " added " + FourDecPlace.format(PrintTime / 60.0) + " hours to their total.");
                    }

                    //now we have this users total time worked for this period so far so we will write it to the totals file

                    BufferedWriter OutputTime = new BufferedWriter(new FileWriter(Location + "\\" + user.getChangeName() + "\\" + "total.txt"));

                    OutputTime.write(Integer.toString(CurrentUserTotalMinutes));

                    InOut.clear();

                    OutputTime.flush();

                    OutputTime.close();

                    employeeReader.close();
                }
            }

            //now after all the users totals have been updated

            //for the negative users, we will inform the admin or user that this needs to be corrected before the specific users can punch in again
            String NegativeUserString = NegativeUsers.toString();

            //one negative user
            if (!NegativeUserString.equalsIgnoreCase("[]") && NegativeUsers.size() == 1) {
                Object[] options = {"OK"};

                windowsFeel();

                int choice = JOptionPane.showOptionDialog(null, "One of the punch-out times for the user " + NegativeUserString + " is before"
                        + " the corresponding\npunch-in time. This user will not be able to punch in until the issue"
                        + " is resolved.", "", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE,scaledDown, options, options[0]);

                swingFeel();
            }

            //more than one negative user
            else if (!NegativeUserString.equalsIgnoreCase("[]") && NegativeUsers.size() > 1) {
                Object[] options = {"OK"};

                windowsFeel();

                int choice = JOptionPane.showOptionDialog(null, "One of the punch-out times for the users " + NegativeUserString + " is before"
                        + " the corresponding\npunch-in time. These users will not be able to punch in until the issue"
                        + " is resolved.", "", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE,scaledDown, options, options[0]);

                swingFeel();
            }

            if (ChangedUsers.size() != 0) {
                LinkedList<AD> PoppedADS = new LinkedList<>();

                AD currentAD = ADS.removeLast();

                while (!currentAD.getName().equals("recent time changes header")) {
                    PoppedADS.push(currentAD);

                    currentAD = ADS.removeLast();
                }

                ADS.add(new AD("recent time changes header","Recent time changes:"));

                for (String currentChanged: ChangedUsers) {
                    ADS.add(new AD("time change",currentChanged));
                }

                while (PoppedADS.size() != 0) {
                    ADS.add(PoppedADS.pop());
                }

                if (!ADS.peekLast().getValue().equals("------------------------------------------------------")) {
                    ADS.add(new AD("sep","------------------------------------------------------"));
                }

                writeAD();

                readAD();
            }
        }

        catch (Exception ex) {
            windowsFeel();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                    "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

            swingFeel();
        }
    }

    //this method outputs an easy to read file conveying total time for each employee
    private void QuickTotal() {
        try {
            //init users
            read();

            File NoError = new File(Location);

            NoError.mkdirs();

            File LocationMake = new File(Location + "\\QuickTotal.txt");

            LocationMake.createNewFile();

            QuickTotals.clear();

            //make file to output to and setup objects

            //for all users get the total minutes if it exists and convert to hours
            for (Users user : Users) {
                double ThisTotal;

                String LocationRead = Location + "\\" + user.getChangeName() + "\\total.txt";

                if (new File(LocationRead).exists()) {
                    BufferedReader NullCheck = new BufferedReader(new FileReader(LocationRead));

                    if (NullCheck.readLine() == null) {
                        ThisTotal = 0;
                    } else {
                        File TotalTime = new File(LocationRead);

                        Scanner Time = new Scanner(TotalTime);

                        ThisTotal = Time.nextInt();

                        Time.close();
                    }

                    NullCheck.close();

                    QuickTotals.add(new QuickTotals(user.getChangeName(), ThisTotal / 60));
                }
            }

            //write the employee names and hours worked in the folder
            employeeWriter = new BufferedWriter(new FileWriter(Location + "\\QuickTotal.txt"));

            employeeWriter.write("[Name],[Hours Worked]");

            employeeWriter.write("\r\n");

            employeeWriter.flush();

            for (QuickTotals u: QuickTotals) {
                if (TwoDecPlace.format(u.getHours()).equalsIgnoreCase(".0")) {
                    employeeWriter.write(u.getName() + ",0.0");
                }

                else {
                    employeeWriter.write(u.getName() + "," + TwoDecPlace.format(u.getHours()));
                }

                employeeWriter.write("\r\n");

                employeeWriter.flush();
            }
            employeeWriter.close();
        }

        catch (Exception ex) {
            windowsFeel();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                    "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

            swingFeel();
        }
    }

    //Setup for a new pay period
    private void NewPeriod() {
        try {
            read();

            for (int i = 0 ; i < Users.size() ; i++) {
                if (Users.get(i).getChangeBinary().equalsIgnoreCase("true")) {
                    String Name = Users.get(i).getChangeName();

                    String LocationWrite = Location + "\\" + Name + "\\all.txt";

                    ForcedOutUsers.add(Users.get(i).getChangeName());

                    BufferedWriter TimeWriter = new BufferedWriter(new FileWriter(LocationWrite,true));

                    TimeWriter.write("\r\n" + "\r\n" + "Out:\r\n" +  "Missing Punch" + "\r\n" + "\r\n" + "------------------------" + "\r\n" + "\r\n");

                    TimeWriter.flush();

                    TimeWriter.close();

                    employeeWriter = new BufferedWriter(new FileWriter("EmployeeList.txt"));

                    for (Users u: Users) {
                        if (u.getChangeName().equalsIgnoreCase(Name)) {
                            u.setChangeBinary("false");
                        }

                        employeeWriter.write(u.getChangeName() + "," + u.getChangePass() + "," + u.getChangeBinary());

                        employeeWriter.write("\r\n");

                        employeeWriter.flush();
                    }
                    employeeWriter.close();
                }
            }

            //Sum up the hours employees worked and output it
            QuickTotal();

            //Createa new folder
            RenameFolder();

            //Log the reset
            AddDateToLog();

            //remove time changes from AD.txt
            readAD();

            LinkedList<AD> TempADS = new LinkedList<>();

            AD firstAD = ADS.removeFirst();

            while (ADS.size() != 0) {
                if (!firstAD.getName().equals("time change")) {
                    TempADS.add(firstAD);
                }

                firstAD = ADS.removeFirst();
            }

            ADS = TempADS;

            writeAD();
        }

        catch (Exception ex) {
            windowsFeel();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                    "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

            swingFeel();
        }
    }

    //Used to create a new folder after a new period has been called
    private void RenameFolder() {
        DateFormat RenameFormat = new SimpleDateFormat("MM-dd-yyyy");

        Date CurrentDate = new Date();

        File OldNameFile = new File(Location);

        String NewNameString = Location + " " + RenameFormat.format(CurrentDate);

        File NewNameFile = new File(NewNameString);

        if (NewNameFile.exists()) {
            DeleteFolder(NewNameFile);
        }

        OldNameFile.renameTo(NewNameFile);
    }

    //Deletes a folder
    private void DeleteFolder(File folder) {
        File[] files = folder.listFiles();

        if (files != null) {
            for (File f: files) {
                if (f.isDirectory()) {
                    DeleteFolder(f);
                }

                else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    private void swingFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

            Frame bodgeFrame = new JFrame();

            SwingUtilities.updateComponentTreeUI(bodgeFrame);
        }

        catch (Exception ex) {
            windowsFeel();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                    "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

            swingFeel();
        }
    }

    private void windowsFeel() {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");

            JFrame bodgeFrame = new JFrame();

            SwingUtilities.updateComponentTreeUI(bodgeFrame);
        }

        catch (Exception ex) {
            windowsFeel();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                    "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

            swingFeel();
        }
    }

    //Dialog box that returns a bool value
    private boolean ConfirmNewPeriod() {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");

            JFrame bodgeFrame = new JFrame();

            SwingUtilities.updateComponentTreeUI(bodgeFrame);

            Object[] dialogueButton = {"Aces; do it!","No, nevermind"};

            ImageIcon scaledDown = new ImageIcon(new ImageIcon("Logo.png").getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT));

            windowsFeel();

            int dialogueResult = JOptionPane.showOptionDialog(null, "Are you sure you want to force a new pay period?", "",
                    JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE,scaledDown, dialogueButton,dialogueButton[1]);

            swingFeel();

            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

            SwingUtilities.updateComponentTreeUI(bodgeFrame);

            return (dialogueResult == JOptionPane.YES_OPTION);
        }

        catch (Exception ex) {
            windowsFeel();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                    "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

            swingFeel();
        }

        return false;
    }

    //Determines the gramatically correct apostrophe usage
    private String GetApostrophe(String Name) {
        if (Name.charAt(Name.length()-1) == 's') {
            return "'";
        }

        else {
            return "'s";
        }
    }

    //Used to tell if an automatic period should be called
    private boolean NotInLog() {
        SimpleDateFormat CF = new SimpleDateFormat("MM/dd/yy");

        Date CurrentDate = new Date();

        String CDString = CF.format(CurrentDate);

        for (AD ad: ADS) {
            if (ad.getName().equals("reset date") && ad.getValue().equals(CDString)) {
                return false;
            }
        }

        return true;
    }

    private void AddDateToLog() {
        try {
            AD Temp = ADS.removeLast();

            DateFormat DF = new SimpleDateFormat("MM/dd/yy");
            Date Today = new Date();
            AD NewResetDate = new AD("reset date", DF.format(Today));

            ADS.add(NewResetDate);
            ADS.add(Temp);

            writeAD();
        }

        catch (Exception ex) {
            windowsFeel();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                    "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

            swingFeel();
        }
    }

    private byte[] getSHA(String input) {
        try {
            // Static getInstance method is called with hashing SHA
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            return md.digest(input.getBytes(StandardCharsets.UTF_8));
        }

        catch (Exception ex) {
            windowsFeel();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                    "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

            swingFeel();
        }

        return null;
    }

    private String toHexString(byte[] hash) {
        // Convert byte array into signum representation
        BigInteger number = new BigInteger(1, hash);

        // Convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 32)
        {
            hexString.insert(0, '0');
        }
        return hexString.toString();
    }

    private boolean AdminCheck(String name, String pass) {
        try {
            BufferedReader AdminReader = new BufferedReader(new FileReader("AC.txt"));

            String Line;

            Line = AdminReader.readLine();

            while (Line != null) {
                if (!emptyStr(Line) && !Line.substring(0, 2).equals("//")) {
                    if (pass.equals(Line)) return true;
                }
                Line = AdminReader.readLine();
            }
            AdminReader.close();
        }

        catch (Exception ex) {
            windowsFeel();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                    "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

            swingFeel();
        }

        return false;
    }

    private void AdminCheck() {
        boolean noPassYet = true;

        read();

        try {
            LinkedList<String> LinesList = new LinkedList<>();

            BufferedReader AdminReader = new BufferedReader(new FileReader("AC.txt"));

            String Line;

            Line = AdminReader.readLine();

            while (Line != null) {
                //is a comment so ignore it
                if (emptyStr(Line)) {
                    LinesList.add("\\n");
                    Line = AdminReader.readLine();
                }

                else if (Line.substring(0,2).equals("//")) {
                    LinesList.add(Line);
                    Line = AdminReader.readLine();
                }

                //has not been initiated so we do that now and return true
                else if (Line.charAt(0) == '!' && Line.contains(":")) {
                    String[] credentials = Line.split(":");

                    String storeName = credentials[0].replace("!","");

                    String storePass = credentials[1];

                    //create hash with a secret inside ;)
                    String hash = toHexString(getSHA(storeName +
                            "b91332ab9b07376173e11198629b8fb11081f8bb2a1fb555af4d8f44e75c9cac" + storePass));

                    BufferedWriter AdminWriter = new BufferedWriter(new FileWriter("AC.txt",false));

                    for (String line: LinesList) {
                        if (line.equals("\\n")) {
                            AdminWriter.write("\n");
                        } else {
                            AdminWriter.write(line + "\n");
                        }
                    }

                    AdminWriter.write(hash);

                    AdminWriter.close();

                    AdminReader.close();

                    noPassYet = false;

                    employeeWriter = new BufferedWriter(new FileWriter("EmployeeList.txt"));

                    employeeWriter.write(storeName + "," + hash + ",false");

                    employeeWriter.write("\r\n");

                    employeeWriter.flush();

                    employeeWriter.close();

                    break;
                }
            }

            AdminReader.close();

            if (noPassYet) {
                windowsFeel();

                JOptionPane.showMessageDialog(null, "Please note that there is no registered admin.\n" +
                        "You will need to create one before you can access the admin panel\nto force a new pay period.\n" +
                        "Simply go to the AC.txt file and follow the directions.","No admin was found", JOptionPane.INFORMATION_MESSAGE,scaledDown );

                swingFeel();

                System.exit(0);
            }

            else {
                windowsFeel();

                JOptionPane.showMessageDialog(null, "Admin sucessfully registered.\nYou may now access the admin panel."
                        ,"Administrator Registered", JOptionPane.INFORMATION_MESSAGE,scaledDown );

                swingFeel();
            }
        }

        catch (Exception ex) {
            windowsFeel();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                    "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

            swingFeel();
        }
    }

    private boolean emptyStr(String empt) {
        return (empt == null || empt.equals("") || empt.trim().length() == 0);
    }

    private void AdminActions() {
        NameField.setText("");
        PasswordField.setText("");

        JFrame AdminMainFrame = new JFrame();

        AdminMainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        AdminMainFrame.setSize(350,650);

        AdminMainFrame.setTitle("Admin panel");

        AdminMainFrame.setResizable(false);

        ImageIcon img = new ImageIcon("Logo.png");

        AdminMainFrame.setIconImage(img.getImage());

        JPanel ParentPanel = new JPanel();

        ParentPanel.setLayout(new BoxLayout(ParentPanel, BoxLayout.Y_AXIS));

        JPanel TopPanel = new JPanel();

        JLabel CloseTimesLabel = new JLabel("Close times:");

        CloseTimesLabel.setFont(new Font("tahoma", Font.BOLD, 15));

        CloseTimesLabel.setForeground(gray);

        TopPanel.add(CloseTimesLabel);

        JTextField CloseTimes = new JTextField(20);

        CloseTimes.setText("");

        CloseTimes.setFont(new Font("Segoe UI Black", Font.PLAIN, 10));

        CloseTimes.setSelectionColor(new Color(204,153,0));

        CloseTimes.setCaretColor(gray);

        CloseTimes.setPreferredSize(new Dimension(300,25));

        CloseTimes.setBorder(new LineBorder(Navy,3,false));

        TopPanel.add(CloseTimes);

        ParentPanel.add(TopPanel);

        JPanel BelowTop1Panel = new JPanel();

        JLabel AutoNewPeriodLabel = new JLabel("Auto new period:");

        AutoNewPeriodLabel.setFont(new Font("tahoma", Font.BOLD, 15));

        AutoNewPeriodLabel.setForeground(gray);

        BelowTop1Panel.add(AutoNewPeriodLabel);

        JTextField AutoNewPeriod = new JTextField(20);

        AutoNewPeriod.setText("");

        AutoNewPeriod.setFont(new Font("Segoe UI Black", Font.PLAIN, 10));

        AutoNewPeriod.setSelectionColor(new Color(204,153,0));

        AutoNewPeriod.setCaretColor(gray);

        AutoNewPeriod.setBorder(new LineBorder(Navy,3,false));

        BelowTop1Panel.add(AutoNewPeriod);

        ParentPanel.add(BelowTop1Panel);

        JPanel BelowTop1Panel2 = new JPanel();

        JLabel ResetLogLabel = new JLabel("Recent time changes:");

        ResetLogLabel.setFont(new Font("tahoma", Font.BOLD, 15));

        ResetLogLabel.setForeground(gray);

        BelowTop1Panel2.add(ResetLogLabel);

        JTextArea LogArea = new JTextArea();

        LogArea.setBorder(BorderFactory.createLineBorder(Navy, 5));

        LogArea.setEditable(false);

        LogArea.setAutoscrolls(true);

        LogArea.setLineWrap(true);

        LogArea.setWrapStyleWord(true);

        LogArea.setFocusable(true);

        LogArea.setForeground(c.darker());

        LogArea.setSelectionColor(new Color(204,153,0));

        LogArea.setBackground(new Color(252,251,227));

        LogArea.setOpaque(true);

        LogArea.setFont(new Font("tahoma", Font.BOLD, 14));

        JScrollPane ChangeScroll = new JScrollPane(LogArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        ChangeScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

        ChangeScroll.setPreferredSize(new Dimension(300,180));

        ChangeScroll.getViewport().setBorder(null);

        ChangeScroll.getViewport().setOpaque(false);

        ChangeScroll.setOpaque(false);

        ChangeScroll.setBorder(BorderFactory.createEmptyBorder());

        BelowTop1Panel2.add(ChangeScroll);

        ParentPanel.add(BelowTop1Panel2);

        JPanel AboveBottom1 = new JPanel();

        JLabel NewUserLabel = new JLabel("New user:");

        NewUserLabel.setFont(new Font("tahoma", Font.BOLD, 15));

        NewUserLabel.setForeground(gray);

        AboveBottom1.add(NewUserLabel);

        JTextField NewUserField = new JTextField(15);

        NewUserField.setText("");

        NewUserField.setFont(new Font("Segoe UI Black", Font.PLAIN, 10));

        NewUserField.setSelectionColor(new Color(204,153,0));

        NewUserField.setCaretColor(gray);

        NewUserField.setPreferredSize(new Dimension(300,20));

        NewUserField.setBorder(new LineBorder(Navy,3,false));

        AboveBottom1.add(NewUserField);

        ParentPanel.add(AboveBottom1);

        JPanel PasswordPanel = new JPanel();

        JLabel Password = new JLabel("Password:");

        Password.setFont(new Font("tahoma", Font.BOLD, 15));

        Password.setForeground(gray);

        PasswordPanel.add(Password);

        JPasswordField newPassField = new JPasswordField(15);

        newPassField.setSelectionColor(new Color(204,153,0));

        newPassField.setFont(new Font("Segoe UI Black", Font.PLAIN, 10));

        newPassField.setCaretColor(gray);

        newPassField.setBorder(new LineBorder(Navy,3,false));

        PasswordPanel.add(newPassField);

        ParentPanel.add(PasswordPanel);

        JButton AddUser = new JButton("Add New User");

        AddUser.setFocusPainted(false);

        AddUser.setFont(new Font("Segoe UI Black", Font.BOLD, 14));

        AddUser.setBackground(new Color(223,85,83));

        AddUser.setForeground(Color.black);

        AddUser.addActionListener(e -> {
            try {
                String NewUserName = NewUserField.getText().trim();
                char[] NewUserPassPrime = newPassField.getPassword();
                String NewUserPass = new String(NewUserPassPrime);

                NewUserField.setText("");
                newPassField.setText("");

                if (!emptyStr(NewUserName) && !emptyStr(NewUserPass)) {
                    try {
                        read();

                        for (Users User: Users) {
                            if (User.getChangeName().equalsIgnoreCase(NewUserName)) {
                                throw new NullPointerException("User already exists");
                            }
                        }


                        Users.add(new Users(NewUserName,toHexString(getSHA(NewUserName +
                                "b91332ab9b07376173e11198629b8fb11081f8bb2a1fb555af4d8f44e75c9cac"
                                + NewUserPass)),"false"));

                        employeeWriter = new BufferedWriter(new FileWriter("EmployeeList.txt"));

                        for (Users u: Users) {
                            employeeWriter.write(u.getChangeName() + "," + u.getChangePass() + "," + u.getChangeBinary());

                            employeeWriter.write("\r\n");

                            employeeWriter.flush();
                        }

                        employeeWriter.close();

                        windowsFeel();

                        JOptionPane.showMessageDialog(null, "New user " + NewUserName + " was successfully created.",
                                "New user", JOptionPane.INFORMATION_MESSAGE,scaledDown);

                        PunchIn.setEnabled(true);
                        PunchOut.setEnabled(true);
                        GetReport.setEnabled(true);


                        swingFeel();
                    }

                    catch (NullPointerException exce) {
                        windowsFeel();

                        JOptionPane.showMessageDialog(null, "Sorry but that username is already in use." +
                                "\nPlease choose a different username.", "Already exists", JOptionPane.INFORMATION_MESSAGE,scaledDown);

                        swingFeel();
                    }
                    catch (Exception exc) {
                        windowsFeel();

                        JOptionPane.showMessageDialog(null, "Error: user was not created.", "ERROR", JOptionPane.ERROR_MESSAGE,scaledDown);

                        swingFeel();
                    }
                }

                else {
                    windowsFeel();

                    JOptionPane.showMessageDialog(null, "Sorry but you did not enter a valid username " +
                            "and password combo.", "Invalid credentials", JOptionPane.INFORMATION_MESSAGE,scaledDown);

                    swingFeel();
                }
            }

            catch (Exception ex) {
                windowsFeel();

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);

                JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                        "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

                swingFeel();
            }
        });

        AddUser.setPreferredSize(new Dimension(250,25));

        AddUser.setBorder(new LineBorder(Navy,3,false));

        JButton NewPeriod = new JButton("New Period");

        NewPeriod.setFocusPainted(false);

        NewPeriod.setFont(new Font("Segoe UI Black", Font.BOLD, 14));

        NewPeriod.setBackground(new Color(223,85,83));

        NewPeriod.setForeground(Color.black);

        NewPeriod.addActionListener(e -> {
            try {
                //confirmation for new pay period
                if (ConfirmNewPeriod()) {
                    //Make sure times are correct
                    RefreshTime();

                    //Reset credentiali fields
                    PasswordField.setText("");

                    NameField.setText("");

                    //New period function
                    NewPeriod();

                    //Compile a list of the users that were forced to punchout and inform the admin and write their names to the period summary folder
                    String ForcedOut = ForcedOutUsers.toString();

                    if (!ForcedOut.equals("[]")) {
                        windowsFeel();

                        JOptionPane.showMessageDialog(null, "The users " + ForcedOut + "\nwere forced to punch out.",
                                "Forced Punch Outs", JOptionPane.WARNING_MESSAGE,scaledDown);

                        swingFeel();

                        String ForcedOutLocation = Rename + "\\ForcedOutUsers.txt";

                        BufferedWriter ForcedOutWriter = new BufferedWriter(new FileWriter(ForcedOutLocation,true));

                        ForcedOutWriter.write(ForcedOut);

                        ForcedOutWriter.flush();

                        ForcedOutWriter.close();
                    }
                    //Exit the program
                    PunchFrame.dispose();

                    System.exit(0);

                    //Failsafe for security reasons
                    PasswordField.setText("");
                    NameField.setText("");
                 }
            }

            catch (Exception ex) {
                windowsFeel();

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);

                JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                        "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

                swingFeel();
            }
        });

        NewPeriod.setPreferredSize(new Dimension(250,25));

        NewPeriod.setBorder(new LineBorder(Navy,3,false));

        JPanel FlowPanel = new JPanel();

        FlowPanel.setLayout(new FlowLayout());

        FlowPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        FlowPanel.add(AddUser);

        FlowPanel.add(NewPeriod);

        ParentPanel.add(FlowPanel, Component.CENTER_ALIGNMENT);

        JPanel BottomPanel = new JPanel();

        JLabel LocationName = new JLabel("Title:");

        LocationName.setFont(new Font("tahoma", Font.BOLD, 15));

        LocationName.setForeground(gray);

        BottomPanel.add(LocationName);

        JTextField LocationNameField = new JTextField(20);

        LocationNameField.setText(Location);

        LocationNameField.setFont(new Font("Segoe UI Black", Font.PLAIN, 10));

        LocationNameField.setSelectionColor(new Color(204,153,0));

        LocationNameField.setCaretColor(gray);

        LocationNameField.setPreferredSize(new Dimension(300,25));

        LocationNameField.setBorder(new LineBorder(Navy,3,false));

        BottomPanel.add(LocationNameField);

        ParentPanel.add(BottomPanel);

        JButton Refreshall = new JButton("Refresh all fields");

        Refreshall.setFocusPainted(false);

        Refreshall.setFont(new Font("Segoe UI Black", Font.BOLD, 14));

        Refreshall.setBackground(new Color(223,85,83));

        Refreshall.setForeground(Color.black);

        Refreshall.addActionListener(e -> {
            try {
                readAD();

                boolean CloseTimeUpdated = false;
                boolean AutoPeriodUpdated = false;
                boolean LocationUpdated = false;

                String NewCloseTime = CloseTimes.getText().trim();
                String NewAutoPeriod = AutoNewPeriod.getText().trim();
                String NewLocation = LocationNameField.getText().trim();

                try {
                    if (emptyStr(NewCloseTime)) {
                        throw new Exception("New close time is blank.");
                    }

                    if (emptyStr(NewAutoPeriod)) {
                        throw new Exception("New auto period is blank.");
                    }

                    if (emptyStr(NewLocation)) {
                        throw new Exception("New location is blank.");
                    }
                }

                catch (Exception ex) {
                    windowsFeel();

                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    ex.printStackTrace(pw);

                    JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                            "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

                    swingFeel();
                }


                boolean closeTimeFormatCorrect = true;
                String[] CloseTimes1 = NewCloseTime.split(",");

                for (String time: CloseTimes1) {
                    String[] parts = time.split(":");

                    if (!(Integer.parseInt(parts[0]) < 24) || !(Integer.parseInt(parts[0]) >= 0) ||
                            !(Integer.parseInt(parts[1]) < 60) || !(Integer.parseInt(parts[1]) >= 0)) {

                        closeTimeFormatCorrect = false;

                        windowsFeel();

                        JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nThis" +
                                "is likely due to a formatting error with your requested close times.\n" +
                                "Please recheck your close times format to ensure they are formatted correctly.\n" +
                                "Examples: \"23:00\",\"14:45,20:45,02:30\"", "Parsing error", JOptionPane.ERROR_MESSAGE,scaledDown);

                        swingFeel();
                    }
                }

                if (closeTimeFormatCorrect) {
                    for (AD ad: ADS) {
                        if (ad.getName().equals("close at")) {
                            if (!ad.getValue().equals(NewCloseTime)) {
                                CloseTimeUpdated = true;
                            }
                            ad.setValue(NewCloseTime);
                        }
                    }
                }

                if (NewLocation.trim().length() >= 1 && !NewLocation.equals("")) {
                    Location = NewLocation.trim();
                    for (AD ad: ADS) {
                        if (ad.getName().equals("location name")) {
                            if (!ad.getValue().equals(NewLocation)) {
                                LocationUpdated = true;
                            }
                            ad.setValue(Location);
                        }
                    }

                    writeAD();

                }
                else {
                    throw new Exception("New Location field is blank.");
                }

                boolean periodDateFormatCorrect = true;

                try {
                    if (NewAutoPeriod.length() != 8 || NewAutoPeriod.contains("??/??/??")) {
                        throw new Exception("new auto period improperly formatted\nCorrect formats: \"12/25/19\",\"06/27/21\"");
                    }

                    SimpleDateFormat DF = new SimpleDateFormat("MM/dd/yy");
                    DF.setLenient(false);
                    Date result = DF.parse(NewAutoPeriod);
                }

                catch (ParseException excep) {
                    periodDateFormatCorrect = false;

                    windowsFeel();

                    JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nThis" +
                            "is likely due to a formatting error with your requested auto period time.\n" +
                            "Please recheck your format to ensure that it is formatted correctly\n" +
                            "Examples: \"02/14/20\",\"03/01/21,\".", "Parsing error", JOptionPane.ERROR_MESSAGE,scaledDown);

                    swingFeel();
                }

                if (periodDateFormatCorrect) {
                    for (AD ad: ADS) {
                        if (ad.getName().equals("auto period")) {
                            if (!ad.getValue().equals(NewAutoPeriod)) {
                                AutoPeriodUpdated = true;
                            }
                            ad.setValue(NewAutoPeriod);
                        }
                    }
                }

                if (AutoPeriodUpdated || LocationUpdated || CloseTimeUpdated) {
                    String build = "The following values were successfully updated:\n";

                    if (AutoPeriodUpdated) {
                        build = build + "Auto period was set to: " + NewAutoPeriod + "\n";
                    }

                    if (LocationUpdated) {
                        build = build + "Location was set to: " + NewLocation + "\n";
                        PunchFrame.setTitle(NewLocation);
                    }

                    if (CloseTimeUpdated) {
                        String[] HowMany = NewCloseTime.split(",");
                        if (HowMany.length > 1) {
                            build = build + "Close times were set to: " + NewCloseTime + "\n";
                        }

                        else {
                            build = build + "Close time was set to: " + NewCloseTime + "\n";
                        }
                    }

                    windowsFeel();

                    JOptionPane.showMessageDialog(null,build, "Updates", JOptionPane.INFORMATION_MESSAGE,scaledDown);

                    swingFeel();

                    System.exit(0);
                }

                writeAD();

                AdminMainFrame.dispose();

            }

            catch (Exception ex) {
                windowsFeel();

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);

                JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                        "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

                swingFeel();
            }
        });

        Refreshall.setPreferredSize(new Dimension(250,25));

        Refreshall.setBorder(new LineBorder(Navy,3,false));

        JPanel LastPanel = new JPanel();

        LastPanel.setLayout(new FlowLayout());

        LastPanel.add(Refreshall);

        ParentPanel.add(LastPanel, Component.CENTER_ALIGNMENT);

        ParentPanel.setBorder(new LineBorder(Navy,10,false));

        AdminMainFrame.add(ParentPanel);

        AdminMainFrame.setLocationRelativeTo(null);

        AdminMainFrame.repaint();

        AdminMainFrame.revalidate();

        AdminMainFrame.setVisible(true);

        readAD();

        try {
            for (AD ad: ADS) {
                switch (ad.getName()) {
                    case "close at":
                        CloseTimes.setText(ad.getValue());
                        break;
                    case "auto period":
                        AutoNewPeriod.setText(ad.getValue());
                        break;
                    case "location name":
                        LocationNameField.setText(ad.getValue());
                        break;
                    case "time change":
                        LogArea.append(ad.getValue() + "\n");
                        break;

                }
            }
        }

        catch (Exception ex) {
            windowsFeel();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                    "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

            swingFeel();
        }
    }

    private void readAD() {
        try {
            ADS.clear();

            BufferedReader ADSreader = new BufferedReader(new FileReader("AD.txt"));

            String Line;

            Line = ADSreader.readLine();

            while(Line != null && !Line.equals("") && Line.length() != 0) {

                if (Line.startsWith("//")) {
                    ADS.add(new AD("comment",Line));
                }

                else if (Line.equals("------------------------------------------------------")) {
                    ADS.add(new AD("sep",Line));
                }

                else if (Line.equals("Name:")) {
                    ADS.add(new AD("location name header",Line));
                    Line = ADSreader.readLine();
                    ADS.add(new AD("location name",Line));
                }

                else if (Line.equals("Close at:")) {
                    ADS.add(new AD("close at header",Line));
                    Line = ADSreader.readLine();
                    ADS.add(new AD("close at",Line));
                }

                else if (Line.equals("Automatic period (mod 2 weeks from):")) {
                    ADS.add(new AD("auto period header",Line));
                    Line = ADSreader.readLine();
                    ADS.add(new AD("auto period",Line));
                }

                else if (Line.equals("Recent time changes:")) {
                    ADS.add(new AD("recent time changes header",Line));
                    Line = ADSreader.readLine();
                    while (!Line.equals("------------------------------------------------------")) {
                        ADS.add(new AD("time change",Line));
                        Line = ADSreader.readLine();
                    }
                    ADS.add(new AD("sep",Line));
                }

                else if (Line.equals("Reset Log:")) {
                    ADS.add(new AD("reset log header",Line));
                    Line = ADSreader.readLine();
                    while (!Line.equals("------------------------------------------------------")) {
                        ADS.add(new AD("reset date",Line));
                        Line = ADSreader.readLine();
                    }
                    ADS.add(new AD("sep",Line));
                }
                Line = ADSreader.readLine();
            }
            ADSreader.close();
        }

        catch (Exception ex) {
            windowsFeel();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                    "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

            swingFeel();
        }
    }

    private void writeAD() {
        try {
            BufferedWriter ADSwriter = new BufferedWriter(new FileWriter("AD.txt"));

            String WriteLast = ADS.removeLast().getValue();

            for (AD ad: ADS) {
                ADSwriter.write(ad.getValue());

                ADSwriter.write("\r\n");

                ADSwriter.flush();
            }

            ADSwriter.write(WriteLast);

            if (!WriteLast.equals("------------------------------------------------------")) {
                ADSwriter.write("\n------------------------------------------------------");
            }

            ADSwriter.close();
        }

        catch (Exception ex) {
            windowsFeel();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            JOptionPane.showMessageDialog(null,"An unfortunate error occured.\nMaybe Nathan is an idiot?\n" +
                    "We won't know unless you email him the following error at: NathanJavaDevelopment@gmail.com\nError: " + sw.toString(), "Error", JOptionPane.ERROR_MESSAGE,scaledDown);

            swingFeel();
        }
    }

    //go through ADS object list and setup program processes based on them
    private void ActOnAdminActions() {
        readAD();

        for (AD ad: ADS) {
            switch (ad.getName()) {
                case "close at":
                    String[] Dates = ad.getValue().split(",");
                    for (String date : Dates) {
                        String[] HourMin = date.split(":");
                        CloseAtHourMinute(Integer.parseInt(HourMin[0]), Integer.parseInt(HourMin[1]));
                    }
                    break;
                case "auto period":
                    CheckPeriod(ad.getValue());
                    break;
                case "location name":
                    Location = ad.getValue();
                    break;
            }
        }
    }

    //Process to close the program at hour:minute
    private void CloseAtHourMinute(int hour, int minute) {
        Calendar CloseCalendar = Calendar.getInstance();

        CloseCalendar.add(Calendar.DAY_OF_MONTH, 0);

        CloseCalendar.set(Calendar.HOUR_OF_DAY, hour);

        CloseCalendar.set(Calendar.MINUTE, minute);

        CloseCalendar.set(Calendar.SECOND, 0);

        CloseCalendar.set(Calendar.MILLISECOND, 0);

        long HowMany = CloseCalendar.getTimeInMillis() - System.currentTimeMillis();

        //if we are past it for the day then it will just instantly close so we need to set it for tomorrow's dates
        HowMany = (HowMany < 0 ? HowMany + 24 * 60 * 60 * 1000 : HowMany);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.schedule(ExitRunnable ,HowMany, TimeUnit.MILLISECONDS);
    }

    //Exit the program
    private Runnable ExitRunnable = new Runnable() {
        @Override
        public void run() {
            PunchFrame.dispose();

            System.exit(0);
        }
    };
}