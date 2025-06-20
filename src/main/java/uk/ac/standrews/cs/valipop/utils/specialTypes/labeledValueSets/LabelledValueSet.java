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

import java.util.Map;
import java.util.Set;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public interface LabelledValueSet<L,V> {

    Map<L,V> getMap();

    V getSumOfValues();

    V getValue(L label);

    Set<L> getLabels();

    void add(L label, V value);

    V get(L label);

    void update(L label, V value);

    V remove(L label);

    LabelledValueSet<L,Double> productOfValuesAndN(double n);

    LabelledValueSet<L, Double> valuesSubtractValues(LabelledValueSet<L, ? extends Number> n);

    LabelledValueSet<L,Integer> floorValues();

    LabelledValueSet<L,V> clone();

    L getLabelOfValueWithGreatestRemainder(Set<L> usedLabels);

    LabelledValueSet<L,Double> valuesPlusValues(LabelledValueSet<L, ? extends Number> n);

    LabelledValueSet<L,Double> reproportion();

    int countNegativeValues();

    LabelledValueSet<L,V> zeroNegativeValues();

    int countPositiveValues();

    LabelledValueSet<L,Double> divisionOfValuesByN(V n);
}
