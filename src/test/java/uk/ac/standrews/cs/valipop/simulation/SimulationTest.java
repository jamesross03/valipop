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
package uk.ac.standrews.cs.valipop.simulation;

import org.junit.jupiter.api.Disabled;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * E2E tests which compares ValiPop generated record checksums 
 *
 * @author Daniel Brathagen (dbrathagen@gmail.com)
 */
public class SimulationTest {

    private static final Path TEST_RESOURCE_DIR = Path.of("src/test/resources/valipop/config/simulation");
    private static final String RECORD_DIR = "records";

    private static final List<String> RECORD_NAMES = List.of(
        "birth_records.csv",
        "death_records.csv",
        "marriage_records.csv"
    );

    private static List<Arguments> configurations = List.of(
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-1.txt"), "+wEc08pgIweIC4advvfcIw==", "DWj48GAsFtJS/PX4c+59dA==", "D0jUpvTWOpCVGYPRbc4dQw=="),
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-2.txt"), "mEkjBgtyHFMqTZz02r9H4w==", "fhE/MQV8AkzKMtaLcA7rQA==", "jjcDTaoYW4OizssyCtRnTA==" ),
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-3.txt"), "VJw8XWYYWzyn3OCs5/wcgA==", "3nKYMFhMxJ1rLhfNYEv/zA==", "QKw8S3qci40qr/ofUNvoJg==" ),
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-4.txt"), "FdGj7mPJh9jBk9LGZuhGCA==", "9wdMQtsoGhVGTDH/T49QpQ==", "FxXNt+HHl+xek5Qw6kozsA==" )
    );

    @ParameterizedTest
    @FieldSource("configurations")
    @Disabled
    public void testGeneratedRecordsAsExpected(Path configPath, String expectedBirthHash, String expectedDeathHash, String expectedMarriageHash) throws IOException, NoSuchAlgorithmException {

        Config config = new Config(configPath);
        OBDModel model = new OBDModel(config);
        model.runSimulation();
        model.analyseAndOutputPopulation(true, 5);

        // Calculate MD5 hash of output records and compare
        for (String record : RECORD_NAMES) {
            Path recordPath = config.getRunPath().resolve(RECORD_DIR).resolve(record);
            byte[] bytes = Files.readAllBytes(recordPath);

            String actualHash = Base64.getEncoder().encodeToString(MessageDigest.getInstance("MD5").digest(bytes));

            switch (record) {
                case "birth_records.csv":
                    assertEquals("Comparing " + record + " of " + configPath.getFileName(), expectedBirthHash, actualHash);
                    break;
                case "death_records.csv":
                    assertEquals("Comparing " + record + " of " + configPath.getFileName(), expectedDeathHash, actualHash);
                    break;
                case "marriage_records.csv":
                    assertEquals("Comparing " + record + " of " + configPath.getFileName(), expectedMarriageHash, actualHash);
                    break;
            }
        }
    }
}
