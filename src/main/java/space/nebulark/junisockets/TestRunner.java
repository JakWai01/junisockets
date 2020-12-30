package space.nebulark.junisockets;

import org.apache.log4j.BasicConfigurator;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import space.nebulark.junisockets.addresses.IPAddressTest;

public class TestRunner {
   
    public static void main(String[] args) {
        BasicConfigurator.configure();
        Result result = JUnitCore.runClasses(IPAddressTest.class);

        for(Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }

        System.out.println(result.wasSuccessful());
    }
}
