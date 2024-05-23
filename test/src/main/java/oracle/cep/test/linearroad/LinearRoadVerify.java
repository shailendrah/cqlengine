package oracle.cep.test.linearroad;
/* $Header: LinearRoadVerify.java 15-jul-2007.09:27:04 anasrini Exp $ */

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
 *  @version $Header: LinearRoadVerify.java 15-jul-2007.09:27:04 anasrini Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

/********* Requirements

   1. Output a toll when a car emits a signal. The toll is based on the
      volume of cars in that segment, and the average speed.

**********/

import java.util.*;
import java.io.*;


public class LinearRoadVerify
{
  public static final int EXIT_LANE = 4;
  public static final int EXPECTED_GAP = 30000;

  public static final int MAX_SEGMENTS = 100;
  public static final int MAX_DIR      = 1;
  public static final int MAX_XWAY     = 10;  

  public static final int AVG_SPEED_TIME = 5 * 60 * 1000; // 5 minutes  

  static int minCarId;
  static int maxCarId;
  static int curTime;
  static int numCars;
  static BitSet carIds;
  static CarEntry nxt;
  static PrintWriter pw;
  static StreamTokenizer tokenizer;
  static CarData[] cars;
  static SegData[][][] segData;
  static CarEntry[] currentCars;
  static int numEntries;
  static int nullEntries;
  static int firstEmptyEntry;
  static int maxNumEntries;
  static int volLast;
  static boolean newTime;

  static
  {
    minCarId = 0;
    maxCarId = 0;
    numCars = 0;
    nullEntries = 0;
    firstEmptyEntry = 0;
    curTime = 0;
    volLast = 0;
    newTime = false;
    carIds = new BitSet();
    segData = new SegData[MAX_XWAY+1][MAX_DIR+1][MAX_SEGMENTS+1];
    for (int i = 0; i <= MAX_XWAY; i++)
      for (int j = 0; j <= MAX_DIR; j++)
	for (int k = 0; k <= MAX_SEGMENTS; k++)
	  segData[i][j][k] = new SegData();

    numEntries = 1000;
    maxNumEntries = 1000;
    currentCars = new CarEntry[numEntries];
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

/**********************
    
    // not the efficient ways of proceeding, but will work anyway
    reader = new BufferedReader(new FileReader(args[0]));
    tokenizer = new StreamTokenizer(reader);   
    tokenizer.parseNumbers();
    tokenizer.eolIsSignificant(true);
    tokenizer.slashStarComments(true);

    while ((t = tokenizer.nextToken()) != StreamTokenizer.TT_EOL);

    while (getNextTuple())
      processTuple2();

    validate();

    reader.close();

***************/

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

  private static void processTuple3() throws Exception
  {
    // find all entries 35 seconds old, and reduce the volume
    if ((volLast == 0) || (nxt.getTime() > volLast))
    {
      if (volLast != 0) newTime = true;
      for (int i = numEntries-1; i >= 0; i--)
      {
	CarEntry curr = currentCars[i];
	if (curr == null) continue;
	if (curr.getTime() < (nxt.getTime() - 1000))
	  break;
	else if (curr.getTime() == (nxt.getTime() - 1000))
	{
	  SegData seg1 = segData[curr.getXway()][curr.getDir()][curr.getPos()/5280];
	  if ((seg1.getAvgSpeed() >= 50) || (seg1.getAvgVol() <= 10))
	    seg1.setToll(0);
	  else
	  {
	    int toll = 2 * (seg1.getAvgVol()-10) * (seg1.getAvgVol()-10);
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
	  segData[curr.getXway()][curr.getDir()][curr.getPos()/5280].decrVol();
	else if ((curr.getTime() == (nxt.getTime() - 30000)) &&
		 (curr.getCarId() == nxt.getCarId()))
	{
	  assert curr.getIsDecrVol() == false;
	  segData[curr.getXway()][curr.getDir()][curr.getPos()/5280].decrVol();
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
	  segData[curr.getXway()][curr.getDir()][curr.getPos()/5280].decrVol();
	  curr.setIsDecrVol(true);
	}
      }
      newTime = false;
    }

    segData[nxt.getXway()][nxt.getDir()][nxt.getPos()/5280].incrVol();
    segData[nxt.getXway()][nxt.getDir()][nxt.getPos()/5280].addCarAvg(nxt.getSpeed());
    
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
      int  dir   = tmp.getDir();
      int  pos   = tmp.getPos()/5280;
      int  speed = tmp.getSpeed();

      segData[xway][dir][pos].removeCarAvg(speed);
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

    // calculate toll for the entry that just came in
    /**********************

    SegData seg = segData[nxt.getXway()][nxt.getDir()][nxt.getPos()/5280];
    if ((seg.getAvgSpeed() >= 50) || (seg.getAvgVol() <= 10))
      seg.setToll(0);
    else
    {
      int toll = 2 * (seg.getAvgVol()-10) * (seg.getAvgVol()-10);
      seg.setToll(toll);
    }


    if (newTime == true)
    {
      for (int i = numEntries-1; i >= 0; i--)
      {
	CarEntry curr = currentCars[i];
	if (curr == null) continue;
	if (curr.getTime() < (nxt.getTime() - 1000))
	  break;
	else if (curr.getTime() == (nxt.getTime() - 1000))
	{
	  SegData seg1 = segData[curr.getXway()][curr.getDir()][curr.getPos()/5280];
	  if ((seg1.getAvgSpeed() >= 50) || (seg1.getAvgVol() <= 10))
	    seg1.setToll(0);
	  else
	  {
	    int toll = 2 * (seg1.getAvgVol()-10) * (seg1.getAvgVol()-10);
	    seg1.setToll(toll);
	  }
	  
	  if (seg1.getToll() != 0)
	    System.out.println("Toll " + curr.getTime() + " " + curr.getCarId() + " " + seg1.getToll());
	}
      }
    }    
    ***************/

    //    System.out.println("Ts: " + nxt.getTime() + "debug: " + segData[0][0][4].getAvgVol());

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



