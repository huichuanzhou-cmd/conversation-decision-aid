public class Factor {
    private final Criterion criterion;
    private String notes;
    private boolean enabled;
    private int benefitProbability;
    private int benefitImpact;
    private int harmProbability;
    private int harmImpact;

    public Factor(String id, String categoryId, boolean custom) {
        this(new Criterion(id, categoryId, custom));
    }

    public Factor(Criterion criterion) {
        this.criterion = criterion;
        notes = "";
        enabled = false;
        benefitProbability = 0;
        benefitImpact = 50;
        harmProbability = 0;
        harmImpact = 50;
    }

    public boolean isProbabilityValid() {
        return benefitProbability + harmProbability <= 100;
    }

    public int getNeutralProbability() {
        // The remaining chance represents no significant change.
        return 100 - benefitProbability - harmProbability;
    }

    public double calculateContribution() {
        if (!enabled) {
            return 0.0;
        }
        if (!isProbabilityValid()) {
            throw new IllegalStateException("Benefit and harm probabilities exceed 100%.");
        }

        // Convert each 0-100 slider value into a 0.0-1.0 fraction.
        double importancePart = criterion.getImportance() / 100.0;
        double benefitProbabilityPart = benefitProbability / 100.0;
        double benefitImpactPart = benefitImpact / 100.0;
        double harmProbabilityPart = harmProbability / 100.0;
        double harmImpactPart = harmImpact / 100.0;

        double possibleBenefit = benefitProbabilityPart * benefitImpactPart;
        double possibleHarm = harmProbabilityPart * harmImpactPart;
        return importancePart * (possibleBenefit - possibleHarm);
    }

    public Criterion getCriterion() { return criterion; }
    public String getId() { return criterion.getId(); }
    public String getCategoryId() { return criterion.getCategoryId(); }
    public boolean isCustom() { return criterion.isCustom(); }
    public String getNotes() { return notes; }
    public boolean isEnabled() { return enabled; }
    public int getImportance() { return criterion.getImportance(); }
    public int getBenefitProbability() { return benefitProbability; }
    public int getBenefitImpact() { return benefitImpact; }
    public int getHarmProbability() { return harmProbability; }
    public int getHarmImpact() { return harmImpact; }

    public void setNotes(String notes) { this.notes = notes; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setImportance(int importance) { criterion.setImportance(importance); }
    public void setBenefitProbability(int value) { benefitProbability = value; }
    public void setBenefitImpact(int value) { benefitImpact = value; }
    public void setHarmProbability(int value) { harmProbability = value; }
    public void setHarmImpact(int value) { harmImpact = value; }
}
