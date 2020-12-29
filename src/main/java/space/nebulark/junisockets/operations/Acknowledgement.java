package space.nebulark.junisockets.operations;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;

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

    public String getAsJSON(Object operationObject) {

            Acknowledgement operation = (Acknowledgement)operationObject;
            
            JSONObject obj = new JSONObject();
            String jsonText;

            Map<Object, Object> m1 = new LinkedHashMap<Object, Object>();
            m1.put("id", (String) operation.getId());
            m1.put("rejected", operation.getRejected());

            obj.put("data", m1);
            obj.put("opcode", operation.opcode.getValue());

            jsonText = obj.toString();

            return jsonText;
    }
}
