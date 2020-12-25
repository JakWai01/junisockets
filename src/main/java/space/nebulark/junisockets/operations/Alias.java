package space.nebulark.junisockets.operations;

public class Alias implements IAlias {
   public ESignalingOperationCode opcode = ESignalingOperationCode.ALIAS;
   private String id;
   private String alias;
   private String clientConnectionId;
   private boolean set;
   private boolean isConnectionAlias;

   public Alias(String id, String alias, boolean set) {
        this.id = id;
        this.alias = alias;
        this.set = set;
   }

   public Alias(String id, String alias, boolean set, String clientConnectionId) {
       this.id = id;
       this.alias = alias;
       this.set = set;
       this.clientConnectionId = clientConnectionId;
   }

    public Alias(String id, String alias, boolean set, String clientConnectionId, boolean isConnectionAlias) {
       this.id = id;
       this.alias = alias;
       this.set = set;
       this.clientConnectionId = clientConnectionId;
       this.isConnectionAlias = isConnectionAlias;
   }

   public String getId() {
       return id;
   }

   public String getAlias() {
       return alias;
   }

   public String getClientConnectionId() {
       return clientConnectionId;
   }

   public boolean getSet() {
       return set;
   }

   public boolean getIsConnectionAlias() {
       return isConnectionAlias;
   }

}
