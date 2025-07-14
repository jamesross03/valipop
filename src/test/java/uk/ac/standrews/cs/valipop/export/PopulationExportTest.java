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

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.provider.Arguments;
import uk.ac.standrews.cs.valipop.simulationEntities.IPersonCollection;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.ac.standrews.cs.valipop.config.TestCases.getTestConfigurations;

/**
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 */
public abstract class PopulationExportTest {

    // Eventual population sizes, used in names of expected files: 113, 188.
    public static final List<Integer> FAST_TEST_CASE_INITIAL_POPULATION_SIZES = List.of(200, 300);

    // Eventual population sizes, used in names of expected files: 19065, 86033.
    public static final List<Integer> SLOW_TEST_CASE_INITIAL_POPULATION_SIZES = List.of(1000, 5000);

    public static final String TEST_DIRECTORY_PATH_STRING = "src/test/resources/valipop/";

    protected final IPersonCollection population;
    final String file_name_root;

    @TempDir Path temp_dir;
    Path generated_output_file1;
    Path generated_output_file2;
    Path expected_output_file;

    PopulationExportTest(final IPersonCollection population) {

        this.population = population;
        this.file_name_root = "file" + population.getNumberOfPeople() + "_expected";
    }

    static List<Arguments> getFastTestCases()  {

        return getTestConfigurations(FAST_TEST_CASE_INITIAL_POPULATION_SIZES);
    }

    static List<Arguments> getSlowTestCases()  {

        return getTestConfigurations(SLOW_TEST_CASE_INITIAL_POPULATION_SIZES);
    }

    protected static void assertThatFilesHaveSameContent(final Path path1, final Path path2) throws IOException {

        try (final BufferedReader reader1 = Files.newBufferedReader(path1, StandardCharsets.UTF_8); final BufferedReader reader2 = Files.newBufferedReader(path2, StandardCharsets.UTF_8)) {

            String line1;

            while ((line1 = reader1.readLine()) != null) {
                final String line2 = reader2.readLine();
                assertEquals(line1, line2);
            }

            assertNull(reader2.readLine());
        }
    }
}
