package space.nebulark.junisockets.operations;

public class Goodbye implements IGoodbye {
   public ESignalingOperationCode opcode = ESignalingOperationCode.GOODBYE;
   
   private String id;

   public Goodbye(String id) {
        this.id = id;
   }

   public String getId() {
       return id;
   }
}
