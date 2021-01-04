package space.nebulark.junisockets.operations;

/**
 * ESignalingOperationCode
 */
public enum ESignalingOperationCode { 
    GOODBYE("goodbye"),
    KNOCK("knock"),
    ACKNOWLEDGED("acknowledged"),
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

    /**
     * Constructor ESignalingOperationCode
     * @param value
     */
    private ESignalingOperationCode(String value) {
        this.value = value;
    }    

    /**
     * Returns value
     * @return String
     */
    public String getValue() {
        return value;
    }
}