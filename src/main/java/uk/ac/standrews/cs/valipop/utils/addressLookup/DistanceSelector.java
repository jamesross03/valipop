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
package uk.ac.standrews.cs.valipop.utils.addressLookup;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Selects a random distance (with hard-coded parameters).
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class DistanceSelector {

    RandomGenerator random;

    private final PoissonDistribution distribution;
    private final UniformRealDistribution secondaryDistribution;


    private static final double poissonM = 7.0;
    private static final double averageMoveDistance = 21.0;

    public DistanceSelector(RandomGenerator random) {

        this.random = random;
        distribution = new PoissonDistribution(random, poissonM - 0.5, PoissonDistribution.DEFAULT_EPSILON, PoissonDistribution.DEFAULT_MAX_ITERATIONS);
        secondaryDistribution = new UniformRealDistribution(random, 0.0, averageMoveDistance / poissonM);
    }

    public double selectRandomDistance() {

        return (distribution.sample() * averageMoveDistance / poissonM) + secondaryDistribution.sample();
    }
}
