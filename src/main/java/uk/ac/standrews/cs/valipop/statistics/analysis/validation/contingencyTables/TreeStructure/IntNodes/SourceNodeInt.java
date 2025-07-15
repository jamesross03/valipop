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
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TableStructure.CTRow;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TableStructure.CTRowInt;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.*;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class SourceNodeInt extends IntNode<SourceType, Year> {

    @SuppressWarnings("rawtypes")
    private Node parent;

    public SourceNodeInt(final SourceType option, final CTtree parent) {
        super(option, parent);
        this.parent = parent;
    }

    @SuppressWarnings("rawtypes")
    public Node getAncestor(final Node nodeType) {

        if (nodeType.getClass().isInstance(this)) {
            return this;
        } else if (nodeType.getClass().isInstance(parent)) {
            return parent;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Node<Year, ?, Integer, ?> makeChildInstance(final Year childOption, final Integer initCount) {
        return new YOBNodeInt(childOption, this, initCount);
    }

    public void addDelayedTask(final RunnableNode node) {
        parent.addDelayedTask(node);
    }

    public void processPerson(final IPerson person, final LocalDate currentDate) {

        // increase own count
        incCountByOne();

        // pass person to appropriate child node
        final Year yearAfterBirth = Year.of(person.getBirthDate().getYear() + 1);

        try {
            getChild(yearAfterBirth).processPerson(person, currentDate);
        }
        catch (final ChildNotFoundException e) {
            addChild(yearAfterBirth).processPerson(person, currentDate);
        }
    }

    public ArrayList<String> toStringAL() {
        final ArrayList<String> s = new ArrayList<>();
        s.add(getOption().toString());
        return s;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public CTRow<Integer> toCTRow() {
        final CTRow r = new CTRowInt();
        r.setVariable(getVariableName(), getOption().toString());
        return r;
    }

    @Override
    public String getVariableName() {
        return "Source";
    }

    public ArrayList<String> getVariableNamesAL() {
        final ArrayList<String> s = new ArrayList<>();
        s.add(getClass().getName());
        return s;
    }
}
