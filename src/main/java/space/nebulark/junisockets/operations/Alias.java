package space.nebulark.junisockets.operations;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;

public class Alias implements IAlias {
    public ESignalingOperationCode opcode = ESignalingOperationCode.ALIAS;
    private String id;
    private String alias;
    private String clientConnectionId;
    private boolean set;
    private boolean isConnectionAlias;

    public Alias(String id, String alias, boolean set) {
        this.id = id;
        this.alias = alias;
        this.set = set;
    }

    public Alias(String id, String alias, boolean set, String clientConnectionId) {
        this.id = id;
        this.alias = alias;
        this.set = set;
        this.clientConnectionId = clientConnectionId;
    }

    public Alias(String id, String alias, boolean set, String clientConnectionId, boolean isConnectionAlias) {
        this.id = id;
        this.alias = alias;
        this.set = set;
        this.clientConnectionId = clientConnectionId;
        this.isConnectionAlias = isConnectionAlias;
    }

    public String getId() {
        return id;
    }

    public String getAlias() {
        return alias;
    }

    public String getClientConnectionId() {
        return clientConnectionId;
    }

    public boolean getSet() {
        return set;
    }

    public boolean getIsConnectionAlias() {
        return isConnectionAlias;
    }

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

    public String getOpCode() {
        return opcode.toString();
    }
}
