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
package uk.ac.standrews.cs.valipop.utils;

import org.apache.commons.math3.random.RandomGenerator;
import uk.ac.standrews.cs.valipop.Config;
import uk.ac.standrews.cs.valipop.statistics.distributions.InconsistentWeightException;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsTables.dataDistributions.*;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsTables.dataDistributions.selfCorrecting.MotherChildAdapter;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsTables.dataDistributions.selfCorrecting.SelfCorrectingOneDimensionDataDistribution;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsTables.dataDistributions.selfCorrecting.SelfCorrecting2DIntegerRangeProportionalDistribution;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.statsTables.dataDistributions.selfCorrecting.SelfCorrectingTwoDimensionDataDistribution;
import uk.ac.standrews.cs.valipop.utils.specialTypes.labeledValueSets.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Year;
import java.util.*;
import java.util.logging.Logger;

/**
 * Parses the input files into input data to form the distribution used by the simulation.
 * 
 * Supports reading data in several different formats.
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class InputFileReader {

    private static final String TAB = "\t";
    private static final String COMMENT_INDICATOR = "#";
    public static Logger log = Logger.getLogger(InputFileReader.class.getName());

    public static List<String> getAllLines(Path path) throws IOException {

        List<String> lines = new ArrayList<>();

        // Reads in all lines to a collection of Strings
        try (BufferedReader reader = Files.newBufferedReader(path)) {

            String line;
            while ((line = reader.readLine()) != null) {

                if (!line.startsWith(COMMENT_INDICATOR) && line.length() != 0) {
                    lines.add(line);
                }
            }
            reader.close();
        }

        return lines;
    }

    public static TreeMap<Year, Double> readInSingleInputFile(Path path) throws IOException, InvalidInputFileException {

        TreeMap<Year, Double> data = new TreeMap<>();

        List<String> lines = new ArrayList<>(getAllLines(path));

        for (int i = 0; i < lines.size(); i++) {

            String s = lines.get(i);
            String[] split = s.split(TAB);

            if (split[0].toLowerCase().equals("data")) {

                i++; // go to next line for data rows
                for (; i < lines.size(); i++) {
                    s = lines.get(i);
                    split = s.split(TAB);

                    try {
                        data.put(Year.parse(split[0]), Double.parseDouble(split[1]));

                    } catch (NumberFormatException e) {
                        throw new InvalidInputFileException("The year is of an incorrect form on line " + (i + 1) + " in the file: " + path.toString(), e);
                    }
                }
            }
        }

        return data;
    }

    public static SelfCorrectingTwoDimensionDataDistribution readInSC2DDataFile(Path path, Config config, RandomGenerator randomGenerator) throws IOException, InvalidInputFileException {

        List<String> lines = new ArrayList<>(getAllLines(path));

        Year year = null;
        String sourcePopulation = null;
        String sourceOrganisation = null;

        List<IntegerRange> columnLabels = new ArrayList<>();
        Map<IntegerRange, SelfCorrectingOneDimensionDataDistribution> data = new TreeMap<>();

        for (int i = 0; i < lines.size(); i++) {

            String s = lines.get(i);
            String[] split = s.split(TAB, 2);

            switch (split[0].toLowerCase()) {
                case "year":
                    try {
                        year = Year.parse(split[1]);
                    } catch (NumberFormatException e) {
                        throw new InvalidInputFileException("Non integer value given for year in file: " + path.toString(), e);
                    }
                    break;
                case "population":
                    sourcePopulation = split[1];
                    break;
                case "source":
                    sourceOrganisation = split[1];
                    break;
                case "labels":
                    columnLabels = readInLabels(split, path);
                    break;
                case "data":
                    i++; // go to next line for data rows
                    for (; i < lines.size(); i++) {
                        s = lines.get(i);
                        split = s.split(TAB);

                        if (split.length != columnLabels.size() + 1) {
                            throw new InvalidInputFileException("One or more data rows do not have the correct number of values in the file: " + path.toString());
                        }

                        IntegerRange rowLabel = null;
                        try {
                            rowLabel = new IntegerRange(split[0]);
                        } catch (NumberFormatException e) {
                            throw new InvalidInputFileException("The first column is of an incorrect form on line " + (i + 1) + " in the file: " + path.toString(), e);
                        } catch (InvalidRangeException e) {
                            throw new InvalidInputFileException("The first column specifies an invalid range on line " + (i + 1) + " in the file: " + path.toString(), e);
                        }

                        Map<IntegerRange, Double> rowMap = new TreeMap<>();

                        for (int j = 1; j < split.length; j++) {
                            try {
                                rowMap.put(columnLabels.get(j - 1), Double.parseDouble(split[j]));
                            } catch (NumberFormatException e) {
                                throw new InvalidInputFileException("The value in column " + j + " should be a Double on line " + (i + 1) + "in the file: " + path.toString(), e);
                            }
                        }

                        data.put(rowLabel,
                                new SelfCorrectingOneDimensionDataDistribution(year, sourcePopulation, sourceOrganisation, rowMap, config.getBinomialSampling(), randomGenerator)
                        );
                    }
                    break;
            }
        }

        return new SelfCorrectingTwoDimensionDataDistribution(year, sourcePopulation, sourceOrganisation, data);
    }

    public static ValiPopEnumeratedDistribution readInNameDataFile(Path path, RandomGenerator randomGenerator) throws IOException, InvalidInputFileException, InconsistentWeightException {

        List<String> lines = new ArrayList<>(getAllLines(path));

        Year year = null;
        String sourcePopulation = null;
        String sourceOrganisation = null;

        List<String> columnLabels = new ArrayList<>();
        Map<String, Double> data = new HashMap<>();

        for (int i = 0; i < lines.size(); i++) {

            String s = lines.get(i);
            String[] split = s.split(TAB, 2);

            switch (split[0].toLowerCase()) {
                case "year":
                    try {
                        year = Year.parse(split[1]);
                    } catch (NumberFormatException e) {
                        throw new InvalidInputFileException("Non integer value given for year in file: " + path.toString(), e);
                    }
                    break;
                case "population":
                    sourcePopulation = split[1];
                    break;
                case "source":
                    sourceOrganisation = split[1];
                    break;
                case "labels":
                    columnLabels = readInStringLabels(split);
                    break;
                case "data":
                    i++; // go to next line for data rows
                    for (; i < lines.size(); i++) {
                        s = lines.get(i);
                        split = s.split(TAB);

                        if (split.length != columnLabels.size()) {
                            throw new InvalidInputFileException("One or more data rows do not have the correct number of values in the file: " + path.toString());
                        }

                        String rowLabel = split[0];

                        data.put(rowLabel, Double.valueOf(split[1]));
                    }
                    break;
            }
        }

        return new ValiPopEnumeratedDistribution(year, sourcePopulation, sourceOrganisation, data, randomGenerator);
    }

    public static AgeDependantEnumeratedDistribution readInDeathCauseDataFile(Path path, RandomGenerator randomGenerator) throws IOException, InvalidInputFileException, InconsistentWeightException {

        List<String> lines = new ArrayList<>(getAllLines(path));

        Year year = null;
        String sourcePopulation = null;
        String sourceOrganisation = null;

        List<String> columnLabels = new ArrayList<>();
        Map<IntegerRange, LabelledValueSet<String, Double>> data = new TreeMap<>();

        for (int i = 0; i < lines.size(); i++) {

            String s = lines.get(i);
            String[] split = s.split(TAB, 2);

            switch (split[0].toLowerCase()) {
                case "year":
                    try {
                        year = Year.parse(split[1]);
                    } catch (NumberFormatException e) {
                        throw new InvalidInputFileException("Non integer value given for year in file: " + path.toString(), e);
                    }
                    break;
                case "population":
                    sourcePopulation = split[1];
                    break;
                case "source":
                    sourceOrganisation = split[1];
                    break;
                case "labels":
                    columnLabels = readInStringLabels(split);
                    break;
                case "data":
                    data = readIn2DDataTable(i, lines, path, columnLabels, StringToDoubleSet.class, IntegerRange.class, randomGenerator);
                    break;
            }
        }

        return new AgeDependantEnumeratedDistribution(year, sourcePopulation, sourceOrganisation, data, randomGenerator);
    }

    public static OneDimensionDataDistribution readIn1DDataFile(Path path) throws IOException, InvalidInputFileException {

        List<String> lines = new ArrayList<>(getAllLines(path));

        Year year = null;
        String sourcePopulation = null;
        String sourceOrganisation = null;

        Map<IntegerRange, Double> data = new TreeMap<>();

        for (int i = 0; i < lines.size(); i++) {

            String s = lines.get(i);
            String[] split = s.split(TAB);

            switch (split[0].toLowerCase()) {
                case "year":
                    try {
                        year = Year.parse(split[1]);
                    } catch (NumberFormatException e) {
                        throw new InvalidInputFileException("Non integer value given for year in file: " + path.toString(), e);
                    }
                    break;
                case "population":
                    sourcePopulation = split[1];
                    break;
                case "source":
                    sourceOrganisation = split[1];
                    break;
                case "data":
                    i++; // go to next line for data rows
                    for (; i < lines.size(); i++) {
                        s = lines.get(i);
                        split = s.split(TAB);

                        IntegerRange rowLabel = null;
                        try {
                            rowLabel = new IntegerRange(split[0]);
                        } catch (NumberFormatException e) {
                            throw new InvalidInputFileException("The label is of an incorrect form on line " + (i + 1) + " in the file: " + path.toString(), e);
                        } catch (InvalidRangeException e) {
                            throw new InvalidInputFileException("The label specifies an invalid range on line " + (i + 1) + "in the file: " + path.toString(), e);
                        }

                        data.put(rowLabel, Double.parseDouble(split[1]));
                    }
                    break;
            }
        }

        return new OneDimensionDataDistribution(year, sourcePopulation, sourceOrganisation, data);
    }

    public static SelfCorrectingOneDimensionDataDistribution readInSC1DDataFile(Path path, Config config, RandomGenerator randomGenerator) throws IOException, InvalidInputFileException {

        OneDimensionDataDistribution d = readIn1DDataFile(path);
        return new SelfCorrectingOneDimensionDataDistribution(
                d.getYear(), d.getSourcePopulation(), d.getSourceOrganisation(), d.cloneData(), config.getBinomialSampling(), randomGenerator);
    }

    public static SelfCorrecting2DIntegerRangeProportionalDistribution readInAgeAndProportionalStatsInput(Path path, RandomGenerator random) throws IOException, InvalidInputFileException {

        List<String> lines = new ArrayList<>(getAllLines(path));

        Year year = null;
        String sourcePopulation = null;
        String sourceOrganisation = null;

        List<IntegerRange> columnLabels = new ArrayList<>();
        Map<IntegerRange, LabelledValueSet<IntegerRange, Double>> data = new TreeMap<>();

        for (int i = 0; i < lines.size(); i++) {

            String s = lines.get(i);
            String[] split = s.split(TAB, 2);

            switch (split[0].toLowerCase()) {
                case "year":
                    try {
                        year = Year.parse(split[1]);
                    } catch (NumberFormatException e) {
                        throw new InvalidInputFileException("Non integer value given for year in file: " + path.toString(), e);
                    }
                    break;
                case "population":
                    sourcePopulation = split[1];
                    break;
                case "source":
                    sourceOrganisation = split[1];
                    break;
                case "labels":
                    columnLabels = readInLabels(split, path);
                    break;
                case "data":
                    data = readIn2DDataTable(i, lines, path, columnLabels, IntegerRangeToDoubleSet.class, IntegerRange.class, random);
                    break;
                default:
                    break;
            }
        }
        return new SelfCorrecting2DIntegerRangeProportionalDistribution(year, sourcePopulation, sourceOrganisation, data, random);
    }

    public static SelfCorrecting2DEnumeratedProportionalDistribution readInStringAndProportionalStatsInput(Path path, RandomGenerator random) throws IOException, InvalidInputFileException {

        List<String> lines = new ArrayList<>(getAllLines(path));

        Year year = null;
        String sourcePopulation = null;
        String sourceOrganisation = null;

        List<String> columnLabels = new ArrayList<>();
        Map<String, LabelledValueSet<String, Double>> data = new TreeMap<>();

        for (int i = 0; i < lines.size(); i++) {

            String s = lines.get(i);
            String[] split = s.split(TAB, 2);

            switch (split[0].toLowerCase()) {
                case "year":
                    try {
                        year = Year.parse(split[1]);
                    } catch (NumberFormatException e) {
                        throw new InvalidInputFileException("Non integer value given for year in file: " + path.toString(), e);
                    }
                    break;
                case "population":
                    sourcePopulation = split[1];
                    break;
                case "source":
                    sourceOrganisation = split[1];
                    break;
                case "labels":
                    columnLabels = readInStringLabels(split);
                    break;
                case "data":
                    data = readIn2DDataTable(i, lines, path, columnLabels, StringToDoubleSet.class, String.class, random);
                    break;
                default:
                    break;
            }
        }
        return new SelfCorrecting2DEnumeratedProportionalDistribution(year, sourcePopulation, sourceOrganisation, data, random);
    }

    public static SelfCorrectingProportionalDistribution<IntegerRange, Integer, Integer> readInAndAdaptAgeAndProportionalStatsInput(Path path, RandomGenerator random) throws IOException, InvalidInputFileException {

        List<String> lines = new ArrayList<>(getAllLines(path));

        Year year = null;
        String sourcePopulation = null;
        String sourceOrganisation = null;

        List<IntegerRange> columnLabels = new ArrayList<>();
        Map<IntegerRange, LabelledValueSet<IntegerRange, Double>> data = new TreeMap<>();

        for (int i = 0; i < lines.size(); i++) {

            String s = lines.get(i);
            String[] split = s.split(TAB, 2);

            switch (split[0].toLowerCase()) {
                case "year":
                    try {
                        year = Year.parse(split[1]);
                    } catch (NumberFormatException e) {
                        throw new InvalidInputFileException("Non integer value given for year in file: " + path.toString(), e);
                    }
                    break;
                case "population":
                    sourcePopulation = split[1];
                    break;
                case "source":
                    sourceOrganisation = split[1];
                    break;
                case "labels":
                    columnLabels = readInLabels(split, path);
                    break;
                case "data":
                    data = readIn2DDataTable(i, lines, path, columnLabels, IntegerRangeToDoubleSet.class, IntegerRange.class, random);
                    break;
                default:
                    break;
            }
        }
        return new MotherChildAdapter(year, sourcePopulation, sourceOrganisation, data, random);
    }

    private static List<IntegerRange> readInLabels(String[] split, Path path) throws InvalidInputFileException {

        List<IntegerRange> columnLabels = new ArrayList<>();
        String s = split[1];
        split = s.split(TAB);

        for (String l : split) {
            try {
                columnLabels.add(new IntegerRange(l));
            } catch (NumberFormatException e) {
                throw new InvalidInputFileException("A LABEL is of the incorrect form in the file: " + path.toString(), e);
            } catch (InvalidRangeException e) {
                throw new InvalidInputFileException("A LABEL specifies an invalid range in the file: " + path.toString(), e);
            }
        }

        return columnLabels;
    }

    private static List<String> readInStringLabels(String[] split) {

        List<String> columnLabels = new ArrayList<>();
        String s = split[1];
        split = s.split(TAB);

        Collections.addAll(columnLabels, split);

        return columnLabels;
    }

    private static <R, L, V extends Number> Map<R, LabelledValueSet<L, V>> readIn2DDataTable(
            int i, List<String> lines, Path path, List<L> columnLabels, Class<? extends AbstractLabelToAbstractValueSet<L, V>> setType, Class<R> rowType, RandomGenerator random) throws InvalidInputFileException {

        try {
            Map<R, LabelledValueSet<L, V>> data = new TreeMap<>();

            i++; // go to next line for data rows
            for (; i < lines.size(); i++) {
                String s = lines.get(i);
                String[] split = s.split(TAB);

                if (split.length != columnLabels.size() + 1) {
                    throw new InvalidInputFileException("One or more data rows do not have the correct number of values in the file: " + path.toString());
                }

                R rowLabel;
                try {
                    rowLabel = rowType.getConstructor(String.class).newInstance(split[0]);
                } catch (NumberFormatException e) {
                    throw new InvalidInputFileException("The first column is of an incorrect form on line " + (i + 1) + " in the file: " + path.toString(), e);
                } catch (InvalidRangeException e) {
                    throw new InvalidInputFileException("The first column specifies an invalid range on line " + (i + 1) + " in the file: " + path.toString(), e);
                }

                Map<L, V> rowMap = new TreeMap<>();

                Class<V> clazz = setType.getConstructor(RandomGenerator.class).newInstance(random).getValueClass();
                Constructor<V> constructor = clazz.getConstructor(String.class);

                for (int j = 1; j < split.length; j++) {
                    try {
                        rowMap.put(columnLabels.get(j - 1), constructor.newInstance(split[j]));

                    } catch (NumberFormatException e) {
                        throw new InvalidInputFileException("The value in column " + j + " should be a Double on line " + (i + 1) + "in the file: " + path.toString(), e);
                    }
                }

                data.put(rowLabel, setType.getConstructor(RandomGenerator.class).newInstance(random).init(rowMap));
            }
            return data;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);

        }
    }
}
