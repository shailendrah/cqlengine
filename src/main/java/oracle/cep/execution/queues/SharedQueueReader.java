/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/queues/SharedQueueReader.java /main/18 2011/04/10 21:20:46 sborah Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares SharedQueueReader in package oracle.cep.execution.queues.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 anasrini  04/05/11 - add get/set readerContext
 hopark    12/02/08 - move LogLevelManager to ExecContext
 hopark    10/10/08 - remove statics
 hopark    06/18/08 - logging refactor
 najain    04/24/08 - stats
 najain    04/11/08 - add trace
 hopark    02/25/08 - support paged queue
 hopark    12/27/07 - support xmllog
 hopark    11/08/07 - handle exception
 hopark    10/30/07 - remove IQueueElement
 hopark    10/25/07 - remove QueueElement
 najain    07/23/07 - move stats to SharedQueueWriter
 hopark    06/07/07 - use LogArea
 hopark    05/28/07 - logging support
 hopark    03/23/07 - throws Exception from QueueElement
 najain    03/12/07 - bug fix
 hopark    03/07/07 - spill-over support
 najain    10/12/06 - add statistics
 najain    06/28/06 - integration with memory manager 
 najain    06/20/06 - add remove 
 najain    06/13/06 - bug fix 
 najain    05/04/06 - sharing support 
 anasrini  03/24/06 - add toString 
 skaluska  02/22/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/queues/SharedQueueReader.java /main/17 2008/12/10 18:55:56 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.queues;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.logging.DumpDesc;
import oracle.cep.logging.IDumpContext;
import oracle.cep.logging.ILogEvent;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.trace.LogArea;
import oracle.cep.logging.trace.LogEvent;
import oracle.cep.logging.trace.LogLevel;
import oracle.cep.logging.trace.LogLevelManager;
import oracle.cep.logging.trace.LogTags;
import oracle.cep.service.ExecContext;
import oracle.cep.util.StringUtil;

/**
 * SharedQueueReader
 *
 * @author skaluska
 */
@DumpDesc(attribTags={"Id", "PhyId", "ReaderId", "WriterId"}, 
          attribVals={"getId", "getPhyId", "getReaderId", "getWriterId"},
          infoLevel=LogLevel.QUEUE_INFO,
          evPinLevel=LogLevel.QUEUE_ELEMENT_PINNED,
          evUnpinLevel=LogLevel.QUEUE_ELEMENT_UNPINNED,
          dumpLevel=LogLevel.QUEUE_DUMP,
          verboseDumpLevel=LogLevel.QUEUE_DUMPELEMS)
public class SharedQueueReader extends Queue implements ISharedQueueReader, Externalizable
{
  private static final long serialVersionUID = 5656949632354627296L;

/** reader operator */
  private ExecOpt destOp;

  /** Shared writer queue from which we read dequeues */
  private ISharedQueueWriter writer;

  private SharedQueueReaderStats stats;

  /** The Id by which we identify ourselves to the writer */
  private int               readerId;

  /** The reader context associated with this reader by the reader operator */
  private QueueReaderContext readerCtx;

  public SharedQueueReader()
  {}
  
  /**
   * Constructor for SharedQueueReader
   */
  public SharedQueueReader(ExecContext ec)
  {
    super(ec);
    LogLevelManager.trace(LogArea.QUEUE, LogEvent.QUEUE_DDL, this, "new");
  }

  public void initialize()
  {
    writer = null;
    readerId = 0;
  }

  /**
   * @return Returns the destOp.
   */
  public ExecOpt getDestOp()
  {
    return destOp;
  }

  /**
   * @param destOp The destOp to set.
   */
  public void setDestOp(ExecOpt destOp)
  {
    this.destOp = destOp;
  }

  /**
   * Getter for readerId in SharedQueueReader
   * 
   * @return Returns the readerId
   */
  public int getReaderId()
  {
    return readerId;
  }

  /**
   * Getter for writerId
   * 
   * @return writerId
   */
  public int getWriterId()
  {
    return writer.getId();
  }
  
  /**
   * @return Returns the stats.
   */
  public SharedQueueReaderStats getStats()
  {
    return stats;
  }

  /**
   * Setter for readerId in SharedQueueReader
   * 
   * @param readerId
   *          The readerId to set.
   */
  public void setReaderId(int readerId)
  {
    this.readerId = readerId;
    assert writer != null;
    stats = (SharedQueueReaderStats) writer.getReaderStats(readerId);
  }

  /**
   * Getter for writer in SharedQueueReader
   * 
   * @return Returns the writer
   */
  public ISharedQueueWriter getWriter()
  {
    return writer;
  }

  /**
   * Setter for writer in SharedQueueReader
   * 
   * @param writer
   *          The writer to set.
   */
  public void setWriter(ISharedQueueWriter writer)
  {
    this.writer = writer;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.queues.Queue#enqueue(oracle.cep.execution.queues.Element)
   */
  public void enqueue(QueueElement e)
  {
    // should never be called
    assert false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.queues.Queue#dequeue()
   */
  public QueueElement dequeue(QueueElement buf) throws ExecException
  {
    QueueElement e = writer.dequeue(readerId, buf);

    if (e != null)
    {
      stats.setTsLastElement(e.getTs());
      switch (e.getKind())
      {
        case E_PLUS: stats.setTsLastPosElement(e.getTs()); break;
        case E_MINUS: stats.setTsLastNegElement(e.getTs()); break;
        case E_HEARTBEAT: stats.setTsLastHeartbeat(e.getTs()); break;
        default: assert false;
      }

      stats.incrTotalNumElements();
      switch (e.getKind())
      {
        case E_PLUS: stats.incrTotalNumPosElements(); break;
        case E_MINUS: stats.incrTotalNumNegElements(); break;
        case E_HEARTBEAT: stats.incrTotalNumHeartbeats(); break;
        default: assert false;
      }
      LogLevelManager.trace(LogArea.QUEUE, LogEvent.QUEUE_DEQUEUE, this, readerId, e);
    }
    
    return e;
  }

  public void remove()
  {
    LogLevelManager.trace(LogArea.QUEUE, LogEvent.QUEUE_DDL, this, 
                  "remove", readerId);
    writer.remove(readerId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.queues.Queue#peek()
   */
  public QueueElement peek(QueueElement buf)
  {
    return writer.peek(readerId, buf);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.queues.Queue#isFull()
   */
  public boolean isFull()
  {
    return writer.isFull(readerId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.queues.Queue#isEmpty()
   */
  public boolean isEmpty()
  {
    return writer.isEmpty(readerId);
  }

  /**
   * Setter for reader context associated with this reader
   * @param readerCtx reader context associated with this reader
   */
  public void setReaderContext(QueueReaderContext readerCtx)
  {
    this.readerCtx = readerCtx;
  }

  /**
   * Getter for reader context associated with this reader
   * @return reader context associated with this reader
   */
  public QueueReaderContext getReaderContext()
  {
    return readerCtx;
  }


  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("<SharedQueueReader id=\"" + getId() + "\" writer=\"" + 
              writer.getId() + "\" readerId=\"" + readerId + "\" />");
    return sb.toString();
  }

  /*************************************************************************/
  // ILoggable helpers
  protected String getStatsStr()
  {
    return stats.toString();
  }
  
  public synchronized void dump(IDumpContext dumper) 
  {
    try
    {
      if (!dumper.isVerbose())
      {
        String tag = LogUtil.beginDumpObj(dumper, this);
        dumper.writeln("Size", writer.getSize(readerId));
        LogUtil.endDumpObj(dumper, tag);
        return;
      }
      String dumperKey = StringUtil.getBaseClassName(this);
      IDumpContext w = dumper.openDumper(dumperKey);
      String tag = LogUtil.beginDumpObj(w, this);
      writer.dumpElements(readerId, w);
      LogUtil.endDumpObj(w, tag);
      w.closeDumper(dumperKey, dumper);
    }
    catch(ExecException e)
    {
      dumper.writeln(LogTags.DUMP_ERR, e.toString());
    }
  }

  public void trace(IDumpContext dumper, ILogEvent event, int level, Object[] args)
  {
    if (level == LogLevel.QUEUE_STATS)
    {
      dumper.writeln("SharedQueueReaderId", Integer.toString(getId()));

      dumper.writeln("SharedQueueReaderPhyId", Integer.toString(getPhyId()));

      dumper.writeln("numElementsReader", 
		     Integer.toString(((SharedQueueWriterStats)writer.getStats()).getTotalNumElements() - stats.getTotalNumElements()));

      dumper.writeln("numPosElementsReader", 
		     Integer.toString(((SharedQueueWriterStats)writer.getStats()).getTotalNumPosElements() - stats.getTotalNumPosElements()));

      dumper.writeln("numNegElementsReader", 
		     Integer.toString(((SharedQueueWriterStats)writer.getStats()).getTotalNumNegElements() - stats.getTotalNumNegElements()));

      dumper.writeln("numHeartbeats", 
		     Integer.toString(((SharedQueueWriterStats)writer.getStats()).getTotalNumHeartbeats() - stats.getTotalNumHeartbeats()));

    }
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    out.writeObject(writer);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    writer = (ISharedQueueWriter) in.readObject();
  }

  @Override
  public void copyFrom(ISharedQueueReader other) throws ExecException
  {
    if(other instanceof SharedQueueReader)
    {
      SharedQueueReader otherReader = (SharedQueueReader)other;
      writer.copyFrom(otherReader.writer);      
    }
    else
      throw new ExecException(ExecutionError.SNAPSHOT_LOAD_ERROR);
  }
}
