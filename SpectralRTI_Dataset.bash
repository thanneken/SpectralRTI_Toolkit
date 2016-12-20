#!/bin/bash
if [[ -n "$( pwd | grep -o -P " " )" ]]; 
then echo "Present working directory ($(pwd)) contains spaces. Please choose a different directory." 
fi
mkdir -p SpectralRTI/Macros/
mkdir -p SpectralRTI/Plugins/
mkdir -p SpectralRTI/Projects/Figurine/AccurateColor/
mkdir -p SpectralRTI/Projects/Figurine/Captures-Hemisphere/
mkdir -p SpectralRTI/Projects/Figurine/Captures-Narrowband/
wget -nc -O SpectralRTI/Macros/SpectralRTI_Toolkit.ijm https://raw.githubusercontent.com/thanneken/SpectralRTI_Toolkit/master/SpectralRTI_Toolkit.ijm
wget -nc -O SpectralRTI/Plugins/PCA_.class https://raw.githubusercontent.com/thanneken/SpectralRTI_Toolkit/master/PCA_.class
wget -nc -O SpectralRTI/Plugins/ijp-toolkit_bin_2.1.0.zip  https://sourceforge.net/projects/ij-plugins/files/ij-plugins_toolkit/v.2.1.0/ijp-toolkit_bin_2.1.0.zip
wget -nc -O SpectralRTI/Projects/Figurine/AccurateColor/Figurine-Color.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Accc/20140220-Sold-00-Accc-03-50m.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-01.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-01-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-02.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-02-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-03.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-03-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-04.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-04-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-05.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-05-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-06.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-06-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-07.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-07-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-08.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-08-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-09.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-09-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-10.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-10-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-11.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-11-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-12.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-12-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-13.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-13-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-14.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-14-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-15.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-15-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-16.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-16-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-17.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-17-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-18.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-18-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-19.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-19-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-20.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-20-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-21.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-21-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-22.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-22-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-23.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-23-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-24.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-24-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-25.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-25-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-26.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-26-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-27.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-27-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-28.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-28-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-29.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-29-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-30.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-30-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-31.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-31-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-32.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-32-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-33.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-33-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-34.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-34-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Hemisphere/Figurine-Position-35.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-Fla-35-Capt-01-50m-011.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Narrowband/Figurine-Band-01.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-001-00-Capt-01-50m.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Narrowband/Figurine-Band-02.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-002-00-Capt-01-50m.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Narrowband/Figurine-Band-03.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-003-00-Capt-01-50m.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Narrowband/Figurine-Band-04.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-004-00-Capt-01-50m.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Narrowband/Figurine-Band-05.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-005-00-Capt-01-50m.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Narrowband/Figurine-Band-06.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-006-00-Capt-01-50m.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Narrowband/Figurine-Band-07.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-007-00-Capt-01-50m.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Narrowband/Figurine-Band-08.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-008-00-Capt-01-50m.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Narrowband/Figurine-Band-09.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-009-00-Capt-01-50m.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Narrowband/Figurine-Band-10.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-010-00-Capt-01-50m.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Narrowband/Figurine-Band-11.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-011-00-Capt-01-50m.tif
wget -nc -O SpectralRTI/Projects/Figurine/Captures-Narrowband/Figurine-Band-12.tif http://palimpsest.stmarytx.edu/integratingdataarchive/Sold/Capt/20130723-Sold-012-00-Capt-01-50m.tif
echo "The toolkit also requires ImageJ (or Fiji) and the RTI Builder from http://culturalheritageimaging.org/What_We_Offer/Downloads/Process/"
