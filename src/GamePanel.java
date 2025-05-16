import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private Player player;
    private ArrayList<Bullet> bullets;
    private ArrayList<Enemy> enemies;
    private Image backgroundImage;
    private ScoreManager scoreManager;
    private GameOver gameOver = new GameOver();
    private boolean gameOverStarted = false;

    private Image destructionImage;
    private boolean showDestruction = false;
    private int destructionX = 0, destructionY = 0;

    private Random rand = new Random();
    private long lastEnemySpawnTime = 0; // To control enemy spawn rate
    private final long ENEMY_SPAWN_DELAY = 2000;

    private long lastBulletLeftTime = 0;
    private long lastBulletRightTime = 0;
    private final long BULLET_FIRE_DELAY = 100;

    private long lastSurvivalPointTime = 0;

    // New fields for destruction timer
    private long destructionStartTime = 0;
    private final int DESTRUCTION_DISPLAY_MS = 1000;

    private Mystery mystery;
    private boolean powerUpActive = false;
    private long powerUpEndTime = 0;
    private long powerUpStartTime = 0;
    private final long POWER_UP_DURATION = 30000;
    private Clip powerUpClip;

    private void loadPowerUpSound() {
        try {
            URL soundURL = getClass().getResource("/Sound/mystery.wav");
            if (soundURL != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
                powerUpClip = AudioSystem.getClip();
                powerUpClip.open(audioIn);
            } else {
                System.err.println("Power-up sound file not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void activatePowerUp() {
        powerUpActive = true;
        powerUpStartTime = System.currentTimeMillis();
        powerUpEndTime = powerUpStartTime + POWER_UP_DURATION;

        if (powerUpClip != null) {
            if (powerUpClip.isRunning()) {
                powerUpClip.stop();
            }
            powerUpClip.setFramePosition(0);
            powerUpClip.start();
        }
    }

    private void restartGame() {
        GameOver.reset();
        gameOver.visible = false;
        gameOverStarted = false;

        enemies.clear();
        bullets.clear();
        scoreManager.reset();

        // Reset player position (adjust as needed)
        player.x = 400;
        player.y = 500;

        // Respawn initial enemies
        for (int i = 0; i < 5; i++) {
            spawnEnemy();
        }

        timer.start();
        repaint();
    }


    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);

        try {
            backgroundImage = new ImageIcon(getClass().getClassLoader().getResource("Image/Background.jpg"))
                    .getImage().getScaledInstance(800, 600, Image.SCALE_SMOOTH);
            if (backgroundImage == null) {
                System.out.println("Background image is null.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        player = new Player(400, 500, 100, 80);
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        scoreManager = new ScoreManager();

        // Initialize 5 enemies
        for (int i = 0; i < 5; i++) {
            spawnEnemy();
        }

        timer = new Timer(16, this);
        timer.start();
        lastSurvivalPointTime = System.currentTimeMillis();

        try {
            destructionImage = new ImageIcon(getClass().getClassLoader().getResource("Image/Destruction.png")).getImage();
        } catch (Exception e) {
            System.err.println("Destruction image not found.");
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameOver.isVisible() && gameOver.isRestartClicked(e.getX(), e.getY())) {
                    restartGame();
                }
            }
        });

        loadPowerUpSound();
        addKeyListener(this);
        setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        // Draw player only if game over screen is not visible
        if (!gameOver.shouldHidePlayer()) {
            player.draw(g);
        }


        for (Bullet bullet : bullets) {
            bullet.draw(g);
        }

        // Draw enemies only if destruction not showing
        if (!showDestruction) {
            for (Enemy enemy : enemies) {
                enemy.draw(g);
            }
        } else if (destructionImage != null) {
            g.drawImage(destructionImage, destructionX, destructionY, 200, 200, this);
        }

        if (!gameOver.isTriggered()) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString("Score: " + scoreManager.getScore(), 650, 30);
        }

        if (mystery != null && !gameOver.isTriggered()) {
            mystery.draw(g);
        }

        if (gameOver.isVisible()) {
            gameOver.draw(g, getWidth());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver.isTriggered()) {
            long now = System.currentTimeMillis();

            if (destructionStartTime == 0) {
                destructionStartTime = now;
            }

            if (now - destructionStartTime > DESTRUCTION_DISPLAY_MS) {
                showDestruction = false;
                gameOver.visible = true;
            }

            repaint();
            return;
        }

        long currentTime = System.currentTimeMillis();

        // Survival score update every second
        if (currentTime - lastSurvivalPointTime >= 1000) {
            if (powerUpActive && currentTime - powerUpStartTime <= POWER_UP_DURATION) {
                scoreManager.addSurvivalPoint();
                scoreManager.addSurvivalPoint(); // Double survival points
            } else {
                scoreManager.addSurvivalPoint();
            }
            lastSurvivalPointTime = currentTime;
        }

        // Spawn new enemy every ENEMY_SPAWN_DELAY ms
        if (currentTime - lastEnemySpawnTime >= ENEMY_SPAWN_DELAY) {
            spawnEnemy();
            lastEnemySpawnTime = currentTime;
        }

        for (Bullet bullet : bullets) {
            bullet.move();
        }

        if (mystery == null && rand.nextInt(1000) < 2) {
            mystery = new Mystery(rand.nextInt(760), 0);
        }
        if (mystery != null && !gameOver.isTriggered()) {
            mystery.move();
            if (mystery.y > getHeight()) {
                mystery = null;
            }
        }

        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.moveToward(player);

            if (checkCollision(enemy) && !gameOver.isTriggered()) {
                showDestruction = true;
                destructionX = player.x + player.width / 2 - 100;
                destructionY = player.y + player.height / 2 - 100;

                enemies.clear();
                bullets.clear();
                gameOver.trigger(scoreManager.getScore());
                destructionStartTime = 0;

                try {
                    URL soundURL = getClass().getResource("/Sound/dragon_roar.wav");
                    if (soundURL != null) {
                        AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
                        Clip clip = AudioSystem.getClip();
                        clip.open(audioIn);
                        clip.start();
                    } else {
                        System.err.println("Sound not found.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                break;
            }

            if (enemy.y > getHeight()) {
                enemies.remove(i);
            }
        }

        // Bullet-enemy collision detection
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            for (int j = enemies.size() - 1; j >= 0; j--) {
                Enemy enemy = enemies.get(j);
                if (bullet.collidesWith(enemy)) {
                    bullets.remove(i);
                    enemies.remove(j);

                    if (powerUpActive && currentTime - powerUpStartTime <= POWER_UP_DURATION) {
                        scoreManager.addKillPoints();
                        scoreManager.addKillPoints(); // Double kill points
                    } else {
                        scoreManager.addKillPoints();
                    }

                    break;
                }
            }
        }

        // Bullet-mystery collision detection
        if (mystery != null) {
            for (int i = bullets.size() - 1; i >= 0; i--) {
                Bullet bullet = bullets.get(i);
                if (bullet.getBounds().intersects(mystery.getBounds())) {
                    bullets.remove(i);
                    activatePowerUp();
                    mystery = null;
                    break;
                }
            }
        }

        if (powerUpActive && System.currentTimeMillis() > powerUpStartTime + POWER_UP_DURATION) {
            powerUpActive = false;
        }

        repaint();
    }

    private void spawnEnemy() {
        int side = rand.nextInt(3); // 0 = top, 1 = left, 2 = right
        int ex = 0, ey = 0;

        switch (side) {
            case 0: // top edge
                ex = rand.nextInt(800 - 40);
                ey = rand.nextInt(200);
                break;
            case 1: // left edge
                ex = 0;
                ey = rand.nextInt(300);
                break;
            case 2: // right edge
                ex = 800 - 40;
                ey = rand.nextInt(300);
                break;
        }

        enemies.add(new Enemy(ex, ey));
    }

    private boolean checkCollision(Enemy enemy) {
        Rectangle enemyRect = new Rectangle(enemy.x, enemy.y, enemy.getWidth(), enemy.getHeight());
        Rectangle playerRect = new Rectangle(player.x, player.y, player.width, player.height);
        return enemyRect.intersects(playerRect);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver.isTriggered()) return;
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_LEFT) {
            player.moveLeft();
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            player.moveRight();
        } else if (keyCode == KeyEvent.VK_SPACE) {
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastBulletLeftTime >= BULLET_FIRE_DELAY) {
                lastBulletLeftTime = currentTime;

                if (powerUpActive && currentTime <= powerUpEndTime) {
                    // Double bullet: left and right from center
                    int centerX = player.x + player.width / 2;
                    int bulletY = player.y - 10;
                    bullets.add(new Bullet(centerX - 20, bulletY)); // Left of center
                    bullets.add(new Bullet(centerX + 20, bulletY)); // Right of center
                } else {
                    // Single bullet from center
                    int bulletX = player.x + player.width / 2 - 4; // 4 is half bullet width
                    int bulletY = player.y - 10;
                    bullets.add(new Bullet(bulletX, bulletY));
                }
            }

        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
}
