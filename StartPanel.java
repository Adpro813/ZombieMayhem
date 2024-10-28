
//this class will show the loading screen. It will include the compoinents to go to the main parts of the game.
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
public class StartPanel extends JPanel implements ActionListener {
    Image background,skull;
    Main manager;
    CardLayout cards;
    JButton instrusctionButton,mapButton,quitButton;
    JLabel instructionLabel,mapLabel,titleLabel,quitLabel;
    InstructionPanel instructionPanel;
    //this constructor will initlize all components/images and add them to the panel.
    public StartPanel(Main mn, CardLayout cd,InstructionPanel ip) {
        instructionPanel = ip;
        cards = cd;
        manager = mn;
        setBackground(Color.white);
        setLayout(null); 

        background = new ImageIcon(getClass().getResource("resources/background.png")).getImage();
        skull = new ImageIcon(getClass().getResource("resources/skull.png")).getImage();

        
        mapButton = new JButton("");
        mapButton.addActionListener(this);
        mapButton.setOpaque(false);
        mapButton.setContentAreaFilled(false);
        mapButton.setBorderPainted(false);
        mapButton.setBounds(316, 600, 200, 60);
        add(mapButton);

        instrusctionButton = new JButton("");
        instrusctionButton.addActionListener(this);
        instrusctionButton.setOpaque(false);
        instrusctionButton.setContentAreaFilled(false);
        instrusctionButton.setBorderPainted(false);
        instrusctionButton.setBounds(75, 600, 225, 60);
        add(mapButton);

        mapLabel = new JLabel("Go to Map");
        mapLabel.setBounds(340, 603, 200, 60);
        mapLabel.setForeground(Color.WHITE); 
        mapLabel.setFont(buttonFont());
        
        instructionLabel = new JLabel("Instructions");
        instructionLabel.setBounds(78, 603, 225, 60);
        instructionLabel.setForeground(Color.WHITE); 
        instructionLabel.setFont(buttonFont());

        titleLabel = new JLabel("Z    MBIE MAYHEM");
        titleLabel.setBounds(285, 0, 500, 60);
        titleLabel.setForeground(Color.white); 
        titleLabel.setFont(buttonFont());

        quitButton = new JButton("");
        quitButton.addActionListener(this);
        quitButton.setOpaque(false);
        quitButton.setContentAreaFilled(false);
        quitButton.setBorderPainted(false);
        quitButton.setBounds(557, 600, 100, 60);
        
        quitLabel = new JLabel("Shop");
        quitLabel.setBounds(570, 603, 100, 60);
        quitLabel.setForeground(Color.WHITE); 
        quitLabel.setFont(buttonFont());

        add(mapButton);
        add(instrusctionButton);
        add(mapLabel);
        add(instructionLabel);
        add(titleLabel);
        add(quitButton);
        add(quitLabel);


       

        
    }
    
    //this method will draw all neccesary iamges
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        g.drawImage(skull,300,0,60,50,null);
    }

    @Override
    //take care of the buttons and other coponents.
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == mapButton) {
            System.out.println("Map button pressed.");
            cards.show(manager.cardsPanel, "mapPanel");
        }
        if(e.getSource()==instrusctionButton)
        {
            instructionPanel.typingTimer.start();
            instructionPanel.playSound("resources/typing.wav");
            cards.show(manager.cardsPanel, "instructionPanel");
        }
        if(e.getSource()==quitButton)
        {
            cards.show(manager.cardsPanel, "shopPanel");
        }


        
    }
    //this method will create and return a new font for the labels, and that will be used for the buttons' text.
    public Font buttonFont() {
        Font customFont = null;
        try {
            InputStream fontStream = getClass().getResourceAsStream("resources/Foul Fiend.ttf");
            customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(24f); // Larger size for testing
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
            fontStream.close();
            mapLabel.setFont(customFont);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
        return customFont;
    }


}
