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
package uk.ac.standrews.cs.valipop.simulationEntities;

import java.time.LocalDate;

/**
 * Interface for a collection of persons
 */
public interface IPersonCollection {

    /**
     * Allows iteration over the people in the population.
     * The order is determined by the underlying population implementation.
     * The unique identifiers of people are allocated in temporal order.
     *
     * @return an iterable sequence of people
     */
    Iterable<IPerson> getPeople();

    /**
     * Allows iteration over the partnerships in the population.
     * The order is determined by the underlying population implementation.
     *
     * @return an iterable sequence of partnerships
     */
    Iterable<IPartnership> getPartnerships();

    /**
     * Retrieves a person by id.
     * @param id the id
     * @return the corresponding person
     */
    IPerson findPerson(int id);

    /**
     * Retrieves a partnership by id.
     * @param id the id
     * @return the corresponding partnership
     */
    IPartnership findPartnership(int id);

    /**
     * Returns the number of people in the population.
     * @return the number of people in the population
     */
    int getNumberOfPeople();

    /**
     * Returns the number of partnerships in the population.
     * @return the number of partnerships in the population
     */
    int getNumberOfPartnerships();

    LocalDate getStartDate();

    LocalDate getEndDate();

    /**
     * Sets a description for the population, which may be useful for testing and debugging.
     * @param description the description
     */
    void setDescription(String description);
}
