import junit.framework.TestSuite;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runners.Suite;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.*;

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
    void testBornBeforeParentsDeath(){
        ArrayList<String> erroList = new ArrayList<String>();
        Individual husband = new Individual("I1");
        Individual wife = new Individual("I2");
        Individual child = new Individual("I3");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy");
        LocalDate inputdate = LocalDate.parse("1 Aug 2005",formatter);
        child.setBirthday(inputdate);
        
        
        inputdate = LocalDate.parse("1 Aug 2005",formatter);
        husband.setDeath(inputdate);
        inputdate = LocalDate.parse("1 Aug 2020",formatter);
        wife.setDeath(inputdate);
        boolean result1 = GEDCOMParser.isBornBeforeParentsDeath(child,husband,wife,erroList);
        assertTrue(result1);

        inputdate = LocalDate.parse("1 Aug 2004",formatter);
        husband.setDeath(inputdate);
        inputdate = LocalDate.parse("1 Aug 2020",formatter);
        wife.setDeath(inputdate);
        boolean result2 = GEDCOMParser.isBornBeforeParentsDeath(child,husband,wife,erroList);
        assertFalse(result2);
       
        inputdate = LocalDate.parse("1 Aug 2007",formatter);
        husband.setDeath(inputdate);
        inputdate = LocalDate.parse("1 Aug 2003",formatter);
        wife.setDeath(inputdate);
        boolean result3 = GEDCOMParser.isBornBeforeParentsDeath(child,husband,wife,erroList);
        assertFalse(result3);
    }
    @Test
    void testBirthBeforeMarriage(){
        Map<String,Individual> indimap = new TreeMap<>();
        Individual husband = new Individual("I1");
        Individual wife = new Individual("I2");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy");
        LocalDate inputdate = LocalDate.parse("1 Aug 1978",formatter);
        husband.setBirthday(inputdate);
        indimap.put("I1", husband);
        inputdate = LocalDate.parse("1 Aug 1980",formatter);
        wife.setBirthday(inputdate);
        indimap.put("I2", wife);

        Family fam = new Family("1");
        fam.setHusbandID("I1");
        fam.setWifeID("I2");
        inputdate = LocalDate.parse("1 Aug 2000",formatter);
        fam.setMarried(inputdate);
        boolean result1 = GEDCOMParser.isBirthBeforeMarriage(indimap,fam);
        assertTrue(result1);
        inputdate = LocalDate.parse("1 Aug 1979",formatter);
        fam.setMarried(inputdate);
        boolean result2 = GEDCOMParser.isBirthBeforeMarriage(indimap,fam);
        assertFalse(result2);
        inputdate = LocalDate.parse("1 Aug 1960",formatter);
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

        Individual ind1 =  new Individual("I111");
        ind1.setBirthday(LocalDate.of(1111,3,8));
        ind1.setDeath(LocalDate.of(2020,1,3));

        Individual ind2 =  new Individual("I112");
        ind2.setBirthday(LocalDate.of(1870,3,8));
        ind2.setDeath(LocalDate.of(1970,1,3));

        Individual ind3 =  new Individual("I113");
        ind3.setBirthday(LocalDate.of(1800,3,8));
        ind3.setDeath(LocalDate.of(1950,1,3));

        int test1 = ind1.getAge();
        boolean result1 = test1 < 150;
        assertFalse(result1);

        int test2 = ind2.getAge();
        boolean result2 = test2 < 150;
        assertTrue(result2);

        int test3 = ind3.getAge();
        boolean result3 = test3 < 150;
        assertTrue(result3);
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

    @Test
    void testInvalidDates(){
        boolean isvalid;
        isvalid = GEDCOMParser.isValidDate("29","FEB","2000");
        assertTrue(isvalid);
        isvalid = GEDCOMParser.isValidDate("29","FEB","2001");
        assertFalse(isvalid);
        isvalid = GEDCOMParser.isValidDate("32","JAN","2001");
        assertFalse(isvalid);
        isvalid = GEDCOMParser.isValidDate("0","JAN","2001");
        assertFalse(isvalid);
        isvalid = GEDCOMParser.isValidDate("1","JAN","2001");
        assertTrue(isvalid);
    }

    @Test
    void testGender(){
        boolean isvalidGender;
        Family fam = new Family("fam");
        Individual husband = new Individual("test");
        husband.setFamily(fam);
        Individual wife = new Individual("test2");
        wife.setFamily(fam);
        husband.setGender("M");
        isvalidGender = GEDCOMParser.isGenderCorrect(husband, "M",new ArrayList<>());
        assertTrue(isvalidGender);
        husband.setGender("F");
        isvalidGender = GEDCOMParser.isGenderCorrect(husband, "M",new ArrayList<>());
        assertFalse(isvalidGender);
        wife.setGender("F");
        isvalidGender = GEDCOMParser.isGenderCorrect(wife, "F",new ArrayList<>());
        assertTrue(isvalidGender);
        wife.setGender("M");
        isvalidGender = GEDCOMParser.isGenderCorrect(wife, "F",new ArrayList<>());
        assertFalse(isvalidGender);
    }

    @Test
    public void testIsBirthBeforeMarriage() {
        // Create individual objects
        Individual husband = new Individual("H1");
        husband.setName("John Doe");
        husband.setGender("M");
        husband.setBirthday(LocalDate.of(1980, 1, 1));
        Individual wife = new Individual("W1");
        wife.setName("Jane Smith");
        wife.setGender("F");
        wife.setBirthday(LocalDate.of(1985, 1, 1));

        // Create family object
        Family fam = new Family("F1");
        fam.setHusbandID(husband.getId());
        fam.setWifeID(wife.getId());
        fam.setMarried(LocalDate.of(2010, 1, 1));

        // Create individual map and add individuals
        Map<String, Individual> indiMap = new HashMap<>();
        indiMap.put(husband.getId(), husband);
        indiMap.put(wife.getId(), wife);

        // Test if marriage date is after both birth dates
        assertTrue(GEDCOMParser.isBirthBeforeMarriage(indiMap, fam));

        // Test if marriage date is before husband's birth date
        husband.setBirthday(LocalDate.of(2010, 1, 1));
        assertFalse(GEDCOMParser.isBirthBeforeMarriage(indiMap, fam));

        // Test if marriage date is before wife's birth date
        husband.setBirthday(LocalDate.of(1980, 1, 1));
        wife.setBirthday(LocalDate.of(2010, 1, 1));
        assertFalse(GEDCOMParser.isBirthBeforeMarriage(indiMap, fam));
    }
    
    @Test
    public void testIsDivorceBeforeDeath() {
        // Create husband and wife objects
        Individual husband = new Individual("Husband");
        Individual wife = new Individual("Wife");

        // Set husband's death date to null and wife's death date to a future date
        husband.setDeath(null);
        wife.setDeath(LocalDate.of(2030, 1, 1));

        // Create family object and set divorce date to a past date
        Family family = new Family("1");
        family.setHusbandID(husband.getId());
        family.setWifeID(wife.getId());
        family.setDivorced(LocalDate.of(2020, 1, 1));

        // Create individual map and add husband and wife objects
        Map<String, Individual> indiMap = new HashMap<>();
        indiMap.put(husband.getId(), husband);
        indiMap.put(wife.getId(), wife);

        // Test that isDivorceBeforeDeath returns true
        Assertions.assertTrue(GEDCOMParser.isDivorceBeforeDeath(indiMap, family));

        // Set husband's death date to a past date
        husband.setDeath(LocalDate.of(2010, 1, 1));

        // Test that isDivorceBeforeDeath returns false
        Assertions.assertFalse(GEDCOMParser.isDivorceBeforeDeath(indiMap, family));
    }

}
