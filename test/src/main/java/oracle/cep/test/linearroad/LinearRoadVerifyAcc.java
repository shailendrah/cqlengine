package oracle.cep.test.linearroad;
/* $Header: LinearRoadVerifyAcc.java 15-jul-2007.09:27:05 anasrini Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    07/15/07 - 
    najain      06/25/07 - Creation
 */

/**
 *  @version $Header: LinearRoadVerifyAcc.java 15-jul-2007.09:27:05 anasrini Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

/********* Requirements

   1. Output a toll when a car emits a signal. The toll is based on the
      volume of cars in that segment, and the average speed.

**********/

import java.util.*;
import java.io.*;

public class LinearRoadVerifyAcc
{
  public static final int EXIT_LANE = 4;
  public static final int EXPECTED_GAP = 30000;

  public static final int MAX_SEGMENTS = 100;
  public static final int MAX_DIR      = 1;
  public static final int MAX_LANES    = 4;  
  public static final int MAX_XWAY     = 10;  

  public static final int AVG_SPEED_TIME = 5 * 60 * 1000; // 5 minutes  
  public static final int ACC_AFFECTED_TIME = 20 * 60 * 1000; // 20 minutes  

  static int minCarId;
  static int maxCarId;
  static int curTime;
  static int numCars;
  static BitSet carIds;
  static CarEntry nxt;
  static PrintWriter pw;
  static StreamTokenizer tokenizer;
  static CarData[] cars;
  static SegData[][][][] segData;
  static int[][][][] accAffSeg;
  static ArrayList<CarData> clearAccidents;
  static CarEntry[] currentCars;
  static int numEntries;
  static int nullEntries;
  static int firstEmptyEntry;
  static int maxNumEntries;
  static int volLast;

  static
  {
    minCarId = 0;
    maxCarId = 0;
    numCars = 0;
    nullEntries = 0;
    firstEmptyEntry = 0;
    curTime = 0;
    volLast = 0;
    carIds = new BitSet();

    segData = new SegData[MAX_XWAY+1][MAX_LANES+1][MAX_DIR+1][MAX_SEGMENTS+1];
    for (int i = 0; i <= MAX_XWAY; i++)
      for (int j = 0; j <= MAX_LANES; j++)
	for (int k = 0; k <= MAX_DIR; k++)
	  for (int l = 0; l <= MAX_SEGMENTS; l++)
	    segData[i][j][k][l] = new SegData();

    accAffSeg = new int[MAX_XWAY+1][MAX_LANES+1][MAX_DIR+1][MAX_SEGMENTS+1];
    for (int i = 0; i <= MAX_XWAY; i++)
      for (int j = 0; j <= MAX_LANES; j++)
	for (int k = 0; k <= MAX_DIR; k++)
	  for (int l = 0; l <= MAX_SEGMENTS; l++)
	    accAffSeg[i][j][k][l] = 0;

    numEntries = 1000;
    maxNumEntries = 1000;
    currentCars = new CarEntry[numEntries];
    clearAccidents = new ArrayList<CarData>();
  }

  public static void main(String[] args) throws Exception
  {
    BufferedReader reader = new BufferedReader(new FileReader(args[0]));
    pw = new PrintWriter(new File(args[1]));

    tokenizer = new StreamTokenizer(reader);   

    nxt = new CarEntry();

    tokenizer.parseNumbers();
    tokenizer.eolIsSignificant(true);
    tokenizer.slashStarComments(true);

    // skip the schema for now
    int t;
    while ((t = tokenizer.nextToken()) != StreamTokenizer.TT_EOL);

    while (getNextTuple())
      processTuple();

    dump();
    
    reader.close();

    cars = new CarData[maxCarId + 1];

    // not the efficient ways of proceeding, but will work anyway
    reader = new BufferedReader(new FileReader(args[0]));
    tokenizer = new StreamTokenizer(reader);   
    tokenizer.parseNumbers();
    tokenizer.eolIsSignificant(true);
    tokenizer.slashStarComments(true);

    while ((t = tokenizer.nextToken()) != StreamTokenizer.TT_EOL);

    while (getNextTuple())
      processTuple3();
    pw.close();

    System.out.println("Max num entries : " + maxNumEntries);
  }

  private static void dump()
  {
    System.out.println("Minimum car id: " + minCarId);
    System.out.println("Maximum car id: " + maxCarId);

    numCars = 0;
    for (int i = carIds.nextSetBit(0); i >= 0; i = carIds.nextSetBit(i+1))
      numCars++;

    System.out.println("Number of cars: " + numCars);
  }

  private static boolean getNextTuple() throws Exception
  {
    // Get the next token
    int t = tokenizer.nextToken();

    // Remember if we reached EOF
    if (t == StreamTokenizer.TT_EOF) return false;

    if (t != StreamTokenizer.TT_NUMBER)
      return false;
    nxt.setTime((int)tokenizer.nval);
    curTime = nxt.getTime();

    t = tokenizer.nextToken();
    assert (t == StreamTokenizer.TT_NUMBER);
    nxt.setCarId((int)tokenizer.nval);

    t = tokenizer.nextToken();
    assert (t == ',');

    t = tokenizer.nextToken();
    assert (t == StreamTokenizer.TT_NUMBER);
    nxt.setSpeed((int)tokenizer.nval);

    t = tokenizer.nextToken();
    assert (t == ',');

    t = tokenizer.nextToken();
    assert (t == StreamTokenizer.TT_NUMBER);
    nxt.setXway((int)tokenizer.nval);

    t = tokenizer.nextToken();
    assert (t == ',');

    t = tokenizer.nextToken();
    assert (t == StreamTokenizer.TT_NUMBER);
    nxt.setLane((int)tokenizer.nval);

    t = tokenizer.nextToken();
    assert (t == ',');

    t = tokenizer.nextToken();
    assert (t == StreamTokenizer.TT_NUMBER);
    nxt.setDir((int)tokenizer.nval);

    t = tokenizer.nextToken();
    assert (t == ',');

    t = tokenizer.nextToken();
    assert (t == StreamTokenizer.TT_NUMBER);
    nxt.setPos((int)tokenizer.nval);

    t = tokenizer.nextToken();
    assert (t == StreamTokenizer.TT_EOL);
    
    return true;
  }

  private static int max(int a, int b)
  {
    if (a > b) return a;
    return b;
  }

  private static void accidentDetected(CarEntry e) throws Exception
  {
    int segNo = e.getPos()/5280;
    if (e.getDir() == 0)
      for (int i = max(segNo - 10 + 1, 0); i <= segNo; i++)
	accAffSeg[e.getXway()][e.getLane()][e.getDir()][i]++;
    else
      for (int i = max(segNo + 10 - 1, MAX_SEGMENTS); i >= segNo; i--)
	accAffSeg[e.getXway()][e.getLane()][e.getDir()][i]++;
  }

  private static void accidentCleared(CarEntry e) throws Exception
  {
    int delTime = e.getTime() + ACC_AFFECTED_TIME;
    CarData tmp = new CarData(delTime, 0, e.getXway(), e.getLane(), e.getDir(), e.getPos());
    clearAccidents.add(tmp);
  }

  private static void processTuple3() throws Exception
  {
    // clear accidents
    if (nxt.getTime() > volLast)
    {
      while (true)
      {
	if (clearAccidents.size() == 0) break;
	CarData e = clearAccidents.get(0);
	if ((e == null) || (e.getTime() > nxt.getTime())) break;
	assert e.getTime() == nxt.getTime();

	int segNo = e.getPos()/5280;
	if (e.getDir() == 0)
	  for (int i = max(segNo - 10 + 1, 0); i <= segNo; i++)
	    accAffSeg[e.getXway()][e.getLane()][e.getDir()][i]--;
	else
	  for (int i = max(segNo + 10 - 1, MAX_SEGMENTS); i >= segNo; i--)
	    accAffSeg[e.getXway()][e.getLane()][e.getDir()][i]--;
      }
    }

    CarEntry e1 = null, e2 = null, e3 = null, e4 = null;
    
    // report accidents if any
    // here I have assumed that a car cannot exit and re-enter very soon
    for (int i = numEntries-1; i >= 0; i--)
    {
      CarEntry curr = currentCars[i];
      if (curr == null) continue;
      if (curr.getTime() < (nxt.getTime() - 120000)) break;

      if ((curr.getTime() == (nxt.getTime() - 30000)) &&
	  (curr.getCarId() == nxt.getCarId()))
	e4 = curr;
      else if ((curr.getTime() == (nxt.getTime() - 60000)) &&
	       (curr.getCarId() == nxt.getCarId()))
	e3 = curr;
      else if ((curr.getTime() == (nxt.getTime() - 90000)) &&
	       (curr.getCarId() == nxt.getCarId()))
	e2 = curr;
      else if ((curr.getTime() == (nxt.getTime() - 120000)) &&
	       (curr.getCarId() == nxt.getCarId()))
	e1 = curr;
    }

    if ((e4 != null) && (e3 != null) && (e2 != null))
    {
      if ((e2.getPos() == e3.getPos()) &&
	  (e3.getPos() == e4.getPos()) &&
	  (e4.getPos() == nxt.getPos()))
      {
	if ((e1 == null) || (e1.getPos() != e2.getPos()))
	{
	  if (segData[nxt.getXway()][nxt.getLane()][nxt.getDir()][nxt.getPos()/5280].incrCountAcc() == 0)
	    accidentDetected(nxt);
	}
      }
      else if ((e1 != null) &&
	       (e1.getPos() == e2.getPos()) &&
	       (e2.getPos() == e3.getPos()) &&
	       (e3.getPos() == e4.getPos()))
      {
	if (segData[nxt.getXway()][nxt.getLane()][nxt.getDir()][nxt.getPos()/5280].decrCountAcc() == 0)
	  accidentCleared(e1);
      }
    }

    // find all entries 35 seconds old, and reduce the volume
    if ((volLast == 0) || (nxt.getTime() > volLast))
    {
      // report tolls if any
      for (int i = numEntries-1; i >= 0; i--)
      {
	CarEntry curr = currentCars[i];
	if (curr == null) continue;
	if (curr.getTime() < (nxt.getTime() - 1000))
	  break;
	else if (curr.getTime() == (nxt.getTime() - 1000))
	{
	  SegData seg1 = segData[curr.getXway()][curr.getLane()][curr.getDir()][curr.getPos()/5280];
	  if (((seg1.getAvgSpeed() >= 50) || (seg1.getAvgVol() <= 50))
	      || (accAffSeg[curr.getXway()][curr.getLane()][curr.getDir()][curr.getPos()/5280] > 0))
	    seg1.setToll(0);
	  else
	  {
	    int toll = 2 * (seg1.getAvgVol()-50) * (seg1.getAvgVol()-50);
	    seg1.setToll(toll);
	    pw.println(curr.getTime() + ":+ " + curr.getCarId() + "," + seg1.getToll());
	  }
	}
      }

      for (int i = numEntries-1; i >= 0; i--)
      {
	CarEntry curr = currentCars[i];
	if (curr == null) continue;
	if (curr.getTime() < (nxt.getTime() - 35000))
	  break;
	else if ((curr.getTime() == (nxt.getTime() - 35000)) &&
		 (curr.getIsDecrVol() == false))
	  segData[curr.getXway()][curr.getLane()][curr.getDir()][curr.getPos()/5280].decrVol();
	else if ((curr.getTime() == (nxt.getTime() - 30000)) &&
		 (curr.getCarId() == nxt.getCarId()))
	{
	  assert curr.getIsDecrVol() == false;
	  segData[curr.getXway()][curr.getLane()][curr.getDir()][curr.getPos()/5280].decrVol();
	  curr.setIsDecrVol(true);
	}
      }
      volLast = nxt.getTime();
    }
    else
    {
      for (int i = numEntries-1; i >= 0; i--)
      {
	CarEntry curr = currentCars[i];
	if (curr == null) continue;
	if (curr.getTime() < (nxt.getTime() - 30000))
	  break;
	else if ((curr.getTime() == (nxt.getTime() - 30000)) &&
		 (curr.getCarId() == nxt.getCarId()))
	{
	  assert curr.getIsDecrVol() == false;
	  segData[curr.getXway()][curr.getLane()][curr.getDir()][curr.getPos()/5280].decrVol();
	  curr.setIsDecrVol(true);
	}
      }
    }

    segData[nxt.getXway()][nxt.getLane()][nxt.getDir()][nxt.getPos()/5280].incrVol();
    segData[nxt.getXway()][nxt.getLane()][nxt.getDir()][nxt.getPos()/5280].addCarAvg(nxt.getSpeed());
    
    // average speed is computed over 5 minutes 
    assert firstEmptyEntry <= numEntries;

    if (firstEmptyEntry == numEntries)
    {
      CarEntry[] tmp = new CarEntry[numEntries + 1000];
      for (int i = 0; i < numEntries; i++)
	tmp[i] = currentCars[i];
      currentCars = tmp;
      numEntries += 1000;
      if (maxNumEntries < numEntries)
	maxNumEntries = numEntries;
    }

    assert currentCars[firstEmptyEntry] == null : firstEmptyEntry;
    currentCars[firstEmptyEntry] = new CarEntry(nxt.getTime(), nxt.getCarId(), nxt.getSpeed(), nxt.getXway(), nxt.getLane(), nxt.getDir(), nxt.getPos());
    firstEmptyEntry++;

    while ((currentCars[nullEntries].getTime() + AVG_SPEED_TIME) <= 
	   nxt.getTime())
    {
      CarEntry tmp = currentCars[nullEntries];
      int  xway  = tmp.getXway();
      int  lane  = tmp.getLane();
      int  dir   = tmp.getDir();
      int  pos   = tmp.getPos()/5280;
      int  speed = tmp.getSpeed();

      segData[xway][lane][dir][pos].removeCarAvg(speed);
      currentCars[nullEntries] = null;
      nullEntries++;
    }

    // shrink the array
    while (nullEntries > 1000)
    {
      CarEntry[] tmp = new CarEntry[numEntries - 1000];
      for (int i = 1000; i < numEntries; i++)
	tmp[i-1000] = currentCars[i];
      currentCars  = tmp;
      numEntries  -= 1000;
      nullEntries -= 1000;
      firstEmptyEntry -= 1000;
    }
  }

  private static void processTuple() throws Exception
  {
    if ((minCarId == 0) || (nxt.getCarId() < minCarId))
      minCarId = nxt.getCarId();

    if ((maxCarId == 0) || (nxt.getCarId() > maxCarId))
      maxCarId = nxt.getCarId();
      
    if (carIds.get(nxt.getCarId()) == false)
      carIds.set(nxt.getCarId());
  }

  private static void processTuple2() throws Exception
  {
    // verify that lane 4 is the exit lane
    if (cars[nxt.getCarId()] == null)
      cars[nxt.getCarId()] = new CarData();
    else
    {
      if ((cars[nxt.getCarId()].getLane() == EXIT_LANE) &&
	  (nxt.getLane() == EXIT_LANE))
      {
	System.out.println("EXIT LANE REPEAT");

	System.out.println("OLD DATA:");
	System.out.println(cars[nxt.getCarId()].getTime());
	System.out.println(cars[nxt.getCarId()].getSpeed());
	System.out.println(cars[nxt.getCarId()].getXway());
	System.out.println(cars[nxt.getCarId()].getLane());
	System.out.println(cars[nxt.getCarId()].getDir());    
	System.out.println(cars[nxt.getCarId()].getPos());    

	System.out.println("NEW DATA:");
	System.out.println(nxt.getTime());
	System.out.println(nxt.getSpeed());
	System.out.println(nxt.getXway());
	System.out.println(nxt.getLane());
	System.out.println(nxt.getDir());    
	System.out.println(nxt.getPos());    
      }
      if ((cars[nxt.getCarId()].getLane() == EXIT_LANE) &&
	  (nxt.getLane() != EXIT_LANE))
      {
	System.out.println("EXIT FOLLOWED BY RE-ENTRY");

	System.out.println("OLD DATA:");
	System.out.println(cars[nxt.getCarId()].getTime());
	System.out.println(cars[nxt.getCarId()].getSpeed());
	System.out.println(cars[nxt.getCarId()].getXway());
	System.out.println(cars[nxt.getCarId()].getLane());
	System.out.println(cars[nxt.getCarId()].getDir());    
	System.out.println(cars[nxt.getCarId()].getPos());    

	System.out.println("NEW DATA:");
	System.out.println(nxt.getTime());
	System.out.println(nxt.getSpeed());
	System.out.println(nxt.getXway());
	System.out.println(nxt.getLane());
	System.out.println(nxt.getDir());    
	System.out.println(nxt.getPos());    
      }
      else
      {
	// there is an entry every 30 seconds
	if (nxt.getTime() - cars[nxt.getCarId()].getTime() != EXPECTED_GAP)
	{
	  System.out.println("EXPECTED GAP INCORRECT");
	  
	  System.out.println("OLD DATA:");
	  System.out.println(cars[nxt.getCarId()].getTime());
	  System.out.println(cars[nxt.getCarId()].getSpeed());
	  System.out.println(cars[nxt.getCarId()].getXway());
	  System.out.println(cars[nxt.getCarId()].getLane());
	  System.out.println(cars[nxt.getCarId()].getDir());    
	  System.out.println(cars[nxt.getCarId()].getPos());    
	  
	  System.out.println("NEW DATA:");
	  System.out.println(nxt.getTime());
	  System.out.println(nxt.getSpeed());
	  System.out.println(nxt.getXway());
	  System.out.println(nxt.getLane());
	  System.out.println(nxt.getDir());    
	  System.out.println(nxt.getPos());    
	}
      }
    }

    CarData tmp = cars[nxt.getCarId()];

    tmp.setTime(nxt.getTime());
    tmp.setSpeed(nxt.getSpeed());
    tmp.setXway(nxt.getXway());
    tmp.setLane(nxt.getLane());
    tmp.setDir(nxt.getDir());    
    tmp.setPos(nxt.getPos());    
  }

  private static void validate() throws Exception
  {
    // all the entries should be either for the exit lane, or within 30 
    // seconds
    CarData car;
    int numTuples = 0;
    for (int i = 0; i < cars.length; i++)
    { 
      car = cars[i];
      if (car != null)
      {
	if ((car.getLane() != 4) && (curTime - car.getTime() > EXPECTED_GAP))
	{
	  //	  System.out.println("MISSING DATA");
	  numTuples++;

	  /****************
	  System.out.println(car.getTime());
	  System.out.println(i);
	  System.out.println(car.getSpeed());
	  System.out.println(car.getXway());
	  System.out.println(car.getLane());
	  System.out.println(car.getDir());    
	  System.out.println(car.getPos());    
	  *************************/
	}
      }
    }

    System.out.println("Number of offending tuples " + numTuples);
  }
}



