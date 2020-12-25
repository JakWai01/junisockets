package space.nebulark.junisockets.errors;

public class ClientDoesNotExist extends Exception {
   
    public ClientDoesNotExist() {
        super("client does not exist");
    }

}
