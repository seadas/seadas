# SeaDAS-OCSSW Client Server Configuration

## Introduction

The SeaDAS processing components (OCSSW) can be installed only on Linux or macOS (Intel) systems. For this reason,
it has been difficult for Windows users to use the OCSSW programs in an efficient way. SeaDAS 7.5 introduces a client/server model, which
enables the physical decoupling of the SeaDAS GUI application and the OCSSW package.

Since the SeaDAS client and the OCSSW server reside on two separate machines, they need a way to share
files.  They can either transmit the files over the network or use a shared folder.
For virtual machines, we can use a shared folder and eliminate the transmission of files over
the network.

## OCSSW Server Installation

The OCSSW server provides access to OCSSW programs through web services.
The web services are packaged as an independent JAR (Java ARchive) file and can be deployed on a machine (real or virtual)
capable of running OCSSW programs. With this new capability, we have three additional options that
enable SeaDAS to access to OCSSW programs:

   * Install a Linux virtual machine (e.g. VirtualBox) on the same Windows machine that has the SeaDAS GUI installed
   * Install the Windows Subsystem for Linux (WSL - available for Windows 10) on the same machine
   * Install the web services package on any Unix or MacOS machine that provides network access to other
  computers

In this manual, we explain how to configure the OCSSW server and the SeaDAS client for each scenario.


## 1a. Installing an OCSSW Server on a Guest Virtual Machine

(For this option, we are using Oracle's VirtualBox software.  Other VM software may work, but we have only tested VirtualBox using an Ubuntu 16.04 server configuration)

1. Install Oracle VirtualBox from https://www.virtualbox.org/
1. Install the Ubuntu 16.04 server on the VM
1. Create a Linux Ubuntu guest virtual machine. For SeaDAS 7.5, we used Ubuntu 16.04 LTS
1. Set up VirtualBox Guest Additions on GUI-less VM server
```
$ sudo mount /dev/cdrom /media/cdrom
$ cd /media/cdrom
$ sudo apt-get install -y dkms build-essential linux-headers-generic linux-headers-$(uname -r)
$ sudo su
$ ./VBoxLinuxAdditions.run
$ reboot
$ lsmod | grep -io vboxguest
$ sudo usermod -aG vboxsf <user>
```
1. Create a shared folder

  * Create a folder on the Host computer (Windows) that you would like to share, for example ```C:\Users\${username}\seadasClientServerShared```
  * Boot the Guest operating system in VirtualBox.
  * Create a folder on the Guest computer (Ubuntu) that you would like to share, for example ```~/seadasClientServerShared```
  * Select Devices -> Shared Folders...
  * Choose the 'Add' button.
  * Select ```C:\Users\${username}\seadasClientServerShared``` in the "Folder Path" and name it, for example, "seadasClientServerShared", in the "Folder Name"
  * Optionally select the 'Make permanent' option

1. Prepare the shared folder

  With a shared folder named "seadasClientServerShared", as above, the folder can be mounted as the directory ```~/seadasClientServerShared``` with the command
```
    $ sudo mount -t vboxsf -o uid=$UID,gid=$(id -g) ocssw_shared ~/seadasClientServerShared
```
If you experience any problem with setting up a shared folder, please visit https://help.ubuntu.com/community/VirtualBox/SharedFolders.


## 1b. Installing an OCSSW Server on a Windows Subsystem Linux

  1. Install WSL and a supported Linux distribution (Ubuntu was used during testing) following the instructions available from the Microsoft website: https://docs.microsoft.com/en-us/windows/wsl/install-win10.
  1. The client and server share the whole disk.


## 1c. Installing an OCSSW Server on a Remote Machine

  1. Obtain a suitable networked Linux server :)
   ##### NOTE:  _The client and server do not have a shared disk._


## 2. Configure the Virtual or Real Machine

1. Set up Java
```
    $ sudo add-apt-repository ppa:webupd8team/java
    $ sudo apt update; sudo apt install oracle-java8-installer
    $ sudo apt install oracle-java8-set-default
```
1. Install python2.7
```
    $ sudo apt install python
```
1. Install
```
    $ sudo apt install git
```


## 3. Deploy the OCSSW Web Services Package

The OCSSW Web Services Package consists of two files than need to be downloaded to the server machine.
For our example we will put them in the home directory.

  1. [Server jar file](https://oceandata.sci.gsfc.nasa.gov/SeaDAS/installer/7.5/seadas-ocsswserver.jar)
  1. [Server config file](https://oceandata.sci.gsfc.nasa.gov/SeaDAS/installer/7.5/ocsswserver.config)

The jar file is a self-contained webserver and multiple web services that access various OCSSW programs.
The configuration file provides crucial information for establishing communication between the OCSSW
server and the SeaDAS client.
##### NOTE: we have not had success with openJDK for this server JAR, so you will need to install the official Oracle Java package - step #1 under "Configure the Virtual or Real Machine" above

#### Example ocsswserver.config for Virtual Machine:

    ocsswrest.version=1.0
    baseUriPortNumber=6400
    ocsswroot=${user.home}/ocssw
    serverWorkingDirectory=${user.home}/seadasClientServerShared
    clientServerSharedDir=true
    keepIntermediateFilesOnServer=false
    processInputStreamPortNumber=6402
    processErrorStreamPortNumber=6403

#### Example ocsswserver.config for WSL:

    ocsswrest.version=1.0
    baseUriPortNumber=6400
    ocsswroot=${user.home}/ocssw
    serverWorkingDirectory=/mnt/c/Users/${user.name}/seadasClientServerShared
    clientServerSharedDir=true
    keepIntermediateFilesOnServer=false
    processInputStreamPortNumber=6402
    processErrorStreamPortNumber=6403

#### Example ocsswserver.config for Remote Machine:

    ocsswrest.version=1.0
    baseUriPortNumber=6400
    ocsswroot=${user.home}/ocssw
    serverWorkingDirectory=${user.home}/seadasClientServerShared
    clientServerSharedDir=false
    keepIntermediateFilesOnServer=false
    processInputStreamPortNumber=6402
    processErrorStreamPortNumber=6403

The config file properties related to port numbers and the folders are essential and should match
the values in the client configuration file.  Please see the "SeaDAS Client Configuration" section
of this document for the corresponding client configuration properties and their values.


###  Starting and Stopping the OCSSW Server

* To start the OCSSW server, execute the following command:

```
$ java -Xmx4G -jar seadas-ocsswserver.jar ocsswserver.config
```
* To stop the OCSSW server, press 'Ctrl' + 'C'.



## 4. SeaDAS Client Configuration

To configure the client to communicate with the server, the SeaDAS config file needs to be edited.

    <SeaDAS install dir>/config/seadas.config

Edit the following lines in the seadas.config file:

#### Example for Virtual Machine:

       seadas.ocssw.location=virtualMachine
       seadas.ocssw.port=6400
       seadas.ocssw.sharedDir=${user.dir}/seadasClientServerShared
       seadas.client.id=${user.name}
       seadas.ocssw.keepFilesOnServer=false
       seadas.ocssw.processInputStreamPort=6402
       seadas.ocssw.processErrorStreamPort=6403

#### Example for WSL:

       seadas.ocssw.location=virtualMachine
       seadas.ocssw.port=6400
       seadas.ocssw.sharedDir=${user.dir}/seadasClientServerShared
       seadas.client.id=${user.name}
       seadas.ocssw.keepFilesOnServer=false
       seadas.ocssw.processInputStreamPort=6402
       seadas.ocssw.processErrorStreamPort=6403

#### Example for Remote Machine:

       seadas.ocssw.location=remote server IP addess
       seadas.ocssw.port=6400
       seadas.client.id=${user.name}
       seadas.ocssw.keepFilesOnServer=false
       seadas.ocssw.processInputStreamPort=6402
       seadas.ocssw.processErrorStreamPort=6403
