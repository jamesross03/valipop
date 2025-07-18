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

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.ac.standrews.cs.valipop.Config;
import uk.ac.standrews.cs.valipop.export.ExportFormat;
import uk.ac.standrews.cs.valipop.export.IPopulationWriter;
import uk.ac.standrews.cs.valipop.export.PopulationConverter;
import uk.ac.standrews.cs.valipop.export.gedcom.GEDCOMPopulationWriter;
import uk.ac.standrews.cs.valipop.export.geojson.GeojsonPopulationWriter;
import uk.ac.standrews.cs.valipop.export.graphviz.GraphvizPopulationWriter;
import uk.ac.standrews.cs.valipop.simulationEntities.*;
import uk.ac.standrews.cs.valipop.simulationEntities.dataStructure.*;
import uk.ac.standrews.cs.valipop.statistics.analysis.populationAnalytics.AnalyticsRunner;
import uk.ac.standrews.cs.valipop.statistics.analysis.simulationSummaryLogging.SummaryRow;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.ContingencyTableFactory;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SexOption;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.PopulationStatistics;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.determinedCounts.DeterminedCount;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.determinedCounts.MultipleDeterminedCountByIR;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.determinedCounts.SingleDeterminedCount;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsKeys.*;
import uk.ac.standrews.cs.valipop.utils.CollectionUtils;
import uk.ac.standrews.cs.valipop.utils.ProgramTimer;
import uk.ac.standrews.cs.valipop.utils.addressLookup.Address;
import uk.ac.standrews.cs.valipop.utils.addressLookup.Area;
import uk.ac.standrews.cs.valipop.utils.addressLookup.DistanceSelector;
import uk.ac.standrews.cs.valipop.utils.addressLookup.Geography;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.RecordFormat;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.RecordGenerationFactory;
import uk.ac.standrews.cs.valipop.utils.specialTypes.dates.DateSelector;
import uk.ac.standrews.cs.valipop.utils.specialTypes.dates.DeathDateSelector;
import uk.ac.standrews.cs.valipop.utils.specialTypes.dates.MarriageDateSelector;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Period;
import java.time.Year;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static uk.ac.standrews.cs.valipop.simulationEntities.PopulationNavigation.*;
import static uk.ac.standrews.cs.valipop.utils.specialTypes.dates.DateUtils.divideYieldingDouble;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class OBDModel {

    // TODO use more informative class name

    private static final int MINIMUM_POPULATION_SIZE = 100;
    private static final int EARLIEST_AGE_OF_MARRIAGE = 16;
    private static final int MAX_ATTEMPTS = 1;

    // TODO enforce maximum age.
    public static final int MAXIMUM_AGE_AT_DEATH = 110;

    public static Logger log;

    static {
        log = Logger.getLogger(OBDModel.class.getName());
        log.setLevel(Level.INFO);
    }

    private final Geography geography;

    private final Config config;
    private SummaryRow summary;
    private final PopulationStatistics desiredStatistics;
    private final Population population;
    private final LocalDate endOfInitPeriod;
    private final Collection<IPerson> partnersToSeparate;

    private final DeathDateSelector deathDateSelector;
    private final DateSelector marriageDateSelector;

    private final DistanceSelector moveDistanceSelector;

    private LocalDate currentDate;
    private ProgramTimer simTimer;
    private int currentHypotheticalPopulationSize;

    private int birthsCount = 0;
    private int deathCount = 0;

    private final PersonFactory personFactory;
    private final BalancedMigrationModel migrationModel;
    private final OccupationChangeModel occupationChangeModel;

    public OBDModel(final Config config) {

        try {
            this.config = config;

            currentDate = config.getTS();

            partnersToSeparate = new HashSet<>();
            population = new Population(config);
            desiredStatistics = new PopulationStatistics(config);

            geography = new Geography(readAreaList(config), Randomness.getRandomGenerator(), config.getOverSizedGeographyFactor());

            currentHypotheticalPopulationSize = calculateStartingPopulationSize();

            deathDateSelector = new DeathDateSelector(Randomness.getRandomGenerator());
            marriageDateSelector = new MarriageDateSelector(Randomness.getRandomGenerator());
            moveDistanceSelector = new DistanceSelector(Randomness.getRandomGenerator());

            personFactory = new PersonFactory(population, desiredStatistics, config.getSimulationTimeStep(), Randomness.getRandomGenerator());
            migrationModel = new BalancedMigrationModel(population, Randomness.getRandomGenerator(), geography, personFactory, desiredStatistics);
            occupationChangeModel = new OccupationChangeModel(population, desiredStatistics, config);

            log.info("Random seed: " + config.getSeed());
            log.info("Population seed size: " + config.getT0PopulationSize());
            log.info("Initial hypothetical population size set: " + currentHypotheticalPopulationSize);

            // End of init period is the greatest age specified in the ordered birth rates (take min bound if max bound is unset)
            final Period timeStep = Period.ofYears(desiredStatistics.getOrderedBirthRates(Year.of(currentDate.getYear())).getLargestLabel().getValue());
            endOfInitPeriod = currentDate.plus(timeStep);

            log.info("End of Initialisation Period set: " + endOfInitPeriod);

            try {
                summary = new SummaryRow(config, JobQueueRunner.execCmd("git rev-parse HEAD").trim(), JobQueueRunner.execCmd("hostname").trim());
            } catch (final IOException e) {
                summary = new SummaryRow(config, "no git install to get version number from", JobQueueRunner.execCmd("hostname").trim());
            }

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Area> readAreaList(final Config config) throws IOException {

        final ObjectMapper objectMapper = new ObjectMapper();
        return Arrays.stream(objectMapper.readValue(new File(config.getGeographyFilePath().toString()), Area[].class)).toList();
    }

    public void runSimulation() {

        for (int countAttempts = 0; countAttempts < MAX_ATTEMPTS; countAttempts++) {
            try {
                simTimer = new ProgramTimer();
                runSimulationAttempt();
                break;

            } catch (final InsufficientNumberOfPeopleException e) {
                resetSimulation(simTimer);
            }
        }

        recordFinalSummary();
    }

    public Population getPopulation() {
        return population;
    }

    public PopulationStatistics getDesiredPopulationStatistics() {
        return desiredStatistics;
    }

    public SummaryRow getSummaryRow() {
        return summary;
    }

    public void analyseAndOutputPopulation(final boolean outputSummaryRow) {

        final ProgramTimer recordTimer = new ProgramTimer();

        try (final PrintStream resultsOutput = new PrintStream(config.getDetailedResultsPath().toFile(), StandardCharsets.UTF_8)) {

            if (config.shouldGenerateContingencyTables())
                ContingencyTableFactory.generateContingencyTables(population.getPeople(), desiredStatistics, config, summary);

            if (config.getOutputRecordFormat() != RecordFormat.NONE)
                RecordGenerationFactory.outputRecords(config.getOutputRecordFormat(), config.getRecordsDirPath(), population.getPeople(), config.getT0());

            if (config.getOutputGraphFormat() != ExportFormat.NONE)
                outputToGraph(config.getOutputGraphFormat(), population.getPeople(), config.getGraphsDirPath());

            summary.setRecordsRunTime(recordTimer.getRunTimeSeconds());
            AnalyticsRunner.runAnalytics(population.getPeople(config.getT0(), config.getTE(), Period.ofYears(MAXIMUM_AGE_AT_DEATH)), resultsOutput);

        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        MemoryUsageAnalysis.log();
        summary.setMaxMemoryUsage(MemoryUsageAnalysis.getMaxSimUsage());
        MemoryUsageAnalysis.reset();

        if (outputSummaryRow)
            summary.outputSummaryRowToFile();

        log.info("OBDModel --- Output complete");
    }

    private static void outputToGraph(final ExportFormat type, final IPersonCollection people, final Path outputDir) throws Exception {

        final IPopulationWriter populationWriter;
        switch (type) {
            case GEDCOM:
                populationWriter = new GEDCOMPopulationWriter(outputDir.resolve("graph.ged"));
                break;
            case GRAPHVIZ:
                populationWriter = new GraphvizPopulationWriter(people, outputDir.resolve("graph.dot"));
                break;
            case GEOJSON:
                populationWriter = new GeojsonPopulationWriter(outputDir.resolve("graph.geojson"));
                break;
            default:
                return;
        }

        try (final PopulationConverter converter = new PopulationConverter(people, populationWriter)) {
            converter.convert();
        }
    }

    private void runSimulationAttempt() {

        initialisePopulation();
        simulatePopulationUntilStart();
        simulatePopulationUntilEnd();

        logResults();
        recordSummary();

        closeLogFile();
    }

    private static void closeLogFile() {

        for (final Handler h : log.getHandlers())
            h.close();

        LogManager.getLogManager().reset();

        log = Logger.getLogger(OBDModel.class.getName());
        log.setLevel(Level.INFO);
    }

    private void finalisePartnerships() {

        for (final IPerson person : population.getPeople())
            for (final IPartnership partnership : person.getPartnerships())
                if (!partnership.isFinalised())
                    handleSeparationMoves(partnership, person);
    }

    private void countBirthsAndDeaths(final int births, final int deaths) {

        birthsCount += births;
        deathCount += deaths;
    }

    private void logTimeStep(final int numberBorn, final int shortFallInBirths, final int numberDying) {

        MemoryUsageAnalysis.log();

        final String logEntry = currentDate + "\t" + MemoryUsageAnalysis.getMaxSimUsage() / 1e6 + "MB\t" + numberBorn + "\t" +
            shortFallInBirths + "\t" +
            numberDying + "\t" +
            population.getLivingPeople().getNumberOfPeople() + "\t" +
            population.getDeadPeople().getNumberOfPeople();

        log.info(logEntry);
    }


    // Progress the simulation until initialisation is finished
    private void initialisePopulation() throws InsufficientNumberOfPeopleException {

        while (!currentDate.isAfter(endOfInitPeriod)) {

            final int numberBorn = createBirths();

            final int shortFallInBirths = adjustPopulationNumbers(numberBorn);

            final int maleDeaths = createDeaths(SexOption.MALE);
            final int femaleDeaths = createDeaths(SexOption.FEMALE);

            final int numberDying = maleDeaths + femaleDeaths;

            migrationModel.performMigration(currentDate, this);
            occupationChangeModel.performOccupationChange(currentDate);

            logTimeStep(numberBorn, shortFallInBirths, numberDying);
            countBirthsAndDeaths(numberBorn, numberDying);

            advanceSimulationTime();
        }

        if (populationTooSmall()) {
            cleanUpAfterUnsuccessfulAttempt();
            throw new InsufficientNumberOfPeopleException("Seed size likely too small");
        }
    }

    private void simulatePopulationUntilStart() {

        while (currentDate.isBefore(config.getT0())) {

            final int numberBorn = createBirths();
            final int numberDying = createDeaths(SexOption.MALE) + createDeaths(SexOption.FEMALE);

            migrationModel.performMigration(currentDate, this);
            occupationChangeModel.performOccupationChange(currentDate);

            logTimeStep(numberBorn, 0, numberDying);
            countBirthsAndDeaths(numberBorn, numberDying);

            advanceSimulationTime();
        }

        summary.setStartPop(population.getLivingPeople().getNumberOfPeople());
    }

    private void simulatePopulationUntilEnd() {

        while (!currentDate.isAfter(config.getTE())) {

            final int numberBorn = createBirths();
            final int numberDying = createDeaths(SexOption.MALE) + createDeaths(SexOption.FEMALE);

            migrationModel.performMigration(currentDate, this);
            occupationChangeModel.performOccupationChange(currentDate);

            logTimeStep(numberBorn, 0, numberDying);
            countBirthsAndDeaths(numberBorn, numberDying);

            advanceSimulationTime();

            population.getPopulationCounts().updateMaxPopulation(population.getLivingPeople().getNumberOfPeople());
        }

        finalisePartnerships();
    }

    private void cleanUpAfterUnsuccessfulAttempt() {

        summary.setCompleted(false);

        logResults();

        summary.setEndPop(population.getLivingPeople().getNumberOfPeople());
        summary.setPeakPop(population.getPopulationCounts().getPeakPopulationSize());

        closeLogFile();
    }

    private int calculateStartingPopulationSize() {

        // Performs compound growth in reverse to work backwards from the target population to the
        return (int) (config.getT0PopulationSize() / StrictMath.pow(config.getSetUpBR() - config.getSetUpDR() + 1, Period.between(config.getTS(), config.getT0()).getYears()));
    }

    private void advanceSimulationTime() {

        currentDate = currentDate.plus(config.getSimulationTimeStep());
    }

    private void resetSimulation(final ProgramTimer simTimer) {

        summary.setCompleted(false);
        summary.setSimRunTime(simTimer.getRunTimeSeconds());
        summary.outputSummaryRowToFile();

        deathCount = 0;
        birthsCount = 0;
    }

    private int adjustPopulationNumbers(final int birthsInTimeStp) {

        final Period initTimeStep = config.getSimulationTimeStep();

        // calculate hypothetical number of expected births
        final int hypotheticalBirths = calculateNumberToHaveEvent(config.getSetUpBR() * divideYieldingDouble(initTimeStep, Period.ofYears(1)));

        final int shortFallInBirths = hypotheticalBirths - birthsInTimeStp;

        // calculate hypothetical number of expected deaths
        final int hypotheticalDeaths = calculateNumberToHaveEvent(config.getSetUpDR() * divideYieldingDouble(initTimeStep, Period.ofYears(1)));

        // update hypothetical population
        currentHypotheticalPopulationSize += hypotheticalBirths - hypotheticalDeaths;

        if (shortFallInBirths >= 0)
            createOrphanChildren(shortFallInBirths);

        return shortFallInBirths;
    }

    private void createOrphanChildren(final int shortFallInBirths) {

        for (int i = 0; i < shortFallInBirths; i++) {

            final IPerson person = personFactory.makePersonWithRandomBirthDate(currentDate, null, false);
            population.getLivingPeople().add(person);
        }
    }

    private int createBirths() {

        final FemaleCollection femalesLiving = population.getLivingPeople().getFemales();
        final Period timeStep = config.getSimulationTimeStep();
        final Set<LocalDate> divisionDates = femalesLiving.getDivisionDates(timeStep);

        int count = 0;

        // For each division in the population data store up to the current date
        for (final LocalDate divisionDate : divisionDates) {
            if (divisionDate.isAfter(currentDate)) break;
            count += getBornAtTS(femalesLiving, divisionDate);
        }

        return count;
    }

    private int getBornAtTS(final FemaleCollection femalesLiving, final LocalDate divisionDate) {

        final Period consideredTimePeriod = config.getSimulationTimeStep();
        final int age = Period.between(divisionDate.plus(consideredTimePeriod), currentDate).getYears();

        final int cohortSize = femalesLiving.getPeopleBornInTimePeriod(divisionDate, consideredTimePeriod).size();

        final Set<IntegerRange> birthOrders = desiredStatistics.getOrderedBirthRates(Year.of(currentDate.getYear())).getColumnLabels();

        int count = 0;

        for (final IntegerRange birthOrder : birthOrders)
            count += getBornInRange(femalesLiving, divisionDate, age, cohortSize, birthOrder);

        return count;
    }

    private int getBornInRange(final FemaleCollection femalesLiving, final LocalDate divisionDate, final int age, final int cohortSize, final IntegerRange birthOrder) {

        final Period consideredTimePeriod = config.getSimulationTimeStep();

        // TODO already retrieved women for this period in calling method.
        final List<IPerson> people = new ArrayList<>(femalesLiving.getByDatePeriodAndBirthOrder(divisionDate, consideredTimePeriod, birthOrder));
        final BirthStatsKey key = new BirthStatsKey(age, birthOrder.getValue(), cohortSize, consideredTimePeriod, currentDate);
        final SingleDeterminedCount determinedCount = (SingleDeterminedCount) desiredStatistics.getDeterminedCount(key, config);

        final int numberOfChildren = determinedCount.getDeterminedCount();

        // Make women into mothers
        final MothersNeedingPartners mothersNeedingPartners = selectMothers(people, numberOfChildren);

        // Partner females of age who don't have partners
        // Children are created in the partnerships phase
        final int cancelledChildren = createPartnerships(mothersNeedingPartners.mothers);
        final int fulfilled = mothersNeedingPartners.newlyProducedChildren - cancelledChildren;

        determinedCount.setFulfilledCount(fulfilled);
        desiredStatistics.returnAchievedCount(determinedCount);

        return fulfilled;
    }

    private int createDeaths(final SexOption sex) {

        int killedAtTS = 0;

        final PersonCollection ofSexLiving = getLivingPeopleOfSex(sex);
        final Set<LocalDate> divisionDates = ofSexLiving.getDivisionDates(config.getSimulationTimeStep());

        // For each division in the population data store up to the current date
        for (final LocalDate divisionDate : divisionDates) {

            if (divisionDate.isAfter(currentDate)) break;
            final int killedAtTS1 = getKilledAtTS(sex, ofSexLiving, divisionDate);

            killedAtTS += killedAtTS1;
        }

        return killedAtTS;
    }

    private int getKilledAtTS(final SexOption sex, final PersonCollection ofSexLiving, final LocalDate divisionDate) {

        final Period consideredTimePeriod = config.getSimulationTimeStep();

        final int age = Period.between(divisionDate, currentDate).getYears();
        final int peopleOfAge = ofSexLiving.getNumberOfPeople(divisionDate, consideredTimePeriod);

        // gets death rate for people of age at the current date
        final StatsKey<Integer,Integer> key = new DeathStatsKey(age, peopleOfAge, consideredTimePeriod, currentDate, sex);
        @SuppressWarnings("unchecked")
        final DeterminedCount<Integer, Double, Integer, Integer> determinedCount = (DeterminedCount<Integer, Double, Integer, Integer>) desiredStatistics.getDeterminedCount(key, config);

        // Calculate the appropriate number to kill
        final int numberToKill = determinedCount.getDeterminedCount();

        final Collection<IPerson> peopleToKill = ofSexLiving.removeNPersons(numberToKill, divisionDate, consideredTimePeriod, true);

        final int killed = killPeople(peopleToKill);

        // Returns the number killed to the distribution manager
        determinedCount.setFulfilledCount(killed);
        desiredStatistics.returnAchievedCount(determinedCount);

        return killed;
    }

    private int createPartnerships(final List<NewMother> mothersNeedingPartners) {

        if (mothersNeedingPartners.isEmpty()) return 0;

        final List<NewMother> mothersNeedingPartnersCopy = new ArrayList<>(mothersNeedingPartners);

        final int age = ageOnDate(mothersNeedingPartnersCopy.getFirst().newMother, currentDate);

        final PartneringStatsKey key = new PartneringStatsKey(age, mothersNeedingPartners.size(), config.getSimulationTimeStep(), currentDate);

        final MultipleDeterminedCountByIR determinedCounts = (MultipleDeterminedCountByIR) desiredStatistics.getDeterminedCount(key, config);

        final OperableLabelledValueSet<IntegerRange, Integer> partnerCounts = new IntegerRangeToIntegerSet(determinedCounts.getDeterminedCount(), Randomness.getRandomGenerator());
        final LabelledValueSet<IntegerRange, Integer> achievedPartnerCounts = new IntegerRangeToIntegerSet(partnerCounts.getLabels(), 0, Randomness.getRandomGenerator());
        final LabelledValueSet<IntegerRange, Integer> availableMen = new IntegerRangeToIntegerSet(partnerCounts.getLabels(), 0, Randomness.getRandomGenerator());

        final Map<IntegerRange, List<IPerson>> menMap = getAllMen(partnerCounts, availableMen);
        final OperableLabelledValueSet<IntegerRange, Integer> redistributedPartnerCounts = redistributePartnerCounts(partnerCounts, availableMen);

        final List<ProposedPartnership> proposedPartnerships = getProposedPartnerships(mothersNeedingPartnersCopy, menMap, redistributedPartnerCounts, achievedPartnerCounts);

        findPartners(mothersNeedingPartnersCopy, menMap, redistributedPartnerCounts, proposedPartnerships);

//        if (!mothersNeedingPartnersCopy.isEmpty())
//            System.out.println("mothers needing partners:");
//        for (NewMother newMother : mothersNeedingPartnersCopy) {
//            System.out.println(newMother.newMother);
//        }
//        final int cancelledChildren = removeLastPartners(mothersNeedingPartnersCopy);

        separationEvent(getPartneredFemalesByChildren(determinedCounts, proposedPartnerships));

//        return cancelledChildren;
        return 0;
    }

    private Map<Integer, List<IPerson>> getPartneredFemalesByChildren(final MultipleDeterminedCountByIR determinedCounts, final List<ProposedPartnership> proposedPartnerships) {

        final LabelledValueSet<IntegerRange, Integer> returnPartnerCounts = determinedCounts.getZeroedCountsTemplate(Randomness.getRandomGenerator());
        final Map<Integer, List<IPerson>> partneredFemalesByChildren = new HashMap<>();

        for (final ProposedPartnership partnership : proposedPartnerships) {

            setUpPartnership(partnership, returnPartnerCounts);
            addMotherToMap(partnership, partneredFemalesByChildren);
        }

        determinedCounts.setFulfilledCount(returnPartnerCounts);
        desiredStatistics.returnAchievedCount(determinedCounts);
        return partneredFemalesByChildren;
    }

    private void setUpPartnership(final ProposedPartnership partnership, final LabelledValueSet<IntegerRange, Integer> partnerCounts) {

        final IPerson mother = partnership.female;
        final IPerson father = partnership.male;

        final int numChildrenInPartnership = partnership.numberOfChildren;

        // Decide on marriage
        final MarriageStatsKey marriageKey = new MarriageStatsKey(ageOnDate(mother, currentDate), numChildrenInPartnership, config.getSimulationTimeStep(), currentDate);
        final SingleDeterminedCount marriageCounts = (SingleDeterminedCount) desiredStatistics.getDeterminedCount(marriageKey, config);

        final boolean isIllegitimate = !needsNewPartner(father, currentDate);
        final boolean marriedAtBirth = !isIllegitimate && (int) Math.round(marriageCounts.getDeterminedCount() / (double) numChildrenInPartnership) == 1;

        final IPartnership marriage = createNewPartnership(numChildrenInPartnership, father, mother, isIllegitimate, marriedAtBirth);

        marriageCounts.setFulfilledCount(marriage.getMarriageDate() != null ? numChildrenInPartnership : 0);
        desiredStatistics.returnAchievedCount(marriageCounts);

        final IntegerRange maleAgeRange = resolveAgeToIntegerRange(father, partnerCounts.getLabels(), currentDate);
        partnerCounts.update(maleAgeRange, partnerCounts.getValue(maleAgeRange) + 1);
    }

    private void handleAddressChanges(final IPartnership partnership) {

        final IPerson mother = partnership.getFemalePartner();
        final IPerson father = partnership.getMalePartner();

        final Address lastMaleAddress = partnership.getMalePartner().getAddress(LocalDate.MAX);
        final Address lastFemaleAddress = partnership.getFemalePartner().getAddress(LocalDate.MAX);

        final LocalDate moveDate = partnership.getMarriageDate() != null ? partnership.getMarriageDate() : partnership.getPartnershipDate();
        final double moveDistance = moveDistanceSelector.selectRandomDistance();

        final Address newAddress;

        if (lastMaleAddress == null || lastMaleAddress.getArea() == null) {
            if (lastFemaleAddress == null || lastFemaleAddress.getArea() == null)
                newAddress = geography.getRandomEmptyAddress();
            else
                newAddress = geography.getNearestEmptyAddressAtDistance(lastFemaleAddress.getArea().getCentroid(), moveDistance);

        } else {
            if (lastFemaleAddress == null || lastFemaleAddress.getArea() == null) {
                newAddress = geography.getNearestEmptyAddressAtDistance(lastMaleAddress.getArea().getCentroid(), moveDistance);
            } else {
                // both already have address, so flip coin to decide who acts as origin for move
                if (Randomness.getRandomGenerator().nextBoolean())
                    newAddress = geography.getNearestEmptyAddressAtDistance(lastMaleAddress.getArea().getCentroid(), moveDistance);
                else
                    newAddress = geography.getNearestEmptyAddressAtDistance(lastFemaleAddress.getArea().getCentroid(), moveDistance);
            }
        }

        father.setAddress(moveDate, newAddress);
        mother.setAddress(moveDate, newAddress);

        if (father.getPartnerships().size() > 1) // if this is the persons first partnership then they are a child at their own address - therefore they don't take the other inhabitants (i.e. parents and siblings) into their new marriage home
            while (lastMaleAddress != null && !lastMaleAddress.getInhabitants().isEmpty()) {
                final IPerson individual = lastMaleAddress.getInhabitants().getFirst();
                final LocalDate individualMoveDate = moveDate.isBefore(individual.getBirthDate()) ? individual.getBirthDate() : moveDate;
                individual.setAddress(individualMoveDate, newAddress);
            }

        if (mother.getPartnerships().size() > 1) // if this is the persons first partnership then they are a child at their own address - therefore they don't take the other inhabitants (i.e. parents and siblings) into their new marriage home
            while(lastFemaleAddress != null && !lastFemaleAddress.getInhabitants().isEmpty()) {
                final IPerson individual = lastFemaleAddress.getInhabitants().getFirst();
                final LocalDate individualMoveDate = moveDate.isBefore(individual.getBirthDate()) ? individual.getBirthDate() : moveDate;
                individual.setAddress(individualMoveDate, newAddress);
            }
    }

    private void findPartners(final List<NewMother> women, final Map<IntegerRange, List<IPerson>> menMap,
                              final LabelledValueSet<IntegerRange, Integer> partnerCounts, final List<ProposedPartnership> proposedPartnerships) {

        final Iterator<NewMother> iterator = women.iterator();

        while (iterator.hasNext()) {

            final NewMother newMother = iterator.next();

            partnerSearchLoop:
            for (final IntegerRange range : partnerCounts.getLabels()) {
                for (final IPerson man : menMap.get(range)) {

                    if (eligible(man, newMother) && !inPartnerships(man, proposedPartnerships)) {

                        proposedPartnerships.add(new ProposedPartnership(man, newMother.newMother, newMother.numberOfChildrenInMaternity));
                        iterator.remove();

                        break partnerSearchLoop;
                    }
                }
            }
        }
    }

    private List<ProposedPartnership> getProposedPartnerships(final List<NewMother> women, final Map<IntegerRange, List<IPerson>> menMap,
                                                              final LabelledValueSet<IntegerRange, Integer> partnerCounts, final LabelledValueSet<IntegerRange, Integer> achievedPartnerCounts) {

        final List<ProposedPartnership> proposedPartnerships = new ArrayList<>();

        // for each age range of males
        for (final IntegerRange range : partnerCounts.getLabels()) {

            final List<IPerson> men = menMap.get(range);
            final Collection<NewMother> unmatchedFemales = new ArrayList<>();

            final int determinedCount = addPartnerships(women, men, proposedPartnerships, unmatchedFemales, partnerCounts.get(range));

            women.addAll(unmatchedFemales);

            // note how many females have been partnered at this age range
            achievedPartnerCounts.add(range, partnerCounts.get(range) - determinedCount);
        }

        return proposedPartnerships;
    }

    private int addPartnerships(final List<NewMother> women, final List<IPerson> men, final List<ProposedPartnership> proposedPartnerships, final Collection<NewMother> unmatchedFemales, final int initialCount) {

        int determinedCount = initialCount;

        IPerson head = null; // keeps track of first man seen to prevent infinite loop

        // Keep going until enough females have been matched for this range
        while (determinedCount > 0 && !women.isEmpty()) {

            final IPerson man = men.removeFirst();
            NewMother woman = women.removeFirst();

            // if man is head of list - i.e. this is the second time round
            if (man == head) {
                // thus female has not been able to be matched
                unmatchedFemales.add(woman);
                head = null;

                // get next woman to check for partnering
                if (women.isEmpty()) break;
                woman = women.removeFirst();
            }

            // check if there is any reason why these people cannot lawfully be partnered...
            if (eligible(man, woman)) {
                // if they can - then note as a proposed partnership
                proposedPartnerships.add(new ProposedPartnership(man, woman.newMother, woman.numberOfChildrenInMaternity));
                determinedCount--;
                head = null;

            } else {
                // else we need to loop through more men - so keep track of the first man we looked at
                if (head == null) {
                    head = man;
                }
                men.addLast(man);
                women.addFirst(woman);
            }
        }
        return determinedCount;
    }

    private static OperableLabelledValueSet<IntegerRange, Integer> redistributePartnerCounts(final OperableLabelledValueSet<IntegerRange, Integer> initialPartnerCounts, final LabelledValueSet<IntegerRange, Integer> availableMen) {

        OperableLabelledValueSet<IntegerRange, Integer> partnerCounts = initialPartnerCounts;
        OperableLabelledValueSet<IntegerRange, Double> shortfallCounts;

        // this section redistributes the determined partner counts based on the number of available men in each age range
        do {
            shortfallCounts = new IntegerRangeToDoubleSet(partnerCounts.valuesSubtractValues(availableMen), Randomness.getRandomGenerator());

            final LabelledValueSet<IntegerRange, Double> zeroedNegShortfalls = shortfallCounts.zeroNegativeValues();
            final int numberOfRangesWithSpareMen = shortfallCounts.countNegativeValues();
            final double totalShortfall = zeroedNegShortfalls.getSumOfValues();
            final double shortfallToShare = totalShortfall / (double) numberOfRangesWithSpareMen;

            partnerCounts = new IntegerRangeToDoubleSet(partnerCounts.valuesAddNWhereCorrespondingLabelNegativeInLVS(shortfallToShare, shortfallCounts)
                    .valuesSubtractValues(zeroedNegShortfalls), Randomness.getRandomGenerator()).controlledRoundingMaintainingSum();

        } while (shortfallCounts.countPositiveValues() != 0);

        return partnerCounts;
    }

    private Map<IntegerRange, List<IPerson>> getAllMen(final LabelledValueSet<IntegerRange, Integer> partnerCounts, final LabelledValueSet<IntegerRange, Integer> availableMen) {

        final Map<IntegerRange, List<IPerson>> allMen = new TreeMap<>();
        for (final IntegerRange range : partnerCounts.getLabels()) {

            final Period rangeLength = getRangeLength(range);

            final List<IPerson> men = new ArrayList<>(population.getLivingPeople().getMales().getPeopleBornInTimePeriod(getYearOfBirthOfOlderEndOfRange(range, currentDate), rangeLength));

            CollectionUtils.shuffle(men, Randomness.getRandomGenerator());

            allMen.put(range, men);
            availableMen.update(range, men.size());
        }
        return allMen;
    }

    // TODO rationalise next four methods

    private IPartnership createNewPartnership(final int numberOfChildren, final IPerson father, final IPerson mother, final boolean adulterousBirth, final boolean marriedAtBirth) throws PersonNotFoundException {

        population.getLivingPeople().remove(mother);
        population.getLivingPeople().remove(father);  // TODO why necessary to remove/add father, if only indexed by year of birth? TD: it probably isn't...

        final IPartnership partnership = new Partnership(father, mother);
        makeChildren(partnership, numberOfChildren, adulterousBirth, marriedAtBirth);

        if (adulterousBirth)
            partnership.setFinalised(true);

        population.getLivingPeople().add(partnership);

        final IPartnership motherLastPartnership = PopulationNavigation.getLastPartnership(mother);
        final IPartnership fatherLastPartnership = PopulationNavigation.getLastPartnership(father);

        mother.recordPartnership(partnership);
        father.recordPartnership(partnership);

        // these need to happen post recording of new partnership
        handleSeparationMoves(motherLastPartnership, mother);

        if (!adulterousBirth) {
            handleSeparationMoves(fatherLastPartnership, father);
            handleAddressChanges(partnership);
        }
        else if(motherLastPartnership == null) {
            if (father.getAddress(LocalDate.MAX) != null)
                mother.setAddress(partnership.getPartnershipDate(), geography.getNearestEmptyAddressAtDistance(father.getAddress(LocalDate.MAX).getArea().getCentroid(), moveDistanceSelector.selectRandomDistance()));
            else
                mother.setAddress(partnership.getPartnershipDate(), geography.getRandomEmptyAddress());
        }

        for (final IPerson child : partnership.getChildren())
            child.setAddress(child.getBirthDate(), partnership.getFemalePartner().getAddress(child.getBirthDate()));

        if (partnership.getMarriageDate() != null && partnership.getMarriagePlace() == null)
            partnership.setMarriagePlace(mother.getAddress(partnership.getMarriageDate()).toShortForm());

        // re-insert parents into population, this allows their position in the data structure to be updated
        population.getLivingPeople().add(mother);
        population.getLivingPeople().add(father);

        return partnership;
    }

    protected void handleSeparationMoves(final IPartnership lastPartnership, final IPerson rePartneringPartner) {

        if (lastPartnership != null && !lastPartnership.isFinalised()) {

            // the getting process forces these to be set - they can only be set once the next partnership has been set up - i.e. now!
            final LocalDate sepDate = lastPartnership.getSeparationDate(Randomness.getRandomGenerator());

            if (sepDate != null) {
                final IPerson ex = lastPartnership.getPartnerOf(rePartneringPartner);

                if (!ex.hasEmigrated() && !rePartneringPartner.hasEmigrated()) { // if neither has emigrated then we need to make these separtaion decisions - otherwise the house and kids stay with who is still in the country (the sim will have already handled this)

                    // flip coin for who gets the house
                    final boolean keepHouse = ex.isPhantom() || ex.getDeathDate() != null || Randomness.getRandomGenerator().nextBoolean();

                    // flip coin for who gets the kids
                    final boolean keepKids = ex.isPhantom() || ex.getDeathDate() != null || Randomness.getRandomGenerator().nextBoolean();

                    final Address oldFamilyAddress = rePartneringPartner.getAddress(sepDate);

                    if (keepHouse) {
                        // ex moves
                        Address exsNewAddress = null;
                        if (!ex.isPhantom() && ex.getDeathDate() == null) {
                            final Address originalAddress = ex.getAddress(sepDate);
                            if (originalAddress != null) {
                                exsNewAddress = geography.getNearestEmptyAddressAtDistance(originalAddress.getArea().getCentroid(), moveDistanceSelector.selectRandomDistance());
                                ex.setAddress(sepDate, exsNewAddress);
                            }
                        }

                        if (!keepKids && oldFamilyAddress != null) {
                            // kids move to ex
                            for (final IPerson child : lastPartnership.getChildren()) {
                                if (oldFamilyAddress.getInhabitants().contains(child))
                                    child.setAddress(sepDate, exsNewAddress); // never get to here if ex is a phantom and thus exsNewAddress is still null (because ex can never get the kids if they're a phantom)
                            }
                        }

                    } else {
                        if (oldFamilyAddress != null) {
                            final Address newAddress = geography.getNearestEmptyAddressAtDistance(oldFamilyAddress.getArea().getCentroid(), moveDistanceSelector.selectRandomDistance());
                            rePartneringPartner.setAddress(sepDate, newAddress);

                            if (keepKids) {
                                for (final IPerson child : lastPartnership.getChildren()) {
                                    if (oldFamilyAddress.getInhabitants().contains(child))
                                        child.setAddress(sepDate, newAddress);
                                }
                            }
                        }
                    }
                }
            }

            lastPartnership.setFinalised(true);
        }
    }

    private void addChildrenToCurrentPartnership(final IPerson mother, final int numberOfChildren) {

        population.getLivingPeople().remove(mother);

        final IPerson mostRecentPreviousChild = PopulationNavigation.getLastChild(mother);
        final IPartnership mostRecentPartnership = mostRecentPreviousChild.getParents();

        final LocalDate newChildBirthDate = addChildrenToPartnership(numberOfChildren, mostRecentPartnership, mostRecentPreviousChild.isAdulterousBirth());

        updateAdulterousCounts(numberOfChildren, mostRecentPartnership, newChildBirthDate, mostRecentPreviousChild.isAdulterousBirth());
        updateMarriageCounts(mother, numberOfChildren, mostRecentPreviousChild, mostRecentPartnership, newChildBirthDate);

        population.getLivingPeople().add(mother);
    }

    // TODO what's the difference between the next two methods?
    private LocalDate addChildrenToPartnership(final int numberOfChildren, final IPartnership partnership, final boolean isIllegitimate) {

        LocalDate birthDate = null;

        for (int i = 0; i < numberOfChildren; i++) {

            final IPerson child;

            if (birthDate == null) {

                child = personFactory.makePersonWithRandomBirthDate(currentDate, partnership, isIllegitimate);
                birthDate = child.getBirthDate();

            } else {

                child = personFactory.makePerson(birthDate, partnership, false);
            }

            partnership.addChildren(Collections.singleton(child));

            child.setAddress(child.getBirthDate(), partnership.getFemalePartner().getAddress(child.getBirthDate()));

            population.getLivingPeople().add(child);
        }

        return birthDate;
    }

    private void makeChildren(final IPartnership partnership, final int numberOfChildren, final boolean adulterousBirth, final boolean marriedAtBirth) {

        final List<IPerson> children = new ArrayList<>();

        // This ensures twins are born on the same day
        LocalDate childrenBirthDate = null;

        // the loop here allows for the multiple children in pregnancies
        for (int i = 0; i < numberOfChildren; i++) {

            final IPerson child = childrenBirthDate == null ?
                    personFactory.makePersonWithRandomBirthDate(currentDate, partnership, adulterousBirth) :
                    personFactory.makePerson(childrenBirthDate, partnership, adulterousBirth);

            population.getLivingPeople().add(child);
            children.add(child);

            childrenBirthDate = child.getBirthDate();
        }

        setMarriageDate(partnership, marriedAtBirth, childrenBirthDate);
        partnership.setPartnershipDate(childrenBirthDate);
        partnership.addChildren(children);
    }

    private void setMarriageDate(final IPartnership partnership, final boolean marriedAtBirth, final LocalDate childrenBirthDate) {

        LocalDate marriageDate = null;
        if (marriedAtBirth) {

            final LocalDate motherLastPrevPartneringEvent = getDateOfLastLegitimatePartnershipEventBefore(partnership.getFemalePartner(), childrenBirthDate);
            final LocalDate fatherLastPrevPartneringEvent = getDateOfLastLegitimatePartnershipEventBefore(partnership.getMalePartner(), childrenBirthDate);

            final LocalDate earliestPossibleMarriageDate = motherLastPrevPartneringEvent.isAfter(fatherLastPrevPartneringEvent) ? motherLastPrevPartneringEvent : fatherLastPrevPartneringEvent;

            if (earliestPossibleMarriageDate.isBefore(childrenBirthDate))
                marriageDate = marriageDateSelector.selectRandomDate(earliestPossibleMarriageDate, childrenBirthDate);
        }
        partnership.setMarriageDate(marriageDate);
    }

    private static LocalDate getDateOfLastLegitimatePartnershipEventBefore(final IPerson person, final LocalDate date) {

        LocalDate latestDate = getEarliestPossibleMarriageDate(person);

        for (final IPartnership partnership : person.getPartnerships()) {
            if (partnership.getPartnershipDate().isBefore(date)) {

                final List<IPerson> children = partnership.getChildren();

                if (children.isEmpty())
                    throw new UnsupportedOperationException("Childless marriages not supported");

                if (hasLegitimateChildren(partnership)) {

                    final LocalDate separationDate = partnership.getEarliestPossibleSeparationDate();
                    if (separationDate != null && latestDate.isBefore(separationDate))
                        latestDate = separationDate;

                    final LocalDate partnerDeathDate = partnership.getPartnerOf(person).getDeathDate();
                    if (partnerDeathDate != null && latestDate.isBefore(partnerDeathDate))
                        latestDate = partnerDeathDate;
                }
            }
        }

        return latestDate;
    }

    private static boolean hasLegitimateChildren(final IPartnership partnership) {

        for (final IPerson child : partnership.getChildren())
            if (!child.isAdulterousBirth()) return true;

        return false;
    }

    private static LocalDate getEarliestPossibleMarriageDate(final IPerson person) {

        return person.getBirthDate().plusYears(EARLIEST_AGE_OF_MARRIAGE);
    }

    private boolean populationTooSmall() {

        return population.getLivingPeople().getPeople().size() < MINIMUM_POPULATION_SIZE;
    }

    private MothersNeedingPartners selectMothers(final List<IPerson> females, final int numberOfChildren) {

        if (females.isEmpty()) return new MothersNeedingPartners();

        final int ageOfMothers = ageOnDate(females.getFirst(), currentDate);

        final MultipleDeterminedCountByIR requiredBirths = calcNumberOfPregnanciesOfMultipleBirth(ageOfMothers, numberOfChildren);
        final LabelledValueSet<IntegerRange, Integer> motherCountsByMaternities = new IntegerRangeToIntegerSet(requiredBirths.getDeterminedCount().getLabels(), 0, Randomness.getRandomGenerator());
        final OperableLabelledValueSet<IntegerRange, Integer> remainingMothersToFind = new IntegerRangeToIntegerSet(requiredBirths.getDeterminedCount().clone(), Randomness.getRandomGenerator());

        try {
            return getMothersNeedingPartners(females, numberOfChildren, requiredBirths, motherCountsByMaternities, remainingMothersToFind);

        } catch (final NoSuchElementException e) {
            return new MothersNeedingPartners();
        }
    }

    private MothersNeedingPartners getMothersNeedingPartners(final List<IPerson> females, final int numberOfChildren, final MultipleDeterminedCountByIR requiredBirths,
                                                             final LabelledValueSet<IntegerRange, Integer> motherCountsByMaternities, final OperableLabelledValueSet<IntegerRange, Integer> remainingMothersToFind) {

        CollectionUtils.shuffle(females, Randomness.getRandomGenerator());

        IntegerRange highestBirthOption = remainingMothersToFind.getLargestLabelOfNonZeroValue();

        int childrenMade = 0;
        final List<NewMother> newMothers = new ArrayList<>();

        final Map<Integer, List<IPerson>> continuingPartneredFemalesByChildren = new HashMap<>();

        for (final IPerson female : females) {

            if (eligible(female)) {

                final int numberOfChildrenForThisMother = highestBirthOption.getValue();

                childrenMade += numberOfChildrenForThisMother;

                addChildrenForMother(female, numberOfChildrenForThisMother, newMothers, continuingPartneredFemalesByChildren);

                // updates count of remaining mothers to find
                final int furtherMothersNeededForMaternitySize = remainingMothersToFind.get(highestBirthOption) - 1;
                remainingMothersToFind.update(highestBirthOption, furtherMothersNeededForMaternitySize);

                // updates count of mother found
                motherCountsByMaternities.update(highestBirthOption, motherCountsByMaternities.getValue(highestBirthOption) + 1);

                if (furtherMothersNeededForMaternitySize <= 0) {
                    try {
                        highestBirthOption = remainingMothersToFind.getLargestLabelOfNonZeroValue();

                    } catch (final NoSuchElementException e) {
                        // In this case we have created all the new mothers and children required
                        break;
                    }
                }

                if (childrenMade >= numberOfChildren) break;
            }
        }

        separationEvent(continuingPartneredFemalesByChildren);

        requiredBirths.setFulfilledCount(motherCountsByMaternities);
        desiredStatistics.returnAchievedCount(requiredBirths);

        return new MothersNeedingPartners(newMothers, childrenMade);
    }

    private void addChildrenForMother(final IPerson mother, final int numberOfChildrenForThisMother, final List<NewMother> newMothers, final Map<Integer, List<IPerson>> continuingPartneredFemalesByChildren) {

        if (needsNewPartner(mother, currentDate)) {
            newMothers.add(new NewMother(mother, numberOfChildrenForThisMother));

        } else {
            addChildrenToCurrentPartnership(mother, numberOfChildrenForThisMother);
            final int numberOfChildrenInLatestPartnership = numberOfChildrenInLatestPartnership(mother);

            if (!continuingPartneredFemalesByChildren.containsKey(numberOfChildrenInLatestPartnership))
                continuingPartneredFemalesByChildren.put(numberOfChildrenInLatestPartnership, new ArrayList<>());

            continuingPartneredFemalesByChildren.get(numberOfChildrenInLatestPartnership).add(mother);
        }
    }

    private MultipleDeterminedCountByIR calcNumberOfPregnanciesOfMultipleBirth(final int ageOfMothers, final int numberOfChildren) {

        final MultipleBirthStatsKey key = new MultipleBirthStatsKey(ageOfMothers, numberOfChildren, config.getSimulationTimeStep(), currentDate);
        return (MultipleDeterminedCountByIR) desiredStatistics.getDeterminedCount(key, config);
    }

    private boolean eligible(final IPerson potentialMother) {

        // No previous partners or children - thus eligible
        if (potentialMother.getPartnerships().isEmpty()) return true;

        // if last partnership has not ended in separation and the spouse has emigrated then this women cannot produce a child (now or at any future point in the simulation)
        if (!needsNewPartner(potentialMother, currentDate) && potentialMother.getLastPartnership().getMalePartner().hasEmigrated()) return false;

        final IPerson lastChild = PopulationNavigation.getLastChild(potentialMother);

        if (lastChild != null) {

            final LocalDate earliestDateOfNextChild = lastChild.getBirthDate().plus(desiredStatistics.getMinBirthSpacing());

            // Returns true if last child was born far enough in the past for another child to be born at currentTime
            return earliestDateOfNextChild.isBefore(currentDate);
        }

        // i.e. there is no previous child and thus no limitation to birth
        return true;
    }

    private boolean eligible(final IPerson man, final NewMother newMother) {

        population.getPopulationCounts().incEligibilityCheck();

        final boolean eligible = maleAvailable(man, newMother.numberOfChildrenInMaternity) && legallyEligibleToMarry(man, newMother.newMother);

        if (!eligible) population.getPopulationCounts().incFailedEligibilityCheck();

        return eligible;
    }

    private void updateAdulterousCounts(final int numberOfChildren, final IPartnership partnership, final LocalDate birthDate, final boolean isAdulterous) {

        final IPerson man = partnership.getMalePartner();

        final AdulterousBirthStatsKey adulterousKey = new AdulterousBirthStatsKey(ageOnDate(man, birthDate), numberOfChildren, config.getSimulationTimeStep(), birthDate);
        final SingleDeterminedCount adulterousCounts = (SingleDeterminedCount) desiredStatistics.getDeterminedCount(adulterousKey, config);

        if(isAdulterous)
            adulterousCounts.setFulfilledCount(numberOfChildren);
        else
            adulterousCounts.setFulfilledCount(0);

        desiredStatistics.returnAchievedCount(adulterousCounts);
    }

    private void updateMarriageCounts(final IPerson mother, final int numberOfChildren, final IPerson mostRecentPreviousChild,
                                      final IPartnership mostRecentPartnership, final LocalDate newChildBirthDate) {

        final MarriageStatsKey marriageKey = new MarriageStatsKey(ageOnDate(mother, newChildBirthDate), numberOfChildren, config.getSimulationTimeStep(), newChildBirthDate);
        final SingleDeterminedCount marriageCounts = (SingleDeterminedCount) desiredStatistics.getDeterminedCount(marriageKey, config);

        if (mostRecentPartnership.getMarriageDate() != null) {
            // is already married - so return as married
            marriageCounts.setFulfilledCount(numberOfChildren);

        } else {
            final boolean marriedAtBirth = (int) Math.round(marriageCounts.getDeterminedCount() / (double) numberOfChildren) == 1;

            if (marriedAtBirth) {
                marriageCounts.setFulfilledCount(numberOfChildren);
                final LocalDate mostRecentPreviousChildBirthDate = mostRecentPreviousChild.getBirthDate();
                final LocalDate marriageDate = marriageDateSelector.selectRandomDate(mostRecentPreviousChildBirthDate, newChildBirthDate);
                mostRecentPartnership.setMarriageDate(marriageDate);

            } else {
                marriageCounts.setFulfilledCount(0);
            }
        }

        desiredStatistics.returnAchievedCount(marriageCounts);
    }

    private static void addMotherToMap(final ProposedPartnership partnership, final Map<Integer, List<IPerson>> partneredFemalesByChildren) {

        final IPerson mother = partnership.female;
        final int numChildrenInPartnership = partnership.numberOfChildren;

        if (!partneredFemalesByChildren.containsKey(numChildrenInPartnership))
            partneredFemalesByChildren.put(numChildrenInPartnership, new ArrayList<>());

        partneredFemalesByChildren.get(numChildrenInPartnership).add(mother);
    }

    private int removeLastPartners(final List<NewMother> women) {

        int cancelledChildren = 0;

        if (!women.isEmpty()) {
            for (final NewMother newMother : women) {

                System.out.println("removing last partnership of: " + newMother.newMother);

                // update position in data structures
                population.getLivingPeople().remove(newMother.newMother);

                cancelledChildren += newMother.numberOfChildrenInMaternity;
                // cancel birth(s) as no father can be found
                IPartnership lastPartnership = getLastPartnership(newMother.newMother);
                newMother.newMother.getPartnerships().remove(lastPartnership);

                population.getLivingPeople().add(newMother.newMother);
            }
        }
        return cancelledChildren;
    }

    private static IntegerRange resolveAgeToIntegerRange(final IPerson male, final Set<IntegerRange> labels, final LocalDate currentDate) {

        final int age = ageOnDate(male, currentDate);

        for (final IntegerRange range : labels)
            if (range.contains(age))
                return range;

        throw new InvalidRangeException("Male does not fit in expected ranges...");
    }

    private static boolean inPartnerships(final IPerson person, final List<ProposedPartnership> partnerships) {

        for (final ProposedPartnership partnership : partnerships)
            if (partnership.male == person || partnership.female == person)
                return true;

        return false;
    }

    private boolean maleAvailable(final IPerson man, final int childrenInPregnancy) {

        // if the man has immigrated this year, was he present early enough in the year to be the father?
        if (man.getImmigrationDate() != null &&
            man.getImmigrationDate().plus(desiredStatistics.getMinGestationPeriod()).isAfter(currentDate.plusYears(1).minusDays(1)))
                return false;

        // during the initialisation phase any partnering is allowed
        if (!currentDate.isAfter(endOfInitPeriod)) return true;

        // Get adulterous birth rates
        final AdulterousBirthStatsKey adulterousBirthKey = new AdulterousBirthStatsKey(ageOnDate(man, currentDate), childrenInPregnancy, config.getSimulationTimeStep(), currentDate);
        final SingleDeterminedCount adulterousBirthCounts = (SingleDeterminedCount) desiredStatistics.getDeterminedCount(adulterousBirthKey, config);
        final int permitted = (int) Math.round(adulterousBirthCounts.getDeterminedCount() / (double) childrenInPregnancy);

        if (needsNewPartner(man, currentDate)) {
            // record the legitimate birth
            adulterousBirthCounts.setFulfilledCount(0);
            desiredStatistics.returnAchievedCount(adulterousBirthCounts);
            return true;
        }

        if (permitted == 1) {
            // record the adulterous birth
            adulterousBirthCounts.setFulfilledCount(childrenInPregnancy);
            desiredStatistics.returnAchievedCount(adulterousBirthCounts);
            return true;
        }

        // the man is not a father - therefore we don't report any achieved count for the statistic
        return false;
    }

    private static boolean legallyEligibleToMarry(final IPerson man, final IPerson woman) {

        return
            !femaleAncestorsOf(man).contains(woman) &&
            !femaleAncestorsOf(man).contains(woman) &&
            !femaleDescendantsOf(man).contains(woman) &&
            !sistersOf(man).contains(woman) &&
            !femaleAncestorsOf(descendantsOf(man)).contains(woman) &&
            !femaleDescendantsOf(ancestorsOf(man)).contains(woman) &&
            !partnersOf(maleAncestorsOf(man)).contains(woman) &&
            !partnersOf(maleDescendantsOf(man)).contains(woman) &&
            !partnersOf(brothersOf(man)).contains(woman) &&
            !femaleDescendantsOf(siblingsOf(man)).contains(woman) &&
            !femaleAncestorsOf(partnersOf(man)).contains(woman) &&
            !femaleDescendantsOf(partnersOf(man)).contains(woman);
    }

    private static Period getRangeLength(final IntegerRange range) {

        return Period.ofYears(range.getMax() - range.getMin() + 1);
    }

    private static LocalDate getYearOfBirthOfOlderEndOfRange(final IntegerRange range, final LocalDate currentDate) {

        return currentDate.minusYears(range.getMax() + 1);
    }

    private void separationEvent(final Map<Integer, List<IPerson>> continuingPartneredFemalesByChildren) {

        // Consideration of separation is based on number of children in females current partnerships
        for (final Map.Entry<Integer, List<IPerson>> entry : continuingPartneredFemalesByChildren.entrySet()) {

            final int numberOfChildren = entry.getKey();
            int ageOfMothers = 0;

            // Get mothers with given number of children in current partnership
            final List<IPerson> mothers = entry.getValue();

            if (!mothers.isEmpty())
                ageOfMothers = ageOnDate(mothers.getFirst(), currentDate);

            // Get determined count for separations for this group of mothers
            final SeparationStatsKey key = new SeparationStatsKey(numberOfChildren, ageOfMothers, mothers.size(), config.getSimulationTimeStep(), currentDate);
            final SingleDeterminedCount dC = (SingleDeterminedCount) desiredStatistics.getDeterminedCount(key, config);

            int count = 0;

            // For each mother in this group
            for (final IPerson mother : mothers) {

                // If enough mothers have been separated then break
                if (count >= dC.getDeterminedCount()) break;

                // else mark partnership for separation
                separate(getLastPartnership(mother), getLastChild(mother).getBirthDate());
                count++;
            }

            // Return achieved statistics to the statistics handler
            dC.setFulfilledCount(count);
            desiredStatistics.returnAchievedCount(dC);
        }
    }

    private int killPeople(final Collection<IPerson> people) {

        int killed = 0;

        for (final IPerson person : people) {

            // choose date of death
            final LocalDate deathDate = deathDateSelector.selectDate(person, desiredStatistics, currentDate, config.getSimulationTimeStep());

            final int ageAtDeath = Period.between(person.getBirthDate(), deathDate).getYears();
            final String deathCause = desiredStatistics.getDeathCauseRates(Year.of(deathDate.getYear()), person.getSex(), ageAtDeath).getSample();

            person.setDeathDate(deathDate);
            person.setDeathCause(deathCause);

            for (final IPartnership partnership : person.getPartnerships())
                handleSeparationMoves(partnership, partnership.getPartnerOf(person));

            final Address lastAddress = person.getAddress(deathDate);
            if (lastAddress != null)
                lastAddress.removeInhabitant(person);

            killed++;

            // move person to correct place in data structure
            population.getDeadPeople().add(person);
        }

        return killed;
    }

    private PersonCollection getLivingPeopleOfSex(final SexOption sex) {

        final PeopleCollection livingPeople = population.getLivingPeople();
        return sex == SexOption.MALE ? livingPeople.getMales() : livingPeople.getFemales();
    }

    private boolean needsNewPartner(final IPerson person, final LocalDate currentDate) {

        return person.getPartnerships().isEmpty() || partnersToSeparate.contains(person) || lastPartnerDied(person, currentDate);
    }

    private void separate(final IPartnership partnership, final LocalDate earliestPossibleSeparationDate) {

        partnership.setEarliestPossibleSeparationDate(earliestPossibleSeparationDate);

        partnersToSeparate.add(partnership.getFemalePartner());
        partnersToSeparate.add(partnership.getMalePartner());
    }

    public void recordOutOfMemorySummary() {

        summary.setCompleted(false);
        summary.setPeakPop(population.getPopulationCounts().getPeakPopulationSize());
        summary.setEligibilityChecks(population.getPopulationCounts().getEligibilityChecks());
        summary.setFailedEligibilityChecks(population.getPopulationCounts().getFailedEligibilityChecks());
        summary.setTotalPop(population.getPeople(config.getT0(), config.getTE(), Period.ofYears(MAXIMUM_AGE_AT_DEATH)).getNumberOfPeople());
        summary.setSimRunTime(simTimer.getRunTimeSeconds());
        summary.setMaxMemoryUsage(MemoryUsageAnalysis.getMaxSimUsage());
        MemoryUsageAnalysis.reset();
    }

    private void recordSummary() {

        summary.setCompleted(true);
        summary.setEndPop(population.getLivingPeople().getNumberOfPeople());
        summary.setPeakPop(population.getPopulationCounts().getPeakPopulationSize());
        summary.setEligibilityChecks(population.getPopulationCounts().getEligibilityChecks());
        summary.setFailedEligibilityChecks(population.getPopulationCounts().getFailedEligibilityChecks());
    }

    private void recordFinalSummary() {

        MemoryUsageAnalysis.log();

        summary.setTotalPop(population.getPeople(config.getT0(), config.getTE(), Period.ofYears(MAXIMUM_AGE_AT_DEATH)).getNumberOfPeople());
        summary.setSimRunTime(simTimer.getRunTimeSeconds());
    }

    private void logResults() {

        log.info("TKilled\t" + deathCount);
        log.info("TBorn\t" + birthsCount);
        log.info("Ratio\t" + deathCount / (double) birthsCount);
    }

    private int calculateNumberToHaveEvent(final double eventRate) {

        double toHaveEvent = currentHypotheticalPopulationSize * eventRate;
        int flooredToHaveEvent = (int) toHaveEvent;
        toHaveEvent -= flooredToHaveEvent;

        // this is a random dice roll to see if the fraction of a has the event or not

        if (Randomness.getRandomGenerator().nextInt(100) < toHaveEvent * 100)
            flooredToHaveEvent++;

        return flooredToHaveEvent;
    }

    private class ProposedPartnership {

        private final IPerson male;
        private final IPerson female;
        private final int numberOfChildren;

        ProposedPartnership(final IPerson male, final IPerson female, final int numberOfChildren) {

            this.male = male;
            this.female = female;
            this.numberOfChildren = numberOfChildren;

            partnersToSeparate.remove(male);
            partnersToSeparate.remove(female);
        }
    }

    private static class NewMother {

        private final IPerson newMother;
        private final int numberOfChildrenInMaternity;

        NewMother(final IPerson newMother, final int numberOfChildrenInMaternity) {

            this.newMother = newMother;
            this.numberOfChildrenInMaternity = numberOfChildrenInMaternity;
        }
    }

    private static class MothersNeedingPartners {

        private final List<NewMother> mothers;

        // This includes those added to existing partnerships and those marked for creation in the imminent partnering step
        private final int newlyProducedChildren;

        MothersNeedingPartners() {
            this(new ArrayList<>(), 0);
        }

        MothersNeedingPartners(final List<NewMother> mothers, final int newlyProducedChildren) {

            this.mothers = mothers;
            this.newlyProducedChildren = newlyProducedChildren;
        }
    }
}
