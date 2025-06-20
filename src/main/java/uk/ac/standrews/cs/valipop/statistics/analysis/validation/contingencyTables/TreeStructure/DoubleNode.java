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

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public abstract class DoubleNode<Op extends Comparable<Op>, cOp extends Comparable<cOp>> extends Node<Op, cOp, Double, Double> {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public DoubleNode(final Op option, final Node parentNode, final double initCount) {
        super(option, parentNode, initCount);
    }

    @SuppressWarnings("rawtypes")
    public DoubleNode(final Op option, final Node parentNode) {
        this(option, parentNode, 0);
    }

    public DoubleNode() {

    }

    @Override
    public void incCount(final Double byCount) {

        if (getCount() == null) {
            setCount(byCount);
        } else {
            setCount(getCount() + byCount);
        }
    }

    @Override
    public void incCountByOne() {

        setCount(getCount() + 1);
    }

    public void setCount(final double count) {
        super.setCount(count);
    }

    @SuppressWarnings("unchecked")
    public Node<?, Op, Double, Double> getParent() {
        return (Node<?, Op, Double, Double>) super.getParent();
    }

    @SuppressWarnings("Duplicates")
    @Override
    public Node<cOp, ?, Double, ?> addChild(final cOp childOption, final Double initCount) {

        Node<cOp, ?, Double, ?> child;

        try {
            child = getChild(childOption);
            child.incCount(initCount);
        } catch (ChildNotFoundException e) {
            child = makeChildInstance(childOption, initCount);
            super.addChild(child);
        }

        return child;
    }

    @Override
    public Node<cOp, ?, Double, ?> addChild(final cOp childOption) {
        return addChild(childOption, 0.0);
    }

    public abstract Node<cOp, ?, Double, ?> makeChildInstance(final cOp childOption, final Double initCount);
}
