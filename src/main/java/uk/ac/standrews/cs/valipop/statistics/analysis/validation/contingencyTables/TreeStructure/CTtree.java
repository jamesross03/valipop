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
package uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure;

import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.simulationEntities.PopulationNavigation;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.DoubleNodes.*;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.IntNodes.SourceNodeInt;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.PopulationStatistics;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class CTtree extends Node<String, SourceType, Number, Number> {

    public static final Logger log = Logger.getLogger(CTtree.class.getName());

    private final LinkedList<RunnableNode> deathTasks = new LinkedList<>();
    private final LinkedList<RunnableNode> ageTasks = new LinkedList<>();
    private final LinkedList<RunnableNode> nciyTasks = new LinkedList<>();
    private final LinkedList<RunnableNode> nciapTasks = new LinkedList<>();
    private final LinkedList<RunnableNode> sepTasks = new LinkedList<>();

    public static double NODE_MIN_COUNT = 1E-66;

    private PopulationStatistics expected;

    private LocalDate endDate;
    private LocalDate startDate;

    private SourceNodeInt simNode;
    private SourceNodeDouble statNode = null;

    public CTtree(final Iterable<IPerson> population, final PopulationStatistics expected, final LocalDate startDate, final LocalDate zeroDate, final LocalDate endDate, final int startStepBack, final double precision) {

        CTtree.NODE_MIN_COUNT = precision;

        this.expected = expected;
        this.startDate = startDate;
        this.endDate = endDate.minusYears(1);

        log.info("CTree --- Populating tree with observed population");

        for (LocalDate year = startDate; year.isBefore(endDate.minusYears(1)); year = year.plusYears(1)) {

            final LocalDate firstYearToProcess = zeroDate.minusYears(startStepBack);
            final LocalDate lastDayOfPreviousYear = LocalDate.of(year.getYear() - 1, 12, 31);

            for (final IPerson person : population) {

                if (PopulationNavigation.aliveOnDate(person, lastDayOfPreviousYear) && PopulationNavigation.inCountryOnDate(person, lastDayOfPreviousYear)) {

                    if (year.getYear() == firstYearToProcess.getYear())
                        processPerson(person, year, SourceType.STAT);

                    if (year.getYear() >= firstYearToProcess.getYear())
                        processPerson(person, year, SourceType.SIM);
                }
            }
        }

        executeDelayedTasks();

        log.info("CTree --- Tree completed");
    }

    public CTtree() {
    }

    @SuppressWarnings("rawtypes")
    public Collection<Node> getLeafNodes() {

        final Collection<Node> childNodes = new ArrayList<>();

        childNodes.addAll(simNode.getLeafNodes());
        childNodes.addAll(statNode.getLeafNodes());

        return childNodes;
    }

    @Override
    public String getVariableName() {
        return null;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public PopulationStatistics getInputStats() {
        return expected;
    }

    @SuppressWarnings("rawtypes")
    private Node addChildA(final SourceType childOption) {

        if (childOption == SourceType.SIM) {
            simNode = new SourceNodeInt(childOption, this);
            return simNode;
        } else {
            statNode = new SourceNodeDouble(childOption, this);
            return statNode;
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Node getChild(final SourceType option) throws ChildNotFoundException {

        if (option == SourceType.SIM)
            if (simNode != null) return simNode;
            else throw new ChildNotFoundException();

        else
            if (statNode != null) return statNode;
            else throw new ChildNotFoundException();
    }

    @Override
    public void addDelayedTask(final RunnableNode node) {

        if (node instanceof DiedNodeDouble) {
            deathTasks.add(node);
        } else if (node instanceof AgeNodeDouble) {
            ageTasks.add(node);
        } else if (node instanceof NumberOfChildrenInYearNodeDouble) {
            nciyTasks.add(node);
        } else if (node instanceof NumberOfPreviousChildrenInAnyPartnershipNodeDouble) {
            nciapTasks.add(node);
        } else if (node instanceof SeparationNodeDouble) {
            sepTasks.add(node);
        }
    }

    private void executeDelayedTasks() {

        log.info("CTree --- Initialising tree - death nodes from seed");

        while (!deathTasks.isEmpty()) {

            final RunnableNode n = deathTasks.removeFirst();
            n.run();
        }

        while (nciyTasks.size() + sepTasks.size() + nciapTasks.size() + ageTasks.size() != 0) {

            while (nciyTasks.size() + sepTasks.size() + nciapTasks.size() != 0) {

                while (!sepTasks.isEmpty()) {

                    final RunnableNode n = sepTasks.removeFirst();
                    n.run();
                }

                while (!nciapTasks.isEmpty()) {

                    final RunnableNode n = nciapTasks.removeFirst();
                    n.run();
                }
            }

            for (int i = 0; i < 2; i++) {
                if (ageTasks.isEmpty()) break;

                final RunnableNode n = ageTasks.removeFirst();
                final AgeNodeDouble a = (AgeNodeDouble) n;
                final YOBNodeDouble y = (YOBNodeDouble) a.getAncestor(new YOBNodeDouble());
                log.info("CTree --- Creating nodes for year: " + y.getOption().toString());
                n.run();
            }
        }
    }

    public void processPerson(final IPerson person, final LocalDate currentDate, final SourceType source) {

        try {
            getChild(source).processPerson(person, currentDate);
        }
        catch (final ChildNotFoundException e) {
            addChildA(source).processPerson(person, currentDate);
        }
    }

    @Override
    public Node<SourceType, ?, Number, ?> addChild(final SourceType childOption) {
        return null;
    }

    @Override
    public Node<SourceType, ?, Number, ?> addChild(final SourceType childOption, final Number initCount) {
        return null;
    }

    @Override
    public void processPerson(final IPerson person, final LocalDate currentDate) {

    }

    @Override
    public void incCount(final Number byCount) {

    }

    @Override
    public void incCountByOne() {

    }
}
