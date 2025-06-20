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

import uk.ac.standrews.cs.valipop.simulationEntities.IPersonCollection;
import uk.ac.standrews.cs.valipop.export.graphviz.GraphvizPopulationWriter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Generates test cases for graphviz export.
 *
 * @author Daniel Brathagen (dbrathagen@gmail.com)
 */
public class GraphvizTestCaseRecorder extends AbstractTestCaseRecorder {

    // The generated DOT files can be checked for validity at: https://magjac.com/graphviz-visual-editor/

    public static void main(final String[] args) throws Exception {

        new GraphvizTestCaseRecorder().recordTestCase();
    }

    @Override
    protected IPopulationWriter getPopulationWriter(final Path path, final IPersonCollection population) throws IOException {

        return new GraphvizPopulationWriter(population, path);
    }

    @Override
    protected String getIntendedOutputFileSuffix() {

        return ".dot";
    }

    @Override
    protected String getDirectoryName() {
        return "graphviz";
    }
}
