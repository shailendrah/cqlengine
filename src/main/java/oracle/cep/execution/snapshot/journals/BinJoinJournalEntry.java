package oracle.cep.execution.snapshot.journals;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedHashMap;

import oracle.cep.dataStructures.internal.ITuplePtr;

/**
 * Journal Entry for Binary Join Operator. 
 * @author sbishnoi
 */
public class BinJoinJournalEntry implements Externalizable
{
  private LinkedHashMap<Long,ITuplePtr> innerPlusChangeEvents;
  private LinkedHashMap<Long,ITuplePtr> innerMinusChangeEvents;
  private LinkedHashMap<Long,ITuplePtr> outerPlusChangeEvents;
  private LinkedHashMap<Long,ITuplePtr> outerMinusChangeEvents;
  
  public BinJoinJournalEntry()
  {
    innerPlusChangeEvents = new LinkedHashMap<Long, ITuplePtr>();
    innerMinusChangeEvents = new LinkedHashMap<Long, ITuplePtr>();
    outerPlusChangeEvents = new LinkedHashMap<Long, ITuplePtr>();
    outerMinusChangeEvents = new LinkedHashMap<Long, ITuplePtr>();
  }

  public LinkedHashMap<Long, ITuplePtr> getInnerPlusChangeEvents()
  {
    return innerPlusChangeEvents;
  }

  public void setInnerPlusChangeEvents(
      LinkedHashMap<Long, ITuplePtr> innerPlusChangeEvents)
  {
    this.innerPlusChangeEvents = innerPlusChangeEvents;
  }

  public LinkedHashMap<Long, ITuplePtr> getInnerMinusChangeEvents()
  {
    return innerMinusChangeEvents;
  }

  public void setInnerMinusChangeEvents(
      LinkedHashMap<Long, ITuplePtr> innerMinusChangeEvents)
  {
    this.innerMinusChangeEvents = innerMinusChangeEvents;
  }

  public LinkedHashMap<Long, ITuplePtr> getOuterPlusChangeEvents()
  {
    return outerPlusChangeEvents;
  }

  public void setOuterPlusChangeEvents(
      LinkedHashMap<Long, ITuplePtr> outerPlusChangeEvents)
  {
    this.outerPlusChangeEvents = outerPlusChangeEvents;
  }

  public LinkedHashMap<Long, ITuplePtr> getOuterMinusChangeEvents()
  {
    return outerMinusChangeEvents;
  }

  public void setOuterMinusChangeEvents(
      LinkedHashMap<Long, ITuplePtr> outerMinusChangeEvents)
  {
    this.outerMinusChangeEvents = outerMinusChangeEvents;
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    out.writeObject(innerPlusChangeEvents);
    out.writeObject(innerMinusChangeEvents);
    out.writeObject(outerPlusChangeEvents);
    out.writeObject(outerMinusChangeEvents);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    innerPlusChangeEvents = (LinkedHashMap<Long, ITuplePtr>) in.readObject();
    innerMinusChangeEvents = (LinkedHashMap<Long, ITuplePtr>) in.readObject();
    outerPlusChangeEvents = (LinkedHashMap<Long, ITuplePtr>) in.readObject();
    outerMinusChangeEvents = (LinkedHashMap<Long, ITuplePtr>) in.readObject();
  }

  @Override
  public String toString()
  {
    return "BinJoinJournalEntry [num-inner-plus-change-events=" + innerPlusChangeEvents.size()
        + ", num-inner-minus-change-events=" + innerMinusChangeEvents.size() 
        + ", num-outer-plus-change-events=" + outerPlusChangeEvents.size() 
        + ", num-outer-minus-change-events=" + outerMinusChangeEvents.size() 
        + "]";
  }  
}