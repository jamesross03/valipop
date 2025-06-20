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

import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.utils.specialTypes.dates.DateUtils;
import uk.ac.standrews.cs.valipop.utils.specialTypes.dates.MisalignedTimeDivisionException;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

import static uk.ac.standrews.cs.valipop.simulationEntities.PopulationNavigation.diedAfter;

/**
 * A PersonCollection contains a set of collections of people where the collections are organised by the year of birth
 * of the person.
 *
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public abstract class PersonCollection implements Iterable<IPerson> {

    // TODO rationalise with PeopleCollection

    private LocalDate startDate;
    private LocalDate endDate;
    private Period divisionSize;
    protected String description;
    protected int size = 0;

    /**
     * Instantiates a new PersonCollection. The dates specify the earliest and latest expected birth dates of
     * individuals in the PersonCollection. There is no hard enforcement of this as the bounds are intended to serve
     * mainly as a guide for when other things make use of the PersonCollection - e.g. producing plots, applying
     * validation statistics.
     *
     * @param startDate the start date
     * @param endDate   the end date
     */
    PersonCollection(LocalDate startDate, LocalDate endDate, Period divisionSize, String description) {

        this.startDate = startDate;
        this.endDate = endDate;
        this.divisionSize = divisionSize;
        this.description = description;
    }

    /**
     * Gets all the people that exist in the underlying sub-structure of this PersonCollection. Likely to be expensive,
     * if needing a count of people in collection then check to see if the instance has an index and take a count using
     * the size of the index.
     *
     * @return All people in the PersonCollection
     */
    public abstract Iterable<IPerson> getPeople();

    /**
     * Adds the given person to the PersonCollection.
     *
     * @param person the person to be added
     */
    abstract void add(final IPerson person);

    /**
     * Removes the specified person from this PersonCollection.
     *
     * @param person the person to be removed
     * @throws PersonNotFoundException If the specified person is not found then an exception is thrown
     */
    abstract void remove(final IPerson person) throws PersonNotFoundException;

    /**
     * Counts and returns the number of people born in the given year in the PersonCollection. This may be very
     * expensive as it may involve combining the counts of many under-lying Collection objects. If the instance contains
     * an index it will likely be more efficient to take the size of the index as the count.
     *
     * @return the number of persons in the PersonCollection
     */
    public abstract int getNumberOfPeople(final LocalDate firstDate, final Period timePeriod);

    public abstract Set<LocalDate> getDivisionDates();

    /**
     * Returns the number of people.
     *
     * @return the number of persons
     */
    public int getNumberOfPeople() {
        return size;
    }

    /**
     * Gets all the people in the PersonCollection who were alive in the given years.
     *
     * @param firstDate the year of birth of the desired cohort
     * @return the desired cohort
     */
    Collection<IPerson> getPeopleAliveInTimePeriod(final LocalDate firstDate, final Period timePeriod, final Period maxAge) {

        Collection<IPerson> peopleAlive = new ArrayList<>();

        Collection<IPerson> peopleBorn = getPeopleBornInTimePeriod(firstDate.minus(maxAge), timePeriod.plus(maxAge));

        for (IPerson person : peopleBorn) {
            if (diedAfter(person, firstDate)) {
                peopleAlive.add(person);
            }
        }

        return peopleAlive;
    }

    /**
     * Removes n people with the specified year of birth from the PersonCollection. If there are not enough people then
     * an exception is thrown.
     *
     * @param numberToRemove the number of people to remove
     * @param firstDate      the year of birth of those to remove
     * @param bestAttempt    returns the people that do exist even if there is not enough to meet numberToRemove
     * @return the random Collection of people who have been removed
     * @throws InsufficientNumberOfPeopleException If there are less people alive for the given year of birth than
     */
    public TreeSet<IPerson> removeNPersons(final int numberToRemove, final LocalDate firstDate, final Period timePeriod, final boolean bestAttempt) throws InsufficientNumberOfPeopleException {

        final int divisionsInPeriod = DateUtils.calcSubTimeUnitsInTimeUnit(divisionSize, timePeriod);

        if (divisionsInPeriod <= 0) {
            throw new MisalignedTimeDivisionException();
        }

        final TreeSet<IPerson> people = new TreeSet<>();
        LocalDate divisionDate = firstDate;

        final LinkedList<LocalDate> reusableDivisions = new LinkedList<>();

        // find all the division dates
        for (int i = 0; i < divisionsInPeriod; i++) {
            reusableDivisions.add(divisionDate);
            divisionDate = divisionDate.plus(getDivisionSize());
        }

        // this by design rounds down
        int numberToRemoveFromDivision = (numberToRemove - people.size()) / reusableDivisions.size();

        // check variables to decide when to recalculate number to remove from each division at the current iteration
        int numberOfReusableDivisions = reusableDivisions.size();
        int divisionsUsed = 0;

        while (people.size() < numberToRemove) {

            if (reusableDivisions.isEmpty()) {
                if (bestAttempt) {
                    return people;
                } else {
                    throw new InsufficientNumberOfPeopleException("Not enought people in time period to meet request of " +
                            numberToRemove + " females from " + firstDate + " and following time period " + timePeriod);
                }
            }

            // If every division has been sampled at the current level then and we are still short of people then we
            // need to recalculate the number to take from each division
            if (divisionsUsed == numberOfReusableDivisions) {
                // Reset check variables
                numberOfReusableDivisions = reusableDivisions.size();
                divisionsUsed = 0;

                final double tempNumberToRemoveFromDivisions = (numberToRemove - people.size()) / (double) reusableDivisions.size();
                if (tempNumberToRemoveFromDivisions < 1) {
                    // in the case where we are down to the last couple of people (defined by the current number of
                    // reusable divisions minus 1) we proceed to remove 1 person from each interval in turn until we
                    // reach the required number of people to be removed.
                    numberToRemoveFromDivision = (int) Math.ceil(tempNumberToRemoveFromDivisions);
                } else {
                    numberToRemoveFromDivision = (int) tempNumberToRemoveFromDivisions;
                }
            }

            // dequeue division
            final LocalDate consideredDivision = reusableDivisions.removeFirst();
            divisionsUsed++;

            final Collection<IPerson> selectedPeople = removeNPersonsFromDivision(numberToRemoveFromDivision, consideredDivision);
            people.addAll(selectedPeople);

            // if more people in division keep note in case of shortfall in other divisions
            if (selectedPeople.size() >= numberToRemoveFromDivision) {
                // enqueue division is still containing people
                reusableDivisions.addLast(consideredDivision);
            }
        }

        return people;
    }

    /**
     * Gets all the people in the PersonCollection who were born in the given years.
     *
     * @param firstDate the year of birth of the desired cohort
     * @return the desired cohort
     */
    public Collection<IPerson> getPeopleBornInTimePeriod(final LocalDate firstDate, final Period timePeriod) {

        final Collection<IPerson> people = new ArrayList<>();

        final int divisionsInPeriod = DateUtils.calcSubTimeUnitsInTimeUnit(getDivisionSize(), timePeriod);

        if (divisionsInPeriod <= 0) {
            throw new MisalignedTimeDivisionException();
        }

        LocalDate divisionDate = firstDate;

        // for all the division dates
        for (int i = 0; i < divisionsInPeriod; i++) {

            addPeople(people, divisionDate);

            divisionDate = divisionDate.plus(getDivisionSize());
        }

        return people;
    }

    @Override
    public Iterator<IPerson> iterator() {
        return getPeople().iterator();
    }

    void addPeople(final Collection<IPerson> people, final LocalDate divisionDate) {}

    private TreeSet<IPerson> removeNPersonsFromDivision(final int numberToRemove, final LocalDate divisionDate) {

        final TreeSet<IPerson> selectedPeople = new TreeSet<>();

        if (numberToRemove == 0) {
            return selectedPeople;
        }

        final LinkedList<IPerson> cohort = new LinkedList<>(getPeopleBornInTimePeriod(divisionDate, divisionSize));

        while (selectedPeople.size() < numberToRemove) {

            if (cohort.isEmpty()) {
                return selectedPeople;
            }

            final IPerson person = cohort.removeFirst();

            remove(person);
            selectedPeople.add(person);
        }

        return selectedPeople;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setStartDate(final LocalDate startDate) {
        this.startDate = startDate;
    }

    void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    Period getDivisionSize() {
        return divisionSize;
    }

    public Set<LocalDate> getDivisionDates(final Period forTimeStep) {

        final int jump = DateUtils.calcSubTimeUnitsInTimeUnit(getDivisionSize(), forTimeStep);

        if (jump == -1) {
            throw new MisalignedTimeDivisionException();
        }

        if (jump == 1) {
            return getDivisionDates();
        }

        int count = jump;

        final Set<LocalDate> allDivisionDates = getDivisionDates();
        final Set<LocalDate> selectedDivisionDates = new TreeSet<>();

        for (LocalDate date : allDivisionDates) {

            if (count == jump) {
                selectedDivisionDates.add(date);
                count = 0;
            }

            count++;
        }

        return selectedDivisionDates;
    }

    boolean checkDateAlignmentToDivisions(final LocalDate date) {
        return DateUtils.matchesInterval(date, divisionSize, startDate);
    }

    LocalDate resolveDateToCorrectDivisionDate(final LocalDate date) {

        // TODO clarify

        int dM = date.getMonth().getValue();
        int dY = date.getYear();

        int sM = startDate.getMonth().getValue();
        int sY = startDate.getYear();

        // Time unit in months
        int tsc = (int) divisionSize.toTotalMonths();

        int adm = (12 * ((dY - sY) % tsc)) + dM;

        int cm = (sM % tsc) + tsc * (int) Math.floor((adm - (sM % tsc)) / tsc);

        int absm = cm - 12 * ((dY - sY) % tsc);

        int iM = 12 + absm - 12 * (int) Math.ceil((absm / 12.0D));

        int iY = dY + (int) Math.ceil(absm / 12.0D) - 1;

        return LocalDate.of(iY, iM, 1);
    }
}
