package oracle.cep.test.linearroad;

public class CarData
{
    int time;
    int speed; 
    int xway; 
    int lane;
    int dir; 
    int pos;
    
    CarData()
    {
      this.time = 0;
      this.speed = 0;
      this.xway = 0;
      this.lane = 0;
      this.dir = 0;
      this.pos = 0;
    }

    CarData(int time, int speed, int xway, int lane, int dir, int pos)
    {
      this.time  = time;
      this.speed = speed;
      this.xway  = xway;
      this.lane  = lane;
      this.dir   = dir;
      this.pos   = pos;
    }

    int getTime() { return time; }
    void setTime(int time) { this.time = time; }

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
  }
