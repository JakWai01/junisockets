package space.nebulark.junisockets.errors;

public class UnimplementedOperation extends Exception {
    
    public UnimplementedOperation(Object object) {
        super("unimplemented operation " + (String)object);
    }

}
