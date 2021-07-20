ant_compile:
	ant compile

ant_all:
	ant compile jar run

run_premade:
	java -jar dist/SINS2.jar\
		-projectName Premade_SINS_Project\
		-outDir output\
		-numberOfSimulations 4\
		-compress noComp\
		-parallel true\
		-parallelCores 4\
		-verbose false\
		-outputFormat sins\
		-makeDemographicImages false
