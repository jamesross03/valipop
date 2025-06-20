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
package uk.ac.standrews.cs.valipop.statistics.analysis.simulationSummaryLogging;

import uk.ac.standrews.cs.valipop.Config;
import uk.ac.standrews.cs.valipop.implementations.SerializableSummaryRow;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.RecordFormat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Data structure containing simulation meta data.
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class SummaryRow {

    private static final String SEPARATOR = ",";

    private LocalDateTime startTime;
    private String reason;
    private String codeVersion;

    private Period inputWidth;
    private Period timestep;

    private LocalDate startDate;
    private LocalDate endDate;

    private int simLength;

    private int seed;

    private double recoveryFactor;
    private double proportionalRecoveryFactor;
    private double oversizedGeographyFactor;
    private Period minBirthSpacing;

    private RecordFormat outputRecordFormat;

    private int seedPop;

    private Path resultsDirectory;
    private Path inputsDirectory;

    private int startPop;
    private int totalPop;
    private int endPop;
    private int peakPop;

    private int ctTreeStepback;
    private double ctTreePrecision;

    private int eligibilityChecks;
    private int failedEligibilityChecks;

    private boolean completed = false;

    private double simRunTime;
    private double ctRunTime;
    private double recordsRunTime;
    private double statsRunTime;

    private boolean binomialSampling;

    private long maxMemoryUsage = -1L;
    private Double v = Double.NaN;

    private String hostname;

    private Config config;

    public SummaryRow(Config config, String codeVersion, String hostname) {

        this.config = config;
        this.resultsDirectory = config.getRunPath();
        this.inputsDirectory = config.getVarPath();
        this.startTime = config.getStartTime();
        this.reason = config.getRunPurpose();
        this.codeVersion = codeVersion;
        this.timestep = config.getSimulationTimeStep();
        this.inputWidth = config.getInputWidth();
        this.startDate = config.getT0();
        this.endDate = config.getTE();
        this.simLength = (int) DAYS.between(startDate, endDate);
        this.recoveryFactor = config.getRecoveryFactor();
        this.proportionalRecoveryFactor = config.getProportionalRecoveryFactor();
        this.binomialSampling = config.getBinomialSampling();
        this.minBirthSpacing = config.getMinBirthSpacing();
        this.outputRecordFormat = config.getOutputRecordFormat();
        this.seedPop = config.getT0PopulationSize();
        this.seed = config.getSeed();
        this.ctTreeStepback = config.getCtTreeStepback();
        this.ctTreePrecision = config.getCtTreePrecision();
        this.hostname = hostname;
        this.oversizedGeographyFactor = config.getOverSizedGeographyFactor();
    }

    public void setStartPop(int startPop) {
        this.startPop = startPop;
    }

    public void setTotalPop(int totalPop) {
        this.totalPop = totalPop;
    }

    public void setEndPop(int endPop) {
        this.endPop = endPop;
    }

    public void setPeakPop(int peakPop) {
        this.peakPop = peakPop;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setSimRunTime(double simRunTime) {
        this.simRunTime = simRunTime;
    }

    public void setCTRunTime(double ctRunTime) {
        this.ctRunTime = ctRunTime;
    }

    public void setRecordsRunTime(double recordsRunTime) {
        this.recordsRunTime = recordsRunTime;
    }

    public void setStatsRunTime(double statsRunTime) {
        this.statsRunTime = statsRunTime;
    }

    public void setMaxMemoryUsage(long maxUsage) {
        this.maxMemoryUsage = maxUsage;
    }

    public void setEligibilityChecks(int eligibilityChecks) {
        this.eligibilityChecks = eligibilityChecks;
    }

    public void setFailedEligibilityChecks(int failedEligibilityChecks) {
        this.failedEligibilityChecks = failedEligibilityChecks;
    }

    public String toString() {

        return makeRow(startTime, reason, codeVersion, inputsDirectory, totalPop, seedPop,
                completed, simLength, timestep, inputWidth, startPop,
                endPop, peakPop, startDate, endDate, simRunTime,
                ctRunTime, recordsRunTime, resultsDirectory, recoveryFactor,
                proportionalRecoveryFactor, binomialSampling,
                minBirthSpacing, (maxMemoryUsage / 1e6), outputRecordFormat.toString(),
                v.toString(), statsRunTime, eligibilityChecks, failedEligibilityChecks, seed,
                ctTreeStepback, ctTreePrecision, hostname, oversizedGeographyFactor) + "\n";
    }

    private static String makeRow(Object... values) {

        StringBuilder builder = new StringBuilder();

        for (int i = 0 ; i < values.length - 1; i ++) {
            Object value = values[i];

            builder.append(value);
            builder.append(SEPARATOR);
        }

        builder.append(values[values.length - 1]);

        return builder.toString();
    }

    public static String getSeparatedHeadings() {

        return makeRow("Start Time" ,"Reason", "Code Version", "Inputs Directory", "Total Pop",
                 "Seed Pop Size", "Completed", "Sim Length", "Timestep" ,
                 "Input Width", "Start Pop", "End Pop", "Peak Pop", "Start Date" ,
                 "End Date", "Sim Run time", "CT Run time", "Records Run time" ,
                 "Results Directory", "Recovery Factor" ,
                 "Proportional Recovery Factor", "binomial Sampling", "Min Birth Spacing" ,
                 "Peak Memory Usage (MB)", "Output Record Format", "v/M", "Stats Run Time" ,
                 "Eligibility Checks", "Failed Eligibility Checks", "Seed", "CT Tree Stepback",
                 "CT Tree Precision", "Hostname", "Oversized Geography Factor");
    }

    public void outputSummaryRowToFile() {
        try {
            Files.write(config.getGlobalSummaryPath(), toString().getBytes(), StandardOpenOption.APPEND);
            Files.write(config.getResultsSummaryPath(), toString().getBytes(), StandardOpenOption.APPEND);

        } catch (IOException e) {
            throw new RuntimeException("Summary row could not be printed to summary files", e);
        }
    }

    public void setV(double v) {
        this.v = v;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public SummaryRow(SerializableSummaryRow sr) {
        this.startTime                     = sr.startTime;
        this.reason                        = sr.reason;
        this.codeVersion                   = sr.codeVersion;
        this.inputWidth                    = sr.inputWidth;
        this.timestep                      = sr.timestep;
        this.startDate                     = sr.startDate;
        this.endDate                       = sr.endDate;
        this.simLength                     = sr.simLength;
        this.seed                          = sr.seed;
        this.recoveryFactor                = sr.recoveryFactor;
        this.proportionalRecoveryFactor    = sr.proportionalRecoveryFactor;
        this.oversizedGeographyFactor      = sr.oversizedGeographyFactor;
        this.minBirthSpacing               = sr.minBirthSpacing;
        this.outputRecordFormat            = sr.outputRecordFormat;
        this.seedPop                       = sr.seedPop;
        this.resultsDirectory              = Path.of(sr.resultsDirectory);
        this.inputsDirectory               = Path.of(sr.inputsDirectory);
        this.startPop                      = sr.startPop;
        this.totalPop                      = sr.totalPop;
        this.endPop                        = sr.endPop;
        this.peakPop                       = sr.peakPop;
        this.ctTreeStepback                = sr.ctTreeStepback;
        this.ctTreePrecision               = sr.ctTreePrecision;
        this.eligibilityChecks             = sr.eligibilityChecks;
        this.failedEligibilityChecks       = sr.failedEligibilityChecks;
        this.completed                     = sr.completed;
        this.simRunTime                    = sr.simRunTime;
        this.ctRunTime                     = sr.ctRunTime;
        this.recordsRunTime                = sr.recordsRunTime;
        this.statsRunTime                  = sr.statsRunTime;
        this.binomialSampling              = sr.binomialSampling;
        this.maxMemoryUsage                = sr.maxMemoryUsage;
        this.v                             = sr.v;
        this.hostname                      = sr.hostname;
        this.config                        = new Config(sr.config);
    }

    public SerializableSummaryRow toSerialized() {
        return new SerializableSummaryRow(
            startTime,
            reason,
            codeVersion,
            inputWidth,
            timestep,
            startDate,
            endDate,
            simLength,
            seed,
            recoveryFactor,
            proportionalRecoveryFactor,
            oversizedGeographyFactor,
            minBirthSpacing,
            outputRecordFormat,
            seedPop,
            resultsDirectory.toString(),
            inputsDirectory.toString(),
            startPop,
            totalPop,
            endPop,
            peakPop,
            ctTreeStepback,
            ctTreePrecision,
            eligibilityChecks,
            failedEligibilityChecks,
            completed,
            simRunTime,
            ctRunTime,
            recordsRunTime,
            statsRunTime,
            binomialSampling,
            maxMemoryUsage,
            v,
            hostname,
            config.toSerialized()
        );
    }
}
