import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.*;

//this class will be the shop panel, where the user can buy different arrows.
public class ShopPanel extends JPanel implements MouseListener, ActionListener {
    Image background, arrow1, arrow2, arrow3, coin;
    JLabel coinsLabel, arrow3Label, arrow1Label, arrow2Label;
    int arrow1X = 100, arrow1Y = 250, arrow2X = 325, arrow2Y = 220, arrow3X = 600, arrow3Y = 220;
    Image selectedArrow = null;
    JLabel priceLabel, speedLabel, damageLabel;
    Rectangle snapArea = new Rectangle(250, 430, 300, 120);
    Main manager;
    CardLayout cards;
    int balance=0, arrow3Price, arrow1Price, arrow2Price;
    JButton buyButton;
    int selectedArrowPrice;
    Level1Panel level1;
    Level2Panel level2;
    Level3Panel level3;
    int arrow1Damage = 1;
    int arrow2Damage = 2;
    int arrow3Damage = 3;
    Image coinBar;

    // initilizes the shop panel, and sets up all the buttons and labels
    public ShopPanel(Main mn, CardLayout cd) {
        manager = mn;
        cards = cd;
        arrow1Price = 2;
        arrow2Price = 5;
        arrow3Price = 7;
        Color darkGreen = new Color(0, 100, 0);
        buyButton = new JButton("Buy");
        buyButton.setBounds(550, 475, 100, 50);
        buyButton.setForeground(darkGreen);
        buyButton.addActionListener(this);
        buyButton.setFont(buttonFont());
        add(buyButton);

        setBackground(Color.white);
        setLayout(null);
        loadImages();
        setupLabels();
        addMouseListener(this);

        addComponentListener(new ComponentAdapter() {
            //when the component is shown, the coins label will be updated to the current balance
            public void componentShown(ComponentEvent e) {
                coinsLabel.setText(""+balance);
            }
        });
        // have a button with at the top middle that says Go back
        JButton backButton = new JButton("Go Back");
        backButton.setBounds(300, 0, 200, 50);
        backButton.setOpaque(false);
        backButton.setContentAreaFilled(false);
        backButton.setBorderPainted(false);
        backButton.setForeground(Color.WHITE);
        backButton.setFont(buttonFont());
        backButton.setForeground(Color.green);
        backButton.addActionListener(this);
        add(backButton);

    }

    // used for passing in instances of the other levels withouth using circular
    // logic in the main class
    public void setLevel1(Level1Panel level1, Level2Panel level2, Level3Panel level3) {
        this.level1 = level1;
        this.level2 = level2;
        this.level3 = level3;

    }

    // checks for the user buying the arrow or going back to main menu
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == buyButton) {
            purchaseArrow();
        }
        if (e.getActionCommand().equals("Go Back")) {
            cards.show(manager.cardsPanel, "mapPanel");
        }
    }

    // checks if the user has enough money to buy the arrow, shows a message if they do not
    private void purchaseArrow() {
        if (balance >= selectedArrowPrice) {
            balance -= selectedArrowPrice;
            coinsLabel.setText("" + balance);
            JOptionPane.showMessageDialog(this, "Press OK to equip", "Purchase Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            level1.arrowImage = selectedArrow;
            level2.arrowImage = selectedArrow;
            level3.arrowImage = selectedArrow;

        } else {
            JOptionPane.showMessageDialog(this, "You do not have enough funds", "Purchase Failed",
                    JOptionPane.ERROR_MESSAGE);

        }
    }

    // loads the images for the shop panel
    public void loadImages() {
        background = new ImageIcon(getClass().getResource("resources/shopBackground.png")).getImage();
        arrow1 = new ImageIcon(getClass().getResource("resources/arrow1.png")).getImage();
        arrow2 = new ImageIcon(getClass().getResource("resources/arrow2.png")).getImage();
        arrow3 = new ImageIcon(getClass().getResource("resources/arrow3.png")).getImage();
        coin = new ImageIcon(getClass().getResource("resources/coin.png")).getImage();
        coinBar = new ImageIcon(getClass().getResource("resources/coinBar.png")).getImage();

    }

    // sets up the labels for the shop panel
    public void setupLabels() {
        coinsLabel = createLabel(""+balance, 675, 0, 250, 60, Color.WHITE);
        arrow3Label = createLabel("MoneyMaker", 100, 180, 300, 60, Color.RED);
        arrow2Label = createLabel("Stone Arrow", 350, 180, 300, 60, Color.RED);
        arrow1Label = createLabel("Magic Arrow", 600, 180, 300, 60, Color.RED);

        priceLabel = createLabel("Price: ", 78, 570, 225, 60, Color.black);
        speedLabel = createLabel("Speed: ", 300, 570, 200, 60, Color.black);
        damageLabel = createLabel("Damage: ", 570, 570, 250, 60, Color.black);
    }
    // creates a label with the given text, x, y, width, height, and color. used for less repetition
    public JLabel createLabel(String text, int x, int y, int width, int height, Color color) {
        JLabel label = new JLabel(text);
        label.setBounds(x, y, width, height);
        label.setForeground(color);
        label.setFont(buttonFont());
        add(label);
        return label;
    }
    // updates the labels for the arrows. used for less repetition
    public void updateLabels(int price, String speed, String damage) {
        priceLabel.setText("Price: " + price);
        speedLabel.setText("Speed: " + speed);
        damageLabel.setText("Damage: " + damage);
    }
    // paints the components of the shop panel
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        g.drawImage(arrow1, arrow1X, arrow1Y, 150, 100, this);
        g.drawImage(arrow2, arrow2X, arrow2Y, 200, 150, this);
        g.drawImage(arrow3, arrow3X, arrow3Y, 200, 150, this);
        g.drawImage(coinBar, 550, 0, 250, 60, this);
        // g.drawImage(coin, 740, 0, 50, 50, this);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(5));
        g2d.setColor(Color.blue);
        g2d.draw(snapArea); // Drawing the snap area
    }
    // checks if the user has clicked on an arrow, and if they have enough money to buy it
    public void mousePressed(MouseEvent e) {
        if (new Rectangle(arrow1X, arrow1Y, 150, 100).contains(e.getPoint())) {
            selectedArrowPrice = arrow1Price;
            arrow1X = snapArea.x + (snapArea.width - 150) / 2;
            arrow1Y = snapArea.y + (snapArea.height - 100) / 2;
            repaint();
            updateLabels(arrow1Price, "Med", "Low");
            selectedArrow = arrow1;
        } else if (new Rectangle(arrow2X, arrow2Y, 200, 150).contains(e.getPoint())) {
            selectedArrowPrice = arrow2Price;
            arrow2X = snapArea.x + (snapArea.width - 170) / 2;
            arrow2Y = snapArea.y + (snapArea.height - 150) / 2;
            repaint();
            updateLabels(arrow2Price, "High", "Med");
            selectedArrow = arrow2;
        } else if (new Rectangle(arrow3X, arrow3Y, 200, 150).contains(e.getPoint())) {
            selectedArrowPrice = arrow3Price;
            arrow3X = snapArea.x + (snapArea.width - 175) / 2;
            arrow3Y = snapArea.y + (snapArea.height - 150) / 2;
            repaint();
            updateLabels(arrow3Price, "Low", "High");
            selectedArrow = arrow3;

        }
        if (snapArea.contains(e.getPoint())) {
            if (new Rectangle(arrow1X, arrow1Y, 150, 100).contains(e.getPoint())) {
                arrow1X = 100;
                arrow1Y = 250;
                repaint();
            } else if (new Rectangle(arrow2X, arrow2Y, 200, 150).contains(e.getPoint())) {
                arrow2X = 325;
                arrow2Y = 220;
                repaint();
            } else if (new Rectangle(arrow3X, arrow3Y, 200, 150).contains(e.getPoint())) {
                arrow3X = 600;
                arrow3Y = 220;
                repaint();
            }
        }
    }
    // used for the font of the buttons
    public Font buttonFont() {
        try (InputStream fontStream = getClass().getResourceAsStream("resources/Foul Fiend.ttf")) {
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(24f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
            return customFont;
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
            return new Font("Serif", Font.BOLD, 24);
        }
    }
    // unused methods but will be called when user releases mouse
    public void mouseReleased(MouseEvent e) {
    }
    // unused methods but will be called when user clicks mouse
    public void mouseClicked(MouseEvent e) {
    }
    // unused methods but will be called when user enters mouse in the frame
    public void mouseEntered(MouseEvent e) {
    }
    // unused methods but will be called when user exits mouse out of frame
    public void mouseExited(MouseEvent e) {
    }
}
