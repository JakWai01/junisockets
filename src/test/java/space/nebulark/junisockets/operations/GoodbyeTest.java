package space.nebulark.junisockets.operations;

import org.junit.Assert;
import org.junit.Test;

public class GoodbyeTest {
   
    @Test
    public void testGetAsJSON() {
        
        Goodbye goodbye = new Goodbye("127.0.0.2");

        Assert.assertEquals("{\"data\":{\"id\":\"127.0.0.2\"},\"opcode\":\"goodbye\"}", goodbye.getAsJSON(goodbye));
    }
}
