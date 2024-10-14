# SINS

## Overview

SINS, a Java program to Simulate INdividuals in Space.

## Prerequisites

Download the latest package from our release page (https://github.com/PopConGen/SINS/releases) and unzip it.

Make sure you have Java version 8 (or later) installed in your machine.

You can check this in the command line with:
``` bash 
java --version
```
## User guide

You can check the detailed user guide here: https://github.com/PopConGen/SINS/blob/main/SINS_Userguide.pdf

## Getting started

We include a toy example that you can use to try SINS.

In the command line, move to the folder where SINS is located. If you downloaded our release (_sins.zip_) to your "_Downloads_" folder, then after you unzip it, the folder where SINS is located is called "sins" and it will be under "_~/Downloads/sins_".

In the command line type or paste the following command:
``` bash
java -jar SINS2.jar -projectName Premade_SINS_Project -outDir output -numberOfSimulations 4 -compress noComp -parallel true -parallelCores 4 -verbose true -outputFormat sins -makeDemographicImages false
```

You should see something like the following output:
``` bash
Simulation Premade_SINS_Project is starting...
Simulation 2 Generation 0
Simulation 4 Generation 0
Simulation 3 Generation 0
Simulation 1 Generation 0
...
...
...
Simulation 4 Generation 999
Simulation 2 Generation 998
Simulation 2 Generation 999
Simulation Premade_SINS_Project has finished, time elapsed: 00hh : 00mm : 04ss 228ms
```

You can now check the output of your simulation in the output folder inside the SINS folder (eg. at "~/Downloads/SINS/output/")

If you want to know which other command line options are available when running SINS type or paste the following command in the command line:
``` bash
java -jar SINS2.jar -help
```

### Plot demographic stats
If you want to create the demography statistics plots set '-makeDemographicImages true' in the command line, eg:
``` bash
java -jar SINS2.jar -projectName Premade_SINS_Project -outDir output -numberOfSimulations 4 -compress noComp -parallel true -parallelCores 4 -verbose true -outputFormat sins -makeDemographicImages true
```


Make sure you have the python library 'matplotlib' installed.
