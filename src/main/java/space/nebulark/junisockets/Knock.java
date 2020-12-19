package space.nebulark.junisockets;

public class Knock implements ISignalingOperation<IKnockData>{
    ESignalingOperationCode opcode = ESignalingOperationCode.KNOCK;

    IKnockData data;

    public Knock(IKnockData data) {
        this.data = data;
    }
}
