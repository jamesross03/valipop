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

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import uk.ac.standrews.cs.valipop.Config;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.determinedCounts.DeterminedCount;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.determinedCounts.SingleDeterminedCount;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsKeys.StatsKey;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsTables.dataDistributions.OneDimensionDataDistribution;
import uk.ac.standrews.cs.valipop.utils.MapUtils;
import uk.ac.standrews.cs.valipop.utils.specialTypes.dates.DateUtils;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRange;

import java.time.Period;
import java.time.Year;
import java.util.Map;

/**
 * An implementation of the input data type, expecting a mapping of integer ranges
 * to floating point values and supporting self correction.
 * 
 * Trying to correct by compensating against what couldn't achieve the statistic in the prior year.
 * 
 * But correcting quickly could be inaccurate and slowly could change distribution too much.
 * 
 * Perhaps better to have correction value per property in model.
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class SelfCorrectingOneDimensionDataDistribution extends OneDimensionDataDistribution implements SelfCorrection<Integer, Double, Integer, Integer> {

    private final boolean binomialSampling;
    private final RandomGenerator rng;

    private final Map<IntegerRange, Double> appliedRates;
    private final Map<IntegerRange, Double> appliedCounts;

    public SelfCorrectingOneDimensionDataDistribution(final Year year, final String sourcePopulation, final String sourceOrganisation, final Map<IntegerRange, Double> tableData, final boolean binomialSampling, final RandomGenerator randomGenerator) {

        super(year, sourcePopulation, sourceOrganisation, tableData);

        this.appliedRates = MapUtils.cloneODM(tableData);
        this.appliedCounts = MapUtils.cloneODM(tableData);
        this.binomialSampling = binomialSampling;

        for (final IntegerRange iR : appliedCounts.keySet()) {
            appliedCounts.replace(iR, 0.0);
            appliedRates.replace(iR, 0.0);
        }

        rng = randomGenerator;
    }

    public SingleDeterminedCount determineCount(final StatsKey<Integer, Integer> key, final Config config, final RandomGenerator random) {

        final IntegerRange range = resolveRowValue(key.getYLabel());

        final double targetRate = targetRates.get(range);

        final double appliedCount = appliedCounts.get(range);

        // if no correction data - i.e. first call to this method
        if (appliedCount == 0) {
            final double rateToApply = calcSubRateFromYearRate(targetRate, key.getConsideredTimePeriod());
            return resolveRateToCount(key, rateToApply, rateToApply); // Same due to correction rate currently same as target rate
        }

        // to apply to
        final double tAT = key.getForNPeople();

        final double appliedRate = appliedRates.get(range);

        // if no N value given in StatsKey
        if (tAT == 0) {
            final double rateToApply = calcSubRateFromYearRate(targetRate, key.getConsideredTimePeriod());
            return resolveRateToCount(key, rateToApply, rateToApply);
        }

        double rf = config != null ? config.getRecoveryFactor() : 1;

        final double shortfall = Math.ceil((appliedCount * targetRate) - (appliedCount * appliedRate));

        double cD = Math.min(Math.max(shortfall > 0 ? (shortfall * rf + tAT * targetRate) / tAT : 0, 0), 1);

        final double rateToApply = calcSubRateFromYearRate(cD, key.getConsideredTimePeriod());
        final double uncorrectedRate = calcSubRateFromYearRate(targetRate, key.getConsideredTimePeriod());

        return resolveRateToCount(key, rateToApply, uncorrectedRate);
    }

    public void returnAchievedCount(final DeterminedCount<Integer, Double, Integer, Integer> achievedCount, final RandomGenerator random) {

        final StatsKey<Integer, Integer> key = achievedCount.getKey();

        final int count = achievedCount.getFulfilledCount();
        double achievedRate = 0;
        if (key.getForNPeople() != 0)
            achievedRate = count / key.getForNPeople();

        // This is age for Death (1DDD) but this is order in the case of birth (2DDD)
        final IntegerRange age = resolveRowValue(key.getYLabel());

        final double oldAppliedRate = appliedRates.get(age);

        final double oldAppliedCount = appliedCounts.get(age);

        final double appliedCorrectionRate = calcAppliedYearRateFromSubRate(achievedRate, key.getConsideredTimePeriod());

        // to apply to
        final double tAT = key.getForNPeople();

        final double newAppliedCount = oldAppliedCount + tAT;

        final double newAppliedRate = newAppliedCount != 0 ?
            ((oldAppliedRate * oldAppliedCount) + (appliedCorrectionRate * tAT)) / newAppliedCount :
            0;

        // target rate
        final double targetRate = targetRates.get(age);

        // if new applied rate has switched across target rate then reset count
        if ((oldAppliedRate < targetRate && newAppliedRate >= targetRate) || (oldAppliedRate > targetRate && newAppliedRate <= targetRate)) {

            // the number of people it takes at the the applied rate back to the target rate
            final double numberOfPeopleToBringRateToCrossOverPoint;

            if (targetRate == appliedCorrectionRate)
                numberOfPeopleToBringRateToCrossOverPoint = tAT;
            else
                numberOfPeopleToBringRateToCrossOverPoint = (oldAppliedCount * (oldAppliedRate - targetRate)) / (targetRate - appliedCorrectionRate);

            appliedRates.replace(age, appliedCorrectionRate);
            appliedCounts.replace(age, tAT - numberOfPeopleToBringRateToCrossOverPoint);

        } else {
            appliedRates.replace(age, newAppliedRate);
            appliedCounts.replace(age, newAppliedCount);
        }
    }

    private SingleDeterminedCount resolveRateToCount(final StatsKey<Integer,Integer> key, final double rate, final double uncorrectedRate) {

        final double rawCorrectedCount = rate * key.getForNPeople();
        final double rawUncorrectedCount = uncorrectedRate * key.getForNPeople();

        final int determinedCount = binomialSampling ?
            new BinomialDistribution(rng, (int) Math.round(key.getForNPeople()), rate).sample() :
            (int) Math.round(rate * key.getForNPeople());

        return new SingleDeterminedCount(key, determinedCount, rawCorrectedCount, rawUncorrectedCount);
    }

    private static double calcAppliedYearRateFromSubRate(final double subRate, final Period timePeriod) {

        return 1 - StrictMath.pow(1 - subRate, DateUtils.stepsInYear(timePeriod));
    }

    private static double calcSubRateFromYearRate(final double yearRate, final Period timePeriod) {

        final double stepsInYear = DateUtils.stepsInYear(timePeriod);

        return 1 - StrictMath.pow(1 - yearRate, 1 / stepsInYear);
    }
}
