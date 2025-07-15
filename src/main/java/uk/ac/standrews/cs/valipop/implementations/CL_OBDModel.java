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
import uk.ac.standrews.cs.valipop.utils.ProcessArgs;
import uk.ac.standrews.cs.valipop.utils.ProgramTimer;
import uk.ac.standrews.cs.valipop.utils.RCaller;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Year;

/**
 * Command line entry point to simulate a population model and analysis.
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class CL_OBDModel {

    public static void main(final String[] args) throws IOException, StatsException {

        final String[] pArgs = ProcessArgs.process(args, "STANDARD");

        if (ProcessArgs.check(pArgs, "STANDARD")) {
            runOBDModel(Paths.get(pArgs[0]));
        } else {
            System.err.println("Incorrect arguments given");
        }
    }

    public static void runOBDModel(final Path pathToConfigFile) throws IOException, PreEmptiveOutOfMemoryWarning, StatsException {

        final Config config = new Config(pathToConfigFile);
        System.out.println("Running simulation with " + pathToConfigFile.toAbsolutePath());

        final OBDModel model = new OBDModel(config);
        model.runSimulation();
        model.analyseAndOutputPopulation(false);

        if (config.shouldGenerateContingencyTables())
            performAnalysis(model, config);

        model.getSummaryRow().outputSummaryRowToFile();
    }

    private static void performAnalysis(final OBDModel model, final Config config) throws IOException, StatsException {

        final ProgramTimer statsTimer = new ProgramTimer();

        final int value = model.getDesiredPopulationStatistics().getOrderedBirthRates(Year.of(0)).getLargestLabel().getValue();

        final double v = RCaller.getGeeglmV(config.getRunPath(), value);

        // This gives a human readable score
        String score = "good";
        if (v > 0 && v <= 10) {
            score = "okay";
        } else if (v > 30) {
            score = "bad";
        }

        System.out.println("Validation score: " + v + " (" + score + ")");

        model.getSummaryRow().setStatsRunTime(statsTimer.getRunTimeSeconds());
        model.getSummaryRow().setV(v);
    }
}
