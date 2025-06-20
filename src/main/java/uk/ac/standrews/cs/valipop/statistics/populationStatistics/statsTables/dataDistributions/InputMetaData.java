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

import java.time.Year;
import java.util.Collection;


/**
 * The InputMetaData interface provides the provision of the general information required of all input statistics in
 * the program. A distribution contains labels which correspond to a value.
 *
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public interface InputMetaData<Label> {

    /**
     * @return the year to which the distribution pertains
     */
    Year getYear();

    /**
     * @return the 'real world' population which this distribution of statistical data has been drawn
     */
    String getSourcePopulation();

    /**
     * @return the organisation that produced/release the data to make this distribution
     */
    String getSourceOrganisation();

    /**
     * @return the smallest label value in the distribution
     */
    Label getSmallestLabel();

    /**
     * @return the largest label value in the distribution
     */
    Label getLargestLabel();

    Collection<Label> getLabels();

//    boolean outputToFile(Path directory);

}
