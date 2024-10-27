import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
//this class will be the panel that pops up when user clicks "Instructions"
public class InstructionPanel extends JPanel implements ActionListener {
    Clip typingSoundClip;
    CardLayout cards;
     Image background;
     Main manager;
     Font font;
     JButton startButton;
     JLabel startLabel;
     JLabel[] instructionLabels;
     Timer typingTimer;
     String[] instructionsTexts;
     int currentLabelIndex = 0;
     int currentCharIndex = 0;

    //initlize variables and call key methods to reduce clutter in constructor.
    public InstructionPanel(Main mn, CardLayout cd) {
        cards = cd;
        setLayout(null);
        manager = mn;
        background = new ImageIcon(getClass().getResource("resources/InstructionsBackground.png")).getImage();
        font = loadFont();

        instructionsTexts = new String[]{
            "1. Navigate to the map and click on the available planet.",
            "2. Planets are levels. Unlock each by completing the previous one.",
            "3. To complete a level, eliminate all zombie waves before crossing.",
            "4. To shoot, hold and drag back to aim.",
            "5. After clearing a level, return to the main map to access the next.",
            "6. SAVE THE WORLD."
        };
        startButton = new JButton("");
        startButton.addActionListener(this);
        startButton.setOpaque(false);
        startButton.setContentAreaFilled(false);
        startButton.setBorderPainted(false);
        startButton.setBounds(300, 20, 200, 60);

        startLabel = new JLabel("Go Back");
        startLabel.setBounds(305, 25, 200, 60);
        startLabel.setForeground(Color.GREEN); 
        startLabel.setFont(buttonFont());
        
        add(startButton);
        add(startLabel);


        instructionLabels = new JLabel[instructionsTexts.length];
        createInstructionLabels();
        animateTyping();
    }
    //this creates an array of jlabels that are empty. This will be used in the animateTyping method to type out the instructions.
    public void createInstructionLabels() {
        int y = 100;
        for (int i = 0; i < instructionsTexts.length; i++) {
            instructionLabels[i] = new JLabel("");
            instructionLabels[i].setBounds(50, y, 750, 60);
            instructionLabels[i].setFont(font);
            instructionLabels[i].setForeground(Color.white);
            add(instructionLabels[i]);
            y += 100;
        }
    }
    //makes it so that the the instructions are typed out letter by letter, instead of all at once.
     public void animateTyping() {
        typingTimer = new Timer(35, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = instructionsTexts[currentLabelIndex];
                if (currentCharIndex < text.length()) {
                    instructionLabels[currentLabelIndex].setText(instructionLabels[currentLabelIndex].getText() + text.charAt(currentCharIndex));
                    currentCharIndex++;
                } else if (currentLabelIndex < instructionsTexts.length - 1) {
                    currentLabelIndex++;
                    currentCharIndex = 0;
                } else {
                    typingTimer.stop();
                }
            }
        });
        // typingTimer.start();
        // playSound("resources/typing.wav");

    }
    //font used for the instructions
    public Font loadFont() {
        try {
            InputStream fontStream = getClass().getResourceAsStream("resources/Foul Fiend.ttf");
            return Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(15f); // Larger size for testing
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
            return new Font("SansSerif", Font.BOLD, 15);
        }
    }
    //this method will take care of the sound effect when the instructions are being typed out.
    public void playSound(String soundFileName) {
        try {
            if (typingSoundClip != null && typingSoundClip.isRunning()) {
                typingSoundClip.stop(); // Stop the sound if it's still running
            }
            
            URL url = this.getClass().getResource(soundFileName);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            typingSoundClip = AudioSystem.getClip();
            typingSoundClip.open(audioIn);
            typingSoundClip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    //when the user clicks go back the sound stops
    public void stopSound() {
        if (typingSoundClip != null) {
            typingSoundClip.stop();
            typingSoundClip.close();
        }
    }
   
    

    //draws images
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }
    //goes back to the main menu
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==startButton)
        {
            cards.show(manager.cardsPanel,"startPanel");
            stopSound();
            typingTimer.stop();
        }
    }
    //custom font for the labels/buttons
    public Font buttonFont() {
        Font customFont = null;
        try {
            InputStream fontStream = getClass().getResourceAsStream("resources/Foul Fiend.ttf");
            customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(24f); // Larger size for testing
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
            fontStream.close();
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
        return customFont;
    }
}
