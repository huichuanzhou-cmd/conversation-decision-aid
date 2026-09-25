public class CalculationTest {
    private static int testsRun = 0;

    private static void check(double expected, double actual, String name) {
        testsRun++;
        if (Math.abs(expected - actual) > 0.000001) {
            throw new AssertionError(name + ": expected " + expected + ", got " + actual);
        }
    }

    private static void check(boolean expected, boolean actual, String name) {
        testsRun++;
        if (expected != actual) {
            throw new AssertionError(name + ": expected " + expected + ", got " + actual);
        }
    }

    private static void check(String expected, String actual, String name) {
        testsRun++;
        if (!expected.equals(actual)) {
            throw new AssertionError(name + ": expected " + expected + ", got " + actual);
        }
    }

    private static Factor factor(int importance, int benefitProbability,
            int benefitImpact, int harmProbability, int harmImpact) {
        Factor factor = new Factor("test.factor", "category.mental", true);
        factor.setEnabled(true);
        factor.setImportance(importance);
        factor.setBenefitProbability(benefitProbability);
        factor.setBenefitImpact(benefitImpact);
        factor.setHarmProbability(harmProbability);
        factor.setHarmImpact(harmImpact);
        return factor;
    }

    public static void main(String[] args) {
        Factor disabled = factor(100, 100, 100, 0, 50);
        disabled.setEnabled(false);
        check(0.0, disabled.calculateContribution(), "disabled factor");
        check(0.0, factor(0, 100, 100, 0, 50).calculateContribution(),
                "zero importance");
        check(0.0, factor(50, 0, 100, 0, 50).calculateContribution(),
                "zero benefit probability");
        check(0.0, factor(50, 0, 50, 0, 100).calculateContribution(),
                "zero harm probability");

        Factor benefit = factor(70, 90, 80, 0, 50);
        check(0.504, benefit.calculateContribution(), "benefit only");
        Factor harm = factor(50, 0, 50, 80, 50);
        check(-0.2, harm.calculateContribution(), "harm only");
        check(0.008, factor(80, 30, 70, 40, 50).calculateContribution(),
                "benefit and harm on one factor");
        check(0.0, factor(50, 50, 80, 40, 100).calculateContribution(),
                "equal effects cancel");
        check(1.0, factor(100, 100, 100, 0, 50).calculateContribution(),
                "100 percent probability, impact, and importance");
        check(-1.0, factor(100, 0, 50, 100, 100).calculateContribution(),
                "100 percent harm");

        DecisionOption option = new DecisionOption("option.test", false);
        option.getFactors().add(benefit);
        option.getFactors().add(harm);
        check(30.4, option.calculateScore(), "option aggregates benefit and harm");
        option.getFactors().add(factor(80, 30, 70, 40, 50));
        check(31.2, option.calculateScore(), "multiple factor aggregation");

        Factor neutral = factor(80, 30, 70, 40, 50);
        check(30.0, neutral.getNeutralProbability(), "neutral remainder");
        check(true, neutral.isProbabilityValid(), "probabilities below 100 valid");
        neutral.setHarmProbability(70);
        check(0.0, neutral.getNeutralProbability(), "probabilities exactly 100");
        check(true, neutral.isProbabilityValid(), "exactly 100 valid");
        neutral.setHarmProbability(71);
        check(false, neutral.isProbabilityValid(), "probabilities over 100 invalid");
        testsRun++;
        try {
            neutral.calculateContribution();
            throw new AssertionError("invalid probability must reject calculation");
        } catch (IllegalStateException expected) {
            // The invalid factor must be corrected before calculation.
        }

        DecisionModel model = new DecisionModel();
        check(0.0, model.getOptions().get(0).calculateScore(), "new option starts at zero");
        model.getOptions().get(0).getFactors().get(0).setEnabled(true);
        check(0.0, model.getOptions().get(1).calculateScore(),
                "options own separate factor instances");

        Factor face = model.getOptions().get(0).getFactors().get(8);
        Factor text = model.getOptions().get(1).getFactors().get(8);
        Factor wait = model.getOptions().get(2).getFactors().get(8);
        check(true, face.getCriterion() == text.getCriterion(),
                "preset effects share one criterion identity");
        face.setImportance(90);
        check(90.0, text.getImportance(), "preset importance shared with text");
        check(90.0, wait.getImportance(), "preset importance shared with wait");
        face.setEnabled(true);
        face.setBenefitProbability(30);
        face.setBenefitImpact(70);
        face.setHarmProbability(40);
        face.setHarmImpact(50);
        text.setEnabled(true);
        text.setBenefitProbability(20);
        text.setBenefitImpact(60);
        text.setHarmProbability(25);
        text.setHarmImpact(40);
        check(0.009, face.calculateContribution(), "face uses shared importance");
        check(0.018, text.calculateContribution(), "text uses own effect estimates");
        check(30.0, face.getBenefitProbability(), "face benefit remains local");
        check(70.0, face.getBenefitImpact(), "face benefit impact remains local");
        check(40.0, face.getHarmProbability(), "face harm remains local");
        check(50.0, face.getHarmImpact(), "face harm impact remains local");
        check(20.0, text.getBenefitProbability(), "text benefit remains local");
        check(60.0, text.getBenefitImpact(), "text benefit impact remains local");
        check(25.0, text.getHarmProbability(), "text harm remains local");
        check(40.0, text.getHarmImpact(), "text harm impact remains local");
        check(false, wait.isEnabled(), "enabled state remains local");
        check(0.0, wait.calculateContribution(), "disabled option contributes zero");

        DecisionOption customOption = model.addCustomOption("My option");
        Factor customPreset = customOption.getFactors().get(8);
        check(90.0, customPreset.getImportance(),
                "custom option receives shared preset importance");
        customPreset.setImportance(60);
        check(60.0, face.getImportance(), "custom option updates common priority");
        check(60.0, text.getImportance(), "all existing options update priority");
        check(0.006, face.calculateContribution(), "face score follows shared priority");
        check(0.012, text.calculateContribution(), "text score follows shared priority");

        Factor customOne = new Factor("Same display name", "My category", true);
        Factor customTwo = new Factor("Same display name", "My category", true);
        customOne.setImportance(75);
        check(false, customOne.getCriterion().getIdentity().equals(
                customTwo.getCriterion().getIdentity()),
                "same-name custom criteria have distinct internal identities");
        check(50.0, customTwo.getImportance(),
                "same-name custom factors do not share importance");

        DecisionModel comparison = new DecisionModel();
        LocalizationManager english = new LocalizationManager("en");
        check(0, comparison.getEvaluatedOptions().size(),
                "no enabled factors means no evaluated options");
        check(0, comparison.getHighestScoringOptions().size(),
                "no evaluated options means no highest score");
        check("No options have been evaluated yet. Enable a factor to begin.",
                MainWindow.comparisonMessage(comparison, english),
                "no evaluated options show a neutral message");
        Factor first = comparison.getOptions().get(0).getFactors().get(0);
        first.setEnabled(true);
        first.setImportance(100);
        first.setHarmProbability(50);
        first.setHarmImpact(100);
        check(1, comparison.getEvaluatedOptions().size(),
                "one enabled option is evaluated");
        check(true, comparison.getHighestScoringOptions().get(0)
                == comparison.getOptions().get(0),
                "negative evaluated score beats unevaluated options");
        check("Only one option has been evaluated so far. Evaluate other options to compare them.",
                MainWindow.comparisonMessage(comparison, english),
                "one evaluated option needs another for comparison");
        Factor second = comparison.getOptions().get(1).getFactors().get(0);
        second.setEnabled(true);
        check(2, comparison.getEvaluatedOptions().size(),
                "enabled zero-score option is still evaluated");
        check(true, comparison.getHighestScoringOptions().get(0)
                == comparison.getOptions().get(1),
                "evaluated zero score beats negative evaluated score");
        check("Based on the values you entered, Text / messaging currently has the highest estimated score.",
                MainWindow.comparisonMessage(comparison, english),
                "two evaluated options produce a winner");
        Factor third = comparison.getOptions().get(2).getFactors().get(0);
        third.setEnabled(true);
        comparison.addCustomOption("Unevaluated");
        check(2, comparison.getHighestScoringOptions().size(),
                "ties include only evaluated options");
        check("Based on the values you entered, these options tie for the highest estimated score: Text / messaging, Wait / no action for now.",
                MainWindow.comparisonMessage(comparison, english),
                "tie message excludes unevaluated option");
        System.out.println("Passed " + testsRun + " calculation tests.");
    }
}
