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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * E2E tests which compares ValiPop generated record checksums 
 *
 * @author Daniel Brathagen (dbrathagen@gmail.com)
 */
public class RecordExportTest {

    private static final Path TEST_RESOURCE_DIR = Path.of("src/test/resources/valipop/config/simulation");
    private static final String RECORD_DIR = "records";

    private static final List<Arguments> configurations = List.of(
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-1.txt"), "w+1VmnOwhHlzZGC9WA3QdQ==", "qV4bmFQBZcC43cPkS/lwHw==", "wvD8izD4+XQTxMw/z2wXZw=="),
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-2.txt"), "/3mh2/Usl9NddImYMIETng==", "hL8T7cQQhei9FWLK/TkeAw==", "nWyeCUb3O/T0XP/Y4UmKuQ==" ),
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-4.txt"), "TOObtE1NLzZpWq186JDSHw==", "ntVNvj0KF+sNmcolCvn2PQ==", "8k7Whuk4cPeqtAxLNmgg+Q==" )
    );

    private static final List<Arguments> slowConfigurations = List.of(
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-3.txt"), "FUx4sNgABA3j9ZGHhox1CA==", "v/xm1+ELIHIfWzJI7YAkTA==", "glN6fSAyJ3saiCIGnrMN+g==" )
    );

    @ParameterizedTest
    @FieldSource("configurations")
    public void recordsGeneratedAsExpected(final Path configPath, final String expectedBirthHash, final String expectedDeathHash, final String expectedMarriageHash) throws IOException, NoSuchAlgorithmException {

        runTest(configPath, expectedBirthHash, expectedDeathHash, expectedMarriageHash);
    }

    @ParameterizedTest
    @FieldSource("slowConfigurations")
    @Tag("slow")
    public void recordsGeneratedAsExpectedSlow(final Path configPath, final String expectedBirthHash, final String expectedDeathHash, final String expectedMarriageHash) throws IOException, NoSuchAlgorithmException {

        runTest(configPath, expectedBirthHash, expectedDeathHash, expectedMarriageHash);
    }

    private static void runTest(final Path configPath, final String expectedBirthHash, final String expectedDeathHash, final String expectedMarriageHash) throws IOException, NoSuchAlgorithmException {

        final Config config = new Config(configPath);
        final OBDModel model = new OBDModel(config);
        model.runSimulation();
        model.analyseAndOutputPopulation(true);

        checkHash(config,"birth_records.csv", expectedBirthHash);
        checkHash(config,"death_records.csv", expectedDeathHash);
        checkHash(config,"marriage_records.csv", expectedMarriageHash);
    }

    private static void checkHash(final Config config, final String fileName, final String expectedHash) throws IOException, NoSuchAlgorithmException {

        final Path recordPath = config.getRunPath().resolve(RECORD_DIR).resolve(fileName);
        final byte[] bytes = Files.readAllBytes(recordPath);

        final String actualHash = Base64.getEncoder().encodeToString(MessageDigest.getInstance("MD5").digest(bytes));

        assertEquals(expectedHash, actualHash, "Checking records from " + fileName);
    }
}
