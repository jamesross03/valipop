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
package uk.ac.standrews.cs.valipop.simulationEntities.dataStructure;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.ac.standrews.cs.valipop.simulationEntities.PopulationNavigation.ageOnDate;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
@SuppressWarnings("ImplicitNumericConversion")
public class PersonTest {

    @Test
    public void testAgeOnDate() {

        final LocalDate birthday1 = LocalDate.of(1900, 1, 1);
        final LocalDate birthday2 = LocalDate.of(1900, 1, 2);
        final LocalDate birthday3 = LocalDate.of(1900, 12, 31);

        final LocalDate query1 = LocalDate.of(1900, 1, 1);
        final LocalDate query2 = LocalDate.of(1901, 1, 1);
        final LocalDate query3 = LocalDate.of(1901, 1, 2);
        final LocalDate query4 = LocalDate.of(1901, 12, 31);
        final LocalDate query5 = LocalDate.of(1902, 1, 1);

        assertEquals(0, ageOnDate(birthday1, query1));
        assertEquals(1, ageOnDate(birthday1, query2));
        assertEquals(1, ageOnDate(birthday1, query3));
        assertEquals(1, ageOnDate(birthday1, query4));
        assertEquals(2, ageOnDate(birthday1, query5));

        assertEquals(0, ageOnDate(birthday2, query1));
        assertEquals(0, ageOnDate(birthday2, query2));
        assertEquals(1, ageOnDate(birthday2, query3));
        assertEquals(1, ageOnDate(birthday2, query4));
        assertEquals(1, ageOnDate(birthday2, query5));

        assertEquals(0, ageOnDate(birthday3, query1));
        assertEquals(0, ageOnDate(birthday3, query2));
        assertEquals(0, ageOnDate(birthday3, query3));
        assertEquals(1, ageOnDate(birthday3, query4));
        assertEquals(1, ageOnDate(birthday3, query5));
    }
}
