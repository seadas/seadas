SeaDAS, version ${seadas.version}
=================================

Installation Guide
------------------

1. Download and install build tools:
 *   Install J2SE 1.6 and set `JAVA_HOME` accordingly.
 *   Install Maven and set `MAVEN_HOME` accordingly.
 *   Install GIT and set `GIT_HOME` accordingly.
      +   On Windows we recommend the `msysGit` package.
      +   Make sure Git is configured correctly: type `git config -l` at your console; the value `core.autocrlf` has to be set to `input`
      +   If it is not, open `$GIT_HOME/etc/gitconfig` and set `core.autocrlf` to `input`
 *   Create a directory for SeaDAS and set `SEADAS` to this directory.

1.  Add `$JAVA_HOME/bin`, `$MAVEN_HOME/bin` and `$GIT_HOME/bin` to your `PATH`. (Windows:  `%JAVA_HOME%\bin`, `%MAVEN_HOME%\bin` and `%GIT_HOME%\bin`)

1.  Checkout Ceres, BEAM and SeaDAS using `git`:

        cd $SEADAS
        git clone git://github.com/bcdev/ceres.git ceres
        git clone git@github.com:seadas/beam.git beam
        git clone git@github.com:seadas/seadas.git seadas

1.  Need to checkout the correct version for ceres

        cd ceres
        git checkout --track remotes/origin/0.13.x

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

