package space.nebulark.junisockets.errors;

public class PortAlreadyAllocatedError extends Exception {
   
    /**
     *
     */
    private static final long serialVersionUID = -2877114095535702618L;

    public PortAlreadyAllocatedError() {
        super("port already allocated");
    }

}
