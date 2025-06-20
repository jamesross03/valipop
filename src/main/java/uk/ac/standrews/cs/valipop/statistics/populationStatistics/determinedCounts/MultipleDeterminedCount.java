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
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.LabelledValueSet;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsKeys.StatsKey;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public abstract class MultipleDeterminedCount<Lookup, X, Y> implements DeterminedCount<LabelledValueSet<Lookup, Integer>, LabelledValueSet<Lookup, Double>, X, Y> {

    private StatsKey<X, Y> key;

    protected LabelledValueSet<Lookup, Integer> determinedCount;
    private LabelledValueSet<Lookup, Integer> fulfilledCount;

    private LabelledValueSet<Lookup, Double> rawCorrectedCount;
    private LabelledValueSet<Lookup, Double> rawUncorrectedCount;

    public MultipleDeterminedCount(StatsKey<X, Y> key, LabelledValueSet<Lookup, Integer> determinedCount,
                                       LabelledValueSet<Lookup, Double> rawCorrectedCount,
                                       LabelledValueSet<Lookup, Double> rawUncorrectedCount) {
        this.key = key;
        this.determinedCount = determinedCount;
        this.rawCorrectedCount = rawCorrectedCount;
        this.rawUncorrectedCount = rawUncorrectedCount;
    }

    public LabelledValueSet<Lookup, Integer> getDeterminedCount() {
        return determinedCount;
    }

    public StatsKey<X, Y> getKey() {
        return key;
    }

    public LabelledValueSet<Lookup, Integer> getFulfilledCount() {
        return fulfilledCount;
    }

    @Override
    public LabelledValueSet<Lookup, Double> getRawCorrectedCount() {
        return rawCorrectedCount;
    }

    @Override
    public LabelledValueSet<Lookup, Double> getRawUncorrectedCount() {
        return rawUncorrectedCount;
    }

    @Override
    public void setFulfilledCount(LabelledValueSet<Lookup, Integer> fulfilledCount) {
        this.fulfilledCount = fulfilledCount;
    }

    public abstract LabelledValueSet<Lookup, Integer> getZeroedCountsTemplate(RandomGenerator random);

}
