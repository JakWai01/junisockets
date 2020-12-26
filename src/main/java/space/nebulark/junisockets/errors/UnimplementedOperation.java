package space.nebulark.junisockets.errors;

import space.nebulark.junisockets.operations.ESignalingOperationCode;

public class UnimplementedOperation extends Exception {
    
    public UnimplementedOperation(Object object) {
        super("unimplemented operation " + object);
    }

}
