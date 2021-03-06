package dev.webnetes.junisockets.operations;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;

/**
 * Goodbye
 */
public class Goodbye implements IGoodbye {
   private ESignalingOperationCode opcode = ESignalingOperationCode.GOODBYE;
   private String id;

   /**
    * Constructor Goodbye
    * @param id id
    */
   public Goodbye(String id) {
        this.id = id;
   }

   
   /** 
    * Returns id
    * @return String
    */
   public String getId() {
       return id;
   }

   
   /** 
    * Returns operation as JSON. Warnings are suppressed because there are unavoidable ones when using json-simple in this case.
    * @param operationObject operation
    * @return String
    */
    @SuppressWarnings("unchecked")
   public String getAsJSON(Object operationObject) {

        Goodbye operation = (Goodbye) operationObject;

        JSONObject obj = new JSONObject();
        String jsonText;

        Map<Object, Object> m1 = new LinkedHashMap<Object, Object>();
        m1.put("id", operation.getId());

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
