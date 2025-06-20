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

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.RecordFormat;

public class SerializableSummaryRow implements Serializable {
    public LocalDateTime startTime;
    public String reason;
    public String codeVersion;
    public Period inputWidth;
    public Period timestep;
    public LocalDate startDate;
    public LocalDate endDate;
    public int simLength;
    public int seed;
    public double recoveryFactor;
    public double proportionalRecoveryFactor;
    public double oversizedGeographyFactor;
    public Period minBirthSpacing;
    public RecordFormat outputRecordFormat;
    public int seedPop;
    public String resultsDirectory;
    public String inputsDirectory;
    public int startPop;
    public int totalPop;
    public int endPop;
    public int peakPop;
    public int ctTreeStepback;
    public double ctTreePrecision;
    public int eligibilityChecks;
    public int failedEligibilityChecks;
    public boolean completed;
    public double simRunTime;
    public double ctRunTime;
    public double recordsRunTime;
    public double statsRunTime;
    public boolean binomialSampling;
    public long maxMemoryUsage;
    public Double v;
    public String hostname;
    public SerializableConfig config;

    public SerializableSummaryRow(
        LocalDateTime startTime,
        String reason,
        String codeVersion,
        Period inputWidth,
        Period timestep,
        LocalDate startDate,
        LocalDate endDate,
        int simLength,
        int seed,
        double recoveryFactor,
        double proportionalRecoveryFactor,
        double oversizedGeographyFactor,
        Period minBirthSpacing,
        RecordFormat outputRecordFormat,
        int seedPop,
        String resultsDirectory,
        String inputsDirectory,
        int startPop,
        int totalPop,
        int endPop,
        int peakPop,
        int ctTreeStepback,
        double ctTreePrecision,
        int eligibilityChecks,
        int failedEligibilityChecks,
        boolean completed,
        double simRunTime,
        double ctRunTime,
        double recordsRunTime,
        double statsRunTime,
        boolean binomialSampling,
        long maxMemoryUsage,
        Double v,
        String hostname,
        SerializableConfig config
    ) {
        this.startTime                     = startTime;
        this.reason                        = reason;
        this.codeVersion                   = codeVersion;
        this.inputWidth                    = inputWidth;
        this.timestep                      = timestep;
        this.startDate                     = startDate;
        this.endDate                       = endDate;
        this.simLength                     = simLength;
        this.seed                          = seed;
        this.recoveryFactor                = recoveryFactor;
        this.proportionalRecoveryFactor    = proportionalRecoveryFactor;
        this.oversizedGeographyFactor      = oversizedGeographyFactor;
        this.minBirthSpacing               = minBirthSpacing;
        this.outputRecordFormat            = outputRecordFormat;
        this.seedPop                       = seedPop;
        this.resultsDirectory              = resultsDirectory;
        this.inputsDirectory               = inputsDirectory;
        this.startPop                      = startPop;
        this.totalPop                      = totalPop;
        this.endPop                        = endPop;
        this.peakPop                       = peakPop;
        this.ctTreeStepback                = ctTreeStepback;
        this.ctTreePrecision               = ctTreePrecision;
        this.eligibilityChecks             = eligibilityChecks;
        this.failedEligibilityChecks       = failedEligibilityChecks;
        this.completed                     = completed;
        this.simRunTime                    = simRunTime;
        this.ctRunTime                     = ctRunTime;
        this.recordsRunTime                = recordsRunTime;
        this.statsRunTime                  = statsRunTime;
        this.binomialSampling              = binomialSampling;
        this.maxMemoryUsage                = maxMemoryUsage;
        this.v                             = v;
        this.hostname                      = hostname;
        this.config                        = config;
    }
}
