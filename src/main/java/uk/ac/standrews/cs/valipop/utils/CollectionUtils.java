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
