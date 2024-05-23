
/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    alealves    07/23/09 - Data cartridge
    hopark      06/05/09 - Creation
 */
package oracle.cep.extensibility.cartridge;

/**
 * Root exception for cartridge-related exceptions. 
 *
 */
public class CartridgeException extends Exception
{
  private static final long serialVersionUID = -6289025138668132559L;

  private final String cartridgeName;

  public CartridgeException(String cartridgeName)
  {
    super();
    this.cartridgeName = cartridgeName;
  }

  public CartridgeException(String cartridgeName, String message, Throwable cause)
  {
    super(message, cause);
    this.cartridgeName = cartridgeName;
  }

  public CartridgeException(String cartridgeName, String message)
  {
    super(message);
    this.cartridgeName = cartridgeName;
  }

  public CartridgeException(String cartridgeName, Throwable cause)
  {
    super(cause);    
    this.cartridgeName = cartridgeName;
  }
  
  public String getCartridgeName() 
  {
    return cartridgeName;
  }
}