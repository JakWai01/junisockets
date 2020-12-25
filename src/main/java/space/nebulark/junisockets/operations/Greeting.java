package space.nebulark.junisockets.operations;

public class Greeting implements IGreeting {
   public ESignalingOperationCode opcode = ESignalingOperationCode.GREETING;
   private String offererId;
   private String answererId;
   
   public Greeting(String offererId, String answererId) {
        this.offererId = offererId;
        this.answererId = answererId;
   }

   public String getOffererId() {
       return offererId;
   }

   public String getAnswererId() {
       return answererId;
   }
}
