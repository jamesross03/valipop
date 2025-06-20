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
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TableStructure.CTRow;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.*;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRange;

import java.time.LocalDate;
import java.time.Year;

import static uk.ac.standrews.cs.valipop.simulationEntities.PopulationNavigation.diedInYear;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class AgeNodeDouble extends DoubleNode<IntegerRange, Boolean> implements ControlChildrenNode, RunnableNode {

    private boolean initNode = false;

    AgeNodeDouble(IntegerRange age, SexNodeDouble parentNode, double initCount, boolean init) {

        super(age, parentNode, initCount);

        Year yob = ((YOBNodeDouble) getAncestor(new YOBNodeDouble())).getOption();

        if (yob.getValue() + age.getValue() < getStartDate().getYear()) {
            initNode = true;
        }

        if (!init) {
            makeChildren();
        }
    }

    @Override
    public void incCount(Double byCount) {
        setCount(getCount() + byCount);
    }

    AgeNodeDouble() {
        super();
    }

    @Override
    public Node<Boolean, ?, Double, ?> makeChildInstance(Boolean childOption, Double initCount) {
        return new DiedNodeDouble(childOption, this, false);
    }

    @Override
    public void makeChildren() {

        addChild(true);
        addChild(false);
    }

    @Override
    public void processPerson(IPerson person, LocalDate currentDate) {

        initNode = true;

        incCountByOne();

        Boolean option = diedInYear(person, Year.of(currentDate.getYear()));

        try {
            getChild(option).processPerson(person, currentDate);

        } catch (ChildNotFoundException e) {
            DiedNodeDouble n = (DiedNodeDouble) addChild(new DiedNodeDouble(option, this, true));
            n.processPerson(person, currentDate);
            addDelayedTask(n);
        }
    }

    @Override
    public String getVariableName() {
        return "Age";
    }

    @Override
    public void run() {
        makeChildren();
    }

    @SuppressWarnings("rawtypes")
    double sumOfNPCIAPDescendants(IntegerRange option) {

        double count = 0;

        for (Node c : getChildren()) {
            // c is of type died
            DiedNodeDouble cD = (DiedNodeDouble) c;
            for (Node gc : cD.getChildren()) {
                // gc is of type pncip
                PreviousNumberOfChildrenInPartnershipNodeDouble gcP = (PreviousNumberOfChildrenInPartnershipNodeDouble) gc;
                for (Node ggc : gcP.getChildren()) {
                    // ggc is of type NPCIAP
                    NumberOfPreviousChildrenInAnyPartnershipNodeDouble ggcN = (NumberOfPreviousChildrenInAnyPartnershipNodeDouble) ggc;

                    if (ggcN.getOption().hash() == option.hash()) {
                        count += ggcN.getCount();
                    }
                }
            }
        }
        return count;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public CTRow<Double> toCTRow() {

        if (initNode) {
            return null;
        } else {

            CTRow r = getParent().toCTRow();
            if (r != null) {
                r.setVariable(getVariableName(), getOption().toString());
            }
            return r;
        }
    }
}
