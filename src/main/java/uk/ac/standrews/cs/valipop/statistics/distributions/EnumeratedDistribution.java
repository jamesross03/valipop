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

import org.apache.commons.math3.random.RandomGenerator;

import java.io.Serializable;
import java.util.*;

/**
 * A distribution of strings controlled by specified probabilities.
 *
 * @author Alan Dearle (alan.dearle@st-andrews.ac.uk)
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 */
public class EnumeratedDistribution implements Distribution<String> {

    private static final Double ALLOWABLE_TOTAL_WEIGHT_DISCREPANCY = 0.001;
    private static final Comparator<? super StringWithCumulativeProbability> ITEM_COMPARATOR = new ItemComparator();

    private final RandomGenerator random;
    protected StringWithCumulativeProbability[] items = null;

    private EnumeratedDistribution(final RandomGenerator random) {
        this.random = random;
    }

    /**
     * Creates an Enumerated distribution.
     *
     * @param item_probabilities a map of strings to probabilities to be used in the creation of the distribution.
     * @param random             a Random instance for use in creation of distribution.
     * @throws InconsistentWeightException if the weights in the underlying distribution do not sum to 1.
     */
    public EnumeratedDistribution(final Map<String, Double> item_probabilities, final RandomGenerator random) throws InconsistentWeightException {

        this(random);
        configureProbabilities(item_probabilities);
    }

    private void configureProbabilities(final Map<String, Double> item_probabilities) throws InconsistentWeightException {

        List<StringWithCumulativeProbability> items_temp = new ArrayList<>();
        Double cumulative_probability = 0.0;

        for (final Map.Entry<String, Double> entry : item_probabilities.entrySet()) {

            if (entry.getValue() != 0) {
                cumulative_probability += entry.getValue();
                items_temp.add(new StringWithCumulativeProbability(entry.getKey(), cumulative_probability));
            }
        }

        if (Math.abs(cumulative_probability - 1) > ALLOWABLE_TOTAL_WEIGHT_DISCREPANCY) {
            throw new InconsistentWeightException();
        }

        items = items_temp.toArray(new StringWithCumulativeProbability[0]);
    }

    @Override
    public String getSample() {

        final Double dice_throw = random.nextDouble();

        int sample_index = Arrays.binarySearch(items, new StringWithCumulativeProbability("", dice_throw), ITEM_COMPARATOR);

        // If the exact cumulative probability isn't matched - and it's very unlikely to be - the result of binarySearch() is (-(insertion point) - 1).
        if (sample_index < 0) {
            sample_index = -sample_index - 1;
        }
        if (sample_index >= items.length) {
            sample_index = items.length - 1;
        }

        return items[sample_index].getItem();
    }

    private static class ItemComparator implements Comparator<StringWithCumulativeProbability>, Serializable {

        @Override
        public int compare(final StringWithCumulativeProbability o1, final StringWithCumulativeProbability o2) {
            return o1.getCumulativeProbability().compareTo(o2.getCumulativeProbability());
        }
    }
}
