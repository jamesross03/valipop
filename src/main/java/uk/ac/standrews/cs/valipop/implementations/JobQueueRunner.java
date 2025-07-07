/*
 * valipop - <https://github.com/stacs-srg/valipop>
 * Copyright © 2025 Systems Research Group, University of St Andrews (graham.kirby@st-andrews.ac.uk)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.valipop.implementations;

import com.google.common.collect.Sets;
import uk.ac.standrews.cs.valipop.Config;
import uk.ac.standrews.cs.valipop.utils.*;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.RecordFormat;

import java.io.IOException;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class JobQueueRunner {

    public static int threadCount = 1;

    private static final double appropriateUsageThreshold = 0.65;
    private static final double memoryIncreaseOnMemoryException = 1.2;

    private static final ArrayList<String> order = new ArrayList<>(Arrays.asList(new String[]{"priority","code version","reason","n","seed size","rf","prf","iw","input dir","results dir","summary results dir","required memory","output record format","deterministic","seed","setup br","setup dr","tS","t0","tE","timestep","binomial sampling","min birth spacing","min ges period","ct tree stepback","oversized geography factor"}));

    public static void main(final String[] args) throws InterruptedException, IOException {

        // from cmd line args:
        // get status path
        final Path statusPath = Paths.get(args[0]);

        // get job q path
        final Path jobQPath = Paths.get(args[1]);

        // get assigned memory
        final int assignedMemory = Integer.parseInt(args[2]);

        final int THREAD_LIMIT = Integer.parseInt(args[3]);

        final double threshold = Double.parseDouble(args[4]);

        while(getStatus(statusPath)) {

            if(nodeIdle(threshold) && !checkPause(statusPath)) {

                try {
                    final DataRow chosenJob = getJob(jobQPath, assignedMemory);

                    if (chosenJob != null) {
                        System.out.println("JOB TAKEN @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " - " + chosenJob.toString(order));

                        try {

                            // runs GC to ensure no object left in memory from previous sims that may skew memory usage logging
                            System.gc();

                            final Config config = convertJobToConfig(chosenJob);

                            
                            final OBDModel model = new OBDModel(config);
                            try {
                                doubleLog(OBDModel.log, "Sim commencing @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " with seed: " + config.getSeed());
                                model.runSimulation();
                                doubleLog(OBDModel.log, "Sim concluded, beginning CT tables generation @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                                model.analyseAndOutputPopulation(false);
                                doubleLog(OBDModel.log, "CT tables generation concluded @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                                if (THREAD_LIMIT == 1) {
                                    doubleLog(OBDModel.log, "Beginning R Analysis in main thread @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                                    new AnalysisThread(model, config, threadCount).run(); // this runs it in the main thread
                                } else {
                                    while (threadCount >= THREAD_LIMIT) {
                                        System.out.println("Waiting on availiable thread @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                                        Thread.sleep(10000);
                                    }

                                    doubleLog(OBDModel.log, "Beginning R Analysis in new thread @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                                    new AnalysisThread(model, config, threadCount).start(); // this runs it in a new thread
                                }

                                doubleLog(OBDModel.log, "R Analysis concluded @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                            } catch (final PreEmptiveOutOfMemoryWarning e) {
                                model.recordOutOfMemorySummary();
                                model.getSummaryRow().outputSummaryRowToFile();

                                System.out.println("JOB RETURNED - Insufficient memory @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " - " + chosenJob.toString(order));
                                // put job back in queue with higher memory requirement
                                returnJobToQueue(jobQPath, chosenJob, (int) Math.ceil(assignedMemory * memoryIncreaseOnMemoryException), chosenJob.getInt("priority"), true);
                            }

                        } catch (final InvalidInputFileException e) {
                            System.out.println("JOB RETURNED - Invalid input @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " - " + chosenJob.toString(order));
                            returnJobToQueue(jobQPath, chosenJob, chosenJob.getInt("required memory"), 99, true);
                        }

                    } else {
                        System.out.println("NO SUITABLE JOB FOUND @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        Thread.sleep(10000);
                    }
                } catch (final InvalidInputFileException e) {
                    System.out.println("Either the job file doesn't have any jobs or it's malformed - I'm going to keep looping and wait for you to fix that... @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    Thread.sleep(60000);
                }
            }
        }

        System.out.println("Closing due to status");
    }

    private static void doubleLog(final Logger l, final String s) {
        System.out.println(s);
        l.info(s);
    }

    // checks recent load average - if over threshold then does not run next sim until load average has dropped
    private static boolean nodeIdle(final double threshold) throws IOException, InterruptedException {
        final String result = execCmd("uptime");
        final String[] split = result.split(" ");
        final double load = Double.parseDouble(split[split.length - 3].split(",")[0]);

        System.out.println("Node check (" + load + ") @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        if(load > threshold) {
            System.out.println("Node busy (" + load + ") @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            Thread.sleep(60000);
        }
        
        return load < threshold;
    }

    public static String execCmd(final String cmd) throws java.io.IOException {
        // TODO use ProcessBuilder.
        try (final java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        }
    }

    private static boolean getStatus(final Path statusPath) throws IOException {
        // read in file
        final ArrayList<String> lines = new ArrayList<>(InputFileReader.getAllLines(statusPath));

        if(!lines.isEmpty()) {
            switch (lines.get(0)) {
                case "run": return true;
                case "terminate" : return false;
            }
        }

        return true;
    }

    private static boolean checkPause(final Path statusPath) throws IOException, InterruptedException {
        // read in file
        final ArrayList<String> lines = new ArrayList<>(InputFileReader.getAllLines(statusPath));
        
        boolean pause = false;
        
        if(!lines.isEmpty())
            pause = lines.get(0).equals("pause");

        if(pause) {
            System.out.println("Status 'pause' @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            Thread.sleep(10000);
        }

        return pause;
    }

    public static DataRow getJob(final Path jobFile, final int availiableMemory) throws IOException, InterruptedException, InvalidInputFileException {

        final FileChannel fileChannel = getFileChannel(jobFile);
        DataRow chosenJob = null;

        try {
            // lock job file
            System.out.println("Locking job file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            fileChannel.lock(0, Long.MAX_VALUE, false);
            System.out.println("Locked job file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            DataRowSet jobs = readInJobFile(fileChannel);

            jobs = explodeRegexJobs(jobs);
            writeToJobFile(fileChannel, jobs);

            int maxRequiredMemory = 0;
            int maxPriorityLevel = 99;

            int maxUncheckedMemory = 0;
            int maxUncheckedPriorityLevel = 99;

            // throw away jobs requiring too much memory
            final Map<String, DataRowSet> jobsByMemory = jobs.splitOn("required memory");

            for(final String memoryRequired : jobsByMemory.keySet()) {

                final int reqMemory = Integer.valueOf(memoryRequired);

                if(availiableMemory >= reqMemory) {

                    for(final DataRow dr : jobsByMemory.get(memoryRequired)) {
                        // get priority
                        try {
                            final int priority = dr.getInt("priority");

                            // if priority greater than maxPriority
                            if(priority < maxPriorityLevel) {
                                // if priority equal to 1 OR required memory meets usage threshold then
                                if(priority == 1 || reqMemory > availiableMemory * appropriateUsageThreshold) {
                                    // update maxPriority and maxRequiredMemory
                                    maxPriorityLevel = priority;
                                    maxRequiredMemory = reqMemory;
                                }
                            }

                            // if priority equal to maxPriority and memory greater than maxRequiredMemory
                            if(priority == maxPriorityLevel && reqMemory > maxRequiredMemory)
                                maxRequiredMemory = reqMemory;

                            if(priority <= maxUncheckedPriorityLevel && reqMemory > maxUncheckedMemory) {
                                maxUncheckedMemory = reqMemory;
                                maxUncheckedPriorityLevel = priority;
                            }

                        } catch (final InvalidInputFileException e) {
                            // Priority is malformed
                            returnJobToQueue(jobFile, dr, dr.getInt("required memory"), 99, false);
                        }
                    }
                }
            }

            // if a job has been found
            if(maxPriorityLevel != 99 && maxRequiredMemory != 0) {
                chosenJob = chooseJob(jobsByMemory, maxRequiredMemory, maxPriorityLevel, jobs, fileChannel);
            } else if(maxUncheckedPriorityLevel != 99 & maxUncheckedMemory != 0) {
                chosenJob = chooseJob(jobsByMemory, maxUncheckedMemory, maxUncheckedPriorityLevel, jobs, fileChannel);
            }

        } finally {
            // release job file
            fileChannel.close(); // also releases the lock
            System.out.println("Releasing job file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        return chosenJob;
        
    }

    static String multipleJobs = "-*[0-4]\\.[0-9]+->[0-4]\\.[0-9]+@[0-4]\\.[0-9]+";

    private static DataRowSet explodeRegexJobs(final DataRowSet jobs) throws InvalidInputFileException {

        DataRowSet explodedJobs = null;
        DataRowSet toRemoveJobs = null;

        for(final DataRow job : jobs) {

            if(job.getInt("priority") != 99 && (job.getValue("prf").matches(multipleJobs) || job.getValue("rf").matches(multipleJobs))) {

                try {
                    for(final double prf : toValueSet(job.getValue("prf"))) {
                        for(final double rf : toValueSet(job.getValue("rf"))) {
                            final DataRow copy = job.clone();
                            copy.setValue("rf", String.valueOf(rf));
                            copy.setValue("prf", String.valueOf(prf));

                            if (explodedJobs == null) {
                                explodedJobs = new DataRowSet(copy);
                            } else {
                                explodedJobs.add(copy);
                            }

                        }
                    }

                    if(toRemoveJobs == null) {
                        toRemoveJobs = new DataRowSet(job);
                    } else {
                        toRemoveJobs.add(job);
                    }

                } catch (final InvalidInputFileException e) {
                    job.setValue("priority", "99");
                }
            }
        }

        if(toRemoveJobs != null)
            for(final DataRow toRemove : toRemoveJobs)
                jobs.remove(toRemove);

        if(explodedJobs != null)
            for(final DataRow toAdd : explodedJobs)
                jobs.add(toAdd);

        return jobs;

    }

    private static Set<Double> toValueSet(final String rfExpression) throws InvalidInputFileException {

        if(rfExpression.matches(multipleJobs)) {
            final Set<Double> set = new HashSet<>();

            final String[] splitA = rfExpression.split("->");
            if(splitA.length != 2) throw new InvalidInputFileException("Multi job expresion incorrect");

            final String[] splitB = splitA[1].split("@");
            if(splitB.length != 2) throw new InvalidInputFileException("Multi job expresion incorrect");

            double a = Double.valueOf(splitA[0]);
            double b = Double.valueOf(splitB[0]);
            final double inc = Double.valueOf(splitB[1]);

            if(a > b) {
                final double temp = a;
                a = b;
                b = temp;
            }

            for(double d = a; clean(d, a, inc) <= b; d+=inc) {
                set.add(clean(d, a, inc));
            }

            return set;

        } else {
            return Collections.singleton(Double.valueOf(rfExpression));
        }

    }

    private static double clean(final double d, final double a, final double inc) {
        final int roundTo = Math.max(postPointDigits(a), postPointDigits(inc));
        final DecimalFormat df = new DecimalFormat(generatePattern(roundTo));
        df.setRoundingMode(RoundingMode.HALF_UP);
        return Double.valueOf(df.format(d));
    }

    private static String generatePattern(final int roundTo) {
        final StringBuilder sb = new StringBuilder("#.");

        for(int i = 0; i < roundTo; i ++) {
            sb.append("#");
        }

        return sb.toString();
    }

    private static int postPointDigits(final double d) {
        final String[] split = String.valueOf(d).split("\\.");
        return split.length == 1 ? 1 : split[1].length();
    }

    private static DataRow chooseJob(final Map<String, DataRowSet> jobsByMemory, final int maxRequiredMemory, final int maxPriorityLevel, final DataRowSet jobs, final FileChannel fileChannel) throws IOException, InvalidInputFileException {

        // set of jobs that can be done iwth this host's resources
        final DataRowSet chosenJobSet = jobsByMemory.get(String.valueOf(maxRequiredMemory)).splitOn("priority").get(String.valueOf(maxPriorityLevel));

        // take job at head of remaning jobQueue
        final DataRow chosenJob  = chosenJobSet.iterator().next();

        // decrement n value in chosen job
        int n = Integer.valueOf(chosenJob.getValue("n"));
        n--;

        // if n now equals 0
        if (n == 0) {
            // remove job from data structure
            jobs.remove(chosenJob);
        } else {
            chosenJob.setValue("n", String.valueOf(n));
        }

        if(chosenJob.getInt("priority") > 3)
            chosenJob.setValue("priority", "3");

        // write datastructure back to job file
        writeToJobFile(fileChannel, jobs);

        return chosenJob;
    }

    private static Config convertJobToConfig(final DataRow chosenJob) throws InvalidInputFileException {

        final Config config = new Config(
                chosenJob.getLocalDate("tS"),
                chosenJob.getLocalDate("t0"),
                chosenJob.getLocalDate("tE"),
                chosenJob.getInt("seed size"),
                chosenJob.getPath("input dir"),
                chosenJob.getPath("results dir"),
                chosenJob.getValue("reason"),
                chosenJob.getPath("summary results dir"));

        config.setSetupBirthRate(chosenJob.getDouble("setup br"));
        config.setSetupDeathRate(chosenJob.getDouble("setup dr"));
        config.setRecoveryFactor(chosenJob.getDouble("rf"));
        config.setProportionalRecoveryFactor(chosenJob.getDouble("prf"));
        config.setInputWidth(chosenJob.getPeriod("iw"));
        config.setMinBirthSpacing(chosenJob.getPeriod("min birth spacing"));
        config.setMinGestationPeriod(chosenJob.getPeriod("min ges period"));
        config.setOverSizedGeographyFactor(chosenJob.getValue("oversized geography factor"));

        try {
            config.setOutputRecordFormat(Enum.valueOf(RecordFormat.class, chosenJob.getValue("output record format")));
        } catch (final IllegalArgumentException e) {
            throw new InvalidInputFileException("Invalid value for output record format");
        }

        final boolean deterministic = chosenJob.getBoolean("deterministic");
        config.setDeterministic(deterministic);
        if(deterministic) {
            config.setSeed(chosenJob.getInt("seed"));
        }

        config.setTimestep(chosenJob.getPeriod("timestep"));
        config.setBinomialSampling(chosenJob.getBoolean("binomial sampling"));
        config.setCTtreeStepBack(chosenJob.getInt("ct tree stepback"));

        // return config
        return config;
    }

    private static FileChannel getFileChannel(final Path jobFile) throws IOException {
        final HashSet<StandardOpenOption> options = new HashSet<>(Sets.newHashSet(StandardOpenOption.READ, StandardOpenOption.WRITE));
        return FileChannel.open(jobFile, options);
    }

    public static void returnJobToQueue(final Path jobFile, final DataRow chosenJob, final int requiredMemory, final int priority, final boolean releaseLockOnExit) throws IOException, InvalidInputFileException, InterruptedException {

        // get file and lock
        final FileChannel fileChannel = getFileChannel(jobFile);

        try {
            System.out.println("Locking job file (R) @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            fileChannel.lock(0, Long.MAX_VALUE, false);
            System.out.println("Locked job file (R) @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // read in file to data structure
            final DataRowSet jobs = readInJobFile(fileChannel);

            boolean jobFound = false;

            for(final DataRow job : jobs) {
                // find job that matches on all fields apart from count and memory requirment
                if(sameJob(job, chosenJob)) {
                    // inc. count by 1
                    job.setValue("n", String.valueOf(job.getInt("n") + 1));

                    // update memory requirement (if an increase)
                    if(job.getInt("required memory") < requiredMemory) {
                        job.setValue("required memory", String.valueOf(requiredMemory));
                    }

                    job.setValue("priority", String.valueOf(priority));
                    jobFound = true;
                    break;
                }
            }

            if(!jobFound) {
                // assume remaining job count has been undertaken by other process and thus count has reached zero - thus reinsert job with count 1
                chosenJob.setValue("n", String.valueOf(1));
                chosenJob.setValue("required memory", String.valueOf(requiredMemory));
                chosenJob.setValue("priority", String.valueOf(priority));
                jobs.add(chosenJob);
            }

            // write ds back to file
            writeToJobFile(fileChannel, jobs);

        } finally {
            // release job file
            if(releaseLockOnExit) {
                fileChannel.close(); // also releases the lock
                System.out.println("Released job file (R) @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
        }
    }

    private static boolean sameJob(final DataRow a, final DataRow b) {
        // assert if all fields apart from count and memory requirment
        for(final String label : a.getLabels())
            if(!label.equals("required memory") && !label.equals("n") )
                if(!a.getValue(label).equals(b.getValue(label)))
                    return false;

        return true;
    }

    public static void writeToJobFile(final FileChannel jobFile, final DataRowSet jobs) throws IOException {
        jobFile.truncate(0);

        final String toFileString = jobs.toString(order);

        final ByteBuffer buf = ByteBuffer.allocate(toFileString.getBytes().length + 1000);
        buf.clear();
        buf.put(toFileString.getBytes());

        buf.flip();

        while(buf.hasRemaining()) {
            jobFile.write(buf);
        }
    }

    public static DataRowSet readInJobFile(final FileChannel jobFile) throws IOException, InvalidInputFileException, InterruptedException {

        final ByteBuffer buffer = ByteBuffer.allocate((int) jobFile.size());
        int noOfBytesRead = jobFile.read(buffer);

        String labels = null;
        final Set<String> data = new HashSet<>();

        while (noOfBytesRead != -1) {

            buffer.flip();

            String line = "";

            while (buffer.hasRemaining()) {

                final char c = (char) buffer.get();
                if(c == '\n') {
                    if(labels == null)
                        labels = line;
                    else
                        data.add(line);

                    line = "";
                } else {
                    line += c;
                }

            }

            if(!line.equals("")) data.add(line);

            buffer.clear();
            Thread.sleep(1000);
            noOfBytesRead = jobFile.read(buffer);
        }

        try {
            return new DataRowSet(labels, data);
        } catch (final NoSuchElementException e) {
            throw new InvalidInputFileException("There's no jobs in the file");
        }
    }

}
