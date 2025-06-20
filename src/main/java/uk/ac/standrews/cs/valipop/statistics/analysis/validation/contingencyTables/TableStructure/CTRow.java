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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public abstract class CTRow<count extends Number> {

    private count count;

    public count getCount() {
        return count;
    }

    public void setCount(count count) {
        this.count = count;
    }

    CTCell[] c = {
            new CTCell("Source", ""),
            new CTCell("YOB", ""),
            new CTCell("Sex", ""),
            new CTCell("Age", ""),
            new CTCell("Died", ""),
            new CTCell("PNCIP", "0"),
            new CTCell("NPCIAP", "0"),
            new CTCell("CIY", "NO"),
            new CTCell("NCIY", "0"),
            new CTCell("NCIP", "0"),
            new CTCell("Separated", "NA"),
            new CTCell("NPA", "na")
    };

    protected Collection<CTCell> cells = new ArrayList<>(Arrays.asList(c));

    public Collection<CTCell> getCells() {
        return cells;
    }

    public CTCell getVariable(String variable) {

        for (CTCell cell : cells) {
            if (Objects.equals(variable, cell.getVariable())) {
                return cell;
            }
        }
        throw new RuntimeException("Cell not in row");
    }

    public void setVariable(String variable, String value) {

        try {
            getVariable(variable).setValue(value);
        } catch (RuntimeException e) {
            addVariable(variable, value);
        }
    }

    private void addVariable(String variable, String value) {
        cells.add(new CTCell(variable, value));
    }

    public void addDateVariable(int offset) {

        Integer yob = Integer.valueOf(getVariable("YOB").getValue());
        Integer age = Integer.valueOf(getVariable("Age").getValue());

        Integer date = yob + age + offset;

        addVariable("Date", String.valueOf(date));
    }

    public void addDateVariable() {
        addDateVariable(0);
    }

    public void deleteVariable(String variable) {
        try {
            cells.remove(getVariable(variable));
        } catch (RuntimeException e) {
            // this is okay - it's effectively deleted as it isn't there in the first place
        }
    }

    public abstract count combineCount(count a, count b);

    public String toString(String sep) {

        StringBuilder s = new StringBuilder();

        for (CTCell cell : cells) {
            s.append(cell.getValue()).append(sep);
        }

        s.append(getCount()).append("\n");

        return s.toString();
    }

    public String hash() {

        StringBuilder s = new StringBuilder();
        for (CTCell cell : cells) {
            s.append(cell.getVariable()).append(cell.getValue());
        }

        return s.toString();
    }

    public abstract int getIntegerCount();

    public abstract boolean countEqualToZero();

    public abstract boolean countGreaterThan(Double v);
}
