import java.awt.*;

public class GameOver {
    public static boolean triggered = false;
    private int finalScore = 0;

    public void trigger(int score) {
        triggered = true;
        finalScore = score;
    }

    public boolean isTriggered() {
        return triggered;
    }

    public int getFinalScore() {
        return finalScore;
    }

    public void draw(Graphics g) {
        if (triggered) {
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.setColor(Color.RED);
            g.drawString("GAME OVER", 300, 250);

            g.setFont(new Font("Arial", Font.PLAIN, 30));
            g.setColor(Color.BLUE);
            g.drawString("Your Score: " + finalScore, 320, 300);
        }
    }
}
