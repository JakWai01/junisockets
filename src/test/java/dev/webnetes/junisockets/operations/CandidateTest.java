package dev.webnetes.junisockets.operations;

import org.junit.Assert;
import org.junit.Test;

/**
 * @see dev.webnetes.junisockets.operations.Candidate
 */
public class CandidateTest {
   
    /**
     * @see dev.webnetes.junisockets.operations.Candidate#getAsJSON()
     */
    @Test
    public void testGetAsJSON() {
        
        Candidate candidate = new Candidate("127.0.0.1", "127.0.0.2", "c1");

        Assert.assertEquals("{\"data\":{\"offererId\":\"127.0.0.1\",\"answererId\":\"127.0.0.2\",\"candidate\":\"c1\"},\"opcode\":\"candidate\"}", candidate.getAsJSON(candidate));
    }

}
