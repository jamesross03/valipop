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

import java.time.LocalDate;
import java.time.Period;

/**
 * Utility functions on Java datas and periods.
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class DateUtils {

    public static final int MONTHS_IN_YEAR = 12;

    public static boolean matchesInterval(final LocalDate currentDate, final Period interval, final LocalDate startDate) {

        return Period.between(startDate, currentDate).toTotalMonths() % interval.toTotalMonths() == 0;
    }

    public static LocalDate earlierOf(final LocalDate date1, final LocalDate date2) {

        return date1.isAfter(date2) ? date2 : date1;
    }

    public static LocalDate laterOf(final LocalDate date1, final LocalDate date2) {

        return date1.isAfter(date2) ? date1 : date2;
    }

    public static double stepsInYear(final Period timeStep) {

        return MONTHS_IN_YEAR / (double) timeStep.toTotalMonths();
    }

    public static int calcSubTimeUnitsInTimeUnit(final Period subTimeUnit, final Period timeUnit) {

        // div by 0?
        double n = timeUnit.toTotalMonths() / (double) subTimeUnit.toTotalMonths();

        if (n % 1 == 0) {
            return (int) Math.floor(n);
        }

        return -1;
    }
}
