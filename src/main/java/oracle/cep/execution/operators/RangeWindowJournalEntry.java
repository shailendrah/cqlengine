package oracle.cep.execution.operators;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import oracle.cep.dataStructures.internal.ITuplePtr;

public class RangeWindowJournalEntry implements Externalizable
{     
  private LinkedHashMap<Long,ITuplePtr> plusChangeEvents;
  private LinkedHashMap<Long,ITuplePtr> minusChangeEvents;
  private LinkedList<ITuplePtr> plusBufferedOutTuples;
  private LinkedList<Long> plusBufferedOutTuplesTs;
  private boolean hasBufferedElements;
  private boolean shouldBeBuffered;
  private boolean synopsisScanRequired;
  private RangeWindowState mutable_state;
  
  public RangeWindowJournalEntry()
  {
    plusChangeEvents = new LinkedHashMap<Long,ITuplePtr>();    
  } 
  
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
     // Instance variables in RangeWindow operator
    out.writeObject(plusBufferedOutTuples);
    out.writeObject(plusBufferedOutTuplesTs);
    out.writeBoolean(hasBufferedElements);
    out.writeBoolean(shouldBeBuffered);
    out.writeBoolean(synopsisScanRequired);
    
    // Write RangeWindowState into output buffer
    out.writeObject((RangeWindowState)mutable_state);
    
    // Write change events to output buffer
    out.writeObject(plusChangeEvents);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {    
    // Instance variables of RangeWindow operator
    plusBufferedOutTuples = (LinkedList<ITuplePtr>) in.readObject();
    plusBufferedOutTuplesTs = (LinkedList<Long>) in.readObject();
    hasBufferedElements = in.readBoolean();
    shouldBeBuffered = in.readBoolean();
    synopsisScanRequired = in.readBoolean();
    
    // Read RangeWindowState from input buffer
    mutable_state = (RangeWindowState) in.readObject();
    
    plusChangeEvents = (LinkedHashMap<Long, ITuplePtr>) in.readObject();    
  }

  public LinkedHashMap<Long, ITuplePtr> getPlusChangeEvents()
  {
    return plusChangeEvents;
  }

  public void setPlusChangeEvents(LinkedHashMap<Long, ITuplePtr> plusChangeEvents)
  {
    this.plusChangeEvents = plusChangeEvents;
  }

  public LinkedHashMap<Long, ITuplePtr> getMinusChangeEvents()
  {
    return minusChangeEvents;
  }

  public void setMinusChangeEvents(
      LinkedHashMap<Long, ITuplePtr> minusChangeEvents)
  {
    this.minusChangeEvents = minusChangeEvents;
  }

  public LinkedList<ITuplePtr> getPlusBufferedOutTuples()
  {
    return plusBufferedOutTuples;
  }

  public void setPlusBufferedOutTuples(LinkedList<ITuplePtr> plusBufferedOutTuples)
  {
    this.plusBufferedOutTuples = plusBufferedOutTuples;
  }

  public LinkedList<Long> getPlusBufferedOutTuplesTs()
  {
    return plusBufferedOutTuplesTs;
  }

  public void setPlusBufferedOutTuplesTs(LinkedList<Long> plusBufferedOutTuplesTs)
  {
    this.plusBufferedOutTuplesTs = plusBufferedOutTuplesTs;
  }

  public boolean isHasBufferedElements()
  {
    return hasBufferedElements;
  }

  public void setHasBufferedElements(boolean hasBufferedElements)
  {
    this.hasBufferedElements = hasBufferedElements;
  }

  public boolean isShouldBeBuffered()
  {
    return shouldBeBuffered;
  }

  public void setShouldBeBuffered(boolean shouldBeBuffered)
  {
    this.shouldBeBuffered = shouldBeBuffered;
  }

  public boolean isSynopsisScanRequired()
  {
    return synopsisScanRequired;
  }

  public void setSynopsisScanRequired(boolean synopsisScanRequired)
  {
    this.synopsisScanRequired = synopsisScanRequired;
  }

  public RangeWindowState getMutable_state()
  {
    return mutable_state;
  }

  public void setMutable_state(RangeWindowState mutable_state)
  {
    this.mutable_state = mutable_state;
  }    
  
  @Override
  public String toString()
  {
    return "RangeWindowJournalEntry [plusChangeEvents=" + plusChangeEvents
        + ", minusChangeEvents=" + minusChangeEvents
        + ", plusBufferedOutTuples=" + plusBufferedOutTuples
        + ", plusBufferedOutTuplesTs=" + plusBufferedOutTuplesTs
        + ", hasBufferedElements=" + hasBufferedElements
        + ", shouldBeBuffered=" + shouldBeBuffered + ", synopsisScanRequired="
        + synopsisScanRequired + ", mutable_state=" + mutable_state + "]";
  }
}
