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
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SexOption;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRange;

import java.time.LocalDate;

import static uk.ac.standrews.cs.valipop.simulationEntities.PopulationNavigation.ageOnDate;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)=
 */
public class SexNodeInt extends IntNode<SexOption, IntegerRange> {

    public SexNodeInt(SexOption option, YOBNodeInt parentNode, int initCount) {
        super(option, parentNode, initCount);
    }

    @Override
    public Node<IntegerRange, ?, Integer, ?> makeChildInstance(IntegerRange childOption, Integer initCount) {
        return new AgeNodeInt(childOption, this, initCount);
    }


    @Override
    public void processPerson(IPerson person, LocalDate currentDate) {

        incCountByOne();

        int age = ageOnDate(person, currentDate);

        try {
            resolveChildNodeForAge(age).processPerson(person, currentDate);
        } catch (ChildNotFoundException e) {
            addChild(new IntegerRange(age)).processPerson(person, currentDate);

        }
    }

    @Override
    public String getVariableName() {
        return "Sex";
    }

    private Node<IntegerRange, ?, ?, ?> resolveChildNodeForAge(int age) throws ChildNotFoundException {

        for(Node<IntegerRange, ?, ?, ?> aN : getChildren()) {
            if(aN.getOption().contains(age)) {
                return aN;
            }
        }

        throw new ChildNotFoundException();
    }
}
