# SpectralRTI_Toolkit
Process Spectral RTI Images in ImageJ

Spectral RTI is a technique that combines color information from spectral imaging with texture information from RTI or raking images.
The SpectralRTI_Toolkit for ImageJ processes the data from a Spectral RTI capture session and creates raking, RTI, and WebRTI images ready for publication.
Specifically the toolkit accepts data from diffuse narrowband spectral images (reflectance, optionally transmissive and fluorescence) and texture broadband images (a virtual dome for RTI, or fewer for raking).
The toolkit accepts accurate color images from the spectral system and has built-in color processing for Extended Spectrum and PCA Pseudocolor.
Additional custom color processes can also be used.

Documentation for the complete process is available in the `Guide` directory and at [http://jubilees.stmarytx.edu/spectralrtiguide/](http://jubilees.stmarytx.edu/spectralrtiguide/).

Examples of Spectral RTI images can be viewed in Mirador at [http://jubilees.stmarytx.edu/mirador/](http://jubilees.stmarytx.edu/mirador/).

Further information is available from the project website at [http://jubilees.stmarytx.edu/](http://jubilees.stmarytx.edu/).

The development of the software and documentation is generously supported by a grant from the National Endowment for the Humanities (2016-2019).

## History
* The SpectralRTI_Toolkit debuted in 2016 as an ImageJ macro.
* The SpectralRTI_Toolkit was developed into a Java plugin for ImageJ2 by the Walter J. Ong S.J. Center for Digital Humanities at Saint Louis University.
* The Java plugin surpassed the macro in performance and functionality in June 2018 and is now the master branch.
* The macro remains available in the macro folder and its own branch of the repository, but is not being actively developed.
* NEH-supported development continues through 2019.