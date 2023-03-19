import junit.framework.TestSuite;
import org.junit.jupiter.api.Test;
import org.junit.runners.Suite;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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
    @Test
    void testBirthBeforeMarriage(){
        Individual husband = new Individual("1");
        Individual wife = new Individual("2")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern( tokens[2].length() < 2 ? "d MMM yyyy": "dd MMM yyyy");
        LocalDate inputdate = LocalDate.parse("1 AUG 1978",formatter);
        husband.setBirthday(inputdate);
        inputdate = LocalDate.parse("1 AUG 1980",formatter);
        wife.setBirthday(inputdate);
         Map<String,Individual> indimap = new TreeMap<>();
        Family fam = new Family("1");
        fam.setHusbandID("1");
        fam.setWifeID("2");
        inputdate = LocalDate.parse("1 AUG 2000",formatter);
        fam.setMarried(inputdate);
        boolean result1 = GEDCOMParser.isBirthBeforeMarriage(indimap,fam);
        assertTrue(result1);
        inputdate = LocalDate.parse("1 AUG 1979",formatter);
        fam.setMarried(inputdate);
        boolean result2 = GEDCOMParser.isBirthBeforeMarriage(indimap,fam);
        assertFalse(result2);
        inputdate = LocalDate.parse("1 AUG 1960",formatter);
        fam.setMarried(inputdate);
        boolean result3 = GEDCOMParser.isBirthBeforeMarriage(indimap,fam);
        assertFalse(result3);
    }

    @Test
    void testAgeDiff(){
        Individual hus = new Individual("I00");
        hus.setBirthday(LocalDate.of(2000, 1, 23));
        hus.setSpouse("F99");
        Individual wif = new Individual("I23");
        wif.setBirthday(LocalDate.of(2023, 1, 23));
        wif.setSpouse("F99");
        Family fam = new Family("F99");
        fam.setHusbandID("I00");
        fam.setWifeID("I23");
        Map<String, Individual> indiMap = new HashMap<>(){{
            put("I00", hus);
            put("I23", wif);
        }};

        assertTrue( GEDCOMParser.marrAgeDiff(indiMap, fam));

    }

    @Test
    void RecentBirthsTest() {
        boolean test1 = GEDCOMParser.isRecentBorn(LocalDate.of(2000, 2, 18));
        assertFalse(test1);

        boolean test2 = GEDCOMParser.isRecentBorn(LocalDate.of(2022, 2, 1));
        assertFalse(test2);

        boolean test3 = GEDCOMParser.isRecentBorn(LocalDate.of(2023, 3, 8));
        assertTrue(test3);
    }

    @Test
    void NotTooOldTest(){
        int test1 = GEDCOMParser.calcAgeAtDeath(LocalDate.of(1111,3,8),LocalDate(2020,1,3));
        boolean result1 = test1 < 150
        assertFalse(result1)
        int test2 = GEDCOMParser.calcAgeAtDeath(LocalDate.of(1870,3,8),LocalDate(1970,1,3));
        boolean result2 = test2 < 150
        assertTrue(result2)
        int test1 = GEDCOMParser.calcAgeAtDeath(LocalDate.of(1800,3,8),LocalDate(1950,1,3));
        boolean result3 = test3 < 150
        assertFalse(result3)
    }

    @Test
    void testUniqueIndividualID(){
        Individual indi1 = new Individual("1");
        Map<String,Individual> indimap = new TreeMap<>();
        indimap.put("1",indi1);
        boolean result1 = GEDCOMParser.isIndividualUniqueId(indimap,"1");
        assertFalse(result1);
        boolean result2 = GEDCOMParser.isIndividualUniqueId(indimap, "2");
        assertTrue(result2);
        Individual indi2 = new Individual("2");
        indimap.put("2", indi2);
        boolean result3 = GEDCOMParser.isIndividualUniqueId(indimap, "2");
        assertFalse(result3);

    }
    @Test
    void testUniqueFamilyID(){
        Family fam1 = new Family("1");
        Map<String,Family> famMap = new TreeMap<>();
        famMap.put("1",fam1);
        boolean result1 = GEDCOMParser.isFamilyUniqueId(famMap,"1");
        assertFalse(result1);
        boolean result2 = GEDCOMParser.isFamilyUniqueId(famMap, "2");
        assertTrue(result2);
        Family fam2 = new Family("2");
        famMap.put("2",fam2);
        boolean result3 = GEDCOMParser.isFamilyUniqueId(famMap, "2");
        assertFalse(result3);
    }


}