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
package uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.DoubleNodes;

import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.ChildNotFoundException;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.DoubleNode;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.Node;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsTables.dataDistributions.selfCorrecting.SelfCorrectingTwoDimensionDataDistribution;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRange;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.InvalidRangeException;

import java.time.LocalDate;
import java.time.Year;
import java.util.Collection;

import static uk.ac.standrews.cs.valipop.simulationEntities.PopulationNavigation.numberOfChildrenBirthedBeforeDate;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class PreviousNumberOfChildrenInPartnershipNodeDouble extends DoubleNode<IntegerRange, IntegerRange> {

    public PreviousNumberOfChildrenInPartnershipNodeDouble(IntegerRange option, DiedNodeDouble parentNode, double initCount) {
        super(option, parentNode, initCount);
    }

    public PreviousNumberOfChildrenInPartnershipNodeDouble() {
        super();
    }

    @Override
    public void processPerson(IPerson person, LocalDate currentDate) {
        incCountByOne();

        Integer numberOfPrevChildrenInAnyPartnership = numberOfChildrenBirthedBeforeDate(person, currentDate);
        IntegerRange range = resolveToChildRange(numberOfPrevChildrenInAnyPartnership);

        try {
            getChild(range).processPerson(person, currentDate);
        } catch (ChildNotFoundException e) {
            addChild(range).processPerson(person, currentDate);
        }
    }

    public Node<IntegerRange, ?, Double, ?> addChild(IntegerRange childOption, Double initCount) {

        Node<IntegerRange, ?, Double, ?> child;

        try {
            child = getChild(childOption);
            child.incCount(initCount);
        } catch (ChildNotFoundException e) {
            child = makeChildInstance(childOption, initCount);
            super.addChild(child);
        }

        return child;
    }

    @Override
    public String getVariableName() {
        return "PNCIP";
    }

    @Override
    public Node<IntegerRange, ?, Double, ?> makeChildInstance(IntegerRange childOption, Double initCount) {
        return new NumberOfPreviousChildrenInAnyPartnershipNodeDouble(childOption, this, initCount);
    }

    @SuppressWarnings("Duplicates")
    public IntegerRange resolveToChildRange(Integer npciap) {

        for (Node<IntegerRange, ?, ?, ?> aN : getChildren()) {
            if (aN.getOption().contains(npciap)) {
                return aN.getOption();
            }
        }

        Year yob = ((YOBNodeDouble) getAncestor(new YOBNodeDouble())).getOption();
        int age = ((AgeNodeDouble) getAncestor(new AgeNodeDouble())).getOption().getValue();

        Year currentDate = getYearAtAge(yob, age);

        Collection<IntegerRange> birthOrders;
        try {
            birthOrders = getInputStats().getOrderedBirthRates(currentDate).getData(age).getLabels();
        } catch (InvalidRangeException e) {
            SelfCorrectingTwoDimensionDataDistribution data = getInputStats().getOrderedBirthRates(currentDate);
            birthOrders = data.getData(data.getSmallestLabel().getValue()).getLabels();
        }

        for (IntegerRange o : birthOrders) {
            if (o.contains(npciap)) {
                return o;
            }
        }

        throw new RuntimeException("Did not resolve any permissible ranges");
    }
}
