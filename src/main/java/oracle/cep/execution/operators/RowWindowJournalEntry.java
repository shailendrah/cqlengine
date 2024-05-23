package oracle.cep.execution.operators;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedHashMap;

import oracle.cep.dataStructures.internal.ITuplePtr;

public class RowWindowJournalEntry implements Externalizable
{
  private LinkedHashMap<Long,ITuplePtr> plusChangeEvents;
  private RowWindowState mutable_state;
  
  public RowWindowJournalEntry()
  {
    plusChangeEvents = new LinkedHashMap<Long,ITuplePtr>();
  }

  public LinkedHashMap<Long, ITuplePtr> getPlusChangeEvents()
  {
    return plusChangeEvents;
  }

  public void setPlusChangeEvents(LinkedHashMap<Long, ITuplePtr> plusChangeEvents)
  {
    this.plusChangeEvents = plusChangeEvents;
  }

  public RowWindowState getMutable_state()
  {
    return mutable_state;
  }

  public void setMutable_state(RowWindowState mutable_state)
  {
    this.mutable_state = mutable_state;
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    out.writeObject((RowWindowState)mutable_state);
    out.writeObject(plusChangeEvents);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    mutable_state = (RowWindowState) in.readObject();    
    plusChangeEvents = (LinkedHashMap<Long, ITuplePtr>) in.readObject();
  }

  @Override
  public String toString()
  {
    return "RowWindowJournalEntry [num-plusChangeEvents=" + plusChangeEvents.size()
        + ", mutable_state=" + mutable_state + "]";
  }
}
