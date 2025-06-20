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
package uk.ac.standrews.cs.valipop.statistics.distributions;

/**
 * Provides the functionality of a string with associated cumulative probability.
 */
public class StringWithCumulativeProbability {

    private final String item;
    private final Double cumulative_probability;

    /**
     * Creates a cumulative probability string.
     * 
     * @param item The associated string.
     * @param cumulative_probability The strings associated cumulative probability.
     */
    public StringWithCumulativeProbability(final String item, final Double cumulative_probability) {

        this.item = item;
        this.cumulative_probability = cumulative_probability;
    }

    /**
     * Returns item string.
     * 
     * @return Returns the associated string.
     */
    public String getItem() {
        return item;
    }

    /**
     * Returns the cumulative probability pertaining to the given item string.
     * 
     * @return Returns the associated cumulative probability.
     */
    public Double getCumulativeProbability() {
        return cumulative_probability;
    }
}
