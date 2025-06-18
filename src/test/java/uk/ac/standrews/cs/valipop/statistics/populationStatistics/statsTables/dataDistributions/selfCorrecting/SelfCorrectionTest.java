/*
 * Copyright 2017 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module population_model.
 *
 * population_model is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * population_model is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with population_model. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsTables.dataDistributions.selfCorrecting;

import org.junit.Test;
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

import static org.junit.Assert.assertEquals;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class SelfCorrectionTest {

    private final double DELTA = 1E-10;

    private SelfCorrectingOneDimensionDataDistribution createSC1DDD() {

        Map<IntegerRange, Double> data = new TreeMap<>();
        data.put(new IntegerRange(0), 0.1);
        data.put(new IntegerRange(1), 0.000037);
        data.put(new IntegerRange(2), 0D);
        data.put(new IntegerRange(3), 1D);
        data.put(new IntegerRange(4), 0.5);
        data.put(new IntegerRange(5), 0.01);

        return new SelfCorrectingOneDimensionDataDistribution(Year.of(0), "test", "test", data, false, PopulationStatistics.randomGenerator);
    }

    @Test
    public void firstAccessSC1DDD() {

        SelfCorrectingOneDimensionDataDistribution sc1DDD = createSC1DDD();
        OneDimensionDataDistribution sc1DDDCopy = sc1DDD.clone();

        Period y = Period.ofYears(1);

        for (IntegerRange iR : sc1DDD.getRate().keySet()) {

            double check = sc1DDDCopy.getRate(iR.getValue());

            // Basic first retrieval tests
            StatsKey<Integer,Integer> k1 = new DeathStatsKey(iR.getValue(), 100, y, null, SexOption.MALE);
            @SuppressWarnings("rawtypes")
            DeterminedCount r1 = sc1DDD.determineCount(k1, null, PopulationStatistics.randomGenerator);
            assertEquals((int) Math.round(check * 100), (int) r1.getDeterminedCount(), DELTA);

            StatsKey<Integer, Integer> k2 = new DeathStatsKey(iR.getValue(), 1000, y, null, SexOption.MALE);
            @SuppressWarnings("rawtypes")
            DeterminedCount r2 = sc1DDD.determineCount(k2, null, PopulationStatistics.randomGenerator);
            assertEquals((int) Math.round(check * 1000), (int) r2.getDeterminedCount(), DELTA);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void correctingChecksSC1DDD() {

        SelfCorrectingOneDimensionDataDistribution sc1DDD = createSC1DDD();
        OneDimensionDataDistribution sc1DDDCopy = sc1DDD.clone();

        Period y = Period.ofYears(1);

        Period[] tps = {y};

        for (Period tp : tps) {

            for (IntegerRange iR : sc1DDD.getRate().keySet()) {

                StatsKey<Integer, Integer> k1 = new DeathStatsKey(iR.getValue(), 100, tp, null, SexOption.MALE);

                double c1 = sc1DDDCopy.getRate(iR.getValue());

                @SuppressWarnings("rawtypes")
                DeterminedCount r1 = sc1DDD.determineCount(k1, null, PopulationStatistics.randomGenerator);
                assertEquals((int) Math.round(c1 * r1.getKey().getForNPeople()), r1.getDeterminedCount());

                int rr2 = (int) Math.round(1.5 * (int) r1.getDeterminedCount());
                r1.setFulfilledCount(rr2);
                sc1DDD.returnAchievedCount(r1, PopulationStatistics.randomGenerator);
            }
        }
    }

    @SuppressWarnings({ "unused", "rawtypes" })
    private int calcExpectedCount(DeterminedCount applied, StatsKey corrective, double targetRate) {

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
    private int calcUnfetteredExpectedCount(DeterminedCount applied, StatsKey corrective, double targetRate) {

        return (int) Math.ceil(targetRate * (applied.getKey().getForNPeople() + corrective.getForNPeople()) - (int) applied.getFulfilledCount());
    }

    @SuppressWarnings({ "unused", "rawtypes" })
    private double calcAdditiveRate(StatsKey k1, double r1, StatsKey k2, double r2) {

        return (r1 * k1.getForNPeople() + r2 * k2.getForNPeople()) / (k1.getForNPeople() + k2.getForNPeople());
    }

    @SuppressWarnings({ "unused", "rawtypes" })
    private double calcExpectedCorrectiveRate(double targetRate, double returnedRate, StatsKey returnedKey, StatsKey checkKey) {

        double expectedCorrectiveRate = (targetRate * (returnedKey.getForNPeople() + checkKey.getForNPeople()) - (returnedRate * returnedKey.getForNPeople())) / checkKey.getForNPeople();

        if (expectedCorrectiveRate > 1) {
            return 1;
        }

        if (expectedCorrectiveRate < 0) {
            return 0;
        }

        return expectedCorrectiveRate;
    }

    @SuppressWarnings({ "unused", "rawtypes" })
    private double calcUnfetteredExpectedCorrectiveRate(double targetRate, double returnedRate, StatsKey returnedKey, StatsKey checkKey) {

        return (targetRate * (returnedKey.getForNPeople() + checkKey.getForNPeople()) - (returnedRate * returnedKey.getForNPeople())) / checkKey.getForNPeople();
    }

    @Test
    public void variedTimeStepTest() {

        SelfCorrectingOneDimensionDataDistribution data = createSC1DDD();

        int age = 5;

        int popSize = 1000000;
        Period y = Period.ofYears(1);
        Period m2 = Period.ofMonths(2);

        StatsKey<Integer, Integer> yearK = new DeathStatsKey(age, popSize, y, null, SexOption.MALE);
        int expPopSize = popSize - data.determineCount(yearK, null, PopulationStatistics.randomGenerator).getDeterminedCount();

        for (int m = 1; m <= 12; m += 2) {
            StatsKey<Integer, Integer> k = new DeathStatsKey(age, popSize, m2, null, SexOption.MALE);

            int count = data.determineCount(k, null, PopulationStatistics.randomGenerator).getDeterminedCount();

            popSize -= count;
        }

        assertEquals(expPopSize, popSize);
    }
}
