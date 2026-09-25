import java.util.ArrayList;
import java.util.List;

public class DecisionModel {
    private String situation;
    private List<DecisionOption> options;
    private List<Criterion> presetCriteria;

    public DecisionModel() {
        situation = "";
        options = new ArrayList<DecisionOption>();
        presetCriteria = FactorLibrary.createPresetCriteria();
        addPresetOption("option.face");
        addPresetOption("option.text");
        addPresetOption("option.wait");
    }

    private void addPresetOption(String id) {
        DecisionOption option = new DecisionOption(id, false);
        addPresetEffects(option);
        options.add(option);
    }

    private void addPresetEffects(DecisionOption option) {
        // Importance weights the user's criterion, so it is shared across options.
        // Each Factor holds the effect estimates for just one option.
        for (Criterion criterion : presetCriteria) {
            option.getFactors().add(new Factor(criterion));
        }
    }

    public DecisionOption addCustomOption(String name) {
        DecisionOption option = new DecisionOption(name, true);
        addPresetEffects(option);
        options.add(option);
        return option;
    }

    public List<DecisionOption> getEvaluatedOptions() {
        List<DecisionOption> evaluated = new ArrayList<DecisionOption>();
        for (DecisionOption option : options) {
            for (Factor factor : option.getFactors()) {
                if (factor.isEnabled()) {
                    evaluated.add(option);
                    break;
                }
            }
        }
        return evaluated;
    }

    public List<DecisionOption> getHighestScoringOptions() {
        List<DecisionOption> highestOptions = new ArrayList<DecisionOption>();
        double highest = Double.NEGATIVE_INFINITY;
        List<DecisionOption> evaluated = getEvaluatedOptions();
        for (DecisionOption option : evaluated) {
            double score = option.calculateScore();
            if (score > highest) {
                highest = score;
            }
        }
        for (DecisionOption option : evaluated) {
            if (Math.abs(option.calculateScore() - highest) < 0.000001) {
                highestOptions.add(option);
            }
        }
        return highestOptions;
    }

    public String getSituation() { return situation; }
    public List<DecisionOption> getOptions() { return options; }
    public void setSituation(String situation) { this.situation = situation; }
}
