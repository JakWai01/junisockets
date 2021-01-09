package dev.webnetes.junisockets.operations;

/**
 * Defines offer operation
 */
public interface IOffer extends IOperation {
   
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
     * Returns offer
     * @return String
     */
    String getOffer();

}
