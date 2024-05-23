/* $Header: pcbpel/cep/common/src/oracle/cep/descriptors/ArrayContext.java /main/2 2008/09/10 14:06:32 skmishra Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    skmishra    08/25/08 - removing IStorageContext
    skmishra    08/20/08 - changing package name
    mthatte     08/27/07 - To cache Descriptors when database access is not
                           required
    mthatte     08/27/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/descriptors/ArrayContext.java /main/2 2008/09/10 14:06:32 skmishra Exp $
 *  @author  mthatte 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.descriptors;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.ListIterator;

public class ArrayContext implements Externalizable 
{
    private static final long serialVersionUID = 466477373968866490L;

    private ArrayList<MetadataDescriptor> list;
	
	public ArrayContext() {
		list=new ArrayList<MetadataDescriptor>();
	}
	
	public ListIterator<MetadataDescriptor> getIterator() {
		return list.listIterator();
	}
	
	public void add(MetadataDescriptor m) {
		list.add(m);
	}
	
	public MetadataDescriptor getNext() {
		if(list.listIterator().hasNext())
			return list.listIterator().next();
		return null;
	}
	
	public boolean next() {
		if(list.listIterator().hasNext()) {
			list.listIterator().next();
			return true;
		}
		
		return false;
	}

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        int n = list.size();
        out.writeInt(n);
        for (int i = 0; i < n; i++) {
            MetadataDescriptor m = list.get(i);
            out.writeObject(m);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        int n = in.readInt();
        for (int i = 0; i < n; i++) {
            MetadataDescriptor m = (MetadataDescriptor) in.readObject();
            list.add(m);
        }
    }
	
}
