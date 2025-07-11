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

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.FieldSource;
import uk.ac.standrews.cs.valipop.Config;
import uk.ac.standrews.cs.valipop.implementations.OBDModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * E2E tests which compares ValiPop generated record checksums 
 *
 * @author Daniel Brathagen (dbrathagen@gmail.com)
 */
public class RecordExportTest {

    private static final Path TEST_RESOURCE_DIR = Path.of("src/test/resources/valipop/config/simulation");
    private static final String RECORD_DIR = "records";

    private static final List<Arguments> configurations = List.of(
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-1.txt"), "BR8JFQzWW6NfPSMekgpqHA==", "q12spKlZuMkBtxwSaRqvdQ==", "lbpktE0QdRQnJxo+2mq8Uw==", 80440, 80440, 21394),
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-2.txt"), "3eAZn6WNGUVYso7HLoLPJQ==", "mxi+9JHBR4u09qj9y4NgnQ==", "aDG2Txpg5RqPnPxmScQ/zg==", 71125, 71125, 18253 ),
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-4.txt"), "Neom4z1Q/sAS40kOIPu/Gg==", "G3CcsZHSDZGQBgWHTn6nyw==", "mnBe+SrHwp/zOQn5obDqyQ==", 150842, 150842, 21803 )
    );

    private static final List<Arguments> slowConfigurations = List.of(
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-3.txt"), "yLtjxyKoLtZFzdv1nIgBMw==", "RDX7qoB+uHXxP6xbA/Segw==", "HoFomUFcKVIPhD0cftRgCQ==", 999935, 999935, 314724)
    );

    @ParameterizedTest
    @FieldSource("configurations")
    public void recordsGeneratedAsExpected(final Path configPath, final String expectedBirthHash, final String expectedDeathHash, final String expectedMarriageHash,
                                           final int expectedBirthRecordCount, final int expectedDeathRecordCount, final int expectedMarriageRecordCount) throws IOException, NoSuchAlgorithmException {

        runTest(configPath, expectedBirthHash, expectedDeathHash, expectedMarriageHash, expectedBirthRecordCount,expectedDeathRecordCount, expectedMarriageRecordCount);
    }

    @ParameterizedTest
    @FieldSource("slowConfigurations")
    @Tag("slow")
    public void recordsGeneratedAsExpectedSlow(final Path configPath, final String expectedBirthHash, final String expectedDeathHash, final String expectedMarriageHash,
                                               final int expectedBirthRecordCount, final int expectedDeathRecordCount, final int expectedMarriageRecordCount) throws IOException, NoSuchAlgorithmException {

        runTest(configPath, expectedBirthHash, expectedDeathHash, expectedMarriageHash, expectedBirthRecordCount,expectedDeathRecordCount, expectedMarriageRecordCount);
    }

    private static void runTest(final Path configPath, final String expectedBirthHash, final String expectedDeathHash, final String expectedMarriageHash,
                                final int expectedBirthRecordCount, final int expectedDeathRecordCount, final int expectedMarriageRecordCount) throws IOException, NoSuchAlgorithmException {

        final Config config = new Config(configPath);
        final OBDModel model = new OBDModel(config);
        model.runSimulation();

        model.analyseAndOutputPopulation(true);

        check(config,"birth_records.csv", expectedBirthHash, expectedBirthRecordCount);
        check(config,"death_records.csv", expectedDeathHash, expectedDeathRecordCount);
        check(config,"marriage_records.csv", expectedMarriageHash, expectedMarriageRecordCount);
    }

    private static void check(final Config config, final String fileName, final String expectedHash, final int expectedRecordCount) throws IOException, NoSuchAlgorithmException {

        final Path recordPath = config.getRunPath().resolve(RECORD_DIR).resolve(fileName);

        final List<String> lines = Files.readAllLines(recordPath);
        assertEquals(expectedRecordCount, lines.size());

        final byte[] bytes = (String.join("\n", lines) + "\n").getBytes();
        final String actualHash = Base64.getEncoder().encodeToString(MessageDigest.getInstance("MD5").digest(bytes));

        assertEquals(expectedHash, actualHash, "Checking records from " + fileName);
    }
}
