package space.nebulark.junisockets.operations;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;

public class Greeting implements IGreeting {
    public ESignalingOperationCode opcode = ESignalingOperationCode.GREETING;
    private String offererId;
    private String answererId;

    public Greeting(String offererId, String answererId) {
        this.offererId = offererId;
        this.answererId = answererId;
    }

    public String getOffererId() {
        return offererId;
    }

    public String getAnswererId() {
        return answererId;
    }

    public String getAsJSON(Object operationObject) {

        Greeting operation = (Greeting) operationObject;

        JSONObject obj = new JSONObject();
        String jsonText;

        Map<Object, Object> m1 = new LinkedHashMap<Object, Object>();
        m1.put("offererId", (String) operation.getOffererId());
        m1.put("answererId", operation.getAnswererId());

        obj.put("data", m1);
        obj.put("opcode", operation.opcode.getValue());

        jsonText = obj.toString();

        return jsonText;
    }
}
