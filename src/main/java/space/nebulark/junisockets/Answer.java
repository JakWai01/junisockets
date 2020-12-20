package space.nebulark.junisockets;

public class Answer {
   ESignalingOperationCode opcode = ESignalingOperationCode.ANSWER;

   private String offererId;
   private String answererId; 
   private String answer;
   
   public Answer(String offererId, String answererId, String answer) { 
        this.offererId = offererId;
        this.answererId = answererId;
        this.answer = answer;
   }

   public String getOffererId() {
        return offererId;
   }

   public String getAnswererId() {
       return answererId;
   }

   public String getAnswer() {
       return answer;
   }
    
}
