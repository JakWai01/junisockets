package space.nebulark.junisockets.errors;

/**
 * Client is closed
 */
public class ClientClosed extends Exception {
    
    /**
     *
     */
    private static final long serialVersionUID = -7585215220730792267L;

    /**
     * Constructor ClientClosed
     */
    public ClientClosed() {
        super("client is closed");
    }

}