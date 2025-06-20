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

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Utility class for selecting a valid random marriage date of a couple.
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class MarriageDateSelector extends DateSelector {

    private final PoissonDistribution distribution;

    private static final double poissonM = 15;
    private static final double averageYearsFromMarriageToChild = 3.0;
    private static final int daysInYear = 365;

    public MarriageDateSelector(final RandomGenerator random) {

        super(random);
        distribution = new PoissonDistribution(random, poissonM - 0.5, PoissonDistribution.DEFAULT_EPSILON, PoissonDistribution.DEFAULT_MAX_ITERATIONS);
    }

    public LocalDate selectRandomDate(final LocalDate earliestDate, final LocalDate latestDate) {

        final int daysInWindow = (int) DAYS.between(earliestDate, latestDate);

        final double chosenYear = distribution.sample() * averageYearsFromMarriageToChild / poissonM;
        final double dayAdjust = random.nextInt((int) (Math.floor(daysInYear * (averageYearsFromMarriageToChild / poissonM))));

        int chosenDay = Math.toIntExact(Math.round(chosenYear * daysInYear + dayAdjust));

        if (chosenDay > daysInWindow) {

            // revert to uniform distribution
            chosenDay = random.nextInt(daysInWindow);
        }

        return latestDate.minusDays(chosenDay);
    }
}
