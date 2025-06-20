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
package uk.ac.standrews.cs.valipop.simulationEntities;

import org.apache.commons.math3.random.RandomGenerator;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SexOption;
import uk.ac.standrews.cs.valipop.utils.specialTypes.dates.DateSelector;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static uk.ac.standrews.cs.valipop.simulationEntities.PopulationNavigation.getDateOfNextPostSeparationEvent;

/**
 * Implementation of a partnership in a table format.
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class Partnership implements IPartnership {

    private static int nextId = 0;
    private final int id;
    private final IPerson male;
    private final IPerson female;
    private final List<IPerson> children = new ArrayList<>();

    private LocalDate partnershipDate;
    private LocalDate marriageDate = null;
    private LocalDate separationDate = null;
    private LocalDate earliestPossibleSeparationDate = null;

    private String marriageLocation;

    private boolean finalised = false;

    public Partnership(final IPerson male, final IPerson female, final LocalDate partnershipDate) {

        this(male, female);

        this.partnershipDate = partnershipDate;
    }

    public Partnership(final IPerson male, final IPerson female) {

        this.id = getNewId();

        this.male = male;
        this.female = female;
    }

    public String toString() {

        final StringBuilder s = new StringBuilder();

        s.append("--Partnership: ");
        s.append(id).append("--\n");

        appendPerson(s, male);
        appendPerson(s, female);

        s.append("----Children----\n");

        for (final IPerson c : children) {
            appendPerson(s, c);
        }

        s.append("--End Partnership: ");
        s.append(id).append("--\n");

        return s.toString();
    }

    private static void appendPerson(final StringBuilder s, final IPerson person) {

        s.append(person.getSurname()).append(" | ");
        s.append(person.getSex()).append(" | ");
        s.append(person.getBirthDate()).append(" | ");
        s.append(person.getDeathDate() != null ? person.getDeathDate() + "\n" : "no DOD\n");
    }

    public void setPartnershipDate(final LocalDate startDate) {
        partnershipDate = startDate;
    }

    private static synchronized int getNewId() {
        return nextId++;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean isFinalised() {
        return finalised;
    }

    @Override
    public void setFinalised(final boolean finalised) {
        this.finalised = finalised;
    }

    @Override
    public void setMarriagePlace(final String place) {
        marriageLocation = place;
    }

    public static synchronized void resetIds() {
        nextId = 0;
    }

    public void setMarriageDate(final LocalDate marriageDate) {
        this.marriageDate = marriageDate;
    }

    @Override
    public LocalDate getMarriageDate() {
        return marriageDate;
    }

    @Override
    public String getMarriagePlace() {
        return marriageLocation;
    }

    @Override
    public IPerson getFemalePartner() {
        return female;
    }

    @Override
    public IPerson getMalePartner() {
        return male;
    }

    @Override
    public IPerson getPartnerOf(final IPerson person) {

        return person.getSex() == SexOption.MALE ? female : male;
    }

    @Override
    public List<IPerson> getChildren() {
        return children;
    }

    @Override
    public LocalDate getPartnershipDate() {
        return partnershipDate;
    }

    @Override
    public synchronized LocalDate getSeparationDate(final RandomGenerator random) {

        if (earliestPossibleSeparationDate == null) return null;
        if (separationDate == null) setSeparationDate(random);

        return separationDate;
    }

    private void setSeparationDate(final RandomGenerator random) {

        final LocalDate maleMovedOnDate = getDateOfNextPostSeparationEvent(male, earliestPossibleSeparationDate);
        final LocalDate femaleMovedOnDate = getDateOfNextPostSeparationEvent(female, earliestPossibleSeparationDate);

        final LocalDate earliestMovedOnDate;

        if (maleMovedOnDate != null) {

            if (femaleMovedOnDate != null) {
                earliestMovedOnDate = maleMovedOnDate.isBefore( femaleMovedOnDate) ? maleMovedOnDate : femaleMovedOnDate;
            } else {
                earliestMovedOnDate = maleMovedOnDate;
            }

        } else {
            if (femaleMovedOnDate != null) {
                earliestMovedOnDate = femaleMovedOnDate;

            } else {

                // pick a date in the next 30 years
                earliestMovedOnDate = earliestPossibleSeparationDate.plusYears(30);
            }
        }

        separationDate = new DateSelector(random).selectRandomDate(earliestPossibleSeparationDate, earliestMovedOnDate);
    }

    @Override
    public LocalDate getEarliestPossibleSeparationDate() {
        return earliestPossibleSeparationDate;
    }

    @Override
    public void setEarliestPossibleSeparationDate(final LocalDate date) {
        earliestPossibleSeparationDate = date;
    }

    @Override
    public int compareTo(final IPartnership o) {
        return Integer.compare(id, o.getId());
    }

    @Override
    public void addChildren(final Collection<IPerson> children) {
        this.children.addAll(children);
    }
}
