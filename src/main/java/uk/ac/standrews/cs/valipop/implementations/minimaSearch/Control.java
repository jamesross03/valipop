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
package uk.ac.standrews.cs.valipop.implementations.minimaSearch;

import java.security.InvalidParameterException;
import java.util.Arrays;

/**
 * Specifies the controlling factor of the minima search.
 * 
 * <br>
 * 
 * Either RF (recovery factor).
 * 
 * <br>
 * 
 * Or PRF (proportional recovery factor).
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public enum Control {

    RF,
    PRF;

    public static Control resolve(String s) {

        s = s.toLowerCase();

        switch (s) {

            case "rf":
                return RF;
            case "prf":
                return PRF;

        }

        throw new InvalidParameterException("Given Control option is not recognised. Supported options are: " + Arrays.asList(Control.values()));
    }
}
