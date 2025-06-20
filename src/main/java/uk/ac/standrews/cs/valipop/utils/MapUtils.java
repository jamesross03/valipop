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
package uk.ac.standrews.cs.valipop.utils;


import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsTables.dataDistributions.OneDimensionDataDistribution;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRange;

import java.util.*;

/**
 * Utility functions for the Java Map type.
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class MapUtils {

    public static int getMax(Set<Integer> integers) {

        int max = Integer.MIN_VALUE;

        for (Integer i : integers) {
            if (i > max) {
                max = i;
            }
        }
        return max;
    }

    public static void print(String label, Map<IntegerRange, ?> temp, int s, int interval, int e) {

        System.out.print(label + " | ");
        for (int i = s; i <= e; i += interval) {
            IntegerRange iR = null;
            for (IntegerRange r : temp.keySet()) {
                if (r.contains(i)) {
                    iR = r;
                    break;
                }
            }

            System.out.print(temp.get(iR) + " | ");
        }
        System.out.println();
    }

    public static Map<IntegerRange, OneDimensionDataDistribution> clone(Map<IntegerRange, OneDimensionDataDistribution> tableData) {

        Map<IntegerRange, OneDimensionDataDistribution> clone = new TreeMap<>();

        for (Map.Entry<IntegerRange, OneDimensionDataDistribution> iR : tableData.entrySet()) {
            clone.put(iR.getKey(), iR.getValue().clone());
        }

        return clone;
    }

    public static Map<IntegerRange, Double> cloneODM(Map<IntegerRange, Double> tableData) {

        Map<IntegerRange, Double> clone = new TreeMap<>();

        for(Map.Entry<IntegerRange, Double> iR : tableData.entrySet()) {
            clone.put(iR.getKey(), iR.getValue());
        }

        return clone;
    }
}
