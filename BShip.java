//Main class for BShip game.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;

public class BShip {

 boolean shipsPlaced = false;
 int rows = 10;
 int columns = 10;
 JFrame theFrame = new JFrame("Battleship DB");
 JPanel thePanel = new JPanel();
 GridGUI grid1;
 AIGridGUI grid2;
 JPanel textPanel = new JPanel();
 JTextArea textArea = new JTextArea();
 JMenuItem[] boardSize = new JMenuItem[3];
 JMenuItem auto;
 boolean autoPlacement = false;
 Object lock = new Object();

 public static void main(String[] args) {
  BShip game = new BShip();
  game.startGame();
 }

 public void startGame() {

  //Create grids and rest of GUI.

  grid1 = new GridGUI(rows, columns);
  grid1.build();

  grid2 = new AIGridGUI(rows, columns);

  buildGUI();

  //Place ships on grid 2.

  if(autoPlacement) {
   grid2.autoPlaceShips();
  } else {
   grid2.placeShips();
  }

  //Start threads to check if ships have been placed and if guesses have been made.

  Runnable reader = new IncomingGuessReader();
  Thread readerThread = new Thread(reader);
  readerThread.start();

  Runnable spReader = new ShipsPlacedReader();
  Thread spReaderThread = new Thread(spReader);
  spReaderThread.start();
 }

 public void buildGUI() {

  thePanel.setLayout(new BoxLayout(thePanel, BoxLayout.PAGE_AXIS));
  thePanel.add(grid1);

  //Setup menu with options for new game, varying board sizes, and auto placement of ships.

  boardSize[0] = new JMenuItem("8x8");
  boardSize[1] = new JMenuItem("10x10");
  boardSize[2] = new JMenuItem("12x12");

  JMenuBar bar = new JMenuBar();
  JMenu menu = new JMenu("file");
  JMenuItem newGame = new JMenuItem("New Game");
  newGame.addActionListener(new NewGameListener());
  JMenu sizes = new JMenu("Board Size");
  for(int i = 0; i < boardSize.length; i++) {
   boardSize[i].addActionListener(new NewGameListener());
   sizes.add(boardSize[i]);
  }
  auto = new JMenuItem("Auto Place Ships");
  auto.addActionListener(new NewGameListener());
  auto.setEnabled(true);
  menu.add(newGame);
  menu.add(sizes);
  menu.add(auto);
  bar.add(menu);

  //Setup text area between grids to show game status.

  textArea.setText("");
  textPanel.setBackground(Color.white);
  textPanel.setMaximumSize(new Dimension(700, 10));
  textPanel.add(textArea);
  thePanel.add(textPanel);

  thePanel.add(grid2);

  theFrame.add(thePanel);
  theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  theFrame.setJMenuBar(bar);
  theFrame.setBounds(0,0,500,750);  
  theFrame.setVisible(true);
 }

 public class NewGameListener implements ActionListener {
  public void actionPerformed(ActionEvent a) {

   thePanel.remove(grid1);
   thePanel.remove(grid2);
   grid1 = null;
   grid2 = null;
   shipsPlaced = false;

   //Set new game grid size to selected dimensions.

   if(a.getSource() == boardSize[0]) {
    rows = 8;
    columns = 8;
   }

   if(a.getSource() == boardSize[1]) {
    rows = 10;
    columns = 10;
   }

   if(a.getSource() == boardSize[2]) {
    rows = 12;
    columns = 12;
   }

   //Determine whether auto placement selected.

   if(a.getSource() == auto) {
    autoPlacement = true;
   } else {
    autoPlacement = false;
   }

   startGame();
  }
 }

 public class IncomingGuessReader implements Runnable {
  public void run() {
   while(grid1 != null && grid2 != null) {

    if(grid1.getEndGame() || grid2.getEndGame()) {
     //Disable all cells in grid if game over.
     grid1.setEndGame();
    } else {

     if(grid2.areShipsPlaced()) {
      //Notify other thread when all ships have been placed.
      shipsPlaced = true;		
      synchronized (lock) {
       lock.notifyAll();
      }
     } else {
      //While not all ships have been placed, get the text to display from grid 2.
      textArea.setText(grid2.getText());
     }

     if(grid1.getClicked()) {

      //If grid 1 has been clicked, get the text to display.
      textArea.setText(grid1.getText());

      //Pause 2 seconds before continuing to the computer's turn.
      try {
       Thread.sleep(2000);
      } catch(InterruptedException ex) {
       ex.printStackTrace();
      }

      if(!grid1.getEndGame()) {
       //Computer player takes its turn and appropriate text is displayed.
       grid2.go();
       textArea.setText(grid2.getText());
      } else {
       textArea.setText(grid1.getText());
      }

      //Reset the clicked variable to false until a cell has been clicked again.
      grid1.setClicked();
     }
    }
   }
  }
 }

 public class ShipsPlacedReader implements Runnable {
  public void run() {
   //Wait until all ships have been placed before enabling grid 1 cells.
   while(grid1 != null && grid2 != null) {
    while(!shipsPlaced) {
     synchronized (lock) {
      try {
       lock.wait();
      } catch(InterruptedException e) {}
     }
	}
    grid1.enableCells();
    auto.setEnabled(false);
    break;
   }
  }
 }
}
