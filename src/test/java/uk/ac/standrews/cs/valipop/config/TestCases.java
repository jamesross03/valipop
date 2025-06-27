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
package uk.ac.standrews.cs.valipop.config;

import org.junit.jupiter.params.provider.Arguments;
import uk.ac.standrews.cs.valipop.Config;
import uk.ac.standrews.cs.valipop.implementations.OBDModel;
import uk.ac.standrews.cs.valipop.simulationEntities.IPersonCollection;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

public abstract class TestCases {

    public static final int SEED = 841584;

    public static IPersonCollection generatePopulation(final int initialPopulationSize)  {

        final Config config = new Config(
            LocalDate.of(1599, 1, 1),
            LocalDate.of(1855, 1, 1),
            LocalDate.of(2016, 1, 1),
            initialPopulationSize,
            Paths.get("src/test/resources/valipop/test-pop"),
            Config.DEFAULT_RESULTS_SAVE_PATH,
            "testing",
            Config.DEFAULT_RESULTS_SAVE_PATH);

        config.setDeterministic(true).setSeed(SEED);

        final OBDModel model = new OBDModel(config);
        model.runSimulation();

        final IPersonCollection population = model.getPopulation().getPeople();
        population.setDescription("initial size=" + initialPopulationSize + ", seed=" + SEED);
        return population;
    }

    public static List<Arguments> getTestConfigurations(final List<Integer> initialPopulationSizes) {

        return initialPopulationSizes.stream().map(TestCases::makeTestConfiguration).toList();
    }

    private static Arguments makeTestConfiguration(final int initialPopulationSize) {

        return Arguments.of(generatePopulation(initialPopulationSize));
    }
}
