import java.util.prefs.Preferences;

public class HighScoreManager {
    private static final Preferences prefs = Preferences.userRoot().node(HighScoreManager.class.getName());

    public static int getHighScore(GameLauncher.GameMode mode) {
        return prefs.getInt("highscore_" + mode.name(), 0);
    }

    public static boolean saveScoreIfNewBest(GameLauncher.GameMode mode, int currentScore) {
        int bestScore = getHighScore(mode);

        if (currentScore > bestScore) {
            prefs.putInt("highscore_" + mode.name(), currentScore);
            return true;
        }
        return false;
    }
}