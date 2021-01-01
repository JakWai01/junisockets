package space.nebulark.junisockets.operations;

import space.nebulark.junisockets.errors.UnimplementedOperation;

public class OperationFactory {

    // der else kann nicht eintreten
    public Object getOperation(ESignalingOperationCode opcode, Object operation) throws UnimplementedOperation {

        if (opcode.getValue().equals(ESignalingOperationCode.ACKNOWLEDGED.getValue())) {
            return (Acknowledgement)operation;
        } else if (opcode.getValue().equals(ESignalingOperationCode.GREETING.getValue())) {
            return (Greeting)operation;
        } else if (opcode.getValue().equals(ESignalingOperationCode.OFFER.getValue())) {
            return (Offer)operation;
        } else if (opcode.getValue().equals(ESignalingOperationCode.ANSWER.getValue())) {
            return (Answer)operation;
        } else if (opcode.getValue().equals(ESignalingOperationCode.CANDIDATE.getValue())) {
            return (Candidate)operation;
        } else if (opcode.getValue().equals(ESignalingOperationCode.ALIAS.getValue())) {
            return (Alias)operation;
        } else if (opcode.getValue().equals(ESignalingOperationCode.ACCEPT.getValue())) {
            return (Accept)operation;
        } else if (opcode.getValue().equals(ESignalingOperationCode.GOODBYE.getValue())) {
            return (Goodbye)operation;
        } else {
            throw new UnimplementedOperation(opcode.getValue());
        }
    }
}
