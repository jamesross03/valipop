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
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.ChildNotFoundException;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.ControlChildrenNode;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.ControlSelfNode;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.DoubleNode;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.Node;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.determinedCounts.MultipleDeterminedCountByIR;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.determinedCounts.SingleDeterminedCount;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsKeys.BirthStatsKey;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsKeys.MultipleBirthStatsKey;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRange;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.LabelledValueSet;

import java.time.LocalDate;
import java.time.Period;
import java.time.Year;
import java.time.temporal.ChronoUnit;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class ChildrenInYearNodeDouble extends DoubleNode<Boolean, Integer> implements ControlSelfNode, ControlChildrenNode {

    public ChildrenInYearNodeDouble(final Boolean option, final NumberOfPreviousChildrenInAnyPartnershipNodeDouble parentNode, final double initCount, final boolean init) {
        super(option, parentNode, initCount);

        if (!init) {
            calcCount();
        }
    }

    public ChildrenInYearNodeDouble() {
        super();
    }

    @Override
    public Node<Integer, ?, Double, ?> makeChildInstance(final Integer childOption, final Double initCount) {
        return new NumberOfChildrenInYearNodeDouble(childOption, this, initCount, false);
    }

    @Override
    public void processPerson(IPerson person, LocalDate currentDate) {

        incCountByOne();

        IPartnership activePartnership = PersonCharacteristicsIdentifier.getActivePartnership(person, currentDate);

        int option;

        if (activePartnership == null) {
            option = 0;
        } else {
            option = PersonCharacteristicsIdentifier.getChildrenBirthedInYear(activePartnership, Year.of(currentDate.getYear()));
        }

        try {
            getChild(option).processPerson(person, currentDate);
        } catch (ChildNotFoundException e) {
            NumberOfChildrenInYearNodeDouble n = (NumberOfChildrenInYearNodeDouble) addChild(new NumberOfChildrenInYearNodeDouble(option, this, 0.0, true));
            n.processPerson(person, currentDate);
        }
    }

    @Override
    public String getVariableName() {
        return "CIY";
    }

    @Override
    public void advanceCount() {
        makeChildren();
    }

    @Override
    public void calcCount() {

        Year yob = ((YOBNodeDouble) getAncestor(new YOBNodeDouble())).getOption();
        AgeNodeDouble aN = ((AgeNodeDouble) getAncestor(new AgeNodeDouble()));
        int age = aN.getOption().getValue();

        Integer order = ((PreviousNumberOfChildrenInPartnershipNodeDouble) getAncestor(new PreviousNumberOfChildrenInPartnershipNodeDouble())).getOption().getValue();
        LocalDate currentDate = LocalDate.of(yob.getValue(), 1, 1).plus(age - 1, ChronoUnit.YEARS);

        double forNPeople = ((AgeNodeDouble) getAncestor(new AgeNodeDouble())).getCount();

        Period timePeriod = Period.ofYears(1);

        SingleDeterminedCount sDC = (SingleDeterminedCount) getInputStats().getDeterminedCount(new BirthStatsKey(age, order, forNPeople, timePeriod, currentDate), null);

        double numberOfChildren = sDC.getRawUncorrectedCount();

        MultipleDeterminedCountByIR mDc = (MultipleDeterminedCountByIR) getInputStats().getDeterminedCount(new MultipleBirthStatsKey(age, numberOfChildren, timePeriod, currentDate), null);

        double numberOfMothers = mDc.getRawUncorrectedCount().getSumOfValues();

        NumberOfPreviousChildrenInAnyPartnershipNodeDouble parent = (NumberOfPreviousChildrenInAnyPartnershipNodeDouble) getParent();

        double numOfType = aN.sumOfNPCIAPDescendants(parent.getOption());

        double adjustment = parent.getCount() / numOfType;

        if (getOption()) {
            double v = numberOfMothers * adjustment;
            if (v > getParent().getCount() || Double.isNaN(v)) {
                v = getParent().getCount();
            }
            setCount(v);
        } else {
            double v = parent.getCount() - (numberOfMothers * adjustment);
            if (v < 0 || Double.isNaN(v)) {
                v = 0;
            }
            setCount(v);
        }

        if (!getOption() || getCount().equals(0.0)) {
            addChild(0, getCount());
        } else {

            LabelledValueSet<IntegerRange, Double> stat = mDc.getRawUncorrectedCount().reproportion();

            for (IntegerRange o : stat.getLabels()) {
                if (!stat.get(o).equals(0.0)) {
                    addChild(o.getValue(), stat.get(o) * getCount());
                }
            }
        }
    }

    @Override
    public void makeChildren() {

        if (!getOption() || getCount().equals(0.0)) {
            addChild(0);
        } else {

            Year yob = ((YOBNodeDouble) getAncestor(new YOBNodeDouble())).getOption();
            int age = ((AgeNodeDouble) getAncestor(new AgeNodeDouble())).getOption().getValue();

            LocalDate currentDate = getDateAtAge(yob, age);

            MultipleDeterminedCountByIR mDC = (MultipleDeterminedCountByIR) getInputStats().getDeterminedCount(new MultipleBirthStatsKey(age, getCount(), Period.ofYears(1), currentDate), null);

            LabelledValueSet<IntegerRange, Double> stat = mDC.getRawUncorrectedCount();

            for (IntegerRange o : stat.getLabels()) {
                if (!stat.get(o).equals(0.0)) {
                    addChild(o.getValue());
                }
            }
        }
    }
}
