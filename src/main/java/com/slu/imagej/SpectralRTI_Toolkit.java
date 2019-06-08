/**
 * @author  Bryan Haberberger
 * @email   digitalhumanities@slu.edu
 * @version 1.0
 * @since   07/01/2017
 * <h1>Spectral RTI Toolkit ImageJ2 Java Plugin</h1>
 * <p>
    * Originally created as an ImageJ macro by Todd Hanneken.  This is a conversion
    * making the macro a Java plugin with improvements and fixes along the way.
    * https://github.com/thanneken/SpectralRTI_Toolkit
    * 
    * The conversion was performed by the Walter J. Ong S.J. Center for Digital
    * Humanities at Saint Louis University.  You can see the work history 
    * hosted at the Center's GitHub repository
    * https://github.com/CenterForDigitalHumanities/SpectralRTI_Toolkit
 * </p>
  * <h2> Helpful sources' links for the conversion process </h2>
  * <ul>
  *     <li> https://imagej.nih.gov/ij/docs/guide/146-26.html </li>
  *     <li> https://imagej.net/2016-04-19_-_Writing_ImageJ2_Plugins:_A_Beginner%27s_Perspective </li>
  *     <li> https://github.com/imagej/tutorials/tree/master/maven-projects/simple-commands/src/main/java </li>
  *     <li> https://github.com/imagej/tutorials/blob/master/maven-projects/add-rois/src/main/java/AddROIs.java </li>
  *     <li> http://palimpsest.stmarytx.edu/integrating/WhitePaper-20140630.pdf </li>
  *     <li> https://imagej.nih.gov/ij/developer/macro/functions.html </li>
  *     <li> http://imagej.net/ImgLib2_Examples </li>
  *     <li> https://imagej.nih.gov/ij/developer/api/ij/gui/GenericDialog.html </li>
  *     <li> https://docs.oracle.com/javase/tutorial/essential/io/file.html </li>
  *     <li> http://imagej.1557.x6.nabble.com/Re-Plugin-Command-To-Close-Window-Without-quot-Save-Changes-quot-Dialog-td3683293.html </li>
  *     <li> http://www.javapractices.com/topic/TopicAction.do?Id=42 </li>
  *     <li> https://stackoverflow.com/questions/15199119/runtime-exec-waitfor-doesnt-wait-until-process-is-done </li>
  *     <li> https://stackoverflow.com/questions/35427546/how-to-add-scrollablescrollbar-component-checkbox-in-a-panel-in-javaswing </li>
  *     <li> https://docs.oracle.com/javase/tutorial/uiswing/layout/visual.html </li> 
  *     <li> https://stackoverflow.com/questions/10685893/run-exe-file-in-java-from-file-location </li>
  *     <li> https://stackoverflow.com/questions/201287/how-do-i-get-which-jradiobutton-is-selected-from-a-buttongroup </li>
  *     <li> http://www.codejava.net/java-se/swing/jradiobutton-basic-tutorial-and-examples </li>
  *     <li> https://stackoverflow.com/questions/3597550/ideal-method-to-truncate-a-string-with-ellipsis </li>
  *     <li> https://imagej.nih.gov/ij/developer/api/ij/plugin/package-summary.html </li>
  * </ul> 
*/

package com.slu.imagej;

//ImageJ specific imports
import ij.IJ;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.WaitForUserDialog;
import ij.io.OpenDialog;
import ij.io.DirectoryChooser;
import ij.io.Opener;
import ij.ImagePlus; // this is IJ 1.x but still needs to be used for the complexity found here.  IJ2 and ImgLib2 and ImgPlus are not fully supported quite yet.
import ij.plugin.Concatenator;

//ImageJ2 specific imports
import net.imagej.ImageJ; 
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.log.LogService;
import io.scif.img.ImgIOException;
import net.imagej.overlay.RectangleOverlay;

//Extra java core functionality imports
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.Enumeration;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.commons.io.comparator.NameFileComparator;
import ui.*;

/**
 * @author Bryan Haberberge
 * @see the @Plugin tag here, it allows me to define where I want this to show up in the ImageJ menu.
 * The class to be be implemented as an ImageJ command.  Does not need an image opened already.  
 * This is compliant with ImageJ2 and intended to be used with the Fiji version of ImageJ.
 */
@Plugin(type = Command.class, menuPath = "Plugins>SpectralRTI_Toolkit")  
public class SpectralRTI_Toolkit implements Command {
        private Context context;
        /** The global ImagePlus object to be used throughout.  Used with the IJ library for ImageJ functionality, supported by IJ2. */
        protected ImagePlus imp;
        /** The global ImgLib2 compatible Img type object to be used throughout.  Used with the ImgLib2 library. */
        private final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); //screenSize dimensions for sizing custom windows
        int prefW = (int) (screenSize.width*.85); //Use 85% of the screen for appropriate windows. 
        int prefH = (int) (screenSize.height*.85);//Use 85% of the screen for appropriate windows. 
        private final Dimension preferredSize = new Dimension(prefW, prefH); //Use 85% of the screen for appropriate windows. 
        private final Dimension bestFit = new Dimension(screenSize.width-20, screenSize.height-20); //Best fit for full screen sized windows.
        /** The logger for the given application, in this case ImageJ */
        @Parameter
        private LogService logService;
        //SRTI vars
        private Process p;
        private Process p2;
        private int jpegQuality = 90; //maximize quality for non-distribution phases
        private final int jpegQualityWebRTI = 90; //lower for final distribution
        private final int ramWebRTI = 8192;
        private String brightnessAdjustOption = "";
        private String brightnessAdjustApply = "";
        private String transmissiveSource= ""; //(thanks Kathryn!)
	private int normX;
	private int normY;
	private int normWidth;
	private int normHeight;
	private double normalizationFixedValue = 1.00;
	private int pcaX = 0;
        private int pcaY = 0;
        private int pcaWidth = 0;
        private int pcaHeight = 0;
	private String lpSource = "";
	private String projectDirectory = "";
	private String projectName = "";
        private List<Boolean> listOfRakingDirections = new ArrayList<>();
        private String simpleImageName = "";
        private String filePath = "";
        private Rectangle bounds;
        //This is important for native commands.  Java is NOT PLATFORM INDEPENDENT, so check what platform we are.
        private final boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        private String positionNumber = "";
        String pcaMethod = "";
        private final List<String> redNarrowbands_list = new ArrayList<>();
        private final List<String> greenNarrowbands_list = new ArrayList<>();
        private final List<String> blueNarrowbands_list = new ArrayList<>();
        Object[] redNarrowbands = new File[0];
        Object[] greenNarrowbands = new File[0];
        Object[] blueNarrowbands = new File[0];
        File toDelete = null;
        WaitForUserDialog dWait = null;
        RectangleOverlay region = new RectangleOverlay();
        final Opener opener = new Opener();//ImageJ Image Opener
        //This works as the List used throughout the macro.
        private final static HashMap<String, String> theList; //List var from macro
        private String startTime = "";
        private boolean webRtiDesired = false;
        private Boolean shortName = false; 
        static
        {
            theList = new HashMap<>();
            theList.put("preferredCompress", "");
            theList.put("preferredJp2Args", "");
            theList.put("preferredFitter", "");
            theList.put("jpegQuality", "90");
            theList.put("hshOrder", "0");
            theList.put("hshThreads", "0");
            theList.put("webRtiMaker", "");
            theList.put("shortFileNames", "false");
        }
        private JPanel contentPane = new JPanel();
        final ImageJ ij2 = new ImageJ();
	public double value;
        public String name;
        public File spectralPrefsFile = new File("SpectralRTI_Toolkit-prefs.txt");//This is in the base fiji folder. 
        public String prefsFileAsText = "";
        
        private void plugin() throws IOException, Throwable{
            //want these variables to be accessible across functions and to reset each time the macro is run
            startTime = timestamp();
            //The log.warn() is a quick fix to force the log and console to pop up without user interaction.
            logService.log().info("Starting SpectralRTI Plugin at "+startTime);
            logService.log().info("Detected OS "+System.getProperty("os.name")+".  Treat as Windows = "+isWindows);
            File accurateColorSource = null; //may be a better way to do this without the null.
            String[] prefs = null;
            DirectoryChooser file_dialog;
            Boolean lpDesired=false;
            Boolean acRtiDesired=false;
            Boolean xsRtiDesired=false;
            Boolean psRtiDesired=false;
            Boolean psRakingDesired = false;
            Boolean csRtiDesired = false;
            Boolean csRakingDesired = false;
            Boolean acRakingDesired = false;
            Boolean xsRakingDesired = false;

            File[] listOfAccurateColorSources = new File[0];
            File[] listOfNarrowbandCaptures = new File[0];
            File[] listOfHemisphereCaptures = new File[0];
            String csSource = "";
            Concatenator con = new Concatenator();
            File projectFile = null;
            
            /**
             * Make sure there is a preference file.  If not, create one with the default empty entries.
             */
            if (!spectralPrefsFile.exists()){ //If this exists, overwrite the labels and show a dialog with the settings
                JOptionPane.showMessageDialog(null, "A prefs file will be created for you in the ImageJ directory to store your choices for later sessions.", "No Preference File Found", JOptionPane.PLAIN_MESSAGE);
                logService.log().info("We are making a new prefs file with the empty defaults.");
                /**
                    *This will put the prefs file the folder that ImageJ.exe is run out of.  Do we want a prefs directory inside a project folder instead? 
                    *@see projectDirectory 
                */
                String arguments = "-rate -,2.4,1.48331273,.91673033,.56657224,.35016049,.21641118,.13374944,.08266171 Creversible=no Clevels=5 Stiles={1024,1024} Cblk={64,64} Cuse_sop=yes Cuse_eph=yes Corder=RPCL ORGgen_plt=yes ORGtparts=R Cmodes=BYPASS -double_buffering 10 -num_threads 4 -no_weights";
                Files.createFile(spectralPrefsFile.toPath()); 
                Files.write(Paths.get(spectralPrefsFile.toString()), ("preferredCompress="+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                Files.write(Paths.get(spectralPrefsFile.toString()), ("preferredJp2Args="+arguments+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                Files.write(Paths.get(spectralPrefsFile.toString()), ("preferredFitter="+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                Files.write(Paths.get(spectralPrefsFile.toString()), ("jpegQuality=90"+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                Files.write(Paths.get(spectralPrefsFile.toString()), ("hshOrder=3"+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                Files.write(Paths.get(spectralPrefsFile.toString()), ("hshThreads=16"+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                Files.write(Paths.get(spectralPrefsFile.toString()), ("webRtiMaker="+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                Files.write(Paths.get(spectralPrefsFile.toString()), ("shortFileNames=false"+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
            }
            prefsFileAsText = new String(Files.readAllBytes(spectralPrefsFile.toPath()), "UTF8");
            prefs = prefsFileAsText.split(System.lineSeparator());
            /**
             * First ask the user to locate the project directory.  We cannot continue without one. 
             */
            while(projectDirectory == null || projectDirectory.equals("") || null == projectFile || !projectFile.exists()){
                file_dialog = new DirectoryChooser("Choose the Project Directory"); //The first thing the user does is provide the project directory.
                projectDirectory = file_dialog.getDirectory();
                if(null==projectDirectory){
                    //@userHitCancel
                    IJ.error("You must provide a project directory to continue.  Exiting...");
                    throw new Throwable("You must provide a project directory."); //DIE if no directory provided
                }
                projectFile = new File(projectDirectory);
            }
            if(projectDirectory == null || projectDirectory.equals("")){
                IJ.error("You must provide a project directory to continue.  Exiting...");
                throw new Throwable("You must provide a project directory."); //DIE if no directory provided
            }
            else{
                projectName = projectFile.getName();
            }
            projectDirectory = projectDirectory.substring(0, projectDirectory.length() - 1); //always has a trailing '/'
            projectDirectory = projectDirectory + File.separator; //make sure it ends with the proper trailing slash for the OS
            logService.log().info("Project directory: "+projectDirectory);
            
            File light_position_dir = new File(projectDirectory+"LightPositionData"+File.separator);
            File accurate_color_dir = new File(projectDirectory+"AccurateColor"+File.separator);
            File accurate_colorrti_dir = new File(projectDirectory+"AccurateColorRTI"+File.separator);
            File narrow_band_dir = new File(projectDirectory+"Captures-Narrowband-NoGamma"+File.separator);
            if(narrow_band_dir.exists()){
                listOfNarrowbandCaptures = narrow_band_dir.listFiles();
                Arrays.sort(listOfNarrowbandCaptures, NameFileComparator.NAME_COMPARATOR);
            }
            File pseudo_color_dir = new File(projectDirectory+"PseudoColorRTI"+File.separator);
            File extended_spectrum_dir = new File(projectDirectory+"ExtendedSpectrumRTI"+File.separator);
            File static_ranking_dir = new File(projectDirectory+"StaticRaking"+File.separator);
            File transmissive_gamma_dir = new File(projectDirectory+"Captures-Transmissive-Gamma"+File.separator);
            File hemi_gamma_dir = new File(projectDirectory+"Captures-Hemisphere-Gamma"+File.separator);
            /**
             * Second, consult with the user about their desired tasks.  This will help us know what to ask them throughout the plugin.
             * We cannot continue without a task.
             */
            //Hmm this feels like it should be some kind of defined private array or something somwhere, not just defined all willy nilly here.  
            //Original macro task defaulting behavior
            JCheckBox[] tasks = new JCheckBox[11];
            JCheckBox ch1 = new JCheckBox("Light Position Data");
            ch1.setToolTipText("Generate light position data.  You will need a Fitter to perform this task.");
            JCheckBox ch2 = new JCheckBox("Accurate Color RTI");
            ch2.setToolTipText("Generate an Accurate Color RTI Image.  You will need a Fitter to perform this task.");
            JCheckBox ch12 = new JCheckBox("Accurate Color Static Raking");
            ch12.setToolTipText("Generate a JP2 Accurate Color Static Raking image.  You will need a JP2 Compressor to perform this task.");
            JCheckBox ch3 = new JCheckBox("Extended Spectrum RTI");
            ch3.setToolTipText("Generate an Extended Spectrum RTI Image.  You will need a Fitter to perform this task.");
            JCheckBox ch4 = new JCheckBox("Extended Spectrum Static Raking");
            ch4.setToolTipText("Generate a JP2 Extended Spectrum Static Raking image.  You will need a JP2 Compressor to perform this task.");
            JCheckBox ch5 = new JCheckBox("PseudoColor RTI");
            ch5.setToolTipText("Generate a Pseudocolor RTI Image.  You will need a Fitter to perform this task.");
            JCheckBox ch6 = new JCheckBox("PseudoColor Static Raking");
            ch6.setToolTipText("Generate a JP2 Pseudocolor Static Raking image.  You will need a JP2 Compressor to perform this task.");
            JCheckBox ch7 = new JCheckBox("Custom RTI");
            ch7.setToolTipText("Generate a Custom Spectral RTI Image.  You will need a Fitter and a custom source to perform this task.");
            JCheckBox ch8 = new JCheckBox("Custom Static Raking");
            ch8.setToolTipText("Generate a Custom JP2 Static Raking image.  You will need a JP2 Compressor and a custom source to perform this task.");
            JCheckBox ch9 = new JCheckBox("WebRTI");
            ch9.setToolTipText("Generate a WebRTI image for all RTI images created from other selected tasks.  If no other RTI task is selected, this process will ask for you to provide an RTI image.");
            JLabel snL = new JLabel("Check below to use names instead of paths.");
            snL.setToolTipText("/path/to/file.exe  vs.  file.exe");
            snL.setBorder(new EmptyBorder(15,0,0,0)); //put some margin/padding around a label
            JCheckBox ch10 = new JCheckBox("Short File Names");
            ch10.setToolTipText("A preferece as to whether you want to see the full file path or just the file name throughout the plugin.");
            
            //original macro defaulting behavior
            if (!light_position_dir.exists() || light_position_dir.listFiles().length == 0) { 
                ch1.setSelected(true);
            }
            if (!accurate_colorrti_dir.exists() || !accurate_color_dir.exists() || accurate_color_dir.listFiles().length == 0){
                ch2.setSelected(true);
            }
            if(!extended_spectrum_dir.exists() || extended_spectrum_dir.listFiles().length == 0){
                ch3.setSelected(true);
            }
            if(!pseudo_color_dir.exists() || pseudo_color_dir.listFiles().length == 0){
                ch5.setSelected(true);
            }
            if(listOfNarrowbandCaptures.length < 9){
                ch3.setSelected(false);
                ch5.setSelected(false);
            }
            
            //FIXME:This is a bit of a hack.  If the shortFileName is not the last preference in the prefs file, this will break.
            int last_pref = prefs.length - 1;
            String shortNamePref = prefs[last_pref].substring(prefs[last_pref].indexOf("=")+1); //Pre-populate choices
            if(shortNamePref.equals("true") || shortNamePref.equals("yes")){
                shortName = true;
            }
            ch10.setSelected(shortName);
            tasks[0] = ch1;
            tasks[1] = ch2;
            tasks[2] = ch12;
            tasks[3] = ch3;
            tasks[4] = ch4;
            tasks[5] = ch5;
            tasks[6] = ch6;
            tasks[7] = ch7;
            tasks[8] = ch8;
            tasks[9] = ch9;
            tasks[10] = ch10;
            while(!(acRakingDesired || acRtiDesired || xsRtiDesired || xsRakingDesired || psRtiDesired || psRakingDesired || csRtiDesired || csRakingDesired || lpDesired || webRtiDesired)){
                contentPane = new JPanel();
                JPanel scrollGrid = new JPanel();
                scrollGrid.setLayout(new BoxLayout(scrollGrid,BoxLayout.PAGE_AXIS));
                contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));
                JPanel labelPanel = new JPanel();
                JLabel taskDirection = new JLabel("Select the tasks you would like to complete.  You must select at least one.");
                labelPanel.add(taskDirection);
                contentPane.add(labelPanel);
                /**
                 * UI for creating the checkbox selections.  
                 * @see shortName
                */
                scrollGrid.add(tasks[0]);
                scrollGrid.add(tasks[1]);
                scrollGrid.add(tasks[2]);
                scrollGrid.add(tasks[3]);
                scrollGrid.add(tasks[4]);
                scrollGrid.add(tasks[5]);
                scrollGrid.add(tasks[6]);
                scrollGrid.add(tasks[7]);
                scrollGrid.add(tasks[8]);
                scrollGrid.add(tasks[9]);
                scrollGrid.add(snL);
                scrollGrid.add(tasks[10]);
                JScrollPane spanel = new JScrollPane(scrollGrid);
                spanel.setBorder(BorderFactory.createEmptyBorder());  
                contentPane.add(spanel);
                Object[] taskBtnLabels = {"Confirm",
                    "Quit"};
                int taskResult = JOptionPane.showOptionDialog(null, contentPane, "Choose Desired Tasks", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, taskBtnLabels, taskBtnLabels[0]);
                /**
                 * Gather and process user selected tasks
                */
                if (taskResult == JOptionPane.OK_OPTION){
                    lpDesired = tasks[0].isSelected();
                    acRtiDesired = tasks[1].isSelected();
                    acRakingDesired = tasks[2].isSelected();
                    xsRtiDesired = tasks[3].isSelected();
                    xsRakingDesired = tasks[4].isSelected();
                    psRtiDesired = tasks[5].isSelected();
                    psRakingDesired = tasks[6].isSelected();
                    csRtiDesired = tasks[7].isSelected();
                    csRakingDesired = tasks[8].isSelected();
                    webRtiDesired = tasks[9].isSelected();
                    if(!(acRakingDesired || acRtiDesired || xsRtiDesired || xsRakingDesired || psRtiDesired || psRakingDesired || csRtiDesired || csRakingDesired || lpDesired || webRtiDesired)){
                        JOptionPane.showMessageDialog(null,
                        "You must select at least one task.", "Try Again",
                        JOptionPane.PLAIN_MESSAGE);
                    }
                    else{
                        shortName = tasks[10].isSelected(); //This is a preference, must write to prefs file
                        prefsFileAsText = new String(Files.readAllBytes(spectralPrefsFile.toPath()), "UTF8");
                        String filePreferenceString = "shortFileNames="+shortName+System.lineSeparator();
                        prefsFileAsText = prefsFileAsText.replaceFirst("shortFileNames=.*\\"+System.lineSeparator(), filePreferenceString); //replace the prefs var
                        Files.write(spectralPrefsFile.toPath(), prefsFileAsText.getBytes()); 
                    }
                }
                else {
                     //@userHitCancel
                    IJ.error("You must provide a task set to continue.  Exiting...");
                    throw new Throwable("You must provide a task set to continue.");
                }
            }
            /**
             * consult with user about values stored in prefs file in base fiji folder.  
             * Some will be required and if not provided, the plugin will have to ask for them later.
             * TODO: Since we know desired tasks, make sure to call out the required preferences with a bold or colored label.  
             */           
            contentPane = new JPanel();
            JPanel scrollGrid = new JPanel();
            scrollGrid.setLayout(new SpringLayout());
            //display label and text area side by side in two columns for as many prefs exist
            contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));
            JPanel labelPanel = new JPanel();
            JPanel labelPanel2 = new JPanel();
            JPanel labelPanel3 = new JPanel();
            JLabel prefsLabel = new JLabel("The following settings are in the configuration file.");
            JLabel prefsLabel2 = new JLabel("Edit or clear as desired.  Required information is bolded.");
            JLabel prefsLabel3 = new JLabel("Tip: Select Window > Console in the ImageJ panel during processing for more information.");
            labelPanel.add(prefsLabel);
            labelPanel2.add(prefsLabel2);
            labelPanel3.add(prefsLabel3);
            contentPane.add(labelPanel);
            contentPane.add(labelPanel2);
            contentPane.add(labelPanel3);
            //If there was no prefs file when the plugin first ran, it created a prefs file with the default values, which will be read out here.  
            //Otherwise, it found a prefs file and will read out what it had stored from last time.
            logService.log().info(Arrays.toString(prefs));
            JTextField[] fields = new JTextField[prefs.length];
            for (int i=0;i<prefs.length;i++){
                //Swap the labels out for presentation
                String key = prefs[i].substring(0, prefs[i].indexOf("="));
                JLabel fieldLabel = null;
                switch(key){
                    case "preferredCompress":
                        key = key.replace("preferredCompress","JP2 Compressor");
                        fieldLabel = new JLabel(key, JLabel.TRAILING);
                        fieldLabel.setToolTipText("Your preferred JP2 Compressor.  This will be required for Static Raking operations. ");
                    break;

                    case "preferredJp2Args":
                        key = key.replace("preferredJp2Args","JP2 Arguments");
                        fieldLabel = new JLabel(key, JLabel.TRAILING);
                        fieldLabel.setToolTipText("Your preferred JP2 Arguments for the JP2 Compressor.  Default settings will be offered to you if not set.  This will be required for Static Raking operations.");
                    break;

                    case "preferredFitter":
                        key = key.replace("preferredFitter","HSH Fitter");
                        fieldLabel = new JLabel(key, JLabel.TRAILING);
                        fieldLabel.setToolTipText("Your preferred HSH fitter for creating RTI files.  This will be required for all RTI Image operations. ");
                    break;

                    case "jpegQuality":
                        key = key.replace("jpegQuality","JPEG Quality");
                        fieldLabel = new JLabel(key, JLabel.TRAILING);
                        fieldLabel.setToolTipText("Your preferred JPEG Quality (1-100).  This will be applied when creating JPEG source files for RTI images. ");
                    break;

                    case "hshOrder":
                        key = key.replace("hshOrder","HSH Order");
                        fieldLabel = new JLabel(key, JLabel.TRAILING);
                        fieldLabel.setToolTipText("Default is 3.  This will be applied when creating RTI images. ");
                    break;

                    case "hshThreads":
                        key = key.replace("hshThreads","HSH Threads");
                        fieldLabel = new JLabel(key, JLabel.TRAILING);
                        fieldLabel.setToolTipText("Default is 16.  This will be applied when creating RTI images. ");
                    break;

                    case "webRtiMaker":
                        key = key.replace("webRtiMaker","Web RTI Maker");
                        fieldLabel = new JLabel(key, JLabel.TRAILING);
                        fieldLabel.setToolTipText("Your preferred WEB RTI maker.  The plugin will send the produced or selected RTI file to be converted into WebRTI and is required for this operation. ");
                    break;

                    case "shortFileNames":
                        key = key.replace("shortFileNames","Short File Names");
                        fieldLabel = new JLabel(key, JLabel.TRAILING);
                        fieldLabel.setToolTipText("A preferece as to whether you want to see the full file path or just the file name throughout the UI.  This setting is not required.");
                    break;

                    default:
                        key = "**"+key;
                        fieldLabel = new JLabel(key, JLabel.TRAILING);
                        fieldLabel.setToolTipText("This is an unknown or unsupported preference.");
                        //This is an unknown setting or an attempt at expansion
                }
                String value1 = prefs[i].substring(prefs[i].indexOf("=")+1); //Pre-populate choices
                value1 = value1.replace("/", File.separator); //ensure dir values are displayed with the correct slashes
                value1 = value1.replace("\\", File.separator); //ensure dir values are displayed with the correct slashes
                if(key.equals("Shot File Names")){
                    value1 = ""+shortName;
                }
                if(key.equals("HSH Fitter") || key.equals("HSH Order") || key.equals("HSH Threads")){
                    if(acRtiDesired || xsRtiDesired || psRtiDesired || csRtiDesired){
                    //We will need to know the fitter
                        Font font = fieldLabel.getFont();
                        // same font but bold
                        Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
                        fieldLabel.setFont(boldFont);
                    }
                }
                if(key.equals("JP2 Arguments") || key.equals("JP2 Compressor") || key.equals("JPEG Quality")){
                    if(acRakingDesired || xsRakingDesired || psRakingDesired || csRakingDesired){
                    //We will need to know the compressor and arguments
                        Font font = fieldLabel.getFont();
                        // same font but bold
                        Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
                        fieldLabel.setFont(boldFont);
                    }
                }
                if(key.equals("Web RTI Maker")){
                    if(webRtiDesired){
                    //We will need to know the web RTI Maker
                        Font font = fieldLabel.getFont();
                        // same font but bold
                        Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
                        fieldLabel.setFont(boldFont);
                    }
                }
                scrollGrid.add(fieldLabel);
                JTextField fieldToAdd = new JTextField(value1, 50);
                JButton chooseBtn = new JButton("Choose File");
                fields[i] = fieldToAdd;
                if(key.equals("HSH Fitter") || key.equals("JP2 Compressor") || key.equals("Web RTI Maker")){
                    //We want to offer the user a file picker to populate these text fields alongside the textfield
                    JPanel chooserArea = new JPanel();
                    JFileChooser chooser = new JFileChooser();
                    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    chooseBtn.addActionListener(new ActionListener() { 
                    public void actionPerformed(ActionEvent e) { 
                            //prefFileChooser();
                            int returnVal = chooser.showOpenDialog(null);
                            if(returnVal == JFileChooser.APPROVE_OPTION) {
                                fieldToAdd.setText(chooser.getSelectedFile().getAbsolutePath());
                            }
                            else{
                                fieldToAdd.setText("");
                            }
                        } 
                    });
                    chooserArea.add(chooseBtn);
                    chooserArea.add(fieldToAdd);
                    fieldLabel.setLabelFor(chooserArea);
                    scrollGrid.add(chooserArea);
                }
                else{
                    //Just a label and text field.
                    fieldLabel.setLabelFor(fieldToAdd);
                    scrollGrid.add(fieldToAdd);
                }
            }
            SpringUtilities.makeCompactGrid(scrollGrid,
                8, 2, //rows, cols
                6, 6, //initX, initY
                6, 6);//xPad, yPad
            JScrollPane spanel = new JScrollPane(scrollGrid);
            spanel.setBorder(BorderFactory.createEmptyBorder());
            contentPane.add(spanel);

            //Gather new values from the dialog, reset the labels and update the new values.
            Object[] prefBtnLabels = {"Update",
                "Continue"};
            int prefsResult = JOptionPane.showOptionDialog(null, contentPane, "Consult Preferences", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, prefBtnLabels, prefBtnLabels[0]);
            if (prefsResult == JOptionPane.OK_OPTION){
                for (int j=0; j<prefs.length;j++) {
                    //Swap the labels back for processing
                    String key = prefs[j].substring(0, prefs[j].indexOf("="));
                    key = key.replace("JP2 Compressor","preferredCompress");
                    key = key.replace("JP2 Arguments","preferredJp2Args");
                    key = key.replace("HSH Fitter","preferredFitter");
                    key = key.replace("JPEG Quality","jpegQuality");
                    key = key.replace("HSH Order","hshOrder");
                    key = key.replace("HSH Threads","hshThreads");
                    key = key.replace("Web RTI Maker","webRtiMaker");
                    key = key.replace("Short File Names","shortFileNames");
                    String value2 = fields[j].getText(); //Gather new information
                    if(key.equals("shortFileNames")){
                        shortName = (value2.equals("true") || value2.equals("yes"));
                        value2 = ""+shortName;
                    }
                    if(key.equals("jpegQuality")){
                        int jpq = Integer.parseInt(value2);
                        if (jpq > 0){
                            jpegQuality = jpq;
                        }
                        else{
                            jpegQuality = ij.plugin.JpegWriter.getQuality();
                        }
                        value2 = Integer.toString(jpegQuality);
                    }
                    theList.put(key,value2);
                    value2 = value2.replace("\\", "/"); //Always store dirs to prefs file with backslash.
                    prefsFileAsText = prefsFileAsText.replaceFirst(key+"=.*\\"+System.lineSeparator(), key+"="+value2+System.lineSeparator()); //replace the prefs var
                }
            }
            else {
                //@userHitCancel
                for (int j=0; j<prefs.length;j++) {
                    //Swap the labels back for processing
                    String key = prefs[j].substring(0, prefs[j].indexOf("="));
                    key = key.replace("JP2 Compressor","preferredCompress");
                    key = key.replace("JP2 Arguments","preferredJp2Args");
                    key = key.replace("HSH Fitter","preferredFitter");
                    key = key.replace("JPEG Quality","jpegQuality");
                    key = key.replace("HSH Order","hshOrder");
                    key = key.replace("HSH Threads","hshThreads");
                    key = key.replace("Web RTI Maker","webRtiMaker");
                    key = key.replace("Short File Names","shortFileNames");
                    String value2 = fields[j].getText(); //Gather new information
                    if(key.equals("shortFileNames")){
                        shortName = (value2.equals("true") || value2.equals("yes"));
                        value2 = ""+shortName;
                    }
                    theList.put(key,value2);
                }
            }
            logService.log().info("These are the provided preferences: ");
            logService.log().info(theList.toString());
            Files.write(spectralPrefsFile.toPath(), prefsFileAsText.getBytes()); //rewrite the prefs file       
            IJ.run("Input/Output...","jpeg="+jpegQuality);          
            if (!hemi_gamma_dir.exists()) {
                Path createPath = hemi_gamma_dir.toPath();
                Files.createDirectory(createPath);
                //We will alert users that this did not exist because it is so important
                JOptionPane.showMessageDialog(null, "A Hemisphere Captures folder has been created for you in your project directory.  You will need captures to use this plugin. ", "Hemisphere Captures Directory Not Found", JOptionPane.PLAIN_MESSAGE);
                logService.log().info("A directory has been created for the Hemisphere Captures at "+projectDirectory+"Captures-Hemisphere-Gamma");
                hemi_gamma_dir = new File(createPath.toString());
            }
            listOfHemisphereCaptures = getHemisphereCaptures(hemi_gamma_dir.toString());
            while (listOfHemisphereCaptures.length < 1 && IJ.showMessageWithCancel("Please Populate Hemisphere Captures","The software expects at least 1 image in HemisphereCaptures folder.\nPlease populate the folder then press Ok to continue, or cancel.")){
                listOfHemisphereCaptures = getHemisphereCaptures(hemi_gamma_dir.toString());
            }
            if(listOfHemisphereCaptures.length < 1){
                //WindowManager.closeAllWindows();
                IJ.error("There must be at least 1 image in the hemisphere captures folder to continue.  Please populate for next time.  Exiting...");
                throw new Throwable("There must be at least 1 image in the hemisphere caputres folder to continue.  Please populate for next time.");
            }
            Arrays.sort(listOfHemisphereCaptures, NameFileComparator.NAME_COMPARATOR);
            /**
            * These aren't always necessary and therefore aren't technically required.  We will create them automatically, but we do not need to tell the user.
            * Consequently, this controls building the full required project folder structure automatically or not.  Whether we choose to do it or not does not 
            * affect functionality.  
            */
            if (!light_position_dir.exists() ){ 
                Files.createDirectory(light_position_dir.toPath());
            }
            if (!accurate_color_dir.exists() ){
                Files.createDirectory(accurate_color_dir.toPath());
            }

            if(!narrow_band_dir.exists()){
                Files.createDirectory(narrow_band_dir.toPath());
            }
            else{
                listOfNarrowbandCaptures = narrow_band_dir.listFiles();
                Arrays.sort(listOfNarrowbandCaptures, NameFileComparator.NAME_COMPARATOR);
            }
            /** DEBUGGING **/
//            logService.log().info("Variable States listed below!");
//            logService.log().info("lpDesired: "+lpDesired);
//            logService.log().info("acRtiDesired: "+acRtiDesired);
//            logService.log().info("acRakingDesired: "+acRakingDesired);
//            logService.log().info("xsRtiDesired: "+xsRtiDesired);
//            logService.log().info("xsRakingDesired: "+xsRakingDesired);
//            logService.log().info("psRtiDesired: "+psRtiDesired);
//            logService.log().info("psRakingDesired: "+psRakingDesired);
//            logService.log().info("csRtiDesired: "+csRtiDesired);
//            logService.log().info("csRakingDesired: "+csRakingDesired);
//            logService.log().info("webRtiDesired: "+webRtiDesired);
//            logService.log().info("shotFileNames: "+shortName);
            /** END DEBUGGING **/
 
            if(!(acRakingDesired || acRtiDesired || xsRtiDesired || xsRakingDesired || psRtiDesired || psRakingDesired || csRtiDesired || csRakingDesired || lpDesired)){
                if(webRtiDesired){ 
                    /**
                     * If this is the only option selected, allow the user to tell us where the RTI image is for processing
                     */
                    String rtiImageToUse = "";
                    while(null == rtiImageToUse || rtiImageToUse.equals("") || !rtiImageToUse.endsWith(".rti") ){
                        OpenDialog rti_image = new OpenDialog("Locate the RTI image (.rti extension) to make into WebRTI.");
                        if(null== rti_image.getPath()){
                            //@UserHitCanvel
                            IJ.error("You must provide an RTI Image for processing to continue.  Exiting...");
                            throw new Throwable("You must provide an RTI Image for processing to continue.");
                        }
                        rtiImageToUse = rti_image.getPath();
                        if(null == rtiImageToUse || rtiImageToUse.equals("") || !rtiImageToUse.endsWith(".rti")){
                            JOptionPane.showMessageDialog(null,
                            "You must provide an RTI image.", "Try Again",
                            JOptionPane.PLAIN_MESSAGE);
                        }
                    }
                    createWebRTIFiles("", rtiImageToUse, false);
                }
                else{
                    IJ.error("You must provide at least one task.  Exiting...");
                    throw new Throwable("You must provide at least one task set to continue.");
                }
            }
            if (acRakingDesired || acRtiDesired || xsRtiDesired || xsRakingDesired || psRtiDesired || psRakingDesired || csRtiDesired || csRakingDesired){
                if (brightnessAdjustOption.equals("")) promptBrightnessAdjust(listOfHemisphereCaptures); 
            }
            if (acRakingDesired || xsRakingDesired || psRakingDesired || csRakingDesired){
                if (!static_ranking_dir.exists()) {
                    Path staticFilePath = static_ranking_dir.toPath();
                    Files.createDirectory(staticFilePath);
                    logService.log().info("A directory has been created for lossless static raking images at "+projectDirectory+"StaticRaking");
                }
                File[] listOfTransmissiveSources_dir = new File[0];
                List<String> listOfTransmissiveSources_list = new ArrayList<>();
                List<String> listOfTransmissiveSources_short = new ArrayList<>();
                String[] listOfTransmissiveSources;
                String[] listOfTransmissiveSourcePaths;
                if(transmissive_gamma_dir.exists()){
                    listOfTransmissiveSources = new String[transmissive_gamma_dir.listFiles().length];
                    listOfTransmissiveSourcePaths = new String[transmissive_gamma_dir.listFiles().length];
                    if (transmissive_gamma_dir.listFiles().length > 0){
                        listOfTransmissiveSources_dir=transmissive_gamma_dir.listFiles();
                        for (File f : listOfTransmissiveSources_dir){
                            listOfTransmissiveSources_list.add(f.toString());
                            listOfTransmissiveSources_short.add("..."+f.getName());
                        }
                        if(shortName){
                           listOfTransmissiveSources_short.toArray(listOfTransmissiveSources);
                        }
                        else{
                           listOfTransmissiveSources_list.toArray(listOfTransmissiveSources); 
                        }
                        listOfTransmissiveSources_list.toArray(listOfTransmissiveSourcePaths);
                    }
                }
                else{
                    listOfTransmissiveSources = new String[0];
                    listOfTransmissiveSourcePaths = new String[0];
                }
                Arrays.sort(listOfTransmissiveSourcePaths);
                Arrays.sort(listOfTransmissiveSources);
                logService.log().info("List of transmissive sources");
                logService.log().info(Arrays.toString(listOfTransmissiveSources));
                if(listOfTransmissiveSources.length == 1){ // no opt out of creating a transmissive static if transmissive folder is populated, but not a problem
                    transmissiveSource = listOfTransmissiveSourcePaths[0];
                } 
                else if (listOfTransmissiveSources.length > 1){
                    contentPane = new JPanel();
                    scrollGrid = new JPanel();
                    scrollGrid.setLayout(new BoxLayout(scrollGrid,BoxLayout.PAGE_AXIS));
                    contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));
                    labelPanel = new JPanel();
                    JLabel taskDirection = new JLabel("Make a selection from available transmissive sources.");
                    labelPanel.add(taskDirection);
                    contentPane.add(labelPanel);
                    /**
                     * UI for creating the radio button selections.  
                     * @see shortName
                    */
                    //There will be a button group for each narrow band capture.  We need to keep track of each group as a distinct object.
                    ButtonGroup capture_radios = new ButtonGroup();
                    for (int i=0; i<listOfTransmissiveSources.length; i++) {
                        //Create a new button group for this capture
                        JRadioButton radioOption = new JRadioButton(listOfTransmissiveSources[i]);
                        if(shortName){
                            radioOption.setToolTipText(listOfTransmissiveSources_list.get(i));
                        }
                        radioOption.setActionCommand(listOfTransmissiveSources[i]);
                        if(i==0){
                            radioOption.setSelected(true);
                        }
                        capture_radios.add(radioOption);
                        //Add the button group panel to the overall content container
                        scrollGrid.add(radioOption);                   
                    } 
                    spanel = new JScrollPane(scrollGrid);
                    spanel.setBorder(BorderFactory.createEmptyBorder());
                    spanel.setMaximumSize(bestFit);    
                    contentPane.add(spanel);
                    Object[] transmissiveSourcesBtnLabels = {"Confirm",
                        "Quit"};
                    int transmissiveResult = JOptionPane.showOptionDialog(null, contentPane, "Select Transmissive Source", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, transmissiveSourcesBtnLabels, transmissiveSourcesBtnLabels[0]);
                    if (transmissiveResult == JOptionPane.OK_OPTION){
                        //Is there an easier way to get the selected btton from a buttonGroup?
                        for (Enumeration<AbstractButton> buttons = capture_radios.getElements(); buttons.hasMoreElements();) {
                            AbstractButton button = buttons.nextElement();
                            //Loop each button and see if it is selected
                            if (button.isSelected()) {
                                //If it is selected, it will have "R", "G", "B". or "None" as its text.  Designate to the appropriate list based on this text.
                               transmissiveSource = button.getText();
                               break;
                            }
                        }
                    }
                    else{ 
                        //@userHitCancel is it OK to default to the first source?
                        transmissiveSource = listOfTransmissiveSourcePaths[0];
                        //WindowManager.closeAllWindows();
                        IJ.error("You must select one transmissive source to continue.  Exiting...");
                        throw new Throwable("You must select one transmissive source.");
                    }
                }
                else if (listOfTransmissiveSources.length == 0) {
                    transmissiveSource = "";
                }
                transmissiveSource = transmissiveSource.replace("...", transmissive_gamma_dir.toString()+File.separator); //in case of shortName
                /**
                 * UI for raking images selection window.
                 */
                contentPane = new JPanel();
                scrollGrid = new JPanel();
                scrollGrid.setLayout(new GridLayout(20, 0, 0, 0));
                contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));
                labelPanel = new JPanel();
                JLabel selectLightPositions = new JLabel("Select light positions for lossless static raking images");
                labelPanel.add(selectLightPositions);
                contentPane.add(labelPanel);
                //Make JOptionPane's resizeable when feeding in panel.
                contentPane.addHierarchyListener(new HierarchyListener() {
                    public void hierarchyChanged(HierarchyEvent e) {
                     //when the hierarchy changes get the ancestor for the message
                    Window window = SwingUtilities.getWindowAncestor(contentPane);
                     //check to see if the ancestor is an instance of Dialog and isn't resizable
                        if (window instanceof Dialog) {
                            Dialog dialog = (Dialog)window;
                            if (!dialog.isResizable()) {
                            //set resizable to true
                                dialog.setResizable(true);
                            }
                        }
                    }
                }); 
                
                /**
                 * UI for creating the checkbox selections.  
                 * @see shortName
                */
                JCheckBox[] positions = new JCheckBox[listOfHemisphereCaptures.length];
                for(int l=0; l<listOfHemisphereCaptures.length; l++){
                    JCheckBox ch = null;
                    if(shortName){
                        ch = new JCheckBox("..."+listOfHemisphereCaptures[l].getName());
                        ch.setToolTipText(listOfHemisphereCaptures[l].toString());
                    }
                    else{
                        ch = new JCheckBox(listOfHemisphereCaptures[l].toString());
                    }
                    positions[l] = ch;
                    scrollGrid.add(ch);
                }
                spanel = new JScrollPane(scrollGrid);
                spanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                spanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                spanel.setPreferredSize(preferredSize);     
                contentPane.add(spanel);
                /**
                 * Gather and process user selected raking images
                */
                boolean atLeastOne = false;
                Object[] btns = {"Confirm",
                        "Quit"};
                while(!atLeastOne){
                    int lpResult = JOptionPane.showOptionDialog(null, contentPane, "Select Light Positions", JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE, null, btns, btns[0]);
                    if (lpResult == JOptionPane.OK_OPTION){
                        for(JCheckBox check : positions){
                            listOfRakingDirections.add(check.isSelected());
                            if(check.isSelected()){
                                atLeastOne = true;
                            }
                        }
                    }
                    else {
                        //@userHitCancel
                        //How should i handle (@userHitCancel).  Make them all false?
                        /*
                        listOfRakingDirections = new ArrayList<>();
                        for(JCheckBox check : positions){
                            listOfRakingDirections.add(Boolean.FALSE);
                        }
                        */
                        //WindowManager.closeAllWindows();
                        IJ.error("You must make at least one selection to continue!  Exiting...");
                        throw new Throwable("You must make at least one selection to continue!");
                    }
                    if(!atLeastOne){
                        JOptionPane.showMessageDialog(null,
                            "You select at least one.", "Try Again",
                            JOptionPane.PLAIN_MESSAGE);
                    }
                }
            }
            else { //We already have the list initiated, fill it with false.
                listOfRakingDirections = new ArrayList<>();
                while(listOfRakingDirections.size() < listOfHemisphereCaptures.length) listOfRakingDirections.add(Boolean.FALSE);
                logService.log().info("Raking is not desired.");
            }
            logService.log().info("Gathered "+listOfRakingDirections.size()+" raking image selections.");
            logService.log().info(listOfRakingDirections);
            if (xsRtiDesired || xsRakingDesired){ // only interaction here, processing later
		/**
                 * Create a dialog suggesting and confirming which narrowband captures to use for R,G,and B
                 */
                String[] rgbnOptions = new String[4];
                rgbnOptions[0] = "R";
                rgbnOptions[1] = "G";
                rgbnOptions[2] = "B";
                rgbnOptions[3] = "none";
                String rangeChoice = "";
                boolean atLeastOneR = false;
                boolean atLeastOneG = false;
                boolean atLeastOneB = false;
                while(listOfNarrowbandCaptures.length<9){
                    contentPane = new JPanel();
                    contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));
                    labelPanel = new JPanel();
                    JLabel directions = new JLabel("You must have 9 or more narrow band captures for Extended Spectrum.  Please add them at this time or quit to add them later.");
                    labelPanel.add(directions);
                    contentPane.add(labelPanel);
                    Object[] btns = {"Confirm",
                        "Quit"};
                    int datasetResult = JOptionPane.showOptionDialog(null, contentPane, "Source Dataset Too Small", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, btns, btns[0]);
                    if (datasetResult == JOptionPane.OK_OPTION){
                        listOfNarrowbandCaptures = narrow_band_dir.listFiles();
                        Arrays.sort(listOfNarrowbandCaptures, NameFileComparator.NAME_COMPARATOR);
                    }
                    else {
                         //@userHitCancel
                        //WindowManager.closeAllWindows();
                        IJ.error("You must have 9 or more narrow band captures for Extended Spectrum.  Exiting...");
                        throw new Throwable("You must have 9 or more narrow band captures for Extended Spectrum.");
                    }
                    if (listOfNarrowbandCaptures.length<9) { 
                        JOptionPane.showMessageDialog(null,
                        "You must have 9 or more narrow band captures for Extended Spectrum.", "Try Again",
                        JOptionPane.PLAIN_MESSAGE);
                    }
                }
                if (listOfNarrowbandCaptures.length<9) { 
                    //WindowManager.closeAllWindows();
                    IJ.error("You must have 9 or more narrow band captures for Extended Spectrum!  Exiting...");
                    throw new Throwable("You must have 9 or more narrow band captures for Extended Spectrum!");
                }
                /**
                 * UI for custom visible RGB range assignment window.
                */
                contentPane = new JPanel();
                JPanel scrollPanel = new JPanel();
                scrollPanel.setLayout(new GridLayout(0, 1, 1, 0));
                contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));
                labelPanel = new JPanel();
                labelPanel2 = new JPanel();
                JLabel assignNarrowband = new JLabel("Assign each narrowband capture to the visible range of R, G, B, or none.");
                JLabel assignNarrowband2 = new JLabel("You must provide at least one selection for each visible range R, G and B.");
                assignNarrowband2.setBorder(new EmptyBorder(0,0,15,0));
                labelPanel.add(assignNarrowband);
                labelPanel2.add(assignNarrowband2);
                contentPane.add(labelPanel);        
                contentPane.add(labelPanel2);
                //Make JOptionPane's resizable when feeding in panel.
                contentPane.addHierarchyListener(new HierarchyListener() {
                    public void hierarchyChanged(HierarchyEvent e) {
                     //when the hierarchy changes get the ancestor for the message
                     Window window = SwingUtilities.getWindowAncestor(contentPane);
                     //check to see if the ancestor is an instance of Dialog and isn't resizable
                     if (window instanceof Dialog) {
                      Dialog dialog = (Dialog)window;
                      if (!dialog.isResizable()) {
                       //set resizable to true
                       dialog.setResizable(true);
                      }
                     }
                    }
                });
                ButtonGroup[] bgroups = new ButtonGroup[listOfNarrowbandCaptures.length];
                //There will be a button group for each narrow band capture.  We need to keep track of each group as a distinct object.
                for (int i=0; i<listOfNarrowbandCaptures.length; i++) {
                    //Create a new button group for this capture
                    ButtonGroup capture_radios = new ButtonGroup();
                    JRadioButton radioOptionR = new JRadioButton("R");
                    radioOptionR.setActionCommand("R");
                    JRadioButton radioOptionG = new JRadioButton("G");
                    radioOptionR.setActionCommand("G");
                    JRadioButton radioOptionB = new JRadioButton("B");
                    radioOptionR.setActionCommand("B");
                    JRadioButton radioOptionNone = new JRadioButton("None");
                    radioOptionR.setActionCommand("None");
                    capture_radios.add(radioOptionR);
                    radioOptionR.setBorder(new EmptyBorder(0,10,0,20));
                    radioOptionG.setBorder(new EmptyBorder(0,0,0,20));
                    radioOptionB.setBorder(new EmptyBorder(0,0,0,20));
                    radioOptionNone.setBorder(new EmptyBorder(0,0,0,0));
                    capture_radios.add(radioOptionG);
                    capture_radios.add(radioOptionB);
                    capture_radios.add(radioOptionNone);
                    //add this new group to the array of groups
                    bgroups[i] = capture_radios;
                    //Auto-select the correct button
                    String narrowCapture = "";
                    if(shortName){
                        narrowCapture = "..."+listOfNarrowbandCaptures[i].getName();
                    }
                    else{
                        narrowCapture = listOfNarrowbandCaptures[i].toString();
                    }
                    JLabel jlabel = new JLabel(narrowCapture);
                    jlabel.setToolTipText(listOfNarrowbandCaptures[i].toString());
                    jlabel.setBorder(new EmptyBorder(0,10,0,0));
                    scrollPanel.add(jlabel);
                    JPanel contentGroup = new JPanel();
                    contentGroup.setLayout(new BoxLayout(contentGroup,BoxLayout.LINE_AXIS)); // Want radio options in one row, should only need a single column.
                    //Add the button group it its own panel
                    contentGroup.setName(narrowCapture);
                    contentGroup.add(radioOptionR);
                    contentGroup.add(radioOptionG);
                    contentGroup.add(radioOptionB);
                    contentGroup.add(radioOptionNone);
                    jlabel.setLabelFor(contentGroup);
                    /**
                       * Since it is conventional to capture in a sequence of short to long wavelengths, it is helpful to default the first third of the files 
                       * in "narrowband captures" to blue, the middle third to green, and final third to red. 
                    */
                    float defaultFraction;
                    float ind = (float) (i+1.0);
                    float len = (float) listOfNarrowbandCaptures.length;
                    defaultFraction = ind/len;
                    if (defaultFraction < 0.34){
                        //defaultRange = "B";
                        radioOptionB.setSelected(true);
                    }
                    else if(defaultFraction > 0.67){
                        //defaultRange = "R";
                        radioOptionR.setSelected(true);
                    }
                    else{
                        //defaultRange = "G";
                        radioOptionG.setSelected(true);
                    }
                    //Add the button group panel to the overall content container
                    scrollPanel.add(contentGroup);                   
		} 
                spanel = new JScrollPane(scrollPanel);
                spanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                Dimension prefSize = new Dimension(800, preferredSize.height);
                spanel.setPreferredSize(prefSize);      
                contentPane.add(spanel);
                /**
                 * Gather user visible range selections.
                 */
                //Make sure this doesn't show infinite confirm dialogs.  
                Object[] btns = {"Apply",
                    "Quit"};
                while(!(atLeastOneR && atLeastOneG && atLeastOneB)){
                    int capturesResult = JOptionPane.showOptionDialog(null, contentPane, "Assign Narrowband Captures", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, btns, btns[0]);
                    if ( capturesResult==JOptionPane.OK_OPTION) {
                        for(int d=0; d<bgroups.length; d++){
                            //Go over each button group (one for each narrow band capture, in order)
                            ButtonGroup grabSelection = bgroups[d]; 
                            //Get each button out of the group
                            for (Enumeration<AbstractButton> buttons = grabSelection.getElements(); buttons.hasMoreElements();) {
                                AbstractButton button = buttons.nextElement();
                                //Loop each button and see if it is selected
                                if (button.isSelected()) {
                                    //If it is selected, it will have "R", "G", "B". or "None" as its text.  Designate to the appropriate list based on this text.
                                    rangeChoice = button.getText();
                                    if (rangeChoice.equals("R")) {
                                        redNarrowbands_list.add(listOfNarrowbandCaptures[d].getName());
                                        atLeastOneR = true;
                                    } 
                                    else if (rangeChoice.equals("G")) {
                                        greenNarrowbands_list.add(listOfNarrowbandCaptures[d].getName());
                                        atLeastOneG = true;
                                    } 
                                    else if (rangeChoice.equals("B")) {
                                        blueNarrowbands_list.add(listOfNarrowbandCaptures[d].getName());
                                        atLeastOneB = true;
                                    }
                                }
                            }
                        }
                        if(!(atLeastOneR && atLeastOneG && atLeastOneB)){
                            JOptionPane.showMessageDialog(null,
                            "You must designate at least one capture to each visible range of R, G, B", "Try Again",
                            JOptionPane.PLAIN_MESSAGE);
                        }
                    } 
                    else {
                        //Pane was cancelled or closed. (@userHitCancel)
                        IJ.error("You must designate the captures to the visible range of R, G, B, or none to continue!  Exiting...");
                        throw new Throwable("You must designate the captures to the visible range of R, G, B, or none to continue!");
                    }
                }
                if(!(atLeastOneR && atLeastOneG && atLeastOneB)){
                    String whichOnes = "You did not provide a selection of the visible range(s) ";
                    if(!atLeastOneR){
                        whichOnes +="R ";
                    }
                    if(!atLeastOneG){
                        whichOnes +="G ";
                    }
                    if(!atLeastOneB){
                        whichOnes +="B";
                    }
                    //WindowManager.closeAllWindows();
                    IJ.error("You must designate at least one capture to each of the visible ranges (R,G,B). "+whichOnes+" --- Exiting...");
                    throw new Throwable("You must designate at least one capture to each of the visible ranges (R,G,B). "+whichOnes);
                }
                logService.log().info("We should have red, green and blue narrow bands");
                redNarrowbands = redNarrowbands_list.toArray();
                greenNarrowbands = greenNarrowbands_list.toArray();
                blueNarrowbands = blueNarrowbands_list.toArray();
                logService.log().info(Arrays.toString(redNarrowbands));
                logService.log().info(Arrays.toString(greenNarrowbands));
                logService.log().info(Arrays.toString(blueNarrowbands));
		if (pcaHeight < 100){ 
                    File narrowbandNoGamma = new File(listOfNarrowbandCaptures[Math.round(listOfNarrowbandCaptures.length/2)].toString()); 
                    imp = opener.openImage( narrowbandNoGamma.toString() );
                    imp.setTitle("Preview");
                    imp.show();
                    while(imp.getRoi() == null){
                        dWait = new WaitForUserDialog("Select area", "Draw a rectangle containing the colors of interest for PCA then click OK\n(hint: limit to object or smaller).  Press 'Esc' to quit.");
                        dWait.show();
                        if(dWait.escPressed()){
                            //@userHitCancel
                            IJ.error("You must draw a rectangle to continue!  Exiting...");
                            throw new Throwable("You must draw a rectangle to continue!");
                        }
                        if(imp.getRoi() == null){
                            JOptionPane.showMessageDialog(null,
                            "You must draw a rectangle.", "Try Again",
                            JOptionPane.PLAIN_MESSAGE);
                        }
                    }
                    if(imp.getRoi() == null){
                        //@userHitCancel
                        IJ.error("You must draw a rectangle to continue!  Exiting...");
                        throw new Throwable("You must draw a rectangle to continue!");
                    }
                    bounds = imp.getRoi().getBounds();
                    pcaX = bounds.x;
                    pcaY = bounds.y;
                    pcaHeight = bounds.height;
                    pcaWidth = bounds.width;
                    imp.close();
		}
            }
            /**
             * @see only interaction here, processing later 
             */
            if (psRtiDesired || psRakingDesired){
                //identify 2 source images for pca pseudocolor
                File listOfPseudoColorSources_dir = new File(projectDirectory+"PCA"+File.separator);
                File[] listOfPseudoColorSources = new File[0];
                if(listOfPseudoColorSources_dir.exists()){
                    listOfPseudoColorSources = listOfPseudoColorSources_dir.listFiles();
                }
                while(listOfNarrowbandCaptures.length<9){
                    contentPane = new JPanel();
                    contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));
                    labelPanel = new JPanel();
                    JLabel directions = new JLabel("You must have 9 or more narrow band captures for Pseudocolor.  Please add them at this time or quit to add them later.");
                    labelPanel.add(directions);
                    contentPane.add(labelPanel);
                    Object[] btns = {"Confirm",
                        "Quit"};
                    int datasetResult = JOptionPane.showOptionDialog(null, contentPane, "Source Dataset Too Small", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, btns, btns[0]);
                    if (datasetResult == JOptionPane.OK_OPTION){
                        listOfNarrowbandCaptures = narrow_band_dir.listFiles();
                        Arrays.sort(listOfNarrowbandCaptures, NameFileComparator.NAME_COMPARATOR);
                    }
                    else {
                        //@userHitCancel
                        //WindowManager.closeAllWindows();
                        IJ.error("You must have 9 or more narrow band captures for Pseudocolor.  Exiting...");
                        throw new Throwable("You must have 9 or more narrow band captures for Extended Spectrum.");
                    }
                    if (listOfNarrowbandCaptures.length<9) { 
                        JOptionPane.showMessageDialog(null,
                        "You must have 9 or more narrow band captures for Pseudocolor.", "Try Again",
                        JOptionPane.PLAIN_MESSAGE);
                    }
                }
                if (listOfNarrowbandCaptures.length<9) { 
                    //WindowManager.closeAllWindows();
                    IJ.error("You must have 9 or more narrow band captures for Pseudocolor!  Exiting...");
                    throw new Throwable("You must have 9 or more narrow band captures for Extended Spectrum!");
                }
                
                int defaultPca = 2;
                if (listOfPseudoColorSources.length > 1) defaultPca = 2 ;
                else defaultPca = 1;
                String[] listOfPcaMethods = new String[3];
                listOfPcaMethods[0]="Generate and select using defaults";
                listOfPcaMethods[1]="Generate and manually select two";
                listOfPcaMethods[2]="Open pregenerated images";
                contentPane = new JPanel();
                scrollGrid = new JPanel();
                scrollGrid.setLayout(new BoxLayout(scrollGrid,BoxLayout.PAGE_AXIS));
                contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));
                labelPanel = new JPanel();
                JLabel directions = new JLabel("PseudoColor images require two source images (typically principal component images).");
                labelPanel.add(directions);
                contentPane.add(labelPanel);
                /**
                 * UI for creating the radio selections.  
                 * @see shortName
                */
                //There will be a button group for each narrow band capture.  We need to keep track of each group as a distinct object.
                ButtonGroup capture_radios = new ButtonGroup();
                for (int i=0; i<listOfPcaMethods.length; i++) {
                    //Create a new button group for this capture
                    JRadioButton radioOption = new JRadioButton(listOfPcaMethods[i]);
                    radioOption.setActionCommand(listOfPcaMethods[i]);
                    if(i==defaultPca){
                        radioOption.setSelected(true);
                    }
                    capture_radios.add(radioOption);
                    //Add the button group panel to the overall content container
                    scrollGrid.add(radioOption);                   
                } 
                spanel = new JScrollPane(scrollGrid);
                spanel.setBorder(BorderFactory.createEmptyBorder());
                spanel.setMaximumSize(bestFit);    
                contentPane.add(spanel);
                Object[] psuedoMethodBtnLabels = {"Confirm",
                    "Quit"};
                int pseudoMethodResult = JOptionPane.showOptionDialog(null, contentPane, "Select Method for Pseudocolor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, psuedoMethodBtnLabels, psuedoMethodBtnLabels[0]);
                if (pseudoMethodResult == JOptionPane.OK_OPTION){
                    //Is there an easier way to get the selected btton from a buttonGroup?
                    for (Enumeration<AbstractButton> buttons = capture_radios.getElements(); buttons.hasMoreElements();) {
                        AbstractButton button = buttons.nextElement();
                        //Loop each button and see if it is selected
                        if (button.isSelected()) {
                           pcaMethod = button.getText();
                           break;
                        }
                    }
                }
                else{ 
                    //@userHitCancel is it OK to default to the first source?
                    pcaMethod = listOfPcaMethods[0];
                    IJ.error("You must select one Psuedocolor method to continue.  Exiting...");
                    throw new Throwable("You must select one Psuedocolor method.");
                }
                logService.log().info("Got PCA method: "+pcaMethod);
                if (pcaHeight < 100) { 
                   //It should already have been forced that there are at least 9 listOfNarrobandCapture images
                    imp = opener.openImage( listOfNarrowbandCaptures[Math.round(listOfNarrowbandCaptures.length/2)].toString());
                    imp.setTitle("Preview");
                    imp.show();
                    while(imp.getRoi() == null){
                        dWait = new WaitForUserDialog("Select area", "Draw a rectangle containing the colors of interest for PCA then click OK\n(hint: limit to object or smaller).  Press 'Esc' to quit.");
                        dWait.show();
                        if(dWait.escPressed()){
                            //@userHitCancel
                            IJ.error("You must draw a rectangle to continue!  Exiting...");
                            throw new Throwable("You must draw a rectangle to continue!");
                        }
                        if(imp.getRoi() == null){
                            JOptionPane.showMessageDialog(null,
                            "You must draw a rectangle.", "Try Again",
                            JOptionPane.PLAIN_MESSAGE);
                        }
                    }
                    bounds = WindowManager.getImage("Preview").getRoi().getBounds();
                    pcaX = bounds.x;
                    pcaY = bounds.y;
                    pcaHeight = bounds.height;
                    pcaWidth = bounds.width;
                    imp.close();
                }
            }
            if (csRtiDesired || csRakingDesired) { //interaction phase jhg 
                while(null == csSource || csSource.equals("")){
                    OpenDialog csSourceDialog = new OpenDialog("Choose a Source for Custom Process");
                    if(null == csSourceDialog.getPath()){
                        //@userHitCanvel
                        IJ.error("You must provide a custom source to continue.  Exiting...");
                        throw new Throwable("You must provide a custom source to continue.");
                    }
                    csSource = csSourceDialog.getPath();
                    if(null == csSource || csSource.equals("")){
                        JOptionPane.showMessageDialog(null,
                        "You must provide a custom source.", "Try Again",
                        JOptionPane.PLAIN_MESSAGE);
                    }
                }
                logService.log().info("Should have custom source");
                logService.log().info(csSource);
            }
            /**
             *@see create base lp file
             */
            if (lpDesired) {
                imp = opener.openImage(listOfHemisphereCaptures[20].toString());
                imp.setTitle("Preview");
                imp.show();
                while(imp.getRoi() == null){
                    dWait = new WaitForUserDialog("Select ROI", "Draw a rectangle loosely around a reflective hemisphere then press Ok.  Press 'Esc' to quit.");
                    dWait.show();
                    if(dWait.escPressed()){
                        //@userHitCancel
                        IJ.error("You must draw a rectangle to continue!  Exiting...");
                        throw new Throwable("You must draw a rectangle to continue!");
                    }
                    if(imp.getRoi() == null){
                        JOptionPane.showMessageDialog(null,
                        "You must draw a rectangle.", "Try Again",
                        JOptionPane.PLAIN_MESSAGE);
                    }
                }
                bounds = imp.getRoi().getBounds();
                imp.close();
                for(int i=0;i<listOfHemisphereCaptures.length;i++) {
                    if (listOfHemisphereCaptures[i].toString().endsWith("tiff") || listOfHemisphereCaptures[i].toString().endsWith("tif")) {
                        logService.log().info("On hem capture index "+i);
                        imp=opener.openImage(listOfHemisphereCaptures[i].toString());
                        String imageName = listOfHemisphereCaptures[i].getName();
                        int extensionIndex = listOfHemisphereCaptures[i].getName().indexOf(".");
                        if (extensionIndex != -1)
                        {
                            filePath = projectDirectory+"LightPositionData"+File.separator+"jpeg-exports"+File.separator+listOfHemisphereCaptures[i].getName().substring(0, extensionIndex);
                        }
                        else{
                            IJ.error("A file in your hemisphere folder does not have an extension.  Please review around "+imageName+"  ---  Exiting...");
                            throw new Throwable("A file in your hemisphere folder does not have an extension.  Please review around "+imageName); 
                        }
                        imp.setRoi(bounds);
                        IJ.run(imp, "Crop", ""); //Crops the image or stack based on the current rectangular selection.
                        File jpegExportsFile = new File(projectDirectory+"LightPositionData"+File.separator+"jpeg-exports"+File.separator);
                        if (!light_position_dir.exists()) Files.createDirectory(light_position_dir.toPath());
                        if (!jpegExportsFile.exists()) Files.createDirectory(jpegExportsFile.toPath());
                        //Do we need to tell the users we created these directories?
                        IJ.saveAs(imp, "jpeg", filePath+".jpg"); //Use this submenu to save the active image in TIFF, GIF, JPEG, or format
                        imp.close();
                    }
                }
                IJ.showMessageWithCancel("Use RTI Builder to Create LP File","Please use RTI Builder to create an LP file based on the reflective hemisphere detail images in\n"+projectDirectory+"LightPositionData"+File.separator+"\nPress cancel to discontinue Spectral RTI Toolkit or Ok to complete other selected tasks.");
            }
            if(acRtiDesired || acRakingDesired){ //Gather accurate color info
                listOfAccurateColorSources = accurate_color_dir.listFiles();
                ArrayList<String>  listOfAccurateColorSources_list = new ArrayList<String>();
                ArrayList<String>  listOfAccurateColorSources_short = new ArrayList<String>();
                while(listOfAccurateColorSources.length<1){
                    contentPane = new JPanel();
                    contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));
                    labelPanel = new JPanel();
                    JLabel directions = new JLabel("You must have at least 1 accurate color image for the Accurate Color process.  Please add them at this time or quit to add them later.");
                    labelPanel.add(directions);
                    contentPane.add(labelPanel);
                    Object[] btns = {"Confirm",
                        "Quit"};
                    int datasetReult = JOptionPane.showOptionDialog(null, contentPane, "Source Dataset Too Small", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, btns, btns[0]);
                    if (datasetReult == JOptionPane.OK_OPTION){
                        listOfAccurateColorSources = accurate_color_dir.listFiles();
                    }
                    else {
                        //@userHitCancel
                        //WindowManager.closeAllWindows();
                        IJ.error("Need at least one color image file in "+projectDirectory+"AccurateColor"+File.separator+"  ---  Exiting...");
                        throw new Throwable("Need at least one color image file in "+projectDirectory+"AccurateColor"+File.separator);
                    }
                    if(listOfAccurateColorSources.length<1){
                        JOptionPane.showMessageDialog(null,
                        "Need at least one color image file in "+projectDirectory+"AccurateColor"+File.separator, "Try Again",
                        JOptionPane.PLAIN_MESSAGE);
                    }
                }
                if (listOfAccurateColorSources.length<1){
                    IJ.error("Need at least one color image file in "+projectDirectory+"AccurateColor"+File.separator+"  ---  Exiting...");
                    throw new Throwable("Need at least one color image file in "+projectDirectory+"AccurateColor"+File.separator);
                }
                String[] listOfAccurateColorSources_string = new String[listOfAccurateColorSources.length];
                for (File f : listOfAccurateColorSources) {
                   listOfAccurateColorSources_list.add(f.toString());
                   listOfAccurateColorSources_short.add("..."+f.getName());
                   //elipses makes a weird box pop up next to the radio button...
                }
                listOfAccurateColorSources_list.toArray(listOfAccurateColorSources_string);
		if (listOfAccurateColorSources.length == 1) { //There was only one source, so auto select it
                    accurateColorSource = listOfAccurateColorSources[0];
		} 
                else { //There were multiple sources, let the user pick the one they want to use.
                    while(null == accurateColorSource || !accurateColorSource.exists()) {
                        contentPane = new JPanel();
                        scrollGrid = new JPanel();
                        scrollGrid.setLayout(new BoxLayout(scrollGrid,BoxLayout.PAGE_AXIS));
                        contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));
                        labelPanel = new JPanel();
                        JLabel directions = new JLabel("Choose the Accurate Color source to use.");
                        labelPanel.add(directions);
                        contentPane.add(labelPanel);
                        /**
                         * UI for creating the checkbox selections.  
                         * @see shortName
                        */
                        ButtonGroup capture_radios = new ButtonGroup();
                        for (int i=0; i<listOfAccurateColorSources.length; i++) {
                            JRadioButton radioOption = null;
                            if(shortName){
                               radioOption = new JRadioButton(listOfAccurateColorSources_short.get(i)); 
                               radioOption.setToolTipText(listOfAccurateColorSources_string[i]);
                            }
                            else{
                               radioOption =  new JRadioButton(listOfAccurateColorSources_string[i]);
                            }
                            radioOption.setActionCommand(listOfAccurateColorSources_string[i]);
                            if(i==0){
                                radioOption.setSelected(true);
                            }
                            capture_radios.add(radioOption);
                            //Add the button group panel to the overall content container
                            scrollGrid.add(radioOption);                   
                        } 
                        spanel = new JScrollPane(scrollGrid);
                        spanel.setBorder(BorderFactory.createEmptyBorder());
                        spanel.setMaximumSize(bestFit);    
                        contentPane.add(spanel);
                        Object[] btns = {"Confirm",
                            "Quit"};
                        int colorSourceResult = JOptionPane.showOptionDialog(null, contentPane, "Select Color Source", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, btns, btns[0]);
                        if (colorSourceResult == JOptionPane.OK_OPTION){
                            //Is there an easier way to get the selected btton from a buttonGroup?
                            for (Enumeration<AbstractButton> buttons = capture_radios.getElements(); buttons.hasMoreElements();) {
                                AbstractButton button = buttons.nextElement();
                                //Loop each button and see if it is selected
                                String sourcePath = "";
                                if (button.isSelected()) {
                                    sourcePath = button.getText();
                                    sourcePath = sourcePath.replace("...", accurate_color_dir.toString()+File.separator); //in case of shortName
                                    accurateColorSource = new File(sourcePath);
                                    break;
                                }
                            }
                        }
                        else{ 
                            //@userHitCancel is it OK to default to the first source?
                            IJ.error("You must provide a color source to continue!  Exiting...");
                            throw new Throwable("You must provide a color source to continue!");
                        }
                        if(null == accurateColorSource || !accurateColorSource.exists()){
                            JOptionPane.showMessageDialog(null,
                            "You must provide an Accurate Color source", "Try Again",
                            JOptionPane.PLAIN_MESSAGE);
                        }
                    }
		}
            }
            //Think about acRtiDesired || acRakingDesired so we can use just one loop over the hemishpere captures for all processing.  
            if (acRtiDesired) {
		/**
                 *create series of images with luminance from hemisphere captures and chrominance from color image
                 */
                File accurateRTI = new File(projectDirectory+"AccurateColorRTI"+File.separator);
		if (!accurateRTI.exists()) {
                    Files.createDirectory(accurateRTI.toPath());
                    logService.log().info("A directory has been created for Accurate Color RTI at "+projectDirectory+"AccurateColorRTI");
		}
		//integration
                imp = opener.openImage( accurateColorSource.toString() );
                //imglib2_img = ImagePlusAdapter.wrap( imp );
		if (imp.getBitDepth() == 8) {
                    IJ.run(imp,"RGB Color","");
                    imp.close();
                    imp = WindowManager.getCurrentImage();
                    imp.setTitle("RGBtiff");
		}
		IJ.run(imp, "RGB to YCbCr stack", "");
		IJ.run("Stack to Images");
                //Stack to Images will automatically cause windows to open in the interim.  For processing, it is best if they are hidden.
                WindowManager.getImage("Y").close();
                ImagePlus cb = WindowManager.getImage("Cb");
                ImagePlus cr = WindowManager.getImage("Cr");
                cb.hide();
                cr.hide();
                ImagePlus keptPieces = con.concatenate(cb, cr, true);
                imp.close();
		/**
                 *Luminance from hemisphere captures
                 */
		for(int i=0;i<listOfHemisphereCaptures.length;i++) {
                    if (listOfHemisphereCaptures[i].toString().endsWith("tiff") || listOfHemisphereCaptures[i].toString().endsWith("tif")) { 
                        logService.log().info("On hem capture index "+i);
                        //better to trim list at the beginning so that array.length can be used in lp file
                        imp = opener.openImage( listOfHemisphereCaptures[i].toString() );
                        imp.setTitle("Luminance");
                        // it would be better to crop early in the process, especially before reducing to 8-bit and jpeg compression
                        // normalize
                        if (brightnessAdjustApply.equals("RTI images also")) {
                            if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                                imp.setRoi(normX, normY, normWidth, normHeight);
                                IJ.run(imp, "Enhance Contrast...", "saturated=0.4");//Enhances image contrast by using either histogram stretching or histogram equalization.  Affects entire stack
                            } 
                            else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                                IJ.run(imp,"Multiply...", "value="+normalizationFixedValue+"");
                            }
                        }
                        IJ.run(imp,"8-bit", ""); //Applies the current display range mapping function to the pixel data. If there is a selection, only pixels within the selection are modified
                        ImagePlus stack = con.concatenate(imp, keptPieces, true);
                        stack.setTitle("YCC");
                        IJ.run(stack, "YCbCr stack to RGB", ""); //Converts a two or three slice stack into an RGB image, assuming that the slices are in R, G, B order. The stack must be 8-bit or 16-bit grayscale
                        //Save as jpeg
                        ImagePlus stackRGB = WindowManager.getImage("YCC - RGB");
                        stackRGB.hide();
                        int extensionIndex = listOfHemisphereCaptures[i].getName().indexOf(".");
                        if (extensionIndex != -1)
                        {
                            String simpleName1 = listOfHemisphereCaptures[i].getName().substring(0, extensionIndex); //.toString().substring(0, extensionIndex)
                            filePath = projectDirectory+"AccurateColorRTI"+File.separator+"AccurateColor_"+simpleName1;
                            simpleImageName = "AccurateColor_"+simpleName1;
                        }
                        else{
                            IJ.error("A file in your hemisphere folder does not have an extension.  Please review around "+listOfHemisphereCaptures[i].getName()+"  ---  Exiting...");
                            throw new Throwable("A file in your hemisphere folder does not have an extension.  Please review around "+listOfHemisphereCaptures[i].getName()); 
                        }
                        noClobber(filePath+".jpg");
                        logService.log().info("Saving ACRTI source image "+filePath+".jpg");
                        IJ.saveAs(stackRGB, "jpeg", filePath+".jpg");
                        stackRGB.close();
                        imp.changes=false;
                        imp.close();   
                        stack.close();
                    }
		}
                cb.close();
                cr.close();
                keptPieces.close();
                createLpFile("AccurateColor", projectDirectory); 
                //WindowManager.closeAllWindows();
		runFitter("AccurateColor");
            }
            if (acRakingDesired) {
                imp = opener.openImage( accurateColorSource.toString() ); 
                imp.setTitle("RGBtiff");
		if (imp.getBitDepth() == 8) {
                    IJ.run(imp,"RGB Color","");
		}
		/**
                 * create accurate color static diffuse
                */
		noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_00"+".tiff");
		IJ.save(imp, projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_00"+".tiff");
		createJp2(projectName+"_Ac_00", projectDirectory);
                toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_00"+".tiff");
                Files.deleteIfExists(toDelete.toPath());
		IJ.run(imp, "RGB to YCbCr stack", "");
		IJ.run("Stack to Images"); //Converts the slices in the current stack to separate image windows.
                WindowManager.getImage("Y").close();
                imp.changes = false;
                imp.close();
                ImagePlus cb = WindowManager.getImage("Cb");
                ImagePlus cr = WindowManager.getImage("Cr");
                cb.hide();
                cr.hide();
		//Luminance from transmissive
                logService.log().info("What is transmissive source "+transmissiveSource);
		if (!transmissiveSource.equals("")){
                    imp = opener.openImage( transmissiveSource ); 
                    imp.setTitle("TransmissiveLuminance");
                    IJ.run(imp, "8-bit", "");
                    ImagePlus keptPieces = con.concatenate(cb, cr, true);
                    ImagePlus stack = con.concatenate(imp, keptPieces, true);
                    stack.setTitle("YCC");
                    stack.hide();
                    imp.changes=false;
                    imp.close();
                    IJ.run(stack, "YCbCr stack to RGB", "");
                    ImagePlus stackRGB = WindowManager.getImage("YCC - RGB");
                    stackRGB.hide();
                    noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_Tx.tiff");
                    logService.log().info("Save ACRaking source image "+projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_Tx.tiff");
                    IJ.save(stackRGB,projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_Tx.tiff");
                    createJp2(projectName+"_Ac_Tx", projectDirectory);
                    toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_Tx.tiff");
                    logService.log().info("Remove ACRaking source image "+projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_Tx.tiff");
                    Files.deleteIfExists(toDelete.toPath());   
                    stack.close();
                    stackRGB.close();
		}
		//Luminance from hemisphere captures
		for(int i=0;i<listOfHemisphereCaptures.length;i++) {
                    if (listOfHemisphereCaptures[i].toString().endsWith("tiff") || listOfHemisphereCaptures[i].toString().endsWith("tif")){ //better to trim list at the beginning so that array.length can be used in lp file
                        logService.log().info("On hem capture index "+i);
                        if (listOfRakingDirections.get(i)) {
                            imp = opener.openImage( listOfHemisphereCaptures[i].toString() );
                            imp.setTitle("Luminance");
                            //imglib2_img = ImagePlusAdapter.wrap(imp);
                            if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                                imp.setRoi(normX, normY, normWidth, normHeight);
                                IJ.run(imp, "Enhance Contrast...", "saturated=0.4");
                            } else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                                IJ.run(imp, "Multiply...", "value="+normalizationFixedValue+""); //Multiplies the image or selection by the specified real constant. With 8-bit images, results greater than 255 are set to 255
                            }
                            IJ.run(imp, "8-bit", "");
                            ImagePlus keptPieces = con.concatenate(cb, cr, true);
                            cb.close();
                            cr.close(); 
                            ImagePlus stack = con.concatenate(imp, keptPieces, true);
                            stack.setTitle("YCC");
                            stack.hide();
                            IJ.run(stack, "YCbCr stack to RGB", "");
                            ImagePlus stackRGB = WindowManager.getImage("YCC - RGB");
                            stackRGB.hide();
                            imp.changes = false;
                            imp.close();
                            positionNumber = IJ.pad(i+1, 2).toString();
                            noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_"+positionNumber+".tiff");
                            logService.log().info("Save ACRaking source image "+projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_"+positionNumber+".tiff");
                            IJ.save(stackRGB, projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_"+positionNumber+".tiff");
                            createJp2(projectName+"_Ac_"+positionNumber, projectDirectory);
                            toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_"+positionNumber+".tiff");
                            logService.log().info("Remove ACRaking source image "+projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_"+positionNumber+".tiff");
                            Files.deleteIfExists(toDelete.toPath());   
                            stack.close();
                            stackRGB.close();
                        }
                    }
		}
                cb.close();
                cr.close();
            }
            IJ.run("Collect Garbage");
            if (xsRtiDesired || xsRakingDesired) {
		//Red
		String redStringList = redNarrowbands[0].toString();  //might be an array to string function somewhere to do this more elegantly
		for (int i=1;i<redNarrowbands.length;i++) {
                    redStringList = redStringList+"|"+redNarrowbands[i].toString();
		}
		IJ.run("Image Sequence...", "open="+projectDirectory+"Captures-Narrowband-NoGamma"+File.separator+" file=("+redStringList+") sort"); //Opens a series of images in a chosen folder as a stack. Images may have different dimensions and can be of any format supported by ImageJ              
                ImagePlus redStacker = WindowManager.getImage("Captures-Narrowband-NoGamma");
                redStacker.setTitle("RedStack");
                logService.log().info("There are "+redStacker.getStackSize()+" images in the RED STACK");
                redStacker.hide();
                if(redNarrowbands.length == 1){
                    IJ.run(redStacker, "Add Slice", "");
                }
                //What happens if these weren't set yet?  Do I need to get the width of height of the image?
                //It should at least be set to the height or width of the image when grabbed above in the pcaHeight < 100 clause right?
                if(pcaWidth == 0){
                    
                }
                if(pcaHeight == 0){
                    
                }
                redStacker.setRoi(pcaX,pcaY,pcaWidth, pcaHeight); 
                logService.log().info("PCA on red stack");
		IJ.run(redStacker,"PCA ", ""); 
                ImagePlus pca_redStacker = WindowManager.getImage("PCA of RedStack");
                pca_redStacker.hide();
		IJ.run( pca_redStacker,"Slice Keeper", "first=1 last=1 increment=1");      
                WindowManager.getImage("Eigenvalue spectrum of RedStack").close();
                redStacker.changes=false;
                redStacker.close();
                pca_redStacker.close();
                ImagePlus kept_pcaRedStacker = WindowManager.getImage("PCA of RedStack kept stack");
                kept_pcaRedStacker.hide();
                kept_pcaRedStacker.setTitle("R");
                kept_pcaRedStacker.setRoi(pcaX,pcaY,pcaWidth,pcaHeight);
		IJ.run(kept_pcaRedStacker,"Enhance Contrast...", "saturated=0.4 normalize");
		IJ.run(kept_pcaRedStacker, "8-bit", "");                
		//Green
		String greenStringList = greenNarrowbands[0].toString();
		for (int i=1;i<greenNarrowbands.length;i++) {
                    greenStringList = greenStringList+"|"+greenNarrowbands[i].toString();
		}
                IJ.run("Image Sequence...", "open="+projectDirectory+"Captures-Narrowband-NoGamma"+File.separator+" file=("+greenStringList+") sort"); 
                ImagePlus greenStacker = WindowManager.getImage("Captures-Narrowband-NoGamma");
                logService.log().info("There are "+greenStacker.getStackSize()+" images in the GREEN STACK");
                greenStacker.hide();
                if(greenNarrowbands.length == 1){
                    IJ.run(greenStacker, "Add Slice", "");
                }
                greenStacker.setTitle("GreenStack");
                greenStacker.setRoi(pcaX,pcaY,pcaWidth,pcaHeight); 
                logService.log().info("Run PCA on the GREEN STACK");
		IJ.run(greenStacker,"PCA ", ""); 
                ImagePlus pca_greenStacker = WindowManager.getImage("PCA of GreenStack");
                pca_greenStacker.hide();
		IJ.run(pca_greenStacker,"Slice Keeper", "first=1 last=1 increment=1");
                WindowManager.getImage("Eigenvalue spectrum of GreenStack").close();
                greenStacker.changes=false;
                greenStacker.close();
                pca_greenStacker.close();
                ImagePlus kept_pcaGreenStacker = WindowManager.getImage("PCA of GreenStack kept stack");
                kept_pcaGreenStacker.hide();
                kept_pcaGreenStacker.setTitle("G");
                kept_pcaGreenStacker.setRoi(pcaX,pcaY,pcaWidth,pcaHeight); 
		IJ.run(kept_pcaGreenStacker, "Enhance Contrast...", "saturated=0.4 normalize");
		IJ.run(kept_pcaGreenStacker, "8-bit", "");              		
                //Blue
		String blueStringList = blueNarrowbands[0].toString();  
		for (int i=1;i<blueNarrowbands.length;i++) {
                    blueStringList = blueStringList+"|"+blueNarrowbands[i].toString();
		}
                IJ.run("Image Sequence...", "open="+projectDirectory+"Captures-Narrowband-NoGamma"+File.separator+" file=("+blueStringList+") sort");
                ImagePlus blueStacker = WindowManager.getImage("Captures-Narrowband-NoGamma");
                logService.log().info("There are "+blueStacker.getStackSize()+" images in the BLUE STACK");
                blueStacker.hide();
                if(blueNarrowbands.length == 1){
                    IJ.run(blueStacker, "Add Slice", "");
                }
                blueStacker.setTitle("BlueStack");
                blueStacker.setRoi(pcaX,pcaY,pcaWidth,pcaHeight); 
                logService.log().info("Run PCA on the BLUE STACK");
		IJ.run(blueStacker,"PCA ", ""); 
                ImagePlus pca_blueStacker = WindowManager.getImage("PCA of BlueStack");
                pca_blueStacker.hide();
		IJ.run(pca_blueStacker,"Slice Keeper", "first=1 last=1 increment=1");
                WindowManager.getImage("Eigenvalue spectrum of BlueStack").close();
                blueStacker.changes = false;
                blueStacker.close();
                pca_blueStacker.close();
                ImagePlus kept_pcaBlueStacker =  WindowManager.getImage("PCA of BlueStack kept stack");
                kept_pcaBlueStacker.hide();
                kept_pcaBlueStacker.setTitle("B");
                kept_pcaBlueStacker.setRoi(pcaX,pcaY,pcaWidth,pcaHeight); 
		IJ.run(kept_pcaBlueStacker, "Enhance Contrast...", "saturated=0.4 normalize");
		IJ.run(kept_pcaBlueStacker, "8-bit", "");  
                ImagePlus stack = Concatenator.run(kept_pcaRedStacker, kept_pcaBlueStacker, kept_pcaGreenStacker);
                stack.setTitle("Stack");
                stack.hide();
                kept_pcaRedStacker.close();
                kept_pcaBlueStacker.close();
                kept_pcaGreenStacker.close();
		IJ.run(stack, "Stack to RGB", "");
                ImagePlus stackRGB = WindowManager.getImage("Stack (RGB)");
                stackRGB.hide();
                stack.close();
		//create extended spectrum static diffuse
		if (xsRakingDesired){
                    noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_00"+".tiff");
                    logService.log().info("Save XS static raking source image "+projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_00"+".tiff");
                    IJ.save(stackRGB, projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_00"+".tiff");
                    createJp2(projectName+"_Xs_00", projectDirectory); 
                    toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_00"+".tiff");
                    logService.log().info("Remove XS static raking source image "+projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_00"+".tiff");
                    Files.deleteIfExists(toDelete.toPath());
		}
		IJ.run(stackRGB, "RGB to YCbCr stack", "");
                stackRGB.close();
                ImagePlus YcbCrStack = WindowManager.getImage("Stack - YCbCr");
                YcbCrStack.hide();
		IJ.run(YcbCrStack, "Stack to Images", "");
                YcbCrStack.close();
                WindowManager.getImage("Y").close();
                ImagePlus cb = WindowManager.getImage("Cb");
                ImagePlus cr = WindowManager.getImage("Cr");
                cb.hide();
                cr.hide();
                ImagePlus keptPieces = con.concatenate(cb, cr, true);
                logService.log().info("What is transmissive source "+transmissiveSource);
		if (!transmissiveSource.equals("")){
                    imp = opener.openImage( transmissiveSource );
                    imp.setTitle("TransmissiveLuminance");
                    IJ.run(imp, "8-bit", "");
                    ImagePlus stack2 = con.concatenate(imp, keptPieces, true);
                    stack2.setTitle("YCC");
                    IJ.run(stack2,"YCbCr stack to RGB","");
                    ImagePlus stackRGB2 = WindowManager.getImage("YCC - RGB");
                    stackRGB2.hide();
                    imp.changes=false;
                    imp.close();
                    noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_Tx.tiff");
                    logService.log().info("Save XS static raking source image "+projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_Tx.tiff");
                    IJ.save(stackRGB2, projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_Tx.tiff");
                    createJp2(projectName+"_Xs_Tx", projectDirectory);
                    toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_Tx.tiff");
                    logService.log().info("Remove XS static raking source image "+projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_Tx.tiff");
                    Files.deleteIfExists(toDelete.toPath()); 
                    stack2.close();
                    stackRGB2.close();
		}
		if (xsRtiDesired) {
                    if (!extended_spectrum_dir.exists()) {
                        Files.createDirectory(extended_spectrum_dir.toPath());
                        logService.log().info("A directory has been created for Extended Spectrum RTI at "+projectDirectory+"ExtendedSpectrumRTI");
                    }
		}
		for(int i=0;i<listOfHemisphereCaptures.length;i++){
                    logService.log().info("On hem capture index "+i);
                    if (xsRtiDesired) {
                        imp = opener.openImage( listOfHemisphereCaptures[i].toString() );
                        imp.setTitle("Luminance");
                        if (brightnessAdjustApply.equals("RTI images also")) {
                            if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                                imp.setRoi(pcaX,pcaY,pcaWidth,pcaHeight);
                                IJ.run(imp,"Enhance Contrast...", "saturated=0.4");
                            } 
                            else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                                IJ.run(imp, "Multiply...", "value="+normalizationFixedValue+"");
                            }
                        }
                        IJ.run(imp, "8-bit", "");
                        ImagePlus stack3 = con.concatenate(imp, keptPieces, true);
                        stack3.setTitle("YCC");
                        IJ.run(stack3, "YCbCr stack to RGB", "");
                        ImagePlus stackRGB2 = WindowManager.getImage("YCC - RGB");
                        stackRGB2.hide();
                        int extensionIndex = listOfHemisphereCaptures[i].getName().indexOf(".");
                        if (extensionIndex != -1)
                        {
                            String simpleName1 = listOfHemisphereCaptures[i].getName().substring(0, extensionIndex); //.toString().substring(0, extensionIndex)
                            filePath = projectDirectory+"ExtendedSpectrumRTI"+File.separator+"ExtendedSpectrum_"+simpleName1;
                            simpleImageName = "ExtendedSpectrum_"+simpleName1;
                        }
                        else{
                            IJ.error("A file in your hemisphere folder does not have an extension.  Please review around "+listOfHemisphereCaptures[i].getName()+"  ---  Exiting...");
                            throw new Throwable("A file in your hemisphere folder does not have an extension.  Please review around "+listOfHemisphereCaptures[i].getName()); 
                        }
                        noClobber(filePath+".jpg");
                        logService.log().info("Save XS RTI source image "+filePath+".jpg");
                        IJ.saveAs(stackRGB2, "jpeg",filePath+".jpg");
                        stack3.close();
                        imp.changes = false;
                        imp.close();
                        stackRGB2.close();
                    }
                    if (xsRakingDesired) {
                        if (listOfRakingDirections.get(i)) {
                            imp = opener.openImage( listOfHemisphereCaptures[i].toString() );
                            imp.setTitle("Luminance");
                            if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                                imp.setRoi(normX,normY,normWidth,normHeight); 
                                IJ.run(imp, "Enhance Contrast...", "saturated=0.4");
                            } 
                            else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                                IJ.run(imp, "Multiply...", "value="+normalizationFixedValue+"");
                            }
                            IJ.run(imp, "8-bit", "");
                            ImagePlus stack4 = con.concatenate(imp, keptPieces, true);
                            stack4.setTitle("YCC");
                            stack4.hide();
                            imp.changes = false;
                            imp.close();
                            IJ.run(stack4, "YCbCr stack to RGB", "");
                            ImagePlus stackRGB3 = WindowManager.getImage("YCC - RGB");
                            stackRGB3.hide();
                            positionNumber = IJ.pad(i+1, 2).toString();
                            noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_"+positionNumber+".tiff");
                            logService.log().info("Save XS Raking source image "+projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_"+positionNumber+".tiff");
                            IJ.save(stackRGB3, projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_"+positionNumber+".tiff");
                            createJp2(projectName+"_Xs_"+positionNumber, projectDirectory);
                            toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_"+positionNumber+".tiff");
                            logService.log().info("Remove XS Raking source image "+projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_"+positionNumber+".tiff");
                            Files.deleteIfExists(toDelete.toPath()); 
                            stack4.close();
                            stackRGB3.close();
                        }
                    }
		}
                cb.close();
                cr.close();
		if (xsRtiDesired) {
                    createLpFile("ExtendedSpectrum", projectDirectory);
                    runFitter("ExtendedSpectrum");
		}
                IJ.run("Collect Garbage");
            }
            if (psRtiDesired || psRakingDesired) {
                File fluorescenceNoGamma = new File(projectDirectory+"Captures-Fluorescence-NoGamma"+File.separator);
                ImagePlus flNoGamma;
                ImagePlus flNarrowNoGammaStack;
                ImagePlus narrowNoGamma;
                ImagePlus narrowKeptPCA = new ImagePlus();
                IJ.run("Image Sequence...", "open="+projectDirectory+"Captures-Narrowband-NoGamma"+File.separator+" sort");
                narrowNoGamma = WindowManager.getImage("Captures-Narrowband-NoGamma");//There are supposed to be a minimum of 9 images here. That should have already been checked.  
                narrowNoGamma.hide();
		//option to create new ones based on narrowband captures and assumption that pc1 and pc2 are best
		if (pcaMethod.equals("Generate and select using defaults")){
                    if (fluorescenceNoGamma.exists()) {
                        IJ.run("Image Sequence...", "open="+projectDirectory+"Captures-Fluorescence-NoGamma"+File.separator+" sort");
                        flNoGamma = WindowManager.getImage("Captures-Fluorescence-NoGamma");
                        flNoGamma.hide();
                        flNarrowNoGammaStack = con.concatenate(narrowNoGamma, flNoGamma, false);
                        narrowNoGamma.close();
                        narrowNoGamma = flNarrowNoGammaStack;
                        narrowNoGamma.setTitle("Captures-Narrowband-NoGamma");
                    }
                    else{
                        // ?
                    }
                    narrowNoGamma.setRoi(pcaX,pcaY,pcaWidth,pcaHeight); 
                    IJ.run(narrowNoGamma, "PCA ", "");
                    IJ.run(WindowManager.getImage("PCA of Captures-Narrowband-NoGamma"),"Slice Keeper", "first=2 last=3 increment=1");
                    WindowManager.getImage("Eigenvalue spectrum of Captures-Narrowband-NoGamma").close();
                    narrowNoGamma.changes = false;
                    WindowManager.getImage("PCA of Captures-Narrowband-NoGamma").close();
                    ImagePlus keptNoGammaPCA = WindowManager.getImage("PCA of Captures-Narrowband-NoGamma kept stack");
                    keptNoGammaPCA.hide();
                    keptNoGammaPCA.setRoi(pcaX,pcaY,pcaWidth,pcaHeight); 
                    IJ.run(keptNoGammaPCA, "Enhance Contrast...", "saturated=0.3 normalize update process_all");
                    IJ.run(keptNoGammaPCA, "8-bit", "");
                    narrowKeptPCA = keptNoGammaPCA;
		} 
                else if (pcaMethod.equals("Generate and manually select two")) {
                    //option to create new ones and manually select (close all but two)
                    if (fluorescenceNoGamma.exists()) {
                        IJ.run("Image Sequence...", "open="+projectDirectory+"Captures-Fluorescence-NoGamma"+File.separator+" sort");
                        flNoGamma = WindowManager.getImage("Captures-Fluorescence-NoGamma");
                        flNoGamma.hide();
                        flNarrowNoGammaStack = con.concatenate(narrowNoGamma, flNoGamma, false);
                        narrowNoGamma.close();
                        narrowNoGamma = flNarrowNoGammaStack;
                        narrowNoGamma.setTitle("Captures-Narrowband-NoGamma");
                    }
                    else{
                        // ?
                    }
                    narrowNoGamma.setRoi(pcaX,pcaY,pcaWidth,pcaHeight); 
                    IJ.run(narrowNoGamma, "PCA ", "");
                    WindowManager.getImage("Eigenvalue spectrum of Captures-Narrowband-NoGamma").close();
                    narrowNoGamma.changes = false;
                    ImagePlus noGammaPCA = WindowManager.getImage("PCA of Captures-Narrowband-NoGamma");
                    noGammaPCA.show();
                    if(noGammaPCA.getStackSize() > 2){
                        contentPane = new JPanel();
                        contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));
                        labelPanel = new JPanel();
                        labelPanel2 = new JPanel();
                        labelPanel3 = new JPanel();
                        JPanel sliceCountPanel = new JPanel();
                        JLabel directions = new JLabel("Delete slices from the PCA stack until two remain.");
                        JLabel directions2 = new JLabel("Navigate to the slice in the stack you want to delete using the buttons below.");
                        JLabel directions3 = new JLabel("Once there are only two slices remaining, click Finish to accept the slices.");
                        JLabel sliceCount = new JLabel(noGammaPCA.getStackSize()+" slices");
                        sliceCountPanel.add(sliceCount);
                        labelPanel.add(directions);
                        labelPanel2.add(directions2);
                        labelPanel3.add(directions3);
                        labelPanel3.setBorder(new EmptyBorder(0,0,10,0));
                        contentPane.add(labelPanel);
                        contentPane.add(labelPanel2);
                        contentPane.add(labelPanel3);
                        contentPane.add(sliceCountPanel);
                        JButton deleteSlice = new JButton("Delete Slice");
                        deleteSlice.addActionListener(new ActionListener() { 
                            public void actionPerformed(ActionEvent e) { 
                                if(noGammaPCA.getStackSize() > 2){
                                    IJ.run(noGammaPCA, "Delete Slice", "");
                                    sliceCount.setText(noGammaPCA.getStackSize()+" slices");
                                }
                                else{
                                    JOptionPane.showMessageDialog(null,
                                    "Only two slices, cannot remove any more.", "Try Again",
                                    JOptionPane.PLAIN_MESSAGE);
                                }
                            } 
                        });
                        JButton nextSlice = new JButton("Next Slice");
                        nextSlice.addActionListener(new ActionListener() { 
                            public void actionPerformed(ActionEvent e) { 
                                IJ.run(noGammaPCA, "Next Slice [>]", "");
                            } 
                        });
                        JButton previousSlice = new JButton("Previous Slice");
                        previousSlice.addActionListener(new ActionListener() { 
                            public void actionPerformed(ActionEvent e) { 
                                IJ.run(noGammaPCA, "Previous Slice [<]", "");
                            } 
                        });
                        JPanel buttonPanel = new JPanel();
                        buttonPanel.add(nextSlice);
                        buttonPanel.add(previousSlice);
                        buttonPanel.add(deleteSlice);
                        contentPane.add(buttonPanel);
                        JFrame contentFrame = new JFrame("Delete Slices Frame");
                        contentFrame.setLocation(screenSize.width/2-contentFrame.getSize().width/2, screenSize.height/2-contentFrame.getSize().height/2);
                        Object[] btns = {"Finish",
                        "Quit"};
                        JOptionPane pane = new JOptionPane(contentPane, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, btns, btns[0]);
                        JDialog d2 = pane.createDialog(contentFrame, "Delete Slices Dialog w/ pane");
                        d2.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
                        String deleteSliceResult = "";
                        while(noGammaPCA.getStackSize() != 2){
                            d2.setVisible(true);
                            if(null == pane.getValue()){
                                //@UserHitCancel
                                noGammaPCA.changes = false; //otherwise it asks to save changes..
                                contentFrame.dispose();
                                d2.dispose();
                                IJ.error("You must delete until there are two slices to continue!   Exiting...");
                                throw new Throwable("You must delete until there are two slices to continue!");
                            }
                            deleteSliceResult = pane.getValue().toString();
                            if(deleteSliceResult.equals("Quit") || deleteSliceResult.equals("")){
                                //@UserHitCancel
                                noGammaPCA.changes = false; //otherwise it asks to save changes..
                                contentFrame.dispose();
                                d2.dispose();
                                IJ.error("You must delete until there are two slices to continue!   Exiting...");
                                throw new Throwable("You must delete until there are two slices to continue!");
                            }
                            else if(deleteSliceResult.equals("Finish")){
                                if(noGammaPCA.getStackSize() == 2){
                                    noGammaPCA.hide();
                                    noGammaPCA.setTitle("PCA of Captures-Narrowband-NoGamma kept stack");
                                    noGammaPCA.setRoi(pcaX,pcaY,pcaWidth,pcaHeight); 
                                    IJ.run(noGammaPCA, "8-bit", "");
                                    narrowKeptPCA = noGammaPCA;
                                    contentFrame.dispose();
                                    d2.dispose();
                                }
                                else{
                                    JOptionPane.showMessageDialog(null,
                                    "You must have a stack of two slices.", "Try Again",
                                    JOptionPane.PLAIN_MESSAGE);
                                }
                            }
                            else{
                                //BAD
                            }                          
                        }
                    }
                    else{
                        //There were supposed to be a minimum of 9 images in here, so we are ignoring it as if we didn't have the data.  
                        IJ.error("You must have 9 or more narrow band captures!  Exiting...");
                        throw new Throwable("You must have 9 or more narrow band captures for this processr!");
                    }
		/**
                 * @see option to use previously generated principal component images
                 */
		}
                else if (pcaMethod.equals("Open pregenerated images")) {
                    while(WindowManager.getImageCount() != 2){
                        dWait = new WaitForUserDialog("Designated Images", "Open a pair of images or stack of two slices.\nEnhance contrast as desired\nThen press Ok.  Press 'Esc' to quit.");
                        dWait.show();
                        if(dWait.escPressed()){
                            //@userHitCancel
                            IJ.error("You must make selections to continue!  Exiting...");
                            throw new Throwable("You must make selections to continue!");
                        }
                    }
                    if (WindowManager.getImageCount() > 1){ 
                        IJ.run("Images to Stack", "name=Stack title=[] use"); 
                        WindowManager.getActiveWindow().setName("PCA of Captures-Narrowband-NoGamma kept stack");
                    }
                    else{
                        IJ.error("Open a pair of images or stack of two slices to continue!  Exiting...");
                        throw new Throwable("Open a pair of images or stack of two slices to continue!");
                    }
                    IJ.run(WindowManager.getImage("PCA of Captures-Narrowband-NoGamma kept stack"), "8-bit", "");
                    narrowKeptPCA = WindowManager.getImage("PCA of Captures-Narrowband-NoGamma kept stack");
		}
		
                //@see integrate pca pseudocolor with rti luminance
                //@see create static diffuse (not trivial... use median of all)
                
		if (psRakingDesired){
                    IJ.run("Image Sequence...", "open="+projectDirectory+"Captures-Hemisphere-Gamma"+File.separator);
                    narrowNoGamma = WindowManager.getImage("Captures-Hemisphere-Gamma");
                    narrowNoGamma.hide();
                    IJ.run(narrowNoGamma, "Z Project...", "projection=Median");
                    narrowNoGamma.changes = false;
                    narrowNoGamma.close();
                    ImagePlus luminance = WindowManager.getImage("MED_Captures-Hemisphere-Gamma");
                    luminance.setTitle("Luminance");
                    luminance.hide();
                    if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                        region = new RectangleOverlay();
                        luminance.setRoi(normX,normY,normWidth,normHeight); 
                        IJ.run(luminance, "Enhance Contrast...", "saturated=0.4");
                    } 
                    else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                        IJ.run(luminance, "Multiply...", "value="+normalizationFixedValue+"");
                    }
                    IJ.run(luminance, "8-bit", "");
                    ImagePlus lumNarrowKeptStack = con.concatenate(luminance, narrowKeptPCA, true);
                    lumNarrowKeptStack.setTitle("YCC");
                    lumNarrowKeptStack.hide();
                    IJ.run(lumNarrowKeptStack, "YCbCr stack to RGB", "");
                    ImagePlus lumRGB = WindowManager.getImage("YCC - RGB");
                    lumRGB.hide();
                    noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_00.tiff");
                    logService.log().info("Save PS Raking source image "+projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_00.tiff");
                    IJ.save(lumRGB, projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_00.tiff");
                    createJp2(projectName+"_Ps_00", projectDirectory);
                    toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_00.tiff");
                    logService.log().info("Remove PS Raking source image "+projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_00.tiff");
                    Files.deleteIfExists(toDelete.toPath());
                    lumRGB.close();
                    lumNarrowKeptStack.close();
                    luminance.changes = false;
                    luminance.close();
                    if (!transmissiveSource.equals("")) {
                        imp = opener.openImage( transmissiveSource );
                        imp.setTitle("TransmissiveLuminance");
                        IJ.run(imp, "8-bit", "");
                        ImagePlus transNarrowKeptStack = con.concatenate(imp, narrowKeptPCA, true);
                        transNarrowKeptStack.setTitle("YCC");
                        transNarrowKeptStack.hide();
                        IJ.run(transNarrowKeptStack, "YCbCr stack to RGB", "");
                        ImagePlus transRGB = WindowManager.getImage("YCC - RGB");
                        transRGB.hide();
                        imp.changes = false;
                        imp.close();
                        noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_Tx.tiff");
                        logService.log().info("Save PS Raking source image "+ projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_Tx.tiff");
                        IJ.save(transRGB, projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_Tx.tiff");
                        createJp2(projectName+"_Ps_Tx", projectDirectory);
                        toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_Tx.tiff");
                        logService.log().info("Remove PS Raking source image "+ projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_Tx.tiff");
                        Files.deleteIfExists(toDelete.toPath());
                        transRGB.close();
                        transNarrowKeptStack.close();
                    }
		}
		if (psRtiDesired) {
                    if (!pseudo_color_dir.exists()) {
                        File createPseudo = new File(projectDirectory+"PseudoColorRTI"+File.separator);
                        Files.createDirectory(createPseudo.toPath());
                        logService.log().info("A directory has been created for PseudoColor RTI at "+projectDirectory+"PseudoColorRTI");
                    }
		}
		for(int i=0;i<listOfHemisphereCaptures.length;i++) {
                    logService.log().info("On hem capture index "+i);
                    if (psRtiDesired||listOfRakingDirections.get(i)){ //YIKES double check on this
                        imp = opener.openImage( listOfHemisphereCaptures[i].toString() );
                        imp.setTitle("Luminance");
                        int extensionIndex = listOfHemisphereCaptures[i].getName().indexOf(".");
                        IJ.run(imp, "Duplicate...", "title=EnhancedLuminance");
                        ImagePlus enhancedLum = WindowManager.getImage("EnhancedLuminance");
                        enhancedLum.hide();
                        if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                            enhancedLum.setRoi(normX,normY,normWidth,normHeight); 
                            IJ.run(enhancedLum,"Enhance Contrast...", "saturated=0.4");
                        } 
                        else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                            IJ.run(enhancedLum,"Multiply...", "value="+normalizationFixedValue+"");
                        }
                        IJ.run(imp, "8-bit", "");
                        IJ.run(enhancedLum, "8-bit", "");
                        ImagePlus enhancedNarrowKeptStack = con.concatenate(enhancedLum, narrowKeptPCA, true);
                        enhancedNarrowKeptStack.setTitle("YCC");
                        enhancedNarrowKeptStack.hide();
                        IJ.run(enhancedNarrowKeptStack, "YCbCr stack to RGB", "");
                        ImagePlus RGBImg = WindowManager.getImage("YCC - RGB");
                        RGBImg.hide();
                        if (listOfRakingDirections.get(i)) { 
                            enhancedNarrowKeptStack.close();
                            positionNumber = IJ.pad(i+1, 2).toString();                          
                            noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_"+positionNumber+".tiff");
                            logService.log().info("Save PS Raking source image "+ projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_"+positionNumber+".tiff");
                            IJ.save(RGBImg, projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_"+positionNumber+".tiff");
                            createJp2(projectName+"_Ps_"+positionNumber, projectDirectory);
                            toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_"+positionNumber+".tiff");
                            logService.log().info("Remove PS Raking source image "+ projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_"+positionNumber+".tiff");
                            Files.deleteIfExists(toDelete.toPath());
                        }
                        if ((psRtiDesired)&&(brightnessAdjustApply.equals("RTI images also"))){ 
                            extensionIndex = listOfHemisphereCaptures[i].getName().indexOf(".");
                            if (extensionIndex != -1)
                            {
                                String simpleName1 = listOfHemisphereCaptures[i].getName().substring(0, extensionIndex);
                                filePath = projectDirectory+"PseudoColorRTI"+File.separator+"PseudoColor_"+simpleName1;
                                simpleImageName = "PseudoColor_"+simpleName1;
                            }
                            else{
                                IJ.error("A file in your hemisphere folder does not have an extension.  Please review around "+listOfHemisphereCaptures[i].getName()+"  ---  Exiting...");
                                throw new Throwable("A file in your hemisphere folder does not have an extension.  Please review around "+listOfHemisphereCaptures[i].getName()); 
                            }
                            if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                                region = new RectangleOverlay();
                                enhancedLum.setRoi(normX,normY,normWidth,normHeight); 
                                IJ.run(RGBImg,"Enhance Contrast...", "saturated=0.4");
                            } 
                            else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                                IJ.run(RGBImg,"Multiply...", "value="+normalizationFixedValue+"");
                            }
                            noClobber(filePath+".jpg");
                            logService.log().info("Save PS RTI source image "+filePath+".jpg");
                            IJ.saveAs(RGBImg, "jpeg", filePath+".jpg");
                        } 
                        else if (psRtiDesired) {
                            extensionIndex = listOfHemisphereCaptures[i].getName().indexOf(".");
                            if (extensionIndex != -1)
                            {
                                String simpleName1 = listOfHemisphereCaptures[i].getName().substring(0, extensionIndex);
                                filePath = projectDirectory+"PseudoColorRTI"+File.separator+"PseudoColor_"+simpleName1;
                                simpleImageName = "PseudoColor_"+simpleName1;
                            }
                            else{
                                IJ.error("A file in your hemisphere folder does not have an extension.  Please review around "+listOfHemisphereCaptures[i].getName()+"  ---  Exiting...");
                                throw new Throwable("A file in your hemisphere folder does not have an extension.  Please review around "+listOfHemisphereCaptures[i].getName()); 
                            }
                            noClobber(filePath+".jpg");
                            logService.log().info("Save PS RTI source image "+filePath+".jpg");
                            IJ.saveAs(RGBImg, "jpeg", filePath+".jpg");
                        }
                        enhancedLum.changes = false;
                        imp.changes = false;
                        enhancedLum.close();
                        imp.close();
                        RGBImg.close();
                    }
		}
                narrowKeptPCA.changes = false;
                narrowKeptPCA.close();
		if (psRtiDesired) {
                    createLpFile("PseudoColor", projectDirectory);
                    runFitter("PseudoColor");
		}
                IJ.run("Collect Garbage");
            }
            if (csRtiDesired || csRakingDesired) { //processing phase
		csSource = csSource.replace("\\",File.separator);
                //Need to pull info out of csSource name.  Use File API to do this.
                File csFile = new File(csSource);
                File csProcessFile = new File(csFile.getParent());
                String csProcessName = csProcessFile.getName();
                logService.log().info("Process name is "+csProcessName);
                logService.log().info("Assuming process file is "+projectDirectory+csProcessName+"RTI"+File.separator);
                csProcessFile = new File(projectDirectory+csProcessName+"RTI"+File.separator);
                ImagePlus cb = new ImagePlus();
                ImagePlus cr = new ImagePlus();
		if (!csProcessFile.exists()) {
                    Files.createDirectory(csProcessFile.toPath());
                    logService.log().info("A directory has been created for "+csProcessName+" RTI at "+csProcessFile.toString());
		}
                logService.log().info("Open the custom source");
                imp = opener.openImage(csSource);
                logService.log().info("Process custom source");
		if ((imp.getImageStackSize() == 1)&&(imp.getBitDepth()<24)) {
                    if (csRakingDesired) {
                        noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_00"+".tiff");
                        IJ.save(imp, projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_00"+".tiff");
                        createJp2(projectName+"_"+csProcessName+"_00", projectDirectory);
                        toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_00"+".tiff");
                        Files.deleteIfExists(toDelete.toPath());
                    }
                    IJ.run(imp, "8-bit", "");
                    IJ.run(imp, "Duplicate...", "title=Cb");
                    IJ.run(imp, "Duplicate...", "title=Cr");
                    cb = WindowManager.getImage("Cb");
                    cr = WindowManager.getImage("Cr");
                    cb.hide();
                    cr.hide();
		} 
                else if (imp.getImageStackSize() == 2) {
                    IJ.run(imp, "8-bit", "");
                    IJ.run(imp, "Stack to Images", "");
                    cb = WindowManager.getImage(1);
                    cr = WindowManager.getImage(2);
                    cb.setTitle("Cb");
                    cr.setTitle("Cr");
                    cb.hide();
                    cr.hide();
		} 
                else if ((imp.getImageStackSize() > 2)||(imp.getBitDepth()==24)){
                    if (imp.getImageStackSize() > 3) {
                        IJ.run(imp, "Slice Keeper", "first=1 last=3 increment=1");
                        logService.log().info("Only the first three slices in the stack can be used at this time.");
                    }
                    if (imp.getBitDepth() == 8) {
                        IJ.run(imp, "RGB Color", "");
                    }
                    //create a 00 static diffuse
                    if (csRakingDesired) {
                        noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_00"+".tiff");
                        IJ.save(imp, projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_00"+".tiff");
                        createJp2(projectName+"_"+csProcessName+"_00", projectDirectory);
                        toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_00"+".tiff");
                        Files.deleteIfExists(toDelete.toPath());
                    }
                    IJ.run(imp, "RGB to YCbCr stack", "");
                    IJ.run("8-bit");
                    IJ.run("Stack to Images");
                    WindowManager.getImage("Y").close();
                    cb = WindowManager.getImage("Cb");
                    cr = WindowManager.getImage("Cr");
                    cb.hide();
                    cr.hide();
		}
                imp.close();
                logService.log().info("What is transmissive source "+transmissiveSource);
		if (!transmissiveSource.equals("")){
                    imp = opener.openImage( transmissiveSource );
                    IJ.run(imp, "8-bit", "");
                    ImagePlus piece = con.concatenate(cb, cr, true);
                    ImagePlus stack = con.concatenate(imp, piece, true);
                    stack.setTitle("YCC");
                    IJ.run(stack, "YCbCr stack to RGB", "");
                    ImagePlus RGBImage = WindowManager.getImage("YCC - RGB");
                    RGBImage.hide();
                    imp.changes = false;
                    imp.close();
                    noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_Tx.tiff");
                    IJ.save(RGBImage, projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_Tx.tiff");
                    createJp2(projectName+"_"+csProcessName+"_Tx", projectDirectory);
                    toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_Tx.tiff");
                    Files.deleteIfExists(toDelete.toPath());
                    RGBImage.close();
                    stack.close();
		}
		for(int i=0;i<listOfHemisphereCaptures.length;i++) {
                    if (listOfHemisphereCaptures[i].toString().endsWith("tiff")|| listOfHemisphereCaptures[i].toString().endsWith("tif")) {
                        logService.log().info("On hem capture index "+i);
                        if ((csRtiDesired)|| listOfRakingDirections.get(i)) {
                            imp = opener.openImage( listOfHemisphereCaptures[i].toString() );
                            int extensionIndex = listOfHemisphereCaptures[i].getName().indexOf(".");
                            if (extensionIndex != -1)
                            {
                                String simpleName1 = listOfHemisphereCaptures[i].getName().substring(0, extensionIndex); 
                                filePath = projectDirectory+csProcessName+"RTI"+File.separator+csProcessName+"_"+simpleName1;
                                simpleImageName = csProcessName+"_"+simpleName1;
                            }
                            else{
                                IJ.error("A file in your hemisphere folder does not have an extension.  Please review around "+listOfHemisphereCaptures[i].getName()+"  ---  Exiting...");
                                throw new Throwable("A file in your hemisphere folder does not have an extension.  Please review around "+listOfHemisphereCaptures[i].getName()); 
                            }
                            IJ.run(imp, "Duplicate...", "title=EnhancedLuminance");
                            ImagePlus enhancedLum = WindowManager.getImage("EnhancedLuminance");
                            if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                                region = new RectangleOverlay();
                                enhancedLum.setRoi(normX,normY,normWidth,normHeight); 
                                IJ.run(enhancedLum,"Enhance Contrast...", "saturated=0.4");
                            } 
                            else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                                IJ.run(enhancedLum,"Multiply...", "value="+normalizationFixedValue+"");
                            }
                            enhancedLum.hide();
                            IJ.run(imp, "8-bit", "");
                            IJ.run(enhancedLum, "8-bit", "");
                            ImagePlus piece = con.concatenate(cb, cr, true);
                            ImagePlus stack = con.concatenate(enhancedLum, piece, true);
                            stack.setTitle("YCC");
                            IJ.run(stack, "YCbCr stack to RGB", "");
                            ImagePlus RGBImage = WindowManager.getImage("YCC - RGB");
                            RGBImage.hide();
                            stack.close();
                            if (listOfRakingDirections.get(i)){
                                positionNumber = IJ.pad(i+1, 2).toString();
                                noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_"+positionNumber+".tiff");
                                IJ.save(RGBImage,projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_"+positionNumber+".tiff");
                                RGBImage.close();
                                createJp2(projectName+"_"+csProcessName+"_"+positionNumber, projectDirectory);
                                toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_"+positionNumber+".tiff");
                                Files.deleteIfExists(toDelete.toPath());
                            }
                            if (csRtiDesired) {
                                if(brightnessAdjustApply.equals("RTI images also")){
                                    noClobber(filePath+".jpg");
                                    logService.log().info("Saving custom source image "+filePath+".jpg");
                                    IJ.saveAs(RGBImage,"jpeg", filePath+".jpg");
                                }
                                else{
                                    RGBImage.close();
                                    piece = con.concatenate(cb, cr, true);
                                    stack = con.concatenate(imp, piece, true);
                                    stack.setTitle("YCC");
                                    IJ.run(stack, "YCbCr stack to RGB", "");
                                    RGBImage = WindowManager.getImage("YCC - RGB");
                                    RGBImage.hide();
                                    stack.close();
                                    noClobber(filePath+".jpg");
                                    logService.log().info("Saving custom source image "+filePath+".jpg");
                                    IJ.saveAs(RGBImage,"jpeg", filePath+".jpg");
                                }
                                RGBImage.close();
                            }
                            enhancedLum.changes = false;
                            imp.changes = false;
                            enhancedLum.close();
                            imp.close();
                        }
                    }
		}
                logService.log().info("Custom source files created, create lp file and run the fitter");
                cb.close();
                cr.close();
                if (csRtiDesired) {
                    createLpFile(csProcessName, projectDirectory);
                    runFitter(csProcessName);
		}
                IJ.run("Collect Garbage");
            }
            IJ.beep();
            WindowManager.closeAllWindows();
            GenericDialog end = new GenericDialog("Processing Complete");
            end.addMessage("Processing Complete at "+timestamp());
            end.setMaximumSize(bestFit);
            end.showDialog();
            logService.log().info("End of SpectralRTI Plugin.");
        }
        	
        /**
         * The whole macro will run from here.  This is what fires when its clicked in imageJ.  
         */
        @Override
	public void run() {
            try {
               plugin();
            } catch (IOException ex) {
                Logger.getLogger(SpectralRTI_Toolkit.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Throwable ex) {
                Logger.getLogger(SpectralRTI_Toolkit.class.getName()).log(Level.SEVERE, null, ex);
            }
              logService.log().info("Finished processing the run().");
	}       

        /**
        * Helper function to populate the file list for hemisphereCaputers.  Based off getFilesList() macro
        * @param dir The directory to check
        * @return listOfFiles A list of files from the given directory
        */
        public File[] getHemisphereCaptures(String dir){
            File folder = new File(dir);
            File[] listOfFiles = folder.listFiles();
            return listOfFiles;
        }
        
       
        /** 
        * Create a JP2000 by compressing a .tiff
        * @param inFile The directory to check
        * @param projDir The project directory
        * @return listOfFiles A list of files from the given directory
        * @throws java.io.IOException
        */
        public String createJp2(String inFile, String projDir) throws IOException, InterruptedException, Throwable {
            String preferredCompress = theList.get("preferredCompress");
            String preferredJp2Args = theList.get("preferredJp2Args");
            String compressString = "";
            String preferredString = "";
            OpenDialog dialog;  //For files
            String returnString = "/new/JP2file";
            File preferredCompressFile = new File(preferredCompress);
            while(preferredCompress.equals("")  || !preferredCompressFile.exists()){
                dialog = new OpenDialog("Locate kdu_compress or ojp_compress"); 
                if(null==dialog.getPath()){
                    //@userHitCancel
                    WindowManager.closeAllWindows();
                    IJ.error("You must provide the jp2 compressor location to continue.  Exiting...");
                    throw new Throwable("You must provide the jp2 compressor location to continue!");
                }
                preferredCompress = dialog.getPath();
                preferredCompressFile = new File(preferredCompress);
            }
            theList.put("preferredCompress", preferredCompress); //always keep dirs locally with correct slash for OS
            while(preferredJp2Args.equals("")){
                contentPane = new JPanel();
                //display label and text area side by side in two columns for as many prefs exist
                //Make this resizable.
                contentPane.addHierarchyListener(new HierarchyListener() {
                    public void hierarchyChanged(HierarchyEvent e) {
                     //when the hierarchy changes get the ancestor for the message
                    Window window = SwingUtilities.getWindowAncestor(contentPane);
                     //check to see if the ancestor is an instance of Dialog and isn't resizable
                        if (window instanceof Dialog) {
                            Dialog dialog = (Dialog)window;
                            if (!dialog.isResizable()) {
                            //set resizable to true
                                dialog.setResizable(true);
                            }
                        }
                    }
                }); 
                contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));
                JPanel labelPanel = new JPanel();
                JLabel prefsLabel = new JLabel("Below are the values for JP2 Compression. You can alter these values as needed.");
                prefsLabel.setToolTipText("If you had never set these before, we provide default values.  You can use them or provide your own.");
                labelPanel.add(prefsLabel);
                contentPane.add(labelPanel);
                String arguments = "-rate -,2.4,1.48331273,.91673033,.56657224,.35016049,.21641118,.13374944,.08266171 Creversible=no Clevels=5 Stiles={1024,1024} Cblk={64,64} Cuse_sop=yes Cuse_eph=yes Corder=RPCL ORGgen_plt=yes ORGtparts=R Cmodes=BYPASS -double_buffering 10 -num_threads 4 -no_weights";
                JTextField args = new JTextField(arguments, 85);
                contentPane.add(args);
                //Gather new values from the dialog, reset the labels and update the new values.
                Object[] btns = {"Apply",
                    "Default"};
                int argsResult = JOptionPane.showOptionDialog(null, contentPane, "Approve JP2 Arguments", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, btns, btns[0]);
                
                if (argsResult == JOptionPane.OK_OPTION){
                   preferredJp2Args = args.getText();
                }
                else {
                     //@userHitCancel
                    preferredJp2Args = arguments;
                    JOptionPane.showMessageDialog(null,
                        "You did not provide a jp2 arguments.  We are using the default value.", "Notice",
                        JOptionPane.PLAIN_MESSAGE);
                }
            }
            theList.put("preferredJp2Args", preferredJp2Args);
            String compressLocation = preferredCompressFile.getParent();
            Boolean noClob = noClobber(projDir+"StaticRaking"+File.separator+inFile+".jp2"); 
            String commandString = preferredCompress+" -i "+projDir+"StaticRaking"+File.separator+inFile+".tiff -o "+projDir+"StaticRaking"+File.separator+inFile+".jp2 "+preferredJp2Args;
            if(isWindows){
                p = Runtime.getRuntime().exec(commandString, null, new File(compressLocation)); //compressLocation
                p.waitFor();   
                returnString = projDir+"StaticRaking"+File.separator+inFile+".jp2";
                //preferred compress is kdu_compress.exe (or some other executable).  The args used are from those.  It should be platform independent
            }
            else{
                logService.log().info("Non windows .jp2 creation.  Command below: ");
                logService.log().info(commandString);
                p = Runtime.getRuntime().exec(commandString);
                p.waitFor();   
                returnString = projDir+"StaticRaking"+File.separator+inFile+".jp2";
            }
            preferredCompress = preferredCompress.replace("\\", "/"); //write dirs to prefs file with backslash
            compressString = "preferredCompress="+preferredCompress+System.lineSeparator();
            preferredString = "preferredJp2Args="+preferredJp2Args+System.lineSeparator();
            prefsFileAsText = new String(Files.readAllBytes(spectralPrefsFile.toPath()), "UTF8");
            prefsFileAsText = prefsFileAsText.replaceFirst("preferredCompress=.*\\"+System.lineSeparator(), compressString); //replace the prefs var
            prefsFileAsText = prefsFileAsText.replaceFirst("preferredJp2Args=.*\\"+System.lineSeparator(), preferredString); //replace the prefs var
            Files.write(spectralPrefsFile.toPath(), prefsFileAsText.getBytes()); //rewrite the prefs file
            logService.log().info("Created JP2 "+returnString);
            return returnString;
        }
        
        /** 
        * Used to make sure not to overwrite a file that already exists.  
        * If it finds the existence of the old file, it attaches a date to it so when this one goes to save, it will not get FileAreadyExists notices
        * @param safeName the file name to check
        * @return whether or not the creation and/or rename was successful
        * @throws java.io.IOException
        */
        public Boolean noClobber(String safeName) throws IOException {
            Boolean success = false;
            File oldFile = new File(safeName);
            Path safeNamePath = Paths.get(safeName);
            String verboseDate = "";
            String newFileName = safeName;
            if (oldFile.exists()){
                logService.log().info(safeName+" already exists, so I will rename it to avoid label conflicts.");
                verboseDate = Files.getLastModifiedTime(safeNamePath).toString();
                verboseDate = verboseDate.replace(" ","_");
                verboseDate = verboseDate.replace(":","-");
                newFileName = newFileName.replace(".","("+verboseDate+").");
                File newFileFileName = new File(newFileName);
                success = oldFile.renameTo(newFileFileName);
            }
            else{ //Old file did not exist, so no need to rename and we can return success.
                success = true;
            }
            return success;
        }
        
        /**
        * Get the current time and format it appropriately (20171224_1200)
        * @noparam This function requires no parameters
        * @return: A formatted time String
        */
        public String timestamp() {
            Date currentDate = new Date();
            SimpleDateFormat ft = new SimpleDateFormat ("yyyyMMdd'_'HHmm");
            String dateString = ft.format(currentDate);
            return dateString;
        }
        
        /**
        * Prompt the user about adjusting the brightness of their hemisphere caputre images
        * @param listOfHemisphereCaptures the list of files in the hemishphere folder
        * @exception ImgIOException if image cannot be opened.
        */
        public void promptBrightnessAdjust(File[] listOfHemisphereCaptures) throws ImgIOException, Throwable {
            /* DEBGUGGIN 
            logService.log().info("In brightness adjust.  I should have a list of hemisphere captures.  What is the legnth?");
            logService.log().info(listOfHemisphereCaptures.length);
            logService.log().info("I would like to access an index.  That index is ");
            logService.log().info(Math.round(listOfHemisphereCaptures.length/2));
            logService.log().info(listOfHemisphereCaptures[Math.round(listOfHemisphereCaptures.length/2)].toString());
            logService.log().info("I am looking inside this directory");
            logService.log().info(projectDirectory+"Captures-Hemisphere-Gamma"+File.separator);
            logService.log().warn("I want this image!");
            logService.log().warn(listOfHemisphereCaptures[Math.round(listOfHemisphereCaptures.length/2)].toString());
            END DEBUGGING */
            imp = opener.openImage( listOfHemisphereCaptures[Math.round(listOfHemisphereCaptures.length/2)].toString() );
            imp.setTitle("Preview");
            imp.show();
            String[] brightnessAdjustOptions = new String[3];
            brightnessAdjustOptions[0] = "No";
            brightnessAdjustOptions[1] = "Yes, by normalizing each image to a selected area";
            brightnessAdjustOptions[2] = "Yes, by multiplying all images by a fixed value";
            String[] brightnessAdjustApplies = new String[2];
            brightnessAdjustApplies[0] = "Static raking images only (recommended)";
            brightnessAdjustApplies[1] = "RTI images also";
            contentPane = new JPanel();
            JPanel scrollGrid = new JPanel();
            JPanel scrollGrid2 = new JPanel();
            scrollGrid.setLayout(new BoxLayout(scrollGrid,BoxLayout.PAGE_AXIS));
            scrollGrid2.setLayout(new BoxLayout(scrollGrid2,BoxLayout.PAGE_AXIS));
            contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));
            JLabel directions = new JLabel("Adjust brightness of hemisphere captures?");
            JLabel directions2 = new JLabel("Apply adjustment to which output images?");
            directions2.setBorder(new EmptyBorder(15,0,0,0)); //put some margin/padding around a label
            scrollGrid.add(directions);
            scrollGrid2.add(directions2);
            /**
             * UI for creating the checkbox selections.  
            */
            //There will be a button group for each narrow band capture.  We need to keep track of each group as a distinct object.
            ButtonGroup adjust_radios = new ButtonGroup();
            ButtonGroup apply_radios = new ButtonGroup();
            for (int i=0; i<brightnessAdjustOptions.length; i++) {
                //Create a new button group for this capture
                JRadioButton radioOption = new JRadioButton(brightnessAdjustOptions[i]);
                radioOption.setActionCommand(brightnessAdjustOptions[i]);
                if(i==1){
                    radioOption.setSelected(true);
                }
                adjust_radios.add(radioOption);
                scrollGrid.add(radioOption);
                //Add the button group panel to the overall content container
            } 
            for (int j=0; j<brightnessAdjustApplies.length; j++) {
                //Create a new button group for this capture
                JRadioButton radioOption2 = new JRadioButton(brightnessAdjustApplies[j]);
                radioOption2.setActionCommand(brightnessAdjustApplies[j]);
                if(j==0){
                    radioOption2.setSelected(true);
                }
                apply_radios.add(radioOption2);
                scrollGrid2.add(radioOption2);
                //Add the button group panel to the overall content container
            }    
            JPanel choices = new JPanel();
            choices.add(scrollGrid);
            choices.add(scrollGrid2);
            contentPane.add(scrollGrid);
            contentPane.add(scrollGrid2);
            Object[] transmissiveSourcesBtnLabels = {"Confirm",
                "Skip"};
            int brightResult = JOptionPane.showOptionDialog(null, contentPane, "Adjust Brightness", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, transmissiveSourcesBtnLabels, transmissiveSourcesBtnLabels[0]);
            if (brightResult == JOptionPane.OK_OPTION){
                //Is there an easier way to get the selected btton from a buttonGroup?
                for (Enumeration<AbstractButton> buttons = adjust_radios.getElements(); buttons.hasMoreElements();) {
                    AbstractButton button = buttons.nextElement();
                    //Loop each button and see if it is selected
                    if (button.isSelected()) {
                        //If it is selected, it will have "R", "G", "B". or "None" as its text.  Designate to the appropriate list based on this text.
                       brightnessAdjustOption = button.getText();
                       break;
                    }
                }
                for (Enumeration<AbstractButton> buttons = apply_radios.getElements(); buttons.hasMoreElements();) {
                    AbstractButton button = buttons.nextElement();
                    //Loop each button and see if it is selected
                    if (button.isSelected()) {
                        //If it is selected, it will have "R", "G", "B". or "None" as its text.  Designate to the appropriate list based on this text.
                       brightnessAdjustApply = button.getText();
                       break;
                    }
                }
            }
            else{ 
                brightnessAdjustOption = "No";
            }
            if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                //gd.setVisible(false);
                while(imp.getRoi() == null){
                    dWait = new WaitForUserDialog("Select Area","Draw a rectangle containing the brighest white and darkest black desired then press OK\n(hint: use a large area including spectralon and the object, excluding glare).  Press 'Esc' to quit." );
                    dWait.show();
                    if(dWait.escPressed()){
                        //@userHitCancel
                        WindowManager.closeAllWindows();
                        IJ.error("You must draw a rectangle to continue!  Exiting...");
                        throw new Throwable("You must draw a rectangle to continue!");
                    }
                    if(imp.getRoi() == null){
                        JOptionPane.showMessageDialog(null,
                        "You must draw a rectangle.", "Try Again",
                        JOptionPane.PLAIN_MESSAGE);
                    }
                }
                bounds = imp.getRoi().getBounds();
                region = new RectangleOverlay();
                normX = bounds.x;
                normY = bounds.y;
                normHeight = bounds.height;
                normWidth = bounds.width;
            } 
            else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                boolean properValue = false;
                while(!properValue){
                    contentPane = new JPanel();
                    contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));
                    JPanel labelPanel = new JPanel();
                    JLabel prefsLabel = new JLabel("Enter a valid brightness adjustment between 0.00 and 2.00");
                    JLabel prefsLabel2 = new JLabel("The Preview image will change with your adjustment.");
                    labelPanel.add(prefsLabel);
                    labelPanel.add(prefsLabel2);
                    contentPane.add(labelPanel);
                    JTextField adjustment = new JTextField("1.00", 10);
                    adjustment.getDocument().addDocumentListener(new DocumentListener() {
                        @Override
                        public void changedUpdate(DocumentEvent e) {
                          process();
                        }
                        @Override
                        public void removeUpdate(DocumentEvent e) {
                          process();
                        }
                        @Override
                        public void insertUpdate(DocumentEvent e) {
                          process();
                        }
                        public void process() {
                           try{
                                double previewAdjustVal = 1.00;
                                if (Double.parseDouble(adjustment.getText())<=0 || Double.parseDouble(adjustment.getText())>2.00){
                                    //ignore the value, it won't work in the preview
                                }
                                else{
                                    imp.changes = false;
                                    imp.revert(); //should undo the last brightness change
                                    previewAdjustVal = Double.parseDouble(adjustment.getText());
                                    IJ.run(imp, "Multiply...", "value="+previewAdjustVal+"");
                                }
                           }
                           catch(Exception e){
                               // ignore the value, it won't work in the preview.
                           }
                        }
                    });
                    contentPane.add(adjustment);
                    //Gather new values from the dialog, reset the labels and update the new values.
                    Object[] btns = {"Apply",
                        "Default"};
                    int valueResult = JOptionPane.showOptionDialog(null, contentPane, "Set Brightness Value", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, btns, btns[0]);
                    if (valueResult == JOptionPane.OK_OPTION){
                        try{
                            if (Double.parseDouble(adjustment.getText())<=0 || Double.parseDouble(adjustment.getText())>2.00){
                                properValue = false;
                            }
                            else{
                                normalizationFixedValue = Double.parseDouble(adjustment.getText());
                                properValue = true;
                            }
                        }
                        catch(Exception e){
                            //user did not provide a number, or the number provided was less than 1 or greater than 2.
                            properValue = false;
                            normalizationFixedValue = 1.00;
                            JOptionPane.showMessageDialog(null,
                            "Value not valid", "Try Again",
                            JOptionPane.PLAIN_MESSAGE);
                        }
                    }
                    else {
                        //@userHitCancel
                        properValue = true; //The user hit cancel, so they are tired of seeing the message.  Just default to normal brightness.
                        normalizationFixedValue = 1.00;
                        JOptionPane.showMessageDialog(null,
                        "You did not provide a value, defaulting to 100% brightness", "Notice",
                        JOptionPane.PLAIN_MESSAGE);
                    }
                }
            }
            else{
                logService.log().info("Brightness not modified");
            }
            logService.log().info("Set a fixed brightness value.  It is "+normalizationFixedValue);
            imp.changes=false;
            imp.close();
        } 
        
        /** 
         * identify preferred fitter and exec with arguments
         * @param colorProcess
         * @exception IOException if file is not found.  
        */
        public void runFitter(String colorProcess) throws IOException, Throwable {
            logService.log().info("Running the fitter for "+colorProcess+"...");
            String preferredFitter = theList.get("preferredFitter");
            File preferredHSH;
            String hshLocation = "";
            File fitterFile = new File(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt");
            if(!fitterFile.exists()){
                Files.createFile(fitterFile.toPath());
            }
            String appendString = "";
            while(preferredFitter.equals("")){ //|| !(preferredFitter.endsWith("hshfitter.exe") || preferredFitter.endsWith("cmd") || preferredFitter.endsWith("bash"))
                OpenDialog dialog = new OpenDialog("Locate Preferred RTI Fitter or cmd file for batch processing");
                if(null==dialog.getPath()){
                    //@userHitCancel
                    IJ.error("You must provide the location for the RTI Fitter or cmd file to continue.  Exiting...");
                    throw new Throwable("You must provide the location for the RTI Fitter or cmd file to continue.");
                }
                preferredFitter = dialog.getPath();
            }
            theList.put("preferredFitter", preferredFitter);
            JFrame fitterNoticeFrame = new JFrame("Fitter Working...");
            GenericDialog fitterMessageFrame = new GenericDialog("Try Again");
            contentPane = new JPanel();
            contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));
            JPanel labelPanel = new JPanel();
            JLabel fitterText = new JLabel("Running the fitter.  This could take a while. "+System.lineSeparator()+"  This window will close and a notification"
                + " will appear when the process is complete.  Thank you for your patience.");
            labelPanel.add(fitterText);
            contentPane.add(labelPanel);
            fitterNoticeFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            fitterNoticeFrame.getContentPane().add(contentPane);
            fitterNoticeFrame.pack();
            fitterNoticeFrame.setLocation(screenSize.width/2-fitterNoticeFrame.getSize().width/2, screenSize.height/2-fitterNoticeFrame.getSize().height/2);
            contentPane = new JPanel();
            contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));
            JPanel msgPanel = new JPanel();
            JLabel msgText = new JLabel("The chosen fitter is not supported.  Please use hshfitter.exe or defer with a bash or a cmd file.");
            msgPanel.add(msgText);
            contentPane.add(msgPanel);
            fitterMessageFrame.add(contentPane);
            fitterMessageFrame.pack();
            fitterMessageFrame.setLocation(screenSize.width/2-fitterMessageFrame.getSize().width/2, screenSize.height/2-fitterMessageFrame.getSize().height/2);
            logService.log().info("Preferred fitter is "+preferredFitter);
            appendString = "preferredFitter="+preferredFitter+System.lineSeparator();
            boolean noRun = false;
            String preferredFitterCheck = preferredFitter.toLowerCase();
            if (preferredFitterCheck.endsWith("hshfitter.exe") || preferredFitterCheck.endsWith("hshfitter")) { // use HSH fitter
                preferredFitter =preferredFitter.replace("\\", "/"); //write dir to prefs file with backslash
                String fitterString= "preferredFitter="+preferredFitter+System.lineSeparator();
                prefsFileAsText = prefsFileAsText.replaceFirst("preferredFitter=.*\\"+System.lineSeparator(), fitterString); //replace the prefs var
                Files.write(spectralPrefsFile.toPath(), prefsFileAsText.getBytes()); //rewrite the prefs file
                int hshOrder = Integer.parseInt(theList.get("hshOrder"));
                /**
                 * Could add a message here notifying the user about defaulting hshOrder and hshThreads
                 */
                if (hshOrder < 2 ) hshOrder = 3;
                int hshThreads = Integer.parseInt(theList.get("hshThreads"));
                if (hshThreads < 1 ) hshThreads = 16;
                preferredHSH = new File(preferredFitter);
                hshLocation = preferredHSH.getParent();
                appendString += "Brightness Adjust Option: "+brightnessAdjustOption+System.lineSeparator();
                if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")){
                    appendString += "Normalization area bounds: "+normX+", "+normY+", "+normWidth+", "+normHeight+System.lineSeparator();
                }
                else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                    appendString += "Normalization fixed value: "+normalizationFixedValue+System.lineSeparator();
                }
                if (pcaX > 0) {
                    appendString += "PCA area bounds: "+pcaX+", "+pcaY+", "+pcaWidth+", "+pcaHeight+System.lineSeparator();
                }
                appendString += "Jpeg Quality: "+jpegQuality+" (edit SpectralRTI_Toolkit-prefs.txt to change)"+System.lineSeparator();
                fitterNoticeFrame.setVisible(true);
                String commandString = "";
                if(isWindows){
                    //preferredfitter is hshFitter.exe (or some other executable).  The args used are from those.  It should be platform independent
                    commandString = preferredFitter+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp"+" "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti";
                    logService.log().info("Running the following fitter command...");
                    logService.log().info(commandString);
                    logService.log().info("Working directory for command is "+hshLocation);
                    appendString += "Executing command "+commandString;
                    p = Runtime.getRuntime().exec(commandString, null, new File(hshLocation)); //hshLocation
                    p.waitFor();
                    contentPane.removeAll();
                }
                else{
                    commandString = preferredFitter+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp"+" "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti";
                    appendString += "Executing command "+commandString;
                    p = Runtime.getRuntime().exec(commandString);
                    p.waitFor();
                    contentPane.removeAll();
                }
                fitterNoticeFrame.dispose();
                logService.log().info("End fitter process");
                Files.write(fitterFile.toPath(), appendString.getBytes(), StandardOpenOption.APPEND);
                createWebRTIFiles(colorProcess, "", noRun);
            } 
            else if (preferredFitter.endsWith("cmd")||preferredFitter.endsWith("bash")) {
                noRun = true;
                logService.log().info("Detected the preferred fitter is in fact a cmd or bash file.  This will defer processing.");
                preferredFitter =preferredFitter.replace("\\", "/"); //write dir to prefs file with backslash
                String fitterString= "preferredFitter="+preferredFitter+System.lineSeparator();
                prefsFileAsText = prefsFileAsText.replaceFirst("preferredFitter=.*\\"+System.lineSeparator(), fitterString); //replace the prefs var
                Files.write(spectralPrefsFile.toPath(), prefsFileAsText.getBytes()); //rewrite the prefs file
                fitterNoticeFrame.setVisible(true);
                //This is the deferred batch section.  Just write to the file, do not perform processes.
                int hshOrder = Integer.parseInt(theList.get("hshOrder"));
                if (hshOrder < 2 ) hshOrder = 3;
                int hshThreads = Integer.parseInt(theList.get("hshThreads"));
                if (hshThreads < 1 ) hshThreads = 16;
                String commandString = "";
                appendString += "Brightness Adjust Option: "+brightnessAdjustOption+System.lineSeparator();
                if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                    appendString += "Normalization area bounds: "+normX+", "+normY+", "+normWidth+", "+normHeight+System.lineSeparator();
                } 
                else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                    appendString += "Normalization fixed value: "+normalizationFixedValue+System.lineSeparator();
                }
                if (pcaX > 0) {
                    appendString += "PCA area bounds: "+pcaX+", "+pcaY+", "+pcaWidth+", "+pcaHeight+System.lineSeparator();
                }
                appendString += "Jpeg Quality: "+jpegQuality+" (edit SpectralRTI_Toolkit-prefs.txt to change)"+System.lineSeparator();
                appendString += "Deferring fitter command hshfitter "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti"+System.lineSeparator();
                commandString += "hshfitter "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti"+System.lineSeparator();
                commandString += "webGLRtiMaker "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti -q "+jpegQualityWebRTI+" -r "+ramWebRTI+System.lineSeparator();
                if (webRtiDesired) {
                    String webRtiString = "Creating .wrti file"+System.lineSeparator();
                    webRtiString += "deferring Web RTI Maker command webGLRtiMaker "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti -q "+jpegQualityWebRTI+" -r "+ramWebRTI+System.lineSeparator();
                    appendString += webRtiString;
                }
                Files.write(fitterFile.toPath(), appendString.getBytes(), StandardOpenOption.APPEND);
                Files.write(Paths.get(preferredFitter), commandString.getBytes(), StandardOpenOption.APPEND);
                fitterNoticeFrame.dispose();
                createWebRTIFiles(colorProcess, "", noRun);
                logService.log().info("End fitter process");
            } 
            else if (preferredFitter.endsWith("PTMfitter.exe")) { // use PTM fitter
                //This will show a mesasge saying this fitter is not supported, then restart the process of asking for the fitter again.
                fitterNoticeFrame.dispose();
                fitterMessageFrame.showDialog();
                preferredFitter = "";
                theList.put("preferredFitter", preferredFitter);
                runFitter(colorProcess);
            } 
            else {
                //This will show a mesasge saying this fitter is not supported, then restart the process of asking for the fitter again.
                fitterNoticeFrame.dispose();
                fitterMessageFrame.showDialog();
                preferredFitter = "";
                theList.put("preferredFitter", preferredFitter);
                runFitter(colorProcess);
            }
           
        }
        
        /**
         * Create Light Position file with filenames from newly created series and light positions from previously generated lp file.
         * @param colorProcess The name of the process being performed
         * @param projDir The project directory
         * @throws java.io.IOException
         *@throws java.lang.Throwable
        */
        public void createLpFile(String colorProcess, String projDir) throws IOException, Throwable{
            List<String> listOfLpFiles_list;
            String[] listOfLpFiles;
            String[] lpLines;
            File[] list;
            File folder;
            File lpFile = null;
            if (lpSource.equals("")) { //Then we need to find and set it
                //Check LightPositionData folder
                listOfLpFiles_list = new ArrayList<String>();
                folder = new File(projectDirectory+"LightPositionData"+File.separator);
                if(folder.exists()){
                    list = folder.listFiles();
                    for (File file1 : list) {
                        if (file1.getName().endsWith("lp")) {
                            if(shortName){
                                listOfLpFiles_list.add("..."+file1.getName());
                            }
                            else{
                                listOfLpFiles_list.add(file1.toString());
                            }
                        }
                    }
                }
                else{
                    JOptionPane.showMessageDialog(null, "Please provide LP data in a LightPositionData directory in your project directory.  A Light Position directory was created for you.", "Light Position Data Not Found", JOptionPane.PLAIN_MESSAGE);
                    Files.createDirectory(folder.toPath());
                }    
                //Check assembly-files folder inside LightPositionData folder
                folder = new File(projectDirectory+"LightPositionData"+File.separator+"assembly-files"+File.separator);
                if(folder.exists()){
                    list = folder.listFiles();
                    for (int i=0; i<list.length; i++) {
                        if (list[i].getName().endsWith("OriginalSet.lp")) { //ignore this one

                        } 
                        else if (list[i].getName().endsWith("lp")) {
                            if(shortName){
                                listOfLpFiles_list.add("...assembly-files"+File.separator+list[i].getName());
                            }
                            else{
                                listOfLpFiles_list.add(list[i].toString());
                            }
                        }
                    }
                }             
                listOfLpFiles = new String[listOfLpFiles_list.size()];
                listOfLpFiles_list.toArray(listOfLpFiles);
                while(lpSource.equals("") || null == lpFile || !lpFile.exists()){
                    if(listOfLpFiles_list.size() == 1){
                        lpSource = listOfLpFiles_list.get(0);
                    } 
                    else if(listOfLpFiles_list.isEmpty()){
                        OpenDialog dialog = new OpenDialog("Locate Light Position Source File"); 
                        if(null==dialog.getPath()){
                            //@userHitCancel
                            WindowManager.closeAllWindows();
                            IJ.error("You must provide the location for the light position source file to continue.  Exiting...");
                            throw new Throwable("You must provide the location for the light position source file to continue.");
                        }
                        logService.log().info("Selected lp source of "+dialog.getPath());
                        lpSource = dialog.getPath();
                    }
                    else{
                        contentPane = new JPanel();
                        JPanel scrollGrid = new JPanel();
                        scrollGrid.setLayout(new BoxLayout(scrollGrid,BoxLayout.PAGE_AXIS));
                        contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));
                        JPanel labelPanel = new JPanel();
                        JLabel directions = new JLabel("Choose Light Position File.");
                        labelPanel.add(directions);
                        contentPane.add(labelPanel);
                        /**
                         * UI for creating the checkbox selections.  
                         * @see shortName
                        */
                        ButtonGroup capture_radios = new ButtonGroup();
                        for (int i=0; i<listOfLpFiles.length; i++) {
                            JRadioButton radioOption = new JRadioButton(listOfLpFiles[i]);
                            radioOption.setActionCommand(listOfLpFiles[i]);
                            if(i==0){
                                radioOption.setSelected(true);
                            }
                            capture_radios.add(radioOption);
                            //Add the button group panel to the overall content container
                            scrollGrid.add(radioOption);                   
                        } 
                        JScrollPane spanel = new JScrollPane(scrollGrid);
                        spanel.setBorder(BorderFactory.createEmptyBorder());
                        spanel.setMaximumSize(bestFit);    
                        contentPane.add(spanel);
                        Object[] btns = {"Confirm",
                            "Quit"};
                        int lpFileResult = JOptionPane.showOptionDialog(null, contentPane, "Select Light Position Source File", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, btns, btns[0]);
                        if (lpFileResult == JOptionPane.OK_OPTION){
                            for (Enumeration<AbstractButton> buttons = capture_radios.getElements(); buttons.hasMoreElements();) {
                                AbstractButton button = buttons.nextElement();
                                //Loop each button and see if it is selected
                                if (button.isSelected()) {
                                   lpSource = button.getText();
                                   break;
                                }
                            }
                        }
                        else{ 
                            //@userHitCancel is it OK to default to the first source?
                            WindowManager.closeAllWindows();
                            IJ.error("You must make a selection for which light position file to use.  Exiting...");
                            throw new Throwable("You must make a selection for which light position file to use.");
                        }
                    }
                    lpSource = lpSource.replace("...", projectDirectory+"LightPositionData"+File.separator);
                    lpFile = new File(lpSource);
                }
            }
            else{
                lpSource = lpSource.replace("...", projectDirectory+"LightPositionData"+File.separator);
                lpFile = new File(lpSource);
            }
            BufferedReader lpFileReader = Files.newBufferedReader(lpFile.toPath());
            String line= "";
            String lpFileAsText = "";
            while((line=lpFileReader.readLine()) != null){
                lpFileAsText += line+System.lineSeparator(); 
            }
            lpFileReader.close();
            lpLines = lpFileAsText.split("\n");
            noClobber(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp");
            File newLpFile = new File(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp");
            if(!newLpFile.exists()){
                Files.createFile(newLpFile.toPath());
            }
            String newLpLine = "";
            for (int i=0;i<lpLines.length;i++) {
                newLpLine = lpLines[i];
                newLpLine = newLpLine.replace("\\", "/"); //simplest to avoid a backslash on the right side of a regular expression replace in the next few lines
                String funnyProjectDirectory = projectDirectory.replace("\\","/"); //Detect this slash and replace.
                newLpLine = newLpLine.replace("LightPositionData/jpeg-exports/",colorProcess+"RTI/"+colorProcess+"_");
                newLpLine = newLpLine.replace("canonical",funnyProjectDirectory+colorProcess+"RTI/"+colorProcess+"_"+projectName+"_RTI");
                newLpLine = newLpLine + System.lineSeparator();
                newLpLine = newLpLine.replace("/", File.separator);
                Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp"), newLpLine.getBytes(), StandardOpenOption.APPEND);
            }
        }
        
        /**
         * Create necessary web RTI files and start a thread for the webRTIMaker
         * @param colorProcess The name of the process being performed
         * @param rtiImage the path to an RTI image or an empty string if no rti image yet
         * @param noRun flagging whether or not the user decided to defer and just write the command to the batch file instead of running it now.
         * @throws java.io.IOException
         *@throws java.lang.Throwable
        */
        private void createWebRTIFiles(String colorProcess, String rtiImage, boolean noRun) throws IOException, InterruptedException, Throwable{
            logService.log().info("Create WebRTI for color process "+colorProcess+"...");
            File webRTIFolder;
            File fitterFile = new File(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt");
            if(!rtiImage.equals("")){
                //The user has chosen to just create a webrti from an existing RTI Image, we do not know for what process.
                File imgFile = new File(rtiImage);
                String RTIDir = imgFile.getParent();
                logService.log().info("RTI DIR: "+RTIDir);
                //Make webRTI files in the same directory as the provided rti image  
                colorProcess = RTIDir;
                webRTIFolder = new File(RTIDir);
            }
            else{
                //Prepare to make RTI files from plugin generated rti file
                colorProcess += "RTI";
                webRTIFolder = new File(projectDirectory+colorProcess+File.separator);
            }
            logService.log().info("Do I have color process name? " + colorProcess);
            String webRtiMaker = "";
            JFrame noticeFrame = new JFrame("WebRTI Maker Working...");
            contentPane = new JPanel();
            contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));
            JPanel labelPanel = new JPanel();
            JLabel fitterText = new JLabel("Creating webRTI files.  This could take a while.  This window will close and a notification"
                    + " will appear when the process is complete.  Thank you for your patience.");
            labelPanel.add(fitterText);
            contentPane.add(labelPanel);
            noticeFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            noticeFrame.getContentPane().add(contentPane);
            noticeFrame.pack();
            noticeFrame.setLocation(screenSize.width/2-noticeFrame.getSize().width/2, screenSize.height/2-noticeFrame.getSize().height/2);
            String webRtiString = "<html lang=\"en\" xml:lang=\"en\"> <head> <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /> <title>WebRTI "+projectName+"_"+colorProcess+"</title> <link type=\"text/css\" href=\"css/ui-lightness/jquery-ui-1.10.3.custom.css\" rel=\"Stylesheet\"> <link type=\"text/css\" href=\"css/webrtiviewer.css\" rel=\"Stylesheet\"> <script type=\"text/javascript\" src=\"js/jquery.js\"></script> <script type=\"text/javascript\" src=\"js/jquery-ui.js\"></script> <script type=\"text/javascript\" src=\"spidergl/spidergl_min.js\"></script> <script type=\"text/javascript\" src=\"spidergl/multires_min.js\"></script> </head> <body> <div id=\"viewerContainer\"> <script  type=\"text/javascript\"> createRtiViewer(\"viewerContainer\", \""+projectName+"_"+colorProcess+"_"+startTime+"\", $(\"body\").width(), $(\"body\").height()); </script> </div> </body> </html>";
            if (webRtiDesired) {
                if(noRun){
                    /* 
                       The user has deferred creating their RTI file.  This means we can't run the webGLRTIMaker command because it will fail without an .rti image creates
                       Just make the .wrti file so when the deferred command runs and makes the folders for the .wrti file, it will just work.
                    */
                    Files.createFile(new File(webRTIFolder+File.separator+projectName+"_"+colorProcess+"_"+startTime+"_wrti.html").toPath());
                    Files.write(Paths.get(webRTIFolder+File.separator+projectName+"_"+colorProcess+"_"+startTime+"_wrti.html"), webRtiString.getBytes(), StandardOpenOption.APPEND);
                }
                else{
                    noticeFrame.setVisible(true);  
                    logService.log().info("I have found a desire for WebRTI...input");
                    String webString;
                    String webRTIDir;
                    webRtiMaker = theList.get("webRtiMaker");
                    webRtiMaker = webRtiMaker.replace("/", File.separator); //ensure dir has correct slash for OS
                    webRtiMaker = webRtiMaker.replace("\\", File.separator); //ensure dir has correct slash for OS
                    File webRTIFile = new File(webRtiMaker);
                    while(webRtiMaker.equals("") || !webRTIFile.exists()) {
                        OpenDialog dialog2 = new OpenDialog("Locate webGLRtiMaker.exe");
                        if(null==dialog2.getPath()){
                            //@userHitCancel
                            WindowManager.closeAllWindows();
                            IJ.error("You must provide the webGLRtiMaker.exe location to continue.  Exiting...");
                            throw new Throwable("You must provide the webGLRtiMaker.exe location to continue.");
                        }
                        webRtiMaker = dialog2.getPath();
                        webRTIFile = new File(webRtiMaker);
                    }
                    webRTIDir = new File(webRtiMaker).getParent();
                    /**
                     * if the user provided an RTI image location, use that.  Otherwise, use the one the fitter made. 
                     */
                    if(rtiImage.equals("")){
                        rtiImage = projectDirectory+colorProcess+File.separator+projectName+"_"+colorProcess+"_"+startTime+".rti";
                    }
                    logService.log().info("I need to know what the webRTI maker is..."+webRtiMaker);
                    if (!webRTIFolder.exists() && rtiImage.equals("")) {
                        //Make sure the directory we want to use exists if it is a user provided rti file.
                        Path createPath = webRTIFolder.toPath();
                        Files.createDirectory(createPath);
                        logService.log().info("A directory has been created for the Web RTI file at "+webRTIFolder.toString());
                    }
                    String commandString= "";
                    String appendString = "";
                    File wrti = new File(webRTIFolder+File.separator+projectName+"_"+colorProcess+"_"+startTime+"_wrti.html");
                    if(isWindows){
                        commandString = webRtiMaker+" "+rtiImage+" -q "+jpegQualityWebRTI+" -r "+ramWebRTI;
                        logService.log().info("Running the webRTICommand...");
                        logService.log().info(commandString); 
                        appendString = "Executing command "+commandString+System.lineSeparator();
                        p2 = Runtime.getRuntime().exec(commandString, null, new File(webRTIDir)); //hshLocation
                        p2.waitFor();
                        if(!Files.exists(wrti.toPath())){
                            Files.createFile(wrti.toPath());
                        }
                        Files.write(wrti.toPath(), webRtiString.getBytes());
                    }
                    else{
                        commandString = webRtiMaker+" "+rtiImage+" -q "+jpegQualityWebRTI+" -r "+ramWebRTI;
                        logService.log().info("Running the webRTICommand...");
                        logService.log().info(commandString); 
                        appendString = "Executing command "+commandString+System.lineSeparator();
                        p2 = Runtime.getRuntime().exec(commandString);
                        p2.waitFor();
                        if(!Files.exists(wrti.toPath())){
                            Files.createFile(wrti.toPath());
                        }
                        Files.write(wrti.toPath(), webRtiString.getBytes());
                    }
                    Files.write(fitterFile.toPath(), appendString.getBytes(), StandardOpenOption.APPEND);
                    noticeFrame.dispose();  
                    theList.put("webRtiMaker", webRtiMaker); //always keep locally with correct slash for OS
                    webRtiMaker =webRtiMaker.replace("\\", "/"); //always write dirs to prefs file with backslash
                    webString = "webRtiMaker="+webRtiMaker;
                    prefsFileAsText = prefsFileAsText.replaceFirst("webRtiMaker=.*\\"+System.lineSeparator(),webString+System.lineSeparator()); //replace the prefs var
                    Files.write(spectralPrefsFile.toPath(), prefsFileAsText.getBytes()); //rewrite the prefs file
                }
            }
            else{ 
                //webRTIDesired was false.  Give a message? 
            }
        }
        
	/**
	 * Main method so imageJ can start up the plugin.
	 *
	 * For debugging, it is convenient to have a method that starts ImageJ, loads
	 * an image and calls the plugin, e.g. after setting breakpoints.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
            // set the plugins.dir property to make the plugin appear in the Plugins menu
            System.out.println("Hello Word 1");
            long startTime = System.nanoTime();
            Class<?> clazz = SpectralRTI_Toolkit.class;
            String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
            String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
            System.setProperty("plugins.dir", pluginsDir);                
            final ImageJ IJinstance = net.imagej.Main.launch(args);
            IJinstance.command().run(SpectralRTI_Toolkit.class, false);
            long endTime = System.nanoTime();
            long totalTime = endTime - startTime;
            System.out.println("Finished processing MAIN");
            System.out.println("It took "+(totalTime/1000)+" seconds.");
	}
      
}
