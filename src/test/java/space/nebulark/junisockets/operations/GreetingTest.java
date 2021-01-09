package dev.webnetes.junisockets.operations;

import org.junit.Assert;
import org.junit.Test;

/**
 * @see dev.webnetes.junisockets.operations.Greeting
 */
public class GreetingTest {
   
    /**
     * @see dev.webnetes.junisockets.operations.Greeting#getAsJSON()
     */
    @Test
    public void testGetAsJSON() {

        Greeting greeting = new Greeting("127.0.01", "127.0.0.2");

        Assert.assertEquals("{\"data\":{\"offererId\":\"127.0.01\",\"answererId\":\"127.0.0.2\"},\"opcode\":\"greeting\"}", greeting.getAsJSON(greeting));
    }

}
