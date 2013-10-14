//Implements the grid for the human player as a JPanel of buttons.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;
import java.io.*;

public class GridGUI extends JPanel {

 ArrayList<BSButton> buttons = new ArrayList<BSButton>();
 ArrayList<Ship> allShips = new ArrayList<Ship>();
 int[] testLocations;
 int numOfGuesses = 0;
 String text = "";
 int rows;
 int columns;
 boolean clicked = false;
 boolean endGame = false;
 Color darkRed = new Color(100, 0, 0);
 Border loweredBevel = BorderFactory.createLoweredBevelBorder();

 Ship destroyer = new Ship(2, "destroyer");
 Ship cruiser = new Ship(3, "cruiser");
 Ship submarine = new Ship(3, "submarine");
 Ship battleship = new Ship(4, "battleship");
 Ship aircraftCarrier = new Ship(5, "aircraft carrier");

 public GridGUI(int r, int c) {
  rows = r;
  columns = c;
 }

 public void build() {

  //Add all ships to an ArrayList to allow for cycling through all ships.

  allShips.add(destroyer);
  allShips.add(cruiser);
  allShips.add(submarine);
  allShips.add(battleship);
  allShips.add(aircraftCarrier);

  //Make an ArrayList of buttons for each cell on the grid.

  for(int i = 0; i < (rows * columns); i++) {
   BSButton b = new BSButton();
   b.setEnabled(false);
   b.setGridLocation(i);
   buttons.add(b);
  }

  setShipLocations();

  GridLayout g = new GridLayout(rows,columns);
  this.setLayout(g);

  //Add listeners to all cells in grid to listen for guesses.

  for (BSButton bsb : buttons) {
   bsb.addActionListener(new MyCellListener());
   this.add(bsb);
  }
 }

 public void setShipLocations() {

  for(Ship s : allShips) {
   int shipLength = s.getLength();
   int clearSpace = 0;
   testLocations = new int[shipLength];

   //Randomly select starting position to place ship and check if sufficient space to place ship.

   while(clearSpace < shipLength) {

    //Randomly choose whether to place ship vertically or horizontally and choose location of ship.

    boolean vert = new Random().nextBoolean();
    int x;
    int y;

    if(vert) {

     x = (int) (Math.random() * (columns));
     y = (int) (Math.random() * (rows - shipLength));
     for(int i = 0; i < shipLength; i++) {
      testLocations[i] = x + (columns*(y+i));
     }
    } else {
     x = (int) (Math.random() * (columns - shipLength));
     y = (int) (Math.random() * (rows));
     for(int i = 0; i < shipLength; i++) {
      testLocations[i] = x + i + (columns*y);
     }
    }

    //Check if the location is clear.

    clearSpace = 0;
    for(int i = 0; i < shipLength; i++) {
     if(buttons.get(testLocations[i]).getCellContents() == null) {
      clearSpace++;
     }
    }
   }

   //Set the contents of the chosen cells to contain the ship.

   for(int i = 0; i < shipLength; i++) {
    buttons.get(testLocations[i]).setCellContents(s);
   }

   testLocations = null;
  }
 }

 //Methods related to whether a cell has been clicked/guess has been made.

 public boolean getClicked() {
  return clicked;
 }

 public void setClicked() {
  clicked = false;
 }

 //Methods related to whether the game is over.

 public boolean getEndGame() {
  return endGame;
 }

 public void setEndGame() {
  for(JButton j : buttons){
   j.setEnabled(false);
  }
 }

 public class MyCellListener implements ActionListener {
  public void actionPerformed(ActionEvent a) {
   if(!clicked) {
    BSButton cell = (BSButton) a.getSource();
    Ship s = cell.getCellContents();
    boolean killed = false;
    numOfGuesses++;
    boolean gameOver = true;

    //Mark cell as guessed.

    cell.setEnabled(false);
    cell.setBorder(loweredBevel);

    if(s == null) {
     //Mark cell as missed.
     text = "You missed. Other player's turn...";
     cell.setBackground(Color.lightGray);
    } else {
     killed = s.counter();
     if(killed) {
      //Mark all of the ship's cells as killed.
      text = "You sunk the " + s.getName() + "! Other player's turn...";
      for(BSButton bu : buttons) {
       if(bu.getCellContents() == s) {
        bu.setBackground(darkRed);
       }
      } 
     } else {
      //Mark cell as hit.
      text = "You got a hit. Other player's turn...";
      cell.setBackground(Color.red);
     }    
    }

    //If any ships remain unkilled, game is not over.
    for(Ship sh : allShips) {
     if(!sh.isKilled()) {
      gameOver = false;
     }
    }

    if(gameOver) {
     text = "You win! You took " + numOfGuesses + " guesses.";
     endGame = true;
    }

    clicked = true;
   }
  }
 }

 public void enableCells() {
  for(BSButton bsb : buttons) {
   bsb.setEnabled(true);
  }
 }

 public String getText() {
  return text;
 }
}
