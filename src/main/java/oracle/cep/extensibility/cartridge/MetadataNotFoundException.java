package oracle.cep.extensibility.cartridge;


public class MetadataNotFoundException extends CartridgeException
{
  private static final long serialVersionUID = -4842084958602514515L;
  
  private String metadataName;

  public MetadataNotFoundException(String cartridgeName, String metadataName)
  {
    super(cartridgeName);
    this.metadataName = metadataName;
  }

  public MetadataNotFoundException(String cartridgeName, String metadataName, 
      Throwable cause)
  {
    super(cartridgeName, cause);
    this.metadataName = metadataName;
  }
  
  public String getMetadataName() 
  {
    return this.metadataName;
  }
}
