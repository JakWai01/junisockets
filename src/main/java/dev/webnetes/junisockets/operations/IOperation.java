package dev.webnetes.junisockets.operations;

/**
 * Defines operation
 */
public interface IOperation {
   
    /**
     * Returns operation as JSON
     * @param operation operation
     * @return String
     */
    String getAsJSON(Object operation);

    /**
     * Returns opcode
     * @return String
     */
    String getOpCode();
}
