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

    public BalancedMigrationModel(final Population population, final RandomGenerator randomNumberGenerator, final Geography geography, final PersonFactory personFactory, final PopulationStatistics desired) {
        this.population = population;
        this.randomNumberGenerator = randomNumberGenerator;
        this.geography = geography;
        this.personFactory = personFactory;
        this.desired = desired;
        foreignGeography = new ForeignGeography(randomNumberGenerator);
    }

    public void performMigration(final LocalDate currentTime, final OBDModel model) {

        final double migrationRate = model.getDesiredPopulationStatistics().getMigrationRateDistribution(Year.of(currentTime.getYear())).getRate(0);
        final int numberOfPeople = population.getLivingPeople().getNumberOfPeople();
        final int numberToMigrate = Math.toIntExact(Math.round(numberOfPeople * migrationRate));

        final Collection<List<IPerson>> peopleToMigrate = new ArrayList<>();
        final List<IPerson> livingPeople = new ArrayList<>(population.getLivingPeople().getPeople());

        final HashSet<IPerson> theMigrated = new HashSet<>();
        // select people to move out of country
        while(theMigrated.size() < numberToMigrate) {


            IPerson selected;
            do {
                final int random = randomNumberGenerator.nextInt(livingPeople.size());
                selected = livingPeople.get(random);
            } while(theMigrated.contains(selected));

            theMigrated.add(selected);

            final LocalDate moveDate = getMoveDate(currentTime, selected);
            final Address currentAbode = selected.getAddress(moveDate);
            final boolean withHousehold = migrateWithHousehold(currentAbode);

            final List<IPerson> household = new ArrayList<>();

            if(withHousehold) {
                Address emigrateTo = null;

                while(!currentAbode.getInhabitants().isEmpty()) {
                    final IPerson person = currentAbode.getInhabitants().getFirst();

                    final IPerson lastChild = PopulationNavigation.getLastChild(person);

                    LocalDate personalMoveDate = moveDate;

                    if (lastChild != null && !household.contains(lastChild) && !currentAbode.getInhabitants().contains(lastChild) && lastChild.getDeathDate() == null) {
                        // if emigrating persons last child exists and is not emigrating in this group and is not dead
                        // we need to make sure that this last child was not conceived after the father left
                        personalMoveDate = checkConceptionBeforeMove(currentTime, moveDate, lastChild, personalMoveDate);
                    }
                    emigrateTo = emigratePerson(personalMoveDate, currentAbode, household, person, emigrateTo);
                }

                peopleToMigrate.add(household);
                theMigrated.addAll(household);

            } else {
                final IPerson lastChild = PopulationNavigation.getLastChild(selected);

                LocalDate personalMoveDate = moveDate;

                if (lastChild != null && lastChild.getDeathDate() == null) {
                    // if emigrating persons last child exists and is not dead
                    // we need to make sure that this last child was not conceived after the father left
                    personalMoveDate = checkConceptionBeforeMove(currentTime, moveDate, lastChild, personalMoveDate);
                }

                emigratePerson(personalMoveDate, currentAbode, household, selected);
                peopleToMigrate.add(household);
            }
        }

        // create immigrants by approximately mimicing the emigrants
        for (final List<IPerson> household : peopleToMigrate) {

            if (household.size() == 1) {
                final IPerson toMimic = household.getFirst();
                final IPerson mimic = mimicPerson(toMimic, null, new HashMap<>());

                immigratePerson(mimic, toMimic);

            } else {

                final Map<IPartnership, IPartnership> mimicLookup = new HashMap<>();
                final Map<IPerson, IPerson> mimicPersonLookup = new HashMap<>();

                household.sort((o1, o2) -> -o1.getBirthDate().compareTo(o2.getBirthDate()));

                final Address newHouse = geography.getRandomEmptyAddress();
                final Address oldCountry = foreignGeography.getCountry();

                for (final IPerson p : household) {

                    IPerson mimic = mimicPersonLookup.get(p);

                    if (mimic == null) {

                        final IPartnership mimicParents = mimicLookup.get(p.getParents());

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
                            final IPartnership parents = mimicParents(p, mimicPersonLookup, mimic.getSurname());

                            mimic.setParents(parents);

                            if (parents != null) {

                                population.getLivingPeople().remove(parents.getFemalePartner());
                                parents.addChildren(List.of(mimic));
                                population.getLivingPeople().add(parents.getFemalePartner());
                            }
                        }
                    }

                    final LocalDate arrivalDate = p.getEmigrationDate().isBefore(mimic.getBirthDate()) ? mimic.getBirthDate() : p.getEmigrationDate();

                    mimic.setImmigrationDate(arrivalDate);

                    if (arrivalDate.isAfter(mimic.getBirthDate())) {
                        mimic.setAddress(mimic.getBirthDate(), oldCountry);
                    }

                    mimic.setAddress(arrivalDate, newHouse);
                }

                for (final IPerson p : mimicPersonLookup.values()) {

                    if (!newHouse.getInhabitants().contains(p))
                        p.setPhantom(true);
                }
            }
        }
    }

    private LocalDate checkConceptionBeforeMove(final LocalDate currentTime, final LocalDate moveDate, final IPerson lastChild, LocalDate personalMoveDate) {

        final LocalDate conception = lastChild.getBirthDate().minus(desired.getMinGestationPeriod());
        if (moveDate.isBefore(conception)) {
            final int windowInDays = (int) conception.until(currentTime.plusYears(1L), ChronoUnit.DAYS) - 1;

            personalMoveDate = conception.plusDays(randomNumberGenerator.nextInt(windowInDays));
        }
        return personalMoveDate;
    }

    private void immigratePerson(final IPerson person, final IPerson toMimic) {

        population.getLivingPeople().add(person);
        final LocalDate arrivalDate = toMimic.getEmigrationDate().isBefore(person.getBirthDate()) ? person.getBirthDate() : toMimic.getEmigrationDate();
        person.setImmigrationDate(arrivalDate);

        if (arrivalDate.isAfter(person.getBirthDate())) {
            person.setAddress(person.getBirthDate(), foreignGeography.getCountry());
        }

        person.setAddress(arrivalDate, geography.getRandomEmptyAddress());
    }

    private void emigratePerson(final LocalDate moveDate, final Address currentAbode, final List<IPerson> emigratingGroup, final IPerson person) {
        emigratePerson(moveDate, currentAbode, emigratingGroup, person, null);
    }

    private Address emigratePerson(final LocalDate moveDate, final Address currentAbode, final List<IPerson> emigratingGroup, final IPerson person, final Address emigrateTo) {

        population.getLivingPeople().remove(person);

        population.getEmigrants().add(person);
        person.setEmigrationDate(moveDate.isBefore(person.getBirthDate()) ? person.getBirthDate() : moveDate);

        if (currentAbode != null) currentAbode.removeInhabitant(person);
        emigratingGroup.add(person);

        Address newCountry = emigrateTo;
        if (newCountry == null) newCountry = foreignGeography.getCountry();

        person.setAddress(moveDate, newCountry);

        return newCountry;
    }

    private LocalDate getMoveDate(final LocalDate currentDate, final IPerson person) {

        final LocalDate moveDate;
        LocalDate lastMoveDate = person.getLastMoveDate();

        if (lastMoveDate != null && lastMoveDate.isAfter(currentDate.plusYears(1))) {
            // last move is projected significantly into future - occurs when last partner dies and no future events are in surviving partners timeline
            // therefore we rollback the future move and emigrate as below
            person.cancelLastMove(geography);
            lastMoveDate = person.getLastMoveDate();
        }

        if (lastMoveDate != null && lastMoveDate.isAfter(currentDate)) {
            final int excludedDays = (int) ChronoUnit.DAYS.between(currentDate, lastMoveDate);

            moveDate = lastMoveDate.plusDays(randomNumberGenerator.nextInt(366 - excludedDays));
        } else {
            moveDate = currentDate.plusDays(randomNumberGenerator.nextInt(365));
        }
        return moveDate;
    }

    private IPerson mimicPerson(final IPerson person, final IPartnership mimicedParents, final Map<IPerson, IPerson> mimicPersonLookup) {

        final boolean adulterousBirth = person.isAdulterousBirth();
        final LocalDate birthDate = randomDateInYear(person.getBirthDate());
        IPartnership parents = mimicedParents;

        if (parents == null) {
            parents = mimicParents(person, mimicPersonLookup, null);
        }

        final IPerson p = personFactory.makePerson(birthDate, parents, adulterousBirth, true, person.getSex());
        population.getLivingPeople().add(p);

        if (parents != null) {
            population.getLivingPeople().remove(parents.getFemalePartner());
            parents.addChildren(Collections.singleton(p));
            population.getLivingPeople().add(parents.getFemalePartner());
        }

        return p;
    }

    private IPartnership mimicParents(final IPerson person, final Map<IPerson, IPerson> mimicPersonLookup, final String fatherSurname) {

        IPartnership parents = null;
        final IPartnership parentsToMimic = person.getParents();

        if (parentsToMimic != null) {
            final IPerson fatherToMimic = parentsToMimic.getMalePartner();
            final IPerson motherToMimic = parentsToMimic.getFemalePartner();

            // Record newly created parents in population

            IPerson mimicedFather = mimicPersonLookup.get(fatherToMimic);
            if (mimicedFather == null) {
                final LocalDate birthDate = randomDateInYear(fatherToMimic.getBirthDate());
                mimicedFather = personFactory.makePerson(birthDate, null, fatherToMimic.isAdulterousBirth(), true, SexOption.MALE, fatherSurname);
            }

            IPerson mimicedMother = mimicPersonLookup.get(motherToMimic);
            if (mimicedMother == null) {
                final LocalDate birthDate = randomDateInYear(motherToMimic.getBirthDate());
                mimicedMother = personFactory.makePerson(birthDate, null, motherToMimic.isAdulterousBirth(), true, SexOption.FEMALE);
            }

            parents = new Partnership(mimicedFather, mimicedMother);
            parents.setPartnershipDate(parentsToMimic.getPartnershipDate());
            parents.setMarriageDate(parentsToMimic.getMarriageDate());

            mimicedFather.recordPartnership(parents);
            mimicedMother.recordPartnership(parents);

            population.getLivingPeople().add(parents);
            population.getLivingPeople().add(mimicedFather);
            population.getLivingPeople().add(mimicedMother);
        }

        return parents;
    }

    private LocalDate randomDateInYear(final LocalDate birthDate) {

        final int year = birthDate.getYear();
        final int day = randomNumberGenerator.nextInt(365);

        return LocalDate.of(year, 1, 1).plusDays(day);
    }

    private boolean migrateWithHousehold(final Address address) {

        return address != null && address.getInhabitants().size() > 1 && randomNumberGenerator.nextBoolean();
    }
}
