import sys
import numpy as np
import glob
from scipy.stats import sem
from sys import argv
import matplotlib.ticker as FormatStrFormatter
import os, os.path
import datetime

# Iterates over all correctness files and collates results.
# @author Frances

np.set_printoptions(suppress=True)
prePath = sys.argv[1]

################# variables
correctnessNames = os.listdir(prePath)
dirName = "stats/results"
outFileName = dirName + "/correctness.txt"

#####################################
# main
#####################################
def main():
    if not os.path.exists(dirName):
        os.makedirs(dirName)

    now = datetime.datetime.now()
    outFile = open(outFileName, 'w')
    outFile.write("Correctness runthrough conducted at: " 
        + now.strftime("%Y-%m-%d %H:%M") + "\n\n")
    outFile.close()

    errorFnames = ""
    failFnames = ""

    # for each experiment type
    for cname in correctnessNames:
        pathResults = prePath + cname + "/Correctness/"

        totalCorrectnessChecked = 0
        total_reDegreeIter_timeout = 0
        total_reStablePair_timeout = 0
        totalRegEqEnum_timeout = 0
        totalIPTimeout = 0
        totalPassed = 0
        totalFailed = 0

        # run over the results to get the optimal matching indices
        for name in os.listdir(pathResults):
            if os.path.isfile(pathResults + name):
                totalCorrectnessChecked+=1
                with open(pathResults + name) as f:
                    enumCorr = ""
                    rediCorr = ""
                    respCorr = ""
                    scoreMatch = ""
                    numEnum = -1
                    numIP = -2
                    thisInstRegEqAlg_timeout = False
                    thisInstRegEqAlgAlt_timeout = False
                    thisInstRegEqEnum_timeout = False
                    thisInstIP_timeout = False
                    content = f.readlines()
                    for s in content:
                        # general info
                        if "REDI_Alg_timeout" in s:
                            total_reDegreeIter_timeout += 1
                            thisInstRegEqAlg_timeout = True
                        if "RESP_Alg_timeout" in s:
                            total_reStablePair_timeout += 1
                            thisInstRegEqAlgAlt_timeout = True
                        if "ENUM_Alg_timeout" in s:
                            totalRegEqEnum_timeout += 1
                            thisInstRegEqEnum_timeout = True
                        if "ip_timeout" in s:
                            totalIPTimeout += 1
                            thisInstIP_timeout = True
                        if "error" in s:
                            errorFnames += name + "\n";

                        # tested results
                        if "ENUM_Alg_correctness_pass:" in s:
                            enumCorr = s.split()[1]
                        if "REDI_Alg_correctness_pass:" in s:
                            rediCorr = s.split()[1]
                        if "RESP_Alg_correctness_pass:" in s:
                            respCorr = s.split()[1]
                        if "REscoreMatch:" in s:
                            scoreMatch = s.split()[1]

                        if "ENUM_Alg_numStableMatchings:" in s:
                            numEnum = int(s.split()[1])
                        if "num_solutions_found_by_ip" in s:
                            numIP = int(s.split()[1])
                    passed = False

                    # an instance passed if the correctness passed, IP tests passed and
                    # the regret equal scores match
                    testedIP = (numEnum == -1 and numIP == -2 and thisInstIP_timeout == False)
                    if (enumCorr == "true" or thisInstRegEqEnum_timeout) and \
                        (rediCorr == "true" or thisInstRegEqAlg_timeout) and \
                        (respCorr == "true" or thisInstRegEqAlgAlt_timeout) and \
                        (numEnum == numIP or thisInstIP_timeout or not testedIP) and \
                        (scoreMatch == "true" or scoreMatch == "timeout"):
                        passed = True

                    # record how many instances passed and failed
                    if passed:
                        totalPassed = totalPassed + 1
                    else:
                        totalFailed = totalFailed + 1
                        failFnames += name + "\n"

        # output to a summary file
        outFile = open(outFileName, 'a')
        outFile.write("# experiment: " + cname + "\t")
        outFile.write("totalChecked: " + str(totalCorrectnessChecked) + "\t")
        outFile.write("total_reDegreeIter_timeout: " 
            + str(total_reDegreeIter_timeout) + "\t")
        outFile.write("total_reStablePair_timeout: " 
            + str(total_reStablePair_timeout) + "\t")
        outFile.write("totalRegEqEnum_timeout: " 
            + str(totalRegEqEnum_timeout) + "\t")
        outFile.write("totalIPTimeout: " + str(totalIPTimeout) + "\t")
        outFile.write("totalPassed: " + str(totalPassed) + "\t")
        outFile.write("totalFailed: " + str(totalFailed) + "\t")
        outFile.write("\n")
        outFile.close()

    outFile = open(outFileName, 'a')
    outFile.write("\n# errors: \n" + errorFnames)
    outFile.write("\n\n# fails: \n" + failFnames)
    outFile.close()

    exit(0)



#####################################
# main def
#####################################
if __name__ == '__main__':
    main()

