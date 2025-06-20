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
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.IntNode;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.Node;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRange;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class NewPartnerAgeNodeInt extends IntNode<IntegerRange, String> {

    public NewPartnerAgeNodeInt(IntegerRange option, SeparationNodeInt parentNode, Integer initCount) {
        super(option, parentNode, initCount);
    }

    @Override
    public void processPerson(IPerson person, LocalDate currentDate) {
        incCountByOne();
    }

    @Override
    public Node<String, ?, Integer, ?> makeChildInstance(String childOption, Integer initCount) {
        return null;
    }

    public List<String> toStringAL() {
        List<String> s = getParent().toStringAL();
        s.add(getOption().toString());
        s.add(getCount().toString());
        return s;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public CTRow<Integer> toCTRow() {
        CTRow r = getParent().toCTRow();

        if(getOption() == null) {
            r.setVariable(getVariableName(), "na");
        } else {
            r.setVariable(getVariableName(), getOption().toString());
        }

        r.setCount(getCount());

        return r;
    }

    @Override
    public String getVariableName() {
        return "NPA";
    }
}
