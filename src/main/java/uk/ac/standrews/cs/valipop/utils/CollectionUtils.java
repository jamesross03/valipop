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

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

/**
 * Utility functions on Java Collections.
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class CollectionUtils {

    /*
    -------- Adapted shuffle code taken from Collections class ---------
     */

    private static final int SHUFFLE_THRESHOLD = 5;

    @SuppressWarnings("unchecked")
    public static <T extends Object> void shuffle(List<T> list, RandomGenerator rnd) {

        int size = list.size();
        if (size < SHUFFLE_THRESHOLD || list instanceof RandomAccess) {
            for (int i = size; i > 1; i--)
                Collections.swap(list, i - 1, rnd.nextInt(i));
        } else {
            Object[] array = list.toArray();

            // Shuffle array
            for (int i = size; i > 1; i--)
                swap(array, i - 1, rnd.nextInt(i));

            // Dump array back into list
            // instead of using a raw type here, it's possible to capture
            // the wildcard but it will require a call to a supplementary
            // private method
            ListIterator<T> it = list.listIterator();
            for (Object o : array) {
                it.next();
                it.set((T) o);
            }
        }
    }

    private static void swap(Object[] arr, int i, int j) {
        Object tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }
}
