/* (c) 2006-2009 Oracle.  All rights reserved. */
package oracle.cep.test.userfunctions;

import java.io.Serializable;

public class TkUserObj implements Serializable 
{
  private String message;

  public TkUserObj(Object o)
  {
    message = o.toString();
  }

  public String getMessage() 
  {
    return message;
  }

  public void setMessage (String message) 
  {
    this.message = message;
  }

  public String toString()
  {
    return "msg=" + message;
  }

  public int hashCode()
  {
    return message.hashCode();
  }

  public boolean equals(Object other)
  {
    if(other == null)
      return false;

    if(!(other instanceof TkUserObj))
      return false;

    TkUserObj otherObj = (TkUserObj)other;

    if(this.message.equals(otherObj.getMessage()))
      return true;
    else
      return false;
  }  
}

