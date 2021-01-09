package dev.webnetes.junisockets.operations;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;

/**
 * Answer
 */
public class Answer implements IAnswer {
    private ESignalingOperationCode opcode = ESignalingOperationCode.ANSWER;
    private String offererId;
    private String answererId;
    private String answer;

    /**
     * Constructor Answer
     * @param offererId offererId
     * @param answererId answererId
     * @param answer answer
     */
    public Answer(String offererId, String answererId, String answer) {
        this.offererId = offererId;
        this.answererId = answererId;
        this.answer = answer;
    }

    
    /** 
     * Returns offererId
     * @return String
     */
    public String getOffererId() {
        return offererId;
    }

    
    /** 
     * Returns answererId
     * @return String
     */
    public String getAnswererId() {
        return answererId;
    }

    
    /** 
     * Returns answer
     * @return String
     */
    public String getAnswer() {
        return answer;
    }

    
    /** 
     * Returns operation as JSON. Warnings are suppressed because there are unavoidable ones when using json-simple in this case.
     * @param operationObject operation
     * @return String
     */
    @SuppressWarnings("unchecked")
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

    
    /** 
     * Returns opcode
     * @return String
     */
    public String getOpCode() {
        return opcode.toString();
    }
}
