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

* Consolidate prompts at the beginning rather than a series of interactions
* Set timestamp once when script initiated, use throughout
* Consider whether rotation belongs in the Toolkit
* Options for generating WebRTI (tiles, html)
* More helpful error messages (check dependencies, instructions for fixing errors)
