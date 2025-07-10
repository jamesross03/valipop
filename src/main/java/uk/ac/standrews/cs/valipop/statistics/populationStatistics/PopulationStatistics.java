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
package uk.ac.standrews.cs.valipop.statistics.populationStatistics;

import uk.ac.standrews.cs.valipop.Config;
import uk.ac.standrews.cs.valipop.implementations.Randomness;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SexOption;
import uk.ac.standrews.cs.valipop.statistics.distributions.EnumeratedDistribution;
import uk.ac.standrews.cs.valipop.statistics.distributions.InconsistentWeightException;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.determinedCounts.DeterminedCount;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsKeys.*;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsTables.EventRateTables;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsTables.dataDistributions.*;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsTables.dataDistributions.selfCorrecting.SelfCorrecting2DIntegerRangeProportionalDistribution;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsTables.dataDistributions.selfCorrecting.SelfCorrectingOneDimensionDataDistribution;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsTables.dataDistributions.selfCorrecting.SelfCorrectingTwoDimensionDataDistribution;
import uk.ac.standrews.cs.valipop.utils.InputFileReader;
import uk.ac.standrews.cs.valipop.utils.InvalidInputFileException;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRange;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.time.Period;
import java.time.Year;
import java.util.Arrays;
import java.util.TreeMap;

/**
 * The PopulationStatistics holds data about the rate at which specified events occur to specified subsets of
 * members of the summative population.
 *
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class PopulationStatistics implements EventRateTables {

    private TreeMap<Year, SelfCorrectingOneDimensionDataDistribution> maleDeath;
    private TreeMap<Year, SelfCorrectingOneDimensionDataDistribution> femaleDeath;
    private TreeMap<Year, SelfCorrecting2DIntegerRangeProportionalDistribution> partnering;
    private TreeMap<Year, SelfCorrectingTwoDimensionDataDistribution> orderedBirth;
    private TreeMap<Year, SelfCorrectingProportionalDistribution<IntegerRange, Integer, Integer>> multipleBirth;
    private TreeMap<Year, SelfCorrectingOneDimensionDataDistribution> adulterousBirth;
    private TreeMap<Year, SelfCorrectingOneDimensionDataDistribution> marriage;
    private TreeMap<Year, SelfCorrectingTwoDimensionDataDistribution> separation;

    private TreeMap<Year, Double> sexRatioBirth;

    private TreeMap<Year, ValiPopEnumeratedDistribution> maleForenames;
    private TreeMap<Year, ValiPopEnumeratedDistribution> femaleForenames;
    private TreeMap<Year, ValiPopEnumeratedDistribution> surnames;

    private TreeMap<Year, ValiPopEnumeratedDistribution> migrantMaleForenames;
    private TreeMap<Year, ValiPopEnumeratedDistribution> migrantFemaleForenames;
    private TreeMap<Year, ValiPopEnumeratedDistribution> migrantSurnames;
    private TreeMap<Year, SelfCorrectingOneDimensionDataDistribution> migrationRate;

    private TreeMap<Year, AgeDependantEnumeratedDistribution> maleDeathCauses;
    private TreeMap<Year, AgeDependantEnumeratedDistribution> femaleDeathCauses;

    private Period minGestationPeriod;
    private Period minBirthSpacing;

    private TreeMap<Year, AgeDependantEnumeratedDistribution> maleOccupation;
    private TreeMap<Year, AgeDependantEnumeratedDistribution> femaleOccupation;

    private TreeMap<Year, SelfCorrecting2DEnumeratedProportionalDistribution> maleOccupationChange;
    private TreeMap<Year, SelfCorrecting2DEnumeratedProportionalDistribution> femaleOccupationChange;

    public PopulationStatistics(final Config config) {

        try {
            if (!config.deterministic()) {
                // sets a seed based on time so that it can be logged for recreation of simulation
                config.setSeed((int) System.nanoTime());
            }

            Randomness.getRandomGenerator().setSeed(config.getSeed());

            final TreeMap<Year, SelfCorrectingOneDimensionDataDistribution> maleDeath = readInSC1DDataFiles(config.getVarMaleLifetablePaths(), config);
            final TreeMap<Year, AgeDependantEnumeratedDistribution> maleDeathCauses = readInAgeDependantEnumeratedDistributionDataFiles(config.getVarMaleDeathCausesPaths(), config);
            final TreeMap<Year, SelfCorrectingOneDimensionDataDistribution> femaleDeath = readInSC1DDataFiles(config.getVarFemaleLifetablePaths(), config);
            final TreeMap<Year, AgeDependantEnumeratedDistribution> femaleDeathCauses = readInAgeDependantEnumeratedDistributionDataFiles(config.getVarFemaleDeathCausesPaths(), config);
            final TreeMap<Year, SelfCorrecting2DIntegerRangeProportionalDistribution> partnering = readInAgeAndProportionalStatsInputFiles(config.getVarPartneringPaths(), config);
            final TreeMap<Year, SelfCorrectingTwoDimensionDataDistribution> orderedBirth = readInSC2DDataFiles(config.getVarOrderedBirthPaths(), config);
            final TreeMap<Year, SelfCorrectingProportionalDistribution<IntegerRange, Integer, Integer>> multipleBirth = readInAndAdaptAgeAndProportionalStatsInputFiles(config.getVarMultipleBirthPaths(), config);
            final TreeMap<Year, SelfCorrectingOneDimensionDataDistribution> adulterousBirth = readInSC1DDataFiles(config.getVarAdulterousBirthPaths(), config);
            final TreeMap<Year, SelfCorrectingOneDimensionDataDistribution> marriage = readInSC1DDataFiles(config.getVarMarriagePaths(), config);
            final TreeMap<Year, SelfCorrectingTwoDimensionDataDistribution> separation = readInSC2DDataFiles(config.getVarSeparationPaths(), config);
            final TreeMap<Year, Double> sexRatioBirth = readInSingleInputDataFile(config.getVarBirthRatioPath());
            final TreeMap<Year, ValiPopEnumeratedDistribution> maleForename = readInNamesDataFiles(config.getVarMaleForenamePath(), config);
            final TreeMap<Year, ValiPopEnumeratedDistribution> femaleForename = readInNamesDataFiles(config.getVarFemaleForenamePath(), config);
            final TreeMap<Year, ValiPopEnumeratedDistribution> surname = readInNamesDataFiles(config.getVarSurnamePath(), config);
            final TreeMap<Year, ValiPopEnumeratedDistribution> migrantMaleForename = readInNamesDataFiles(config.getVarMigrantMaleForenamePath(), config);
            final TreeMap<Year, ValiPopEnumeratedDistribution> migrantFemaleForename = readInNamesDataFiles(config.getVarMigrantFemaleForenamePath(), config);
            final TreeMap<Year, ValiPopEnumeratedDistribution> migrantSurname = readInNamesDataFiles(config.getVarMigrantSurnamePath(), config);
            final TreeMap<Year, SelfCorrectingOneDimensionDataDistribution> migrationRate = readInSC1DDataFiles(config.getVarMigrationRatePath(), config);

            final TreeMap<Year, AgeDependantEnumeratedDistribution> maleOccupation = readInAgeDependantEnumeratedDistributionDataFiles(config.getVarMaleOccupationPaths(), config);
            final TreeMap<Year, SelfCorrecting2DEnumeratedProportionalDistribution> maleOccupationChange = readInStringAndProportionalStatsInputFiles(config.getVarMaleOccupationChangePaths(), config);

            final TreeMap<Year, AgeDependantEnumeratedDistribution> femaleOccupation = readInAgeDependantEnumeratedDistributionDataFiles(config.getVarFemaleOccupationPaths(), config);
            final TreeMap<Year, SelfCorrecting2DEnumeratedProportionalDistribution> femaleOccupationChange = readInStringAndProportionalStatsInputFiles(config.getVarFemaleOccupationChangePaths(), config);

            init(maleDeath, maleDeathCauses, femaleDeath, femaleDeathCauses, partnering, orderedBirth, multipleBirth, adulterousBirth,
                    marriage, separation, sexRatioBirth, maleForename, femaleForename, surname,
                    migrantMaleForename, migrantFemaleForename, migrantSurname, migrationRate,
                    maleOccupation, femaleOccupation, maleOccupationChange, femaleOccupationChange,
                    config.getMinBirthSpacing(), config.getMinGestationPeriod());

        } catch (final IOException | InvalidInputFileException | InconsistentWeightException e) {
            throw new RuntimeException(e);
        }
    }

    private void init(final TreeMap<Year, SelfCorrectingOneDimensionDataDistribution> maleDeath, final TreeMap<Year, AgeDependantEnumeratedDistribution> maleDeathCauses, final TreeMap<Year, SelfCorrectingOneDimensionDataDistribution> femaleDeath,
                      final TreeMap<Year, AgeDependantEnumeratedDistribution> femaleDeathCauses, final TreeMap<Year, SelfCorrecting2DIntegerRangeProportionalDistribution> partnering, final TreeMap<Year, SelfCorrectingTwoDimensionDataDistribution> orderedBirth,
                      final TreeMap<Year, SelfCorrectingProportionalDistribution<IntegerRange, Integer, Integer>> multipleBirth, final TreeMap<Year, SelfCorrectingOneDimensionDataDistribution> adulterousBirth, final TreeMap<Year, SelfCorrectingOneDimensionDataDistribution> marriage,
                      final TreeMap<Year, SelfCorrectingTwoDimensionDataDistribution> separation, final TreeMap<Year, Double> sexRatioBirths, final TreeMap<Year, ValiPopEnumeratedDistribution> maleForename, final TreeMap<Year, ValiPopEnumeratedDistribution> femaleForename,
                      final TreeMap<Year, ValiPopEnumeratedDistribution> surname, final TreeMap<Year, ValiPopEnumeratedDistribution> migrantMaleForenames,
                      final TreeMap<Year, ValiPopEnumeratedDistribution> migrantFemaleForenames, final TreeMap<Year, ValiPopEnumeratedDistribution> migrantSurname, final TreeMap<Year, SelfCorrectingOneDimensionDataDistribution> migrationRate,
                      final TreeMap<Year, AgeDependantEnumeratedDistribution> maleOccupation, final TreeMap<Year, AgeDependantEnumeratedDistribution> femaleOccupation,
                      final TreeMap<Year, SelfCorrecting2DEnumeratedProportionalDistribution> maleOccupationChange, final TreeMap<Year, SelfCorrecting2DEnumeratedProportionalDistribution> femaleOccupationChange,
                      final Period minBirthSpacing, final Period minGestationPeriod) {

        this.maleDeath = maleDeath;
        this.maleDeathCauses = maleDeathCauses;
        this.femaleDeath = femaleDeath;
        this.femaleDeathCauses = femaleDeathCauses;
        this.partnering = partnering;
        this.orderedBirth = orderedBirth;
        this.multipleBirth = multipleBirth;
        this.adulterousBirth = adulterousBirth;
        this.marriage = marriage;
        this.separation = separation;
        this.sexRatioBirth = sexRatioBirths;

        this.maleForenames = maleForename;
        this.femaleForenames = femaleForename;
        this.surnames = surname;

        this.migrantMaleForenames = migrantMaleForenames;
        this.migrantFemaleForenames = migrantFemaleForenames;
        this.migrantSurnames = migrantSurname;

        this.migrationRate = migrationRate;

        this.minBirthSpacing = minBirthSpacing;
        this.minGestationPeriod = minGestationPeriod;

        this.maleOccupation = maleOccupation;
        this.femaleOccupation = femaleOccupation;

        this.maleOccupationChange = maleOccupationChange;
        this.femaleOccupationChange = femaleOccupationChange;
    }

    /*
    -------------------- EventRateTables interface methods --------------------
     */

    public DeterminedCount<?,?,?,?> getDeterminedCount(final StatsKey<?, ?> key, final Config config) {

        if (key instanceof final DeathStatsKey k)
            return getDeathRates(k.getYear(), k.getSex()).determineCount(k, config, Randomness.getRandomGenerator());

        if (key instanceof final BirthStatsKey k)
            return getOrderedBirthRates(k.getYear()).determineCount(k, config, Randomness.getRandomGenerator());

        if (key instanceof final MultipleBirthStatsKey k)
            return getMultipleBirthRates(k.getYear()).determineCount(k, config, Randomness.getRandomGenerator());

        if (key instanceof final AdulterousBirthStatsKey k)
            return getAdulterousBirthRates(k.getYear()).determineCount(k, config, Randomness.getRandomGenerator());

        if (key instanceof final MarriageStatsKey k)
            return getMarriageRates(k.getYear()).determineCount(k, config, Randomness.getRandomGenerator());

        if (key instanceof final SeparationStatsKey k)
            return getSeparationByChildCountRates(k.getYear()).determineCount(k, config, Randomness.getRandomGenerator());

        if (key instanceof final PartneringStatsKey k)
            return getPartneringProportions(k.getYear()).determineCount(k, config, Randomness.getRandomGenerator());

        if (key instanceof final OccupationChangeStatsKey k)
            return getOccupationChangeProportions(k.getYear(), k.getSex()).determineCount(k, config, Randomness.getRandomGenerator());

        throw new Error("Key based access not implemented for key class: " + key.getClass().toGenericString());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void returnAchievedCount(final DeterminedCount achievedCount) {

        if (achievedCount.getKey() instanceof final DeathStatsKey k) {
            getDeathRates(k.getYear(), k.getSex()).returnAchievedCount(achievedCount, Randomness.getRandomGenerator());
            return;
        }

        if (achievedCount.getKey() instanceof final BirthStatsKey k) {
            getOrderedBirthRates(k.getYear()).returnAchievedCount(achievedCount, Randomness.getRandomGenerator());
            return;
        }

        if (achievedCount.getKey() instanceof final MultipleBirthStatsKey k) {
            getMultipleBirthRates(k.getYear()).returnAchievedCount(achievedCount, Randomness.getRandomGenerator());
            return;
        }

        if (achievedCount.getKey() instanceof final AdulterousBirthStatsKey k) {
            getAdulterousBirthRates(k.getYear()).returnAchievedCount(achievedCount, Randomness.getRandomGenerator());
            return;
        }

        if (achievedCount.getKey() instanceof final MarriageStatsKey k) {
            getMarriageRates(k.getYear()).returnAchievedCount(achievedCount, Randomness.getRandomGenerator());
            return;
        }

        if (achievedCount.getKey() instanceof final SeparationStatsKey k) {
            getSeparationByChildCountRates(k.getYear()).returnAchievedCount(achievedCount, Randomness.getRandomGenerator());
            return;
        }

        if (achievedCount.getKey() instanceof final PartneringStatsKey k) {
            getPartneringProportions(k.getYear()).returnAchievedCount(achievedCount, Randomness.getRandomGenerator());
            return;
        }

        if (achievedCount.getKey() instanceof final OccupationChangeStatsKey k) {
            getOccupationChangeProportions(k.getYear(), k.getSex()).returnAchievedCount(achievedCount, Randomness.getRandomGenerator());
            return;
        }

        throw new Error("Key based access not implemented for key class: " + achievedCount.getKey().getClass().toGenericString());
    }

    @Override
    public SelfCorrectingOneDimensionDataDistribution getDeathRates(final Year year, final SexOption sex) {

        if (sex == SexOption.MALE)
            return maleDeath.get(getNearestYearInMap(year, maleDeath));
        else
            return femaleDeath.get(getNearestYearInMap(year, femaleDeath));
    }

    @Override
    public EnumeratedDistribution getDeathCauseRates(final Year year, final SexOption sex, final int age) {

        if (sex == SexOption.MALE)
            return maleDeathCauses.get(getNearestYearInMap(year, maleDeathCauses)).getDistributionForAge(age);
        else
            return femaleDeathCauses.get(getNearestYearInMap(year, femaleDeathCauses)).getDistributionForAge(age);
    }

    @Override
    public SelfCorrecting2DIntegerRangeProportionalDistribution getPartneringProportions(final Year year) {
        return partnering.get(getNearestYearInMap(year, partnering));
    }

    @Override
    public SelfCorrecting2DEnumeratedProportionalDistribution getOccupationChangeProportions(final Year year, final SexOption sex) {

        if (sex == SexOption.MALE)
            return maleOccupationChange.get(getNearestYearInMap(year, maleOccupationChange));
        else
            return femaleOccupationChange.get(getNearestYearInMap(year, femaleOccupationChange));
    }

    @Override
    public SelfCorrectingOneDimensionDataDistribution getAdulterousBirthRates(final Year year) {
        return adulterousBirth.get(getNearestYearInMap(year, adulterousBirth));
    }

    @Override
    public SelfCorrectingOneDimensionDataDistribution getMarriageRates(final Year year) {
        return marriage.get(getNearestYearInMap(year, marriage));
    }

    @Override
    public SelfCorrectingTwoDimensionDataDistribution getOrderedBirthRates(final Year year) {
        return orderedBirth.get(getNearestYearInMap(year, orderedBirth));
    }

    @Override
    public SelfCorrectingProportionalDistribution<IntegerRange, Integer, Integer> getMultipleBirthRates(final Year year) {
        return multipleBirth.get(getNearestYearInMap(year, multipleBirth));
    }

    @Override
    public SelfCorrectingTwoDimensionDataDistribution getSeparationByChildCountRates(final Year year) {
        return separation.get(getNearestYearInMap(year, separation));
    }

    @Override
    public EnumeratedDistribution getForenameDistribution(final Year year, final SexOption sex) {

        if (sex == SexOption.MALE)
            return maleForenames.get(getNearestYearInMap(year, maleForenames));
        else
            return femaleForenames.get(getNearestYearInMap(year, femaleForenames));
    }

    @Override
    public EnumeratedDistribution getMigrantForenameDistribution(final Year year, final SexOption sex) {

        if (sex == SexOption.MALE)
            return migrantMaleForenames.get(getNearestYearInMap(year, migrantMaleForenames));
        else
            return migrantFemaleForenames.get(getNearestYearInMap(year, migrantFemaleForenames));
    }

    @Override
    public EnumeratedDistribution getSurnameDistribution(final Year year) {
        return surnames.get(getNearestYearInMap(year, surnames));
    }

    @Override
    public EnumeratedDistribution getMigrantSurnameDistribution(final Year year) {
        return migrantSurnames.get(getNearestYearInMap(year, migrantSurnames));
    }

    @Override
    public AgeDependantEnumeratedDistribution getOccupation(final Year year, final SexOption sex) {

        if (sex == SexOption.MALE)
            return maleOccupation.get(getNearestYearInMap(year, maleOccupation));
        else
            return femaleOccupation.get(getNearestYearInMap(year, femaleOccupation));
    }

    @Override
    public SelfCorrectingOneDimensionDataDistribution getMigrationRateDistribution(final Year year) {
        return migrationRate.get(getNearestYearInMap(year, migrationRate));
    }

    @Override
    public double getMaleProportionOfBirths(final Year onDate) {
        return sexRatioBirth.get(getNearestYearInMap(onDate, sexRatioBirth));
    }

    private static Year getNearestYearInMap(final Year year, final TreeMap<Year, ?> map) {

        final Year ceiling = map.ceilingKey(year);
        final Year floor = map.floorKey(year);

        if (ceiling == null) return floor;
        if (floor == null) return ceiling;

        final int yearInt = year.getValue();

        if (ceiling.getValue() - yearInt > yearInt - floor.getValue())
            return floor;
        else
            return ceiling;
    }

    public Period getMinBirthSpacing() {
        return minBirthSpacing;
    }

    public Period getMinGestationPeriod() {
        return minGestationPeriod;
    }

    private static TreeMap<Year, Double> readInSingleInputDataFile(final DirectoryStream<Path> paths) throws IOException, InvalidInputFileException {

        int c = 0;

        TreeMap<Year, Double> data = new TreeMap<>();

        for (final Path p : paths) {
            if (c == 1) {
                throw new RuntimeException("Too many sex ratio files - there should only be one - remove any additional files from the ratio_birth directory");
            }

            data = InputFileReader.readInSingleInputFile(p);
            c++;
        }

        // TODO shouldn't be hard-wired

        if (data.isEmpty()) {
            data.put(Year.of(1600), 0.5);
        }

        paths.close();
        return data;
    }

    private static TreeMap<Year, SelfCorrectingOneDimensionDataDistribution> readInSC1DDataFiles(final DirectoryStream<Path> paths, final Config config) throws IOException, InvalidInputFileException {

        final TreeMap<Year, SelfCorrectingOneDimensionDataDistribution> data = new WriteOnceTreeMap<>();

        for (final Path path : paths) {
            final SelfCorrectingOneDimensionDataDistribution tempData = InputFileReader.readInSC1DDataFile(path, config, Randomness.getRandomGenerator());
            data.put(tempData.getYear(), tempData);
        }

        paths.close();
        return insertDistributionsToMeetInputWidth(config, data);
    }

    private static TreeMap<Year, ValiPopEnumeratedDistribution> readInNamesDataFiles(final DirectoryStream<Path> paths, final Config config) throws IOException, InvalidInputFileException, InconsistentWeightException {

        final TreeMap<Year, ValiPopEnumeratedDistribution> data = new WriteOnceTreeMap<>();

        for (final Path path : paths) {
            final ValiPopEnumeratedDistribution tempData = InputFileReader.readInNameDataFile(path, Randomness.getRandomGenerator());
            data.put(tempData.getYear(), tempData);
        }

        paths.close();
        return insertDistributionsToMeetInputWidth(config, data);
    }

    private static TreeMap<Year, AgeDependantEnumeratedDistribution> readInAgeDependantEnumeratedDistributionDataFiles(final DirectoryStream<Path> paths, final Config config) throws IOException, InvalidInputFileException, InconsistentWeightException {

        final TreeMap<Year, AgeDependantEnumeratedDistribution> data = new WriteOnceTreeMap<>();

        for (final Path path : paths) {
            final AgeDependantEnumeratedDistribution tempData = InputFileReader.readInDeathCauseDataFile(path, Randomness.getRandomGenerator());
            data.put(tempData.getYear(), tempData);
        }

        paths.close();
        return insertDistributionsToMeetInputWidth(config, data);
    }

    private static TreeMap<Year, SelfCorrectingTwoDimensionDataDistribution> readInSC2DDataFiles(final DirectoryStream<Path> paths, final Config config) throws IOException, InvalidInputFileException {

        final TreeMap<Year, SelfCorrectingTwoDimensionDataDistribution> data = new WriteOnceTreeMap<>();

        for (final Path path : paths) {

            final SelfCorrectingTwoDimensionDataDistribution tempData = InputFileReader.readInSC2DDataFile(path, config, Randomness.getRandomGenerator());
            data.put(tempData.getYear(), tempData);
        }

        paths.close();
        return insertDistributionsToMeetInputWidth(config, data);
    }

    private static TreeMap<Year, SelfCorrecting2DIntegerRangeProportionalDistribution> readInAgeAndProportionalStatsInputFiles(final DirectoryStream<Path> paths, final Config config) throws IOException, InvalidInputFileException {

        final TreeMap<Year, SelfCorrecting2DIntegerRangeProportionalDistribution> data = new WriteOnceTreeMap<>();

        for (final Path path : paths) {
            final SelfCorrecting2DIntegerRangeProportionalDistribution tempData = InputFileReader.readInAgeAndProportionalStatsInput(path, Randomness.getRandomGenerator());
            data.put(tempData.getYear(), tempData);
        }

        paths.close();
        return insertDistributionsToMeetInputWidth(config, data);
    }

    private static TreeMap<Year, SelfCorrecting2DEnumeratedProportionalDistribution> readInStringAndProportionalStatsInputFiles(final DirectoryStream<Path> paths, final Config config) throws IOException, InvalidInputFileException {

        final TreeMap<Year, SelfCorrecting2DEnumeratedProportionalDistribution> data = new WriteOnceTreeMap<>();

        for (final Path path : paths) {
            final SelfCorrecting2DEnumeratedProportionalDistribution tempData = InputFileReader.readInStringAndProportionalStatsInput(path, Randomness.getRandomGenerator());
            data.put(tempData.getYear(), tempData);
        }

        paths.close();
        return insertDistributionsToMeetInputWidth(config, data);
    }

    private static TreeMap<Year, SelfCorrectingProportionalDistribution<IntegerRange, Integer, Integer>> readInAndAdaptAgeAndProportionalStatsInputFiles(final DirectoryStream<Path> paths, final Config config) throws IOException, InvalidInputFileException {

        final TreeMap<Year, SelfCorrectingProportionalDistribution<IntegerRange, Integer, Integer>> data = new WriteOnceTreeMap<>();

        for (final Path path : paths) {
            final SelfCorrectingProportionalDistribution<IntegerRange, Integer, Integer> tempData = InputFileReader.readInAndAdaptAgeAndProportionalStatsInput(path, Randomness.getRandomGenerator());
            data.put(tempData.getYear(), tempData);
        }

        paths.close();
        return insertDistributionsToMeetInputWidth(config, data);
    }

    private static <T extends InputMetaData<?>> TreeMap<Year, T> insertDistributionsToMeetInputWidth(final Config config, final TreeMap<Year, T> inputs) {

        final Period inputWidth = config.getInputWidth();

        final int diff = config.getT0().getYear() - config.getTS().getYear();
        final int stepBack = (int) (inputWidth.getYears() * Math.ceil(diff / (double) inputWidth.getYears()));

        Year prevInputDate = Year.of(config.getT0().minus(Period.ofYears(stepBack)).getYear());

        int c = 1;

        final Year[] years = inputs.keySet().toArray(new Year[0]);
        Arrays.sort(years);

        if (years.length == 0) {
            return inputs;
        }

        Year curDate;
        while (true) {

            curDate = prevInputDate.plus(Period.ofYears(inputWidth.getYears() * c));
            if (!curDate.isBefore(years[0])) break;
            inputs.put(curDate, inputs.get(years[0]));
            c++;
        }

        prevInputDate = years[0];

        for (final Year curInputDate : years) {

            while (true) {
                curDate = prevInputDate.plus(Period.ofYears(inputWidth.getYears() * c));
                if (curDate.isAfter(curInputDate)) break;
                final Year duplicateFrom = getNearestDate(curDate, prevInputDate, curInputDate);
                inputs.put(curDate, inputs.get(duplicateFrom));
                c++;
            }

            c = 1;
            prevInputDate = curInputDate;
        }

        while (true) {
            curDate = prevInputDate.plus(Period.ofYears(inputWidth.getYears() * c));
            if (curDate.isAfter(Year.of(config.getTE().getYear()))) break;
            c++;
        }

        return inputs;
    }

    private static Year getNearestDate(final Year referenceDate, final Year option1, final Year option2) {

        final int refTo1 = Math.abs(referenceDate.getValue() - option1.getValue());
        final int refTo2 = Math.abs(referenceDate.getValue() - option2.getValue());

        return (refTo1 < refTo2) ? option1 : option2;
    }

    private static class WriteOnceTreeMap<K, V> extends TreeMap<K, V> {

        public V put(final K key, final V value) {
            if (containsKey(key)) throw new RuntimeException("Key " + key + " already exists");
            return super.put(key, value);
        }
    }
}
