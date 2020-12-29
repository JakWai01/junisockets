package space.nebulark.junisockets.operations;

public interface IAnswer extends IOperation {
   
    String getOffererId();

    String getAnswererId();

    String getAnswer();

}
