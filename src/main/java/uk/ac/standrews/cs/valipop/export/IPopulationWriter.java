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

/**
 * Interface to be implemented by classes that process information from a population.
 *
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 * @see PopulationConverter
 */
public interface IPopulationWriter extends AutoCloseable {

    /**
     * Records a given person from the population.
     *
     * @param person the person
     */
    void recordPerson(IPerson person);

    /**
     * Records a given partnership from the population.
     *
     * @param partnership the person
     */
    void recordPartnership(IPartnership partnership);
}
