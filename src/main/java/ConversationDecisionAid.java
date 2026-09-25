import java.awt.BorderLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class ConversationDecisionAid {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                String code = LocalizationManager.loadSavedLanguage();
                if (code == null) {
                    code = chooseFirstLanguage();
                    if (code == null) {
                        return;
                    }
                }
                LocalizationManager language = new LocalizationManager(code);
                language.saveLanguage();
                MainWindow window = new MainWindow(new DecisionModel(), language);
                window.setVisible(true);
            }
        });
    }

    private static String chooseFirstLanguage() {
        LocalizationManager english = new LocalizationManager("en");
        JComboBox<String> choices = new JComboBox<String>(
                LocalizationManager.LANGUAGE_NAMES);
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(new JLabel(english.text("start.choose")), BorderLayout.NORTH);
        panel.add(choices, BorderLayout.CENTER);
        String[] buttons = {
            english.text("start.continue"), english.text("start.cancel")
        };
        int answer = JOptionPane.showOptionDialog(null, panel,
                english.text("app.title"), JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, buttons, buttons[0]);
        if (answer != 0) {
            return null;
        }
        return LocalizationManager.LANGUAGE_CODES[choices.getSelectedIndex()];
    }
}
