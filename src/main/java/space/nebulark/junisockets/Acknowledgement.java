package space.nebulark.junisockets;

public class Acknowledgement {
    ESignalingOperationCode opcode = ESignalingOperationCode.ACKNOWLEDGED;

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
