package space.nebulark.junisockets.operations;

/**
 * Defines alias operation
 */
public interface IAlias extends IOperation {
   
    /**
     * Returns id
     * @return String
     */
    String getId();

    /**
     * Returns alias
     * @return String
     */
    String getAlias();

    /**
     * Returns clientConnectionId
     * @return String
     */
    String getClientConnectionId();

    /**
     * Returns set
     * @return boolean
     */
    boolean getSet();

    /**
     * Returns isConnectionAlias
     * @return boolean
     */
    boolean getIsConnectionAlias();

}
