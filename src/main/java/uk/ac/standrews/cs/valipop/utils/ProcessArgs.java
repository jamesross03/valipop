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

import java.security.InvalidParameterException;
import java.util.Objects;

/**
 * Utility functions for parsing process arguments.
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class ProcessArgs {

    public static String[] process(String[] args, String executionType) {

        String[] processed = new String[args.length];

        try {
            processed[0] = args[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("No config file given as 1st arg");
        }


        if (executionType.equals("MINIMA_SEARCH") || executionType.equals("N-RUNS") || executionType.equals("FACTOR_SEARCH") || executionType.equals("FACTOR_SEARCH_PRECISION")) {
            try {
                processed[1] = args[1];
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("No results write path given as 2nd arg");
            }

            try {
                processed[2] = args[2];
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("No run purpose given as 3rd arg");
            }

            try {
                processed[3] = args[3];
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("No desired number of populations specified as 4th arg");
            }
        }

        if (executionType.equals("MINIMA_SEARCH")) {

            try {
                processed[4] = args[4];
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Minima Error A");
            }

            try {
                processed[5] = args[5];
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Minima Error B");
            }

            try {
                processed[6] = args[6];
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Minima Error C");
            }

            try {
                processed[7] = args[7];
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Minima Error D");
            }
        }

        if (executionType.equals("FACTOR_SEARCH")) {

            try {
                processed[4] = args[4];
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Factor Error A");
            }

            try {
                processed[5] = args[5];
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Factor Error B");
            }

            try {
                processed[6] = args[6];
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Factor Error C");
            }
        }

        if (executionType.equals("FACTOR_SEARCH_PRECISION")) {

            try {
                processed[4] = args[4];
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Factor Error A");
            }

            try {
                processed[5] = args[5];
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Factor Error B");
            }

            try {
                processed[6] = args[6];
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Factor Error C");
            }

            try {
                processed[7] = args[7];
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Factor Error C");
            }

            try {
                processed[8] = args[8];
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Factor Error D");
            }

            try {
                processed[9] = args[9];
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Factor Error E");
            }
        }

        return processed;

    }

    public static boolean check(String[] args, String executionType) {

        switch (executionType) {
            case "STANDARD":
                return standardCheck(args);
            case "MINIMA_SEARCH":
                return minimaCheck(args);
            case "N-RUNS":
                return nRunsCheck(args);
            case "FACTOR_SEARCH":
                return factorCheck(args);
            case "FACTOR_SEARCH_PRECISION":
                return factorPrecisionCheck(args);
            default:
                throw new InvalidParameterException();
        }
    }

    private static boolean standardCheck(String[] args) {

        return args.length == 1
                && !Objects.equals(args[0], "");
    }

    private static boolean nRunsCheck(String[] args) {

        return args.length == 4
                && !Objects.equals(args[0], "") && !Objects.equals(args[1], "")
                && !Objects.equals(args[2], "") && !Objects.equals(args[3], "");
    }

    private static boolean minimaCheck(String[] args) {

        return args.length == 8
                && !Objects.equals(args[0], "") && !Objects.equals(args[1], "")
                && !Objects.equals(args[2], "") && !Objects.equals(args[3], "")
                && !Objects.equals(args[4], "") && !Objects.equals(args[5], "")
                && !Objects.equals(args[6], "") && !Objects.equals(args[7], "");
    }

    private static boolean factorCheck(String[] args) {

        return args.length == 7
                && !Objects.equals(args[0], "") && !Objects.equals(args[1], "")
                && !Objects.equals(args[2], "") && !Objects.equals(args[3], "")
                && !Objects.equals(args[4], "") && !Objects.equals(args[5], "")
                && !Objects.equals(args[6], "");
    }

    private static boolean factorPrecisionCheck(String[] args) {

        return (args.length == 9 || args.length == 10)
                && !Objects.equals(args[0], "") && !Objects.equals(args[1], "")
                && !Objects.equals(args[2], "") && !Objects.equals(args[3], "")
                && !Objects.equals(args[4], "") && !Objects.equals(args[5], "")
                && !Objects.equals(args[6], "") && !Objects.equals(args[7], "")
                && !Objects.equals(args[8], "");
    }
}
