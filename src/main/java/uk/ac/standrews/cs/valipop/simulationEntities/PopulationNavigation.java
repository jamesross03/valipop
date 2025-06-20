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

import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SexOption;

import java.time.LocalDate;
import java.time.Period;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/**
 * Utility functions to navigate through population model via persons.
 */
public class PopulationNavigation {

    private static final int NUMBER_OF_GENERATIONS_TO_EXCLUDE = 3;

    public static Collection<IPerson> siblingsOf(final IPerson person) {

        // Include half-siblings.

        final Collection<IPerson> siblings = new TreeSet<>();
        final IPartnership parents = person.getParents();

        if (parents != null) {

            siblings.addAll(getAllChildren(parents.getMalePartner()));
            siblings.addAll(getAllChildren(parents.getFemalePartner()));
        }

        return siblings;
    }

    public static Collection<IPerson> imidiateFamilyOf(final IPerson root) {

        final Collection<IPerson> family = new ArrayList<>();

        family.addAll(siblingsOf(root));
        family.addAll(siblingsOf(root));
        family.addAll(getAllChildren(root));
        family.addAll(ancestorsOf(root, 1));

        return family;
    }

    private static Collection<IPerson> siblingsOf(final IPerson person, final SexOption sex) {

        final Collection<IPerson> siblings = siblingsOf(person);
        siblings.removeIf(p -> p.getSex() != sex);
        return siblings;
    }

    public static Collection<IPerson> sistersOf(final IPerson person) {

        return siblingsOf(person, SexOption.FEMALE);
    }

    public static Collection<IPerson> brothersOf(final IPerson person) {

        return siblingsOf(person, SexOption.MALE);
    }

    public static Collection<IPerson> partnersOf(final IPerson person) {

        final Collection<IPerson> partners = new TreeSet<>();

        for (final IPartnership partnership : person.getPartnerships()) {
            partners.add(partnership.getPartnerOf(person));
        }
        return partners;
    }

    private static Collection<IPerson> descendantsOf(final Collection<IPerson> people) {

        final Collection<IPerson> descendants = new TreeSet<>();
        for (final IPerson person : people) {
            descendants.addAll(descendantsOf(person));
        }
        return descendants;
    }

    public static Collection<IPerson> descendantsOf(final IPerson person) {

        return descendantsOf(person, NUMBER_OF_GENERATIONS_TO_EXCLUDE);
    }

    public static Collection<IPerson> femaleDescendantsOf(final Collection<IPerson> people) {

        final Collection<IPerson> descendants = descendantsOf(people);
        descendants.removeIf(p -> p.getSex() != SexOption.FEMALE);
        return descendants;
    }

    public static Collection<IPerson> femaleDescendantsOf(final IPerson person) {

        return descendantsOf(person, SexOption.FEMALE);
    }

    public static Collection<IPerson> maleDescendantsOf(final IPerson person) {

        return descendantsOf(person, SexOption.MALE);
    }

    private static Collection<IPerson> descendantsOf(final IPerson person, final SexOption sex) {

        final Collection<IPerson> descendants = descendantsOf(person);
        descendants.removeIf(p -> p.getSex() != sex);
        return descendants;
    }

    public static Collection<IPerson> ancestorsOf(final IPerson person) {

        return ancestorsOf(person, NUMBER_OF_GENERATIONS_TO_EXCLUDE);
    }

    public static Collection<IPerson> femaleAncestorsOf(final Collection<IPerson> people) {

        final Collection<IPerson> ancestors = new ArrayList<>();
        for (final IPerson person : people) {
            ancestors.addAll(femaleAncestorsOf(person));
        }
        return ancestors;
    }

    public static Collection<IPerson> femaleAncestorsOf(final IPerson person) {

        return ancestorsOf(person, SexOption.FEMALE);
    }

    public static Collection<IPerson> maleAncestorsOf(final IPerson person) {

        return ancestorsOf(person, SexOption.MALE);
    }

    private static Collection<IPerson> ancestorsOf(final IPerson person, final SexOption sex) {

        final Collection<IPerson> ancestors = ancestorsOf(person);
        ancestors.removeIf(p -> p.getSex() != sex);
        return ancestors;
    }

    private static Collection<IPerson> descendantsOf(final IPerson person, final int generations) {

        final Collection<IPerson> descendants = new TreeSet<>();

        if (generations > 0) {

            for (final IPerson child : getAllChildren(person)) {

                descendants.add(child);
                descendants.addAll(descendantsOf(child, generations - 1));
            }
        }

        return descendants;
    }

    public static Collection<IPerson> getAllChildren(final IPerson person) {

        final Collection<IPerson> children = new ArrayList<>();

        for (final IPartnership partnership : person.getPartnerships()) {
            children.addAll(partnership.getChildren());
        }

        return children;
    }

    private static Collection<IPerson> ancestorsOf(final IPerson person, final int generations) {

        final Collection<IPerson> ancestors = new TreeSet<>();

        if (generations > 0) {

            final IPartnership parentsPartnership = person.getParents();

            if (parentsPartnership != null) {
                final IPerson mother = parentsPartnership.getFemalePartner();
                final IPerson father = parentsPartnership.getMalePartner();

                ancestors.add(mother);
                ancestors.add(father);

                ancestors.addAll(ancestorsOf(mother, generations - 1));
                ancestors.addAll(ancestorsOf(father, generations - 1));
            }
        }

        return ancestors;
    }

    public static Collection<IPerson> partnersOf(final Collection<IPerson> people) {

        final List<IPerson> partners = new ArrayList<>();

        for (final IPerson person : people) {
            partners.addAll(partnersOf(person));
        }
        return partners;
    }

    public static boolean aliveOnDate(final IPerson person, final LocalDate date) {

        if (!person.getBirthDate().isAfter(date)) {

            final LocalDate deathDate = person.getDeathDate();
            return deathDate == null || date.isBefore(deathDate);
        }
        return false;
    }

    public static IPerson getLastChild(final IPerson person) {

        LocalDate latestChildBirthDate = LocalDate.MIN;
        IPerson child = null;

        for (final IPartnership p : person.getPartnerships()) {
            for (final IPerson c : p.getChildren()) {

                if (!latestChildBirthDate.isAfter(c.getBirthDate())) {
                    latestChildBirthDate = c.getBirthDate();
                    child = c;
                }
            }
        }

        return child;
    }

    public static boolean lastPartnerDied(final IPerson person, final LocalDate currentDate) {

        try {
            final IPerson lastPartner = getLastChild(person).getParents().getPartnerOf(person);
            return !aliveOnDate(lastPartner, currentDate);

        } catch (final NullPointerException e) {
            return true;
        }
    }

    public static int ageOnDate(final IPerson person, final LocalDate queryDate) {

        return ageOnDate(person.getBirthDate(), queryDate);
    }

    public static int ageOnDate(final LocalDate birthDate, final LocalDate queryDate) {

//        if (birthDate.getDayOfMonth() == 1 && birthDate.getMonthValue() == 1 && queryDate.getDayOfMonth() == 1 && queryDate.getMonthValue() == 1) {
//            final int age = Period.between(birthDate, queryDate).getYears() - 1;
//            return age == -1 ? 0 : age;
//        } else {
            return Math.max(0, Period.between(birthDate, queryDate).getYears());
//        }
    }

    public static int numberOfChildrenInLatestPartnership(final IPerson person) {

        return getLastChild(person).getParents().getChildren().size();
    }

    public static boolean bornInYear(final IPerson person, final Year year) {

        final LocalDate birthDate = person.getBirthDate();
        return birthDate != null && birthDate.getYear() == year.getValue();
    }

    public static boolean diedInYear(final IPerson person, final Year year) {

        final LocalDate deathDate = person.getDeathDate();
        return deathDate != null && deathDate.getYear() == year.getValue();
    }

    public static boolean diedAfter(final IPerson person, final LocalDate date) {

        final LocalDate deathDate = person.getDeathDate();
        return deathDate == null || deathDate.isAfter(date);
    }

    public static Collection<IPartnership> getPartnershipsActiveInYear(final IPerson person, final Year year) {

        final Collection<IPartnership> activePartnerships = new ArrayList<>();

        for (final IPartnership partnership : person.getPartnerships()) {

            final LocalDate startDate = partnership.getPartnershipDate();

            if (startDate.getYear() == year.getValue()) {
                activePartnerships.add(partnership);
            } else {
                for (final IPerson p : partnership.getChildren()) {
                    if (p.getBirthDate().getYear() == year.getValue()) {
                        activePartnerships.add(partnership);
                        break;
                    }
                }
            }
        }

        return activePartnerships;
    }

    public static IPartnership getLastPartnership(final IPerson person) {

        LocalDate latestPartnershipDate = LocalDate.MIN;
        IPartnership partnership = null;

        for (final IPartnership p : person.getPartnerships()) {
            if (latestPartnershipDate.isBefore(p.getPartnershipDate())) {
                latestPartnershipDate = p.getPartnershipDate();
                partnership = p;
            }
        }
        return partnership;
    }

    public static IPartnership getLastPartnershipBeforeDate(final IPerson person, final LocalDate date) {

        LocalDate latestPartnershipDate = LocalDate.MIN;
        IPartnership partnership = null;

        for (final IPartnership p : person.getPartnerships()) {
            if (p.getPartnershipDate().isBefore(date) && latestPartnershipDate.isBefore(p.getPartnershipDate())) {
                latestPartnershipDate = p.getPartnershipDate();
                partnership = p;
            }
        }
        return partnership;
    }

    public static Integer numberOfChildrenBirthedBeforeDate(final IPerson person, final LocalDate y) {

        int count = 0;

        for (final IPartnership p : person.getPartnerships()) {
            for (final IPerson c : p.getChildren()) {
                if (c.getBirthDate().isBefore(y)) {
                    count++;
                }
            }
        }

        return count;
    }

    public static LocalDate getDateOfNextPostSeparationEvent(final IPerson person, final LocalDate separationDate) {

        LocalDate earliestDate = null;

        for (final IPartnership partnership : person.getPartnerships()) {
            LocalDate date = partnership.getPartnershipDate();
            if (separationDate.isBefore(date)) {

                if (earliestDate == null || date.isBefore(earliestDate)) {
                    earliestDate = date;
                }
            }

            date = partnership.getMarriageDate();

            if (date != null) {
                if (separationDate.isBefore(date)) {

                    if (earliestDate == null || date.isBefore(earliestDate)) {
                        earliestDate = date;
                    }
                }
            }
        }

        if (earliestDate == null) {
            earliestDate = person.getDeathDate();
        }

        return earliestDate;
    }

    // Ensures a person is present in country at the given date
    public static boolean presentOnDate(final IPerson person, final LocalDate date) {

        final LocalDate immigrationDate = person.getImmigrationDate();
        final LocalDate emigrationDate = person.getEmigrationDate();

        if(immigrationDate != null && immigrationDate.isAfter(date)) {
            // date is before person arrived in country
            return false;
        } else {
            // not immigrant or already arrived
            if(emigrationDate != null && emigrationDate.isBefore(date)) {
                // date is after person leaves country
                // therefore not present on date
                return false;
            } else {
                // never emigrates or not yet left
                // therefore present on date
                return true;
            }
        }
    }

    public static boolean childOf(final IPerson parent, final IPerson person) {

        if(person.getParents() == null) return false;
        if(person.getParents().getMalePartner() == parent) return true;
        if(person.getParents().getFemalePartner() == parent) return true;

        return false;

    }
}
