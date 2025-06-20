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
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.*;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SeparationOption;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.determinedCounts.MultipleDeterminedCountByIR;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.determinedCounts.SingleDeterminedCount;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsKeys.PartneringStatsKey;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsKeys.SeparationStatsKey;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRange;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.InvalidRangeException;

import java.time.LocalDate;
import java.time.Period;
import java.time.Year;
import java.util.Set;

import static uk.ac.standrews.cs.valipop.simulationEntities.PopulationNavigation.ageOnDate;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class SeparationNodeDouble extends DoubleNode<SeparationOption, IntegerRange> implements ControlSelfNode, ControlChildrenNode, RunnableNode {

    public SeparationNodeDouble(SeparationOption option, NumberOfChildrenInPartnershipNodeDouble parentNode, Double initCount, boolean init) {

        super(option, parentNode, initCount);

        if (!init) {
            calcCount();
            makeChildren();
        }
    }

    @Override
    public Node<IntegerRange, ?, Double, ?> makeChildInstance(IntegerRange childOption, Double initCount) {
        return new NewPartnerAgeNodeDouble(childOption, this, initCount, false);
    }

    @Override
    public void processPerson(IPerson person, LocalDate currentDate) {

        incCountByOne();

        IPartnership activePartnership = PersonCharacteristicsIdentifier.getActivePartnership(person, currentDate);

        Integer newPartnerAge = null;

        if (activePartnership != null && PersonCharacteristicsIdentifier.startedInYear(activePartnership, Year.of(currentDate.getYear()))) {
            IPerson partner = activePartnership.getPartnerOf(person);
            newPartnerAge = ageOnDate(partner, activePartnership.getPartnershipDate());
        }

        // check if the partner falls into one of the child ranges
        for (Node<IntegerRange, ?, Double, ?> node : getChildren()) {

            Boolean in;
            try {
                in = node.getOption().contains(newPartnerAge);
            } catch (InvalidRangeException | NullPointerException e) {
                in = null;
            }

            // if partners age is in the considered range then process this person using this NPA range and return
            if (in != null && in) {
                node.processPerson(person, currentDate);
                return;
            }

            // if in is null due to range being 'na' and there is no new partner (thus NPA == null) then process this person using the current NPA range (na)
            if (newPartnerAge == null && in == null) {
                node.processPerson(person, currentDate);
                return;
            }
        }

        // if we get here then the age range we want hasn't been created yet

        if (newPartnerAge == null) {

            // if no NPA then a 'na' range hasn't been created yet - so we create it
            NewPartnerAgeNodeDouble node = new NewPartnerAgeNodeDouble(new IntegerRange("na"), this, 0.0, true);
            addChild(node).processPerson(person, currentDate);

        } else {

            // this accessing of the statistical code isn't to calculate new values - we just use it to get the age
            // ranges from the stats tables
            Integer age = ((AgeNodeDouble) getAncestor(new AgeNodeDouble())).getOption().getValue();

            double numberOfFemales = getCount();
            Period timePeriod = Period.ofYears(1);

            MultipleDeterminedCountByIR mDC = (MultipleDeterminedCountByIR) getInputStats().getDeterminedCount(new PartneringStatsKey(age, numberOfFemales, timePeriod, currentDate), null);

            // getting the age range labels
            Set<IntegerRange> options = mDC.getRawUncorrectedCount().getLabels();

            // finding which the persons partner is in and creating it
            for (IntegerRange range : options) {
                if (range.contains(newPartnerAge)) {

                    NewPartnerAgeNodeDouble node = new NewPartnerAgeNodeDouble(range, this, 0.0, true);
                    addChild(node).processPerson(person, currentDate);
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
    public void advanceCount() {

        DiedNodeDouble diedN = (DiedNodeDouble) getAncestor(new DiedNodeDouble());
        boolean died = diedN.getOption();

        Year yob = ((YOBNodeDouble) getAncestor(new YOBNodeDouble())).getOption();
        int age = ((AgeNodeDouble) getAncestor(new AgeNodeDouble())).getOption().getValue();

        Year currentDate = getYearAtAge(yob, age);

        if (!died && currentDate.isBefore( Year.of(getEndDate().getYear())) && diedN.getCount() > CTtree.NODE_MIN_COUNT) {

            SexNodeDouble s = (SexNodeDouble) getAncestor(new SexNodeDouble());

            AgeNodeDouble a;
            try {
                a = (AgeNodeDouble) s.resolveChildNodeForAge(age + 1);
            } catch (ChildNotFoundException e) {
                throw new Error("Age Node should have already been created");
            }

            // Split count between branches

            for (Node<Boolean, ?, Double, ?> n : a.getChildren()) {

                DiedNodeDouble d = (DiedNodeDouble) n;

                double partOfCount = getCount() * d.getCount() / a.getCount();

                IntegerRange prevNumberOfChildrenInPartnership = ((NumberOfChildrenInPartnershipNodeDouble) getAncestor(new NumberOfChildrenInPartnershipNodeDouble())).getOption();

                // Move over to correct IntegerRange object with the same value at age + 1
                prevNumberOfChildrenInPartnership = d.resolveToChildRange(prevNumberOfChildrenInPartnership.getValue());

                PreviousNumberOfChildrenInPartnershipNodeDouble pncip;

                try {
                    pncip = (PreviousNumberOfChildrenInPartnershipNodeDouble) d.getChild(prevNumberOfChildrenInPartnership);
                } catch (ChildNotFoundException e) {
                    pncip = (PreviousNumberOfChildrenInPartnershipNodeDouble) d.addChild(prevNumberOfChildrenInPartnership);
                }

                pncip.incCount(partOfCount);

                IntegerRange numberOfPrevChildrenInAnyPartnership = ((NumberOfPreviousChildrenInAnyPartnershipNodeDouble) getAncestor(new NumberOfPreviousChildrenInAnyPartnershipNodeDouble())).getOption();

                int childrenInYear = ((NumberOfChildrenInYearNodeDouble) getAncestor(new NumberOfChildrenInYearNodeDouble())).getOption();

                int numberOfChildrenInAnyPartnership = numberOfPrevChildrenInAnyPartnership.getValue() + childrenInYear;

                NumberOfPreviousChildrenInAnyPartnershipNodeDouble nciap;

                IntegerRange rangeNCIAP = pncip.resolveToChildRange(numberOfChildrenInAnyPartnership);

                try {
                    nciap = (NumberOfPreviousChildrenInAnyPartnershipNodeDouble) pncip.getChild(rangeNCIAP);
                } catch (ChildNotFoundException e) {
                    nciap = (NumberOfPreviousChildrenInAnyPartnershipNodeDouble) pncip.addChild(rangeNCIAP);
                    addDelayedTask(nciap);
                }

                nciap.incCount(partOfCount);
            }
        }
    }

    @Override
    public void calcCount() {

        if (getOption() == SeparationOption.NA) {
            setCount(getParent().getCount());
        } else {

            IntegerRange numberOfChildrenInPartnership = ((NumberOfChildrenInPartnershipNodeDouble) getAncestor(new NumberOfChildrenInPartnershipNodeDouble())).getOption();

            if (numberOfChildrenInPartnership.getValue() == 0) {
                setCount(getParent().getCount());

            } else {

                double forNPeople = getParent().getCount();
                Period timePeriod = Period.ofYears(1);

                Year yob = ((YOBNodeDouble) getAncestor(new YOBNodeDouble())).getOption();
                int age = ((AgeNodeDouble) getAncestor(new AgeNodeDouble())).getOption().getValue();

                LocalDate currentDate = getDateAtAge(yob, age);

                SingleDeterminedCount sDC = (SingleDeterminedCount) getInputStats().getDeterminedCount(
                        new SeparationStatsKey(numberOfChildrenInPartnership.getValue(), age, forNPeople, timePeriod, currentDate), null);

                if (getOption() == SeparationOption.YES) {
                    setCount(sDC.getRawUncorrectedCount());
                } else {
                    setCount(forNPeople - sDC.getRawUncorrectedCount());
                }
            }
        }

        advanceCount();
    }

    @Override
    public void makeChildren() {

        // WHY DO WE NEVER CONSIDER THE SEPARATION STATUS IN THIS NODE? SURELY IT SHOULD IMPACT ON THE CHILDREN?
        // Answer: No, you need to remember that the New Partner Age is to do with the person who children have been
        // had with in the current year (NOT the next partner who will be moved onto). The fact this node comes
        // beneath the 'Separation' node in the tree is a misnoma, this age does not pertain to the partner who will
        // be moved onto post separation.
        // Also it is possible for there to partner ages below both the YES and NO separation nodes as you may
        // get a female who had no children entering the year (PNCIP = 0) who then has children (CIY = YES) who thus
        // has a new partner (thus NPA will be set) but who also separates from the partner in the same year
        // (Separation = YES).

        IntegerRange ncip = ((NumberOfChildrenInPartnershipNodeDouble) getAncestor(new NumberOfChildrenInPartnershipNodeDouble())).getOption();

        if (ncip.getValue() == 0) { // i.e. no current partner and no children in year, therefore no NPA as no NP
            addChild(new IntegerRange("na"));

        } else {
            boolean ciy = ((ChildrenInYearNodeDouble) getAncestor(new ChildrenInYearNodeDouble())).getOption();

            if (!ciy) { // if no children in year then by definition no new partner can exit - thus no NPA
                addChild(new IntegerRange("na"));

            } else {

                IntegerRange pncip = ((PreviousNumberOfChildrenInPartnershipNodeDouble) getAncestor(new PreviousNumberOfChildrenInPartnershipNodeDouble())).getOption();

                // at this point we know this partnership has borne children, paired with the knowledge of if there has
                // been any previous children in this partnership we can decide

                if (pncip.getValue() != 0) { // if it is an ongoing partnership - then no NP and thus no NPA
                    addChild(new IntegerRange("na"));
                } else {
                    // or is a new partnership - thus record NPA

                    Year yob = ((YOBNodeDouble) getAncestor(new YOBNodeDouble())).getOption();
                    int age = ((AgeNodeDouble) getAncestor(new AgeNodeDouble())).getOption().getValue();

                    LocalDate currentDate = getDateAtAge(yob, age);

                    double numberOfFemales = getCount();
                    Period timePeriod = Period.ofYears(1);

                    MultipleDeterminedCountByIR mDC = (MultipleDeterminedCountByIR) getInputStats().getDeterminedCount(new PartneringStatsKey(age, numberOfFemales, timePeriod, currentDate), null);

                    Set<IntegerRange> options = mDC.getRawUncorrectedCount().getLabels();

                    for (IntegerRange range : options) {
                        addChild(range);
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        calcCount();
    }
}
