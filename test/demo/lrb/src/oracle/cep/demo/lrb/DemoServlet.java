package oracle.cep.demo.lrb;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.*;
import javax.servlet.*;
import javax.servlet.http.*;

import oracle.cep.dataStructures.external.AttributeValue;
import oracle.cep.dataStructures.external.TupleValue;

public class DemoServlet extends HttpServlet
{
  static DemoServlet s_instance;
  Map<Integer, Integer>      accSeg;
  Map<Integer, SegCoord>     segCoord;
  Map<Integer, SegTravelTime> segTravelTime;
  float[]                    segSpeed;
  StringBuffer               events;
  static int                 MAX_SEGS = 100;
  ServletConfig              config;
  static int[]               knownSegs = {8 ,12 , 15, 89, 14, 10};
  
  int                        MAX_SIZE;
  ArrayBlockingQueue<String> queue;
  ReentrantLock              queueLock   = new ReentrantLock();
  boolean                    queuefull   = false;
  
  public static DemoServlet getInstance()
  {
    if (s_instance == null)
      s_instance = new DemoServlet();
    return s_instance;
  }

  DemoServlet()
  {
    s_instance = this;
    events = new StringBuffer();
    MAX_SIZE = 50;
    queue = new ArrayBlockingQueue<String>(MAX_SIZE);

    accSeg = new HashMap<Integer, Integer>();
    segCoord = new HashMap<Integer, SegCoord>();

    segCoord.put(8, new SegCoord(8,    37.488631,   -122.212653,   37.483814,   -122.181394));
    segCoord.put(12, new SegCoord(12,   37.483814,   -122.181394,   37.478975,   -122.172494));
    segCoord.put(15, new SegCoord(15,   37.478975,   -122.172494,   37.464647,   -122.147906));
    segCoord.put(89, new SegCoord(89,   37.464647,   -122.147906,   37.460406,   -122.140792));
    segCoord.put(14, new SegCoord(14,   37.460406,   -122.140792,   37.452022,   -122.126597));
    segCoord.put(10, new SegCoord(10,   37.452022,   -122.126597,   37.430081,   -122.103281));
    segCoord.put(85, new SegCoord(85,   37.488631,   -122.212653,   37.483814,   -122.181394));
    segCoord.put(95, new SegCoord(95,   37.483814,   -122.181394,   37.478975,   -122.172494));
    segCoord.put(86, new SegCoord(86,   37.478975,   -122.172494,   37.464647,   -122.147906));
    segCoord.put(92, new SegCoord(92,   37.464647,   -122.147906,   37.460406,   -122.140792));
    segCoord.put(4, new SegCoord(4,   37.460406,   -122.140792,   37.452022,   -122.126597));
    segCoord.put(87, new SegCoord(87,   37.452022,   -122.126597,   37.430081,   -122.103281));

    segTravelTime = new HashMap<Integer, SegTravelTime>();
    segTravelTime.put(10, new SegTravelTime(10, 0, 20, 30));
    segTravelTime.put(14, new SegTravelTime(14, 0, 25, 35));
    segTravelTime.put(89, new SegTravelTime(89, 0, 30, 40));
    segTravelTime.put(15, new SegTravelTime(15, 5, 35, 45));
    segTravelTime.put(12, new SegTravelTime(12, 10, 40, 50));
    segTravelTime.put(8, new SegTravelTime(8, 15, 45, 55));
    
    segSpeed = new float[MAX_SEGS];
  }

  public void destroy()
  {
  }

  public void init(ServletConfig config) throws ServletException
  {
    super.init(config);
    this.config = config;
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException
  {
    doMainPage(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException
  {
    doMainPage(request, response);
  }

  private void doMainPage(HttpServletRequest request,
      HttpServletResponse response) throws IOException
  {
    response.setContentType("text/html");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Cache-Control", "must-revalidate");
    response.setHeader("Cache-Control", "no-cache");
    response.setHeader("Cache-Control", "no-store");
    response.setDateHeader("Expires", 0L);
    PrintWriter out = response.getWriter();
    String input = request.getQueryString();
    String type;
    int segId;
    int delimValue = input.indexOf("&");
    if(delimValue == -1)
    {
      type  = input.substring("type=".length(),input.length());
      segId = 0;
    }
    else
    {
      type = input.substring("type=".length(),delimValue);
      segId = Integer.valueOf(input.substring(delimValue+1+"segid=".length(), input.length())).intValue();
    }
    if(type.equalsIgnoreCase("acc"))
    {
      out.println(getAccHistory());  
    }
    else if(type.equalsIgnoreCase("speed"))
    {
      out.println(getSpeed());  
    }
    else if(type.equalsIgnoreCase("time"))
    {
      out.println(getTime(segId));
    }
  }

  private String getAccHistory()
  {
    StringBuffer buffer = new StringBuffer();
    buffer.append("[ ");
    Iterator<String> i = queue.iterator();
    while(i.hasNext())
    {
      String element = i.next();
      buffer.append(element);
      if (i.hasNext())
        buffer.append(",");
    }
    buffer.append(" ]\n");
    //System.out.println("speed : " + tuple.toString());
    return buffer.toString();
  }
  
  private String getTime(int segId){
    
    String[] dest = {"Palo Alto", "San Jose", "San Jose Airport"};
    SegTravelTime r = segTravelTime.get(segId);
    StringBuffer tuple = new StringBuffer();
    tuple.append("<table><tr><th>Estimated Travel Time</th></tr><tr>");
    for(int i = 0; i < 3; i++)
    {
      int v = r.dests[i];
      tuple.append("<tr><td><b>"); tuple.append(dest[i-1]); tuple.append("</b></td>");
      tuple.append("<td><b>"); tuple.append(v); tuple.append(" minutes</b></td></tr>");
    }
    tuple.append("</tr></table>");
    return tuple.toString();
  }

  private String getSpeed(){
    
    StringBuffer tuple = new StringBuffer();
    tuple.append("[ ");
    int pos = 0;
    for (int segId : knownSegs)
    {
      if (pos > 0)
        tuple.append(",\n");
      tuple.append("{ segid: ");
      tuple.append(segId); tuple.append(",");
      SegCoord coord = segCoord.get(segId);
      tuple.append(" sx: "); tuple.append(coord.sx); tuple.append(",");
      tuple.append(" sy: "); tuple.append(coord.sy); tuple.append(",");
      tuple.append(" ex: "); tuple.append(coord.ex); tuple.append(",");
      tuple.append(" ey: "); tuple.append(coord.ey); tuple.append(",");
      tuple.append(" speed: "); tuple.append(segSpeed[segId]); tuple.append(",");
      Integer acc = null; 
      synchronized(accSeg) {
        acc = accSeg.get(segId);
      }
      tuple.append(" acc: "); tuple.append(acc == null ? -1: Integer.toString(segId)); 
      tuple.append("}");
      pos++;
    }
    tuple.append(" ]\n");
    //System.out.println("speed : " + tuple.toString());
    return tuple.toString();
  }
  
  private void enqueue(String str) throws Exception
  {
    if (queuefull)
    {
      queue.take();
      queue.put(str);
    } else
    {
      queueLock.lock();
      if (!queuefull)
      {
        queue.put(str);
        if (queue.size() == MAX_SIZE)
          queuefull = true;
      } else
      {
        queue.take();
        queue.put(str);
      }
      queueLock.unlock();
    }
  }
  
  public void add(String typ, TupleValue event)
  {
    try
    {
      AttributeValue[] attrs = event.attrs;
      if (typ.equals("AccSeg"))
      {
        int segid = attrs[0].iValueGet();
        synchronized(accSeg) {
          accSeg.put(segid, segid);
        }
        StringBuilder sb = new StringBuilder(120);
        sb.append("{ Time: '");
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss a");
        Date date = new Date(event.time);
        String s = formatter.format(date);
        sb.append(s);
        sb.append("', Segment: ");
        sb.append(segid);
        sb.append("}");
        enqueue(sb.toString());
      } else if (typ.equals("SegSpeed"))
      {
        int seg = attrs[0].iValueGet();
        float speed = attrs[1].fValueGet();
        segSpeed[seg] = speed;
      }
    } catch (Exception e)
    {
      System.out.println("Exception while enqueuing events in Servlet: " + e.toString());
    }
  }

  public void remove(String typ, TupleValue event)
  {
    AttributeValue[] attrs = event.attrs;
    try
    {
      if (typ.equals("AccSeg"))
      {
        int segid = attrs[0].iValueGet();
        synchronized(accSeg) {
          accSeg.remove(segid);
        }
      }
    }
    catch (Exception e)
    {
      System.out.println("Exception while removing events in Servlet: " + e.toString());
    }
  }
  
  private static class SegCoord {
    int segId;
    double sx;
    double sy;
    double ex;
    double ey;
    public SegCoord(int segId, double sx, double sy, double ex, double ey)
    {
      this.segId = segId;
      this.sx = sx;
      this.sy = sy;
      this.ex = ex;
      this.ey = ey;
    }
  }
  private static class SegTravelTime
  {
    int dests[];

    public SegTravelTime(int segId, int dest1, int dest2, int dest3)
    {
      this.dests = new int[3];
      this.dests[0] = dest1;
      this.dests[1] = dest2;
      this.dests[2] = dest3;
    }
  }
}

