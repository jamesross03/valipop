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
package uk.ac.standrews.cs.valipop.utils.specialTypes.dates;

import org.apache.commons.math3.random.RandomGenerator;

import java.time.LocalDate;
import java.time.Period;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * For utility class for selecting random dates in a range.
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class DateSelector {

    final RandomGenerator random;

    public DateSelector(final RandomGenerator random) {

        this.random = random;
    }

    public LocalDate selectRandomDate(final LocalDate earliestDate, final LocalDate latestDate) {

        final int daysInWindow = (int)DAYS.between(earliestDate, latestDate);

        return selectRandomDate(earliestDate, daysInWindow);
    }

    public LocalDate selectRandomDate(final LocalDate earliestDate, final Period timePeriod) {

        return selectRandomDate(earliestDate, earliestDate.plus(timePeriod));
    }

    private LocalDate selectRandomDate(final LocalDate earliestDate, int daysInWindow) {

        // TODO clean up.
        daysInWindow = daysInWindow == 0 ? 1 : Math.abs(daysInWindow);

        return earliestDate.plusDays(random.nextInt(daysInWindow));
    }
}
