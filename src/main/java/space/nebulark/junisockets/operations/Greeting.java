package space.nebulark.junisockets.operations;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;

/**
 * Greeting
 */
public class Greeting implements IGreeting {
    public ESignalingOperationCode opcode = ESignalingOperationCode.GREETING;
    private String offererId;
    private String answererId;

    /**
     * Constructor Greeting
     * @param offererId
     * @param answererId
     */
    public Greeting(String offererId, String answererId) {
        this.offererId = offererId;
        this.answererId = answererId;
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
     * Returns operation as JSON
     * @param operationObject
     * @return String
     */
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

    
    /** 
     * Returns opcode
     * @return String
     */
    public String getOpCode() {
        return opcode.toString();
    }
}
