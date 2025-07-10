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
import uk.ac.standrews.cs.valipop.implementations.OBDModel;
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
 * Trying to correct by compensating against what couldn't acheive the statistic in the prior year.
 * 
 * But correcting quickly could be inaccurate and slowly could change distribution too much.
 * 
 * Perhaps better to have correction value per properity in model.
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class SelfCorrectingOneDimensionDataDistribution extends OneDimensionDataDistribution implements SelfCorrection<Integer, Double, Integer, Integer> {

    private boolean binomialSampling;
    private RandomGenerator rng;

    private Map<IntegerRange, Double> appliedRates;
    private Map<IntegerRange, Double> appliedCounts;

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

        // target rate
        final double targetRate = targetRates.get(range);

        // applied count
        final double appliedCount = appliedCounts.get(range);

        // if no correction data - i.e. first call to this method
        if (appliedCount == 0) {
            final double rateToApply = calcSubRateFromYearRate(targetRate, key.getConsideredTimePeriod());
            SingleDeterminedCount singleDeterminedCount = resolveRateToCount(key, rateToApply, rateToApply);
            if ((OBDModel.global_debug))
            {
                System.out.println("targetRates:");
                for (IntegerRange r : targetRates.keySet()) {
                    System.out.println("\t" + r + ": " + targetRates.get(r));
                }
                System.out.println();

                System.out.println("range: " + range);
                System.out.println("targetRate: " + targetRate);
                System.out.println("rateToApply: " + rateToApply);
                System.out.println("getDeterminedCount: " + singleDeterminedCount.getDeterminedCount());
                System.out.println("getFulfilledCount: " + singleDeterminedCount.getFulfilledCount());
                System.out.println("getRawUncorrectedCount: " + singleDeterminedCount.getRawUncorrectedCount());
                System.out.println("getRawCorrectedCount: " + singleDeterminedCount.getRawCorrectedCount());
            }
            return singleDeterminedCount; // Same due to correction rate currently same as target rate

        }

        // to apply to
        final double tAT = key.getForNPeople();

        // applied rate
        final double appliedRate = appliedRates.get(range);

        // if no N value given in StatsKey
        if (tAT == 0) {
            final double rateToApply = calcSubRateFromYearRate(targetRate, key.getConsideredTimePeriod());
            if ((OBDModel.global_debug))
            {
                System.out.println("rateToApply: " + rateToApply);
            }
            return resolveRateToCount(key, rateToApply, rateToApply);
        }

        double rf = 1;
        if (config != null) {
            rf = config.getRecoveryFactor();
        }

        // shortfall
        final double fall = Math.ceil((appliedCount * targetRate) - (appliedCount * appliedRate));

        double cD;
        if (fall > 0) {
            cD = (fall * rf + tAT * targetRate) / tAT;
        } else {
            cD = 0;
        }

        // Checks that rate falls in bounds
        if (cD < 0) {
            cD = 0;
        } else if (cD > 1) {
            cD = 1;
        }

        final double rateToApply = calcSubRateFromYearRate(cD, key.getConsideredTimePeriod());
        final double uncorrectedRate = calcSubRateFromYearRate(targetRate, key.getConsideredTimePeriod());


//        if ((OBDModel.global_debug))
//        {
//            System.out.println("rateToApply: " + rateToApply);
//            System.out.println("uncorrectedRate: " + uncorrectedRate);
//        }



            return resolveRateToCount(key, rateToApply, uncorrectedRate);
    }

    public void returnAchievedCount(final DeterminedCount<Integer, Double, Integer, Integer> achievedCount, final RandomGenerator random) {

        final StatsKey<Integer, Integer> key = achievedCount.getKey();

        final int count = achievedCount.getFulfilledCount();
        double achievedRate = 0;
        if (key.getForNPeople() != 0) {
            achievedRate = count / key.getForNPeople();
        }

        // This is age for Death (1DDD) but this is order in the case of birth (2DDD)
        final IntegerRange age = resolveRowValue(key.getYLabel());

        // old applied rate
        final double aDo = appliedRates.get(age);

        // old applied count
        final double aCo = appliedCounts.get(age);

        // actually applied correction rate
        final double aacD = calcAppliedYearRateFromSubRate(achievedRate, key.getConsideredTimePeriod());

        // to apply to
        final Double tAT = key.getForNPeople();

        // new applied count
        final double aCn = aCo + tAT;

        // new applied rate
        double aDn = 0;
        if (aCn != 0) {
            aDn = ((aDo * aCo) + (aacD * tAT)) / aCn;
        }

        // target rate
        final double tD = targetRates.get(age);

        // if new applied rate has switched across target rate then reset count
        if ((aDo < tD && aDn >= tD) || (aDo > tD && aDn <= tD)) {

            // the number of people it takes at the the applied rate back to the target rate
            final double numberOfPeopleToBringRateToCrossOverPoint;

            if (tD == aacD) {
                numberOfPeopleToBringRateToCrossOverPoint = tAT;
            } else {
                numberOfPeopleToBringRateToCrossOverPoint = (aCo * (aDo - tD)) / (tD - aacD);
            }

            appliedRates.replace(age, aacD);
            appliedCounts.replace(age, tAT - numberOfPeopleToBringRateToCrossOverPoint);
        } else {
            appliedRates.replace(age, aDn);
            appliedCounts.replace(age, aCn);
        }
    }

    private SingleDeterminedCount resolveRateToCount(final StatsKey<Integer,Integer> key, final double rate, final double uncorrectedRate) {

        final double rawCorrectedCount = rate * key.getForNPeople();
        final double rawUncorrectedCount = uncorrectedRate * key.getForNPeople();

//        if ((OBDModel.global_debug))
//        {
//            System.out.println("rawCorrectedCount: " + rawCorrectedCount);
//            System.out.println("rawUncorrectedCount: " + rawUncorrectedCount);
//            System.out.println("binomialSampling: " + binomialSampling);
//            System.out.println("key.getForNPeople(): " + key.getForNPeople());
//        }

        final int determinedCount;
        if (binomialSampling) {


//            if ((OBDModel.global_debug))
//            {
//                System.out.println("rng state before passing to binomial distribution: " + rng.nextDouble());
//            }
            determinedCount = new BinomialDistribution(rng, (int) Math.round(key.getForNPeople()), rate).sample();
//            if ((OBDModel.global_debug))
//            {
//                System.out.println("determinedCount: " + determinedCount);
//            }
        } else {
            determinedCount = (int) Math.round(rate * key.getForNPeople());
        }

        return new SingleDeterminedCount(key, determinedCount, rawCorrectedCount, rawUncorrectedCount);
    }

    private double calcAppliedYearRateFromSubRate(final double subRate, final Period timePeriod) {

        return 1 - Math.pow(1 - subRate, DateUtils.stepsInYear(timePeriod));
    }

    private double calcSubRateFromYearRate(final double yearRate, final Period timePeriod) {

        final double stepsInYear = DateUtils.stepsInYear(timePeriod);

        return 1 - Math.pow(1 - yearRate, 1 / stepsInYear);
    }
}
