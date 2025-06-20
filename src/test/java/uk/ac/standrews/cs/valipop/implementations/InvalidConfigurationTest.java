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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.FieldSource;
import uk.ac.standrews.cs.valipop.Config;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests various erroneous configs to check they are handled correctly
 *
 * @author Daniel Brathagen (dbrathagen@gmail.com)
 */
public class InvalidConfigurationTest {

    private static final Path TEST_RESOURCE_DIR = Path.of("src/test/resources/valipop/config/error");

    private static List<Arguments> configurations = List.of(
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-1.txt"), "t0" ),
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-2.txt"), "tE" ),
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-3.txt"), "tS" ),
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-4.txt"), "t0_pop_size" ),
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-5.txt"), "t0_pop_size" ),
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-6.txt"), "simulation_time_step" ),
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-7.txt"), "recovery_factor" ),
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-8.txt"), "over_sized_geography_factor" ),
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-9.txt"), "output_record_format" ),
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-10.txt"), "output_graph_format" ),
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-11.txt"), "tS" ),
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-12.txt"), "t0" ),
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-13.txt"), "tE"),
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-14.txt"), "t0_pop_size" ),
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-15.txt"), "var_data_files" ),
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-16.txt"), "Illegal line" ));

    @ParameterizedTest
    @FieldSource("configurations")
    public void t(Path configPath, String errorOption) {

        final Exception e = assertThrows(IllegalArgumentException.class, () -> new Config(configPath));

        assertTrue(e.getMessage().contains(errorOption));
    }
}
