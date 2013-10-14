//Class implementing the buttons representing cells on a BShip game grid.

import javax.swing.*;

public class BSButton extends JButton {

 int gridLocation;
 Ship cellContents = null;
 boolean guessed = false;

 public void setGridLocation(int l) {
  gridLocation = l;
 }

 public int getGridLocation() {
  return gridLocation;
 }

 public void setCellContents(Ship s) {
  cellContents = s;
 }

 public Ship getCellContents() {
  return cellContents;
 }

 public void guess() {
  guessed = true;
 }

 public boolean isGuessed() {
  return guessed;
 }
}
