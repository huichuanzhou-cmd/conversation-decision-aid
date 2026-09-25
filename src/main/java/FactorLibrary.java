import java.util.ArrayList;
import java.util.List;

public class FactorLibrary {
    // IDs remain the same when the visible language changes.
    public static List<Criterion> createPresetCriteria() {
        List<Criterion> criteria = new ArrayList<Criterion>();
        String mental = "category.mental";
        String relationship = "category.relationship";
        String practical = "category.practical";
        String risk = "category.risk";
        String information = "category.information";

        criteria.add(new Criterion("factor.emotional_state", mental, false));
        criteria.add(new Criterion("factor.stress", mental, false));
        criteria.add(new Criterion("factor.organize_thoughts", mental, false));
        criteria.add(new Criterion("factor.forget_points", mental, false));
        criteria.add(new Criterion("factor.sense_control", mental, false));
        criteria.add(new Criterion("factor.cognitive_effort", mental, false));
        criteria.add(new Criterion("factor.mental_fatigue", mental, false));
        criteria.add(new Criterion("factor.think_before_responding", mental, false));

        criteria.add(new Criterion("factor.relationship_quality", relationship, false));
        criteria.add(new Criterion("factor.trust", relationship, false));
        criteria.add(new Criterion("factor.mutual_understanding", relationship, false));
        criteria.add(new Criterion("factor.future_communication", relationship, false));
        criteria.add(new Criterion("factor.express_full_point", relationship, false));
        criteria.add(new Criterion("factor.interruption", relationship, false));
        criteria.add(new Criterion("factor.misunderstanding", relationship, false));
        criteria.add(new Criterion("factor.respect", relationship, false));

        criteria.add(new Criterion("factor.time_cost", practical, false));
        criteria.add(new Criterion("factor.opportunity_cost", practical, false));
        criteria.add(new Criterion("factor.convenience", practical, false));
        criteria.add(new Criterion("factor.daily_environment", practical, false));
        criteria.add(new Criterion("factor.future_coordination", practical, false));
        criteria.add(new Criterion("factor.privacy", practical, false));
        criteria.add(new Criterion("factor.financial_cost", practical, false));
        criteria.add(new Criterion("factor.disruption", practical, false));

        criteria.add(new Criterion("factor.conflict_escalation", risk, false));
        criteria.add(new Criterion("factor.ability_to_exit", risk, false));
        criteria.add(new Criterion("factor.reversibility", risk, false));
        criteria.add(new Criterion("factor.loss_of_control", risk, false));
        criteria.add(new Criterion("factor.secondary_consequences", risk, false));
        criteria.add(new Criterion("factor.dependence_on_cooperation", risk, false));
        criteria.add(new Criterion("factor.unwanted_disclosure", risk, false));
        criteria.add(new Criterion("factor.recovery_difficulty", risk, false));

        criteria.add(new Criterion("factor.information_gained", information, false));
        criteria.add(new Criterion("factor.uncertainty_reduction", information, false));
        criteria.add(new Criterion("factor.written_record", information, false));
        criteria.add(new Criterion("factor.future_viability", information, false));
        criteria.add(new Criterion("factor.verify_what_said", information, false));
        criteria.add(new Criterion("factor.learning_from_outcome", information, false));
        return criteria;
    }

    // Standalone factor copies remain useful for small calculations and examples.
    public static List<Factor> createPresetFactors() {
        List<Factor> factors = new ArrayList<Factor>();
        for (Criterion criterion : createPresetCriteria()) {
            factors.add(new Factor(criterion));
        }
        return factors;
    }
}
