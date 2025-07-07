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
package uk.ac.standrews.cs.valipop.implementations;

import uk.ac.standrews.cs.valipop.Config;
import uk.ac.standrews.cs.valipop.utils.AnalysisThread;
import uk.ac.standrews.cs.valipop.utils.ProcessArgs;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Period;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class FactorSearch {

    private static final int THREAD_LIMIT = 2;
    public static int threadCount = 0;

    public static void main(String[] args) throws InterruptedException {

        String[] pArgs = ProcessArgs.process(args, "FACTOR_SEARCH_PRECISION");
        if (!ProcessArgs.check(pArgs, "FACTOR_SEARCH_PRECISION")) {
            System.err.println("Incorrect arguments given");
            throw new Error("Incorrect arguments given");
        }

        Path dataFiles = Paths.get(pArgs[0]);
        int seedSize = Integer.valueOf(pArgs[1]);
        String runPurpose = pArgs[2];
        System.out.println(pArgs[3]);
        int numberOfRunsPerSim = Integer.valueOf(pArgs[3]);

        String rfsArg = pArgs[4];
        String prfsArg = pArgs[5];

        Path resultsDir = Paths.get(pArgs[6]);

        double[] precisions = toDoubleArray(pArgs[7]);

        double[] rfs = toDoubleArray(rfsArg);
        double[] prfs = toDoubleArray(prfsArg);

        runFactorSearch(seedSize, rfs, prfs, precisions, dataFiles, numberOfRunsPerSim, runPurpose, resultsDir);
    }

    private static double[] toDoubleArray(String rfsArg) {

        String[] split = rfsArg.split(",");
        double[] ret = new double[split.length];

        int c = 0;

        for (String s : split) {
            ret[c++] = Double.valueOf(s);
        }

        return ret;
    }

    private static void runFactorSearch(int size0, double[] recovery_factors, double[] proportional_recovery_factors, double[] precisions, Path dataFiles, int numberOfRunsPerSim, String runPurpose, Path results_save_location) throws InterruptedException {

        final LocalDate tS = LocalDate.of(1599, 1, 1);
        final LocalDate t0 = LocalDate.of(1855, 1, 1);
        final LocalDate tE = LocalDate.of(2015, 1, 1);

        final double set_up_br = 0.0233;
        final double set_up_dr = 0.0322;

        final Period[] input_widths = new Period[]{Period.ofYears(10)};
        final Period[] minBirthSpacings = new Period[]{Period.ofDays(147)};
        final int[] t0_pop_sizes = new int[]{size0};

        for (double precision : precisions) {

            for (int size : t0_pop_sizes) {
                for (double recovery_factor : recovery_factors) {
                    for (double proportional_recovery_factor : proportional_recovery_factors) {
                        for (Period input_width : input_widths) {
                            for (Period minBirthSpacing : minBirthSpacings) {
                                for (int n = 0; n < numberOfRunsPerSim; n++) {

                                    Config config = new Config(tS, t0, tE, size, dataFiles, results_save_location, runPurpose, results_save_location);

                                    config.setCTtreePrecision(precision);
                                    config.setRunPurpose(runPurpose);
                                    config.setSetupBirthRate(set_up_br);
                                    config.setSetupDeathRate(set_up_dr);
                                    config.setRecoveryFactor(recovery_factor);
                                    config.setProportionalRecoveryFactor(proportional_recovery_factor);
                                    config.setInputWidth(input_width);
                                    config.setMinBirthSpacing(minBirthSpacing);

                                    OBDModel model = new OBDModel(config);
                                    try {
                                        model.runSimulation();
                                        model.analyseAndOutputPopulation(false);

                                    } catch (PreEmptiveOutOfMemoryWarning e) {
                                        model.getSummaryRow().outputSummaryRowToFile();
                                        throw e;
                                    }

                                    while (threadCount >= THREAD_LIMIT) {
                                        Thread.sleep(10000);
                                    }

                                    new AnalysisThread(model, config, threadCount).start();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
