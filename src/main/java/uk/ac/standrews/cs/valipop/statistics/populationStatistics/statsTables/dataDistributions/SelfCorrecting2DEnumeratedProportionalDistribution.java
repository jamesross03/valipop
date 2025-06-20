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
package uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsTables.dataDistributions;

import org.apache.commons.math3.random.RandomGenerator;
import uk.ac.standrews.cs.valipop.Config;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.determinedCounts.DeterminedCount;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.determinedCounts.MultipleDeterminedCountByString;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsKeys.StatsKey;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.*;

import java.time.Year;
import java.util.*;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class SelfCorrecting2DEnumeratedProportionalDistribution implements SelfCorrectingProportionalDistribution<String, String, String> {

    // The integer range here represents the row labels (i.e. the age ranges on the ordered birth table)
    private Map<String, LabelledValueSet<String, Double>> targetProportions;
    private Map<String, LabelledValueSet<String, Integer>> achievedCounts;

    private Year year;
    private String sourcePopulation;
    private String sourceOrganisation;

    public SelfCorrecting2DEnumeratedProportionalDistribution(Year year, String sourcePopulation, String sourceOrganisation, Map<String, LabelledValueSet<String, Double>> targetProportions, RandomGenerator random) {

        this.year = year;
        this.sourceOrganisation = sourceOrganisation;
        this.sourcePopulation = sourcePopulation;
        this.targetProportions = targetProportions;

        this.achievedCounts = new TreeMap<>();

        for (Map.Entry<String, LabelledValueSet<String, Double>> entry : targetProportions.entrySet()) {
            achievedCounts.put(entry.getKey(), new StringToIntegerSet(entry.getValue().getLabels(), 0, random));
        }
    }

    public MultipleDeterminedCountByString determineCount(StatsKey<String, String> key, Config config, RandomGenerator random) {

        String occupationA = key.getYLabel();

        LabelledValueSet<String, Integer> achievedCountsForOccupation;
//        try {
            achievedCountsForOccupation = achievedCounts.get(resolveRowValue(occupationA));

//        } catch (InvalidRangeException e) {

        if(achievedCountsForOccupation == null) {
            // If no stats in distribution for the given key then return a zero count object
            return new MultipleDeterminedCountByString(key,
                    new StringToIntegerSet(Collections.singleton(""), 0, random),
                    new StringToDoubleSet(Collections.singleton(""), 0.0, random),
                    new StringToDoubleSet(Collections.singleton(""), 0.0, random));
        }

        Integer sumOfAC = achievedCountsForOccupation.getSumOfValues();
        Double totalCount = sumOfAC + key.getForNPeople();

        double rf = 1;
        if (config != null) {
            rf = config.getProportionalRecoveryFactor();
        }

        LabelledValueSet<String, Double> rawFullCorrectionValues =
                targetProportions.get(resolveRowValue(occupationA)).productOfValuesAndN(totalCount).valuesSubtractValues(achievedCountsForOccupation);

        LabelledValueSet<String, Double> rawUncorrectedValues =
                targetProportions.get(resolveRowValue(occupationA)).productOfValuesAndN(key.getForNPeople());

        LabelledValueSet<String, Double> fullCorrectionAdjustment =
                rawFullCorrectionValues.valuesSubtractValues(rawUncorrectedValues);

        LabelledValueSet<String, Double> correctionAdjustment =
                fullCorrectionAdjustment.productOfValuesAndN(rf);

        LabelledValueSet<String, Double> rawCorrectedValues =
                rawUncorrectedValues.valuesPlusValues(correctionAdjustment);

        LabelledValueSet<String, Integer> retValues;
        try {
            retValues = new StringToDoubleSet(rawCorrectedValues, random).controlledRoundingMaintainingSum();
        } catch (ValuesDoNotSumToWholeNumberException e) {
            return new MultipleDeterminedCountByString(key, null, rawCorrectedValues, rawUncorrectedValues);
        }

        return new MultipleDeterminedCountByString(key, retValues, rawCorrectedValues, rawUncorrectedValues);
    }

    public void returnAchievedCount(DeterminedCount<LabelledValueSet<String, Integer>, LabelledValueSet<String, Double>, String, String> achievedCount, RandomGenerator random) {

        String occupationA = achievedCount.getKey().getYLabel();
        LabelledValueSet<String, Integer> previousAchievedCountsForOccupation;

//        try {
            previousAchievedCountsForOccupation = achievedCounts.get(resolveRowValue(occupationA));
//        } catch (InvalidRangeException e) {
//            return;
//        }

        if(previousAchievedCountsForOccupation == null)
            return;

        LabelledValueSet<String, Integer> newAchievedCountsForOccupation = achievedCount.getFulfilledCount();

        LabelledValueSet<String, Integer> summedAchievedCountsForOccupation = previousAchievedCountsForOccupation.valuesPlusValues(newAchievedCountsForOccupation).floorValues();

        achievedCounts.replace(resolveRowValue(occupationA), previousAchievedCountsForOccupation, summedAchievedCountsForOccupation);
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
    public String getSmallestLabel() {

        ArrayList<String> al = new ArrayList<String>(targetProportions.keySet());
        Collections.sort(al);
        return al.get(0);

//        int min = Integer.MAX_VALUE;
//        IntegerRange minRange = null;
//        for (IntegerRange iR : targetProportions.keySet()) {
//            int v = iR.getMin();
//            if (v < min) {
//                min = v;
//                minRange = iR;
//            }
//        }
//        return minRange;
    }

    @Override
    public String getLargestLabel() {

        ArrayList<String> al = new ArrayList<String>(targetProportions.keySet());
        Collections.sort(al);
        return al.get(al.size() - 1);

//        IntegerRange max = null;
//        int maxV = Integer.MIN_VALUE;
//        for (IntegerRange iR : targetProportions.keySet()) {
//            int v = iR.getMax();
//            if (v > maxV) {
//                max = iR;
//                maxV = v;
//            }
//        }
//        return max;
    }

    @Override
    public Collection<String> getLabels() {
        return targetProportions.keySet();
    }

    private String resolveRowValue(String rowValue) {


        // the fast way
        return rowValue;

        // the very slow and graceful way
//        if(targetProportions.containsKey(rowValue)) {
//            return rowValue;
//        }
//
//        throw new InvalidRangeException("Given value not covered by rows - value " + rowValue);
    }
}
