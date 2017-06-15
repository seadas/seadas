## _Chapter 2_


# THE LEVEL-2 FILE GENERATION SOFTWARE (_l2gen_)

The SeaDAS software which generates a level-2 file from an input level-1 file is Level-2 Gen (**_l2gen_**).  Level-2 data consist of derived geophysical variables at the same resolution as the source Level-1 data.  The input level-1 files can be very mission specific in format and content, however the output level-2 file format is not mission specific.  The availability of certain level-2 products is mission specific.  

Level-2 Gen (l2gen) is written in C and can be run from either the command line or from within the SeaDAS GUI, which is written in Java.

The Level-2 Gen GUI is automatically configured by a call to l2gen.  The products, parameters, and defaults within l2gen will appear within the L2Gen GUI.  This functionality has the advantage that l2gen may be updated (or even revised by the user) and the new products, parameters and defaults will be available to the Level-2 Gen GUI.

## 2.1. AVAILABLE PRODUCTS

There are many products which can be selected, but the availability of certain products is mission specific.  The following is a categorized listing of many of the key products which can be produced by l2gen for inclusion within the level-2 file.

#### 2.1.1. RADIANCES AND REFLECTANCES  
These include (but are not limited to):  
**_Es_** - Solar Irradiance at Surface    
**_Lt_** - Calibrated Top of Atmosphere Radiance  
**_Lw_** - Water Leaving Radiance  
**_nLw_** - Normalized Water Leaving Radiance  
**_rhos_** - Surface Reflectance  
**_rhot_** - Top of Atmosphere Reflectance  
**_Rrs_** - Remote Sensing Reflectance  

#### 2.1.2 DERIVED GEOPHYSICAL PARAMETERS   
These include (but are not limited to):  
**_aot_** - Aerosol Optical Thickness  
**_angstrom_** - Aerosol Angstrom Exponent  
**_cdom_index_** - CDOM Index  
[**_chlor_a_** - Chlorophyll Concentration](https://oceancolor.gsfc.nasa.gov/atbd/chlor_a/)      
[**_Kd_490_** - Diffuse Attenuation Coefficient](https://oceancolor.gsfc.nasa.gov/atbd/kd_490/)    
**_nflh_** - Normalized Fluorescence Line Height  
**_ndvi_** - Normalized Difference Vegetation Index  
**_par_** - Photosynthetically Available Radiation  
**_pic_** - Particulate Inorganic Carbon  
**_poc_** - Particulate Organic Carbon  
**_sst_** - Sea Surface Temperature  
**_Zeu_** - Euphotic Depth  

#### 2.1.3 INHERENT OPTICAL PRODUCTS
These include (but are not limited to):  
**_a_** -  Total Absorption  
**_adg_** - Absorption Due to Gelbstoff and Detrital Material  
**_aph_** - Absorption Due to Phyoplankton  
**_bb_** - Total Backscattering  

#### 2.1.4 ANCILLARY, METEROLOGICAL AND GEOMETRIC PARAMETERS
These include (but are not limited to):  
**_height_** - Terrain Height  
**_humidity_** - Relative Humidity  
**_no2_frac_** - Fraction of Tropospheric NO2 above 200m  
**_no2_strat_** - Stratospheric NO2  
**_no2_trop_** - Tropospheric NO2  
**_ozone_** - Ozone Concentration  
**_pressure_** - Surface Pressure  
**_scattang_** - Scattering Angle  
**_sena_** - Sensor Azimuth Angle  
**_senz_** - Sensor Zenith Angle  
**_sola_** - Solar Azimuth Angle  
**_solz_** - Solar Zenith Angle  
**_water_vapor_** - Water Vapor  
**_windangle_** - Wind Direction  
**_windspeed_** - Wind Speed  

#### 2.1.5 ATMOSPHERIC CORRECTION INTERMEDIATES
These include (but are not limited to):
**_cloud_albedo_** - Cloud Albedo (used for thresholding)
**_dpol_** - Degree of Rayleigh Polarization
**_epsilon_** - Single-scattering aerosol epsilon
**_fsol_** - Solar distance correction factor
**_glint_coef_** - Cox-Munk normalized glint radiance
**_ms_epsilon_** - Observed (multi-scattering) aerosol epsilon
**_polcor_** - Polarization correction
**_tg_sen_** - Gaseous Transmittance, Surface to Sensor
**_tg_sol_** - Gaseous Transmittance, Sun to Surface
**_t_h2o_** - Total water vapor transmittance
**_t_o2_** - Total oxygen transmittance
**_t_sen_** - Rayleigh-Aerosol Diff. Trans., Surface to Sensor
**_t_sol_** - Rayleigh-Aerosol Diff. Trans., Sun to Surface

#### 2.1.6 UNCERTAINTIES AND ERROR ESTIMATES
These include: Lt_unc, Rrs_unc, ...

#### 2.1.7 MISCELLANEOUS
These include: resolution, ...

## 2.2  PROCESSING OPTIONS
Text goes here.

## 2.3  SUBSETTING OPTIONS
Text goes here.<

## 2.4  THRESHOLDS
These include (but are not limited to):
**_aotthreshold_** - threshold on L2 data AOTs (1.000000=AOT_MAX)
**_chlthreshold_** - threshold on L2 data chlorophyll (100.000000=CHL_MAX)
**_cirrus_thresh_** - cirrus reflectance thresholds
**_cloud_eps_** - cloud reflectance ratio threshold
**_cloud_thresh_** - cloud reflectance threshold (_flag: **CLDICE**_)
**_cloud_wave_** - wavelength of cloud reflectance test (_flag: **CLDICE**_)
**_epsmax_** - maximum epsilon to trigger atmospheric correction failure flag (_flag: **ATMWARN**_)
**_epsmin_** - minimum epsilon to trigger atmospheric correction failure flag (_flag: **ATMWARN**_)
**_flh_offset_** - bias to subtract from retrieved fluorescence line height
**_glint_thresh_** - high sun glint threshold (_flag: **HIGLINT**_)
**_ice_threshold_** - sea ice fraction above which will be flagged as sea ice (_flag: **SEAICE**_)
**_nlwmin_** - minimum nLw(555) to trigger low Lw flag (_flag: **LOWLW**_)
**_rhoamin_** - min NIR aerosol reflectance to attempt model lookup
**_satzen_** - satellite zenith angle threshold in degrees (_flag: **HISATZEN**_)
**_sunzen_** - sun zenith angle threshold in degees (_flag: **HISOLZEN**_)
**_tauamax_** - maximum 865 aerosol optical depth to trigger hitau flag
**_threshold_** - threshold for the number of good pixels before an image is produced
**_wsmax_** - windspeed limit on white-cap correction in m/s

## 2.5  ANCILLARY INPUT DATA
Text goes here.

## 2.6  IOP OPTIONS
Text goes here.

## 2.7  CALIBRATION OPTIONS
Text goes here.

## 2.8  MISCELLANEOUS
Text goes here.

## 2.9  PRODUCT SUITE
Text goes here.

## 2.10  THE INPUT PARAMETER FILE
A multitude of input parameters to l2gen and choices of output level-2 products are available to the user.  Every parameter has a default and the user need only include a parameter as input when it deviates from the default.  When run from the GUI, SeaDAS automatically detects when the user has entered a parameter with its default value and does not include this parameter in the run.  This can greatly simply diagnosing the run and its deviation from the defaults.

## 2.11  CONFIGURATION FILES

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
These include (but are not limited to):
**_LAND_** - Land
**_PRODWARN_** - One (or more) product algorithms generated a warning
**_HIGLINT_** - High glint determined
**_HILT_** - High (or saturating) TOA radiance
**_HISATZEN_** - Large satellite zenith angle
**_COASTZ_** - Shallow water (\<30m)
**_STRAYLIGHT_** - Straylight determined
**_CLDICE_** - Cloud/Ice determined
**_COCCOLITH_** - Coccolithophores detected
**_TURBIDW_** - Turbid water determined
**_HISOLZEN_** - High solar zenith angle
**_LOWLW_** - Low Lw @ 555nm (possible cloud shadow)
**_CHLFAIL_** - Chlorophyll algorithm failure
**_NAVWARN_** - Navigation suspect
**_ABSAER_** - Absorbing Aerosols determined
**_MAXAERITER_** - Maximum iterations reached for NIR iteration
**_MODGLINT_** - Moderate glint determined
**_CHLWARN_** - Chlorophyll out-of-bounds (\<0.01 or >100 mg m^-3)
**_ATMWARN_** - Atmospheric correction warning; Epsilon out-of-bounds
**_SEAICE_** - Sea ice determined
**_NAVFAIL_** - Navigation failure
**_FILTER_** - Insufficient data for smoothing filter
**_BOWTIEDEL_** - Bowtie deleted pixels (VIIRS)
**_HIPOL_** - High degree of polariztion determined
**_PRODFAIL_** - One (or more) product algorithms produced a failure
