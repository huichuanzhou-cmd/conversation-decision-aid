# Conversation Decision Aid v1.0.0

**Status: early hobby prototype.** A tiny, offline Java Swing program for experimenting with transparent decision scoring. It is a thinking aid, not a scientifically validated predictor, psychological assessment, or objective way to choose. Weighted decision matrices and expected-utility tools already exist; this project is a small, readable experiment.

v1.0.0 is the first public release. Earlier 0.x versions were development builds.

Each factor can have **both** a possible benefit and a possible harm. You enter the probabilities, impacts, and personal importance. The program only performs arithmetic on those entries. It has no accounts, cloud service, AI, NLP, or telemetry.

## For users

1. Download the package for your operating system. For Windows, extract the entire ZIP and open `ConversationDecisionAid.exe` inside the extracted folder. The application bundles its own Java runtime.
2. On first launch, choose a language. The choice is saved locally; later launches go straight to the main window. You can change language in the selector at the top of the window without losing the current decision.
3. Optionally describe the situation. Add an option or use Face-to-face, Text / messaging, and Wait / no action for now.
4. Enable only the relevant factors. Disabled factors stay compact and contribute zero. Every preset starts disabled.
5. For each enabled factor, set Importance, possible Benefit Probability and Impact, and possible Harm Probability and Impact. Benefit means a favorable change in that factor; Harm means an unfavorable change. For Stress level, less stress is a benefit and more stress is a harm. All sliders run from 0 to 100. A preset factor's Importance is shared across options; the effect estimates and enabled state belong to the current option. The remaining Neutral / no significant change probability is shown automatically.
6. Click Calculate to compare scores and inspect the full factor breakdown. An option with no enabled factors is shown as Not evaluated and is excluded from highest-score comparisons, rather than treated as a zero-score alternative. A warning blocks calculation when benefit and harm probabilities for an enabled factor add to more than 100%.

You can add custom options and custom factors, including a category and optional notes. A custom option uses the same preset criterion importance values as other options. A custom factor added inside one option belongs to that option; equal custom names are not merged. Situation text, names, and notes are displayed but never interpreted or scored. They remain in memory for the current session; only the chosen language is persisted. Custom text is not translated when the interface language changes.

## Formula and meaning

For criterion `i` under option `a`, convert every slider value from 0–100 to 0–1:

```text
contribution(a,i) = (importance(i) / 100)
    × ((benefitProbability(a,i) / 100) × (benefitImpact(a,i) / 100)
       − (harmProbability(a,i) / 100) × (harmImpact(a,i) / 100))

neutralProbability(a,i) = 100 − benefitProbability(a,i) − harmProbability(a,i)
score(a) = 100 × sum of enabled factor contributions for option a
```

An enabled factor with importance 80, benefit probability 30, benefit impact 70, harm probability 40, and harm impact 50 contributes `0.8 × (0.3 × 0.7 − 0.4 × 0.5) = 0.008`, or **+0.8** on the displayed score. Its neutral probability is 30%. Neutral contributes zero. A disabled factor contributes zero regardless of its saved slider values. There are no preset scores or hidden weights. Scores can exceed −100 or +100 because factor contributions are summed; they are **not percentages**.

Importance means how much a factor matters to you compared with others. Probability means how likely that type of effect is. Impact is the overall size if it occurs; consider strength and duration together. No separate duration or confidence multiplier is used. The score reflects your estimates and can change when they change. The highest score is not automatically the best real-world choice.

### Shared criterion, separate effects

Importance describes your priority for a criterion, so a preset factor has **one importance value across all options**. Each option separately stores whether that factor is enabled, its benefit/harm probabilities and impacts, and its note. Changing a preset Importance slider in one option updates the same slider in the others; changing an effect slider does not.

For example, Relationship quality can have shared Importance **90%**. Face-to-face might have Benefit `30% × 70%` and Harm `40% × 50%`; Text might have Benefit `20% × 60%` and Harm `25% × 40%`. The estimates differ, while the 90% priority stays the same.

## Factors and languages

The 38 preset factors are grouped in five categories: Mental / Cognitive; Relationship / Communication; Practical / Resource; Risk / Control; Information / Uncertainty. A preset is a dimension, not a permanent benefit or harm. Every preset uses a stable internal ID, such as `factor.relationship_quality`; its display label comes from a Java `ResourceBundle` UTF-8 `.properties` file.

Because scores are additive, several factors describing essentially the same consequence can count it more than once. Enable only distinct effects where possible. The app repeats this warning under Help → Formula and About.

Supported languages: English, Simplified Chinese, Traditional Chinese, Japanese, Korean, Spanish, French, German, Portuguese, Italian, Russian, Ukrainian, Turkish, Vietnamese, Indonesian, Thai, Malay, Polish, Dutch, and Czech. Translation files are complete but have **not been professionally reviewed**; corrections are welcome. To add a language, add a `messages_<locale>.properties` resource and register its code and visible name in `LocalizationManager.java`. English is the fallback for missing keys.

## Build and test from source

Developers need **JDK 21** with `javac`, `jar`, and `jpackage` on `PATH`. End users of a packaged app do not need a separate JDK or terminal.

On Windows PowerShell, run:

```powershell
powershell -File scripts/build-windows.ps1
java -cp build/classes ConversationDecisionAid
```

On macOS or Linux, compile and run without packaging:

```sh
mkdir -p build/classes build/test-classes
javac -encoding UTF-8 -d build/classes src/main/java/*.java
cp src/main/resources/*.properties build/classes/
javac -encoding UTF-8 -cp build/classes -d build/test-classes src/test/java/*.java
java -cp build/classes:build/test-classes CalculationTest
java -cp build/classes:build/test-classes LocalizationTest
java -cp build/classes ConversationDecisionAid
```

The Windows script compiles, runs both tests, creates a JAR, and packages the app. On macOS and Ubuntu Linux use the matching script:

| System | Command | Output |
| --- | --- | --- |
| Windows x64 | `powershell -File scripts/build-windows.ps1` | `dist/ConversationDecisionAid-v1.0.0-Windows-x64.zip` with `.exe` and runtime |
| macOS | `bash scripts/build-unix.sh macos` | `dist/ConversationDecisionAid-v1.0.0-macOS.dmg` |
| Ubuntu Linux | `bash scripts/build-unix.sh linux` | `dist/ConversationDecisionAid-v1.0.0-Linux.deb` |

Ubuntu needs `fakeroot` for DEB packaging (`sudo apt-get install fakeroot`). `jpackage` builds native packages on the matching operating system. The Windows ZIP is a portable application folder, not an installer. The macOS DMG is unsigned and may trigger Gatekeeper; signing and notarization are future work. GitHub Actions runs the same tests and operating-system-specific scripts on Windows, macOS, and Ubuntu runners, then uploads artifacts. Local builds on one system do not verify the other systems.

## Project structure

| File | Purpose |
| --- | --- |
| `src/main/java/Criterion.java` | Shared preset identity, category, and importance |
| `src/main/java/Factor.java` | One option's effect estimates, neutral probability, contribution formula |
| `src/main/java/FactorLibrary.java` | Stable IDs for 38 preset factors in five categories |
| `src/main/java/DecisionOption.java`, `DecisionModel.java` | Options, scores, and current decision |
| `src/main/java/LocalizationManager.java` | ResourceBundle lookup and saved language |
| `src/main/resources/messages*.properties` | 20 interface translations plus English fallback |
| `src/main/java/MainWindow.java` | Swing factor cards, language switching, results, help |
| `src/main/java/ConversationDecisionAid.java` | First-launch language selector and entry point |
| `src/test/java/CalculationTest.java`, `LocalizationTest.java` | Model and localization checks |
| `scripts/`, `.github/workflows/build.yml` | Native packaging and CI |

## License

MIT. See [LICENSE](LICENSE).

## Disclaimer

This is experimental hobby software. Its transparent formula has not been scientifically validated as a decision method. It is not a psychological diagnostic tool or a source of professional psychological, medical, legal, financial, or safety advice. It cannot predict another person's behavior or identify the objectively correct action.

The program calculates only from the values you provide. Inaccurate assumptions can give misleading scores. Free-text descriptions and notes are not semantically understood. Scores are artificial comparative values, not probabilities or measurements of real-world outcomes. You remain responsible for your decisions.

Overlapping factors can double-count the same consequence. The user must decide which factors describe distinct effects.

## v1.0.0 first public release

The v0.3.0 development build introduced simultaneous possible Benefit and Harm, 20 languages, and five factor categories. The v0.3.1 development build shared preset Importance correctly across options, made directional factor labels more neutral, improved English and the Chinese/Japanese wording, and explained the risk of double-counting. These improvements are included in the first public release.

## Future ideas

Save and load decisions as JSON; import/export; charts; sensitivity analysis; uncertainty ranges and confidence in estimates; Monte Carlo exploration; reusable templates; RTL language support; accessibility improvements; signed and notarized releases. These are not part of v1.0.0.
