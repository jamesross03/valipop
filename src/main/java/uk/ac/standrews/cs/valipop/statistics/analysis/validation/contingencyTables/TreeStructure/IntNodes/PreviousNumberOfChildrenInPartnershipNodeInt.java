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
package uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.IntNodes;

import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.ChildNotFoundException;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.IntNode;
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
public class PreviousNumberOfChildrenInPartnershipNodeInt extends IntNode<IntegerRange, IntegerRange> {

    public PreviousNumberOfChildrenInPartnershipNodeInt(final IntegerRange option, final DiedNodeInt parentNode, final Integer initCount) {
        super(option, parentNode, initCount);
    }

    public PreviousNumberOfChildrenInPartnershipNodeInt() {
        super();
    }

    @Override
    public void processPerson(final IPerson person, final LocalDate currentDate) {

        incCountByOne();

        final IntegerRange range = resolveToChildRange(numberOfChildrenBirthedBeforeDate(person, currentDate));

        try {
            getChild(range).processPerson(person, currentDate);
        } catch (final ChildNotFoundException e) {
            addChild(range).processPerson(person, currentDate);
        }
    }

    @Override
    public String getVariableName() {
        return "PNCIP";
    }

    @Override
    public Node<IntegerRange, ?, Integer, ?> makeChildInstance(final IntegerRange childOption, final Integer initCount) {
        return new NumberOfPreviousChildrenInAnyPartnershipNodeInt(childOption, this, initCount);
    }

    private IntegerRange resolveToChildRange(final int numberOfChildren) {

        for (final Node<IntegerRange, ?, ?, ?> node : getChildren())
            if (node.getOption().contains(numberOfChildren))
                return node.getOption();

        final int age = ((AgeNodeInt) getAncestor(new AgeNodeInt())).getOption().getValue();
        final Year yearOfBirth = ((YOBNodeInt) getAncestor(new YOBNodeInt())).getOption();
        final Year currentDate = getYearAtAge(yearOfBirth, age);

        Collection<IntegerRange> birthOrders;
        try {
            birthOrders = getInputStats().getOrderedBirthRates(currentDate).getData(age).getLabels();
        }
        catch (final InvalidRangeException e) {
            final SelfCorrectingTwoDimensionDataDistribution data = getInputStats().getOrderedBirthRates(currentDate);
            birthOrders = data.getData(data.getSmallestLabel().getValue()).getLabels();
        }

        for (final IntegerRange range : birthOrders)
            if (range.contains(numberOfChildren))
                return range;

        throw new Error("Did not resolve any permissible ranges");
    }
}
