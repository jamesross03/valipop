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

import java.io.PrintStream;
import java.util.*;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public abstract class CTtable {

    @SuppressWarnings("rawtypes")
    protected Map<String, CTRow> table = new HashMap<>();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void outputToFile(PrintStream ps) throws NoTableRowsException {

        @SuppressWarnings("unused")
        int simZeroFreqs = 0;
        @SuppressWarnings("unused")
        int statZeroFreqs = 0;
        ps.print(getVarNames());

        for (CTRow row : table.values()) {

            // TODO paramaterise this?
            if (row.countGreaterThan(0.0001)) {

                ps.print(row.toString(","));

                Collection<CTCell> cells = row.getCells();

                CTRowInt twin = new CTRowInt(cells);

                if (Objects.equals(row.getVariable("Source").getValue(), "STAT")) {

                    twin.setVariable("Source", "SIM");
                    CTRow t = table.get(twin.hash());

                    if (t == null) {
                        simZeroFreqs++;
                    }

                } else {

                    twin.setVariable("Source", "STAT");
                    CTRow t = table.get(twin.hash());

                    if (t == null) {
                        statZeroFreqs++;
                    }
                }
            }
        }

        ps.close();
    }

    @SuppressWarnings("rawtypes")
    private String getVarNames() throws NoTableRowsException {

        ArrayList<String> keys = new ArrayList<>(table.keySet());
        if (keys.size() == 0) {
            throw new NoTableRowsException();
        }

        CTRow row = table.get(keys.get(0));

        StringBuilder s = new StringBuilder();

        for (Object cell : row.getCells()) {

            s.append(((CTCell) cell).getVariable()).append(",");
        }

        s.append("freq\n");

        return s.toString();
    }
}
