/* $Header: pcbpel/cep/server/src/oracle/cep/util/CloneCreator.java /main/1 2009/01/16 22:55:00 parujain Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates.All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    01/13/09 - clone creator
    parujain    01/13/09 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/util/CloneCreator.java /main/1 2009/01/16 22:55:00 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class CloneCreator
{
  public static Object cloneObject(Object obj) throws Exception
  {
    ObjectOutputStream oos = null;
    ObjectInputStream ois = null;
    
    try{
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      oos = new ObjectOutputStream(bos);
      // serialize and pass the old obj
      oos.writeObject(obj);
      oos.flush();
      ByteArrayInputStream bis = 
                    new ByteArrayInputStream(bos.toByteArray());
      ois = new ObjectInputStream(bis);
      /*
      // Need to use the classloader from ClassGenBase
      // to resolve dynamically generated tuple/page classes.
      ois = new ObjectInputStream(bis) {
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
          if (desc.getName().startsWith("oracle.cep.memmgr.Page_"))
            return ClassGenBase.getClassLoader().loadClass(desc.getName());
          return super.resolveClass(desc);
        }
      };
Add proper exception handling
    } catch (IOException e)
    {
      LogUtil.warning(LoggerType.TRACE, e.toString());
    }
    catch (NotSerializableException se)
    {
      LogUtil.severe(LoggerType.TRACE, "Failed to serialize from " + (obj == null) ? "null" : obj.toString());
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, se);
      assert false : se.toString();
    } catch (ClassNotFoundException e)
    {
      LogUtil.severe(LoggerType.TRACE, "Failed to deserialize from " + (obj == null) ? "null" : obj.toString());
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
      assert false : e.toString();
    } catch (Throwable oe)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
    } 
       */
      return ois.readObject();
    }
    catch(Exception e)
    {
  
    }
    finally
    {
      oos.close();
      ois.close();
    }
    return null;
  }
}