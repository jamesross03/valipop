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

import org.junit.jupiter.api.Test;
import uk.ac.standrews.cs.valipop.implementations.Randomness;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SexOption;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.PopulationStatistics;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.determinedCounts.DeterminedCount;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsKeys.DeathStatsKey;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsKeys.StatsKey;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsTables.dataDistributions.OneDimensionDataDistribution;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRange;

import java.time.Period;
import java.time.Year;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class SelfCorrectionTest {

    private final double DELTA = 1E-10;

    private SelfCorrectingOneDimensionDataDistribution createSC1DDD() {

        final Map<IntegerRange, Double> data = new TreeMap<>();
        data.put(new IntegerRange(0), 0.1);
        data.put(new IntegerRange(1), 0.000037);
        data.put(new IntegerRange(2), 0D);
        data.put(new IntegerRange(3), 1D);
        data.put(new IntegerRange(4), 0.5);
        data.put(new IntegerRange(5), 0.01);

        return new SelfCorrectingOneDimensionDataDistribution(Year.of(0), "test", "test", data, false, Randomness.getRandomGenerator());
    }

    @Test
    public void firstAccessSC1DDD() {

        final SelfCorrectingOneDimensionDataDistribution sc1DDD = createSC1DDD();
        final OneDimensionDataDistribution sc1DDDCopy = sc1DDD.clone();

        final Period y = Period.ofYears(1);

        for (final IntegerRange iR : sc1DDD.getRate().keySet()) {

            final double check = sc1DDDCopy.getRate(iR.getValue());

            // Basic first retrieval tests
            final StatsKey<Integer,Integer> k1 = new DeathStatsKey(iR.getValue(), 100, y, null, SexOption.MALE);
            @SuppressWarnings("rawtypes") final DeterminedCount r1 = sc1DDD.determineCount(k1, null, Randomness.getRandomGenerator());
            assertEquals((int) Math.round(check * 100), (int) r1.getDeterminedCount(), DELTA);

            final StatsKey<Integer, Integer> k2 = new DeathStatsKey(iR.getValue(), 1000, y, null, SexOption.MALE);
            @SuppressWarnings("rawtypes") final DeterminedCount r2 = sc1DDD.determineCount(k2, null, Randomness.getRandomGenerator());
            assertEquals((int) Math.round(check * 1000), (int) r2.getDeterminedCount(), DELTA);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void correctingChecksSC1DDD() {

        final SelfCorrectingOneDimensionDataDistribution sc1DDD = createSC1DDD();
        final OneDimensionDataDistribution sc1DDDCopy = sc1DDD.clone();

        final Period y = Period.ofYears(1);

        final Period[] tps = {y};

        for (final Period tp : tps) {

            for (final IntegerRange iR : sc1DDD.getRate().keySet()) {

                final StatsKey<Integer, Integer> k1 = new DeathStatsKey(iR.getValue(), 100, tp, null, SexOption.MALE);

                final double c1 = sc1DDDCopy.getRate(iR.getValue());

                @SuppressWarnings("rawtypes") final DeterminedCount r1 = sc1DDD.determineCount(k1, null, Randomness.getRandomGenerator());
                assertEquals((int) Math.round(c1 * r1.getKey().getForNPeople()), r1.getDeterminedCount());

                final int rr2 = (int) Math.round(1.5 * (int) r1.getDeterminedCount());
                r1.setFulfilledCount(rr2);
                sc1DDD.returnAchievedCount(r1, Randomness.getRandomGenerator());
            }
        }
    }

    @SuppressWarnings({ "unused", "rawtypes" })
    private int calcExpectedCount(final DeterminedCount applied, final StatsKey corrective, final double targetRate) {

        int count = calcUnfetteredExpectedCount(applied, corrective, targetRate);

        if (count > corrective.getForNPeople()) {
            count = (int) corrective.getForNPeople();
        }

        if (count < 0) {
            count = 0;
        }

        return count;
    }

    @SuppressWarnings("rawtypes")
    private static int calcUnfetteredExpectedCount(final DeterminedCount applied, final StatsKey corrective, final double targetRate) {

        return (int) Math.ceil(targetRate * (applied.getKey().getForNPeople() + corrective.getForNPeople()) - (int) applied.getFulfilledCount());
    }

    @SuppressWarnings({ "unused", "rawtypes" })
    private static double calcAdditiveRate(final StatsKey k1, final double r1, final StatsKey k2, final double r2) {

        return (r1 * k1.getForNPeople() + r2 * k2.getForNPeople()) / (k1.getForNPeople() + k2.getForNPeople());
    }

    @SuppressWarnings({ "unused", "rawtypes" })
    private static double calcExpectedCorrectiveRate(final double targetRate, final double returnedRate, final StatsKey returnedKey, final StatsKey checkKey) {

        final double expectedCorrectiveRate = (targetRate * (returnedKey.getForNPeople() + checkKey.getForNPeople()) - (returnedRate * returnedKey.getForNPeople())) / checkKey.getForNPeople();

        if (expectedCorrectiveRate > 1) {
            return 1;
        }

        if (expectedCorrectiveRate < 0) {
            return 0;
        }

        return expectedCorrectiveRate;
    }

    @SuppressWarnings({ "unused", "rawtypes" })
    private static double calcUnfetteredExpectedCorrectiveRate(final double targetRate, final double returnedRate, final StatsKey returnedKey, final StatsKey checkKey) {

        return (targetRate * (returnedKey.getForNPeople() + checkKey.getForNPeople()) - (returnedRate * returnedKey.getForNPeople())) / checkKey.getForNPeople();
    }

    @Test
    public void variedTimeStepTest() {

        final SelfCorrectingOneDimensionDataDistribution data = createSC1DDD();

        final int age = 5;

        int popSize = 1000000;
        final Period y = Period.ofYears(1);
        final Period m2 = Period.ofMonths(2);

        final StatsKey<Integer, Integer> yearK = new DeathStatsKey(age, popSize, y, null, SexOption.MALE);
        final int expPopSize = popSize - data.determineCount(yearK, null, Randomness.getRandomGenerator()).getDeterminedCount();

        for (int m = 1; m <= 12; m += 2) {
            final StatsKey<Integer, Integer> k = new DeathStatsKey(age, popSize, m2, null, SexOption.MALE);

            final int count = data.determineCount(k, null, Randomness.getRandomGenerator()).getDeterminedCount();

            popSize -= count;
        }

        assertEquals(expPopSize, popSize);
    }
}
