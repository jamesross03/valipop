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

import org.apache.commons.math3.random.RandomGenerator;
import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.simulationEntities.Person;
import uk.ac.standrews.cs.valipop.simulationEntities.PopulationCounts;
import uk.ac.standrews.cs.valipop.simulationEntities.dataStructure.Population;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SexOption;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.PopulationStatistics;
import uk.ac.standrews.cs.valipop.utils.specialTypes.dates.DateSelector;

import java.time.LocalDate;
import java.time.Period;
import java.time.Year;
import java.time.temporal.ChronoUnit;

/**
 * Source for creating persons in simulation.
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class PersonFactory {

    private final Population population;
    private final PopulationStatistics desired;
    private final Period simulationTimeStep;
    private final DateSelector birthDateSelector;

    public PersonFactory(Population population, PopulationStatistics desired, Period simulationTimeStep, RandomGenerator randomNumberGenerator) {
        this.population = population;
        this.desired = desired;
        this.simulationTimeStep = simulationTimeStep;
        birthDateSelector = new DateSelector(randomNumberGenerator);
    }

    public IPerson makePerson(final LocalDate birthDate, final IPartnership parents, final boolean adulterous) {
        return makePerson(birthDate, parents, adulterous, false);
    }

    public IPerson makePerson(final LocalDate birthDate, final IPartnership parents, final boolean adulterous, final boolean immigrant) {

        SexOption sex = getSex(population.getPopulationCounts(), desired, birthDate);
        return new Person(sex, birthDate, parents, desired, adulterous, immigrant);
    }

    public IPerson makePerson(final LocalDate birthDate, final IPartnership parents, final boolean adulterous, final boolean immigrant, final SexOption sex) {

        if (sex == SexOption.MALE)
            population.getPopulationCounts().newMale();
        else
            population.getPopulationCounts().newFemale();

        Person person = new Person(sex, birthDate, parents, desired, adulterous, immigrant);
        return person;
    }

    public IPerson makePerson(final LocalDate birthDate, final IPartnership parents, final boolean adulterous, final boolean immigrant, final SexOption sex, final String surname) {

        if (sex == SexOption.MALE)
            population.getPopulationCounts().newMale();
        else
            population.getPopulationCounts().newFemale();

        Person person = new Person(sex, birthDate, parents, desired, adulterous, immigrant);
        if (surname != null) person.setSurname(surname);
        return person;
    }

    public IPerson makePersonWithRandomBirthDate(final LocalDate currentDate, final IPartnership parents, final boolean adulterous) {

        LocalDate immigrationDateFather = parents == null ? null : parents.getMalePartner().getImmigrationDate();

        if(immigrationDateFather != null) {
            if(immigrationDateFather.plus(desired.getMinGestationPeriod()).isAfter(currentDate))
                return makePerson(
                        birthDateSelector.selectRandomDate(
                            immigrationDateFather.plus(desired.getMinGestationPeriod()),
                            currentDate.plus(1, ChronoUnit.YEARS)).minus(1, ChronoUnit.DAYS),
                        parents, adulterous);
        }

        return makePerson(birthDateSelector.selectRandomDate(currentDate, simulationTimeStep), parents, adulterous);
    }

    private SexOption getSex(final PopulationCounts counts, final PopulationStatistics statistics, final LocalDate currentDate) {

        final double sexBalance = counts.getAllTimeSexRatio();

        if (sexBalance < statistics.getMaleProportionOfBirths(Year.of(currentDate.getYear()))) {

            counts.newMale();
            return SexOption.MALE;

        } else {

            counts.newFemale();
            return SexOption.FEMALE;
        }
    }

}
