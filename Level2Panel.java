
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

//this is the level two class user will be taken to after they beat the first level and travel to the second level
public class Level2Panel extends JPanel implements MouseMotionListener, MouseListener, KeyListener, ActionListener {
    int babyZombieDeathCount = 0;
    Image background, characterImage, bowImage, arrowImage, coinImage, zombieImage;
    List<Zombie> zombies;
    Timer animationTimer, arrowTimer, swingTimer, respawnTimer, coinTimer;
    List<Point> trajectoryPoints = new ArrayList<>();
    List<Point> coinPoints = new ArrayList<>();
    boolean isDragging = false;
    Point mousePress, releasePoint;
    int arrowX, arrowY;
    int charY = 350, charX = 100;
    int bowX = 115, bowY = 315;
    double arrowAngle = 0;
    Main manager;
    CardLayout cards;
    int waveCount = 1;
    boolean arrowVisible = true;
    JLabel returnToMapLabel;
    JButton returnToMapButton;
    boolean showConfetti = false;
    JLabel coinsLabel;
    boolean droppedCoin = false;
    int count;
    ShopPanel shopPanel;
    boolean firstCoinDrop = false;
    Image babyZombieImage;
    List<BabyZombie> babyZombies = new ArrayList<>();
    boolean shouldBeRemoved = false;
    double swingOffset = 0;
    double swingSpeed = 0.005;
    double swingAmplitude = 35;
    Image wind;
    Image smashBall;

    boolean smashBallVisible = false;
    int smashBallX = 1000;
    int smashBallY = 1000;
    int smashBallHealth = 2;
    Timer smashBallMoveTimer, smashBallSpawnTimer, oneShotTimer;
    boolean isEnraged = false;
    long enragedEndTime;

    Image auraImage;
    boolean showAura = false;
    boolean showSmashBallHealthBar = false;
    Image coinBar;

    // constructor to initialize the level two panel and its components
    public Level2Panel(Main mn, CardLayout cd, ShopPanel sp) {
        this.shopPanel = sp;
        this.manager = mn;
        this.cards = cd;

        setBackground(Color.white);
        setLayout(null);
        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        requestFocusInWindow();
        addComponentListener(new ComponentAdapter() {
            // when the panel is on screen, request focus in window
            public void componentShown(ComponentEvent e) {
                System.out.println(shopPanel.balance);
                coinsLabel.setText(""+shopPanel.balance);
                requestFocusInWindow();
            }
        });
        // timer to start the second wave of zombies
        respawnTimer = new Timer(20000, e -> {
            if (waveCount < 2) {
                spawnZombies(5, 125);
                waveCount++;
                if (waveCount > 2) {
                    respawnTimer.stop();
                }
            }
        });

        returnToMapButton = new JButton("");
        returnToMapButton.setOpaque(false);
        returnToMapButton.setContentAreaFilled(false);
        returnToMapButton.setBorderPainted(false);
        returnToMapButton.setBounds(285, 400, 300, 100);
        returnToMapButton.addActionListener(this);

        returnToMapLabel = new JLabel("Return to Map");
        returnToMapLabel.setFont(buttonFont());
        returnToMapLabel.setForeground(Color.GREEN);
        returnToMapLabel.setBounds(300, 400, 300, 100);

        coinsLabel = new JLabel("" + this.shopPanel.balance);
        coinsLabel.setFont(buttonFont());
        coinsLabel.setForeground(Color.white);
        coinsLabel.setBounds(100,0, 225, 60);


        returnToMapButton.setVisible(false);
        returnToMapLabel.setVisible(false);

        losePanel lose = new losePanel();
        manager.cardsPanel.add(lose, "losePanel");
        loadImages();
        initializeZombies(5, 125);
        animationTimer = new Timer(250, e -> animateZombies());
        coinTimer = new Timer(3000, e -> {
            coinPoints.clear();
            droppedCoin = false;
            coinTimer.stop();
        });

        // Timer to handle the duration of the enraged state
        oneShotTimer = new Timer(10000, e -> {
            isEnraged = false;
            showAura = false;
            oneShotTimer.stop();

        });
        //delay for the smash ball to spawn
        smashBallSpawnTimer = new Timer(20000, e -> {
            smashBallVisible = true;
            smashBallX = 700;
            smashBallY = 50;
            smashBallMoveTimer.start(); // Start the movement timer when the ball spawns
        });
        smashBallSpawnTimer.setRepeats(false);
        //move the smash ball
        smashBallMoveTimer = new Timer(15, e -> {
            if (smashBallVisible) {
                smashBallX -= 2; // Move smash ball left
                if (smashBallX < 0) {
                    smashBallVisible = false;
                    smashBallMoveTimer.stop();
                    // smashBallSpawnTimer.start(); // Restart the spawn timer
                }
                repaint();
            }
        });

        add(returnToMapButton);
        add(returnToMapLabel);
        add(coinsLabel);

        // Timer to update the swing offset
        swingTimer = new Timer(30, e -> updateSwingOffset());
        swingTimer.start();
    }

    // method to make the arrow move in a swinging motion, effect of wind
    private void updateSwingOffset() {
        swingOffset = Math.sin(System.currentTimeMillis() * swingSpeed) * swingAmplitude;
        if (isDragging && mousePress != null && releasePoint != null) {
            calculateTrajectoryPoints(mousePress, releasePoint);
            repaint();
        }
    }

    // method to load the images used for less clutter in constructor
    private void loadImages() {
        background = new ImageIcon(getClass().getResource("Level2Images/background.png")).getImage();
        characterImage = new ImageIcon(getClass().getResource("Level1Images/character.png")).getImage();
        bowImage = new ImageIcon(getClass().getResource("Level1Images/bow.png")).getImage();
        arrowImage = new ImageIcon(getClass().getResource("Level1Images/arrow.png")).getImage();
        coinImage = new ImageIcon(getClass().getResource("resources/coin.png")).getImage();
        zombieImage = new ImageIcon(getClass().getResource("Level2Images/bigZombie.png")).getImage();
        babyZombieImage = new ImageIcon(getClass().getResource("Level2Images/babyZombie.png")).getImage();
        wind = new ImageIcon(getClass().getResource("resources/wind.gif")).getImage();
        smashBall = new ImageIcon(getClass().getResource("Level2Images/smashBall.gif")).getImage();
        auraImage = new ImageIcon(getClass().getResource("Level2Images/aura.png")).getImage();
        coinBar = new ImageIcon(getClass().getResource("resources/coinBar.png")).getImage();

    }

    // adds the zombies to the list based on the given parameters
    public void initializeZombies(int count, int spacing) {
        zombies = new ArrayList<>();
        int startY = 25;
        for (int i = 0; i < count; i++) {
            zombies.add(new Zombie(700, startY + i * spacing));
        }
    }

    // checks if the user won, if they have, trigger the winning animation
    public void finishLevel() {
        if ((zombies.isEmpty() && waveCount == 2) && babyZombies.isEmpty()) {
            showConfetti = true;
            returnToMapButton.setVisible(true);
            returnToMapLabel.setVisible(true);
            manager.mapPanel.isLocked2 = false;
            manager.mapPanel.planet3Button.setEnabled(true);
            respawnTimer.stop();
            animationTimer.stop();
        } else {
            showConfetti = false;
        }

        repaint();
    }

    // method to spawn baby zombies when the big zombie dies
    public void spawnZombies(int count, int spacing) {
        int startY = 25;
        for (int i = 0; i < count; i++) {
            zombies.add(new Zombie(700, startY + i * spacing));
        }
    }

    // moves the zombies from right to left
    public void animateZombies() {
        boolean lose = false;
        for (Zombie zombie : zombies) {
            zombie.x -= 5;
            if (zombie.x < 0) {
                lose = true;
            }
        }
        if (lose) {
            cards.show(manager.cardsPanel, "losePanel");
        } else {
            repaint();
        }
    }

    // drops coins when the zombie dies so the player can pick up
    public void dropCoins(int zombieX, int zombieY) {
        if (firstCoinDrop) {
            for (int i = 0; i < 3; i++) {
                coinPoints.add(new Point(zombieX + i * 50, zombieY));
            }
            firstCoinDrop = false;
        } else {
            coinPoints.add(new Point(zombieX, zombieY));
        }
        coinTimer.restart();
    }

    // draw the images and lines of trajectory
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        g.drawImage(coinBar, 0, 0, 225, 60, this);

        // Draw aura if enraged
        if (isEnraged) {
            int auraX = charX - 30; // Adjust to make the aura slightly bigger
            int auraY = charY - 20; // Adjust to make the aura slightly bigger
            int auraWidth = 160; // Adjust to make the aura slightly bigger
            int auraHeight = 190; // Adjust to make the aura slightly bigger
            g.drawImage(auraImage, auraX, auraY, auraWidth, auraHeight, this);
        }

        g.drawImage(characterImage, charX, charY, 120, 150, this);
        g.drawImage(bowImage, bowX, bowY, 150, 125, this);
        g.drawImage(wind, 0, getHeight() / 2 - 150, 250, 200, this);

        if (smashBallVisible) {
            g.drawImage(smashBall, smashBallX, smashBallY, 75, 75, this);

            // Draw health bar for smash ball
            if (showSmashBallHealthBar) {
                int healthBarWidth = (int) (75 * (smashBallHealth / 2.0));
                g.setColor(Color.RED);
                g.fillRect(smashBallX, smashBallY - 10, 75, 5);
                g.setColor(Color.GREEN);
                g.fillRect(smashBallX, smashBallY - 10, healthBarWidth, 5);
            }
        }

        Graphics2D g2d = (Graphics2D) g.create();

        for (Zombie zombie : zombies) {
            g2d.drawImage(zombieImage, zombie.x, zombie.y, 100, 100, this);
            if (zombie.showHealthBar) {
                g2d.setColor(Color.RED);
                g2d.fillRect(zombie.x, zombie.y - 20, 100, 10);
                g2d.setColor(Color.GREEN);
                int healthBarWidth = (int) (100 * (zombie.health / 24.0));
                g2d.fillRect(zombie.x, zombie.y - 20, healthBarWidth, 10);
            }
        }

        for (BabyZombie bz : babyZombies) {
            g.drawImage(babyZombieImage, bz.x, bz.y, 75, 75, null);
            if (bz.showHealthBar) {
                g.setColor(Color.GREEN);
                g.fillRect(bz.x, bz.y - 10, 20 * bz.health, 5);
            }
        }

        if (isDragging) {
            g2d.setStroke(
                    new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10, new float[] { 10, 10 }, 0));
            g2d.setPaint(Color.RED);
            for (int i = 1; i < trajectoryPoints.size(); i++) {
                Point p1 = trajectoryPoints.get(i - 1);
                Point p2 = trajectoryPoints.get(i);
                g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }

        if (arrowVisible) {
            AffineTransform old = g2d.getTransform();
            g2d.rotate(arrowAngle, arrowX + 25, arrowY + 25);
            g2d.drawImage(arrowImage, arrowX, arrowY, 50, 50, this);
            g2d.setTransform(old);
        }
        if (showConfetti) {
            g2d.drawImage(new ImageIcon(getClass().getResource("Level1Images/confetti.gif")).getImage(), 0, 0,
                    getWidth(), getHeight(), this);
        }
        if (droppedCoin && count % 3 == 0) {
            for (Point p : coinPoints) {
                g2d.drawImage(coinImage, p.x, p.y, 50, 50, this);
                coinTimer.start();

                if (new Rectangle(charX, charY, 120, 150).contains(p)) {
                    if (shopPanel.selectedArrow == shopPanel.arrow1) {
                        shopPanel.balance += 2;
                    } else {
                        shopPanel.balance++;
                    }

                    coinsLabel.setText(""+ shopPanel.balance);
                    coinPoints.remove(p);
                    break;
                }
            }
        }

        g2d.setColor(Color.red);
        g2d.setStroke(new BasicStroke(5));
        g2d.drawLine(50, 0, 50, getHeight());
        g2d.dispose();
    }

    // creates the zombies and has methods from them to take damage
    class Zombie {
        int x, y;
        int health = 24;
        boolean showHealthBar = false;
        Timer healthBarTimer;

        // constructor to initlize the zombies postion
        public Zombie(int x, int y) {
            this.x = x;
            this.y = y;
        }

        // method to take damage from the arrow and display the health bar
        public void takeDamage(int damage) {
            health -= damage;
            showHealthBar = true;
            if (healthBarTimer != null) {
                healthBarTimer.stop();
            }
            healthBarTimer = new Timer(1000, e -> showHealthBar = false);
            healthBarTimer.setRepeats(false);
            healthBarTimer.start();
            if (health <= 0) {
                spawnBabyZombies(this.x, this.y);
                shouldBeRemoved = true;
                health = 0;
                zombies.remove(this);
                finishLevel();
            }
        }

        // spawn the baby zombies after the big zombie dies
        private void spawnBabyZombies(int x, int y) {
            babyZombies.add(new BabyZombie(x, y));
        }
    }

    // font for the buttons
    Font buttonFont() {
        Font customFont = null;
        try {
            InputStream fontStream = getClass().getResourceAsStream("resources/Foul Fiend.ttf");
            customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(24f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
            fontStream.close();
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
            return new Font("SansSerif", Font.BOLD, 24);
        }
        return customFont;
    }

    // when the user presses the mouse the start of the trajector line is set
    // (aiming the arrow)
    public void mousePressed(MouseEvent e) {
        mousePress = new Point(bowX + 75, bowY + 62);
        releasePoint = mousePress; // Initialize releasePoint to avoid null pointer
        trajectoryPoints.clear();
        isDragging = true;
    }

    // drag the mouse to aim the arrow
    public void mouseDragged(MouseEvent e) {
        Point dragPoint = e.getPoint();
        releasePoint = dragPoint;
        calculateTrajectoryPoints(new Point(bowX + 75, bowY + 62), dragPoint);
        repaint();
    }

    // method to release the arrow and start the animation
    public void mouseReleased(MouseEvent e) {
        releasePoint = e.getPoint();
        isDragging = false;
        if (!trajectoryPoints.isEmpty()) {
            animateArrow();
        }
    }

    // method to move the character and bow uses W, A, S, D keys
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        switch (key) {
            case KeyEvent.VK_W:
                charY -= 25;
                bowY -= 25;
                break;
            case KeyEvent.VK_S:
                charY += 25;
                bowY += 25;
                break;
            case KeyEvent.VK_D:
                charX += 25;
                bowX += 25;
                break;
            case KeyEvent.VK_A:
                charX -= 25;
                bowX -= 25;
                break;
            default:
                break;
        }
        repaint();
    }

    // method not used but is called when the user clicks the mouse
    public void mouseClicked(MouseEvent e) {
    }

    // method not used but is called when the user enters the mouse
    public void mouseEntered(MouseEvent e) {
    }

    // method not used but is called when the user exits the mouse
    public void mouseExited(MouseEvent e) {
    }

    // method not used but is called when the user releases the mouse
    public void mouseMoved(MouseEvent e) {
    }

    // method not used but is called when the user types a key
    public void keyTyped(KeyEvent e) {
    }

    // method not used but is called when the user releases a key
    public void keyReleased(KeyEvent e) {
    }

    // method to animate the arrow when the user releases the mouse, move the arrow
    // towards the line of trajectory
    public void animateArrow() {
        arrowX = trajectoryPoints.get(0).x;
        arrowY = trajectoryPoints.get(0).y;
        arrowAngle = calculateAngle(trajectoryPoints.get(0), trajectoryPoints.get(1));
        arrowVisible = true;
        arrowTimer = new Timer(1, new ActionListener() {
            int i = 1;

            // moves the arrow every millisecond, based on the timer
            public void actionPerformed(ActionEvent evt) {
                if (i < trajectoryPoints.size()) {
                    arrowX = trajectoryPoints.get(i).x;
                    arrowY = trajectoryPoints.get(i).y;
                    if (i < trajectoryPoints.size() - 1) {
                        arrowAngle = calculateAngle(trajectoryPoints.get(i), trajectoryPoints.get(i + 1));
                    }
                    if (checkCollision()) {
                        arrowTimer.stop();
                        arrowVisible = false;
                    }
                    if (babyZombieCollision()) {
                        arrowTimer.stop();
                        arrowVisible = false;
                    }
                    if (checkSmashBallCollision()) {
                        arrowTimer.stop();
                        arrowVisible = false;
                    }
                    i++;
                } else {
                    arrowTimer.stop();
                }
                repaint();
            }
        });
        arrowTimer.start();
    }
    //check if the arrow collides with the smash ball, if it does, show health bar, and show the aura if its killed.
    public boolean checkSmashBallCollision() {
        int arrowTipX = arrowX + 50;
        Rectangle smashBallRect = new Rectangle(smashBallX, smashBallY, 75, 75);

        if (smashBallRect.contains(arrowTipX, arrowY + 25)) {
            smashBallHealth--;
            showSmashBallHealthBar = true; // Show health bar when hit
            if (smashBallHealth <= 0) {
                smashBallX = 1000;
                smashBallY = 1000;
                smashBallVisible = false;
                showSmashBallHealthBar = false; // Hide health bar when destroyed
                smashBallHealth = 2;
                isEnraged = true;
                showAura = true;
                enragedEndTime = System.currentTimeMillis() + 10000; // Enraged for 10 seconds
                oneShotTimer.start();
            }
            return true;
        }
        return false;
    }

    // method to check if the arrow collides with the big zombie
    public boolean checkCollision() {
        int arrowTipX = arrowX + 50;
        for (Zombie zombie : zombies) {
            Rectangle zombieBackEdge = new Rectangle(zombie.x + 95, zombie.y, 5, 100);
            if (zombieBackEdge.contains(arrowTipX, arrowY + 25)) {
                int damage = 0;

                if (isEnraged) {
                    damage = 24; // One-shot damage if enraged
                } else {
                    if (shopPanel.selectedArrow == shopPanel.arrow1) {
                        damage = 8;
                    } else if (shopPanel.selectedArrow == shopPanel.arrow2) {
                        damage = 12;
                    } else if (shopPanel.selectedArrow == shopPanel.arrow3) {
                        damage = 24;
                    } else {
                        damage = 6;
                    }
                }

                zombie.takeDamage(damage);
                if (zombie.health <= 0) {
                    zombies.remove(zombie);
                    droppedCoin = true;
                    coinPoints.add(new Point(zombie.x, zombie.y));
                    count++;
                }
                return true;
            }
        }
        return false;
    }

    // method to calculate the angle of the arrow
    public double calculateAngle(Point start, Point end) {
        return Math.atan2(end.y - start.y, end.x - start.x);
    }

    // calcualte the line of trajectory based on where the user drags,
    public void calculateTrajectoryPoints(Point start, Point end) {
        trajectoryPoints.clear();
        double dx = end.x - start.x;
        double dy = end.y - start.y;
        double slope = -dy / dx;
        double intercept = start.y - slope * start.x;
        int direction = 0;
        if (dx > 0) {
            direction = 1;
        } else {
            direction = -1;
        }
        for (int x = start.x; x != start.x - dx * 2; x -= direction) {
            int y = (int) (slope * x + intercept + swingOffset);
            int flippedY = 2 * start.y - y;
            trajectoryPoints.add(new Point(x, flippedY));
        }
    }

    // method to return to the map panel
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == returnToMapButton) {
            resetLevel();
            cards.show(manager.cardsPanel, "mapPanel");
        }
    }

    // method to reset the level when the user returns to the map panel
    public void resetLevel() {
        charX = 100;
        charY = 350;
        bowX = 115;
        bowY = 315;
        babyZombies.clear();
        zombies.clear();
        initializeZombies(5, 125);
        waveCount = 1;
        showConfetti = false;

        if (animationTimer != null) {
            animationTimer.stop();
        }
        if (respawnTimer != null) {
            respawnTimer.stop();
        }

        arrowVisible = true;
    }

    // panel that is shown when the user loses the game
    class losePanel extends JPanel {
        Image background = new ImageIcon(getClass().getResource("Level1Images/YouLose.png")).getImage();

        // constructor to initialize the lose panel
        public losePanel() {
            setLayout(null);
            returnToMapButton.setBounds(returnToMapButton.getX(), returnToMapButton.getY() - 400,
                    returnToMapButton.getWidth(), returnToMapButton.getHeight());
            returnToMapLabel.setBounds(returnToMapLabel.getX(), returnToMapLabel.getY() - 400,
                    returnToMapLabel.getWidth(), returnToMapLabel.getHeight());
            returnToMapLabel.setForeground(Color.RED);
            add(returnToMapButton);
            add(returnToMapLabel);
        }

        // paints the background image
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        }
    }

    // creates the baby zombies, moves them, and checks if they are dead
    class BabyZombie {
        int x, y;
        int health;
        boolean showHealthBar = false;
        Timer moveTimer;
        Timer healthBarTimer;
        boolean lose = false;
        int SPEED = 1;

        // constructor to initialize the baby zombie
        public BabyZombie(int x, int y) {
            this.x = x;
            this.y = y;
            this.health = 2;
            moveTimer = new Timer(15, e -> move());
            moveTimer.start();
        }

        // method to move the baby zombie using a timer
        public void move() {
            x -= SPEED;
            if (x < 0) {
                lose = true;
            }
            if (lose) {
                cards.show(manager.cardsPanel, "losePanel");
            }

            if (health <= 0) {
                babyZombies.remove(this);
                moveTimer.stop();
                if (healthBarTimer != null) {
                    healthBarTimer.stop();

                }
                finishLevel();
            }
            repaint();
        }

        // method to take damage from the arrow and display the health bar
        public void takeDamage(int damage) {
            health -= damage;
            showHealthBar = true;
            if (healthBarTimer != null) {
                healthBarTimer.restart();
            } else {
                healthBarTimer = new Timer(1000, e -> showHealthBar = false);
                healthBarTimer.start();
            }
            if (health <= 0) {
                moveTimer.stop();
                if (healthBarTimer != null){
                    healthBarTimer.stop();
                }
                babyZombies.remove(this);
                finishLevel(); // Ensure finishLevel is called after baby zombie is removed

            }
        }
    }

    // check if the arrow collides with the baby zombie
    public boolean babyZombieCollision() {
        int arrowTipX = arrowX + 50;

        for (BabyZombie babyZombie : babyZombies) {
            Rectangle babyZombieBackEdge = new Rectangle(babyZombie.x, babyZombie.y, 75, 75);
            if (babyZombieBackEdge.contains(arrowTipX, arrowY)) {
                int damage = calculateDamage();
                babyZombie.takeDamage(damage);
                if (babyZombie.health <= 0) {
                    babyZombies.remove(babyZombie);
                    droppedCoin = true;
                }
                return true;
            }
        }
        return false;
    }

    // calcualte the damage of the arrow based on the arrow the user selects, for
    // the baby Zombies, used for less clutter
    private int calculateDamage() {
        if (isEnraged) {
            return 4;
        }
        if (shopPanel.selectedArrow == shopPanel.arrow1) {
            return 2;
        } else if (shopPanel.selectedArrow == shopPanel.arrow2) {
            return 4;
        } else if (shopPanel.selectedArrow == shopPanel.arrow3) {
            return 4;
        }
        return 1;
    }
}
