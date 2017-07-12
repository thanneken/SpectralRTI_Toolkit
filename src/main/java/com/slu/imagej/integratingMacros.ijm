/*
Title: INTEGRATING MACROS
Version: 1.0
Date: June 30, 2014
Author: Todd R. Hanneken, thanneken@stmarytx.edu, thanneke@uchicago.edu
Description: The major ImageJ macros used in the Integrating Spectral and 
Reflectance Transformation Imaging (RTI) Project (2013-2014), a Phase of the 
Jubilees Palimpsest Project, supported by the National Endowment for the 
Humanities, Office of Digital Humanities. http://palimpsest.stmarytx.edu/

About: 
This file includes 4 macros and 5 supporting functions.
Method 18 is the major macro essential for the products of the project. It can 
be invoked by loading this file and pressing "8".
Method 16 is the first draft and is replaced by Method 18 in all ways except 
the excision of the reflective hemispheres.
Method 17 is an additional experiment in an interactive script designed to 
focus PCA pseudocolor processing on a small, user selected region of interest. It is not 
part of the proposed activities of the project.
Method 19 builds on method 17 to generate a series of small images, each 
offset by one pixel from the previous. The images can be combined into a video 
that pans across the frame. It is not part of the proposed activities of the project.

All the macros are tailored to the objects and filepaths of the Integrating 
Spectral and RTI Project. It may be freely used on other projects, but the 
macros must be edited significantly to work on additional objects. Doing so 
requires basic familiarity with the ImageJ Macro language, which resembles 
JavaScript and is well documented. One may also check back to the project 
website or contact the author directly to inquire if a more open-ended version 
has been developed.
*/
function cropObject() {
	if (processType == "Ball")
	{

		if (object == "Mask") makeRectangle(791, 480, 759, 732);
		else if (object=="Sold") makeRectangle(264, 3900, 1044, 948);
		else if (object == "Pal1") makeRectangle(5626, 4510, 719, 716);
		else if (object == "Ant2") makeRectangle(191, 5114, 941, 945);
		else if (object == "Pal3") makeRectangle(1796, 4555, 772, 741);
		else if (object == "Ant3") makeRectangle(121, 129, 972, 943);
		else if (object == "Pal2") makeRectangle(5284, 5085, 802, 816);
		else exit("Area not defined for object");
	} else {
		if (object == "Mask") makeRectangle(250, 1635, 7656, 2028);
		else if (object == "Sold") makeRectangle(1236, 168, 2880, 4728);
		//else if (object == "Pal1") makeRectangle(1304, 252, 5424, 3952); //full page, including some black background
		else if (object == "Pal1") makeRectangle(2940, 848, 3040, 2376); //detail on text
		else if (object == "Ant2") makeRectangle(1116, 1176, 5564, 3952);
		else if (object == "Pal3") makeRectangle(1596, 350, 5444, 3952);
		else if (object == "Ant3") makeRectangle(0, 1275, 8176, 4857);
		else if (object == "Pal2") makeRectangle(1256, 576, 5536, 4128);
		else exit("Area not defined for object");
	}
	run("Crop");
}

function rotateObject() {
	if (object == "Mask") run("Rotate 90 Degrees Right");
	else if (object == "Sold") run("Rotate... ", "angle=180 grid=1 interpolation=Bilinear");
	else if (object == "Pal1") run("Rotate 90 Degrees Left");
	else if (object == "Ant2") run("Rotate 90 Degrees Left");
	else if (object == "Pal3") run("Rotate 90 Degrees Left");
	else if (object == "Pal2") run("Rotate... ", "angle=-92 grid=1 interpolation=Bilinear");
}

function enhanceObject() {
	if (object == "Pal3") makeRectangle(4428,1989,1000,1000); //924,1686
	else if (object == "Pal1") makeRectangle(3000, 888, 2992, 2272);
	//else if (object == "Mask") makeRectangle(250, 1635, 7656, 2028); //complete object including exposed wood and black background
	else if (object == "Mask") makeRectangle(1206,1900,3096,1500); //more focused region of interest
	//else if (object == "Sold") makeRectangle(1266, 168, 2850, 4728); //whole object with background
	else if (object == "Sold") makeRectangle(1662,468,1884,2430);  //only terracotta
	else if (object == "Ant2") makeRectangle(1260, 1422, 5304, 3576);
	else if (object == "Ant3") makeRectangle(0, 1520, 8176, 4612);
	else exit("Rectangle for enhanceObject not yet defined");
	run("Enhance Contrast...", "saturated=0.4");
	run("8-bit");
	run("Select None");
	if (object == "Pal3") run("Invert");
}

function enhanceMacbeth() {
	if (object == "Mask") makeRectangle(2058, 1230, 1770, 302);
	else if (object == "Sold") makeRectangle(4506, 2245, 312, 1798);
	else if (object == "Pal1") makeRectangle(3691, 4382, 1705, 310);
	else if (object == "Pal3") makeRectangle(4117, 4549, 1608, 172);
	else if (object == "Ant2") makeRectangle(6826, 3050, 304, 1790);
	else if (object == "Ant3") makeRectangle(2096, 48, 6016, 552);
	else exit("Macbeth chart area not defined for object");
	run("Enhance Contrast...", "saturated=0.4");
	run("Select None");
}

function selectPCAFocus() {
	if (object == "Pal1") makeRectangle(3378, 1296, 828, 810);
	else if (object == "Mask") makeRectangle(1206,1900,3096,1500);
	else if (object == "Sold") makeRectangle(1662,468,1884,2430); 
	else if (object == "Ant2") makeRectangle(1260, 1422, 5304, 3576); //same as enhance object, but could set more narrowly if one wanted to study just the text, for example
	else if (object == "Ant3") makeRectangle(144, 1616, 7960, 4392);
	//else if (object == "Pal3") makeRectangle(3726, 1970, 1977, 1084); //difficult text only
	//else if (object == "Pal3") makeRectangle(4428,1989,1000,1000); //same as enhance
	else if (object == "Pal3") makeRectangle(1596, 350, 5444, 3952); //same as crop
	else exit("Rectangle for selectPCAFocus not yet defined");
}

macro "Method 16 [6]" {
	//Method 16 designed to encompass all objects and processes
	setBatchMode(true); 
	Dialog.create("Confirm information");
	Dialog.addMessage("Confirm the following");
	Dialog.addString("General Project Path: ", File.separator+"home"+File.separator+"faculty"+File.separator+"thanneken"+File.separator+"Integrating"+File.separator, 20);
	var objects = newArray("Mask", "Sold", "Pal1", "Ant2", "Pal3", "Ant3", "Pal2");
	Dialog.addRadioButtonGroup("Object: ", objects, 4, 2, "Sold");
	var processTypes = newArray("Ball", "Acca", "Accb", "Pcaa", "Pcab", "Pcac","Pcad","Pcae", "Pcaf", "Pcag","Suva","Igua");
	Dialog.addRadioButtonGroup("Processing: ", processTypes,4,3,"Ball");
	/*
	Ball creates two files, one under 45-capture process based on the flash reflection in the shiny ball, the other under 324-capture process based on the visible led reflection  in the shiny ball
	Acca is the 45 capture process, accurate color using a* and b* channels from eureka lights and L from flash at 35 angles
	Accb is the 324 capture process, accurate color generated from 7 visible magic flashlight illuminations at each of 35 angles
	Pcaa is the 45 capture process, Todd's PCA pseudocolor generated fromY=Flash Cb=Pc2 Cr=Pc3 from eureka lights
	Pcab is the 45 capture process, Roger's PCA pseudocolor in LAB from Eureka lights with Flash as L for each angle
	Pcac is the 324 capture process, Roger's PCA pseudocolor generated at each angle using global stats (based on Eureka00)
	Pcad is the 324 capture process, Roger's PCA pseudocolor generated at each angle using local stats 
	Pcae is the 324 capture process, Todd's PCA pseudocolor generated from Y=localPc0 Cb=localPc1 Cr=localPc3 from global stats
	Pcaf is the 324 capture process, Todd's PCA pseudocolor generated from Y=localPc0 Cb=localPc1 Cr=localPc2 from local stats
	Pcag is the 324 capture process, Y=localAvgL Cb=localPc2 Cr=localPc3 from global stats
	Suva is Simply UltraViolet, normalized separately at each angle to 0.1% saturation
	Igua is Infrared-529-Ultraviolet mapped to RGB, normalized separately at each angle to 0.1% saturation
	*/
	Dialog.addString("Start Direction: ", "1",2);
	Dialog.addString("Direction Count: ","1",2);
	Dialog.show();
	var pathRoot = Dialog.getString();
	var object = Dialog.getRadioButton();
	var processType = Dialog.getRadioButton();
	var startDirection = Dialog.getString();
	var directions = Dialog.getString();
	getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
	year = toString(year);
	if (month < 9) month = "0" + toString(++month);
	else month = toString(++month);
	if (dayOfMonth < 10) dayOfMonth = "0" + toString(dayOfMonth);
	else dayOfMonth = toString(dayOfMonth);
	if (object == "Mask") captureDate = "20130722";
	else if (object == "Sold") captureDate = "20130723";
	else if (object == "Pal1") captureDate = "20130725";
	else if (object == "Ant2") captureDate = "20130724";
	else if (object == "Pal3") captureDate = "20130726";
	else if (object == "Ant3") captureDate = "20130726";
	else if (object == "Pal2") captureDate = "20130725";
	if (processType == "Ball")
	{
		if (!File.exists(pathRoot+"Process"+File.separator+"45CaptureProcess"+File.separator+object+File.separator+processType)) File.makeDirectory(pathRoot+"Process"+File.separator+"45CaptureProcess"+File.separator+object+File.separator+processType);
		if (!File.exists(pathRoot+"Process"+File.separator+"45CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports")) File.makeDirectory(pathRoot+"Process"+File.separator+"45CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports");
		for (direction=startDirection; direction <= directions; direction++) 
		{
			if (direction < 10) angle = "0" + toString(direction);
			else angle = toString(direction);
			if (object == "Sold") angle = replace(angle,"01","01b"); //exception
			if (object == "Pal3")
			{
				if (angle == "02") open(pathRoot+"Capture"+File.separator+"Processed"+File.separator+captureDate+"-"+object+"-"+angle+"-Capt-01-50m"+File.separator+captureDate+"-"+object+"-"+angle+"-Capt-01-50m_011.tif");
				else open(pathRoot+"Capture"+File.separator+"Processed"+File.separator+captureDate+"-"+object+"-"+angle+"-Capt-01-50m"+File.separator+captureDate+"-"+object+"-"+angle+"-Capt-01-50m+FLA-_011.tif");
			} 
			else if (object == "Ant3") open (pathRoot+"Capture"+File.separator+"Processed"+File.separator+captureDate+"-"+object+"-"+angle+"-Capt-01-50m"+File.separator+captureDate+"-"+object+"-"+angle+"-Capt-01-50m_001.tif");
			else if (object == "Pal2") open (pathRoot+"Capture"+File.separator+"Processed"+File.separator+captureDate+"-"+object+"-AB-Capt-01-50m"+File.separator+captureDate+"-"+object+"-AB-Capt-01-50m_0"+angle+".tif");
			else open(pathRoot+"Capture"+File.separator+"Processed"+File.separator+captureDate+"-"+object+"-"+angle+"-Capt-01-50m"+File.separator+captureDate+"-"+object+"-"+angle+"-Capt-01-50m+FLA_011.tif");
			cropObject();
			rotateObject();
			saveAs("jpeg", pathRoot+"Process"+File.separator+"45CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports"+File.separator+year+month+dayOfMonth+"-"+object+"-"+angle+"-"+processType+"-03-50m-Fla.jpg");
			close();
			if (object == "Mask" ||object == "Sold" || object == "Pal1"|| object == "Ant2" || object == "Pal3") //skip objects for which Magic Flashlight not used, create shiny ball from visible led for others
			{
				if (object == "Pal3") open(pathRoot+"Capture"+File.separator+"Processed"+File.separator+captureDate+"-"+object+"-"+angle+"-Capt-01-50m"+File.separator+captureDate+"-"+object+"-"+angle+"-Capt-01-50m+507-_004.tif");
				else open(pathRoot+"Capture"+File.separator+"Processed"+File.separator+captureDate+"-"+object+"-"+angle+"-Capt-01-50m"+File.separator+captureDate+"-"+object+"-"+angle+"-Capt-01-50m+507_004.tif");
				cropObject();
				rotateObject();
				if (!File.exists(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType)) File.makeDirectory(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType);
				if (!File.exists(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports")) File.makeDirectory(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports");
				saveAs("jpeg", pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports"+File.separator+year+month+dayOfMonth+"-"+object+"-"+angle+"-"+processType+"-03-50m-507.jpg");
				close();
			}
		}
	}
	else if (processType == "Acca") //Acca is the 45 capture process, accurate color using a* and b* channels from eureka lights and L from flash at 35 angles
	{
		if (object=="Mask") open (pathRoot+"Process"+File.separator+"9CaptureProcess"+File.separator+"Mask"+File.separator+"20130722-Mask-00-Capt-01-50m_PSC.tif");
		else if (object=="Sold") open (pathRoot+"Process"+File.separator+"9CaptureProcess"+File.separator+"Sold"+File.separator+"20130723-Sold-00-Capt-01-50m_PSC.tif");
		else if (object=="Ant2") open (pathRoot+"Process"+File.separator+"9CaptureProcess"+File.separator+"Ant2"+File.separator+"20130724-Ant2-00-Capt-01-50m_PSC.tif");
		else print("Accurate Color from Eureka Lights not available for object "+object+".");
		if (!File.exists(pathRoot+"Process"+File.separator+"45CaptureProcess"+File.separator+object+File.separator+processType)) File.makeDirectory(pathRoot+"Process"+File.separator+"45CaptureProcess"+File.separator+object+File.separator+processType);
		if (!File.exists(pathRoot+"Process"+File.separator+"45CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports")) File.makeDirectory(pathRoot+"Process"+File.separator+"45CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports");
		lpFile = File.open(pathRoot+"Process"+File.separator+"45CaptureProcess"+File.separator+object+File.separator+processType+File.separator+year+month+dayOfMonth+"-"+object+"-99-"+processType+"-04-50m.txt");
		print(lpFile,"35\n");
		cropObject();
		rename("Non-Standard LAB");
		run("RGB Shifted32NonCom");
		rename("LAB Stack");
		close("Non-Standard LAB");
		run("Stack to Images");
		close("R-scale");
		for (direction=startDirection; direction <= directions; direction++) {
			if (direction < 10) {
				angle = "0" + toString(direction);
			} else {
				angle = toString(direction);
			}
			if (object == "Sold") angle = replace(angle,"01","01b"); //exception
			open(pathRoot+"Capture"+File.separator+"Processed"+File.separator+captureDate+"-"+object+"-"+angle+"-Capt-01-50m"+File.separator+captureDate+"-"+object+"-"+angle+"-Capt-01-50m+FLA_011.tif");
			cropObject();
			rename("Flash"+angle);
			run("32-bit");
			if (object=="Mask") multiplier = 0.003;
			else if (object=="Sold") multiplier = 0.0025;
			else if (object=="Ant2") multiplier = 0.003; //formerly 0.002 but visualize normals showed diminished results
			run("Multiply...", "value="+multiplier);
			multiplierString = toString(multiplier);
			multiplierString = replace(multiplierString,"0\\.","");
			setMinAndMax(0, 100);
			run("Concatenate...", "  title=[LAB] keep image1=Flash"+angle+" image2=G-shift image3=B-shift image4=[-- None --]");
			run("CIE L*a*b* stack to RGB");
			rotateObject();
			saveAs("jpeg", pathRoot+"Process"+File.separator+"45CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports"+File.separator+year+month+dayOfMonth+"-"+object+"-"+angle+"-"+processType+"-03-50m-"+multiplierString+".jpg");
			print(lpFile,pathRoot+"Process"+File.separator+"45CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports"+File.separator+year+month+dayOfMonth+"-"+object+"-"+angle+"-"+processType+"-03-50m-"+multiplierString+".jpg\n");
			close();
			close("LAB");
			close("Flash"+angle);
			call("java.lang.System.gc");
		}
		close("?-shift");
	}
	else if (processType == "Accb")
	{
		print("Accb not yet written");
	}
	else if (processType == "Pcaa") //Pcaa is the 45 capture process, Todd's PCA pseudocolor generated fromY=Flash Cb=Pc2 Cr=Pc3 from eureka lights
	{
		if (!File.exists(pathRoot+"Process"+File.separator+"45CaptureProcess"+File.separator+object+File.separator+processType)) File.makeDirectory(pathRoot+"Process"+File.separator+"45CaptureProcess"+File.separator+object+File.separator+processType);
		if (!File.exists(pathRoot+"Process"+File.separator+"45CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports")) File.makeDirectory(pathRoot+"Process"+File.separator+"45CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports");
		if (object == "Pal1") objectFS = "PAL1"; 
		else if (object == "Pal3") objectFS = "PAL3";
		else objectFS = object;
		if (object=="Mask") open(pathRoot+"Process"+File.separator+"9CaptureProcess"+File.separator+object+File.separator+captureDate+"-"+object+"-00-Capt-01-50m_bands01-12_RF_cal_PCA_region_PC2_imglin2.tif");
		else if (object == "Pal3") open(pathRoot+"Process"+File.separator+"20130913-Easton"+File.separator+objectFS+"-00"+File.separator+"PCA"+File.separator+"PAL3-00_bands01-09_cal_textblock_without-rubric_stats_PCA_1.img.tif");
		else open(pathRoot+"Process"+File.separator+"9CaptureProcess"+File.separator+object+File.separator+captureDate+"-"+object+"-00-Capt-01-50m_12bands_cal_figure_stats_PC2_roi.tif");
		enhanceObject();
		cropObject();
		rename("PCA2");
		if (object=="Mask") open(pathRoot+"Process"+File.separator+"9CaptureProcess"+File.separator+object+File.separator+captureDate+"-"+object+"-00-Capt-01-50m_bands01-12_RF_cal_PCA_region_PC3_imglin2.tif");
		else if (object == "Pal3") open(pathRoot+"Process"+File.separator+"20130913-Easton"+File.separator+objectFS+"-00"+File.separator+"PCA"+File.separator+"PAL3-00_bands01-09_cal_textblock_without-rubric_stats_PCA_3.img.tif");
		else open(pathRoot+"Process"+File.separator+"9CaptureProcess"+File.separator+object+File.separator+captureDate+"-"+object+"-00-Capt-01-50m_12bands_cal_figure_stats_PC3_roi.tif");
		enhanceObject();
		cropObject();
		rename("PCA3");
		for (direction=startDirection; direction <= directions; direction++) {
			if (direction < 10) {
				angle = "0" + toString(direction);
			} else {
				angle = toString(direction);
			}
			if (object == "Sold") angle = replace(angle,"01","01b"); //exception
			//open(pathRoot+"Capture"+File.separator+"Processed"+File.separator+captureDate+"-"+objectFS+"-"+angle+"-Capt-01-50m"+File.separator+captureDate+"-"+objectFS+"-"+angle+"-Capt-01-50m+FLA-_011.tif"); //hyphen in FLA-_011 varies
			open(pathRoot+"Capture"+File.separator+"Processed"+File.separator+captureDate+"-"+objectFS+"-"+angle+"-Capt-01-50m"+File.separator+captureDate+"-"+objectFS+"-"+angle+"-Capt-01-50m+WHI-_010.tif"); //White
			enhanceObject();
			cropObject();
			rename("Flash"+angle);
			run("Concatenate...", "  title=[YCC] keep image1=Flash"+angle+" image2=PCA2 image3=PCA3 image4=[-- None --]");
			run("YCbCr stack to RGB");
			rotateObject();
			saveAs("jpeg", pathRoot+"Process"+File.separator+"45CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports"+File.separator+year+month+dayOfMonth+"-"+object+"-"+angle+"-"+processType+"-03-50m-WHI.jpg"); //White
			close();
			close("YCC");
			close("Flash"+angle);
		}
		close("PCA?");
	}
	else if (processType == "Pcab") //Pcab is the 45 capture process, Roger's PCA pseudocolor in LAB from Eureka lights with Flash as L for each angle
	{
		print("Pcab not yet written: don't have Eurekalight Pseudocolor");
	}
	else if (processType == "Pcac") //Pcac is the 324 capture process, Roger's PCA pseudocolor generated at each angle using global stats (based on Eureka00)
	{
		//question about RL00... means the red channel is luminance based on what? ... eureka lights!
		if (!File.exists(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType)) File.makeDirectory(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType);
		if (!File.exists(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports")) File.makeDirectory(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports");
		for (direction=startDirection; direction <= directions; direction++) {
			if (direction < 10) {
				angle = "0" + toString(direction);
			} else {
				angle = toString(direction);
			}
			if (object == "Sold") angle = replace(angle,"01","01b"); //exception
			if (object == "Sold") angle = replace(angle,"04","04a"); //exception
			open(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+"PCAPseudocolor-Eureka00_stats-RGB"+File.separator+captureDate+"-"+object+"-"+angle+"-Capt-01-50m_RL00_GPC2_BPC3_roi.tif");
			cropObject();
			rotateObject();
			saveAs("jpeg",pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports"+File.separator+year+month+dayOfMonth+"-"+object+"-"+angle+"-"+processType+"-03-50m.jpg");
			close();
		}
	} 
	else if (processType == "Pcad") //Pcad is the 324 capture process, Roger's PCA pseudocolor generated at each angle using local stats 
	{
		print("Pcad not yet written: don't have local stats");
	}
	else if (processType == "Pcae") //Pcae is the 324 capture process, Todd's PCA pseudocolor generated from Y=localPc1 Cb=localPc2 Cr=localPc3 from global stats
	{
		if (!File.exists(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType)) File.makeDirectory(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType);
		if (!File.exists(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports")) File.makeDirectory(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports");
		for (direction=startDirection; direction <= directions; direction++) {
			if (direction < 10) {
				angle = "0" + toString(direction);
			} else {
				angle = toString(direction);
			}
			if (object == "Sold") angle = replace(angle,"01","01b"); //exception
			if (object == "Pal1") objectFS = "PAL1"; 
			else if (object == "Pal3") objectFS = "PAL3";
			else objectFS = object;
			//Y = Pc0
			open(pathRoot+"Process"+File.separator+"20130913-Easton"+File.separator+objectFS+"-"+angle+File.separator+"PCA"+File.separator+"Global"+File.separator+objectFS+"-"+angle+"_bands01-09_cal_textblock_without-rubric_stats_global_PCA_0.img.tif");
			if (object == "Pal3") makeRectangle(4428,1989,924,1686);
			run("Enhance Contrast...", "saturated=0.4 normalize"); 
			run("8-bit");
			cropObject();
			if (getPixel(4338,1584) < 128) run("Invert"); //standardize light text on dark for Pal3
			rename("Y");
			//Cb = Pc1
			open(pathRoot+"Process"+File.separator+"20130913-Easton"+File.separator+objectFS+"-"+angle+File.separator+"PCA"+File.separator+"Global"+File.separator+objectFS+"-"+angle+"_bands01-09_cal_textblock_without-rubric_stats_global_PCA_1.img.tif");
			if (object == "Pal3") makeRectangle(4428,1989,924,1686);
			run("Enhance Contrast...", "saturated=0.4 normalize"); 
			run("8-bit");
			cropObject();
			if (getPixel(4338,1584) < 128) run("Invert"); //standardize light text on dark for Pal3
			rename("Cb");
			//Cr = Pc2
			open(pathRoot+"Process"+File.separator+"20130913-Easton"+File.separator+objectFS+"-"+angle+File.separator+"PCA"+File.separator+"Global"+File.separator+objectFS+"-"+angle+"_bands01-09_cal_textblock_without-rubric_stats_global_PCA_2.img.tif");
			if (object == "Pal3") makeRectangle(4428,1989,924,1686);
			run("Enhance Contrast...", "saturated=0.4 normalize"); 
			run("8-bit");
			cropObject();
			if (getPixel(4338,1584) < 128) run("Invert"); //standardize light text on dark for Pal3
			rename("Cr");
			//Concatenate, convert, and save
			run("Concatenate...", "  title=[YCC] image1=Y image2=Cb image3=Cr image4=[-- None --]");
			run("YCbCr stack to RGB");
			rotateObject();
			saveAs("jpeg", pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports"+File.separator+year+month+dayOfMonth+"-"+object+"-"+angle+"-"+processType+"-03-50m-LightText-012.jpg"); //light text variant
			close("YCC");
		}
	}
	else if (processType == "Pcaf") //Pcaf is the 324 capture process, Todd's PCA pseudocolor generated from Y=localPc0 Cb=localPc1 Cr=localPc2 from local stats
	{
		if (!File.exists(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType)) File.makeDirectory(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType);
		if (!File.exists(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports")) File.makeDirectory(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports");
		for (direction=startDirection; direction <= directions; direction++) {
			if (direction < 10) {
				angle = "0" + toString(direction);
			} else {
				angle = toString(direction);
			}
			if (object == "Sold") angle = replace(angle,"01","01b"); //exception
			if (object == "Pal1") objectFS = "PAL1"; 
			else if (object == "Pal3") objectFS = "PAL3";
			else objectFS = object;
			//Y = Pc0
			open(pathRoot+"Process"+File.separator+"20130913-Easton"+File.separator+objectFS+"-"+angle+File.separator+"PCA"+File.separator+"Individual"+File.separator+objectFS+"-"+angle+"_bands01-09_cal_textblock_without-rubric_stats_individual_PCA_0.img.tif");
			enhanceObject();
			cropObject();
			if (getPixel(4338,1584) < 128) run("Invert"); //standardize light text on dark for Pal3
			rename("Y");
			//Cb = Pc1
			open(pathRoot+"Process"+File.separator+"20130913-Easton"+File.separator+objectFS+"-"+angle+File.separator+"PCA"+File.separator+"Individual"+File.separator+objectFS+"-"+angle+"_bands01-09_cal_textblock_without-rubric_stats_individual_PCA_1.img.tif");
			if (object == "Pal3") makeRectangle(4428,1989,924,1686);
			run("Enhance Contrast...", "saturated=0.4 normalize"); 
			run("8-bit");
			cropObject();
			if (getPixel(4338,1584) < 128) run("Invert"); //standardize light text on dark for Pal3
			rename("Cb");
			//Cr = Pc2
			open(pathRoot+"Process"+File.separator+"20130913-Easton"+File.separator+objectFS+"-"+angle+File.separator+"PCA"+File.separator+"Individual"+File.separator+objectFS+"-"+angle+"_bands01-09_cal_textblock_without-rubric_stats_individual_PCA_2.img.tif");
			if (object == "Pal3") makeRectangle(4428,1989,924,1686);
			run("Enhance Contrast...", "saturated=0.4 normalize"); 
			run("8-bit");
			cropObject();
			if (getPixel(4338,1584) < 128) run("Invert"); //standardize light text on dark for Pal3
			rename("Cr");
			//Concatenate, convert, and save
			run("Concatenate...", "  title=[YCC] image1=Y image2=Cb image3=Cr image4=[-- None --]");
			run("YCbCr stack to RGB");
			rotateObject();
			saveAs("jpeg", pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports"+File.separator+year+month+dayOfMonth+"-"+object+"-"+angle+"-"+processType+"-03-50m-LightText.jpg"); //light text variant
			close("YCC");
		}
	} 
	else if (processType == "Pcag") //Pcag is the 324 capture process, Y=localAvgL Cb=localPc2 Cr=localPc3 from local global
	{
		if (!File.exists(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType)) File.makeDirectory(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType);
		if (!File.exists(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports")) File.makeDirectory(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports");
		for (direction=startDirection; direction <= directions; direction++) {
			if (direction < 10) {
				angle = "0" + toString(direction);
			} else {
				angle = toString(direction);
			}
			if (object == "Sold") angle = replace(angle,"01","01b"); //exception
			//calculate luminance from average intensity of nine bands, normalized
			imageSequenceArg = "open="+pathRoot+"Capture"+File.separator+"Processed"+File.separator+captureDate+"-"+object+"-"+angle+"-Capt-01-50m"+File.separator+captureDate+"-"+object+"-"+angle+"-Capt-01-50m+446_002.tif number=9 starting=1 increment=1 scale=100 file=(\\+[0-9]) sort use";
			run("Image Sequence...", imageSequenceArg);
			cropObject();
			run("Z Project...", "start=1 stop=9 projection=[Average Intensity]");
			close(captureDate+"-"+object+"-"+angle+"-Capt-01-50m");
			run("Enhance Contrast...", "saturated=0.4 normalize"); 
			run("8-bit");
			rename("Y");
			//use pc2 as Cb
			open(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+"PCA"+File.separator+captureDate+"-"+object+"-"+angle+"-Capt-01-50m_cal_figure_stats_PC2_roi.tif");
			cropObject();
			run("8-bit");
			rename("Cb");
			//use pc3 as Cr
			open(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+"PCA"+File.separator+captureDate+"-"+object+"-"+angle+"-Capt-01-50m_cal_figure_stats_PC3_roi.tif");
			cropObject();
			run("8-bit");
			rename("Cr");
			//combine
			run("Concatenate...", "  title=[YCC] image1=Y image2=Cb image3=Cr image4=[-- None --]");
			run("YCbCr stack to RGB");
			rotateObject();
			saveAs("jpeg", pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports"+File.separator+year+month+dayOfMonth+"-"+object+"-"+angle+"-"+processType+"-03-50m.jpg");
			close("YCC");
		}
	}
	else if (processType == "Suva") //Suva is Simply UltraViolet, normalized separately at each angle to 0.1% saturation
	{
		if (!File.exists(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType)) File.makeDirectory(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType);
		if (!File.exists(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports")) File.makeDirectory(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports");
		for (direction=startDirection; direction <= directions; direction++) {
			if (direction < 10) {
				angle = "0" + toString(direction);
			} else {
				angle = toString(direction);
			}
			if (object == "Sold") angle = replace(angle,"01","01b"); //exception
			open(pathRoot+"Capture"+File.separator+"Processed"+File.separator+captureDate+"-"+object+"-"+angle+"-Capt-01-50m"+File.separator+captureDate+"-"+object+"-"+angle+"-Capt-01-50m+393_001.tif");
			cropObject();
			run("Enhance Contrast...", "saturated=0.1 normalize"); 
			run("8-bit");
			rotateObject();
			rename("Ruv");
			run("Duplicate...", "title=Guv");
			run("Duplicate...", "title=Buv");
			run("Concatenate...", "  title=[RGB] image1=Ruv image2=Guv image3=Buv image4=[-- None --]");
			run("Stack to RGB");
			saveAs("jpeg", pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports"+File.separator+year+month+dayOfMonth+"-"+object+"-"+angle+"-"+processType+"-03-50m.jpg");
			close();
		}
	}
	else if (processType == "Igua") //Igua is Infrared-529-Ultraviolet mapped to RGB, normalized separately at each angle to 0.1% saturation
	{
		if (!File.exists(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType)) File.makeDirectory(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType);
		if (!File.exists(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports")) File.makeDirectory(pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports");
		for (direction=startDirection; direction <= directions; direction++) {
			if (direction < 10) {
				angle = "0" + toString(direction);
			} else {
				angle = toString(direction);
			}
			if (object == "Sold") angle = replace(angle,"01","01b"); //exception
			if (object == "Pal1") objectFS = "PAL1"; 
			if (object == "Pal3") objectFS = "PAL3"; 
			else objectFS = object;
			//IR
			open(pathRoot+"Capture"+File.separator+"Processed"+File.separator+captureDate+"-"+objectFS+"-"+angle+"-Capt-01-50m"+File.separator+captureDate+"-"+objectFS+"-"+angle+"-Capt-01-50m+830-_009.tif");
			enhanceObject();
			cropObject();
			//run("Enhance Contrast...", "saturated=0.1 normalize"); 
			//run("8-bit");
			rename("IR");
			//G = 529
			open(pathRoot+"Capture"+File.separator+"Processed"+File.separator+captureDate+"-"+objectFS+"-"+angle+"-Capt-01-50m"+File.separator+captureDate+"-"+objectFS+"-"+angle+"-Capt-01-50m+529-_005.tif");
			enhanceObject();
			cropObject();
			//run("Enhance Contrast...", "saturated=0.1 normalize"); 
			//run("8-bit");
			rename("G");
			//UV
			open(pathRoot+"Capture"+File.separator+"Processed"+File.separator+captureDate+"-"+objectFS+"-"+angle+"-Capt-01-50m"+File.separator+captureDate+"-"+objectFS+"-"+angle+"-Capt-01-50m+393-_001.tif");
			enhanceObject();
			cropObject();
			//run("Enhance Contrast...", "saturated=0.1 normalize"); 
			//run("8-bit");
			rename("UV");
			run("Concatenate...", "  title=[RGB] image1=IR image2=G image3=UV image4=[-- None --]");
			run("Stack to RGB");
			rotateObject();
			saveAs("jpeg", pathRoot+"Process"+File.separator+"324CaptureProcess"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports"+File.separator+year+month+dayOfMonth+"-"+object+"-"+angle+"-"+processType+"-03-50m-n.jpg");
			close();
		}
	}
	setBatchMode(false);
}

macro "Method 17 [7]" {
	//prompt user for object
	setBatchMode(true); 
	Dialog.create("Confirm information");
	Dialog.addMessage("Confirm the following");
	Dialog.addString("General Project Path: ", File.separator+"home"+File.separator+"faculty"+File.separator+"thanneken"+File.separator+"Projects"+File.separator+"Integrating"+File.separator, 40);
	var objects = newArray("Mask", "Sold", "Pal1", "Ant2", "Pal3", "Ant3", "Pal2");
	Dialog.addRadioButtonGroup("Object: ", objects, 4, 2, "Pal3");
	Dialog.addString("Start Direction: ", "1",2);
	Dialog.addString("Direction Count: ","5",2);
	Dialog.show();
	var pathRoot = Dialog.getString();
	var object = Dialog.getRadioButton();
	var startDirection = Dialog.getString();
	var directions = Dialog.getString();
	//dates
	getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
	year = toString(year);
	if (month < 9) month = "0" + toString(++month);
	else month = toString(++month);
	if (dayOfMonth < 10) dayOfMonth = "0" + toString(dayOfMonth);
	else dayOfMonth = toString(dayOfMonth);
	if (object == "Mask") captureDate = "20130722";
	else if (object == "Sold") captureDate = "20130723";
	else if (object == "Pal1") captureDate = "20130725";
	else if (object == "Ant2") captureDate = "20130724";
	else if (object == "Pal3") captureDate = "20130726";
	else if (object == "Ant3") captureDate = "20130726";
	else if (object == "Pal2") captureDate = "20130725";
	//prompt user for roi
	if (!isOpen("Preview"))	{
		open (pathRoot+"Curated"+File.separator+object+File.separator+"Capt"+File.separator+captureDate+"-"+object+"-393-01-Capt-01-50m-001.tif");
		rename("Preview");
		rotateObject();
		run("Enhance Contrast...", "saturated=0.4 normalize");
		setBatchMode(false); //there is a better way with newer versions of imagej
		waitForUser("Select ROI", "Select a Region of Interest (ROI) in the preview image");
	}
	getSelectionBounds(x, y, width, height);
	setBatchMode(true); //there is a better way with newer versions of imagej
	//prompt user for process, and contrast enhancement
	Dialog.create("Select Process");
	Dialog.addMessage("What would you like to do with this region of interest?");
	var processes = newArray("35xUV","35x9");
	Dialog.addRadioButtonGroup("Process: ", processes, 1, 2, "35xUV");
	Dialog.addCheckbox("Enhance",true) 
	Dialog.show();
	var process = Dialog.getRadioButton();
	var enhance = Dialog.getCheckbox();
	//35xflash
	if (process == "35xUV")
	{
		for (direction=startDirection; direction <= directions; direction++) {
			if (direction < 10) {
				angle = "0" + toString(direction);
			} else {
				angle = toString(direction);
			}
			open (pathRoot+"Curated"+File.separator+object+File.separator+"Capt"+File.separator+captureDate+"-"+object+"-393-"+angle+"-Capt-01-50m-001.tif");
			makeRectangle(getWidth()-y-height, x, height, width);
			run("Crop");
			if (enhance) run("Enhance Contrast...", "saturated=0.4 normalize");
		}
	}
	//35x9
	else if (process == "35x9")
	{
		fileList = getFileList(pathRoot+"Curated"+File.separator+object+File.separator+"Capt");
		for (i=0;i<fileList.length;i++)
		{
			print("Considered "+fileList[i]);
			if (matches(fileList[i],".+50m-00[1-9]\.tif"))
			{
				print("Approved "+fileList[i]);
				open(pathRoot+"Curated"+File.separator+object+File.separator+"Capt"+File.separator+fileList[i]);
				makeRectangle(getWidth()-y-height, x, height, width);
				run("Crop");
				if (enhance) run("Enhance Contrast...", "saturated=0.4 normalize");
			}
		}
	}
	//could show user top 6 principal components, prompt to close 3
	//map remaining 3 to ycc, convert to rgb
	run("Images to Stack", "name=Stack title=["+captureDate+"] use");
	//print(nSlices);
	selectWindow("Stack");
	run("PCA ");
	selectWindow("Eigenvalue spectrum of Stack");
	run("Close");
	selectWindow("Stack");
	run("Close");
	selectWindow("PCA of Stack");
	run("Slice Keeper", "first=1 last=3 increment=1"); //could increase if want to examine more than 3
	selectWindow("PCA of Stack");
	run("Close");
	//setBatchMode("exit and display"); //
	//waitForUser("Select 3", "Reduce to 3 best principal components");//
	selectWindow("PCA of Stack kept stack");
	run("8-bit");
	run("YCbCr stack to RGB");
	selectWindow("PCA of Stack kept stack");
	run("Close");
	selectWindow("PCA of Stack kept stack - RGB");
	rename(year+month+dayOfMonth+"-"+object+"-99-"+process+"-03-50m");
	rotateObject();
	setBatchMode(false);
}
macro "Method 18 [8]" {
	//Method 18 revises method 16 to use curated files and a more straightforward prompting structure
	setBatchMode(true); 
	Dialog.create("Confirm information");
	Dialog.addMessage("Confirm the following");
	Dialog.addString("General Project Path: ", File.separator+"home"+File.separator+"faculty"+File.separator+"thanneken"+File.separator+"Projects"+File.separator+"Integrating"+File.separator, 20);
	var objects = newArray("Mask", "Sold", "Pal1", "Ant2", "Pal3", "Ant3", "Pal2");
	Dialog.addRadioButtonGroup("Object: ", objects, 4, 2, "Pal1");
	var processTypes1 = newArray("Accurate Color","Pseudocolor","eXtended Color"); //"Simple UV","Infrared-Green-Ultraviolet"
	Dialog.addRadioButtonGroup("Processing Category: ",processTypes1,2,2,"Pseudocolor");
	var processTypes2 = newArray("Eureka Lights","Magic Flashlight");
	Dialog.addRadioButtonGroup("Chrominance Source: ",processTypes2,1,2,"Eureka Lights");
	var processTypes3 = newArray("ICA","PCA");
	Dialog.addRadioButtonGroup("Analysis type: ",processTypes3,1,2,"PCA");
	var processTypes4 = newArray("Global","Local");
	Dialog.addRadioButtonGroup("Stats basis: ",processTypes4,1,2,"Local");
	Dialog.addString("Start Direction: ", "1",2);
	Dialog.addString("Direction Count: ","1",2);
	Dialog.show();
	var pathRoot = Dialog.getString();
	var object = Dialog.getRadioButton();
	var process1 = Dialog.getRadioButton();
		if (process1 == "Accurate Color") process1 = "A";
		else if (process1 == "Pseudocolor") process1 = "P";
		else if (process1 == "eXtended Color") process1 = "X";
	var process2 = Dialog.getRadioButton();
		if (process2 == "Eureka Lights") process2 = "e";
		else if (process2 == "Magic Flashlight") process2 = "m";
	var process3 = Dialog.getRadioButton();
		if (process3 == "ICA") process3 = "i";
		else if (process3 == "PCA") process3 = "p";
	var process4 = Dialog.getRadioButton(); 
		if (process4 == "Global") process4 = "g";
		else if (process4 == "Local") process4 = "l";
	if (process1 == "X") process4 = "n";
	if (process1 == "A") processType = "Acce";
	else processType = process1+process2+process3+process4;
	var startDirection = Dialog.getString();
	var directions = Dialog.getString();
	getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
	year = toString(year);
	if (month < 9) month = "0" + toString(++month);
	else month = toString(++month);
	if (dayOfMonth < 10) dayOfMonth = "0" + toString(dayOfMonth);
	else dayOfMonth = toString(dayOfMonth);
	if (object == "Mask") captureDate = "20130722";
	else if (object == "Sold") captureDate = "20130723";
	else if (object == "Pal1") captureDate = "20130725";
	else if (object == "Ant2") captureDate = "20130724";
	else if (object == "Pal3") captureDate = "20130726";
	else if (object == "Ant3") captureDate = "20130726";
	else if (object == "Pal2") captureDate = "20130725";
	if (!File.exists(pathRoot+"Curated"+File.separator+object+File.separator+processType)) File.makeDirectory(pathRoot+"Curated"+File.separator+object+File.separator+processType);
	if (!File.exists(pathRoot+"Curated"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports")) File.makeDirectory(pathRoot+"Curated"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports");
	if (process1 == "P") //Pseudocolor PTM
	{
		if (process3 == "i") sourceMethod = "Ica";
		else if (process3 == "p") sourceMethod = "Pca";
		if (process2 == "e") //Chrominance from Eureka lights
		{
			open(pathRoot+"Curated"+File.separator+object+File.separator+sourceMethod+process4+File.separator+captureDate+"-"+object+"-Hyb-00-"+sourceMethod+process4+"-01-50m-1.tif"); //1
			enhanceObject();
			cropObject();
			rename("Chrominance1");
			open(pathRoot+"Curated"+File.separator+object+File.separator+sourceMethod+process4+File.separator+captureDate+"-"+object+"-Hyb-00-"+sourceMethod+process4+"-01-50m-2.tif"); //2
			enhanceObject();
			cropObject();
			rename("Chrominance2");
			for (direction=startDirection; direction <= directions; direction++) {
				if (direction < 10) {
					angle = "0" + toString(direction);
				} else {
					angle = toString(direction);
				}
				open(pathRoot+"Curated"+File.separator+object+File.separator+"Capt"+File.separator+captureDate+"-"+object+"-Fla-"+angle+"-Capt-01-50m-011.tif"); //luminance from Flash
//				open(pathRoot+"Curated"+File.separator+object+File.separator+"Capt"+File.separator+captureDate+"-"+object+"-393-"+angle+"-Capt-01-50m-001.tif"); //luminance from uv
				enhanceObject();
				cropObject();
				rename("Luminance"+angle);
				run("Concatenate...", "  title=[YCC] keep image1=Luminance"+angle+" image2=Chrominance1 image3=Chrominance2 image4=[-- None --]");
				run("YCbCr stack to RGB");
				rotateObject();
				saveAs("jpeg", pathRoot+"Curated"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports"+File.separator+year+month+dayOfMonth+"-"+object+"-045-"+angle+"-"+processType+"-03-50m.jpg"); //variants: -yccf01, -yccf34 (unspecified default is yccf12)
				run("Close");
				selectWindow("YCC");
				run("Close");
				selectWindow("Luminance"+angle);
				run("Close");
			}
			selectWindow("Chrominance1");
			run("Close");
			selectWindow("Chrominance2");
			run("Close");
		}
		else if (process2 == "m") //Chrominance from Magic flashlight
		{
			for (direction=startDirection; direction <= directions; direction++) {
				if (direction < 10) {
					angle = "0" + toString(direction);
				} else {
					angle = toString(direction);
				}
				//Luminance from first component
				open(pathRoot+"Curated"+File.separator+object+File.separator+sourceMethod+process4+File.separator+captureDate+"-"+object+"-Hyb-"+angle+"-"+sourceMethod+process4+"-01-50m-0.tif"); 
/*				if (object=="Pal1")
				{
					if (getPixel(5869,1092)<getPixel(5885,1092)) 	run("Invert");
				}
*/				enhanceObject();
				cropObject();
				rename("Luminance"+angle);
				//Chrominance 1 from second component
				open(pathRoot+"Curated"+File.separator+object+File.separator+sourceMethod+process4+File.separator+captureDate+"-"+object+"-Hyb-"+angle+"-"+sourceMethod+process4+"-01-50m-1.tif"); 
/*				if (object=="Pal1")
				{
					if (getPixel(5869,1092)<getPixel(5885,1092)) 	run("Invert");
				}
*/				enhanceObject();
				cropObject();
				rename("Chrominance1"+angle);
				//Chrominance 2 from third component
				open(pathRoot+"Curated"+File.separator+object+File.separator+sourceMethod+process4+File.separator+captureDate+"-"+object+"-Hyb-"+angle+"-"+sourceMethod+process4+"-01-50m-2.tif"); 
/*				if (object=="Pal1")
				{
					if (getPixel(5869,1092)<getPixel(5885,1092)) 	run("Invert");
				}
*/				enhanceObject();
				cropObject();
				rename("Chrominance2"+angle);
				//YCC BASE
					run("Concatenate...", "  title=[YCC] keep image1=Luminance"+angle+" image2=Chrominance1"+angle+" image3=Chrominance2"+angle+" image4=[-- None --]");
					run("YCbCr stack to RGB");
					rotateObject();
					saveAs("jpeg", pathRoot+"Curated"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports"+File.separator+year+month+dayOfMonth+"-"+object+"-324-"+angle+"-"+processType+"-03-50m.jpg"); //-lighttext1 / 3
				//RGB210 VARIANT
/*					run("Concatenate...", "  title=[YCC] keep image1=Chrominance2"+angle+" image2=Chrominance1"+angle+" image3=Luminance"+angle+" image4=[-- None --]");
					run("Stack to RGB");
					rotateObject();
					saveAs("jpeg", pathRoot+"Curated"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports"+File.separator+year+month+dayOfMonth+"-"+object+"-324-"+angle+"-"+processType+"-03-50m-rgb210.jpg"); //-lighttext1 / 3
*/				run("Close");
				selectWindow("YCC");
				run("Close");
				selectWindow("Luminance"+angle);
				run("Close");
				selectWindow("Chrominance1"+angle);
				run("Close");
				selectWindow("Chrominance2"+angle);
				run("Close");
			}
		}
	}
	else if (process1 == "X") //Extended color
	{
		if (process2 == "m") //Chrominance from Magic flashlight
		{
			for (direction=startDirection; direction <= directions; direction++) {
				if (direction < 10) {
					angle = "0" + toString(direction);
				} else {
					angle = toString(direction);
				}
				//R from PCA of bands 7-9
				run("Image Sequence...", "open="+pathRoot+File.separator+"Curated"+File.separator+object+File.separator+"Capt/ number=3 starting=1 increment=1 scale=100 file=("+angle+"-Capt-01-50m-00[7-9]) sort");
				selectPCAFocus();
				run("PCA ");
				selectWindow("Eigenvalue spectrum of Capt");
				run("Close");
				selectWindow("Capt");
				run("Close");
				selectWindow("PCA of Capt");
				run("Slice Keeper", "first=1 last=1 increment=1");
				selectWindow("PCA of Capt");
				run("Close");
				selectWindow("PCA of Capt kept stack");
				rename("R");
				cropObject();
				run("Enhance Contrast...", "saturated=0.4 normalize"); 
				run("8-bit");
				//G from PCA of bands 4-6
				run("Image Sequence...", "open="+pathRoot+File.separator+"Curated"+File.separator+object+File.separator+"Capt/ number=3 starting=1 increment=1 scale=100 file=("+angle+"-Capt-01-50m-00[4-6]) sort");
				selectPCAFocus();
				run("PCA ");
				selectWindow("Eigenvalue spectrum of Capt");
				run("Close");
				selectWindow("Capt");
				run("Close");
				selectWindow("PCA of Capt");
				run("Slice Keeper", "first=1 last=1 increment=1");
				selectWindow("PCA of Capt");
				run("Close");
				selectWindow("PCA of Capt kept stack");
				rename("G");
				cropObject();
				run("Enhance Contrast...", "saturated=0.4 normalize"); 
				run("8-bit");
				//B from PCA of bands 1-3
				run("Image Sequence...", "open="+pathRoot+File.separator+"Curated"+File.separator+object+File.separator+"Capt/ number=3 starting=1 increment=1 scale=100 file=("+angle+"-Capt-01-50m-00[1-3]) sort");
				selectPCAFocus();
				run("PCA ");
				selectWindow("Eigenvalue spectrum of Capt");
				run("Close");
				selectWindow("Capt");
				run("Close");
				selectWindow("PCA of Capt");
				run("Slice Keeper", "first=1 last=1 increment=1");
				selectWindow("PCA of Capt");
				run("Close");
				selectWindow("PCA of Capt kept stack");
				rename("B");
				cropObject();
				run("Enhance Contrast...", "saturated=0.4 normalize"); 
				run("8-bit");
				//Save as rgb jpg
				run("Concatenate...", "  title=[Stack] image1=R image2=G image3=B image4=[-- None --]");
				run("Stack to RGB");
				selectWindow("Stack");
				run("Close");
				selectWindow("Stack (RGB)");
				rotateObject();
				saveAs("jpeg", pathRoot+"Curated"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports"+File.separator+year+month+dayOfMonth+"-"+object+"-324-"+angle+"-"+processType+"-03-50m-n.jpg"); //n for normalized
				run("Close");
			}
		}
		else if (process2 == "e") //Chrominance from Eureka Lights
		{
			//R from PCA of bands 09-12
			run("Image Sequence...", "open="+pathRoot+File.separator+"Curated"+File.separator+object+File.separator+"Capt/ number=4 starting=1 increment=1 scale=100 file=((009)|(010)|(011)|(012)-00-Capt) sort");
			selectPCAFocus();
			run("PCA ");
			selectWindow("Eigenvalue spectrum of Capt");
			run("Close");
			selectWindow("Capt");
			run("Close");
			selectWindow("PCA of Capt");
			run("Slice Keeper", "first=1 last=1 increment=1");
			selectWindow("PCA of Capt");
			run("Close");
			selectWindow("PCA of Capt kept stack");
			rename("R");
			cropObject();
			run("Enhance Contrast...", "saturated=0.4 normalize"); 
			run("8-bit");
			//G from PCA of bands 5-8
			run("Image Sequence...", "open="+pathRoot+File.separator+"Curated"+File.separator+object+File.separator+"Capt/ number=4 starting=1 increment=1 scale=100 file=(00[5-8]-00-Capt) sort");
			selectPCAFocus();
			run("PCA ");
			selectWindow("Eigenvalue spectrum of Capt");
			run("Close");
			selectWindow("Capt");
			run("Close");
			selectWindow("PCA of Capt");
			run("Slice Keeper", "first=1 last=1 increment=1");
			selectWindow("PCA of Capt");
			run("Close");
			selectWindow("PCA of Capt kept stack");
			rename("G");
			cropObject();
			run("Enhance Contrast...", "saturated=0.4 normalize"); 
			run("8-bit");
			//B from PCA of bands 1-4
			run("Image Sequence...", "open="+pathRoot+File.separator+"Curated"+File.separator+object+File.separator+"Capt/ number=4 starting=1 increment=1 scale=100 file=(00[1-4]-00-Capt) sort");
			selectPCAFocus();
			run("PCA ");
			selectWindow("Eigenvalue spectrum of Capt");
			run("Close");
			selectWindow("Capt");
			run("Close");
			selectWindow("PCA of Capt");
			run("Slice Keeper", "first=1 last=1 increment=1");
			selectWindow("PCA of Capt");
			run("Close");
			selectWindow("PCA of Capt kept stack");
			rename("B");
			cropObject();
			run("Enhance Contrast...", "saturated=0.4 normalize"); 
			run("8-bit");
			run("Concatenate...", "  title=[Stack] image1=R image2=G image3=B image4=[-- None --]");
			run("Stack to RGB");
			selectWindow("Stack");
			run("Close");
			selectWindow("Stack (RGB)");
			run("RGB to YCbCr stack");
			run("Stack to Images");
			selectWindow("Y");
			run("Close");
			selectWindow("Stack (RGB)");
			run("Close");
			//luminance from flash
			for (direction=startDirection; direction <= directions; direction++) {
				if (direction < 10) {
					angle = "0" + toString(direction);
				} else {
					angle = toString(direction);
				}
				open(pathRoot+"Curated"+File.separator+object+File.separator+"Capt"+File.separator+captureDate+"-"+object+"-Fla-"+angle+"-Capt-01-50m-011.tif"); 
				enhanceObject();
				cropObject();
				rename("Luminance"+angle);
				run("Concatenate...", "  title=[YCC] keep image1=Luminance"+angle+" image2=Cb image3=Cr image4=[-- None --]");
				run("YCbCr stack to RGB");
				rotateObject();
				saveAs("jpeg", pathRoot+"Curated"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports"+File.separator+year+month+dayOfMonth+"-"+object+"-045-"+angle+"-"+processType+"-03-50m-n.jpg");//n for normalized
				run("Close");
				selectWindow("YCC");
				run("Close");
				selectWindow("Luminance"+angle);
				run("Close");
			}
			selectWindow("Cb");
			run("Close");
			selectWindow("Cr");
			run("Close");
		}
	}
	else if (process1 == "A") //Accurate color Acce (no macro needed for Accm)
	{
		//Chrominance from Eureka Lights
		Dialog.create("Select normalization method");
		Dialog.addMessage("Select normalization method");
		var normalizationMethods = newArray("Object Area", "MacBeth Chart", "Whole Field", "Fixed Value");
		Dialog.addRadioButtonGroup("Method: ", normalizationMethods, 2, 2, "Object Area");
		Dialog.addString("Fixed value", "1.20",3);
		Dialog.show();
		var normalizationMethod = Dialog.getRadioButton();
		var normalizationFixedValue = Dialog.getString();
		if (object == "Pal1") acccDate = "20140218";
		else if (object == "Mask") acccDate = "20140217";
		else if (object == "Sold") acccDate = "20140220";
		else if (object == "Ant2") acccDate = "20140224";
		else if (object == "Ant3") acccDate = "20140224";
		else exit("Accc date not defined for object");
		open(pathRoot+"Curated"+File.separator+object+File.separator+"Accc"+File.separator+acccDate+"-"+object+"-00-Accc-03-50m.tif"); 
		rename("RGBtiff");
		run("RGB to YCbCr stack");
		run("Stack to Images");
		selectWindow("Y");
		run("Close");
		selectWindow("RGBtiff");
		run("Close");
		//Luminance from flash at 35 angles
		for (direction=startDirection; direction <= directions; direction++) 
		{
			if (direction < 10) {
				angle = "0" + toString(direction);
			} else {
				angle = toString(direction);
			}
			open(pathRoot+"Curated"+File.separator+object+File.separator+"Capt"+File.separator+captureDate+"-"+object+"-Fla-"+angle+"-Capt-01-50m-011.tif"); 
			rename("Luminance");
			if (normalizationMethod == "Object Area") //enhances contrast for each angle based on object area
			{
				enhanceObject();
				variant = ""; //in some versions may appear as "e"
			}
			else if (normalizationMethod == "MacBeth Chart")
			{
				enhanceMacbeth();
				variant = "-m";
			}
			else if (normalizationMethod == "Whole Field")
			{
				run("Enhance Contrast...", "saturated=0.4 normalize"); 
				variant = "-w";
			}
			else if (normalizationMethod == "Fixed Value")
			{
				run("Multiply...", "value="+normalizationFixedValue+"");
				variant = replace(normalizationFixedValue,"\\.","");
				variant = ("-"+variant);
			}
			run("8-bit");
			run("Concatenate...", "  title=[YCC] keep image1=Luminance image2=Cb image3=Cr image4=[-- None --]");
			run("YCbCr stack to RGB");
			//Save as jpeg
			cropObject();
			rotateObject();
			saveAs("jpeg", pathRoot+"Curated"+File.separator+object+File.separator+processType+File.separator+"jpeg-exports"+File.separator+year+month+dayOfMonth+"-"+object+"-045-"+angle+"-"+processType+"-03-50m"+variant+".jpg"); 
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
	}
	setBatchMode(false);
}

macro "Method 19 [9]" {
	setBatchMode(true); 
	//non-interactive definition of variables
	object="Pal1";
	processType="M19";
//	cordx=5466;
	cordx=5700;
	pathRoot=File.separator+"home"+File.separator+"faculty"+File.separator+"thanneken"+File.separator+"Projects"+File.separator+"Integrating"+File.separator;
	//dates
	getDateAndTime(year, month, dayOfWeek, dayOfMonth, hour, minute, second, msec);
	year = toString(year);
	if (month < 9) month = "0" + toString(++month);
	else month = toString(++month);
	if (dayOfMonth < 10) dayOfMonth = "0" + toString(dayOfMonth);
	else dayOfMonth = toString(dayOfMonth);
	if (object == "Mask") captureDate = "20130722";
	else if (object == "Sold") captureDate = "20130723";
	else if (object == "Pal1") captureDate = "20130725";
	else if (object == "Ant2") captureDate = "20130724";
	else if (object == "Pal3") captureDate = "20130726";
	else if (object == "Ant3") captureDate = "20130726";
	else if (object == "Pal2") captureDate = "20130725";
//	run("Image Sequence...", "open="+pathRoot+File.separator+"Curated"+File.separator+object+File.separator+"Capt/ number=403 starting=1 increment=1 scale=100 file=393");
	run("Image Sequence...", "open="+pathRoot+File.separator+"Curated"+File.separator+object+File.separator+"Capt/ number=403 starting=1 increment=1 scale=100 file=(20130725-Pal1-[3-8]) use");
	rename("35uv");
//	for (cordy=861; cordy <= 2585; cordy++) //640x480
//	for (cordy=900; cordy <= 3185; cordy++)  //240x240
//	for (cordy=900; cordy <= 3185; cordy+=240) //non-overlaping blocks of 240
	for (cordy=900; cordy <= 900; cordy+=240) //just first block
		{ 
		selectWindow("35uv");
//		run("Specify...", "width=480 height=640 x="+cordx+" y="+cordy+" slice=1");
		run("Specify...", "width=240 height=240 x="+cordx+" y="+cordy+" slice=1");
//		run("Duplicate...", "title=duplicate duplicate range=1-35");
		run("Duplicate...", "title=duplicate duplicate range=1-315");
		run("Enhance Contrast...", "saturated=0.4 normalize process_all");
		run("PCA ");
		selectWindow("Eigenvalue spectrum of duplicate");
		run("Close");
		selectWindow("duplicate");
		run("Slice Keeper", "first=1 last=3 increment=1");
		selectWindow("duplicate");
		run("Close");
		selectWindow("PCA of duplicate");
		run("Close");
		selectWindow("duplicate kept stack");
		run("Enhance Contrast...", "saturated=0.4 normalize process_all");
		run("8-bit");
		run("YCbCr stack to RGB");
		rotateObject();
//		saveAs("jpeg", pathRoot+"Curated"+File.separator+object+File.separator+processType+File.separator+year+month+dayOfMonth+"-"+object+"-035-99-"+processType+"-03-50m-"+cordx+"-"+cordy+".jpg");
		saveAs("jpeg", pathRoot+"Curated"+File.separator+object+File.separator+processType+File.separator+year+month+dayOfMonth+"-"+object+"-315-99-"+processType+"-03-50m-"+cordx+"-"+cordy+".jpg");
		run("Close");
		selectWindow("duplicate kept stack");
		run("Close"); 
	}
	setBatchMode(false);
}