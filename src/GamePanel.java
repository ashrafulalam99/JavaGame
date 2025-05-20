import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
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
    private long lastEnemySpawnTime = 0;
    private final long ENEMY_SPAWN_DELAY = 2000;

    private long lastBulletLeftTime = 0;
    private final long BULLET_FIRE_DELAY = 100;

    private long lastSurvivalPointTime = 0;
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

        player = new Player(400, 500, 100, 80);
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            spawnEnemy();
        }

        scoreManager.reset();
        lastEnemySpawnTime = System.currentTimeMillis();
        lastSurvivalPointTime = System.currentTimeMillis();
        lastBulletLeftTime = 0;

        mystery = null;
        powerUpActive = false;
        powerUpStartTime = 0;
        powerUpEndTime = 0;

        showDestruction = false;
        destructionStartTime = 0;

        timer.restart();
        repaint();
    }

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);

        try {
            backgroundImage = new ImageIcon(getClass().getClassLoader().getResource("Image/Background.jpg"))
                    .getImage().getScaledInstance(800, 600, Image.SCALE_SMOOTH);
        } catch (Exception e) {
            e.printStackTrace();
        }

        player = new Player(400, 500, 100, 80);
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        scoreManager = new ScoreManager();

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
        if (currentTime - lastSurvivalPointTime >= 1000) {
            scoreManager.addSurvivalPoint();
            if (powerUpActive && currentTime - powerUpStartTime <= POWER_UP_DURATION) {
                scoreManager.addSurvivalPoint();  // Double point
            }
            lastSurvivalPointTime = currentTime;
        }

        if (currentTime - lastEnemySpawnTime >= ENEMY_SPAWN_DELAY) {
            spawnEnemy();
            lastEnemySpawnTime = currentTime;
        }

        for (Bullet bullet : bullets) bullet.move();

        if (mystery == null && rand.nextInt(1000) < 2) {
            mystery = new Mystery(rand.nextInt(800 - 40), 0); // Assume mystery width is 40
        }
        if (mystery != null && !gameOver.isTriggered()) {
            mystery.move();
            if (mystery.y > getHeight()) mystery = null;
        }

        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.moveToward(player);

            if (checkCollision(enemy)) {
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
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break;
            }

            if (enemy.y > getHeight()) enemies.remove(i);
        }

        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            for (int j = enemies.size() - 1; j >= 0; j--) {
                Enemy enemy = enemies.get(j);
                if (bullet.collidesWith(enemy)) {
                    bullets.remove(i);
                    enemies.remove(j);
                    scoreManager.addKillPoints();
                    if (powerUpActive && currentTime - powerUpStartTime <= POWER_UP_DURATION) {
                        scoreManager.addKillPoints(); // Double kill points
                    }
                    break;
                }
            }
        }

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
        int side = rand.nextInt(3);
        int ex = 0, ey = 0;
        int enemyWidth = 80;

        switch (side) {
            case 0: // Top
                ex = rand.nextInt(800 - enemyWidth);
                ey = rand.nextInt(200);
                break;
            case 1: // Left
                ex = 0;
                ey = rand.nextInt(300);
                break;
            case 2: // Right
                ex = 800 - enemyWidth;
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
            }
            else {
                int bulletX = player.x + player.width / 2 - 4;
                int bulletY = player.y - 10;
                bullets.add(new Bullet(bulletX, bulletY));
            }
        }
    }

    // Getters
    public Player getPlayer() { return player;}
    public ArrayList<Bullet> getBullets() { return bullets;}
    public ArrayList<Enemy> getEnemies() { return enemies;}
    public Image getBackgroundImage() { return backgroundImage;}
    public GameOver getGameOver() { return gameOver;}
    public boolean isShowDestruction() { return showDestruction;}
    public Image getDestructionImage() { return destructionImage;}
    public int getDestructionX() { return destructionX;}
    public int getDestructionY() { return destructionY;}
    public ScoreManager getScoreManager() { return scoreManager;}
    public Mystery getMystery() { return mystery;}
}