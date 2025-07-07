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
package uk.ac.standrews.cs.valipop.simulationEntities.dataStructure;

import uk.ac.standrews.cs.valipop.Config;
import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.simulationEntities.IPersonCollection;
import uk.ac.standrews.cs.valipop.simulationEntities.PopulationNavigation;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SexOption;
import uk.ac.standrews.cs.valipop.utils.addressLookup.Address;
import uk.ac.standrews.cs.valipop.utils.addressLookup.DistanceSelector;
import uk.ac.standrews.cs.valipop.utils.addressLookup.Geography;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

/**
 * Implementation of PersonCollection, storing people by gender.
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class PeopleCollection extends PersonCollection implements Cloneable, IPersonCollection {

    private final MaleCollection males;
    private final FemaleCollection females;

    private final Map<Integer, IPartnership> partnershipIndex = new HashMap<>();

    /**
     * Instantiates a new PersonCollection. The dates specify the earliest and latest expected birth dates of
     * individuals in the PersonCollection. There is no hard enforcement of this as the bounds are intended to serve
     * mainly as a guide for when other things make use of the PersonCollection - e.g. producing plots, applying
     * validation statistics.
     *
     * @param start the start
     * @param end   the end
     */
    public PeopleCollection(final LocalDate start, final LocalDate end, final Period divisionSize, final String description) {

        super(start, end, divisionSize, description);

        males = new MaleCollection(start, end, divisionSize, description);
        females = new FemaleCollection(start, end, divisionSize, description);
    }

    @Override
    public PeopleCollection clone() {

        final PeopleCollection clone = new PeopleCollection(getStartDate(), getEndDate(), getDivisionSize(), description);

        for (final IPerson person : males.getPeople()) {
            clone.add(person);
        }

        for (final IPerson person : females.getPeople()) {
            clone.add(person);
        }

        for (final IPartnership partnership : partnershipIndex.values()) {
            clone.add(partnership);
        }

        return clone;
    }

    public MaleCollection getMales() {
        return males;
    }

    public FemaleCollection getFemales() {
        return females;
    }

    public void add(final IPartnership partnership) {

        partnershipIndex.put(partnership.getId(), partnership);
    }

    public void removeMales(final int numberToRemove, final LocalDate firstDate, final Period timePeriod, final boolean bestAttempt, final Geography geography, final DistanceSelector moveDistanceSelector, final Config config) throws InsufficientNumberOfPeopleException {

        removePeople(males, numberToRemove, firstDate, timePeriod, bestAttempt, geography, moveDistanceSelector, config);
    }

    public void removeFemales(final int numberToRemove, final LocalDate firstDate, final Period timePeriod, final boolean bestAttempt, final Geography geography, final DistanceSelector moveDistanceSelector, final Config config) throws InsufficientNumberOfPeopleException {

        removePeople(females, numberToRemove, firstDate, timePeriod, bestAttempt, geography, moveDistanceSelector, config);
    }

    @Override
    public Collection<IPerson> getPeople() {

        final Collection<IPerson> people = females.getPeople();
        people.addAll(males.getPeople());

        return people;
    }

    @Override
    public Collection<IPerson> getPeopleBornInTimePeriod(final LocalDate firstDate, final Period timePeriod) {

        final Collection<IPerson> people = females.getPeopleBornInTimePeriod(firstDate, timePeriod);
        people.addAll(males.getPeopleBornInTimePeriod(firstDate, timePeriod));

        return people;
    }

    @Override
    public Collection<IPerson> getPeopleAliveInTimePeriod(final LocalDate firstDate, final Period timePeriod, final Period maxAge) {

        // TODO try to reduce amount of copying.
        final Collection<IPerson> people = females.getPeopleAliveInTimePeriod(firstDate, timePeriod, maxAge);
        people.addAll(males.getPeopleAliveInTimePeriod(firstDate, timePeriod, maxAge));

        return people;
    }

    @Override
    public void add(final IPerson person) {

        if (person.getSex() == SexOption.MALE) {
            males.add(person);

        } else {
            females.add(person);
        }
    }

    @Override
    public void remove(final IPerson person) {

        if (person.getSex() == SexOption.MALE) {
            males.remove(person);

        } else {
            females.remove(person);
        }
    }

    @Override
    public int getNumberOfPeople() {

        return females.getNumberOfPeople() + males.getNumberOfPeople();
    }

    @Override
    public int getNumberOfPeople(final LocalDate firstDate, final Period timePeriod) {

        return females.getNumberOfPeople(firstDate, timePeriod) + males.getNumberOfPeople(firstDate, timePeriod);
    }

    @Override
    public Set<LocalDate> getDivisionDates() {
        return females.getDivisionDates();
    }


    @Override
    public Iterable<IPartnership> getPartnerships() {

        return partnershipIndex.values();
    }

    @Override
    public IPerson findPerson(final int id) {

        for (final IPerson person : males.getPeople()) {
            if (person.getId() == id) return person;
        }

        for (final IPerson person : females.getPeople()) {
            if (person.getId() == id) return person;
        }
        return null;
    }

    @Override
    public IPartnership findPartnership(final int id) {
        return partnershipIndex.get(id);
    }

    @Override
    public int getNumberOfPartnerships() {
        return partnershipIndex.size();
    }

    @Override
    public void setDescription(final String description) {
        this.description = description;
    }

    public String toString() {
        return description;
    }

    private void removePeople(final PersonCollection collection, final int numberToRemove, final LocalDate firstDate, final Period timePeriod, final boolean bestAttempt, final Geography geography, final DistanceSelector moveDistanceSelector, final Config config) throws InsufficientNumberOfPeopleException {

        final TreeSet<IPerson> removed = collection.removeNPersons(numberToRemove, firstDate, timePeriod, true);

        for (final IPerson person : removed) {
            removeChildFromParentsPartnership(person, geography, moveDistanceSelector, config);

            for (final Address address : person.getAllAddresses())
                address.removeInhabitant(person);
        }
    }

    private void removeChildFromParentsPartnership(final IPerson person, final Geography geography, final DistanceSelector moveDistanceSelector, final Config config) {

        final IPartnership parents = person.getParents();

        if (parents != null) {
            final IPerson mother = parents.getFemalePartner();

            if (!nonImmigratingMotherOfImmigrantPerson(mother, person))
                remove(mother);

            parents.getChildren().remove(person);

            person.cancelLastMove(geography);

            if (parents.getChildren().isEmpty()) {

                cancelPartnership(parents);

                final IPartnership mothersLastPartnership = PopulationNavigation.getLastPartnership(mother);

                if (mothersLastPartnership == null) {
                    parents.getFemalePartner().rollbackLastMove(geography);
                }
                else if (!person.isAdulterousBirth()) {
                    parents.getMalePartner().rollbackLastMove(geography);
                    parents.getFemalePartner().rollbackLastMove(geography);

                    // if mother now has no address history (but has previous partnership)
                    if(parents.getFemalePartner().getAllAddresses().isEmpty()) {
                        // we need to provide an address for her and (adulterousBirth - only way this scenario can arise) child

                        final IPerson adulterousBirth = mothersLastPartnership.getChildren().getFirst();

                        if(adulterousBirth.getParents().getMalePartner().getPartnerships().size() > 1) {
                            // we should make it at a distance from the childs father

                            final Address newAddress;

                            if(adulterousBirth.getParents().getMalePartner().getAddress(adulterousBirth.getBirthDate().minus(config.getMinGestationPeriod())) == null) {
                                // in this case the nature of assigning N birth events to a time period means it appears the father did not have an address at the time of birth
                                // however as he produces ligitmiate children later in the year the earlier part of the simulation will have taken the address at that point
                                // as the origin address for choosing the address for the adulterousBirth child and mother - we will do that again in this case
                                newAddress = geography.getNearestEmptyAddressAtDistance(
                                        adulterousBirth.getParents().getMalePartner().getAddressHistory()
                                                .ceilingEntry(adulterousBirth.getBirthDate().minus(config.getMinGestationPeriod())).getValue()
                                                .getArea().getCentroid()
                                        , moveDistanceSelector.selectRandomDistance());
                            } else {
                                // in this case the father is where he is supposed to be!

                                newAddress = geography.getNearestEmptyAddressAtDistance(
                                        adulterousBirth.getParents().getMalePartner()
                                                .getAddress(adulterousBirth.getBirthDate().minus(config.getMinGestationPeriod()))
                                                .getArea().getCentroid(),
                                        moveDistanceSelector.selectRandomDistance());
                            }
                            parents.getFemalePartner().setAddress(adulterousBirth.getBirthDate(), newAddress);

                            for (final IPerson child : mothersLastPartnership.getChildren())
                                child.setAddress(child.getBirthDate(), newAddress);
                        } else {
                            // In this case the mother in currently having her just created ligitimate partnership rolled back
                            // this leaves here with just a previous adulterousBirth child
                            // however the father of the adulterousBirth child has also had his legitiamate partnership
                            // (i.e the one which makes this child to be adulterousBirth)
                            // rolled back
                            // Thus... the only partnerships the mother and father have are the one producing the adulterousBirth child
                            // Thus making it legitimate, so we're going to make the child legitimate and put them all in a house together
                            // Also this only happens early in the sim (i.e. pre T0) so we can just set move in date based on childs birth date

                            adulterousBirth.setAdulterousBirth(false);

                            final Address newAddress = geography.getRandomEmptyAddress();
                            adulterousBirth.setAddress(adulterousBirth.getBirthDate(), newAddress);
                            adulterousBirth.getParents().getMalePartner().setAddress(adulterousBirth.getBirthDate(), newAddress);
                            adulterousBirth.getParents().getFemalePartner().setAddress(adulterousBirth.getBirthDate(), newAddress);
                        }
                    }
                }
            }

            add(mother);
        }
    }

    private boolean nonImmigratingMotherOfImmigrantPerson(final IPerson mother, final IPerson person) {

        if (person.getImmigrationDate() == null) {
            return false;
        } else return mother.getImmigrationDate() == null;
    }

    private void cancelPartnership(final IPartnership partnership) {

        // remove from parents partnership history
        partnership.getMalePartner().getPartnerships().remove(partnership);
        partnership.getFemalePartner().getPartnerships().remove(partnership);

        // remove partnership from index
        partnershipIndex.remove(partnership.getId());
    }
}
