package space.nebulark.junisockets.errors;

public class PortAlreadyAllocatedError extends Exception {
   
    public PortAlreadyAllocatedError() {
        super("port already allocated");
    }

}
