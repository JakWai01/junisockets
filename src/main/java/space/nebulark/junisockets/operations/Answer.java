package space.nebulark.junisockets.operations;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;

public class Answer implements IAnswer {
    public ESignalingOperationCode opcode = ESignalingOperationCode.ANSWER;

    private String offererId;
    private String answererId;
    private String answer;

    public Answer(String offererId, String answererId, String answer) {
        this.offererId = offererId;
        this.answererId = answererId;
        this.answer = answer;
    }

    public String getOffererId() {
        return offererId;
    }

    public String getAnswererId() {
        return answererId;
    }

    public String getAnswer() {
        return answer;
    }

    public String getAsJSON(Object operationObject) {

        Answer operation = (Answer) operationObject;

        JSONObject obj = new JSONObject();
        String jsonText;

        Map<Object, Object> m1 = new LinkedHashMap<Object, Object>();
        m1.put("offererId", (String) operation.getOffererId());
        m1.put("answererId", operation.getAnswererId());
        m1.put("answer", operation.getAnswer());

        obj.put("data", m1);
        obj.put("opcode", operation.opcode.getValue());

        jsonText = obj.toString();

        return jsonText;
    }

    public String getOpCode() {
        return opcode.toString();
    }
}
