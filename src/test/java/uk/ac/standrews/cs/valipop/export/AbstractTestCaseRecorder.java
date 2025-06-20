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
package uk.ac.standrews.cs.valipop.export;

import uk.ac.standrews.cs.valipop.Config;
import uk.ac.standrews.cs.valipop.implementations.OBDModel;
import uk.ac.standrews.cs.valipop.simulationEntities.IPersonCollection;
import uk.ac.standrews.cs.valipop.statistics.distributions.InconsistentWeightException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

public abstract class AbstractTestCaseRecorder {

    protected void recordTestCase() throws Exception {

        for (int i = 0; i < AbstractExporterTest.TEST_CASE_POPULATION_SIZES.length; i++) {

            final Path path = Paths.get(AbstractExporterTest.TEST_DIRECTORY_PATH_STRING, getDirectoryName(), AbstractExporterTest.TEST_CASE_FILE_NAME_ROOTS[i] + getIntendedOutputFileSuffix());

            Config config = new Config(
                    LocalDate.of(1599, 1, 1),
                    LocalDate.of(1855, 1, 1),
                    LocalDate.of(2015, 1, 1),
                    AbstractExporterTest.TEST_CASE_POPULATION_SIZES[i],
                    Paths.get("src/test/resources/valipop/test-pop"),
                    Paths.get("/tmp/results"),
                    "graph-test",
                    Paths.get("/tmp/results"));

            config.setDeterministic(true);

            OBDModel sim = new OBDModel(config);
            sim.runSimulation();

            final IPersonCollection abstract_population = sim.getPopulation().getPeople();
            final IPopulationWriter population_writer = getPopulationWriter(path, abstract_population);

            try (PopulationConverter converter = new PopulationConverter(abstract_population, population_writer)) {
                converter.convert();
            }
        }
    }

    protected abstract String getIntendedOutputFileSuffix();

    protected abstract String getDirectoryName();

    protected abstract IPopulationWriter getPopulationWriter(Path path, IPersonCollection population) throws IOException, InconsistentWeightException;
}
