package uk.ac.standrews.cs.valipop.simulationEntities.dataStructure;

import org.junit.Test;
import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
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
