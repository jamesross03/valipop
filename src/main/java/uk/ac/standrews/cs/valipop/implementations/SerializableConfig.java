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

import uk.ac.standrews.cs.valipop.export.ExportFormat;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.RecordFormat;

public class SerializableConfig implements Serializable {
    public String varPath;
    public String varOrderedBirthPaths;
    public String varMaleLifetablePaths;
    public String varMaleDeathCausesPaths;
    public String varFemaleLifetablePaths;
    public String varFemaleDeathCausesPaths;
    public String varMultipleBirthPaths;
    public String varAdulterousBirthPaths;
    public String varPartneringPaths;
    public String varSeparationPaths;
    public String varBirthRatioPaths;
    public String varMaleForenamePaths;
    public String varFemaleForenamePaths;
    public String varMigrantMaleForenamePaths;
    public String varMigrantFemaleForenamePaths;
    public String varMigrantSurnamePaths;
    public String varMigrationRatePaths;
    public String varSurnamePaths;
    public String varMarriagePaths;
    public String varGeographyPaths;
    public String varMaleOccupationPaths;
    public String varFemaleOccupationPaths;
    public String varMaleOccupationChangePaths;
    public String varFemaleOccupationChangePaths;
    public String globalSummaryPath;
    public String resultsSummaryPath;
    public String detailedResultsPath;
    public String birthOrdersPath;
    public String recordsPath;
    public String graphsPath;
    public String contingencyTablesPath;
    public String runPath;
    public double setUpBR;
    public double setUpDR;
    public double recoveryFactor;
    public double proportionalRecoveryFactor;
    public boolean binomialSampling;
    public boolean deterministic;
    public boolean outputTables;
    public Period simulationTimeStep;
    public Period minBirthSpacing;
    public Period minGestationPeriod;
    public Period inputWidth;
    public String summaryResultsDirPath;
    public String resultsSavePath;
    public String geographyFilePath;
    public String projectPath;
    public int seed;
    public double overSizedGeographyFactor;
    public int ctTreeStepback;
    public double ctTreePrecision;
    public String runPurpose;
    public RecordFormat outputRecordFormat;
    public ExportFormat outputGraphFormat;
    public LocalDateTime startTime;
    public LocalDate tS;
    public LocalDate t0;
    public LocalDate tE;
    public int t0PopulationSize;

    public SerializableConfig(
        String varPath,
        String varOrderedBirthPaths,
        String varMaleLifetablePaths,
        String varMaleDeathCausesPaths,
        String varFemaleLifetablePaths,
        String varFemaleDeathCausesPaths,
        String varMultipleBirthPaths,
        String varAdulterousBirthPaths,
        String varPartneringPaths,
        String varSeparationPaths,
        String varBirthRatioPaths,
        String varMaleForenamePaths,
        String varFemaleForenamePaths,
        String varMigrantMaleForenamePaths,
        String varMigrantFemaleForenamePaths,
        String varMigrantSurnamePaths,
        String varMigrationRatePaths,
        String varSurnamePaths,
        String varMarriagePaths,
        String varGeographyPaths,
        String varMaleOccupationPaths,
        String varFemaleOccupationPaths,
        String varMaleOccupationChangePaths,
        String varFemaleOccupationChangePaths,
        String globalSummaryPath,
        String resultsSummaryPath,
        String detailedResultsPath,
        String birthOrdersPath,
        String recordsPath,
        String graphsPath,
        String contingencyTablesPath,
        String runPath,
        double setUpBR,
        double setUpDR,
        double recoveryFactor,
        double proportionalRecoveryFactor,
        boolean binomialSampling,
        boolean deterministic,
        boolean outputTables,
        Period simulationTimeStep,
        Period minBirthSpacing,
        Period minGestationPeriod,
        Period inputWidth,
        String summaryResultsDirPath,
        String resultsSavePath,
        String geographyFilePath,
        String projectPath,
        int seed,
        double overSizedGeographyFactor,
        int ctTreeStepback,
        double ctTreePrecision,
        String runPurpose,
        RecordFormat outputRecordFormat,
        ExportFormat outputGraphFormat,
        LocalDateTime startTime,
        LocalDate tS,
        LocalDate t0,
        LocalDate tE,
        int t0PopulationSize
    ) {
        this.varPath                          =varPath;
        this.varOrderedBirthPaths             =varOrderedBirthPaths;
        this.varMaleLifetablePaths            =varMaleLifetablePaths;
        this.varMaleDeathCausesPaths          =varMaleDeathCausesPaths;
        this.varFemaleLifetablePaths          =varFemaleLifetablePaths;
        this.varFemaleDeathCausesPaths        =varFemaleDeathCausesPaths;
        this.varMultipleBirthPaths            =varMultipleBirthPaths;
        this.varAdulterousBirthPaths          =varAdulterousBirthPaths;
        this.varPartneringPaths               =varPartneringPaths;
        this.varSeparationPaths               =varSeparationPaths;
        this.varBirthRatioPaths               =varBirthRatioPaths;
        this.varMaleForenamePaths             =varMaleForenamePaths;
        this.varFemaleForenamePaths           =varFemaleForenamePaths;
        this.varMigrantMaleForenamePaths      =varMigrantMaleForenamePaths;
        this.varMigrantFemaleForenamePaths    =varMigrantFemaleForenamePaths;
        this.varMigrantSurnamePaths           =varMigrantSurnamePaths;
        this.varMigrationRatePaths            =varMigrationRatePaths;
        this.varSurnamePaths                  =varSurnamePaths;
        this.varMarriagePaths                 =varMarriagePaths;
        this.varGeographyPaths                =varGeographyPaths;
        this.varMaleOccupationPaths           =varMaleOccupationPaths;
        this.varFemaleOccupationPaths         =varFemaleOccupationPaths;
        this.varMaleOccupationChangePaths     =varMaleOccupationChangePaths;
        this.varFemaleOccupationChangePaths   =varFemaleOccupationChangePaths;
        this.globalSummaryPath                =globalSummaryPath;
        this.resultsSummaryPath               =resultsSummaryPath;
        this.detailedResultsPath              =detailedResultsPath;
        this.birthOrdersPath                  =birthOrdersPath;
        this.recordsPath                      =recordsPath;
        this.graphsPath                       =graphsPath;
        this.contingencyTablesPath            =contingencyTablesPath;
        this.runPath                          =runPath;
        this.setUpBR                          =setUpBR;
        this.setUpDR                          =setUpDR;
        this.recoveryFactor                   =recoveryFactor;
        this.proportionalRecoveryFactor       =proportionalRecoveryFactor;
        this.binomialSampling                 =binomialSampling;
        this.deterministic                    =deterministic;
        this.outputTables                     =outputTables;
        this.simulationTimeStep               =simulationTimeStep;
        this.minBirthSpacing                  =minBirthSpacing;
        this.minGestationPeriod               =minGestationPeriod;
        this.inputWidth                       =inputWidth;
        this.summaryResultsDirPath            =summaryResultsDirPath;
        this.resultsSavePath                  =resultsSavePath;
        this.geographyFilePath                =geographyFilePath;
        this.projectPath                      =projectPath;
        this.seed                             =seed;
        this.overSizedGeographyFactor         =overSizedGeographyFactor;
        this.ctTreeStepback                   =ctTreeStepback;
        this.ctTreePrecision                  =ctTreePrecision;
        this.runPurpose                       =runPurpose;
        this.outputRecordFormat               =outputRecordFormat;
        this.outputGraphFormat                =outputGraphFormat;
        this.startTime                        =startTime;
        this.tS                               =tS;
        this.t0                               =t0;
        this.tE                               =tE;
        this.t0PopulationSize                 =t0PopulationSize;
    }
}
