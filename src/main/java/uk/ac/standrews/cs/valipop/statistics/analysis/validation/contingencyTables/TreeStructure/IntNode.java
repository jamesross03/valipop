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
public abstract class IntNode<Op extends Comparable<Op>, cOp extends Comparable<cOp>> extends Node<Op, cOp, Integer, Integer> {

    @SuppressWarnings("unchecked")
    public IntNode(final Op option, @SuppressWarnings("rawtypes") final Node parentNode, final int initCount) {
        super(option, parentNode, initCount);
    }

    public IntNode() {

    }

    @SuppressWarnings("unchecked")
    public IntNode(final Op option, @SuppressWarnings("rawtypes") final Node parentNode) {
        super(option, parentNode, 0);
    }

    @Override
    public void incCount(final Integer byCount) {
        setCount(getCount() + byCount);
    }

    @Override
    public void incCountByOne() {
        setCount(getCount() + 1);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public Node<cOp, ?, Integer, ?> addChild(final cOp childOption, final Integer initCount) {

        Node<cOp, ?, Integer, ?> child;

        try {
            child = getChild(childOption);
            child.incCount(initCount);
        }
        catch (final ChildNotFoundException e)  {
            child = makeChildInstance(childOption, initCount);
            super.addChild(child);
        }

        return child;
    }

    @Override
    public Node<cOp, ?, Integer, ?> addChild(final cOp childOption) {
        return addChild(childOption, 0);
    }

    public abstract Node<cOp, ?, Integer, ?> makeChildInstance(cOp childOption, Integer initCount);
}
