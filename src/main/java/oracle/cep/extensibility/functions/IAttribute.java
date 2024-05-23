package oracle.cep.extensibility.functions;

import java.io.Serializable;

import oracle.cep.common.Datatype;

public interface IAttribute extends Serializable, Cloneable
{
  /**
   * Getter for name in Attribute
   * @return Returns the name
   */
  String getName();

  /**
   * Getter for type in Attribute
   * @return Returns the type
   */
  Datatype getType();

  /**
   * Getter for maxLength in Attribute
   * @return Returns the maxLength
   */
  int getMaxLength();

  /**
   * Getter for precision value of Attribute
   * @return the precision value
   */
  int getPrecision();
  
  /**
   * Getter for scale value of Attribute
   * @return the scale value
   */
  int getScale();
  
  
  /**
   * Getter for position in Attribute
   * @return Returns the position
   */
  int getPosition();

  /**
   * Construct the attribute metadata as XML
   * @return XML string
   */
  String toXml();

}