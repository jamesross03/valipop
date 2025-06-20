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

import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TableStructure.PersonCharacteristicsIdentifier;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.*;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRange;

import java.time.LocalDate;
import java.time.Year;


/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class NumberOfPreviousChildrenInAnyPartnershipNodeDouble extends DoubleNode<IntegerRange, Boolean> implements RunnableNode, ControlChildrenNode {

    public NumberOfPreviousChildrenInAnyPartnershipNodeDouble(IntegerRange option, PreviousNumberOfChildrenInPartnershipNodeDouble parentNode, Double initCount) {
        super(option, parentNode, initCount);
    }

    public NumberOfPreviousChildrenInAnyPartnershipNodeDouble() {
        super();
    }

    @Override
    public Node<Boolean, ?, Double, ?> makeChildInstance(Boolean childOption, Double initCount) {
        return new ChildrenInYearNodeDouble(childOption, this, initCount, false);
    }

    @Override
    public void processPerson(IPerson person, LocalDate currentDate) {

        incCountByOne();

        IPartnership activePartnership = PersonCharacteristicsIdentifier.getActivePartnership(person, currentDate);

        boolean option;

        if (activePartnership == null) {
            option = false;
        } else {
            option = PersonCharacteristicsIdentifier.getChildrenBirthedInYear(activePartnership, Year.of(currentDate.getYear())) != 0;
        }

        try {
            getChild(option).processPerson(person, currentDate);

        } catch (ChildNotFoundException e) {
            addChild(new ChildrenInYearNodeDouble(option, this, 0.0, true)).processPerson(person, currentDate);
        }
    }

    @Override
    public String getVariableName() {
        return "NPCIAP";
    }

    @Override
    public void run() {
        makeChildren();
    }

    @Override
    public void makeChildren() {

        addChild(true);
        addChild(false);
    }
}
