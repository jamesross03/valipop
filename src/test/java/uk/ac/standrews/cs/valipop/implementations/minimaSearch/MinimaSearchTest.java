package uk.ac.standrews.cs.valipop.implementations.minimaSearch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.standrews.cs.valipop.Config;
import uk.ac.standrews.cs.valipop.implementations.OBDModel;
import uk.ac.standrews.cs.valipop.implementations.SpaceExploredException;

import java.nio.file.Paths;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class MinimaSearchTest {

    OBDModel model;

    @BeforeEach
    public void setup() {

        Config config = new Config(
                LocalDate.of(1,1,1),
                LocalDate.of(200,1,1),
                LocalDate.of(300,1,1),
                0,
                Paths.get("src/test/resources/valipop/test-pop"),
                Config.DEFAULT_RESULTS_SAVE_PATH, "MINIMA_SEARCH_TEST",
                Config.DEFAULT_RESULTS_SAVE_PATH).setDeterministic( true);

        model = new OBDModel(config);
    }

    @Test
    public void nanTesting() throws SpaceExploredException {

        double startingFactor = 0.0;

        MinimaSearch.startFactor = startingFactor;
        MinimaSearch.step = 0.5;
        MinimaSearch.initStep = 0.5;

        Control control = Control.RF;

        MinimaSearch.setControllingFactor(control, MinimaSearch.startFactor);
        double rf = MinimaSearch.getControllingFactor(control);

        assertEquals(rf, startingFactor, 1E-6);

        MinimaSearch.setControllingFactor(control, MinimaSearch.getNextFactorValue());
        rf = MinimaSearch.getControllingFactor(control);
        assertEquals(startingFactor, rf, 1E-6);

        MinimaSearch.logFactortoV(rf, 0.2078297837489273);

        MinimaSearch.setControllingFactor(control, MinimaSearch.getNextFactorValue());
        rf = MinimaSearch.getControllingFactor(control);
        assertEquals(startingFactor + 0.5, rf, 1E-6);
    }
}
