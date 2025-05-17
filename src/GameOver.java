import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.net.URL;

public class GameOver {
    public static boolean triggered = false;
    private int finalScore = 0;
    boolean visible = false;
    private Clip clip;

    private static int highScore = 0;

    // Restart button rectangle
    private Rectangle restartButton = new Rectangle(320, 350, 160, 50);

    public void trigger(int score) {
        if (triggered) return;
        finalScore = score;
        if (score > highScore) {
            highScore = score;
        }
        triggered = true; // Freeze game
        playSound("/Sound/dragon_roar.wav");
    }

    public static boolean isTriggered() {
        return triggered;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean shouldHidePlayer() {
        return visible;
    }

    public void draw(Graphics g, int panelWidth) {
        if (!visible) return;

        int y = 200; // Start Y a bit higher
        int gap = 50; // Increased vertical gap

        g.setFont(new Font("Arial", Font.BOLD, 50));
        g.setColor(Color.RED);
        String gameOverText = "GAME OVER";
        int gameOverX = (panelWidth - g.getFontMetrics().stringWidth(gameOverText)) / 2;
        g.drawString(gameOverText, gameOverX, y);

        y += gap;

        g.setFont(new Font("Arial", Font.ITALIC, 30));
        g.setColor(Color.GREEN);
        String scoreText = "Your Score: " + finalScore;
        int scoreX = (panelWidth - g.getFontMetrics().stringWidth(scoreText)) / 2;
        g.drawString(scoreText, scoreX, y);

        y += gap;

        g.setColor(Color.WHITE);
        String highScoreText = "High Score: " + highScore;
        int highScoreX = (panelWidth - g.getFontMetrics().stringWidth(highScoreText)) / 2;
        g.drawString(highScoreText, highScoreX, y);

        y += gap;

        // Draw Restart button centered
        int buttonWidth = 200;
        int buttonHeight = 60;
        int buttonX = (panelWidth - buttonWidth) / 2;
        int buttonY = y;

        restartButton.setBounds(buttonX, buttonY, buttonWidth, buttonHeight);

        g.setColor(Color.CYAN);
        g.fillRect(buttonX, buttonY, buttonWidth, buttonHeight);

        // Center "Restart" text inside the button
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        String restartText = "Restart";
        FontMetrics fm = g.getFontMetrics();
        int textX = buttonX + (buttonWidth - fm.stringWidth(restartText)) / 2;
        int textY = buttonY + ((buttonHeight - fm.getHeight()) / 2) + fm.getAscent();
        g.drawString(restartText, textX, textY);
    }


    private void playSound(String filePath) {
        try {
            URL soundURL = getClass().getResource(filePath);
            if (soundURL == null) {
                System.err.println("Sound file not found: " + filePath);
                visible = true;
                return;
            }

            AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundURL);
            clip = AudioSystem.getClip();
            clip.open(audioInput);

            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                    visible = true; // Show Game Over screen *after* sound ends
                }
            });

            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
            visible = true; // On error, just show the screen
        }
    }

    // Check if mouse click is inside restart button
    public boolean isRestartClicked(int mouseX, int mouseY) {
        return restartButton.contains(mouseX, mouseY);
    }

    // Reset GameOver state for restarting game
    public static void reset() {
        triggered = false;
    }
}
