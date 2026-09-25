import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class LocalizationManager {
    public static final String[] LANGUAGE_CODES = {
        "en", "zh-CN", "zh-TW", "ja", "ko", "es", "fr", "de", "pt", "it",
        "ru", "uk", "tr", "vi", "id", "th", "ms", "pl", "nl", "cs"
    };
    public static final String[] LANGUAGE_NAMES = {
        "English", "简体中文", "繁體中文", "日本語", "한국어", "Español",
        "Français", "Deutsch", "Português", "Italiano", "Русский",
        "Українська", "Türkçe", "Tiếng Việt", "Bahasa Indonesia",
        "ไทย", "Bahasa Melayu", "Polski", "Nederlands", "Čeština"
    };

    private static final String PREFERENCE_KEY = "language";
    private String languageCode;
    private ResourceBundle bundle;
    private ResourceBundle english;

    public LocalizationManager(String languageCode) {
        english = ResourceBundle.getBundle("messages", Locale.ENGLISH);
        setLanguage(languageCode);
    }

    public void setLanguage(String code) {
        if (!isSupported(code)) {
            code = "en";
        }
        languageCode = code;
        bundle = ResourceBundle.getBundle("messages", Locale.forLanguageTag(code));
    }

    public String text(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException ignored) {
            try {
                return english.getString(key);
            } catch (MissingResourceException alsoMissing) {
                return key;
            }
        }
    }

    public String getLanguageCode() { return languageCode; }

    public static boolean isSupported(String code) {
        for (String supported : LANGUAGE_CODES) {
            if (supported.equals(code)) {
                return true;
            }
        }
        return false;
    }

    public static int indexOf(String code) {
        for (int i = 0; i < LANGUAGE_CODES.length; i++) {
            if (LANGUAGE_CODES[i].equals(code)) {
                return i;
            }
        }
        return 0;
    }

    public static String loadSavedLanguage() {
        // Preferences stores only the language choice, never personal decision data.
        String saved = Preferences.userNodeForPackage(LocalizationManager.class)
                .get(PREFERENCE_KEY, "");
        if (isSupported(saved)) {
            return saved;
        }
        return null;
    }

    public void saveLanguage() {
        Preferences.userNodeForPackage(LocalizationManager.class)
                .put(PREFERENCE_KEY, languageCode);
    }
}
