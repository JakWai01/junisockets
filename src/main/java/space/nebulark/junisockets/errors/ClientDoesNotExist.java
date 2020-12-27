package space.nebulark.junisockets.errors;

public class ClientDoesNotExist extends Exception {
   
    /**
     *
     */
    private static final long serialVersionUID = 8193638927264736702L;

    public ClientDoesNotExist() {
        super("client does not exist");
    }

}
