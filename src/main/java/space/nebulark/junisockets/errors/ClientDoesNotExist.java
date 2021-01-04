package space.nebulark.junisockets.errors;

/**
 * Client does not exist
 */
public class ClientDoesNotExist extends Exception {
   
    /**
     *
     */
    private static final long serialVersionUID = 8193638927264736702L;

    /** 
     * Constructor ClientDoesNotExist
     */
    public ClientDoesNotExist() {
        super("client does not exist");
    }

}
