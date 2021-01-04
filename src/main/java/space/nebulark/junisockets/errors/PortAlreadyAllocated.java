package space.nebulark.junisockets.errors;

/**
 * Port already allocated
 */
public class PortAlreadyAllocated extends Exception {
   
    /**
     *
     */
    private static final long serialVersionUID = -2877114095535702618L;

    /**
     * Constructor PortAlreadyAllocated
     */
    public PortAlreadyAllocated() {
        super("port already allocated");
    }

}
