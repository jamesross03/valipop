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
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TableStructure.PersonCharacteristicsIdentifier;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.ChildNotFoundException;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.IntNode;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.Node;

import java.time.LocalDate;
import java.time.Year;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class ChildrenInYearNodeInt extends IntNode<Boolean, Integer> {

    public ChildrenInYearNodeInt(final Boolean option, final NumberOfPreviousChildrenInAnyPartnershipNodeInt parentNode, final int initCount) {
        super(option, parentNode, initCount);
    }

    @Override
    public void processPerson(final IPerson person, final LocalDate currentDate) {

        incCountByOne();

        final IPartnership activePartnership = PersonCharacteristicsIdentifier.getActivePartnership(person, currentDate);

        final int option = activePartnership == null ? 0 : PersonCharacteristicsIdentifier.getChildrenBirthedInYear(activePartnership, Year.of(currentDate.getYear()));

        try {
            getChild(option).processPerson(person, currentDate);
        } catch (ChildNotFoundException e) {
            addChild(option).processPerson(person, currentDate);
        }
    }

    @Override
    public String getVariableName() {
        return "CIY";
    }

    @Override
    public Node<Integer, ?, Integer, ?> makeChildInstance(final Integer childOption, final Integer initCount) {
        return new NumberOfChildrenInYearNodeInt(childOption, this, initCount);
    }
}
