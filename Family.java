import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Family {
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

    public void addChildren(String ChildId) {
        childrenId.add(ChildId);
    }

    public List<String> getChildren(){
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