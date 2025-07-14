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
package uk.ac.standrews.cs.valipop.implementations;

import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.ac.standrews.cs.valipop.export.gedcom.GEDCOMPopulationAdapter;
import uk.ac.standrews.cs.valipop.simulationEntities.IPersonCollection;

import java.nio.file.Path;
import java.util.List;

import static uk.ac.standrews.cs.valipop.export.PopulationExportTest.TEST_DIRECTORY_PATH_STRING;

/**
 * Tests of properties of abstract population interface that should hold for all populations.
 *
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 */
@ParameterizedClass
@MethodSource("getGEDCOMTestCases")
public class PopulationStructureImportedGEDCOMTest extends PopulationStructureTest {

    // Resources also include cardinal.ged, which requires more complex GEDCOM processing
    // that isn't done by ValiPop.
    public static final List<String> GEDCOM_TEST_CASES = List.of("kennedy.ged");

    PopulationStructureImportedGEDCOMTest(final IPersonCollection population) {

        super(population);
    }

    static List<Arguments> getGEDCOMTestCases()  {

        return GEDCOM_TEST_CASES.stream().
            map(testCaseFileName -> Arguments.of(loadPopulation(testCaseFileName))).
            toList();
    }

    private static IPersonCollection loadPopulation(final String testCaseFileName) {

        try {
            final Path gedcom_file = Path.of(TEST_DIRECTORY_PATH_STRING, "gedcom", testCaseFileName);

            return new GEDCOMPopulationAdapter(gedcom_file);
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
