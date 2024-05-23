package oracle.cep.driver.net;

/**
 * Struct containing the data that the server returns in response
 * to a register input command
 */
public class RegInputRet {
    
    /// The identifier for the input
    public int inputId;
    
    /// The error code
    public int errorCode;
}

