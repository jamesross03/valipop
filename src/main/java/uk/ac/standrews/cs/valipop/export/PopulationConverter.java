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

import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.simulationEntities.IPersonCollection;

import java.util.*;

/**
 * Converts a population from one representation to another.
 *
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 */
public class PopulationConverter implements AutoCloseable {

    private final IPersonCollection population;
    private final IPopulationWriter population_writer;

    /**
     * Initialises the population converter.
     *
     * @param population        the population to be converted
     * @param population_writer the population writer to be used to create the new representation
     */
    public PopulationConverter(final IPersonCollection population, final IPopulationWriter population_writer) {

        this.population = population;
        this.population_writer = population_writer;
    }

    /**
     * Creates a new population representation by passing each person and partnership in the population to the population writer.
     */
    public void convert() {

        final List<IPerson> people = new ArrayList<>();
        for (final IPerson p : population.getPeople()) people.add(p);

        final Set<IPerson> immigrantParentsToAdd = new HashSet<>();

        final List<IPartnership> partnerships = new ArrayList<>();
        for (final IPartnership p : population.getPartnerships()) partnerships.add(p);

        final Set<IPartnership> immigrantParentPartnershipsToAdd = new HashSet<>();

        for (final IPerson person : people) {

            final IPartnership parents = person.getParents();

            if (parents != null) {
                final IPerson mother = parents.getFemalePartner();
                final IPerson father = parents.getMalePartner();

                if (!people.contains(mother)) immigrantParentsToAdd.add(mother);
                if (!people.contains(father)) immigrantParentsToAdd.add(father);

                if (!partnerships.contains(parents)) immigrantParentPartnershipsToAdd.add(parents);
            }
        }

        people.addAll(immigrantParentsToAdd);
        partnerships.addAll(immigrantParentPartnershipsToAdd);

        for (final IPerson person : sort(people))
            population_writer.recordPerson(person);


        for (final IPartnership partnership : sort(partnerships))
            population_writer.recordPartnership(partnership);
    }

    @Override
    public void close() throws Exception {

        population_writer.close();
    }

    private <T extends Comparable<T>> Iterable<T> sort(final Iterable<T> unsorted) {

        final List<T> list = new ArrayList<>();
        for (final T value : unsorted) {
            list.add(value);
        }
        Collections.sort(list);
        return list;
    }
}
