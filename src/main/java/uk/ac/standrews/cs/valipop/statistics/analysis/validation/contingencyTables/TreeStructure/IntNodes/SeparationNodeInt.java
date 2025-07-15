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
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.IntNode;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.Node;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SeparationOption;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.determinedCounts.MultipleDeterminedCountByIR;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsKeys.PartneringStatsKey;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRange;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.InvalidRangeException;

import java.time.LocalDate;
import java.time.Period;
import java.time.Year;
import java.util.Collection;
import java.util.Set;

import static uk.ac.standrews.cs.valipop.simulationEntities.PopulationNavigation.ageOnDate;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class SeparationNodeInt extends IntNode<SeparationOption, IntegerRange> {

    public SeparationNodeInt(final SeparationOption option, final NumberOfChildrenInPartnershipNodeInt parentNode, final int initCount) {
        super(option, parentNode, initCount);
    }

    @Override
    public void processPerson(final IPerson person, final LocalDate currentDate) {

        incCountByOne();

        final IPartnership activePartnership = PersonCharacteristicsIdentifier.getActivePartnership(person, currentDate);

        Integer newPartnerAge = null;

        if (activePartnership != null && PersonCharacteristicsIdentifier.startedInYear(activePartnership, Year.of(currentDate.getYear()))) {
            final IPerson partner = activePartnership.getPartnerOf(person);
            newPartnerAge = ageOnDate(partner, activePartnership.getPartnershipDate());
        }

        // check if the partner falls into one of the child ranges

        for (final Node<IntegerRange, ?, Integer, ?> node : getChildren()) {

            Boolean in = null;

            if (newPartnerAge != null)
                try {
                    in = node.getOption().contains(newPartnerAge);
                } catch (final InvalidRangeException e) {
                    in = null;
                }

            // if partners age is in the considered range then process this person using this NPA range and return
            if (in != null && in) {
                node.processPerson(person, currentDate);
                return;
            }

            // if in is null due to range being 'na' and there is no new partner (thus NPA == null) then process this person using the current NPA range (na)
            if (newPartnerAge == null) {
                node.processPerson(person, currentDate);
                return;
            }
        }

        // if we get here then the age range we want hasn't been created yet

        if (newPartnerAge == null) {
            // if no NPA then a 'na' range hasn't been created yet - so we create it
            addChild(new IntegerRange("na")).processPerson(person, currentDate);

        } else {

            // this accessing of the statistical code isn't to calculate new values - we just use it to get the age
            // ranges from the stats tables
            final int age = ((AgeNodeInt) getAncestor(new AgeNodeInt())).getOption().getValue();

            final double numberOfFemales = getCount();
            final Period timePeriod = Period.ofYears(1);

            final MultipleDeterminedCountByIR mDC = (MultipleDeterminedCountByIR) getInputStats().getDeterminedCount(new PartneringStatsKey(age, numberOfFemales, timePeriod, currentDate), null);

            // getting the age range labels
            final Set<IntegerRange> options = mDC.getRawUncorrectedCount().getLabels();

            // finding which the persons partner is in and creating it
            for (final IntegerRange o : options) {
                if (o.contains(newPartnerAge)) {
                    addChild(o).processPerson(person, currentDate);
                    return;
                }
            }
        }
    }

    @Override
    public String getVariableName() {
        return "Separated";
    }

    @Override
    public Node<IntegerRange, ?, Integer, ?> makeChildInstance(final IntegerRange childOption, final Integer initCount) {
        return new NewPartnerAgeNodeInt(childOption, this, initCount);
    }
}
