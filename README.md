# SpectralRTI_Toolkit
Process Spectral RTI Images in ImageJ

Spectral RTI is a technique that combines color information from spectral imaging with texture information from RTI or raking images.
The SpectralRTI_Toolkit for ImageJ processes the data from a Spectral RTI capture session and creates raking, RTI, and WebRTI images ready for publication.
Specifically the toolkit accepts data from diffuse narrowband spectral images (reflectance, optionally transmissive and fluorescence) and texture broadband images (a virtual dome for RTI, or fewer for raking).
The toolkit accepts accurate color images from the spectral system and has built-in color processing for Extended Spectrum and PCA Pseudocolor.
Additional custom color processes can also be used.

## Installation
* Install Fiji (non-Fiji distributions of ImageJ will require additional dependencies)
* Copy the plugin (`SpectralRTI_Toolkit-1.0.0.jar`) and its dependencies (`ijp-toolkit` and `PCA`) from the `plugins` folder in this repository to the `plugins` folder under Fiji. 
* Restart Fiji. 

Documentation for the complete process is available in the `Guide` directory and at [http://jubilees.stmarytx.edu/spectralrtiguide/](http://jubilees.stmarytx.edu/spectralrtiguide/).

Sample data for processing Spectral RTI can be downloaded by running `downloaddataset.bash` (Linux and MacOS) or `downloaddataset.cmd` (Windows) in the `sampledata` directory. 

Examples of Spectral RTI images can be viewed in Mirador at [http://jubilees.stmarytx.edu/mirador/](http://jubilees.stmarytx.edu/mirador/).

Further information is available from the project website at [http://jubilees.stmarytx.edu/](http://jubilees.stmarytx.edu/).

The development of the software and documentation was generously supported by a grant from the National Endowment for the Humanities (2016-2019).

## History
* The SpectralRTI_Toolkit debuted in 2016 as an ImageJ macro.
* Starting in 2017, the SpectralRTI_Toolkit was developed into a Java plugin for ImageJ2 by the Walter J. Ong S.J. Center for Digital Humanities at Saint Louis University.
* The Java plugin surpassed the macro in performance and functionality in June 2018 and became the master branch. The macro is still available in the "macro" branch.
* In September 2019 the plugin was released as the 1.0 stable version in the master branch. 
