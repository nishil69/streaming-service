package com.mc.streaming.service;

import akka.actor.ActorSystem;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

public class StreamingServiceTest {

    // collaborator
    private static ActorSystem actorSystem;

    // service to be tested
    private static StreamingService underTest;

    @BeforeClass
    public static void setUp() {

        actorSystem = ActorSystem.create();
        underTest = new StreamingService(actorSystem);

    }

    @AfterClass
    public static void tearDown() {

        actorSystem.terminate();

    }

    @Test
    public void givenAllValidInput_ShouldFinishAndContainNoErrorMessages() {

        final String fileName = "all_valid_input.txt";
        final ClassLoader classLoader = getClass().getClassLoader();
        final File file = new File(classLoader.getResource(fileName).getFile());

        underTest.loadDataFromFile(file)
                .thenAccept(done -> actorSystem.terminate());
    }

    @Test
    public void givenAllSomeInvalidInput_ShouldFinishAndContainErrorMessages() {

        final String fileName = "invalid_business_case_input.txt";
        final ClassLoader classLoader = getClass().getClassLoader();
        final File file = new File(classLoader.getResource(fileName).getFile());

        underTest.loadDataFromFile(file)
                .thenAccept(done -> actorSystem.terminate());
    }

    @Test
    public void givenSomeDataWhichCauseErrors_ShouldFinishAndContainErrorAndExceptionMessages() {

        final String fileName = "exception_causing_input.txt";
        final ClassLoader classLoader = getClass().getClassLoader();
        final File file = new File(classLoader.getResource(fileName).getFile());

        underTest.loadDataFromFile(file)
                .thenAccept(done -> actorSystem.terminate());
    }

}
