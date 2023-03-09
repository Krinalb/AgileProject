import junit.framework.TestSuite;
import org.junit.jupiter.api.Test;
import org.junit.runners.Suite;

import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
@Suite.SuiteClasses(GEDCOMTest.class)
public class GEDCOMTest {

    @Test
    void testCurrDateBeforeNow(){
        boolean result1 = GEDCOMParser.isInputDateValid(LocalDate.of(2000, 2, 18));
        assertTrue(result1);
        boolean result2 = GEDCOMParser.isInputDateValid(LocalDate.of(2050, 2, 18));
        assertFalse(result2);
        boolean result3 = GEDCOMParser.isInputDateValid(LocalDate.of(2023, 4, 3));
        assertFalse(result3);
    }
}