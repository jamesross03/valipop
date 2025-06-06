package uk.ac.standrews.cs.valipop.implementations;

import org.apache.commons.math3.random.RandomGenerator;
import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.simulationEntities.Partnership;
import uk.ac.standrews.cs.valipop.simulationEntities.PopulationNavigation;
import uk.ac.standrews.cs.valipop.simulationEntities.dataStructure.Population;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SexOption;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.PopulationStatistics;
import uk.ac.standrews.cs.valipop.utils.addressLookup.Address;
import uk.ac.standrews.cs.valipop.utils.addressLookup.ForeignGeography;
import uk.ac.standrews.cs.valipop.utils.addressLookup.Geography;

import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class BalancedMigrationModel {

    private final Population population;
    private final RandomGenerator randomNumberGenerator;
    private final Geography geography;

    private final ForeignGeography foreignGeography;

    private final PersonFactory personFactory;
    private final PopulationStatistics desired;

    public BalancedMigrationModel(Population population, RandomGenerator randomNumberGenerator, Geography geography, PersonFactory personFactory, PopulationStatistics desired) {
        this.population = population;
        this.randomNumberGenerator = randomNumberGenerator;
        this.geography = geography;
        this.personFactory = personFactory;
        this.desired = desired;
        foreignGeography = new ForeignGeography(randomNumberGenerator);
    }

    public void performMigration(LocalDate currentTime, OBDModel model) {

        int numberToMigrate = Math.toIntExact(Math.round(population.getLivingPeople().getNumberOfPeople()
                * model.getDesiredPopulationStatistics()
                .getMigrationRateDistribution(Year.of(currentTime.getYear()))
                .getRate(0)));

        Collection<List<IPerson>> peopleToMigrate = new ArrayList<>();
        List<IPerson> livingPeople = new ArrayList<>(population.getLivingPeople().getPeople());

        HashSet<IPerson> theMigrated = new HashSet<>();
        // select people to move out of country
        while(theMigrated.size() < numberToMigrate) {


            IPerson selected;
            do {
                int random = randomNumberGenerator.nextInt(livingPeople.size());
                selected = livingPeople.get(random);
            } while(theMigrated.contains(selected));

            theMigrated.add(selected);

            LocalDate moveDate = getMoveDate(currentTime, selected);
            Address currentAbode = selected.getAddress(moveDate);
            boolean withHousehold = migrateWithHousehold(currentTime, selected, currentAbode);

            List<IPerson> household = new ArrayList<>();

            if(withHousehold) {
                Address emigrateTo = null;

                while(!currentAbode.getInhabitants().isEmpty()) {
                    IPerson person = currentAbode.getInhabitants().get(0);

                    IPerson lastChild = PopulationNavigation.getLastChild(person);

                    LocalDate personalMoveDate = moveDate;

                    if(lastChild != null && !household.contains(lastChild) && !currentAbode.getInhabitants().contains(lastChild) && lastChild.getDeathDate() == null) {
                        // if emigrating persons last child exists and is not emigrating in this group and is not dead
                        // we need to make sure that this last child was not conceived after the father left
                        personalMoveDate = checkConceptionBeforeMove(currentTime, moveDate, lastChild, personalMoveDate);
                    }
                    emigrateTo = emigratePerson(personalMoveDate, currentAbode, household, person, emigrateTo, model);
                }

                peopleToMigrate.add(household);
                theMigrated.addAll(household);

            } else {
                IPerson lastChild = PopulationNavigation.getLastChild(selected);

                LocalDate personalMoveDate = moveDate;

                if(lastChild != null && lastChild.getDeathDate() == null) {
                    // if emigrating persons last child exists and is not dead
                    // we need to make sure that this last child was not conceived after the father left
                    personalMoveDate = checkConceptionBeforeMove(currentTime, moveDate, lastChild, personalMoveDate);
                }

                emigratePerson(personalMoveDate, currentAbode, household, selected, model);
                peopleToMigrate.add(household);
            }

        }

        // create immigrants by approximately mimicing the emigrants
        for(List<IPerson> household : peopleToMigrate) {

            if (household.size() == 1) {
                IPerson toMimic = household.getFirst();
                IPerson mimic = mimicPerson(toMimic, null, new HashMap<>());

                immigratePerson(mimic, toMimic);

            } else {

                Map<IPartnership, IPartnership> mimicLookup = new HashMap<>();
                Map<IPerson, IPerson> mimicPersonLookup = new HashMap<>();

                household.sort((o1, o2) -> -o1.getBirthDate().compareTo(o2.getBirthDate()));

                Address newHouse = geography.getRandomEmptyAddress();
                Address oldCountry = foreignGeography.getCountry();

                for (IPerson p : household) {

                    IPerson mimic = mimicPersonLookup.get(p);

                    if (mimic == null) {

                        IPartnership mimicParents = mimicLookup.get(p.getParents());

                        if (mimicParents == null) {
                            mimic = mimicPerson(p, null, mimicPersonLookup);

                            mimicLookup.put(p.getParents(), mimic.getParents());

                            if (p.getParents() != null) {
                                if (p.getParents().getMalePartner() != null)
                                    mimicPersonLookup.put(p.getParents().getMalePartner(), mimic.getParents().getMalePartner());

                                if (p.getParents().getFemalePartner() != null)
                                    mimicPersonLookup.put(p.getParents().getFemalePartner(), mimic.getParents().getFemalePartner());
                            }

                        } else {
                            mimic = mimicPerson(p, mimicParents, mimicPersonLookup);
                        }

                    } else {

                        if (mimic.getParents() == null) {
                            IPartnership parents = mimicParents(p, mimicPersonLookup);
                            mimic.setParents(parents);
                            if (parents != null) {
                                parents.addChildren(List.of(mimic));
                            }
                        }
                    }

                    LocalDate arrivalDate = p.getEmigrationDate().isBefore(mimic.getBirthDate()) ? mimic.getBirthDate() : p.getEmigrationDate();

                    mimic.setImmigrationDate(arrivalDate);

                    if (arrivalDate.isAfter(mimic.getBirthDate())) {
                        mimic.setAddress(mimic.getBirthDate(), oldCountry);
                    }

                    mimic.setAddress(arrivalDate, newHouse);
                }

                for (IPerson p : mimicPersonLookup.values()) {

                    if (!newHouse.getInhabitants().contains(p))
                        p.setPhantom(true);
                }
            }
        }
    }

    private LocalDate checkConceptionBeforeMove(LocalDate currentTime, LocalDate moveDate, IPerson lastChild, LocalDate personalMoveDate) {
        LocalDate conception = lastChild.getBirthDate().minus(desired.getMinGestationPeriod());
        if (moveDate.isBefore(conception)) {
            int windowInDays = (int) conception.until(currentTime.plus(1, ChronoUnit.YEARS), ChronoUnit.DAYS) - 1;

            personalMoveDate = conception.plus(randomNumberGenerator.nextInt(windowInDays), ChronoUnit.DAYS);
        }
        return personalMoveDate;
    }

    private void immigratePerson(IPerson person, IPerson toMimic) {
        population.getLivingPeople().add(person);
        LocalDate arrivalDate = toMimic.getEmigrationDate().isBefore(person.getBirthDate()) ? person.getBirthDate() : toMimic.getEmigrationDate();
        person.setImmigrationDate(arrivalDate);

        if(arrivalDate.isAfter(person.getBirthDate())) {
            person.setAddress(person.getBirthDate(), foreignGeography.getCountry());
        }

        person.setAddress(arrivalDate, geography.getRandomEmptyAddress());

    }

    private Address emigratePerson(LocalDate moveDate, Address currentAbode, List<IPerson> emigratingGroup, IPerson person, OBDModel model) {
        return emigratePerson(moveDate, currentAbode, emigratingGroup, person, null, model);
    }

    private Address emigratePerson(LocalDate moveDate, Address currentAbode, List<IPerson> emigratingGroup, IPerson person, Address emigrateTo, OBDModel model) {

        population.getLivingPeople().remove(person);

        population.getEmigrants().add(person);
        person.setEmigrationDate(moveDate.isBefore(person.getBirthDate()) ? person.getBirthDate() : moveDate);

        if(currentAbode != null) currentAbode.removeInhabitant(person);
        emigratingGroup.add(person);

        Address newCountry = emigrateTo;
        if(newCountry == null) newCountry = foreignGeography.getCountry();

        person.setAddress(moveDate, newCountry);
//        model.handleSeperationMoves(person.getLastPartnership(), person);

        return newCountry;
    }

    private LocalDate getMoveDate(LocalDate currentDate, IPerson person) {
        LocalDate moveDate;
        LocalDate lastMoveDate = person.getLastMoveDate();

        if(lastMoveDate != null && lastMoveDate.isAfter(currentDate.plus(1, ChronoUnit.YEARS))) {
            // last move is projected signifcantly into future - occurs when last partner dies and no future events are sin surviving partners timeline
            // therefore we rollback the future move and emmigrate as below
            person.cancelLastMove(geography);
            lastMoveDate = person.getLastMoveDate();
        }

        if(lastMoveDate != null && lastMoveDate.isAfter(currentDate)) {
            int excludedDays = (int) ChronoUnit.DAYS.between(currentDate, lastMoveDate);

            moveDate = lastMoveDate.plus(randomNumberGenerator.nextInt(366 - excludedDays), ChronoUnit.DAYS);
        } else {
            moveDate = currentDate.plus(randomNumberGenerator.nextInt(365), ChronoUnit.DAYS);
        }
        return moveDate;
    }

    private IPerson mimicPerson(IPerson person, IPartnership mimicedParents, Map<IPerson, IPerson> mimicPersonLookup) {

        boolean adulterousBirth = person.isAdulterousBirth();
        LocalDate birthDate = randomDateInYear(person.getBirthDate());
        IPartnership parents = mimicedParents;

        if(parents == null) {
            parents = mimicParents(person, mimicPersonLookup);
        }

        IPerson p = personFactory.makePerson(birthDate, parents, adulterousBirth, true, person.getSex());
        population.getLivingPeople().add(p);

        if(parents != null)
            parents.addChildren(Collections.singleton(p));

        return p;

    }

    private IPartnership mimicParents(IPerson person, Map<IPerson, IPerson> mimicPersonLookup) {
        IPartnership parents = null;
        IPartnership parentsToMimic = person.getParents();

        if(parentsToMimic != null) {
            IPerson fatherToMimic = parentsToMimic.getMalePartner();
            IPerson motherToMimic = parentsToMimic.getFemalePartner();

            // Record newly created parents in population

            IPerson mimicedFather = mimicPersonLookup.get(fatherToMimic);
            if (mimicedFather == null) {
                LocalDate birthDate = randomDateInYear(fatherToMimic.getBirthDate());
                mimicedFather = personFactory.makePerson(birthDate, null, fatherToMimic.isAdulterousBirth(), true, SexOption.MALE);
                population.getLivingPeople().add(mimicedFather);
            }

            IPerson mimicedMother = mimicPersonLookup.get(motherToMimic);
            if (mimicedMother == null) {
                LocalDate birthDate = randomDateInYear(motherToMimic.getBirthDate());
                mimicedMother = personFactory.makePerson(birthDate, null, motherToMimic.isAdulterousBirth(), true, SexOption.FEMALE);
                population.getLivingPeople().add(mimicedMother);
            }

            parents = new Partnership(mimicedFather, mimicedMother);
            parents.setPartnershipDate(parentsToMimic.getPartnershipDate());
            parents.setMarriageDate(parentsToMimic.getMarriageDate());

            mimicedFather.recordPartnership(parents);
            mimicedMother.recordPartnership(parents);
        }

        return parents;
    }

    private LocalDate randomDateInYear(LocalDate birthDate) {
        int year = birthDate.getYear();
        int day = randomNumberGenerator.nextInt(365);

        return LocalDate.of(year, 1, 1).plus(day, ChronoUnit.DAYS);
    }

    private boolean migrateWithHousehold(LocalDate currentDate, IPerson person, Address address) {

        boolean withHousehold = false;

        if(address != null && address.getInhabitants().size() > 1) {
            withHousehold = randomNumberGenerator.nextBoolean();
        }

        return withHousehold;
    }

}
