package oracle.cep.test.csfb;
import java.util.Arrays;
import java.util.Random;
import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;


/**
 * This is a test data generator for the CSFB test
 * <p>
 *
 * The arguments to main are as follows -
 * <ol>
 * <li> The number of trade requests to be generated
 * <li> Number of trades per second
 * <li> Acceptable delay limit in seconds
 * <li> Name of the file to hold TradeInputs data
 * <li> Name of the File to hold TradeUpdates data
 * <li> Name of the File to hold TradeMatched data
 * <li> Name of the file to hold Req1 output
 * <li> Name of the file to hold Req2 output
 * <li> Name of the file to hold Req3 output
 * <li> Name of the file to hold Req4 output
 * </ol>
 
 Requirements

   1. Every 5 minutes send one element with following counts -
         1. Total number of trades in last 5 mins -- 
               total number of trades that had cutoff in last 5 mins
         2. Total number of failed trades in last 5 mins
               1. BUY among these
               2. SELL among these
               3. EXCHG among these
               4. In TRADE_NOACK status among these
               5. In TRADE_RECVD status among these
               6. In TRADE_PROCESSING status among these
         3. To report total failures (BUY+SELL+EXCHG)
   2. Every 5 minutes send one element per ticker symbol with following info
         1. Ticker Symbol
         2. Number of failed trades with that ticker symbol
   3. Send one element with total count and failed count when SLA1 violation 
      occurs -
      SLA1 detection to be done once every 5 mins. 
      SLA1 - more than 5% of trades having cutoff in last 5 minutes are failed
      i.e not executed within 20 seconds
   4. Send one element with total count and failed count when SLA3 violation 
      occurs -
      As per SLA3 a trade is considered failed iff no state change occurs
      for more than 3 seconds
      SLA3 detection to be done once every 5 mins
      SLA3 - more than 10% of trades having state change cutoff in last 
             10 minutes are failed trades

Here is the proposed schema -

_*Schema

   1. TradeInputs(int tradeId, int tradeVolume, char(4) tickerSymbol,
      int tradeType)
         1. tradeType is an integer enumeration constant for BUY, SELL,
            EXCHG such as 0,1,2
   2. TradeUpdates(int tradeId, int status)
         1. status is an integer enumeration constant for 
            TRADE_NOACK, TRADE_RECVD, TRADE_PROCESSING, TRADE_EXECUTED
            such as 0,1,2,3
         2. TRADE_EXECUTED will not come on this stream, it is implied
            by arrival of element on TradeMatched stream
   3. TradeMatched(int tradeId)


*******************************/


public class CSFBGenTest {

  private static int     numTrades;
  private static double  tradeInterval;
  private static String  inpFileName;
  private static String  updFileName;
  private static String  matFileName;
  private static String  req1FileName;
  private static String  req2FileName;
  private static String  req3FileName;
  private static String  req4FileName;
  
  private static ArrayList<UpdData> updData;
  private static UpdData[]          updDataArr;
  private static ArrayList<UpdData> sla3List;
  private static ArrayList<UpdData> sla1List;
  private static UpdData[]          sla3Arr;
  private static UpdData[]          sla1Arr;
  

  private static String[] symbols = {
    "ORCL", "MSFT", "GOOG", "YHOO"
  };
  private static int numSym = symbols.length;


  private static String[] tradeTypes = {
    "BUY", "SELL", "EXCHG"
  };
  private static int numTypes = tradeTypes.length;

  private static String[] states = {
    "TRADE_NOACK",
    "TRADE_RECVD",
    "TRADE_PROCESSING",
    "TRADE_EXECUTED"
  };
  private static int numStates = states.length;


  private static class UpdData implements Comparable<UpdData> {
    long ts;
    int tradeId;
    int status;

    UpdData(long ts, int tradeId, int status) {
      this.ts      = ts;
      this.tradeId = tradeId;
      this.status  = status;
    }
    
    UpdData(UpdData d) {
      this.ts = d.getTs();
      this.tradeId = d.getTradeId();
      this.status = d.getStatus();
    }
    
    public long getTs() {
      return this.ts;
    }
    
    public int getTradeId() {
      return this.tradeId;
    }
    
    public int getStatus() {
      return this.status;
    }

    public int compareTo(UpdData u2) {
      if (ts < u2.ts)
        return -1;
      else if (ts == u2.ts)
        return 0;
      else
        return 1;
    }
  }

  public static void main(String[] args) throws Exception {

    int accDelay;
    int j = 0;

    if (args.length != 10) {
      usage();
      return;
    }

    numTrades     = Integer.parseInt(args[j++]);
    tradeInterval = ((double)1000)/Integer.parseInt(args[j++]);
    accDelay      = Integer.parseInt(args[j++]);
    inpFileName   = args[j++];
    updFileName   = args[j++];
    matFileName   = args[j++];
    req1FileName  = args[j++];
    req2FileName  = args[j++];
    req3FileName  = args[j++];
    req4FileName  = args[j++];
    
//    int length = (int) ((float) numTrades / (float) tradeInterval);
//    if (length < )

    assert numTrades > 1 : numTrades;
    assert tradeInterval > 0 : tradeInterval;
    assert accDelay > 1 : accDelay;
    assert inpFileName != null;
    assert updFileName != null;
    assert matFileName != null;

    updData  = new ArrayList<UpdData>();
    sla3List = new ArrayList<UpdData>();
    sla1List = new ArrayList<UpdData>();
    
    Random  bern  = new Random();
    Random  delay = new Random();
    int     randDelay;
    boolean stateChange;
    double  prevTs = 0;
    long    currTs;

    for (int i=0; i<numTrades; i++) {
      prevTs = prevTs + tradeInterval;
      currTs = (long)Math.ceil(prevTs); //currTs is in msec
      for (int k=1; k<numStates; k++) {
        stateChange = bern.nextInt(2) == 0;
        if (!stateChange)
          break;

        randDelay  = (1 + delay.nextInt(accDelay)) * 1000; //express delay in msec
        currTs     = currTs + randDelay;
        updData.add(new UpdData(currTs, i+1, k));
      }
    }

    updDataArr = updData.toArray(new UpdData[0]);
    Arrays.sort(updDataArr);

    populateInputFile();
    populateUpdMatFile();
  }

  public static void populateInputFile() throws Exception {
    
    File        f    = new File(inpFileName);
    File        r1   = new File(req1FileName);
    File        r2   = new File(req2FileName);
    File        r3   = new File(req3FileName);
    File        r4   = new File(req4FileName);
    PrintWriter pw   = new PrintWriter(f);
    PrintWriter pwr1 = new PrintWriter(r1);
    PrintWriter pwr2 = new PrintWriter(r2);
    PrintWriter pwr3 = new PrintWriter(r3);
    PrintWriter pwr4 = new PrintWriter(r4);
    
    int totaltrades  = 0;
    int sla1Failed   = 0;
    int countBuy     = 0;
    int countSell    = 0;
    int countExchg   = 0;
    int countNoAck   = 0;
    int countRecvd   = 0;
    int countProc    = 0;
    int fivemincount = 1;
    int[] symCount   = new int[numSym];
    UpdData cur;
    int t;
    int s;
    int m;
    int type;
    int sym;
    
    Random      rtt = new Random();
    
    ArrayList<Integer> typeList = new ArrayList<Integer>();
    ArrayList<Integer> symList  = new ArrayList<Integer>();
    Integer[] typeArr;
    Integer[] symArr;
    long ts;
    double temp = 0;
    pw.println("i, i, c 4, i");

    for (int i=0; i<numTrades; i++) {
      temp = temp + tradeInterval;
      ts = (long)Math.ceil(temp);
      sla3List.add(new UpdData(ts, i+1, 0));
      sla1List.add(new UpdData(ts, i+1, 0));
      sym = rtt.nextInt(numSym);
      type = rtt.nextInt(numTypes);
      
      typeList.add(new Integer(type));
      symList.add(new Integer(sym));
      
      pw.println(ts + " " + (i+1) + ", " + (i+1)*100 + ", " + 
                 symbols[sym] + ", " + type);
    }
    ts = 2*updDataArr[updData.size()-1].ts;
    if (ts < 300000)
    {
      ts = 600000;
    }
    pw.println("h " + ts);
    pw.close();

   for(int j=0; j<updDataArr.length; j++) {
      sla3List.add(updDataArr[j]);
   }    

    sla1List.add(new UpdData(ts, -1, 0));

    sla1Arr = sla1List.toArray(new UpdData[0]);
    
    typeArr = typeList.toArray(new Integer[0]);
    symArr = symList.toArray(new Integer[0]);
   
    pwr1.println("1:+ 0,0,0,0,0,0,0,0");
    long curTs = 0; 
    for(int i=0; i<sla1Arr.length; i++) {
      curTs = sla1Arr[i].getTs();
      cur   = new UpdData(sla1Arr[i]);
      //System.out.println(curTs + ":" + cur.getTradeId()+","+cur.getStatus());
      if((curTs + 20000) > fivemincount*300*1000) {
        sla1Failed = countBuy + countSell + countExchg;
        pwr1.print(fivemincount*300*1000L + ":+ " + totaltrades);
        pwr1.print("," + sla1Failed);
        pwr1.print("," + countBuy + "," + countSell + "," +countExchg);
        pwr1.println("," + countNoAck + "," + countRecvd + "," + countProc);
        
        for(int l=0; l<numSym; l++) {
          if(symCount[l]!=0) {
            pwr2.print(fivemincount*300*1000L + ":+ ");
            pwr2.println(symbols[l] + "," + symCount[l]);
          }
        }
        
        if(sla1Failed > (int)(0.05*totaltrades)) {
          pwr3.print(fivemincount*300*1000L + ":+ ");
          pwr3.println(totaltrades + "," + sla1Failed);
        }
        //calculate for sla1
        for(int k=0; k<numSym; k++) {
          symCount[k]=0;
        }
        
        fivemincount = fivemincount + 1;
        totaltrades = 0; countBuy = 0; countSell = 0; countExchg = 0;
        countNoAck = 0; countRecvd = 0; countProc = 0;
      }
      
      if (sla1Arr[i].getTradeId() < 0)
      {
        continue;
      }
      
      totaltrades = totaltrades + 1;
      
      UpdData exec = null; 
      for(int j=0; j<updDataArr.length; j++) {
        if(updDataArr[j].ts > curTs + 20000) {
          break;
        }
        else {
          if(updDataArr[j].getTradeId() == sla1Arr[i].getTradeId()) {
            if(updDataArr[j].getStatus() == 3) {
              exec = updDataArr[j];
              break;
            }else
              cur = updDataArr[j];
          }
        }
      }
      if (exec == null ||
         (exec.getTs() - curTs) > 20000)
      {
          s = cur.getStatus();
          if(s == 0)
            countNoAck = countNoAck + 1;
          else if(s == 1)
            countRecvd = countRecvd + 1;
          else if(s == 2)
            countProc = countProc + 1;
          t = typeArr[i].intValue();
          if(t == 0)
            countBuy = countBuy + 1;
          else if(t == 1)
            countSell = countSell + 1;
          else
            countExchg = countExchg + 1;
          m = symArr[i].intValue();
          symCount[m] = symCount[m] + 1;
      }
    }
    //pwr1.println(fivemincount*300*1000L + ":+ 0,0,0,0,0,0,0,0");

    sla3Arr = sla3List.toArray(new UpdData[0]);
    Arrays.sort(sla3Arr);
    
    fivemincount = 1;
    int index=-2;
    int sla3Total = 0;
    int sla3Failed = 0;
    while(true) { // every run in this whileloop is for every five min.
      if((index = getsla3Index(fivemincount)) == -1)
        break;
      sla3Total = 0;
      sla3Failed = 0;
      for(int i=index; (i<sla3Arr.length); i++){
        if((sla3Arr[i].getTs() + 3000) > (fivemincount*300*1000))
          break;
        if(sla3Arr[i].getStatus() == 3) { //if trade is in executed state, no status change needed.
          continue;
        }
        sla3Total =  sla3Total + 1;
        
        int pos = -1;
        for(int j = i+1; j < sla3Arr.length;j++) {
          if (sla3Arr[j].getTradeId() != sla3Arr[i].getTradeId())
            continue;
          pos = j;
          break;
        }
        if (pos == -1 ||
          (sla3Arr[pos].getTs() - sla3Arr[i].getTs()) > 3000) {
            sla3Failed = sla3Failed + 1;
          //System.out.println("*" + (sla3Arr[i].getTs() + 3000) + "," + sla3Arr[i].getTradeId());
          //System.out.println(pos == -1 ? "null" : (sla3Arr[pos].getTs() + 3000) + "," + sla3Arr[pos].getTradeId());
        } else {
          //System.out.println(" " + (sla3Arr[i].getTs() + 3000) + "," + sla3Arr[i].getTradeId());
          //System.out.println(pos == -1 ? "null" : (sla3Arr[pos].getTs() + 3000) + "," + sla3Arr[pos].getTradeId());
        }
      }
      if (sla3Failed > (int) (0.1 * sla3Total)) {
        //System.out.println("**************************************************************");
        //System.out.println(fivemincount * 300 * 1000 + ":+ " + sla3Total + "," + sla3Failed);
        pwr4.print(fivemincount * 300 * 1000 + ":+ ");
        pwr4.println(sla3Total + "," + sla3Failed);
      }
      fivemincount++;
    }
    
    pwr1.close();
    pwr2.close();
    pwr3.close();
    pwr4.close();
  }

  private static int getsla3Index(int fivemincount) {
    for(int i=0;i<sla3Arr.length;i++){
      if((sla3Arr[i].getTs() + 3000) > (fivemincount-2)*300*1000) {
        return i;
      }
    }
    return -1;
  }
  
  public static void populateUpdMatFile() throws Exception {
    
    File        fu  = new File(updFileName);
    PrintWriter pwu = new PrintWriter(fu);
    File        fm  = new File(matFileName);
    PrintWriter pwm = new PrintWriter(fm);

    pwu.println("i, i");
    pwm.println("i");

    int     limit = updData.size();
    UpdData tu;

    for (int i=0; i<limit; i++) {
      tu = updDataArr[i];
      if (tu.status != numStates-1)
        pwu.println(tu.ts + " " + tu.tradeId + ", " + tu.status);
      else
        pwm.println(tu.ts + " " + tu.tradeId);
    }
    pwu.println("h " + 2*updDataArr[limit-1].ts);
    pwm.println("h " + 2*updDataArr[limit-1].ts);
    pwu.close();
    pwm.close();
  }

  private static void usage() {

    System.out.println("");
    System.out.println("The arguments to main are as follows - ");
    System.out.println("");
    System.out.println("0 - The number of trade requests to be generated");
    System.out.println("1 - Number of Trades per second");
    System.out.println("2 - Acceptable delay limit in seconds");
    System.out.println("3 - Name of the file to hold TradeInputs data");
    System.out.println("4 - Name of the File to hold TradeUpdates data");
    System.out.println("5 - Name of the File to hold TradeMatched data");
    System.out.println("6 - Name of the file to hold Req1 output");
    System.out.println("7 - Name of the file to hold Req2 output");
    System.out.println("8 - Name of the file to hold Req3 output");
    System.out.println("9 - Name of the file to hold Req4 output");
  }
}
