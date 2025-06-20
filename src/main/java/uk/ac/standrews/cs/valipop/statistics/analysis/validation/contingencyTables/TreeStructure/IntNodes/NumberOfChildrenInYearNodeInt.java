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
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRange;

import java.time.LocalDate;
import java.time.Year;
import java.util.Collection;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class NumberOfChildrenInYearNodeInt extends IntNode<Integer, IntegerRange> {

    public NumberOfChildrenInYearNodeInt(Integer option, ChildrenInYearNodeInt parentNode, Integer initCount) {
        super(option, parentNode, initCount);
    }

    public NumberOfChildrenInYearNodeInt() {
        super();
    }


    @Override
    public void processPerson(IPerson person, LocalDate currentDate) {

        incCountByOne();

        int prevChildren = ((PreviousNumberOfChildrenInPartnershipNodeInt) getAncestor(new PreviousNumberOfChildrenInPartnershipNodeInt())).getOption().getValue();

        int childrenThisYear = ((NumberOfChildrenInYearNodeInt) getAncestor(new NumberOfChildrenInYearNodeInt())).getOption();

        int ncip = prevChildren + childrenThisYear;
        IntegerRange range = resolveToChildRange(ncip);

        try {
            getChild(range).processPerson(person, currentDate);

        } catch (ChildNotFoundException e) {
            addChild(range).processPerson(person, currentDate);
        }
    }

    @Override
    public String getVariableName() {
        return "NCIY";
    }

    @Override
    public Node<IntegerRange, ?, Integer, ?> makeChildInstance(IntegerRange childOption, Integer initCount) {
        return new NumberOfChildrenInPartnershipNodeInt(childOption, this, initCount);
    }

    @SuppressWarnings("Duplicates")
    private IntegerRange resolveToChildRange(Integer ncip) {

        for (Node<IntegerRange, ?, ?, ?> aN : getChildren()) {
            if (aN.getOption().contains(ncip)) {
                return aN.getOption();
            }
        }

        Year yob = ((YOBNodeInt) getAncestor(new YOBNodeInt())).getOption();
        int age = ((AgeNodeInt) getAncestor(new AgeNodeInt())).getOption().getValue();

        Year currentDate = Year.of(yob.getValue() + age);

        Collection<IntegerRange> sepRanges = getInputStats().getSeparationByChildCountRates(currentDate).getColumnLabels();

        for (IntegerRange o : sepRanges) {
            if (o.contains(ncip)) {
                return o;
            }
        }

        if (ncip == 0) {
            return new IntegerRange(0);
        }

        throw new Error("Did not resolve any permissible ranges");
    }
}
