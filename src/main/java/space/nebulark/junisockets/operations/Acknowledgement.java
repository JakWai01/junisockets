package space.nebulark.junisockets.operations;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;

/**
 * Acknowledgement
 */
public class Acknowledgement implements IAcknowledgement {
    public ESignalingOperationCode opcode = ESignalingOperationCode.ACKNOWLEDGED;

    private String id;
    private boolean rejected;

    /**
     * Constructor Acknowledgement
     * @param id id
     * @param rejected rejected
     */
    public Acknowledgement(String id, boolean rejected) {
        this.id = id;
        this.rejected = rejected;
    } 

    
    /** 
     * Returns id
     * @return String
     */
    public String getId() {
        return id;
    }

    
    /** 
     * Returns rejected
     * @return boolean
     */
    public boolean getRejected() {
        return rejected;
    }

    
    /** 
     * Returns operation as JSON
     * @param operationObject operation
     * @return String
     */
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

    
    /** 
     * Returns opcode
     * @return String
     */
    public String getOpCode() {
        return opcode.toString();
    }
}
