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
