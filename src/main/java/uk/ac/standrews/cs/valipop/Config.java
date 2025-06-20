/*
 * Copyright 2017 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module population_model.
 *
 * population_model is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * population_model is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with population_model. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.valipop;

import uk.ac.standrews.cs.valipop.export.ExportFormat;
import uk.ac.standrews.cs.valipop.implementations.SerializableConfig;
import uk.ac.standrews.cs.valipop.statistics.analysis.simulationSummaryLogging.SummaryRow;
import uk.ac.standrews.cs.valipop.utils.InputFileReader;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.RecordFormat;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.logging.*;

/**
 * This class provides the configuration for the Simulation model.
 *
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class Config implements Serializable {

    // ---- Constants ----
    private static final Level DEFAULT_LOG_LEVEL = Level.SEVERE;

    private static final boolean DEFAULT_BINOMIAL_SAMPLING_FLAG = true;
    private static final boolean DEFAULT_DETERMINISTIC_FLAG = false;
    private static final boolean DEFAULT_OUTPUT_TABLES_FLAG = true;

    private static final double DEFAULT_SETUP_BR = 0.0133;
    private static final double DEFAULT_SETUP_DR = 0.0122;
    private static final double DEFAULT_RECOVERY_FACTOR = 1.0;
    private static final double DEFAULT_PROPORTIONAL_RECOVERY_FACTOR = 1.0;
    private static final double DEFAULT_OVERSIZED_GEOGRAPHY_FACTOR = 1.0;

    private static final Period DEFAULT_SIMULATION_TIME_STEP = Period.ofYears(1);
    private static final Period DEFAULT_INPUT_WIDTH = Period.ofYears(1);
    private static final Period DEFAULT_MIN_BIRTH_SPACING = Period.ofDays(147);
    private static final Period DEFAULT_MIN_GESTATION_PERIOD = Period.ofDays(147);

    private static final int DEFAULT_SEED = 56854687;
    private static final int DEFAULT_CT_TREE_STEPBACK = 1;
    private static final double DEFAULT_CT_TREE_PRECISION = 1E-66;

    private static final RecordFormat DEFAULT_OUTPUT_RECORD_FORMAT = RecordFormat.NONE;
    private static final ExportFormat DEFAULT_OUTPUT_GRAPH_FORMAT = ExportFormat.NONE;
    private static final String DEFAULT_RUN_PURPOSE = "default";

    // Input directory structure
    private static final String birthSubFile = "birth";
    private static final String orderedBirthSubFile = "ordered_birth";
    private static final String multipleBirthSubFile = "multiple_birth";
    private static final String adulterousBirthSubFile = "adulterous_birth";
    private static final String birthRatioSubFile = "ratio_birth";

    private static final String relationshipsSubFile = "relationships";
    private static final String partneringSubFile = "partnering";
    private static final String separationSubFile = "separation";
    private static final String marriageSubFile = "marriage";

    private static final String deathSubFile = "death";
    private static final String maleDeathSubFile = "males";
    private static final String femaleDeathSubFile = "females";
    private static final String lifetableSubFile = "lifetable";
    private static final String deathCauseSubFile = "cause";

    private static final String annotationsSubFile = "annotations";
    private static final String maleForenameSubFile = "male_forename";
    private static final String femaleForenameSubFile = "female_forename";
    private static final String maleMigrantForenameSubFile = "migration/male_forename";
    private static final String femaleMigrantForenameSubFile = "migration/female_forename";
    private static final String migrantSurnameSubFile = "migration/surname";
    private static final String migrationRateSubFile = "migration/rate";
    private static final String surnameSubFile = "surname";
    private static final String geographySubFile = "geography";

    private static final String maleOccupationSubFile = "occupation/male";
    private static final String femaleOccupationSubFile = "occupation/female";

    private static final String maleOccupationChangeSubFile = "occupation/change/male";
    private static final String femaleOccupationChangeSubFile = "occupation/change/female";

    private static final Logger log = Logger.getLogger(Config.class.getName());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss-SSS", Locale.UK);
    private static Level logLevel = DEFAULT_LOG_LEVEL;
    public static final Path DEFAULT_RESULTS_SAVE_PATH = Paths.get("results");
    private final Path DEFAULT_GEOGRAPHY_FILE_PATH = Paths.get("geography.ser");
    private final Path DEFAULT_PROJECT_PATH = Paths.get(".");

    // ---- Input directory paths ----

    // Input directory path
    private Path varPath;

    // Paths to leaf directories within the input directory
    private Path varOrderedBirthPaths;
    private Path varMaleLifetablePaths;
    private Path varMaleDeathCausesPaths;
    private Path varFemaleLifetablePaths;
    private Path varFemaleDeathCausesPaths;
    private Path varMultipleBirthPaths;
    private Path varAdulterousBirthPaths;
    private Path varPartneringPaths;
    private Path varSeparationPaths;
    private Path varBirthRatioPaths;
    private Path varMaleForenamePaths;
    private Path varFemaleForenamePaths;
    private Path varMigrantMaleForenamePaths;
    private Path varMigrantFemaleForenamePaths;
    private Path varMigrantSurnamePaths;
    private Path varMigrationRatePaths;
    private Path varSurnamePaths;
    private Path varMarriagePaths;
    private Path varGeographyPaths;
    private Path varMaleOccupationPaths;
    private Path varFemaleOccupationPaths;
    private Path varMaleOccupationChangePaths;
    private Path varFemaleOccupationChangePaths;

    // ---- Run result paths ----

    // Path for summary of results for all runs among all run purposes
    private Path globalSummaryPath;

    // Path for summary of results for all runs within the run purpose
    private Path resultsSummaryPath;

    // Path for detailed results for the run
    private Path detailedResultsPath;

    // Path for the birth orders dump of a run
    private Path birthOrdersPath;

    // Path for the records of a run
    private Path recordsPath;

    // Path for the graphs of a run
    private Path graphsPath;

    // Path for the CT tables used in R analysis of a run
    private Path contingencyTablesPath;

    // Path to directory of a run (defaults to the timestamp)
    private Path runPath;

    // ---- Other configuration options ----

    // Factors
    private double setUpBR = DEFAULT_SETUP_BR;
    private double setUpDR = DEFAULT_SETUP_DR;
    private double recoveryFactor = DEFAULT_RECOVERY_FACTOR;
    private double proportionalRecoveryFactor = DEFAULT_PROPORTIONAL_RECOVERY_FACTOR;

    private boolean binomialSampling = DEFAULT_BINOMIAL_SAMPLING_FLAG;
    private boolean deterministic = DEFAULT_DETERMINISTIC_FLAG;
    private boolean outputTables = DEFAULT_OUTPUT_TABLES_FLAG;

    // Time steps
    private Period simulationTimeStep = DEFAULT_SIMULATION_TIME_STEP;
    private Period minBirthSpacing = DEFAULT_MIN_BIRTH_SPACING;
    private Period minGestationPeriod = DEFAULT_MIN_GESTATION_PERIOD;
    private Period inputWidth = DEFAULT_INPUT_WIDTH;

    // Locations
    private Path projectPath = DEFAULT_PROJECT_PATH;
    private Path summaryResultsDirPath = DEFAULT_RESULTS_SAVE_PATH;
    private Path resultsSavePath = DEFAULT_RESULTS_SAVE_PATH;
    private Path geographyFilePath = DEFAULT_GEOGRAPHY_FILE_PATH;

    private int seed = DEFAULT_SEED;
    private double overSizedGeographyFactor = DEFAULT_OVERSIZED_GEOGRAPHY_FACTOR;

    private int ctTreeStepback = DEFAULT_CT_TREE_STEPBACK;
    private double ctTreePrecision = DEFAULT_CT_TREE_PRECISION;

    private String runPurpose = DEFAULT_RUN_PURPOSE;
    private RecordFormat outputRecordFormat = DEFAULT_OUTPUT_RECORD_FORMAT;
    private ExportFormat outputGraphFormat = DEFAULT_OUTPUT_GRAPH_FORMAT;

    private LocalDateTime startTime = LocalDateTime.now();

    // Simulation period and start size
    private LocalDate tS;
    private LocalDate t0;
    private LocalDate tE;
    private Integer t0PopulationSize;

    private Map<String, Processor> processors;

    public static String formatTimeStamp(final LocalDateTime startTime) {
        return startTime.format(FORMATTER);
    }

    // Initialise configuration programmatically
    public Config(final LocalDate tS, final LocalDate t0, final LocalDate tE, final int t0PopulationSize, final Path varPath, final Path resultsDir, final String runPurpose, final Path summaryResultsDir) {
        this.tS = tS;
        this.t0 = t0;
        this.tE = tE;
        this.t0PopulationSize = t0PopulationSize;
        this.varPath = varPath;
        this.resultsSavePath = resultsDir;
        this.runPurpose = runPurpose;
        this.summaryResultsDirPath = summaryResultsDir;

        validateOptions();
        setUpFileStructure();
        configureLogging();
        initialiseVarPaths();
        setGeographyPath();
    }

    // Initialise configuration from file
    public Config(final Path pathToConfigFile) {
        configureFileProcessors();
        readConfigFile(pathToConfigFile);

        validateOptions();
        setUpFileStructure();
        configureLogging();
        initialiseVarPaths();
        setGeographyPath();
    }

    private void setGeographyPath() {
        final Iterator<Path> it = getVarGeographyPaths().iterator();
        setGeographyFilePath(it.next());

        if(it.hasNext())
            throw new UnsupportedOperationException("Only one geography file is supported for each simulation - please remove surplus files from input data structure or write more code...");
    }

    public int getCtTreeStepback() {
        return ctTreeStepback;
    }

    public Path getDetailedResultsPath() {
        return detailedResultsPath;
    }

    public Path getRecordsDirPath() {
        return recordsPath;
    }

    public Path getGraphsDirPath() {
        return graphsPath;
    }

    public Path getContingencyTablesPath() {
        return contingencyTablesPath;
    }

    public Path getGlobalSummaryPath() {
        return globalSummaryPath;
    }

    public Path getResultsSummaryPath() {
        return resultsSummaryPath;
    }

    public Path getProjectPath() {
        return projectPath;
    }

    private Path pathToLogDir(final String runPurpose, final LocalDateTime startTime, final Path resultPath) {
        return resultPath.resolve(runPurpose).resolve(formatTimeStamp(startTime)).resolve("log").resolve("trace.txt");
    }

    public Path getRunPath() {
        return runPath;
    }

    public Path getVarPath() {
        return varPath;
    }

    public DirectoryStream<Path> getVarOrderedBirthPaths() {
        return getDirectories(varOrderedBirthPaths);
    }

    public DirectoryStream<Path> getVarMaleLifetablePaths() {
        return getDirectories(varMaleLifetablePaths);
    }

    public DirectoryStream<Path> getVarMaleDeathCausesPaths() {
        return getDirectories(varMaleDeathCausesPaths);
    }

    public DirectoryStream<Path> getVarMaleOccupationPaths() {
        return getDirectories(varMaleOccupationPaths);
    }

    public DirectoryStream<Path> getVarMaleOccupationChangePaths() {
        return getDirectories(varMaleOccupationChangePaths);
    }

    public DirectoryStream<Path> getVarFemaleOccupationPaths() {
        return getDirectories(varFemaleOccupationPaths);
    }

    public DirectoryStream<Path> getVarFemaleOccupationChangePaths() {
        return getDirectories(varFemaleOccupationChangePaths);
    }

    public DirectoryStream<Path> getVarFemaleLifetablePaths() {
        return getDirectories(varFemaleLifetablePaths);
    }

    public DirectoryStream<Path> getVarFemaleDeathCausesPaths() {
        return getDirectories(varFemaleDeathCausesPaths);
    }

    public DirectoryStream<Path> getVarMultipleBirthPaths() {
        return getDirectories(varMultipleBirthPaths);
    }

    public DirectoryStream<Path> getVarAdulterousBirthPaths() {
        return getDirectories(varAdulterousBirthPaths);
    }

    public DirectoryStream<Path> getVarMarriagePaths() {
        return getDirectories(varMarriagePaths);
    }

    public DirectoryStream<Path> getVarGeographyPaths() {
        return getDirectories(varGeographyPaths);
    }

    public DirectoryStream<Path> getVarPartneringPaths() {
        return getDirectories(varPartneringPaths);
    }

    public DirectoryStream<Path> getVarSeparationPaths() {
        return getDirectories(varSeparationPaths);
    }

    public DirectoryStream<Path> getVarBirthRatioPath() {
        return getDirectories(varBirthRatioPaths);
    }

    public DirectoryStream<Path> getVarMaleForenamePath() {
        return getDirectories(varMaleForenamePaths);
    }

    public DirectoryStream<Path> getVarFemaleForenamePath() {
        return getDirectories(varFemaleForenamePaths);
    }

    public DirectoryStream<Path> getVarMigrantMaleForenamePath() { return getDirectories(varMigrantMaleForenamePaths); }

    public DirectoryStream<Path> getVarMigrantFemaleForenamePath() { return getDirectories(varMigrantFemaleForenamePaths); }

    public DirectoryStream<Path> getVarSurnamePath() {
        return getDirectories(varSurnamePaths);
    }

    public DirectoryStream<Path> getVarMigrantSurnamePath() {
        return getDirectories(varMigrantSurnamePaths);
    }

    public DirectoryStream<Path> getVarMigrationRatePath() {
        return getDirectories(varMigrationRatePaths);
    }

    public LocalDate getTS() {
        return tS;
    }

    public LocalDate getT0() {
        return t0;
    }

    public LocalDate getTE() {
        return tE;
    }

    public Period getSimulationTimeStep() {
        return simulationTimeStep;
    }

    public int getT0PopulationSize() {
        return t0PopulationSize;
    }

    public double getSetUpBR() {
        return setUpBR;
    }

    public double getSetUpDR() {
        return setUpDR;
    }

    public Path getResultsSavePath() {
        return resultsSavePath;
    }

    public Path getBirthOrdersPath() {
        return birthOrdersPath;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public String getRunPurpose() {
        return runPurpose;
    }

    public Config setRunPurpose(final String runPurpose) {

        this.runPurpose = runPurpose;
        return this;
    }

    public Period getInputWidth() {
        return inputWidth;
    }

    public boolean getBinomialSampling() {
        return binomialSampling;
    }

    public Period getMinBirthSpacing() {
        return minBirthSpacing;
    }

    public double getRecoveryFactor() {
        return recoveryFactor;
    }

    public double getProportionalRecoveryFactor() {
        return proportionalRecoveryFactor;
    }

    public RecordFormat getOutputRecordFormat() {
        return outputRecordFormat;
    }

    public ExportFormat getOutputGraphFormat() {
        return outputGraphFormat;
    }

    public boolean getOutputTables() {
        return outputTables;
    }

    public Period getMinGestationPeriod() {
        return minGestationPeriod;
    }

    public int getSeed() {
        return seed;
    }

    public boolean deterministic() {
        return deterministic;
    }

    public Config setDeterministic(final boolean deterministic) {

        this.deterministic = deterministic;
        return this;
    }

    public Config setSetupBirthRate(final double setUpBR) {

        this.setUpBR = setUpBR;
        return this;
    }

    public Config setSetupDeathRate(final double setUpDR) {

        this.setUpDR = setUpDR;
        return this;
    }

    public Config setRecoveryFactor(final double recoveryFactor) {

        this.recoveryFactor = recoveryFactor;
        return this;
    }

    public Config setProportionalRecoveryFactor(final double proportionalRecoveryFactor) {

        this.proportionalRecoveryFactor = proportionalRecoveryFactor;
        return this;
    }

    public Config setInputWidth(final Period inputWidth) {

        this.inputWidth = inputWidth;
        return this;
    }

    public Config setMinBirthSpacing(final Period minBirthSpacing) {

        this.minBirthSpacing = minBirthSpacing;
        return this;
    }

    public Config setMinGestationPeriod(final Period minGestationPeriod) {

        this.minGestationPeriod = minGestationPeriod;
        return this;
    }

    public Config setResultsSavePath(final Path resultsSavePath) {

        this.resultsSavePath = resultsSavePath;
        return this;
    }

    public Config setProjectPath(final Path projectPath) {
        this.projectPath = projectPath;
        return this;
    }

    private DirectoryStream<Path> getDirectories(final Path path) {

        try {
            return Files.newDirectoryStream(path, filter);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initialiseVarPaths() {

        final Path birthPath = varPath.resolve(birthSubFile);
        varOrderedBirthPaths = birthPath.resolve(orderedBirthSubFile);
        varMultipleBirthPaths = birthPath.resolve(multipleBirthSubFile);
        varAdulterousBirthPaths = birthPath.resolve(adulterousBirthSubFile);
        varBirthRatioPaths = birthPath.resolve(birthRatioSubFile);

        final Path deathPath = varPath.resolve(deathSubFile);
        varMaleLifetablePaths = deathPath.resolve(maleDeathSubFile).resolve(lifetableSubFile);
        varMaleDeathCausesPaths = deathPath.resolve(maleDeathSubFile).resolve(deathCauseSubFile);
        varFemaleLifetablePaths = deathPath.resolve(femaleDeathSubFile).resolve(lifetableSubFile);
        varFemaleDeathCausesPaths = deathPath.resolve(femaleDeathSubFile).resolve(deathCauseSubFile);

        final Path relationshipsPath = varPath.resolve(relationshipsSubFile);
        varPartneringPaths = relationshipsPath.resolve(partneringSubFile);
        varSeparationPaths = relationshipsPath.resolve(separationSubFile);
        varMarriagePaths = relationshipsPath.resolve(marriageSubFile);

        final Path annotationsPath = varPath.resolve(annotationsSubFile);
        varMaleForenamePaths = annotationsPath.resolve(maleForenameSubFile);
        varFemaleForenamePaths = annotationsPath.resolve(femaleForenameSubFile);

        varMigrantMaleForenamePaths = annotationsPath.resolve(maleMigrantForenameSubFile);
        varMigrantFemaleForenamePaths = annotationsPath.resolve(femaleMigrantForenameSubFile);

        varSurnamePaths = annotationsPath.resolve(surnameSubFile);
        varMigrantSurnamePaths = annotationsPath.resolve(migrantSurnameSubFile);

        varGeographyPaths = annotationsPath.resolve(geographySubFile);

        varMigrationRatePaths = annotationsPath.resolve(migrationRateSubFile);

        varMaleOccupationPaths = annotationsPath.resolve(maleOccupationSubFile);
        varFemaleOccupationPaths = annotationsPath.resolve(femaleOccupationSubFile);

        varMaleOccupationChangePaths = annotationsPath.resolve(maleOccupationChangeSubFile);
        varFemaleOccupationChangePaths = annotationsPath.resolve(femaleOccupationChangeSubFile);
    }

    public static void mkBlankFile(final Path blankFilePath) {

        try {
            createFileIfDoesNotExist(blankFilePath);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createFileIfDoesNotExist(Path path) throws IOException {
        if (!Files.exists(path)) {
            createParentDirectoryIfDoesNotExist(path);
            Files.createFile(path);
        }
    }

    public static void createParentDirectoryIfDoesNotExist(Path path) throws IOException {
        Path parent_dir = path.getParent();
        if (parent_dir != null) {
            Files.createDirectories(parent_dir);
        }
    }

    private static void mkSummaryFile(final Path summaryFilePath) {

        if (summaryFilePath.toFile().exists()) {
            return;
        }

        try {
            mkBlankFile(summaryFilePath);
            final PrintWriter write = new PrintWriter(summaryFilePath.toFile());
            write.println(SummaryRow.getSeparatedHeadings());
            write.close();

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void mkDirs(final Path path) {

        if (!Files.exists(path)) {
            new File(path.toString()).mkdirs();
        }
    }

    // Filter method to exclude dot files from data file directory streams
    private DirectoryStream.Filter<Path> filter = file -> {

        Path path = file.getFileName();
        if (path != null) {
            return !path.toString().matches("^\\..+");
        }
        throw new IOException("Failed to get Filename");
    };

    // Defines the allowed options in the config file and how to handle their values.
    private void configureFileProcessors() {

        processors = new HashMap<>();

        processors.put("var_data_files", value -> varPath = Paths.get(value));
        processors.put("results_save_location", value -> resultsSavePath = Paths.get(value));
        processors.put("summary_results_save_location", value -> summaryResultsDirPath = Paths.get(value));
        processors.put("project_location", value -> projectPath = Paths.get(value));

        processors.put("simulation_time_step", value -> simulationTimeStep = parsePeriod(value, "simulation_time_step"));
        processors.put("input_width", value -> inputWidth = parsePeriod(value, "input_width"));
        processors.put("min_birth_spacing", value -> minBirthSpacing = parsePeriod(value, "min_birth_spacing"));
        processors.put("min_gestation_period", value -> minGestationPeriod = parsePeriod(value, "min_gestation_period"));

        processors.put("tS", value -> tS = parseDate(value, "tS"));
        processors.put("t0", value -> t0 = parseDate(value, "t0"));
        processors.put("tE", value -> tE = parseDate(value, "tE"));

        processors.put("t0_pop_size", value -> t0PopulationSize = parsePositiveInteger(value, "t0_pop_size"));
        processors.put("seed", value -> seed = parseInteger(value, "seed"));
        processors.put("ct_tree_stepback", value -> ctTreeStepback = parsePositiveInteger(value, "ct_tree_stepback"));
        processors.put("ct_tree_precision", value -> ctTreePrecision = parseDouble(value, "ct_tree_precision"));

        processors.put("set_up_br", value -> setUpBR = parseDouble(value, "set_up_br"));
        processors.put("set_up_dr", value -> setUpDR = parseDouble(value, "set_up_dr"));
        processors.put("recovery_factor", value -> recoveryFactor = parseDouble(value, "recovery_factor"));
        processors.put("proportional_recovery_factor", value -> proportionalRecoveryFactor = parseDouble(value, "recovery_factor"));
        processors.put("over_sized_geography_factor", value -> overSizedGeographyFactor = parseOversizedGeographyFactor(value, "over_sized_geography_factor"));

        processors.put("binomial_sampling", value -> binomialSampling = value.equalsIgnoreCase("true"));
        processors.put("output_tables", value -> outputTables = value.equalsIgnoreCase("true"));
        processors.put("deterministic", value -> deterministic = value.equalsIgnoreCase("true"));

        processors.put("output_record_format", value -> {
            try {
                outputRecordFormat = RecordFormat.valueOf(value);
            } catch (final IllegalArgumentException e) {
                throw new IllegalArgumentException("'" + value + "' not a valid option for `output_record_format`");
            }
        });
        processors.put("output_graph_format", value -> {
            try {
                outputGraphFormat = ExportFormat.valueOf(value);
            } catch (final IllegalArgumentException e) {
                throw new IllegalArgumentException("'" + value + "' not a valid option for `output_graph_format`");
            }
        });
        processors.put("log_level", value -> logLevel = Level.parse(value));
        processors.put("run_purpose", value -> runPurpose = value);
    }

    private LocalDate parseDate(final String value, final String option) {
        try {
            return LocalDate.parse(value);
        } catch (final DateTimeParseException e) {
            throw new IllegalArgumentException("`" + option + "` must a parseable date, not '" + value + "'");
        }
    }

    private Period parsePeriod(final String value, final String option) {
        try {
            return Period.parse(value);
        } catch (final DateTimeParseException e) {
            throw new IllegalArgumentException("`" + option + "` must be a period of the format 'P<years>Y<months>M<days>D', not '" + value + "'");
        }
    }

    private int parseInteger(final String value, final String option) {
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("`" + option + "` must be an integer, not '" + value + "'");
        }
    }

    private int parsePositiveInteger(final String value, final String option) {
        final int val = parseInteger(value, option);
        if (val < 0) {
            throw new IllegalArgumentException("`" + option + "` cannot be a negative number");
        }
        return val;
    }

    private double parseDouble(final String value, final String option) {
        try {
            return Double.parseDouble(value);
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("`" + option + "` must be a floating point number, not '" + value + "'");
        }
    }

    public double parseOversizedGeographyFactor(final String value, final String option) {
        final double v = parseDouble(value, option);

        if(v < 1) {
            throw new IllegalArgumentException("`" + option + "` cannot be less than 1");
        }

        return v;
    }

    public void setOverSizedGeographyFactor(final String value) {
        overSizedGeographyFactor = parseOversizedGeographyFactor(value, "over_sized_geography_factor");
    }

    private void readConfigFile(final Path pathToConfigFile) {

        try {
            for (final String line : InputFileReader.getAllLines(pathToConfigFile)) {

                final String[] split = line.split("=");

                if (split.length < 2) {
                    throw new IllegalArgumentException("Illegal line '" + line + "' read in config file. Each line should be of the format '<option> = <value>'");
                }

                final String key = split[0].trim();

                // Join remaining equals together if any, in case they were part of the value
                final String value = String.join("=", Arrays.copyOfRange(split, 1, split.length)).trim();

                final Processor processor = processors.get(key);
                if (processor == null) {
                    throw new RuntimeException("No configuration processor defined for key: " + key);
                }
                processor.set(value);
            }
        } catch (final IOException e) {
            log.severe("error reading config: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void validateOptions() {
        // Ensure required options are set
        if (tS == null) {
            throw new IllegalArgumentException("`tS` is required");
        }
        if (t0 == null) {
            throw new IllegalArgumentException("`t0` is required");
        }
        if (tE == null) {
            throw new IllegalArgumentException("`tE` is required");
        }
        if (t0PopulationSize == null) {
            throw new IllegalArgumentException("`t0_pop_size` is required");
        }
        if (varPath == null) {
            throw new IllegalArgumentException("`var_data_files` is required");
        }

        // Ensure ordering of dates
        if (tS.isAfter(t0) ) {
            throw new IllegalArgumentException("`tS` cannot be after `t0`");
        }
        if (t0.isAfter(tE)) {
            throw new IllegalArgumentException("`t0` cannot be after `tE`");
        }

        // This allows the simulation enough time to burn in
        if (t0.getYear() - tS.getYear() < 150) {
            throw new IllegalArgumentException("`tS` must be at least 150 years before `t0`");
        }
    }

    private void setUpFileStructure() {

        globalSummaryPath = summaryResultsDirPath.resolve( "global-results-summary.csv");
        final Path purpose = resultsSavePath.resolve(runPurpose);
        resultsSummaryPath = summaryResultsDirPath.resolve(runPurpose).resolve( runPurpose + "-results-summary.csv");
        runPath = purpose.resolve(formatTimeStamp(startTime));
        detailedResultsPath = runPath.resolve("detailed-results-" + formatTimeStamp(startTime) + ".txt");
        final Path dumpPath = runPath.resolve("dump");
        birthOrdersPath = dumpPath.resolve("order.csv");
        recordsPath = runPath.resolve("records");
        graphsPath = runPath.resolve("graphs");
        contingencyTablesPath = runPath.resolve("tables");
        final Path log = runPath.resolve("log");
        final Path tracePath = log.resolve("trace.txt");

        mkDirs(resultsSavePath);
        mkDirs(purpose);
        mkDirs(runPath);
        mkDirs(dumpPath);
        mkDirs(recordsPath);
        mkDirs(graphsPath);
        mkDirs(contingencyTablesPath);
        mkDirs(log);

        mkSummaryFile(globalSummaryPath);
        mkSummaryFile(resultsSummaryPath);

        mkBlankFile(detailedResultsPath);
        mkBlankFile(birthOrdersPath);
        mkBlankFile(tracePath);
    }

    private void configureLogging() {

        try {

            final Logger globalLogger = Logger.getLogger("");

            // When running sims back to back we need to first stop writing to the old log file
            for(final Handler h : globalLogger.getHandlers()) {
                globalLogger.removeHandler(h);
            }

            final Handler handler = new FileHandler(pathToLogDir(runPurpose, startTime, resultsSavePath).toString());
            handler.setFormatter(new SimpleFormatter());

            globalLogger.addHandler(handler);
            globalLogger.setLevel(logLevel);

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path getGeographyFilePath() {
        return geographyFilePath;
    }

    public void setGeographyFilePath(final Path geographyFilePath) {
        this.geographyFilePath = geographyFilePath;
    }

    public void setOutputRecordFormat(final RecordFormat output_record_format) {
        this.outputRecordFormat = output_record_format;
    }

    public Config setSeed(final int seed) {
        this.seed = seed;
        return this;
    }

    public void setTimestep(final Period timestep) {
        this.simulationTimeStep = timestep;
    }

    public void setBinomialSampling(final boolean binomial_sampling) {
        this.binomialSampling = binomial_sampling;
    }

    public void setCTtreeStepBack(final int ct_tree_stepback) {
        this.ctTreeStepback = ct_tree_stepback;
    }

    public double getCtTreePrecision() {
        return ctTreePrecision;
    }

    public void setCTtreePrecision(final double precision) {
        this.ctTreePrecision = precision;
    }

    public double getOverSizedGeographyFactor() {
        return overSizedGeographyFactor;
    }

    private interface Processor {

        void set(String rep);
    }

    public SerializableConfig toSerialized() {
        return new SerializableConfig(
            varPath.toString(),
            varOrderedBirthPaths.toString(),
            varMaleLifetablePaths.toString(),
            varMaleDeathCausesPaths.toString(),
            varFemaleLifetablePaths.toString(),
            varFemaleDeathCausesPaths.toString(),
            varMultipleBirthPaths.toString(),
            varAdulterousBirthPaths.toString(),
            varPartneringPaths.toString(),
            varSeparationPaths.toString(),
            varBirthRatioPaths.toString(),
            varMaleForenamePaths.toString(),
            varFemaleForenamePaths.toString(),
            varMigrantMaleForenamePaths.toString(),
            varMigrantFemaleForenamePaths.toString(),
            varMigrantSurnamePaths.toString(),
            varMigrationRatePaths.toString(),
            varSurnamePaths.toString(),
            varMarriagePaths.toString(),
            varGeographyPaths.toString(),
            varMaleOccupationPaths.toString(),
            varFemaleOccupationPaths.toString(),
            varMaleOccupationChangePaths.toString(),
            varFemaleOccupationChangePaths.toString(),
            globalSummaryPath.toString(),
            resultsSummaryPath.toString(),
            detailedResultsPath.toString(),
            birthOrdersPath.toString(),
            recordsPath.toString(),
            graphsPath.toString(),
            contingencyTablesPath.toString(),
            runPath.toString(),
            setUpBR,
            setUpDR,
            recoveryFactor,
            proportionalRecoveryFactor,
            binomialSampling,
            deterministic,
            outputTables,
            simulationTimeStep,
            minBirthSpacing,
            minGestationPeriod,
            inputWidth,
            summaryResultsDirPath.toString(),
            resultsSavePath.toString(),
            geographyFilePath.toString(),
            projectPath.toString(),
            seed,
            overSizedGeographyFactor,
            ctTreeStepback,
            ctTreePrecision,
            runPurpose,
            outputRecordFormat,
            outputGraphFormat,
            startTime,
            tS,
            t0,
            tE,
            t0PopulationSize
        );
    }

    public Config(final SerializableConfig config) {
        this.varPath                          =Path.of(config.varPath);
        this.varOrderedBirthPaths             =Path.of(config.varOrderedBirthPaths);
        this.varMaleLifetablePaths            =Path.of(config.varMaleLifetablePaths);
        this.varMaleDeathCausesPaths          =Path.of(config.varMaleDeathCausesPaths);
        this.varFemaleLifetablePaths          =Path.of(config.varFemaleLifetablePaths);
        this.varFemaleDeathCausesPaths        =Path.of(config.varFemaleDeathCausesPaths);
        this.varMultipleBirthPaths            =Path.of(config.varMultipleBirthPaths);
        this.varAdulterousBirthPaths          =Path.of(config.varAdulterousBirthPaths);
        this.varPartneringPaths               =Path.of(config.varPartneringPaths);
        this.varSeparationPaths               =Path.of(config.varSeparationPaths);
        this.varBirthRatioPaths               =Path.of(config.varBirthRatioPaths);
        this.varMaleForenamePaths             =Path.of(config.varMaleForenamePaths);
        this.varFemaleForenamePaths           =Path.of(config.varFemaleForenamePaths);
        this.varMigrantMaleForenamePaths      =Path.of(config.varMigrantMaleForenamePaths);
        this.varMigrantFemaleForenamePaths    =Path.of(config.varMigrantFemaleForenamePaths);
        this.varMigrantSurnamePaths           =Path.of(config.varMigrantSurnamePaths);
        this.varMigrationRatePaths            =Path.of(config.varMigrationRatePaths);
        this.varSurnamePaths                  =Path.of(config.varSurnamePaths);
        this.varMarriagePaths                 =Path.of(config.varMarriagePaths);
        this.varGeographyPaths                =Path.of(config.varGeographyPaths);
        this.varMaleOccupationPaths           =Path.of(config.varMaleOccupationPaths);
        this.varFemaleOccupationPaths         =Path.of(config.varFemaleOccupationPaths);
        this.varMaleOccupationChangePaths     =Path.of(config.varMaleOccupationChangePaths);
        this.varFemaleOccupationChangePaths   =Path.of(config.varFemaleOccupationChangePaths);
        this.globalSummaryPath                =Path.of(config.globalSummaryPath);
        this.resultsSummaryPath               =Path.of(config.resultsSummaryPath);
        this.detailedResultsPath              =Path.of(config.detailedResultsPath);
        this.birthOrdersPath                  =Path.of(config.birthOrdersPath);
        this.recordsPath                      =Path.of(config.recordsPath);
        this.graphsPath                       =Path.of(config.graphsPath);
        this.contingencyTablesPath            =Path.of(config.contingencyTablesPath);
        this.runPath                          =Path.of(config.runPath);
        this.setUpBR                          =config.setUpBR;
        this.setUpDR                          =config.setUpDR;
        this.recoveryFactor                   =config.recoveryFactor;
        this.proportionalRecoveryFactor       =config.proportionalRecoveryFactor;
        this.binomialSampling                 =config.binomialSampling;
        this.deterministic                    =config.deterministic;
        this.outputTables                     =config.outputTables;
        this.simulationTimeStep               =config.simulationTimeStep;
        this.minBirthSpacing                  =config.minBirthSpacing;
        this.minGestationPeriod               =config.minGestationPeriod;
        this.inputWidth                       =config.inputWidth;
        this.summaryResultsDirPath            =Path.of(config.summaryResultsDirPath);
        this.resultsSavePath                  =Path.of(config.resultsSavePath);
        this.geographyFilePath                =Path.of(config.geographyFilePath);
        this.projectPath                      =Path.of(config.projectPath);
        this.seed                             =config.seed;
        this.overSizedGeographyFactor         =config.overSizedGeographyFactor;
        this.ctTreeStepback                   =config.ctTreeStepback;
        this.ctTreePrecision                  =config.ctTreePrecision;
        this.runPurpose                       =config.runPurpose;
        this.outputRecordFormat               =config.outputRecordFormat;
        this.outputGraphFormat                =config.outputGraphFormat;
        this.startTime                        =config.startTime;
        this.tS                               =config.tS;
        this.t0                               =config.t0;
        this.tE                               =config.tE;
        this.t0PopulationSize                 =config.t0PopulationSize;
    }
}
