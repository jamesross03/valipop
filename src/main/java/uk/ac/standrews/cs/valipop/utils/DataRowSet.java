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

import org.apache.commons.math3.random.RandomGenerator;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRange;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.LabelledValueSet;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.StringToDoubleSet;

import java.util.*;

/**
 * Represents a set of CSV rows.
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class DataRowSet implements Iterable<DataRow> {

    List<String> labels;
    Set<DataRow> dataset = new HashSet<>();

    public DataRowSet(DataRow initialRow) {
        setLabels(initialRow.getLabels());
        dataset.add(initialRow);
    }

    public DataRowSet(String labels, List<String> lines, String filterOn, String filterValue) throws InvalidInputFileException {

        for(String line : lines) {
            DataRow dr = new DataRow(labels, line);
            if(dr.getValue(filterOn).equals(filterValue)) {
                if(!dr.getValue("Age").equals(".")) {
                    dr.setValue("Age", String.valueOf(Double.valueOf(Math.floor(Double.valueOf(dr.getValue("Age")))).intValue()));
                    dataset.add(dr);
                }
            }
        }

        setLabels(dataset.iterator().next().getLabels());
    }

    private void setLabels(Set<String> labels) {
        this.labels = new ArrayList<>(labels);
    }

    public DataRowSet(String labels, Set<String> lines) throws InvalidInputFileException {

        for(String line : lines)
            dataset.add(new DataRow(labels, line));

        setLabels(dataset.iterator().next().getLabels());
    }


    public boolean hasLabel(String label) {
        return labels.contains(label);
    }

    public Map<String, DataRowSet> splitOn(String splitOn) {

        Map<String, DataRowSet> tables = new HashMap<>();

        for (DataRow row : dataset) {
            String splitValue = row.getValue(splitOn);
            if(tables.keySet().contains(splitValue)) {
                tables.get(splitValue).add(row);
            } else {
                tables.put(splitValue, new DataRowSet(row));
            }
        }

        return tables;
    }

    public TreeMap<IntegerRange, LabelledValueSet<String, Double>> to2DTableOfProportions(String xLabelOfInt, String yLabelOfString, RandomGenerator random) {

        Map<String, Map<String, Integer>> counts = new HashMap<>();
        Map<String, Integer> totalCountsUnderX = new HashMap<>();

        for(DataRow row : dataset) {

            String iR = new IntegerRange(row.getValue(xLabelOfInt)).toString();

            if(totalCountsUnderX.containsKey(iR)) {
                Map<String, Integer> map = counts.get(iR);

                if(map.containsKey(row.getValue(yLabelOfString))) {
                    map.put(row.getValue(yLabelOfString), map.get(row.getValue(yLabelOfString)) + 1);
                } else {
                    map.put(row.getValue(yLabelOfString), 1);
                }

            } else {
                Map<String, Integer> map = new HashMap<>();
                map.put(row.getValue(yLabelOfString), 1);
                counts.put(iR, map);
            }

//            if(!totalCountsUnderX.containsKey(iR)) totalCountsUnderX.put(iR, 0);

            totalCountsUnderX.computeIfAbsent(iR, k -> 0 );
            totalCountsUnderX.put(iR, totalCountsUnderX.get(iR) + 1);

        }

        TreeMap<IntegerRange, LabelledValueSet<String, Double>> proportions = new TreeMap<>();
        Set<String> labels = new TreeSet<>();

        for(String iR : counts.keySet()) {
            int totalCount = totalCountsUnderX.get(iR);
            Map<String, Integer> countMap = counts.get(iR);

            LabelledValueSet<String, Double> proportionMap = new StringToDoubleSet(random);

            for(String label : countMap.keySet()) {
                proportionMap.add(label, countMap.get(label) / (double) totalCount);
                labels.add(label);
            }

            proportions.put(new IntegerRange(iR), proportionMap);
        }

        for(IntegerRange iR : proportions.keySet()) {

            LabelledValueSet<String, Double> proportionMap = proportions.get(iR);

            for(String label : labels) {
                if(!proportionMap.getLabels().contains(label)) {
                    proportionMap.add(label, 0.0);
                }
            }

        }
        return proportions;

    }

    public void add(DataRow row) {

        dataset.add(row);
    }



    public void remove(DataRow row) {
        dataset.remove(row);
    }

    @Override
    public Iterator<DataRow> iterator() {
        return dataset.iterator();
    }

    public String toString(List<String> order) {
        StringBuilder sb = new StringBuilder();

        for(int s = 0; s < order.size() - 1; s++) {
            sb.append(order.get(s) + ",");
        }
        sb.append(order.get(order.size() - 1) + "\n");

        for(DataRow dr : dataset) {
            for(int s = 0; s < order.size() - 1; s++) {
                sb.append(dr.getValue(order.get(s)) + ",");
            }
            sb.append(dr.getValue(order.get(order.size() - 1)) + "\n");
        }

        return sb.toString();
    }

    public int size() {
        return dataset.size();
    }
}
