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

import java.time.Period;

/**
 * Utility functions on Java datas and periods.
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class DateUtils {

//    public static final int MONTHS_IN_YEAR = 12;

    public static double stepsInYear(final Period timeStep) {

//        return MONTHS_IN_YEAR / (double) timeStep.toTotalMonths();
        return divideYieldingInt(Period.ofYears(1), timeStep);
    }

    public static int divideYieldingInt(final Period dividend, final Period divisor) {

        final long divisor_months = divisor.toTotalMonths();
        final long dividend_months = dividend.toTotalMonths();

        if (dividend_months % divisor_months != 0)
            throw new MisalignedTimeDivisionException();

        return (int)(dividend_months / divisor_months);


//        // div by 0?
//        double n = timeUnit.toTotalMonths() / (double) subTimeUnit.toTotalMonths();
//
//
//        if (n % 1 == 0) {
//            return (int) Math.floor(n);
//        }
//
//        return -1;
    }

    public static double divideYieldingDouble(final Period dividend, final Period divisor) {

        //        return period.toTotalMonths() / (double) DateUtils.MONTHS_IN_YEAR;
        return dividend.toTotalMonths() / (double) divisor.toTotalMonths();
    }
}
