package space.nebulark.junisockets.operations;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;

/**
 * Alias
 */
public class Alias implements IAlias {
    public ESignalingOperationCode opcode = ESignalingOperationCode.ALIAS;
    private String id;
    private String alias;
    private String clientConnectionId;
    private boolean set;
    private boolean isConnectionAlias;

    /**
     * Constructor Alias
     * @param id
     * @param alias
     * @param set
     */
    public Alias(String id, String alias, boolean set) {
        this.id = id;
        this.alias = alias;
        this.set = set;
    }

    /**
     * Constructor Alias
     * @param id
     * @param alias
     * @param set
     * @param clientConnectionId
     */
    public Alias(String id, String alias, boolean set, String clientConnectionId) {
        this.id = id;
        this.alias = alias;
        this.set = set;
        this.clientConnectionId = clientConnectionId;
    }

    /**
     * Constructor Alias
     */
    public Alias(String id, String alias, boolean set, String clientConnectionId, boolean isConnectionAlias) {
        this.id = id;
        this.alias = alias;
        this.set = set;
        this.clientConnectionId = clientConnectionId;
        this.isConnectionAlias = isConnectionAlias;
    }

    
    /** 
     * Returns id
     * @return String
     */
    public String getId() {
        return id;
    }

    
    /** 
     * Returns alias
     * @return String
     */
    public String getAlias() {
        return alias;
    }

    
    /** 
     * Returns clientConnectionId
     * @return String
     */
    public String getClientConnectionId() {
        return clientConnectionId;
    }

    
    /** 
     * Returns set
     * @return boolean
     */
    public boolean getSet() {
        return set;
    }

    
    /** 
     * Returns isConnectionAlias
     * @return boolean
     */
    public boolean getIsConnectionAlias() {
        return isConnectionAlias;
    }

    
    /** 
     * Returns operation as JSON
     * @param operationObject
     * @return String
     */
    public String getAsJSON(Object operationObject) {

        Alias operation = (Alias) operationObject;

        JSONObject obj = new JSONObject();
        String jsonText;

        Map<Object, Object> m1 = new LinkedHashMap<Object, Object>();
        m1.put("id", (String) operation.getId());
        m1.put("alias", operation.getAlias());
        m1.put("set", operation.getSet());

        if (operation.getClientConnectionId() != null) {
            m1.put("clientConnectionId", operation.getClientConnectionId());
        }

        if (operation.getIsConnectionAlias()) {
            m1.put("isConnectionAlias", operation.getIsConnectionAlias());
        }

        obj.put("data", m1);
        obj.put("opcode", operation.opcode.getValue());

        jsonText = obj.toString();

        return jsonText;
    }

    
    /** 
     * Returns opcode
     * @return String
     */
    public String getOpCode() {
        return opcode.toString();
    }
}
