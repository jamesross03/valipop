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

import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TableStructure.CTCell;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TableStructure.CTRow;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TableStructure.CTRowInt;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TableStructure.CTtable;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.*;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.IntNodes.*;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.IntegerRange;

import java.io.PrintStream;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;


/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class CTtableFull extends CTtable {

    @SuppressWarnings({ "rawtypes", "unused" })
    public CTtableFull(CTtree tree, PrintStream ps, int zeroAdjustValue) {

        Iterator<Node> leafs = tree.getLeafNodes().iterator();

        boolean first = true;

        while (leafs.hasNext()) {
            Node node = leafs.next();
            CTRow leaf = node.toCTRow();

            if (leaf != null) {

                if (first) {
                    ps.print(getVarNames(",", leaf));
                    first = false;
                }

                if (leaf.getCount() != null && leaf.countGreaterThan(0.5)) {

                    ps.print(leaf.toString(","));

                    if (Objects.equals(leaf.getVariable("Source").getValue(), "STAT")) {

                        Year year = Year.parse(leaf.getVariable("YOB").getValue());
                        SexOption sex;
                        switch (leaf.getVariable("Sex").getValue()) {
                            case "MALE":
                                sex = SexOption.MALE;
                                break;
                            case "FEMALE":
                                sex = SexOption.FEMALE;
                                break;
                            default:
                                throw new Error();
                        }

                        IntegerRange age = new IntegerRange(leaf.getVariable("Age").getValue());

                        boolean died;
                        switch (leaf.getVariable("Died").getValue()) {
                            case "YES":
                                died = true;
                                break;
                            case "NO":
                                died = false;
                                break;
                            default:
                                throw new Error();
                        }

                        IntegerRange pncip = new IntegerRange(leaf.getVariable("PNCIP").getValue());
                        IntegerRange npciap = new IntegerRange(leaf.getVariable("NPCIAP").getValue());

                        boolean ciy;
                        switch (leaf.getVariable("CIY").getValue()) {
                            case "YES":
                                ciy = true;
                                break;
                            case "NO":
                                ciy = false;
                                break;
                            default:
                                throw new Error();
                        }

                        int nciy = Integer.parseInt(leaf.getVariable("NCIY").getValue());
                        IntegerRange ncip = new IntegerRange(leaf.getVariable("NCIP").getValue());

                        SeparationOption sep;
                        switch (leaf.getVariable("Separated").getValue()) {
                            case "YES":
                                sep = SeparationOption.YES;
                                break;
                            case "NO":
                                sep = SeparationOption.NO;
                                break;
                            case "NA":
                                sep = SeparationOption.NA;
                                break;
                            default:
                                throw new Error();
                        }

                        IntegerRange npa = new IntegerRange(leaf.getVariable("NPA").getValue());

                        try {
                            SourceNodeInt sN = (SourceNodeInt) tree.getChild(SourceType.SIM);
                            YOBNodeInt yobN = (YOBNodeInt) sN.getChild(year);
                            SexNodeInt sexN = (SexNodeInt) yobN.getChild(sex);
                            AgeNodeInt ageN = (AgeNodeInt) sexN.getChild(age);
                            DiedNodeInt diedN = (DiedNodeInt) ageN.getChild(died);
                            PreviousNumberOfChildrenInPartnershipNodeInt pncipN = (PreviousNumberOfChildrenInPartnershipNodeInt) diedN.getChild(pncip);
                            NumberOfPreviousChildrenInAnyPartnershipNodeInt npciapN = (NumberOfPreviousChildrenInAnyPartnershipNodeInt) pncipN.getChild(npciap);
                            ChildrenInYearNodeInt ciyN = (ChildrenInYearNodeInt) npciapN.getChild(ciy);
                            NumberOfChildrenInYearNodeInt nciyN = (NumberOfChildrenInYearNodeInt) ciyN.getChild(nciy);
                            NumberOfChildrenInPartnershipNodeInt ncipN = (NumberOfChildrenInPartnershipNodeInt) nciyN.getChild(ncip);
                            SeparationNodeInt sepN = (SeparationNodeInt) ncipN.getChild(sep);
                            NewPartnerAgeNodeInt npaN = (NewPartnerAgeNodeInt) sepN.getChild(npa);

                            // If we got here then this node exists - we don't need to add it
                        } catch (ChildNotFoundException e) {
                            // If we couldn't find the node then we need to add it

                            CTCell[] cells = {
                                    new CTCell("Source", "SIM"),
                                    new CTCell("YOB", String.valueOf(year.getValue())),
                                    new CTCell("Sex", sex.toString()),
                                    new CTCell("Age", age.toString()),
                                    new CTCell("Died", String.valueOf(died)),
                                    new CTCell("PNCIP", pncip.toString()),
                                    new CTCell("NPCIAP", npciap.toString()),
                                    new CTCell("CIY", String.valueOf(ciy)),
                                    new CTCell("NCIY", String.valueOf(nciy)),
                                    new CTCell("NCIP", ncip.toString()),
                                    new CTCell("Separated", sep.toString()),
                                    new CTCell("NPA", npa.toString())
                            };

                            CTRowInt r = new CTRowInt(new ArrayList<>(Arrays.asList(cells)));
                            r.setCount(zeroAdjustValue);

                            ps.print(r.toString(","));
                        }
                    }
                }
            }
        }

        ps.close();
    }

    @SuppressWarnings("rawtypes")
    private String getVarNames(String sep, CTRow row) {

        StringBuilder s = new StringBuilder();

        for (Object cell : row.getCells()) {

            s.append(((CTCell) cell).getVariable() + sep);
        }

        s.append("freq\n");

        return s.toString();
    }
}
