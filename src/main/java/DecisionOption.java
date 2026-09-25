import java.util.ArrayList;
import java.util.List;

public class DecisionOption {
    private String id;
    private boolean custom;
    private String notes;
    private List<Factor> factors;

    public DecisionOption(String id, boolean custom) {
        this.id = id;
        this.custom = custom;
        notes = "";
        factors = new ArrayList<Factor>();
    }

    public double calculateScore() {
        double total = 0.0;
        for (Factor factor : factors) {
            total += factor.calculateContribution();
        }
        return 100.0 * total;
    }

    public String getId() { return id; }
    public boolean isCustom() { return custom; }
    public String getNotes() { return notes; }
    public List<Factor> getFactors() { return factors; }
    public void setNotes(String notes) { this.notes = notes; }
}
