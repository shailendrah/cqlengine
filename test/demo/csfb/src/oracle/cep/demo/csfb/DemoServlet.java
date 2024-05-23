package oracle.cep.demo.csfb;

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
  private static final long serialVersionUID = -6618609805182159940L;

  static DemoServlet         s_instance;

  static String[]            symbolCodes =
                                         { "MSFT", "YHOO", "ORCL", "GOOG" };
  int[]                      counts      = new int[4];

  StringBuffer               events;
  int                        MAX_SIZE;
  ArrayBlockingQueue<String> queue;
  ReentrantLock              queueLock   = new ReentrantLock();
  boolean                    queuefull   = false;
  boolean                    verbose     = false;
  ServletConfig              config;

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
    StringBuffer buffer = new StringBuffer("[");
    String histParam = request.getParameter("histogram");
    if (histParam != null)
    {
      if (!generateHistogram(buffer))
      {
        response.sendError(404);
        return;
      }
    } else
    {
      generateDashboardTableEntry(buffer);
    }
    buffer.append("];");
    out.println(buffer.toString());
    //System.out.println("servlet\n" + buffer.toString());
    buffer = null;
  }

  private void generateDashboardTableEntry(StringBuffer buffer)
  {
    Iterator<String> i = queue.iterator();
    while(i.hasNext())
    {
      String element = i.next();
      buffer.append(element);
      if (i.hasNext())
        buffer.append(",");
    } while (true);
  }

  private boolean generateHistogram(StringBuffer buffer)
  {
    for (int i = 0; i < symbolCodes.length; i++)
    {
      buffer.append("{label: '");
      buffer.append(symbolCodes[i]);
      buffer.append("', value: ");
      buffer.append(counts[i]);
      if (i == symbolCodes.length - 1)
        buffer.append("}");
      else
        buffer.append("},");
    }
    return true;
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

  public void addSymbol(String symbol, int count)
  {
    for (int i = 0; i < symbolCodes.length; i++)
    {
      if (symbol.equals(symbolCodes[i]))
      {
        synchronized (counts)
        {
          counts[i] += count;
        }
        break;
      }
    }
    try
    {
      StringBuilder sb = new StringBuilder(120);
      sb.append("{ Symbol: '");
      sb.append(symbol);
      sb.append("', Count: '");
      sb.append(count);
      sb.append("'}");
      enqueue(sb.toString());
    } catch (Exception e)
    {
      System.out.println("Servelet:addSymbol " + e.toString());
    }
  }
}

