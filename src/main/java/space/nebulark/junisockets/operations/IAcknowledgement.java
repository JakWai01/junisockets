package space.nebulark.junisockets.operations;

public interface IAcknowledgement extends IOperation {
   
    public String getId();

    public boolean getRejected();
    
}
