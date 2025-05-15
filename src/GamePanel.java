import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (gameOver.isVisible() && gameOver.isRestartClicked(e.getX(), e.getY())) {
                    restartGame();
                }
            }
        });


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

        if (gameOver.isVisible()) {
            gameOver.draw(g, getWidth());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver.isTriggered()) {
            // Handle destruction display timing
            long now = System.currentTimeMillis();

            if (destructionStartTime == 0) {
                destructionStartTime = now;
            }

            if (now - destructionStartTime > DESTRUCTION_DISPLAY_MS) {
                // After destruction time, show Game Over screen
                showDestruction = false; // hide destruction animation
                gameOver.visible = true;  // show Game Over screen
            }

            repaint();
            return;  // Freeze game updates when Game Over triggered
        }

        long currentTime = System.currentTimeMillis();

        // Survival score update every second
        if (currentTime - lastSurvivalPointTime >= 1000) {
            scoreManager.addSurvivalPoint();
            lastSurvivalPointTime = currentTime;
        }

        // Spawn new enemy every ENEMY_SPAWN_DELAY ms
        if (currentTime - lastEnemySpawnTime >= ENEMY_SPAWN_DELAY) {
            spawnEnemy();
            lastEnemySpawnTime = currentTime;
        }

        // Move bullets
        for (Bullet bullet : bullets) {
            bullet.move();
        }

        // Move enemies and check collision with player
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.moveToward(player);

            if (checkCollision(enemy) && !gameOver.isTriggered()) {
                // Trigger game over sequence
                showDestruction = true;
                destructionX = player.x + player.width / 2 - 100; // assuming destruction image is 200px wide
                destructionY = player.y + player.height / 2 - 100;


                enemies.clear();
                bullets.clear();

                gameOver.trigger(scoreManager.getScore());

                destructionStartTime = 0;  // reset timer for destruction animation

                // Play sound once
                try {
                    java.net.URL soundURL = getClass().getResource("/Sound/dragon_roar.wav");
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
                    scoreManager.addKillPoints();
                    break;
                }
            }
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
                int bulletX = player.x + player.width / 2 - 4; // 4 is half bullet width
                int bulletY = player.y - 10;
                bullets.add(new Bullet(bulletX, bulletY));
                lastBulletLeftTime = currentTime;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
}
