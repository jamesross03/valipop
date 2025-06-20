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
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TableStructure.CTRow;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.*;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SexOption;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.determinedCounts.SingleDeterminedCount;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsKeys.DeathStatsKey;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRange;

import java.time.LocalDate;
import java.time.Period;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static uk.ac.standrews.cs.valipop.simulationEntities.PopulationNavigation.getPartnershipsActiveInYear;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class DiedNodeDouble extends DoubleNode<Boolean, IntegerRange> implements ControlSelfNode, RunnableNode, ControlChildrenNode {

    DiedNodeDouble(Boolean option, AgeNodeDouble parentNode, boolean init) {
        super(option, parentNode);

        if (!init) {
            calcCount();

            Integer age = ((AgeNodeDouble) getAncestor(new AgeNodeDouble())).getOption().getValue();
            if (age == 0) {
                makeChildren();
            }
        }
    }

    DiedNodeDouble() {
        super();
    }

    @Override
    public void advanceCount() {

        Year yob = ((YOBNodeDouble) getAncestor(new YOBNodeDouble())).getOption();
        int age = ((AgeNodeDouble) getAncestor(new AgeNodeDouble())).getOption().getValue();

        Year currentDate = getYearAtAge(yob, age);

        if (!getOption() && currentDate.isBefore(Year.of(getEndDate().getYear())) && getCount() > CTtree.NODE_MIN_COUNT) {

            SexNodeDouble sN = (SexNodeDouble) getAncestor(new SexNodeDouble());
            IntegerRange ageR = new IntegerRange(age + 1);

            try {
                sN.getChild(ageR).incCount(getCount());
            } catch (ChildNotFoundException e) {
                sN.addChild(ageR, getCount());
            }
        }
    }

    @Override
    public void calcCount() {

        Year yob = ((YOBNodeDouble) getAncestor(new YOBNodeDouble())).getOption();
        int age = ((AgeNodeDouble) getAncestor(new AgeNodeDouble())).getOption().getValue();

        LocalDate currentDate = getDateAtAge(yob, age);

        double forNPeople = getParent().getCount();
        Period timePeriod = Period.ofYears(1);

        SexOption sexOption = (SexOption) getAncestor(new SexNodeDouble()).getOption();

        SingleDeterminedCount rDC = (SingleDeterminedCount) getInputStats().getDeterminedCount(new DeathStatsKey(age, forNPeople, timePeriod, currentDate, sexOption), null);

        if (getOption()) {
            setCount(rDC.getRawUncorrectedCount());
        } else {
            setCount(forNPeople - rDC.getRawUncorrectedCount());
        }

        advanceCount();
    }

    @Override
    public void processPerson(IPerson person, LocalDate currentDate) {

        incCountByOne();

        if (person.getSex() == SexOption.FEMALE) {
            List<IPartnership> partnershipsInYear = new ArrayList<>(getPartnershipsActiveInYear(person, Year.of(currentDate.getYear())));

            if (partnershipsInYear.size() == 0) {
                IntegerRange range = resolveToChildRange(0);
                try {
                    getChild(range).processPerson(person, currentDate);
                } catch (ChildNotFoundException e) {
                    addChild(range).processPerson(person, currentDate);
                }
            } else if (partnershipsInYear.size() == 1) {

                IPartnership partnership = partnershipsInYear.remove(0);
                int numberOfChildren = partnership.getChildren().size();
                IntegerRange range = resolveToChildRange(numberOfChildren);

                try {
                    getChild(range).processPerson(person, currentDate);
                } catch (ChildNotFoundException e) {
                    addChild(range).processPerson(person, currentDate);
                }
            } else {
                throw new RuntimeException("Woman in too many partnerships in year");
            }
        }
    }

    @Override
    public Node<IntegerRange, ?, Double, ?> makeChildInstance(IntegerRange childOption, Double initCount) {
        return new PreviousNumberOfChildrenInPartnershipNodeDouble(childOption, this, initCount);
    }

    @Override
    public void run() {
        advanceCount();
    }

    @Override
    public void makeChildren() {

        SexOption sex = ((SexNodeDouble) getAncestor(new SexNodeDouble())).getOption();

        if (sex == SexOption.FEMALE) {

            Year yob = ((YOBNodeDouble) getAncestor(new YOBNodeDouble())).getOption();

            Collection<IntegerRange> ranges = getInputStats().getOrderedBirthRates(yob).getColumnLabels();

            for (IntegerRange iR : ranges) {
                if (iR.contains(0)) {
                    PreviousNumberOfChildrenInPartnershipNodeDouble pncip = new PreviousNumberOfChildrenInPartnershipNodeDouble(iR, this, getCount());

                    addChild(pncip);

                    NumberOfPreviousChildrenInAnyPartnershipNodeDouble npciap = (NumberOfPreviousChildrenInAnyPartnershipNodeDouble) pncip.makeChildInstance(new IntegerRange(0), getCount());

                    pncip.addChild(npciap);
                    addDelayedTask(npciap);

                    break;
                }
            }
        }
    }

    @SuppressWarnings("Duplicates")
    IntegerRange resolveToChildRange(Integer pncip) {

        for (Node<IntegerRange, ?, ?, ?> aN : getChildren()) {
            if (aN.getOption().contains(pncip)) {
                return aN.getOption();
            }
        }

        Year yob = ((YOBNodeDouble) getAncestor(new YOBNodeDouble())).getOption();
        int age = ((AgeNodeDouble) getAncestor(new AgeNodeDouble())).getOption().getValue();

        Year currentDate = getYearAtAge(yob, age);

        Collection<IntegerRange> sepRanges = getInputStats().getSeparationByChildCountRates(currentDate).getColumnLabels();

        for (IntegerRange range : sepRanges) {
            if (range.contains(pncip)) {
                return range;
            }
        }

        if (pncip == 0) {
            return new IntegerRange(0);
        }

        throw new RuntimeException("Did not resolve any permissible ranges");
    }

    public List<String> toStringAL() {
        List<String> s = getParent().toStringAL();
        s.add(getOption().toString());
        s.add(getCount().toString());
        return s;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public CTRow<Double> toCTRow() {
        CTRow r = getParent().toCTRow();

        if (r != null) {
            r.setVariable(getVariableName(), getOption().toString());
            r.setCount(getCount());
        }

        return r;
    }

    @Override
    public String getVariableName() {
        return "Died";
    }
}
