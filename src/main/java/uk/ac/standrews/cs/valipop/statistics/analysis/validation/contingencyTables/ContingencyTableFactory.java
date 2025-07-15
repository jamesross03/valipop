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
package uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables;

import uk.ac.standrews.cs.valipop.Config;
import uk.ac.standrews.cs.valipop.implementations.MemoryUsageAnalysis;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.statistics.analysis.simulationSummaryLogging.SummaryRow;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TableInstances.*;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TableStructure.CTtable;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TableStructure.NoTableRowsException;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.CTtree;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.PopulationStatistics;
import uk.ac.standrews.cs.valipop.utils.ProgramTimer;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class ContingencyTableFactory {

    public static final Logger log = Logger.getLogger(ContingencyTableFactory.class.getName());

    public static void generateContingencyTables(final Iterable<IPerson> population, final PopulationStatistics desired,
                                                 final Config config, final SummaryRow summary)  {

        final ProgramTimer tableTimer = new ProgramTimer();

        // TODO revert back to T0?
        final CTtree fullTree = new CTtree(population, desired, config.getTS(), config.getT0(), config.getTE(), config.getCtTreeStepback(), config.getCtTreePrecision());

        MemoryUsageAnalysis.log();

        try {
            log.info("OBDModel --- Extracting and Outputting CTtables to files");

            final CTtableOB obTable = new CTtableOB(fullTree);
            outputToFile(obTable, "ob-CT.csv", config);

            final CTtableMB mbTable = new CTtableMB(fullTree);
            outputToFile(mbTable, "mb-CT.csv", config);

            final CTtablePart partTable = new CTtablePart(fullTree);
            outputToFile(partTable, "part-CT.csv", config);

            final CTtableSep sepTable = new CTtableSep(fullTree);
            outputToFile(sepTable, "sep-CT.csv", config);

            final CTtableDeath deathTable = new CTtableDeath(fullTree);
            outputToFile(deathTable, "death-CT.csv", config);

        } catch (final IOException | NoTableRowsException e) {
            throw new RuntimeException(e);
        }

        summary.setCTRunTime(tableTimer.getRunTimeSeconds());
    }

    private static void outputToFile(final CTtable table, final String fileName, final Config config) throws IOException, NoTableRowsException {

        MemoryUsageAnalysis.log();
        final Path path = config.getContingencyTablesPath().resolve( fileName);
        Config.mkBlankFile(path);
        final PrintStream ps = new PrintStream(path.toFile(), StandardCharsets.UTF_8);
        table.outputToFile(ps);
        MemoryUsageAnalysis.log();
    }
}
