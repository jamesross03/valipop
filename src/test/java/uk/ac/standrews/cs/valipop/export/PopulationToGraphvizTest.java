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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import uk.ac.standrews.cs.valipop.export.graphviz.GraphvizPopulationWriter;
import uk.ac.standrews.cs.valipop.simulationEntities.IPersonCollection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * E2£ tests of Graphviz export.
 *
 * @author Daniel Brathagen (dbrathagen@gmail.com)
 */
public class PopulationToGraphvizTest extends AbstractExporterTest {

    static final String INTENDED_SUFFIX = ".dot";

    @BeforeEach
    public void setup() throws IOException {

        generated_output1 = Files.createTempFile(null, INTENDED_SUFFIX);
        expected_output = Paths.get(TEST_DIRECTORY_PATH_STRING, "graphviz", file_name_root + INTENDED_SUFFIX);
    }

    public PopulationToGraphvizTest(final IPersonCollection population, final String file_name) {

        super(population, file_name);
    }

    @Test
    @Disabled
    public void graphvizExportIsAsExpected() throws Exception {

        final IPopulationWriter population_writer = new GraphvizPopulationWriter(population, generated_output1);

        try (PopulationConverter converter = new PopulationConverter(population, population_writer)) {
            converter.convert();
        }

        assertThatFilesHaveSameContent(generated_output1, expected_output);
    }
}

