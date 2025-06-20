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
package uk.ac.standrews.cs.valipop.statistics.populationStatistics.determinedCounts;

import org.apache.commons.math3.random.RandomGenerator;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsKeys.StatsKey;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRange;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRangeToIntegerSet;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.LabelledValueSet;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class MultipleDeterminedCountByIR extends MultipleDeterminedCount<IntegerRange, Integer, Integer> {

    public MultipleDeterminedCountByIR(StatsKey<Integer, Integer> key, LabelledValueSet<IntegerRange, Integer> determinedCount,
                                   LabelledValueSet<IntegerRange, Double> rawCorrectedCount,
                                   LabelledValueSet<IntegerRange, Double> rawUncorrectedCount) {

        super(key, determinedCount, rawCorrectedCount, rawUncorrectedCount);
    }

    @Override
    public LabelledValueSet<IntegerRange, Integer> getZeroedCountsTemplate(RandomGenerator random) {
        return new IntegerRangeToIntegerSet(determinedCount.getLabels(), 0, random);
    }
}
