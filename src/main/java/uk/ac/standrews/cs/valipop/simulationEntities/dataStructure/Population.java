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
package uk.ac.standrews.cs.valipop.simulationEntities.dataStructure;

import uk.ac.standrews.cs.valipop.Config;
import uk.ac.standrews.cs.valipop.simulationEntities.*;
import uk.ac.standrews.cs.valipop.utils.specialTypes.dates.DateUtils;

import java.time.LocalDate;
import java.time.Period;

/**
 * Represents a population.
 *
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class Population {

    // TODO rationalise OBDModel, Population, PersonCollection, PeopleCollection, IPersonCollection.

    private PeopleCollection livingPeople;
    private PeopleCollection deadPeople;

    private PeopleCollection emigrants;

    private PopulationCounts populationCounts;

    public Population(final Config config) {

        // TODO avoid statics.
        Person.resetIds();
        Partnership.resetIds();

        livingPeople = new PeopleCollection(config.getTS(), config.getTE(), config.getSimulationTimeStep(), "living");

        deadPeople = new PeopleCollection(config.getTS(), config.getTE(), config.getSimulationTimeStep(), "dead");

        emigrants = new PeopleCollection(config.getTS(), config.getTE(), config.getSimulationTimeStep(), "emigrants");

        populationCounts = new PopulationCounts();
    }

    public PeopleCollection getPeople() {

        return combine(combine(livingPeople, deadPeople), emigrants);
    }

    public PeopleCollection getPeople(final LocalDate first, final LocalDate last, final Period maxAge) {

        final PeopleCollection result = new PeopleCollection(first, last, Period.ofYears(1), "combined");

        Period period = Period.between(first, last);

        for (final IPerson person : livingPeople.getPeopleAliveInTimePeriod(first, period, maxAge)) {
            result.add(person);
        }

        for (final IPerson person : deadPeople.getPeopleAliveInTimePeriod(first, period, maxAge)) {
            result.add(person);
        }

        return result;
    }

    public PeopleCollection getLivingPeople() {
        return livingPeople;
    }

    public PeopleCollection getDeadPeople() {
        return deadPeople;
    }

    public PeopleCollection getEmigrants() {
        return emigrants;
    }

    public PopulationCounts getPopulationCounts() {
        return populationCounts;
    }

    private PeopleCollection combine(final PeopleCollection collection1, final PeopleCollection collection2) {

        final LocalDate start = DateUtils.earlierOf(collection1.getStartDate(), collection2.getStartDate());
        final LocalDate end = DateUtils.laterOf(collection1.getStartDate(), collection2.getStartDate());

        final PeopleCollection cloned1 = collection1.clone();
        final PeopleCollection cloned2 = collection2.clone();

        cloned1.setStartDate(start);
        cloned1.setEndDate(end);

        for (IPerson person : cloned2) {
            cloned1.add(person);
        }

        for (IPartnership person : cloned2.getPartnerships()) {
            cloned1.add(person);
        }

        return cloned1;
    }
}
