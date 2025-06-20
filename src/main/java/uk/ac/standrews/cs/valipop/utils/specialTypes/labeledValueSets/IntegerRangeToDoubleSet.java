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

import org.apache.commons.math3.random.RandomGenerator;
import uk.ac.standrews.cs.valipop.utils.CollectionUtils;
import uk.ac.standrews.cs.valipop.utils.DoubleComparer;

import java.util.*;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class IntegerRangeToDoubleSet extends AbstractLabelToAbstractValueSet<IntegerRange, Double> implements OperableLabelledValueSet<IntegerRange, Double> {

    private static double DELTA = 1E-2;

    public IntegerRangeToDoubleSet(List<IntegerRange> labels, List<Double> values, RandomGenerator random) {
        super(labels, values, random);
    }

    public IntegerRangeToDoubleSet(Set<IntegerRange> labels, Double initValue, RandomGenerator random) {
        super(labels, initValue, random);
    }

    public IntegerRangeToDoubleSet(LabelledValueSet<IntegerRange, Double> set, RandomGenerator random) {
        super(set.getMap(), random);
    }

    public IntegerRangeToDoubleSet(RandomGenerator random) {
        super(random);
    }

    @Override
    public Class<Double> getValueClass() {
        return Double.class;
    }

    @Override
    public LabelledValueSet<IntegerRange, Double> constructSelf(List<IntegerRange> labels, List<Double> values) {
        return new IntegerRangeToDoubleSet(labels, values, random);
    }

    @Override
    public LabelledValueSet<IntegerRange, Integer> constructIntegerEquivalent(List<IntegerRange> labels, List<Integer> values) {
        return new IntegerRangeToIntegerSet(labels, values, random);
    }

    @Override
    public LabelledValueSet<IntegerRange, Double> constructDoubleEquivalent(List<IntegerRange> labels, List<Double> values) {
        return constructSelf(labels, values);
    }

    @Override
    public Double zero() {
        return 0.0;
    }

    @Override
    public Double sum(Double a, Double b) {
        return a + b;
    }

    @Override
    public Double multiply(Double a, int n) {
        return a * n;
    }

    @Override
    public OperableLabelledValueSet<IntegerRange, Double> productOfLabelsAndValues() {
        List<IntegerRange> labels = new ArrayList<>();
        List<Double> products = new ArrayList<>();

        for (IntegerRange iR : map.keySet()) {
            labels.add(iR);
            products.add(iR.getValue() * getValue(iR));
        }

        return new IntegerRangeToDoubleSet(labels, products, random);
    }

    @Override
    public OperableLabelledValueSet<IntegerRange, Integer> controlledRoundingMaintainingSum() {

        double sum = getSumOfValues();
        double sumRounded = Math.round(sum);

        if (!DoubleComparer.equal(sum, sumRounded, DELTA)) {
            throw new ValuesDoNotSumToWholeNumberException("Cannot perform controlled rounding and maintain sum as values do not sum to a whole number");
        }

        int sumInt = (int) sumRounded;

        OperableLabelledValueSet<IntegerRange, Integer> roundingSet = new IntegerRangeToIntegerSet(random);

        for (IntegerRange iR : getLabels()) {
            if (getValue(iR) < 0) {
                roundingSet.add(iR, 0);
            } else {
                roundingSet.add(iR, (int) Math.floor(getValue(iR)));
            }
        }

        Set<IntegerRange> usedLabels = new TreeSet<>();

        int roundingSetSum;
        while ((roundingSetSum = roundingSet.getSumOfValues()) != sumInt) {

            if (roundingSetSum < sumInt) {
                // need more in the rounding set therefore
                IntegerRange labelOfGreatestRemainder = this.getLabelOfValueWithGreatestRemainder(usedLabels);
                roundingSet.update(labelOfGreatestRemainder, roundingSet.getValue(labelOfGreatestRemainder) + 1);
            }

            if (roundingSetSum > sumInt) {
                IntegerRange largestReducatbleLabel;
                try {
                    largestReducatbleLabel =
                            roundingSet.getLargestLabelOfNonZeroValueAndLabelLessOrEqualTo(new IntegerRange(roundingSetSum - sumInt));
                } catch (NoSuchElementException e) {
                    largestReducatbleLabel = this.smallestLabel();
                }
                roundingSet.update(largestReducatbleLabel, roundingSet.getValue(largestReducatbleLabel) - 1);
            }
        }

        return roundingSet;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public OperableLabelledValueSet<IntegerRange, Integer> controlledRoundingMaintainingSumProductOfLabelValues() {

        double sum = productOfLabelsAndValues().getSumOfValues();
        double sumRounded = Math.round(sum);

        if (!DoubleComparer.equal(sum, sumRounded, DELTA)) {
            throw new ValuesDoNotSumToWholeNumberException("Cannot perform controlled rounding and maintain sum as values do not sum to a whole number");
        }

        int sumInt = (int) sumRounded;

        OperableLabelledValueSet<IntegerRange, Integer> roundingSet = new IntegerRangeToIntegerSet(random);

        for (IntegerRange iR : getLabels()) {
            if (getValue(iR) < 0) {
                roundingSet.add(iR, 0);
            } else {
                roundingSet.add(iR, (int) Math.floor(getValue(iR)));
            }
        }

        Set<IntegerRange> usedLabels = new TreeSet<>();

        int roundingSetSum;
        while ((roundingSetSum = roundingSet.productOfLabelsAndValues().getSumOfValues()) != sumInt) {

            if (roundingSetSum < sumInt) {
                // need more in the rounding set therefore
                IntegerRange labelOfGreatestRemainder;
                try {
                    labelOfGreatestRemainder = this.getLabelOfValueWithGreatestRemainder(usedLabels);
                } catch (NoSuchElementException e) {
                    labelOfGreatestRemainder = this.smallestLabel();
                }
                roundingSet.update(labelOfGreatestRemainder, roundingSet.getValue(labelOfGreatestRemainder) + 1);
                usedLabels.add(labelOfGreatestRemainder);
            }

            // too many in rounding set therefore
            if (roundingSetSum > sumInt) {
                // catch and increase to self - then the up will put in a lower order birth
                IntegerRange largestReducatbleLabel;
                try {
                    largestReducatbleLabel = roundingSet.getLargestLabelOfNonZeroValueAndLabelPreferablyLessOrEqualTo(new IntegerRange(roundingSetSum - sumInt));
                } catch (NoSuchElementException e) {
                    largestReducatbleLabel = this.smallestLabel();
                }
                roundingSet.update(largestReducatbleLabel, roundingSet.getValue(largestReducatbleLabel) - 1);
            }
        }

        return roundingSet;
    }

    @Override
    public OperableLabelledValueSet<IntegerRange, Double> divisionOfValuesByLabels() {
        List<IntegerRange> labels = new ArrayList<>();
        List<Double> products = new ArrayList<>();

        for (IntegerRange iR : map.keySet()) {
            labels.add(iR);
            products.add(getValue(iR) / (double) iR.getValue());
        }

        return new IntegerRangeToDoubleSet(labels, products, random);
    }

    @Override
    public IntegerRange getLargestLabelOfNonZeroValueAndLabelLessOrEqualTo(IntegerRange n) {

        IntegerRange largestLabel = null;

        for (IntegerRange range : map.keySet()) {

            final int currentIRLabel = range.getValue();

            if (currentIRLabel <= n.getValue()) {
                if (largestLabel == null || currentIRLabel > largestLabel.getValue()) {
                    largestLabel = range;
                }
            }
        }

        if (largestLabel == null) {
            throw new NoSuchElementException("No values in set or no values in set less than n - set size: " + getLabels().size());
        }

        return largestLabel;
    }

    @Override
    public IntegerRange getLargestLabelOfNonZeroValueAndLabelPreferablyLessOrEqualTo(IntegerRange n) {

        IntegerRange largestLabel = null;
        IntegerRange smallestLabelLargerThanN = null;

        for (IntegerRange range : map.keySet()) {

            final int currentIRLabel = range.getValue();

            if (currentIRLabel <= n.getValue()) {
                if (largestLabel == null || currentIRLabel > largestLabel.getValue()) {
                    largestLabel = range;
                }
            } else {
                if (largestLabel == null || smallestLabelLargerThanN == null || currentIRLabel < smallestLabelLargerThanN.getValue()) {
                    smallestLabelLargerThanN = range;
                }
            }
        }

        if (largestLabel == null) {

            if (smallestLabelLargerThanN != null) {
                return smallestLabelLargerThanN;
            }

            throw new NoSuchElementException("No values in set or no values in set less that n - set size: " + getLabels().size());
        }

        return largestLabel;
    }

    @Override
    public IntegerRange getLargestLabelOfNonZeroValue() {
        IntegerRange largestLabel = null;

        for (IntegerRange range : map.keySet()) {

            int currentIRLabel = range.getValue();

            if (largestLabel == null || currentIRLabel > largestLabel.getValue()) {
                if (!DoubleComparer.equal(0, get(range), DELTA)) {
                    largestLabel = range;
                }
            }
        }

        if (largestLabel == null) {
            throw new NoSuchElementException("No non zero values in set - set size: " + getLabels().size());
        }

        return largestLabel;
    }

    @Override
    public IntegerRange getRandomLabelOfNonZeroValue() {

        ArrayList<IntegerRange> keys = new ArrayList<>(map.keySet());
        CollectionUtils.shuffle(keys, random);

        for (IntegerRange range : keys) {
            if (!DoubleComparer.equal(0, get(range), DELTA))
                return range;
        }

        throw new NoSuchElementException("No non zero values in set - set size: " + getLabels().size());

    }

    @Override
    public IntegerRange smallestLabel() {
        Set<IntegerRange> labels = getLabels();

        int minLabelInt = Integer.MAX_VALUE;
        IntegerRange minLabel = null;

        for (IntegerRange label : labels) {
            if (label.getValue() < minLabelInt) {
                minLabel = label;
                minLabelInt = label.getValue();
            }
        }

        return minLabel;
    }

    @Override
    public OperableLabelledValueSet<IntegerRange, Double> valuesAddNWhereCorrespondingLabelNegativeInLVS(double n, OperableLabelledValueSet<IntegerRange, ? extends Number> lvs) {

        List<IntegerRange> labels = new ArrayList<>(getLabels());
        List<Double> newValues = new ArrayList<>();

        for (IntegerRange label : labels) {
            if ((Double) lvs.get(label) < 0) {
                newValues.add(getValue(label) + n);
            } else {
                newValues.add(getValue(label));
            }
        }

        return new IntegerRangeToDoubleSet(labels, newValues, random);
    }
}
