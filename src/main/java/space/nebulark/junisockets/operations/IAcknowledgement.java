package space.nebulark.junisockets.operations;

public interface IAcknowledgement extends IOperation {
   
    String getId();

    boolean getRejected();
    
}
