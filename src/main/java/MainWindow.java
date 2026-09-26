import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class MainWindow extends JFrame {
    private DecisionModel model;
    private LocalizationManager language;
    private JTextArea situationArea;
    private JTabbedPane optionTabs;
    private JTextArea resultsArea;
    private List<OptionPanel> optionPanels;

    public MainWindow(DecisionModel model, LocalizationManager language) {
        this.model = model;
        this.language = language;
        optionPanels = new ArrayList<OptionPanel>();

        setTitle(text("app.title") + " v1.0.0");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1240, 800);
        setMinimumSize(new Dimension(980, 650));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        JMenuBar menuBar = new JMenuBar();
        JMenu help = new JMenu(text("menu.help"));
        JMenuItem about = new JMenuItem(text("menu.about"));
        about.addActionListener(event -> showAbout());
        help.add(about);
        JMenuItem formula = new JMenuItem(text("menu.formula"));
        formula.addActionListener(event -> showFormula());
        help.add(formula);
        menuBar.add(help);
        setJMenuBar(menuBar);

        JPanel header = new JPanel(new BorderLayout(6, 6));
        header.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));
        JPanel titleLine = new JPanel(new BorderLayout());
        JLabel title = new JLabel(text("app.title"));
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        titleLine.add(title, BorderLayout.WEST);
        JPanel languagePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        languagePanel.add(new JLabel(text("label.language")));
        JComboBox<String> languages = new JComboBox<String>(
                LocalizationManager.LANGUAGE_NAMES);
        languages.setSelectedIndex(LocalizationManager.indexOf(language.getLanguageCode()));
        languages.addActionListener(event -> {
            String code = LocalizationManager.LANGUAGE_CODES[languages.getSelectedIndex()];
            switchLanguage(code);
        });
        languagePanel.add(languages);
        titleLine.add(languagePanel, BorderLayout.EAST);
        header.add(titleLine, BorderLayout.NORTH);

        JPanel situationPanel = new JPanel(new BorderLayout(4, 4));
        situationPanel.add(new JLabel(text("label.situation")), BorderLayout.NORTH);
        situationArea = new JTextArea(model.getSituation(), 3, 40);
        situationArea.setLineWrap(true);
        situationArea.setWrapStyleWord(true);
        situationPanel.add(new JScrollPane(situationArea), BorderLayout.CENTER);
        header.add(situationPanel, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        optionTabs = new JTabbedPane();
        for (DecisionOption option : model.getOptions()) {
            addOptionTab(option);
        }

        resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        resultsArea.setLineWrap(true);
        resultsArea.setWrapStyleWord(true);
        resultsArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        resultsArea.setText(text("label.instructions") + "\n\n"
                + text("app.short_disclaimer"));
        JScrollPane resultsScroll = new JScrollPane(resultsArea);
        resultsScroll.setBorder(BorderFactory.createTitledBorder(text("label.results")));
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                optionTabs, resultsScroll);
        split.setResizeWeight(0.64);
        add(split, BorderLayout.CENTER);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                split.setDividerLocation(0.64);
            }
        });

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBorder(BorderFactory.createEmptyBorder(4, 12, 12, 12));
        footer.add(new JLabel(text("app.status")));
        JButton addOption = new JButton(text("button.add_option"));
        addOption.addActionListener(event -> addCustomOption());
        footer.add(addOption);
        JButton calculate = new JButton(text("button.calculate"));
        calculate.addActionListener(event -> showResults());
        footer.add(calculate);
        add(footer, BorderLayout.SOUTH);
    }

    private String text(String key) {
        return language.text(key);
    }

    private String message(String key, String value) {
        return text(key).replace("{0}", value);
    }

    private String optionName(DecisionOption option) {
        if (option.isCustom()) {
            return option.getId();
        }
        return text(option.getId());
    }

    private String factorName(Factor factor) {
        if (factor.isCustom()) {
            return factor.getId();
        }
        return text(factor.getId());
    }

    private String categoryName(Factor factor) {
        if (factor.isCustom()) {
            return factor.getCategoryId();
        }
        return text(factor.getCategoryId());
    }

    private void addOptionTab(DecisionOption option) {
        OptionPanel panel = new OptionPanel(option);
        optionPanels.add(panel);
        optionTabs.addTab(optionName(option), panel);
    }

    private void saveInputs() {
        model.setSituation(situationArea.getText());
        for (OptionPanel panel : optionPanels) {
            panel.saveNotes();
        }
    }

    private void synchronizeImportance(Criterion criterion, int value) {
        for (OptionPanel panel : optionPanels) {
            panel.updateImportance(criterion, value);
        }
    }

    public MainWindow switchLanguage(String code) {
        if (language.getLanguageCode().equals(code)) {
            return this;
        }
        // Slider listeners already updated the model; save text before rebuilding UI.
        saveInputs();
        int selectedTab = optionTabs.getSelectedIndex();
        Dimension size = getSize();
        Point position = getLocation();
        language.setLanguage(code);
        language.saveLanguage();
        MainWindow replacement = new MainWindow(model, language);
        replacement.setSize(size);
        replacement.setLocation(position);
        replacement.setVisible(true);
        if (selectedTab >= 0 && selectedTab < replacement.optionTabs.getTabCount()) {
            replacement.optionTabs.setSelectedIndex(selectedTab);
        }
        dispose();
        return replacement;
    }

    private void addCustomOption() {
        String name = JOptionPane.showInputDialog(this, text("dialog.option_name"),
                text("button.add_option"), JOptionPane.PLAIN_MESSAGE);
        if (name == null) {
            return;
        }
        name = name.trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, text("dialog.option_empty"));
            return;
        }
        for (DecisionOption option : model.getOptions()) {
            if (optionName(option).equalsIgnoreCase(name)) {
                JOptionPane.showMessageDialog(this, text("dialog.option_duplicate"));
                return;
            }
        }
        DecisionOption option = model.addCustomOption(name);
        addOptionTab(option);
        optionTabs.setSelectedIndex(optionTabs.getTabCount() - 1);
    }

    private void showAbout() {
        String about = text("app.title") + " v1.0.0\n\n"
                + text("help.about") + "\n\n"
                + text("help.shared_importance") + "\n"
                + text("help.overlap") + "\n\n"
                + text("app.privacy") + "\n\n"
                + text("app.short_disclaimer") + "\n"
                + text("label.score_meaning");
        showHelpMessage(about, text("menu.about"));
    }

    private void showFormula() {
        String formula = text("help.formula") + "\n\n"
                + "C = W × (P+ × I+ − P− × I−)\n"
                + "S = 100 × Σ C\n\n"
                + text("help.formula_percent") + "\n"
                + text("help.benefit_harm") + "\n"
                + text("help.shared_importance") + "\n"
                + text("help.overlap") + "\n"
                + text("label.score_meaning");
        showHelpMessage(formula, text("menu.formula"));
    }

    private void showHelpMessage(String message, String title) {
        JTextArea body = new JTextArea(message);
        body.setEditable(false);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        body.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane scroll = new JScrollPane(body);
        scroll.setPreferredSize(new Dimension(640, 260));
        JOptionPane.showMessageDialog(this, scroll, title,
                JOptionPane.INFORMATION_MESSAGE);
    }

    private String formatScore(double value) {
        if (Math.abs(value) < 0.0000001) {
            value = 0.0;
        }
        return String.format(Locale.US, "%+.1f", value);
    }

    static String comparisonMessage(DecisionModel model, LocalizationManager language) {
        List<DecisionOption> evaluated = model.getEvaluatedOptions();
        if (evaluated.isEmpty()) {
            return language.text("result.none_evaluated");
        }
        if (evaluated.size() == 1) {
            return language.text("result.only_one_evaluated");
        }
        StringBuilder winners = new StringBuilder();
        List<DecisionOption> highest = model.getHighestScoringOptions();
        for (DecisionOption option : highest) {
            if (winners.length() > 0) {
                winners.append(", ");
            }
            winners.append(option.isCustom() ? option.getId() : language.text(option.getId()));
        }
        String key = highest.size() == 1 ? "result.winner" : "result.tie";
        return language.text(key).replace("{0}", winners.toString());
    }

    private void showResults() {
        saveInputs();
        if (model.getOptions().isEmpty()) {
            JOptionPane.showMessageDialog(this, text("dialog.no_options"));
            return;
        }
        for (int index = 0; index < model.getOptions().size(); index++) {
            DecisionOption option = model.getOptions().get(index);
            for (Factor factor : option.getFactors()) {
                if (factor.isEnabled() && !factor.isProbabilityValid()) {
                    optionTabs.setSelectedIndex(index);
                    JOptionPane.showMessageDialog(this,
                            message("dialog.invalid_probability", factorName(factor)),
                            text("dialog.invalid_title"), JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
        }

        StringBuilder result = new StringBuilder();
        result.append(text("app.short_disclaimer")).append("\n");
        result.append(text("label.text_not_scored")).append("\n");
        result.append(text("label.score_meaning")).append("\n\n");
        if (!model.getSituation().trim().isEmpty()) {
            result.append(text("label.situation_result")).append(": ")
                    .append(model.getSituation()).append("\n\n");
        }

        result.append(text("label.comparison")).append("\n");
        List<DecisionOption> evaluated = model.getEvaluatedOptions();
        for (DecisionOption option : model.getOptions()) {
            result.append(optionName(option)).append(": ");
            if (evaluated.contains(option)) {
                result.append(formatScore(option.calculateScore()));
            } else {
                result.append(text("label.not_evaluated"));
            }
            result.append("\n");
        }

        result.append("\n").append(comparisonMessage(model, language)).append("\n");

        result.append("\n").append(text("label.breakdown")).append("\n");
        for (DecisionOption option : model.getOptions()) {
            result.append("\n").append(optionName(option)).append("\n");
            if (!option.getNotes().trim().isEmpty()) {
                result.append(text("label.option_notes_result")).append(": ")
                        .append(option.getNotes()).append("\n");
            }
            int enabledCount = 0;
            for (Factor factor : option.getFactors()) {
                if (factor.isEnabled()) {
                    enabledCount++;
                    result.append("  ").append(factorName(factor)).append("\n");
                    result.append("    ").append(text("label.importance")).append(": ")
                            .append(factor.getImportance()).append("%\n");
                    result.append("    ").append(text("label.possible_benefit")).append(": ")
                            .append(factor.getBenefitProbability()).append("% × ")
                            .append(factor.getBenefitImpact()).append("%\n");
                    result.append("    ").append(text("label.possible_harm")).append(": ")
                            .append(factor.getHarmProbability()).append("% × ")
                            .append(factor.getHarmImpact()).append("%\n");
                    result.append("    ").append(text("label.neutral")).append(": ")
                            .append(factor.getNeutralProbability()).append("%\n");
                    result.append("    ").append(text("label.contribution")).append(": ")
                            .append(formatScore(100.0 * factor.calculateContribution()))
                            .append("\n");
                    if (!factor.getNotes().trim().isEmpty()) {
                        result.append("    ").append(text("label.factor_notes_result"))
                                .append(": ").append(factor.getNotes()).append("\n");
                    }
                }
            }
            if (enabledCount == 0) {
                result.append("  ").append(text("label.no_factors")).append("\n");
            }
            result.append(text("label.total")).append(": ")
                    .append(enabledCount == 0 ? text("label.not_evaluated")
                            : formatScore(option.calculateScore())).append("\n");
        }
        resultsArea.setText(result.toString());
        resultsArea.setCaretPosition(0);
    }

    private JPanel sliderPanel(String title, JSlider slider, JLabel value) {
        JPanel panel = new JPanel(new BorderLayout(2, 2));
        panel.add(new JLabel(title), BorderLayout.NORTH);
        panel.add(slider, BorderLayout.CENTER);
        panel.add(value, BorderLayout.EAST);
        return panel;
    }

    private JSlider dialogSlider(int initial, JLabel value) {
        JSlider slider = new JSlider(0, 100, initial);
        slider.addChangeListener(event -> value.setText(slider.getValue() + "%"));
        return slider;
    }

    private void updateNeutral(JLabel label, int benefit, int harm) {
        int total = benefit + harm;
        if (total > 100) {
            label.setText(text("label.invalid_probability") + " (" + total + "%)");
            label.setForeground(Color.RED.darker());
        } else {
            label.setText(text("label.neutral") + ": " + (100 - total) + "%");
            label.setForeground(Color.BLACK);
        }
        label.setToolTipText(text("tooltip.neutral"));
    }

    private class OptionPanel extends JPanel {
        private DecisionOption option;
        private JTextArea notesArea;
        private JPanel factorsPanel;
        private Map<Factor, JTextField> factorNoteFields;
        private Map<Criterion, JSlider> importanceSliders;

        public OptionPanel(DecisionOption option) {
            this.option = option;
            factorNoteFields = new LinkedHashMap<Factor, JTextField>();
            importanceSliders = new LinkedHashMap<Criterion, JSlider>();
            setLayout(new BorderLayout(6, 6));
            setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            JPanel notesPanel = new JPanel(new BorderLayout(4, 4));
            notesPanel.add(new JLabel(text("label.option_notes")), BorderLayout.NORTH);
            notesArea = new JTextArea(option.getNotes(), 2, 30);
            notesArea.setLineWrap(true);
            notesArea.setWrapStyleWord(true);
            notesPanel.add(new JScrollPane(notesArea), BorderLayout.CENTER);
            add(notesPanel, BorderLayout.NORTH);

            factorsPanel = new JPanel();
            factorsPanel.setLayout(new BoxLayout(factorsPanel, BoxLayout.Y_AXIS));
            JScrollPane scroll = new JScrollPane(factorsPanel);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            add(scroll, BorderLayout.CENTER);

            JButton addFactor = new JButton(text("button.add_factor"));
            addFactor.addActionListener(event -> addCustomFactor());
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
            bottom.add(addFactor);
            add(bottom, BorderLayout.SOUTH);
            refreshFactors();
        }

        public void saveNotes() {
            option.setNotes(notesArea.getText());
            for (Map.Entry<Factor, JTextField> entry : factorNoteFields.entrySet()) {
                entry.getKey().setNotes(entry.getValue().getText());
            }
        }

        public void updateImportance(Criterion criterion, int value) {
            JSlider slider = importanceSliders.get(criterion);
            if (slider != null && slider.getValue() != value) {
                slider.setValue(value);
            }
        }

        private void refreshFactors() {
            saveNotes();
            factorNoteFields.clear();
            importanceSliders.clear();
            factorsPanel.removeAll();
            Map<String, JPanel> categories = new LinkedHashMap<String, JPanel>();
            for (Factor factor : option.getFactors()) {
                String categoryId = factor.getCategoryId();
                JPanel categoryPanel = categories.get(categoryId);
                if (categoryPanel == null) {
                    categoryPanel = new JPanel();
                    categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));
                    categoryPanel.setBorder(BorderFactory.createTitledBorder(
                            categoryName(factor)));
                    categories.put(categoryId, categoryPanel);
                }
                categoryPanel.add(createFactorCard(factor));
            }
            for (JPanel categoryPanel : categories.values()) {
                factorsPanel.add(categoryPanel);
            }
            factorsPanel.revalidate();
            factorsPanel.repaint();
        }

        private JPanel createFactorCard(Factor factor) {
            JPanel card = new JPanel(new BorderLayout(4, 4));
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEtchedBorder(),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JCheckBox enabled = new JCheckBox(factorName(factor), factor.isEnabled());
            top.add(enabled);
            if (factor.isCustom()) {
                JButton remove = new JButton(text("button.remove_factor"));
                remove.addActionListener(event -> {
                    option.getFactors().remove(factor);
                    refreshFactors();
                });
                top.add(remove);
            }
            card.add(top, BorderLayout.NORTH);

            JPanel details = new JPanel();
            details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
            JSlider importance = new JSlider(0, 100, factor.getImportance());
            JLabel importanceValue = new JLabel(factor.getImportance() + "%");
            importanceSliders.put(factor.getCriterion(), importance);
            importance.setToolTipText(text(factor.isCustom()
                    ? "tooltip.importance_custom" : "tooltip.importance"));
            importance.addChangeListener(event -> {
                factor.setImportance(importance.getValue());
                importanceValue.setText(importance.getValue() + "%");
                synchronizeImportance(factor.getCriterion(), importance.getValue());
            });
            details.add(sliderPanel(text(factor.isCustom()
                    ? "label.importance" : "label.importance_shared"),
                    importance, importanceValue));

            JSlider benefitProbability = new JSlider(0, 100,
                    factor.getBenefitProbability());
            JLabel benefitProbabilityValue = new JLabel(factor.getBenefitProbability() + "%");
            JSlider benefitImpact = new JSlider(0, 100, factor.getBenefitImpact());
            JLabel benefitImpactValue = new JLabel(factor.getBenefitImpact() + "%");
            JSlider harmProbability = new JSlider(0, 100, factor.getHarmProbability());
            JLabel harmProbabilityValue = new JLabel(factor.getHarmProbability() + "%");
            JSlider harmImpact = new JSlider(0, 100, factor.getHarmImpact());
            JLabel harmImpactValue = new JLabel(factor.getHarmImpact() + "%");
            JLabel neutral = new JLabel();

            benefitProbability.setToolTipText(text("tooltip.probability") + " "
                    + text("help.benefit_harm"));
            harmProbability.setToolTipText(text("tooltip.probability") + " "
                    + text("help.benefit_harm"));
            benefitImpact.setToolTipText(text("tooltip.impact"));
            harmImpact.setToolTipText(text("tooltip.impact"));
            benefitProbability.addChangeListener(event -> {
                factor.setBenefitProbability(benefitProbability.getValue());
                benefitProbabilityValue.setText(benefitProbability.getValue() + "%");
                updateNeutral(neutral, factor.getBenefitProbability(),
                        factor.getHarmProbability());
            });
            benefitImpact.addChangeListener(event -> {
                factor.setBenefitImpact(benefitImpact.getValue());
                benefitImpactValue.setText(benefitImpact.getValue() + "%");
            });
            harmProbability.addChangeListener(event -> {
                factor.setHarmProbability(harmProbability.getValue());
                harmProbabilityValue.setText(harmProbability.getValue() + "%");
                updateNeutral(neutral, factor.getBenefitProbability(),
                        factor.getHarmProbability());
            });
            harmImpact.addChangeListener(event -> {
                factor.setHarmImpact(harmImpact.getValue());
                harmImpactValue.setText(harmImpact.getValue() + "%");
            });

            JPanel outcomes = new JPanel(new GridLayout(1, 2, 8, 0));
            JPanel benefit = new JPanel(new GridLayout(2, 1, 2, 2));
            benefit.setToolTipText(text("help.benefit_harm"));
            benefit.setBorder(BorderFactory.createTitledBorder(
                    text("label.possible_benefit")));
            benefit.add(sliderPanel(text("label.probability"), benefitProbability,
                    benefitProbabilityValue));
            benefit.add(sliderPanel(text("label.impact"), benefitImpact,
                    benefitImpactValue));
            JPanel harm = new JPanel(new GridLayout(2, 1, 2, 2));
            harm.setToolTipText(text("help.benefit_harm"));
            harm.setBorder(BorderFactory.createTitledBorder(text("label.possible_harm")));
            harm.add(sliderPanel(text("label.probability"), harmProbability,
                    harmProbabilityValue));
            harm.add(sliderPanel(text("label.impact"), harmImpact,
                    harmImpactValue));
            outcomes.add(benefit);
            outcomes.add(harm);
            details.add(outcomes);
            updateNeutral(neutral, factor.getBenefitProbability(),
                    factor.getHarmProbability());
            details.add(neutral);

            if (factor.isCustom()) {
                JPanel notePanel = new JPanel(new BorderLayout(4, 4));
                notePanel.add(new JLabel(text("label.factor_notes")), BorderLayout.WEST);
                JTextField noteField = new JTextField(factor.getNotes());
                factorNoteFields.put(factor, noteField);
                notePanel.add(noteField, BorderLayout.CENTER);
                details.add(notePanel);
            }
            details.setVisible(factor.isEnabled());
            card.add(details, BorderLayout.CENTER);
            enabled.addActionListener(event -> {
                factor.setEnabled(enabled.isSelected());
                details.setVisible(enabled.isSelected());
                card.revalidate();
                factorsPanel.revalidate();
            });
            return card;
        }

        private void addCustomFactor() {
            JTextField nameField = new JTextField();
            JTextField categoryField = new JTextField(text("category.custom"));
            JCheckBox enabled = new JCheckBox(text("label.enabled"), true);
            JLabel importanceValue = new JLabel("50%");
            JLabel benefitProbabilityValue = new JLabel("0%");
            JLabel benefitImpactValue = new JLabel("50%");
            JLabel harmProbabilityValue = new JLabel("0%");
            JLabel harmImpactValue = new JLabel("50%");
            JSlider importance = dialogSlider(50, importanceValue);
            importance.setToolTipText(text("tooltip.importance_custom"));
            JSlider benefitProbability = dialogSlider(0, benefitProbabilityValue);
            benefitProbability.setToolTipText(text("help.benefit_harm"));
            JSlider benefitImpact = dialogSlider(50, benefitImpactValue);
            JSlider harmProbability = dialogSlider(0, harmProbabilityValue);
            harmProbability.setToolTipText(text("help.benefit_harm"));
            JSlider harmImpact = dialogSlider(50, harmImpactValue);
            JLabel neutral = new JLabel();
            benefitProbability.addChangeListener(event -> updateNeutral(neutral,
                    benefitProbability.getValue(), harmProbability.getValue()));
            harmProbability.addChangeListener(event -> updateNeutral(neutral,
                    benefitProbability.getValue(), harmProbability.getValue()));
            updateNeutral(neutral, 0, 0);
            JTextArea noteField = new JTextArea(2, 25);
            noteField.setLineWrap(true);
            noteField.setWrapStyleWord(true);

            JPanel form = new JPanel();
            form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
            form.add(new JLabel(text("dialog.factor_name")));
            form.add(nameField);
            form.add(new JLabel(text("dialog.factor_category")));
            form.add(categoryField);
            form.add(enabled);
            form.add(sliderPanel(text("label.importance"), importance, importanceValue));
            form.add(new JLabel(text("label.possible_benefit")));
            form.add(sliderPanel(text("label.probability"), benefitProbability,
                    benefitProbabilityValue));
            form.add(sliderPanel(text("label.impact"), benefitImpact,
                    benefitImpactValue));
            form.add(new JLabel(text("label.possible_harm")));
            form.add(sliderPanel(text("label.probability"), harmProbability,
                    harmProbabilityValue));
            form.add(sliderPanel(text("label.impact"), harmImpact,
                    harmImpactValue));
            form.add(neutral);
            form.add(new JLabel(text("label.factor_notes")));
            form.add(new JScrollPane(noteField));

            while (true) {
                int answer = JOptionPane.showConfirmDialog(MainWindow.this, form,
                        text("button.add_factor"), JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE);
                if (answer != JOptionPane.OK_OPTION) {
                    return;
                }
                if (nameField.getText().trim().isEmpty()
                        || categoryField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(MainWindow.this,
                            text("dialog.factor_empty"));
                    continue;
                }
                if (benefitProbability.getValue() + harmProbability.getValue() > 100) {
                    JOptionPane.showMessageDialog(MainWindow.this,
                            text("label.invalid_probability"));
                    continue;
                }
                break;
            }

            Factor factor = new Factor(nameField.getText().trim(),
                    categoryField.getText().trim(), true);
            factor.setEnabled(enabled.isSelected());
            factor.setImportance(importance.getValue());
            factor.setBenefitProbability(benefitProbability.getValue());
            factor.setBenefitImpact(benefitImpact.getValue());
            factor.setHarmProbability(harmProbability.getValue());
            factor.setHarmImpact(harmImpact.getValue());
            factor.setNotes(noteField.getText());
            option.getFactors().add(factor);
            refreshFactors();
        }
    }
}
