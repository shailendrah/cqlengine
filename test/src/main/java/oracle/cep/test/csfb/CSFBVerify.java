package oracle.cep.test.csfb;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import oracle.cep.util.CSVUtil;

/********* Requirements

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

public class CSFBVerify
{
  //SLA1 cutoff 20 seconds
  static final int SLA1_CUTOFF = 20 * 1000; 
  static final int SLA3_CUTOFF = 3 * 1000;
  static final float SLA1_PERCENT = 0.05f;
  static final float SLA3_PERCENT = 0.1f;
  static final int FIVE_MIN = 300 * 1000;

  
  private Map<String, Integer> m_symbolMap;
  private Map<Integer, Trade> m_tradeMap;
  private List<Trade> m_trades;
  private List<Status> m_statuses;
  
  
  enum TradeType 
  {
    Buy, Sell, Exchg;
    public static TradeType fromOrdinal(int ord)
    {
      for (TradeType k : TradeType.values()) 
      {
        if (k.ordinal() == ord) return k;
      }
      assert false : "unknown tradetype " + ord;
      return Buy;
    }
  }

  enum TradeStatus
  {
    NoAck, Recvd, Processing, Executed;
    public static TradeStatus fromOrdinal(int ord)
    {
      for (TradeStatus k : TradeStatus.values()) 
      {
        if (k.ordinal() == ord) return k;
      }
      assert false : "unknown tradestatus " + ord;
      return NoAck;
    }
  }
  
  static final String  s_symbols[] = {
    "ORCL", "MSFT", "GOOG", "YHOO"};

  private static class Stat
  {
    int totalTrades;
    int failedBuy;
    int failedSell;
    int failedExchg;
    int failedNoack;
    int failedNoRecvd;
    int failedProcessing;
    int[] failedSymbol = new int[s_symbols.length];
    
    public void clear()
    {
      totalTrades = 0;
      failedBuy = 0;
      failedSell = 0;
      failedExchg = 0;
      failedNoack = 0;
      failedNoRecvd = 0;
      failedProcessing = 0;
      java.util.Arrays.fill(failedSymbol, 0);
    }
  }
  
  private static class Trade implements Comparable<Trade>
  {
    long m_ts;
    int m_tradeId;
    int m_tradeVolume;
    int m_symbolId;
    TradeType m_tradeType;
    List<Update> m_updates;
    List<Long> m_matches;
    
    public Trade()
    {
      m_updates = new LinkedList<Update>();
      m_matches = new LinkedList<Long>();
    }
    
    @SuppressWarnings("unused")
    public Trade(Trade other)
    {
      m_ts = other.m_ts;
      m_tradeId = other.m_tradeId;
      m_tradeVolume = other.m_tradeVolume;
      m_symbolId = other.m_symbolId;
      m_tradeType = other.m_tradeType;
      m_updates = other.m_updates;
      m_matches = other.m_matches;
    }

    public int compareTo(Trade ot)
    {
      if (m_ts < ot.m_ts) return -1;
      else if (m_ts == ot.m_ts) return 0;
      else return 1;
    }
    
    public String toString()
    {
      StringBuffer buf = new StringBuffer();
      buf.append(m_ts);
      if (m_tradeId >= 0) {
        buf.append(":");
        buf.append(m_tradeId);
        buf.append(",");
        buf.append(m_tradeVolume);
        buf.append(",");
        buf.append(s_symbols[m_symbolId]);
        buf.append(" ");
        for (Update u : m_updates)
        {
          buf.append(u.toString());
          buf.append(" ");
        }
      }
      return buf.toString();
    }
  }

  private static class Status implements Comparable<Status>
  {
    long m_ts;
    int m_tradeId;
    TradeStatus m_status;
    
    @SuppressWarnings("unused")
    public Status()
    {
      m_status = TradeStatus.NoAck;
    }
    
    public Status(Trade other)
    {
      m_ts = other.m_ts;
      m_tradeId = other.m_tradeId;
      m_status = TradeStatus.NoAck;
    }

    public int compareTo(Status ot)
    {
      if (m_ts < ot.m_ts) return -1;
      else if (m_ts == ot.m_ts) return 0;
      else return 1;
    }
    
    public String toString()
    {
      StringBuffer buf = new StringBuffer();
      buf.append(m_ts);
      if (m_tradeId >= 0) {
        buf.append(":");
        buf.append(m_tradeId);
        buf.append(",");
        buf.append(m_status);
      }
      return buf.toString();
    }
  }

  private static class Update
  {
    long m_ts;
    TradeStatus m_status;
    public String toString()
    {
      StringBuffer buf = new StringBuffer();
      buf.append(m_ts);
      buf.append(":");
      buf.append(m_status);
      return buf.toString();
    }
  }

  private static class Tuple
  {
    Object[] values;
    public Tuple(Object[] val) 
    {
      values = val;
    }
    
    public String toString()  
    {
      StringBuffer buf = new StringBuffer();
      int i = 0;
      for (Object v : values) 
      {
        if (i > 0) buf.append(",");
        buf.append(v.toString());
        i++;
      }
      return buf.toString();
    }
  }

  interface IProcessTuple 
  {
    void process(long ts, Tuple tuple);
  };
  
  enum ColType 
  {
    Integer,
    CharArray
  };
  
  public CSFBVerify()
  {
    m_symbolMap = new HashMap<String,Integer>();
    int sid = 0;
    for (String s : s_symbols)
    {
      m_symbolMap.put(s, sid);
      sid++;
    }
    m_tradeMap = new HashMap<Integer, Trade>();
    m_trades = new LinkedList<Trade>();
    m_statuses = new LinkedList<Status>();
  }

  private int getSymbol(String s)
  {
    Integer sid = m_symbolMap.get(s);
    assert sid != null;
    return sid.intValue();
  }
  
  private Tuple getTuple(ColType spec[], String line)
   throws Exception
  {
    List<String> vals = CSVUtil.parseStr(line);
    int pos = 0;
    Object[] tuple = new Object[spec.length];
    for (String s : vals)
    {
      if (line.startsWith("h"))
      {
        return null;
      }
    
      switch(spec[pos])
      {
        case Integer:
          tuple[pos] = Integer.parseInt(s);          
          break;
        case CharArray:
          tuple[pos] = s;
          break;
      }
      pos++;
    }
    return new Tuple(tuple);
  }
  
  private void processFile(String filename, IProcessTuple proc)
   throws Exception
  {
    BufferedReader in = new BufferedReader(new FileReader(filename));
    String str;
    int pos = 0;
    ColType[] spec = null;
    while ((str = in.readLine()) != null) {
      if (pos == 0) 
      {
        List<String> vals = CSVUtil.parseStr(str);
        spec = new ColType[vals.size()];
        int i = 0;
        for (String s : vals)
        {
          if (s.equals("i")) 
          {
            spec[i] = ColType.Integer;
          } else if (s.startsWith("c"))
          {
            spec[i] = ColType.CharArray;
          }
          i++;
        }
      } else {
        int spos = str.indexOf(' ');
        if (spos < 0) spos = str.indexOf('\t');
        assert spos > 0;
        String tsstr = str.substring(0, spos);
        String val = str.substring(spos);
        val = val.trim();
        
        long ts = 0;
        Tuple tuple = null;
        if (tsstr.startsWith("h"))
        {
          ts = Long.parseLong(val);  
        } else {
          ts = Long.parseLong(tsstr);
          tuple = getTuple(spec, val);
        }
        proc.process(ts, tuple);
      }
      pos++;
    }
    in.close();
  }
  
  class LoadTradeInputs implements IProcessTuple
  {
    public void process(long ts, Tuple tuple)
    {
      Trade trade = new Trade();
      trade.m_ts = ts;
      if (tuple == null)
      {
        trade.m_tradeId = -1;
        trade.m_tradeVolume = 0;
        trade.m_symbolId = -1;
        trade.m_tradeType = TradeType.Buy;
      } else {
        trade.m_tradeId = ((Integer) tuple.values[0]).intValue();
        trade.m_tradeVolume = ((Integer) tuple.values[1]).intValue();
        trade.m_symbolId = getSymbol((String) tuple.values[2]);
        int ttype = ((Integer) tuple.values[3]).intValue();
        trade.m_tradeType = TradeType.fromOrdinal(ttype);
      }
      //if (trade.m_symbolId != 1) return;
      m_trades.add(trade);
      if (trade.m_tradeId >= 0)
        m_tradeMap.put(trade.m_tradeId, trade);
      Status st = new Status(trade);
      st.m_ts = ts + SLA3_CUTOFF;
      m_statuses.add(st);
    }
  }
  
  class LoadTradeUpdates implements IProcessTuple
  {
    public void process(long ts, Tuple tuple)
    {
      if (tuple == null) return;
      int tradeId = ((Integer) tuple.values[0]).intValue();
      Trade trade = m_tradeMap.get(tradeId);
      if (trade == null) return;

      Update update = new Update();
      update.m_ts = ts;
      update.m_status = TradeStatus.fromOrdinal( ((Integer) tuple.values[1]).intValue() );
      trade.m_updates.add(update);
      Status st = new Status(trade);
      st.m_ts = ts + SLA3_CUTOFF;
      st.m_status = update.m_status;
      m_statuses.add(st);
    }
  }
  
  class LoadTradeMatches implements IProcessTuple
  {
    public void process(long ts, Tuple tuple)
    {
      if (tuple == null) return;
      int tradeId = ((Integer) tuple.values[0]).intValue();
      Trade trade = m_tradeMap.get(tradeId);
      if (trade == null) return;
      
      trade.m_matches.add(ts);
      Update update = new Update();
      update.m_ts = ts;
      update.m_status = TradeStatus.Executed;
      trade.m_updates.add(update);
      Status st = new Status(trade);
      st.m_ts = ts + SLA3_CUTOFF;
      st.m_status = TradeStatus.Executed;
      m_statuses.add(st);
    }
  }
  
  @SuppressWarnings("unused")
private void checkSla1Violation(long checkpoint, Trade trade, Stat stat)
  {
    long ts = trade.m_ts;
    TradeStatus s = TradeStatus.NoAck;
    Update execupd = null;
    Update lastupd = null;
    for (Update upd : trade.m_updates)
    {
      if (upd.m_ts >= checkpoint)
        break;
      lastupd = upd;
      s = upd.m_status;
      if (upd.m_status == TradeStatus.Executed) 
      {
        execupd = upd;
        break;
      }
    }
        
    if (execupd == null || 
        execupd.m_ts > (ts + SLA1_CUTOFF))
    {
       //System.out.println("*"+trade.toString() + " >>> " + ((lastupd == null) ? s.toString() : lastupd.toString()));
        switch(s)
        {
          case NoAck: stat.failedNoack++; break;
          case Recvd: stat.failedNoRecvd++; break;
          case Processing: stat.failedProcessing++; break;
        default:
            break;
        }
        switch(trade.m_tradeType)
        {
          case Buy:  stat.failedBuy++; break;
          case Sell: stat.failedSell++; break;
          case Exchg: stat.failedExchg++; break;
        }
        stat.failedSymbol[trade.m_symbolId]++;
    } else 
    {
       //System.out.println(" "+trade.toString());
    }
  }

  public void process(String tiFileName, String tuFileName, String tmFileName, String outfolder) 
    throws Exception
  {
    System.out.println(tiFileName);
    System.out.println(tuFileName);
    System.out.println(tmFileName);
    System.out.println("outfiles in " + outfolder);
    processFile(tiFileName, new LoadTradeInputs());
    processFile(tuFileName, new LoadTradeUpdates());
    processFile(tmFileName, new LoadTradeMatches());
    
    Collections.sort(m_trades);
    Collections.sort(m_statuses);
    
    PrintWriter[] outs = new PrintWriter[4];
    outs[0] = new PrintWriter(new File(outfolder + "/req1.txt"));
    outs[1] = new PrintWriter(new File(outfolder + "/req2.txt"));
    outs[2] = new PrintWriter(new File(outfolder + "/req3.txt"));
    outs[3] = new PrintWriter(new File(outfolder + "/req4.txt"));


    Stat stat = new Stat();
    long checkpoint = FIVE_MIN;
    for (Trade trade : m_trades)
    {
      long ts = trade.m_ts;
      // dump stat every five minutes
      if ( ( ts + SLA1_CUTOFF) > checkpoint )
      {
        System.out.println("\n**************************************************************");
        int sla1failed = stat.failedBuy + stat.failedSell + stat.failedExchg;
        String req1fmt = "{0}: {1}, {2}, {3}, {4}, {5}, {6}, {7}, {8}";
        String req1 = MessageFormat.format(req1fmt, 
          checkpoint,
          stat.totalTrades,
          sla1failed,
          stat.failedBuy, stat.failedSell, stat.failedExchg,
          stat.failedNoack, stat.failedNoRecvd, stat.failedProcessing);
        outs[0].println( req1 );
        System.out.println(req1);
        for (int i = 0; i < s_symbols.length; i++)
        {
          if (stat.failedSymbol[i] > 0)
          {
            String req2fmt = "{0}: {1}, {2}";
            String req2 = MessageFormat.format(req2fmt,
              checkpoint, s_symbols[i], stat.failedSymbol[i]);
            outs[2].println(req2);
            System.out.println(req2);
          }
        }
        if (sla1failed > (int) (SLA1_PERCENT * stat.totalTrades))
        {
          String req3fmt = "{0}: {1}, {2}";
          String req3 = MessageFormat.format(req3fmt,
            checkpoint, stat.totalTrades, sla1failed);
          outs[3].println(req3);
          System.out.println(req3);
        }
        
        checkpoint += FIVE_MIN;
        stat.clear();
      }

      // update stats
      if (trade.m_tradeId >= 0 ) 
      {
        checkSla1Violation(checkpoint, trade, stat);
        stat.totalTrades++;
      } else 
      {
        //System.out.println(" " + trade.toString());
      }
    }

    int sla3failed = 0;
    int sla3total= 0;
    checkpoint = FIVE_MIN;
    int laststartpos = 0;
    while(true) 
    {
      int startpos = -1;
      for (int i = laststartpos; i < m_statuses.size(); i++) 
      {
        Status trade = m_statuses.get(i);      
        long ts = trade.m_ts;
        // slide
        if ( ts  > (checkpoint - FIVE_MIN*2) )
        {
          startpos = i;
          break;
        }
      }
      if (startpos == -1) break;
      for (int j = startpos; j < m_statuses.size(); j++) 
      {
        Status trade = m_statuses.get(j);      
        long ts = trade.m_ts;
        if ( ts > checkpoint) break;
        if (trade.m_status == TradeStatus.Executed) {
          //System.out.println(" " + trade.toString());
          continue;
        }
        Status next = null;
        for (int k = (j + 1); k < m_statuses.size(); k++) 
        {
          Status ntrade = m_statuses.get(k); 
          if (ntrade.m_tradeId != trade.m_tradeId) continue;
          //System.out.println("     " + ntrade.toString());
          next = ntrade;
          break;
        }
        if (next == null || (next.m_ts - trade.m_ts) > SLA3_CUTOFF) 
        {
          //System.out.println("*" + trade.toString());
          //System.out.println(next == null ? "null" : next.toString());
          sla3failed++;
        } else 
        {
          //System.out.println(" " + trade.toString());
          //System.out.println(next == null ? "null" : next.toString());
        }
        sla3total++;
      }
      
      if (sla3failed > (int) (SLA3_PERCENT * sla3total))
      {
       System.out.println("\n**************************************************************");
        String req4fmt = "{0}: {1}, {2}";
        String req4 = MessageFormat.format(req4fmt,
          checkpoint, sla3total, sla3failed);
        outs[3].println( req4);
        System.out.println(req4);
      }
      sla3total = 0;
      sla3failed = 0;
      laststartpos = startpos;
      checkpoint += FIVE_MIN;

    }
    for (PrintWriter o : outs)
    {
      o.close();
    }
  }
  
  public static void main(String[] args)
  {
    String size = args[0];
    String rate = args[1];
    String infolder = null;
    String outfolder = null;
    if (args.length >= 3) infolder = args[2];
    if (args.length >= 4) outfolder = args[3];
    
    String twork = System.getenv("T_WORK");
    twork += "/cep";
    if (outfolder == null) outfolder = twork;

    String cep = System.getenv("ADE_VIEW_ROOT");
    cep += "/pcbpel/cep/test/data";
    if (infolder == null) infolder = cep;
    try {
      CSFBVerify v = new CSFBVerify();
      String tifilename = MessageFormat.format("{0}/inpTIDataSize{1}Rate{2}.txt", infolder, size, rate);
      String tufilename = MessageFormat.format("{0}/inpTUDataSize{1}Rate{2}.txt", infolder, size, rate);
      String tmfilename = MessageFormat.format("{0}/inpTMDataSize{1}Rate{2}.txt", infolder, size, rate);
      v.process(tifilename, tufilename, tmfilename, outfolder);
    } catch(Exception e)
    {
      System.out.println(e);
    }
  }
}
