import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.*;
//this is the class that pops up when the user clicks go to map, and this will navigate the user to each level.
public class MapPanel extends JPanel implements ActionListener {
    Image background, ufo,lock;
    Main manager;
    CardLayout cards;
    int ufoX, ufoY, targetX, targetY;
    JButton planet1Button, planet2Button, planet3Button,startButton;
    JLabel startLabel;
    Timer animationTimer;
    int delay = 20;
    int stepSize = 5;  
    boolean isLocked = true;
    boolean isLocked2 = true;
    Level1Panel level1;
    Level2Panel level2;
    //initlize all buttons, images, and other components
    public MapPanel(Main mn, CardLayout cd, Level1Panel l1,Level2Panel l2) {
        level1 = l1;
        level2 = l2;
        cards = cd;
        manager = mn;
        setBackground(Color.white);
        setLayout(null);

        background = new ImageIcon(getClass().getResource("resources/planets.png")).getImage();
        ufo = new ImageIcon(getClass().getResource("resources/Ufo.png")).getImage();
        lock = new ImageIcon(getClass().getResource("resources/lock.png")).getImage();

        ufoX = 380;
        ufoY = 320;
        targetX = ufoX;
        targetY = ufoY;

        planet1Button = new JButton("");
        planet1Button.addActionListener(this);
        planet1Button.setOpaque(false);
        planet1Button.setContentAreaFilled(false);
        planet1Button.setBorderPainted(false);
        planet1Button.setBounds(35, 100, 200, 170);

        planet2Button = new JButton("");
        planet2Button.addActionListener(this);
        planet2Button.setOpaque(false);
        planet2Button.setContentAreaFilled(false);
        planet2Button.setBorderPainted(false);
        planet2Button.setBounds(350, 30, 200, 140);
        
       

        planet3Button = new JButton("");
        planet3Button.addActionListener(this);
        planet3Button.setOpaque(false);
        planet3Button.setContentAreaFilled(false);
        planet3Button.setBorderPainted(false);
        planet3Button.setBounds(660, 100, 200, 170);
       
        startButton = new JButton("");
        startButton.addActionListener(this);
        startButton.setOpaque(false);
        startButton.setContentAreaFilled(false);
        startButton.setBorderPainted(false);
        startButton.setBounds(350, 610, 200, 60);

        startLabel = new JLabel("Go Back");
        startLabel.setBounds(355, 615, 200, 60);
        startLabel.setForeground(Color.GREEN); 
        startLabel.setFont(buttonFont());
        if(isLocked==true)
        planet2Button.setEnabled(false);
        if(isLocked2==true)
        planet3Button.setEnabled(false);

        add(planet1Button);
        add(planet2Button);
        add(planet3Button);
        add(startButton);
        add(startLabel);
        //this will reset the ufo to the starting position when the map is shown. i.e beating a level and going back to the map.
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                ufoX = 380;
                ufoY = 320;
                repaint();
            }
        });
    




        // Initialize animation timer
        animationTimer = new Timer(delay, new ActionListener() {
            //this will move the ufo to the target planet.
            public void actionPerformed(ActionEvent e) {
                if (moveUfo()) {
                    animationTimer.stop();
                }
                repaint();
            }
        });
    }


    @Override
    //draws images
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        g.drawImage(ufo, ufoX, ufoY, 100, 100, null);
        if(isLocked)
        {
            g.drawImage(lock, planet2Button.getX(), planet2Button.getY(), planet2Button.getWidth(), planet2Button.getHeight(), null);

        }
        if(isLocked2)
        {
            g.drawImage(lock, planet3Button.getX(), planet3Button.getY(), planet3Button.getWidth(), planet3Button.getHeight(), null);


        }

        
    }
    //transports the ufo to the planet, sets the target x and y to the planet's x and y, and starts the animation timer.
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == planet1Button) {
            targetX = 70;  
            targetY = 135; 
            animationTimer.start();
        }
        if (e.getSource() == planet2Button) {
            targetX = 400;  
            targetY = 40; 
            animationTimer.start();

        }
        if (e.getSource() == planet3Button) {
            targetX = 690;  
            targetY = 120; 
            animationTimer.start();
        }
        if(e.getSource()==startButton)
        {
            cards.show(manager.cardsPanel,"startPanel");
        }

    }
    //checks if the ufo is at the target planet, if not, animates the movement to get there.
    public boolean moveUfo() {
        int deltaX = targetX - ufoX;
        int deltaY = targetY - ufoY;
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (distance > 0) {
            int moveX = (int) Math.round(stepSize * (deltaX / distance));
            int moveY = (int) Math.round(stepSize * (deltaY / distance));
            
            ufoX += moveX;
            ufoY += moveY;
            
            if (Math.abs(targetX - ufoX) < Math.abs(moveX)) {
                ufoX = targetX;
            }
            if (Math.abs(targetY - ufoY) < Math.abs(moveY)) {
                ufoY = targetY;
            }
        }
        
        if (ufoX == targetX && ufoY == targetY) {
            if (targetX == 70 && targetY == 135) {
                cards.show(manager.cardsPanel, "Level1");
                level1.respawnTimer.start();
                level1.animationTimer.start();
            } else if (targetX == 400 && targetY == 40) {
                cards.show(manager.cardsPanel, "Level2");
                level2.respawnTimer.start();
                level2.animationTimer.start();
                level2.smashBallSpawnTimer.start();

            } else if (targetX == 690 && targetY == 120) {
                cards.show(manager.cardsPanel, "Level3"); 
                manager.level3.gunTimer.start();
                manager.level3.bossMoveTimer.start();

                
                
            }
            return true;
        }
        return false;
    }

    
    // font of the "go back" button
    public Font buttonFont() {
        Font customFont = null;
        try {
            try (InputStream fontStream = getClass().getResourceAsStream("resources/Foul Fiend.ttf")) {
                customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(24f); // Larger size for testing
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(customFont);
            } // Larger size for testing
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
        return customFont;
    }
}
