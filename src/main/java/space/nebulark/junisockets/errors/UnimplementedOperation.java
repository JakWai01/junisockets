package space.nebulark.junisockets.errors;

/**
 * Unimplemented operation
 */
public class UnimplementedOperation extends Exception {
    
    /**
     *
     */
    private static final long serialVersionUID = -353607365316792890L;

    /**
     * Constructor UnimplementedOperation
     * @param object
     */
    public UnimplementedOperation(Object object) {
        super("unimplemented operation " + (String)object);
    }

}
