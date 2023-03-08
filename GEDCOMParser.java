import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

public class GEDCOMParser {

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
                    if(!cfam.getChildern().contains(iid.toString())){
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
            childern = fam.getChildern();

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
    private static boolean isValidDate(String day, String month, String year){
        Pattern dpattern = Pattern.compile("^\\d{1,2}$");
        Pattern ypattern = Pattern.compile("^\\d{4,4}$");
        Pattern mpattern = Pattern.compile("^[a-zA-Z]{3,3}$");
        HashMap<String, Integer> months = new HashMap<>() {{put("JAN", 31);put("FEB", 28);put("MAR", 31);
            put("APR", 30);put("MAY", 31);put("JUN", 30);put("JUL", 31);put("AUG", 31);put("SEP", 30);
            put("OCT", 31);put("NOV", 30);put("DEC", 31);
        }};

        if(!dpattern.matcher(day).matches()) return false;
        if(!ypattern.matcher(year).matches()) return false;
        if(!mpattern.matcher(month).matches()) return false;

        if(months.get(month.toUpperCase()) != null){
            if(!(Integer.parseInt(day)>0 && Integer.parseInt(day) <= months.get(month.toUpperCase()))){
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

    public static void main(String[] args) {
        String fileName = "/Users/jaydeepdobariya/Desktop/Spring Sem/CS 555 - Agile Methodologies/family.ged"; // replace with actual file name

        Map<String, Individual> individualsMap = new TreeMap<>();
        Map<String, Family> familiesMap = new TreeMap<>();
        ArrayList<String> errorList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            Individual currentIndividual = null;
            Family currentFamily = null;
            String[] preTokens = null;
            boolean preTokenflag = true;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(" ");
                if (preTokenflag) {
                    preTokens = tokens;
                    preTokenflag = false;
                }
                if (tokens[0].equals("0")) {
                    if (tokens.length >= 3 && tokens[2].equals("INDI")) {
                        if(individualsMap.containsKey(tokens[1])){
                            errorList.add(String.format("Error US22: Id (%s) is not unqiue ",tokens[1]));
                        }
                        else {
                            currentIndividual = new Individual(tokens[1]);
                            individualsMap.put(tokens[1], currentIndividual);
                        }
                    } else if (tokens.length >= 3 && tokens[2].equals("FAM")) {
                        if( familiesMap.containsKey(tokens[1])){
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
                            if (currentFamily != null) currentFamily.setHusbandID(tokens[2]);
                            break;

                        case "WIFE":
                            if (currentFamily != null) currentFamily.setWifeID(tokens[2]);
                            break;

                        case "CHIL":
                            if (currentFamily != null) currentFamily.addChildern(tokens[2]);
                            break;

                        case "DATE":
                            String dateType = "";
                            String day = tokens[2];
                            String month = tokens[3].toUpperCase().charAt(0)+tokens[3].substring(1).toLowerCase();
                            String year = tokens[4];
                            String dateStr = day + " "+ month +" "+ year;
                            if(!isValidDate(day, month, year)){
                                errorList.add(String.format("Error US01: Entered invalid date (%s) for %s (%s)", dateStr,currentIndividual.getName().replace("/", ""),currentIndividual.getId()));
                            }
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern( tokens[2].length() < 2 ? "d MMM yyyy": "dd MMM yyyy");
                            LocalDate currDate = LocalDate.now();
                            LocalDate inputdate = LocalDate.parse(dateStr,formatter);
                            if (preTokens[1].equals("BIRT")){
                                currentIndividual.setBirthday(inputdate);
                                dateType = "Birth";
                                if(isRecentBorn(inputdate)){
                                    errorList.add(String.format("Error US35: %s (%s) is born in the last 30 days", currentIndividual.getName().replace("/",""), currentIndividual.getId()));
                                }
                            }

                            if (preTokens[1].equals("DEAT")){
                                currentIndividual.setDeath(inputdate);
                                dateType = "Death";
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
                            }

                            if (preTokens[1].equals("MARR")) {
                                currentFamily.setMarried(inputdate);
                                dateType = "Married";
                                if(marrAgeDiff(individualsMap, currentFamily)){
                                    Individual husband = individualsMap.get(currentFamily.getHusbandID());
                                    Individual wife = individualsMap.get(currentFamily.getWifeID());
                                    errorList.add(String.format("Error US34: FamilyID = {%s}, Husband = {%s}, HusbandID = {%s}, Husband-brirthdate = {%s}, Wife = {%s}, WifeID = {%s}, Wife-birthdate = {%s}",currentFamily.getId(), husband.getName(), husband.getId(), husband.getBirthday().toString(), wife.getName(), wife.getId(), wife.getBirthday()));
                                }
                            }
                            if(!inputdate.isBefore(currDate)){
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
            System.out.printf("ID = {%s}, Name = {%s}, Gender = {%s}, Birthday = {%s}, Age = {%d}, Alive = {%b}, Death = {%s}, Child = {%s}, Spouse = {%s}\n",
                    iid, indiv.getName(), indiv.getGender(), indiv.getBirthday().toString(), indiv.getAge(), indiv.isAlive(), indiv.getDeathDate().toString(), indiv.isChild(), indiv.isSpouse());
        }

        System.out.println("Family:");
        for (String fid : familiesMap.keySet()) {
            Family fam = familiesMap.get(fid);
            System.out.printf("ID = {%s}, Married = {%s}, Divorced = {%s}, Husband ID = {%s}, Husband Name = {%s}, Wife ID = {%s}, Wife Name = {%s}, Childern = {%s}\n",
                    fid, fam.getMarried().toString(), fam.getDivorced().toString(), fam.getHusbandID(), individualsMap.get(fam.getHusbandID()).getName(),fam.getWifeID(), individualsMap.get(fam.getWifeID()).getName(), fam.getChildern().toString());
        }
        System.out.println("Deceased:");
        for (String iid : individualsMap.keySet()) {
            Individual indiv = individualsMap.get(iid);
            if(!indiv.isAlive()) {
                System.out.printf("ID = {%s}, Name = {%s}, Gender = {%s}, Birthday = {%s}, Age = {%d}, Alive = {%b}, Death = {%s}, Child = {%s}, Spouse = {%s}\n",
                        iid, indiv.getName(), indiv.getGender(), indiv.getBirthday().toString(), indiv.getAge(), indiv.isAlive(), indiv.getDeathDate().toString(), indiv.isChild(), indiv.isSpouse());

            }
        }
        System.out.println("\nErrors and Anomalies:");
        for(String err: errorList){
            System.out.println(err);
        }
    }
}

class Individual {
    private String id = "NA";
    private String name = "NA";
    private String gender = "NA";
    private LocalDate birthday = null;
    private int age = -1;
    private boolean alive = true;
    private LocalDate death = null;
    private String isSpouse = "NA";
    private String isChild = "NA";
    private List<String> comments = new ArrayList<>();
    public Individual(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender(){
        return gender;
    }
    public void setGender(String gender){
        this.gender = gender;
    }
    public LocalDate getBirthday(){
        return birthday;
    }
    public void setBirthday(LocalDate dob){
        this.birthday = dob;

    }
    private int calcAge(LocalDate dob){
        LocalDate birthDate = LocalDate.parse(dob.toString());
        LocalDate currDate = LocalDate.now();
        this.age = Period.between(birthday, currDate).getYears();
        return age;
    }
    public int getAge(){
        return calcAge(birthday);
    }

    public boolean isAlive(){
        return alive;
    }

    public Object getDeathDate() {
        if(death != null)
            return death;
        else return "NA";
    }

    public void setDeath(LocalDate death){
        this.death = death;
        this.alive = false;
    }

    public String isSpouse(){
        return isSpouse;
    }

    public void setSpouse(String spouse){
        this.isSpouse = spouse;
    }

    public String isChild(){
        return isChild;
    }

    public void setChild(String child){
        this.isChild = child;
    }

}

class Family {
    private String id = "NA";
    private LocalDate married = null;
    private LocalDate divorced = null;
    private String husbandID = null;
    private String wifeID = null;
    private List<String> childrenId = new ArrayList<>();

    public Family(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getHusbandID() {
        return husbandID;
    }

    public void setHusbandID(String husband) {
        this.husbandID = husband;
    }

    public String getWifeID() {
        return wifeID;
    }

    public void setWifeID(String wife) {
        this.wifeID = wife;
    }

    public void addChildern(String ChildId) {
        childrenId.add(ChildId);
    }

    public List getChildern(){
        return childrenId;
    }

    public Object getMarried(){
        if(married != null)
            return married;
        else return "NA";
    }
    public void setMarried(LocalDate married){
        this.married = married;
    }

    public Object getDivorced(){
        if(divorced != null)
            return divorced;
        else return "NA";
    }
    public void setDivorced(LocalDate divorced){
        this.divorced = divorced;
    }

}