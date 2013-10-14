//Class representing the ships in the BShip game.

class Ship {

 String name;
 int length;
 int numOfHits = 0;
 boolean kill = false;

 public Ship(int l, String n) { 
  length = l;
  name = n;
 }

 public String getName() {
  return name;
 }

 public int getLength() {
  return length;
 }

 public boolean isKilled() {
  return kill;
 }

 public boolean counter() {
  //Count hits until number of hits equals ship length, then mark as killed.
  numOfHits++;
  if(numOfHits == length) {
   kill = true;
  }
  return kill;
 } 
}
