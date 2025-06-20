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
package uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TableInstances;

import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TableStructure.CTRow;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TableStructure.CTtable;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.CTtree;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.Node;

import java.util.Objects;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class CTtablePart extends CTtable {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public CTtablePart(CTtree tree) {

        for (Node n : tree.getLeafNodes()) {
            CTRow leaf = n.toCTRow();

            if (leaf != null && leaf.getCount() != null) {
                try {
                    leaf.addDateVariable();

                    if (Objects.equals(leaf.getVariable("Sex").getValue(), "F")) {
                        leaf.deleteVariable("Sex");

                        leaf.deleteVariable("Died");
                        leaf.deleteVariable("PNCIP");
                        leaf.deleteVariable("NPCIAP");
                        leaf.deleteVariable("CIY");
                        leaf.deleteVariable("NCIP");
                        leaf.deleteVariable("Separated");
                        leaf.deleteVariable("NCIY");


                        CTRow h = table.get(leaf.hash());

                        if (h == null) {
                            table.put(leaf.hash(), leaf);
                        } else {
                            h.setCount(h.combineCount(h.getCount(), leaf.getCount()));
                        }
                    }

                } catch (RuntimeException e) {
                    // Unfilled row - thus pass
                }
            }
        }
    }
}
