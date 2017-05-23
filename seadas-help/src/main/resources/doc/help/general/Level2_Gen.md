## _Chapter 2_

# The Level-2 File Generation Software (l2gen)

The SeaDAS software which generates a level-2 file from an input level-1 file is Level-2 Gen (_l2gen_).  Level-2 data consist of derived geophysical variables at the same resolution as the source Level-1 data.  The input level-1 files can be very mission specific in format and content, however the output level-2 file format is not mission specific.  The availability of certain level-2 products is mission specific.

Level-2 Gen (l2gen) is written in C and can be run from either the command line or from within the SeaDAS GUI, which is written in Java.

The Level-2 Gen GUI is automatically configured by a call to l2gen.  The products, parameters, and defaults within l2gen will appear within the L2Gen GUI.  This functionality has the advantage that l2gen may be updated (or even revised by the user) and the new products, parameters and defaults will be available to the Level-2 Gen GUI.

## 2.1. Available Products

There are many products which can be selected, but the availability of certain products is mission specific.  The following is a categorized listing of many of the key products which can be produced by l2gen for inclusion within the level-2 file.

### 2.1.1. Radiances and Reflectances
These include (but not limited to):
1. **_Es_** - Solar Irradiance at Surface
1. **_Lt_** - Calibrated Top of Atmosphere Radiance
1. **_Lw_** - Water Leaving Radiance
1. nLw - Normalized Water Leaving Radiance
1. rhos - Surface Reflectance
1. rhot - Top of Atmosphere Reflectance
1. Rrs - Remote Sensing Reflectance

### 2.1.2 Derived Geophysical Parameters
These include (but not limited to):
- aot - Aerosol Optical Thickness
- angstrom - Aerosol Angstrom Exponent
- cdom_index - CDOM Index
- chlor_a - Chlorophyll Concentration (see 7.3.2)
- Kd_490 - Diffuse Attenuation Coefficient (see 7.3.3)
- nflh - Normalized Fluorescence Line Height
- ndvi - Normalized Difference Vegetation Index
- par - Photosynthetically Available Radiation
- pic - Particulate Inorganic Carbon
- poc - Particulate Organic Carbon
- sst - Sea Surface Temperature
- Zeu - Euphotic Depth

#### 2.1.3 Inherent Optical Products
These include (but not limited to):
- a -  Total Absorption
- adg - Absorption Due to Gelbstoff and Detrital Material
- aph - Absorption Due to Phyoplankton
- bb - Total Backscattering

#### 2.1.4 Ancillary, Meterological and Geometric Parameters
These include (but not limited to):
- height - Terrain Height
- humidity - Relative Humidity
- no2_frac - Fraction of Tropospheric NO2 above 200m
- no2_strat - Stratospheric NO2
- no2_trop - Tropospheric NO2
- ozone - Ozone Concentration
- pressure - Surface Pressure
- scattang - Scattering Angle
- sena - Sensor Azimuth Angle
- senz - Sensor Zenith Angle
- sola - Solar Azimuth Angle
- solz - Solar Zenith Angle
- water_vapor - Water Vapor
- windangle - Wind Direction
- windspeed - Wind Speed

#### 2.1.5 Atmospheric Correction Intermediates
These include: cloud_albedo, ...

#### 2.1.6 Uncertainties and Error Estimates
These include: Lt_unc, Rrs_unc, ...

#### 2.1.7 Miscellaneous
These include: resolution, ...

## 2.2  Processing Options
Text goes here.

## 2.3  Subsetting Options
Text goes here.

## 2.4  Thresholds
Text goes here.

## 2.5  Ancillary Input Data
Text goes here.

## 2.6  IOP Options
Text goes here.

## 2.7  Calibration Options
Text goes here.

## 2.8  Miscellaneous
Text goes here.

## 2.9  Product Suite
Text goes here.

## 2.10  The Input Parameter File
A multitude of input parameters to l2gen and choices of output level-2 products are available to the user.  Every parameter has a default and the user need only include a parameter as input when it deviates from the default.  When run from the GUI, SeaDAS automatically detects when the user has entered a parameter with its default value and does not include this parameter in the run.  This can greatly simply diagnosing the run and its deviation from the defaults.

## 2.11  Configuration Files

#### 2.11.1  Product Configuration
The file "product.xml" is read by l2gen and configures certain product metadata written to the level-2 file.  The user can modify this file to customize this product metadata.  This metadata is available to SeaDAS when the level-2 file is loaded as band metadata and is also used to build the product fields of the L2Gen GUI.  Some key metadata in this file include:
validMin - used to build the field valid_pixel_expression
validMax - used to build the field valid_pixel_expression
displayMin - not currently used but will eventually be the default in the color scheme
displayMax - not currently used but will eventually be the default in the color scheme
displayScale - not currently used but will eventually be the default in the color scheme

## 2.12  Product Algorithms
The Ocean Biology Processing Group (OBPG) produces and distributes a standard suite of ocean color products for all compatible sensors at Level-2 and Level-3, plus sea surface temperature (SST) products from MODIS.  The OBPG also produces a suite of Level-3 evaluation products. Descriptions and references for these standard and evaluation products are provided below.

#### 2.12.1  Chlorophyll
This algorithm returns the near-surface concentration of chlorophyll-a (chlor_a) in mg m-3, calculated using an empirical relationship derived from in situ measurements of chlor_a and remote sensing reflectances (Rrs) in the blue-to-green region of the visible spectrum. The implementation is contingent on the availability three or more sensor bands spanning the 440 - 670 nm spectral regime. The algorithm is applicable to all current ocean color sensors. The chlor_a product is included as part of the standard Level-2 OC product suite and the Level-3 CHL product suite. The current implementation for the default chlorophyll algorithm (chlor_a) employs the standard OC3/OC4 (OCx) band ratio algorithm merged with the color index (CI) of Hu et al. (2012). As described in that paper, this refinement is restricted to relatively clear water, and the general impact is to reduce artifacts and biases in clear-water chlorophyll retrievals due to residual glint, stray light, atmospheric correction errors, and white or spectrally-linear bias errors in Rrs. As implemented, the algorithm diverges slightly from what was published in Hu et al. (2012) in that the transition between CI and OCx now occurs at 0.15 < CI < 0.2 mg/m^3 to ensure a smooth transition.
This is made available and consistent across missions by lookup tables which account for band wavelength differences.

#### 2.12.2  Diffuse attenuation coefficient for downwelling irradiance at 490 nm
This algorithm returns the diffuse attenuation coefficient for downwelling irradiance at 490 nm (Kd_490) in m-1, calculated using an empirical relationship derived from in situ measurements of Kd_490 and blue-to-green band ratios of remote sensing reflectances (Rrs).
Implementation of this algorithm is contingent on the availability of Rrs in the blue-green spectral region (e.g., 490 - 565 nm). CZCS, OCTS, MODIS-Aqua and -Terra, MERIS, SeaWiFS, VIIRS, and others are all supported.

## 2.13  Level-2 Product Flags
Text goes here.
