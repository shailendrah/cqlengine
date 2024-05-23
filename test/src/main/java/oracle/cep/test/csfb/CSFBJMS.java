package oracle.cep.test.csfb;
/* $Header: CSFBJMS.java 12-oct-2006.01:59:59 anasrini Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    OOW Demo CSFB test case with JMS as sources

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    10/12/06 - scale to 100
    najain      10/09/06 - test change
    anasrini    09/19/06 - Creation
    anasrini    09/19/06 - Creation
 */

/**
 *  @version $Header: CSFBJMS.java 12-oct-2006.01:59:59 anasrini Exp $
 *  @author  anasrini
 *  @since   1.0
 */

import java.sql.Timestamp;

import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.Datatype;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.external.TupleKind;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.queues.ElementKind;
import oracle.cep.interfaces.input.FileSource;
import oracle.cep.interfaces.output.QueryOutput;
//import oracle.cep.interfaces.output.JMSDestination;
import oracle.cep.interfaces.output.QueryOutputBase;


public class CSFBJMS implements Runnable {

  private static final String CONN_FACT = "jms/ConnectionFactory";
  private static long tbase;
  private static long tsysbase;
  private static final long scale = 100;
  private static final long ONE_MINUTE = 60000;

  //TODO fix with new api
  private FileSource     fs;
  private QueryOutput jd;
  private TupleValue     hbeat;

  public CSFBJMS(String fileName, String jmsConnFact, String jmsDest) {
    //fs = new FileSource(fileName);
    //jd = new JMSDestination(jmsConnFact, jmsDest);

    hbeat = new TupleValue();
    hbeat.setBHeartBeat(true);
  }

  public void setNumAttrs(int numAttrs) {
    jd.setNumAttrs(numAttrs);
  }

  public void setAttrInfo(int attrPos, String attrName, Datatype attrType,
                          int attrLen) {
    
    jd.setAttrInfo(attrPos, attrName, new AttributeMetadata(attrType, attrLen, 0, 0));
  }

  public void run() {
    TupleValue  tv;
    long        tsys;
    long        tcurr;
    long        ttarget;
    QueueElement.Kind kind;

    try {
      fs.start();
      jd.start();

      tv = fs.getNext();
      while (tv != null) {
        tsys    = System.currentTimeMillis() - tsysbase;
        tcurr   = tv.getTime();
        ttarget = tcurr/scale;

        while (tsys < ttarget) {
          try {
            if (ttarget-tsys > ONE_MINUTE/scale)
              Thread.sleep(ONE_MINUTE/scale);
            else
              Thread.sleep(ttarget-tsys);
          }
          catch (InterruptedException ie) {
          }
          tsys = System.currentTimeMillis() - tsysbase;

          if (tsys < ttarget) {
            hbeat.setTime(tsys*scale+tbase);
            jd.putNext(hbeat, QueueElement.Kind.E_HEARTBEAT);
          }

        }

        tv.setTime(tcurr+tbase);
        if (tv.isBHeartBeat())
          kind = QueueElement.Kind.E_HEARTBEAT;
        else if (tv.getKind() == TupleKind.PLUS)
          kind = QueueElement.Kind.E_PLUS;
        else
          kind = QueueElement.Kind.E_MINUS;
        
        jd.putNext(tv, kind);
        tv = fs.getNext();

      }
      
      fs.end();
      jd.end();
    }
    catch (CEPException e) {
      e.printStackTrace();
    }
  }

  private static CSFBJMS setupTI(String fileName, String destName) {
    CSFBJMS cj = new CSFBJMS(fileName, CONN_FACT, destName);
    cj.setNumAttrs(4);
    cj.setAttrInfo(0, "tradeId", Datatype.INT, 0);
    cj.setAttrInfo(1, "tradeVolume", Datatype.INT, 0);
    cj.setAttrInfo(2, "tradeSymbol", Datatype.CHAR, 4);
    cj.setAttrInfo(3, "tradeType", Datatype.INT, 0);

    return cj;
  }

  private static CSFBJMS setupTU(String fileName, String destName) {
    CSFBJMS cj = new CSFBJMS(fileName, CONN_FACT, destName);
    cj.setNumAttrs(2);
    cj.setAttrInfo(0, "tradeId", Datatype.INT, 0);
    cj.setAttrInfo(1, "statusCode", Datatype.INT, 0);

    return cj;
  }

  private static CSFBJMS setupTM(String fileName, String destName) {
    CSFBJMS cj = new CSFBJMS(fileName, CONN_FACT, destName);
    cj.setNumAttrs(1);
    cj.setAttrInfo(0, "tradeId", Datatype.INT, 0);

    return cj;
  }


  public static void main(String[] args) throws Exception {

    CSFBJMS i = setupTI("../inpTI2.txt", "jms/TradeInputs");
    CSFBJMS u = setupTU("../inpTU2.txt", "jms/TradeUpdates");
    CSFBJMS m = setupTM("../inpTM2.txt", "jms/TradeMatched");

    tsysbase = System.currentTimeMillis();
    tbase    = tsysbase/1000;
    long t = tbase / 300;
    if((tbase % 300) == 0)
      tbase = (t * 300);
    else
      tbase = ((t + 1) * 300);
    tbase *= 1000;
    
    Thread ti = new Thread(i);
    Thread tu = new Thread(u);
    Thread tm = new Thread(m);
    
    ti.start();
    tu.start();
    tm.start();

    ti.join();
    tu.join();
    tm.join();
  }
  
}

