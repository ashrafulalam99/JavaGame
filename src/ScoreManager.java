public class ScoreManager {
    private int score = 0;

    public void addSurvivalPoint() {
        score += 1;
    }

    public void addKillPoints() {
        score += 10;
    }

    public int getScore() {
        return score;
    }

    public void reset() {
        score = 0;
    }
}
