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
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.*;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SexOption;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.determinedCounts.MultipleDeterminedCountByIR;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsKeys.MultipleBirthStatsKey;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRange;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.LabelledValueSet;

import java.time.LocalDate;
import java.time.Period;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class NumberOfChildrenInYearNodeDouble extends DoubleNode<Integer, IntegerRange> implements ControlSelfNode, ControlChildrenNode, RunnableNode {

    private Collection<IPerson> people = new ArrayList<>();

    public NumberOfChildrenInYearNodeDouble(Integer option, ChildrenInYearNodeDouble parentNode, Double initCount, boolean init) {
        super(option, parentNode, initCount);

        if (!init) {
            advanceCount();
            makeChildren();
        }
    }

    public NumberOfChildrenInYearNodeDouble() {
        super();
    }

    @Override
    public Node<IntegerRange, ?, Double, ?> makeChildInstance(IntegerRange childOption, Double initCount) {
        return new NumberOfChildrenInPartnershipNodeDouble(childOption, this, initCount, false);
    }

    @Override
    public void processPerson(IPerson person, LocalDate currentDate) {

        people.add(person);

        incCountByOne();

        int prevChildren = ((PreviousNumberOfChildrenInPartnershipNodeDouble) getAncestor(new PreviousNumberOfChildrenInPartnershipNodeDouble())).getOption().getValue();

        int childrenThisYear = ((NumberOfChildrenInYearNodeDouble) getAncestor(new NumberOfChildrenInYearNodeDouble())).getOption();
        int ncip = prevChildren + childrenThisYear;
        IntegerRange range = resolveToChildRange(ncip);

        try {
            getChild(range).processPerson(person, currentDate);
        } catch (ChildNotFoundException e) {

            addChild(new NumberOfChildrenInPartnershipNodeDouble(range, this, 0.0, true)).processPerson(person, currentDate);
        }
    }

    @Override
    public String getVariableName() {
        return "NCIY";
    }

    @Override
    public void advanceCount() {

        // Should we be restricting this so much?
        if (getCount() > CTtree.NODE_MIN_COUNT && getOption() != 0) {

            Year yob = ((YOBNodeDouble) getAncestor(new YOBNodeDouble())).getOption();
            int age = ((AgeNodeDouble) getAncestor(new AgeNodeDouble())).getOption().getValue();

            Year currentDate = getYearAtAge(yob, age);

            SourceNodeDouble sN = (SourceNodeDouble) getAncestor(new SourceNodeDouble());

            YOBNodeDouble yobN;
            try {
                yobN = (YOBNodeDouble) sN.getChild(currentDate);
            } catch (ChildNotFoundException e) {
                yobN = (YOBNodeDouble) sN.addChild(currentDate);
            }

            double sexRatio = getInputStats().getMaleProportionOfBirths(currentDate);

            for (Node<SexOption, ?, Double, ?> n : yobN.getChildren()) {

                SexNodeDouble sexN = (SexNodeDouble) n;
                double adjCount;

                double childrenFromThisNode = getCount() * getOption();

                if (sexN.getOption() == SexOption.MALE) {
                    adjCount = childrenFromThisNode * sexRatio;
                } else { // i.e. if female
                    adjCount = childrenFromThisNode * (1 - sexRatio);
                }

                try {
                    sexN.getChild(new IntegerRange(0)).incCount(adjCount);
                } catch (ChildNotFoundException e) {
                    AgeNodeDouble aN = new AgeNodeDouble(new IntegerRange(0), sexN, adjCount, true);
                    sexN.addChild(aN);
                    addDelayedTask(aN);
                }
            }
        }
    }

    @Override
    public void calcCount() {

        if (getOption() == 0) {
            setCount(getParent().getCount());
        } else {

            Year yob = ((YOBNodeDouble) getAncestor(new YOBNodeDouble())).getOption();
            int age = ((AgeNodeDouble) getAncestor(new AgeNodeDouble())).getOption().getValue();

            LocalDate currentDate = getDateAtAge(yob, age);

            MultipleDeterminedCountByIR mDC = (MultipleDeterminedCountByIR) getInputStats().getDeterminedCount(new MultipleBirthStatsKey(age, getParent().getCount(), Period.ofYears(1), currentDate), null);

            LabelledValueSet<IntegerRange, Double> stat = mDC.getRawUncorrectedCount();

            for (IntegerRange iR : stat.getLabels()) {
                if (iR.contains(getOption())) {
                    setCount(stat.get(iR));
                }
            }
        }

        advanceCount();
        makeChildren();
    }

    @Override
    public void makeChildren() {

        int numberOfPrevChildInPartnership = ((PreviousNumberOfChildrenInPartnershipNodeDouble) getAncestor(new PreviousNumberOfChildrenInPartnershipNodeDouble())).getOption().getValue();
        int childrenInYear = getOption();

        int numberOfChildInPartnership = numberOfPrevChildInPartnership + childrenInYear;
        IntegerRange range = resolveToChildRange(numberOfChildInPartnership);

        addChild(range, getCount());
    }

    @Override
    public void run() {
        advanceCount();
    }

    @SuppressWarnings("Duplicates")
    private IntegerRange resolveToChildRange(Integer ncip) {

        for (Node<IntegerRange, ?, ?, ?> aN : getChildren()) {
            if (aN.getOption().contains(ncip)) {
                return aN.getOption();
            }
        }

        Year yob = ((YOBNodeDouble) getAncestor(new YOBNodeDouble())).getOption();
        int age = ((AgeNodeDouble) getAncestor(new AgeNodeDouble())).getOption().getValue();

        Year currentDate = getYearAtAge(yob, age);

        Collection<IntegerRange> sepRanges = getInputStats().getSeparationByChildCountRates(currentDate).getColumnLabels();

        for (IntegerRange o : sepRanges) {
            if (o.contains(ncip)) {
                return o;
            }
        }

        if (ncip == 0) {
            return new IntegerRange(0);
        }

        throw new Error("Did not resolve any permissable ranges");
    }
}
