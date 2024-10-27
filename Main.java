//Aditya Dendukuri
//Main.java
//Zombie Mayhem
import java.awt.CardLayout;
import javax.swing.*;
//this class  will run the starting Panel. It will also hold the main method to run the program. It will also hold the card layout manager.
public class Main extends JFrame {
     JPanel cardsPanel;
     CardLayout cards;
     StartPanel startPanel;
     MapPanel mapPanel;
     InstructionPanel InstructionPanel;
     ShopPanel shopPanel;
     Level1Panel level1;
     Level2Panel level2;
     Level3Panel level3;
    //this constructor initilizes the frame and the card layout and calls the other classes.
    public Main() {
        super("Zombie Mayhem");

        cards = new CardLayout();
        cardsPanel = new JPanel(cards);

        shopPanel =  new ShopPanel(this,cards);
        level1 = new Level1Panel(this,cards,shopPanel);
        level2 = new Level2Panel(this,cards,shopPanel);
        level3 = new Level3Panel(this,cards,cardsPanel,shopPanel);
        mapPanel = new MapPanel(this,cards,level1,level2);
        InstructionPanel = new InstructionPanel(this, cards);
        startPanel = new StartPanel(this, cards,InstructionPanel);
        setContentPane(cardsPanel);
        shopPanel.setLevel1(level1,level2,level3);
        
        cardsPanel.add(startPanel, "startPanel");
        cardsPanel.add(mapPanel,"mapPanel");
        cardsPanel.add(InstructionPanel,"instructionPanel");
        cardsPanel.add(shopPanel, "shopPanel");
        cardsPanel.add(level1, "Level1");
        cardsPanel.add(level2, "Level2");
        cardsPanel.add(level3, "Level3");

        cards.show(cardsPanel, "startPanel");


        setLocation(275, 100);
        setSize(825, 693);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    //runs the program, creates instance of main which runs the constructor of other classes
    public static void main(String[] args) {
        new Main();

    }
}
