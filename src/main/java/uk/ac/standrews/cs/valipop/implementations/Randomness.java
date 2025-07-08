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
package uk.ac.standrews.cs.valipop.implementations;

import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

public class Randomness {

    private static RandomGenerator randomGenerator = null;

    public static int debug_count = 0;
    public static boolean do_debug = false;

    public synchronized static RandomGenerator getRandomGenerator() {

        if (randomGenerator == null) {
            randomGenerator = new JDKRandomGenerator() {

                int call_count = 0;

                @Override
                public boolean nextBoolean() {
                    call_count++;
                    boolean b = super.nextBoolean();
                    if ((OBDModel.global_debug))
                        System.out.println("Number of rng calls: " + call_count);

                    return b;
                }

                @Override
                public double nextDouble() {
                    call_count++;
                    double v = super.nextDouble();
                    if ((OBDModel.global_debug))
                        System.out.println("Number of rng calls: " + call_count);
                    return v;
                }

                public int nextInt(int bound) {
                    call_count++;
                    int i = super.nextInt(bound);
                    if ((OBDModel.global_debug))
                        System.out.println("Number of rng calls: " + call_count);
                    return i;
                }
            };
        }

        return randomGenerator;
    }
}
