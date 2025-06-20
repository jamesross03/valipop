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

import java.util.Collection;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class CTRowInt extends CTRow<Integer> {

    public CTRowInt() {}

    public CTRowInt(Collection<CTCell> cells) {
        this.cells = cells;
    }

    @Override
    public Integer combineCount(Integer a, Integer b) {
        return a + b;
    }

    @Override
    public int getIntegerCount() {
        return getCount();
    }

    @Override
    public boolean countEqualToZero() {
        return getCount() == 0;
    }

    @Override
    public boolean countGreaterThan(Double v) {
        return true;
    }
}
