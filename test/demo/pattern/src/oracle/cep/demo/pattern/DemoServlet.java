package oracle.cep.demo.pattern;

import java.io.*;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.*;
import javax.servlet.*;
import javax.servlet.http.*;

import oracle.cep.dataStructures.external.AttributeValue;
import oracle.cep.dataStructures.external.TupleValue;

public class DemoServlet extends HttpServlet
{
  static DemoServlet s_instance;

  StringBuffer               events;
  static int                 MAX_SIZE       = 35;
  ArrayBlockingQueue<String> queue;
  ReentrantLock              queueLock = new ReentrantLock();
  boolean                    queuefull = false;
  ServletConfig              config;

  public static DemoServlet getInstance()
  {
    if (s_instance == null)
      s_instance = new DemoServlet();
    return s_instance;
  }

  DemoServlet()
  {
    System.out.println("DemoServlet created");
    events = new StringBuffer();
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
    generateStocks(buffer);
    buffer.append("];");
    out.println(buffer.toString());
    // System.out.println("servlet\n" + buffer.toString());
    buffer = null;
  }

  private void generateStocks(StringBuffer buffer)
  {
    Iterator<String> i = queue.iterator();
    do
    {
      if (!i.hasNext())
        break;
      String element = i.next();
      buffer.append(element);
      if (i.hasNext())
        buffer.append(",");
    } while (true);
  }

  public void addTime(TupleValue event)
  {
    try
    {
      AttributeValue[] attrs = event.attrs;
      int tm = attrs[0].iValueGet();
      StringBuilder sb = new StringBuilder(120);
      sb.append("{ time: ");
      sb.append(tm);
      sb.append(", value: -1");
      sb.append("}");
      String str = sb.toString();
      enqueue(str);
    } catch (Exception e)
    {
      System.out.println("Servlet:add " + e.toString());
    }
  }
  
  public void addInput(TupleValue event)
  {
    try
    {
      AttributeValue[] attrs = event.attrs;
      int tm = attrs[0].iValueGet();
      int val = attrs[1].iValueGet();

      StringBuilder sb = new StringBuilder(120);
      sb.append("{ time: ");
      sb.append(tm);
      sb.append(", value: ");
      sb.append(val);
      sb.append("}");
      enqueue(sb.toString());
    } catch (Exception e)
    {
      System.out.println("Servlet:add " + e.toString());
    }
  }
  
  private void enqueue(String str) throws InterruptedException
  {
      //System.out.println(str);
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
}
