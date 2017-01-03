# SpectralRTI_Toolkit
Process Spectral RTI Images in ImageJ

The toolkit processes the data from a Spectral RTI capture session. This data includes diffuse narrowband spectral images, monochrome RTI captures from 35 or more light positions, and an accurate color image. The toolkit guides processing of the light position (lp) file, and outputs to HSH RTI, PTM, and WebRTI formats. The base color processing options are Accurate Color, Extended Spectrum, and PCA Pseudocolor. The Toolkit is intended to be usable by general users without requiring any editing of text files, command line arguments, or regular expressions. For more information about Spectral RTI see [http://palimpsest.stmarytx.edu/integrating] (http://palimpsest.stmarytx.edu/integrating). 

##Version History 
Version 0.1 is an ImageJ Macro.  

##Roadmap
The major development goals are: 

1. Rewrite as Java Plugin for ImageJ2, optimize for speed
2. Create documentation for basic users 

Additional plans include: 
* More helpful error messages (check dependencies, instructions for fixing errors)

Features added in the October 28, 2016 update:

* create jp2 files for IIIF repository
* option to create selectively static raking files directly to jp2 without jpeg compression 
* create WebRTI files
* consolidate prompts at beginning
* option to defer HSHfitter to batch command file
* human readable time stamps when renaming rather than overwriting files
* timestamp set once for entire sequence

Features added in the November 16, 2016 update:

* Better support for raking light options distinct from RTI
* Option to use transmissive light with each color process
* Option to use custom image (or stack) for chrominance
* Consult users about options stored in preferences file 
* Clears some variables between runs to facilitate continous use without restarting ImageJ

Features added in the December 19, 2016 update: 

* Enhanced support for transmissive and florescence captures
* Revised expected directory structure
* Adds companion macro to curate files exported by Photoshoot

Minor changes January 2, 2017:

* Jpeg quality maximized until final distribution
* Fix to curation macro for rotation from exif metadata xml
* Option to createLpFile from canonical version
