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
import org.junit.jupiter.api.Test;
import uk.ac.standrews.cs.valipop.export.graphviz.GraphvizPopulationWriter;
import uk.ac.standrews.cs.valipop.simulationEntities.IPersonCollection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * E2£ tests of Graphviz export.
 *
 * @author Daniel Brathagen (dbrathagen@gmail.com)
 */
public abstract class GraphvizTest extends PopulationExportTest {

    // Files can be checked for validity at: https://magjac.com/graphviz-visual-editor/

    static final String INTENDED_SUFFIX = ".dot";

    @BeforeEach
    public void setup() throws IOException {

        generated_output_file1 = Files.createTempFile(temp_dir, null, INTENDED_SUFFIX);
        expected_output_file = Paths.get(TEST_DIRECTORY_PATH_STRING, "graphviz", file_name_root + INTENDED_SUFFIX);
    }

    public GraphvizTest(final IPersonCollection population) {

        super(population);
    }

    @Test
    public void graphvizExportIsAsExpected() throws Exception {

        final IPopulationWriter population_writer = new GraphvizPopulationWriter(population, generated_output_file1);

        try (final PopulationConverter converter = new PopulationConverter(population, population_writer)) {
            converter.convert();
        }

        assertThatFilesHaveSameContent(generated_output_file1, expected_output_file);
    }
}
