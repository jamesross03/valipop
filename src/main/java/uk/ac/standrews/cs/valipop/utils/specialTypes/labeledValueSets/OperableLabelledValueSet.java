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
package uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public interface OperableLabelledValueSet<L,V> extends LabelledValueSet<L,V> {

    OperableLabelledValueSet<L,V> productOfLabelsAndValues();

    OperableLabelledValueSet<L,Double> divisionOfValuesByLabels();

    OperableLabelledValueSet<L,Integer> controlledRoundingMaintainingSum();

    OperableLabelledValueSet<IntegerRange, Integer> controlledRoundingMaintainingSumProductOfLabelValues();

    L getLargestLabelOfNonZeroValueAndLabelLessOrEqualTo(L n);

    L getLargestLabelOfNonZeroValueAndLabelPreferablyLessOrEqualTo(L n);

    L getLargestLabelOfNonZeroValue();

    L getRandomLabelOfNonZeroValue();

    L smallestLabel();

    OperableLabelledValueSet<L,Double> valuesAddNWhereCorrespondingLabelNegativeInLVS(double n, OperableLabelledValueSet<L, ? extends Number> lvs);
}
