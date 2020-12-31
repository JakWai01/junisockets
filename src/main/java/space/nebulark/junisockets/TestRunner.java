package space.nebulark.junisockets;

import org.apache.log4j.BasicConfigurator;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import space.nebulark.junisockets.addresses.IPAddressTest;
import space.nebulark.junisockets.addresses.TCPAddressTest;
import space.nebulark.junisockets.operations.AcceptTest;
import space.nebulark.junisockets.operations.AcknowledgementTest;
import space.nebulark.junisockets.operations.AliasTest;
import space.nebulark.junisockets.operations.AnswerTest;
import space.nebulark.junisockets.operations.CandidateTest;
import space.nebulark.junisockets.operations.GoodbyeTest;
import space.nebulark.junisockets.operations.GreetingTest;
import space.nebulark.junisockets.operations.OfferTest;

public class TestRunner {
   
    public static void main(String[] args) {
        BasicConfigurator.configure();
        Result result = JUnitCore.runClasses(IPAddressTest.class);
        Result result2 = JUnitCore.runClasses(TCPAddressTest.class);
        Result result3 = JUnitCore.runClasses(AcceptTest.class);
        Result result4 = JUnitCore.runClasses(AcknowledgementTest.class);
        Result result5 = JUnitCore.runClasses(AliasTest.class);
        Result result6 = JUnitCore.runClasses(AnswerTest.class);
        Result result7 = JUnitCore.runClasses(CandidateTest.class);
        Result result8 = JUnitCore.runClasses(GoodbyeTest.class);
        Result result9 = JUnitCore.runClasses(GreetingTest.class);
        Result result10 = JUnitCore.runClasses(OfferTest.class);

        for(Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }

        for (Failure failure : result2.getFailures()) {
            System.out.println(failure.toString());
        }

        for (Failure failure : result3.getFailures()) {
            System.out.println(failure.toString());
        }

        for (Failure failure : result4.getFailures()) {
            System.out.println(failure.toString());
        }

        for (Failure failure : result5.getFailures()) {
            System.out.println(failure.toString());
        }

        for (Failure failure : result6.getFailures()) {
            System.out.println(failure.toString());
        }

        for (Failure failure : result7.getFailures()) {
            System.out.println(failure.toString());
        }

        for (Failure failure : result8.getFailures()) {
            System.out.println(failure.toString());
        }

        for (Failure failure : result9.getFailures()) {
            System.out.println(failure.toString());
        }

        for (Failure failure : result10.getFailures()) {
            System.out.println(failure.toString());
        }

        System.out.println(result.wasSuccessful());
        System.out.println(result2.wasSuccessful());
        System.out.println(result3.wasSuccessful());
        System.out.println(result4.wasSuccessful());
        System.out.println(result5.wasSuccessful());
        System.out.println(result6.wasSuccessful());
        System.out.println(result7.wasSuccessful());
        System.out.println(result8.wasSuccessful());
        System.out.println(result9.wasSuccessful());
        System.out.println(result10.wasSuccessful());
    }
}
