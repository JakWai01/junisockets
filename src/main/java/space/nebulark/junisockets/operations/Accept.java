package space.nebulark.junisockets.operations;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;

public class Accept implements IAccept {
    public ESignalingOperationCode opcode = ESignalingOperationCode.ACCEPT;

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

    public String getAsJSON(Object operationObject) {

        Accept operation = (Accept)operationObject;

        JSONObject obj = new JSONObject();
        String jsonText;

        Map<Object, Object> m1 = new LinkedHashMap<Object, Object>();
        m1.put("boundAlias", (String) operation.getBoundAlias());
        m1.put("clientAlias", operation.getClientAlias());

        obj.put("data", m1);
        obj.put("opcode", operation.opcode.getValue());

        jsonText = obj.toString();

        return jsonText;
    }
}
