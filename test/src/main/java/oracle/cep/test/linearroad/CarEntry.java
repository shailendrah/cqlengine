package oracle.cep.test.linearroad;

public 
class CarEntry
{
  int time; 
  int carId; 
  int speed; 
  int xway; 
  int lane; 
  int dir; 
  int pos;
  boolean isDecrVol;

  CarEntry()
  {
    isDecrVol = false;
  }

  CarEntry(int time, int carId, int speed, int xway, int lane, int dir, int pos)
  {
    this.time  = time; 
    this.carId = carId; 
    this.speed = speed; 
    this.xway  = xway; 
    this.lane  = lane; 
    this.dir   = dir; 
    this.pos   = pos;
    isDecrVol  = false;
  }

  void setIsDecrVol(boolean isDecrVol) { this.isDecrVol = isDecrVol; }
  boolean getIsDecrVol() { return isDecrVol; }

  int getTime() { return time; }
  void setTime(int time) { this.time = time; }

  int getCarId() { return carId; }
  void setCarId(int carId) { this.carId = carId; }

  int getSpeed() { return speed; }
  void setSpeed(int speed) { this.speed = speed; }

  int getXway() { return xway; }
  void setXway(int xway) { this.xway = xway; }

  int getLane() { return lane; }
  void setLane(int lane) { this.lane = lane; }

  int getDir() { return dir; }
  void setDir(int dir) { this.dir = dir; }

  int getPos() { return pos; }
  void setPos(int pos) { this.pos = pos; }
};
