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
package uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsTables.dataDistributions.selfCorrecting;

import org.apache.commons.math3.random.RandomGenerator;
import uk.ac.standrews.cs.valipop.Config;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.determinedCounts.DeterminedCount;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.determinedCounts.MultipleDeterminedCountByIR;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsKeys.StatsKey;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsTables.dataDistributions.SelfCorrectingProportionalDistribution;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRange;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRangeToDoubleSet;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRangeToIntegerSet;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.LabelledValueSet;

import java.time.Year;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class MotherChildAdapter implements SelfCorrectingProportionalDistribution<IntegerRange, Integer, Integer> {

    private SelfCorrecting2DIntegerRangeProportionalDistribution distribution;

    public MotherChildAdapter(Year year, String sourcePopulation, String sourceOrganisation, Map<IntegerRange, LabelledValueSet<IntegerRange, Double>> targetProportions, RandomGenerator random) {

        Map<IntegerRange, LabelledValueSet<IntegerRange, Double>> transformedProportions = new TreeMap<>();

        for (Map.Entry<IntegerRange, LabelledValueSet<IntegerRange, Double>> iR : targetProportions.entrySet()) {
            LabelledValueSet<IntegerRange, Double> tp = iR.getValue();

            if (tp.getSumOfValues() != 0) {
                transformedProportions.put(iR.getKey(), new IntegerRangeToDoubleSet(tp, random).productOfLabelsAndValues().reproportion());
            } else {
                transformedProportions.put(iR.getKey(), tp);
            }
        }

        distribution = new SelfCorrecting2DIntegerRangeProportionalDistribution(year, sourcePopulation, sourceOrganisation, transformedProportions, random);
    }

    @Override
    public Year getYear() {
        return distribution.getYear();
    }

    @Override
    public String getSourcePopulation() {
        return distribution.getSourcePopulation();
    }

    @Override
    public String getSourceOrganisation() {
        return distribution.getSourceOrganisation();
    }

    @Override
    public IntegerRange getSmallestLabel() {
        return distribution.getSmallestLabel();
    }

    @Override
    public IntegerRange getLargestLabel() {
        return distribution.getLargestLabel();
    }

    @Override
    public Collection<IntegerRange> getLabels() {
        return distribution.getLabels();
    }

    @Override
    public MultipleDeterminedCountByIR determineCount(StatsKey<Integer, Integer> key, Config config, RandomGenerator random) {

        MultipleDeterminedCountByIR childNumbers = distribution.determineCount(key, config, random);

        LabelledValueSet<IntegerRange, Double> rawCorrectedMotherNumbers = new IntegerRangeToDoubleSet(childNumbers.getRawCorrectedCount(), random).divisionOfValuesByLabels();
        LabelledValueSet<IntegerRange, Double> rawUncorrectedMotherNumbers = new IntegerRangeToDoubleSet(childNumbers.getRawUncorrectedCount(), random).divisionOfValuesByLabels();

        try {
            LabelledValueSet<IntegerRange, Integer> motherNumbers = new IntegerRangeToIntegerSet(childNumbers.getDeterminedCount(), random).divisionOfValuesByLabels().controlledRoundingMaintainingSumProductOfLabelValues();

            return new MultipleDeterminedCountByIR(key, motherNumbers, rawCorrectedMotherNumbers, rawUncorrectedMotherNumbers);

        } catch (NullPointerException e) {
            return new MultipleDeterminedCountByIR(key, null, rawCorrectedMotherNumbers, rawUncorrectedMotherNumbers);
        }
    }

    @Override
    public void returnAchievedCount(DeterminedCount<LabelledValueSet<IntegerRange, Integer>, LabelledValueSet<IntegerRange, Double>, Integer, Integer> achievedCount, RandomGenerator random) {

        // Transforms counts to be of children born rather than mothers giving birth
        achievedCount.setFulfilledCount(new IntegerRangeToIntegerSet(achievedCount.getFulfilledCount(), random).productOfLabelsAndValues());
        distribution.returnAchievedCount(achievedCount, random);
    }
}
