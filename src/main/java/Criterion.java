import java.util.UUID;

public class Criterion {
    private final String identity;
    private final String id;
    private final String categoryId;
    private final boolean custom;
    private int importance;

    public Criterion(String id, String categoryId, boolean custom) {
        this.id = id;
        this.categoryId = categoryId;
        this.custom = custom;
        // Presets keep their stable key; equal custom names remain distinct criteria.
        identity = custom ? "custom." + UUID.randomUUID() : id;
        importance = 50;
    }

    public String getIdentity() { return identity; }
    public String getId() { return id; }
    public String getCategoryId() { return categoryId; }
    public boolean isCustom() { return custom; }
    public int getImportance() { return importance; }
    public void setImportance(int importance) { this.importance = importance; }
}
