# LiveModularInstrument

To run the software, you will need SuperCollider version 3.6.6 or later. The entire program is a library, so it requires the following steps to install:<br />

     1) place the entire "LiveModularInstrument" directory in the "/Users/user/Library/Application Support/SuperCollider/Extensions/" folder, then re-compile the SC Library or restart SuperCollider. The "LiveModularInstrument.scd" file in the main directory contains the line of code needed to run the software. 
     
     2) copy the "startup.scd" file to the "/Users/user/Library/Application Support/SuperCollider/" folder or update your "startup.scd" file to reflect the changes that I have made in this file. The software requires increased memory and increased bus allocation to function correctly.
     
     3) many of the modules require elements from the "SCPlugins" collection of SuperCollider plugins. This is available on the SuperCollider GitHub page: http://sourceforge.net/projects/sc3-plugins/files/OSX_3.6/.
