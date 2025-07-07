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
package uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TableStructure;

import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SeparationOption;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.PopulationStatistics;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import static uk.ac.standrews.cs.valipop.simulationEntities.PopulationNavigation.bornInYear;
import static uk.ac.standrews.cs.valipop.simulationEntities.PopulationNavigation.getPartnershipsActiveInYear;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class PersonCharacteristicsIdentifier {

    public static int getImmigrantGeneration(final IPerson person) {

        if(person.getImmigrationDate() != null) {
            return 1;
        } else {

            if(person.getParents() == null) {
                return -1;
            } else {

                final int fathersIG = getImmigrantGeneration(person.getParents().getMalePartner());
                final int mothersIG = getImmigrantGeneration(person.getParents().getFemalePartner());

                if (fathersIG == -1 && mothersIG == -1)
                    return -1;

                if (fathersIG == -1)
                    return mothersIG + 1;

                if (mothersIG == -1)
                    return fathersIG + 1;

                if (fathersIG < mothersIG)
                    return fathersIG + 1;
                else
                    return mothersIG + 1;
            }
        }
    }

    public static IPartnership getActivePartnership(final IPerson person, final LocalDate currentDate) {

        final List<IPartnership> partnershipsInYear = new ArrayList<>(getPartnershipsActiveInYear(person, Year.of(currentDate.getYear())));

        if (partnershipsInYear.size() > 1) {
            throw new RuntimeException("Lots of partners in year - likely for a female to get this error");
        } else if (partnershipsInYear.isEmpty()) {
            return null;
        } else {
//            return partnershipsInYear.getFirst();
            return partnershipsInYear.get(0);
        }
    }

    public static int getChildrenBirthedInYear(final IPartnership activePartnership, final Year year) {

        int count = 0;

        for (final IPerson child : activePartnership.getChildren()) {
            if (bornInYear(child, year)) {
                count++;
            }
        }

        return count;
    }

    public static int getChildrenBirthedBeforeDate(final IPartnership activePartnership, final LocalDate year) {

        int count = 0;

        for (final IPerson child : activePartnership.getChildren()) {
            if (child.getBirthDate().isBefore( year)) {
                count++;
            }
        }

        return count;
    }

    public static SeparationOption toSeparate(final IPartnership activePartnership, final Year year) {

        if (activePartnership == null) {
            return SeparationOption.NA;
        }

        final List<IPerson> children = activePartnership.getChildren();
        final IPerson lastChild = children.get(children.size() - 1);
//        final IPerson lastChild = children.getLast();

        if (!bornInYear(lastChild, year)) {
            return SeparationOption.NO;
        } else if (activePartnership.getSeparationDate(PopulationStatistics.randomGenerator) != null) { // TODO Would this be better to use earliest possible sep date?
            return SeparationOption.YES;
        } else {
            return SeparationOption.NO;
        }
    }

    public static boolean startedInYear(final IPartnership activePartnership, final Year year) {

        return activePartnership.getPartnershipDate().getYear() == year.getValue();
    }
}
