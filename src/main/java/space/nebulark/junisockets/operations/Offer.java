package space.nebulark.junisockets.operations;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;

public class Offer implements IOffer {
    public ESignalingOperationCode opcode = ESignalingOperationCode.OFFER;

    private String offererId;
    private String answererId;
    private String offer;

    public Offer(String offererId, String answererId, String offer) {
        this.offererId = offererId;
        this.answererId = answererId;
        this.offer = offer;
    }

    public String getOffererId() {
        return offererId;
    }

    public String getAnswererId() {
        return answererId;
    }

    public String getOffer() {
        return offer;
    }

    public String getAsJSON(Object operationObject) {

        Offer operation = (Offer) operationObject;

        JSONObject obj = new JSONObject();
        String jsonText;

        Map<Object, Object> m1 = new LinkedHashMap<Object, Object>();
        m1.put("offererId", (String) operation.getOffererId());
        m1.put("answererId", operation.getAnswererId());
        m1.put("offer", operation.getOffer());

        obj.put("data", m1);
        obj.put("opcode", operation.opcode.getValue());

        jsonText = obj.toString();

        return jsonText;
    }

    public String getOpCode() {
        return opcode.toString();
    }
}
