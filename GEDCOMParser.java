import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

public class GEDCOMParser {

    public static boolean isLastNameSameInFamily(Map<String, Individual> indiMap, Family fam){
        String chLastName;
        String husLastName = indiMap.get(fam.getHusbandID()).getName().split("/")[1];
        for(String cID: fam.getChildren()){
            Individual child = indiMap.get(cID);
            if(child.getGender() != "M") continue;
            chLastName = child.getName().split("/")[1];
            if(!husLastName.toLowerCase().equals(chLastName.toLowerCase())){
                return false;
            }
        }
        return true;
    }

    public static boolean isBirthBeforeMarriageOfParents(Individual indi){
        Family fam = indi.getFamily();
        LocalDate marrDate = null;
        if(fam != null){
            marrDate = (LocalDate) fam.getMarried();
        }else{
            return false;
        }

        LocalDate bday = (LocalDate) indi.getBirthday();


        if(bday.isAfter(marrDate)){
            return false;
        }

        if(!fam.getDivorced().toString().equals("NA")){
            LocalDate divDate = (LocalDate) fam.getDivorced();
            System.out.println(divDate.toString());
            LocalDate divDateAfterNineMonths = divDate.plusMonths(9);
            if(bday.isBefore(divDateAfterNineMonths)) return false;
        }

        return true;


    }

    public static boolean isDivorceBeforeDeath(Map<String, Individual> indiMap, Family fam) {
        // Get husband and wife objects from individual map
        Individual husband = indiMap.get(fam.getHusbandID());
        Individual wife = indiMap.get(fam.getWifeID());

        // Get death dates and divorce date
        if(fam.getDivorced() != "NA"){
            LocalDate divDate = (LocalDate) fam.getDivorced();
            if(husband.getDeathDate() != "NA") {
                LocalDate hdd = (LocalDate) husband.getDeathDate();
                if (divDate.isAfter(hdd)) {
                    return false;
                }
            }
            if(wife.getDeathDate() != "NA") {
                LocalDate wdd = (LocalDate) wife.getDeathDate();
                if (divDate.isAfter(wdd)) {
                    return false;
                }
            }

        }
        return true;
    }

    public static void listUpcomingBirthdays(Map<String, Individual> individualMap){
        LocalDate currentDate = LocalDate.now();
      
        for(String iid: individualMap.keySet()){
            Individual indiv= individualMap.get(iid);
            LocalDate birthday = indiv.getBirthday().withYear(2023);

            
            if(currentDate.isBefore(birthday)){
                long daysBtn = ChronoUnit.DAYS.between(currentDate, birthday);
                if(daysBtn <=30){
                System.out.printf("ID = {%s}, Name = {%s}, Gender = {%s}, Birthday = {%s}, Age = {%d}, Alive = {%b}, Death = {%s}, Child = {%s}, Spouse = {%s}\n",
                        iid, indiv.getName(), indiv.getGender(), indiv.getBirthday().toString(), indiv.getAge(), indiv.isAlive(), indiv.getDeathDate().toString(), indiv.isChild(), indiv.isSpouse());
                    }   
                }
        }
    }

    public static void listUpcomingAnniversaries(Map<String, Family> familyMap, Map<String, Individual> individualsMap){
        LocalDate currentDate = LocalDate.now();
      
        for(String fid: familyMap.keySet()){
            Family fam= familyMap.get(fid);
            LocalDate anni = (LocalDate) fam.getMarried();
            anni = anni.withYear(2023);

            
            if(currentDate.isBefore(anni)){
                long daysBtn = ChronoUnit.DAYS.between(currentDate, anni);
                if(daysBtn <=30){
                
                    System.out.printf("ID = {%s}, Married = {%s}, Divorced = {%s}, Husband ID = {%s}, Husband Name = {%s}, Wife ID = {%s}, Wife Name = {%s}, Childern = {%s}\n",
                    fid, fam.getMarried().toString(), fam.getDivorced().toString(), fam.getHusbandID(), individualsMap.get(fam.getHusbandID()).getName(),fam.getWifeID(), individualsMap.get(fam.getWifeID()).getName(), fam.getChildren().toString());                   }   
                }
        }
    }


    public static void parentsTooOld(Family fam, Map<String,Individual> individualMap,ArrayList<String> errorList){
        Individual hus = individualMap.get(fam.getHusbandID());
        Individual wif = individualMap.get(fam.getWifeID());
        for(String iid: fam.getChildren()){
            Individual child = individualMap.get(iid);
            if((hus.getAge() - child.getAge()) >= 80){
                String err12 = String.format("Error US12: %s (%s) Father %s (%s) is too old.",child.getName(), child.getId(), hus.getName(), hus.getId());
                errorList.add(err12);

            }
            if((wif.getAge() - child.getAge()) >= 60){
                String err12 = String.format("Error US12: %s (%s) Mother %s (%s) is too old.",child.getName(), child.getId(), wif.getName(), wif.getId());
                errorList.add(err12);

            }

        }
    }

    public static void hasOrphans(Family fam, Map<String,Individual> individualMap, Map<String,Individual> orphanMap,ArrayList<String> errorList) {
        Individual hus = individualMap.get(fam.getHusbandID());
        Individual wif = individualMap.get(fam.getWifeID());
        if ((!hus.isAlive()) && (!wif.isAlive())) {
            for (String iid : fam.getChildren()) {
                Individual child = individualMap.get(iid);
                if (child.getAge() < 18) {
                    orphanMap.put(child.getId(), child);
                }
            }
        }
    }

    public static void listSingleOverThirty(Map<String, Individual> individualMap){
        for(String iid: individualMap.keySet()){
            Individual indiv= individualMap.get(iid);
            if(indiv.isAlive() == true && indiv.getAge()>30 && indiv.isSpouse() == "NA"){
                System.out.printf("ID = {%s}, Name = {%s}, Gender = {%s}, Birthday = {%s}, Age = {%d}, Alive = {%b}, Death = {%s}, Child = {%s}, Spouse = {%s}\n",
                        iid, indiv.getName(), indiv.getGender(), indiv.getBirthday().toString(), indiv.getAge(), indiv.isAlive(), indiv.getDeathDate().toString(), indiv.isChild(), indiv.isSpouse());
            }
        }
    }

    public static void listLivingMarried(Map<String, Individual> individualMap){
        for(String iid: individualMap.keySet()){
            Individual indiv= individualMap.get(iid);
            if(indiv.isAlive() == true && indiv.isSpouse() != "NA"){
                System.out.printf("ID = {%s}, Name = {%s}, Gender = {%s}, Birthday = {%s}, Age = {%d}, Alive = {%b}, Death = {%s}, Child = {%s}, Spouse = {%s}\n",
                        iid, indiv.getName(), indiv.getGender(), indiv.getBirthday().toString(), indiv.getAge(), indiv.isAlive(), indiv.getDeathDate().toString(), indiv.isChild(), indiv.isSpouse());
            }
        }
    }

    public static boolean isGenderCorrect(Individual ind, String expGender,ArrayList<String> errorList){
        if(!ind.getGender().equals(expGender)){
            String err21 = String.format("Error US21: %s (%s) in family (%s) should be %s.",(expGender == "M" ? "Husband": "Wife"),ind.getId(), ind.getFamily().getId(), (expGender == "M"? "male":"female"));
            errorList.add(err21);
            return false;
        }
        return true;
    }
    public static boolean isBirthBeforeMarriage(Map<String, Individual> indiMap, Family fam){
        Individual husband = indiMap.get(fam.getHusbandID());
        Individual wife = indiMap.get(fam.getWifeID());
        LocalDate hbd = husband.getBirthday();
        LocalDate wbd = wife.getBirthday();
        LocalDate marrDate = (LocalDate) fam.getMarried();
        if(marrDate.isBefore(hbd) || marrDate.isBefore(wbd) || marrDate.isEqual(hbd) || marrDate.isEqual(wbd)){
            return false;
        }
        return true;
    }

    public static boolean isBirthBeforeDeath(Individual indi){
        if(!indi.isAlive()){
            LocalDate birthdate = indi.getBirthday();
            LocalDate deathDate = (LocalDate) indi.getDeathDate();
            if(!birthdate.isBefore(deathDate)){
                return true;
            }
        }
        return false;
    }

    public static boolean isMarrBeforeDiv(Family fam){
        if(fam.getMarried() != "NA" && fam.getDivorced() != "NA"){
            LocalDate marrdate = (LocalDate) fam.getMarried();
            LocalDate divDate = (LocalDate) fam.getDivorced();
            if(!marrdate.isBefore(divDate)){
                return true;
            }
        }
        return false;
    }
    public static boolean marrAgeDiff(Map<String, Individual> indiMap, Family fam){
            Individual husband = indiMap.get(fam.getHusbandID());
            Individual wife = indiMap.get(fam.getWifeID());
            long hbd = husband.getAge();
            long wbd = wife.getAge();
            if(hbd > wbd && hbd > wbd*2){
                return true;
            }else if(wbd > hbd && wbd > hbd*2){
                return true;
            }
        return false;
    }
    public static boolean isRecentBorn(LocalDate brithDate){
        LocalDate currentDate = LocalDate.now();
        long daysBtn = ChronoUnit.DAYS.between(brithDate, currentDate);
        if(daysBtn <= 30 && daysBtn >= 0){
            return true;
        }
        return false;
    }

    public static void checkCorrEntries(Map<String, Individual> indis, Map<String, Family> fams, ArrayList<String> errorList){
        String isChild;
        String isSpouse;
        String indiName;
        for (String iid : indis.keySet()) {
            Individual indiv = indis.get(iid);
            isChild = indiv.isChild();
            isSpouse = indiv.isSpouse();
            indiName = indiv.getName().replace("/", "");
            if(isChild.equals("NA") && isSpouse.equals("NA")){
                errorList.add(String.format("Error US26: %s (%s) doesn't belong to any family!",indiName, iid));
            }
            if(!isChild.equals("NA")){
                Family cfam = fams.get(isChild);
                if(cfam == null){
                    errorList.add(String.format("Error US26: %s (%s) is a child in a family (%s) which doesn't exist in the database!",indiName,iid,isChild));
                }else{
                    if(!cfam.getChildren().contains(iid.toString())){
                        errorList.add(String.format("Error US26: %s (%s) doesn't belong in family (%s) as a child!",indiName,iid,isChild));
                    }
                }
            }
            if(!isSpouse.equals("NA")){
                Family sfam = fams.get(isSpouse);
                if(sfam == null){
                    errorList.add(String.format("Error US26: %s (%s) is a spouse in a family (%s) which doesn't exist in the database!",indiName,iid,isSpouse));
                }else {
                    if (!(sfam.getHusbandID().equals(iid) || sfam.getWifeID().equals(iid))) {
                        errorList.add(String.format("Error US26: %s (%s) doesn't belong in family (%s) as a spouse!", indiName, iid, isSpouse));
                    }
                }
            }
        }

        String husbandID;
        String wifeID;
        List<String> childern;
        for(String fid : fams.keySet()){
            Family fam = fams.get(fid);
            husbandID = fam.getHusbandID();
            wifeID = fam.getWifeID();
            childern = fam.getChildren();

            if(indis.get(husbandID) == null){
                errorList.add(String.format("Error US26: Husband (%s) in family (%s) does not exist in the database!", husbandID, fid));
            }
            if(indis.get(wifeID) == null){
                errorList.add(String.format("Error US26: Wife (%s) in family (%s) does not exist in the database!", wifeID, fid));
            }

            for(String cid : childern){
                if(indis.get(cid) == null){
                    errorList.add(String.format("Error US26: Child (%s) in family (%s) does not exist in the database!", cid, fid));
                }
            }
        }
    }
    public static boolean isBornBeforeParentsDeath(Individual child, Individual hus, Individual wif,ArrayList<String> errorList ){
        if(hus.getDeathDate() != "NA"){
            if(!child.getBirthday().isBefore(((LocalDate) hus.getDeathDate()).minusMonths(9))){
                errorList.add(String.format("Error US09: Child (%s) is born after their father's(%s) death!", child.getId(),hus.getId()));
                return false;
            }
        }
        if(wif.getDeathDate() != "NA"){
            if(!child.getBirthday().isBefore((LocalDate)wif.getDeathDate())){
                errorList.add(String.format("Error US09: Child (%s) is born after their Mothers's(%s) death!", child.getId(),wif.getId()));
                return false;
            }
        }
        return true;
    }
    public static boolean isValidDate(String day, String month, String year){
        Pattern dpattern = Pattern.compile("^\\d{1,2}$");
        Pattern ypattern = Pattern.compile("^\\d{4,4}$");
        Pattern mpattern = Pattern.compile("^[a-zA-Z]{3,3}$");


        HashMap<String, Integer> months = new HashMap<>() {{put("JAN", 1);put("FEB", 2);put("MAR", 3);
            put("APR", 4);put("MAY", 5);put("JUN", 6);put("JUL", 7);put("AUG", 8);put("SEP", 9);
            put("OCT", 10);put("NOV", 11);put("DEC", 12);
        }};

        if(!dpattern.matcher(day).matches()) return false;
        if(!ypattern.matcher(year).matches()) return false;
        if(!mpattern.matcher(month).matches()) return false;

        YearMonth yearMonthObject = YearMonth.of(Integer.parseInt(year), months.get(month.toUpperCase()));
        int daysInMonth = yearMonthObject.lengthOfMonth();

        if(months.get(month.toUpperCase()) != null){
            if(!(Integer.parseInt(day)>0 && Integer.parseInt(day) <= daysInMonth)){
                return false;
            }
        }else{
            return false;
        }

        if(Integer.parseInt(year) < 1){
            return false;
        }

        return true;
    }

    public static boolean isInputDateValid(LocalDate input){
        if(input.isBefore(LocalDate.now())){
            return true;
        }
        return false;
    }

    public static boolean isIndividualUniqueId(Map<String, Individual> indiMap, String id){
       return !indiMap.containsKey(id);
    }
    public static boolean isFamilyUniqueId(Map<String, Family> famMap, String id){
        return !famMap.containsKey(id);
    }

    public static void listDeceased(Map<String,Individual> indiMap){
        for (String iid : indiMap.keySet()) {
            Individual indiv = indiMap.get(iid);
            if(!indiv.isAlive()) {
                System.out.printf("ID = {%s}, Name = {%s}, Gender = {%s}, Birthday = {%s}, Age = {%d}, Alive = {%b}, Death = {%s}, Child = {%s}, Spouse = {%s}\n",
                        iid, indiv.getName(), indiv.getGender(), indiv.getBirthday().toString(), indiv.getAge(), indiv.isAlive(), indiv.getDeathDate().toString(), indiv.isChild(), indiv.isSpouse());

            }
        }
    }
    public static void main(String[] args) {
        String fileName ="/Users/jaydeepdobariya/Desktop/Spring Sem/CS 555 - Agile Methodologies/family.ged"; // replace with actual file name

        Map<String, Individual> individualsMap = new TreeMap<>();
        Map<String, Family> familiesMap = new TreeMap<>();
        ArrayList<String> errorList = new ArrayList<>();
        Map<String, Individual> orphanMap = new TreeMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            Individual currentIndividual = null;
            Family currentFamily = null;
            String[] preTokens = null;
            boolean preTokenflag = true;
            boolean tooOldFlag = false;
            while ((line = br.readLine()) != null) {
                
                String[] tokens = line.split(" ");
                if (preTokenflag) {
                    preTokens = tokens;
                    preTokenflag = false;
                }
                if (tokens[0].equals("0")) {
                    tooOldFlag = false;
                    if (tokens.length >= 3 && tokens[2].equals("INDI")) {
                        
                        if(!isIndividualUniqueId(individualsMap,tokens[1])){
                            errorList.add(String.format("Error US22: Id (%s) is not unqiue ",tokens[1]));
                        }
                        else {
                            currentIndividual = new Individual(tokens[1]);
                            individualsMap.put(tokens[1], currentIndividual);
                        }
                    } else if (tokens.length >= 3 && tokens[2].equals("FAM")) {
                        if( !isFamilyUniqueId(familiesMap,tokens[1])){
                            errorList.add(String.format("Error US22: Id (%s) is not unqiue ",tokens[1]));
                        }
                        else {
                            currentFamily = new Family(tokens[1]);
                            familiesMap.put(tokens[1], currentFamily);
                        }
                    }
                } else if (tokens[0].equals("1") || tokens[0].equals("2") || tokens[0].equals("0")) {
                    switch (tokens[1]) {
                        case "NAME":
                            if (currentIndividual != null) currentIndividual.setName(String.join(" ",Arrays.copyOfRange(tokens, 2, tokens.length)));
                            break;

                        case "HUSB":
                            if (currentFamily != null){
                                Individual husband;
                                currentFamily.setHusbandID(tokens[2]);
                                husband = individualsMap.get(tokens[2]);
                                husband.setFamily(currentFamily);
                                isGenderCorrect(husband, "M",errorList);
                            }
                            break;

                        case "WIFE":
                            if (currentFamily != null){
                                Individual wife;
                                currentFamily.setWifeID(tokens[2]);
                                wife = individualsMap.get(tokens[2]);
                                wife.setFamily(currentFamily);
                                isGenderCorrect(wife, "F",errorList);
                            }
                            break;

                        case "CHIL":
                            if (currentFamily != null){
                                currentFamily.addChildren(tokens[2]);
                                individualsMap.get(tokens[2]).setFamily(currentFamily);
                            };
                            break;

                        case "DATE":
                            String dateType = "";
                            String day = tokens[2];
                            String month = tokens[3].toUpperCase().charAt(0)+tokens[3].substring(1).toLowerCase();
                            String year = tokens[4];
                            String dateStr = day + " "+ month +" "+ year;
                            if(!isValidDate(day, month, year)){
                                errorList.add(String.format("Error US42: Entered invalid date (%s) for %s (%s)", dateStr,currentIndividual.getName().replace("/", ""),currentIndividual.getId()));
                            }
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern( tokens[2].length() < 2 ? "d MMM yyyy": "dd MMM yyyy");
                            LocalDate currDate = LocalDate.now();
                            LocalDate inputdate = LocalDate.parse(dateStr,formatter);
                            if (preTokens[1].equals("BIRT")){
                                currentIndividual.setBirthday(inputdate);
                                dateType = "Birth";
                                if(currentIndividual.getAge() > 150) tooOldFlag = true;
                                if(isRecentBorn(inputdate)){
                                    errorList.add(String.format("Error US35: %s (%s) is born in the last 30 days", currentIndividual.getName().replace("/",""), currentIndividual.getId()));
                                }
                            }

                            if (preTokens[1].equals("DEAT")){
                                currentIndividual.setDeath(inputdate);
                                dateType = "Death";
                                if(currentIndividual.getAge() < 150) tooOldFlag = false;
                                if(isBirthBeforeDeath(currentIndividual)){
                                    LocalDate birthdate = currentIndividual.getBirthday();
                                    LocalDate deathDate = (LocalDate) currentIndividual.getDeathDate();
                                    errorList.add(String.format("Error US03: Birth date (%s) of %s should occur before death date (%s) of an individual", birthdate.toString() , currentIndividual.getName().replace("/", ""), deathDate.toString()));
                                }
                            }

                            if (preTokens[1].equals("DIV")){
                                currentFamily.setDivorced(inputdate);
                                dateType = "Divorced";
                                if(isMarrBeforeDiv(currentFamily)){
                                    LocalDate marrdate = (LocalDate) currentFamily.getMarried();
                                    LocalDate divDate = (LocalDate) currentFamily.getDivorced();
                                    errorList.add(String.format("Error US04: Married date (%s) of Husband = %s and Wife = %s should occur before divorced date (%s) of an individual", marrdate.toString(), currentFamily.getHusbandID(), currentFamily.getWifeID(), divDate.toString()));
                                }
                                if(!isDivorceBeforeDeath(individualsMap,currentFamily)){
                                    Individual husband = individualsMap.get(currentFamily.getHusbandID());
                                    Individual wife = individualsMap.get(currentFamily.getWifeID());
                                    errorList.add(String.format("Error US06: DIVORCED AFTER DEATH - FamilyID = {%s}, Husband = {%s}, HusbandID = {%s}, Husband-brirthdate = {%s}, Wife = {%s}, WifeID = {%s}, Wife-birthdate = {%s}, Marriage-Date = {%s}",currentFamily.getId(), husband.getName(), husband.getId(), husband.getBirthday().toString(), wife.getName(), wife.getId(), wife.getBirthday(), currentFamily.getMarried()));

                                }
                            }

                            if (preTokens[1].equals("MARR")) {
                                currentFamily.setMarried(inputdate);
                                dateType = "Married";
                                if(!isBirthBeforeMarriage(individualsMap,currentFamily)){
                                    Individual husband = individualsMap.get(currentFamily.getHusbandID());
                                    Individual wife = individualsMap.get(currentFamily.getWifeID());
                                    errorList.add(String.format("Error US02: MARRIAGE BEFORE BIRTH - FamilyID = {%s}, Husband = {%s}, HusbandID = {%s}, Husband-brirthdate = {%s}, Wife = {%s}, WifeID = {%s}, Wife-birthdate = {%s}, Marriage-Date = {%s}",currentFamily.getId(), husband.getName(), husband.getId(), husband.getBirthday().toString(), wife.getName(), wife.getId(), wife.getBirthday(), currentFamily.getMarried()));
                                
                                }
                                if(marrAgeDiff(individualsMap, currentFamily)){
                                    Individual husband = individualsMap.get(currentFamily.getHusbandID());
                                    Individual wife = individualsMap.get(currentFamily.getWifeID());
                                    errorList.add(String.format("Error US34: FamilyID = {%s}, Husband = {%s}, HusbandID = {%s}, Husband-brirthdate = {%s}, Wife = {%s}, WifeID = {%s}, Wife-birthdate = {%s}",currentFamily.getId(), husband.getName(), husband.getId(), husband.getBirthday().toString(), wife.getName(), wife.getId(), wife.getBirthday()));
                                }
                            }
                            if(!isInputDateValid(inputdate)){
                                errorList.add(String.format("Error US01 : %s date (%s) of %s (%s) must be before today's date!", dateType,dateStr,currentIndividual.getName().replace("/", ""),currentIndividual.getId()));
                            }
                            break;

                        case "SEX":
                            String gen = tokens[2].toLowerCase();
                            if (gen.equals("m") || gen.equals("male"))
                                currentIndividual.setGender("M");

                            if (gen.equals("f") || gen.equals("female"))
                                currentIndividual.setGender("F");
                            break;

                        case "FAMC":
                            currentIndividual.setChild(tokens[2]);
                            break;

                        case "FAMS":
                            currentIndividual.setSpouse(tokens[2]);
                            break;

                        case "HEAD", "TRLR", "NOTE":
                            break;
                        
                        
                    }
                }
                preTokens = tokens;
                if(tooOldFlag == true){
                    errorList.add(String.format("Error US07 :Individual %s (%s) was alive for 150 or more years",currentIndividual.getName().replace("/", ""),currentIndividual.getId()));
                    tooOldFlag = false;
                }
                 
            }
           
           
            checkCorrEntries(individualsMap, familiesMap, errorList);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        System.out.println("Individuals:");
        for (String iid : individualsMap.keySet()) {
            Individual indiv = individualsMap.get(iid);
            if(indiv.isChild() !="NA"){
                isBornBeforeParentsDeath(indiv, individualsMap.get(familiesMap.get(indiv.isChild()).getHusbandID()), individualsMap.get(familiesMap.get(indiv.isChild()).getWifeID()), errorList);
            }
            System.out.printf("ID = {%s}, Name = {%s}, Gender = {%s}, Birthday = {%s}, Age = {%d}, Alive = {%b}, Death = {%s}, Child = {%s}, Spouse = {%s}\n",
                    iid, indiv.getName(), indiv.getGender(), indiv.getBirthday().toString(), indiv.getAge(), indiv.isAlive(), indiv.getDeathDate().toString(), indiv.isChild(), indiv.isSpouse());
        }

        System.out.println("Family:");
        for (String fid : familiesMap.keySet()) {
            Family fam = familiesMap.get(fid);
            parentsTooOld(fam,individualsMap,errorList);
            hasOrphans(fam,individualsMap,orphanMap,errorList);
            System.out.printf("ID = {%s}, Married = {%s}, Divorced = {%s}, Husband ID = {%s}, Husband Name = {%s}, Wife ID = {%s}, Wife Name = {%s}, Childern = {%s}\n",
                    fid, fam.getMarried().toString(), fam.getDivorced().toString(), fam.getHusbandID(), individualsMap.get(fam.getHusbandID()).getName(),fam.getWifeID(), individualsMap.get(fam.getWifeID()).getName(), fam.getChildren().toString());
        }
        System.out.println("Deceased:");
        listDeceased(individualsMap);

        System.out.println("\nUS12: Orphans:");
        for (String iid : orphanMap.keySet()) {
            Individual indiv = orphanMap.get(iid);
            System.out.printf("ID = {%s}, Name = {%s}, Gender = {%s}, Birthday = {%s}, Age = {%d}, Alive = {%b}, Death = {%s}, Child = {%s}, Spouse = {%s}\n",
                        iid, indiv.getName(), indiv.getGender(), indiv.getBirthday().toString(), indiv.getAge(), indiv.isAlive(), indiv.getDeathDate().toString(), indiv.isChild(), indiv.isSpouse());
            }
        System.out.println("\nUS30: Living married people:");
        listLivingMarried(individualsMap);

        System.out.println("\nUS31: Single who is alive, over 30, and never married before:");
        listSingleOverThirty(individualsMap);

        System.out.println("\nUS38:List upcoming birthdays:");
        listUpcomingBirthdays(individualsMap);

        System.out.println("\nUS39: List upcoming anniversaries:");
        listUpcomingAnniversaries(familiesMap,individualsMap);


        for(String famID: familiesMap.keySet()){
            if(!isLastNameSameInFamily(individualsMap, familiesMap.get(famID))){
                errorList.add(String.format("Error US16: All male members of a family (%s)  must have the same last name",famID));
            }
        }
        for(String iID: individualsMap.keySet()){
            Individual indi = individualsMap.get(iID);
            if(indi.isChild().equals("NA")) {
                continue;
            };

            if(isBirthBeforeMarriageOfParents(indi)){
                errorList.add(String.format("Error US08: Children (%s) should be born after marriage of parents (and not more than 9 months after their divorce))", iID));
            }
        }
        System.out.println("\nErrors and Anomalies:");
        for(String err: errorList){
            System.out.println(err);
        }


    }
}



