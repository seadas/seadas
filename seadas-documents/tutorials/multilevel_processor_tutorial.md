#Tutorial for the multilevel_processor.py program

##Overview


The multilevel_processor.py program allows for the automated processing of data through several OBPG programs.  For example, MODIS L0 files can be processed through Level 1A, Level 1B, Level 2, Level 3 Bin, and SMI by issuing a single command.  This allows for less interaction with the programs needed to produce the products at each of these levels.

##Running the program

**Command line parameters**

The only command line parameter the mlp program accepts is the name of a parameter (par) file.  This is required for a processing run of the program.  Par files are more fully explained below.

**Command line options**

The mlp program has several options that can be specified on the command line:

*  --version             The version option causes the program to display its version number and exit.

*  -h, --help            The help option causes the program to show a help message and exit.

*  -k, --keepfiles       The keepfiles options (which has "-k" as an alternate form) causes the program to keep all files created during processing.

*  --ifile=IFILE         The ifile option specifies the input file to be used.  It can be a data file, or a file containing a list of data files.  In the latter case, each data file must appear on a separate line.

*  --output\_dir=ODIR, --odir=ODIR The output_dir option (odir, alternatively) allows specification of the output directory.  By default, output is put in the current working directory.

*  --overwrite           If a data file already exists and the program's processing would create an output file by the same name, the overwrite option will cause the program to overwrite the data file.  The default behavior is to stop processing when an output file would replace an already existing file.

*  --timing              The timing option causes the program to report the time required to run each program and the total time the program runs.

*  --use\_existing        The use_existing files option causes the program to use intermediate data files which already exist (default = stop processing if file already exists)

*  -v, --verbose         The verbose option causes the program to print status messages to stdout.  Normally these messages are written to log files, but not stdout.

**Parameter Files**

To use the mlp program, a parameter (par) file is required.  While similar to par files used in other OBPG programs, there are some differences.  The mlp par file is divided into sections. Each section begins with a header.  A section header is simply a line containing the name of the section surrounded by square brackets ("[" and "]"); for example, the section header for an l2gen section would be:

[l2gen]

A special section, "main", defines the input files and some other parameters used by all parts of the multilevel_processor.py run.  The other sections are named for the programs to be run.  It is not necessary to have a section for every program to be run; the mlp will determine what intermediate steps are needed to complete processing through the final program.  However, if special processing is needed for an intermediate program, that program should have a section defining the options for the special processing. The final program to be run is defined by the "highest" section specified.  For some programs alternative names can be used. The program recognizes the following sections: 

* main

* l1agen (alternates: level 1a, l1a, modis_L1A.py)

* l1aextract\_modis

* l1aextract\_seawifs

* l1brsgen

* modis_GEO.py (alternate: geo)

* l1bgen (alternates: modis_L1B.py, level 1b, l1b)

* l2gen (alternate:level 2)

* l2bin

* l2brsgen

* l2extract

* l2mapgen

* l3bin

* smigen
  
##Examples

* Process an L1A file to L2, using default options:

  \[main]<br>
  ifile=2010345034027\.L1A\_LAC<br>
  \[l2gen]<br>
