/**
 * @author  Bryan Haberberger
 * @version 0.5
 * @since   07/01/2017
 * <h1>Spectral RTI Toolkit ImageJ2 Java Plugin</h1>
 * <p>
    * Created by the Walter J Ong S.J. Center for Digital Humanities at Saint Louis University.  
    * Hosted at the Center's GitHub repo 
    * https://github.com/CenterForDigitalHumanities/SpectralRTI_Toolkit
 * </p>
 * <p>
    * This was originally written as an ImageJ Macro by Todd Hanneken.  Hosted in Todd's repo at
    * https://github.com/thanneken/SpectralRTI_Toolkit
 * </p>

  *<h2> Helpful sources links </h2>
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


//ImageJ2 specific imports
import java.awt.Rectangle;
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
import java.io.UnsupportedEncodingException;
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

// Apache Commons for native shell command support.  Still requires a windows + all other OS version.  Follow the isWindows variable
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;

/**
 * @author bhaberbe
 * @see the @Plugin tag here, it allows me to define where I want this to show up in the ImageJ menu.
 * The class to be be implemented as an ImageJ command.  Does not need an image opened already.  
 * This is compliant with ImageJ2 and intended to be used with the Fiji version. Java 8 compatible?
 */
@Plugin(type = Command.class, menuPath = "Plugins>SpectralRTI_Toolkit")  
public class SpectralRTI_Toolkit implements Command {
        private Context context;
        
        /** The global ImagePlus object to be used throughout.  Used with the IJ library for ImageJ functionality */
        protected ImagePlus imp;
        
        /** The global ImgLib2 compatible Img type object to be used throughout.  Used with the ImgLib2 library. */
        protected Img< FloatType > imglib2_img;       
        
        /** The logger for the given application, in this case ImageJ */
        @Parameter
        private LogService logService;
                
        //SRTI vars
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
        private Rectangle bounds;
        //This is important for native commands.  Java is NOT PLATFORM INDEPENDENT, so check what platform we are.
        private final boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        private String positionNumber = "";
        String pcaMethod = "";
        private List<File> redNarrowbands_list = new ArrayList<>();
        private List<File> greenNarrowbands_list = new ArrayList<>();
        private List<File> blueNarrowbands_list = new ArrayList<>();
        
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
            theList.put("webRitMaker", "");
        }
        final ImageJ ij2 = new ImageJ();
        //End SRTI vars
	public double value;
        public String name;
        public File spectralPrefsFile = new File("SpectralRTI_Toolkit-prefs.txt");//This is in the base fiji folder. 
        public String prefsFileAsText = "";
        
        private void theMacro_tested() throws IOException, Throwable{
            //want these variables to be accessible across functions and to reset each time the macro is run
            startTime = timestamp();
            File accurateColorSource = null;
            //vars that I had to add
            HashMap <String, String> prefsConsolut = new HashMap<>();
            
            GenericDialog prefsDialog = new GenericDialog("Consult Preferences");
            String line= "";
            String[] prefs = null;
            String prefsLines = "";
            List<String> prefsConsult_list = null;
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
            File[] listOfAccurateColorFiles = new File[0];
            File[] listOfNarrowbandCaptures = new File[0];
            File[] listOfHemisphereCaptures = new File[0];
            String csSource = "";
            BufferedReader prefsReader = null;
            boolean swapBack = false;
            //End vars I had to add
            /*
                *Consult user for the project directory
                *@exception Kill if no directory provided
            */
            file_dialog = new DirectoryChooser("Choose the Project Directory"); 
            projectDirectory = file_dialog.getDirectory();
            logService.log().info("Project directory is ...  "+projectDirectory+" ...");
            if(projectDirectory == null || projectDirectory.equals("")){
                logService.log().warn("No project directory provided.  Error out");
                IJ.error("You must provide a project directory to continue.");
                throw new Throwable("You must provide a project directory."); //DIE if now directory provided
            }
            else{
                projectDirectory = projectDirectory.replace("\\",File.separator);
            }
                                    
            /*
             *consult with user about values stored in prefs file in base fiji folder.  Do not need project directory for this.
            */
            if (spectralPrefsFile.exists()) { //If this exists, overwrite the labels and show a dialog with the settings
                prefsDialog.addMessage("The following settings are remembered from the configuration file or a previous run.\nEdit or clear as desired.");
                prefsFileAsText = new String(Files.readAllBytes(spectralPrefsFile.toPath()), "UTF8");

                prefs = prefsFileAsText.split(System.lineSeparator()); //This does not work
                logService.log().info(Arrays.toString(prefs));
                for (int i=0;i<prefs.length;i++) {
                    //Swap the labels out for presentation
                    String key = prefs[i].substring(0, prefs[i].indexOf("="));
                    key = key.replace("preferredCompress","JP2 Compressor");
                    key = key.replace("preferredJp2Args","JP2 Arguments");
                    key = key.replace("preferredFitter","HSH Fitter");
                    key = key.replace("jpegQuality","JPEG Quality");
                    key = key.replace("hshOrder","HSH Order");
                    key = key.replace("hshThreads","HSH Threads");
                    key = key.replace("webRtiMaker","Web RTI maker");
                    String value1 = prefs[i].substring(prefs[i].indexOf("=")+1); //Pre-populate choices
                    prefsDialog.addStringField(key, value1, 80);
                }
                prefsDialog.showDialog();
                swapBack = true;
            }
            else{
                GenericDialog noPrefs = new GenericDialog("No preference file found");
                noPrefs.addMessage("A prefs file will be created for you to store your choices in later sessions.");
                noPrefs.showDialog();
                logService.log().warn("We are making  a new prefs file with the empty defaults.");
                /**
                    *This will put it in the folder that ImageJ.exe is run out of.  Do we want a prefs directory inside a project folder instead? 
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
                    String value2 = prefsDialog.getNextString(); //Gather new information
                    theList.put(key,value2);
                    prefsFileAsText = prefsFileAsText.replaceFirst(key+"=.*\\"+System.getProperty("line.separator"), key+"="+value2+System.getProperty("line.separator")); //replace the prefs var
                }
                Files.write(spectralPrefsFile.toPath(), prefsFileAsText.getBytes()); //rewrite the prefs file
            }
            else{
                logService.log().info("A prefs file was just created.");
            }
            jpegQuality = ij.plugin.JpegWriter.getQuality();
            logService.log().info("JPEG quality from system: "+jpegQuality);
            logService.log().info("JPEG from theList "+theList.get("jpegQuality"));
            int jpq = Integer.parseInt(theList.get("jpegQuality"));
            if (jpq > 0){
                jpegQuality = jpq;
                logService.log().info("JPEG quality from prefs: "+jpegQuality);
            }
            else{
                /**
                 * Don't actually write this to the file, force the user to edit it themselves. 
                 * @see appendString variable
                 */
            }
            IJ.run("Input/Output...","jpeg="+jpegQuality);
            
            listOfHemisphereCaptures = getHemisphereCaptures(projectDirectory+"Captures-Hemisphere-Gamma"+File.separator);
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

            if (!hemi_gamma_dir.exists()) { //added the ! here.  Should have I? Todd Says yes!
                Path createPath = hemi_gamma_dir.toPath();
                Files.createDirectory(createPath);
                logService.log().info("A directory has been created for the Hemisphere Captures at "+projectDirectory+"Captures-Hemisphere-Gamma"+File.separator);
                hemi_gamma_dir = new File(createPath.toString());
            }
            listOfHemisphereCaptures = hemi_gamma_dir.listFiles();
            while (listOfHemisphereCaptures.length <= 29 && IJ.showMessageWithCancel("Please Populate Hemisphere Captures","The software expects at least 30 images in HemisphereCaptures folder.\nPlease populate the folder and press Ok to continue, or cancel.")) {
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
            tasksDialog.addCheckbox("Accurate Color Static Raking",acRtiDesired);
            tasksDialog.addCheckbox("Extended Spectrum RTI",xsRtiDesired);
            tasksDialog.addCheckbox("Extended Spectrum Static Raking",xsRtiDesired);
            tasksDialog.addCheckbox("Pseudocolor RTI",psRtiDesired);
            tasksDialog.addCheckbox("Pseudocolor Static Raking",psRtiDesired);
            tasksDialog.addCheckbox("Custom RTI",false);
            tasksDialog.addCheckbox("Custom Static Raking",false);
            tasksDialog.addCheckbox("WebRTI",true);
            tasksDialog.showDialog();
            if (tasksDialog.wasCanceled()) {
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
            /** END DEBUGGING **/
            if (acRakingDesired || acRtiDesired || xsRtiDesired || xsRakingDesired || psRtiDesired || psRakingDesired || csRtiDesired || csRakingDesired){
                if (brightnessAdjustOption.equals("")) promptBrightnessAdjust(listOfHemisphereCaptures);
                logService.log().info("Back in main macro after brightness adjust prompt");
            }
            if (acRakingDesired || xsRakingDesired || psRakingDesired || csRakingDesired){
                logService.log().info("We triggered static raking code in main macro");
                if (!static_ranking_dir.exists()) {
                    Path staticFilePath = static_ranking_dir.toPath();
                    Files.createDirectory(staticFilePath);
                    logService.log().info("A directory has been created for lossless static raking images at "+projectDirectory+"StaticRaking"+File.separator);
                }
                File[] listOfTransmissiveSources_dir = new File[0];
                ArrayList<String> listOfTransmissiveSources_list = new ArrayList<String>();
                String[] listOfTransmissiveSources = new String[0];
                if(transmissive_gamma_dir.exists()){
                    listOfTransmissiveSources_dir=transmissive_gamma_dir.listFiles();
                    for (File f : listOfTransmissiveSources_dir) {
                        listOfTransmissiveSources_list.add(f.toString());
                    }
                }
                listOfTransmissiveSources_list.toArray(listOfTransmissiveSources); //will this work for an array of 0?
                if (listOfTransmissiveSources.length == 1){ // no opt out of creating a transmissive static if transmissive folder is populated, but not a problem
                    transmissiveSource = listOfTransmissiveSources[0];
                } 
                else if (listOfTransmissiveSources.length > 1) {
                    GenericDialog transSourceDialog = new GenericDialog("Select Transmissive Source");
                    transSourceDialog.addMessage("Select Transmissive Source");
                    transSourceDialog.addRadioButtonGroup("File: ", listOfTransmissiveSources, listOfTransmissiveSources.length, 1, listOfTransmissiveSources[0]);
                    transSourceDialog.showDialog();
                    transmissiveSource = transSourceDialog.getNextRadioButton();
                } 
                else if (listOfTransmissiveSources.length == 0) {
                    transmissiveSource = "";
                }

                boolean[] defaults = new boolean[listOfHemisphereCaptures.length];
                GenericDialog lightDialog = new GenericDialog("Select Light Positions");
                lightDialog.addMessage("Select light positions for lossless static raking images");
                ArrayList<String> listOfHemisphereCaptures_list = new ArrayList<String>();
                String[] listOfHemisphereCaptures_string = new String[listOfHemisphereCaptures.length];
                for(int l=0; l<listOfHemisphereCaptures.length; l++){
                    listOfHemisphereCaptures_list.add(listOfHemisphereCaptures[l].toString());
                }
                listOfHemisphereCaptures_list.toArray(listOfHemisphereCaptures_string);
                lightDialog.addCheckboxGroup(1+(listOfHemisphereCaptures.length/4), 4, listOfHemisphereCaptures_string, defaults); //8 columns
                lightDialog.showDialog();
                logService.log().info("Add T/F list of raking directions for hemisphere captures.");
                for(int k=0;k<listOfHemisphereCaptures.length;k++) {
                    //logService.log().info(lightDialog.getNextBoolean());
                    listOfRakingDirections.add(lightDialog.getNextBoolean());
                }
                logService.log().warn(listOfRakingDirections.toString());
            }
            else { //We already have the list initiated, so do nothing
                logService.log().info("We already have the list initiated, so do nothing.  Raking is not desired.");
                //listOfRakingDirections = newArray(listOfHemisphereCaptures.length+1);
            }
                       
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
                GenericDialog narrowBandDialog = new GenericDialog("Assign Narrowband Captures");
		narrowBandDialog.addMessage("Assign each narrowband capture to the visible range of R, G, B, or none");
                logService.log().info("I need a list of narrow band captures here");
                logService.log().info(Arrays.toString(listOfNarrowbandCaptures));
                if (listOfNarrowbandCaptures.length<9) { //Yikes is this right!  
                    logService.log().warn("You must have 9 or more narrow band captures!");
                    throw new Throwable("You must have 9 or more narrow band captures for Extended Spectrum!");
                }
		for (int i=0; i<listOfNarrowbandCaptures.length; i++) {
                    if ((i+1)/listOfNarrowbandCaptures.length < 0.34) defaultRange = "B";
                    else if ((i+1)/listOfNarrowbandCaptures.length > 0.67) defaultRange = "R";
                    else defaultRange = "G";
                    narrowBandDialog.setInsets(0,0,0);
                    String narrowCapture = listOfNarrowbandCaptures[i].toString();
                    narrowBandDialog.addRadioButtonGroup(narrowCapture, rgbnOptions, 1, 4, defaultRange);
		} 
                /**
                 * @see problem here if runs off screen... no an option to use two columns.  Bryan confirms, I noticed this as well.
                 */
                narrowBandDialog.showDialog();
		for (int j=0; j<listOfNarrowbandCaptures.length; j++) {
                    rangeChoice = narrowBandDialog.getNextRadioButton();
                    if (rangeChoice.equals("R")) {
                        redNarrowbands_list.add(listOfNarrowbandCaptures[j]);
                    } 
                    else if (rangeChoice.equals("G")) {
                        greenNarrowbands_list.add(listOfNarrowbandCaptures[j]);
                    } 
                    else if (rangeChoice.equals("B")) {
                        blueNarrowbands_list.add(listOfNarrowbandCaptures[j]);
                    }
		}
                logService.log().info("We should have red, green and blue narrow bands");
                
                logService.log().info("Do we have the three?");
                redNarrowbands = redNarrowbands_list.toArray();
                greenNarrowbands = greenNarrowbands_list.toArray();
                blueNarrowbands = blueNarrowbands_list.toArray();
                logService.log().info(Arrays.toString(redNarrowbands));
                logService.log().info(Arrays.toString(greenNarrowbands));
                logService.log().info(Arrays.toString(blueNarrowbands));
		if (pcaHeight < 100) { //Looks like it was defined as 0 and never set or changed.  
                    File narrowbandNoGamma = new File(listOfNarrowbandCaptures[Math.round(listOfNarrowbandCaptures.length/2)].toString()); //projectDirectory+"Captures-Narrowband-NoGamma"+File.separator+
                    imp = opener.openImage( narrowbandNoGamma.toString() );
                    imglib2_img = ImagePlusAdapter.wrap( imp );
                    ImageJFunctions.show(imglib2_img, "Preview");
                    dWait = new WaitForUserDialog("Select area", "Draw a rectangle containing the colors of interest for PCA\n(hint: limit to object or smaller)");
                    dWait.show();
                    bounds = WindowManager.getImage("Preview").getRoi().getBounds();
                    pcaX = bounds.x;
                    pcaY = bounds.y;
                    pcaHeight = bounds.height;
                    pcaWidth = bounds.width;
                    WindowManager.getImage("Preview").close();
		};
            }
            /**
             * @see only interaction here, processing later 
             */
            if (psRtiDesired || psRakingDesired) {
                //identify 2 source images for pca pseudocolor
                File listOfPseudocolorSources_dir = new File(projectDirectory+"PCA"+File.separator);
                if(!listOfPseudocolorSources_dir.exists()){
                    GenericDialog nofldr = new GenericDialog("FYI");
                    nofldr.addMessage("A Pseudo Color folder has been created for you");
                    nofldr.showDialog();
                    Files.createDirectory(listOfPseudocolorSources_dir.toPath());
                }
                File[] listOfPseudocolorSources = listOfPseudocolorSources_dir.listFiles();
                String defaultPca = "";
                if (listOfPseudocolorSources.length > 1) defaultPca = "Open pregenerated images" ;
                else defaultPca = "Generate and select using defaults";
                String[] listOfPcaMethods = new String[3];
                listOfPcaMethods[0]="Generate and select using defaults";
                listOfPcaMethods[1]="Generate and manually select two";
                listOfPcaMethods[2]="Open pregenerated images";
                GenericDialog pseudoSources = new GenericDialog("Select sources for Pseudocolor");
                pseudoSources.addMessage("Pseudocolor images require two source images (typically principal component images).");
                pseudoSources.addRadioButtonGroup("Method: ",listOfPcaMethods,listOfPcaMethods.length,1,defaultPca);
                pseudoSources.showDialog();
                pcaMethod = pseudoSources.getNextRadioButton();
                if (pcaHeight < 100) { //Looks like it was defined as 0 and never set or changed.  
                    imp = opener.openImage( listOfNarrowbandCaptures[Math.round(listOfNarrowbandCaptures.length/2)].toString() );
                    imglib2_img = ImagePlusAdapter.wrap( imp );
                    ImageJFunctions.show(imglib2_img, "Preview");
                    dWait = new WaitForUserDialog("Select area", "Draw a rectangle containing the colors of interest for PCA\n(hint: limit to object or smaller)");
                    dWait.show();
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
                logService.log().info("Should have source");
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
                dWait.show();
                bounds = WindowManager.getImage("Preview").getRoi().getBounds();
                WindowManager.getImage("Preview").close();
                for(int i=0;i<listOfHemisphereCaptures.length;i++) {
                    if (listOfHemisphereCaptures[i].toString().endsWith("tiff") || listOfHemisphereCaptures[i].toString().endsWith("tif")) {
                        imp=opener.openImage(listOfHemisphereCaptures[i].toString());
                        imglib2_img = ImagePlusAdapter.wrap( imp );
                        
                        //int extensionIndex = listOfHemisphereCaptures[i].toString().indexOf(".");
                        int extensionIndex = listOfHemisphereCaptures[i].getName().indexOf(".");
                        if (extensionIndex != -1)
                        {
                            simpleImageName = listOfHemisphereCaptures[i].getName().substring(0, extensionIndex); //.toString().substring(0, extensionIndex)
                        }
                        ImageJFunctions.show(imglib2_img, "LightPosition");
                        WindowManager.getImage("LightPosition").setRoi(0,0,(int)imglib2_img.dimension(3), (int)imglib2_img.dimension(4));
                        IJ.run("Crop"); //Crops the image or stack based on the current rectangular selection.
                        File jpegExportsFile = new File(projectDirectory+"LightPositionData"+File.separator+"jpeg-exports"+File.separator);
                        if (!light_position_dir.exists()) Files.createDirectory(light_position_dir.toPath());
                        if (!jpegExportsFile.exists()) Files.createDirectory(jpegExportsFile.toPath());
                        //Do we need to tell the users we created these directories?
                        IJ.saveAs("jpeg",projectDirectory+"LightPositionData"+File.separator+"jpeg-exports"+File.separator+simpleImageName+".jpg"); //Use this submenu to save the active image in TIFF, GIF, JPEG, or format
                        WindowManager.getImage("LightPosition").close();
                    }
                }
                //Wasn't sure exactly how to accomplish this with GenericDialog
                IJ.showMessageWithCancel("Use RTI Builder to Create LP File","Please use RTI Builder to create an LP file based on the reflective hemisphere detail images in\n"+projectDirectory+"LightPositionData"+File.separator+"\nPress cancel to discontinue Spectral RTI Toolkit or Ok to continue with other tasks after the lp file has been created.");
            }
            if(acRtiDesired || acRakingDesired){ //Gather accurate color info
                //if(null != accurateColorSources_dirs.listFiles()){
                listOfAccurateColorSources = accurate_color_dir.listFiles();
                //}
//                logService.log().warn("I need to know my list of color sources");
//                logService.log().warn(listOfAccurateColorSources.length);
//                logService.log().warn(listOfAccurateColorSources);
                String[] listOfAccurateColorSources_string = new String[listOfAccurateColorSources.length];
                ArrayList<String>  listOfAccurateColorSources_list = new ArrayList<String>();
                for (File f : listOfAccurateColorSources) {
                   listOfAccurateColorSources_list.add(f.toString());
                }
                listOfAccurateColorSources_list.toArray(listOfAccurateColorSources_string);
                List<String> sourceListToConvert;
//                logService.log().warn("I need to know my list of sources length");
//                logService.log().warn(listOfAccurateColorSources.length);
		if (listOfAccurateColorSources.length == 1) {
                    accurateColorSource = listOfAccurateColorSources[0];
		} 
                else if (listOfAccurateColorSources.length == 0) {
                    IJ.error("Need at least one color image file in "+projectDirectory+"AccurateColor"+File.separator);
                    throw new Throwable("Need at least one color image file in "+projectDirectory+"AccurateColor"+File.separator); 
		} 
                else {
                    for (int i=0; i<listOfAccurateColorSources.length; i++) {
                        if (listOfAccurateColorSources[i].toString().indexOf("sRGB")>0) {
                            accurateColorSource = listOfAccurateColorSources[i];
                        }
                    }
                    if (!accurateColorSource.exists()) {
                        logService.log().info("Could not find accurateColorSource");
                        GenericDialog gd = new GenericDialog("Select Color Source");
                        gd.addMessage("Select Color Source");
                        gd.addRadioButtonGroup("File: ", listOfAccurateColorSources_string, listOfAccurateColorSources.length, 1, listOfAccurateColorSources[0].toString());
                        gd.showDialog();
                        accurateColorSource = new File(gd.getNextRadioButton());
                    }
		}
            }
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
                //ImageJFunctions.show(imglib2_img,"RGBtiff");
		if (imp.getBitDepth() == 8) {
                    //IJ.run(WindowManager.getImage("RGBtiff"),"RGB Color","");
                    IJ.run(imp,"RGB Color","");
                    imp.close();
                    //WindowManager.getImage("RGBtiff").close(); 
                    //imp = WindowManager.getCurrentImage();
                    //imp.setTitle("RGBtiff");
                    imp = WindowManager.getCurrentImage();
                    imp.setTitle("RGBtiff");
                    //WindowManager.getActiveWindow().setName("RGBtiff"); 
		}
                //imp.show();
		IJ.run(imp, "RGB to YCbCr stack", "");
		IJ.run("Stack to Images");
                
                WindowManager.getImage("Y").close();
                //WindowManager.getImage("RGBtiff").close();
                imp.close();
		/**
                 *Luminance from hemisphere captures
                 */
                logService.log().info("Luminance from hem captures! "+listOfHemisphereCaptures.length);
		for(int i=0;i<listOfHemisphereCaptures.length;i++) {
                    if (listOfHemisphereCaptures[i].toString().endsWith("tiff") || listOfHemisphereCaptures[i].toString().endsWith("tif")) { 
                        logService.log().info(i);
                        /**
                         *@see better to trim list at the beginning so that array.length can be used in lp file
                         */
                        imp = opener.openImage( listOfHemisphereCaptures[i].toString() );
                        imglib2_img = ImagePlusAdapter.wrap( imp );
                        imp.setTitle("Luminance");
                        //ImageJFunctions.show(imglib2_img,"Luminance");
                        // it would be better to crop early in the process, especially before reducing to 8-bit and jpeg compression
                        // normalize
                        if (brightnessAdjustApply.equals("RTI images also")) {
                            if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                                //WindowManager.getImage("Luminance").setRoi(normX, normY, normWidth, normHeight);
                                imp.setRoi(normX, normY, normWidth, normHeight);
                                IJ.run(imp, "Enhance Contrast...", "saturated=0.4");//Enhances image contrast by using either histogram stretching or histogram equalization.  Affects entire stack
                                IJ.run("Select None");//Deactivates the selection in the active image.
                            } 
                            else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                                IJ.run(imp,"Multiply...", "value="+normalizationFixedValue+"");
                            }
                        }
                        IJ.run(imp,"8-bit", ""); //Applies the current display range mapping function to the pixel data. If there is a selection, only pixels within the selection are modified
                        imp.show();
                        IJ.run("Concatenate...", "  title=[YCC] keep image1=Luminance image2=Cb image3=Cr image4=[-- None --]"); //Concatenates multiple images or stacks. Images with mismatching type and dimensions are omitted
                        IJ.run("YCbCr stack to RGB"); //Converts a two or three slice stack into an RGB image, assuming that the slices are in R, G, B order. The stack must be 8-bit or 16-bit grayscale
                        //Save as jpeg
                        int extensionIndex = listOfHemisphereCaptures[i].getName().indexOf(".");
                       
                        String simpleName1 = listOfHemisphereCaptures[i].getName().substring(0, extensionIndex); //.toString().substring(0, extensionIndex)
                        String simpleName2 = projectName + "_";
                        String simpleName3 = simpleName1.substring(simpleName1.indexOf("RTI-"));
                        
//                        logService.log().info("1 : "+simpleName1);
//                        logService.log().info("2 : "+simpleName2);
//                        logService.log().info("3 : "+simpleName3);
                        simpleImageName = projectDirectory+"AccurateColorRTI"+File.separator+"AccurateColor_"+simpleName2+simpleName3;
                        logService.log().info(simpleImageName);
                        
                        noClobber(simpleImageName+".jpg");
                        IJ.saveAs("jpeg",simpleImageName+".jpg");
                        WindowManager.getImage("AccurateColor_"+simpleName2+simpleName3+".jpg").close();
                        WindowManager.getImage("YCC").close();
                        imp.changes=false;
                        imp.close();
                        //WindowManager.getImage("Luminance").changes = false;
                        //WindowManager.getImage("Luminance").close();                      
                    }
		}
                WindowManager.getImage("Cb").close();
                WindowManager.getImage("Cr").close();              
                createLpFile("AccurateColor", projectDirectory); 
		runFitter("AccurateColor");
            }
            if (acRakingDesired) {
                imp = opener.openImage( accurateColorSource.toString() ); 
                imglib2_img = ImagePlusAdapter.wrap( imp );
                ImageJFunctions.show(imglib2_img, "RGBtiff");
		if (imp.getBitDepth() == 8) {
                    IJ.run("RGB Color");
		}
		/**
                 * @see create accurate color static diffuse
                */
		noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_00"+".tiff");
		IJ.save(WindowManager.getImage("RGBtiff"), projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_00"+".tiff");
		createJp2(projectName+"_Ac_00", projectDirectory);
                toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_00"+".tiff");
                //Files.deleteIfExists(toDelete.toPath()); //Why do we do this?
		IJ.run("RGB to YCbCr stack");
		IJ.run("Stack to Images"); //Converts the slices in the current stack to separate image windows.
                WindowManager.getImage("Y").close();
                WindowManager.getImage("RGBtiff").setActivated();
		//Luminance from transmissive
                logService.log().info("Transmissive source has value "+transmissiveSource);
		if (!transmissiveSource.equals("")){
                    logService.log().info("Inside Transmissive Source Flag");
                    imp = opener.openImage( transmissiveSource ); 
                    ImageJFunctions.show(imglib2_img,"TransmissiveLuminance");
                    IJ.run("8-bit");
                    IJ.run("Concatenate...", "  title=[YCC] keep image1=TransmissiveLuminance image2=Cb image3=Cr image4=[-- None --]");
                    IJ.run("YCbCr stack to RGB");
                    noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_Tx.tiff");
                    IJ.save(WindowManager.getImage("YCC - RGB"),projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_Tx.tiff");
                    createJp2(projectName+"_Ac_Tx", projectDirectory);
                    toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_Tx.tiff");
                    //Files.deleteIfExists(toDelete.toPath());   
                    WindowManager.getImage("YCC - RGB").close();
                    WindowManager.getImage("YCC").close();
                    WindowManager.getImage("RGBtiff").changes = false;
                    WindowManager.getImage("RGBtiff").close(); //Yikes pay attention to this.  Do we want to close it here?
		}
		//Luminance from hemisphere captures
                logService.log().warn("Entering a hemishpere captures loop");
		for(int i=0;i<listOfHemisphereCaptures.length;i++) {
                    if (listOfHemisphereCaptures[i].toString().endsWith("tiff") || listOfHemisphereCaptures[i].toString().endsWith("tif")){ //@@@ better to trim list at the beginning so that array.length can be used in lp file
                        if (listOfRakingDirections.get(i)) {
                            logService.log().info("listOfRakingDirection is "+listOfRakingDirections.get(i));
                            logService.log().info("Hemisphere caputre is "+listOfHemisphereCaptures[i].toString());
                            imp = opener.openImage( listOfHemisphereCaptures[i].toString() );
                            imglib2_img = ImagePlusAdapter.wrap(imp);
                            ImageJFunctions.show(imglib2_img,"Luminance");
                            if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                                WindowManager.getImage("Luminance").setRoi(normX, normY, normWidth, normHeight);
                                IJ.run("Enhance Contrast...", "saturated=0.4");
//                                run("Select None");
                            } else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                                IJ.run("Multiply...", "value="+normalizationFixedValue+""); //Multiplies the image or selection by the specified real constant. With 8-bit images, results greater than 255 are set to 255
                            }
                            WindowManager.getImage("Luminance").setActivated(); //This image is 16bit and causing problems below
                            IJ.run("8-bit");
                            IJ.run("Concatenate...", "title=[YCC] keep image1=Luminance image2=Cb image3=Cr image4=[-- None --]");
                            IJ.run("YCbCr stack to RGB");
                            positionNumber = IJ.pad(i+1, 2).toString();
                            noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_"+positionNumber+".tiff");
                            logService.log().info("What is the thing we are trying to save here   " + projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_"+positionNumber+".tiff");
                            IJ.save(WindowManager.getImage("YCC - RGB"), projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_"+positionNumber+".tiff");
                            createJp2(projectName+"_Ac_"+positionNumber, projectDirectory);
                            toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_"+positionNumber+".tiff");
                            //Files.deleteIfExists(toDelete.toPath());     
                            WindowManager.getImage("YCC - RGB").close();
                            WindowManager.getImage("YCC").close(); 
                            WindowManager.getImage("Luminance").changes = false;
                            WindowManager.getImage("Luminance").close();
                        }
                    }
		}
                WindowManager.getImage("Cb").close();
                WindowManager.getImage("Cr").close();
                logService.log().info("Made it thru ac raking");
            }
            if (xsRtiDesired || xsRakingDesired) {
		//Red
                logService.log().info("Access red narrow bands "+redNarrowbands.length);
		String redStringList = redNarrowbands[0].toString();  //might be an array to string function somewhere to do this more elegantly
                logService.log().info("What is my first rednarrowband "+redNarrowbands[0].toString());
		for (int i=1;i<redNarrowbands.length;i++) {
                    logService.log().info(redNarrowbands[i].toString());
                    redStringList = redStringList+"|"+redNarrowbands[i].toString();
		}
                logService.log().info("What is red string list");
                logService.log().info(redStringList);
		IJ.run("Image Sequence...", "title=[RedStack] open="+projectDirectory+"Captures-Narrowband-NoGamma"+File.separator+" file=("+redStringList+") sort"); 
                //Opens a series of images in a chosen folder as a stack. Images may have different dimensions and can be of any format supported by ImageJ              
                WindowManager.getImage("Captures-Narrowband-NoGamma").setTitle("RedStack");
                //YIKES
                //What happens if these weren't set yet?  Do I need to get the width of height of the image?
                //It should at least be set to the height or width of the image when grabbed above in the pcaHeight < 100 clause
                if(pcaWidth == 0){
                    
                }
                if(pcaHeight == 0){
                    
                }
                WindowManager.getImage("RedStack").setRoi(pcaX,pcaY,pcaWidth, pcaHeight); 
		IJ.run(WindowManager.getImage("RedStack"),"PCA ", ""); 
                //WindowManager.getImage("PCA of RedStack").setActivated();
		IJ.run( WindowManager.getImage("PCA of RedStack"),"Slice Keeper", "first=1 last=1 increment=1");               
                WindowManager.getImage("Eigenvalue spectrum of RedStack").close();
                WindowManager.getImage("RedStack").changes=false;
                WindowManager.getImage("RedStack").close();
                WindowManager.getImage("PCA of RedStack").close();
                WindowManager.getWindow("PCA of RedStack kept stack").toFront();
                WindowManager.getImage("PCA of RedStack kept stack").setTitle("R");
                WindowManager.getImage("R").setRoi(pcaX,pcaY,pcaWidth,pcaHeight); // questionable
		IJ.run(WindowManager.getImage("R"),"Enhance Contrast...", "saturated=0.4 normalize");
		IJ.run(WindowManager.getImage("R"), "8-bit", "");
		//Green
		String greenStringList = greenNarrowbands[0].toString();  //might be an array to string function somewhere to do this more elegantly
		for (int i=1;i<greenNarrowbands.length;i++) {
                    greenStringList = greenStringList+"|"+greenNarrowbands[i].toString();
		}
		IJ.run("Image Sequence...", "open="+projectDirectory+"Captures-Narrowband-NoGamma"+File.separator+" file=("+greenStringList+") sort");
                WindowManager.getImage("Captures-Narrowband-NoGamma").setTitle("GreenStack");
                WindowManager.getImage("GreenStack").setRoi(pcaX,pcaY,pcaWidth,pcaHeight); 

		IJ.run(WindowManager.getImage("GreenStack"),"PCA ", ""); 
//                WindowManager.getWindow("PCA of GreenStack").toFront();
//                WindowManager.getImage("PCA of GreenStack").setActivated();
		IJ.run(WindowManager.getImage("PCA of GreenStack"),"Slice Keeper", "first=1 last=1 increment=1");
                WindowManager.getImage("Eigenvalue spectrum of GreenStack").close();
                WindowManager.getImage("GreenStack").changes=false;
                WindowManager.getImage("GreenStack").close();
                WindowManager.getImage("PCA of GreenStack").close();
                WindowManager.getWindow("PCA of GreenStack kept stack").toFront();
                WindowManager.getImage("PCA of GreenStack kept stack").setTitle("G");
                WindowManager.getImage("G").setRoi(pcaX,pcaY,pcaWidth,pcaHeight); 
		IJ.run(WindowManager.getImage("G"), "Enhance Contrast...", "saturated=0.4 normalize");
		IJ.run(WindowManager.getImage("G"), "8-bit", "");
		//Blue
		String blueStringList = blueNarrowbands[0].toString();  //might be an array to string function somewhere to do this more elegantly
		for (int i=1;i<blueNarrowbands.length;i++) {
                    blueStringList = blueStringList+"|"+blueNarrowbands[i].toString();
		}
		IJ.run("Image Sequence...", "open="+projectDirectory+"Captures-Narrowband-NoGamma"+File.separator+" file=("+blueStringList+") sort");
                WindowManager.getImage("Captures-Narrowband-NoGamma").setTitle("BlueStack");
                WindowManager.getImage("BlueStack").setRoi(pcaX,pcaY,pcaWidth,pcaHeight); 
		IJ.run(WindowManager.getImage("BlueStack"),"PCA ", ""); 
		IJ.run(WindowManager.getImage("PCA of BlueStack"),"Slice Keeper", "first=1 last=1 increment=1");
                WindowManager.getImage("Eigenvalue spectrum of BlueStack").close();
                WindowManager.getImage("BlueStack").changes = false;
                WindowManager.getImage("BlueStack").close();
                WindowManager.getImage("PCA of BlueStack").close();
                WindowManager.getImage("PCA of BlueStack kept stack").setTitle("B");
                WindowManager.getImage("B").setRoi(pcaX,pcaY,pcaWidth,pcaHeight); 
		IJ.run(WindowManager.getImage("B"), "Enhance Contrast...", "saturated=0.4 normalize");
		IJ.run(WindowManager.getImage("B"), "8-bit", "");
                
		IJ.run("Concatenate...", "  title=[Stack] image1=R image2=G image3=B image4=[-- None --]");
                //RGB stack of black and white
		IJ.run(WindowManager.getImage("Stack"), "Stack to RGB", "");
                //RGB image of color.  Why isn't it color?
                WindowManager.getImage("Stack").close();
                
                
		//create extended spectrum static diffuse
		if (xsRakingDesired){
                    noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_00"+".tiff");
                    IJ.save(WindowManager.getImage("Stack (RGB)"), projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_00"+".tiff");
                    createJp2(projectName+"_Xs_00", projectDirectory); 
                    toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_00"+".tiff");
                    //Files.deleteIfExists(toDelete.toPath());      //Yikes uncomment when done debugging
		}
		IJ.run(WindowManager.getImage("Stack (RGB)"), "RGB to YCbCr stack", "");
		IJ.run("Stack to Images");
                WindowManager.getImage("Y").close();
                WindowManager.getImage("Stack (RGB)").close();
                logService.log().info("What is transmissive source "+transmissiveSource);
		if (!transmissiveSource.equals("")){
                    imp = opener.openImage( transmissiveSource );
                    imglib2_img = ImagePlusAdapter.wrap( imp );
                    ImageJFunctions.show(imglib2_img, "TransmissiveLuminance");
                    IJ.run("8-bit");
                    IJ.run("Concatenate...", "  title=[YCC] keep image1=TransmissiveLuminance image2=Cb image3=Cr image4=[-- None --]");
                    IJ.run("YCbCr stack to RGB");
                    noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_Tx.tiff");
                    IJ.save(WindowManager.getImage("YCC - RGB"), projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_Tx.tiff");
                    createJp2(projectName+"_Xs_Tx", projectDirectory);
                    toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_Tx.tiff");
                   // Files.deleteIfExists(toDelete.toPath()); 
                    WindowManager.getImage("YCC - RGB").close();
                    WindowManager.getImage("YCC").close();
                    WindowManager.getImage("TransmissiveLuminance").changes = false;
                    WindowManager.getImage("TransmissiveLuminance").close();
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
                        ImageJFunctions.show(imglib2_img,"Luminance");
                        if (brightnessAdjustApply.equals("RTI images also")) {
                            if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                                WindowManager.getImage("Luminance").setRoi(pcaX,pcaY,pcaWidth,pcaHeight); 
                                IJ.run(WindowManager.getImage("Luminance"),"Enhance Contrast...", "saturated=0.4");
                                IJ.run("Select None");
                            } else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                                IJ.run(WindowManager.getImage("Luminance"), "Multiply...", "value="+normalizationFixedValue+"");
                            }
                        }
                        IJ.run("8-bit");
                        //IJ.run(WindowManager.getImage("Luminance"), "8-bit", "");
                        IJ.run("Concatenate...", "  title=[YCC] keep image1=Luminance image2=Cb image3=Cr image4=[-- None --]");
                        //Should this be color here?
                        IJ.run("YCbCr stack to RGB");
                        int extensionIndex = listOfHemisphereCaptures[i].getName().indexOf(".");
                        if (extensionIndex != -1)
                        {
                            simpleImageName = listOfHemisphereCaptures[i].getName().substring(0, extensionIndex); //.toString().substring(0, extensionIndex)
                        }
                        
                        noClobber(projectDirectory+"ExtendedSpectrumRTI"+File.separator+simpleImageName+".jpg");
                        IJ.saveAs("jpeg", projectDirectory+"ExtendedSpectrumRTI"+File.separator+simpleImageName+".jpg");
                        WindowManager.getImage("YCC").close();
                        WindowManager.getImage("Luminance").changes = false;
                        WindowManager.getImage("Luminance").close();
                        WindowManager.getImage(simpleImageName+".jpg").close();
                    }
                    if (xsRakingDesired) {
                        if (listOfRakingDirections.get(i)) {
                            imp = opener.openImage( listOfHemisphereCaptures[i].toString() );
                            imglib2_img = ImagePlusAdapter.wrap( imp );
                            ImageJFunctions.show(imglib2_img,"Luminance");
                            if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                                WindowManager.getImage("Luminance").setRoi(normX,normY,normWidth,normHeight); 
                                IJ.run("Enhance Contrast...", "saturated=0.4");
                                IJ.run("Select None");
                            } else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                                IJ.run("Multiply...", "value="+normalizationFixedValue+"");
                            }
                            IJ.run(WindowManager.getImage("Luminance"), "8-bit", "");
                            IJ.run("Concatenate...", "  title=[YCC] keep image1=Luminance image2=Cb image3=Cr image4=[-- None --]");
                            IJ.run("YCbCr stack to RGB");
                            positionNumber = IJ.pad(i+1, 2).toString();
                            noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_"+positionNumber+".tiff");
                            IJ.save(WindowManager.getImage("YCC - RGB"), projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_"+positionNumber+".tiff");
                            createJp2(projectName+"_Xs_"+positionNumber, projectDirectory);
                            toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_"+positionNumber+".tiff");
                            //Files.deleteIfExists(toDelete.toPath()); 
                            WindowManager.getImage("YCC").close();
                            WindowManager.getImage("YCC - RGB").close();
                            WindowManager.getImage("Luminance").changes = false;
                            WindowManager.getImage("Luminance").close();
                            //IJ.selectWindow("Luminance");
                            //IJ.run("Close");
                        }
                    }
		}
                WindowManager.getImage("Cb").close();
                WindowManager.getImage("Cr").close();
		IJ.run("Collect Garbage");
		if (xsRtiDesired) {
                    createLpFile("ExtendedSpectrum", projectDirectory);
                    runFitter("ExtendedSpectrum");
		}
            }
            if (psRtiDesired || psRakingDesired) {
                File fluorescenceNoGamma = new File(projectDirectory+"Captures-Fluorescence-NoGamma"+File.separator);
		//option to create new ones based on narrowband captures and assumption that pc1 and pc2 are best
		if (pcaMethod.equals("Generate and select using defaults")) {
                    IJ.run("Image Sequence...", "open="+projectDirectory+"Captures-Narrowband-NoGamma"+File.separator+" sort");
                    if (fluorescenceNoGamma.exists()) {
                            IJ.run("Image Sequence...", "open="+projectDirectory+"Captures-Fluorescence-NoGamma"+File.separator+" sort");
                            IJ.run("Concatenate...", "  title=Captures-Narrowband-NoGamma image1=Captures-Narrowband-NoGamma image2=Captures-Fluorescence-NoGamma image3=[-- None --]");
                    } 
                    else {
                        //WindowManager.getImage("Captures-Narrowband-NoGamma").setTitle("Captures-Fluorescence-NoGamma");
                            //rename("Captures-Narrowband-NoGamma");
                    }
                    WindowManager.getImage("Captures-Narrowband-NoGamma").setRoi(pcaX,pcaY,pcaWidth,pcaHeight); 
                    IJ.run("PCA ");
                    //WindowManager.getWindow("PCA of Captures-Narrowband-NoGamma").toFront();
                    IJ.run(WindowManager.getImage("PCA of Captures-Narrowband-NoGamma"),"Slice Keeper", "first=2 last=3 increment=1");
                    WindowManager.getImage("Eigenvalue spectrum of Captures-Narrowband-NoGamma").close();
                    WindowManager.getImage("Captures-Narrowband-NoGamma").close();
                    WindowManager.getImage("PCA of Captures-Narrowband-NoGamma").close();
                    WindowManager.getImage("PCA of Captures-Narrowband-NoGamma kept stack").setActivated();
                    WindowManager.getImage("PCA of Captures-Narrowband-NoGamma kept stack").setRoi(pcaX,pcaY,pcaWidth,pcaHeight); 
                    IJ.run(WindowManager.getImage("PCA of Captures-Narrowband-NoGamma kept stack"), "Enhance Contrast...", "saturated=0.3 normalize update process_all");
                    IJ.run(WindowManager.getImage("PCA of Captures-Narrowband-NoGamma kept stack"), "8-bit", "");
		//option to create new ones and manually select (close all but two)
		} 
                else if (pcaMethod.equals("Generate and manually select two")) {
                    IJ.run("Image Sequence...", "open="+projectDirectory+"Captures-Narrowband-NoGamma"+File.separator+" sort");
                    if (fluorescenceNoGamma.exists()) {
                        IJ.run("Image Sequence...", "open="+projectDirectory+"Captures-Fluorescence-NoGamma"+File.separator+" sort");
                        IJ.run("Concatenate...", "  title=Captures-Narrowband-NoGamma image1=Captures-Narrowband-NoGamma image2=Captures-Fluorescence-NoGamma image3=[-- None --]");
                    } else {
                        //WindowManager.getActiveWindow().setName("Captures-Narrowband-NoGamma");
                        //rename("Captures-Narrowband-NoGamma");
                    }
                    IJ.run("PCA ");
                    WindowManager.getImage("PCA of Captures-Narrowband-NoGamma kept stack").setRoi(pcaX,pcaY,pcaWidth,pcaHeight); 
                    WindowManager.getImage("Eigenvalue spectrum of Captures-Narrowband-NoGamma").close();
                    WindowManager.getImage("Captures-Narrowband-NoGamma").close();
                    WindowManager.getImage("PCA of Captures-Narrowband-NoGamma").setActivated();
                    WindowManager.getImage("PCA of Captures-Narrowband-NoGamma").setRoi(pcaX,pcaY,pcaWidth,pcaHeight); 
                    dWait = new WaitForUserDialog("Select area", "Delete slices from the stack until two remain\n(Hint: Image > Stacks > Delete Slice)\nEnhance contrast as desired\nThen press Ok");
                    dWait.show();
                    WindowManager.getActiveWindow().setName("PCA of Captures-Narrowband-NoGamma kept stack");
                    IJ.run(WindowManager.getImage("PCA of Captures-Narrowband-NoGamma kept stack"), "8-bit", "");
                    //IJ.run("PCA of Captures-Narrowband-NoGamma","8-bit");
		/**
                 * @see option to use previously generated principal component images
                 */
		} 
                else if (pcaMethod.equals("Open pregenerated images")) {
                    dWait = new WaitForUserDialog("Select area", "Open a pair of images or stack of two slices.\nEnhance contrast as desired\nThen press Ok");
                    if (WindowManager.getImageCount() > 1){ 
                        IJ.run("Images to Stack", "name=Stack title=[] use"); 
                    }
                    //setBatchMode(true); 
                    //setBatchMode("hide");
                    IJ.run(WindowManager.getImage("PCA of Captures-Narrowband-NoGamma kept stack"), "8-bit", "");
		}
		/**
                    * @see integrate pca pseudocolor with rti luminance
                    * @see create static diffuse (not trivial... use median of all)
                */
		if (psRakingDesired){
                    IJ.run("Image Sequence...", "open="+projectDirectory+"Captures-Hemisphere-Gamma"+File.separator);
                    IJ.run("Z Project...", "projection=Median");
                    WindowManager.getActiveWindow().setName("Luminance");
                    WindowManager.getImage("Captures-Hemisphere-Gamma").close();    
                    WindowManager.getImage("Luminance").setActivated();
                    if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                        region = new RectangleOverlay();
                        WindowManager.getImage("Luminance").setRoi(normX,normY,normWidth,normHeight); 
                        IJ.run("Enhance Contrast...", "saturated=0.4");
                        IJ.run("Select None");
                    } else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                        IJ.run("Multiply...", "value="+normalizationFixedValue+"");
                    }
                    IJ.run("Luminance", "8-bit"); //on Luminance
                    IJ.run("Concatenate...", "  title=[YCC] keep image1=Luminance image2=[PCA of Captures-Narrowband-NoGamma kept stack] image3=[-- None --]");
                    IJ.run("YCbCr stack to RGB");
                    noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_00.tiff");
                    IJ.save(WindowManager.getImage("YCC - RGB"), projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_00.tiff");
                    createJp2(projectName+"_Ps_00", projectDirectory);
                    toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_00.tiff");
                    //Files.deleteIfExists(toDelete.toPath());
                    WindowManager.getImage("YCC - RGB").close();
                    WindowManager.getImage("YCC").close();
                    WindowManager.getImage("Luminance").changes = false;
                    WindowManager.getImage("Luminance").close();
                    
                    if (!transmissiveSource.equals("")) {
                        imp = opener.openImage( transmissiveSource );
                        imglib2_img = ImagePlusAdapter.wrap( imp );
                        ImageJFunctions.show(imglib2_img,"TransmissiveLuminance");
                        IJ.run("8-bit");
                        IJ.run("Concatenate...", "  title=[YCC] keep image1=TransmissiveLuminance image2=[PCA of Captures-Narrowband-NoGamma kept stack] image3=[-- None --]");
                        IJ.run("YCbCr stack to RGB");
                        noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_Tx.tiff");
                        IJ.save(WindowManager.getImage("YCC - RGB"), projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_Tx.tiff");
                        createJp2(projectName+"_Ps_Tx", projectDirectory);
                        toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_Tx.tiff");
                        //Files.deleteIfExists(toDelete.toPath());
                        WindowManager.getImage("YCC - RGB").close();
                        WindowManager.getImage("YCC").close();
                        WindowManager.getImage("TransmissiveLuminance").changes = false;
                        WindowManager.getImage("TransmissiveLuminance").close();
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
                    if (psRtiDesired||listOfRakingDirections.size() >= i+1) {
                        imp = opener.openImage( listOfHemisphereCaptures[i].toString() );
                        imglib2_img = ImagePlusAdapter.wrap( imp );
                        ImageJFunctions.show(imglib2_img,"Luminance");
                        int extensionIndex = -1;
                        IJ.run("Duplicate...", "title=EnhancedLuminance");
                        WindowManager.getImage("EnhancedLuminance").setActivated();
                        if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                            WindowManager.getImage("EnhancedLuminance").setRoi(normX,normY,normWidth,normHeight); 
                            IJ.run("Enhance Contrast...", "saturated=0.4");
                            IJ.run("Select None");
                        } else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                            IJ.run("Multiply...", "value="+normalizationFixedValue+"");
                        }
                        IJ.run(WindowManager.getImage("Luminance"), "8-bit", "");
                        IJ.run(WindowManager.getImage("EnhancedLuminance"), "8-bit", "");
                        //IJ.run("8-bit");
                        if (listOfRakingDirections.size() >= i+1) {
                            IJ.run("Concatenate...", "  title=[YCC] keep image1=EnhancedLuminance image2=[PCA of Captures-Narrowband-NoGamma kept stack] image3=[-- None --]");
                            IJ.run("YCbCr stack to RGB");
                            WindowManager.getImage("YCC").close();
                            positionNumber = IJ.pad(i+1, 2).toString();
                            noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_"+positionNumber+".tiff");
                            IJ.save(WindowManager.getImage("YCC - RGB"), projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_"+positionNumber+".tiff");
                            WindowManager.getImage("YCC - RGB").close();
                            createJp2(projectName+"_Ps_"+positionNumber, projectDirectory);
                            toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_"+positionNumber+".tiff");
                            //Files.deleteIfExists(toDelete.toPath());
                        }
                        if ((psRtiDesired)&&(brightnessAdjustApply.equals("RTI images also"))){ 
                            IJ.run("Concatenate...", "  title=[YCC] keep image1=EnhancedLuminance image2=[PCA of Captures-Narrowband-NoGamma kept stack] image3=[-- None --]");
                            IJ.run("YCbCr stack to RGB");
                            WindowManager.getImage("YCC").close();
                            extensionIndex = listOfHemisphereCaptures[i].getName().indexOf(".");
                            if (extensionIndex != -1)
                            {
                                simpleImageName = listOfHemisphereCaptures[i].getName().substring(0, extensionIndex); //.toString().substring(0, extensionIndex)
                            }
                            noClobber(projectDirectory+"PseudocolorRTI"+File.separator+"Pseudocolor_"+simpleImageName+".jpg");
                            IJ.saveAs("jpeg", projectDirectory+"PseudocolorRTI"+File.separator+"Pseudocolor_"+simpleImageName+".jpg");
                            WindowManager.getImage(simpleImageName+".jpg").close();
                        } 
                        else if (psRtiDesired) {
                            logService.log().info("Gotta concat a "+WindowManager.getImage("PCA of Captures-Narrowband-NoGamma kept stack").getBitDepth()+" with a "+WindowManager.getImage("Luminance").getBitDepth());
                            IJ.run("Concatenate...", "  title=[YCC] keep image1=Luminance image2=[PCA of Captures-Narrowband-NoGamma kept stack] image3=[-- None --]");
                            IJ.run("YCbCr stack to RGB");
                            WindowManager.getImage("YCC").close();
                            extensionIndex = listOfHemisphereCaptures[i].getName().indexOf(".");
                            if (extensionIndex != -1)
                            {
                                simpleImageName = listOfHemisphereCaptures[i].getName().substring(0, extensionIndex); //.toString().substring(0, extensionIndex)
                            }
                            noClobber(projectDirectory+"PseudocolorRTI"+File.separator+"Pseudocolor_"+simpleImageName+".jpg");
                            IJ.saveAs("jpeg", projectDirectory+"PseudocolorRTI"+File.separator+"Pseudocolor_"+simpleImageName+".jpg");
                            WindowManager.getImage("Pseudocolor_"+simpleImageName+".jpg").close();
                        }
                        WindowManager.getImage("EnhancedLuminance").changes = false;
                        WindowManager.getImage("Luminance").changes = false;
                        WindowManager.getImage("EnhancedLuminance").close();
                        WindowManager.getImage("Luminance").close();
                    }
		}
                WindowManager.getImage("PCA of Captures-Narrowband-NoGamma kept stack").close();
                System.gc();
		//IJ.run("Collect Garbage");
		if (psRtiDesired) {
                    createLpFile("Pseudocolor", projectDirectory);
                    runFitter("Pseudocolor");
		}
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
                ImageJFunctions.show(imglib2_img,"csSource");
		if ((imp.getImageStackSize() == 1)&&(imp.getBitDepth()<24)) {
                    if (csRakingDesired) {
                        noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_00"+".tiff");
                        IJ.save(imp, projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_00"+".tiff");
                        createJp2(projectName+"_"+csProcessName+"_00", projectDirectory);
                        toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_00"+".tiff");
                        //Files.deleteIfExists(toDelete.toPath());
                    }
                    IJ.run("8-bit");
                    IJ.run("Duplicate...", "title=Cb");
                    IJ.run("Duplicate...", "title=Cr");
		} 
                else if (imp.getImageStackSize() == 2) {
                    IJ.run("8-bit");
                    IJ.run("Stack to Images");
                    WindowManager.getImage(1).setTitle("Cb");
                    WindowManager.getImage(2).setTitle("Cr");
                    
		} 
                else if ((imp.getImageStackSize() > 2)||(imp.getBitDepth()==24)){
                    if (imp.getImageStackSize() > 3) {
                        IJ.run("Slice Keeper", "first=1 last=3 increment=1");
                        logService.log().info("Only the first three slices in the stack can be used at this time.");
                    }
                    if (imp.getBitDepth() == 8) {
                        IJ.run("RGB Color");
                    }
                    //create a 00 static diffuse
                    if (csRakingDesired) {
                        noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_00"+".tiff");
                        IJ.save(imp, projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_00"+".tiff");
                        createJp2(projectName+"_"+csProcessName+"_00", projectDirectory);
                        toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_00"+".tiff");
                        //Files.deleteIfExists(toDelete.toPath());
                    }
                    IJ.run("RGB to YCbCr stack");
                    IJ.run("8-bit");
                    IJ.run("Stack to Images");
                    WindowManager.getImage("Y").close();
		}
                WindowManager.getImage("csSource").close();
		if (!transmissiveSource.equals("")) {
                    imp = opener.openImage( transmissiveSource );
                    imglib2_img = ImagePlusAdapter.wrap( imp );
                    ImageJFunctions.show(imglib2_img,"TransmissiveLuminance");
                    IJ.run("8-bit");
                    IJ.run("Concatenate...", "  title=[YCC] keep image1=TransmissiveLuminance image2=Cb image3=Cr image4=[-- None --]");
                    IJ.run("YCbCr stack to RGB");
                    noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_Tx.tiff");
                    IJ.save(WindowManager.getImage("YCC - RGB"), projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_Tx.tiff");
                    createJp2(projectName+"_"+csProcessName+"_Tx", projectDirectory);
                    toDelete = new File(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_Tx.tiff");
                    //Files.deleteIfExists(toDelete.toPath());
                    WindowManager.getImage("YCC - RGB").close();
                    WindowManager.getImage("YCC").close();
                    WindowManager.getImage("TransmissiveLuminance").changes = false;
                    WindowManager.getImage("TransmissiveLuminance").close();
		}
		for(int i=0;i<listOfHemisphereCaptures.length;i++) {
                    if (listOfHemisphereCaptures[i].toString().endsWith("tiff")|| listOfHemisphereCaptures[i].toString().endsWith("tif")) {
                        if ((csRtiDesired)||(listOfRakingDirections.get(i))) {
                            imp = opener.openImage( listOfHemisphereCaptures[i].toString() );
                            imglib2_img = ImagePlusAdapter.wrap( imp );
                            ImageJFunctions.show(imglib2_img,"Luminance");
                            int extensionIndex = listOfHemisphereCaptures[i].getName().indexOf(".");
                            if (extensionIndex != -1)
                            {
                                simpleImageName = listOfHemisphereCaptures[i].getName().substring(0, extensionIndex); //.toString().substring(0, extensionIndex)
                            }
                            IJ.run("Duplicate...", "title=EnhancedLuminance");
                            if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                                region = new RectangleOverlay();
                                WindowManager.getImage("Luminance").setRoi(normX,normY,normWidth,normHeight); 
                                IJ.run("Enhance Contrast...", "saturated=0.4");
                                IJ.run("Select None");
                            } else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                                IJ.run("Multiply...", "value="+normalizationFixedValue+"");
                            }
                            //IJ.selectWindow("Luminance");
                            IJ.run(WindowManager.getImage("Luminance"), "8-bit", "");
                            WindowManager.getWindow("EnhancedLuminance").toFront();
                            //IJ.selectWindow("EnhancedLuminance");
                            IJ.run("8-bit");
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
                                //Files.deleteIfExists(toDelete.toPath());
                            }
                            if ((csRtiDesired)&&(brightnessAdjustApply.equals("RTI images also"))){
                                IJ.run("Concatenate...", "  title=[YCC] keep image1=EnhancedLuminance image2=Cb image3=Cr image4=[-- None --]");
                                IJ.run("YCbCr stack to RGB");
                                WindowManager.getImage("YCC").close();
                                noClobber(projectDirectory+csProcessName+"RTI"+File.separator+csProcessName+"_"+simpleImageName+".jpg");
                                IJ.saveAs("jpeg", projectDirectory+csProcessName+"RTI"+File.separator+csProcessName+"_"+simpleImageName+".jpg");
                                WindowManager.getImage(simpleImageName+".jpg").close();
                            } 
                            else if (csRtiDesired) {
                                IJ.run("Concatenate...", "  title=[YCC] keep image1=Luminance image2=Cb image3=Cr image4=[-- None --]");
                                IJ.run("YCbCr stack to RGB");
                                WindowManager.getImage("YCC").close();
                                noClobber(projectDirectory+csProcessName+"RTI"+File.separator+csProcessName+"_"+simpleImageName+".jpg");
                                IJ.saveAs("jpeg", projectDirectory+csProcessName+"RTI"+File.separator+csProcessName+"_"+simpleImageName+".jpg");
                                //IJ.selectWindow(File.nameWithoutExtension+".jpg");
                                //IJ.run("Close");
                            }
                            WindowManager.getImage("EnhancedLuminance").changes = false;
                            WindowManager.getImage("Luminance").changes = false;
                            WindowManager.getImage("EnhancedLuminance").close();
                            WindowManager.getImage("Luminance").close();
                        }
                    }
		}
                WindowManager.getImage("Cb").close();
                WindowManager.getImage("Cr").close();
		createLpFile(csProcessName, projectDirectory);
		runFitter(csProcessName);
            }
            IJ.beep();
            WindowManager.closeAllWindows();
            GenericDialog end = new GenericDialog("Processing Complete");
            end.addMessage("Processing Complete at "+timestamp());
            end.showDialog();
            logService.log().warn("END OF TESTED MACRO PIECE");
        }
        	
        //The whole macro will run from here.  However, we should use this to test it a piece at a time. 
        @Override
	public void run() {
            try {
                theMacro_tested();
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
        * Create a JP2000 image?
        * @param inFile The directory to check
        * @param projDir The project directory
        * @return listOfFiles A list of files from the given directory
        * @throws java.io.IOException
        */
        public String createJp2(String inFile, String projDir) throws IOException {
            logService.log().info("We are in create jp2");
            logService.log().warn(inFile +" , "+projDir);
            String preferredCompress = theList.get("preferredCompress");
            String preferredJp2Args = theList.get("preferredJp2Args");
            preferredCompress = preferredCompress.replace("/", File.separator);
            preferredJp2Args = preferredJp2Args.replace("/", File.separator);
            String compressString = "preferredCompress="+preferredCompress+System.lineSeparator();
            String preferredString = "preferredJp2Args="+preferredJp2Args+System.lineSeparator();
            OpenDialog dialog;  //For files
            String directory = "";
            String returnString = "/created/JP2file";
            logService.log().info("Checking prefs and vars for processing");
            if (preferredCompress.equals("")){
                dialog = new OpenDialog("Locate kdu_compress or ojp_compress"); 
                preferredCompress = dialog.getPath();
                preferredCompress = preferredCompress.replace("\\", "/");
                prefsFileAsText = new String(Files.readAllBytes(spectralPrefsFile.toPath()), "UTF8");
                compressString = "preferredCompress="+preferredCompress+System.lineSeparator();
                prefsFileAsText = prefsFileAsText.replaceFirst("preferredCompress=.*\\\n", compressString); //replace the prefs var
                theList.put("preferredCompress", preferredCompress);
                Files.write(spectralPrefsFile.toPath(), prefsFileAsText.getBytes()); //rewrite the prefs file
            }
            if (preferredJp2Args.equals("")){
                GenericDialog gd = new GenericDialog("Approve arguments for Jpeg 2000 compression");
                //Yikes -,2.4, ... is that -, supposed to be
                String arguments = "-rate -,2.4,1.48331273,.91673033,.56657224,.35016049,.21641118,.13374944,.08266171 Creversible\\=no Clevels\\=5 Stiles\\=\\{1024,1024\\} Cblk\\=\\{64,64\\} Cuse_sop\\=yes Cuse_eph\\=yes Corder\\=RPCL ORGgen_plt\\=yes ORGtparts\\=R Cmodes\\=BYPASS -double_buffering 10 -num_threads 4 -no_weights";
                gd.addStringField("Arguments:",arguments,80);
                gd.showDialog();
                preferredJp2Args = gd.getNextString();
                preferredJp2Args =preferredJp2Args.replace("\\", "/");
                preferredString = "preferredJp2Args="+preferredJp2Args;
                theList.put("preferredJp2Args", preferredJp2Args);
                prefsFileAsText = new String(Files.readAllBytes(spectralPrefsFile.toPath()), "UTF8");
                prefsFileAsText = prefsFileAsText.replaceFirst("preferredJp2Args=.*\\\n", preferredString); //replace the prefs var
                Files.write(spectralPrefsFile.toPath(), prefsFileAsText.getBytes()); //rewrite the prefs file
            }
            logService.log().info("Heading off to noClobber from createJP2");
            Boolean noClob = noClobber(projDir+"StaticRaking"+File.separator+inFile+".jp2"); 
            //Boolean noClob = noClobber(projDir+inFile+".txt"); //DEBUGGING
            logService.log().warn("noClobber in createJP2 returned "+noClob);
            logService.log().info("Executing command in createJP2: "+preferredCompress+" -i "+projDir+"StaticRaking"+File.separator+inFile+".tiff -o " +projDir+"StaticRaking"+File.separator+inFile+".jp2 "+preferredJp2Args+"\n");
           
             //Pay attention to preferredJP2Args, it assumes !isWindows
            if(isWindows){
                logService.log().warn("Windows native command");
                //preferred compress is kdu_compress.exe (or some other executable).  The args used are from those.  It should be platform independent
                CommandLine cmdLine = new CommandLine("cmd.exe");
                cmdLine.addArgument(preferredCompress);
                cmdLine.addArgument("-i");
                cmdLine.addArgument(projDir+"StaticRaking"+File.separator+inFile+".tiff");
                cmdLine.addArgument("-o");
                cmdLine.addArgument(projDir+"StaticRaking"+File.separator+inFile+".jp2");
                cmdLine.addArgument(preferredJp2Args);
                DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
                ExecuteWatchdog watchdog = new ExecuteWatchdog(60*1000);
                Executor executor = new DefaultExecutor();
                executor.setWatchdog(watchdog);
                logService.log().info("Running the following command");
                    logService.log().info(cmdLine.toString());
                executor.execute(cmdLine, resultHandler);
                try {
                    // some time later the result handler callback was invoked so we
                    // can safely request the exit value
                    resultHandler.waitFor();
                    logService.log().info("Executed command 5");
                    logService.log().info(resultHandler.getExitValue());
                    returnString = ""+resultHandler.getExitValue();
                } catch (InterruptedException ex) {
                    Logger.getLogger(SpectralRTI_Toolkit.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else{
                CommandLine cmdLine = new CommandLine("sh");
                cmdLine.addArgument(preferredCompress);
                cmdLine.addArgument("-i");
                cmdLine.addArgument(projDir+"StaticRaking"+File.separator+inFile+".tiff");
                cmdLine.addArgument("-o");
                cmdLine.addArgument(projDir+"StaticRaking"+File.separator+inFile+".jp2 ");
                cmdLine.addArgument(preferredJp2Args);
                DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
                ExecuteWatchdog watchdog = new ExecuteWatchdog(60*1000);
                Executor executor = new DefaultExecutor();
                executor.setWatchdog(watchdog);
                executor.execute(cmdLine, resultHandler);
                try {
                    // some time later the result handler callback was invoked so we
                    // can safely request the exit value
                    resultHandler.waitFor();
                    logService.log().info("Executed command 5");
                    returnString = ""+resultHandler.getExitValue();
                } catch (InterruptedException ex) {
                    Logger.getLogger(SpectralRTI_Toolkit.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            logService.log().info("Complete createJP2");
            return returnString;
        }
        
        /** 
        * Used to make sure not to overwrite a file that already exists (Todd said).  
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
            logService.log().info("No clobber safe name");
            logService.log().warn(newFileName);
            if (oldFile.exists()) {
                logService.log().info("Found a matcing filename.  Renaming.");
                verboseDate = Files.getLastModifiedTime(safeNamePath).toString();
                verboseDate = verboseDate.replace(" ","_");
                verboseDate = verboseDate.replace(":","-");
                logService.log().info("verbosedate is "+verboseDate);
                newFileName = newFileName.replace(".","("+verboseDate+").");
                logService.log().info("safeName after rename");
                logService.log().warn(newFileName);
                File newFileFileName = new File(newFileName);
                success = oldFile.renameTo(newFileFileName);
            }
            else{ //Old file did not exist, so no need to rename and we can return success.
                logService.log().error("Could not perform no clobber.  "+safeName+" does not exist.  Assuming success.");
                success = true;
                //DO I CREATE THE FILE HERE?  NOOOO
                //Files.createFile(oldFile.toPath());
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
        public void promptBrightnessAdjust(File[] listOfHemisphereCaptures) throws ImgIOException {
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
            ImageJFunctions.show(imglib2_img, "Preview");
            
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
            gd.showDialog();
            
            brightnessAdjustOption = gd.getNextRadioButton();
            brightnessAdjustApply = gd.getNextRadioButton();
            
//            logService.log().info("I have picked the brightness adjustment.  Here are the vars!");
//            logService.log().info(brightnessAdjustOption);
//            logService.log().info(brightnessAdjustApply);
            
            if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                //gd.setVisible(false);
                //logService.log().warn("Bright Case 1");
                dWait = new WaitForUserDialog("Select Area","Draw a rectangle containing the brighest white and darkest black desired then press OK\n(hint: use a large area including spectralon and the object, excluding glare)" );
                dWait.show();
                bounds = WindowManager.getImage("Preview").getRoi().getBounds();
                region = new RectangleOverlay();
                normX = bounds.x;
                normY = bounds.y;
                normHeight = bounds.height;
                normWidth = bounds.width;

            } 
            else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
               // logService.log().warn("Bright Case 2");
                dWait = new WaitForUserDialog("Use the Muliply dialog to preview and choose a multiplier value.\nThis is just a preview image; the chosen value will be entered next." );
                dWait.show();
                IJ.run("Multiply...");
                GenericDialog gdMultiplier = new GenericDialog("Enter selected multiplier");
                gdMultiplier.addNumericField("Enter selected multiplier: ", 1.30,2,4,"");
                gdMultiplier.showDialog();
                //logService.log().info("Should have a multiplier below...");
                normalizationFixedValue = (int) gdMultiplier.getNumericFields().get(0);
                //logService.log().warn(normalizationFixedValue);
            }
            else{
                logService.log().warn("Bright NOCASE");
            }
            WindowManager.getImage("Preview").close();
        } 
        
        /** 
         * identify preferred fitter and exec with arguments
         * @param colorProcess
         * @exception IOException if file is not found.  
        */
        public void runFitter(String colorProcess) throws IOException, Throwable {
            String preferredFitter = theList.get("preferredFitter");
            preferredFitter = preferredFitter.replace("/", File.separator);
            String fitterOutput = "";
            String webRtiMakerOutput = "";
            String webRtiMaker = "";
            String appendString = "preferredFitter="+preferredFitter+System.lineSeparator();
            if (preferredFitter.equals("")) {
                OpenDialog dialog = new OpenDialog("Locate Preferred RTI Fitter or cmd file for batch processing");
                preferredFitter = dialog.getPath();
                preferredFitter =preferredFitter.replace("\\", "/");
                appendString = "preferredFitter="+preferredFitter+System.lineSeparator();
                prefsFileAsText = prefsFileAsText.replaceFirst("preferredFitter=.*\\"+System.getProperty("line.separator"), appendString); //replace the prefs var
                Files.write(spectralPrefsFile.toPath(), prefsFileAsText.getBytes()); //rewrite the prefs file
            }
            if (preferredFitter.endsWith("hshfitter.exe")) { // use HSH fitter
                int hshOrder = Integer.parseInt(theList.get("hshOrder"));
                if (hshOrder < 2 ) hshOrder = 3;
                int hshThreads = Integer.parseInt(theList.get("hshThreads"));
                if (hshThreads < 1 ) hshThreads = 16;
                appendString += "Brightness Adjust Option: "+brightnessAdjustOption+System.lineSeparator();
                File fitterFile = new File(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt");
                if(!fitterFile.exists()){
                    Files.createFile(fitterFile.toPath());
                }
                //logService.log().info("Executing command "+preferredFitter+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti\nThis could take a while...");
                Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt"), (appendString+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                    appendString += "Normalization area bounds: "+normX+", "+normY+", "+normWidth+", "+normHeight+System.lineSeparator();
                    Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt"), (appendString+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                } 
                else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                    appendString += "Normalization fixed value: "+normalizationFixedValue+System.lineSeparator();
                    Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt"), (appendString+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                }
                if (pcaX > 0) {
                    appendString += "PCA area bounds: "+pcaX+", "+pcaY+", "+pcaWidth+", "+pcaHeight+System.lineSeparator();
                    Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt"), (appendString+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                }
                appendString += "Jpeg Quality: "+jpegQuality+" (edit SpectralRTI_Toolkit-prefs.txt to change)"+System.lineSeparator();
                Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt"), appendString.getBytes(), StandardOpenOption.APPEND);
                appendString += "Executing command "+preferredFitter+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti"+System.lineSeparator();
                Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt"), appendString.getBytes(), StandardOpenOption.APPEND);
                if(isWindows){
                    //preferredfitter is hshFitter.exe (or some other executable).  The args used are from those.  It should be platform independent
                    logService.log().warn("Windows native commands");
                    CommandLine cmdLine = new CommandLine("cmd.exe");
                    cmdLine.addArgument(preferredFitter);
                    cmdLine.addArgument(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp");
                    cmdLine.addArgument(""+hshOrder);
                    cmdLine.addArgument(""+hshThreads);
                    cmdLine.addArgument(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti");
                    logService.log().info("Running the following command");
                    logService.log().info(cmdLine.toString());
                    DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
                    ExecuteWatchdog watchdog = new ExecuteWatchdog(60*1000);
                    Executor executor = new DefaultExecutor();
                    //executor.setExitValue(1);
                    executor.setWatchdog(watchdog);
                    executor.execute(cmdLine, resultHandler);
                    try {
                        // some time later the result handler callback was invoked so we
                        // can safely request the exit value
                        resultHandler.waitFor();
                        logService.log().info("Executed command 6");
                        fitterOutput = ""+resultHandler.getExitValue();
                        logService.log().info("Fiter output   "+fitterOutput);
                    Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt"), fitterOutput.getBytes(), StandardOpenOption.APPEND);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(SpectralRTI_Toolkit.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                else{
                    CommandLine cmdLine = new CommandLine("sh");
                    cmdLine.addArgument(preferredFitter);
                    cmdLine.addArgument(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp ");
                    cmdLine.addArgument(theList.get("hshOrder"));
                    cmdLine.addArgument(theList.get("hshThreads"));
                    cmdLine.addArgument(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti");
                    DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
                    ExecuteWatchdog watchdog = new ExecuteWatchdog(60*1000);
                    Executor executor = new DefaultExecutor();
                    //executor.setExitValue(1);
                    executor.setWatchdog(watchdog);
                    executor.execute(cmdLine, resultHandler);
                    try {
                        // some time later the result handler callback was invoked so we
                        // can safely request the exit value
                        resultHandler.waitFor();
                        logService.log().info("Executed command 6");
                        fitterOutput = ""+resultHandler.getExitValue();
                        logService.log().info("Fiter output   "+fitterOutput);
                        Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt"), fitterOutput.getBytes(), StandardOpenOption.APPEND);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(SpectralRTI_Toolkit.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                if (webRtiDesired) {
                    String webString;
                    webRtiMaker = theList.get("webRtiMaker");
                    webRtiMaker = webRtiMaker.replace("/", File.separator);
                    if (webRtiMaker.equals("")) {
                        OpenDialog dialog2 = new OpenDialog("Locate webGLRTIMaker.exe");
                        webRtiMaker = dialog2.getPath();
                        webRtiMaker =webRtiMaker.replace("\\", "/");
                        webString = "webRtiMaker="+webRtiMaker;
                        prefsFileAsText = prefsFileAsText.replaceFirst("webRtiMaker=.*\\"+System.getProperty("line.separator"),webString+System.getProperty("line.separator")); //replace the prefs var
                        Files.write(spectralPrefsFile.toPath(), prefsFileAsText.getBytes()); //rewrite the prefs file
                    }
                    
                    if(isWindows){
                        //preferredfitter is hshFitter.exe (or some other executable).  The args used are from those.  It should be platform independent
                        logService.log().warn("Windows native commands");   
                        CommandLine cmdLine2 = new CommandLine("cmd.exe");
                        cmdLine2.addArgument(preferredFitter);
                        cmdLine2.addArgument(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp");
                        cmdLine2.addArgument(theList.get("hshOrder"));
                        cmdLine2.addArgument(theList.get("hshThreads"));
                        cmdLine2.addArgument(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti");
                        DefaultExecuteResultHandler resultHandler2 = new DefaultExecuteResultHandler();
                        ExecuteWatchdog watchdog2 = new ExecuteWatchdog(60*1000);
                        Executor executor2 = new DefaultExecutor();
                        executor2.setWatchdog(watchdog2);
                        logService.log().info("Running the following command");
                    logService.log().info(cmdLine2.toString());
                        executor2.execute(cmdLine2, resultHandler2);
                        try {
                            // some time later the result handler callback was invoked so we
                            // can safely request the exit value
                            resultHandler2.waitFor();
                            logService.log().info("Executed command 7");
                            webRtiMakerOutput = ""+resultHandler2.getExitValue();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(SpectralRTI_Toolkit.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    else{
                        CommandLine cmdLine2 = new CommandLine("sh");
                        cmdLine2.addArgument(preferredFitter);
                        cmdLine2.addArgument(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp");
                        cmdLine2.addArgument(theList.get("hshOrder"));
                        cmdLine2.addArgument(theList.get("hshThreads"));
                        cmdLine2.addArgument(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti");
                        DefaultExecuteResultHandler resultHandler2 = new DefaultExecuteResultHandler();
                        ExecuteWatchdog watchdog2 = new ExecuteWatchdog(60*1000);
                        Executor executor2 = new DefaultExecutor();
                        executor2.setWatchdog(watchdog2);
                        executor2.execute(cmdLine2, resultHandler2);
                        try {
                            // some time later the result handler callback was invoked so we
                            // can safely request the exit value
                            resultHandler2.waitFor();
                            logService.log().info("Executed command 7");
                            webRtiMakerOutput = ""+resultHandler2.getExitValue();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(SpectralRTI_Toolkit.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    logService.log().info(webRtiMakerOutput);
                    appendString += "<html lang=\"en\" xml:lang=\"en\"> <head> <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /> <title>WebRTI "+projectName+"_"+colorProcess+"</title> <link type=\"text/css\" href=\"css/ui-lightness/jquery-ui-1.10.3.custom.css\" rel=\"Stylesheet\"> <link type=\"text/css\" href=\"css/webrtiviewer.css\" rel=\"Stylesheet\"> <script type=\"text/javascript\" src=\"js/jquery.js\"></script> <script type=\"text/javascript\" src=\"js/jquery-ui.js\"></script> <script type=\"text/javascript\" src=\"spidergl/spidergl_min.js\"></script> <script type=\"text/javascript\" src=\"spidergl/multires_min.js\"></script> </head> <body> <div id=\"viewerContainer\"> <script  type=\"text/javascript\"> createRtiViewer(\"viewerContainer\", \""+projectName+"_"+colorProcess+"RTI_"+startTime+"\", $(\"body\").width(), $(\"body\").height()); </script> </div> </body> </html>";                       
                    Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+"_wrti.html"), webRtiMaker.getBytes(), StandardOpenOption.APPEND);
                }
            } 
            else if (preferredFitter.endsWith("cmd")||preferredFitter.endsWith("bash")) {
                int hshOrder = Integer.parseInt(theList.get("hshOrder"));
                if (hshOrder < 2 ) hshOrder = 3;
                int hshThreads = Integer.parseInt(theList.get("hshThreads"));
                if (hshThreads < 1 ) hshThreads = 16;

                logService.log().info("Adding command to batch command file "+preferredFitter+": hshfitter "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti\n");

                appendString += "Brightness Adjust Option: "+brightnessAdjustOption+System.lineSeparator();
                Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt"), appendString.getBytes(), StandardOpenOption.APPEND);
                if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                    appendString += "Normalization area bounds: "+normX+", "+normY+", "+normWidth+", "+normHeight+System.lineSeparator();
                    Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt"), appendString.getBytes(), StandardOpenOption.APPEND);
                } else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
                    appendString += "Normalization fixed value: "+normalizationFixedValue+System.lineSeparator();
                    Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt"), appendString.getBytes(), StandardOpenOption.APPEND);
                }
                if (pcaX > 0) {
                    appendString += "PCA area bounds: "+pcaX+", "+pcaY+", "+pcaWidth+", "+pcaHeight+System.lineSeparator();
                    Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt"), appendString.getBytes(), StandardOpenOption.APPEND);
                }
                appendString += "Jpeg Quality: "+jpegQuality+" (edit SpectralRTI_Toolkit-prefs.txt to change)"+System.lineSeparator();
                Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt"), appendString.getBytes(), StandardOpenOption.APPEND);
                appendString += "Executing command "+preferredFitter+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti"+System.lineSeparator();
                Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt"), appendString.getBytes(), StandardOpenOption.APPEND);
                appendString += "hshfitter "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti"+System.lineSeparator();
                Files.write(Paths.get(preferredFitter), appendString.getBytes(), StandardOpenOption.APPEND);
                appendString += "webGLRTIMaker "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti -q "+jpegQualityWebRTI+" -r "+ramWebRTI+System.lineSeparator();
                Files.write(Paths.get(preferredFitter), appendString.getBytes(), StandardOpenOption.APPEND);
                if (webRtiDesired) {
                    String webRtiString = "<html lang=\"en\" xml:lang=\"en\"> <head> <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /> <title>WebRTI "+projectName+"_"+colorProcess+"</title> <link type=\"text/css\" href=\"css/ui-lightness/jquery-ui-1.10.3.custom.css\" rel=\"Stylesheet\"> <link type=\"text/css\" href=\"css/webrtiviewer.css\" rel=\"Stylesheet\"> <script type=\"text/javascript\" src=\"js/jquery.js\"></script> <script type=\"text/javascript\" src=\"js/jquery-ui.js\"></script> <script type=\"text/javascript\" src=\"spidergl/spidergl_min.js\"></script> <script type=\"text/javascript\" src=\"spidergl/multires_min.js\"></script> </head> <body> <div id=\"viewerContainer\"> <script  type=\"text/javascript\"> createRtiViewer(\"viewerContainer\", \""+projectName+"_"+colorProcess+"RTI_"+startTime+"\", $(\"body\").width(), $(\"body\").height()); </script> </div> </body> </html>";
                    //appendString += "<html lang=\"en\" xml:lang=\"en\"> <head> <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /> <title>WebRTI "+projectName+"_"+colorProcess+"</title> <link type=\"text/css\" href=\"css/ui-lightness/jquery-ui-1.10.3.custom.css\" rel=\"Stylesheet\"> <link type=\"text/css\" href=\"css/webrtiviewer.css\" rel=\"Stylesheet\"> <script type=\"text/javascript\" src=\"js/jquery.js\"></script> <script type=\"text/javascript\" src=\"js/jquery-ui.js\"></script> <script type=\"text/javascript\" src=\"spidergl/spidergl_min.js\"></script> <script type=\"text/javascript\" src=\"spidergl/multires_min.js\"></script> </head> <body> <div id=\"viewerContainer\"> <script  type=\"text/javascript\"> createRtiViewer(\"viewerContainer\", \""+projectName+"_"+colorProcess+"RTI_"+startTime+"\", $(\"body\").width(), $(\"body\").height()); </script> </div> </body> </html>";
                    Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+"_wrti.html"), webRtiString.getBytes(), StandardOpenOption.APPEND);
                }
            } 
            else if (preferredFitter.endsWith("PTMfitter.exe")) { // use PTM fitter
                IJ.error("Macro code to execute PTMfitter not yet complete. Try HSHfitter.");
                throw new Throwable("Macro code to execute PTMfitter not yet complete. Try HSHfitter."); //@@@
            } 
            else {
                IJ.error("Problem identifying type of RTI fitter");
                throw new Throwable("Problem identifying type of RTI fitter");
            }
            logService.log().info("Should have an append string out of this");
            logService.log().warn(appendString);
        }
        
        /**
         * Create Light Position file with filenames from newly created series and light positions from previously generated lp file.
         * @param colorProcess The name of the process being performed
         * @param projDir The project direction
     * @throws java.io.IOException
     * @throws java.lang.Throwable
        */
        // BHTODO this is not creating the lpfile correctly
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
                if (listOfLpFiles_list.size() == 1){
                    lpSource = listOfLpFiles_list.get(0);
                } 
                else if (listOfLpFiles_list.isEmpty()) {
                    noLpData.addMessage("Please provide light position source files in your LightPositionData directory in the future.");
                    noLpData.showDialog();
                    //throw new Throwable("You need to have light position data to continue.");
                    OpenDialog dialog = new OpenDialog("Locate Light Position Source File"); 
                    lpSource = dialog.getPath();
                } 
                else {
                    GenericDialog dialog = new GenericDialog("Select Light Position Source File"); 
                    dialog.addMessage("We found light position files in your project directory.  Please choose the source file to use from below.");
                    dialog.addRadioButtonGroup("File: ", listOfLpFiles, listOfLpFiles_list.size(), 1, listOfLpFiles_list.get(0));
                    dialog.showDialog();
                    lpSource = dialog.getNextRadioButton();
                }
            }
            if(lpSource.equals("")){
                GenericDialog lpFileFailure = new GenericDialog("Provide Light Position Source Data");
                lpFileFailure.addMessage("Light position source data not found.  You must provide this data to continue.");
                lpFileFailure.showDialog();
                throw new Throwable("Light position source data not found");
            }
            File lpFile = new File(lpSource);
            if(!lpFile.exists()){
                GenericDialog lpFileFailure = new GenericDialog("Provide Light Position Source Data");
                lpFileFailure.addMessage("Light position source data not found.  You must provide this data to continue.");
                lpFileFailure.showDialog();
                throw new Throwable("Light position source data not found");
            }
            BufferedReader lpFileReader = Files.newBufferedReader(lpFile.toPath());
            String line= "";
            String lpFileAsText = "";
            logService.log().info("Trying to read from "+lpFile.toPath());
            while((line=lpFileReader.readLine()) != null){
                lpFileAsText += line+System.lineSeparator(); 
            }
            lpFileReader.close();
            lpLines = lpFileAsText.split("\n");//split(File.openAsString(lpSource),"\n");
            noClobber(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp");
            File newLpFile = new File(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp");
            if(!newLpFile.exists()){
                Files.createFile(newLpFile.toPath());
            }
            //Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp"), lpLines[0].getBytes(), StandardOpenOption.APPEND);
            String newLpLine = "";
            for (int i=0;i<lpLines.length;i++) {
                newLpLine = lpLines[i];
                newLpLine = newLpLine.replace("\\", "/"); //simplest to avoid a backslash on the right side of a regular expression replace in the next few lines
                String funnyProjectDirectory = projectDirectory.replace("\\","/"); //Detect this slash and replace.
                //Yikes we would like this to be able to save the more complicated name...

                newLpLine = newLpLine.replace("LightPositionData/jpeg-exports/",colorProcess+"RTI/"+colorProcess+"_");
                newLpLine = newLpLine.replace("canonical",funnyProjectDirectory+colorProcess+"RTI/"+colorProcess+"_"+projectName+"_RTI");
                newLpLine = newLpLine + System.lineSeparator();
                //YIKES Must actually write this file with the correct slash in it for the OS you are running on...
                newLpLine = newLpLine.replace("/", File.separator); //write it in with the proper slash
                logService.log().info("New lp line is "+newLpLine);
                //newLpLine = newLpLine.replace("/",File.separator);
                //Yikes not sure this is writing in correct.  Always right in with / and convert on read. Commented out line above.
                Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp"), newLpLine.getBytes(), StandardOpenOption.APPEND);
            }
        }

	/**
	 * Main method for debugging.
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
            System.out.println("Hello Word 2");
            final ImageJ IJinstance = net.imagej.Main.launch(args);
            //IJinstance.ui().showUI();
            IJinstance.command().run(SpectralRTI_Toolkit.class, false);
            System.out.println("Finished processing MAIN");
	}
      
}
