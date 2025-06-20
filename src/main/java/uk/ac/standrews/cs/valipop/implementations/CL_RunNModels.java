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

import uk.ac.standrews.cs.valipop.utils.ProcessArgs;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Command line entry point to run n population models and analysis.
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class CL_RunNModels {

    public static void main(String[] args) throws IOException, StatsException {
        CL_RunNModels.runNModels(args);
    }

    public static void runNModels(String[] args) throws IOException, StatsException {
        // Expects 4 args: path to config file, results path, run purpose, number of runs

        String[] pArgs = ProcessArgs.process(args, "N-RUNS");
        if (!ProcessArgs.check(pArgs, "N-RUNS")) {
            throw new Error("Incorrect arguments given");
        }

        Path pathToConfigFile = Paths.get(pArgs[0]);
        //String resultsPath = pArgs[1];
        //String runPurpose = pArgs[2];

        int nRuns = Integer.parseInt(pArgs[3]);

        for (int n = 0; n < nRuns; n++) {

            CL_OBDModel.runOBDModel(pathToConfigFile);
            System.gc();
        }
    }
}
