package oracle.cep.extensibility.cartridge;

/**
 * Multiple target objects match a metadata name.
 * For example, there are multiple function implementations found in the 
 *  cartridges for a metadata name.
 *  
 * @author Alex Alves
 *
 */
public class AmbiguousMetadataException extends CartridgeException
{
  private static final long serialVersionUID = -4842084958602514515L;
  
  private String metadataName;

  public AmbiguousMetadataException(String cartridgeName, String metadataName)
  {
    super(cartridgeName);
    this.metadataName = metadataName;
  }

  public AmbiguousMetadataException(String cartridgeName, String metadataName, 
      Throwable cause)
  {
    super(cartridgeName, cause);
    this.metadataName = metadataName;
  }
  
  public AmbiguousMetadataException(String cartridgeName, String metadataName, 
      String message, Throwable cause)
  {
    super(cartridgeName, message, cause);
    this.metadataName = metadataName;
  }
  
  public String getMetadataName() 
  {
    return this.metadataName;
  }
}
