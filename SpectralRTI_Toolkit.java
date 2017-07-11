/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

/*  Optimization notes
    - Java exec() command works a bit different than the IJ one.  String should be submitted as an array.  see https://stackoverflow.com/questions/6434009/java-runtime-getruntime-exec-cmd-with-long-parameters
    
*/

package com.slu.imagej;

//ImageJ2 specific imports
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.gui.WaitForUserDialog;
import ij.io.OpenDialog;
import java.awt.Rectangle;
import net.imagej.ImageJ;
import org.scijava.ui.UIService;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.log.LogService;
import net.imagej.display.WindowService;

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * A template for processing each pixel of either
 * GRAY8, GRAY16, GRAY32 or COLOR_RGB images.
 *
 * @author Johannes Schindelin
 */
@Plugin(type = Command.class, headless=true, menuPath = "Plugins>Open Image")  
public class SpectralRTI_Toolkit implements Command {
        
        private Context context;
        
        @Parameter
	private UIService ui;
    
        @Parameter
	protected ImagePlus image;
        
	// image property members
        @Parameter
        private LogService logService;
        
        @Parameter 
        WindowService window;
        
        // @Parameter
        private final ImageJ IJ2 = new ImageJ();
        
        //SRTI vars
        private int jpegQuality = 100; //maximize quality for non-distribution phases
        private int jpegQualityWebRTI = 100; //lower for final distribution
        private int ramWebRTI = 8192;
        private String brightnessAdjustOption = "";
        private String brightnessAdjustApply = "";
        private String transmissiveSource= ""; //(thanks Kathryn!)
	private int normX;
	private int normY;
	private int normWidth;
	private int normHeight;
	private int normalizationFixedValue;
	private final int pcaX = 0;
	private final int pcaY = 0;
	private final int pcaWidth = 0;
	private final int pcaHeight = 0;
	private String lpSource = "";
	private String projectDirectory = "";
	private String projectName = "";
	private List<Boolean> listOfRakingDirections;

        //New vars needed from macro to plugin conversion
        
        //This works as the List used throughout the macro.
        private final static HashMap<String, String> theList; //List var from macro
        private String startTime = "";
        private boolean webRtiDesired = false;
        static
        {
            theList = new HashMap<>();
            theList.put("preferredCompress", "JP2 Compressor");
            theList.put("preferredJp2Args", "JP2 Arguments");
            theList.put("preferredFitter", "HSH Fitter");
            theList.put("jpegQuality", "JPEG Quality");
            theList.put("hshOrder", "HSH Order");
            theList.put("hshThreads", "HSH Threads");
        }

       
        private File[] listOfHemisphereCaptures = getHemisphereCaptures(projectDirectory+"Captures-Hemisphere-Gamma"+File.separator);
        
        //End SRTI vars
        
	private int width;
	private int height;

	public double value;
        public String name;
        
	@Override
	public void run() {
            Boolean clobberCheck = false;
            String tmpFile = "";
            System.out.println("We are running");
            image = IJ.openImage("http://imagej.net/images/clown.jpg");
            image.show("Successfully loaded image");
            logService.info("Let's check a timestamp...");
            String time = timestamp();
            logService.log().warn(time);          
            image = IJ.openImage("C:\\Users\\bhaberbe\\Desktop\\earth_day.jpg");
            image.show("Now we have a Earth!");
            image = WindowManager.getImage(1);
            image.close();
            logService.log().info("Closed the Clown?");
            logService.log().info("Now i want to see if no clobber does alright");
            try {
                clobberCheck = noClobber("C:\\Users\\bhaberbe\\Desktop\\earth_day.jpg");
            } catch (IOException ex) {
                Logger.getLogger(SpectralRTI_Toolkit.class.getName()).log(Level.SEVERE, null, ex);
            }
            logService.log().warn(clobberCheck);
            logService.log().info("Now I want to see if I can get a file list");
            File[] tmpList = getHemisphereCaptures("/the/hemispheres/dir");
            logService.log().warn(tmpList);
            logService.log().info("Now I am wondering if I can make the jp2 file");
            try {
                tmpFile = createJp2("/some/infile");
            } catch (IOException ex) {
                Logger.getLogger(SpectralRTI_Toolkit.class.getName()).log(Level.SEVERE, null, ex);
            }
            logService.warn(tmpFile);
            //Continue testing pieces here.  Eventually, the actual macro code goes right here.  gotta get the helper functions going first. 
	}
        

        //macro "Spectral RTI [n1]"
        //This needs to be assigned in the run() method when ready.  Can be called in run with theMacro() to debug.
        private void theMacro() throws IOException{
            //setBatchMode(true); //Not sure how to handle this specifically.  Controls whether images are visible or hidden during macro execution
            //want these variables to be accessible across functions and to reset each time the macro is run
            String startTime = timestamp();
            String brightnessAdjustOption = "";
            String brightnessAdjustApply = "";
            int pcaX = 0;
            int pcaY = 0;
            int pcaWidth = 0;
            int pcaHeight = 0;
            File accurateColorSource = null;
            //vars that I had to add
            HashMap <String, String> prefsConsolut = new HashMap<>();
            File spectralPrefsFile = new File("SpectralRTI_Toolkit-prefs.txt");
            GenericDialog prefsDialog = new GenericDialog("Consult Preferences");
            BufferedReader prefsReader = Files.newBufferedReader(spectralPrefsFile.toPath());
            String line= "";
            String prefsFileAsText = "";
            String prefsLines = "";
            List<String> prefsConsult_list = null;
            OpenDialog file_dialog;
            Boolean lpDesired=true;
            Boolean acRtiDesired=true;
            Boolean xsRtiDesired=true;
            Boolean psRtiDesired=true;
            Boolean psRakingDesired = false;
            Boolean csRtiDesired = false;
            Boolean csRakingDesired = false;
            Boolean acRakingDesired = false;
            Boolean xsRakingDesired = false;
            File light_position_file = new File(projectDirectory+"LightPositionData"+File.separator);
            File accurate_color_file = new File(projectDirectory+"AccurateColor"+File.separator);
            File narrow_band_file = new File(projectDirectory+"Captures-Narrowband-NoGamma"+File.separator);
            File pseudo_color_file = new File(projectDirectory+"PseudocolorRTI"+File.separator);
            File extended_spectrum_file = new File(projectDirectory+"ExtendedSpectrumRTI"+File.separator);
            File static_ranking_file = new File(projectDirectory+"StaticRaking"+File.separator);
            File transmissive_gamma_file = new File(projectDirectory+"Captures-Transmissive-Gamma"+File.separator);
            String csSource = "";
            //End vars I had to add
            //consult with user about values stored in prefs file
            // prefsConsult = newArray();
            prefsDialog.addMessage("The following settings are remembered from the configuration file or a previous run.\nEdit or clear as desired.");
            
            if (spectralPrefsFile.exists()) {
                    prefsReader = Files.newBufferedReader(spectralPrefsFile.toPath());
                    line= "";
                    prefsFileAsText = "";
                    prefsLines = "";
                    
                    while((line=prefsReader.readLine()) != null){
                        prefsFileAsText += line;
                    }
                    prefsReader.close();
                    String[] prefs = prefsFileAsText.split("\n");
                    for (int i=0;i<prefs.length;i++) {
                            String key = prefs[i].substring(0, prefs[i].indexOf("="));
                            key = key.replace("preferredCompress","JP2 Compressor");
                            key = key.replace("preferredJp2Args","JP2 Arguments");
                            key = key.replace("preferredFitter","HSH Fitter");
                            key = key.replace("jpegQuality","JPEG Quality");
                            key = key.replace("hshOrder","HSH Order");
                            key = key.replace("hshThreads","HSH Threads");
                            String value = prefs[i].substring(prefs[i].indexOf("=")+1);
                            prefsDialog.addStringField(key, value, 80);
                            prefsConsult_list.add(key);
                    }
            }
            prefsDialog.showDialog();
            for (int j=0; j<prefsConsult_list.size();j++) {
                    String key = prefsConsult_list.get(j);
                    key = key.replace("JP2 Compressor","preferredCompress");
                    key = key.replace("JP2 Arguments","preferredJp2Args");
                    key = key.replace("HSH Fitter","preferredFitter");
                    key = key.replace("JPEG Quality","jpegQuality");
                    key = key.replace("HSH Order","hshOrder");
                    key = key.replace("HSH Threads","hshThreads");
                    String value = prefsDialog.getStringFields().get(0).toString();
                    theList.put(key,value);
            }
            jpegQuality = ij.plugin.JpegWriter.getQuality();
            if (Integer.parseInt(theList.get("jpegQuality")) > 0) jpegQuality = Integer.parseInt(theList.get("jpegQuality"));
            IJ.run("Input/Output...","jpeg="+jpegQuality);
            file_dialog = new OpenDialog("Choose a Directory"); 
            projectDirectory = file_dialog.getDirectory();
            projectDirectory = projectDirectory.replace("\\",File.separator);
            File projectFile = new File(projectDirectory);
            projectName = projectFile.getName();
            File hemi_gamma_file = new File(projectDirectory+"Captures-Hemisphere-Gamma"+File.separator);
            if (!hemi_gamma_file.exists()) { //added the ! here.  Should have I?
                    Path createPath = hemi_gamma_file.toPath();
                    Files.createDirectory(createPath);
                    logService.log().info("A directory has been created for the Hemisphere Captures at "+projectDirectory+"Captures-Hemisphere-Gamma"+File.separator);
            }
            listOfHemisphereCaptures = hemi_gamma_file.listFiles();
            GenericDialog hemiWarning = new GenericDialog("Please Populate Hemisphere Captures");
            hemiWarning.addMessage("The software expects at least 30 images in HemisphereCaptures folder.\nPlease populate the folder and press Ok to continue, or cancel.");
            while (listOfHemisphereCaptures.length < 29) {
                hemiWarning.showDialog();
                listOfHemisphereCaptures = hemi_gamma_file.listFiles();
            }
            lpDesired=true;
            acRtiDesired=true;
            xsRtiDesired=true;
            psRtiDesired=true;
            
            if (light_position_file.exists()) lpDesired = false;
            File[] listOfAccurateColorFiles = accurate_color_file.listFiles();
            if (listOfAccurateColorFiles.length<1) acRtiDesired = false;
            if (accurate_color_file.exists()) acRtiDesired = false;
            File[] listOfNarrowbandCaptures = narrow_band_file.listFiles();
            if (listOfNarrowbandCaptures.length<9) {
                    xsRtiDesired=false;
                    psRtiDesired=false;
            }
            if (pseudo_color_file.exists()) psRtiDesired = false;
            if (extended_spectrum_file.exists()) xsRtiDesired = false;
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
            tasksDialog.show();
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
            
            //identify angles for uncompressed static raking
            if (acRakingDesired || acRtiDesired || xsRtiDesired || xsRakingDesired || psRtiDesired || psRakingDesired || csRtiDesired || csRakingDesired){
                    if (brightnessAdjustOption == "") promptBrightnessAdjust();
            }
            if (acRakingDesired || xsRakingDesired || psRakingDesired || csRakingDesired){
                    if (!static_ranking_file.exists()) {
                        Path staticFilePath = static_ranking_file.toPath();
                        Files.createDirectory(staticFilePath);
                        logService.log().info("A directory has been created for lossless static raking images at "+projectDirectory+"StaticRaking"+File.separator);
                    }
                    File[] listOfTransmissiveSources_file = transmissive_gamma_file.listFiles();
                    String[] listOfTransmissiveSources = new String[listOfTransmissiveSources_file.length];
                    ArrayList<String> listOfTransmissiveSources_list = new ArrayList<String>();
                    for (File f : listOfTransmissiveSources_file) {
                       listOfTransmissiveSources_list.add(f.toString());
                    }
                    listOfTransmissiveSources_list.toArray(listOfTransmissiveSources);
                    if (listOfTransmissiveSources.length == 1) 	{ // no opt out of creating a transmissive static if transmissive folder is populated, but not a problem
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
                    for(int k=0;k<listOfHemisphereCaptures.length;k++) {
			listOfRakingDirections.add(lightDialog.getNextBoolean());
                    }                    
            } 
            else { //We already have the list initiated, so do nothing
                    //listOfRakingDirections = newArray(listOfHemisphereCaptures.length+1);
            }
            if (xsRtiDesired || xsRakingDesired) { // only interaction here, processing later
		//create a dialog suggesting and confirming which narrowband captures to use for R,G,and B
                String[] rgbnOptions = new String[3];
                rgbnOptions[0] = "R";
                rgbnOptions[1] = "G";
                rgbnOptions[2] = "B";
                rgbnOptions[3] = "none";
                String defaultRange = "";
                String rangeChoice = "";
		//rgbnOptions = newArray("R","G","B","none");
		File[] redNarrowbands = new File[0];
                File[] greenNarrowbands = new File[0];
                File[] blueNarrowbands = new File[0];
                
                GenericDialog narrowBandDialog = new GenericDialog("Assign Narrowband Captures");
		narrowBandDialog.addMessage("Assign each narrowband capture to the visible range of R, G, B, or none");
		for (int i=0; i<listOfNarrowbandCaptures.length; i++) {
			if ((i+1)/listOfNarrowbandCaptures.length < 0.34) defaultRange = "B";
			else if ((i+1)/listOfNarrowbandCaptures.length > 0.67) defaultRange = "R";
			else defaultRange = "G";
                        narrowBandDialog.setInsets(0,0,0);
                        String narrowCapture = listOfNarrowbandCaptures[i].toString();
			narrowBandDialog.addRadioButtonGroup(narrowCapture, rgbnOptions, 1, 4, defaultRange);
		} // @@@ problem here if runs off screen... no an option to use two columns
                narrowBandDialog.showDialog();
		for (int j=0; j<listOfNarrowbandCaptures.length; j++) {
			rangeChoice = narrowBandDialog.getNextRadioButton();
			if (rangeChoice == "R") {
				redNarrowbands[0] = listOfNarrowbandCaptures[j];
			} else if (rangeChoice == "G") {
				greenNarrowbands[0] = listOfNarrowbandCaptures[j];
			} else if (rangeChoice == "B") {
				blueNarrowbands[0] = listOfNarrowbandCaptures[j];
			}
		}
		if (pcaHeight <100) {
                        File narrowbandNoGamma = new File(projectDirectory+"Captures-Narrowband-NoGamma"+File.separator+listOfNarrowbandCaptures[Math.round(listOfNarrowbandCaptures.length/2)]);
                        ImagePlus img = IJ.openImage(narrowbandNoGamma.toString());
                        //img.setTitle("Preview");
                        img.show();
			//rename("Preview");
			//setBatchMode("show");
                        WaitForUserDialog dWait = new WaitForUserDialog("Select area", "Draw a rectangle containing the colors of interest for PCA\n(hint: limit to object or smaller)");
                        dWait.show();
                        Roi roi = new Roi(pcaX, pcaY, pcaWidth, pcaHeight);
                        //image.setRoi(roi); //? do i need this?  I think only for makeRectangle
                        Rectangle bounds = roi.getBounds(); //getFloatBounds() is also a thing.  Does this do what getSelectionBouds does?
			//getSelectionBounds(pcaX, pcaY, pcaWidth, pcaHeight);
			img.close();
		}
            }
            if (psRtiDesired || psRakingDesired) {// only interaction here, processing later
                //identify 2 source images for pca pseudocolor
                File listOfPseudocolorSources_file = new File(projectDirectory+"PCA"+File.separator);
                File[] listOfPseudocolorSources = listOfPseudocolorSources_file.listFiles();
                String defaultPca = "";
                if (listOfPseudocolorSources.length > 1) defaultPca = "Open pregenerated images" ;
                else defaultPca = "Generate and select using defaults";
                String[] listOfPcaMethods = new String[2];
                listOfPcaMethods[0]="Generate and select using defaults";
                listOfPcaMethods[1]="Generate and manually select two";
                listOfPcaMethods[2]="Open pregenerated images";
                GenericDialog pseudoSources = new GenericDialog("Select sources for Pseudocolor");
                pseudoSources.addMessage("Pseudocolor images require two source images (typically principal component images).");
                pseudoSources.addRadioButtonGroup("Method: ",listOfPcaMethods,listOfPcaMethods.length,1,defaultPca);
                pseudoSources.show();
                String pcaMethod = pseudoSources.getNextRadioButton();
                if (pcaHeight < 100) {
                        ImagePlus imgp = IJ.openImage(projectDirectory+"Captures-Narrowband-NoGamma"+File.separator+listOfNarrowbandCaptures[Math.round(listOfNarrowbandCaptures.length/2)]);
                        imgp.show();
                        //imgp.setTitle("Preview");
                        //rename("Preview");
                        //setBatchMode("show");
                        WaitForUserDialog dWait = new WaitForUserDialog("Select area", "Draw a rectangle containing the colors of interest for PCA\n(hint: limit to object or smaller)");
                        dWait.show();
                        Roi roi = new Roi(pcaX, pcaY, pcaWidth, pcaHeight);
                        //image.setRoi(roi); //? do i need this?  I think only for makeRectangle
                        Rectangle bounds = roi.getBounds(); //getFloatBounds() is also a thing.  Does this do what getSelectionBouds does?
                        imgp.close();
                       // getSelectionBounds(pcaX, pcaY, pcaWidth, pcaHeight);
                }
            }
            if (csRtiDesired || csRakingDesired) { //interaction phase
                OpenDialog csSourceDialog = new OpenDialog("Choose a Source for Custom Process");
                csSource = csSourceDialog.getPath();
            }
            if (acRtiDesired) {
		//create series of images with luminance from hemisphere captures and chrominance from color image
                File accurateRTI = new File(projectDirectory+"AccurateColorRTI"+File.separator);
		if (!accurateRTI.exists()) {
                    Files.createDirectory(accurateRTI.toPath());
                    logService.log().info("A directory has been created for Accurate Color RTI at "+projectDirectory+"AccurateColorRTI"+File.separator);
		}
		//integration
                File accurateColorSources_files = new File(projectDirectory+"AccurateColor"+File.separator);
		File[] listOfAccurateColorSources = accurateColorSources_files.listFiles();
                String[] listOfAccurateColorSources_string = new String[listOfAccurateColorSources.length];;
                ArrayList<String>  listOfAccurateColorSources_list = new ArrayList<String>();
                for (File f : listOfAccurateColorSources) {
                   listOfAccurateColorSources_list.add(f.toString());
                }
                listOfAccurateColorSources_list.toArray(listOfAccurateColorSources_string);
                List<String> sourceListToConvert;
		if (listOfAccurateColorSources.length == 1) 	{
			accurateColorSource = listOfAccurateColorSources[0];
		} else if (listOfAccurateColorSources.length == 0) {
                    IJ.error("Need at least one color image file in "+projectDirectory+"AccurateColorRTI"+File.separator);
		} else {
			for (int i=0; i<listOfAccurateColorSources.length; i++) {
                            if (listOfAccurateColorSources[i].toString().indexOf("sRGB")>0) accurateColorSource = listOfAccurateColorSources[i];
			}
			if (null!=accurateColorSource || !accurateColorSource.exists()){
                            GenericDialog gd = new GenericDialog("Select Color Source");
                            gd.addMessage("Select Color Source");
                            gd.addRadioButtonGroup("File: ", listOfAccurateColorSources_string, listOfAccurateColorSources.length, 1, listOfAccurateColorSources[0].toString());
                            gd.show();
                            accurateColorSource = new File(gd.getNextRadioButton());
			}
		}
                ImagePlus imp = IJ.openImage(projectDirectory+"AccurateColor"+File.separator+ accurateColorSource);
                //imp.setTitle("RGBtiff");
                imp.show();
		if (imp.getBitDepth() == 8) {
			IJ.run("RGB Color");
		}
		IJ.run("RGB to YCbCr stack");
		IJ.run("Stack to Images");
                /*
                Yikes don't know why this is here or what to do with it. 
		selectWindow("Y");
		run("Close");
                */
		imp.close();
		//Luminance from hemisphere captures
		for(int i=0;i<listOfHemisphereCaptures.length;i++) {
			if (listOfHemisphereCaptures[i].toString().endsWith("tiff")) { //@@@ better to trim list at the beginning so that array.length can be used in lp file
				ImagePlus implus = IJ.openImage(projectDirectory+"Captures-Hemisphere-Gamma"+File.separator+listOfHemisphereCaptures[i]);
                                //implus.setTitle("Luminance");
				//rename("Luminance");
				// it would be better to crop early in the process, especially before reducing to 8-bit and jpeg compression
				// normalize
				if (brightnessAdjustApply.equals("RTI images also")) {
					if (brightnessAdjustOption.equals("Yes, by normalizing each image to a selected area")) {
                                            Roi roi = new Roi(normX, normY, normWidth, normHeight);
                                            implus.setRoi(roi); //? do i need this?  I think only for makeRectangle
						//makeRectangle(normX, normY, normWidth, normHeight);
                                            IJ.run("Enhance Contrast...", "saturated=0.4");
                                            IJ.run("Select None");
					} else if (brightnessAdjustOption.equals("Yes, by multiplying all images by a fixed value")) {
						IJ.run("Multiply...", "value="+normalizationFixedValue+"");
					}
				}
				IJ.run("8-bit");
				IJ.run("Concatenate...", "  title=[YCC] keep image1=Luminance image2=Cb image3=Cr image4=[-- None --]");
				IJ.run("YCbCr stack to RGB");
				//Save as jpeg
				noClobber(projectDirectory+"AccurateColorRTI"+File.separator+"AccurateColor_"+implus.getShortTitle()+".jpg");
				IJ.saveAs("jpeg", projectDirectory+"AccurateColorRTI"+File.separator+"AccurateColor_"+implus.getShortTitle()+".jpg");
				//setBatchMode("show"); //debugging
				//selectWindow(implus.getShortTitle()+".jpg");
				//run("Close");
                                //implus.show();
                                implus.close();
                                /*
                                Yikes what do I do with these?  What are these?
                                selectWindow("YCC");
				run("Close");
				selectWindow("Luminance");
				run("Close");
                                */
			}
		}
                /*
                 * Yikes!  What do I do with these?  What are these?
                selectWindow("Cb");
		run("Close");
		selectWindow("Cr");
		run("Close");
                */
		createLpFile("AccurateColor");
		runFitter("AccurateColor");
            }   
            IJ.beep();
            //IJ.setBatchMode("exit and display"); //Not sure what to do with this!
            IJ.showMessage("Processing Complete", "Processing complete at "+timestamp());
        }
        
        //SRTI functions
        
        /* Helper function to populate the file list for hemisphereCaputers.  Based off getFilesList() macro */
        public File[] getHemisphereCaptures(String dir) {
            File folder = new File(dir);
            File[] listOfFiles = folder.listFiles();
            return listOfFiles;
        }
        
        public String createJp2(String inFile) throws IOException {
            String preferredCompress = theList.get("preferredCompress");
            String preferredJp2Args = theList.get("preferredJp2Args");
            String compressString = "preferredCompress="+preferredCompress;
            String preferredString = "preferredJp2Args="+preferredJp2Args;
            File file = new File("SpectralRTI_Toolkit-prefs.txt");
            String fileName = file.getName();
            OpenDialog dialog;  //For files
            String directory = "";
            String returnString = "/created/JP2file";
            if (preferredCompress ==""){
                dialog = new OpenDialog("Locate kdu_compress or ojp_compress", fileName); 
                directory = dialog.getPath();
                Files.write(Paths.get(directory), compressString.getBytes(), StandardOpenOption.APPEND);
            }
            if (preferredJp2Args == ""){
                GenericDialog gd = new GenericDialog("Approve arguments for Jpeg 2000 compression");
                String arguments = "-rate -,2.4,1.48331273,.91673033,.56657224,.35016049,.21641118,.13374944,.08266171 Creversible\\=no Clevels\\=5 Stiles\\=\\{1024,1024\\} Cblk\\=\\{64,64\\} Cuse_sop\\=yes Cuse_eph\\=yes Corder\\=RPCL ORGgen_plt\\=yes ORGtparts\\=R Cmodes\\=BYPASS -double_buffering 10 -num_threads 4 -no_weights";
                gd.addStringField("Arguments:",arguments,80);
                gd.showDialog();
                preferredJp2Args = gd.getNextString();
                preferredString = "preferredJp2Args="+preferredJp2Args;
                Files.write(Paths.get(file.getPath()), preferredString.getBytes(), StandardOpenOption.APPEND);
            }
            noClobber(projectDirectory+"StaticRaking"+File.separator+inFile+".jp2");
            logService.log().info("Executing command "+preferredCompress+" -i "+projectDirectory+"StaticRaking"+File.separator+inFile+".tiff -o " +projectDirectory+"StaticRaking"+File.separator+inFile+".jp2 "+preferredJp2Args+"\n");

            //This failed, I may need clarification on this or may need to run it in a UNIX environment.  
//            String execString = preferredCompress+" -i "+projectDirectory+"StaticRaking"+File.separator+inFile+".tiff -o " +projectDirectory+"StaticRaking"+File.separator+inFile+".jp2 "+preferredJp2Args+"\n";
//            Process start = Runtime.getRuntime().exec(execString);
//            BufferedReader r = new BufferedReader(
//                 new InputStreamReader(start.getErrorStream())
//            );
//            String line = null;
//            
//            while ((line = r.readLine()) != null)
//            {
//                returnString += line;
//            }
            return returnString;
        }
        
        public Boolean noClobber(String safeName) throws IOException {
            Boolean success = false;
            File oldFile = new File(safeName);
            Path safeNamePath = Paths.get(safeName);
            String verboseDate = "";
            String newFileName = safeName;
            logService.log().info("No clobber name b4");
            logService.log().warn(newFileName);
            if (oldFile.exists()) {
                verboseDate = Files.getLastModifiedTime(safeNamePath).toString();
                verboseDate = verboseDate.replace(" ","_");
                verboseDate = verboseDate.replace("\\:","-");
                newFileName = newFileName.replace("\\.","("+verboseDate+").");
                logService.log().info("safeName after");
                logService.log().warn(newFileName);
                File newFileFileName = new File(newFileName);
                success = oldFile.renameTo(newFileFileName);
            }
            else{
                logService.log().error("Could not perform no clobber.  "+safeName+" does not exist");
            }
            return success;
        }
        
        public String timestamp() {
                Date currentDate = new Date();
                SimpleDateFormat ft = new SimpleDateFormat ("yyyyMMdd'_'hhmm");
                String dateString = ft.format(currentDate);
                return dateString;
        }
        
        public void promptBrightnessAdjust() {
            image = IJ.openImage(projectDirectory+"Captures-Hemisphere-Gamma"+File.separator+listOfHemisphereCaptures[Math.round(listOfHemisphereCaptures.length/2)]);
            //rename("Preview");
            //setBatchMode("show");  Use the GeneralDialog.setModal variable and imagePlys.hide() and show() to do what this is trying to do.
            image.show();
            
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
            
            //This really may not be right.  I suppose this is like a 1 or 0 or true or false, what did the original return?
            brightnessAdjustOption = gd.getRadioButtonGroups().get(0).toString();
            brightnessAdjustApply = gd.getRadioButtonGroups().get(1).toString();
            
            if (brightnessAdjustOption == "Yes, by normalizing each image to a selected area") {
                    //gd.setVisible(false);
                    WaitForUserDialog dWait = new WaitForUserDialog("Select Area","Draw a rectangle containing the brighest white and darkest black desired then press OK\n(hint: use a large area including spectralon and the object, excluding glare)" );
                    dWait.show();
                    Roi roi = new Roi(normX, normY, normWidth, normHeight);
                    //image.setRoi(roi); //? do i need this?  I think only for makeRectangle
                    Rectangle bounds = roi.getBounds(); //getFloatBounds() is also a thing
                    logService.log().info("Should have bounds below...");
                    logService.log().warn(bounds);
                    //Will getting the bounds like so do what happens below? 
                    //Returns the smallest rectangle that can completely contain the current selection. x and y are the pixel coordinates of the upper left corner of the rectangle
                    //getSelectionBounds(normX, normY, normWidth, normHeight);
            } 
            else if (brightnessAdjustOption == "Yes, by multiplying all images by a fixed value") {
//                    GenericDialog gdWait2 = new GenericDialog("Choose multiplier");
//                    gd.addMessage("Use the Muliply dialog to preview and choose a multiplier value.\nThis is just a preview image; the chosen value will be entered next.");
//                    gd.setModal(false);
                    WaitForUserDialog dWait = new WaitForUserDialog("Use the Muliply dialog to preview and choose a multiplier value.\nThis is just a preview image; the chosen value will be entered next." );
                    dWait.show();
                    //waitForUser("", "");
                    //run("Multiply...");
                    IJ.run("Multiply...");
                    GenericDialog gdMultiplier = new GenericDialog("Enter selected multiplier");
                    gdMultiplier.addNumericField("Enter selected multiplier: ", 1.30,2,4,"");
                    gdMultiplier.showDialog();
                    logService.log().info("Should have a multiplier below...");
                    //Will this actually get the field?
                    normalizationFixedValue = (int) gdMultiplier.getNumericFields().get(0);
                    logService.log().warn(normalizationFixedValue);
            }
            //selectWindow("Preview");
            //IJ2.command().run("Close", false);
            image.close();
        } 
        
        public void runFitter(String colorProcess) throws IOException { //identify preferred fitter and exec with arguments
                String preferredFitter = theList.get("preferredFitter");
                String fitterOutput = "";
                String webRtiMakerOutput = "";
                String webRtiMaker = "";
                if (preferredFitter == "") {
                        OpenDialog dialog = new OpenDialog("Locate Preferred RTI Fitter or cmd file for batch processing");
                        preferredFitter = dialog.getFileName();
                        String appendString = "preferredFitter="+preferredFitter;
                        Files.write(Paths.get("SpectralRTI_Toolkit-prefs.txt"), appendString.getBytes(), StandardOpenOption.APPEND);
                }
                if (preferredFitter.endsWith("hshfitter.exe")) { // use HSH fitter
                        int hshOrder = Integer.parseInt(theList.get("hshOrder"));
                        if (hshOrder < 2 ) hshOrder = 3;
                        int hshThreads = Integer.parseInt(theList.get("hshThreads"));
                        if (hshThreads < 1 ) hshThreads = 16;
                        String appendString = "Brightness Adjust Option: "+brightnessAdjustOption;
                        //logService.log().info("Executing command "+preferredFitter+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti\nThis could take a while...");
                        Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt"), appendString.getBytes(), StandardOpenOption.APPEND);
                        if (brightnessAdjustOption == "Yes, by normalizing each image to a selected area") {
                            appendString = "Normalization area bounds: "+normX+", "+normY+", "+normWidth+", "+normHeight;
                            Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt"), appendString.getBytes(), StandardOpenOption.APPEND);
                        } 
                        else if (brightnessAdjustOption == "Yes, by multiplying all images by a fixed value") {
                                appendString = "Normalization fixed value: "+normalizationFixedValue;
                                Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt"), appendString.getBytes(), StandardOpenOption.APPEND);
                        }
                        if (pcaX > 0) {
                                appendString = "PCA area bounds: "+pcaX+", "+pcaY+", "+pcaWidth+", "+pcaHeight;
                                Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt"), appendString.getBytes(), StandardOpenOption.APPEND);
                        }
                        appendString = "Jpeg Quality: "+jpegQuality+" (edit SpectralRTI_Toolkit-prefs.txt to change)";
                        Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt"), appendString.getBytes(), StandardOpenOption.APPEND);
                        appendString = "Executing command "+preferredFitter+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti";
                        Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt"), appendString.getBytes(), StandardOpenOption.APPEND);
            
//            String execString = preferredFitter+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti";
            //            Process start = Runtime.getRuntime().exec(execString);
            //            BufferedReader r = new BufferedReader(
            //                 new InputStreamReader(start.getErrorStream())
            //            );
            //            String line = null;
            //            
            //            while ((line = r.readLine()) != null)
            //            {
            //                fitterOutput += line;
            //            }
                        //fitterOutput = exec(preferredFitter+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti");
                        logService.log().info(fitterOutput);
                        
                        Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt"), fitterOutput.getBytes(), StandardOpenOption.APPEND);
                        if (webRtiDesired) {
                                webRtiMaker = theList.get("webRtiMaker");
                                if (webRtiMaker == "") {
                                    OpenDialog dialog2 = new OpenDialog("Locate webGLRTIMaker.exe");
                                    webRtiMaker = dialog2.getPath();
                                    Files.write(Paths.get("SpectralRTI_Toolkit-prefs.txt"), webRtiMaker.getBytes(), StandardOpenOption.APPEND);
                                }
            //            String execString2 = preferredFitter+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti";
            //            Process start2 = Runtime.getRuntime().exec(execString);
            //            BufferedReader r2 = new BufferedReader(
            //                 new InputStreamReader(start2.getErrorStream())
            //            );
            //            String line2 = null;
            //            
            //            while ((line2 = r2.readLine()) != null)
            //            {
            //                webRitMakerOutput += line2;
            //            }
                               // webRtiMakerOutput = exec(webRtiMaker+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti -q "+jpegQualityWebRTI+" -r "+ramWebRTI);
                                logService.log().info(webRtiMakerOutput);
                                appendString = "<html lang=\"en\" xml:lang=\"en\"> <head> <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /> <title>WebRTI "+projectName+"_"+colorProcess+"</title> <link type=\"text/css\" href=\"css/ui-lightness/jquery-ui-1.10.3.custom.css\" rel=\"Stylesheet\"> <link type=\"text/css\" href=\"css/webrtiviewer.css\" rel=\"Stylesheet\"> <script type=\"text/javascript\" src=\"js/jquery.js\"></script> <script type=\"text/javascript\" src=\"js/jquery-ui.js\"></script> <script type=\"text/javascript\" src=\"spidergl/spidergl_min.js\"></script> <script type=\"text/javascript\" src=\"spidergl/multires_min.js\"></script> </head> <body> <div id=\"viewerContainer\"> <script  type=\"text/javascript\"> createRtiViewer(\"viewerContainer\", \""+projectName+"_"+colorProcess+"RTI_"+startTime+"\", $(\"body\").width(), $(\"body\").height()); </script> </div> </body> </html>";                       
                                Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+"_wrti.html"), webRtiMaker.getBytes(), StandardOpenOption.APPEND);
                        }
                } 
                else if (preferredFitter.endsWith("cmd")||preferredFitter.endsWith("bash")) {
                        int hshOrder = Integer.parseInt(theList.get("hshOrder"));
                        if (hshOrder < 2 ) hshOrder = 3;
                        int hshThreads = Integer.parseInt(theList.get("hshThreads"));
                        if (hshThreads < 1 ) hshThreads = 16;
                        
                        logService.log().info("Adding command to batch command file "+preferredFitter+": hshfitter "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti\n");
                        
                        String appendString = "Brightness Adjust Option: "+brightnessAdjustOption;
                        Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt"), appendString.getBytes(), StandardOpenOption.APPEND);
                        if (brightnessAdjustOption == "Yes, by normalizing each image to a selected area") {
                                appendString = "Normalization area bounds: "+normX+", "+normY+", "+normWidth+", "+normHeight;
                                Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt"), appendString.getBytes(), StandardOpenOption.APPEND);
                        } else if (brightnessAdjustOption == "Yes, by multiplying all images by a fixed value") {
                                appendString = "Normalization fixed value: "+normalizationFixedValue;
                                Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt"), appendString.getBytes(), StandardOpenOption.APPEND);
                        }
                        if (pcaX > 0) {
                                appendString = "PCA area bounds: "+pcaX+", "+pcaY+", "+pcaWidth+", "+pcaHeight;
                                Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt"), appendString.getBytes(), StandardOpenOption.APPEND);
                                
                        }
                        appendString = "Jpeg Quality: "+jpegQuality+" (edit SpectralRTI_Toolkit-prefs.txt to change)";
                        Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt"), appendString.getBytes(), StandardOpenOption.APPEND);
                        appendString = "Executing command "+preferredFitter+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti";
                        Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt"), appendString.getBytes(), StandardOpenOption.APPEND);
                        appendString = "hshfitter "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti";
                        Files.write(Paths.get(preferredFitter), appendString.getBytes(), StandardOpenOption.APPEND);
                        appendString = "webGLRTIMaker "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti -q "+jpegQualityWebRTI+" -r "+ramWebRTI;
                        Files.write(Paths.get(preferredFitter), appendString.getBytes(), StandardOpenOption.APPEND);
                        if (webRtiDesired) {
                            appendString = "<html lang=\"en\" xml:lang=\"en\"> <head> <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /> <title>WebRTI "+projectName+"_"+colorProcess+"</title> <link type=\"text/css\" href=\"css/ui-lightness/jquery-ui-1.10.3.custom.css\" rel=\"Stylesheet\"> <link type=\"text/css\" href=\"css/webrtiviewer.css\" rel=\"Stylesheet\"> <script type=\"text/javascript\" src=\"js/jquery.js\"></script> <script type=\"text/javascript\" src=\"js/jquery-ui.js\"></script> <script type=\"text/javascript\" src=\"spidergl/spidergl_min.js\"></script> <script type=\"text/javascript\" src=\"spidergl/multires_min.js\"></script> </head> <body> <div id=\"viewerContainer\"> <script  type=\"text/javascript\"> createRtiViewer(\"viewerContainer\", \""+projectName+"_"+colorProcess+"RTI_"+startTime+"\", $(\"body\").width(), $(\"body\").height()); </script> </div> </body> </html>";
                            Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+"_wrti.html"), appendString.getBytes(), StandardOpenOption.APPEND);
                        }
                } 
                else if (preferredFitter.endsWith("PTMfitter.exe")) { // use PTM fitter
                        IJ.error("Macro code to execute PTMfitter not yet complete. Try HSHfitter.");
                        //exit ("Macro code to execute PTMfitter not yet complete. Try HSHfitter."); // @@@
                } 
                else {
                        IJ.error("Problem identifying type of RTI fitter");
                        //exit("Problem identifying type of RTI fitter");
                }
        }
        
        public void createLpFile(String colorProcess) throws IOException{//create lp file with filenames from newly created series and light positions from previously generated lp file
            List<String> listOfLpFiles_list;
            String[] listOfLpFiles;
            String[] lpLines;
            File[] list;
            File folder;
            if (lpSource.equals("")) {
                    listOfLpFiles_list = new ArrayList<String>();
                    folder = new File(projectDirectory+"LightPositionData"+File.separator);
                    list = folder.listFiles();
                    for (File list1 : list) {
                        if (list1.getName().endsWith("lp")) {
                            listOfLpFiles_list.add(list1.toString());
                        }
                    }
                    folder = new File(projectDirectory+"LightPositionData"+File.separator+"assembly-files"+File.separator);
                    list = folder.listFiles();
                    for (int i=0; i<list.length; i++) {
                            if (list[i].getName().endsWith("OriginalSet.lp")) { //ignore this one
                            
                            } 
                            else if (list[i].getName().endsWith("lp")) {
                                    listOfLpFiles_list.add(projectDirectory+"LightPositionData"+File.separator+"assembly-files"+File.separator+list[i].toString());
                            }
                    }
                    //For AddRadioButtonGroup method below, this must be a simple String[], not a list.
                    listOfLpFiles = new String[listOfLpFiles_list.size()];
                    listOfLpFiles_list.toArray(listOfLpFiles);
                    if (listOfLpFiles_list .size() == 1) 	{
                            lpSource = listOfLpFiles_list .get(0);
                    } else if (listOfLpFiles_list.isEmpty()) {
                            OpenDialog dialog = new OpenDialog("Locate Light Position Directory"); 
                            lpSource = dialog.getPath();
                    } else {
                            GenericDialog dialog = new GenericDialog("Select Light Position Source File"); 
                            dialog.addMessage("Select Light Position Source File");
                            dialog.addRadioButtonGroup("File: ", listOfLpFiles, listOfLpFiles_list.size(), 1, listOfLpFiles_list.get(0));
                            dialog.showDialog();
                            lpSource = dialog.getRadioButtonGroups().get(0).toString();
                    }
            }
            File lpFile = new File(lpSource);
            BufferedReader lpFileReader = Files.newBufferedReader(lpFile.toPath());
            String line= "";
            String lpFileAsText = "";
            while((line=lpFileReader.readLine()) != null){
                lpFileAsText += line;
            }
            lpFileReader.close();
            lpLines = lpFileAsText.split("\n");//split(File.openAsString(lpSource),"\n");
            noClobber(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp");
            Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp"), lpLines[0].getBytes(), StandardOpenOption.APPEND);
            String newLpLine = "";
            for (int i=1;i<lpLines.length;i++) {
                    newLpLine = lpLines[i];
                    newLpLine = newLpLine.replace("\\", "/"); //simplest to avoid a backslash on the right side of a regular expression replace in the next few lines
                    String funnyProjectDirectory = projectDirectory.replace("\\","/");
                    newLpLine = newLpLine.replace("LightPositionData/jpeg-exports/",colorProcess+"RTI/"+colorProcess+"_");
                    newLpLine = newLpLine.replace("canonical",funnyProjectDirectory+colorProcess+"RTI/"+colorProcess+"_"+projectName+"_RTI");
                    newLpLine = newLpLine.replace("/",File.separator);
                    Files.write(Paths.get(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp"), newLpLine.getBytes(), StandardOpenOption.APPEND);
            }
    }
        
        //End SRTI functions
        
        private boolean testCode() {
            System.out.println("Show the D");
            GenericDialog gd = new GenericDialog("Spectral RTI First Dialog");
            // default value is 0.00, 2 digits right of the decimal point
            logService.log().info("show dialog function...");
            gd.addNumericField("value", 0.00, 2);
            gd.addStringField("name", "John");
            gd.addMessage("Welcome to the inputing thing!");
            gd.showDialog();
            logService.log().info("Dialog now showing...");
            if (gd.wasCanceled()){
                    logService.log().info("User cancelled dialog");
                    return false;
            }
            // get entered values
            value = gd.getNextNumber();
            name = gd.getNextString();

            logService.log().info("Return true from showDialog()");
            return true;
	}

	public void showAbout() {
            ui.showDialog("SpectralRTI_Toolkit:  A process for putting spectral filters on RTI images.");
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
                ImageJ IJinstance = new ImageJ();
		IJinstance.ui().showUI();
		// run the plugin
		IJinstance.command().run(SpectralRTI_Toolkit.class, false);
                System.out.println("I am done WORLD!");
	}
}
