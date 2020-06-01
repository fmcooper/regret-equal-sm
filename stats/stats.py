import matplotlib
matplotlib.use('Agg')
import sys
import numpy as np
import matplotlib.pylab as plt
import glob
import math
import scipy
from scipy.stats import sem
from sys import argv
import matplotlib.ticker as FormatStrFormatter
import os, os.path
import datetime
from scipy.optimize import curve_fit

# Iterates over all results files and collates results.
# @author Frances

timeout_ms = 3600000
np.set_printoptions(suppress=True)

# decides whether or not to include results from second regret equal algorithm 
# (not in paper)
paper_string = sys.argv[1]
paper = False
if paper_string == 'true':
    paper = True

################# variables
exp_names = ['S10', 'S20', 'S30', 'S40', 'S50', 'S60', 'S70', 'S80', 'S90', 'S100', 'S200', 'S300', 'S400', 'S500', 'S600', 'S700', 'S800', 'S900', 'S1000']
exp_names = ['S6', 'S8', 'S10']

 # set to true if required instance sizes are run
histogram = False

dir_name = "stats/results"
av_stats = dir_name + '/collected_results.txt'
d = {}
maxprefs = {}
blues = ['#B0E4FF', '#7BB4E3', '#4685C8', '#1156AD']

#####################################
# main
#####################################
def main():
    if not os.path.exists(dir_name):
        os.makedirs(dir_name)

    getAverages()
    createPlots()
    createTables()
    if histogram:
        createHistograms()

    exit(0)


#####################################
# collect the raw data from each instance file
#####################################
def getAverages():
    with open(av_stats) as inF:
        content = inF.readlines()
        for s in content:
            if len(s) < 2 or s[0]=="#":
                continue
            else:
                ssplit = s.split()
                d[ssplit[0]] = ssplit[1:]


#####################################
# matplotlib plots
#####################################
def createPlots():
    # collect the data
    n=[]
    for exp in exp_names:
        n.append(int(exp[1:]))

    ###############
    # optimal measures plot
    ###############
    measures = ["degree", "egalCost", "sexEqualCost", "balancedScore", "regretEqualScore", "sumRegret"]
    labels = ["Degree", "Egalitarian cost", "Sex-equal score", "Balanced score", "Regret-equal score", "Sum regret"]
    for measure, label in zip(measures, labels):
        minreg_stats_av, minreg_stats_median, minreg_stats_5, minreg_stats_95 = [], [], [], []
        egal_stats_av, egal_stats_median, egal_stats_5, egal_stats_95 = [], [], [], []
        se_stats_av, se_stats_median, se_stats_5, se_stats_95 = [], [], [], []
        bal_stats_av, bal_stats_median, bal_stats_5, bal_stats_95 = [], [], [], []
        re_stats_av, re_stats_median, re_stats_5, re_stats_95 = [], [], [], []
        msr_stats_av, msr_stats_median, msr_stats_5, msr_stats_95 = [], [], [], []
        redi_stats_av, redi_stats_median, redi_stats_5, redi_stats_95 = [], [], [], []
        resp_stats_av, resp_stats_median, resp_stats_5, resp_stats_95 = [], [], [], []

        # collect the data
        for exp in exp_names:
            minreg_stats_av.append(float(d['{}Av_MinRegret_{}'.format(exp, measure)][0]))
            egal_stats_av.append(float(d['{}Av_Egalitarian_{}'.format(exp, measure)][0]))
            se_stats_av.append(float(d['{}Av_SexEqual_{}'.format(exp, measure)][0]))
            bal_stats_av.append(float(d['{}Av_Balanced_{}'.format(exp, measure)][0]))
            re_stats_av.append(float(d['{}Av_RegretEqual_{}'.format(exp, measure)][0]))
            msr_stats_av.append(float(d['{}Av_MinSumRegret_{}'.format(exp, measure)][0]))
            redi_stats_av.append(float(d['{}Av_reAlg_{}'.format(exp, measure)][0]))
            resp_stats_av.append(float(d['{}Av_reAlgAlt_{}'.format(exp, measure)][0]))

            minreg_stats_median.append(float(d['{}median_MinRegret_{}'.format(exp, measure)][0]))
            egal_stats_median.append(float(d['{}median_Egalitarian_{}'.format(exp, measure)][0]))
            se_stats_median.append(float(d['{}median_SexEqual_{}'.format(exp, measure)][0]))
            bal_stats_median.append(float(d['{}median_Balanced_{}'.format(exp, measure)][0]))
            re_stats_median.append(float(d['{}median_RegretEqual_{}'.format(exp, measure)][0]))
            msr_stats_median.append(float(d['{}median_MinSumRegret_{}'.format(exp, measure)][0]))
            redi_stats_median.append(float(d['{}median_reAlg_{}'.format(exp, measure)][0]))
            resp_stats_median.append(float(d['{}median_reAlgAlt_{}'.format(exp, measure)][0]))

            minreg_stats_5.append(float(d['{}5Per_MinRegret_{}'.format(exp, measure)][0]))
            egal_stats_5.append(float(d['{}5Per_Egalitarian_{}'.format(exp, measure)][0]))
            se_stats_5.append(float(d['{}5Per_SexEqual_{}'.format(exp, measure)][0]))
            bal_stats_5.append(float(d['{}5Per_Balanced_{}'.format(exp, measure)][0]))
            re_stats_5.append(float(d['{}5Per_RegretEqual_{}'.format(exp, measure)][0]))
            msr_stats_5.append(float(d['{}5Per_MinSumRegret_{}'.format(exp, measure)][0]))
            redi_stats_5.append(float(d['{}5Per_reAlg_{}'.format(exp, measure)][0]))
            resp_stats_5.append(float(d['{}5Per_reAlgAlt_{}'.format(exp, measure)][0]))

            minreg_stats_95.append(float(d['{}95Per_MinRegret_{}'.format(exp, measure)][0]))
            egal_stats_95.append(float(d['{}95Per_Egalitarian_{}'.format(exp, measure)][0]))
            se_stats_95.append(float(d['{}95Per_SexEqual_{}'.format(exp, measure)][0]))
            bal_stats_95.append(float(d['{}95Per_Balanced_{}'.format(exp, measure)][0]))
            re_stats_95.append(float(d['{}95Per_RegretEqual_{}'.format(exp, measure)][0]))
            msr_stats_95.append(float(d['{}95Per_MinSumRegret_{}'.format(exp, measure)][0]))
            redi_stats_95.append(float(d['{}95Per_reAlg_{}'.format(exp, measure)][0]))
            resp_stats_95.append(float(d['{}95Per_reAlgAlt_{}'.format(exp, measure)][0]))

        # create optimal measure plts with average
        createPlot(str(measure) + '_av', "$n$", label, n, \
            bal_stats_av, bal_stats_5, bal_stats_95, "Balanced", \
            se_stats_av, se_stats_5, se_stats_95, "Sex-equal", \
            egal_stats_av, egal_stats_5, egal_stats_95, "Egalitarian", \
            minreg_stats_av, minreg_stats_5, minreg_stats_95, "Minimum regret", \
            re_stats_av, re_stats_5, re_stats_95, "Regret-equal", \
            msr_stats_av, msr_stats_5, msr_stats_95, "Min-regret sum", \
            redi_stats_av, redi_stats_5, redi_stats_95, "Algorithm REDI", \
            resp_stats_av, resp_stats_5, resp_stats_95, "Algorithm RESP")

        createPlotLogLog(str(measure), "$n$", label, n, \
            bal_stats_av, bal_stats_5, bal_stats_95, "Balanced", \
            se_stats_av, se_stats_5, se_stats_95, "Sex-equal", \
            egal_stats_av, egal_stats_5, egal_stats_95, "Egalitarian", \
            minreg_stats_av, minreg_stats_5, minreg_stats_95, "Minimum regret", \
            re_stats_av, re_stats_5, re_stats_95, "Regret-equal", \
            msr_stats_av, msr_stats_5, msr_stats_95, "Min-regret sum",
            redi_stats_av, redi_stats_5, redi_stats_95, "Algorithm REDI",
            resp_stats_av, resp_stats_5, resp_stats_95, "Algorithm RESP")

        # create optimal measure plts with median
        createPlot(str(measure) + '_median', "$n$", label, n, \
            bal_stats_median, bal_stats_5, bal_stats_95, "Balanced", \
            se_stats_median, se_stats_5, se_stats_95, "Sex-equal", \
            egal_stats_median, egal_stats_5, egal_stats_95, "Egalitarian", \
            minreg_stats_median, minreg_stats_5, minreg_stats_95, "Minimum regret", \
            re_stats_median, re_stats_5, re_stats_95, "Regret-equal", \
            msr_stats_median, msr_stats_5, msr_stats_95, "Min-regret sum", \
            redi_stats_median, redi_stats_5, redi_stats_95, "Algorithm REDI", \
            resp_stats_median, resp_stats_5, resp_stats_95, "Algorithm RESP")

        createPlotLogLog(str(measure), "$n$", label, n, \
            bal_stats_median, bal_stats_5, bal_stats_95, "Balanced", \
            se_stats_median, se_stats_5, se_stats_95, "Sex-equal", \
            egal_stats_median, egal_stats_5, egal_stats_95, "Egalitarian", \
            minreg_stats_median, minreg_stats_5, minreg_stats_95, "Minimum regret", \
            re_stats_median, re_stats_5, re_stats_95, "Regret-equal", \
            msr_stats_median, msr_stats_5, msr_stats_95, "Min-regret sum",
            redi_stats_median, redi_stats_5, redi_stats_95, "Algorithm REDI",
            resp_stats_median, resp_stats_5, resp_stats_95, "Algorithm RESP")

    ###############
    # duration plot
    ###############
    re_alg_duration_median, re_alg_duration_5, re_alg_duration_95 = [], [], []
    re_algAlt_duration_median, re_algAlt_duration_5, re_algAlt_duration_95 = [], [], []
    re_enum_duration_median, re_enum_duration_5, re_enum_duration_95 = [], [], []

    # collect data
    for exp in exp_names:
        re_alg_duration_median.append(float(d['{}median_duration_REalg_Total'.format(exp)][0]))
        re_alg_duration_5.append(float(d['{}5Per_duration_REalg_Total'.format(exp)][0]))
        re_alg_duration_95.append(float(d['{}95Per_duration_REalg_Total'.format(exp)][0]))
        re_algAlt_duration_median.append(float(d['{}median_duration_REalgAlt_Total'.format(exp)][0]))
        re_algAlt_duration_5.append(float(d['{}5Per_duration_REalgAlt_Total'.format(exp)][0]))
        re_algAlt_duration_95.append(float(d['{}95Per_duration_REalgAlt_Total'.format(exp)][0]))
        re_enum_duration_median.append(float(d['{}median_duration_Enum_Total'.format(exp)][0]))
        re_enum_duration_5.append(float(d['{}5Per_duration_Enum_Total'.format(exp)][0]))
        re_enum_duration_95.append(float(d['{}95Per_duration_Enum_Total'.format(exp)][0]))
    
    # create duration plot 
    createDurationPlot(n, "n", "Duration (ms)", \
        re_alg_duration_median, re_alg_duration_5, re_alg_duration_95, "Algorithm REDI", \
        re_algAlt_duration_median, re_algAlt_duration_5, re_algAlt_duration_95, "Algorithm RESP", \
        re_enum_duration_median, re_enum_duration_5, re_enum_duration_95, "Algorithm ENUM")
   

# 1D curve function
def func1D(x, a, b):
    return a + b*x


# 2D curve function
def func2D(x, a, b, c):
    y = a + b*x + c*x*x
    return y


# creates the duration plot for the three different ways of getting the regret-equal score
def createDurationPlot(xlist, xlabel, ylabel, \
        re_alg_duration_median, re_alg_duration_5, re_alg_duration_95, alg_label, \
        re_algAlt_duration_median, re_algAlt_duration_5, re_algAlt_duration_95, algAlt_label, \
        re_enum_duration_median, re_enum_duration_5, re_enum_duration_95, enum_label):
    
    xlist_alg, xlist_algAlt, xlist_enum = [], [], []
    new_re_alg_duration_median, new_re_alg_duration_5, new_re_alg_duration_95 = [], [], []
    new_re_algAlt_duration_median, new_re_algAlt_duration_5, new_re_algAlt_duration_95 = [], [], []
    new_re_enum_duration_median, new_re_enum_duration_5, new_re_enum_duration_95 = [], [], []

    # if paper:
    #     matplotlib.rcParams.update({'font.size': 14})

    # collecting the data separately in case we have all timeouts on one particular algorithm
    for i,n in enumerate(xlist):
        if not re_alg_duration_median[i] == -1:
            xlist_alg.append(n)
            new_re_alg_duration_median.append(re_alg_duration_median[i])
            new_re_alg_duration_5.append(re_alg_duration_5[i])
            new_re_alg_duration_95.append(re_alg_duration_95[i])
        if not re_algAlt_duration_median[i] == -1:
            xlist_algAlt.append(n)
            new_re_algAlt_duration_median.append(re_algAlt_duration_median[i])
            new_re_algAlt_duration_5.append(re_algAlt_duration_5[i])
            new_re_algAlt_duration_95.append(re_algAlt_duration_95[i])
        if not re_enum_duration_median[i] == -1:
            xlist_enum.append(n)
            new_re_enum_duration_median.append(re_enum_duration_median[i])
            new_re_enum_duration_5.append(re_enum_duration_5[i])
            new_re_enum_duration_95.append(re_enum_duration_95[i])
    
    re_alg_duration_median = new_re_alg_duration_median
    re_algAlt_duration_median = new_re_algAlt_duration_median
    re_enum_duration_median = new_re_enum_duration_median
    re_alg_duration_5 = new_re_alg_duration_5
    re_algAlt_duration_5 = new_re_algAlt_duration_5
    re_enum_duration_5 = new_re_enum_duration_5
    re_alg_duration_95 = new_re_alg_duration_95
    re_algAlt_duration_95 = new_re_algAlt_duration_95
    re_enum_duration_95 = new_re_enum_duration_95

    # starting the plotting
    plt.figure()
    plt.figure(facecolor='w', edgecolor='k', figsize=(7, 5))
    plt.xlabel(xlabel)
    plt.ylabel(ylabel)

    xlist_alg = np.array(xlist_alg)
    xlist_algAlt = np.array(xlist_algAlt)
    xlist_enum = np.array(xlist_enum)

    # curves
    algCV,_ = curve_fit(func2D, np.log(xlist_alg), np.log(re_alg_duration_median))
    algAltCV,_ = curve_fit(func2D, np.log(xlist_algAlt), np.log(re_algAlt_duration_median))
    enumCV,_ = curve_fit(func2D, np.log(xlist_enum), np.log(re_enum_duration_median))
    alg_5_CV,_ = curve_fit(func2D, np.log(xlist_alg), np.log(re_alg_duration_5))
    algAlt_5_CV,_ = curve_fit(func2D, np.log(xlist_algAlt), np.log(re_algAlt_duration_5))
    enum_5_CV,_ = curve_fit(func2D, np.log(xlist_enum), np.log(re_enum_duration_5))
    alg_95_CV,_ = curve_fit(func2D, np.log(xlist_alg), np.log(re_alg_duration_95))
    algAlt_95_CV,_ = curve_fit(func2D, np.log(xlist_algAlt), np.log(re_algAlt_duration_95))
    enum_95_CV,_ = curve_fit(func2D, np.log(xlist_enum), np.log(re_enum_duration_95))



    
    newx_alg = np.logspace(1, 3, 250)
    newx_algAlt = np.logspace(1, 3, 250)
    newx_algenum = np.logspace(1, 3, 250)

    if paper:
        # plot the best fit curves
        plt.plot(newx_alg, np.exp(func2D(np.log(newx_alg), algCV[0], algCV[1], algCV[2])), '-', color=blues[1])
        plt.plot(newx_algenum, np.exp(func2D(np.log(newx_algenum), enumCV[0], enumCV[1], enumCV[2])), '-', color=blues[3])

        # plot the points
        plt.plot(xlist_alg, re_alg_duration_median, 'o', markersize=4, label=alg_label, color=blues[1])
        plt.plot(xlist_enum, re_enum_duration_median, 's', markersize=4, label=enum_label, color=blues[3])
        
        # plot the confidence intervals
        plt.fill_between(newx_alg, np.exp(func2D(np.log(newx_alg), alg_5_CV[0], alg_5_CV[1], alg_5_CV[2])), \
                                np.exp(func2D(np.log(newx_alg), alg_95_CV[0], alg_95_CV[1], alg_95_CV[2])), color=blues[0], alpha=0.8)
        plt.fill_between(newx_algenum, np.exp(func2D(np.log(newx_algenum), enum_5_CV[0], enum_5_CV[1], enum_5_CV[2])), \
                                np.exp(func2D(np.log(newx_algenum), enum_95_CV[0], enum_95_CV[1], enum_95_CV[2])), color=blues[2], alpha=0.8)
    

    else:
        # plot the best fit curves
        plt.plot(newx_alg, np.exp(func2D(np.log(newx_alg), algCV[0], algCV[1], algCV[2])), '-', color='skyblue')
        plt.plot(newx_algAlt, np.exp(func2D(np.log(newx_algAlt), algAltCV[0], algAltCV[1], algAltCV[2])), '-', color='orangered')
        plt.plot(newx_algenum, np.exp(func2D(np.log(newx_algenum), enumCV[0], enumCV[1], enumCV[2])), '-', color='seagreen')

        # plot the points
        plt.plot(xlist_alg, re_alg_duration_median, 'o', markersize=4, label=alg_label, color='skyblue')
        plt.plot(xlist_algAlt, re_algAlt_duration_median, '*', markersize=4, label=algAlt_label, color='orangered')
        plt.plot(xlist_enum, re_enum_duration_median, 's', markersize=4, label=enum_label, color='seagreen')

        plt.plot([0, 1000], [timeout_ms, timeout_ms], "--", label="Timeout", color='red')

        # plot the confidence intervals
        plt.fill_between(newx_alg, np.exp(func2D(np.log(newx_alg), alg_5_CV[0], alg_5_CV[1], alg_5_CV[2])), \
                                np.exp(func2D(np.log(newx_alg), alg_95_CV[0], alg_95_CV[1], alg_95_CV[2])), color='skyblue', alpha=.5)
        plt.fill_between(newx_algAlt, np.exp(func2D(np.log(newx_algAlt), algAlt_5_CV[0], algAlt_5_CV[1], algAlt_5_CV[2])), \
                                    np.exp(func2D(np.log(newx_algAlt), algAlt_95_CV[0], algAlt_95_CV[1], algAlt_95_CV[2])), color='orangered', alpha=.5)
        plt.fill_between(newx_algenum, np.exp(func2D(np.log(newx_algenum), enum_5_CV[0], enum_5_CV[1], enum_5_CV[2])), \
                                np.exp(func2D(np.log(newx_algenum), enum_95_CV[0], enum_95_CV[1], enum_95_CV[2])), color='seagreen', alpha=.5)

    # plt.plot([100, 1000], [100, 1000000], "--", label="Gradient 4n")

    
    plt.xlim(xmin=0, xmax=1000)
    plt.ylim(ymin=100)
    plt.yscale('log')
    plt.grid()

    plt.legend(loc='upper left')
    plt.tight_layout()
    filename = dir_name + "/duration.pdf"
    if paper:
        filename = dir_name + "/paper_duration.pdf"
    plt.savefig(filename)
    plt.close()
    plt.rcParams.update(plt.rcParamsDefault)


# creates a plot for a particular measure over the 6 different optimal matchings
# listed
def createPlot(name, xlabel, ylabel, xlist, \
        list1, list1_5, list1_95, label1, \
        list2, list2_5, list2_95, label2, \
        list3, list3_5, list3_95, label3, \
        list4, list4_5, list4_95, label4, \
        list5, list5_5, list5_95, label5, \
        list6, list6_5, list6_95, label6, \
        list7, list7_5, list7_95, label7, \
        list8, list8_5, list8_95, label8):

    # collecting the data separately in case there are all timeouts for one
    # particular algorithm for one case
    xlist_list7, new_list7, new_list7_5, new_list7_95 = [], [], [], []
    xlist_list8, new_list8, new_list8_5, new_list8_95 = [], [], [], []
    for i,n in enumerate(xlist):
        if not list7[i] == -1:
            xlist_list7.append(n)
            new_list7.append(list7[i])
            new_list7_5.append(list7_5[i])
            new_list7_95.append(list7_95[i])
        if not list8[i] == -1:
            xlist_list8.append(n)
            new_list8.append(list8[i])
            new_list8_5.append(list8_5[i])
            new_list8_95.append(list8_95[i])

    list7 = new_list7
    list7_5 = new_list7_5
    list7_95 = new_list7_95
    list8 = new_list8
    list8_5 = new_list8_5
    list8_95 = new_list8_95
    

    matplotlib.rcParams.update({'font.size': 14})
    plt.figure(facecolor='w', edgecolor='k', figsize=(7, 5))
    plt.xlabel(xlabel)
    plt.ylabel(ylabel)
    xlist = np.array(xlist)

    # curve for median best fit
    score1CV,_ = curve_fit(func2D, xlist, list1)
    score2CV,_ = curve_fit(func2D, xlist, list2)
    score3CV,_ = curve_fit(func2D, xlist, list3)
    score4CV,_ = curve_fit(func2D, xlist, list4)
    score5CV,_ = curve_fit(func2D, xlist, list5)
    score6CV,_ = curve_fit(func2D, xlist, list6)
    score7CV,_ = curve_fit(func2D, xlist_list7, list7)
    score8CV,_ = curve_fit(func2D, xlist_list8, list8)

    # curve for 5% CI best fit
    score1_5_CV,_ = curve_fit(func2D, xlist, list1_5)
    score2_5_CV,_ = curve_fit(func2D, xlist, list2_5)
    score3_5_CV,_ = curve_fit(func2D, xlist, list3_5)
    score4_5_CV,_ = curve_fit(func2D, xlist, list4_5)
    score5_5_CV,_ = curve_fit(func2D, xlist, list5_5)
    score6_5_CV,_ = curve_fit(func2D, xlist, list6_5)
    score7_5_CV,_ = curve_fit(func2D, xlist_list7, list7_5)
    score8_5_CV,_ = curve_fit(func2D, xlist_list8, list8_5)

    # curve for 95% CI best fit
    score1_95_CV,_ = curve_fit(func2D, xlist, list1_95)
    score2_95_CV,_ = curve_fit(func2D, xlist, list2_95)
    score3_95_CV,_ = curve_fit(func2D, xlist, list3_95)
    score4_95_CV,_ = curve_fit(func2D, xlist, list4_95)
    score5_95_CV,_ = curve_fit(func2D, xlist, list5_95)
    score6_95_CV,_ = curve_fit(func2D, xlist, list6_95)
    score7_95_CV,_ = curve_fit(func2D, xlist_list7, list7_95)
    score8_95_CV,_ = curve_fit(func2D, xlist_list8, list8_95)

    # confidence interval
    # plt.fill_between(xlist, func2D(xlist, score2_5_CV[0], score2_5_CV[1], score2_5_CV[2]), \
    #                         func2D(xlist, score2_95_CV[0], score2_95_CV[1], score2_95_CV[2]), color='blue', alpha=.5)
    # plt.fill_between(xlist, func2D(xlist, score3_5_CV[0], score3_5_CV[1], score3_5_CV[2]), \
    #                         func2D(xlist, score3_95_CV[0], score3_95_CV[1], score3_95_CV[2]), color='seagreen', alpha=.5)
    # plt.fill_between(xlist, func2D(xlist, score1_5_CV[0], score1_5_CV[1], score1_5_CV[2]), \
    #                         func2D(xlist, score1_95_CV[0], score1_95_CV[1], score1_95_CV[2]), color='orangered', alpha=.5)
    # plt.fill_between(xlist, func2D(xlist, score4_5_CV[0], score4_5_CV[1], score4_5_CV[2]), \
    #                         func2D(xlist, score4_95_CV[0], score4_95_CV[1], score4_95_CV[2]), color='skyblue', alpha=.5)
    # plt.fill_between(xlist, func2D(xlist, score5_5_CV[0], score5_5_CV[1], score5_5_CV[2]), \
    #                         func2D(xlist, score5_95_CV[0], score5_95_CV[1], score5_95_CV[2]), color='gold', alpha=.5)
    # plt.fill_between(xlist, func2D(xlist, score6_5_CV[0], score6_5_CV[1], score6_5_CV[2]), \
    #                         func2D(xlist, score6_95_CV[0], score6_95_CV[1], score6_95_CV[2]), color='purple', alpha=.5)
    
    newx = np.linspace(10, 1000, 250)
    # best fit lines
    plt.plot(newx, func2D(np.array(newx), score3CV[0], score3CV[1], score3CV[2]), '-', color='darkgreen')
    plt.plot(newx, func2D(np.array(newx), score6CV[0], score6CV[1], score6CV[2]), '-', color='navy')
    plt.plot(newx, func2D(np.array(newx), score2CV[0], score2CV[1], score2CV[2]), '-', color='mediumseagreen')
    plt.plot(newx, func2D(np.array(newx), score5CV[0], score5CV[1], score5CV[2]), '-', color='royalblue')
    plt.plot(newx, func2D(np.array(newx), score1CV[0], score1CV[1], score1CV[2]), '-', color='lightgreen')
    plt.plot(newx, func2D(np.array(newx), score4CV[0], score4CV[1], score4CV[2]), '-', color='skyblue')    
    plt.plot(newx, func2D(np.array(newx), score7CV[0], score7CV[1], score7CV[2]), '-', color='orange')  
    if not paper:
        plt.plot(newx, func2D(np.array(newx), score8CV[0], score8CV[1], score8CV[2]), '-', color='red')    
    

    # points
    plt.plot(xlist, list3, "s", markersize=4, label=label3, color='darkgreen')
    plt.plot(xlist, list6, "d", markersize=4, label=label6, color='navy')
    plt.plot(xlist, list2, 'o', markersize=4, label=label2, color='mediumseagreen')
    plt.plot(xlist, list5, '^', markersize=4, label=label5, color='royalblue')
    plt.plot(xlist, list1, '+', markersize=4, label=label1, color='lightgreen')
    plt.plot(xlist, list4, '*', markersize=4, label=label4, color='skyblue')
    plt.plot(xlist_list7, list7, '3', markersize=4, label=label7, color='orange')
    if not paper:
        plt.plot(xlist_list8, list8, '4', markersize=4, label=label8, color='red')
    
    
    

    handles, labels = plt.gca().get_legend_handles_labels()
    order = [4,2,0,5,3,1,6]
    if not paper:
        order = [4,2,0,5,3,1,6,7]
    plt.xlim(xmin=0, xmax=1000)
    plt.ylim(ymin=0)
    plt.grid()


    plt.legend([handles[idx] for idx in order],[labels[idx] for idx in order])
    # plt.legend()
    plt.tight_layout()
    filename = dir_name + "/" + name + ".pdf"
    if paper:
        filename = dir_name + "/paper_" + name + ".pdf"
    plt.savefig(filename)
    plt.close()
    plt.rcParams.update(plt.rcParamsDefault)


# creates a plot for a particular measure over the 6 different optimal matchings
# listed
def createPlotLogLog(name, xlabel, ylabel, xlist, \
        list1, list1_5, list1_95, label1, \
        list2, list2_5, list2_95, label2, \
        list3, list3_5, list3_95, label3, \
        list4, list4_5, list4_95, label4, \
        list5, list5_5, list5_95, label5, \
        list6, list6_5, list6_95, label6, \
        list7, list7_5, list7_95, label7, \
        list8, list8_5, list8_95, label8):

    # collecting the data separately in case there are all timeouts for one
    # particular algorithm for one case
    xlist_list7, new_list7, new_list7_5, new_list7_95 = [], [], [], []
    xlist_list8, new_list8, new_list8_5, new_list8_95 = [], [], [], []
    for i,n in enumerate(xlist):
        if not list7[i] == -1:
            xlist_list7.append(n)
            new_list7.append(list7[i])
            new_list7_5.append(list7_5[i])
            new_list7_95.append(list7_95[i])
        if not list8[i] == -1:
            xlist_list8.append(n)
            new_list8.append(list8[i])
            new_list8_5.append(list8_5[i])
            new_list8_95.append(list8_95[i])

    list7 = new_list7
    list7_5 = new_list7_5
    list7_95 = new_list7_95
    list8 = new_list8
    list8_5 = new_list8_5
    list8_95 = new_list8_95

    plt.figure(facecolor='w', edgecolor='k', figsize=(7, 5))
    plt.xlabel(xlabel)
    plt.ylabel(ylabel)
    xlist = np.array(xlist)

    # correcting 0's for curve best fit
    for myList in list1, list2, list3, list4, list5, list6, list7, list8:
        for i, elem in enumerate(myList):
            if elem == 0.0:
                myList[i] = 1

    # curve for median best fit
    score1CV,_ = curve_fit(func2D, np.log(xlist), np.log(list1))
    score2CV,_ = curve_fit(func2D, np.log(xlist), np.log(list2))
    score3CV,_ = curve_fit(func2D, np.log(xlist), np.log(list3))
    score4CV,_ = curve_fit(func2D, np.log(xlist), np.log(list4))
    score5CV,_ = curve_fit(func2D, np.log(xlist), np.log(list5))
    score6CV,_ = curve_fit(func2D, np.log(xlist), np.log(list6))
    score7CV,_ = curve_fit(func2D, np.log(xlist_list7), np.log(list7))
    score8CV,_ = curve_fit(func2D, np.log(xlist_list8), np.log(list8))


    newx = np.logspace(1, 3, 250)
    # best fit lines
    plt.plot(newx, np.exp(func2D(np.log(newx), score1CV[0], score1CV[1], score1CV[2])), '-', color='orangered')
    plt.plot(newx, np.exp(func2D(np.log(newx), score2CV[0], score2CV[1], score2CV[2])), '-', color='blue')
    plt.plot(newx, np.exp(func2D(np.log(newx), score3CV[0], score3CV[1], score3CV[2])), '-', color='seagreen')
    plt.plot(newx, np.exp(func2D(np.log(newx), score4CV[0], score4CV[1], score4CV[2])), '-', color='skyblue')
    plt.plot(newx, np.exp(func2D(np.log(newx), score5CV[0], score5CV[1], score5CV[2])), '-', color='gold')
    plt.plot(newx, np.exp(func2D(np.log(newx), score6CV[0], score6CV[1], score6CV[2])), '-', color='purple')
    plt.plot(newx, np.exp(func2D(np.log(newx), score7CV[0], score7CV[1], score7CV[2])), '-', color='orange')
    if not paper:
        plt.plot(newx, np.exp(func2D(np.log(newx), score8CV[0], score8CV[1], score8CV[2])), '-', color='red')

    # points
    plt.plot(xlist, list1, 'o', markersize=4, label=label1, color='orangered')
    plt.plot(xlist, list2, '*', markersize=4, label=label2, color='blue')
    plt.plot(xlist, list3, 's', markersize=4, label=label3, color='seagreen')
    plt.plot(xlist, list4, '^', markersize=4, label=label4, color='skyblue')
    plt.plot(xlist, list5, "+", markersize=4, label=label5, color='gold')
    plt.plot(xlist, list6, "d", markersize=4, label=label6, color='purple')
    plt.plot(xlist, list6, "3", markersize=4, label=label7, color='orange')
    if not paper:
        plt.plot(xlist, list6, "4", markersize=4, label=label8, color='red')

    plt.legend()
    plt.xscale('log')
    plt.yscale('log')
    filename = dir_name + "/loglog_" + name + ".pdf"
    if paper:
        filename = dir_name + "/paper_loglog_" + name + ".pdf"
    plt.savefig(filename)
    plt.close()


def createHistograms():

    # mean num optimal matchings per optimal matching type
    fig, ax = plt.subplots()
    ind = np.arange(6)
    width = 0.2
    exps = ['S100', 'S400', 'S700', 'S1000']
    exp_bars = []

    for i, exp in enumerate(exps):
        numMinRegret = float(d[exp+'Av_MinRegret_num'][0])
        numEgalitarian = float(d[exp+'Av_Egalitarian_num'][0])
        numBalanced = float(d[exp+'Av_Balanced_num'][0])
        numSeqEqual = float(d[exp+'Av_SexEqual_num'][0])
        numRegretEqual = float(d[exp+'Av_RegretEqual_num'][0])
        numMinSumRegret = float(d[exp+'Av_MinSumRegret_num'][0])

        av_counts = [numBalanced, numSeqEqual, numEgalitarian, numMinRegret, numRegretEqual, numMinSumRegret]
        exp_bars.append(ax.bar(ind + i*width, av_counts, width, color=blues[i])) 

    objects = ['Balanced', 'Sex-equal', 'Egalitarian', 'Minimum\nregret', 'Regret-\nequal', 'Min-regret\nsum']
    y_pos = np.arange(len(objects))
    plt.grid()
    plt.xticks(y_pos + 0.3, objects)
    plt.ylabel('Mean number of stable matchings')
    plt.xlabel('Type of optimal stable matching')
    plt.ylim(ymin=0.7)
    plt.ylim(ymax=100)
    plt.yscale('log')
    ax.legend(exp_bars, exps)
    plt.tight_layout()

    plt.savefig(dir_name + "/paper_bar_optimal_nums.pdf")
    plt.close()

    # average number of matchings which exhibit different numbers of optimal
    # matchings 
    fig, ax = plt.subplots()
    ind = np.arange(7)
    width = 0.2
    exps = ['S100', 'S400', 'S700', 'S1000']
    exp_bars = []
    
    for i, exp in enumerate(exps):
        num0 = float(d[exp+'Av_0_matchingCount'][0])
        num1 = float(d[exp+'Av_1_matchingCount'][0])
        num2 = float(d[exp+'Av_2_matchingCount'][0])
        num3 = float(d[exp+'Av_3_matchingCount'][0])
        num4 = float(d[exp+'Av_4_matchingCount'][0])
        num5 = float(d[exp+'Av_5_matchingCount'][0])
        num6 = float(d[exp+'Av_6_matchingCount'][0])

        av_counts = [num0, num1, num2, num3, num4, num5, num6]
        exp_bars.append(ax.bar(ind + i*width, av_counts, width, color=blues[i])) 

    objects = ['0', '1', '2', '3', '4', '5', '6']
    y_pos = np.arange(len(objects))
    plt.grid()
    plt.xticks(y_pos + 0.3, objects)
    plt.ylabel('Mean number of stable matchings')
    plt.xlabel('Number of optimal matching criteria satisfied')
    plt.ylim(ymin=0.07)
    plt.ylim(ymax=1300)
    plt.yscale('log')
    ax.legend(exp_bars, exps)
    plt.tight_layout()

    plt.savefig(dir_name + "/paper_bar_num_matchings_num_opt.pdf")
    plt.close()


def createTables():

    # instance info
    if paper:
        latexpaper = dir_name + "/paper_latex_table_instance_info.tex"
        latexPaperFile = open(latexpaper, 'w')
        latexPaperFile.write('\\begin{table}[] \centerline{')
        latexPaperFile.write('\\begin{tabular}{ R{1.2cm} | R{1.6cm} R{1.6cm} R{1.6cm} }') 
        latexPaperFile.write('\hline\hline ')
        latexPaperFile.write('Case & $n$ & $|\mathcal{M}|_{av}$ & $|R|_{av}$ \\\\ \n')
        latexPaperFile.write('\hline ')

        for exp in exp_names:
            latexPaperFile.write('{} & ${}$ & ${}$ \\\\ \n '.format(\
                exp, d[exp+'numMenOrWomen'][0],
                d[exp+'Av_numStableMatchings'][0], d[exp+'Av_numRotations'][0]))

        latexPaperFile.write(
            '\hline\hline \end{tabular}} \caption{General instance information.} \label{sm_re_table_instinfo} \end{table} ')
        latexPaperFile.close 

    else:    
        latexpaper = dir_name + "/latex_table_instance_info.tex"
        latexPaperFile = open(latexpaper, 'w')
        latexPaperFile.write('\\begin{table}[] \centerline{')
        latexPaperFile.write('\\begin{tabular}{ R{1.2cm} | R{1.6cm} R{1.6cm} R{1.6cm} R{1.6cm} R{1.6cm} R{1.6cm} }') 
        latexPaperFile.write('\hline\hline ')
        latexPaperFile.write('Case & $n$ & $|\mathcal{M}|_{av}$ & $|R|_{av}$ & Timeout REDI & Timeout RESP & Timeout ENUM \\\\ \n')
        latexPaperFile.write('\hline ')

        for exp in exp_names:
            latexPaperFile.write('{} & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ \\\\ \n '.format(\
                exp, d[exp+'numMenOrWomen'][0],
                d[exp+'Av_numStableMatchings'][0], d[exp+'Av_numRotations'][0], 
                d[exp+'num_re_timeout'][0], d[exp+'num_rea_timeout'][0], 
                d[exp+'num_ree_timeout'][0]))

        latexPaperFile.write(
            '\hline\hline \end{tabular}} \caption{General instance and algorithm timeout information.} \label{sm_re_table_instinfo} \end{table} ')
        latexPaperFile.close 


    # duration
    if paper:
        latexpaper = dir_name + "/paper_latex_table_duration.tex"
        latexPaperFile = open(latexpaper, 'w')
        latexPaperFile.write('\\begin{table}[] \centerline{')
        latexPaperFile.write('\\begin{tabular}{ R{1.2cm} | R{1.4cm} R{1.4cm} R{1.4cm} R{1.4cm} | R{1.4cm} R{1.6cm} R{1.4cm} R{1.4cm} }') 
        latexPaperFile.write('\hline\hline ')
        latexPaperFile.write('Case & REDI$_{av}$ & REDI$_{med}$ & REDI$_{5}$ & REDI$_{95}$ & ENUM$_{av}$ & ENUM$_{med}$ & ENUM$_{5}$ & ENUM$_{95}$ \\\\ \n')
        latexPaperFile.write('\hline ')

        for exp in exp_names:
            latexPaperFile.write('{} & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ \\\\ \n '.format(\
                exp, d[exp+'Av_duration_REalg_Total'][0], d[exp+'median_duration_REalg_Total'][0], 
                d[exp+'5Per_duration_REalg_Total'][0], d[exp+'95Per_duration_REalg_Total'][0],  
                d[exp+'Av_duration_Enum_Total'][0], d[exp+'median_duration_Enum_Total'][0], 
                d[exp+'5Per_duration_Enum_Total'][0], d[exp+'95Per_duration_Enum_Total'][0]))

        latexPaperFile.write(
            '\hline\hline \end{tabular}} \caption{A comparison of time taken to execute Algorithm REDI \
             and Algorithm ENUM. Here REDI$_{av}$, REDI$_{med}$, REDI$_{5}$ and REDI$_{95}$ represent the mean, \
             median, $5$th percentile and $95$th percentile of Algorithm REDI for a given instance type. \
             Similar notation is used for Algorithm ENUM. Times are in ms.} \label{sm_re_table_duration} \end{table} ')
        latexPaperFile.close 

    else:
        latexpaper = dir_name + "/latex_table_duration.tex"
        latexPaperFile = open(latexpaper, 'w')
        latexPaperFile.write('\\begin{table}[] \centerline{')
        latexPaperFile.write('\\begin{tabular}{ R{1.2cm} | R{1.4cm} R{1.4cm} R{1.4cm} R{1.4cm} | R{1.8cm} R{1.8cm} R{1.8cm} R{1.8cm} | R{1.4cm} R{1.4cm} R{1.4cm} R{1.4cm} }') 
        latexPaperFile.write('\hline\hline ')
        latexPaperFile.write('Case & $DI_{av}$ & $DI_{med}$ & $DI_{5}$ & $DI_{95}$ & $SP_{av}$ & $SP_{med}$ & $SP_{5}$ & $SP_{95}$ & $EN_{av}$ & $EN_{med}$ & $EN_{5}$ & $EN_{95}$ \\\\ \n')
        latexPaperFile.write('\hline ')

        for exp in exp_names:
            latexPaperFile.write('{} & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ \\\\ \n '.format(\
                exp, d[exp+'Av_duration_REalg_Total'][0], d[exp+'median_duration_REalg_Total'][0], 
                d[exp+'5Per_duration_REalg_Total'][0], d[exp+'95Per_duration_REalg_Total'][0], 
                d[exp+'Av_duration_REalgAlt_Total'][0], d[exp+'median_duration_REalgAlt_Total'][0], 
                d[exp+'5Per_duration_REalgAlt_Total'][0], d[exp+'95Per_duration_REalgAlt_Total'][0], 
                d[exp+'Av_duration_Enum_Total'][0], d[exp+'median_duration_Enum_Total'][0], 
                d[exp+'5Per_duration_Enum_Total'][0], d[exp+'95Per_duration_Enum_Total'][0]))

        latexPaperFile.write(
            '\hline\hline \end{tabular}} \caption{A comparison of time taken to execute Algorithm REDI \
             , Algorithm RESP and Algorithm ENUM. We \
             abbreviate the algorithms as follows: Algorithm REDI ($DI$), Algorithm RESP ($SP$) and \
             Algorithm ENUM ($EN$). Here $DI_{av}$, $DI_{med}$, $DI_{5}$ and $DI_{95}$ represent the mean, \
             median, $5$th percentile and $95$th percentile of Algorithm REDI for a given instance type. \
             Similar notation is used for Algorithms RESP and ENUM. Times are in ms.} \label{sm_re_table_duration} \end{table} ')
        latexPaperFile.close 

    # measures
    if paper:
        table_measures = ['median', 'Av']
        table_measure_captions = ['Median', 'Mean']
        overall_measures = [
            'degree', 
            'egalCost', 
            'sexEqualCost', 
            'regretEqualScore', 
            'balancedScore', 
            'sumRegret']
        captions = [
            ' degree for six different optimal stable matchings and output from Algorithm REDI.', 
            ' cost for six different optimal stable matchings and output from Algorithm REDI.', 
            ' sex-equal score for six different optimal stable matchings and output from Algorithm REDI.', 
            ' regret-equality score for six different optimal stable matchings and output from Algorithm REDI.', 
            ' balanced score for six different optimal stable matchings and output from Algorithm REDI.', 
            ' regret sum for six different optimal stable matchings and output from Algorithm REDI.']
        

        for tm, tc in zip(table_measures, table_measure_captions):
            for om, cap in zip(overall_measures, captions):

                latexpaper = dir_name + "/paper_latex_table_" + om + "_" + tm + ".tex"
                latexPaperFile = open(latexpaper, 'w')
                latexPaperFile.write('\\begin{table}[] \centerline{')
                latexPaperFile.write('\\begin{tabular}{ R{1.2cm} | R{1.8cm} R{1.8cm} R{1.8cm} R{1.8cm} R{1.8cm} R{2cm} R{1.8cm} }') 
                latexPaperFile.write('\hline\hline ')
                latexPaperFile.write('Case & Balanced & Sex-equal & Egalitarian & Minimum regret & Regret-equal & Min-regret sum & Algorithm REDI  \\\\ \n')
                latexPaperFile.write('\hline ')

                for exp in exp_names:
                    latexPaperFile.write('{} & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ \\\\ \n '.format(\
                        exp, 
                        d[exp+tm+'_Balanced_'+om][0], d[exp+tm+'_SexEqual_'+om][0],
                        d[exp+tm+'_Egalitarian_'+om][0], d[exp+tm+'_MinRegret_'+om][0], 
                        d[exp+tm+'_RegretEqual_'+om][0], d[exp+tm+'_MinSumRegret_'+om][0],
                        d[exp+tm+'_reAlg_'+om][0]))

                latexPaperFile.write(
                    '\hline\hline \end{tabular}} \caption{' + tc + cap + '} \label{sm_re_table_' + om + tm + '} \end{table} ')
                latexPaperFile.close 

    else:
        table_measures = ['median', 'Av']
        table_measure_captions = ['Median', 'Mean']
        overall_measures = [
            'degree', 
            'egalCost', 
            'sexEqualCost', 
            'regretEqualScore', 
            'balancedScore', 
            'sumRegret']
        captions = [
            ' degree for six different optimal stable matchings and outputs from Algorithms REDI and RESP.', 
            ' cost for six different optimal stable matchings and outputs from Algorithms REDI and RESP.', 
            ' sex-equal score for six different optimal stable matchings and outputs from Algorithms REDI and RESP.', 
            ' regret-equality score for six different optimal stable matchings and output from Algorithms REDI and RESP.', 
            ' balanced score for six different optimal stable matchings and outputs from Algorithms REDI and RESP.', 
            ' regret sum for six different optimal stable matchings and outputs from Algorithms REDI and RESP.']
        

        for tm, tc in zip(table_measures, table_measure_captions):
            for om, cap in zip(overall_measures, captions):

                latexpaper = dir_name + "/latex_table_" + om + "_" + tm + ".tex"
                latexPaperFile = open(latexpaper, 'w')
                latexPaperFile.write('\\begin{table}[] \centerline{')
                latexPaperFile.write('\\begin{tabular}{ R{1.2cm} | R{1.8cm} R{1.8cm} R{1.8cm} R{1.8cm} R{1.8cm} R{2cm} R{1.8cm} R{1.8cm} }') 
                latexPaperFile.write('\hline\hline ')
                latexPaperFile.write('Case & Balanced & Sex-equal & Egalitarian & Minimum regret & Regret-equal & Min-regret sum & Algorithm REDI & Algorithm RESP  \\\\ \n')
                latexPaperFile.write('\hline ')

                for exp in exp_names:
                    latexPaperFile.write('{} & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ \\\\ \n '.format(\
                        exp, 
                        d[exp+tm+'_Balanced_'+om][0], d[exp+tm+'_SexEqual_'+om][0],
                        d[exp+tm+'_Egalitarian_'+om][0], d[exp+tm+'_MinRegret_'+om][0], 
                        d[exp+tm+'_RegretEqual_'+om][0], d[exp+tm+'_MinSumRegret_'+om][0],
                        d[exp+tm+'_reAlg_'+om][0], d[exp+tm+'_reAlgAlt_'+om][0]))

                latexPaperFile.write(
                    '\hline\hline \end{tabular}} \caption{' + tc + cap + '} \label{sm_re_table_' + om + tm + '} \end{table} ')
                latexPaperFile.close 

    # mean num optimal matchings per optimal matching type
    latexpaper = dir_name + "/paper_latex_table_mean_num_optimal.tex"
    latexPaperFile = open(latexpaper, 'w')
    latexPaperFile.write('\\begin{table}[] \centerline{')
    latexPaperFile.write('\\begin{tabular}{ R{1.2cm} | R{2cm} R{2cm} R{2cm} R{2cm} R{2cm} R{2cm} }') 
    latexPaperFile.write('\hline\hline ')
    latexPaperFile.write('Case & Balanced & Sex-equal & Egalitarian & Minimum regret & Regret-equal & Min-regret sum  \\\\ \n')
    latexPaperFile.write('\hline ')

    for exp in exp_names:
        latexPaperFile.write('{} & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ \\\\ \n '.format(\
            exp, 
            d[exp+'Av_Balanced_num'][0], d[exp+'Av_SexEqual_num'][0], 
            d[exp+'Av_Egalitarian_num'][0], d[exp+'Av_MinRegret_num'][0], 
            d[exp+'Av_RegretEqual_num'][0], d[exp+'Av_MinSumRegret_num'][0]))

    latexPaperFile.write(
        '\hline\hline \end{tabular}} \caption{Mean number of optimal stable matchings per instance.} \label{sm_re_table_mean_num_optimal} \end{table} ')
    latexPaperFile.close 


    # average number of matchings which exhibit different numbers of optimal matchings 
    latexpaper = dir_name + "/paper_latex_table_mean_num_matchings_num_opt.tex"
    latexPaperFile = open(latexpaper, 'w')
    latexPaperFile.write('\\begin{table}[] \centerline{')
    latexPaperFile.write('\\begin{tabular}{ R{1.2cm} | R{1.5cm} R{1.5cm} R{1.5cm} R{1.5cm} R{1.5cm} R{1.5cm} R{1.5cm} }') 
    latexPaperFile.write('\hline\hline ')
    latexPaperFile.write('Case & $0$ & $1$ & $2$ & $3$ & $4$ & $5$ & $6$  \\\\ \n')
    latexPaperFile.write('\hline ')

    for exp in exp_names:
        latexPaperFile.write('{} & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ \\\\ \n '.format(\
            exp, 
            d[exp+'Av_0_matchingCount'][0], d[exp+'Av_1_matchingCount'][0], 
            d[exp+'Av_2_matchingCount'][0], d[exp+'Av_3_matchingCount'][0], 
            d[exp+'Av_4_matchingCount'][0], d[exp+'Av_5_matchingCount'][0],
            d[exp+'Av_6_matchingCount'][0]))

    latexPaperFile.write(
        '\hline\hline \end{tabular}} \caption{Mean number of stable matchings that satisfy $c$ optimality criteria, where $c$ varies on the x-axis.} \label{sm_re_table_mean_num_matchings_num_opt} \end{table} ')
    latexPaperFile.close 



#####################################
# main def
#####################################
if __name__ == '__main__':
    main()

