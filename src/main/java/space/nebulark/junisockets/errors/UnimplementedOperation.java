package space.nebulark.junisockets.errors;

import space.nebulark.junisockets.operations.ESignalingOperationCode;

public class UnimplementedOperation extends Exception {
    
    public UnimplementedOperation(ESignalingOperationCode opcode) {
        super("unimplemented operation " + opcode);
    }

}
