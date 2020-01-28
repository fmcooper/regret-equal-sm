import sys
import numpy as np
import math
from sys import argv
import os, os.path
import datetime

# Used by the python statistics program to collect results for each experiment
# together
# @author Frances

timeout_ms = 3600000

# statistical information for a single instance
class instance:
    def __init__(self):
        # general stats
        self.numRotations = -1
        self.numStableMatchings = -1

        # optimality criteria
        self.sexequal = optimal_criteria_stats()
        self.egalitarian = optimal_criteria_stats()
        self.minregret = optimal_criteria_stats()
        self.balanced = optimal_criteria_stats()
        self.regretequal = optimal_criteria_stats()
        self.minregretsum = optimal_criteria_stats()
        self.all_optimal = [self.sexequal, self.egalitarian, self.minregret, self.balanced, self.regretequal, self.minregretsum]

        # matching scores
        self.seScores = []
        self.egalScores = []
        self.degreeScores = []
        self.balancedScores = []
        self.reScores = []
        self.rsScores = []

        # optimal indices
        self.seIndex = -1
        self.egalIndex = -1
        self.minRegIndex = -1
        self.balIndex = -1
        self.reIndex = -1
        self.minSumRegIndex = -1

        # durations (3 algorithms)
        self.REalg_Duration_Total_milliseconds = -1
        self.REalgAlt_Duration_Total_milliseconds = -1
        self.Enum_Duration_Total_milliseconds = -1

        # value of 6 measures for the REalg(REDI) and REalgAlt(RESP) algorithm
        # outputs (regret equal will be optimal)
        self.REalg_balancedScore = -1;
        self.REalg_sexEqualityCost = -1;
        self.REalg_egalitarianCost = -1;
        self.REalg_degree = -1;
        self.REalg_regretEqualScore = -1;
        self.REalg_regretSum = -1;
        self.REalgAlt_balancedScore = -1;
        self.REalgAlt_sexEqualityCost = -1;
        self.REalgAlt_egalitarianCost = -1;
        self.REalgAlt_degree = -1;
        self.REalgAlt_regretEqualScore = -1;
        self.REalgAlt_regretSum = -1;

    def calculate_optimal_indices(self):
        self.sexequal.matching_indices = np.where(self.seScores == self.seScores[self.seIndex])[0]
        self.egalitarian.matching_indices = np.where(self.egalScores == self.egalScores[self.egalIndex])[0]
        self.minregret.matching_indices = np.where(self.degreeScores == self.degreeScores[self.minRegIndex])[0]
        self.balanced.matching_indices = np.where(self.balancedScores == self.balancedScores[self.balIndex])[0]
        self.regretequal.matching_indices = np.where(self.reScores == self.reScores[self.reIndex])[0]
        self.minregretsum.matching_indices = np.where(self.rsScores == self.rsScores[self.minSumRegIndex])[0]

    def calculate_matching_optimal_counts(self):
        self.matching_counts = np.zeros(7)
        for i in range(len(self.seScores)):
            count = 0
            if self.seScores[i] == self.seScores[self.seIndex]: 
                count += 1
            if self.egalScores[i] == self.egalScores[self.egalIndex]: 
                count += 1
            if self.degreeScores[i] == self.degreeScores[self.minRegIndex]: 
                count += 1
            if self.balancedScores[i] == self.balancedScores[self.balIndex]: 
                count += 1
            if self.reScores[i] == self.reScores[self.reIndex]: 
                count += 1
            if self.rsScores[i] == self.rsScores[self.minSumRegIndex]: 
                count += 1
            self.matching_counts[count] += 1

    def calculate_additional_stats(self):
        self.calculate_optimal_indices()
        self.calculate_matching_optimal_counts()

        for opt in self.all_optimal:
            opt.sexequal_cost = np.amin(self.seScores[opt.matching_indices])
            opt.egalitarian_cost = np.amin(self.egalScores[opt.matching_indices])
            opt.degree = np.amin(self.degreeScores[opt.matching_indices])
            opt.balanced_score = np.amin(self.balancedScores[opt.matching_indices])
            opt.regretequal_score = np.amin(self.reScores[opt.matching_indices])
            opt.regretsum = np.amin(self.rsScores[opt.matching_indices])

    def __str__(self):
        s = "\nEgalitarian {}\t {}\t ".format(self.egalScores, str(self.egalitarian)) 
        s += "\nSex-equal {}\t {}\t ".format(self.seScores, str(self.sexequal))
        s += "\nminRegret {}\t {}\t ".format(self.degreeScores, str(self.minregret))
        s += "\nBalanced {}\t {}\t ".format(self.balancedScores, str(self.balanced)) 
        s += "\nRegret-equal {}\t {}\t ".format(self.reScores, str(self.regretequal)) 
        s += "\nMin-regret sum {}\t {}\t ".format(self.rsScores, str(self.minregretsum)) 
        return s


# holds statistical information for a particular set of optimal stable matchings
class optimal_criteria_stats:
    def __init__(self):
        self.matching_indices = []
        self.sexequal_cost = -1
        self.egalitarian_cost = -1
        self.degree = -1
        self.balanced_score = -1
        self.regretequal_score = -1
        self.regretsum = -1

    def __str__(self):
        s = "indices {}\t se {}\t egal {}\t degree {}\t bal {}\t re {}\t rs {}".format(
            str(self.matching_indices),
            str(self.sexequal_cost),
            str(self.egalitarian_cost),
            str(self.degree),
            str(self.balanced_score),
            str(self.regretequal_score),
            str(self.regretsum))
        return s


# Iterates over all results files and collates results.
np.set_printoptions(suppress=True)
pre_path = sys.argv[1]

################# variables
exp_names = sorted(os.listdir(pre_path))
dirName = "stats/results"
av_stats = dirName + '/collected_results.txt'
d = {}
maxprefs = {}

#####################################
# main
#####################################
def main():
    if not os.path.exists(dirName):
        os.makedirs(dirName)

    # calculate results
    now = datetime.datetime.now()
    av_stats_file = open(av_stats, 'w')
    av_stats_file.write(
        "Results collected at: " + now.strftime("%Y-%m-%d %H:%M") + "\n\n")
    av_stats_file.close()

    # for each experiment type
    for ind, instance_type in enumerate(exp_names):
        print(instance_type)
        post_path_instances = "/Instances/"
        post_path_degree_iter = "/ReDegreeIterationAlgorithm/"
        post_path_stable_pair = "/ReStablePairAlgorithm/"
        post_path_enumeration = "/EnumerationAlgorithm/"
        

        collectResults(
            instance_type, 
            pre_path, 
            post_path_instances, 
            post_path_degree_iter, 
            post_path_stable_pair, 
            post_path_enumeration)

    exit(0)


#####################################
# collectResults
#####################################
def collectResults(
    instance_type, 
    pre_path, 
    post_path_instances, 
    post_path_degree_iter, 
    post_path_stable_pair, 
    post_path_enumeration):

    infeasibleCounts = []

    pathInstance = pre_path + instance_type + post_path_instances
    pathResultsOrig = pre_path + instance_type + post_path_degree_iter
    pathResultsAlt = pre_path + instance_type + post_path_stable_pair
    pathResultsEnum = pre_path + instance_type + post_path_enumeration

    # collect the raw data and calculate the averages for latex
    total_instances, totalre_timeout, totalrea_timeout, totalree_timeout, all_instances, numMen, skew = collectRawData(
        instance_type, 
        pathInstance, 
        pathResultsOrig, 
        pathResultsAlt, 
        pathResultsEnum)

    outputToFile(
        instance_type, 
        total_instances, 
        totalre_timeout,
        totalrea_timeout,
        totalree_timeout, 
        all_instances,
        numMen,
        skew);


#####################################
# collectRawData
#####################################
def collectRawData(inst, pathInstance, pathResultsOrig, pathResultsAlt, pathResultsEnum):
    all_instances = []
    numMen = ""
    skew = ""
    total_instances = 0
    totalre_timeout = 0
    totalrea_timeout = 0
    totalree_timeout = 0

    # run over the instance statistics
    name = os.listdir(pathInstance)[0]
    with open(pathInstance + name) as f:
        content = f.readlines()
        for s in content:
            # general info
            if "numMenOrWomen" in s:
                numMen = s.split()[1];
            if "skew" in s:
                skew = s.split()[1];

    # run over the results to get the optimal matching indices
    for name in os.listdir(pathResultsOrig):
        origTimeout = False
        origError = False
        altTimeout = False
        altError = False
        enumTimeout = False
        enumError = False
        inst = instance()
        total_instances+=1

        # original algorithm (REDI)
        if os.path.isfile(pathResultsOrig + name):
            with open(pathResultsOrig + name) as f:
                content = f.readlines()
                for s in content:
                    # general info
                    if "timeout" in s:
                        origTimeout = True
                        totalre_timeout += 1
                    if "error" in s:
                        origError = True
                    if "Duration_Total_ms" in s:   
                        inst.REalg_Duration_Total_milliseconds = float(s.split()[1])
                    if "balancedScore:" in s:   
                        inst.REalg_balancedScore = int(s.split()[1])
                    if "sexEqualityCost:" in s:   
                        inst.REalg_sexEqualityCost = int(s.split()[1])
                    if "egalitarianCost:" in s:   
                        inst.REalg_egalitarianCost = int(s.split()[1])
                    if "degree:" in s:   
                        inst.REalg_degree = int(s.split()[1])
                    if "regretEqualScore:" in s:   
                        inst.REalg_regretEqualScore = int(s.split()[1])
                    if "sumRegret:" in s:   
                        inst.REalg_regretSum = int(s.split()[1])


        # alternative algorithm (RESP)
        if os.path.isfile(pathResultsAlt + name):
            with open(pathResultsAlt + name) as f:
                content = f.readlines()
                for s in content:
                    # general info
                    if "timeout" in s:
                        altTimeout = True
                        totalrea_timeout += 1
                    if "error" in s:
                        altError = True
                    if "Duration_Total_ms" in s:
                        inst.REalgAlt_Duration_Total_milliseconds = float(s.split()[1])
                    if "balancedScore:" in s:   
                        inst.REalgAlt_balancedScore = int(s.split()[1])
                    if "sexEqualityCost:" in s:   
                        inst.REalgAlt_sexEqualityCost = int(s.split()[1])
                    if "egalitarianCost:" in s:   
                        inst.REalgAlt_egalitarianCost = int(s.split()[1])
                    if "degree:" in s:   
                        inst.REalgAlt_degree = int(s.split()[1])
                    if "regretEqualScore:" in s:   
                        inst.REalgAlt_regretEqualScore = int(s.split()[1])
                    if "sumRegret:" in s:   
                        inst.REalgAlt_regretSum = int(s.split()[1])



        # enumerating algorithm
        if os.path.isfile(pathResultsEnum + name):
            with open(pathResultsEnum + name) as f:
                seScores=[]
                egalScores=[]
                degreeScores=[]
                reScores=[]
                balancedScores=[]
                rsScores=[]

                content = f.readlines()
                for s in content:
                    if "sex_equal_index" in s:
                        inst.seIndex = int(s.split()[1])
                    if "egalitarian_index" in s:
                        inst.egalIndex = int(s.split()[1])
                    if "balanced_index" in s:
                        inst.balIndex = int(s.split()[1])
                    if "minimumRegret_index" in s:
                        inst.minRegIndex = int(s.split()[1])
                    if "regretEqual_index" in s:
                        inst.reIndex = int(s.split()[1])
                    if "minimumSumRegret_index" in s:
                        inst.minSumRegIndex = int(s.split()[1])

                    if "numStableMatchings" in s:
                        inst.numStableMatchings = int(s.split()[1])
                    if "numRotations" in s:
                        inst.numRotations = int(s.split()[1])

                    if "sexEqualityCost_" in s:
                        seScores.append(int(s.split()[1]))
                    if "egalitarianCost_" in s:
                        egalScores.append(int(s.split()[1]))
                    if "degree_" in s:
                        degreeScores.append(int(s.split()[1]))
                    if "regretEqualScore_" in s:
                        reScores.append(int(s.split()[1]))
                    if "balancedScore_" in s:
                        balancedScores.append(int(s.split()[1]))
                    if "sumRegret_" in s:
                        rsScores.append(int(s.split()[1]))

                    # general info
                    if "timeout" in s:
                        enumTimeout = True
                        totalree_timeout += 1
                    if "error" in s:
                        enumError = True
                    if "Duration_Total_milliseconds" in s:
                        inst.Enum_Duration_Total_milliseconds = float(s.split()[1])

                inst.seScores = np.array(seScores)
                inst.egalScores = np.array(egalScores)
                inst.degreeScores = np.array(degreeScores)
                inst.reScores = np.array(reScores)
                inst.balancedScores = np.array(balancedScores)
                inst.rsScores = np.array(rsScores)

                inst.calculate_additional_stats()

        # save the results if none have errors
        if not origError and not altError and not enumError:
            # save the results if at least one has not timed out
            if not origTimeout or not altTimeout or not enumTimeout:
                all_instances.append(inst) 
         

    return total_instances, totalre_timeout, totalrea_timeout, totalree_timeout, all_instances, numMen, skew


#####################################
# outputToFile
#####################################
def outputToFile(
    instance_type, 
    total_instances, 
    totalre_timeout,
    totalrea_timeout,
    totalree_timeout, 
    all_instances,
    numMen,
    skew):

    # calculate the averages and output
    avstatsFile = open(av_stats, "a")
    avstatsFile.write('\n# stats file for all instance types of ' + instance_type + '\n')
    avstatsFile.write('{}numMenOrWomen {:10}\n'.format(instance_type, numMen))
    avstatsFile.write('{}skew {:10}\n'.format(instance_type, skew))
    writeStatsToFile(avstatsFile, instance_type, "numRotations", [inst.numRotations for inst in all_instances])
    writeStatsToFile(avstatsFile, instance_type, "numStableMatchings", [inst.numStableMatchings for inst in all_instances])
    for i in range(7):
        writeStatsToFile(avstatsFile, instance_type, str(i) + "_matchingCount", [inst.matching_counts[i] for inst in all_instances])

    # total num instances and timeout
    avstatsFile.write('{}numInstances {:10}\n'.format(instance_type, str(total_instances)))
    avstatsFile.write('{}num_re_timeout {:10}\n'.format(instance_type, str(totalre_timeout)))
    avstatsFile.write('{}num_rea_timeout {:10}\n'.format(instance_type, str(totalrea_timeout)))
    avstatsFile.write('{}num_ree_timeout {:10}\n'.format(instance_type, str(totalree_timeout)))
    
    # durations of the 3 different algorithms
    durations_realg = np.array([inst.REalg_Duration_Total_milliseconds for inst in all_instances])
    durations_realgalt = np.array([inst.REalgAlt_Duration_Total_milliseconds for inst in all_instances])
    durations_enumalg = np.array([inst.Enum_Duration_Total_milliseconds for inst in all_instances])
    durations_realg = np.pad(durations_realg, (0, totalre_timeout), 'constant', constant_values=(timeout_ms))
    durations_realgalt = np.pad(durations_realgalt, (0, totalrea_timeout), 'constant', constant_values=(timeout_ms))
    durations_enumalg = np.pad(durations_enumalg, (0, totalree_timeout), 'constant', constant_values=(timeout_ms))
    writeStatsToFileNonNeg(avstatsFile, instance_type, "duration_REalg_Total", durations_realg)
    writeStatsToFileNonNeg(avstatsFile, instance_type, "duration_REalgAlt_Total", durations_realgalt)
    writeStatsToFileNonNeg(avstatsFile, instance_type, "duration_Enum_Total", durations_enumalg)

    # 5 measures (not regret-equality score) for the REDI and RESP algorithms
    balancedScore_reAlg = np.array([inst.REalg_balancedScore for inst in all_instances])
    egalitarianCost_reAlg = np.array([inst.REalg_egalitarianCost for inst in all_instances])
    sexEqualCost_reAlg = np.array([inst.REalg_sexEqualityCost for inst in all_instances])
    degree_reAlg = np.array([inst.REalg_degree for inst in all_instances])
    regretEqualScore_reAlg = np.array([inst.REalg_regretEqualScore for inst in all_instances])
    regretSum_reAlg = np.array([inst.REalg_regretSum for inst in all_instances])
    writeStatsToFileNonNeg(avstatsFile, instance_type, "reAlg_balancedScore", balancedScore_reAlg)
    writeStatsToFileNonNeg(avstatsFile, instance_type, "reAlg_egalCost", egalitarianCost_reAlg)
    writeStatsToFileNonNeg(avstatsFile, instance_type, "reAlg_sexEqualCost", sexEqualCost_reAlg)
    writeStatsToFileNonNeg(avstatsFile, instance_type, "reAlg_degree", degree_reAlg)
    writeStatsToFileNonNeg(avstatsFile, instance_type, "reAlg_regretEqualScore", regretEqualScore_reAlg)
    writeStatsToFileNonNeg(avstatsFile, instance_type, "reAlg_sumRegret", regretSum_reAlg)

    balancedScore_reAlgAlt = np.array([inst.REalgAlt_balancedScore for inst in all_instances])
    egalitarianCost_reAlgAlt = np.array([inst.REalgAlt_egalitarianCost for inst in all_instances])
    sexEqualCost_reAlgAlt = np.array([inst.REalgAlt_sexEqualityCost for inst in all_instances])
    degree_reAlgAlt = np.array([inst.REalgAlt_degree for inst in all_instances])
    regretEqualScore_reAlgAlt = np.array([inst.REalgAlt_regretEqualScore for inst in all_instances])
    regretSum_reAlgAlt = np.array([inst.REalgAlt_regretSum for inst in all_instances])
    writeStatsToFileNonNeg(avstatsFile, instance_type, "reAlgAlt_balancedScore", balancedScore_reAlgAlt)
    writeStatsToFileNonNeg(avstatsFile, instance_type, "reAlgAlt_egalCost", egalitarianCost_reAlgAlt)
    writeStatsToFileNonNeg(avstatsFile, instance_type, "reAlgAlt_sexEqualCost", sexEqualCost_reAlgAlt)
    writeStatsToFileNonNeg(avstatsFile, instance_type, "reAlgAlt_degree", degree_reAlgAlt)
    writeStatsToFileNonNeg(avstatsFile, instance_type, "reAlgAlt_regretEqualScore", regretEqualScore_reAlgAlt)
    writeStatsToFileNonNeg(avstatsFile, instance_type, "reAlgAlt_sumRegret", regretSum_reAlgAlt)

   
    # write optimal matching stats to file
    name_opt_zip = zip(
        ["MinRegret", "Egalitarian", "SexEqual", "Balanced", "RegretEqual", "MinSumRegret"], 
        [[inst.minregret for inst in all_instances],
        [inst.egalitarian for inst in all_instances],
        [inst.sexequal for inst in all_instances],
        [inst.balanced for inst in all_instances],
        [inst.regretequal for inst in all_instances],
        [inst.minregretsum for inst in all_instances]])

    for name, optimals in name_opt_zip:
        writeStatsToFile(avstatsFile, instance_type, name + "_num", [len(opt.matching_indices) for opt in optimals])
        writeStatsToFile(avstatsFile, instance_type, name + "_degree", [opt.degree for opt in optimals])
        writeStatsToFile(avstatsFile, instance_type, name + "_egalCost", [opt.egalitarian_cost for opt in optimals])
        writeStatsToFile(avstatsFile, instance_type, name + "_sexEqualCost", [opt.sexequal_cost for opt in optimals])
        writeStatsToFile(avstatsFile, instance_type, name + "_regretEqualScore", [opt.regretequal_score for opt in optimals])
        writeStatsToFile(avstatsFile, instance_type, name + "_balancedScore", [opt.balanced_score for opt in optimals])
        writeStatsToFile(avstatsFile, instance_type, name + "_sumRegret", [opt.regretsum for opt in optimals])

    avstatsFile.close()

def writeStatsToFile(avstatsFile, instance_type, stats_name, stats):
    avstatsFile.write('{}Av_{} {:0.1f}\n'.format(instance_type, stats_name, getAverage(stats)))
    avstatsFile.write('{}median_{} {:0.1f}\n'.format(instance_type, stats_name, getMedian(stats)))
    avstatsFile.write('{}5Per_{} {:0.1f}\n'.format(instance_type, stats_name, getPercentile(stats, 5.0)))
    avstatsFile.write('{}95Per_{} {:0.1f}\n'.format(instance_type, stats_name, getPercentile(stats, 95.0)))

def writeStatsToFileNonNeg(avstatsFile, instance_type, stats_name, stats):
    avstatsFile.write('{}Av_{} {:0.1f}\n'.format(instance_type, stats_name, getAverageNonNeg(stats)))
    avstatsFile.write('{}median_{} {:0.1f}\n'.format(instance_type, stats_name, getMedianNonNeg(stats)))
    avstatsFile.write('{}5Per_{} {:0.1f}\n'.format(instance_type, stats_name, getPercentileNonNeg(stats, 5.0)))
    avstatsFile.write('{}95Per_{} {:0.1f}\n'.format(instance_type, stats_name, getPercentileNonNeg(stats, 95.0)))


# returns an array with negative elements removed
def getNonNegArray(array):
    new_array = []
    for elem in array:
        if elem >= 0:
            new_array.append(elem)
    return new_array


# gets the average of an array or returns -1 if array is 0 in length
def getAverage(array):
    if len(array) == 0:
        return -1
    else:
        return np.mean(array, dtype=np.float64)


# gets the average of an array discounting negative elements
def getAverageNonNeg(array):
    return getAverage(getNonNegArray(array))


# gets the minimum of an array or returns -1 if array is 0 in length
def getMin(array):
    if len(array) == 0:
        return -1
    else:
        return np.min(array)


# gets the maximum of an array or returns -1 if array is 0 in length
def getMax(array):
    if len(array) == 0:
        return -1
    else:
        return np.max(array)


# gets the average profile of an array or returns -1 if array is 0 in length
def getAverageProfile(array2D):
    avP = [];
    # average profile
    if len(array2D) == 0:
        avP.append(-1.0)
    else:
        profile = np.array(array2D)
        # print profile
        # print profile.shape
        avprofile = profile.mean(axis=0)
        avprofile = np.around(avprofile, decimals=1)
        for x in avprofile:
            avP.append(x)
    return avP


# gets the median of an array or returns -1 if array is 0 in length
def getMedian(array):
    if len(array) == 0:
        return -1
    else:
        return np.median(array)


# gets the median of an array discounting negative elements
def getMedianNonNeg(array):
    return getMedian(getNonNegArray(array))


# gets a given percentile of an array or returns -1 if array is 0 in length
def getPercentile(array, percentile):
    if len(array) == 0:
        return -1
    else:
        return np.percentile(array, percentile)


# gets a given percentile of an array discounting negative elements
def getPercentileNonNeg(array, percentile):
    return getPercentile(getNonNegArray(array), percentile)


# gets the minimum, maximum and average profile degree of an array or returns -1
# if array is 0 in length
def getMinMaxAvDegree(array2D):
    minDegree = -1
    maxDegree = -1
    totalDegree = 0.0
    avDegree = -1
    # average profile
    if len(array2D) == 0:
        return minDegree, maxDegree, avDegree
    else:
        for profile in array2D:
            degree = getDegree(profile)
            totalDegree += degree
            if minDegree == -1 or degree < minDegree:
                minDegree = degree
            if maxDegree == -1 or degree > maxDegree:
                maxDegree = degree
    avDegree = float(totalDegree) / float(len(array2D))

    return minDegree, maxDegree, avDegree


# gets the minimum, maximum and average choice of an array or returns -1 if
# array is 0 in length
def getMinMaxAvChoices(array2D, firstOrLast):
    minChoice = -1
    maxChoice = -1
    totalChoice = 0.0
    avChoice = -1
    if len(array2D) == 0:
        return minChoice, maxChoice, avChoice
    else:
        for profile in array2D:
            numChoice = -1;
            if firstOrLast == "first":
                numChoice = profile[0]
            elif firstOrLast == "last":
                numChoice = profile[len(profile) - 1]
            totalChoice += numChoice
            if minChoice == -1 or numChoice < minChoice:
                minChoice = numChoice
            if maxChoice == -1 or numChoice > maxChoice:
                maxChoice = numChoice
    avChoice = float(totalChoice) / float(len(array2D))

    return minChoice, maxChoice, avChoice


# gets the average profile of an array or returns -1 if array is 0 in length
def getAverageProfileString(array):
    avPString = "";
    # average profile
    avPString = "$<$ "
    for x in array:
        avPString += '${:0.1f}$ '.format(x)
    avPString += "$>$"
    return avPString


# gets the degree of a profile array or returns -1 if array is 0 in length
def getDegree(profile):
    # average profile
    if len(profile) == 0:
        return -1
    else:
        count = 0
        for i in reversed(profile):
            if i == 0:
                count = count + 1;
            if not i == 0:
                return len(profile) - count
        return len(profile) - count


# returns the average number of people in a final section of the profile
def getMinMaxAvLast(array2D, fractionOfProfile):
    if len(array2D) == 0:
        return -1, -1, -1
    else:
        profile2D = np.array(array2D)
        sums = []
        indexFrom = int(len(profile2D[0]) * (1 - fractionOfProfile))
        for i in range(len(profile2D)):
            sums.append(sum(profile2D[i][indexFrom:]))
        return np.array(sums).min(), np.array(sums).max(), np.array(sums).mean()


# returns the cieling of the division of two numbers
def ceildiv(a, b):
    return -(-a // b)


# gets the average of an array or returns -1 if array is 0 in length
def getAverage(array):
    if len(array) == 0:
        return -1
    else:
        return np.mean(array, dtype=np.float64)


#####################################
# main def
#####################################
if __name__ == '__main__':
    main()

