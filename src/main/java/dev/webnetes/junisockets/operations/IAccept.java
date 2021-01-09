package dev.webnetes.junisockets.operations;

/**
 * Defines accept operation
 */
public interface IAccept extends IOperation {
   
    /**
     * Returns boundAlias
     * @return String
     */
    String getBoundAlias();
    
    /**
     * Returns clientAlias
     * @return String
     */
    String getClientAlias();

}
