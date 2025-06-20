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
package uk.ac.standrews.cs.valipop.statistics.analysis.populationAnalytics;

import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.simulationEntities.IPersonCollection;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SexOption;

import java.io.PrintStream;
import java.util.List;
import java.util.stream.IntStream;

/**
 * An analytic class to analyse the distribution of marriages.
 *
 * @author Alan Dearle (alan.dearle@st-andrews.ac.uk)
 */
class MarriageAnalytics {

    private static final int MAX_MARRIAGES = 25;
    private static final int ONE_HUNDRED = 100;

    private final int[] count_marriages = new int[MAX_MARRIAGES];
    private final IPersonCollection population;

    private PrintStream out;

    MarriageAnalytics(final IPersonCollection population, PrintStream resultsOutput) {

        this.population = population;
        out = resultsOutput;
        analyseMarriages();
    }

    void printAllAnalytics() {

        final int sum = IntStream.of(count_marriages).sum();

        out.println("Male marriage sizes:");
        out.println("\t unmarried: " + count_marriages[0]);

        for (int i = 1; i < count_marriages.length; i++) {
            if (count_marriages[i] != 0) {
                out.println("\t Married " + i + " times: " + count_marriages[i] + " = " + String.format("%.1f", count_marriages[i] / (double) sum * ONE_HUNDRED) + '%');
            }
        }
    }

    private void analyseMarriages() {

        for (final IPerson person : population.getPeople()) {

            if (person.getSex() == SexOption.MALE) { // only look at Males to avoid counting marriages twice.

                final List<IPartnership> partnership_ids = person.getPartnerships();

                if (partnership_ids == null) {
                    count_marriages[0]++;
                } else {
                    count_marriages[partnership_ids.size()]++;
                }
            }
        }
    }
}
