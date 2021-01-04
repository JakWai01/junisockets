package space.nebulark.junisockets.operations;

/**
 * Defines acknowledgement operation
 */
public interface IAcknowledgement extends IOperation {
    
    /**
     * Returns id
     * @return String
     */
    String getId();

    /**
     * Returns rejected
     * @return boolean
     */
    boolean getRejected();
    
}
