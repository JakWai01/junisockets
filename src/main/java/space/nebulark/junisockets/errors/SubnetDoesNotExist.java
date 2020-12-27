package space.nebulark.junisockets.errors;

public class SubnetDoesNotExist extends Exception {
    
    /**
     *
     */
    private static final long serialVersionUID = 8161487209290448004L;

    public SubnetDoesNotExist() {
        super("subnet does not exist");
    }

}
