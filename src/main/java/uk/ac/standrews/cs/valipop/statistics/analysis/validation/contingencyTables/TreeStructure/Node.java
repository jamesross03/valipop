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
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TableStructure.CTRow;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.PopulationStatistics;

import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public abstract class Node<Op extends Comparable<Op>, cOp extends Comparable<cOp>, Count extends Number, ChildCount extends Number> {

    private Count count;
    private Op option;
    private Map<cOp, Node<cOp, ?, ChildCount, ?>> children = new TreeMap<>();
    private Node<?, Op, ?, Count> parent;

    public Node() {
    }

    public Node(final Op option, final Node<?, Op, ?, Count> parent, final Count initCount) {
        this.option = option;
        this.parent = parent;
        this.count = initCount;
    }

    public abstract Node<cOp, ?, ChildCount, ?> addChild(final cOp childOption, final ChildCount initCount);

    public abstract Node<cOp, ?, ChildCount, ?> addChild(final cOp childOption);

    public abstract void incCount(final Count byCount);

    public abstract void incCountByOne();

    public abstract void processPerson(final IPerson person, final LocalDate currentDate);

    public Node<cOp, ?, ChildCount, ?> addChild(Node<cOp, ?, ChildCount, ?> child) {

        children.put(child.getOption(), child);
        return child;
    }

    @SuppressWarnings("rawtypes")
    public Collection<Node> getLeafNodes() {

        Collection<Node> childNodes = new ArrayList<>();

        if (getChildren().size() == 0) {
            return Collections.singleton(this);
        } else {

            for (Node<cOp, ?, ChildCount, ?> n : getChildren()) {
                childNodes.addAll(n.getLeafNodes());
            }
        }

        return childNodes;
    }

    public List<String> toStringAL() {
        List<String> s = getParent().toStringAL();
        s.add(getOption().toString());
        return s;
    }

    @SuppressWarnings("unchecked")
    public CTRow<Count> toCTRow() {
        CTRow<Count> r = (CTRow<Count>) getParent().toCTRow();
        if (r != null) {
            r.setVariable(getVariableName(), getOption().toString());
        }
        return r;
    }

    public abstract String getVariableName();

    public void setCount(final Count count) {
        this.count = count;
    }

    public Op getOption() {
        return option;
    }

    public Count getCount() {
        return count;
    }

    public Collection<Node<cOp, ?, ChildCount, ?>> getChildren() {
        return children.values();
    }

    public Node<cOp, ?, ChildCount, ?> getChild(final cOp childOption) throws ChildNotFoundException {

        Node<cOp, ?, ChildCount, ?> n = children.get(childOption);

        if (n == null) {
            throw new ChildNotFoundException();
        }

        return n;
    }

    public Node<?, Op, ?, Count> getParent() {
        return parent;
    }

    public void addDelayedTask(final RunnableNode node) {
        getParent().addDelayedTask(node);
    }

    @SuppressWarnings("rawtypes")
    public Node getAncestor(final Node nodeType) {

        if (nodeType.getClass().isInstance(this)) {
            return this;
        } else {
            return getParent().getAncestor(nodeType);
        }
    }

    public PopulationStatistics getInputStats() {
        return getAncestor(new CTtree()).getInputStats();
    }

    public LocalDate getStartDate() {
        return getAncestor(new CTtree()).getStartDate();
    }

    public LocalDate getEndDate() {
        return getAncestor(new CTtree()).getEndDate();
    }

    @SuppressWarnings({ "unused", "rawtypes" })
    private int printDescent() {

        int depth = 0;

        Node p = getParent();
        if (p != null) {
            depth = p.printDescent() + 1;
        }

        for (int i = 0; i < depth; i++) {
            System.out.print(" ");
        }

        System.out.print(this.getClass().getSimpleName());

        if (option == null) {
            System.out.print(" | Op: null");
        } else {
            System.out.print(" | Op: " + option.toString());
        }

        if (count == null) {
            System.out.print(" | Count: null");
        } else {
            System.out.print(" | Count: " + count);
        }

        System.out.println();

        return depth;
    }

    protected Year getYearAtAge(Year yearOfBirth, int age) {

        return Year.of(yearOfBirth.getValue()).plus(age, ChronoUnit.YEARS);
    }

    protected LocalDate getDateAtAge(Year yearOfBirth, int age) {

        return LocalDate.of(yearOfBirth.getValue(), 1, 1).plus(age, ChronoUnit.YEARS);
    }
}
