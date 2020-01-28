#!/bin/bash

# variables you may change
TIMEOUTCOMMAND="gtimeout"		# timeout operation ** set command to "timeout" for linux and "gtimeout" for mac
TIMEOUT=3600					# timeout time for programs
CP="$(CLASSPATH):../ -XX:+UseSerialGC -Xmx3G"			# java classpath
PREPATH="./examples/DM_pg91/"	# location of the example to solve
# PREPATH="./examples/DG_RI_pg12/"
# PREPATH="./examples/DG_RI_pg22/"

# variables used by other programs (don't change unless necessary)
INSTANCE="example.txt"
ORIG="re_degree_iteration_algorithm.txt"
ALT="re_stable_pair_algorithm.txt"
ENUM="enumeration_algorithm.txt"
CORRECTNESS="correctness.txt"
PATH_INSTANCE="$PREPATH$INSTANCE"
PATH_ORIG_ALG="$PREPATH$ORIG"
PATH_ALT_ALG="$PREPATH$ALT"
PATH_ENUM="$PREPATH$ENUM"
PATH_CORRECTNESS="$PREPATH$CORRECTNESS"


# compile
source ~/.bashrc
javac -d . code/correctness/*.java
javac -d . code/degree_iteration/*.java
javac -d . code/enumeration/*.java
javac -d . code/shared/*.java
javac -d . code/stable_pair/*.java

# find regret equal by alternative algorithm
$TIMEOUTCOMMAND $TIMEOUT java -cp $CP code/stable_pair/Main_ReStablePairAlgorithm $PATH_INSTANCE > $PATH_ALT_ALG
EXIT=$?
if [ $EXIT -eq 124 ] ; then echo "timeout $TIMEOUT s" >> $PATH_ALT_ALG ; 
elif [ $EXIT -eq 4 ] ; then echo "upstream timeout $TIMEOUT s" >> $PATH_ALT_ALG ;
elif [ $EXIT -ne 0 ] ; then echo "**uncontrolled error**" >> $PATH_ALT_ALG ; fi   
echo "exitCode $EXIT" >> $PATH_ALT_ALG 
echo "code/stable_pair/Main_ReStablePairAlgorithm completed" >> $PATH_ALT_ALG 

# find regret equal by original algorithm
$TIMEOUTCOMMAND $TIMEOUT java -cp $CP code/degree_iteration/Main_ReDegreeIterationAlgorithm $PATH_INSTANCE > $PATH_ORIG_ALG
EXIT=$?
if [ $EXIT -eq 124 ] ; then echo "timeout $TIMEOUT s" >> $PATH_ORIG_ALG ; 
elif [ $EXIT -eq 4 ] ; then echo "upstream timeout $TIMEOUT s" >> $PATH_ORIG_ALG ;
elif [ $EXIT -ne 0 ] ; then echo "**uncontrolled error**" >> $PATH_ORIG_ALG ; fi   
echo "exitCode $EXIT" >> $PATH_ORIG_ALG 
echo "code/degree_iteration/Main_ReDegreeIterationAlgorithm completed" >> $PATH_ORIG_ALG 

# find all stable matchings
$TIMEOUTCOMMAND $TIMEOUT java -cp $CP code/enumeration/Main_Enumerate $PATH_INSTANCE > $PATH_ENUM
EXIT=$?
if [ $EXIT -eq 124 ] ; then echo "timeout $TIMEOUT s" >> $PATH_ENUM ; 
elif [ $EXIT -eq 4 ] ; then echo "upstream timeout $TIMEOUT s" >> $PATH_ENUM ;
elif [ $EXIT -ne 0 ] ; then echo "**uncontrolled error**" >> $PATH_ENUM ; fi   
echo "exitCode $EXIT" >> $PATH_ENUM 
echo "code/enumeration/Main_Enumerate completed" >> $PATH_ENUM 

# run the correctness tester without running the IP
java -cp $CP code/correctness/Main_Tester $PATH_INSTANCE $PATH_ORIG_ALG $PATH_ALT_ALG $PATH_ENUM false > $PATH_CORRECTNESS
EXIT=$?
if [ $EXIT -eq 5 ] ; then echo "timeout $TIMEOUT s" >> $PATH_CORRECTNESS ; 
elif [ $EXIT -eq 4 ] ; then echo "upstream timeout $TIMEOUT s" >> $PATH_CORRECTNESS ;
elif [ $EXIT -ne 0 ] ; then echo "**uncontrolled error**" >> $PATH_CORRECTNESS ; fi   
echo "exitCode $EXIT" >> $PATH_CORRECTNESS 
echo "code/correctness/Main_Tester completed" >> $PATH_CORRECTNESS 

# # run the correctness tester IP
# python correctness/pulpIP.py $PATH_INSTANCE $TIMEOUT >> $PATH_CORRECTNESS
# EXIT=$?
# if [ $EXIT -eq 5 ] ; then echo "ip_timeout $TIMEOUT s" >> $PATH_CORRECTNESS ; 
# elif [ $EXIT -eq 4 ] ; then echo "upstream timeout $TIMEOUT s" >> $PATH_CORRECTNESS ;
# elif [ $EXIT -ne 0 ] ; then echo "**uncontrolled error**" >> $PATH_CORRECTNESS ; fi  
# echo "exitCode $EXIT" >> $PATH_CORRECTNESS 
# echo "correctness/pulpIP.py completed" >> $PATH_CORRECTNESS 

echo "all processes complete"

