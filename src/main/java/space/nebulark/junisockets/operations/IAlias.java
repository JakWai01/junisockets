package space.nebulark.junisockets.operations;

public interface IAlias extends IOperation {
   
    String getId();

    String getAlias();

    String getClientConnectionId();

    boolean getSet();

    boolean getIsConnectionAlias();

}
