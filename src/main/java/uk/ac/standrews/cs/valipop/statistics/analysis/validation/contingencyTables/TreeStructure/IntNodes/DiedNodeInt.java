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

import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TableStructure.CTRow;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TableStructure.PersonCharacteristicsIdentifier;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.ChildNotFoundException;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.IntNode;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.Node;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SexOption;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRange;

import java.time.LocalDate;
import java.time.Year;
import java.util.Collection;
import java.util.List;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class DiedNodeInt extends IntNode<Boolean, IntegerRange> {

    public DiedNodeInt(final Boolean option, final AgeNodeInt parentNode, final int initCount) {
        super(option, parentNode, initCount);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void processPerson(final IPerson person, final LocalDate currentDate) {

        incCountByOne();

        if (person.getSex() == SexOption.FEMALE) {

            final IPartnership partnership = PersonCharacteristicsIdentifier.getActivePartnership(person, currentDate);
            final int numberOfChildren = partnership != null ? PersonCharacteristicsIdentifier.getChildrenBirthedBeforeDate(partnership, currentDate) : 0;

            final IntegerRange range = resolveToChildRange(numberOfChildren);

            try {
                getChild(range).processPerson(person, currentDate);
            } catch (final ChildNotFoundException e) {
                addChild(range).processPerson(person, currentDate);
            }
        }
    }

    @Override
    public Node<IntegerRange, ?, Integer, ?> makeChildInstance(final IntegerRange childOption, final Integer initCount) {

        return new PreviousNumberOfChildrenInPartnershipNodeInt(childOption, this, initCount);
    }

    private IntegerRange resolveToChildRange(final int numberOfChildren) {

        for (final Node<IntegerRange, ?, ?, ?> node : getChildren())
            if (node.getOption().contains(numberOfChildren))
                return node.getOption();

        final Year yob = ((YOBNodeInt) getAncestor(new YOBNodeInt())).getOption();
        final int age = ((AgeNodeInt) getAncestor(new AgeNodeInt())).getOption().getValue();

        final Year currentDate = Year.of(yob.getValue() + age);

        for (final IntegerRange range : getInputStats().getSeparationByChildCountRates(currentDate).getColumnLabels())
            if (range.contains(numberOfChildren))
                return range;

        if (numberOfChildren == 0) return new IntegerRange(0);

        throw new Error("Did not resolve any permissible ranges");
    }

    public List<String> toStringAL() {

        final List<String> s = getParent().toStringAL();
        s.add(getOption().toString());
        s.add(getCount().toString());
        return s;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public CTRow<Integer> toCTRow() {

        final CTRow r = getParent().toCTRow();
        r.setVariable(getVariableName(), getOption().toString());
        r.setCount(getCount());
        return r;
    }

    @Override
    public String getVariableName() {
        return "Died";
    }
}
