/*
 * Copyright 2017 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module population_model.
 *
 * population_model is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * population_model is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with population_model. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.valipop.utils.specialTypes.dates;

import org.apache.commons.math3.random.RandomGenerator;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.simulationEntities.PopulationNavigation;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SexOption;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.PopulationStatistics;

import java.time.LocalDate;
import java.time.Period;

/**
 * Utility class for selecting a valid random death date of an person.
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class DeathDateSelector extends DateSelector {

    public DeathDateSelector(final RandomGenerator random) {

        super(random);
    }

    public LocalDate selectDate(final IPerson person, final PopulationStatistics statistics, final LocalDate currentDate, final Period consideredTimePeriod) {

        final IPerson child = PopulationNavigation.getLastChild(person);

        if (child != null) {

            final LocalDate birthDateOfLastChild = child.getBirthDate();

            if (person.getSex() == SexOption.MALE) {

                // If a male with a child then the man cannot die more than the minimum gestation period before the birth date
                final LocalDate lastConceptionDate = birthDateOfLastChild.minus(statistics.getMinGestationPeriod());
                final LocalDate lastMoveDate = person.getLastMoveDate() == null ? LocalDate.MIN : person.getLastMoveDate();

                final LocalDate earliestPossibleDate = lastMoveDate.isBefore(lastConceptionDate) ? lastConceptionDate : lastMoveDate;
                return selectDateRestrictedByEarliestPossibleDate(currentDate, consideredTimePeriod, earliestPossibleDate);

            } else {
                // If a female with a child then they cannot die before birth of child
                final LocalDate lastMoveDate = person.getLastMoveDate() == null ? LocalDate.MIN : person.getLastMoveDate();

                final LocalDate earliestPossibleDate = lastMoveDate.isBefore(birthDateOfLastChild) ? birthDateOfLastChild : lastMoveDate;
                return selectDateRestrictedByEarliestPossibleDate(currentDate, consideredTimePeriod, earliestPossibleDate);
            }

        } else {
            final LocalDate lastMoveDate = person.getLastMoveDate() == null ? person.getBirthDate() : person.getLastMoveDate();
            return selectDateRestrictedByEarliestPossibleDate(currentDate, consideredTimePeriod, lastMoveDate);
        }
    }

    private LocalDate selectDateRestrictedByEarliestPossibleDate(final LocalDate currentDate, final Period consideredTimePeriod, final LocalDate earliestPossibleDate) {

        if (!currentDate.isAfter(earliestPossibleDate)) {

            return selectRandomDate(earliestPossibleDate, currentDate.plus(consideredTimePeriod));

        } else {
            return selectRandomDate(currentDate, consideredTimePeriod);
        }
    }
}
