package space.nebulark.junisockets.operations;

public enum ESignalingOperationCode { 
    GOODBYE("goodbye"),
    KNOCK("knock"),
    ACKNOWLEDGED("acknowledgement"),
    GREETING("greeting"),
    OFFER("offer"),
    ANSWER("answer"),
    CANDIDATE("candidate"),
    BIND("bind"),
    ACCEPTING("accepting"),
    ALIAS("alias"),
    SHUTDOWN("shutdown"),
    CONNECT("connect"),
    ACCEPT("accept");

    private String value;

    private ESignalingOperationCode(String value) {
        this.value = value;
    }    

    public String getValue() {
        return value;
    }
}