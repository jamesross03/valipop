/*
 * Copyright 2017 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module population_model.
 *
 * population_model is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * population_model is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with population_model. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.valipop.utils.sourceEventRecords.processingVisualiserFormat;

import uk.ac.standrews.cs.valipop.Config;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.PopulationStatistics;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class RelationshipsTable {

    final static ArrayList<String[]> relationshipsFather = new ArrayList<>();
    final static ArrayList<String[]> relationshipsMother = new ArrayList<>();
    final static ArrayList<String[]> relationshipsMarriage = new ArrayList<>();

    public static void outputData(Path recordsDirPath) throws IOException {

        toFile(recordsDirPath, "clean-relationships.txt");
        confuseTheData();

        toFile(recordsDirPath, "messy-relationships.txt");
    }

    private static void toFile(Path recordsDirPath, String fileName) throws IOException {

        Path path = recordsDirPath.resolve(fileName);
        Config.mkBlankFile(path);
        PrintStream ps = new PrintStream(path.toFile(), "UTF-8");

        for (String[] line : relationshipsMarriage) {
            ps.println(asString(line));
        }

        for (String[] line : relationshipsMother) {
            ps.println(asString(line));
        }

        for (String[] line : relationshipsFather) {
            ps.println(asString(line));
        }

        ps.close();
    }

    public static void confuseTheData() {

        ArrayList<String[]> fRels = deepCopy(relationshipsFather);
        swapPrimaryValues(fRels, 2);

        ArrayList<String[]> mRels = deepCopy(relationshipsMother);
        swapPrimaryValues(mRels, 2);

        ArrayList<String[]> marRels = deepCopy(relationshipsMarriage);
        swapPrimaryValues(marRels, 1);

        relationshipsFather.addAll(fRels);
        relationshipsMother.addAll(mRels);
        relationshipsMarriage.addAll(marRels);
    }

    private static void swapPrimaryValues(ArrayList<String[]> relations, int position) {

        ArrayList<String[]> used = new ArrayList<>();

        for (int i = 0; i < relations.size() - 1; i++) {
            int r = PopulationStatistics.randomGenerator.nextInt(relations.size());

            String[] swap = relations.remove(r);

            String[] line = relations.get(i);

            String principle = line[position];
            line[position] = swap[position];
            swap[position] = principle;

            swap[3] = String.valueOf(PopulationStatistics.randomGenerator.nextInt(81));
            line[3] = String.valueOf(PopulationStatistics.randomGenerator.nextInt(81));

            used.add(swap);
        }

        relations.addAll(used);
    }


    private static ArrayList<String[]> deepCopy(ArrayList<String[]> toCopy) {

        ArrayList<String[]> ret = new ArrayList<>();

        for (String[] s : toCopy) {
            String[] copy = new String[s.length];

            int c = 0;
            for (String a : s) {
                copy[c++] = a;
            }

            ret.add(copy);
        }

        return ret;
    }

    private static String asString(String[] line) {
        StringBuilder ret = new StringBuilder();

        boolean first = true;

        for (String s : line) {
            if (first) {
                ret.append(s);
                first = false;
            } else {
                ret.append("," + s);
            }
        }

        return ret.toString();
    }
}
