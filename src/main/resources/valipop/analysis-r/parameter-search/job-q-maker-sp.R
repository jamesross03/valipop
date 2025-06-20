#setwd("working directory")

source("parameter-search/function-code/job-file-maker.R")
source("parameter-search/function-code/utils.R")
source("parameter-search/function-code/promising-candidates-functions.R")

ssCS <- makeConstantsSet(0.01,0.01,"1772-01-01")
clustersResultDir <- "sns-populations"
clustersSummaryResultDir <- "sns-populations"

maniResultDir <- "sns-populations"
maniSummaryResultDir <- "results"

localResultDir <- "results"
localSummaryResultDir <- "results"

runs.ss <- filesToDF("1k-sns-cluster-tk5-neg-df-results-summary.csv", onlyGetStatErrors = FALSE)

summary(runs.ss)
passingRuns <- runs.ss[which(runs.ss$v.M == 0),]

selectOne <- function(select.from, seedSize) {
  selected.ss <- select.from[which(select.from$v.M == 0 & select.from$Seed.Pop.Size == seedSize), ]
  selected.ss.2 <- selected.ss[which(selected.ss$Recovery.Factor == unique(selected.ss$Recovery.Factor)[1]), ]
  selected.ss.3 <- selected.ss.2[which(selected.ss.2$Proportional.Recovery.Factor == unique(selected.ss.2$Proportional.Recovery.Factor)[1]), ]
  
  data <- c()
  
  for(seed in unique(selected.ss.3$Seed)) {
    data <- rbind(data, selected.ss.3[which(selected.ss.2$Seed == seed), ][1,])
    print(seed)
  }
  
  print(length(unique(data$Seed)))
  print(length(data$Seed))
  return(data)
}

selectBest <- function(select.from, seedSize) {
  selected.ss <- select.from[which(select.from$Seed.Pop.Size == seedSize), ]
  selected.ss.1 <- selected.ss[which(selected.ss$v.M == min(selected.ss$v.M)), ]
  selected.ss.2 <- selected.ss.1[which(selected.ss.1$Recovery.Factor == unique(selected.ss.1$Recovery.Factor)[1]), ]
  selected.ss.3 <- selected.ss.2[which(selected.ss.2$Proportional.Recovery.Factor == unique(selected.ss.2$Proportional.Recovery.Factor)[1]), ]
  
  data <- c()
  
  for(seed in unique(selected.ss.3$Seed)) {
    data <- rbind(data, selected.ss.3[which(selected.ss.2$Seed == seed), ][1,])
    print(seed)
  }
  
  print(length(unique(data$Seed)))
  print(length(data$Seed))
  return(data)
}

selected <- selectBest(runs.ss, 170)[1:4,]
selected <- rbind(selected, selectBest(runs.ss, 250)[1:4,])
selected <- rbind(selected, selectBest(runs.ss, 275)[1:4,])
selected <- rbind(selected, selectBest(runs.ss, 325)[1:4,])
selected <- rbind(selected, selectBest(runs.ss, 3000)[1:4,])
selected <- rbind(selected, selectBest(runs.ss, 4000)[1:4,])
selected <- rbind(selected, selectBest(runs.ss, 6000)[1:4,])

jobQ <- repeatJobsDeterministically(passingRuns, ssCS, localResultDir, localSummaryResultDir, recordFormat = "TD", reason = "ss-dr-final", reqMemory = 119)
outputToFile(jobQ, "gen-ss-dr-final.csv")

selected.cluster <- selected[which(selected$Peak.Memory.Usage..MB. < 16000),]
selected.mani <- selected[which(selected$Peak.Memory.Usage..MB. > 16000),]

jobQ <- repeatJobsDeterministically(selected, ssCS, maniResultDir, maniSummaryResultDir, recordFormat = "TD", reason = "ss-scot-final", reqMemory = 119)
outputToFile(jobQ, "gen-ss-scot-final.csv")
outputToFile(jobQ.cluster, "gen-ss-max-records-400k.csv")

jobQ <- repeatJobsDeterministically(selected, ssCS, maniResultDir, maniSummaryResultDir, recordFormat = "TD", reason = "ss-scot-final", reqMemory = 119)
outputToFile(jobQ, "gen-ss-scot-final.csv")

checkPlots(selected)
avgRunTimesBySeed(selected)

passingRuns <- runs.ss[which(runs.ss$v.M == 0), ]
jobQ <- repeatJobsDeterministically(runs.ss, ssCS, clustersResultDir, clustersSummaryResultDir, recordFormat = "TD", reason = "dr-final")
outputToFile(jobQ, "gen-dr-final-with-records.csv")

promisingJobExtraRuns <- runMorePromisingJobs(passingRuns, ssCS, 25, maniResultDir, maniSummaryResultDir, 20, reqMemory = 100)
calc <- calcDeployProfile(promisingJobExtraRuns, runs.ss, 22)

#jobQ <- rbind(jobQ, )

outputToFile(promisingJobExtraRuns, "gen-ss-max-job-q-cluster-extras.csv")


pcs <- promisingCandidates(15625, dfToSummaryDF(runs.fs))[1:10,]

o <- data.frame("x" = 0.5, "y" = 0.5)


for(q in 1:8)
  print(signif(chosenStep*cos(pi*(2*q-1)/16), digits = 2))


temp <- data.frame("q" = seq(1,8,1), 
                   "x" = rep(0,8),
                   "y" = rep(0,8))

temp$y <- signif(chosenStep*cos(pi*(4*temp$q-1)/16), digits = 4)
temp$x <- signif(chosenStep*sin(pi*(4*temp$q-1)/16), digits = 4)

library(ggplot2)
ggplot(temp) +
  geom_text(aes(x,y, label = q))


results <- runs.ss[which(runs.ss$Seed.Pop.Size == 400000), ]
search <- searchNearPromisingJobs(results, ssCS, 5, clustersResultDir, clustersSummaryResultDir, 20, 0.01, 0.125, 4, reqMemory = 12)
calc <- calcDeployProfile(search, results, 22)
outputToFile(search, "gen-ss-max-job-q-cluster.csv")


plot(sort(runs.ss$Peak.Memory.Usage..MB.))

ggplot(search) +
  geom_point(aes(rf, prf)) +
  facet_wrap(~search$`seed size`)

results <- runs.ss
ggplot(results) +
  geom_point(aes(Recovery.Factor, Proportional.Recovery.Factor)) +
  facet_wrap(~results$Seed.Pop.Size)
