package space.nebulark.junisockets;

public class Accept {
    ESignalingOperationCode opcode = ESignalingOperationCode.ACCEPT;

    private String boundAlias;
    private String clientAlias;

    public Accept(String boundAlias, String clientAlias) {
        this.boundAlias = boundAlias;
        this.clientAlias = clientAlias;
    }

    public String getBoundAlias() {
        return boundAlias;
    }

    public String getClientAlias() {
        return clientAlias;
    }
}
