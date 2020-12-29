package space.nebulark.junisockets.operations;

public interface IAlias extends IOperation {
   
    public String getId();

    public String getAlias();

    public String getClientConnectionId();

    public boolean getSet();

    public boolean getIsConnectionAlias();

}
