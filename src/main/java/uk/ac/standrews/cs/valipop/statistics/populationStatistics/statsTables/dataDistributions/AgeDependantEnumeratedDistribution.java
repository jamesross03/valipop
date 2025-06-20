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
import uk.ac.standrews.cs.valipop.statistics.distributions.EnumeratedDistribution;
import uk.ac.standrews.cs.valipop.statistics.distributions.InconsistentWeightException;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRange;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.LabelledValueSet;

import java.security.InvalidParameterException;
import java.time.Year;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class AgeDependantEnumeratedDistribution implements InputMetaData<String> {

    private final Year year;
    private final String sourcePopulation;
    private final String sourceOrganisation;

    private final Map<IntegerRange, EnumeratedDistribution> distributionsByAge = new TreeMap<>();

    public AgeDependantEnumeratedDistribution(Year year, String sourcePopulation, String sourceOrganisation,
                                              Map<IntegerRange, LabelledValueSet<String, Double>> item_probabilities, RandomGenerator random) throws InconsistentWeightException {
        this.year = year;
        this.sourceOrganisation = sourceOrganisation;
        this.sourcePopulation = sourcePopulation;

        for (IntegerRange iR : item_probabilities.keySet()) {
            distributionsByAge.put(iR, new EnumeratedDistribution(item_probabilities.get(iR).getMap(), random));
        }
    }

    public EnumeratedDistribution getDistributionForAge(Integer age) {

        for (IntegerRange iR : distributionsByAge.keySet()) {
            if (iR.contains(age)) {
                return distributionsByAge.get(iR);
            }
        }

        throw new InvalidParameterException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLargestLabel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getLabels() {
        throw new UnsupportedOperationException();
    }

}
