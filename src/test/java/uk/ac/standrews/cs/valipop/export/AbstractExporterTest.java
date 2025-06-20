/*
 * Copyright 2014 Digitising Scotland project:
 * <http://digitisingscotland.cs.st-andrews.ac.uk/>
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
package uk.ac.standrews.cs.valipop.export;

import org.junit.After;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.ac.standrews.cs.valipop.Config;
import uk.ac.standrews.cs.valipop.implementations.OBDModel;
import uk.ac.standrews.cs.valipop.simulationEntities.IPersonCollection;

import static uk.ac.standrews.cs.utilities.FileManipulation.FILE_CHARSET;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 */
@RunWith(Parameterized.class)
public abstract class AbstractExporterTest {

    static final String TEST_DIRECTORY_PATH_STRING = "src/test/resources/valipop/";

    static final int[] TEST_CASE_POPULATION_SIZES = {200, 350, 500};
    static final int SEED = 841584;
    static final String[] TEST_CASE_FILE_NAME_ROOTS = new String[TEST_CASE_POPULATION_SIZES.length];

    static {
        for (int i = 0; i < TEST_CASE_FILE_NAME_ROOTS.length; i++) {
            TEST_CASE_FILE_NAME_ROOTS[i] = makeFileNameRoot(TEST_CASE_POPULATION_SIZES[i]);
        }
    }

    protected final IPersonCollection population;
    final String file_name_root;

    Path generated_output1 = null;
    Path generated_output2 = null;
    Path expected_output = null;

    AbstractExporterTest(final IPersonCollection population, final String file_name_root) {

        this.population = population;
        this.file_name_root = file_name_root;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> generateConfigurations() throws Exception {

        final Object[][] configurations = new Object[TEST_CASE_POPULATION_SIZES.length][];
        for (int i = 0; i < configurations.length; i++) {
            configurations[i] = makeTestConfiguration(TEST_CASE_POPULATION_SIZES[i], TEST_CASE_FILE_NAME_ROOTS[i]);
        }
        return Arrays.asList(configurations);
    }

    @After
    public void tearDown() throws IOException {

        Files.delete(generated_output1);
        Files.delete(generated_output2);
    }

    private static Object[] makeTestConfiguration(final int population_size, final String file_name_root) {

        String purpose = "graph-test";

        Config config = new Config(
                LocalDate.of(1599, 1, 1),
                LocalDate.of(1855, 1, 1),
                LocalDate.of(2015, 1, 1),
                population_size,
                Paths.get("src/test/resources/valipop/test-pop"),
                Config.DEFAULT_RESULTS_SAVE_PATH,
                purpose,
                Config.DEFAULT_RESULTS_SAVE_PATH);

        config.setDeterministic(true).setSeed(SEED);

        OBDModel sim = new OBDModel(config);
        sim.runSimulation();

        final IPersonCollection population = sim.getPopulation().getPeople();
        population.setDescription(String.valueOf(population_size));

        return new Object[]{population, file_name_root};
    }

    private static String makeFileNameRoot(final int population_size) {

        return "file" + population_size + "_intended";
    }

    protected static void assertThatFilesHaveSameContent(final Path path1, final Path path2) throws IOException {

        try (BufferedReader reader1 = Files.newBufferedReader(path1, FILE_CHARSET); BufferedReader reader2 = Files.newBufferedReader(path2, FILE_CHARSET)) {

            String line1;

            while ((line1 = reader1.readLine()) != null) {
                String line2 = reader2.readLine();
                assertEquals(line1, line2);
            }

            assertNull(reader2.readLine());
        }
    }
}
