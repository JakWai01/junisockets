package space.nebulark.junisockets.errors;

/**
 * Subnet does not exist
 */
public class SubnetDoesNotExist extends Exception {
    
    /**
     *
     */
    private static final long serialVersionUID = 8161487209290448004L;

    /**
     * Constructor SubnetDoesNotExist
     */
    public SubnetDoesNotExist() {
        super("subnet does not exist");
    }

}
