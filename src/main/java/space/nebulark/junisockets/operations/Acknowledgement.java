package space.nebulark.junisockets.operations;

public class Acknowledgement implements IAcknowledgement {
    public ESignalingOperationCode opcode = ESignalingOperationCode.ACKNOWLEDGED;

    private String id;
    private boolean rejected;

    public Acknowledgement(String id, boolean rejected) {
        this.id = id;
        this.rejected = rejected;
    } 

    public String getId() {
        return id;
    }

    public boolean getRejected() {
        return rejected;
    }
}
