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
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SexOption;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRange;

import java.time.LocalDate;

import static uk.ac.standrews.cs.valipop.simulationEntities.PopulationNavigation.ageOnDate;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class SexNodeDouble extends DoubleNode<SexOption, IntegerRange> {

    public SexNodeDouble(SexOption option, YOBNodeDouble parentNode, double initCount) {
        super(option, parentNode, initCount);
    }

    public SexNodeDouble() {
        super();
    }

    @Override
    public Node<IntegerRange, ?, Double, ?> makeChildInstance(IntegerRange childOption, Double initCount) {
        try {
            return resolveChildNodeForAge(childOption.getValue());
        } catch (ChildNotFoundException e) {
            return new AgeNodeDouble(childOption, this, initCount, false);
        }
    }

    @Override
    public void processPerson(IPerson person, LocalDate currentDate) {

        int age = ageOnDate(person, currentDate);

        try {
            resolveChildNodeForAge(age).processPerson(person, currentDate);
        } catch (ChildNotFoundException e) {
            addChild(new AgeNodeDouble(new IntegerRange(age), this, 0, true)).processPerson(person, currentDate);
        }
    }

    @Override
    public String getVariableName() {
        return "Sex";
    }

    Node<IntegerRange, ?, Double, ?> resolveChildNodeForAge(Integer age) throws ChildNotFoundException {

        if(age != null) {
            for (Node<IntegerRange, ?, Double, ?> aN : getChildren()) {
                if (aN.getOption().contains(age)) {
                    return aN;
                }
            }
        }
        throw new ChildNotFoundException();
    }

    public Node<IntegerRange, ?, Double, ?> getChild(IntegerRange childOption) throws ChildNotFoundException {

        return resolveChildNodeForAge(childOption.getValue());
    }
}
