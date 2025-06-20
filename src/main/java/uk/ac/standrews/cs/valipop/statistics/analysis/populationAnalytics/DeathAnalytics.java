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

import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.simulationEntities.IPersonCollection;

import java.io.PrintStream;
import java.time.LocalDate;
import java.time.Period;
import java.util.stream.IntStream;

/**
 * An analytic class to analyse the distribution of deaths.
 *
 * @author Alan Dearle (alan.dearle@st-andrews.ac.uk)
 */
class DeathAnalytics {

    private static final int MAX_AGE_AT_DEATH = 110;
    private static final int ONE_HUNDRED = 100;

    private final int[] age_at_death = new int[MAX_AGE_AT_DEATH]; // tracks age of death over population
    private final IPersonCollection population;

    private PrintStream out;

    DeathAnalytics(final IPersonCollection population, PrintStream resultsOutput) {

        this.population = population;
        out = resultsOutput;
        analyseDeaths();
    }

    void printAllAnalytics() {

        final int sum = IntStream.of(age_at_death).sum();

        out.println("Death distribution:");
        for (int i = 1; i < age_at_death.length; i++) {
            out.println("\tDeaths at age: " + i + " = " + age_at_death[i] + " = " + String.format("%.3f", age_at_death[i] / (double) sum * ONE_HUNDRED) + '%');
        }
    }

    private void analyseDeaths() {

        for (final IPerson person : population.getPeople()) {

            final LocalDate death_date = person.getDeathDate();

            if (death_date != null) {

                final LocalDate birth_date = person.getBirthDate();
                final int age_at_death_in_years = Period.between(birth_date, death_date).getYears();
                if (age_at_death_in_years >= 0 && age_at_death_in_years < age_at_death.length) {
                    age_at_death[age_at_death_in_years]++;
                }
            }
        }
    }
}
