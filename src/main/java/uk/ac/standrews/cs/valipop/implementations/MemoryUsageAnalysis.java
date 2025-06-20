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

import java.io.IOException;
import java.lang.management.ManagementFactory;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class MemoryUsageAnalysis {

    private static boolean checkMemory = true;

    private static long maxSimUsage = 0L;
    private static long maxRunUsage = 0L;

    private static double threshold = 0.975;

    public static void main(String[] args) throws IOException, StatsException {

        checkMemory = true;

        CL_RunNModels.runNModels(args);

        // We do this to force the latest maxSimUsage to be checked against the maxRunUsage and force the value update if necessary
        reset();

        System.out.println("---------------------------------\n");
        System.out.println("Max Memory Usage : " + (maxRunUsage / 1e6) + " MB");
        System.out.println("We recommend to increase by 10% to give adequate headroom\n");
    }

    public static void reset() {
        if (maxSimUsage > maxRunUsage) {
            maxRunUsage = maxSimUsage;
        }
        maxSimUsage = 0L;
    }

    public static void log() throws PreEmptiveOutOfMemoryWarning {

        if (checkMemory) {
            long currentUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
            if (currentUsage > maxSimUsage) {
                maxSimUsage = currentUsage;
            }

            long mM = Runtime.getRuntime().maxMemory();

            if (mM * threshold < currentUsage) {
                throw new PreEmptiveOutOfMemoryWarning();
            }
        }
    }

    public static void setCheckMemory(boolean b) {
        checkMemory = b;
    }

    public static long getMaxSimUsage() {
        return maxSimUsage;
    }
}
