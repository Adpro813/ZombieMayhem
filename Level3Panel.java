
//import all missing importss 
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import java.io.File;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class Level3Panel extends JPanel implements MouseListener, MouseMotionListener, KeyListener, ActionListener {
    public Image background;
    public ShopPanel shopPanel;
    public CardLayout cardLayout;
    public JPanel cardsPanel;

    // bow and arrow fields
    Image characterImage, bowImage, arrowImage, wind, confetti, shieldImage, powerUpImage;
    List<Point> trajectoryPoints = new ArrayList<>();
    boolean isDragging = false;
    boolean canShoot = true;
    Point mousePress, releasePoint;
    int arrowX, arrowY;
    int charY = 350, charX = 100;
    int bowX = 115, bowY = 315;
    double arrowAngle = 0;
    boolean arrowVisible = true;
    boolean shieldActive = false;
    int shieldsLeft = 3;
    Timer arrowTimer, swingTimer, gunTimer, animationTimer, shellTimer, shellMoveTimer, bossHitTimer, healthBarTimer;
    Timer reloadTimer, shieldTimer, powerUpTimer;
    double swingOffset = 0;
    double swingSpeed = 0.005;
    double swingAmplitude = 35;

    // gun animation fields
    public ImageIcon gunGif;
    public int gunX = 575, gunY = 265; // moved up 10 pixels
    public boolean gunAnimationPlaying = false;
    public Image idleFrame;
    public Image shellImage;
    public boolean shellVisible = false;
    public int shellX = 600, shellY = 295;

    // boss fields
    public Image bossImage;
    public Image bossIdleImage;
    public Image bossHitImage;
    public int bossX = 700, bossY = 200;
    public boolean bossDefeated = false;
    public int bossHealth = 100;
    public Image bossDeathImage;

    // player health fields
    public int playerHealth = 5; // player can take 5 hits
    public int currentPlayerHealthWidth;
    public int targetPlayerHealthWidth;
    public final int maxPlayerHealth = 5;
    public final int playerHealthBarWidth = 200; // width of the health bar in pixels

    // boss movement fields
    public Image bossWalkImage;
    public Timer bossMoveTimer;
    public boolean bossMoving = false;

    // health bar fields
    public int currentHealthWidth;
    public int targetHealthWidth;

    // power-up fields
    public boolean powerUpActive = false;
    public boolean powerUpDropped = false;
    public int powerUpX = 400, powerUpY = 200;
    public Timer powerUpDurationTimer;
    public boolean speedBoostActive = false;
    public boolean damageBoostActive = false;

    // win image
    public Image youWinImage;

    // initialize the panel components and call key method to start the game.
    public Level3Panel(Main mn, CardLayout cl, JPanel cp, ShopPanel sp) {
        LosePanel losePanel = new LosePanel();
        WinPanel winPanel = new WinPanel();
        cp.add(losePanel, "losePanel");
        cp.add(winPanel, "winPanel");

        this.shopPanel = sp;
        this.cardLayout = cl;
        this.cardsPanel = cp;
        setBackground(Color.BLACK);
        setLayout(null);
        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        requestFocusInWindow();

        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                requestFocusInWindow();
            }
        });

        swingTimer = new Timer(30, e -> updateSwingOffset());
        swingTimer.start();
        loadImages();
        startGunAnimationTimer();
        startBossMoveTimer(); // start the boss movement timer
        initHealthBars(); // initialize the health bars
    }

    // load images for the game, used for less clutter in the constructor
    public void loadImages() {
        background = new ImageIcon(getClass().getResource("Level3Images/testBackground.png")).getImage();
        characterImage = new ImageIcon(getClass().getResource("Level1Images/character.png")).getImage();
        bowImage = new ImageIcon(getClass().getResource("Level1Images/bow.png")).getImage();
        wind = new ImageIcon(getClass().getResource("resources/wind.gif")).getImage();
        confetti = new ImageIcon(getClass().getResource("Level1Images/confetti.gif")).getImage();
        bossIdleImage = new ImageIcon(getClass().getResource("Level3Images/idle.gif")).getImage();
        bossHitImage = new ImageIcon(getClass().getResource("Level3Images/hit.gif")).getImage();
        bossWalkImage = new ImageIcon(getClass().getResource("Level3Images/walk.gif")).getImage();
        bossImage = bossIdleImage;
        gunGif = new ImageIcon(getClass().getResource("Level3Images/spas.gif"));
        idleFrame = new ImageIcon(getClass().getResource("Level3Images/frame1.png")).getImage();
        shellImage = new ImageIcon(getClass().getResource("Level3Images/shell.png")).getImage();
        bossDeathImage = new ImageIcon(getClass().getResource("Level3Images/death.gif")).getImage();
        arrowImage = new ImageIcon(getClass().getResource("Level1Images/arrow.png")).getImage();
        shieldImage = new ImageIcon(getClass().getResource("Level3Images/shield.png")).getImage();
        powerUpImage = new ImageIcon(getClass().getResource("Level3Images/powerup.png")).getImage();
        youWinImage = new ImageIcon(getClass().getResource("Level3Images/youWin.png")).getImage();
    }

    public void playSound() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Clip clip = AudioSystem.getClip();
                    AudioInputStream inputStream = AudioSystem.getAudioInputStream(
                            getClass().getResourceAsStream("/resources/spas.wav"));

                    if (inputStream == null) {
                        System.err.println("Could not find the audio file.");
                        return;
                    }

                    clip.open(inputStream);

                    // Reduce volume by 50%
                    FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    float volume = volumeControl.getValue();
                    System.out.println(volume);
                    float reducedVolume = volume - 35.0f; // Reducing by 10 decibels (dB)
                    volumeControl.setValue(reducedVolume);

                    clip.start();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // responsible for the wind effect, is what rocks the line of trajectory back
    // and forth.
    public void updateSwingOffset() {
        swingOffset = Math.sin(System.currentTimeMillis() * swingSpeed) * swingAmplitude;
        if (isDragging && mousePress != null && releasePoint != null) {
            calculateTrajectoryPoints(mousePress, releasePoint);
            repaint();
        }
    }

    // toggles between shooting animation and idle animation
    public void startGunAnimationTimer() {
        gunTimer = new Timer(3000, e -> { // faster bullet timer
            if (!bossDefeated) {
                playSound();
                gunAnimationPlaying = true;
                shellVisible = true; // make shell visible when gun animation starts
                shellX = gunX + 50; // reset shell position to start at the gun's location
                shellY = gunY + 20; // shoot from the gun's coordinates
                repaint();

                // start the shell movement timer when the gun animation starts
                startShellMoveTimer();

                animationTimer = new Timer(1000, evt -> {
                    gunAnimationPlaying = false;
                    repaint();
                });
                animationTimer.setRepeats(false);
                animationTimer.start();
            }
        });
        gunTimer.setInitialDelay(0);
    }

    // start the boss movement timer, meaning the boss moves to the left with the
    // gif animation
    public void startBossMoveTimer() {
        bossMoveTimer = new Timer(200, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (!bossDefeated) {
                    moveBoss();
                    repaint();
                }
            }
        });
    }

    // move the boss to the left, right, up, or down
    public void moveBoss() {
        int moveSpeed = 1; // slower move speed
        if (Math.abs(bossX - charX) < 5 && Math.abs(bossY - charY) < 5) {
            // boss is aligned with the player
        } else if (bossX < charX) { // move right
            bossX += moveSpeed;
        } else { // move left
            bossX -= moveSpeed;
        }

        if (Math.abs(bossY - charY) < 5) {
            // boss is aligned with the player
        } else if (bossY < charY) { // move down
            bossY += 3;
        } else { // move up
            bossY -= 3;
        }

        // update gun position based on boss position
        gunX = bossX - 20;
        gunY = bossY + 70; // moved up 10 pixels

        bossImage = bossWalkImage;
        bossMoving = true;
    }

    // start the shell movement timer, shoots the bullet.
    public void startShellMoveTimer() {
        shellMoveTimer = new Timer(7, new ActionListener() { // faster bullet speed
            public void actionPerformed(ActionEvent evt) {
                if (shellX > 0) {
                    shellX -= 5; // move shell to the left
                    if (!shieldActive) {
                        checkBulletCollision();
                    } else {
                        checkShieldCollision();
                    }
                } else {
                    shellMoveTimer.stop(); // stop timer when shell goes off-screen
                }
                repaint();
            }
        });
        shellMoveTimer.start();
    }

    // initialize the health bars, sets the current health width to full for both
    // the player and the boss
    public void initHealthBars() {
        currentHealthWidth = bossHealth * 2;
        targetHealthWidth = currentHealthWidth;
        currentPlayerHealthWidth = playerHealthBarWidth;
        targetPlayerHealthWidth = currentPlayerHealthWidth;
    }

    // decrement health bar with a timer when boss is hit
    public void decrementHealthBar() {
        if (healthBarTimer != null) {
            healthBarTimer.stop();
        }
        healthBarTimer = new Timer(30, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (currentHealthWidth > targetHealthWidth) {
                    currentHealthWidth--;
                    repaint();
                } else {
                    healthBarTimer.stop();
                    if (bossHealth <= 0) {
                        cardLayout.show(cardsPanel, "winPanel");
                        bossDefeated = true;
                        bossImage = bossDeathImage;
                        stopAllTimersAndAnimations();
                        repaint();
                    }
                }
            }
        });
        healthBarTimer.start();
    }

    // decrement player health bar with a timer when player is hit
    public void decrementPlayerHealthBar() {
        if (healthBarTimer != null) {
            healthBarTimer.stop();
        }
        healthBarTimer = new Timer(30, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (currentPlayerHealthWidth > targetPlayerHealthWidth) {
                    currentPlayerHealthWidth--;
                    repaint();
                } else {
                    healthBarTimer.stop();
                    if (playerHealth <= 0) {
                        stopAllTimersAndAnimations();
                        cardLayout.show(cardsPanel, "losePanel");
                    } else if (playerHealth == maxPlayerHealth / 2 && !powerUpDropped) {
                        dropPowerUp();
                    }
                }
            }
        });
        healthBarTimer.start();
    }

    // drop the power-up in the middle of the screen
    public void dropPowerUp() {
        powerUpDropped = true;
        repaint();
    }

    // activate the power-up effects
    public void activatePowerUp() {
        powerUpActive = false;
        powerUpDropped = false;
        speedBoostActive = true;
        damageBoostActive = true;

        // Deactivate power-up effects after 5 seconds
        powerUpDurationTimer = new Timer(9000, e -> {
            speedBoostActive = false;
            damageBoostActive = false;
        });
        powerUpDurationTimer.setRepeats(false);
        powerUpDurationTimer.start();
    }

    private void stopAllTimersAndAnimations() {
        if (swingTimer != null)
            swingTimer.stop();
        if (gunTimer != null)
            gunTimer.stop();
        if (animationTimer != null)
            animationTimer.stop();
        if (shellTimer != null)
            shellTimer.stop();
        if (shellMoveTimer != null)
            shellMoveTimer.stop();
        if (bossHitTimer != null)
            bossHitTimer.stop();
        if (healthBarTimer != null)
            healthBarTimer.stop();
        if (reloadTimer != null)
            reloadTimer.stop();
        if (bossMoveTimer != null)
            bossMoveTimer.stop();
        if (shieldTimer != null)
            shieldTimer.stop();
        if (powerUpDurationTimer != null)
            powerUpDurationTimer.stop();

        gunAnimationPlaying = false;
        shellVisible = false;
        shieldActive = false;
        powerUpActive = false;
        speedBoostActive = false;
        damageBoostActive = false;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);

        Graphics2D g2d = (Graphics2D) g.create();
        if (speedBoostActive || damageBoostActive) {
            characterImage = new ImageIcon(getClass().getResource("Level3Images/redCharacter.png")).getImage();
        } else {
            characterImage = new ImageIcon(getClass().getResource("Level1Images/character.png")).getImage();
        }
        if (bossDefeated) {
            g.drawImage(confetti, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.drawImage(bossImage, bossX, bossY, 200, 200, this);

            if (gunAnimationPlaying) {
                Image currentFrame = gunGif.getImage();
                g.drawImage(currentFrame, gunX, gunY, 200, 100, this);
            } else {
                g.drawImage(idleFrame, gunX, gunY, 200, 100, this);
            }

            if (shellVisible) {
                g.drawImage(shellImage, shellX, shellY, 50, 20, this);
            }
        }
        // draw the shield if active
        if (shieldActive) {
            g.drawImage(shieldImage, charX - 25, charY - 25, 200, 200, this);
        }
        g.drawImage(characterImage, charX, charY, 120, 150, this);
        g.drawImage(bowImage, bowX, bowY, 150, 125, this);
        g.drawImage(wind, 0, getHeight() / 2 - 150, 250, 200, this);

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

        // draw the boss health bar
        g2d.setColor(Color.RED);
        g2d.fillRect(getWidth() - 220, 20, 200, 30); // health bar background
        g2d.setColor(Color.GREEN);
        g2d.fillRect(getWidth() - 220, 20, currentHealthWidth, 30); // health bar foreground
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(getWidth() - 220, 20, 200, 30);

        // draw the player health bar
        g2d.setColor(Color.RED);
        g2d.fillRect(20, 20, 200, 30); // health bar background
        g2d.setColor(Color.GREEN);
        g2d.fillRect(20, 20, currentPlayerHealthWidth, 30); // health bar foreground
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(20, 20, 200, 30);

        // draw the shields
        int shieldWidth = 40;
        int shieldHeight = 40;
        int shieldSpacing = 10;
        int shieldX = 20;
        int shieldY = getHeight() - shieldHeight - 20;

        for (int i = 0; i < shieldsLeft; i++) {
            g.drawImage(shieldImage, shieldX + (shieldWidth + shieldSpacing) * i, shieldY, shieldWidth, shieldHeight,
                    this);
        }

        // draw the reload label
        if (!canShoot) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.drawString("Press 'R' to reload", getWidth() / 2 - 100, 50);
        }

        // draw the power-up if active
        if (powerUpDropped && !powerUpActive) {
            g.drawImage(powerUpImage, powerUpX, powerUpY, 50, 50, this);
        }

        g2d.dispose();
    }

    // check for collisions between arrow and boss, if so, take damage.
    public void checkCollision() {
        Rectangle arrowRect = new Rectangle(arrowX, arrowY, 50, 50);
        Rectangle bossRect = new Rectangle(bossX + 50, bossY + 75, 150, 125);

        if (arrowRect.intersects(bossRect)) {
            int arrowDamage = 100 / 24; // Default arrow takes 24 hits to kill the boss
            if (shopPanel.selectedArrow == shopPanel.arrow1) {
                arrowDamage = 100 / 23; // Arrow 1 takes 23 hits to kill the boss
            } else if (shopPanel.selectedArrow == shopPanel.arrow2) {
                arrowDamage = 100 / 22; // Arrow 2 takes 22 hits to kill the boss
            } else if (shopPanel.selectedArrow == shopPanel.arrow3) {
                arrowDamage = 100 / 20; // Arrow 3 takes 20 hits to kill the boss
            }

            if (damageBoostActive) {
                arrowDamage *= 2; // Double the damage if damage boost is active
            }

            targetHealthWidth -= arrowDamage * 2; // adjust target health width based on damage
            decrementHealthBar(); // start decrementing the health bar
            bossHealth -= arrowDamage;
            bossImage = bossHitImage;
            arrowTimer.stop();

            arrowVisible = false;
            repaint();

            // pause the boss move timer
            bossMoveTimer.stop();

            // calculate the duration of the hit GIF (assuming it's 600ms here)
            int hitGifDuration = 600;

            bossHitTimer = new Timer(hitGifDuration, e -> {
                if (bossHealth > 0) {
                    bossImage = bossIdleImage;
                }
                bossMoveTimer.start(); // restart the boss move timer
                repaint();
            });
            bossHitTimer.setRepeats(false);
            bossHitTimer.start();
        }
    }

    // check for collisions between bullet and player
    public void checkBulletCollision() {
        Rectangle bulletRect = new Rectangle(shellX, shellY, 50, 20);
        Rectangle playerRect = new Rectangle(charX - 50, charY + 20, 100, 130);

        if (bulletRect.intersects(playerRect) && !shieldActive) {
            playerHealth -= 1; // decrement player health by 1
            targetPlayerHealthWidth = (playerHealth * playerHealthBarWidth) / maxPlayerHealth; // adjust target health
                                                                                               // width
            if (healthBarTimer != null) {
                healthBarTimer.stop();
            }
            decrementPlayerHealthBar(); // start decrementing the player health bar
            shellVisible = false;
            shellMoveTimer.stop();
            repaint();
        } else if (bulletRect.intersects(playerRect) && shieldActive) {
            shellVisible = false;
            shellMoveTimer.stop();
            repaint();
        }
    }

    // check for collisions between bullet and shield
    public void checkShieldCollision() {
        Rectangle bulletRect = new Rectangle(shellX, shellY, 50, 20);
        Rectangle shieldRect = new Rectangle(charX - 25, charY - 25, 200, 200);

        if (bulletRect.intersects(shieldRect)) {
            shellVisible = false;
            shellMoveTimer.stop();
            repaint();
        }
    }

    // check for collisions between player and power-up
    public void checkPowerUpCollision() {
        Rectangle playerRect = new Rectangle(charX - 50, charY + 20, 100, 130);
        Rectangle powerUpRect = new Rectangle(powerUpX, powerUpY, 100, 100);

        if (playerRect.intersects(powerUpRect)) {
            powerUpActive = true;
            activatePowerUp();
            repaint();
        }
    }

    // when mouse is pressed starting of the line is set
    public void mousePressed(MouseEvent e) {
        if (canShoot) {
            mousePress = new Point(bowX + 75, bowY + 62);
            releasePoint = mousePress;
            trajectoryPoints.clear();
            isDragging = true;
        }
    }

    // extends line of trajectory when mouse is dragged
    public void mouseDragged(MouseEvent e) {
        if (canShoot) {
            Point dragPoint = e.getPoint();
            releasePoint = dragPoint;
            calculateTrajectoryPoints(new Point(bowX + 75, bowY + 62), dragPoint);
            repaint();
        }
    }

    // releases arrow, starts animation
    public void mouseReleased(MouseEvent e) {
        if (canShoot) {
            releasePoint = e.getPoint();
            isDragging = false;
            if (!trajectoryPoints.isEmpty()) {
                animateArrow();
                canShoot = false;
                startReloadTimer();
            }
        }
    }

    // starts the reload timer, user can shoot again after 3 seconds
    public void startReloadTimer() {
        reloadTimer = new Timer(3000, e -> canShoot = true);
        reloadTimer.setRepeats(false);
        reloadTimer.start();
    }

    // moves character up down left right based on key pressed
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        int moveSpeed = speedBoostActive ? 50 : 25; // Double the movement speed if speed boost is active

        switch (key) {
            case KeyEvent.VK_W:
                charY -= moveSpeed;
                bowY -= moveSpeed;
                break;
            case KeyEvent.VK_S:
                charY += moveSpeed;
                bowY += moveSpeed;
                break;
            case KeyEvent.VK_D:
                charX += moveSpeed;
                bowX += moveSpeed;
                break;
            case KeyEvent.VK_A:
                charX -= moveSpeed;
                bowX -= moveSpeed;
                break;
            case KeyEvent.VK_R:
                if (!canShoot) {
                    canShoot = true;
                    if (reloadTimer != null) {
                        reloadTimer.stop();
                    }
                }
                break;
            case KeyEvent.VK_F:
                activateShield();
                break;
            default:
                break;
        }
        checkPowerUpCollision();
        repaint();
    }

    // activates the shield
    public void activateShield() {
        if (shieldsLeft > 0 && !shieldActive) {
            shieldActive = true;
            shieldsLeft--;
            shieldTimer = new Timer(3000, e -> shieldActive = false);
            shieldTimer.setRepeats(false);
            shieldTimer.start();
        }
    }

    // move the arrow towards the line of trajectory with a timer
    public void animateArrow() {
        arrowX = trajectoryPoints.get(0).x;
        arrowY = trajectoryPoints.get(0).y;
        arrowAngle = calculateAngle(trajectoryPoints.get(0), trajectoryPoints.get(1));
        arrowVisible = true;
        arrowTimer = new Timer(1, new ActionListener() {
            int i = 1;

            // move the arrow along the trajectory points
            public void actionPerformed(ActionEvent evt) {
                if (i < trajectoryPoints.size()) {
                    arrowX = trajectoryPoints.get(i).x;
                    arrowY = trajectoryPoints.get(i).y;
                    if (i < trajectoryPoints.size() - 1) {
                        arrowAngle = calculateAngle(trajectoryPoints.get(i), trajectoryPoints.get(i + 1));
                    }
                    checkCollision();
                    i++;
                } else {
                    arrowVisible = false;
                    arrowTimer.stop();
                }
                repaint();
            }
        });
        arrowTimer.start();
    }

    // calculate the angle of the line for rotation of the arrow image
    public double calculateAngle(Point start, Point end) {
        return Math.atan2(end.y - start.y, end.x - start.x);
    }

    // calculate the points of the trajectory line using the start and end points
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

    // unused but actionPerformed is called when a component such as a button is
    // clicked, if the corresponding listener is added
    public void actionPerformed(ActionEvent e) {
    }

    // unused but called when a user types a key
    public void keyTyped(KeyEvent e) {
    }

    // unused but called when the mouse exits the frame/panel
    public void mouseExited(MouseEvent e) {
    }

    // unused but called when the mouse enters the frame/panel
    public void mouseEntered(MouseEvent e) {
    }

    // unused but called when the mouse moves (cursor)
    public void mouseMoved(MouseEvent e) {
    }

    // unused but called when the key is released
    public void keyReleased(KeyEvent e) {
    }

    // unused but called when the mouse is clicked
    public void mouseClicked(MouseEvent e) {
    }

    // panel to show when player loses
    class LosePanel extends JPanel {
        Image background = new ImageIcon(getClass().getResource("Level1Images/YouLose.png")).getImage();

        // draws the images
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        }
    }

    class WinPanel extends JPanel {

        // draws the images
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(youWinImage, 0, 0, getWidth(), getHeight(), this);
            g.drawImage(confetti, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
