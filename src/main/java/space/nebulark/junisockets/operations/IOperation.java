package space.nebulark.junisockets.operations;

/**
 * Defines operation
 */
public interface IOperation {
   
    /**
     * Returns operation as JSON
     * @param operation
     * @return
     */
    String getAsJSON(Object operation);

    /**
     * Returns opcode
     * @return
     */
    String getOpCode();
}
