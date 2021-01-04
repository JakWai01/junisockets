package space.nebulark.junisockets.operations;

import org.junit.Assert;
import org.junit.Test;

/**
 * @see space.nebulark.junisockets.operations.Greeting
 */
public class GreetingTest {
   
    /**
     * @see space.nebulark.junisockets.operations.Greeting#getAsJSON()
     */
    @Test
    public void testGetAsJSON() {

        Greeting greeting = new Greeting("127.0.01", "127.0.0.2");

        Assert.assertEquals("{\"data\":{\"offererId\":\"127.0.01\",\"answererId\":\"127.0.0.2\"},\"opcode\":\"greeting\"}", greeting.getAsJSON(greeting));
    }

}
