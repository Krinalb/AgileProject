import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public class Individual {
    private String id = "NA";
    private String name = "NA";
    private String gender = "NA";
    private LocalDate birthday = null;
    private int age = -1;
    private boolean alive = true;
    private LocalDate death = null;
    private String isSpouse = "NA";
    private String isChild = "NA";
    private Family family = null;
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
        if(alive == true){
            LocalDate currDate = LocalDate.now();
            this.age = Period.between(birthday, currDate).getYears();
        }else{
            this.age = Period.between(birthday, death).getYears();
        }
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

    public Family getFamily(){
        return this.family;
    }

    public void setFamily(Family fam){
        this.family = fam;
    }

}