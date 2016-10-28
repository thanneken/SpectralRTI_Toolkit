/*
Title: Spectral RTI Toolkit
Version: 0.1
Date: October 28, 2016
Author: Todd R. Hanneken, thanneken@stmarytx.edu, thanneke@uchicago.edu
Description: A toolkit for processing Spectral RTI images
About: 
See http://palimpsest.stmarytx.edu/integrating
*/
var brightnessAdjustOption = ""; //declare global variables from beginning
var normX; // probably a more elegant way to do this but important that variables are accessible outside of function
var normY;
var normWidth;
var normHeight;
var normalizationFixedValue;
var pcaX;
var pcaY;
var pcaWidth;
var pcaHeight;
var lpSource = "";
var projectDirectory = "";
var projectName = "";
var jpegQuality;
var startTime = timestamp();
var listOfRakingDirections;
function createJp2(inFile) {
	preferredCompress = List.get("preferredCompress");
	preferredJp2Args = List.get("preferredJp2Args");
	if (preferredCompress ==""){
		preferredCompress = File.openDialog("Locate kdu_compress or ojp_compress");
		File.append("preferredCompress="+preferredCompress,"SpectralRTI_Toolkit-prefs.txt");
	}
	if (preferredJp2Args == "") {
		Dialog.create("Approve arguments for Jpeg 2000 compression");
		Dialog.addString("Arguments:","-rate -,2.4,1.48331273,.91673033,.56657224,.35016049,.21641118,.13374944,.08266171 Creversible\=no Clevels\=5 Stiles\=\{1024,1024\} Cblk\=\{64,64\} -jp2_space sRGB Cuse_sop\=yes Cuse_eph\=yes Corder\=RPCL ORGgen_plt\=yes ORGtparts\=R Cmodes\=BYPASS -double_buffering 10 -num_threads 4 -no_weights",80);
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
		print("Executing command "+preferredFitter+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI-"+startTime+".rti\nThis could take a while..."); 
		File.append("Brightness Adjust Option: "+brightnessAdjustOption,projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI-"+startTime+".txt");
		if (brightnessAdjustOption == "Yes, by normalizing each image to a selected area") {
			File.append("Normalization area bounds: "+normX+", "+normY+", "+normWidth+", "+normHeight,projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI-"+startTime+".txt");
		} else if (brightnessAdjustOption == "Yes, by multiplying all images by a fixed value") {
			File.append("Normalization fixed value: "+normalizationFixedValue,projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI-"+startTime+".txt");
		}
		if (pcaX > 0) {
			File.append("PCA area bounds: "+pcaX+", "+pcaY+", "+pcaWidth+", "+pcaHeight,projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI-"+startTime+".txt");
		}
		File.append("Jpeg Quality: "+jpegQuality+" (edit SpectralRTI_Toolkit-prefs.txt to change)",projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI-"+startTime+".txt");
		File.append("Executing command "+preferredFitter+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI-"+startTime+".rti",projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI-"+startTime+".txt");
		fitterOutput = exec(preferredFitter+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI-"+startTime+".rti");
		print(fitterOutput);
		File.append(fitterOutput,projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI-"+startTime+".txt");
		if (webRtiDesired) {
			webRtiMaker = List.get("webRtiMaker"); 
			if (webRtiMaker == "") {
				webRtiMaker = File.openDialog("Locate webGLRTIMaker.exe");
				File.append("webRtiMaker="+webRtiMaker,"SpectralRTI_Toolkit-prefs.txt");
			}
			webRtiMakerOutput = exec(webRtiMaker+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI-"+startTime+".rti -q "+jpegQuality);
			print(webRtiMakerOutput);
			File.append("<html lang=\"en\" xml:lang=\"en\"> <head> <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /> <title>WebRTI "+projectName+"-"+colorProcess+"</title> <link type=\"text/css\" href=\"css/ui-lightness/jquery-ui-1.10.3.custom.css\" rel=\"Stylesheet\"> <link type=\"text/css\" href=\"css/webrtiviewer.css\" rel=\"Stylesheet\"> <script type=\"text/javascript\" src=\"js/jquery.js\"></script> <script type=\"text/javascript\" src=\"js/jquery-ui.js\"></script> <script type=\"text/javascript\" src=\"spidergl/spidergl_min.js\"></script> <script type=\"text/javascript\" src=\"spidergl/multires_min.js\"></script> </head> <body> <div id=\"viewerContainer\"> <script  type=\"text/javascript\"> createRtiViewer(\"viewerContainer\", \""+projectName+"-"+colorProcess+"RTI-"+startTime+"\", $(\"body\").width(), $(\"body\").height()); </script> </div> </body> </html>",projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI-"+startTime+"-wrti.html");
		}
	} else if (endsWith(preferredFitter,"cmd")) {
		hshOrder = List.get("hshOrder");
		if (hshOrder < 2 ) hshOrder = 3;
		hshThreads = List.get("hshThreads");
		if (hshThreads < 1 ) hshThreads = 16;
		print("Adding command to batch command file "+preferredFitter+": hshfitter "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI-"+startTime+".rti\n"); 
		File.append("Brightness Adjust Option: "+brightnessAdjustOption,projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI-"+startTime+".txt");
		if (brightnessAdjustOption == "Yes, by normalizing each image to a selected area") {
			File.append("Normalization area bounds: "+normX+", "+normY+", "+normWidth+", "+normHeight,projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI-"+startTime+".txt");
		} else if (brightnessAdjustOption == "Yes, by multiplying all images by a fixed value") {
			File.append("Normalization fixed value: "+normalizationFixedValue,projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI-"+startTime+".txt");
		}
		if (pcaX > 0) {
			File.append("PCA area bounds: "+pcaX+", "+pcaY+", "+pcaWidth+", "+pcaHeight,projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI-"+startTime+".txt");
		}
		File.append("Jpeg Quality: "+jpegQuality+" (edit SpectralRTI_Toolkit-prefs.txt to change)",projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI-"+startTime+".txt");
		File.append("Executing command "+preferredFitter+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI-"+startTime+".rti",projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI-"+startTime+".txt");
		File.append("hshfitter "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI.lp "+hshOrder+" "+hshThreads+" "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI-"+startTime+".rti",preferredFitter); 
		File.append("webGLRTIMaker "+projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI-"+startTime+".rti -q "+jpegQuality,preferredFitter);
		File.append("<html lang=\"en\" xml:lang=\"en\"> <head> <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /> <title>WebRTI "+projectName+"-"+colorProcess+"</title> <link type=\"text/css\" href=\"css/ui-lightness/jquery-ui-1.10.3.custom.css\" rel=\"Stylesheet\"> <link type=\"text/css\" href=\"css/webrtiviewer.css\" rel=\"Stylesheet\"> <script type=\"text/javascript\" src=\"js/jquery.js\"></script> <script type=\"text/javascript\" src=\"js/jquery-ui.js\"></script> <script type=\"text/javascript\" src=\"spidergl/spidergl_min.js\"></script> <script type=\"text/javascript\" src=\"spidergl/multires_min.js\"></script> </head> <body> <div id=\"viewerContainer\"> <script  type=\"text/javascript\"> createRtiViewer(\"viewerContainer\", \""+projectName+"-"+colorProcess+"RTI-"+startTime+"\", $(\"body\").width(), $(\"body\").height()); </script> </div> </body> </html>",projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI-"+startTime+"-wrti.html");
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
			if (endsWith(list[i],"OriginalSet.lp")) {
				//ignore this one
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
	noClobber(projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI.lp");
	File.append(lpLines[0],projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI.lp");
	for (i=1;i<lpLines.length;i++) {
		newLpLine = replace(lpLines[i],"\\","/");
		newLpLine = replace(newLpLine,"LightPositionData/jpeg-exports/",colorProcess+"RTI/"+colorProcess+"-");
		newLpLine = replace(newLpLine,"/",File.separator);
		File.append(newLpLine,projectDirectory+colorProcess+"RTI"+File.separator+projectName+"-"+colorProcess+"RTI.lp");
	}
}
function promptBrightnessAdjust() {
	open(projectDirectory+"HemisphereCaptures"+File.separator+listOfHemisphereCaptures[round(listOfHemisphereCaptures.length/2)]);
	setBatchMode("show");
	brightnessAdjustOptions = newArray("No","Yes, by normalizing each image to a selected area","Yes, by multiplying all images by a fixed value");
	Dialog.create("Adjust brightness of hemisphere captures?");
	Dialog.addRadioButtonGroup("Adjust brightness of hemisphere captures?", brightnessAdjustOptions, brightnessAdjustOptions.length, 1, brightnessAdjustOptions[0]);
	Dialog.show();
	brightnessAdjustOption = Dialog.getRadioButton();
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
/*
function listFiles(dir) {
	list = getFileList(dir);
	for (i=0; i<list.length; i++) {
		if (endsWith(list[i], "/")) listFiles(dir+list[i]);
		else {
			fullPathName = dir+list[i];
			recursiveListOfFiles = Array.concat(recursiveListOfFiles, fullPathName);
		}
	}
}
*/
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
/*
function readLogFile() {
	logfile = File.openAsString("SpectralRTI_Toolkit.log");
	lines=split(logfile,"\n");
	//for (i=0;i<lengthOf(lines);i++) {
	for (i=0;i<lines.length;i++) {
		values=split(lines[i]);
		//if (values[1]=="ProjectDirectory") ProjectDirectory=values[2];
	}
}
*/
macro "Spectral RTI [n1]" {
	setBatchMode(true);
	if (File.exists("SpectralRTI_Toolkit-prefs.txt")) {
		prefsFile = File.openAsString("SpectralRTI_Toolkit-prefs.txt");
		prefs = split(prefsFile,"\n");
		for (i=0;i<prefs.length;i++) {
			//pairs = split(prefs[i],"=");
			//List.set(pairs[0],pairs[1]); // didn't work with equal signs in values, still can't have them in keys
			List.set(substring(prefs[i],0,indexOf(prefs[i],"=")),substring(prefs[i],indexOf(prefs[i],"=")+1));
		}
	}
	jpegQuality = call("ij.plugin.JpegWriter.getQuality");
	if (List.get("jpegQuality") > 0) jpegQuality = List.get("jpegQuality");
	run("Input/Output...","jpeg="+jpegQuality);
//	File.append(timestamp()+",ToolkitStatus,Started","SpectralRTI_Toolkit.log");
	projectDirectory = getDirectory("Choose a Directory");
	projectDirectory = replace(projectDirectory,"\\",File.separator);
	projectName = File.getName(projectDirectory);
//	File.append(timestamp()+",ProjectDirectory,"+projectDirectory,"SpectralRTI_Toolkit.log");
	if (!File.exists(projectDirectory+"HemisphereCaptures"+File.separator)) {
		File.makeDirectory(projectDirectory+"HemisphereCaptures"+File.separator);
		print("A directory has been created for the Hemisphere Captures at "+projectDirectory+"HemisphereCaptures"+File.separator);
	}
	listOfHemisphereCaptures = getFileList(projectDirectory+"HemisphereCaptures"+File.separator);
	while (listOfHemisphereCaptures.length < 29) {
		showMessageWithCancel("Please Populate Hemisphere Captures","The software expects at least 30 images in HemisphereCaptures folder.\nPlease populate the folder and press Ok to continue, or cancel.");
		listOfHemisphereCaptures = getFileList(projectDirectory+"HemisphereCaptures"+File.separator);
	}
	lpDesired=true;
	acRtiDesired=true;
	xsRtiDesired=true;
	psRtiDesired=true;
	if (File.exists(projectDirectory+"LightPositionData"+File.separator)) lpDesired = false;
	listOfAccurateColorFiles = getFileList(projectDirectory+"AccurateColor"+File.separator);
	if (listOfAccurateColorFiles.length<1) acRtiDesired = false;
	if (File.exists(projectDirectory+"AccurateColorRTI"+File.separator)) acRtiDesired = false;
	listOfNarrowbandCaptures = getFileList(projectDirectory+"NarrowbandCaptures"+File.separator);
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
	Dialog.addCheckbox("Extended Spectrum RTI",xsRtiDesired);
	Dialog.addCheckbox("Pseudocolor RTI",psRtiDesired);
	Dialog.addCheckbox("Static Raking",false);
	Dialog.addCheckbox("WebRTI",false);
	Dialog.show();
	lpDesired = Dialog.getCheckbox();
	acRtiDesired = Dialog.getCheckbox();
	xsRtiDesired = Dialog.getCheckbox();
	psRtiDesired = Dialog.getCheckbox();
	rakingDesired = Dialog.getCheckbox();
	webRtiDesired = Dialog.getCheckbox();
	//identify angles for uncompressed static raking
	if (rakingDesired){
		if (!File.exists(projectDirectory+"StaticRaking"+File.separator)) {
			File.makeDirectory(projectDirectory+"StaticRaking"+File.separator);
			print("A directory has been created for lossless static raking images at "+projectDirectory+"StaticRaking"+File.separator);
		}
		Dialog.create("Select light positions for lossless static raking images");
		for(i=0;i<listOfHemisphereCaptures.length;i++) {
			Dialog.addCheckbox(listOfHemisphereCaptures[i],false);
		}
		Dialog.show();
		for(i=0;i<listOfHemisphereCaptures.length;i++) {
			listOfRakingDirections = Array.concat(listOfRakingDirections,Dialog.getCheckbox());
		}
	}
	if (xsRtiDesired) { // only interaction here, processing later
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
			Dialog.addRadioButtonGroup(listOfNarrowbandCaptures[i], rgbnOptions, 1, 4, defaultRange);
		}
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
			open(projectDirectory+"NarrowbandCaptures"+File.separator+listOfNarrowbandCaptures[round(listOfNarrowbandCaptures.length/2)]);
			setBatchMode("show");
			waitForUser("Select area", "Draw a rectangle containing the colors of interest for PCA\n(hint: limit to object or smaller)");
			getSelectionBounds(pcaX, pcaY, pcaWidth, pcaHeight);
			run("Close");
		}
	}
	if (psRtiDesired) {// only interaction here, processing later
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
				open(projectDirectory+"NarrowbandCaptures"+File.separator+listOfNarrowbandCaptures[round(listOfNarrowbandCaptures.length/2)]);
				setBatchMode("show");
				waitForUser("Select area", "Draw a rectangle containing the colors of interest for PCA\n(hint: limit to object or smaller)");
				getSelectionBounds(pcaX, pcaY, pcaWidth, pcaHeight);
				run("Close");
			}
	}
	//create base lp file
	if (lpDesired) {
		ballArea=false;
		for(i=0;i<listOfHemisphereCaptures.length;i++) {
			if (endsWith(listOfHemisphereCaptures[i],"tif")) {
				open(projectDirectory+"HemisphereCaptures"+File.separator+listOfHemisphereCaptures[i]);
				if (!ballArea) {
					setBatchMode("show");
					waitForUser("Select ROI", "Draw a rectangle loosely around a reflective hemisphere and press Ok");
					getSelectionBounds(x, y, width, height);
					ballArea = true;
				}
				makeRectangle(x,y,width,height);
				run("Crop");
				if (!File.exists(projectDirectory+"LightPositionData"+File.separator)) File.makeDirectory(projectDirectory+"LightPositionData"+File.separator);
				if (!File.exists(projectDirectory+"LightPositionData"+File.separator+"jpeg-exports"+File.separator)) File.makeDirectory(projectDirectory+"LightPositionData"+File.separator+"jpeg-exports");
				saveAs("jpeg",projectDirectory+"LightPositionData"+File.separator+"jpeg-exports"+File.separator+File.nameWithoutExtension+".jpg");
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
		//normalization based on fixed value or prompted area
		if (brightnessAdjustOption == "") promptBrightnessAdjust();
		//integration
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
		open(projectDirectory+"AccurateColor"+File.separator+ accurateColorSource);
		rename("RGBtiff");
		if (bitDepth() == 8) {
			run("RGB Color");
			selectWindow("RGBtiff");
			run("Close");
			rename("RGBtiff");
		}
		//create accurate color static diffuse
		if (rakingDesired){
			noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"-AccurateColor-00"+".tiff");
			save(projectDirectory+"StaticRaking"+File.separator+projectName+"-AccurateColor-00"+".tiff");
			createJp2(projectName+"-AccurateColor-00");
			File.delete(projectDirectory+"StaticRaking"+File.separator+projectName+"-AccurateColor-00"+".tiff");
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
				open(projectDirectory+"HemisphereCaptures"+File.separator+listOfHemisphereCaptures[i]);
				rename("Luminance");
				// it would be better to crop early in the process, especially before reducing to 8-bit and jpeg compression
				// normalize
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
				if (listOfRakingDirections[i+1]) {
					positionNumber = toString(i+1);
					noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"-AccurateColor-"+positionNumber+".tiff");
					save(projectDirectory+"StaticRaking"+File.separator+projectName+"-AccurateColor-"+positionNumber+".tiff");
					createJp2(projectName+"-AccurateColor-"+positionNumber);
					File.delete(projectDirectory+"StaticRaking"+File.separator+projectName+"-AccurateColor-"+positionNumber+".tiff");
				}
				//Save as jpeg
				noClobber(projectDirectory+"AccurateColorRTI"+File.separator+"AccurateColor-"+File.nameWithoutExtension+".jpg");
				saveAs("jpeg", projectDirectory+"AccurateColorRTI"+File.separator+"AccurateColor-"+File.nameWithoutExtension+".jpg");
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
	//create Extended Spectrum RTI
	if (xsRtiDesired) {
		//create series of images with luminance from hemisphere captures and chrominance from extended spectrum color process
		if (!File.exists(projectDirectory+"ExtendedSpectrumRTI"+File.separator)) {
			File.makeDirectory(projectDirectory+"ExtendedSpectrumRTI"+File.separator);
			print("A directory has been created for Extended Spectrum RTI at "+projectDirectory+"ExtendedSpectrumRTI"+File.separator);
		}
		//Dialog moved from here
		//Red
		redStringList = redNarrowbands[0];  //might be an array to string function somewhere to do this more elegantly
		for (i=1;i<redNarrowbands.length;i++) {
			redStringList = redStringList+"|"+redNarrowbands[i]; 
		}
		run("Image Sequence...", "open="+projectDirectory+"NarrowbandCaptures"+File.separator+" file=("+redStringList+") sort");
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
		run("Image Sequence...", "open="+projectDirectory+"NarrowbandCaptures"+File.separator+" file=("+greenStringList+") sort");
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
		run("Image Sequence...", "open="+projectDirectory+"NarrowbandCaptures"+File.separator+" file=("+blueStringList+") sort");
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
		if (rakingDesired) {
			noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"-ExtendedSpectrum-00"+".tiff");
			save(projectDirectory+"StaticRaking"+File.separator+projectName+"-ExtendedSpectrum-00"+".tiff");
			createJp2(projectName+"-ExtendedSpectrum-00");
			File.delete(projectDirectory+"StaticRaking"+File.separator+projectName+"-ExtendedSpectrum-00"+".tiff");
		}
		run("RGB to YCbCr stack");
		run("Stack to Images");
		selectWindow("Y");
		run("Close");
		selectWindow("Stack (RGB)");
		run("Close");
		//normalization based on fixed value or prompted area
		if (brightnessAdjustOption == "") promptBrightnessAdjust();
		//luminance from hemisphere captures
		for(i=0;i<listOfHemisphereCaptures.length;i++) {
			open(projectDirectory+"HemisphereCaptures"+File.separator+listOfHemisphereCaptures[i]);
			rename("Luminance");
			// normalize
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
			//create extended spectrum static raking
			if (listOfRakingDirections[i+1]) {
				positionNumber = toString(i+1);
				noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"-ExtendedSpectrum-"+positionNumber+".tiff");
				save(projectDirectory+"StaticRaking"+File.separator+projectName+"-ExtendedSpectrum-"+positionNumber+".tiff");
				createJp2(projectName+"-ExtendedSpectrum-"+positionNumber);
				File.delete(projectDirectory+"StaticRaking"+File.separator+projectName+"-ExtendedSpectrum-"+positionNumber+".tiff");
			}
			//Save as jpeg
			noClobber(projectDirectory+"ExtendedSpectrumRTI"+File.separator+"ExtendedSpectrum-"+File.nameWithoutExtension+".jpg");
			saveAs("jpeg", projectDirectory+"ExtendedSpectrumRTI"+File.separator+"ExtendedSpectrum-"+File.nameWithoutExtension+".jpg");
			run("Close");
			selectWindow("YCC");
			run("Close");
			selectWindow("Luminance");
			run("Close");
		}
		selectWindow("Cb");
		run("Close");
		selectWindow("Cr");
		run("Close");
		run("Collect Garbage");
		createLpFile("ExtendedSpectrum");
		runFitter("ExtendedSpectrum");
	}
	//create PseudocolorRTI
	if (psRtiDesired) {
		//create directory if not already exists
		if (!File.exists(projectDirectory+"PseudocolorRTI"+File.separator)) {
			File.makeDirectory(projectDirectory+"PseudocolorRTI"+File.separator);
			print("A directory has been created for Pseudocolor RTI at "+projectDirectory+"PseudocolorRTI"+File.separator);
		}
		//dialog moved from here
		//option to create new ones based on narrowband captures and assumption that pc1 and pc2 are best
		if (pcaMethod == "Generate and select using defaults") {
			run("Image Sequence...", "open="+projectDirectory+"NarrowbandCaptures"+File.separator+" sort");
			rename("NarrowbandCaptures");
			makeRectangle(pcaX, pcaY, pcaWidth, pcaHeight);
			run("PCA ");
			selectWindow("Eigenvalue spectrum of NarrowbandCaptures");
			run("Close");
			selectWindow("NarrowbandCaptures");
			run("Close");
			selectWindow("PCA of NarrowbandCaptures");
			run("Slice Keeper", "first=2 last=3 increment=1");
			selectWindow("PCA of NarrowbandCaptures");
			run("Close");
			selectWindow("PCA of NarrowbandCaptures kept stack");
			makeRectangle(pcaX, pcaY, pcaWidth, pcaHeight); // questionable
			run("Enhance Contrast...", "saturated=0.3 normalize update process_all");
			run("8-bit");
		//option to create new ones and manually select (close all but two)
		} else if (pcaMethod =="Generate and manually select two") {
			run("Image Sequence...", "open="+projectDirectory+"NarrowbandCaptures"+File.separator+" sort");
			rename("NarrowbandCaptures");
			makeRectangle(pcaX, pcaY, pcaWidth, pcaHeight);
			run("PCA ");
			selectWindow("Eigenvalue spectrum of NarrowbandCaptures");
			run("Close");
			selectWindow("NarrowbandCaptures");
			run("Close");
			selectWindow("PCA of NarrowbandCaptures");
			makeRectangle(pcaX, pcaY, pcaWidth, pcaHeight);
			setBatchMode("show");
			//while (nSlices() > 2 ) {} //@@@ produces an error if press okay while still more than two
			waitForUser("Select area", "Delete slices from the stack until two remain\n(Hint: Image > Stacks > Delete Slice)\nEnhance contrast as desired\nThen press Ok");
			setBatchMode("hide");
			rename("PCA of NarrowbandCaptures kept stack");
			//run("Enhance Contrast...", "saturated=0.3 normalize update process_all");
			run("8-bit");
		//option to use previously generated principal component images 
		} else if (pcaMethod =="Open pregenerated images") {
			setBatchMode(false);
			waitForUser("Select area", "Open a pair of images or stack of two slices.\nEnhance contrast as desired\nThen press Ok");
			if (nImages() > 1) run("Images to Stack", "name=Stack title=[] use");
			setBatchMode(true);
			setBatchMode("hide");
			rename("PCA of NarrowbandCaptures kept stack");
			run("8-bit");
		}
		//integrate pca pseudocolor with rti luminance
		if (brightnessAdjustOption == "") promptBrightnessAdjust();
		//create static diffuse (not trivial... use median of all)
		if (rakingDesired){
			run("Image Sequence...", "open="+projectDirectory+"HemisphereCaptures"+File.separator);
			run("Z Project...", "projection=Median");
			rename("Luminance");
			selectWindow("HemisphereCaptures");
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
			run("Concatenate...", "  title=[YCC] keep image1=Luminance image2=[PCA of NarrowbandCaptures kept stack] image3=[-- None --]");
			run("YCbCr stack to RGB");
			noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"-PcaPseudocolor-00.tiff");
			save(projectDirectory+"StaticRaking"+File.separator+projectName+"-PcaPseudocolor-00.tiff");
			createJp2(projectName+"-PcaPseudocolor-00");
			File.delete(projectDirectory+"StaticRaking"+File.separator+projectName+"-PcaPseudocolor-00.tiff");
			run("Close");
			selectWindow("YCC");
			run("Close");
			selectWindow("Luminance");
			run("Close");
		}
		for(i=0;i<listOfHemisphereCaptures.length;i++) {
			open(projectDirectory+"HemisphereCaptures"+File.separator+listOfHemisphereCaptures[i]);
			rename("Luminance");
			// normalize
			if (brightnessAdjustOption == "Yes, by normalizing each image to a selected area") {
				makeRectangle(normX, normY, normWidth, normHeight);
				run("Enhance Contrast...", "saturated=0.4");
				run("Select None");
			} else if (brightnessAdjustOption == "Yes, by multiplying all images by a fixed value") {
				run("Multiply...", "value="+normalizationFixedValue+"");
			}
			run("8-bit");
			run("Concatenate...", "  title=[YCC] keep image1=Luminance image2=[PCA of NarrowbandCaptures kept stack] image3=[-- None --]");
			run("YCbCr stack to RGB");
			//create static raking
			if (listOfRakingDirections[i+1]) {
				positionNumber = toString(i+1);
				noClobber(projectDirectory+"StaticRaking"+File.separator+projectName+"-PcaPseudocolor-"+positionNumber+".tiff");
				save(projectDirectory+"StaticRaking"+File.separator+projectName+"-PcaPseudocolor-"+positionNumber+".tiff");
				createJp2(projectName+"-PcaPseudocolor-"+positionNumber);
				File.delete(projectDirectory+"StaticRaking"+File.separator+projectName+"-PcaPseudocolor-"+positionNumber+".tiff");
			}
			//Save as jpeg
			noClobber(projectDirectory+"PseudocolorRTI"+File.separator+"Pseudocolor-"+File.nameWithoutExtension+".jpg");
			saveAs("jpeg", projectDirectory+"PseudocolorRTI"+File.separator+"Pseudocolor-"+File.nameWithoutExtension+".jpg");
			selectWindow(File.nameWithoutExtension+".jpg");
			run("Close");
			selectWindow("YCC");
			run("Close");
			selectWindow("Luminance");
			run("Close");
		}
		selectWindow("PCA of NarrowbandCaptures kept stack");
		run("Close");
		run("Collect Garbage");
		createLpFile("Pseudocolor");
		runFitter("Pseudocolor");
setBatchMode("exit and display");
	}
}
//create a version using LAB and avoiding 8-bit longer
