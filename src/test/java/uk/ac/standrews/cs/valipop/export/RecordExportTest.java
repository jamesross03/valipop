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
        Arguments.of(TEST_RESOURCE_DIR.resolve("config-1.txt"), "w+1VmnOwhHlzZGC9WA3QdQ==", "qV4bmFQBZcC43cPkS/lwHw==", "wvD8izD4+XQTxMw/z2wXZw==")
//        Arguments.of(TEST_RESOURCE_DIR.resolve("config-2.txt"), "/3mh2/Usl9NddImYMIETng==", "hL8T7cQQhei9FWLK/TkeAw==", "nWyeCUb3O/T0XP/Y4UmKuQ==" ),
//        Arguments.of(TEST_RESOURCE_DIR.resolve("config-4.txt"), "TOObtE1NLzZpWq186JDSHw==", "ntVNvj0KF+sNmcolCvn2PQ==", "8k7Whuk4cPeqtAxLNmgg+Q==" )
    );

    private static String expected_sample = "ID,family,marriage,child's forname(s),child's surname,birth day,birth month,birth year,address,sex,father's forename,father's surname,father's occupation,mother's forename,mother's maiden surname,mother's occupation,day of parents' marriage,month of parents' marriage,year of parents' marriage,place of parent's marriage,illegit,notes,Death,CHILD_IDENTITY,MOTHER_IDENTITY,FATHER_IDENTITY,DEATH_RECORD_IDENTITY,PARENT_MARRIAGE_RECORD_IDENTITY,FATHER_BIRTH_RECORD_IDENTITY,MOTHER_BIRTH_RECORD_IDENTITY,MARRIAGE_RECORD_IDENTITY1,MARRIAGE_RECORD_IDENTITY2,MARRIAGE_RECORD_IDENTITY3,MARRIAGE_RECORD_IDENTITY4,MARRIAGE_RECORD_IDENTITY5,MARRIAGE_RECORD_IDENTITY6,MARRIAGE_RECORD_IDENTITY7,MARRIAGE_RECORD_IDENTITY8,IMMIGRANT_GENERATION" +
        "123011,-1,,Irena,Moreau,16,JULY,1727,,F,,,,,,,1,JANUARY,1,,illegitimate,SYNTHETIC DATA PRODUCED USING VALIPOP,123011,123011,,,123011,,,,42084,,,,,,,,NA" +
        "125094,-1,,Javiera,Nielsen,17,SEPTEMBER,1729,,F,,,,,,,1,JANUARY,1,,,SYNTHETIC DATA PRODUCED USING VALIPOP,125094,125094,,,125094,,,,42845,,,,,,,,NA" +
        "129247,-1,,Sydney,Roșca,10,DECEMBER,1729,,F,,,,,,,1,JANUARY,1,,,SYNTHETIC DATA PRODUCED USING VALIPOP,129247,129247,,,129247,,,,44304,,,,,,,,NA" +
        "125296,-1,,Emily,Martinez,10,JUNE,1732,,F,,,,,,,1,JANUARY,1,,,SYNTHETIC DATA PRODUCED USING VALIPOP,125296,125296,,,125296,,,,42910,,,,,,,,NA" +
        "123004,-1,,Paige,Delos Santos,15,APRIL,1733,,F,,,,,,,1,JANUARY,1,,,SYNTHETIC DATA PRODUCED USING VALIPOP,123004,123004,,,123004,,,,42082,,,,,,,,NA" +
        "123218,-1,,Anna,Smit,26,FEBRUARY,1733,,F,,,,,,,1,JANUARY,1,,illegitimate,SYNTHETIC DATA PRODUCED USING VALIPOP,123218,123218,,,123218,,,,42152,,,,,,,,NA" +
        "123793,-1,,Nikola,Krasniqi,24,MARCH,1733,,F,,,,,,,1,JANUARY,1,,,SYNTHETIC DATA PRODUCED USING VALIPOP,123793,123793,,,123793,,,,42366,,,,,,,,NA" +
        "128374,-1,,Maria,Farkas,19,FEBRUARY,1733,,F,,,,,,,1,JANUARY,1,,,SYNTHETIC DATA PRODUCED USING VALIPOP,128374,128374,,,128374,,,,43980,,,,,,,,NA" +
        "121427,-1,,Julie,Walsh,14,JULY,1734,,F,,,,,,,1,JANUARY,1,,,SYNTHETIC DATA PRODUCED USING VALIPOP,121427,121427,,,121427,,,,41510,,,,,,,,NA";

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

        Randomness.do_debug = true;
        final Config config = new Config(configPath);
        final OBDModel model = new OBDModel(config);
        model.runSimulation();


        PeopleCollection people = model.getPopulation().getPeople();
        System.out.println("Number of people: " + people.getNumberOfPeople());
        System.out.println("Number of partnerships: " + people.getNumberOfPartnerships());
        Collection<IPerson> people1 = people.getPeople();

        int debug_count = 0;
        for (IPerson p : people1) {
            if (debug_count++ < 20)
                System.out.println(p);
            else break;
        }

        model.analyseAndOutputPopulation(true);

        final Path recordPath = config.getRunPath().resolve(RECORD_DIR).resolve("birth_records.csv");
        final List<String> lines = Files.readAllLines(recordPath);

        String sample = "";
        for (int i = 0; i < 10; i++) {
            sample += lines.get(i);
        }

//        final byte[] bytes = Files.readAllBytes(recordPath);
//
//        final String actualHash = Base64.getEncoder().encodeToString(MessageDigest.getInstance("MD5").digest(bytes));

        assertEquals(expected_sample, sample, "Checking records from " + "birth_records.csv");

        //        checkHash(config,"death_records.csv", expectedDeathHash);
//        checkHash(config,"marriage_records.csv", expectedMarriageHash);
    }

    private static void checkHash(final Config config, final String fileName, final String expectedHash) throws IOException, NoSuchAlgorithmException {

        final Path recordPath = config.getRunPath().resolve(RECORD_DIR).resolve(fileName);
        final byte[] bytes = Files.readAllBytes(recordPath);

        final String actualHash = Base64.getEncoder().encodeToString(MessageDigest.getInstance("MD5").digest(bytes));

        assertEquals(expectedHash, actualHash, "Checking records from " + fileName);
    }
}
