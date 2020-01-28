#!/bin/bash

# variables you may change
TIMEOUTTIME=3600					# timeout time for programs
CTTIMEOUTTIME=360					# timeout time for correctness testing
TIMEOUT="gtimeout $TIMEOUTTIME"		# timeout operation ** set command to "timeout" for linux and "gtimeout" for mac
PREPATH="./Evaluations/" 			# location of your results
PARALLEL_OPTS="--jobs 2 --bar"		# changes the number of parallel jobs running
JAVA_OPTS="-cp $(CLASSPATH):../ -XX:+UseSerialGC -Xmx2G" 	# java options, serial garbage collection, heap space


# variables used by other programs (don't change unless necessary)
INSTANCESFILE="instanceGenerator/instanceNames.txt"
IPINSTANCESFILE="instanceGenerator/ipInstanceNames.txt"
NONIPINSTANCESFILE="instanceGenerator/nonipInstanceNames.txt"
ORIG="ReDegreeIterationAlgorithm"
ALT="ReStablePairAlgorithm"
ENUM="EnumerationAlgorithm"
RES_CORR="Correctness"


export TIMEOUTTIME=$TIMEOUTTIME
export CTTIMEOUTTIME=$CTTIMEOUTTIME
export TIMEOUT=$TIMEOUT
export ORIG=$ORIG
export ALT=$ALT
export ENUM=$ENUM
export RES_CORR=$RES_CORR
export JAVA_OPTS=$JAVA_OPTS


# compile
source ~/.bashrc
javac -d . code/correctness/*.java
javac -d . code/degree_iteration/*.java
javac -d . code/enumeration/*.java
javac -d . code/shared/*.java
javac -d . code/stable_pair/*.java

# create the instances to generate lists
python instanceGenerator/instancesToGenerate.py userOptions.txt $PREPATH

# create the instances
createInstance() {
	DIR=$(dirname $1)
 	mkdir -p $DIR
 	python instanceGenerator/generator.py $1 > $1
 	# echo "instance created: $1"
}

export -f createInstance
cat $INSTANCESFILE | parallel $PARALLEL_OPTS createInstance {} 


runStableExperiment() {
	INSTANCE=$1
	DIR=$(dirname $INSTANCE)

	# original algorithm
	RES_ORIG_DIR=$(sed "s/Instances/$ORIG/g" <<< $DIR)
	RES_ORIG_FILE=$(sed "s/Instances/$ORIG/g" <<< $INSTANCE)
	mkdir -p $RES_ORIG_DIR
	$TOcommand $TIMEOUT java $JAVA_OPTS code/degree_iteration/Main_ReDegreeIterationAlgorithm $INSTANCE > $RES_ORIG_FILE
	EXIT=$?
	if [ $EXIT -eq 124 ] ; then echo "regretequal_timeout $TIMEOUT s" >> $RES_ORIG_FILE ; 
	elif [ $EXIT -ne 0 ] ; then echo "**uncontrolled error**" >> $RES_ORIG_FILE ; fi   
	echo "exitCode $EXIT" >> $RES_ORIG_FILE 
	echo "code/degree_iteration/Main_ReDegreeIterationAlgorithm completed" >> $RES_ORIG_FILE 

	# alternative algorithm
	RES_ALT_DIR=$(sed "s/Instances/$ALT/g" <<< $DIR)
	RES_ALT_FILE=$(sed "s/Instances/$ALT/g" <<< $INSTANCE)
	mkdir -p $RES_ALT_DIR
	$TOcommand $TIMEOUT java $JAVA_OPTS code/stable_pair/Main_ReStablePairAlgorithm $INSTANCE > $RES_ALT_FILE
	EXIT=$?
	if [ $EXIT -eq 124 ] ; then echo "regretequalalt_timeout $TIMEOUT s" >> $RES_ALT_FILE ; 
	elif [ $EXIT -ne 0 ] ; then echo "**uncontrolled error**" >> $RES_ALT_FILE ; fi   
	echo "exitCode $EXIT" >> $RES_ALT_FILE 
	echo "code/stable_pair/Main_ReStablePairAlgorithm completed" >> $RES_ALT_FILE 
	
	# enumeration algorithm
	RES_ENUM_DIR=$(sed "s/Instances/$ENUM/g" <<< $DIR)
	RES_ENUM_FILE=$(sed "s/Instances/$ENUM/g" <<< $INSTANCE)
	mkdir -p $RES_ENUM_DIR
	$TOcommand $TIMEOUT java $JAVA_OPTS code/enumeration/Main_Enumerate $INSTANCE > $RES_ENUM_FILE
	EXIT=$?
	if [ $EXIT -eq 124 ] ; then echo "allstable_timeout $TIMEOUT s" >> $RES_ENUM_FILE ; 
	elif [ $EXIT -ne 0 ] ; then echo "**uncontrolled error**" >> $RES_ENUM_FILE ; fi   
	echo "exitCode $EXIT" >> $RES_ENUM_FILE 
	echo "code/enumeration/Main_Enumerate" >> $RES_ENUM_FILE 
}

export -f runStableExperiment
cat $INSTANCESFILE | parallel $PARALLEL_OPTS runStableExperiment {} 

# do correctness
runCorrectness() {
	INSTANCE=$1
	ISIP=$2
	DIR=$(dirname $INSTANCE)
	CORDIR=$(sed "s/Instances/$RES_CORR/g" <<< $DIR)
	CORFILE=$(sed "s/Instances/$RES_CORR/g" <<< $INSTANCE)

	# original algorithm
	RES_ORIG_FILE=$(sed "s/Instances/$ORIG/g" <<< $INSTANCE)
	# alternative algorithm
	RES_ALT_FILE=$(sed "s/Instances/$ALT/g" <<< $INSTANCE)
	# enumeration algorithm
	RES_ENUM_FILE=$(sed "s/Instances/$ENUM/g" <<< $INSTANCE)


	mkdir -p $CORDIR
	java $JAVA_OPTS code/correctness/Main_Tester $INSTANCE $RES_ORIG_FILE $RES_ALT_FILE $RES_ENUM_FILE "false" > $CORFILE
	EXIT=$?
	if [ $EXIT -ne 0 ] ; then echo "**uncontrolled error**" >> $CORFILE ; fi   
	echo "exitCode $EXIT" >> $CORFILE 
	echo "code/correctness/Main_Tester completed" >> $CORFILE 

	if [ "$ISIP" == "true" ]
	then
		python code/correctness/pulpIP.py $INSTANCE $CTTIMEOUTTIME >> $CORFILE 
		EXIT=$?
		if [ $EXIT -eq 5 ] ; then echo "ip_timeout $TIMEOUT s" >> $CORFILE ; 
		elif [ $EXIT -ne 0 ] ; then echo "**uncontrolled error**" >> $CORFILE ; fi  
		echo "exitCode $EXIT" >> $CORFILE 
		echo "code/correctness/pulpIP.py completed" >> $CORFILE 
	fi
}

export -f runCorrectness
cat $IPINSTANCESFILE | parallel $PARALLEL_OPTS runCorrectness {} true
cat $NONIPINSTANCESFILE | parallel $PARALLEL_OPTS runCorrectness {} false


# stats
python stats/correctness.py $PREPATH
python stats/collect_results.py $PREPATH
python stats/stats.py true			# results for paper
python stats/stats.py false			# results including second algorithm


echo "all processes complete"

