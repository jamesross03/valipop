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
package uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsKeys;

import java.time.Period;
import java.time.Year;

/**
 * A data structure for representing the keys used to access the input data
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class StatsKey<X, Y> {

    private final Y yLabel;
    private final X xLabel;
    private final double forNPeople;
    private final Period consideredTimePeriod;
    private final Year year;

    public StatsKey(Y yLabel, X xLabel, double forNPeople, Period consideredTimePeriod, Year year) {

        this.yLabel = yLabel;
        this.xLabel = xLabel;
        this.forNPeople = forNPeople;
        this.consideredTimePeriod = consideredTimePeriod;
        this.year = year;
    }

    public StatsKey(Y yLabel, double forNPeople, Period consideredTimePeriod, Year year) {

        this(yLabel, null, forNPeople, consideredTimePeriod, year);
    }

    public double getForNPeople() {
        return forNPeople;
    }

    public X getXLabel() {
        return xLabel;
    }

    public Y getYLabel() {
        return yLabel;
    }

    public Period getConsideredTimePeriod() {
        return consideredTimePeriod;
    }

    public Year getYear() {
        return year;
    }
}
