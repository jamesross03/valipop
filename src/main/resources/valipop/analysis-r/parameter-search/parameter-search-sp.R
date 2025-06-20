#setwd("")

source("parameter-search/function-code/new-search-plots.R")
source("parameter-search/function-code/promising-candidates-functions.R")
source("paper/code/rfs-discovery/explorationPlots.R")

runs.ss <- filesToDF("sns-cluster-adj-death-tk3-results-summary.csv", onlyGetStatErrors = FALSE)

runs.ss <- filesToDF("sns-cluster-adj-death-tk3-mini-results-summary.csv", onlyGetStatErrors = FALSE)

runs.ss <- filesToDF("ss-6-results-summary.csv", onlyGetStatErrors = FALSE)

runs.ss <- filesToDF("ss-scot-final-results-summary.csv", onlyGetStatErrors = FALSE)

runs.ss <- filesToDF("ss-1k-records-results-summary.csv",
                     "ss-10k-records-results-summary.csv",
                     "ss-40k-records-results-summary.csv",
                     "ss-max-results-summary.csv",
                     "ss-max-cluster-results-summary.csv",
                     "ss-max-results-summary.csv", onlyGetStatErrors = FALSE)
runs.ss <- runs.ss[which(runs.ss$Seed.Pop.Size == 1000 | runs.ss$Seed.Pop.Size == 10000 | runs.ss$Seed.Pop.Size == 40000
                         | runs.ss$Seed.Pop.Size == 100000 | runs.ss$Seed.Pop.Size == 200000 | runs.ss$Seed.Pop.Size == 400000
                         | runs.ss$Seed.Pop.Size == 800000 | runs.ss$Seed.Pop.Size == 1600000 | runs.ss$Seed.Pop.Size == 2400000),]


runs.ss <- filesToDF("ss-max-results-summary.csv", onlyGetStatErrors = FALSE)

checkPlots(runs.ss)

avgRunTimesBySeed(runs.ss)

runs<- getBestNRuns(runs.ss[which(runs.ss$Proportional.Recovery.Factor == 0.0 & runs.ss$Recovery.Factor == 0.5),], 5, 40000)
runs<- getBestNRuns(runs.ss[which(runs.ss$Proportional.Recovery.Factor == 0.50 & runs.ss$Recovery.Factor == 1.00),], 5, 1000)


promisingCandidatesBySeed(runs.ss, 10)
promisingCandidatesPlot(runs.ss, 20)


promisingCandidatesPlot(runs.ss, 10, rf.min = 0.95, rf.max = 1.0, prf.min = 0.975, prf.max = 1.0, seeds = 10000)
promisingCandidatesPlot(runs.ss, 10)

promisingCandidates(15625, dfToSummaryDF(runs.fs))[1:10,]

fs3Dbf(15625, dfToSummaryDF(runs.ss)[which(dfToSummaryDF(runs.ss)$min < 10),], type="scatter3d")
fs3Dbf(31250, dfToSummaryDF(runs.fs))

fs3Dbf(31250, promisingCandidates(31250, dfToSummaryDF(runs.fs))[1:10,], type="scatter3d")
fs3Dbf(15625, promisingCandidatesMean(31250, dfToSummaryDF(runs.fs))[1:10,], type="scatter3d")

temp <- runs.fs[which(runs.fs$Seed.Pop.Size == 250000),]
plot(temp$Peak.Memory.Usage..MB.)

labelPlot2D(15625, dfToSummaryDF(runs.fs))
plot3D(15625, dfToSummaryDF(runs.fs))

summaryDfToFinalDF(dfToSummaryDF(runs.fs))

labelPlot2D(15625, summary, rf.min = 0.3390, rf.max = 0.3396, prf.max = 0.005, detail = TRUE)

plot3D(15625, summary, rf.min = 0.3390, rf.max = 0.3396, prf.max = 0.005)


passingRuns <- runs.ss[which(runs.ss$v.M == 0), ]