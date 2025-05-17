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
    private GameRenderer renderer;

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
    private final long POWER_UP_DURATION = 15000;
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

        renderer = new GameRenderer(this);
        addKeyListener(new KeyHandler(player, this));
        loadPowerUpSound();
        addKeyListener(this);
        setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderer.render(g);
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
                ex = rand.nextInt(800 - 80);
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

    public boolean isGameOver() {
        return gameOver.isTriggered();
    }


    private boolean checkCollision(Enemy enemy) {
        Rectangle enemyRect = new Rectangle(enemy.x, enemy.y, enemy.getWidth(), enemy.getHeight());
        Rectangle playerRect = new Rectangle(player.x, player.y, player.width, player.height);
        return enemyRect.intersects(playerRect);
    }

    public void handleBulletFire() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastBulletLeftTime >= BULLET_FIRE_DELAY) {
            lastBulletLeftTime = currentTime;

            if (powerUpActive && currentTime <= powerUpEndTime) {
                int centerX = player.x + player.width / 2;
                int bulletY = player.y - 10;
                bullets.add(new Bullet(centerX - 20, bulletY));
                bullets.add(new Bullet(centerX + 20, bulletY));
            } else {
                int bulletX = player.x + player.width / 2 - 4; // Assuming bullet is 8px wide
                int bulletY = player.y - 10;
                bullets.add(new Bullet(bulletX, bulletY));
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    public Player getPlayer() { return player; }
    public ArrayList<Bullet> getBullets() { return bullets; }
    public ArrayList<Enemy> getEnemies() { return enemies; }
    public Image getBackgroundImage() { return backgroundImage; }
    public GameOver getGameOver() { return gameOver; }
    public boolean isShowDestruction() { return showDestruction; }
    public Image getDestructionImage() { return destructionImage; }
    public int getDestructionX() { return destructionX; }
    public int getDestructionY() { return destructionY; }
    public ScoreManager getScoreManager() { return scoreManager; }
    public Mystery getMystery() { return mystery; }
}
