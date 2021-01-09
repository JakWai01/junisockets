package dev.webnetes.junisockets.operations;

/**
 * Defines candidate operation
 */
public interface ICandidate extends IOperation {
   
    /**
     * Returns offererId
     * @return String
     */
    String getOffererId();

    /**
     * Returns answererId
     * @return String
     */
    String getAnswererId();

    /**
     * Returns candidate
     * @return String
     */
    String getCandidate();

}
