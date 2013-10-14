//Class implementing the directions up, down, right, and left for purposes of movement on a BShip game grid.

class Direction {

 int countHits;
 int cellLocation;

 public void setCount(int a) {
  countHits = a;
 }

 public int getCount() {
  return countHits;
 }

 public void setCell(int b) {
  cellLocation = b;
 }

 public int getCell() {
  return cellLocation;
 }
}
