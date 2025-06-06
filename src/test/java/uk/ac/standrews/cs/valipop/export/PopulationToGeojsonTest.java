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
package uk.ac.standrews.cs.valipop.export;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.ac.standrews.cs.valipop.export.geojson.GeojsonPopulationWriter;
import uk.ac.standrews.cs.valipop.simulationEntities.IPersonCollection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * E2E tests of Geojson export.
 *
 * @author Daniel Brathagen (dbrathagen@gmail.com)
 */
public class PopulationToGeojsonTest extends AbstractExporterTest {

    static final String INTENDED_SUFFIX = ".geojson";

    @Before
    public void setup() throws IOException {

        generated_output1 = Files.createTempFile(null, INTENDED_SUFFIX);
        expected_output = Paths.get(TEST_DIRECTORY_PATH_STRING, "geojson", file_name_root + INTENDED_SUFFIX);
    }

    public PopulationToGeojsonTest(final IPersonCollection population, final String file_name) {

        super(population, file_name);
    }

    @Test
    @Ignore
    public void GeojsonExportIsAsExpected() throws Exception {

        final IPopulationWriter population_writer = new GeojsonPopulationWriter(generated_output1);

        try (PopulationConverter converter = new PopulationConverter(population, population_writer)) {
            converter.convert();
        }

        assertThatFilesHaveSameContent(generated_output1, expected_output);
    }
}
