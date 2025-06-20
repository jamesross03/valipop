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
package uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsTables.dataDistributions;

import org.apache.commons.math3.random.RandomGenerator;
import uk.ac.standrews.cs.valipop.statistics.distributions.StringWithCumulativeProbability;
import uk.ac.standrews.cs.valipop.statistics.distributions.EnumeratedDistribution;
import uk.ac.standrews.cs.valipop.statistics.distributions.InconsistentWeightException;

import java.time.Year;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class ValiPopEnumeratedDistribution extends EnumeratedDistribution implements InputMetaData<String> {

    private final Year year;
    private final String sourcePopulation;
    private final String sourceOrganisation;

    public ValiPopEnumeratedDistribution(Year year, String sourcePopulation, String sourceOrganisation, Map<String, Double> item_probabilities, RandomGenerator random) throws InconsistentWeightException {

        super(item_probabilities, random);
        this.year = year;
        this.sourceOrganisation = sourceOrganisation;
        this.sourcePopulation = sourcePopulation;
    }

    @Override
    public Year getYear() {
        return year;
    }

    @Override
    public String getSourcePopulation() {
        return sourcePopulation;
    }

    @Override
    public String getSourceOrganisation() {
        return sourceOrganisation;
    }

    @Override
    public String getSmallestLabel() {
        return items[0].getItem();
    }

    @Override
    public String getLargestLabel() {
        return items[items.length - 1].getItem();
    }

    @Override
    public Collection<String> getLabels() {

        Collection<String> col = new ArrayList<>(items.length);

        for(StringWithCumulativeProbability i : items) {
            col.add(i.getItem());
        }

        return col;
    }
}
