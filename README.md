# regret-equal-SM Readme

Algorithms for new types of fair stable matchings
******************************
******************************

1) what is this software?
2) software and data
3) compiling and running an example
4) interpreting results files
5) running multiple instances - preparation
6) running multiple instances
7) reproducing paper experiments
8) versions


******************************

# 1) what is this software?

This software contains the following algorithms for the Stable Marriage problem with Incomplete lists (SMI):
* The Regret-Equal Degree Iteration Algorithm (REDI), also known in the code as the original algorithm
* The Regret-Equal Stable Pair Algorithm (RESP), also known in the code as the alternative algorithm
* The Enumeration Algorithm (ENUM)

A stable matching is regret-equal if it minimises the difference in ranks between a worst-off man and a worst-off woman, among the set of all stable matchings. The Algorithms REDI and RESP find a regret-equal stable matching in an instance of SMI. 

The Algorithm ENUM finds all stable matchings for an instance of SMI. This comprises the Extended Gale-Shapley Algorithm to find the man-optimal stable matching and woman-optimal stable matching and then the minimal differences algorithm and digraph creator which allows us to output all stable matchings of an instance. 

Correctness testers are also provided which allow us to check that all stable matchings found by all algorithms are indeed stable, and for the ENUM Algorithm, to check that all stable matchings are found, using an Integer Program (IP).


******************************

# 2) software and data

You must have Java and Python installed on your computer to run all programs and 
PuLP and CPLEX installed to run the IP program.

For PuLP installation please follow the guide at:
https://pythonhosted.org/PuLP/main/installing_pulp_at_home.html

For CPLEX installation follow the guide at: 
https://www.ibm.com/support/knowledgecenter/de/SSSA5P_12.6.1/ilog.odms.studio.help/Optimization_Studio/topics/COS_installing.html making sure you set the $PATH and $LD_LIBRARY_PATH environment variables if on linux.

Download / git clone the regret-equal-SM software from Github:
https://github.com/fmcooper/regret-equal-SM


Data and software information can be found in the following paper: 
Algorithms for new types of fair stable matchings
Authors: Frances Cooper and David Manlove

* The paper is located at: 
* The data is located at: 
* The software is located at: 


******************************

# 3) compiling and running an example

If you are on a mac computer, cd into the regret-equal-SM directory and then run the following command:

```bash
$ source ./runExample.sh
```

This will compile and run an example (found in the "examples" directory) through all the required steps. Note that the default example to run can be found at "examples/DM_pg91/example.txt", which is the SM instance originally described in Irving, Leather and Gusfield's 1987 paper, "An Efficient Algorithm for the “Optimal” Stable Marriage". Results for the example may then be found in the "examples/DM_pg91" directory.

If you are on linux, open the "runExample.sh" file and change the timeout command from "gtimeout" to "timeout". Then proceed as above.

Notes
* There are several variables that may be changed in this script including the timeout command, timeout time, java classpath and location of the example. For more information please see the script.
* If you get CPLEX errors at this point then you will need to check you have followed all installation instructions above. Also ensure that your path variables have been correctly exported by running "$ echo $PATH" etc. 
* If you wish to run algorithms separately, please see the script for an example of how they are called.
* If you want to run the IP correctness testing, please un-comment this section of the script.


******************************

# 4) interpreting results files

Once the above example has been run, there will be four results files created in the "examples/DM_pg91" directory alongside the example.txt instance file.

* re_degree_iteration_algorithm.txt: This is the results file for Algorithm REDI. It comprises the output stable matching, various measures associated with the matching and algorithm statistics.
* re_stable_pair_algorithm.txt: This is the results file for Algorithm RESP. It comprises the output stable matching, various measures associated with the matching and algorithm statistics.
* enumeration_algorithm.txt: This is the results file for Algorithm ENUM. This file lists and gives statistics on all rotations and stable matchings of the instance. The index of stable matching for the rank-maximal, generous, sex_equal, egalitarian, median, balanced, minimum regret, min-regret sum and regret-equal stable matchings are given, and well as the digraph, simple digraph, simple reverse digraph and algorithm statistics.
* correctness.txt: This file gives correctness results for Algorithms REDI, RESP and ENUM. For Algorithms REDI and RESP, correctness checks for the output stable matching are given and the regret-equality score is displayed. For Algorithm ENUM, the number of stable matchings found by the algorithm is displayed, as well as the number that have passed different correctness tests. The regret-equality score of the regret-equal stable matching is also displayed here. At the end of the file, a boolean indicates whether the regret-equality score of all algorithms match. If using the IP correctness testing, it will also have a list of all stable matchings found by the IP as well as statistics on the IP's performance.




******************************

# 5) running multiple instances - preparation

The file "userOptions.txt" lists all instances to be created and run over. The syntax required for each line of this file is:

```
experimentName <numMen>-<minPrefListLength>-<maxPrefListLength>-<skew> <numInstances> <c/nc (correctness testing tag)>
```

As an example, the following line would indicate that we want to create an experiment "S10", with 1000 instances, 10 men, 10 women, preference lists of length 10 and a uniform distribution over preference lists. It also shows that we want to run IP correctness testing on these instances. Please note that for these algorithms SM instances are expected and so it is required that preference lists are complete.

```
S10 10-10-10-0 1000 c
```

Notes
* If lines are removed or added from "userOptions.txt", you will need to update the program at "stats/stats.py" to give the required experiment names (exp_names variable).
* The example "userOptions.txt" file gives 5 experiments of varying size with just a few instances of each type to shorten the running time.


******************************

# 6) running multiple instances

Running these experiments will overwrite any current instances in your "Evaluations" directory so please ensure you have backed up any experiments you would like to keep before running again.

If you are on a mac computer, cd into the regret-equal-SM directory and then run the following command:

```bash
$ source ./runExperiments.sh
```

This will compile, generate instances and run instances through all the required steps. Results for instance ".../Evaluations/exp/Instances/x.txt" of experiment type "exp" may then be found in at the following locations:
* ".../Evaluations/exp/ReDegreeIterationAlgorithm/x.txt"
* ".../Evaluations/exp/ReStablePairAlgorithm/x.txt"
* ".../Evaluations/exp/EnumerationAlgorithm/x.txt"
* ".../Evaluations/exp/Correctness/x.txt"

Summaries of all experiments can be found in the "stats/results" directory, particularly the "correctness.txt" file shows a correctness testing summary, and the "collected_results.txt" file shows the statistical information summary used by "stats/stats.py" to create plots and tables. Other files in this directory include plots and tables.

If you are on linux, open the "runExperiments.sh" file and change the timeout command from "gtimeout" to "timeout". Then proceed as above.

Notes
* There are several variables that may be changed in this script including the timeout command, timeout time, correctness timeout time, location of results, java classpath, java garbage collection options, java heap space per operation and number of jobs to run simultaneously. For more information please see the script. Currently, the number of jobs to run simultaneously and the java heap space are kept reasonably small. 
* If you don't want to run the IP correctness testing, please comment out this section of the script.
* If you change the timeout time and want to accurately gain statistical results on the time taken then you will also need to change the timeout time in the "stats/stats.py" program.
* Currently the small size of instances set to run in the "userOptions.txt" file means that the bar charts in the associated paper cannot be built. Should instances of size 100, 400, 700 and 1000 be processed these bar charts can again be created. This is done by changing the value of "histogram" in the program "stats/stats.py" to "true".

******************************

# 7) reproducing paper experiments
There are several things to change in order to reproduce the experiments in the associated paper. 

* Firstly, the associated data needs to be downloaded and placed in the "Evaluations" directory. Should you wish to place this at a different location, the "prepath" variable in "runExperiments.sh" needs to be updated. You may delete any files in the downloaded data that are results files (keep the instance files).
* In file "runExperiments.sh", comment out lines 

```
export -f createInstance
cat $INSTANCESFILE | parallel $PARALLEL_OPTS createInstance {}
```

in order to stop instances being overwritten.

* Alter any of script "runExperiments.sh" as described in section 6.
* Change the value of "histogram" in the file "stats/stats.py" to "true" and update varible "exp_name" from
```
exp_names = ['S6', 'S8', 'S10', 'S20', 'S30']
```
 to 
 ```
 exp_names = ['S10', 'S20', 'S30', 'S40', 'S50', 'S60', 'S70', 'S80', 'S90', 'S100', 'S200', 'S300', 'S400', 'S500', 'S600', 'S700', 'S800', 'S900', 'S1000']
 ```
* Java and Python configurations and the computer hardware used in the original experiments can be found in the associated paper.

Notes
* Should you wish to run only the statistical program ("stats/stats.py") over already collected results, move file "stats/results/output_2020_01/collected_results.txt" to directory "stats/results". The statistical program will now use these results as a basis for plots and tables.


******************************

# 8) versions

V1.0.0 initial version

******************************

This readme is based on a readme for a previous project found here: https://doi.org/10.5281/zenodo.1183221
