/**
 * @author  Bryan Haberberger
 * @version 0.5
 * @since   07/01/2017
 * <h1>Spectral RTI Toolkit ImageJ2 Java Plugin</h1>
 * <p>
    * Created by the Walter J Ong S.J. Center for Digital Humanities at Saint Louis University.  
    * Hosted at the Center's GitHub repository
    * https://github.com/CenterForDigitalHumanities/SpectralRTI_Toolkit
 * </p>
 * <p>
    * This was originally written as an ImageJ Macro by Todd Hanneken.  Hosted in Todd's repository at
    * https://github.com/thanneken/SpectralRTI_Toolkit
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
  *     <li> NEW </li>
  *     <li> NEW </li>
  *     <li> NEW </li>
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
import ij.plugin.FolderOpener;


//ImageJ2 specific imports
import net.imagej.ImageJ; 
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.log.LogService;
import net.imglib2.img.Img;
import io.scif.img.ImgIOException;
import net.imglib2.img.ImagePlusAdapter; //Wraps ij.ImagePlus into an ImgLib2 image (ImgPlus but acts like ImagePlus)
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
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
import java.util.Collections;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import org.apache.commons.io.comparator.NameFileComparator;

/**
 * @author bhaberbe
 * @see the @Plugin tag here, it allows me to define where I want this to show up in the ImageJ menu.
 * The class to be be implemented as an ImageJ command.  Does not need an image opened already.  
 * This is compliant with ImageJ2 and intended to be used with the Fiji version.
 */
@Plugin(type = Command.class, menuPath = "Plugins>SpectralRTI_Toolkit")  
public class SpectralRTI_Toolkit implements Command {
        private Context context;
        
        /** The global ImagePlus object to be used throughout.  Used with the IJ library for ImageJ functionality, supported by IJ2. */
        protected ImagePlus imp;
        
        /** The global ImgLib2 compatible Img type object to be used throughout.  Used with the ImgLib2 library. */
        protected Img< FloatType > imglib2_img;       
        
        private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        private Dimension minSize = new Dimension(200, 200);
        
        int prefW = (int) (screenSize.width*.85);
        int prefH = (int) (screenSize.height*.85);
        private Dimension preferredSize = new Dimension(prefW, prefH);
        
        private Dimension bestFit = new Dimension(screenSize.width-20, screenSize.height-20);
        
        /** The logger for the given application, in this case ImageJ */
        @Parameter
        private LogService logService;
                
        //SRTI vars
        private Process p;
        private Process p2;
        private int jpegQuality = 100; //maximize quality for non-distribution phases
        private final int jpegQualityWebRTI = 100; //lower for final distribution
        private final int ramWebRTI = 8192;
        private String brightnessAdjustOption = "";
        private String brightnessAdjustApply = "";
        private String transmissiveSource= ""; //(thanks Kathryn!)
	private int normX;
	private int normY;
	private int normWidth;
	private int normHeight;
	private int normalizationFixedValue;
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
        private String justFileName = "";
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
        static
        {
            theList = new HashMap<>();
            theList.put("preferredCompress", "");
            theList.put("preferredJp2Args", "");
            theList.put("preferredFitter", "");
            theList.put("jpegQuality", "0");
            theList.put("hshOrder", "0");
            theList.put("hshThreads", "0");
            theList.put("webRtiMaker", "");
        }
        private JPanel contentPane = new JPanel();
        
        final ImageJ ij2 = new ImageJ();
        //End SRTI vars
	public double value;
        public String name;
        public File spectralPrefsFile = new File("SpectralRTI_Toolkit-prefs.txt");//This is in the base fiji folder. 
        public String prefsFileAsText = "";
                
        private void testCode() throws IOException, Throwable{
            webRtiDesired = true;
            createWebRTIFiles("AccurateColor", "");
        }
        
        private void theMacro_tested() throws IOException, Throwable{
            //want these variables to be accessible across functions and to reset each time the macro is run
            startTime = timestamp();
            File accurateColorSource = null;
            GenericDialog prefsDialog = new GenericDialog("Consult Preferences");
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
            Boolean shortName = false; //BHTODO implement short vs long file name user preference.   
            File[] listOfAccurateColorSources = new File[0];
            File[] listOfAccurateColorFiles = new File[0];
            File[] listOfNarrowbandCaptures = new File[0];
            File[] listOfHemisphereCaptures = new File[0];
            String csSource = "";
            boolean swapBack = false;     
            Concatenator con = new Concatenator();
            
            /*
                *Consult user for the project directory
                *@exception Kill if no directory provided
            */
            file_dialog = new DirectoryChooser("Choose the Project Directory"); 
            projectDirectory = file_dialog.getDirectory();
            logService.log().info("Project directory is ...  "+projectDirectory+" ...");
            if(projectDirectory == null || projectDirectory.equals("")){
                IJ.error("You must provide a project directory to continue.");
                throw new Throwable("You must provide a project directory."); //DIE if now directory provided
            }
            else{
                projectDirectory = projectDirectory.replace("\\",File.separator);
            }
                                    
            /*
             *consult with user about values stored in prefs file in base fiji folder.
            */
            if (spectralPrefsFile.exists()) { //If this exists, overwrite the labels and show a dialog with the settings
                prefsDialog.addMessage("The following settings are remembered from the configuration file or a previous run.\nEdit or clear as desired.");
                prefsFileAsText = new String(Files.readAllBytes(spectralPrefsFile.toPath()), "UTF8");
                prefs = prefsFileAsText.split(System.lineSeparator());
                logService.log().info(Arrays.toString(prefs));
                for (int i=0;i<prefs.length;i++){
                    //Swap the labels out for presentation
                    String key = prefs[i].substring(0, prefs[i].indexOf("="));
                    key = key.replace("preferredCompress","JP2 Compressor");
                    key = key.replace("preferredJp2Args","JP2 Arguments");
                    key = key.replace("preferredFitter","HSH Fitter");
                    key = key.replace("jpegQuality","JPEG Quality");
                    key = key.replace("hshOrder","HSH Order");
                    key = key.replace("hshThreads","HSH Threads");
                    key = key.replace("webRtiMaker","Web RTI Maker");
                    key = key.replace("shortFileNames","Short File Names");
                    String value1 = prefs[i].substring(prefs[i].indexOf("=")+1); //Pre-populate choices
                    prefsDialog.addStringField(key, value1, 80);
                }
                prefsDialog.setMaximumSize(bestFit);
                //prefsDialog.setSize(preferredSize);
                prefsDialog.showDialog();
                swapBack = true;
            }
            else{
                GenericDialog noPrefs = new GenericDialog("No preference file found");
                noPrefs.addMessage("A prefs file will be created for you to store your choices in later sessions.");
                noPrefs.setMaximumSize(bestFit);
                noPrefs.showDialog();
                logService.log().warn("We are making a new prefs file with the empty defaults.");
                /**
                    *This will put the prefs file the folder that ImageJ.exe is run out of.  Do we want a prefs directory inside a project folder instead? 
                    *@see projectDirectory 
                */
                Files.createFile(spectralPrefsFile.toPath()); 
                Files.write(Paths.get(spectralPrefsFile.toString()), ("preferredCompress="+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                Files.write(Paths.get(spectralPrefsFile.toString()), ("preferredJp2Args="+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                Files.write(Paths.get(spectralPrefsFile.toString()), ("preferredFitter="+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                Files.write(Paths.get(spectralPrefsFile.toString()), ("jpegQuality=0"+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                Files.write(Paths.get(spectralPrefsFile.toString()), ("hshOrder=0"+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                Files.write(Paths.get(spectralPrefsFile.toString()), ("hshThreads=0"+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                Files.write(Paths.get(spectralPrefsFile.toString()), ("webRtiMaker="+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                Files.write(Paths.get(spectralPrefsFile.toString()), ("shortFileNames=false"+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
            }
            //Gather new values from the dialog, reset the labels and update the new values.
            if(swapBack){
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
                    String value2 = prefsDialog.getNextString(); //Gather new information
                    if(key.equals("shortFileNames")){
                        shortName = (value2.equals("true") || value2.equals("yes"));
                    }
                    theList.put(key,value2);
                    prefsFileAsText = prefsFileAsText.replaceFirst(key+"=.*\\"+System.lineSeparator(), key+"="+value2+System.lineSeparator()); //replace the prefs var
                }
                Files.write(spectralPrefsFile.toPath(), prefsFileAsText.getBytes()); //rewrite the prefs file
            }
            else{
                logService.log().info("A prefs file was just created.");
            }
            jpegQuality = ij.plugin.JpegWriter.getQuality();
            int jpq = Integer.parseInt(theList.get("jpegQuality"));
            if (jpq > 0){
                jpegQuality = jpq;
            }
            else{
                /**
                 * Don't actually write this to the file, force the user to edit the preference file themselves.
                 * @see appendString variable
                 */
            }
            IJ.run("Input/Output...","jpeg="+jpegQuality);
            
            listOfHemisphereCaptures = getHemisphereCaptures(projectDirectory+"Captures-Hemisphere-Gamma"+File.separator);
            Arrays.sort(listOfHemisphereCaptures, NameFileComparator.NAME_COMPARATOR);
            File light_position_dir = new File(projectDirectory+"LightPositionData"+File.separator);
            File accurate_color_dir = new File(projectDirectory+"AccurateColor"+File.separator);
            File narrow_band_dir = new File(projectDirectory+"Captures-Narrowband-NoGamma"+File.separator);
            File pseudo_color_dir = new File(projectDirectory+"PseudocolorRTI"+File.separator);
            File extended_spectrum_dir = new File(projectDirectory+"ExtendedSpectrumRTI"+File.separator);
            File static_ranking_dir = new File(projectDirectory+"StaticRaking"+File.separator);
            File transmissive_gamma_dir = new File(projectDirectory+"Captures-Transmissive-Gamma"+File.separator);
            File projectFile = new File(projectDirectory);
            if(!projectFile.exists()){
                IJ.error("Problem with the project directory.  I do not think it exists...");
                throw new Throwable("Problem with the project directory.  I do not think it exists...");
            }
            projectName = projectFile.getName();
            File hemi_gamma_dir = new File(projectDirectory+"Captures-Hemisphere-Gamma"+File.separator);

            if (!hemi_gamma_dir.exists()) {
                Path createPath = hemi_gamma_dir.toPath();
                Files.createDirectory(createPath);
                logService.log().info("A directory has been created for the Hemisphere Captures at "+projectDirectory+"Captures-Hemisphere-Gamma"+File.separator);
                hemi_gamma_dir = new File(createPath.toString());
            }
            listOfHemisphereCaptures = hemi_gamma_dir.listFiles();
            while (listOfHemisphereCaptures.length < 30 && IJ.showMessageWithCancel("Please Populate Hemisphere Captures","The software expects at least 30 images in HemisphereCaptures folder.\nPlease populate the folder and press Ok to continue, or cancel.")){
                listOfHemisphereCaptures = hemi_gamma_dir.listFiles();
            }
            if(listOfHemisphereCaptures.length < 30){
                IJ.error("There must be at least 30 images in the hemisphere caputres folder to continue.  Please populate for next time.");
                throw new Throwable("There must be at least 30 images in the hemisphere caputres folder to continue.  Please populate for next time.");
            }
            if ( light_position_dir.exists() ){ 
                lpDesired = false;
            }
            if (accurate_color_dir.exists() ){
                listOfAccurateColorFiles = accurate_color_dir.listFiles();
                if (listOfAccurateColorFiles.length<1) acRtiDesired = false;
            }
            if(narrow_band_dir.exists()){
                listOfNarrowbandCaptures = narrow_band_dir.listFiles();
            }
            if (listOfNarrowbandCaptures.length<9) {
                xsRtiDesired=false;
                psRtiDesired=false;
            }
            if (pseudo_color_dir.exists()) psRtiDesired = false;
            if (extended_spectrum_dir.exists()) xsRtiDesired = false;
            
            GenericDialog tasksDialog = new GenericDialog("Select tasks");
            tasksDialog.addMessage("Select the tasks you would like to complete");
            tasksDialog.addCheckbox("Light Position Data",lpDesired);
            tasksDialog.addCheckbox("Accurate ColorRTI",acRtiDesired);
            tasksDialog.addCheckbox("Accurate Color Static Raking",acRakingDesired);
            tasksDialog.addCheckbox("Extended Spectrum RTI",xsRtiDesired);
            tasksDialog.addCheckbox("Extended Spectrum Static Raking",xsRakingDesired);
            tasksDialog.addCheckbox("Pseudocolor RTI",psRtiDesired);
            tasksDialog.addCheckbox("Pseudocolor Static Raking",psRakingDesired);
            tasksDialog.addCheckbox("Custom RTI",false);
            tasksDialog.addCheckbox("Custom Static Raking",false);
            tasksDialog.addCheckbox("WebRTI",false);
            tasksDialog.addMessage("Set your file name appearance preference below");
            tasksDialog.addCheckbox("Short File Names", shortName);
            tasksDialog.showDialog();
            if (tasksDialog.wasCanceled()) {
                //@userHitCancel
                IJ.error("You must provide a task set to continue.");
                throw new Throwable("You must provide a task set to continue.");
            }
            
            lpDesired = tasksDialog.getNextBoolean();
            acRtiDesired = tasksDialog.getNextBoolean();
            acRakingDesired = tasksDialog.getNextBoolean();
            xsRtiDesired = tasksDialog.getNextBoolean();
            xsRakingDesired = tasksDialog.getNextBoolean();
            psRtiDesired = tasksDialog.getNextBoolean();
            psRakingDesired = tasksDialog.getNextBoolean();
            csRtiDesired = tasksDialog.getNextBoolean();
            csRakingDesired = tasksDialog.getNextBoolean();
            webRtiDesired = tasksDialog.getNextBoolean();
            shortName = tasksDialog.getNextBoolean(); //This is a preference, must write to prefs file
            prefsFileAsText = new String(Files.readAllBytes(spectralPrefsFile.toPath()), "UTF8");
            String filePreferenceString = "shortFileNames="+shortName+System.lineSeparator();
            prefsFileAsText = prefsFileAsText.replaceFirst("shortFileNames=.*\\"+System.lineSeparator(), filePreferenceString); //replace the prefs var
            Files.write(spectralPrefsFile.toPath(), prefsFileAsText.getBytes()); //rewrite the prefsFileprefs file
            
            /** DEBUGGING **/
            logService.log().info("Variable States listed below!");
            logService.log().info("lpDesired: "+lpDesired);
            logService.log().info("acRtiDesired: "+acRtiDesired);
            logService.log().info("acRakingDesired: "+acRakingDesired);
            logService.log().info("xsRtiDesired: "+xsRtiDesired);
            logService.log().info("xsRakingDesired: "+xsRakingDesired);
            logService.log().info("psRtiDesired: "+psRtiDesired);
            logService.log().info("psRakingDesired: "+psRakingDesired);
            logService.log().info("csRtiDesired: "+csRtiDesired);
            logService.log().info("csRakingDesired: "+csRakingDesired);
            logService.log().info("webRtiDesired: "+webRtiDesired);
            logService.log().info("shotFileNames: "+shortName);
            /** END DEBUGGING **/
            
            //Maybe denote these as at least one required in the UI window. 
            if(!(acRakingDesired || acRtiDesired || xsRtiDesired || xsRakingDesired || psRtiDesired || psRakingDesired || csRtiDesired || csRakingDesired)){
                if(webRtiDesired){ //If this is the only option selected, allow the user to tell us where the RTI image is for processing
                    OpenDialog rti_image = new OpenDialog("Locate RTI image to make into WebRTI.");
                    String rtiImageToUse = rti_image.getPath();
                    createWebRTIFiles("", rtiImageToUse);
                }
                else{
                    IJ.error("You must provide at least one task.");
                    throw new Throwable("You must provide at least one task set to continue.");
                }
            }
            if (acRakingDesired || acRtiDesired || xsRtiDesired || xsRakingDesired || psRtiDesired || psRakingDesired || csRtiDesired || csRakingDesired){
                if (brightnessAdjustOption.equals("")) promptBrightnessAdjust(listOfHemisphereCaptures);
                logService.log().info("Back in main macro after brightness adjust prompt");
            }
            if (acRakingDesired || xsRakingDesired || psRakingDesired || csRakingDesired){
                if (!static_ranking_dir.exists()) {
                    Path staticFilePath = static_ranking_dir.toPath();
                    Files.createDirectory(staticFilePath);
                    logService.log().info("A directory has been created for lossless static raking images at "+projectDirectory+"StaticRaking"+File.separator);
                }
                File[] listOfTransmissiveSources_dir = new File[0];
                ArrayList<String> listOfTransmissiveSources_list = new ArrayList<String>();
                ArrayList<String> listOfTransmissiveSources_short = new ArrayList<String>();
                String[] listOfTransmissiveSources = new String[0];
                if(transmissive_gamma_dir.exists()){
                    listOfTransmissiveSources_dir=transmissive_gamma_dir.listFiles();
                    for (File f : listOfTransmissiveSources_dir) {
                        listOfTransmissiveSources_list.add(f.toString());
                        listOfTransmissiveSources_short.add("..."+f.getName());
                    }
                }
                if(shortName){
                    listOfTransmissiveSources_short.toArray(listOfTransmissiveSources); //will this work for an array of 0?
                }
                else{
                   listOfTransmissiveSources_list.toArray(listOfTransmissiveSources); //will this work for an array of 0? 
                }
                
                if (listOfTransmissiveSources.length == 1){ // no opt out of creating a transmissive static if transmissive folder is populated, but not a problem
                    transmissiveSource = listOfTransmissiveSources[0];
                } 
                else if (listOfTransmissiveSources.length > 1) {
                    GenericDialog transSourceDialog = new GenericDialog("Select Transmissive Source");
                    transSourceDialog.addMessage("Select Transmissive Source. ");
                    // Yikes how could I set tooltips on these to reveal full names in cases of shortName preference?
                    transSourceDialog.addRadioButtonGroup("File: ", listOfTransmissiveSources, listOfTransmissiveSources.length, 1, listOfTransmissiveSources[0]);
                    transSourceDialog.setMaximumSize(bestFit);
                    transSourceDialog.showDialog();
                    if(transSourceDialog.wasCanceled()){ 
                        //@userHitCancel is it OK to default to the first source?
                        transmissiveSource = listOfTransmissiveSources[0];
                    }
                    else{
                        transmissiveSource = transSourceDialog.getNextRadioButton();
                    }
                } 
                else if (listOfTransmissiveSources.length == 0) {
                    transmissiveSource = "";
                }

                boolean[] defaults = new boolean[listOfHemisphereCaptures.length];

                contentPane = new JPanel();
                JPanel scrollGrid = new JPanel();
                scrollGrid.setLayout(new GridLayout(20, 0, 0, 0));
                contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));
                JPanel labelPanel = new JPanel();
                JLabel selectLightPositions = new JLabel("Select light positions for lossless static raking images");
                labelPanel.add(selectLightPositions);
                contentPane.add(labelPanel);
                
                //Make JOptionPane's scrollable when feeding in panel.
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

                JScrollPane spanel = new JScrollPane(scrollGrid);
                spanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                spanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                spanel.setPreferredSize(preferredSize);     
                contentPane.add(spanel);
                int result = JOptionPane.showConfirmDialog(null, contentPane, "Select Light Positions", JOptionPane.OK_CANCEL_OPTION);
                boolean atLeastOne = false;
                if (result == JOptionPane.OK_OPTION){
                    for(JCheckBox check : positions){
                        listOfRakingDirections.add(check.isSelected());
                        if(check.isSelected()){
                            atLeastOne = true;
                        }
                    }
                    logService.log().info("List of Raking directions (T/F):");
                    logService.log().info(listOfRakingDirections);
                }
                else {
                    //@userHitCancel
                    //Pane was cancelled or closed.  How should i handle (@userHitCancel).  Make them all false?
                    for(JCheckBox check : positions){
                        listOfRakingDirections.add(Boolean.FALSE);
                    }
                }
                if(!atLeastOne){
                    //Does the user have to make at least one selection?
                    IJ.error("You must make at least one selection to continue!");
                    throw new Throwable("You must make at least one selection to continue!");
                }
            }
            else { //We already have the list initiated, so do nothing
                listOfRakingDirections = new ArrayList<>();
                while(listOfRakingDirections.size() < listOfHemisphereCaptures.length) listOfRakingDirections.add(Boolean.FALSE);
                logService.log().info("We already have the list initiated, so do nothing.  Raking is not desired.");
            }
            System.out.println("listOfRakingDirections length is "+listOfRakingDirections.size());           
            if (xsRtiDesired || xsRakingDesired){ // only interaction here, processing later
		/**
                 * @see create a dialog suggesting and confirming which narrowband captures to use for R,G,and B
                 */
                String[] rgbnOptions = new String[4];
                rgbnOptions[0] = "R";
                rgbnOptions[1] = "G";
                rgbnOptions[2] = "B";
                rgbnOptions[3] = "none";
                String defaultRange = "";
                String rangeChoice = "";
                boolean atLeastOneR = false;
                boolean atLeastOneG = false;
                boolean atLeastOneB = false;

                logService.log().info("List of narrow band captures for xsRTI or xsRaking");
                logService.log().info(Arrays.toString(listOfNarrowbandCaptures));
                if (listOfNarrowbandCaptures.length<9) { 
                    IJ.error("You must have 9 or more narrow band captures for Extended Spectrum!");
                    throw new Throwable("You must have 9 or more narrow band captures for Extended Spectrum!");
                }
                contentPane = new JPanel();
                contentPane.setLayout(new GridLayout(0, 1, 1, 1)); //Just want one column, as tall as it needs to be (scroll vertical)

                JPanel labelPanel = new JPanel();
                JLabel assignNarrowband = new JLabel("Assign each narrowband capture to the visible range of R, G, B, or none.  You must provide at least one selection for each visible range R, G and B.");
                labelPanel.add(assignNarrowband);
                contentPane.add(labelPanel);                

                //Make JOptionPane's scrollable when feeding in panel.
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
                    contentPane.add(jlabel);
                    JPanel contentGroup = new JPanel();
                    contentGroup.setLayout(new GridLayout(1,1,1,1)); // Want radio options in one row, should only need a single column.
                    //Add the button group it its own panel
                    contentGroup.setName(narrowCapture);
                    contentGroup.add(radioOptionR);
                    contentGroup.add(radioOptionG);
                    contentGroup.add(radioOptionB);
                    contentGroup.add(radioOptionNone);
                    /*
                    Since it is conventional to capture in a sequence of short to long wavelengths, it is helpful to default the first third of the files 
                    in "narrowband captures" to blue, the middle third to green, and final third to red. 
                    */
                    float defaultFraction;
                    float ind = (float) (i+1.0);
                    float len = (float) listOfNarrowbandCaptures.length;
                    defaultFraction = ind/len;
                    logService.log().info(defaultFraction);
                    if (defaultFraction < 0.34){
                        defaultRange = "B";
                        radioOptionB.setSelected(true);
                    }
                    else if(defaultFraction > 0.67){
                        defaultRange = "R";
                        radioOptionR.setSelected(true);
                    }
                    else{
                        defaultRange = "G";
                        radioOptionG.setSelected(true);
                    }
                    //Add the button group panel to the overall content container
                    contentPane.add(contentGroup);                   
		} 

                JScrollPane spanel = new JScrollPane(contentPane);
                spanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                //spanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                Dimension prefSize = new Dimension(800, preferredSize.height);
                spanel.setPreferredSize(prefSize);           
                int result = JOptionPane.showConfirmDialog(null, spanel, "Assign Narrowband Captures", JOptionPane.OK_CANCEL_OPTION);
                //Once user hits OK, gather their selections.  If they hit cancel, then fail out.
                if ( result==JOptionPane.OK_OPTION) {
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
                } 
                else {
                    //Pane was cancelled or closed.  How should i handle (@userHitCancel)
                    IJ.error("You must designate the captures to the visible range of R, G, B, or none to continue!");
                    throw new Throwable("You must designate the captures to the visible range of R, G, B, or none to continue!");
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
                    IJ.error("You must designate at least one capture to each of the visible ranges (R,G,B). "+whichOnes);
                    throw new Throwable("You must designate at least one capture to each of the visible ranges (R,G,B). "+whichOnes);
                }
                logService.log().info("We should have red, green and blue narrow bands");
                redNarrowbands = redNarrowbands_list.toArray();
                greenNarrowbands = greenNarrowbands_list.toArray();
                blueNarrowbands = blueNarrowbands_list.toArray();
                logService.log().info(Arrays.toString(redNarrowbands));
                logService.log().info(Arrays.toString(greenNarrowbands));
                logService.log().info(Arrays.toString(blueNarrowbands));
		if (pcaHeight < 100){ //Looks like it was defined as 0 and never set or changed.  
                    File narrowbandNoGamma = new File(listOfNarrowbandCaptures[Math.round(listOfNarrowbandCaptures.length/2)].toString()); //projectDirectory+"Captures-Narrowband-NoGamma"+File.separator+
                    imp = opener.openImage( narrowbandNoGamma.toString() );
                    imglib2_img = ImagePlusAdapter.wrap( imp );
                    imp.setTitle("Preview");
                    //ImageJFunctions.show(imglib2_img, "Preview");
                    imp.show();
                    dWait = new WaitForUserDialog("Select area", "Draw a rectangle containing the colors of interest for PCA then click OK\n(hint: limit to object or smaller)");
                    dWait.show();
                    if(dWait.escPressed() || imp.getRoi() == null){
                        //@userHitCancel
                        IJ.error("You must draw a rectangle to continue!");
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
                File listOfPseudocolorSources_dir = new File(projectDirectory+"PCA"+File.separator);
                if(!listOfPseudocolorSources_dir.exists()){
                    GenericDialog nofldr = new GenericDialog("FYI");
                    nofldr.addMessage("A Pseudo Color folder has been created for you");
                    nofldr.setMaximumSize(bestFit);
                    nofldr.showDialog();
                    Files.createDirectory(listOfPseudocolorSources_dir.toPath());
                }
                logService.log().info("I need a list of narrow band captures here for psRTI || psRaking ");
                logService.log().info(Arrays.toString(listOfNarrowbandCaptures));
                if (listOfNarrowbandCaptures.length<9) { 
                    IJ.error("You must have 9 or more narrow band captures for PseudoColor!");
                    throw new Throwable("You must have 9 or more narrow band captures for PseudoColor!");
                }
                File[] listOfPseudocolorSources = listOfPseudocolorSources_dir.listFiles();
                String defaultPca = "";
                if (listOfPseudocolorSources.length > 1) defaultPca = "Open pregenerated images" ;
                else defaultPca = "Generate and select using defaults";
                String[] listOfPcaMethods = new String[3];
                listOfPcaMethods[0]="Generate and select using defaults";
                listOfPcaMethods[1]="Generate and manually select two";
                listOfPcaMethods[2]="Open pregenerated images";
                GenericDialog pseudoSources = new GenericDialog("Select Method for Pseudocolor");
                pseudoSources.addMessage("Pseudocolor images require two source images (typically principal component images).");
                pseudoSources.addRadioButtonGroup("Select how to provide the source images: ",listOfPcaMethods,listOfPcaMethods.length,1,defaultPca);
                pseudoSources.setMaximumSize(bestFit);
                pseudoSources.showDialog();
                if(pseudoSources.wasCanceled()){
                    //@userHitCancel
                    IJ.error("You must provide which method to use to continue!");
                    throw new Throwable("You must provide which method to use to continue!");
                }
                pcaMethod = pseudoSources.getNextRadioButton();
                logService.log().info("Got PCA method: "+pcaMethod);
                if (pcaHeight < 100) { //Looks like it was defined as 0 and never set or changed.  
                    if(listOfNarrowbandCaptures.length >= 1){
                        imp = opener.openImage( listOfNarrowbandCaptures[Math.round(listOfNarrowbandCaptures.length/2)].toString() );
                    }  
                    else{ //Yikes I think this needs to be a failing situation.  Does it have to be?
                        IJ.error("There needs to be at least one image in the narrowband nogamma captures folder...");
                        throw new Throwable("There needs to be at least one image in the narrowband nogamma captures folder...");
                    }
                    imglib2_img = ImagePlusAdapter.wrap( imp );
                    ImageJFunctions.show(imglib2_img, "Preview");
                    dWait = new WaitForUserDialog("Select area", "Draw a rectangle containing the colors of interest for PCA then click OK\n(hint: limit to object or smaller)");
                    dWait.show();
                    if(dWait.escPressed() || WindowManager.getImage("Preview").getRoi() == null){
                        //@userHitCancel
                        IJ.error("You must draw a rectangle to continue!");
                        throw new Throwable("You must draw a rectangle to continue!");
                    }
                    bounds = WindowManager.getImage("Preview").getRoi().getBounds();
                    pcaX = bounds.x;
                    pcaY = bounds.y;
                    pcaHeight = bounds.height;
                    pcaWidth = bounds.width;
                    WindowManager.getImage("Preview").close();
                }
            }
            if (csRtiDesired || csRakingDesired) { //interaction phase jhg 
                OpenDialog csSourceDialog = new OpenDialog("Choose a Source for Custom Process");
                csSource = csSourceDialog.getPath();
                logService.log().info("Should have custom source");
                logService.log().info(csSource);
            }
            /**
             *@see create base lp file
             */
            if (lpDesired) {
                imp = opener.openImage(listOfHemisphereCaptures[20].toString());
                imglib2_img = ImagePlusAdapter.wrap( imp );
                ImageJFunctions.show(imglib2_img, "Preview");                
                dWait = new WaitForUserDialog("Select ROI", "Draw a rectangle loosely around a reflective hemisphere and press Ok");
                if(dWait.escPressed() || WindowManager.getImage("Preview").getRoi() == null){
                    //@userHitCancel
                    IJ.error("You must draw a rectangle to continue!");
                    throw new Throwable("You must draw a rectangle to continue!");
                }
                dWait.show();
                bounds = WindowManager.getImage("Preview").getRoi().getBounds();
                WindowManager.getImage("Preview").close();
                for(int i=0;i<listOfHemisphereCaptures.length;i++) {
                    if (listOfHemisphereCaptures[i].toString().endsWith("tiff") || listOfHemisphereCaptures[i].toString().endsWith("tif")) {
                        imp=opener.openImage(listOfHemisphereCaptures[i].toString());
                        imglib2_img = ImagePlusAdapter.wrap( imp );
                        int extensionIndex = listOfHemisphereCaptures[i].getName().indexOf(".");
                        if (extensionIndex != -1)
                        {
                            filePath = projectDirectory+"LightPositionData"+File.separator+"jpeg-exports"+File.separator+listOfHemisphereCaptures[i].getName().substring(0, extensionIndex);
                        }
                        ImageJFunctions.show(imglib2_img, "LightPosition");
                        WindowManager.getImage("LightPosition").setRoi(0,0,(int)imglib2_img.dimension(3), (int)imglib2_img.dimension(4));
                        IJ.run("Crop"); //Crops the image or stack based on the current rectangular selection.
                        File jpegExportsFile = new File(projectDirectory+"LightPositionData"+File.separator+"jpeg-exports"+File.separator);
                        if (!light_position_dir.exists()) Files.createDirectory(light_position_dir.toPath());
                        if (!jpegExportsFile.exists()) Files.createDirectory(jpegExportsFile.toPath());
                        //Do we need to tell the users we created these directories?
                        IJ.saveAs("jpeg",filePath+".jpg"); //Use this submenu to save the active image in TIFF, GIF, JPEG, or format
                        WindowManager.getImage("LightPosition").close();
                    }
                }
                IJ.showMessageWithCancel("Use RTI Builder to Create LP File","Please use RTI Builder to create an LP file based on the reflective hemisphere detail images in\n"+projectDirectory+"LightPositionData"+File.separator+"\nPress cancel to discontinue Spectral RTI Toolkit or Ok to continue with other tasks after the lp file has been created.");
            }
            if(acRtiDesired || acRakingDesired){ //Gather accurate color info
                listOfAccurateColorSources = accurate_color_dir.listFiles();
                String[] listOfAccurateColorSources_string = new String[listOfAccurateColorSources.length];
                ArrayList<String>  listOfAccurateColorSources_list = new ArrayList<String>();
                ArrayList<String>  listOfAccurateColorSources_short = new ArrayList<String>();
                for (File f : listOfAccurateColorSources) {
                   listOfAccurateColorSources_list.add(f.toString());
                   listOfAccurateColorSources_short.add("..."+f.getName());
                   //elipses makes a weird box pop up next to the radio button...
                }
                if(shortName){
                    // YIKES null pointer exception when we go to grab the file.  Only use full name!
                     //listOfAccurateColorSources_short.toArray(listOfAccurateColorSources_string);
                    listOfAccurateColorSources_list.toArray(listOfAccurateColorSources_string);
                }
                else{
                     listOfAccurateColorSources_list.toArray(listOfAccurateColorSources_string);
                }
               
		if (listOfAccurateColorSources.length == 1) { //There was only one source, so auto select it
                    accurateColorSource = listOfAccurateColorSources[0];
		} 
                else if (listOfAccurateColorSources.length == 0) { //There were no sources, this is an error.
                    IJ.error("Need at least one color image file in "+projectDirectory+"AccurateColor"+File.separator);
                    throw new Throwable("Need at least one color image file in "+projectDirectory+"AccurateColor"+File.separator); 
		} 
                else { //There were multiple sources, let the user pick the one they want to use.
                    if (accurateColorSource == null) {
                        logService.log().info("Could not find a color source");
                        GenericDialog gd = new GenericDialog("Select Color Source");
                        gd.addMessage("Select Color Source");
                        // Yikes how could I set tooltips on these to reveal full names in cases of shortName preference?
                        gd.addRadioButtonGroup("File: ", listOfAccurateColorSources_string, listOfAccurateColorSources.length, 1, listOfAccurateColorSources[0].toString());
                        gd.showDialog();
                        if(gd.wasCanceled()){
                            //@userHitCancel
                            IJ.error("You must provide a color source to continue!");
                            throw new Throwable("You must provide a color source to continue!");
                        }
                        accurateColorSource = new File(gd.getNextRadioButton());
                    }
		}
            }
            //Think about acRtiDesired || acRakingDesired so we can use just one loop over the hemishpere captures for all processing.  
            if (acRtiDesired) {
		/**
                 *@see create series of images with luminance from hemisphere captures and chrominance from color image
                 */
                File accurateRTI = new File(projectDirectory+"AccurateColorRTI"+File.separator);
		if (!accurateRTI.exists()) {
                    Files.createDirectory(accurateRTI.toPath());
                    logService.log().info("A directory has been created for Accurate Color RTI at "+projectDirectory+"AccurateColorRTI"+File.separator);
		}
		//integration
                imp = opener.openImage( accurateColorSource.toString() );
                imglib2_img = ImagePlusAdapter.wrap( imp );
		if (imp.getBitDepth() == 8) {
                    IJ.run(imp,"RGB Color","");
                    imp.close();
                    imp = WindowManager.getCurrentImage();
                    imp.setTitle("RGBtiff");
		}
		IJ.run(imp, "RGB to YCbCr stack", "");
		IJ.run("Stack to Images");
                
                //Stack to Images will automatically cause windows to open in the interim.  For processing, it is best if they are hidden.
                WindowManager.getImage("Y").close(); //Don'e need this one.
                ImagePlus cb = WindowManager.getImage("Cb");
                ImagePlus cr = WindowManager.getImage("Cr");
                cb.hide();
                cr.hide();
                logService.log().info("Set stack pieces...");
                ImagePlus keptPieces = con.concatenate(cb, cr, true);
                imp.close();
		/**
                 *Luminance from hemisphere captures
                 */
		for(int i=0;i<listOfHemisphereCaptures.length;i++) {
                    if (listOfHemisphereCaptures[i].toString().endsWith("tiff") || listOfHemisphereCaptures[i].toString().endsWith("tif")) { 
                        logService.log().info("On hem capture index "+i);
                        /**
                         *@see better to trim list at the beginning so that array.length can be used in lp file
                         */
                        imp = opener.openImage( listOfHemisphereCaptures[i].toString() );
                        imglib2_img = ImagePlusAdapter.wrap( imp );
                        imp.setTitle("Luminance");
                        // it would be better to crop early in the process, especially before reducing to 8-bit and jpeg compression
                        // normalize
                        if (brightnessAdjustApply.equals("RTI images also")) {
                            if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                                //WindowManager.getImage("Luminance").setRoi(normX, normY, normWidth, normHeight);
                                imp.setRoi(normX, normY, normWidth, normHeight);
                                IJ.run(imp, "Enhance Contrast...", "saturated=0.4");//Enhances image contrast by using either histogram stretching or histogram equalization.  Affects entire stack
                                //IJ.run("Select None");//Deactivates the selection in the active image.
                            } 
                            else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                                IJ.run(imp,"Multiply...", "value="+normalizationFixedValue+"");
                            }
                        }
                        IJ.run(imp,"8-bit", ""); //Applies the current display range mapping function to the pixel data. If there is a selection, only pixels within the selection are modified

                        //This requires the images be showing.  Since this is in a loop, we really want to avoid this at all costs.
                        //imp.show();
                        //IJ.run("Concatenate...", "  title=[YCC] keep image1=Luminance image2=Cb image3=Cr image4=[-- None --]"); //Concatenates multiple images or stacks. Images with mismatching type and dimensions are omitted

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
                            String simpleName2 = projectName + "_";
                            String simpleName3 = simpleName1.substring(simpleName1.indexOf("RTI-"));
                            filePath = projectDirectory+"AccurateColorRTI"+File.separator+"AccurateColor_"+simpleName2+simpleName3;
                            simpleImageName = "AccurateColor_"+simpleName2+simpleName3;
                        }
                        noClobber(filePath+".jpg");
                        IJ.saveAs(stackRGB, "jpeg", filePath+".jpg");
                        stackRGB.close();
                        //WindowManager.getImage(simpleImageName+".jpg").close();
                        //WindowManager.getImage("YCC").close();
                        imp.changes=false;
                        imp.close();          
                        stack.close();
                    }
		}
                //WindowManager.getImage("Cb").close();
                //WindowManager.getImage("Cr").close();    
                cb.close();
                cr.close();
                keptPieces.close();
                createLpFile("AccurateColor", projectDirectory); 
                WindowManager.closeAllWindows(); // IS this needed?
		runFitter("AccurateColor");
            }
            if (acRakingDesired) {
//                logService.log().info("Raking.  Get numbering right.");
                imp = opener.openImage( accurateColorSource.toString() ); 
                imglib2_img = ImagePlusAdapter.wrap( imp );
                imp.setTitle("RGBtiff");
		if (imp.getBitDepth() == 8) {
                    IJ.run(imp,"RGB Color","");
		}
		/**
                 * @see create accurate color static diffuse
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
		if (!transmissiveSource.equals("")){
                    logService.log().info("Transmissive source has value "+transmissiveSource);
                    imp = opener.openImage( transmissiveSource ); 
                    imp.setTitle("TransmissiveLuminance");
                    IJ.run(imp, "8-bit", "");
                    
                    logService.log().info("Set stack pieces...");
                    ImagePlus keptPieces = con.concatenate(cb, cr, true);
                    //imp.show();
                    ImagePlus stack = con.concatenate(imp, keptPieces, true);
                    stack.setTitle("YCC");
                    stack.hide();
                    //IJ.run("Concatenate...", "  title=[YCC] keep image1=TransmissiveLuminance image2=Cb image3=Cr image4=[-- None --]");
                    imp.changes=false;
                    imp.close();
                    IJ.run(stack, "YCbCr stack to RGB", "");
                    ImagePlus stackRGB = WindowManager.getImage("YCC - RGB");
                    stackRGB.hide();
                    noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_Tx.tiff");
                    IJ.save(stackRGB,projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_Tx.tiff");
                    createJp2(projectName+"_Ac_Tx", projectDirectory);
                    toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_Tx.tiff");
                    Files.deleteIfExists(toDelete.toPath());   
                    stack.close();
                    stackRGB.close();
                    //WindowManager.getImage("YCC - RGB").close();
                   // WindowManager.getImage("YCC").close();
		}
		//Luminance from hemisphere captures
		for(int i=0;i<listOfHemisphereCaptures.length;i++) {
                    //logService.log().info("On hem capture "+i+" of "+listOfHemisphereCaptures.length);
                    //logService.log().info(listOfHemisphereCaptures[i].toString());
                    if (listOfHemisphereCaptures[i].toString().endsWith("tiff") || listOfHemisphereCaptures[i].toString().endsWith("tif")){ //@@@ better to trim list at the beginning so that array.length can be used in lp file
                        //logService.log().info("Checking against list of raking directions index "+(i)+" of "+listOfRakingDirections.size()+".");
                        if (listOfRakingDirections.get(i)) {
                            imp = opener.openImage( listOfHemisphereCaptures[i].toString() );
                            imp.setTitle("Luminance");
                            imglib2_img = ImagePlusAdapter.wrap(imp);
                            if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                                imp.setRoi(normX, normY, normWidth, normHeight);
                                IJ.run(imp, "Enhance Contrast...", "saturated=0.4");
                            } else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                                IJ.run(imp, "Multiply...", "value="+normalizationFixedValue+""); //Multiplies the image or selection by the specified real constant. With 8-bit images, results greater than 255 are set to 255
                            }
                            IJ.run(imp, "8-bit", "");
                            //imp.show();
                            //IJ.run("Concatenate...", "title=[YCC] keep image1=Luminance image2=Cb image3=Cr image4=[-- None --]");
                            logService.log().info("Set stack pieces...");
                            ImagePlus keptPieces = con.concatenate(cb, cr, true);
                            cb.close(); //Can this be closed??
                            cr.close(); //Can this be closed??
                            //imp.show();
                            ImagePlus stack = con.concatenate(imp, keptPieces, true);
                            stack.setTitle("YCC");
                            stack.hide();
                            IJ.run(stack, "YCbCr stack to RGB", "");
                            ImagePlus stackRGB = WindowManager.getImage("YCC - RGB");
                            stackRGB.hide();
                            imp.changes = false;
                            imp.close();
                            //logService.log().info("I am making position numbers for these images.  The position number for this static raking is "+(i));
                            //a 00 position number was saved at the beginning
                            positionNumber = IJ.pad(i+1, 2).toString();
                            //logService.log().info("Full position number "+positionNumber);
                            noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_"+positionNumber+".tiff");
                            IJ.save(stackRGB, projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_"+positionNumber+".tiff");
                            createJp2(projectName+"_Ac_"+positionNumber, projectDirectory);
                            toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_"+positionNumber+".tiff");
                            Files.deleteIfExists(toDelete.toPath());     
                            stack.close();
                            stackRGB.close();
//                            WindowManager.getImage("YCC - RGB").close();
//                            WindowManager.getImage("YCC").close(); 
                        }
                    }
		}
                cb.close();
                cr.close();
                //WindowManager.closeAllWindows(); //Is this needed?
            }
            IJ.run("Collect Garbage");
            if (xsRtiDesired || xsRakingDesired) {
		//Red
		String redStringList = redNarrowbands[0].toString();  //might be an array to string function somewhere to do this more elegantly
                if(redNarrowbands.length == 1){
                    //Yikes double check this doesnt break anything.  If it does, the user must provide at least 2 selections.  
                    //add a duplicate in so ImageJ can use a stack.  Cannot have a stack of 1.
                    redNarrowbands_list.add(redNarrowbands[0].toString());
                    redNarrowbands = redNarrowbands_list.toArray();
                }
		for (int i=1;i<redNarrowbands.length;i++) {
                    redStringList = redStringList+"|"+redNarrowbands[i].toString();
		}
                logService.log().info("What is red string list");
                logService.log().info(redStringList);
                ImagePlus redStacker = FolderOpener.open(projectDirectory+"Captures-Narrowband-NoGamma"+File.separator, "file=("+redStringList+") sort");
		//IJ.run("Image Sequence...", "title=[RedStack] open="+projectDirectory+"Captures-Narrowband-NoGamma"+File.separator+" file=("+redStringList+") sort"); 
                //Opens a series of images in a chosen folder as a stack. Images may have different dimensions and can be of any format supported by ImageJ              
                //ImagePlus redStacker = WindowManager.getImage("Captures-Narrowband-NoGamma");
                redStacker.setTitle("RedStack");
                redStacker.hide();
                
                //WindowManager.getImage("Captures-Narrowband-NoGamma").setTitle("RedStack");
               
                //YIKES
                //What happens if these weren't set yet?  Do I need to get the width of height of the image?
                //It should at least be set to the height or width of the image when grabbed above in the pcaHeight < 100 clause right?
                if(pcaWidth == 0){
                    
                }
                if(pcaHeight == 0){
                    
                }
                redStacker.setRoi(pcaX,pcaY,pcaWidth, pcaHeight); 
                //WindowManager.getImage("RedStack").setRoi(pcaX,pcaY,pcaWidth, pcaHeight); 
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
                //WindowManager.getWindow("PCA of RedStack kept stack").toFront();
                kept_pcaRedStacker.setTitle("R");
                kept_pcaRedStacker.setRoi(pcaX,pcaY,pcaWidth,pcaHeight); // questionable
		IJ.run(kept_pcaRedStacker,"Enhance Contrast...", "saturated=0.4 normalize");
		IJ.run(kept_pcaRedStacker, "8-bit", "");
                
                
                
		//Green
		String greenStringList = greenNarrowbands[0].toString();  //might be an array to string function somewhere to do this more elegantly
                if(greenNarrowbands.length == 1){
                    //Yikes double check this doesnt break anything.  If it does, the user must provide at least 2 selections.  
                    //add a duplicate in so ImageJ can use a stack.  Cannot have a stack of 1.
                    greenNarrowbands_list.add(greenNarrowbands[0].toString());
                    greenNarrowbands = greenNarrowbands_list.toArray();
                }
		for (int i=1;i<greenNarrowbands.length;i++) {
                    greenStringList = greenStringList+"|"+greenNarrowbands[i].toString();
		}
                logService.log().info("What is greenStringList");
                logService.log().info(greenStringList);
                ImagePlus greenStacker = FolderOpener.open(projectDirectory+"Captures-Narrowband-NoGamma"+File.separator, "file=("+greenStringList+") sort");
		//IJ.run("Image Sequence...", "title=[RedStack] open="+projectDirectory+"Captures-Narrowband-NoGamma"+File.separator+" file=("+redStringList+") sort"); 
		//IJ.run("Image Sequence...", "open="+projectDirectory+"Captures-Narrowband-NoGamma"+File.separator+" file=("+greenStringList+") sort");
                greenStacker.setTitle("GreenStack");
                greenStacker.setRoi(pcaX,pcaY,pcaWidth,pcaHeight); 
		IJ.run(greenStacker,"PCA ", ""); 
                ImagePlus pca_greenStacker = WindowManager.getImage("PCA of GreenStack");
                pca_greenStacker.hide();
		IJ.run(WindowManager.getImage("PCA of GreenStack"),"Slice Keeper", "first=1 last=1 increment=1");
                WindowManager.getImage("Eigenvalue spectrum of GreenStack").close();
                greenStacker.changes=false;
                greenStacker.close();
                WindowManager.getImage("PCA of GreenStack").close();
                ImagePlus kept_pcaGreenStacker = WindowManager.getImage("PCA of GreenStack kept stack");
                kept_pcaGreenStacker.hide();
                //WindowManager.getWindow("PCA of GreenStack kept stack").toFront();
                kept_pcaGreenStacker.setTitle("G");
                kept_pcaGreenStacker.setRoi(pcaX,pcaY,pcaWidth,pcaHeight); 
		IJ.run(kept_pcaGreenStacker, "Enhance Contrast...", "saturated=0.4 normalize");
		IJ.run(kept_pcaGreenStacker, "8-bit", "");
                
		
//Blue
		String blueStringList = blueNarrowbands[0].toString();  //might be an array to string function somewhere to do this more elegantly
                if(blueNarrowbands.length == 1){
                    //Yikes double check this doesnt break anything.  If it does, the user must provide at least 2 selections.  
                    //add a duplicate in so ImageJ can use a stack.  Cannot have a stack of 1.
                    blueNarrowbands_list.add(blueNarrowbands[0].toString());
                    blueNarrowbands = blueNarrowbands_list.toArray();
                }
		for (int i=1;i<blueNarrowbands.length;i++) {
                    blueStringList = blueStringList+"|"+blueNarrowbands[i].toString();
		}
                logService.log().info("What is blueStringList");
                logService.log().info(blueStringList);
                ImagePlus blueStacker = FolderOpener.open(projectDirectory+"Captures-Narrowband-NoGamma"+File.separator, "file=("+blueStringList+") sort");
                blueStacker.hide();
		//IJ.run("Image Sequence...", "open="+projectDirectory+"Captures-Narrowband-NoGamma"+File.separator+" file=("+blueStringList+") sort");
                blueStacker.setTitle("BlueStack");
                blueStacker.setRoi(pcaX,pcaY,pcaWidth,pcaHeight); 
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
                //imp.show();
                ImagePlus stack = Concatenator.run(imp, kept_pcaRedStacker, kept_pcaBlueStacker, kept_pcaGreenStacker);
                stack.setTitle("Stack");
                stack.hide();
		//IJ.run("Concatenate...", "  title=[Stack] image1=R image2=G image3=B image4=[-- None --]");
                //RGB stack of black and white
		IJ.run(stack, "Stack to RGB", "");
                ImagePlus stackRGB = WindowManager.getImage("Stack - RGB");
                stackRGB.hide();
                stack.hide();
                //RGB image of color.  Why isn't it color?
                //WindowManager.getImage("Stack").close();
		//create extended spectrum static diffuse
		if (xsRakingDesired){
                    noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_00"+".tiff");
                    IJ.save(stack, projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_00"+".tiff");
                    createJp2(projectName+"_Xs_00", projectDirectory); 
                    toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_00"+".tiff");
                    Files.deleteIfExists(toDelete.toPath());
		}
		IJ.run(stack, "RGB to YCbCr stack", "");
		IJ.run(stack, "Stack to Images", "");
                WindowManager.getImage("Y").close();
                stack.close();
                ImagePlus cb = WindowManager.getImage("Cb");
                ImagePlus cr = WindowManager.getImage("Cr");
                cb.hide();
                cr.hide();
                ImagePlus keptPieces = con.concatenate(cb, cr, true);
                logService.log().info("What is transmissive source "+transmissiveSource);
		if (!transmissiveSource.equals("")){
                    imp = opener.openImage( transmissiveSource );
                    imglib2_img = ImagePlusAdapter.wrap( imp );
                    imp.setTitle("TransmissiveLuminance");
                    IJ.run(imp, "8-bit", "");
                    //imp.show();
                    logService.log().info("Set stack pieces...");
                    //imp.show();
                    ImagePlus stack2 = con.concatenate(imp, keptPieces, true);
                    stack2.setTitle("YCC");
                    //IJ.run("Concatenate...", "  title=[YCC] keep image1=TransmissiveLuminance image2=Cb image3=Cr image4=[-- None --]");
                    IJ.run(stack2,"YCbCr stack to RGB","");
                    ImagePlus stackRGB2 = WindowManager.getImage("YCC - RGB");
                    stackRGB2.hide();
                    imp.changes=false;
                    imp.close();
                    noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_Tx.tiff");
                    IJ.save(stackRGB2, projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_Tx.tiff");
                    createJp2(projectName+"_Xs_Tx", projectDirectory);
                    toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_Tx.tiff");
                    Files.deleteIfExists(toDelete.toPath()); 
                    stack2.close();
                    stackRGB2.clone();
                    //WindowManager.getImage("YCC").close();
		}
		if (xsRtiDesired) {
                    if (!extended_spectrum_dir.exists()) {
                        Files.createDirectory(extended_spectrum_dir.toPath());
                        logService.log().info("A directory has been created for Extended Spectrum RTI at "+projectDirectory+"ExtendedSpectrumRTI"+File.separator);
                    }
		}
		for(int i=0;i<listOfHemisphereCaptures.length;i++){
                    if (xsRtiDesired) {
                        imp = opener.openImage( listOfHemisphereCaptures[i].toString() );
                        imglib2_img = ImagePlusAdapter.wrap( imp );
                        imp.setTitle("Luminance");
                        if (brightnessAdjustApply.equals("RTI images also")) {
                            if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                                imp.setRoi(pcaX,pcaY,pcaWidth,pcaHeight); 
                                IJ.run(imp,"Enhance Contrast...", "saturated=0.4");
                                IJ.run("Select None");
                            } 
                            else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                                IJ.run(imp, "Multiply...", "value="+normalizationFixedValue+"");
                            }
                        }
                        IJ.run(imp, "8-bit", "");
                        //imp.show();
                        ImagePlus stack3 = con.concatenate(imp, keptPieces, true);
                        stack3.setTitle("YCC");
                        //IJ.run("Concatenate...", "  title=[YCC] keep image1=Luminance image2=Cb image3=Cr image4=[-- None --]");
                        IJ.run(stack3, "YCbCr stack to RGB", "");
                        ImagePlus stackRGB2 = WindowManager.getImage("YCC - RGB");
                        stackRGB2.hide();
                        int extensionIndex = listOfHemisphereCaptures[i].getName().indexOf(".");
                        if (extensionIndex != -1)
                        {
                            String simpleName1 = listOfHemisphereCaptures[i].getName().substring(0, extensionIndex); //.toString().substring(0, extensionIndex)
                            String simpleName2 = projectName + "_";
                            String simpleName3 = simpleName1.substring(simpleName1.indexOf("RTI-"));
                            filePath = projectDirectory+"ExtendedSpectrumRTI"+File.separator+"ExtendedSpectrum_"+simpleName2+simpleName3;
                            simpleImageName = "ExtendedSpectrum_"+simpleName2+simpleName3;
                        }
                        noClobber(filePath+".jpg");
                        IJ.saveAs(stackRGB2, "jpeg",filePath+".jpg");
                        stack3.close();
                        imp.changes = false;
                        imp.close();
                        stackRGB2.close();
                        stack3.close();
                        //WindowManager.getImage(simpleImageName+".jpg").close();
                    }
                    if (xsRakingDesired) {
                        if (listOfRakingDirections.get(i)) {
                            imp = opener.openImage( listOfHemisphereCaptures[i].toString() );
                            imglib2_img = ImagePlusAdapter.wrap( imp );
                            imp.setTitle("Luminance");
                            if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                                imp.setRoi(normX,normY,normWidth,normHeight); 
                                IJ.run(imp, "Enhance Contrast...", "saturated=0.4");
                                IJ.run("Select None");
                            } 
                            else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                                IJ.run(imp, "Multiply...", "value="+normalizationFixedValue+"");
                            }
                            IJ.run(imp, "8-bit", "");
                            //imp.show();
                            ImagePlus stack4 = con.concatenate(imp, keptPieces, true);
                            stack4.setTitle("YCC");
                            stack4.hide();
                            //IJ.run("Concatenate...", "  title=[YCC] keep image1=Luminance image2=Cb image3=Cr image4=[-- None --]");
                            imp.changes = false;
                            imp.close();
                            IJ.run(stack4, "YCbCr stack to RGB", "");
                            ImagePlus stackRGB3 = WindowManager.getImage("YCC - RGB");
                            stackRGB3.hide();
                            positionNumber = IJ.pad(i+1, 2).toString();
                            noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_"+positionNumber+".tiff");
                            IJ.save(stackRGB3, projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_"+positionNumber+".tiff");
                            createJp2(projectName+"_Xs_"+positionNumber, projectDirectory);
                            toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_"+positionNumber+".tiff");
                            Files.deleteIfExists(toDelete.toPath()); 
                            stack4.close();
                            stackRGB3.close();
//                            WindowManager.getImage("YCC").close();
//                            WindowManager.getImage("YCC - RGB").close();
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
		//option to create new ones based on narrowband captures and assumption that pc1 and pc2 are best
		if (pcaMethod.equals("Generate and select using defaults")){
                    IJ.run("Image Sequence...", "open="+projectDirectory+"Captures-Narrowband-NoGamma"+File.separator+" sort");
                    if (fluorescenceNoGamma.exists()) {
                        IJ.run("Image Sequence...", "open="+projectDirectory+"Captures-Fluorescence-NoGamma"+File.separator+" sort");
                        IJ.run("Concatenate...", "  title=Captures-Narrowband-NoGamma image1=Captures-Narrowband-NoGamma image2=Captures-Fluorescence-NoGamma image3=[-- None --]");
                    }
                    else{
                        // ?
                    }
                    WindowManager.getImage("Captures-Narrowband-NoGamma").setRoi(pcaX,pcaY,pcaWidth,pcaHeight); 
                    IJ.run("PCA ");
                    IJ.run(WindowManager.getImage("PCA of Captures-Narrowband-NoGamma"),"Slice Keeper", "first=2 last=3 increment=1");
                    WindowManager.getImage("Eigenvalue spectrum of Captures-Narrowband-NoGamma").close();
                    WindowManager.getImage("Captures-Narrowband-NoGamma").changes = false;
                    WindowManager.getImage("Captures-Narrowband-NoGamma").close();
                    WindowManager.getImage("PCA of Captures-Narrowband-NoGamma").close();
                    WindowManager.getImage("PCA of Captures-Narrowband-NoGamma kept stack").setActivated();
                    WindowManager.getImage("PCA of Captures-Narrowband-NoGamma kept stack").setRoi(pcaX,pcaY,pcaWidth,pcaHeight); 
                    IJ.run(WindowManager.getImage("PCA of Captures-Narrowband-NoGamma kept stack"), "Enhance Contrast...", "saturated=0.3 normalize update process_all");
                    IJ.run(WindowManager.getImage("PCA of Captures-Narrowband-NoGamma kept stack"), "8-bit", "");
		} 
                else if (pcaMethod.equals("Generate and manually select two")) {
                    //option to create new ones and manually select (close all but two)
                    IJ.run("Image Sequence...", "open="+projectDirectory+"Captures-Narrowband-NoGamma"+File.separator+" sort");
                    if (fluorescenceNoGamma.exists()) {
                        IJ.run("Image Sequence...", "open="+projectDirectory+"Captures-Fluorescence-NoGamma"+File.separator+" sort");
                        IJ.run("Concatenate...", "  title=Captures-Narrowband-NoGamma image1=Captures-Narrowband-NoGamma image2=Captures-Fluorescence-NoGamma image3=[-- None --]");
                    } 
                    else {
                        // ?
                    }
                    IJ.run("PCA ");
                    WindowManager.getImage("PCA of Captures-Narrowband-NoGamma").setRoi(pcaX,pcaY,pcaWidth,pcaHeight); 
                    WindowManager.getImage("Eigenvalue spectrum of Captures-Narrowband-NoGamma").close();
                    WindowManager.getImage("Captures-Narrowband-NoGamma").changes = false;
                    WindowManager.getImage("Captures-Narrowband-NoGamma").close();
                    WindowManager.getImage("PCA of Captures-Narrowband-NoGamma").setActivated();
                    dWait = new WaitForUserDialog("Delete Slices", "Delete slices from the stack until two remain\n(Hint: Image > Stacks > Delete Slice)\nEnhance contrast as desired\nThen press Ok");
                    if(dWait.escPressed()){
                        //@userHitCancel
                        IJ.error("You must delete until there are two slices to continue!");
                        throw new Throwable("You must delete until there are two slices to continue!");
                    }
                    dWait.show();
                    WindowManager.getImage("PCA of Captures-Narrowband-NoGamma").setTitle("PCA of Captures-Narrowband-NoGamma kept stack");
                    WindowManager.getImage("PCA of Captures-Narrowband-NoGamma kept stack").setRoi(pcaX,pcaY,pcaWidth,pcaHeight); 
                    IJ.run(WindowManager.getImage("PCA of Captures-Narrowband-NoGamma kept stack"), "8-bit", "");
		/**
                 * @see option to use previously generated principal component images
                 */
		}
                else if (pcaMethod.equals("Open pregenerated images")) {
                    dWait = new WaitForUserDialog("Designated Images", "Open a pair of images or stack of two slices.\nEnhance contrast as desired\nThen press Ok");
                    if(dWait.escPressed()){
                        //@userHitCancel
                        IJ.error("You must make selections to continue!");
                        throw new Throwable("You must make selections to continue!");
                    }
                    dWait.show();
                    if (WindowManager.getImageCount() > 1){ 
                        IJ.run("Images to Stack", "name=Stack title=[] use"); 
                        WindowManager.getActiveWindow().setName("PCA of Captures-Narrowband-NoGamma kept stack");
                    }
                    else{
                        IJ.error("Open a pair of images or stack of two slices to continue!");
                        throw new Throwable("Open a pair of images or stack of two slices to continue!");
                    }
                    IJ.run(WindowManager.getImage("PCA of Captures-Narrowband-NoGamma kept stack"), "8-bit", "");
		}
		/**
                    * @see integrate pca pseudocolor with rti luminance
                    * @see create static diffuse (not trivial... use median of all)
                */
                logService.log().info("Process Part 2");
		if (psRakingDesired){
                    IJ.run("Image Sequence...", "open="+projectDirectory+"Captures-Hemisphere-Gamma"+File.separator);
                    IJ.run("Z Project...", "projection=Median");
                    WindowManager.getImage("MED_Captures-Hemisphere-Gamma").setTitle("Luminance");
                    WindowManager.getImage("Captures-Hemisphere-Gamma").changes = false;
                    WindowManager.getImage("Captures-Hemisphere-Gamma").close();   
                    WindowManager.getImage("Luminance").setActivated();
                    if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                        region = new RectangleOverlay();
                        WindowManager.getImage("Luminance").setRoi(normX,normY,normWidth,normHeight); 
                        IJ.run("Enhance Contrast...", "saturated=0.4");
                        IJ.run("Select None");
                    } 
                    else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                        IJ.run("Multiply...", "value="+normalizationFixedValue+"");
                    }
                    IJ.run(WindowManager.getImage("Luminance"), "8-bit", "");
                    IJ.run("Concatenate...", "  title=[YCC] keep image1=Luminance image2=[PCA of Captures-Narrowband-NoGamma kept stack] image3=[-- None --]");
                    IJ.run("YCbCr stack to RGB");
                    noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_00.tiff");
                    IJ.save(WindowManager.getImage("YCC - RGB"), projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_00.tiff");
                    createJp2(projectName+"_Ps_00", projectDirectory);
                    toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_00.tiff");
                    Files.deleteIfExists(toDelete.toPath());
                    WindowManager.getImage("YCC - RGB").close();
                    WindowManager.getImage("YCC").close();
                    WindowManager.getImage("Luminance").changes = false;
                    WindowManager.getImage("Luminance").close();
                    
                    if (!transmissiveSource.equals("")) {
                        imp = opener.openImage( transmissiveSource );
                        imglib2_img = ImagePlusAdapter.wrap( imp );
                        imp.setTitle("TransmissiveLuminance");
                        IJ.run(imp, "8-bit", "");
                        imp.show();
                        IJ.run("Concatenate...", "  title=[YCC] keep image1=TransmissiveLuminance image2=[PCA of Captures-Narrowband-NoGamma kept stack] image3=[-- None --]");
                        IJ.run("YCbCr stack to RGB");
                        imp.changes = false;
                        imp.close();
                        noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_Tx.tiff");
                        IJ.save(WindowManager.getImage("YCC - RGB"), projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_Tx.tiff");
                        createJp2(projectName+"_Ps_Tx", projectDirectory);
                        toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_Tx.tiff");
                        Files.deleteIfExists(toDelete.toPath());
                        WindowManager.getImage("YCC - RGB").close();
                        WindowManager.getImage("YCC").close();
                    }
		}
		if (psRtiDesired) {
                    if (!pseudo_color_dir.exists()) {
                        File createPseudo = new File(projectDirectory+"PseudocolorRTI"+File.separator);
                        Files.createDirectory(createPseudo.toPath());
                        logService.log().info("A directory has been created for Pseudocolor RTI at "+projectDirectory+"PseudocolorRTI"+File.separator);
                    }
		}
		for(int i=0;i<listOfHemisphereCaptures.length;i++) {
                    if (psRtiDesired||listOfRakingDirections.get(i)){ //YIKES double check on this
                        imp = opener.openImage( listOfHemisphereCaptures[i].toString() );
                        imglib2_img = ImagePlusAdapter.wrap( imp );
                        imp.setTitle("Luminance");
                        int extensionIndex = listOfHemisphereCaptures[i].getName().indexOf(".");
                        IJ.run(imp, "Duplicate...", "title=EnhancedLuminance");
                        WindowManager.getImage("EnhancedLuminance").setActivated();
                        if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                            WindowManager.getImage("EnhancedLuminance").setRoi(normX,normY,normWidth,normHeight); 
                            IJ.run(WindowManager.getImage("EnhancedLuminance"),"Enhance Contrast...", "saturated=0.4");
                            IJ.run("Select None");
                        } 
                        else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                            IJ.run(WindowManager.getImage("EnhancedLuminance"),"Multiply...", "value="+normalizationFixedValue+"");
                        }
                        IJ.run(imp, "8-bit", "");
                        IJ.run(WindowManager.getImage("EnhancedLuminance"), "8-bit", "");
                        if (listOfRakingDirections.get(i)) { //Yikes double check on this.  I believe it conrtols whether or not a StaticRaking file is created but doesn't rely on psrakingDesired.
                            IJ.run("Concatenate...", "  title=[YCC] keep image1=EnhancedLuminance image2=[PCA of Captures-Narrowband-NoGamma kept stack] image3=[-- None --]");
                            IJ.run("YCbCr stack to RGB");
                            WindowManager.getImage("YCC").close();
                            positionNumber = IJ.pad(i+1, 2).toString();                          
                            noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_"+positionNumber+".tiff");
                            IJ.save(WindowManager.getImage("YCC - RGB"), projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_"+positionNumber+".tiff");
                            WindowManager.getImage("YCC - RGB").close();
                            createJp2(projectName+"_Ps_"+positionNumber, projectDirectory);
                            toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_"+positionNumber+".tiff");
                            Files.deleteIfExists(toDelete.toPath());
                        }
                        if ((psRtiDesired)&&(brightnessAdjustApply.equals("RTI images also"))){ 
                            IJ.run("Concatenate...", "  title=[YCC] keep image1=EnhancedLuminance image2=[PCA of Captures-Narrowband-NoGamma kept stack] image3=[-- None --]");
                            IJ.run("YCbCr stack to RGB");
                            WindowManager.getImage("YCC").close();
                            extensionIndex = listOfHemisphereCaptures[i].getName().indexOf(".");
                            if (extensionIndex != -1)
                            {
                                String simpleName1 = listOfHemisphereCaptures[i].getName().substring(0, extensionIndex);
                                String simpleName2 = projectName + "_";
                                String simpleName3 = simpleName1.substring(simpleName1.indexOf("RTI-"));
                                filePath = projectDirectory+"PseudoColorRTI"+File.separator+"PseudoColor_"+simpleName2+simpleName3;
                                simpleImageName = "PseudoColor_"+simpleName2+simpleName3;
                            }
                            noClobber(filePath+".jpg");
                            IJ.saveAs("jpeg", filePath+".jpg");
                            WindowManager.getImage(simpleImageName+".jpg").close();
                        } 
                        else if (psRtiDesired) {
                            //Was this supposed to be Enhanced Luminance or Luminance?
                            IJ.run("Concatenate...", "  title=[YCC] keep image1=EnhancedLuminance image2=[PCA of Captures-Narrowband-NoGamma kept stack] image3=[-- None --]");
                            IJ.run("YCbCr stack to RGB");
                            WindowManager.getImage("YCC").close();
                            extensionIndex = listOfHemisphereCaptures[i].getName().indexOf(".");
                            if (extensionIndex != -1)
                            {
                                String simpleName1 = listOfHemisphereCaptures[i].getName().substring(0, extensionIndex); //.toString().substring(0, extensionIndex)
                                String simpleName2 = projectName + "_";
                                String simpleName3 = simpleName1.substring(simpleName1.indexOf("RTI-"));
                                filePath = projectDirectory+"PseudoColorRTI"+File.separator+"PseudoColor_"+simpleName2+simpleName3;
                                simpleImageName = "PseudoColor_"+simpleName2+simpleName3;
                            }
                            noClobber(filePath+".jpg");
                            IJ.saveAs("jpeg", filePath+".jpg");
                            WindowManager.getImage(simpleImageName+".jpg").close();
                        }
                        WindowManager.getImage("EnhancedLuminance").changes = false;
                        imp.changes = false;
                        WindowManager.getImage("EnhancedLuminance").close();
                        imp.close();
                    }
		}
                WindowManager.getImage("PCA of Captures-Narrowband-NoGamma kept stack").changes = false;
                WindowManager.getImage("PCA of Captures-Narrowband-NoGamma kept stack").close();
                System.gc();
		if (psRtiDesired) {
                    createLpFile("Pseudocolor", projectDirectory);
                    runFitter("Pseudocolor");
		}
                IJ.run("Collect Garbage");
            }
            if (csRtiDesired || csRakingDesired) { //processing phase
		csSource = csSource.replace("\\",File.separator);
		String[] csParents = csSource.split(File.separator);
		String csProcessName = csParents[csParents.length-2];
                File csProcessFile = new File(projectDirectory+csProcessName+"RTI"+File.separator);
		if (!csProcessFile.exists()) {
                    Files.createDirectory(csProcessFile.toPath());
                    logService.log().info("A directory has been created for "+csProcessName+" RTI at "+projectDirectory+csProcessName+"RTI"+File.separator);
		}
                imp = opener.openImage(csSource);
                imglib2_img = ImagePlusAdapter.wrap( imp );
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
		} 
                else if (imp.getImageStackSize() == 2) {
                    IJ.run(imp, "8-bit", "");
                    IJ.run(imp, "Stack to Images", "");
                    WindowManager.getImage(1).setTitle("Cb");
                    WindowManager.getImage(2).setTitle("Cr");
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
		}
                WindowManager.getImage("csSource").close();
		if (!transmissiveSource.equals("")) {
                    imp = opener.openImage( transmissiveSource );
                    imglib2_img = ImagePlusAdapter.wrap( imp );
                    IJ.run(imp, "8-bit", "");
                    imp.show();
                    IJ.run("Concatenate...", "  title=[YCC] keep image1=TransmissiveLuminance image2=Cb image3=Cr image4=[-- None --]");
                    IJ.run("YCbCr stack to RGB");
                    imp.changes = false;
                    imp.close();
                    noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_Tx.tiff");
                    IJ.save(WindowManager.getImage("YCC - RGB"), projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_Tx.tiff");
                    createJp2(projectName+"_"+csProcessName+"_Tx", projectDirectory);
                    toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_Tx.tiff");
                    Files.deleteIfExists(toDelete.toPath());
                    WindowManager.getImage("YCC - RGB").close();
                    WindowManager.getImage("YCC").close();
		}
		for(int i=0;i<listOfHemisphereCaptures.length;i++) {
                    if (listOfHemisphereCaptures[i].toString().endsWith("tiff")|| listOfHemisphereCaptures[i].toString().endsWith("tif")) {
                        if ((csRtiDesired)|| listOfRakingDirections.get(i)) {
                            imp = opener.openImage( listOfHemisphereCaptures[i].toString() );
                            imglib2_img = ImagePlusAdapter.wrap( imp );
                            int extensionIndex = listOfHemisphereCaptures[i].getName().indexOf(".");
                            if (extensionIndex != -1)
                            {
                                String simpleName1 = listOfHemisphereCaptures[i].getName().substring(0, extensionIndex); //.toString().substring(0, extensionIndex)
                                String simpleName2 = projectName + "_";
                                String simpleName3 = simpleName1.substring(simpleName1.indexOf("RTI-"));
                                filePath = projectDirectory+"CustomSourceRTI"+File.separator+"CustomSource_"+simpleName2+simpleName3;
                                simpleImageName = "CustomSource_"+simpleName2+simpleName3;
                            }
                            noClobber(filePath+".jpg");
                            IJ.saveAs("jpeg", filePath+".jpg");
                            IJ.run(imp, "Duplicate...", "title=EnhancedLuminance");
                            if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                                region = new RectangleOverlay();
                                imp.setRoi(normX,normY,normWidth,normHeight); 
                                IJ.run(WindowManager.getImage("EnhancedLuminance"),"Enhance Contrast...", "saturated=0.4");
                                IJ.run("Select None");
                            } else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                                IJ.run(WindowManager.getImage("EnhancedLuminance"),"Multiply...", "value="+normalizationFixedValue+"");
                            }
                            IJ.run(WindowManager.getImage("Luminance"), "8-bit", "");
                            WindowManager.getWindow("EnhancedLuminance").toFront();
                            IJ.run(WindowManager.getImage("EnhancedLuminance"), "8-bit", "");
                            if (listOfRakingDirections.get(i)){
                                IJ.run("Concatenate...", "  title=[YCC] keep image1=EnhancedLuminance image2=Cb image3=Cr image4=[-- None --]");
                                IJ.run("YCbCr stack to RGB");
                                WindowManager.getImage("YCC").close();
                                positionNumber = IJ.pad(i+1, 2).toString();
                                noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_"+positionNumber+".tiff");
                                IJ.save(WindowManager.getImage("YCC - RGB"),projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_"+positionNumber+".tiff");
                                WindowManager.getImage("YCC - RGB").close();
                                createJp2(projectName+"_"+csProcessName+"_"+positionNumber, projectDirectory);
                                toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_"+positionNumber+".tiff");
                                Files.deleteIfExists(toDelete.toPath());
                            }
                            if ((csRtiDesired)&&(brightnessAdjustApply.equals("RTI images also"))){
                                IJ.run("Concatenate...", "  title=[YCC] keep image1=EnhancedLuminance image2=Cb image3=Cr image4=[-- None --]");
                                IJ.run("YCbCr stack to RGB");
                                WindowManager.getImage("YCC").close();
                                noClobber(filePath+".jpg");
                                IJ.saveAs("jpeg", filePath+".jpg");
                                WindowManager.getImage(simpleImageName+".jpg").close();
                            } 
                            else if (csRtiDesired) {
                                IJ.run("Concatenate...", "  title=[YCC] keep image1=Luminance image2=Cb image3=Cr image4=[-- None --]");
                                IJ.run("YCbCr stack to RGB");
                                WindowManager.getImage("YCC").close();
                                noClobber(filePath+".jpg");
                                IJ.saveAs("jpeg", filePath+".jpg");
                            }
                            WindowManager.getImage("EnhancedLuminance").changes = false;
                            imp.changes = false;
                            WindowManager.getImage("EnhancedLuminance").close();
                            imp.close();
                        }
                    }
		}
                WindowManager.getImage("Cb").close();
                WindowManager.getImage("Cr").close();
		createLpFile(csProcessName, projectDirectory);
		runFitter(csProcessName);
                IJ.run("Collect Garbage");
            }
            IJ.beep();
            WindowManager.closeAllWindows();
            GenericDialog end = new GenericDialog("Processing Complete");
            end.addMessage("Processing Complete at "+timestamp());
            end.setMaximumSize(bestFit);
            end.showDialog();
            logService.log().warn("END OF TESTED MACRO PIECE");
        }
        	
        /**
         * The whole macro will run from here.  This is what fires when its clicked in imageJ.  
         * 
         */
        @Override
	public void run() {
            try {
               theMacro_tested();
               //testCode();
            } catch (IOException ex) {
                Logger.getLogger(SpectralRTI_Toolkit.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Throwable ex) {
                Logger.getLogger(SpectralRTI_Toolkit.class.getName()).log(Level.SEVERE, null, ex);
            }
              logService.log().warn("Finished processing the run().");
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
        public String createJp2(String inFile, String projDir) throws IOException, InterruptedException {
            logService.log().info("We are attempting to create jp2 file "+inFile);
            String preferredCompress = theList.get("preferredCompress");
            String preferredJp2Args = theList.get("preferredJp2Args");
            preferredCompress = preferredCompress.replace("/", File.separator);
            preferredJp2Args = preferredJp2Args.replace("/", File.separator);
            String compressString = "preferredCompress="+preferredCompress+System.lineSeparator();
            String preferredString = "preferredJp2Args="+preferredJp2Args+System.lineSeparator();
            OpenDialog dialog;  //For files
            String directory = "";
            String returnString = "/created/JP2file";
            if (preferredCompress.equals("")){
                dialog = new OpenDialog("Locate kdu_compress or ojp_compress"); 
                preferredCompress = dialog.getPath();
                preferredCompress = preferredCompress.replace("\\", "/");
                prefsFileAsText = new String(Files.readAllBytes(spectralPrefsFile.toPath()), "UTF8");
                compressString = "preferredCompress="+preferredCompress+System.lineSeparator();
                System.out.println("Compress String: "+compressString); 
                prefsFileAsText = prefsFileAsText.replaceFirst("preferredCompress=.*\\"+System.lineSeparator(), compressString); //replace the prefs var
                theList.put("preferredCompress", preferredCompress);
                Files.write(spectralPrefsFile.toPath(), prefsFileAsText.getBytes()); //rewrite the prefs file
            }
            if (preferredJp2Args.equals("")){
                GenericDialog gd = new GenericDialog("Approve arguments for Jpeg 2000 compression");
                String arguments = "-rate -,2.4,1.48331273,.91673033,.56657224,.35016049,.21641118,.13374944,.08266171 Creversible=no Clevels=5 Stiles={1024,1024} Cblk={64,64} Cuse_sop=yes Cuse_eph=yes Corder=RPCL ORGgen_plt=yes ORGtparts=R Cmodes=BYPASS -double_buffering 10 -num_threads 4 -no_weights";
                gd.addStringField("Arguments:",arguments,80);
                gd.setMaximumSize(bestFit);
                gd.showDialog();
                preferredJp2Args = gd.getNextString();
                preferredJp2Args =preferredJp2Args.replace("\\", "/");
                preferredString = "preferredJp2Args="+preferredJp2Args+System.lineSeparator();
                theList.put("preferredJp2Args", preferredJp2Args);
                prefsFileAsText = new String(Files.readAllBytes(spectralPrefsFile.toPath()), "UTF8");
                prefsFileAsText = prefsFileAsText.replaceFirst("preferredJp2Args=.*\\"+System.lineSeparator(), preferredString); //replace the prefs var
                Files.write(spectralPrefsFile.toPath(), prefsFileAsText.getBytes()); //rewrite the prefs file
            }
            File preferredCompressFile = new File(preferredCompress);
            String compressLocation = preferredCompressFile.getParent();
            Boolean noClob = noClobber(projDir+"StaticRaking"+File.separator+inFile+".jp2"); 
            File checkFileForTesting = new File(projDir+"StaticRaking"+File.separator+inFile+".tiff");
            String commandString = preferredCompress+" -i "+projDir+"StaticRaking"+File.separator+inFile+".tiff -o "+projDir+"StaticRaking"+File.separator+inFile+".jp2 "+preferredJp2Args;
            if(isWindows){
                p = Runtime.getRuntime().exec(commandString, null, new File(compressLocation)); //compressLocation
                p.waitFor();   
                returnString = projDir+"StaticRaking"+File.separator+inFile+".jp2";
                //preferred compress is kdu_compress.exe (or some other executable).  The args used are from those.  It should be platform independent
            }
            else{
                p = Runtime.getRuntime().exec(commandString);
            }
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
            if (oldFile.exists()) {
                logService.log().error(safeName+" does exist, we must rename the file.");
                verboseDate = Files.getLastModifiedTime(safeNamePath).toString();
                verboseDate = verboseDate.replace(" ","_");
                verboseDate = verboseDate.replace(":","-");
                newFileName = newFileName.replace(".","("+verboseDate+").");
                File newFileFileName = new File(newFileName);
                success = oldFile.renameTo(newFileFileName);
            }
            else{ //Old file did not exist, so no need to rename and we can return success.
                logService.log().info(safeName+" does not exist, it is a safe name to save as");
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
            SimpleDateFormat ft = new SimpleDateFormat ("yyyyMMdd'_'hhmm");
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
            imglib2_img = ImagePlusAdapter.wrap( imp );
            imp.setTitle("Preview");
            //ImageJFunctions.show(imglib2_img, "Preview");
            imp.show();
            String[] brightnessAdjustOptions = new String[3];
            brightnessAdjustOptions[0] = "No";
            brightnessAdjustOptions[1] = "Yes, by normalizing each image to a selected area";
            brightnessAdjustOptions[2] = "Yes, by multiplying all images by a fixed value";
            
            String[] brightnessAdjustApplies = new String[2];
            brightnessAdjustApplies[0] = "Static raking images only (recommended)";
            brightnessAdjustApplies[1] = "RTI images also";
            
            GenericDialog gd = new GenericDialog("Adjust brightness of hemisphere captures?");
            gd.addRadioButtonGroup("Adjust brightness of hemisphere captures? ", brightnessAdjustOptions, brightnessAdjustOptions.length, 1, brightnessAdjustOptions[1]);
            gd.addRadioButtonGroup("Apply adjustment to which output images? ",brightnessAdjustApplies,brightnessAdjustApplies.length,1,brightnessAdjustApplies[0]);
            gd.setMaximumSize(bestFit);
            gd.showDialog();
            
            brightnessAdjustOption = gd.getNextRadioButton();
            brightnessAdjustApply = gd.getNextRadioButton();
            
            if(gd.wasCanceled()){
                //@userHitCancel
                brightnessAdjustOption = "No";
            }

            if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                //gd.setVisible(false);
                dWait = new WaitForUserDialog("Select Area","Draw a rectangle containing the brighest white and darkest black desired then press OK\n(hint: use a large area including spectralon and the object, excluding glare)" );
                dWait.show();
                if(dWait.escPressed() || imp.getRoi() == null){
                    //@userHitCancel
                    IJ.error("You must draw a rectangle to continue!");
                    throw new Throwable("You must draw a rectangle to continue!");
                }
                bounds = imp.getRoi().getBounds();
                region = new RectangleOverlay();
                normX = bounds.x;
                normY = bounds.y;
                normHeight = bounds.height;
                normWidth = bounds.width;

            } 
            else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                dWait = new WaitForUserDialog("Use the Muliply dialog to preview and choose a multiplier value.\nThis is just a preview image; the chosen value will be entered next." );
                dWait.show();
                if(dWait.escPressed()){
                    //@userHitCancel
                    IJ.error("You must supply a multiplier to continue!");
                    throw new Throwable("You must supply a multiplier to continue!");
                }
                IJ.run(imp, "Multiply...", "");
                GenericDialog gdMultiplier = new GenericDialog("Enter selected multiplier");
                gdMultiplier.addNumericField("Enter selected multiplier: ", 1.30,2,4,"");
                gdMultiplier.setMaximumSize(bestFit);
                gdMultiplier.showDialog();
                normalizationFixedValue = (int) gdMultiplier.getNumericFields().get(0);
            }
            else{
                logService.log().warn("Brightness not modified");
            }
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
            preferredFitter = preferredFitter.replace("/", File.separator);
            String appendString = "preferredFitter="+preferredFitter+System.lineSeparator();
            File preferredHSH;
            String hshLocation = "";
            JFrame fitterNoticeFrame = new JFrame("Fitter Working...");
            contentPane = new JPanel();
            contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));
            JPanel labelPanel = new JPanel();
            JLabel fitterText = new JLabel("Running the fitter.  This could take a while.  This window will close and a notification"
                    + " will appear when the process is complete.  Thank you for your patience.");
            labelPanel.add(fitterText);
            contentPane.add(labelPanel);
            fitterNoticeFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            fitterNoticeFrame.getContentPane().add(contentPane);
            fitterNoticeFrame.pack();
            fitterNoticeFrame.setLocation(screenSize.width/2-fitterNoticeFrame.getSize().width/2, screenSize.height/2-fitterNoticeFrame.getSize().height/2);
            fitterNoticeFrame.setVisible(true);  
            File fitterFile = new File(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt");
            if(!fitterFile.exists()){
                Files.createFile(fitterFile.toPath());
            }
            if (preferredFitter.equals("")) {
                OpenDialog dialog = new OpenDialog("Locate Preferred RTI Fitter or cmd file for batch processing");
                preferredFitter = dialog.getPath();
                preferredFitter =preferredFitter.replace("\\", "/");
                appendString = "preferredFitter="+preferredFitter+System.lineSeparator();
                prefsFileAsText = prefsFileAsText.replaceFirst("preferredFitter=.*\\"+System.lineSeparator(), appendString); //replace the prefs var
                Files.write(spectralPrefsFile.toPath(), prefsFileAsText.getBytes()); //rewrite the prefs file
            }
            if (preferredFitter.endsWith("hshfitter.exe")) { // use HSH fitter
                int hshOrder = Integer.parseInt(theList.get("hshOrder"));
                if (hshOrder < 2 ) hshOrder = 3;
                int hshThreads = Integer.parseInt(theList.get("hshThreads"));
                if (hshThreads < 1 ) hshThreads = 16;
                preferredHSH = new File(preferredFitter);
                hshLocation = preferredHSH.getParent();
                appendString += "Brightness Adjust Option: "+brightnessAdjustOption+System.lineSeparator();
                
                Files.write(fitterFile.toPath(), (appendString+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                    appendString += "Normalization area bounds: "+normX+", "+normY+", "+normWidth+", "+normHeight+System.lineSeparator();
                    Files.write(fitterFile.toPath(), (appendString+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                } 
                else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                    appendString += "Normalization fixed value: "+normalizationFixedValue+System.lineSeparator();
                    Files.write(fitterFile.toPath(), (appendString+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                }
                if (pcaX > 0) {
                    appendString += "PCA area bounds: "+pcaX+", "+pcaY+", "+pcaWidth+", "+pcaHeight+System.lineSeparator();
                    Files.write(fitterFile.toPath(), (appendString+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                }
                appendString += "Jpeg Quality: "+jpegQuality+" (edit SpectralRTI_Toolkit-prefs.txt to change)"+System.lineSeparator();
                Files.write(fitterFile.toPath(), appendString.getBytes(), StandardOpenOption.APPEND);
                appendString += "Executing command "+preferredFitter+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti"+System.lineSeparator();
                Files.write(fitterFile.toPath(), appendString.getBytes(), StandardOpenOption.APPEND);
                if(isWindows){
                    //preferredfitter is hshFitter.exe (or some other executable).  The args used are from those.  It should be platform independent
                    String commandString = preferredFitter+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp"+" "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti";
                    logService.log().info("Running the following fitter command...");
                    logService.log().info(commandString);
                    logService.log().info("Working directory for command is "+hshLocation);
                    p = Runtime.getRuntime().exec(commandString, null, new File(hshLocation)); //hshLocation
                    p.waitFor();
                    contentPane.removeAll();
                }
                else{
                    String commandString = preferredFitter+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp"+" "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti";
                    p = Runtime.getRuntime().exec(commandString);
                    contentPane.removeAll();
                }
                //should if(webRTIDesired) be here??
                createWebRTIFiles(colorProcess, "");
            } 
            else if (preferredFitter.endsWith("cmd")||preferredFitter.endsWith("bash")) {
                logService.log().info("Detected the preferred fitter is in fact a cmd or bash file.  This will defer processing.");
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
                appendString += "Executing command "+preferredFitter+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti"+System.lineSeparator();
                commandString += "hshfitter "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti"+System.lineSeparator();
                commandString += "webGLRTIMaker "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti -q "+jpegQualityWebRTI+" -r "+ramWebRTI+System.lineSeparator();
                if (webRtiDesired) {
                    String webRtiString = "<html lang=\"en\" xml:lang=\"en\"> <head> <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /> <title>WebRTI "+projectName+"_"+colorProcess+"</title> <link type=\"text/css\" href=\"css/ui-lightness/jquery-ui-1.10.3.custom.css\" rel=\"Stylesheet\"> <link type=\"text/css\" href=\"css/webrtiviewer.css\" rel=\"Stylesheet\"> <script type=\"text/javascript\" src=\"js/jquery.js\"></script> <script type=\"text/javascript\" src=\"js/jquery-ui.js\"></script> <script type=\"text/javascript\" src=\"spidergl/spidergl_min.js\"></script> <script type=\"text/javascript\" src=\"spidergl/multires_min.js\"></script> </head> <body> <div id=\"viewerContainer\"> <script  type=\"text/javascript\"> createRtiViewer(\"viewerContainer\", \""+projectName+"_"+colorProcess+"RTI_"+startTime+"\", $(\"body\").width(), $(\"body\").height()); </script> </div> </body> </html>";
                    appendString += webRtiString;
                }
                Files.write(fitterFile.toPath(), appendString.getBytes(), StandardOpenOption.APPEND);
                Files.write(Paths.get(preferredFitter), commandString.getBytes(), StandardOpenOption.APPEND);
            } 
            else if (preferredFitter.endsWith("PTMfitter.exe")) { // use PTM fitter
                IJ.error("Macro code to execute PTMfitter not yet complete. Try HSHfitter.");
                throw new Throwable("Macro code to execute PTMfitter not yet complete. Try HSHfitter."); //@@@
            } 
            else {
                IJ.error("Problem identifying type of RTI fitter.  Please provide the hshfitter or deferred batch file.");
                throw new Throwable("Problem identifying type of RTI fitter");
            }
            fitterNoticeFrame.setVisible(false);
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
            GenericDialog noLpData = new GenericDialog("Light Position data not found.");
            if (lpSource.equals("")) { //Then we need to find and set it
                //Check LightPositionData folder
                listOfLpFiles_list = new ArrayList<String>();
                folder = new File(projectDirectory+"LightPositionData"+File.separator);
                if(folder.exists()){
                    list = folder.listFiles();
                    for (File file1 : list) {
                        if (file1.getName().endsWith("lp")) {
                            listOfLpFiles_list.add(file1.toString());
                        }
                    }
                }
                else{
                    noLpData.addMessage("Please provide LP data in a LightPositionData directory in your project directory.  A LightPositionData directory was created for you.");
                    noLpData.setMaximumSize(bestFit);
                    noLpData.showDialog();
                    //throw new Throwable("You need to have light position data to continue.");
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
                            listOfLpFiles_list.add(projectDirectory+"LightPositionData"+File.separator+"assembly-files"+File.separator+list[i].toString());
                        }
                    }
                }             
                listOfLpFiles = new String[listOfLpFiles_list.size()];
                listOfLpFiles_list.toArray(listOfLpFiles);
                if(listOfLpFiles_list.size() == 1){
                    lpSource = listOfLpFiles_list.get(0);
                } 
                else if(listOfLpFiles_list.isEmpty()){
                    noLpData.addMessage("Please provide light position source files in your LightPositionData directory in the future.");
                    noLpData.setMaximumSize(bestFit);
                    noLpData.showDialog();
                    //throw new Throwable("You need to have light position data to continue.");
                    OpenDialog dialog = new OpenDialog("Locate Light Position Source File"); 
                    lpSource = dialog.getPath();
                }
                else{
                    GenericDialog dialog = new GenericDialog("Select Light Position Source File"); 
                    dialog.addMessage("We found light position files in your project directory.  Please choose the source file to use from below.");
                    dialog.addRadioButtonGroup("File: ", listOfLpFiles, listOfLpFiles_list.size(), 1, listOfLpFiles_list.get(0));
                    dialog.setMaximumSize(bestFit);
                    dialog.showDialog();
                    lpSource = dialog.getNextRadioButton();
                }
            }
            if(lpSource.equals("")){
                GenericDialog lpFileFailure = new GenericDialog("Provide Light Position Source Data");
                lpFileFailure.addMessage("Light position source data not found.  You must provide this data to continue.");
                lpFileFailure.setMaximumSize(bestFit);
                lpFileFailure.showDialog();
                throw new Throwable("Light position source data not found");
            }
            File lpFile = new File(lpSource);
            if(!lpFile.exists()){
                GenericDialog lpFileFailure = new GenericDialog("Provide Light Position Source Data");
                lpFileFailure.addMessage("Light position source data not found.  You must provide this data to continue.");
                lpFileFailure.setMaximumSize(bestFit);
                lpFileFailure.showDialog();
                throw new Throwable("Light position source data not found");
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
        
        private void createWebRTIFiles(String colorProcess, String rtiImage) throws IOException, InterruptedException{
            logService.log().info("Create WebRTI for color process "+colorProcess+"...");
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
            String webRtiString = "<html lang=\"en\" xml:lang=\"en\"> <head> <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /> <title>WebRTI "+projectName+"_"+colorProcess+"</title> <link type=\"text/css\" href=\"css/ui-lightness/jquery-ui-1.10.3.custom.css\" rel=\"Stylesheet\"> <link type=\"text/css\" href=\"css/webrtiviewer.css\" rel=\"Stylesheet\"> <script type=\"text/javascript\" src=\"js/jquery.js\"></script> <script type=\"text/javascript\" src=\"js/jquery-ui.js\"></script> <script type=\"text/javascript\" src=\"spidergl/spidergl_min.js\"></script> <script type=\"text/javascript\" src=\"spidergl/multires_min.js\"></script> </head> <body> <div id=\"viewerContainer\"> <script  type=\"text/javascript\"> createRtiViewer(\"viewerContainer\", \""+projectName+"_"+colorProcess+"RTI_"+startTime+"\", $(\"body\").width(), $(\"body\").height()); </script> </div> </body> </html>";
            if (webRtiDesired) {
                noticeFrame.setVisible(true);  
                logService.log().info("I have found a desire for WebRTI...input");
                String webString;
                String webRTIDir;
                webRtiMaker = theList.get("webRtiMaker");
                webRtiMaker = webRtiMaker.replace("/", File.separator);
                if (webRtiMaker.equals("")) {
                    OpenDialog dialog2 = new OpenDialog("Locate webGLRTIMaker.exe");
                    webRtiMaker = dialog2.getPath();
                    webRTIDir = dialog2.getDirectory();
                    webRtiMaker =webRtiMaker.replace("\\", "/");
                    webString = "webRtiMaker="+webRtiMaker;
                    prefsFileAsText = prefsFileAsText.replaceFirst("webRtiMaker=.*\\"+System.lineSeparator(),webString+System.lineSeparator()); //replace the prefs var
                    Files.write(spectralPrefsFile.toPath(), prefsFileAsText.getBytes()); //rewrite the prefs file
                }
                else{
                    webRTIDir = new File(webRtiMaker).getParent();
                }
                /* if the user provided an RTI image location, use that.  Otherwise, use the one the fitter made. */
                if(rtiImage.equals("")){
                    rtiImage = projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti";
                }
                logService.log().info("I need to know what the webRTI maker is..."+webRtiMaker);
                if(isWindows){
                    String commandString = webRtiMaker+" "+rtiImage+" -q "+jpegQualityWebRTI+" -r "+ramWebRTI;
                    logService.log().info("Running the webRTICommand...");
                    logService.log().info(commandString);                        
                    p2 = Runtime.getRuntime().exec(commandString, null, new File(webRTIDir)); //hshLocation
                    p2.waitFor();
                    Files.createFile(new File(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+"_wrti.html").toPath());
                    Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+"_wrti.html"), webRtiString.getBytes(), StandardOpenOption.APPEND);
                }
                else{
                    String commandString = webRtiMaker+" "+rtiImage+" -q "+jpegQualityWebRTI+" -r "+ramWebRTI;
                    p2 = Runtime.getRuntime().exec(commandString);
                    Files.createFile(new File(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+"_wrti.html").toPath());
                    Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+"_wrti.html"), webRtiString.getBytes(), StandardOpenOption.APPEND);
                }
                noticeFrame.setVisible(false);  
            }
            else{ //webRTIDesired was false.  Give a message? 
                
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
            Class<?> clazz = SpectralRTI_Toolkit.class;
            String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
            String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
            System.setProperty("plugins.dir", pluginsDir);                
            final ImageJ IJinstance = net.imagej.Main.launch(args);
            IJinstance.command().run(SpectralRTI_Toolkit.class, false);
            System.out.println("Finished processing MAIN");
	}
      
}
