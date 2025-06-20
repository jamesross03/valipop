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
package uk.ac.standrews.cs.valipop.utils;

import uk.ac.standrews.cs.valipop.implementations.StatsException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;

/**
 * For extracting, invoking, and reading the results of the R analysis scripts.
 * 
 * @author Daniel Brathagen (db255@st-andrews.ac.uk)
 */
public class RCaller {

    // Constants

    private static final Path R_SCRIPT_LOCATION = Path.of("analysis.R");
    private static final Path R_SCRIPT_OUTPUT_LOCATION = Path.of("analysis.out");

    private static final String[] R_SCRIPT_PATHS = {
        "valipop/analysis-r/geeglm/process-data-functions.R",
        "valipop/analysis-r/geeglm/id-funtions.R",
        "valipop/analysis-r/geeglm/geeglm-functions.R",
        "valipop/analysis-r/geeglm/analysis.R"
    };

    // Public Methods

    /**
     * Extracts and concats the R analysis scripts to a local file.
     * 
     * @param rScriptPath the full path to extract the scripts to
     * 
     * @return the parameter {@code rScriptPath}
     */
    public static Path extractRScript(final Path rScriptPath) throws IOException {

        // The file the R is written to
        final File rScriptFile = new File(rScriptPath.toString());

        // This overwrites any existing file of the same name
        final FileWriter rScriptFileWriter = new FileWriter(rScriptFile, false);
        rScriptFileWriter.close();

        for (final String script : R_SCRIPT_PATHS) {
            try (
                // Retrieving the R files as streams in case they are in a jar
                final InputStream stream = RCaller.class.getClassLoader().getResourceAsStream(script);
                final OutputStream output = new FileOutputStream(rScriptFile, true)
            ) {
                IOUtils.copy(stream, output);
            }
        }

        return rScriptPath;
    }

    /**
     * Executes the R analysis script and returns the running process. The process must be destroyed separately.
     * 
     * @param runDirPath the path of the current run directory
     * @param rScriptPath the path of the R analysis script
     * @param maxBirthingAge the maximum birthing age of the population model
     * 
     * @return the executing process
     */
    public static Process runRScript(final Path runDirPath, final Path rScriptPath, final int maxBirthingAge) throws IOException {

        final String[] params = {runDirPath.toAbsolutePath().toString(), String.valueOf(maxBirthingAge)};
        final String[] commands = joinArrays(new String[]{ "Rscript", rScriptPath.toString()}, params);

        return new ProcessBuilder(commands).start();
    }

    /**
     * Outputs the standard otuput and error of the R analysis to {@code outputPath} and returns the calculated v value.
     * 
     * @param process the executing R analysis process
     * @param outputPath the path of the process output and error streams
     */
    public static double getRScriptResult(final Process process, final Path outputPath) throws IOException {

        // Extracting stdout and stderr
        final BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
        final BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        // The file the output of the R script is written to
        final File outputFile = new File(outputPath.toString());
        final FileWriter outputFileWriter = new FileWriter(outputFile, false);
        outputFile.createNewFile();

        // Filter relevant lines, calculate v per line and sum together
        final int v = stdout.lines()
            // Writing lines to file
            .peek((l) -> {
                try {
                    outputFileWriter.write(l);
                    outputFileWriter.append("\n");
                } catch (final IOException e) {
                    System.err.println("Unable to write results of analysis to file " + outputPath);
                }
            })
            .filter(RCaller::filterAnalysis)
            .map(RCaller::countV)
            .reduce(Double::sum)
            .map((res) -> (int) Math.floor(res))
            .orElse(0);

        // Print out any errors
        stderr.lines().forEach(System.err::println);

        // Clean up
        stdout.close();
        stderr.close();
        outputFileWriter.close();

        return v;
    }

    /**
     * Runs the R analysis on the population model and returns the analysis result
     * 
     * @param runDirPath the path of the run directory 
     * @param maxBirthingAge the maximum birthing age of the population model
     */
    public static double getGeeglmV(final Path runDirPath, final int maxBirthingAge) throws IOException {

        final Path rScriptPath = extractRScript(runDirPath.resolve(R_SCRIPT_LOCATION));
        final Process process = runRScript(runDirPath, rScriptPath, maxBirthingAge);
        final double v = getRScriptResult(process, runDirPath.resolve(R_SCRIPT_OUTPUT_LOCATION));

        process.destroy();

        return v;
    }

    // Private methods

    private static boolean filterAnalysis(final String line) {
        // Only STATS interactions are significant;
        return line.contains("STAT");
    }

    private static double countV(final String line) {
        final int MAX_STARS = 3;
        final double[] STAR_VALUES = new double[]{ 2, 3, 4 };

        // Scan for sequences stars
        // Start from max star count to prevent lower star counts from identifying first
        final int[] starCounts = new int[MAX_STARS];
        for (int starNumber = MAX_STARS; starNumber > 0; starNumber--) {
            starCounts[starNumber - 1] = 0;

            if (line.contains("*".repeat(starNumber) + " ".repeat(MAX_STARS - starNumber))) {
                starCounts[starNumber - 1]++;
                break;
            }
        }

        // Clever way to count dots in line
        final double dotCount = (line.length() - line.replace(".  ", "").length()) / 3;
        double value = dotCount / 3;
        for (int i = 0; i < MAX_STARS; i++) {
            value += starCounts[i] * STAR_VALUES[i];
        }

        return value;
    }

    private static String[] joinArrays(final String[] first, final String[] second) {
        final List<String> both = new ArrayList<String>(first.length + second.length);
        Collections.addAll(both, first);
        Collections.addAll(both, second);
        return both.toArray(new String[0]);
    }
}