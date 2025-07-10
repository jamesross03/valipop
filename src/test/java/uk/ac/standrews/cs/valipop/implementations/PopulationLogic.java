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

import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

/**
 * Defines various methods to do with population structure and constraints.
 *
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class PopulationLogic {

    private static final int MINIMUM_MOTHER_AGE_AT_CHILDBIRTH = 12;
    private static final int MAXIMUM_MOTHER_AGE_AT_CHILDBIRTH = 55;
    private static final int MAX_GESTATION_IN_DAYS = 300;
    private static final int MINIMUM_FATHER_AGE_AT_CHILDBIRTH = 12;

    /**
     * Checks whether the ages of the given parents are sensible for the given child.
     *
     * @param father the father
     * @param mother the mother
     * @param child  the child
     * @return true if the parents' ages are sensible
     */
    public static boolean parentsHaveSensibleAgesAtChildBirth(final IPerson father, final IPerson mother, final IPerson child) {

        final LocalDate mother_birth_date = mother.getBirthDate();
        final LocalDate mother_death_date = mother.getDeathDate();

        final LocalDate father_birth_date = father.getBirthDate();
        final LocalDate father_death_date = father.getDeathDate();

        final LocalDate child_birth_date = child.getBirthDate();

        boolean sensible = parentsHaveSensibleAgesAtChildBirth(father_birth_date, father_death_date, mother_birth_date, mother_death_date, child_birth_date);

        // TODO rewrite as assertions.
        if (!sensible) {
            System.err.println("father: " + father + " mother: " + mother + " child: " + child);
            System.err.println("mother birth: " +  mother_birth_date);
            System.err.println("mother death: " +  mother_death_date);
            System.err.println("father birth: " +  father_birth_date);
            System.err.println("father death: " +  father_death_date);
            System.err.println("child birth: " +  child_birth_date);
        }

        return sensible;
    }

    private static boolean parentsHaveSensibleAgesAtChildBirth(final LocalDate father_birth_date, final LocalDate father_death_date, final LocalDate mother_birth_date, final LocalDate mother_death_date, final LocalDate child_birth_date) {

        boolean motherAlive = motherAliveAtBirth(mother_death_date, child_birth_date);
        boolean motherNotTooYoung = motherNotTooYoungAtBirth(mother_birth_date, child_birth_date);
        boolean motherNotTooOld = motherNotTooOldAtBirth(mother_birth_date, child_birth_date);
        boolean fatherAliveAtConception = fatherAliveAtConception(father_death_date, child_birth_date);
        boolean fatherNotTooYoung = fatherNotTooYoungAtBirth(father_birth_date, child_birth_date);

        if (!motherAlive) System.err.println("mother not alive");
        if (!motherNotTooYoung) System.err.println("mother too young");
        if (!motherNotTooOld) System.err.println("mother too old");
        if (!fatherAliveAtConception) System.err.println("father not alive at conception");
        if (!fatherNotTooYoung) System.err.println("father too young");

        return motherAlive && motherNotTooYoung && motherNotTooOld && fatherAliveAtConception && fatherNotTooYoung;
    }

    private static boolean motherAliveAtBirth(final LocalDate mother_death_date, final LocalDate child_birth_date) {

        return mother_death_date == null || !child_birth_date.isAfter( mother_death_date);
    }

    private static boolean motherNotTooYoungAtBirth(final LocalDate mother_birth_date, final LocalDate child_birth_date) {

        final int mothers_age_at_birth = parentsAgeAtChildBirth(mother_birth_date, child_birth_date);

        return notLessThan(mothers_age_at_birth, MINIMUM_MOTHER_AGE_AT_CHILDBIRTH);
    }

    private static boolean motherNotTooOldAtBirth(final LocalDate mother_birth_date, final LocalDate child_birth_date) {

        final int mothers_age_at_birth = parentsAgeAtChildBirth(mother_birth_date, child_birth_date);

        return notGreaterThan(mothers_age_at_birth, MAXIMUM_MOTHER_AGE_AT_CHILDBIRTH);
    }

    private static boolean fatherAliveAtConception(final LocalDate father_death_date, final LocalDate child_birth_date) {

        return father_death_date == null ||!child_birth_date.isAfter( father_death_date.plus( MAX_GESTATION_IN_DAYS, ChronoUnit.DAYS));
    }

    private static boolean fatherNotTooYoungAtBirth(final LocalDate father_birth_date, final LocalDate child_birth_date) {

        final int fathers_age_at_birth = parentsAgeAtChildBirth(father_birth_date, child_birth_date);

        return notLessThan(fathers_age_at_birth, MINIMUM_FATHER_AGE_AT_CHILDBIRTH);
    }

    private static int parentsAgeAtChildBirth(final LocalDate parent_birth_date, final LocalDate child_birth_date) {

        return Period.between(parent_birth_date, child_birth_date).getYears();
    }

    private static boolean notLessThan(final int i1, final int i2) {

        return i1 >= i2;
    }

    private static boolean notGreaterThan(final int i1, final int i2) {

        return i1 <= i2;
    }
}
