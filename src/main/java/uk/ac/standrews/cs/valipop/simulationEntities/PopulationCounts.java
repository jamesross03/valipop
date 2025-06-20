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
package uk.ac.standrews.cs.valipop.simulationEntities;

/**
 * Class to keep track of statisitcs of population count.l
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class PopulationCounts {

    private int createdMales = 0;
    private int createdFemales = 0;

    private int maxPopulation = 0;

    private int eligibilityChecks = 0;
    private int failedEligibilityChecks = 0;

    public void newMale(final int numberOf) {
        createdMales += numberOf;
    }

    public void newMale() {
        newMale(1);
    }

    public void newFemale(final int numberOf) {
        createdFemales += numberOf;
    }

    public void newFemale() {
        newFemale(1);
    }

    public void updateMaxPopulation(final int populationSize) {
        if (populationSize > maxPopulation) {
            maxPopulation = populationSize;
        }
    }

    public void incEligibilityCheck() {
        eligibilityChecks++;
    }

    public void incFailedEligibilityCheck() {
        failedEligibilityChecks++;
    }

    public int getEligibilityChecks() {
        return eligibilityChecks;
    }

    public int getFailedEligibilityChecks() {
        return failedEligibilityChecks;
    }

    public int getPeakPopulationSize() {
        return maxPopulation;
    }

    public int getCreatedPeople() {
        return createdFemales + createdMales;
    }

    public double getAllTimeSexRatio() {
        return createdMales / ((double) (createdFemales + createdMales));
    }
}
