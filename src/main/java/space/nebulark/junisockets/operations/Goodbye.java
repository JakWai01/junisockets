package space.nebulark.junisockets.operations;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;

public class Goodbye implements IGoodbye {
   public ESignalingOperationCode opcode = ESignalingOperationCode.GOODBYE;
   
   private String id;

   public Goodbye(String id) {
        this.id = id;
   }

   public String getId() {
       return id;
   }

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
}
