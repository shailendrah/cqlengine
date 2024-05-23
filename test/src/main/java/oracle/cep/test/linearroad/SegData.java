package oracle.cep.test.linearroad;

public class SegData
{
    // Note that avgSpeed and avgVol are for different time intervals, therefore
    // total is also internally maintained for the average speed.
    int sumSpeeds;
    int total;
    int avgVol; 
    int toll;
    int countAcc;

    SegData()
    {
      sumSpeeds = 0;
      total = 0;
      avgVol = 0; 
      toll = 0;
      countAcc = 0;
    }

    int getCountAcc() { return countAcc; }
    int incrCountAcc() { return countAcc++; }
    int decrCountAcc() { return --countAcc; }

    int getToll() { return toll; }
    void setToll(int toll) { this.toll = toll; }
    
    float getAvgSpeed() { return sumSpeeds/total; }
    void addCarAvg(int speed) 
    { 
      sumSpeeds += speed;
      total++;
    }

    void removeCarAvg(int speed) 
    { 
      sumSpeeds -= speed;
      total--;
    }

    int getAvgVol() { return avgVol; }
    void incrVol() { avgVol++; }
    void decrVol() { avgVol--; }
  }