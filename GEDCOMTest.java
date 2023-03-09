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

    void LargeAgeDifferencesTest() 
	{
		String test1 = GEDCOMParser.marrAgeDiff(30, 15);
		assertEquals(test1);

		String test2 = GEDCOMParser.marrAgeDiff(15, 30);
		assertEquals(test2);
		
		String test3 = GEDCOMParser.marrAgeDiff(15, 28);
		assertEquals(test3);
		
		String test4 = GEDCOMParser.marrAgeDiff(25, 22);
		assertEquals(test4);
	
		String test5 = GEDCOMParser.marrAgeDiff(15, 50);
		assertEquals(test5);
	}

    void RecentBirthsTest() {
		String test1 = GEDCOMParser.isRecentBorn(LocalDate.of(2000, 2, 18));
		assertEquals(test1);

        String test2 = GEDCOMParser.isRecentBorn(LocalDate.of(2022, 2, 1));
		assertEquals(test2);

        String test3 = GEDCOMParser.isRecentBorn(LocalDate.of(2023, 3, 8));
		assertEquals(test3);
    }
}