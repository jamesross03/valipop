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
import uk.ac.standrews.cs.valipop.implementations.OBDModel;
import uk.ac.standrews.cs.valipop.implementations.Randomness;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.determinedCounts.DeterminedCount;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.determinedCounts.SingleDeterminedCount;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsKeys.StatsKey;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsTables.dataDistributions.InputMetaData;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRange;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.InvalidRangeException;

import java.time.LocalDate;
import java.time.Year;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class SelfCorrectingTwoDimensionDataDistribution implements InputMetaData<IntegerRange>, SelfCorrection<Integer, Double, Integer, Integer> {

    // The integer range here represents the row labels (i.e. the age ranges on the ordered birth table)
    private Map<IntegerRange, SelfCorrectingOneDimensionDataDistribution> data;

    private Year year;
    private String sourcePopulation;

    private String sourceOrganisation;

    public SelfCorrectingTwoDimensionDataDistribution(Year year, String sourcePopulation, String sourceOrganisation, Map<IntegerRange, SelfCorrectingOneDimensionDataDistribution> tableData) {
        this.year = year;
        this.sourceOrganisation = sourceOrganisation;
        this.sourcePopulation = sourcePopulation;
        this.data = tableData;
    }

    public SingleDeterminedCount determineCount(StatsKey<Integer, Integer> key, Config config, RandomGenerator random) {
        try {
            if (OBDModel.global_debug) {
                System.out.println("Number of rng calls during separationStats determineCount: " + Randomness.call_count);
            }
            return getData(key.getXLabel()).determineCount(key, config, random);
        } catch (InvalidRangeException e) {
            return new SingleDeterminedCount(key, 0, 0, 0);
        }
    }

    public void returnAchievedCount(DeterminedCount<Integer, Double, Integer, Integer> achievedCount, RandomGenerator random) {
        try {
            getData(achievedCount.getKey().getXLabel()).returnAchievedCount(achievedCount, random);
        } catch (InvalidRangeException e) {
            if (achievedCount.getDeterminedCount() != 0) throw e;
        }
    }

    public SelfCorrectingOneDimensionDataDistribution getData(Integer yLabel) throws InvalidRangeException {

        IntegerRange row = resolveRowValue(yLabel);
        return data.get(row);
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
        for (IntegerRange iR : data.keySet()) {
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
        for (IntegerRange iR : data.keySet()) {
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
        return getRowLabels();
    }

    private IntegerRange resolveRowValue(Integer rowValue) {

        for (IntegerRange iR : data.keySet()) {
            if (iR.contains(rowValue)) {
                return iR;
            }
        }

        throw new InvalidRangeException("Given value not covered by rows - value " + rowValue);
    }

    public Set<IntegerRange> getRowLabels() {
        return data.keySet();
    }

    public Set<IntegerRange> getColumnLabels() {
        return data.get(resolveRowValue(getSmallestLabel().getValue())).getLabels();
    }
}
