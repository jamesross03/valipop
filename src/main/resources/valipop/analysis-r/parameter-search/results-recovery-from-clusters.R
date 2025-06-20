#setwd("")

source("paper/code/FileFunctions.R")
source("parameter-search/function-code/promising-candidates-functions.R")
source("parameter-search/function-code/utils.R")

all.runs <- filesToDF("ss-40k-records-results-summary.csv", onlyGetStatErrors = FALSE)
runs.to.recover <- all.runs[which(all.runs$Code.Version == "1062b2fc152c78f740ceb02f371702745f4c8fc4"),]

promisingCandidatesBySeed(all.runs, 1)

prf <- 0.5
rf <- 1.0
seed.size <- 1000
bestN <- 5

runs.to.recover <- getBestNRuns(all.runs[which(all.runs$Proportional.Recovery.Factor == prf & all.runs$Recovery.Factor == rf),], bestN, seed.size)
runs.to.recover <- runs.to.recover[which(runs.to.recover$v.M == 0),]

outputToFile(paste0("scp -r ", runs.to.recover$Hostname,":", gsub("\\.", "-", gsub(":", "-", runs.to.recover$Results.Directory)), " ."), "dataRecovery.sh")
