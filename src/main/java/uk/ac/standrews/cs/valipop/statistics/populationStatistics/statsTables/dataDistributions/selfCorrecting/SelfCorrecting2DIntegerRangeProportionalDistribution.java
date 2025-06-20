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
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.InvalidRangeException;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRangeToDoubleSet;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRangeToIntegerSet;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.LabelledValueSet;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.ValuesDoNotSumToWholeNumberException;

import java.time.Year;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class SelfCorrecting2DIntegerRangeProportionalDistribution implements SelfCorrectingProportionalDistribution<IntegerRange, Integer, Integer> {
    // this is a 2 dimentional table? YES

    // The integer range here represents the row labels (i.e. the age ranges on the ordered birth table)
    // Map<FemaleAge, LVS<MaleAge, Proportion>>
    private Map<IntegerRange, LabelledValueSet<IntegerRange, Double>> targetProportions;
    private Map<IntegerRange, LabelledValueSet<IntegerRange, Integer>> achievedCounts;

    private Year year;
    private String sourcePopulation;
    private String sourceOrganisation;

    public SelfCorrecting2DIntegerRangeProportionalDistribution(Year year, String sourcePopulation, String sourceOrganisation, Map<IntegerRange, LabelledValueSet<IntegerRange, Double>> targetProportions, RandomGenerator random) {

        this.year = year;
        this.sourceOrganisation = sourceOrganisation;
        this.sourcePopulation = sourcePopulation;
        this.targetProportions = targetProportions;

        this.achievedCounts = new TreeMap<>();

        for (Map.Entry<IntegerRange, LabelledValueSet<IntegerRange, Double>> iR : targetProportions.entrySet()) {
            achievedCounts.put(iR.getKey(), new IntegerRangeToIntegerSet(iR.getValue().getLabels(), 0, random));
        }
    }

    public MultipleDeterminedCountByIR determineCount(StatsKey<Integer, Integer> key, Config config, RandomGenerator random) {

        int age = key.getYLabel();

        LabelledValueSet<IntegerRange, Integer> achievedCountsForAge;
        try {
            achievedCountsForAge = achievedCounts.get(resolveRowValue(age));

        } catch (InvalidRangeException e) {
            // If no stats in distribution for the given key then return a zero count object
            return new MultipleDeterminedCountByIR(key,
                    new IntegerRangeToIntegerSet(Collections.singleton(new IntegerRange(1)), 0, random),
                    new IntegerRangeToDoubleSet(Collections.singleton(new IntegerRange(1)), 0.0, random),
                    new IntegerRangeToDoubleSet(Collections.singleton(new IntegerRange(1)), 0.0, random));
        }

        Integer sumOfAC = achievedCountsForAge.getSumOfValues();
        Double totalCount = sumOfAC + key.getForNPeople();

        double rf = 1;
        if (config != null) {
            rf = config.getProportionalRecoveryFactor();
        }

        LabelledValueSet<IntegerRange, Double> rawFullCorrectionValues =
                targetProportions.get(resolveRowValue(age)).productOfValuesAndN(totalCount).valuesSubtractValues(achievedCountsForAge);

        LabelledValueSet<IntegerRange, Double> rawUncorrectedValues =
                targetProportions.get(resolveRowValue(age)).productOfValuesAndN(key.getForNPeople());

        LabelledValueSet<IntegerRange, Double> fullCorrectionAdjustment =
                rawFullCorrectionValues.valuesSubtractValues(rawUncorrectedValues);

        LabelledValueSet<IntegerRange, Double> correctionAdjustment =
                fullCorrectionAdjustment.productOfValuesAndN(rf);

        LabelledValueSet<IntegerRange, Double> rawCorrectedValues =
                rawUncorrectedValues.valuesPlusValues(correctionAdjustment);

        LabelledValueSet<IntegerRange, Integer> retValues;
        try {
            retValues = new IntegerRangeToDoubleSet(rawCorrectedValues, random).controlledRoundingMaintainingSum();
        } catch (ValuesDoNotSumToWholeNumberException e) {
            return new MultipleDeterminedCountByIR(key, null, rawCorrectedValues, rawUncorrectedValues);
        }

        return new MultipleDeterminedCountByIR(key, retValues, rawCorrectedValues, rawUncorrectedValues);
    }

    public void returnAchievedCount(DeterminedCount<LabelledValueSet<IntegerRange, Integer>, LabelledValueSet<IntegerRange, Double>, Integer, Integer> achievedCount, RandomGenerator random) {

        int age = achievedCount.getKey().getYLabel();
        LabelledValueSet<IntegerRange, Integer> previousAchievedCountsForAge;

        try {
            previousAchievedCountsForAge = achievedCounts.get(resolveRowValue(age));
        } catch (InvalidRangeException e) {
            return;
        }

        LabelledValueSet<IntegerRange, Integer> newAchievedCountsForAge = achievedCount.getFulfilledCount();

        LabelledValueSet<IntegerRange, Integer> summedAchievedCountsForAge = previousAchievedCountsForAge.valuesPlusValues(newAchievedCountsForAge).floorValues();

        achievedCounts.replace(resolveRowValue(age), previousAchievedCountsForAge, summedAchievedCountsForAge);
    }

    @Override
    public Year getYear() {
        return year;
    }

    @Override
    public String getSourcePopulation() {
        return sourcePopulation;
    }

    @Override
    public String getSourceOrganisation() {
        return sourceOrganisation;
    }

    @Override
    public IntegerRange getSmallestLabel() {
        int min = Integer.MAX_VALUE;
        IntegerRange minRange = null;
        for (IntegerRange iR : targetProportions.keySet()) {
            int v = iR.getMin();
            if (v < min) {
                min = v;
                minRange = iR;
            }
        }
        return minRange;
    }

    @Override
    public IntegerRange getLargestLabel() {
        IntegerRange max = null;
        int maxV = Integer.MIN_VALUE;
        for (IntegerRange iR : targetProportions.keySet()) {
            int v = iR.getMax();
            if (v > maxV) {
                max = iR;
                maxV = v;
            }
        }
        return max;
    }

    @Override
    public Collection<IntegerRange> getLabels() {
        return targetProportions.keySet();
    }

    private IntegerRange resolveRowValue(Integer rowValue) {

        for (IntegerRange iR : targetProportions.keySet()) {
            if (iR.contains(rowValue)) {
                return iR;
            }
        }

        throw new InvalidRangeException("Given value not covered by rows - value " + rowValue);
    }
}
