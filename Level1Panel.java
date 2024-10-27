
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
//this class will be the panel that pops up when user arrives at the first planet 
public class Level1Panel extends JPanel implements MouseMotionListener, MouseListener, KeyListener,ActionListener {

    Image background, characterImage, bowImage, arrowImage, coinImage;
    Image[] zombieFrames = new Image[4];
    List<Zombie> zombies;
    Timer animationTimer, arrowTimer;
    List<Point> trajectoryPoints = new ArrayList<>();
    List<Point>coinPoints = new ArrayList<>();
    boolean isDragging = false;
    Point mousePress, releasePoint;
    int arrowX, arrowY;
    int charY = 350, charX = 100;
    int bowX = 115, bowY = 315;
    double arrowAngle = 0;
    Main manager;
    CardLayout cards;
    int currentFrame = 0;
    int waveCount=1;
    Timer respawnTimer;
    boolean arrowVisible = true;
    JLabel returnToMapLabel;
    JButton returnToMapButton;
    boolean showConfetti = false;
    JLabel coinsLabel;
    boolean droppedCoin = false;
    private int count;
    Timer coinTimer;
    ShopPanel shopPanel;
    boolean firstCoinDrop = false;
    Image coinBar;


    //this constructor will initlize the panel components, and add them to the panel.
    public Level1Panel(Main mn, CardLayout cd, ShopPanel sp) {
        shopPanel = sp;
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
            public void componentShown(ComponentEvent e) {
                requestFocusInWindow();
            }
        });
        //second wave
         respawnTimer = new Timer(9000, e -> {
            if (waveCount <2) {  
                spawnZombies(5, 125);  
                waveCount++;  
                if (waveCount >2) {
                    respawnTimer.stop();
                }
            }
        });
        
        returnToMapButton = new JButton("");
        returnToMapButton.setOpaque(false);
        returnToMapButton.setContentAreaFilled(false);
        returnToMapButton.setBorderPainted(false);
        returnToMapButton.setBounds(285, 400, 300, 100);  // Adjust size and position as needed
        returnToMapButton.addActionListener(this);
        
        returnToMapLabel = new JLabel("Return to Map");
        returnToMapLabel.setFont(buttonFont());
        returnToMapLabel.setForeground(Color.GREEN);
        returnToMapLabel.setBounds(300, 400, 300, 100);  // Align label with button

        coinsLabel = new JLabel(""+shopPanel.balance);
        coinsLabel.setFont(buttonFont());
        coinsLabel.setForeground(Color.white);
        coinsLabel.setBounds(100, 0, 225, 60);

       
       
        returnToMapButton.setVisible(false);
        returnToMapLabel.setVisible(false);

        losePanel lose = new losePanel();
        manager.cardsPanel.add(lose, "losePanel");
        loadImages();
        initializeZombies(5, 125);
        animationTimer = new Timer(150, e -> animateZombies());
        coinTimer = new Timer(3000, e -> {
            coinPoints.clear();
            droppedCoin = false;
            coinTimer.stop();
        });
        add(returnToMapButton);
        add(returnToMapLabel);
        add(coinsLabel);
        
       
    }
    
    //this method will initlize all the images. uesd for less clutter in the constructor
    public void loadImages() {
        background = new ImageIcon(getClass().getResource("Level1Images/firstLevelBackground.png")).getImage();
        characterImage = new ImageIcon(getClass().getResource("Level1Images/character.png")).getImage();
        bowImage = new ImageIcon(getClass().getResource("Level1Images/bow.png")).getImage();
        arrowImage = new ImageIcon(getClass().getResource("Level1Images/arrow.png")).getImage();
        coinImage = new ImageIcon(getClass().getResource("resources/coin.png")).getImage();
        coinBar = new ImageIcon(getClass().getResource("resources/coinBar.png")).getImage();


        for (int i = 0; i < zombieFrames.length; i++) {
            zombieFrames[i] = new ImageIcon(getClass().getResource("Level1Images/zombie" + (i + 1) + ".png")).getImage();
        }
    }
    //this method will initlize 5 diffrenet zombies in a list and it will set their positions.
    public void initializeZombies(int count, int spacing) {
        zombies = new ArrayList<>();
        int startY = 25;
        for (int i = 0; i < count; i++) {
            zombies.add(new Zombie(700, startY + i * spacing));
        }
    }
    //this method will check if the user has won, and if they won confetti and return to the map pops up
    public void finishLevel() {
        if (zombies.isEmpty() && waveCount == 2) {
            showConfetti = true; 
            returnToMapButton.setVisible(true);
            returnToMapLabel.setVisible(true);
            manager.mapPanel.isLocked = false;
            manager.mapPanel.planet2Button.setEnabled(true);
            respawnTimer.stop();
            animationTimer.stop();
            
        } else {
            showConfetti = false;
        }

        repaint();

        
    }

    //this  method is used for the second wave of zombies, it will be called to spawn however many zombies are needed in the same position as the first wave.
    public void spawnZombies(int count, int spacing) {
        int startY = 25;
        for (int i = 0; i < count; i++) {
            zombies.add(new Zombie(700, startY + i * spacing)); 
        }
    }

   
    //this method will toggle between the sprites, and also check if the user has crossed the boundary.
    public void animateZombies() {
        boolean lose = false;
        for (Zombie zombie : zombies) {
            zombie.x -= 5;
            if (zombie.x < 100) {  // Check if any zombie crosses the boundary
                lose = true;
            }

        }
        if (lose) {
            cards.show(manager.cardsPanel, "losePanel");
        } else {
            currentFrame = (currentFrame + 1) % zombieFrames.length;
            repaint();
        }
        
    }
    //this method will drop a coin where the zombie was killed, and will add the coin to the coin list that will be painted in pain.
    public void dropCoins(int zombieX, int zombieY) {
        if (firstCoinDrop) {
            // Drop three coins the first time
            for (int i = 0; i < 3; i++) {
                coinPoints.add(new Point(zombieX + i * 50, zombieY));  // Stagger the coins
            }
            firstCoinDrop = false;
        } else {
            // Drop only one coin subsequently
            coinPoints.add(new Point(zombieX, zombieY));
        }
        coinTimer.restart();  // Restart the timer every time coins are dropped
    }

    //draw imagees, line of trajectory, and the boundary.
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        g.drawImage(characterImage, charX, charY, 120, 150, this);
        g.drawImage(bowImage, bowX, bowY, 150, 125, this);
        g.drawImage(coinBar, 0, 0, 225, 60, this);

        // g.drawImage(coinImage, 185, 0, 50, 50,null);

        Graphics2D g2d = (Graphics2D) g.create();

        // Draw the zombies and their health bars
        for (Zombie zombie : zombies) {
            g2d.drawImage(zombieFrames[currentFrame], zombie.x, zombie.y, 100, 100, this);
            if (zombie.showHealthBar) {
                g2d.setColor(Color.RED);
                g2d.fillRect(zombie.x, zombie.y - 20, 100, 10); // Full health bar background
                g2d.setColor(Color.GREEN);
                g2d.fillRect(zombie.x, zombie.y - 20, 50 * zombie.health, 10); // Health remaining
            }
        }

        if (isDragging) {
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10, new float[] { 10, 10 }, 0));
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
        if(showConfetti)
        {
            g2d.drawImage(new ImageIcon(getClass().getResource("Level1Images/confetti.gif")).getImage(), 0, 0, getWidth(), getHeight(), this);

        }
        if(droppedCoin && count%3==0)
        {
            for(Point p: coinPoints)
            {
            g2d.drawImage(coinImage, p.x, p.y, 50, 50, this);
            coinTimer.start();

            if(new Rectangle(charX,charY,120,150).contains(p))
            {   
                //if the arrow is the first arrow from the shop do balance+=2, if not, do balane++
                if(shopPanel.selectedArrow==shopPanel.arrow1)
                {
                    shopPanel.balance+=2;
                }
                else
                {
                    shopPanel.balance++;
                }

                coinsLabel.setText("" +  shopPanel.balance);
                coinPoints.remove(p);
                break;
            }
            }
        }
        g2d.setColor(Color.red);
        g2d.setStroke(new BasicStroke(5));
        g2d.drawLine(150, 0, 150, getHeight());
        g2d.dispose();
    }
    //this class will hold the zombie object, and will have a method to take damage. Each zombie is represented by this class.
    class Zombie {
        int x, y;
        int health = 2; 
        boolean showHealthBar = false;
        Timer healthBarTimer;
        //initlizes feild level variables to the x and y values passed in.
        public Zombie(int x, int y) {
            this.x = x;
            this.y = y;
        }
        //this method will take away one health point from the zombie, and if the health is 0, it will remove the zombie from the list, despawning it.
        public void takeDamage() {
            health -= 1;
            showHealthBar = true;
            if (healthBarTimer != null) {
                healthBarTimer.stop();
            }
            healthBarTimer = new Timer(1000, e -> showHealthBar = false); // hide health bar after 1 second
            healthBarTimer.setRepeats(false);
            healthBarTimer.start();
            if (health <= 0) {
                zombies.remove(this); 
                finishLevel(); 
            }
        }
    }
    //this method is the font used for the buttons.
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

    // this method is the method that will start the line of trajeectory
    public void mousePressed(MouseEvent e) {
        mousePress = new Point(bowX + 75, bowY + 62);
        trajectoryPoints.clear();
        isDragging = true;
    }
    //decides how long the line of trajectory is, and which way it is facing by calling the method
    public void mouseDragged(MouseEvent e) {
        Point dragPoint = e.getPoint();
        calculateTrajectoryPoints(new Point(bowX + 75, bowY + 62), dragPoint);
        repaint();
    }
    //this method will be called when the user releases the mouse, and will start the arrow animation.
    public void mouseReleased(MouseEvent e) {
        releasePoint = e.getPoint();
        isDragging = false;
        if (!trajectoryPoints.isEmpty()) {
            animateArrow();
        }
    }
    //moves the chaaracter up down left right
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W) {
            charY -= 25;
            bowY -= 25;
        } else if (key == KeyEvent.VK_S) {
            charY += 25;
            bowY += 25;
        } else if (key == KeyEvent.VK_D) {
            charX += 25;
            bowX += 25;
        } else if (key == KeyEvent.VK_A) {
            charX -= 25;
            bowX -= 25;
        }
        repaint();
    }

    //this method is not used, but will be called when the user clicks the mouse.
    public void mouseClicked(MouseEvent e) {
    }
        
    //this method is not used, but will be called when the user enters the frame.
    public void mouseEntered(MouseEvent e) {
    }
    //this method is not used, but it will be called when the user exits the frame.
    public void mouseExited(MouseEvent e) {
    }
    //this method is not used, but it will be called when the user moves the mouse.
    public void mouseMoved(MouseEvent e) {
    }
    //this method is not used, but it will be called when the user types.
    public void keyTyped(KeyEvent e) {
    }
    //this method is not used, but it will be called when the user releases a key.
    public void keyReleased(KeyEvent e) {
    }
    //this will move the arrow towards the direction of the line of trajectory
    public void animateArrow() {
        arrowX = trajectoryPoints.get(0).x;
        arrowY = trajectoryPoints.get(0).y;
        arrowAngle = calculateAngle(trajectoryPoints.get(0), trajectoryPoints.get(1));
        arrowVisible = true; // Reset arrow visibility each time it's fired
        arrowTimer = new Timer(1, new ActionListener() {
            int i = 1;

            public void actionPerformed(ActionEvent evt) {
                if (i < trajectoryPoints.size()) {
                    arrowX = trajectoryPoints.get(i).x;
                    arrowY = trajectoryPoints.get(i).y;
                    if (i < trajectoryPoints.size() - 1) {
                        arrowAngle = calculateAngle(trajectoryPoints.get(i), trajectoryPoints.get(i + 1));

                    }
                    if (checkCollision()) {
                        arrowTimer.stop();
                        arrowVisible = false; // Make arrow disappear after hitting a zombie
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
    //this will check if the arrow has hit a zombie, and if it has, it will take away health points from the zombie by calling the take damage method.
    boolean checkCollision() {
        int arrowTipX = arrowX + 50;
        for (Zombie zombie : zombies) {
            Rectangle zombieBackEdge = new Rectangle(zombie.x + 95, zombie.y, 5, 100);
            if (zombieBackEdge.contains(arrowTipX, arrowY + 25)) {
                zombie.takeDamage();
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
    //this method will calculate the angle of the line of trajectory used for rotating the image of the arrow.
    public double calculateAngle(Point start, Point end) {
        return Math.atan2(end.y - start.y, end.x - start.x);
    }
    //this method will calculate the points of the line of trajectory when the user drags, then draws a line between those points using the slope forumla. 
    public void calculateTrajectoryPoints(Point start, Point end) {
        trajectoryPoints.clear();
        double dx = end.x - start.x;
        double dy = end.y - start.y;
        double slope = -dy / dx;
        double intercept = start.y - slope * start.x;

        int direction;
        if (dx > 0) {
            direction = 1; //any positive number
        } else {
            direction = -1;///any negative number
        }
        for (int x = start.x; x != start.x - dx * 2; x -= direction) {
            int y = (int) (slope * x + intercept);
            int flippedY = 2 * start.y - y;
            trajectoryPoints.add(new Point(x, flippedY));
        }
    }
    //this method will take care of the buttons, and will take the user back to the map panel.
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == returnToMapButton) {
            resetLevel();
            cards.show(manager.cardsPanel, "mapPanel");
        }
    }
    //reset all components of the level, and set them to their original positions.
    public void resetLevel() {
        
        charX = 100;
        charY = 350;
        bowX = 115;
        bowY = 315;
         
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
    
    
    //this class will be the panel that pops up when the user loses the game.
    class losePanel extends JPanel {
        Image background = new ImageIcon(getClass().getResource("Level1Images/YouLose.png")).getImage();
        //initlizes the panel, and sets the layout to null. adds the button and label to the panel.
        public losePanel() {
            setLayout(null);
            returnToMapButton.setBounds(returnToMapButton.getX(), returnToMapButton.getY() -400 , returnToMapButton.getWidth(), returnToMapButton.getHeight());
            returnToMapLabel.setBounds(returnToMapLabel.getX(), returnToMapLabel.getY() -400, returnToMapLabel.getWidth(), returnToMapLabel.getHeight());
            returnToMapLabel.setForeground(Color.RED);
            add(returnToMapButton);
            add(returnToMapLabel);
            
        }
        //draws image
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        }
    }
}

 
