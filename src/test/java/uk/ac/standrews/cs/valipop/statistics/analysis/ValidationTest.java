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
package uk.ac.standrews.cs.valipop.statistics.analysis;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.FieldSource;
import uk.ac.standrews.cs.valipop.implementations.StatsException;
import uk.ac.standrews.cs.valipop.utils.RCaller;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Daniel Brathagen (dbrathagen@gmail.com)
 */
public class ValidationTest {

    private static final Path TEST_RESOURCE_DIR = Path.of("src/test/resources/valipop/validation");

    private static List<Arguments> configurations = List.of(
        Arguments.of(TEST_RESOURCE_DIR.resolve("test1"), 0.0 ),
        Arguments.of(TEST_RESOURCE_DIR.resolve("test2"), 17.0 ),
        Arguments.of(TEST_RESOURCE_DIR.resolve("test3"), 0.0 ),
        Arguments.of(TEST_RESOURCE_DIR.resolve("test4"), 0.0 ),
        Arguments.of(TEST_RESOURCE_DIR.resolve("test5"), 0.0 ),
        Arguments.of(TEST_RESOURCE_DIR.resolve("test6"), 61.0 ),
        Arguments.of(TEST_RESOURCE_DIR.resolve("test8"), 16.0 )
    );

    private static List<Arguments> longRunningConfigurations = List.of(
        Arguments.of(TEST_RESOURCE_DIR.resolve("test7"), 0 )
    );

    // Given model results, the R program should always generate the same V value
    @ParameterizedTest
    @FieldSource("configurations")
    public void runValidation(final Path tableDirectory, final double expectedV) throws IOException, StatsException {

        final int maxBirthingAge = 55;

        final double v = RCaller.getGeeglmV(
            tableDirectory,
            maxBirthingAge
        );

        assertEquals(v, expectedV, 1e-10);
    }
}
