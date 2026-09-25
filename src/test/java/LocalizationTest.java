import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class LocalizationTest {
    private static int testsRun = 0;

    private static void check(String expected, String actual, String name) {
        testsRun++;
        if (!expected.equals(actual)) {
            throw new AssertionError(name + ": expected " + expected + ", got " + actual);
        }
    }

    private static void check(int expected, int actual, String name) {
        testsRun++;
        if (expected != actual) {
            throw new AssertionError(name + ": expected " + expected + ", got " + actual);
        }
    }

    private static Properties loadProperties(String file) {
        Properties properties = new Properties();
        try (InputStream stream = LocalizationTest.class.getResourceAsStream("/" + file)) {
            if (stream == null) {
                throw new AssertionError("Missing bundle: " + file);
            }
            properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (IOException error) {
            throw new AssertionError("Cannot read bundle: " + file, error);
        }
        return properties;
    }

    public static void main(String[] args) {
        Properties base = loadProperties("messages.properties");
        for (String key : new String[] {
                "label.importance_shared", "tooltip.importance_custom",
                "help.shared_importance", "help.overlap", "help.benefit_harm",
                "label.not_evaluated", "result.none_evaluated",
                "result.only_one_evaluated" }) {
            testsRun++;
            if (!base.containsKey(key) || base.getProperty(key).trim().isEmpty()) {
                throw new AssertionError("Missing shared-importance help: " + key);
            }
        }
        for (String code : LocalizationManager.LANGUAGE_CODES) {
            Properties translated = loadProperties("messages_"
                    + code.replace('-', '_') + ".properties");
            for (String key : base.stringPropertyNames()) {
                testsRun++;
                if (!translated.containsKey(key)
                        || translated.getProperty(key).trim().isEmpty()) {
                    throw new AssertionError("Missing translation: " + code + " / " + key);
                }
                if (base.getProperty(key).contains("{0}")
                        && !translated.getProperty(key).contains("{0}")) {
                    throw new AssertionError("Missing placeholder: " + code + " / " + key);
                }
            }
        }

        LocalizationManager language = new LocalizationManager("en");
        check("Not evaluated", language.text("label.not_evaluated"),
                "English unevaluated label");
        check("Benefit means a favorable change in this factor; Harm means an unfavorable change. For example, for Stress level, less stress is a benefit and more stress is a harm.",
                language.text("help.benefit_harm"), "English direction help");
        check("Relationship quality", language.text("factor.relationship_quality"),
                "English factor label");
        language.setLanguage("zh-CN");
        check("未评估", language.text("label.not_evaluated"),
                "Simplified Chinese unevaluated label");
        check("目前只评估了一个选项。请评估其他选项后再进行比较。",
                language.text("result.only_one_evaluated"),
                "Simplified Chinese one-option comparison message");
        check("收益表示这个因素朝对你有利的方向变化，损害表示朝不利方向变化。例如对于‘压力水平’，压力降低属于收益，压力升高属于损害。",
                language.text("help.benefit_harm"), "Simplified Chinese direction help");
        check("关系质量", language.text("factor.relationship_quality"),
                "Simplified Chinese factor label");
        language.setLanguage("ja");
        check("関係の質", language.text("factor.relationship_quality"),
                "Japanese factor label");
        check("missing.example", language.text("missing.example"),
                "missing key displays stable key");
        check(20, LocalizationManager.LANGUAGE_CODES.length,
                "twenty supported languages");
        for (String code : LocalizationManager.LANGUAGE_CODES) {
            language.setLanguage(code);
            testsRun++;
            if (language.text("button.calculate").trim().isEmpty()) {
                throw new AssertionError("Missing calculate label for " + code);
            }
            testsRun++;
            if (language.text("category.relationship").equals("category.relationship")) {
                throw new AssertionError("Missing relationship label for " + code);
            }
            for (Factor factor : FactorLibrary.createPresetFactors()) {
                testsRun++;
                if (language.text(factor.getId()).equals(factor.getId())) {
                    throw new AssertionError("Missing factor label: " + code + " / "
                            + factor.getId());
                }
            }
        }

        DecisionModel model = new DecisionModel();
        Factor preset = model.getOptions().get(0).getFactors().get(0);
        preset.setEnabled(true);
        preset.setImportance(85);
        preset.setBenefitProbability(30);
        preset.setBenefitImpact(70);
        preset.setHarmProbability(40);
        preset.setHarmImpact(50);
        Factor custom = new Factor("My exact wording", "My category", true);
        custom.setNotes("My exact note");
        model.getOptions().get(0).getFactors().add(custom);
        language.setLanguage("en");
        language.setLanguage("zh-CN");
        check(85, preset.getImportance(), "language does not change importance");
        check(30, preset.getBenefitProbability(), "language does not change benefit");
        check(40, preset.getHarmProbability(), "language does not change harm");
        check("My exact wording", custom.getId(), "custom name stays literal");
        check("My exact note", custom.getNotes(), "custom note stays literal");
        System.out.println("Passed " + testsRun + " localization tests.");
    }
}
