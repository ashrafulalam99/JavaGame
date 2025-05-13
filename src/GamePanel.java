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


    private Random rand = new Random();
    private long lastEnemySpawnTime = 0; // To control enemy spawn rate
    private final long ENEMY_SPAWN_DELAY = 2000;

    private long lastBulletLeftTime = 0;
    private long lastBulletRightTime = 0;
    private final long BULLET_FIRE_DELAY = 100;

    private long lastSurvivalPointTime = 0;

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

        player = new Player(400, 500, 100, 80); // Adjusted player size
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        Random rand = new Random();
        scoreManager = new ScoreManager();


        // Initialize 5 enemies
        for (int i = 0; i < 5; i++) {
            spawnEnemy(); // We use spawnEnemy to spawn randomly at edges
        }

        timer = new Timer(16, this);
        timer.start();
        lastSurvivalPointTime = System.currentTimeMillis();

        addKeyListener(this);
        setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw background image if available
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        // Draw player
        player.draw(g);

        // Draw bullets
        for (Bullet bullet : bullets) {
            bullet.draw(g);
        }

        // Draw enemies
        for (Enemy enemy : enemies) {
            enemy.draw(g);
        }

        if(!GameOver.triggered) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString("Score: " + scoreManager.getScore(), 650, 30);
        }
        gameOver.draw(g);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver.isTriggered()) return;

        long currentTime = System.currentTimeMillis();

        // Survival score update
        if (currentTime - lastSurvivalPointTime >= 1000) {
            scoreManager.addSurvivalPoint();
            lastSurvivalPointTime = currentTime;
        }

        // Spawn a new enemy every 2 seconds
        if (currentTime - lastEnemySpawnTime >= ENEMY_SPAWN_DELAY) {
            spawnEnemy();
            lastEnemySpawnTime = currentTime;
        }

        // Move bullets
        for (Bullet bullet : bullets) {
            bullet.move();
        }

        // Move enemies and check for collision with player
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.moveToward(player);

            // Check collision with player
            if (checkCollision(enemy)) {
                gameOver.trigger(scoreManager.getScore());
                timer.stop();
                break;
            }

            // Remove enemy if it goes below the screen
            if (enemy.y > getHeight()) {
                enemies.remove(i);
            }
        }

        // Bullet-enemy collision
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


    // Method to spawn enemies at random positions on the edges of the screen
    private void spawnEnemy() {
        int side = rand.nextInt(3); // 0 = top, 1 = left, 2 = right
        int ex = 0, ey = 0;

        switch (side) {
            case 0: // top edge
                ex = rand.nextInt(800 - 40); // screen width - enemy width
                ey = rand.nextInt(200); // upper 200 px of screen
                break;
            case 1: // left edge
                ex = 0;
                ey = rand.nextInt(300); // Left side height
                break;
            case 2: // right edge
                ex = 800 - 40; // Right side width
                ey = rand.nextInt(300); // Right side height
                break;
        }

        enemies.add(new Enemy(ex, ey));
    }

    private boolean checkCollision(Enemy enemy) {
        // Define the bounding rectangles for player and enemy
        Rectangle enemyRect = new Rectangle(enemy.x, enemy.y, enemy.getWidth(), enemy.getHeight());
        Rectangle playerRect = new Rectangle(player.x, player.y, player.width, player.height);

        return enemyRect.intersects(playerRect); // Check if they intersect (i.e., collide)
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

            // Left side bullet generation with a time gap
            if (currentTime - lastBulletLeftTime >= BULLET_FIRE_DELAY) {
                int bulletXLeft = player.x + 10; // Slightly to the left of the player's center
                int bulletYLeft = player.y - 10; // Just above the player's top
                bullets.add(new Bullet(bulletXLeft, bulletYLeft));
                lastBulletLeftTime = currentTime;
            }

            // Right side bullet generation with a time gap
            if (currentTime - lastBulletRightTime >= BULLET_FIRE_DELAY) {
                int bulletXRight = player.x + player.width - 14; // Slightly to the right of the player's center
                int bulletYRight = player.y - 10; // Just above the player's top
                bullets.add(new Bullet(bulletXRight, bulletYRight));
                lastBulletRightTime = currentTime;
            }
        }
    }


    @Override
    public void keyReleased(KeyEvent e) {}
}
