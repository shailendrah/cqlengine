/* $Header: pcbpel/cep/common/src/oracle/cep/descriptors/IMetadataDescriptorFactory.java /main/2 2008/09/10 14:06:32 skmishra Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    skmishra    08/20/08 - changing package name
    mthatte     08/22/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/descriptors/IMetadataDescriptorFactory.java /main/2 2008/09/10 14:06:32 skmishra Exp $
 *  @author  mthatte 
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.descriptors;
public interface IMetadataDescriptorFactory {
	public MetadataDescriptor allocateDescriptor() throws UnsupportedOperationException;
}
