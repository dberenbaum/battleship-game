//Implements the grid for the computer/AI player as a JPanel of buttons.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;
import java.io.*;

public class AIGridGUI extends JPanel {

 ArrayList<BSButton> buttons = new ArrayList<BSButton>();
 ArrayList<Ship> allShips = new ArrayList<Ship>();
 ArrayList<SetShipsListener> listeners = new ArrayList<SetShipsListener>();
 int[] testLocations;
 int numOfGuesses = 0;
 String text;
 int rows;
 int columns;
 boolean endGame = false;
 boolean[] cellsGuessed;
 boolean[] cellsHit;
 boolean[] cellsKilled;
 boolean randomGuess = true;
 int firstHit;
 Color darkRed = new Color(100, 0, 0);
 Border loweredBevel = BorderFactory.createLoweredBevelBorder();
 Border defaultBorder;
 Ship shipToPlace;
 boolean vertical = false;
 boolean clear;
 boolean shipsPlaced = false;

 Ship destroyer = new Ship(2, "destroyer");
 Ship cruiser = new Ship(3, "cruiser");
 Ship submarine = new Ship(3, "submarine");
 Ship battleship = new Ship(4, "battleship");
 Ship aircraftCarrier = new Ship(5, "aircraft carrier");

 ArrayList<Direction> directions = new ArrayList<Direction>();
 Direction up = new Direction();
 Direction down = new Direction();
 Direction right = new Direction();
 Direction left = new Direction();

 public AIGridGUI(int r, int c) {

  rows = r;
  columns = c;

  //Create arrays to keep track of which cells have been guessed, hit, and killed/sunk.

  cellsGuessed = new boolean[(rows * columns)];
  cellsHit = new boolean[(rows * columns)];
  cellsKilled = new boolean[(rows * columns)];
  for(int i = 0; i < (rows * columns); i++) {
   cellsGuessed[i] = false;
   cellsHit[i] = false;
   cellsKilled[i] = false;
  }

  //Add all ships to an ArrayList to allow for cycling through all ships.

  allShips.add(destroyer);
  allShips.add(cruiser);
  allShips.add(submarine);
  allShips.add(battleship);
  allShips.add(aircraftCarrier);

  //Add all directions to an ArrayList to allow for comparing and sorting directions.

  directions.add(up);
  directions.add(down);
  directions.add(right);
  directions.add(left);

  //Make grid that consists of r rows and c columns of buttons.

  GridLayout g = new GridLayout(rows,columns);
  this.setLayout(g);

  for(int i = 0; i < (rows * columns); i++) {
   BSButton b = new BSButton();
   b.setGridLocation(i);
   buttons.add(b);
   this.add(b);
  }

  defaultBorder = buttons.get(0).getBorder();
 }

 public void autoPlaceShips() {

  //If ships are to be placed automatically, randomly place each ship.

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

  //Mark all cells containing a ship and disable all cells in the grid.

  for (BSButton bsb : buttons) {
   if(bsb.getCellContents() != null) {
    bsb.setBackground(Color.blue);
    bsb.setBorder(loweredBevel);
   }
   bsb.setEnabled(false);
  }

  text = "Ready to start the game.";
  shipsPlaced = true;
 }

 public void placeShips() {

  //Add listeners to all cells in grid to listen for ship placement.

  for(int i = 0; i < buttons.size(); i++) {
   listeners.add(new SetShipsListener());
   buttons.get(i).addMouseListener(listeners.get(i));
  }

  shipToPlace = allShips.get(0);
  text = "Place " + shipToPlace.getName() + ". Right click to toggle horizontal/vertical.";
 }

 public boolean getEndGame() {

  return endGame;
 }

 public void go() {

  //Play a turn by making a guess at a cell.

  int guessLocation = 0;
  boolean gameOver = true;
  numOfGuesses++;
  BSButton b = null;
  Ship s = null;
  boolean killed = false;
  boolean isClear = false;

  if(randomGuess) {

   //Find out the maximum ship length of the surviving ships. The guess should have at least this much clear space around it.

   int minClearSpace = 0;
   for(Ship sh : allShips) {
    if(!sh.isKilled() && sh.getLength() > minClearSpace) {
     minClearSpace = sh.getLength();
    }
   }

   //Create an array of all possible cells and shuffle the order of the cells. Potential guesses are drawn from this array.

   int[] guesses = new int[(rows * columns)];
   for(int i = 0; i < rows * columns; i++) {
    guesses[i] = i;
   }
   Random rand = new Random();
   for(int i = 0; i < guesses.length; i++) {
    int randInt = rand.nextInt(guesses.length);
    int randGuess = guesses[randInt];
    guesses[randInt] = guesses[i];
    guesses[i] = randGuess;
   }

   int numCellsTried = 0;

   //Test potential guesses and mark clear if criteria met.

   while(!isClear && numCellsTried < guesses.length) {
    guessLocation = guesses[numCellsTried];
    numCellsTried++;

    //If the cell has not already been guessed, test whether there are enough clear spaces in at least one direction.

    if(!cellsGuessed[guessLocation]) {

     int u = guessLocation;
     int upCount = -1;
     while(u >= 0 && !cellsHit[u]) {
      u = moveUp(u);
      upCount++;
     }
    
     int d = guessLocation;
     int downCount = -1;
     while(d >= 0 && !cellsHit[d]) {
      d = moveDown(d);
      downCount++;
     }

     int r = guessLocation;
     int rightCount = -1;
     while(r >= 0 && !cellsHit[r]) {
      r = moveRight(r);
      rightCount++;
     }

     int l = guessLocation;
     int leftCount = -1;
     while(l >= 0 && !cellsHit[l]) {
      l = moveLeft(l);
      leftCount++;
     }

     if((upCount + downCount + 1) >= minClearSpace || (rightCount + leftCount + 1) >= minClearSpace) {
      isClear = true;
     }
    }
   }
  } else {

   //If nonrandom guess (locked onto a particular ship that has been hit but not killed), determine where to guess.

   int attempts = 0;

   while(!isClear) {
    attempts++;

    if(attempts == 1) {

     //Starting from the location of the first hit on the ship, test each direction to determine how many consecutive hits have been made in that direction.

     int u = firstHit;
     int upCount = -1;
     while(u >= 0 && cellsHit[u] && !cellsKilled[u]) {
      u = moveUp(u);
      upCount++;
     }
     up.setCell(u);
     up.setCount(upCount);
    
     int d = firstHit;
     int downCount = -1;
     while(d >= 0 && cellsHit[d] && !cellsKilled[d]) {
      d = moveDown(d);
      downCount++;
     }
     down.setCell(d);
     down.setCount(downCount);

     int r = firstHit;
     int rightCount = -1;
     while(r >= 0 && cellsHit[r] && !cellsKilled[r]) {
      r = moveRight(r);
      rightCount++;
     }
     right.setCell(r);
     right.setCount(rightCount);

     int l = firstHit;
     int leftCount = -1;
     while(l >= 0 && cellsHit[l] && !cellsKilled[l]) {
      l = moveLeft(l);
      leftCount++;
     }
     left.setCell(l);
     left.setCount(leftCount);

     //Determine which direction had the most consecutive hits and try to continue in that direction.

     DirectionCompare dc = new DirectionCompare();
     Collections.sort(directions, dc);
     guessLocation = directions.get(0).getCell();
    }

    //If first guess is not clear or is out of bounds, continue trying other directions until one is found that works.

    if(attempts == 2) {
     guessLocation = directions.get(1).getCell();
    }

    if(attempts == 3) {
     guessLocation = directions.get(2).getCell();
    }

    if(attempts == 4) {
     guessLocation = directions.get(3).getCell();
    }

    if(attempts > 4) {
     guessLocation = new Random().nextInt(cellsGuessed.length);
    }

    //Test whether the guess is valid and in an unguessed space.

    if(guessLocation >= 0) {
     if(!cellsGuessed[guessLocation]) {
      isClear = true;
     }
    }
   }
  }

  //Mark the guess on the grid.

  cellsGuessed[guessLocation] = true;
  b = buttons.get(guessLocation);
  s = b.getCellContents();
  b.setBorder(loweredBevel);
  
  if(s == null) {
   //If no ship in that cell, mark as a miss.
   text = "Other player missed. Your turn.";
   b.setBackground(Color.lightGray);
  } else {
   //Check if guess killed a ship.
   killed = s.counter();
   if(killed) {
    text = "Your " + s.getName() + " was sunk. Your turn.";
    boolean unkilledCells = false;
    for(BSButton bu : buttons) {
     //Mark killed cells.
     if(bu.getCellContents() == s) {
      bu.setBackground(darkRed);
      cellsKilled[bu.getGridLocation()] = true;
     } 
     //Mark if any cell remains that has been hit but not yet killed.  If so, lock onto that cell.
     if(cellsHit[bu.getGridLocation()] && !cellsKilled[bu.getGridLocation()]) {
      firstHit = bu.getGridLocation();
      unkilledCells = true;
     }
    }
    //If all hit cells have been killed, return to random guessing.
    if(!unkilledCells) {
     randomGuess = true;
    }
   } else {
    //If cell hit but not killed, mark cell appropriately.
    text = "Other player got a hit. Your turn.";
    b.setBackground(Color.red);
    //If previously random guessing, switch to locking onto the hit cell.
    if(randomGuess) {
     firstHit = b.getGridLocation();
     randomGuess = false;
    }
    cellsHit[guessLocation] = true;
   }    
  }

  //Mark game as not over unless all ships killed.
  for(Ship sh : allShips) {
   if(!sh.isKilled()) {
    gameOver = false;
   }
  }

  //Game over message.
  if(gameOver) {
   text = "You Lost in " + numOfGuesses + " guesses.";
   endGame = true;
  }
 }

 //Return the location of a cell one space in the given direction, or return -1 if out of bounds.

 public int moveUp(int u) {
  int dirUp = u - columns;
  if(dirUp < 0) {
   return -1;
  } else {
   return dirUp;
  }
 } 

 public int moveDown(int d) {
  int dirDown = d + columns;
  if(dirDown >= (rows*columns)) {
   return -1;
  } else {
   return dirDown;
  }
 } 

 public int moveRight(int r) {
  int dirRight = r + 1;
  if((dirRight >= (rows * columns)) || (dirRight % columns == 0)) {
   return -1;
  } else {
   return dirRight;
  }
 } 

 public int moveLeft(int l) {
  int dirLeft = l - 1;
  if((dirLeft < 0) || (l % columns == 0)) {
   return -1;
  } else {
   return dirLeft;
  }
 } 

 //Implement comparator to compare directions.

 class DirectionCompare implements Comparator<Direction> {
  public int compare(Direction one, Direction two) {
   return ((Integer) two.getCount()).compareTo((Integer) one.getCount());
  }
 }

 //Listen for mouse actions to place ships.

 class SetShipsListener implements MouseListener {

  public void mouseEntered(MouseEvent e) {
   //Highlight cell when mouse entered.
   BSButton cell = (BSButton) e.getSource();
   highlightCells(cell, 0);
  }

  public void mouseReleased(MouseEvent e) {
   //If mouse released on cell clear for ship placement, place ship and mark appropriater cells.
   BSButton cell = (BSButton) e.getSource();
   if(e.getButton() == MouseEvent.BUTTON1 && clear) {
    highlightCells(cell, 1);
    if(allShips.indexOf(shipToPlace) < (allShips.size() - 1)) {
     //If more ships still to place, switch to next ship to be placed.
     int nextShip = allShips.indexOf(shipToPlace) + 1;
     shipToPlace = allShips.get(nextShip);
     text = "Place " + shipToPlace.getName() + ". Right click to toggle horizontal/vertical.";
    } else {
     //If no more ships to place, disable cells and start gameplay.
     for(int i = 0; i < buttons.size(); i++) {
      BSButton bsb = buttons.get(i);
      bsb.removeMouseListener(listeners.get(i));
      bsb.setEnabled(false);
     }
     text = "Ready to start the game.";
     shipsPlaced = true;
    }
    clear = false;
   }

   if(e.getButton() == MouseEvent.BUTTON3) {
    //Toggle whether ship placement is vertical or horizontal.
    vertical = !vertical;
    for(BSButton bsb : buttons) {
     if(bsb.getCellContents() == null) {
      bsb.setBorder(defaultBorder);
     }
    }
    highlightCells(cell, 0);
   }
  }

  public void mouseExited(MouseEvent e) {
   //Unhighlight cells when mouse exited.
   BSButton cell = (BSButton) e.getSource();
   highlightCells(cell, 2);
  }

  public void mouseClicked(MouseEvent e) {}

  public void mousePressed(MouseEvent e) {}
 }

 public void highlightCells(BSButton b, int x) {
  BSButton cell = b;
  int actionToTake = x;
  clear = true;

  //Check whether sufficient spaces are clear to place the ship.
  if(vertical) {
   for(int i = 0; i < shipToPlace.getLength(); i++) {
    int testing = cell.getGridLocation() + (i * columns);
    if(testing > (rows * columns) || buttons.get(testing).getCellContents() != null) {
     clear = false;
    }
   }
  } else {

   for(int i = 0; i < shipToPlace.getLength(); i++) {
    int testing = cell.getGridLocation() + i;
    if((i > 0 && (testing % columns) == 0) || buttons.get(testing).getCellContents() != null) {
     clear = false;
    }
   }
  }

  if(clear) {

   if(vertical) {

    for(int i = 0; i < shipToPlace.getLength(); i++) {
     BSButton bsb = buttons.get(cell.getGridLocation() + (i * columns));
     //If mouse entered, highlight cells via lowered bevel.
     if(actionToTake == 0) {
      bsb.setBorder(loweredBevel);
     } else {
      //If mouse released, place ship and color ship cells.
      if(actionToTake == 1) {
       bsb.setCellContents(shipToPlace);
       bsb.setBackground(Color.blue);
       bsb.setBorder(loweredBevel);
      } else {
       //If mouse exited, unhighlight cells.
       bsb.setBorder(defaultBorder);
      }
     }
    }
   } else {

    for(int i = 0; i < shipToPlace.getLength(); i++) {
     BSButton bsb = buttons.get(cell.getGridLocation() + i);
     if(actionToTake == 0) {
      //If mouse entered, highlight cells via lowered bevel.
      bsb.setBorder(loweredBevel);
     } else {
      //If mouse released, place ship and color ship cells.
      if(actionToTake == 1) {
       bsb.setCellContents(shipToPlace);
       bsb.setBackground(Color.blue);
       bsb.setBorder(loweredBevel);
      } else {
       //If mouse exited, unhighlight cells.
       bsb.setBorder(defaultBorder);
      }
     }
    }
   }
  }
 }

 public boolean areShipsPlaced() {
  return shipsPlaced;
 }

 public String getText() {
  return text;
 }
}
