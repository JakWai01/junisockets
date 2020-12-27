package space.nebulark.junisockets.errors;

public class UnimplementedOperation extends Exception {
    
    /**
     *
     */
    private static final long serialVersionUID = -353607365316792890L;

    public UnimplementedOperation(Object object) {
        super("unimplemented operation " + (String)object);
    }

}
