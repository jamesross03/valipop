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
import java.util.*;
import java.util.stream.IntStream;

/**
 * An analytic class to analyse the distribution of children.
 *
 * @author Alan Dearle (alan.dearle@st-andrews.ac.uk)
 * @author Tom Dalton
 */

class ChildrenAnalytics {

    private static final int MAX_CHILDREN = 100;
    private static final int ONE_HUNDRED = 100;

    private final int[] children_per_marriage = new int[MAX_CHILDREN]; // tracks family size
    private final IPersonCollection population;
    private PrintStream out;

    private final Map<Integer, Double> fertilityRateByYear = new TreeMap<>();

    ChildrenAnalytics(final IPersonCollection population, PrintStream resultsOutput) {

        this.population = population;
        out = resultsOutput;
        analyseChildren();
        calculateTFRByYear();
    }

    void printAllAnalytics() {

        final int sum = IntStream.of(children_per_marriage).sum();

        out.println("Children per marriage sizes:");
        for (int i = 0; i < children_per_marriage.length; i++) {
            if (children_per_marriage[i] != 0) {
                out.println("\t" + children_per_marriage[i] + " Marriages with " + i + " child_ids" + " = " + String.format("%.1f", children_per_marriage[i] / (double) sum * ONE_HUNDRED) + '%');
            }
        }

        out.println("Fertility rates by year:");
        for (Map.Entry<Integer, Double> fr : fertilityRateByYear.entrySet()) {
            if (!fr.getValue().equals(0.0)) {
                out.println("\t" + fr.getKey() + " Fertility rate = " + fr.getValue());
            }
        }
    }

    private void analyseChildren() {

        for (final IPerson person : population.getPeople()) {

            if (person.getSex() == SexOption.FEMALE) {
                final List<IPartnership> partnerships = person.getPartnerships();
                if (partnerships != null) {

                    for (final IPartnership partnership : partnerships) {

                        final List<IPerson> child_ids = partnership.getChildren();

                        if (child_ids != null) {
                            children_per_marriage[child_ids.size()]++;
                        }
                    }
                }
            }
        }
    }

    private void calculateTFRByYear() {

        Map<Integer, Integer> livingFemalesOfSBAgeInEachYear = new HashMap<>();
        Map<Integer, Integer> childrenBornInEachYear = new HashMap<>();

        final int MIN_CB_AGE = 15;
        final int MAX_CB_AGE = 50;

        for (final IPerson person : population.getPeople()) {

            if (person.getSex() == SexOption.FEMALE) {
                final List<IPartnership> partnerships = person.getPartnerships();
                if (partnerships != null) {

                    for (final IPartnership partnership : partnerships) {

                        final List<IPerson> child_ids = partnership.getChildren();

                        if (child_ids != null) {

                            for (final IPerson child : child_ids) {

                                int yob = child.getBirthDate().getYear();

                                try {
                                    childrenBornInEachYear.put(yob, childrenBornInEachYear.get(yob) + 1);
                                } catch (NullPointerException e) {
                                    childrenBornInEachYear.put(yob, 1);
                                }
                            }
                        }
                    }
                }

                int femalesYOB = person.getBirthDate().getYear();
                for (int y = femalesYOB + MIN_CB_AGE; y < femalesYOB + MAX_CB_AGE; y++) {

                    try {
                        livingFemalesOfSBAgeInEachYear.put(y, livingFemalesOfSBAgeInEachYear.get(y) + 1);
                    } catch (NullPointerException e) {
                        livingFemalesOfSBAgeInEachYear.put(y, 1);
                    }
                }
            }
        }

        Integer earliestYear = getSmallestValueInSets(livingFemalesOfSBAgeInEachYear.keySet(), childrenBornInEachYear.keySet());
        Integer latestYear = getLargestValueInSets(livingFemalesOfSBAgeInEachYear.keySet(), childrenBornInEachYear.keySet());

        if (earliestYear == null || latestYear == null) {
            return;
        }

        for (int y = earliestYear; y < latestYear; y++) {
            int births;
            int femalesOfCBAge;

            try {
                births = childrenBornInEachYear.get(y);
            } catch (NullPointerException e) {
                births = 0;
            }

            try {
                femalesOfCBAge = livingFemalesOfSBAgeInEachYear.get(y);
            } catch (NullPointerException e) {
                femalesOfCBAge = 0;
            }

            double asfrForYear;
            if (femalesOfCBAge == 0) {
                asfrForYear = 0;
            } else {
                asfrForYear = births / (double) femalesOfCBAge;
            }

            fertilityRateByYear.put(y, asfrForYear);
        }
    }

    private static Integer getSmallestValueInSets(Set<Integer> a, Set<Integer> b) {

        ArrayList<Integer> sets = new ArrayList<>(a);
        sets.addAll(b);

        Collections.sort(sets);

        if (sets.size() == 0) {
            return null;
        } else {
            return sets.get(0);
        }
    }

    private static Integer getLargestValueInSets(Set<Integer> a, Set<Integer> b) {

        ArrayList<Integer> sets = new ArrayList<>(a);
        sets.addAll(b);

        Collections.sort(sets);

        if (sets.size() == 0) {
            return null;
        } else {
            return sets.get(sets.size() - 1);
        }
    }
}
