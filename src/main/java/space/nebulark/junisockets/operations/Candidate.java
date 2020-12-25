package space.nebulark.junisockets.operations;

public class Candidate {
   public ESignalingOperationCode opcode = ESignalingOperationCode.CANDIDATE;
   
   private String offererId;
   private String answererId;
   private String candidate;

   public Candidate(String offererId, String answererId, String candidate) {
       this.offererId = offererId;
       this.answererId = answererId;
       this.candidate = candidate;
   }

   public String getOffererId() {
       return offererId;
   }

   public String getAnswererId() {
       return answererId;
   }

   public String getCandidate() {
       return candidate;
   }
}
