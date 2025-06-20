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
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.ChildNotFoundException;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.ControlChildrenNode;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.DoubleNode;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.Node;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SexOption;

import java.time.LocalDate;
import java.time.Year;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class YOBNodeDouble extends DoubleNode<Year, SexOption> implements ControlChildrenNode {

    public YOBNodeDouble(Year childOption, SourceNodeDouble parentNode, Double initCount) {
        super(childOption, parentNode, initCount);
        makeChildren();
    }

    public YOBNodeDouble() {
        super();
    }

    @Override
    public void processPerson(IPerson person, LocalDate currentDate) {

        try {
            getChild(person.getSex()).processPerson(person, currentDate);
        } catch (ChildNotFoundException e) {
            addChild(person.getSex()).processPerson(person, currentDate);
        }
    }

    @Override
    public String getVariableName() {
        return "YOB";
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public CTRow<Double> toCTRow() {
        CTRow r = getParent().toCTRow();
        r.setVariable(getVariableName(), Integer.toString(getOption().getValue()));
        return r;
    }

    @Override
    public Node<SexOption, ?, Double, ?> makeChildInstance(SexOption childOption, Double initCount) {
        return new SexNodeDouble(childOption, this, initCount);
    }

    @Override
    public void makeChildren() {

        addChild(SexOption.MALE);
        addChild(SexOption.FEMALE);
    }
}
