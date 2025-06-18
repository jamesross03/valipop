package uk.ac.standrews.cs.valipop.statistics.analysis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import uk.ac.standrews.cs.valipop.implementations.StatsException;
import uk.ac.standrews.cs.valipop.utils.RCaller;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * @author Daniel Brathagen (dbrathagen@gmail.com)
 */
@RunWith(Parameterized.class)
public class ValidationTest {

    private final Path tableDirectory;
    private final double expectedV;

    private static final Path TEST_RESOURCE_DIR = Path.of("src/test/resources/valipop/validation");

    public ValidationTest(final Path tableDirectory, final double expectedV) {

        this.tableDirectory = tableDirectory;
        this.expectedV = expectedV;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> tables() {
        return Arrays.asList(new Object[][] {
            { TEST_RESOURCE_DIR.resolve("test1"), 0.0 },
            { TEST_RESOURCE_DIR.resolve("test2"), 17.0 },
            { TEST_RESOURCE_DIR.resolve("test3"), 0.0 },
            { TEST_RESOURCE_DIR.resolve("test4"), 0.0 },
            { TEST_RESOURCE_DIR.resolve("test5"), 0.0 },
            { TEST_RESOURCE_DIR.resolve("test6"), 61.0 },
            { TEST_RESOURCE_DIR.resolve("test8"), 16.0 },
        });
    }

    public static Collection<Object[]> longRunningTestData() {
        return Arrays.asList(new Object[][] {
            { TEST_RESOURCE_DIR.resolve("test7"), 0 },
        });
    }

    // Given model results, the R program should always generate the same V value
    @Test
    public void runValidation() throws IOException, StatsException {

        final int maxBirthingAge = 55;

        final double v = RCaller.getGeeglmV(
            tableDirectory,
            maxBirthingAge
        );

        assertEquals(v, expectedV, 1e-10);
    }
}
