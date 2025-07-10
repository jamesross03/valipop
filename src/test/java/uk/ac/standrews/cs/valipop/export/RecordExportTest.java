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
import uk.ac.standrews.cs.valipop.implementations.Randomness;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.simulationEntities.dataStructure.PeopleCollection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collection;
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
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-1.txt"), "BR8JFQzWW6NfPSMekgpqHA==", "q12spKlZuMkBtxwSaRqvdQ==", "ME5IfxvKtNL4if5btvHvig=="),
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-2.txt"), "3eAZn6WNGUVYso7HLoLPJQ==", "mxi+9JHBR4u09qj9y4NgnQ==", "CGrAAlaT7PGb2ZR4TB/UAg==" ),
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-4.txt"), "Neom4z1Q/sAS40kOIPu/Gg==", "G3CcsZHSDZGQBgWHTn6nyw==", "2JK1oQdoCBr9VJ48czEZTQ==" )
    );

    private static String expected_sample = "ID,family,marriage,child's forname(s),child's surname,birth day,birth month,birth year,address,sex,father's forename,father's surname,father's occupation,mother's forename,mother's maiden surname,mother's occupation,day of parents' marriage,month of parents' marriage,year of parents' marriage,place of parent's marriage,illegit,notes,Death,CHILD_IDENTITY,MOTHER_IDENTITY,FATHER_IDENTITY,DEATH_RECORD_IDENTITY,PARENT_MARRIAGE_RECORD_IDENTITY,FATHER_BIRTH_RECORD_IDENTITY,MOTHER_BIRTH_RECORD_IDENTITY,MARRIAGE_RECORD_IDENTITY1,MARRIAGE_RECORD_IDENTITY2,MARRIAGE_RECORD_IDENTITY3,MARRIAGE_RECORD_IDENTITY4,MARRIAGE_RECORD_IDENTITY5,MARRIAGE_RECORD_IDENTITY6,MARRIAGE_RECORD_IDENTITY7,MARRIAGE_RECORD_IDENTITY8,IMMIGRANT_GENERATION122814,-1,,Sarah,Kapanadze,9,DECEMBER,1726,,F,,,,,,,1,JANUARY,1,,,SYNTHETIC DATA PRODUCED USING VALIPOP,122814,122814,,,122814,,,,,,,,,,,,NA123024,-1,,Michelle,Tsiklauri,27,AUGUST,1729,,F,,,,,,,1,JANUARY,1,,,SYNTHETIC DATA PRODUCED USING VALIPOP,123024,123024,,,123024,,,,41653,,,,,,,,NA139001,-1,,Anouk,Tjin,10,MARCH,1732,,F,,,,,,,1,JANUARY,1,,,SYNTHETIC DATA PRODUCED USING VALIPOP,139001,139001,,,139001,,,,47305,,,,,,,,NA134733,-1,,Ida,De Jong,4,NOVEMBER,1733,,F,,,,,,,1,JANUARY,1,,,SYNTHETIC DATA PRODUCED USING VALIPOP,134733,134733,,,134733,,,,45815,,,,,,,,NA123832,-1,,Catalina,Schmit,16,AUGUST,1734,,F,,,,,,,1,JANUARY,1,,illegitimate,SYNTHETIC DATA PRODUCED USING VALIPOP,123832,123832,,,123832,,,,41948,,,,,,,,NA123497,-1,,Emma,Jacobs,9,OCTOBER,1735,,F,,,,,,,1,JANUARY,1,,,SYNTHETIC DATA PRODUCED USING VALIPOP,123497,123497,,,123497,,,,,,,,,,,,NA126981,-1,,Catarina,Nieminen,3,NOVEMBER,1735,,F,,,,,,,1,JANUARY,1,,illegitimate,SYNTHETIC DATA PRODUCED USING VALIPOP,126981,126981,,,126981,,,,,,,,,,,,NA125051,-1,,Maria,Shevchenko,4,DECEMBER,1738,,F,,,,,,,1,JANUARY,1,,,SYNTHETIC DATA PRODUCED USING VALIPOP,125051,125051,,,125051,,,,,,,,,,,,NA133845,-1,,Maja,Lewandowski,31,JULY,1739,,F,,,,,,,1,JANUARY,1,,,SYNTHETIC DATA PRODUCED USING VALIPOP,133845,133845,,,133845,,,,,,,,,,,,NA";



    private static final List<Arguments> slowConfigurations = List.of(
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-3.txt"), "yLtjxyKoLtZFzdv1nIgBMw==", "RDX7qoB+uHXxP6xbA/Segw==", "tvf2shIRvxoZHOW6fwQMaw==" )
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

        Randomness.do_debug = false;
        Randomness.call_count = 0;
        final Config config = new Config(configPath);
        final OBDModel model = new OBDModel(config);
        model.runSimulation();

        model.analyseAndOutputPopulation(true);

        final Path recordPath = config.getRunPath().resolve(RECORD_DIR).resolve("birth_records.csv");
//        final List<String> lines = Files.readAllLines(recordPath);
//
//        String sample = "";
//        for (int i = 0; i < 10; i++) {
//            sample += lines.get(i);
//        }

////        final byte[] bytes = Files.readAllBytes(recordPath);
//        final byte[] bytes = new String(Files.readAllBytes(recordPath)).replaceAll("\\r\\n", "\n").getBytes();
//
//        final String actualHash = Base64.getEncoder().encodeToString(MessageDigest.getInstance("MD5").digest(bytes));

//        assertEquals(expected_sample, sample, "Checking records from " + "birth_records.csv");

        checkHash(config,"birth_records.csv", expectedBirthHash);
        checkHash(config,"death_records.csv", expectedDeathHash);
        checkHash(config,"marriage_records.csv", expectedMarriageHash);
    }

    private static void checkHash(final Config config, final String fileName, final String expectedHash) throws IOException, NoSuchAlgorithmException {

        final Path recordPath = config.getRunPath().resolve(RECORD_DIR).resolve(fileName);
//        final byte[] bytes = Files.readAllBytes(recordPath);
        final byte[] bytes = new String(Files.readAllBytes(recordPath)).replaceAll("\\r\\n", "\n").getBytes();

        final String actualHash = Base64.getEncoder().encodeToString(MessageDigest.getInstance("MD5").digest(bytes));

        assertEquals(expectedHash, actualHash, "Checking records from " + fileName);
    }
}
