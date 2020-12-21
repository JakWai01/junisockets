package space.nebulark.junisockets;

public class Goodbye {
   ESignalingOperationCode opcode = ESignalingOperationCode.GOODBYE;
   
   private String id;

   public Goodbye(String id) {
        this.id = id;
   }

   public String getId() {
       return id;
   }
}
