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

import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.utils.MapUtils;
import uk.ac.standrews.cs.valipop.utils.specialTypes.dates.DateUtils;
import uk.ac.standrews.cs.valipop.utils.specialTypes.dates.MisalignedTimeDivisionException;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRange;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

/**
 * The FemaleCollection is a specialised concrete implementation of a PersonCollection. The implementation offers an
 * additional layer of division below the year of birth level which divides females out into separate collections based
 * on how many children they have had.
 *
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class FemaleCollection extends PersonCollection {

    private final Map<LocalDate, Map<Integer, Set<IPerson>>> byBirthYearAndNumberOfChildren = new TreeMap<>();

    /**
     * Instantiates a new FemaleCollection. The dates specify the earliest and latest expected birth dates of
     * individuals in the FemaleCollection. There is no hard enforcement of this as the bounds are intended to serve
     * mainly as a guide for when other things make use of the FemaleCollection - e.g. producing plots, applying
     * validation statistics.
     *
     * @param start the start
     * @param end   the end
     */
    FemaleCollection(final LocalDate start, final LocalDate end, final Period divisionSize, final String description) {

        super(start, end, divisionSize, description);

        for (LocalDate date = start; !date.isAfter(end); date = date.plus(divisionSize)) {
            byBirthYearAndNumberOfChildren.put(date, new TreeMap<>());
        }
    }

    @Override
    public Collection<IPerson> getPeople() {

        final Collection<IPerson> people = new ArrayList<>();

        for (Map<Integer, Set<IPerson>> map : byBirthYearAndNumberOfChildren.values()) {
            for (Collection<IPerson> collection : map.values()) {
                people.addAll(collection);
            }
        }

        return people;
    }

    @Override
    void addPeople(final Collection<IPerson> people, final LocalDate divisionDate) {

        // TODO confusing naming mismatch between this and next method

        for (Collection<IPerson> collection : getAllPeopleFromDivision(divisionDate).values()) {
            people.addAll(collection);
        }
    }

    @Override
    public void add(final IPerson person) {

        final LocalDate divisionDate = resolveDateToCorrectDivisionDate(person.getBirthDate());
        final int numberOfChildren = countChildren(person);

        final TreeSet<IPerson> newList = new TreeSet<>();
        newList.add(person);

        if (byBirthYearAndNumberOfChildren.containsKey(divisionDate)) {

            final Map<Integer, Set<IPerson>> map = byBirthYearAndNumberOfChildren.get(divisionDate);

            if (map.containsKey(numberOfChildren)) {
                map.get(numberOfChildren).add(person);

            } else {
                map.put(numberOfChildren, newList);
            }
        } else {

            Map<Integer, Set<IPerson>> newMap = new TreeMap<>();
            newMap.put(numberOfChildren, newList);
            byBirthYearAndNumberOfChildren.put(divisionDate, newMap);
        }

        size++;
    }

    @Override
    public void remove(final IPerson person) {

        final LocalDate divisionDate = resolveDateToCorrectDivisionDate(person.getBirthDate());
        final Map<Integer, Set<IPerson>> familySizeMap = byBirthYearAndNumberOfChildren.get(divisionDate);
        final int numberOfChildren = countChildren(person);
        final Collection<IPerson> people = familySizeMap.get(numberOfChildren);

        if (people == null || !people.remove(person))
            throw new PersonNotFoundException("Specified person not found in data structure");

        size--;
    }

    @Override
    public int getNumberOfPeople(final LocalDate firstDate, final Period timePeriod) {

        return getPeopleBornInTimePeriod(firstDate, timePeriod).size();
    }

    @Override
    public Set<LocalDate> getDivisionDates() {
        return new TreeSet<>(byBirthYearAndNumberOfChildren.keySet());
    }

    @Override
    public int getNumberOfPeople() {
        return size;
    }

    public Collection<IPerson> getByDatePeriodAndBirthOrder(final LocalDate date, final Period period, final IntegerRange birthOrder) {

        int highestBirthOrder = getHighestBirthOrder(date, period);

        if (!birthOrder.isPlus())
            highestBirthOrder = birthOrder.getMax();

        final Collection<IPerson> people = new ArrayList<>();

        for (int i = birthOrder.getMin(); i <= highestBirthOrder; i++)
            people.addAll(getByDatePeriodAndBirthOrder(date, period, i));

        return people;
    }

    /**
     * Returns the highest birth order (number of children) among women in the specified year of birth.
     *
     * @param dateOfBirth the year of birth of the mothers in question
     * @return the highest birth order value
     */
    private int getHighestBirthOrder(final LocalDate dateOfBirth, final Period period) {

        final int divisionsInPeriod = DateUtils.divideYieldingInt(period, getDivisionSize());

        LocalDate divisionDate = dateOfBirth;

        int highestBirthOrder = 0;

        for (int i = 0; i < divisionsInPeriod; i++) {

            final Map<Integer, Set<IPerson>> temp = byBirthYearAndNumberOfChildren.get(divisionDate);

            if (temp != null && MapUtils.getMax(temp.keySet()) > highestBirthOrder) {
                highestBirthOrder = MapUtils.getMax(temp.keySet());
            }

            // move on to the new division date until we've covered the required divisions
            divisionDate = divisionDate.plus(getDivisionSize());
        }

        return highestBirthOrder;
    }

    /**
     * Gets the {@link Collection} of mothers born in the given year with the specified birth order (i.e. number of
     * children)
     *
     * @param date       the date
     * @param period     the period following the date to find people from
     * @param birthOrder the number of children
     * @return the by number of children
     */
    public Collection<IPerson> getByDatePeriodAndBirthOrder(final LocalDate date, final Period period, final int birthOrder) {

        final int divisionsInPeriod = DateUtils.divideYieldingInt(period, getDivisionSize());

        final ArrayList<IPerson> people = new ArrayList<>();
        LocalDate divisionDate = date;

        for (int i = 0; i < divisionsInPeriod; i++) {

            try {
                people.addAll(byBirthYearAndNumberOfChildren.get(divisionDate).get(birthOrder));

            } catch (NullPointerException e) {
                // If no data exists for the year or the given birth order in the given year, then there's no one to add
            }

            // move on to the new division date until we've covered the required divisions
            divisionDate = divisionDate.plus(getDivisionSize());
        }

        return people;
    }

    private Map<Integer, Set<IPerson>> getAllPeopleFromDivision(final LocalDate divisionDate) {

        if (byBirthYearAndNumberOfChildren.containsKey(divisionDate)) {
            return byBirthYearAndNumberOfChildren.get(divisionDate);

        } else {
            if (checkDateAlignmentToDivisions(divisionDate)) {
                // Division date is reasonable but no people exist in it yet
                return new TreeMap<>();
            } else {
                throw new MisalignedTimeDivisionException("Date provided to underlying population structure does not align");
            }
        }
    }

    private int countChildren(final IPerson person) {

        int count = 0;

        for (IPartnership partnership : person.getPartnerships()) {
            count += partnership.getChildren().size();
        }

        return count;
    }
}