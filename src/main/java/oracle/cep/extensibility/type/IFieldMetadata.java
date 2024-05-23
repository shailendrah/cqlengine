package oracle.cep.extensibility.type;

import oracle.cep.extensibility.cartridge.IMetadataElement;

/**
 * Metadata for complex type fields (i.e. attributes) and Java-Bean style
 *  properties.
 * 
 * @author Alex Alves
 *
 */
public interface IFieldMetadata extends IMetadataElement
{
  /** 
   * Returns the type for this field.
   * 
   * @return String
   */
  IType getType();
  
  /**
   * Returns if get is static.
   * 
   * @return boolean
   */
  boolean isGetStatic();
  
  /**
   * Returns if set is static.
   * 
   * @return boolean
   */
  boolean isSetStatic();
  
  /**
   * Returns true if field can be modified.
   * 
   * @return boolean
   */
  boolean hasSet();
  
  /**
   * Returns true if field can be read.
   * 
   * @return boolean
   */
  boolean hasGet();
  
  /**
   * Return underlying runtime implementation
   */
  IField getFieldImplementation();
}
