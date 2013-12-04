SeaDAS Frequently Asked Questions
==================================
Basic file (product) load and display
------------------
1. View product info
1. display band
1. color manipulation
1. no-data value
1. pixel info
1. Product Grabber
1. Session Management

Export Data
-----------------------------
1. GeoTIFF
1. HDF5
1. netCDF
1. flat binary (DIMAP)
1. CSV
1. Shapefile
1. Google KML/KMZ
1. Image (png, tiff, jpg, bmp)

View/Create Masks
----------------------

1.  Need to checkout the correct version for ceres

        cd ceres
        git checkout tags/0.13.2

1.  Build Ceres from source and install in local Maven repository:

        cd $SEADAS/ceres
        mvn install

1.  Build BEAM from source and install in local Maven repository:

        cd $SEADAS/beam
        mvn install

1.  Build SeaDAS from source and install in local Maven repository:

        cd $SEADAS/seadas
        mvn install

1.  Open up the project in your IDE:
   * Netbeans:
      +   *Menu File* -> *Open Project* and select ceres
      +   Check the *Open Required Projects* box
      +   *Menu File* -> *Open Project* and select beam
      +   Check the *Open Required Projects* box
      +   *Menu File* -> *Open Project* and select seadas
      +   Check the *Open Required Projects* box
      +   Set the *SeaDAS Application* as the main project
   * IntelliJ IDEA:
      +   *Main Menu* -> *File* -> *New Project* -> *Import Project from External Model*
      +   Choose Maven
      +   Specify your root directory: `$SEADAS` (Note: put your actual path)
      +   Check the box: *Search for Directories Recursively*
      +   Check the box: *default tools*
      +   Click *Next*
      +   Click *Finish*
      +   edit file $SEADAS/.idea/vcs.xml
         - delete CERES line
         - this stop idea from complaining about the detached head
   * Eclipse:
      +   Build Eclipse project files for BEAM:
         cd $SEADAS/seadas
         mvn eclipse:eclipse
      + Delete the created `.project` file in the main project folder.
      +   Make sure that `M2_REPO` classpath variable is set:
         -   Open *Window* -> *Preferences...* then select *Java* -> *Build Path* -> *Classpath Variables*
         -   Select *New...* and add variable `M2_REPO`
         -   Select *Folder...* and choose the location of your Maven local repository, e.g `~/.m2/repository`. On Windows Vista the default Maven repository is `C:\Users\<Username>\.m2\repository`
      +   Click *Main Menu* -> *File* -> *Import*
      +   Select *General* -> *Existing Project into Workspace*
      +   Select *Root Directory* `$SEADAS/seadas`
      +   Click *Finish*

1. Use the following configuration to run BEAM/VISAT:
   *   Main class: `com.bc.ceres.launcher.Launcher`
   *   VM parameters: `-Xmx2G -Dceres.context=seadas`
   *   Program parameters: `none`
   *   Working directory: `$SEADAS/seadas` (replace $SEADAS with your actual path)
   *   Use classpath of module (project in Eclipse): `seadas-bootstrap`

1. Copy the config file.

            cd $SEADAS/seadas
            mkdir config
            cp src/main/config/seadas.config config

1. Edit the following lines in the config file:
    * Set seadas.home = .
    * Set seadas.app = SeaDAS
    * Set seadas.logLevel = ALL
    * Set seadas.debug = true
    * Set seadas.splash.image = ./src/main/bin/common/splash.png
    * Set seadas.ocssw.root = your OCSSW root dirctory

1. Once you have all the configuration done, hit *Make Project*. Let it rebuild and then *Run*

Original instructions from [Brockmann Consult][bc].

  [bc]: http://www.brockmann-consult.de/beam-wiki/display/BEAM/Build+from+Source

