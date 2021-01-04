package space.nebulark.junisockets.operations;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;

/**
 * Candidate
 */
public class Candidate implements ICandidate {
    public ESignalingOperationCode opcode = ESignalingOperationCode.CANDIDATE;

    private String offererId;
    private String answererId;
    private String candidate;

    /**
     * Constructor Candidate
     * @param offererId offererId
     * @param answererId answererId
     * @param candidate candidate
     */
    public Candidate(String offererId, String answererId, String candidate) {
        this.offererId = offererId;
        this.answererId = answererId;
        this.candidate = candidate;
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
     * Returns candidate
     * @return String
     */
    public String getCandidate() {
        return candidate;
    }

    
    /** 
     * Returns operation as JSON. Warnings are suppressed because there are unavoidable ones when using json-simple in this case.
     * @param operationObject operation
     * @return String
     */
    @SuppressWarnings("unchecked")
    public String getAsJSON(Object operationObject) {

        Candidate operation = (Candidate) operationObject;

        JSONObject obj = new JSONObject();
        String jsonText;

        Map<Object, Object> m1 = new LinkedHashMap<Object, Object>();
        m1.put("offererId", (String) operation.getOffererId());
        m1.put("answererId", operation.getAnswererId());
        m1.put("candidate", operation.getCandidate());

        obj.put("data", m1);
        obj.put("opcode", operation.opcode.getValue());

        jsonText = obj.toString();
        ;

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
