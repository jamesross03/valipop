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
package uk.ac.standrews.cs.valipop.implementations;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import uk.ac.standrews.cs.valipop.Config;

/**
 * Tests various erroneous configs to check they are handled correctly
 *
 * @author Daniel Brathagen (dbrathagen@gmail.com)
 */
@RunWith(Parameterized.class)
public class InvalidConfigurationTest {

    private final Path configPath;
    private final String errorOption;

    private static final Path TEST_RESOURCE_DIR = Path.of("src/test/resources/valipop/config/error");

    public InvalidConfigurationTest(Path configPath, String errorOption) {
        this.configPath = configPath;
        this.errorOption = errorOption;
    }
    
    @Parameterized.Parameters
    public static Collection<Object[]> getTestCases() {
        return Arrays.asList(
            new Object[] { TEST_RESOURCE_DIR.resolve("config-1.txt"), "t0" },
            new Object[] { TEST_RESOURCE_DIR.resolve("config-2.txt"), "tE" },
            new Object[] { TEST_RESOURCE_DIR.resolve("config-3.txt"), "tS" },
            new Object[] { TEST_RESOURCE_DIR.resolve("config-4.txt"), "t0_pop_size" },
            new Object[] { TEST_RESOURCE_DIR.resolve("config-5.txt"), "t0_pop_size" },
            new Object[] { TEST_RESOURCE_DIR.resolve("config-6.txt"), "simulation_time_step" },
            new Object[] { TEST_RESOURCE_DIR.resolve("config-7.txt"), "recovery_factor" },
            new Object[] { TEST_RESOURCE_DIR.resolve("config-8.txt"), "over_sized_geography_factor" },
            new Object[] { TEST_RESOURCE_DIR.resolve("config-9.txt"), "output_record_format" },
            new Object[] { TEST_RESOURCE_DIR.resolve("config-10.txt"), "output_graph_format" },
            new Object[] { TEST_RESOURCE_DIR.resolve("config-11.txt"), "tS" },
            new Object[] { TEST_RESOURCE_DIR.resolve("config-12.txt"), "t0" },
            new Object[] { TEST_RESOURCE_DIR.resolve("config-13.txt"), "tE" },
            new Object[] { TEST_RESOURCE_DIR.resolve("config-14.txt"), "t0_pop_size" },
            new Object[] { TEST_RESOURCE_DIR.resolve("config-15.txt"), "var_data_files" },
            new Object[] { TEST_RESOURCE_DIR.resolve("config-16.txt"), "Illegal line" });
    } 

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void test() {

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(errorOption);
        new Config(configPath);
    }
}
