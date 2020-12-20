package space.nebulark.junisockets;

public class Alias {
   ESignalingOperationCode opcode = ESignalingOperationCode.ALIAS;
   // Consider adding the optional variables
   private String id;
   private String alias;
   private boolean set;

   public Alias(String id, String alias, boolean set) {
        this.id = id;
        this.alias = alias;
        this.set = set;
   }

   public String getId() {
       return id;
   }

   public String getAlias() {
       return alias;
   }

   public boolean getSet() {
       return set;
   }
}
