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
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.LabelledValueSet;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.StringToIntegerSet;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class MultipleDeterminedCountByString extends MultipleDeterminedCount<String, String, String> {

    public MultipleDeterminedCountByString(StatsKey<String, String> key, LabelledValueSet<String, Integer> determinedCount,
                                           LabelledValueSet<String, Double> rawCorrectedCount,
                                           LabelledValueSet<String, Double> rawUncorrectedCount) {

        super(key, determinedCount, rawCorrectedCount, rawUncorrectedCount);
    }

    @Override
    public LabelledValueSet<String, Integer> getZeroedCountsTemplate(RandomGenerator random) {
        return new StringToIntegerSet(determinedCount.getLabels(), 0, random);
    }
}
