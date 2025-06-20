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
import java.time.LocalDate;
import java.util.List;

/**
 * An analytic class to analyse the entire population.
 *
 * @author Alan Dearle (alan.dearle@st-andrews.ac.uk)
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class PopulationAnalytics {

    private static final int ONE_HUNDRED = 100;
    private final IPersonCollection population;
    private PrintStream out;

    /**
     * Creates an analytic instance to analyse the entire population.
     *
     * @param population the population to analyse
     */
    public PopulationAnalytics(final IPersonCollection population, PrintStream resultsOutput) {

        this.population = population;
        out = resultsOutput;
    }

    private void printBirthDate(final IPerson person) {

        out.print(person.getBirthDate().toString());
    }

    private void printDeathDate(final IPerson person) {

        final LocalDate death_date = person.getDeathDate();
        if (death_date != null) {
            out.print(death_date.toString());
        }
    }

    /**
     * Prints out all analyses.
     *
     * @throws Exception if the population size cannot be accessed
     */
    public void printAllAnalytics() {

        final int size;
        try {
            size = population.getNumberOfPeople();
        } catch (Exception e) {
            throw new Error(e);
        }
        final int number_males = countMales();
        final int number_females = countFemales();

        out.println("Population size = " + size);
        out.println("Number of males = " + number_males + " = " + String.format("%.1f", number_males / (double) size * ONE_HUNDRED) + '%');
        out.println("Number of females = " + number_females + " = " + String.format("%.1f", number_females / (double) size * ONE_HUNDRED) + '%');
    }

    private int countMales() {

        int count = 0;
        for (final IPerson person : population.getPeople()) {
            if (person.getSex() == SexOption.MALE) {
                count++;
            }
        }
        return count;
    }

    private int countFemales() {

        int count = 0;
        for (final IPerson person : population.getPeople()) {
            if (person.getSex() == SexOption.FEMALE) {
                count++;
            }
        }
        return count;
    }

    /**
     * Prints the dates of birth of all people.
     */
    public void printAllBirthDates() {

        for (final IPerson person : population.getPeople()) {
            printBirthDate(person);
            out.println();
        }
    }

    /**
     * Prints the dates of death of all people.
     */
    public void printAllDeathDates() {

        for (final IPerson person : population.getPeople()) {
            printDeathDate(person);
            out.println();
        }
    }

    /**
     * Prints the dates of birth of child_ids in a partnership.
     *
     * @param partnership the partnership
     */
    public void printChildren(final IPartnership partnership) {

        if (partnership.getChildren() != null) {
            for (final IPerson child : partnership.getChildren()) {

                out.println("\t\tChild born: " + child.getBirthDate().toString());
            }
        }
    }

    /**
     * Prints the details of partnerships and child_ids for a given person.
     *
     * @param person the person
     */
    @SuppressWarnings("FeatureEnvy")
    public void printPartnerships(final IPerson person) {

        final List<IPartnership> partnership_ids = person.getPartnerships();
        if (partnership_ids != null) {
            for (final IPartnership partnership : partnership_ids) {


                final IPerson partner = partnership.getPartnerOf(person);
                out.println("\tPartner born: " + partner.getBirthDate().toString());

                final LocalDate marriage_date = partnership.getPartnershipDate();
                if (marriage_date != null) {
                    out.println("\tMarriage on " + marriage_date.toString());
                }

                printChildren(partnership);
            }
        }
    }

    /**
     * Prints all significant dates for the population.
     */
    public void printAllDates() {

        for (final IPerson person : population.getPeople()) {

            out.print(person.getSex() + " Born: ");
            printBirthDate(person);
            out.print(", Died: ");
            printDeathDate(person);
            out.println();
            printPartnerships(person);
        }
    }
}
