/*
Title: Spectral RTI Toolkit
Version: 0.1.20180601
Date: June 1, 2018 (small correction since April 24, 2017)
Author: Todd R. Hanneken, thanneken@stmarytx.edu
Description: A toolkit for processing Spectral RTI images
About:
See http://palimpsest.stmarytx.edu/integrating
*/
var jpegQuality = 100; //maximize quality for non-distribution phases
var jpegQualityWebRTI = 100; //lower for final distribution
var ramWebRTI = 8192;
var brightnessAdjustOption = "";
var brightnessAdjustApply = "";
var transmissiveSource= ""; //(thanks Kathryn!)
	var normX;
	var normY;
	var normWidth;
	var normHeight;
	var normalizationFixedValue;
	var pcaX = 0;
	var pcaY = 0;
	var pcaWidth = 0;
	var pcaHeight = 0;
	var lpSource = "";
	var projectDirectory = "";
	var projectName = "";
	var listOfRakingDirections;
	var accurateColorSource = "";
function createJp2(inFile) {
	preferredCompress = List.get("preferredCompress");
	preferredJp2Args = List.get("preferredJp2Args");
	if (preferredCompress ==""){
		preferredCompress = File.openDialog("Locate kdu_compress or ojp_compress");
		File.append("preferredCompress="+preferredCompress,"SpectralRTI_Toolkit-prefs.txt");
	}
	if (preferredJp2Args == "") {
		Dialog.create("Approve arguments for Jpeg 2000 compression");
		Dialog.addString("Arguments:","-rate -,2.4,1.48331273,.91673033,.56657224,.35016049,.21641118,.13374944,.08266171 Creversible\=no Clevels\=5 Stiles\=\{1024,1024\} Cblk\=\{64,64\} Cuse_sop\=yes Cuse_eph\=yes Corder\=RPCL ORGgen_plt\=yes ORGtparts\=R Cmodes\=BYPASS -double_buffering 10 -num_threads 4 -no_weights",80);
		// removed "-jp2_space sRGB" because causes failure on single channel images and creates identical images for 3-channel
		preferredJp2Args = Dialog.getString();
		File.append("preferredJp2Args="+preferredJp2Args,"SpectralRTI_Toolkit-prefs.txt");
	}
	noClobber(projectDirectory+"StaticRaking"+File.separator+inFile+".jp2");
	print("Executing command "+preferredCompress+" -i "+projectDirectory+"StaticRaking"+File.separator+inFile+".tiff -o " +projectDirectory+"StaticRaking"+File.separator+inFile+".jp2 "+preferredJp2Args+"\n");
	exec(preferredCompress+" -i "+projectDirectory+"StaticRaking"+File.separator+inFile+".tiff -o " +projectDirectory+"StaticRaking"+File.separator+inFile+".jp2 "+preferredJp2Args+"\n");
}
function runFitter(colorProcess) { //identify preferred fitter and exec with arguments
	preferredFitter = List.get("preferredFitter");
	if (preferredFitter == "") {
		preferredFitter = File.openDialog("Locate Preferred RTI Fitter or cmd file for batch processing");
		File.append("preferredFitter="+preferredFitter,"SpectralRTI_Toolkit-prefs.txt");
	}
	if (endsWith(preferredFitter,"hshfitter.exe")) { // use HSH fitter
		hshOrder = List.get("hshOrder");
		if (hshOrder < 2 ) hshOrder = 3;
		hshThreads = List.get("hshThreads");
		if (hshThreads < 1 ) hshThreads = 16;
		print("Executing command "+preferredFitter+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti\nThis could take a while...");
		File.append("Brightness Adjust Option: "+brightnessAdjustOption,projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt");
		if (brightnessAdjustOption == "Yes, by normalizing each image to a selected area") {
			File.append("Normalization area bounds: "+normX+", "+normY+", "+normWidth+", "+normHeight,projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt");
		} else if (brightnessAdjustOption == "Yes, by multiplying all images by a fixed value") {
			File.append("Normalization fixed value: "+normalizationFixedValue,projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt");
		}
		if (pcaX > 0) {
			File.append("PCA area bounds: "+pcaX+", "+pcaY+", "+pcaWidth+", "+pcaHeight,projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt");
		}
		File.append("Jpeg Quality: "+jpegQuality+" (edit SpectralRTI_Toolkit-prefs.txt to change)",projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt");
		File.append("Executing command "+preferredFitter+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti",projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt");
		fitterOutput = exec(preferredFitter+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti");
		print(fitterOutput);
		File.append(fitterOutput,projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt");
		if (webRtiDesired) {
			webRtiMaker = List.get("webRtiMaker");
			if (webRtiMaker == "") {
				webRtiMaker = File.openDialog("Locate webGLRTIMaker.exe");
				File.append("webRtiMaker="+webRtiMaker,"SpectralRTI_Toolkit-prefs.txt");
			}
			webRtiMakerOutput = exec(webRtiMaker+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti -q "+jpegQualityWebRTI+" -r "+ramWebRTI);
			print(webRtiMakerOutput);
			File.append("<html lang=\"en\" xml:lang=\"en\"> <head> <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /> <title>WebRTI "+projectName+"_"+colorProcess+"</title> <link type=\"text/css\" href=\"css/ui-lightness/jquery-ui-1.10.3.custom.css\" rel=\"Stylesheet\"> <link type=\"text/css\" href=\"css/webrtiviewer.css\" rel=\"Stylesheet\"> <script type=\"text/javascript\" src=\"js/jquery.js\"></script> <script type=\"text/javascript\" src=\"js/jquery-ui.js\"></script> <script type=\"text/javascript\" src=\"spidergl/spidergl_min.js\"></script> <script type=\"text/javascript\" src=\"spidergl/multires_min.js\"></script> </head> <body> <div id=\"viewerContainer\"> <script  type=\"text/javascript\"> createRtiViewer(\"viewerContainer\", \""+projectName+"_"+colorProcess+"RTI_"+startTime+"\", $(\"body\").width(), $(\"body\").height()); </script> </div> </body> </html>",projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+"_wrti.html");
		}
	} else if (endsWith(preferredFitter,"cmd")||endsWith(preferredFitter,"bash")) {
		hshOrder = List.get("hshOrder");
		if (hshOrder < 2 ) hshOrder = 3;
		hshThreads = List.get("hshThreads");
		if (hshThreads < 1 ) hshThreads = 16;
		print("Adding command to batch command file "+preferredFitter+": hshfitter "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti\n");
		File.append("Brightness Adjust Option: "+brightnessAdjustOption,projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt");
		if (brightnessAdjustOption == "Yes, by normalizing each image to a selected area") {
			File.append("Normalization area bounds: "+normX+", "+normY+", "+normWidth+", "+normHeight,projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt");
		} else if (brightnessAdjustOption == "Yes, by multiplying all images by a fixed value") {
			File.append("Normalization fixed value: "+normalizationFixedValue,projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt");
		}
		if (pcaX > 0) {
			File.append("PCA area bounds: "+pcaX+", "+pcaY+", "+pcaWidth+", "+pcaHeight,projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt");
		}
		File.append("Jpeg Quality: "+jpegQuality+" (edit SpectralRTI_Toolkit-prefs.txt to change)",projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt");
		File.append("Executing command "+preferredFitter+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti",projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".txt");
		File.append("hshfitter "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti",preferredFitter);
		File.append("webGLRTIMaker "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+".rti -q "+jpegQualityWebRTI+" -r "+ramWebRTI,preferredFitter);
		if (webRtiDesired) {
			File.append("<html lang=\"en\" xml:lang=\"en\"> <head> <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /> <title>WebRTI "+projectName+"_"+colorProcess+"</title> <link type=\"text/css\" href=\"css/ui-lightness/jquery-ui-1.10.3.custom.css\" rel=\"Stylesheet\"> <link type=\"text/css\" href=\"css/webrtiviewer.css\" rel=\"Stylesheet\"> <script type=\"text/javascript\" src=\"js/jquery.js\"></script> <script type=\"text/javascript\" src=\"js/jquery-ui.js\"></script> <script type=\"text/javascript\" src=\"spidergl/spidergl_min.js\"></script> <script type=\"text/javascript\" src=\"spidergl/multires_min.js\"></script> </head> <body> <div id=\"viewerContainer\"> <script  type=\"text/javascript\"> createRtiViewer(\"viewerContainer\", \""+projectName+"_"+colorProcess+"RTI_"+startTime+"\", $(\"body\").width(), $(\"body\").height()); </script> </div> </body> </html>",projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI_"+startTime+"_wrti.html");
		}
	} else if (endsWith(preferredFitter,"PTMfitter.exe")) { // use PTM fitter
			exit("Macro code to execute PTMfitter not yet complete. Try HSHfitter."); // @@@
	} else {
		exit("Problem identifying type of RTI fitter");
	}
}
function createLpFile(colorProcess) { //create lp file with filenames from newly created series and light positions from previously generated lp file
	if (lpSource == "") {
		listOfLpFiles = newArray(0);
		list = getFileList(projectDirectory+"LightPositionData"+File.separator);
		for (i=0; i<list.length; i++) {
			if (endsWith(list[i], "lp")) listOfLpFiles = Array.concat(listOfLpFiles,projectDirectory+"LightPositionData"+File.separator+list[i]);
		}
		list = getFileList(projectDirectory+"LightPositionData"+File.separator+"assembly-files"+File.separator);
		for (i=0; i<list.length; i++) {
			if (endsWith(list[i],"OriginalSet.lp")) { //ignore this one
			} else if (endsWith(list[i], "lp")) {
				listOfLpFiles = Array.concat(listOfLpFiles,projectDirectory+"LightPositionData"+File.separator+"assembly-files"+File.separator+list[i]);
			}
		}
		if (listOfLpFiles.length == 1) 	{
			lpSource = listOfLpFiles[0];
		} else if (listOfLpFiles.length == 0) {
			lpSource = File.openDialog("Locate Light Position File");
		} else {
			Dialog.create("Select Light Position Source File");
			Dialog.addMessage("Select Light Position Source File");
			Dialog.addRadioButtonGroup("File: ", listOfLpFiles, listOfLpFiles.length, 1, listOfLpFiles[0]);
			Dialog.show();
			lpSource = Dialog.getRadioButton();
		}
	}
	lpLines = split(File.openAsString(lpSource),"\n");
	noClobber(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp");
	File.append(lpLines[0],projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp");
	for (i=1;i<lpLines.length;i++) {
		newLpLine = lpLines[i];
		newLpLine = replace(newLpLine,"\\","/"); //simplest to avoid a backslash on the right side of a regular expression replace in the next few lines
		funnyProjectDirectory = replace(projectDirectory,"\\","/");
		newLpLine = replace(newLpLine,"LightPositionData/jpeg-exports/",colorProcess+"RTI/"+colorProcess+"_");
		newLpLine = replace(newLpLine,"canonical",funnyProjectDirectory+colorProcess+"RTI/"+colorProcess+"_"+projectName+"_RTI");
		newLpLine = replace(newLpLine,"/",File.separator);
		File.append(newLpLine,projectDirectory+colorProcess+"RTI"+File.separator+projectName+"_"+colorProcess+"RTI.lp");
	}
}
function promptBrightnessAdjust() {
	open(projectDirectory+"Captures-Hemisphere-Gamma"+File.separator+listOfHemisphereCaptures[round(listOfHemisphereCaptures.length/2)]);
	rename("Preview");
	setBatchMode("show");
	brightnessAdjustOptions = newArray("No","Yes, by normalizing each image to a selected area","Yes, by multiplying all images by a fixed value");
	brightnessAdjustApplies = newArray("Static raking images only (recommended)","RTI images also");
	Dialog.create("Adjust brightness of hemisphere captures?");
	Dialog.addRadioButtonGroup("Adjust brightness of hemisphere captures? ", brightnessAdjustOptions, brightnessAdjustOptions.length, 1, brightnessAdjustOptions[1]);
	Dialog.addRadioButtonGroup("Apply adjustment to which output images? ",brightnessAdjustApplies,brightnessAdjustApplies.length,1,brightnessAdjustApplies[0]);
	Dialog.show();
	brightnessAdjustOption = Dialog.getRadioButton();
	brightnessAdjustApply = Dialog.getRadioButton();
	if (brightnessAdjustOption == "Yes, by normalizing each image to a selected area") {
		waitForUser("Select area", "Draw a rectangle containing the brighest white and darkest black desired then press OK\n(hint: use a large area including spectralon and the object, excluding glare)");
		getSelectionBounds(normX, normY, normWidth, normHeight);
	} else if (brightnessAdjustOption == "Yes, by multiplying all images by a fixed value") {
		waitForUser("Choose multiplier", "Use the Muliply dialog to preview and choose a multiplier value.\nThis is just a preview image; the chosen value will be entered next.");
		run("Multiply...");
		Dialog.create("Enter selected multiplier");
		Dialog.addNumber("Enter selected multiplier: ", 1.30,2,4,"");
		Dialog.show();
		normalizationFixedValue = Dialog.getNumber();
	}
	selectWindow("Preview");
	run("Close");
}
function noClobber(safeName) {
	if (File.exists(safeName)) {
		verboseDate = File.dateLastModified(safeName);
		verboseDate = replace(verboseDate," ","_");
		verboseDate = replace(verboseDate,"\:","-");
		newFileName = replace(safeName,"\\.","("+verboseDate+").");
		success = File.rename(safeName,newFileName);
	}
}
function timestamp() {
	getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
	year = toString(year);
	if (month < 9) month = "0" + toString(++month);
		else month = toString(++month);
	if (dayOfMonth < 10) dayOfMonth = "0" + toString(dayOfMonth);
		else dayOfMonth = toString(dayOfMonth);
	if (hour<10) hour = "0"+toString(hour);
		else hour = toString(hour);
	if (minute<10) minute = "0"+toString(minute);
		else minute = toString(minute);
	return year+month+dayOfMonth+"_"+hour+minute;
}
macro "Spectral RTI [n1]" {
	setBatchMode(true);
	//want these variables to be accessible across functions and to reset each time the macro is run
	startTime = timestamp();
	brightnessAdjustOption = "";
	brightnessAdjustApply = "";
	pcaX = 0;
	pcaY = 0;
	pcaWidth = 0;
	pcaHeight = 0;
	accurateColorSource = "";
	//consult with user about values stored in prefs file
	prefsConsult = newArray();
	Dialog.create("Consult Preferences");
	Dialog.addMessage("The following settings are remembered from the configuration file or a previous run.\nEdit or clear as desired.");
	if (File.exists("SpectralRTI_Toolkit-prefs.txt")) {
		prefsFile = File.openAsString("SpectralRTI_Toolkit-prefs.txt");
		prefs = split(prefsFile,"\n");
		for (i=0;i<prefs.length;i++) {
			var key = substring(prefs[i],0,indexOf(prefs[i],"="));
			key = replace(key,"preferredCompress","JP2 Compressor");
			key = replace(key,"preferredJp2Args","JP2 Arguments");
			key = replace(key,"preferredFitter","HSH Fitter");
			key = replace(key,"jpegQuality","JPEG Quality");
			key = replace(key,"hshOrder","HSH Order");
			key = replace(key,"hshThreads","HSH Threads");
			var value = substring(prefs[i],indexOf(prefs[i],"=")+1);
			Dialog.addString(key, value, 80);
			prefsConsult = Array.concat(prefsConsult,key);
		}
	}
	Dialog.show();
	for (i=0; i<prefsConsult.length;i++) {
		var key = prefsConsult[i];
		key = replace(key,"JP2 Compressor","preferredCompress");
		key = replace(key,"JP2 Arguments","preferredJp2Args");
		key = replace(key,"HSH Fitter","preferredFitter");
		key = replace(key,"JPEG Quality","jpegQuality");
		key = replace(key,"HSH Order","hshOrder");
		key = replace(key,"HSH Threads","hshThreads");
		var value = Dialog.getString();
		List.set(key,value);
	}
	jpegQuality = call("ij.plugin.JpegWriter.getQuality");
	if (List.get("jpegQuality") > 0) jpegQuality = List.get("jpegQuality");
	run("Input/Output...","jpeg="+jpegQuality);
	projectDirectory = getDirectory("Choose a Directory");
	projectDirectory = replace(projectDirectory,"\\",File.separator);
	projectName = File.getName(projectDirectory);
	if (!File.exists(projectDirectory+"Captures-Hemisphere-Gamma"+File.separator)) {
		File.makeDirectory(projectDirectory+"Captures-Hemisphere-Gamma"+File.separator);
		print("A directory has been created for the Hemisphere Captures at "+projectDirectory+"Captures-Hemisphere-Gamma"+File.separator);
	}
	listOfHemisphereCaptures = getFileList(projectDirectory+"Captures-Hemisphere-Gamma"+File.separator);
	while (listOfHemisphereCaptures.length < 1) { // changed from 29 to 1 6/1/2018
		showMessageWithCancel("Please Populate Hemisphere Captures","The software expects at least 1 image in HemisphereCaptures folder.\nPlease populate the folder and press Ok to continue, or cancel.");
		listOfHemisphereCaptures = getFileList(projectDirectory+"Captures-Hemisphere-Gamma"+File.separator);
	}
	lpDesired=true;
	acRtiDesired=true;
	xsRtiDesired=true;
	psRtiDesired=true;
	if (File.exists(projectDirectory+"LightPositionData"+File.separator)) lpDesired = false;
	listOfAccurateColorFiles = getFileList(projectDirectory+"AccurateColor"+File.separator);
	if (listOfAccurateColorFiles.length<1) acRtiDesired = false;
	if (File.exists(projectDirectory+"AccurateColorRTI"+File.separator)) acRtiDesired = false;
	listOfNarrowbandCaptures = getFileList(projectDirectory+"Captures-Narrowband-NoGamma"+File.separator);
	if (listOfNarrowbandCaptures.length<9) {
		xsRtiDesired=false;
		psRtiDesired=false;
	}
	if (File.exists(projectDirectory+"PseudocolorRTI"+File.separator)) psRtiDesired = false;
	if (File.exists(projectDirectory+"ExtendedSpectrumRTI"+File.separator)) xsRtiDesired = false;
	Dialog.create("Select Tasks");
	Dialog.addMessage("Select the tasks you would like to complete");
	Dialog.addCheckbox("Light Position Data",lpDesired);
	Dialog.addCheckbox("Accurate ColorRTI",acRtiDesired);
	Dialog.addCheckbox("Accurate Color Static Raking",acRtiDesired);
	Dialog.addCheckbox("Extended Spectrum RTI",xsRtiDesired);
	Dialog.addCheckbox("Extended Spectrum Static Raking",xsRtiDesired);
	Dialog.addCheckbox("Pseudocolor RTI",psRtiDesired);
	Dialog.addCheckbox("Pseudocolor Static Raking",psRtiDesired);
	Dialog.addCheckbox("Custom RTI",false);
	Dialog.addCheckbox("Custom Static Raking",false);
	Dialog.addCheckbox("WebRTI",true);
	Dialog.show();
	lpDesired = Dialog.getCheckbox();
	acRtiDesired = Dialog.getCheckbox();
	acRakingDesired = Dialog.getCheckbox();
	xsRtiDesired = Dialog.getCheckbox();
	xsRakingDesired = Dialog.getCheckbox();
	psRtiDesired = Dialog.getCheckbox();
	psRakingDesired = Dialog.getCheckbox();
	csRtiDesired = Dialog.getCheckbox();
	csRakingDesired = Dialog.getCheckbox();
	webRtiDesired = Dialog.getCheckbox();
	//identify angles for uncompressed static raking
	if (acRakingDesired || acRtiDesired || xsRtiDesired || xsRakingDesired || psRtiDesired || psRakingDesired || csRtiDesired || csRakingDesired){
		if (brightnessAdjustOption == "") promptBrightnessAdjust();
	}
	if (acRakingDesired || xsRakingDesired || psRakingDesired || csRakingDesired){
		if (!File.exists(projectDirectory+"StaticRaking"+File.separator)) {
			File.makeDirectory(projectDirectory+"StaticRaking"+File.separator);
			print("A directory has been created for lossless static raking images at "+projectDirectory+"StaticRaking"+File.separator);
		}
		listOfTransmissiveSources = getFileList(projectDirectory+"Captures-Transmissive-Gamma"+File.separator);
		if (listOfTransmissiveSources.length == 1) 	{ // no opt out of creating a transmissive static if transmissive folder is populated, but not a problem
			transmissiveSource = listOfTransmissiveSources[0];
		} else if (listOfTransmissiveSources.length > 1) {
			Dialog.create("Select Transmissive Source");
			Dialog.addMessage("Select Transmissive Source");
			Dialog.addRadioButtonGroup("File: ", listOfTransmissiveSources, listOfTransmissiveSources.length, 1, listOfTransmissiveSources[0]);
			Dialog.show();
			transmissiveSource = Dialog.getRadioButton();
		} else if (listOfTransmissiveSources.length == 0) {
			transmissiveSource = "";
		}
		defaults = newArray(listOfHemisphereCaptures.length);
		Dialog.create("Select light positions");
		Dialog.addMessage("Select light positions for lossless static raking images");
		Dialog.addCheckboxGroup(1+floor(listOfHemisphereCaptures.length/4), 4, listOfHemisphereCaptures, defaults); //8 columns
		Dialog.show();
		for(i=0;i<listOfHemisphereCaptures.length;i++) {
			listOfRakingDirections = Array.concat(listOfRakingDirections,Dialog.getCheckbox());
		}
	} else {
		listOfRakingDirections = newArray(listOfHemisphereCaptures.length+1);
	}
	if (xsRtiDesired || xsRakingDesired) { // only interaction here, processing later
		//create a dialog suggesting and confirming which narrowband captures to use for R,G,and B
		rgbnOptions = newArray("R","G","B","none");
		redNarrowbands = newArray(0);
		greenNarrowbands = newArray(0);
		blueNarrowbands = newArray(0);
		Dialog.create("Assign Narrowband Captures");
		Dialog.addMessage("Assign each narrowband capture to the visible range of R, G, B, or none");
		for (i=0; i<listOfNarrowbandCaptures.length; i++) {
			if ((i+1)/listOfNarrowbandCaptures.length < 0.34) defaultRange = "B";
			else if ((i+1)/listOfNarrowbandCaptures.length > 0.67) defaultRange = "R";
			else defaultRange = "G";
			Dialog.setInsets(0,0,0);
			Dialog.addRadioButtonGroup(listOfNarrowbandCaptures[i], rgbnOptions, 1, 4, defaultRange);
		} // @@@ problem here if runs off screen... no an option to use two columns
		Dialog.show();
		for (i=0; i<listOfNarrowbandCaptures.length; i++) {
			rangeChoice = Dialog.getRadioButton();
			if (rangeChoice == "R") {
				redNarrowbands = Array.concat(redNarrowbands, listOfNarrowbandCaptures[i]);
			} else if (rangeChoice == "G") {
				greenNarrowbands = Array.concat(greenNarrowbands, listOfNarrowbandCaptures[i]);
			} else if (rangeChoice == "B") {
				blueNarrowbands = Array.concat(blueNarrowbands, listOfNarrowbandCaptures[i]);
			}
		}
		if (pcaHeight <100) {
			open(projectDirectory+"Captures-Narrowband-NoGamma"+File.separator+listOfNarrowbandCaptures[round(listOfNarrowbandCaptures.length/2)]);
			rename("Preview");
			setBatchMode("show");
			waitForUser("Select area", "Draw a rectangle containing the colors of interest for PCA\n(hint: limit to object or smaller)");
			getSelectionBounds(pcaX, pcaY, pcaWidth, pcaHeight);
			selectWindow("Preview");
			run("Close");
		}
	}
	if (psRtiDesired || psRakingDesired) {// only interaction here, processing later
			//identify 2 source images for pca pseudocolor
			listOfPseudocolorSources = getFileList(projectDirectory+"PCA"+File.separator);
			if (listOfPseudocolorSources.length > 1) defaultPca = "Open pregenerated images" ;
			else defaultPca = "Generate and select using defaults";
			listOfPcaMethods = newArray("Generate and select using defaults","Generate and manually select two","Open pregenerated images");
			Dialog.create("Select sources for Pseudocolor");
			Dialog.addMessage("Pseudocolor images require two source images (typically principal component images).");
			Dialog.addRadioButtonGroup("Method: ",listOfPcaMethods,listOfPcaMethods.length,1,defaultPca);
			Dialog.show();
			pcaMethod = Dialog.getRadioButton();
			if (pcaHeight <100) {
				open(projectDirectory+"Captures-Narrowband-NoGamma"+File.separator+listOfNarrowbandCaptures[round(listOfNarrowbandCaptures.length/2)]);
				rename("Preview");
				setBatchMode("show");
				waitForUser("Select area", "Draw a rectangle containing the colors of interest for PCA\n(hint: limit to object or smaller)");
				getSelectionBounds(pcaX, pcaY, pcaWidth, pcaHeight);
				selectWindow("Preview");
				run("Close");
			}
	}
	if (csRtiDesired || csRakingDesired) { //interaction phase
		csSource = File.openDialog("Choose a Source for Custom Process");
	}
	//create base lp file
	if (lpDesired) {
		open(projectDirectory+"Captures-Hemisphere-Gamma"+File.separator+listOfHemisphereCaptures[20]); // twentieth image likely to be well lit
		rename("Preview");
		setBatchMode("show");
		waitForUser("Select ROI", "Draw a rectangle loosely around a reflective hemisphere and press Ok");
		getSelectionBounds(x, y, width, height);
		selectWindow("Preview");
		run("Close");
//		ballArea=false;
		for(i=0;i<listOfHemisphereCaptures.length;i++) {
			if (endsWith(listOfHemisphereCaptures[i],"tif")) {
				open(projectDirectory+"Captures-Hemisphere-Gamma"+File.separator+listOfHemisphereCaptures[i]);
				rename("LightPosition");
				makeRectangle(x,y,width,height);
				run("Crop");
				if (!File.exists(projectDirectory+"LightPositionData"+File.separator)) File.makeDirectory(projectDirectory+"LightPositionData"+File.separator);
				if (!File.exists(projectDirectory+"LightPositionData"+File.separator+"jpeg-exports"+File.separator)) File.makeDirectory(projectDirectory+"LightPositionData"+File.separator+"jpeg-exports");
				saveAs("jpeg",projectDirectory+"LightPositionData"+File.separator+"jpeg-exports"+File.separator+File.nameWithoutExtension+".jpg");
				selectWindow("LightPosition");
				run("Close");
			}
		}
		showMessageWithCancel("Use RTI Builder to Create LP File","Please use RTI Builder to create an LP file based on the reflective hemisphere detail images in\n"+projectDirectory+"LightPositionData"+File.separator+"\nPress cancel to discontinue Spectral RTI Toolkit or Ok to continue with other tasks after the lp file has been created.");
	}
	//create Accurate Color RTI
	if (acRtiDesired) {
		//create series of images with luminance from hemisphere captures and chrominance from color image
		if (!File.exists(projectDirectory+"AccurateColorRTI"+File.separator)) {
			File.makeDirectory(projectDirectory+"AccurateColorRTI"+File.separator);
			print("A directory has been created for Accurate Color RTI at "+projectDirectory+"AccurateColorRTI"+File.separator);
		}
		//integration
		listOfAccurateColorSources = getFileList(projectDirectory+"AccurateColor"+File.separator);
		if (listOfAccurateColorSources.length == 1) 	{
			accurateColorSource = listOfAccurateColorSources[0];
		} else if (listOfAccurateColorSources.length == 0) {
			exit("Need at least one color image file in "+projectDirectory+"AccurateColorRTI"+File.separator);
		} else {
			for (i=0; i<listOfAccurateColorSources.length; i++) {
				if (indexOf(listOfAccurateColorSources[i],"sRGB")>0) accurateColorSource = listOfAccurateColorSources[i];
			}
			if (accurateColorSource == "") {
				Dialog.create("Select Color Source");
				Dialog.addMessage("Select Color Source");
				Dialog.addRadioButtonGroup("File: ", listOfAccurateColorSources, listOfAccurateColorSources.length, 1, listOfAccurateColorSources[0]);
				Dialog.show();
				accurateColorSource = Dialog.getRadioButton();
			}
		}
		open(projectDirectory+"AccurateColor"+File.separator+ accurateColorSource);
		rename("RGBtiff");
		if (bitDepth() == 8) {
			run("RGB Color");
			selectWindow("RGBtiff");
			run("Close");
			rename("RGBtiff");
		}
		run("RGB to YCbCr stack");
		run("Stack to Images");
		selectWindow("Y");
		run("Close");
		selectWindow("RGBtiff");
		run("Close");
		//Luminance from hemisphere captures
		for(i=0;i<listOfHemisphereCaptures.length;i++) {
			if (endsWith(listOfHemisphereCaptures[i],"tif")) { //@@@ better to trim list at the beginning so that array.length can be used in lp file
				open(projectDirectory+"Captures-Hemisphere-Gamma"+File.separator+listOfHemisphereCaptures[i]);
				rename("Luminance");
				// it would be better to crop early in the process, especially before reducing to 8-bit and jpeg compression
				// normalize
				if (brightnessAdjustApply == "RTI images also") {
					if (brightnessAdjustOption == "Yes, by normalizing each image to a selected area") {
						makeRectangle(normX, normY, normWidth, normHeight);
						run("Enhance Contrast...", "saturated=0.4");
						run("Select None");
					} else if (brightnessAdjustOption == "Yes, by multiplying all images by a fixed value") {
						run("Multiply...", "value="+normalizationFixedValue+"");
					}
				}
				run("8-bit");
				run("Concatenate...", "  title=[YCC] keep image1=Luminance image2=Cb image3=Cr image4=[-- None --]");
				run("YCbCr stack to RGB");
				//Save as jpeg
				noClobber(projectDirectory+"AccurateColorRTI"+File.separator+"AccurateColor_"+File.nameWithoutExtension+".jpg");
				saveAs("jpeg", projectDirectory+"AccurateColorRTI"+File.separator+"AccurateColor_"+File.nameWithoutExtension+".jpg");
				//setBatchMode("show"); //debugging
				selectWindow(File.nameWithoutExtension+".jpg");
				run("Close");
				selectWindow("YCC");
				run("Close");
				selectWindow("Luminance");
				run("Close");
			}
		}
		selectWindow("Cb");
		run("Close");
		selectWindow("Cr");
		run("Close");
		createLpFile("AccurateColor");
		runFitter("AccurateColor");
	}
	if (acRakingDesired) {
		if (accurateColorSource == "") {
			listOfAccurateColorSources = getFileList(projectDirectory+"AccurateColor"+File.separator);
			if (listOfAccurateColorSources.length == 1) 	{
				accurateColorSource = listOfAccurateColorSources[0];
			} else if (listOfAccurateColorSources.length == 0) {
				exit("Need at least one color image file in "+projectDirectory+"AccurateColorRTI"+File.separator);
			} else {
				Dialog.create("Select Color Source");
				Dialog.addMessage("Select Color Source");
				Dialog.addRadioButtonGroup("File: ", listOfAccurateColorSources, listOfAccurateColorSources.length, 1, listOfAccurateColorSources[0]);
				Dialog.show();
				accurateColorSource = Dialog.getRadioButton();
			}
		}
		open(projectDirectory+"AccurateColor"+File.separator+ accurateColorSource);
		rename("RGBtiff");
		if (bitDepth() == 8) {
			run("RGB Color");
			selectWindow("RGBtiff");
			run("Close");
			rename("RGBtiff");
		}
		//create accurate color static diffuse
		noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_00"+".tiff");
		save(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_00"+".tiff");
		createJp2(projectName+"_Ac_00");
		File.delete(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_00"+".tiff");
		run("RGB to YCbCr stack");
		run("Stack to Images");
		selectWindow("Y");
		run("Close");
		selectWindow("RGBtiff");
		run("Close");
		//Luminance from transmissive
		if (transmissiveSource != "") {
			open(projectDirectory+"Captures-Transmissive-Gamma"+File.separator+transmissiveSource);
			rename("TransmissiveLuminance");
			run("8-bit");
			run("Concatenate...", "  title=[YCC] keep image1=TransmissiveLuminance image2=Cb image3=Cr image4=[-- None --]");
			run("YCbCr stack to RGB");
			noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_Tx.tiff");
			save(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_Tx.tiff");
			createJp2(projectName+"_Ac_Tx");
			File.delete(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_Tx.tiff");
			selectWindow("YCC - RGB");
			run("Close");
			selectWindow("YCC");
			run("Close");
			selectWindow("TransmissiveLuminance");
			run("Close");
		}
		//Luminance from hemisphere captures
		for(i=0;i<listOfHemisphereCaptures.length;i++) {
			if (endsWith(listOfHemisphereCaptures[i],"tif")) { //@@@ better to trim list at the beginning so that array.length can be used in lp file
				if (listOfRakingDirections[i+1]) {
					open(projectDirectory+"Captures-Hemisphere-Gamma"+File.separator+listOfHemisphereCaptures[i]);
					rename("Luminance");
					if (brightnessAdjustOption == "Yes, by normalizing each image to a selected area") {
						makeRectangle(normX, normY, normWidth, normHeight);
						run("Enhance Contrast...", "saturated=0.4");
						run("Select None");
					} else if (brightnessAdjustOption == "Yes, by multiplying all images by a fixed value") {
						run("Multiply...", "value="+normalizationFixedValue+"");
					}
					run("8-bit");
					run("Concatenate...", "  title=[YCC] keep image1=Luminance image2=Cb image3=Cr image4=[-- None --]");
					run("YCbCr stack to RGB");
					positionNumber = toString(IJ.pad(i+1, 2));
					noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_"+positionNumber+".tiff");
					save(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_"+positionNumber+".tiff");
					createJp2(projectName+"_Ac_"+positionNumber);
					File.delete(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ac_"+positionNumber+".tiff");
					selectWindow("YCC - RGB");
					run("Close");
					selectWindow("YCC");
					run("Close");
					selectWindow("Luminance");
					run("Close");
				}
			}
		}
		selectWindow("Cb");
		run("Close");
		selectWindow("Cr");
		run("Close");
	}
	//create Extended Spectrum RTI
	if (xsRtiDesired || xsRakingDesired) {
		//Red
		redStringList = redNarrowbands[0];  //might be an array to string function somewhere to do this more elegantly
		for (i=1;i<redNarrowbands.length;i++) {
			redStringList = redStringList+"|"+redNarrowbands[i];
		}
		run("Image Sequence...", "open="+projectDirectory+"Captures-Narrowband-NoGamma"+File.separator+" file=("+redStringList+") sort");
		rename("RedStack");
		makeRectangle(pcaX, pcaY, pcaWidth, pcaHeight);
		run("PCA ");
		selectWindow("Eigenvalue spectrum of RedStack");
		run("Close");
		selectWindow("RedStack");
		run("Close");
		selectWindow("PCA of RedStack");
		run("Slice Keeper", "first=1 last=1 increment=1");
		selectWindow("PCA of RedStack");
		run("Close");
		selectWindow("PCA of RedStack kept stack");
		rename("R");
		makeRectangle(pcaX, pcaY, pcaWidth, pcaHeight); // questionable
		run("Enhance Contrast...", "saturated=0.4 normalize");
		run("8-bit");
		//Green
		greenStringList = greenNarrowbands[0];  //might be an array to string function somewhere to do this more elegantly
		for (i=1;i<greenNarrowbands.length;i++) {
			greenStringList = greenStringList+"|"+greenNarrowbands[i];
		}
		run("Image Sequence...", "open="+projectDirectory+"Captures-Narrowband-NoGamma"+File.separator+" file=("+greenStringList+") sort");
		rename("GreenStack");
		makeRectangle(pcaX, pcaY, pcaWidth, pcaHeight);
		run("PCA ");
		selectWindow("Eigenvalue spectrum of GreenStack");
		run("Close");
		selectWindow("GreenStack");
		run("Close");
		selectWindow("PCA of GreenStack");
		run("Slice Keeper", "first=1 last=1 increment=1");
		selectWindow("PCA of GreenStack");
		run("Close");
		selectWindow("PCA of GreenStack kept stack");
		rename("G");
		makeRectangle(pcaX, pcaY, pcaWidth, pcaHeight); // questionable ... @@@maybe use contrast area... another function called if necessary (two more following)
		run("Enhance Contrast...", "saturated=0.4 normalize");
		run("8-bit");
		//Blue
		blueStringList = blueNarrowbands[0];  //might be an array to string function somewhere to do this more elegantly
		for (i=1;i<blueNarrowbands.length;i++) {
			blueStringList = blueStringList+"|"+blueNarrowbands[i];
		}
		run("Image Sequence...", "open="+projectDirectory+"Captures-Narrowband-NoGamma"+File.separator+" file=("+blueStringList+") sort");
		rename("BlueStack");
		makeRectangle(pcaX, pcaY, pcaWidth, pcaHeight);
		run("PCA ");
		selectWindow("Eigenvalue spectrum of BlueStack");
		run("Close");
		selectWindow("BlueStack");
		run("Close");
		selectWindow("PCA of BlueStack");
		run("Slice Keeper", "first=1 last=1 increment=1");
		selectWindow("PCA of BlueStack");
		run("Close");
		selectWindow("PCA of BlueStack kept stack");
		rename("B");
		makeRectangle(pcaX, pcaY, pcaWidth, pcaHeight); // questionable
		run("Enhance Contrast...", "saturated=0.4 normalize");
		run("8-bit");
		run("Concatenate...", "  title=[Stack] image1=R image2=G image3=B image4=[-- None --]");
		run("Stack to RGB");
		selectWindow("Stack");
		run("Close");
		selectWindow("Stack (RGB)");
		//create extended spectrum static diffuse
		if (xsRakingDesired) {
			noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_00"+".tiff");
			save(projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_00"+".tiff");
			createJp2(projectName+"_Xs_00");
			File.delete(projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_00"+".tiff");
		}
		run("RGB to YCbCr stack");
		run("Stack to Images");
		selectWindow("Y");
		run("Close");
		selectWindow("Stack (RGB)");
		run("Close");
		if (transmissiveSource != "") {
			open(projectDirectory+"Captures-Transmissive-Gamma"+File.separator+transmissiveSource);
			rename("TransmissiveLuminance");
			run("8-bit");
			run("Concatenate...", "  title=[YCC] keep image1=TransmissiveLuminance image2=Cb image3=Cr image4=[-- None --]");
			run("YCbCr stack to RGB");
			noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_Tx.tiff");
			save(projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_Tx.tiff");
			createJp2(projectName+"_Xs_Tx");
			File.delete(projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_Tx.tiff");
			selectWindow("YCC - RGB");
			run("Close");
			selectWindow("YCC");
			run("Close");
			selectWindow("TransmissiveLuminance");
			run("Close");
		}
		if (xsRtiDesired) {
			if (!File.exists(projectDirectory+"ExtendedSpectrumRTI"+File.separator)) {
				File.makeDirectory(projectDirectory+"ExtendedSpectrumRTI"+File.separator);
				print("A directory has been created for Extended Spectrum RTI at "+projectDirectory+"ExtendedSpectrumRTI"+File.separator);
			}
		}
		for(i=0;i<listOfHemisphereCaptures.length;i++) {
			if (xsRtiDesired) {
				open(projectDirectory+"Captures-Hemisphere-Gamma"+File.separator+listOfHemisphereCaptures[i]);
				rename("Luminance");
				if (brightnessAdjustApply == "RTI images also") {
					if (brightnessAdjustOption == "Yes, by normalizing each image to a selected area") {
						makeRectangle(normX, normY, normWidth, normHeight);
						run("Enhance Contrast...", "saturated=0.4");
						run("Select None");
					} else if (brightnessAdjustOption == "Yes, by multiplying all images by a fixed value") {
						run("Multiply...", "value="+normalizationFixedValue+"");
					}
				}
				run("8-bit");
				run("Concatenate...", "  title=[YCC] keep image1=Luminance image2=Cb image3=Cr image4=[-- None --]");
				run("YCbCr stack to RGB");
				noClobber(projectDirectory+"ExtendedSpectrumRTI"+File.separator+"ExtendedSpectrum_"+File.nameWithoutExtension+".jpg");
				saveAs("jpeg", projectDirectory+"ExtendedSpectrumRTI"+File.separator+"ExtendedSpectrum_"+File.nameWithoutExtension+".jpg");
				//setBatchMode("show"); //debugging
				selectWindow(File.nameWithoutExtension+".jpg");
				run("Close");
				selectWindow("YCC");
				run("Close");
				selectWindow("Luminance"); //possible to avoid a close and reopen in some circumstances but conditions are complicated
				run("Close");
			}
			if (xsRakingDesired) {
				if (listOfRakingDirections[i+1]) {
					open(projectDirectory+"Captures-Hemisphere-Gamma"+File.separator+listOfHemisphereCaptures[i]);
					rename("Luminance");
					if (brightnessAdjustOption == "Yes, by normalizing each image to a selected area") {
						makeRectangle(normX, normY, normWidth, normHeight);
						run("Enhance Contrast...", "saturated=0.4");
						run("Select None");
					} else if (brightnessAdjustOption == "Yes, by multiplying all images by a fixed value") {
						run("Multiply...", "value="+normalizationFixedValue+"");
					}
					run("8-bit");
					run("Concatenate...", "  title=[YCC] keep image1=Luminance image2=Cb image3=Cr image4=[-- None --]");
					run("YCbCr stack to RGB");
					positionNumber = toString(IJ.pad(i+1, 2));
					noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_"+positionNumber+".tiff");
					save(projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_"+positionNumber+".tiff");
					createJp2(projectName+"_Xs_"+positionNumber);
					File.delete(projectDirectory+"StaticRaking"+File.separator+projectName+"_Xs_"+positionNumber+".tiff");
					selectWindow("YCC - RGB");
					run("Close");
					selectWindow("YCC");
					run("Close");
					selectWindow("Luminance");
					run("Close");
				}
			}
		}
		selectWindow("Cb");
		run("Close");
		selectWindow("Cr");
		run("Close");
		run("Collect Garbage");
		if (xsRtiDesired) {
			createLpFile("ExtendedSpectrum");
			runFitter("ExtendedSpectrum");
		}
	}
	//create PseudocolorRTI
	if (psRtiDesired || psRakingDesired) {
		//option to create new ones based on narrowband captures and assumption that pc1 and pc2 are best
		if (pcaMethod == "Generate and select using defaults") {
			run("Image Sequence...", "open="+projectDirectory+"Captures-Narrowband-NoGamma"+File.separator+" sort");
			if (File.exists(projectDirectory+"Captures-Fluorescence-NoGamma"+File.separator)) {
				run("Image Sequence...", "open="+projectDirectory+"Captures-Fluorescence-NoGamma"+File.separator+" sort");
				run("Concatenate...", "  title=Captures-Narrowband-NoGamma image1=Captures-Narrowband-NoGamma image2=Captures-Fluorescence-NoGamma image3=[-- None --]");
			} else {
				rename("Captures-Narrowband-NoGamma");
			}
			makeRectangle(pcaX, pcaY, pcaWidth, pcaHeight);
			run("PCA ");
			selectWindow("Eigenvalue spectrum of Captures-Narrowband-NoGamma");
			run("Close");
			selectWindow("Captures-Narrowband-NoGamma");
			run("Close");
			selectWindow("PCA of Captures-Narrowband-NoGamma");
			run("Slice Keeper", "first=2 last=3 increment=1");
			selectWindow("PCA of Captures-Narrowband-NoGamma");
			run("Close");
			selectWindow("PCA of Captures-Narrowband-NoGamma kept stack");
			makeRectangle(pcaX, pcaY, pcaWidth, pcaHeight); // questionable
			run("Enhance Contrast...", "saturated=0.3 normalize update process_all");
			run("8-bit");
		//option to create new ones and manually select (close all but two)
		} else if (pcaMethod =="Generate and manually select two") {
			run("Image Sequence...", "open="+projectDirectory+"Captures-Narrowband-NoGamma"+File.separator+" sort");
			if (File.exists(projectDirectory+"Captures-Fluorescence-NoGamma"+File.separator)) {
				run("Image Sequence...", "open="+projectDirectory+"Captures-Fluorescence-NoGamma"+File.separator+" sort");
				run("Concatenate...", "  title=Captures-Narrowband-NoGamma image1=Captures-Narrowband-NoGamma image2=Captures-Fluorescence-NoGamma image3=[-- None --]");
			} else {
				rename("Captures-Narrowband-NoGamma");
			}
			makeRectangle(pcaX, pcaY, pcaWidth, pcaHeight);
			run("PCA ");
			selectWindow("Eigenvalue spectrum of Captures-Narrowband-NoGamma");
			run("Close");
			selectWindow("Captures-Narrowband-NoGamma");
			run("Close");
			selectWindow("PCA of Captures-Narrowband-NoGamma");
			makeRectangle(pcaX, pcaY, pcaWidth, pcaHeight);
			setBatchMode("show");
			waitForUser("Select area", "Delete slices from the stack until two remain\n(Hint: Image > Stacks > Delete Slice)\nEnhance contrast as desired\nThen press Ok");
			setBatchMode("hide");
			rename("PCA of Captures-Narrowband-NoGamma kept stack");
			run("8-bit");
		//option to use previously generated principal component images
		} else if (pcaMethod =="Open pregenerated images") {
			setBatchMode(false);
			waitForUser("Select area", "Open a pair of images or stack of two slices.\nEnhance contrast as desired\nThen press Ok");
			if (nImages() > 1) run("Images to Stack", "name=Stack title=[] use");
			setBatchMode(true);
			setBatchMode("hide");
			rename("PCA of Captures-Narrowband-NoGamma kept stack");
			run("8-bit");
		}
		//integrate pca pseudocolor with rti luminance
		//create static diffuse (not trivial... use median of all)
		if (psRakingDesired){
			run("Image Sequence...", "open="+projectDirectory+"Captures-Hemisphere-Gamma"+File.separator);
			run("Z Project...", "projection=Median");
			rename("Luminance");
			selectWindow("Captures-Hemisphere-Gamma");
			run("Close");
			selectWindow("Luminance");
			if (brightnessAdjustOption == "Yes, by normalizing each image to a selected area") {
				makeRectangle(normX, normY, normWidth, normHeight);
				run("Enhance Contrast...", "saturated=0.4");
				run("Select None");
			} else if (brightnessAdjustOption == "Yes, by multiplying all images by a fixed value") {
				run("Multiply...", "value="+normalizationFixedValue+"");
			}
			run("8-bit");
			run("Concatenate...", "  title=[YCC] keep image1=Luminance image2=[PCA of Captures-Narrowband-NoGamma kept stack] image3=[-- None --]");
			run("YCbCr stack to RGB");
			noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_00.tiff");
			save(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_00.tiff");
			createJp2(projectName+"_Ps_00");
			File.delete(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_00.tiff");
			selectWindow("YCC - RGB");
			run("Close");
			selectWindow("YCC");
			run("Close");
			selectWindow("Luminance");
			run("Close");
			if (transmissiveSource != "") {
				open(projectDirectory+"Captures-Transmissive-Gamma"+File.separator+transmissiveSource);
				rename("TransmissiveLuminance");
				run("8-bit");
				run("Concatenate...", "  title=[YCC] keep image1=TransmissiveLuminance image2=[PCA of Captures-Narrowband-NoGamma kept stack] image3=[-- None --]");
				run("YCbCr stack to RGB");
				noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_Tx.tiff");
				save(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_Tx.tiff");
				createJp2(projectName+"_Ps_Tx");
				File.delete(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_Tx.tiff");
				selectWindow("YCC - RGB");
				run("Close");
				selectWindow("YCC");
				run("Close");
				selectWindow("TransmissiveLuminance");
				run("Close");
			}
		}
		if (psRtiDesired) {
			if (!File.exists(projectDirectory+"PseudocolorRTI"+File.separator)) {
			File.makeDirectory(projectDirectory+"PseudocolorRTI"+File.separator);
			print("A directory has been created for Pseudocolor RTI at "+projectDirectory+"PseudocolorRTI"+File.separator);
			}
		}
		for(i=0;i<listOfHemisphereCaptures.length;i++) {
			if ((psRtiDesired)||(listOfRakingDirections[i+1])) {
				open(projectDirectory+"Captures-Hemisphere-Gamma"+File.separator+listOfHemisphereCaptures[i]);
				rename("Luminance");
				run("Duplicate...", "title=EnhancedLuminance");
				selectWindow("EnhancedLuminance");
				if (brightnessAdjustOption == "Yes, by normalizing each image to a selected area") {
					makeRectangle(normX, normY, normWidth, normHeight);
					run("Enhance Contrast...", "saturated=0.4");
					run("Select None");
				} else if (brightnessAdjustOption == "Yes, by multiplying all images by a fixed value") {
					run("Multiply...", "value="+normalizationFixedValue+"");
				}
				run("8-bit");
				selectWindow("Luminance");
				run("8-bit");
				if (listOfRakingDirections[i+1]) {
					run("Concatenate...", "  title=[YCC] keep image1=EnhancedLuminance image2=[PCA of Captures-Narrowband-NoGamma kept stack] image3=[-- None --]");
					run("YCbCr stack to RGB");
					selectWindow("YCC");
					run("Close");
					positionNumber = toString(IJ.pad(i+1, 2));
					noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_"+positionNumber+".tiff");
					save(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_"+positionNumber+".tiff");
					selectWindow("YCC - RGB");
					run("Close");
					createJp2(projectName+"_Ps_"+positionNumber);
					File.delete(projectDirectory+"StaticRaking"+File.separator+projectName+"_Ps_"+positionNumber+".tiff");
				}
				if ((psRtiDesired)&&(brightnessAdjustApply == "RTI images also")){
					run("Concatenate...", "  title=[YCC] keep image1=EnhancedLuminance image2=[PCA of Captures-Narrowband-NoGamma kept stack] image3=[-- None --]");
					run("YCbCr stack to RGB");
					selectWindow("YCC");
					run("Close");
					noClobber(projectDirectory+"PseudocolorRTI"+File.separator+"Pseudocolor_"+File.nameWithoutExtension+".jpg");
					saveAs("jpeg", projectDirectory+"PseudocolorRTI"+File.separator+"Pseudocolor_"+File.nameWithoutExtension+".jpg");
					//setBatchMode("show"); //debugging
					selectWindow(File.nameWithoutExtension+".jpg");
					run("Close");
				} else if (psRtiDesired) {
					run("Concatenate...", "  title=[YCC] keep image1=Luminance image2=[PCA of Captures-Narrowband-NoGamma kept stack] image3=[-- None --]");
					run("YCbCr stack to RGB");
					selectWindow("YCC");
					run("Close");
					noClobber(projectDirectory+"PseudocolorRTI"+File.separator+"Pseudocolor_"+File.nameWithoutExtension+".jpg");
					saveAs("jpeg", projectDirectory+"PseudocolorRTI"+File.separator+"Pseudocolor_"+File.nameWithoutExtension+".jpg");
					//setBatchMode("show"); //debugging
					selectWindow(File.nameWithoutExtension+".jpg");
					run("Close");
				}
				selectWindow("EnhancedLuminance");
				run("Close");
				selectWindow("Luminance");
				run("Close");
			}
		}
		selectWindow("PCA of Captures-Narrowband-NoGamma kept stack");
		run("Close");
		run("Collect Garbage");
		if (psRtiDesired) {
			createLpFile("Pseudocolor");
			runFitter("Pseudocolor");
		}
	}
	if (csRtiDesired || csRakingDesired) { //processing phase
		csSource = replace(csSource,"\\",File.separator);
		csParents = split(csSource,File.separator);
		csProcessName = csParents[csParents.length-2];
		if (!File.exists(projectDirectory+csProcessName+"RTI"+File.separator)) {
			File.makeDirectory(projectDirectory+csProcessName+"RTI"+File.separator);
			print("A directory has been created for "+csProcessName+" RTI at "+projectDirectory+csProcessName+"RTI"+File.separator);
		}
		open(csSource);
		rename("csSource");
		if ((nSlices == 1)&&(bitDepth()<24)) {
			if (csRakingDesired) {
				noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_00"+".tiff");
				save(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_00"+".tiff");
				createJp2(projectName+"_"+csProcessName+"_00");
				File.delete(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_00"+".tiff");
			}
			run("8-bit");
			run("Duplicate...", "title=Cb");
			run("Duplicate...", "title=Cr");
		} else if (nSlices == 2) {
			run("8-bit");
			run("Stack to Images");
			selectImage(1);
			rename("Cb");
			selectImage(2);
			rename("Cr");
		} else if ((nSlices > 2)||(bitDepth()==24)) {
			if (nSlices > 3) {
				run("Slice Keeper", "first=1 last=3 increment=1");
				print("Only the first three slices in the stack can be used at this time.");
			}
			if (bitDepth() == 8) {
				run("RGB Color");
			}
			//create a 00 static diffuse
			if (csRakingDesired) {
				noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_00"+".tiff");
				save(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_00"+".tiff");
				createJp2(projectName+"_"+csProcessName+"_00");
				File.delete(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_00"+".tiff");
			}
			run("RGB to YCbCr stack");
			run("8-bit");
			run("Stack to Images");
			selectWindow("Y");
			run("Close");
		}
		selectWindow("csSource");
		run("Close");
		if (transmissiveSource != "") {
			open(projectDirectory+"Captures-Transmissive-Gamma"+File.separator+transmissiveSource);
			rename("TransmissiveLuminance");
			run("8-bit");
			run("Concatenate...", "  title=[YCC] keep image1=TransmissiveLuminance image2=Cb image3=Cr image4=[-- None --]");
			run("YCbCr stack to RGB");
			noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_Tx.tiff");
			save(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_Tx.tiff");
			createJp2(projectName+"_"+csProcessName+"_Tx");
			File.delete(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_Tx.tiff");
			selectWindow("YCC - RGB");
			run("Close");
			selectWindow("YCC");
			run("Close");
			selectWindow("TransmissiveLuminance");
			run("Close");
		}
		for(i=0;i<listOfHemisphereCaptures.length;i++) {
			if (endsWith(listOfHemisphereCaptures[i],"tif")) {
				if ((csRtiDesired)||(listOfRakingDirections[i+1])) {
					open(projectDirectory+"Captures-Hemisphere-Gamma"+File.separator+listOfHemisphereCaptures[i]);
					rename("Luminance");
					run("Duplicate...", "title=EnhancedLuminance");
					if (brightnessAdjustOption == "Yes, by normalizing each image to a selected area") {
						makeRectangle(normX, normY, normWidth, normHeight);
						run("Enhance Contrast...", "saturated=0.4");
						run("Select None");
					} else if (brightnessAdjustOption == "Yes, by multiplying all images by a fixed value") {
						run("Multiply...", "value="+normalizationFixedValue+"");
					}
					selectWindow("Luminance");
					run("8-bit");
					selectWindow("EnhancedLuminance");
					run("8-bit");
					if (listOfRakingDirections[i+1]){
						run("Concatenate...", "  title=[YCC] keep image1=EnhancedLuminance image2=Cb image3=Cr image4=[-- None --]");
						run("YCbCr stack to RGB");
						selectWindow("YCC");
						run("Close");
						positionNumber = toString(IJ.pad(i+1, 2));
						noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_"+positionNumber+".tiff");
						save(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_"+positionNumber+".tiff");
						selectWindow("YCC - RGB");
						run("Close");
						createJp2(projectName+"_"+csProcessName+"_"+positionNumber);
						File.delete(projectDirectory+"StaticRaking"+File.separator+projectName+"_"+csProcessName+"_"+positionNumber+".tiff");
					}
					if ((csRtiDesired)&&(brightnessAdjustApply == "RTI images also")){
						run("Concatenate...", "  title=[YCC] keep image1=EnhancedLuminance image2=Cb image3=Cr image4=[-- None --]");
						run("YCbCr stack to RGB");
						selectWindow("YCC");
						run("Close");
						noClobber(projectDirectory+csProcessName+"RTI"+File.separator+csProcessName+"_"+File.nameWithoutExtension+".jpg");
						saveAs("jpeg", projectDirectory+csProcessName+"RTI"+File.separator+csProcessName+"_"+File.nameWithoutExtension+".jpg");
						//setBatchMode("show"); //debugging
						selectWindow(File.nameWithoutExtension+".jpg");
						run("Close");
					} else if (csRtiDesired) {
						run("Concatenate...", "  title=[YCC] keep image1=Luminance image2=Cb image3=Cr image4=[-- None --]");
						run("YCbCr stack to RGB");
						selectWindow("YCC");
						run("Close");
						noClobber(projectDirectory+csProcessName+"RTI"+File.separator+csProcessName+"_"+File.nameWithoutExtension+".jpg");
						saveAs("jpeg", projectDirectory+csProcessName+"RTI"+File.separator+csProcessName+"_"+File.nameWithoutExtension+".jpg");
						//setBatchMode("show"); //debugging
						selectWindow(File.nameWithoutExtension+".jpg");
						run("Close");
					}
					selectWindow("EnhancedLuminance");
					run("Close");
					selectWindow("Luminance");
					run("Close");
				}
			}
		}
		selectWindow("Cb");
		run("Close");
		selectWindow("Cr");
		run("Close");
		createLpFile(csProcessName);
		runFitter(csProcessName);
	}
	beep();
	setBatchMode("exit and display");
	showMessage("Processing Complete", "Processing complete at "+timestamp());
}
//create a version using LAB and avoiding 8-bit longer

macro "Curate [n2]" { // this macro is not part of the plugin
	setBatchMode(true);
	basePath=File.separator+"mnt"+File.separator+"JubPalProj"+File.separator+"AmbrosianaArchive"+File.separator+"Ambrosiana_C73inf"+File.separator; //where to put curated files
	//ask which folder to process
	dir = getDirectory("Choose a Directory to Curate");
	listFiles(dir);
	function listFiles(dir) {
		rotation = -1;
		list = getFileList(dir);
		for (i=0; i<list.length; i++) {
			fileParents = split(dir,File.separator);
			objectName = fileParents[fileParents.length-1]; //1 assumes directly in folder with object name, 2 if intermediate folder
			cleanFilename=replace(list[i]," ","_");
			cleanFilename=replace(cleanFilename,"+","_");
			if(endsWith(list[i],"dng")) { // raw files just move to the Captures-Raw directory
				if (!File.exists(basePath+objectName+File.separator)) {
					File.makeDirectory(basePath+objectName+File.separator);
				}
				if (!File.exists(basePath+objectName+File.separator+"Captures-Raw"+File.separator)) {
					File.makeDirectory(basePath+objectName+File.separator+"Captures-Raw"+File.separator);
				}
				exec("mv "+dir+list[i]+" "+basePath+objectName+File.separator+"Captures-Raw"+File.separator);
				print("moved "+dir+list[i]+" to "+objectName+File.separator+"Captures-Raw");
			} else if (endsWith(list[i],"_F.tif")) { //flattened tiffs require rotation and go to NoGamma directories
				if (!File.exists(basePath+objectName+File.separator)) {
					File.makeDirectory(basePath+objectName+File.separator);
				}
				orientation = exec("exiftool -orientation "+dir+list[i]);
				print("Orientation of "+list[i]+" is: "+orientation);
				if(indexOf(orientation, "Rotate")>0){ //check for orientation in metadata
					startIndex = indexOf(orientation, "Rotate"); //find starting position
					startIndex = startIndex + 7; //take into account the "Rotate " characters
					restOfLine = substring(orientation, startIndex);
					if (indexOf(restOfLine," CW")>0) {
						stopIndex = indexOf(restOfLine, " CW");
					} else {
						stopIndex = lengthOf(restOfLine);
					}
					rotation = substring(orientation, startIndex, startIndex+stopIndex);
				} else { // if no orientation in metadata then ask user
					Dialog.create("Rotation");
					Dialog.addMessage("orientation tag not found, specify rotation for "+objectName);
					rotationOptions=newArray(0,90,180,270);
					Dialog.addChoice("Rotation",rotationOptions,0);
					Dialog.show();
					rotation = Dialog.getChoice();
				}
				if (rotation!=0){
					open(dir+list[i]);
					if (rotation==90) {
						run("Rotate 90 Degrees Right");
					} else if (rotation==180) {
						run("Rotate 90 Degrees Right");
						run("Rotate 90 Degrees Right");
					} else if (rotation==270) {
						run("Rotate 90 Degrees Left");
					}
				}
				if(indexOf(cleanFilename,"_MB")>0) {//identify as a narrowband (mainbank) capture
					if (!File.exists(basePath+objectName+File.separator+"Captures-Narrowband-NoGamma"+File.separator)) {
						File.makeDirectory(basePath+objectName+File.separator+"Captures-Narrowband-NoGamma"+File.separator);
					}
					if (rotation==0){ // move without opening, rotating, and saving
						exec("mv "+dir+list[i]+" "+basePath+objectName+File.separator+"Captures-Narrowband-NoGamma"+File.separator+cleanFilename);
						print("moved "+dir+list[i]+" to "+objectName+File.separator+"Captures-Narrowband-NoGamma"+File.separator+cleanFilename);
					} else {
						save(basePath+objectName+File.separator+"Captures-Narrowband-NoGamma"+File.separator+cleanFilename);
						print("rotated and moved "+dir+list[i]+" to "+objectName+File.separator+"Captures-Narrowband-NoGamma"+File.separator+cleanFilename);
					}
				} else if (indexOf(cleanFilename,"_RTI-")>0){ //identify as hemisphere (RTI) capture
					if (!File.exists(basePath+objectName+File.separator+"Captures-Hemisphere-NoGamma"+File.separator)) {
						File.makeDirectory(basePath+objectName+File.separator+"Captures-Hemisphere-NoGamma"+File.separator);
					}
					if (rotation==0){
						exec("mv "+dir+list[i]+" "+basePath+objectName+File.separator+"Captures-Hemisphere-NoGamma"+File.separator+cleanFilename);
						print("moved "+dir+list[i]+" to "+objectName+File.separator+"Captures-Hemisphere-NoGamma"+File.separator+cleanFilename);
					} else {
						save(basePath+objectName+File.separator+"Captures-Hemisphere-NoGamma"+File.separator+cleanFilename);
						print("rotated and moved "+dir+list[i]+" to "+objectName+File.separator+"Captures-Hemisphere-NoGamma"+File.separator+cleanFilename);
					}
				} else if (indexOf(cleanFilename,"_TX")>0){ //identify as transmissive capture
					if (!File.exists(basePath+objectName+File.separator+"Captures-Transmissive-NoGamma"+File.separator)) {
						File.makeDirectory(basePath+objectName+File.separator+"Captures-Transmissive-NoGamma"+File.separator);
					}
					if (rotation==0){ // move without opening, rotating, and saving
						exec("mv "+dir+list[i]+" "+basePath+objectName+File.separator+"Captures-Transmissive-NoGamma"+File.separator+cleanFilename);
						print("moved "+dir+list[i]+" to "+objectName+File.separator+"Captures-Transmissive-NoGamma"+File.separator+cleanFilename);
					} else {
						save(basePath+objectName+File.separator+"Captures-Transmissive-NoGamma"+File.separator+cleanFilename);
						print("rotated and moved "+dir+list[i]+" to "+objectName+File.separator+"Captures-Transmissive-NoGamma"+File.separator+cleanFilename);
					}
				} else if (indexOf(cleanFilename,"_W")>0){ //identify as Fluorescence (wheelbank) capture
					if (!File.exists(basePath+objectName+File.separator+"Captures-Fluorescence-NoGamma"+File.separator)) {
						File.makeDirectory(basePath+objectName+File.separator+"Captures-Fluorescence-NoGamma"+File.separator);
					}
					if (rotation==0){ // move without opening, rotating, and saving
						exec("mv "+dir+list[i]+" "+basePath+objectName+File.separator+"Captures-Fluorescence-NoGamma"+File.separator+cleanFilename);
						print("moved "+dir+list[i]+" to "+objectName+File.separator+"Captures-Fluorescence-NoGamma"+File.separator+cleanFilename);
					} else {
						save(basePath+objectName+File.separator+"Captures-Fluorescence-NoGamma"+File.separator+cleanFilename);
						print("rotated and moved "+dir+list[i]+" to "+objectName+File.separator+"Captures-Fluorescence-NoGamma"+File.separator+cleanFilename);
					}
				}
				if (rotation!=0){
					selectWindow(list[i]);
					run("Close");
					//exec("rm "+dir+list[i]); //after some tests also delete the original file
				}
			} else if (endsWith(list[i],"_PSC.tif")){ //color images do not require rotation and go in Accurate Color directory
				//move
				if (!File.exists(basePath+objectName+File.separator+"AccurateColor"+File.separator)) {
					File.makeDirectory(basePath+objectName+File.separator+"AccurateColor"+File.separator);
				}
				exec("mv "+dir+list[i]+" "+basePath+objectName+File.separator+"AccurateColor"+File.separator+cleanFilename);
				print("moved "+dir+list[i]+" to "+basePath+objectName+File.separator+"AccurateColor"+File.separator+cleanFilename);
				//execute imagemagick
				//convert Ambrosiana_C73inf_xxx_PSC.tif -colorspace sRGB Ambrosiana_C73inf_xxx_sRGB-ImageMagick.tif
				print("convert "+basePath+objectName+File.separator+"AccurateColor"+File.separator+cleanFilename+" -colorspace sRGB "+basePath+objectName+"_sRGBim.tif");
				exec("convert "+basePath+objectName+File.separator+"AccurateColor"+File.separator+cleanFilename+" -colorspace sRGB "+basePath+objectName+File.separator+"AccurateColor"+File.separator+objectName+"_sRGBim.tif");
				print("sRGB created: "+objectName+"_sRGBim.tif");
			} else if (endsWith(list[i],".tif")) { //gamma curved tiffs do not require rotation and go to Gamma directories
				if (!File.exists(basePath+objectName+File.separator)) {
					File.makeDirectory(basePath+objectName+File.separator);
				}
				cleanFilename=replace(cleanFilename," ","_");
				cleanFilename=replace(cleanFilename,"+","_");
				if(indexOf(cleanFilename,"_MB")>0) {//identify as a narrowband (mainbank) capture
					if (!File.exists(basePath+objectName+File.separator+"Captures-Narrowband-Gamma"+File.separator)) {
						File.makeDirectory(basePath+objectName+File.separator+"Captures-Narrowband-Gamma"+File.separator);
					}
					exec("mv "+dir+list[i]+" "+basePath+objectName+File.separator+"Captures-Narrowband-Gamma"+File.separator+cleanFilename);
					print("moved "+dir+list[i]+" to "+objectName+File.separator+"Captures-Narrowband-Gamma"+File.separator+cleanFilename);
				} else if (indexOf(cleanFilename,"_RTI-")>0){ //identify as hemisphere (RTI) capture
					if (!File.exists(basePath+objectName+File.separator+"Captures-Hemisphere-Gamma"+File.separator)) {
						File.makeDirectory(basePath+objectName+File.separator+"Captures-Hemisphere-Gamma"+File.separator);
					}
					exec("mv "+dir+list[i]+" "+basePath+objectName+File.separator+"Captures-Hemisphere-Gamma"+File.separator+cleanFilename);
					print("moved "+dir+list[i]+" to "+objectName+File.separator+"Captures-Hemisphere-Gamma"+File.separator+cleanFilename);
				} else if (indexOf(cleanFilename,"_TX")>0){ //identify as transmissive capture
					if (!File.exists(basePath+objectName+File.separator+"Captures-Transmissive-Gamma"+File.separator)) {
						File.makeDirectory(basePath+objectName+File.separator+"Captures-Transmissive-Gamma"+File.separator);
					}
					exec("mv "+dir+list[i]+" "+basePath+objectName+File.separator+"Captures-Transmissive-Gamma"+File.separator+cleanFilename);
					print("moved "+dir+list[i]+" to "+objectName+File.separator+"Captures-Transmissive-Gamma"+File.separator+cleanFilename);
				} else if (indexOf(cleanFilename,"_W")>0){ //identify as Fluorescence (wheelbank) capture
					if (!File.exists(basePath+objectName+File.separator+"Captures-Fluorescence-Gamma"+File.separator)) {
						File.makeDirectory(basePath+objectName+File.separator+"Captures-Fluorescence-Gamma"+File.separator);
					}
					exec("mv "+dir+list[i]+" "+basePath+objectName+File.separator+"Captures-Fluorescence-Gamma"+File.separator+cleanFilename);
					print("moved "+dir+list[i]+" to "+objectName+File.separator+"Captures-Fluorescence-Gamma"+File.separator+cleanFilename);
				}
			} //end Gamma curved tif
			if (endsWith(list[i], File.separator)) listFiles(dir+list[i]);
		}//end process this directory
	}//end function list contents of directory
	showMessage("Processing Complete", "Processing complete");
	setBatchMode("exit and display");
	beep();
}
