package space.nebulark.junisockets.operations;

import org.junit.Assert;
import org.junit.Test;

/**
 * @see space.nebulark.junisockets.operations.Answer
 */
public class AnswerTest {
    
    /**
     * @see space.nebulark.junisockets.operations.Answer#getAsJSON()
     */
    @Test
    public void testGetAsJSON() {
        Answer answer = new Answer("127.0.0.1", "127.0.0.2", "a1");

        Assert.assertEquals("{\"data\":{\"offererId\":\"127.0.0.1\",\"answererId\":\"127.0.0.2\",\"answer\":\"a1\"},\"opcode\":\"answer\"}", answer.getAsJSON(answer));
    }
    
}
