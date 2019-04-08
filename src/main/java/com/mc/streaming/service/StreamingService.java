package com.mc.streaming.service;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Framing;
import akka.stream.javadsl.FramingTruncation;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.StreamConverters;
import akka.util.ByteString;
import com.mc.streaming.model.Transaction;
import com.mc.streaming.model.TransactionResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static akka.stream.javadsl.Flow.of;

public class StreamingService implements Service {

    private final ActorSystem system;

    private final static int PARALLEL_PROCESSING_THREADS = 4;
    private final static int PARALLEL_TX_RESULT_WRITER_THREADS = 2;


    public StreamingService(ActorSystem system) {
        this.system = system;
    }

    public static void main(String[] args) {

        final ActorSystem system = ActorSystem.create();

        // load input data file (located in the src/main/resources folder)
        final String testDataFileName = "input_data.txt";
        final ClassLoader classLoader = StreamingService.class.getClassLoader();
        final File file = new File(classLoader.getResource(testDataFileName).getFile());

        new StreamingService(system)
                .loadDataFromFile(file)
                .thenAccept(d -> system.terminate());
    }

    public CompletionStage<Done> loadDataFromFile(File file) {

        long start = System.currentTimeMillis();
        System.out.println("Stream processing started.");

        return Source.single(file)
                .via(processSingleFile())
                .runWith(processTransactionResults(), ActorMaterializer.create(system))
                .whenComplete((done, ex) -> {

                    if (done != null) {

                        long elapsed = System.currentTimeMillis() - start;
                        System.out.println("Stream processing completed - " + elapsed + "ms");

                    } else {

                        System.out.println("Stream processing FAILED due to exception: " + ex.toString());
                    }

                });

    }

    @Override
    public CompletionStage<Optional<String>> processTransaction(Transaction tx) {

        return CompletableFuture.supplyAsync(() -> {

            try {

                int value = Integer.parseInt(tx.getTransactionId());

                if (value < 0) {
                    String errorMsg = String.format("Invalid input: Negative value (%s) for 'TransactionId' field is NOT allowed.", value);
                    return Optional.of(errorMsg); // business logic error.
                }

                return Optional.empty();    // success

            } catch (Exception ex) {

                return Optional.of(ex.toString());  // return exception message
            }

        });

    }

    private Flow<File, TransactionResult, NotUsed> processSingleFile() {

        return Flow.of(File.class)
                .via(parseInputFile())
                .via(processTransaction());

    }

    private Flow<File, Transaction, NotUsed> parseInputFile() {

        return Flow.of(File.class).flatMapConcat(file -> {

            InputStream inputStream = new FileInputStream(file);

            return StreamConverters.fromInputStream(() -> inputStream)
                    .via(Framing.delimiter(ByteString.fromString("\n"), 128, FramingTruncation.ALLOW))
                    .map(ByteString::utf8String)
                    .mapAsync(PARALLEL_PROCESSING_THREADS, this::processLine);
        });

    }

    private CompletionStage<Transaction> processLine(String line) {

        return CompletableFuture.supplyAsync(() -> new Transaction(line));

    }

    private Flow<Transaction,  TransactionResult, NotUsed> processTransaction() {

        return of(Transaction.class)
                .mapAsyncUnordered(PARALLEL_PROCESSING_THREADS, tx -> processTransaction(tx))
                .mapAsync(PARALLEL_PROCESSING_THREADS, this::mapOptionalToTransactionResult);

    }

    private CompletionStage<TransactionResult> mapOptionalToTransactionResult(Optional<String> txResult) {

        return CompletableFuture.supplyAsync(() -> {

            if (txResult.isPresent()) {
                return new TransactionResult(txResult.get(), true);
            }

            return new TransactionResult();
        });
    }

    private Sink<TransactionResult, CompletionStage<Done>> processTransactionResults() {

        return Flow.of(TransactionResult.class)
                .mapAsyncUnordered(PARALLEL_TX_RESULT_WRITER_THREADS, this::outputTransactionResults)
                .toMat(Sink.ignore(), Keep.right());

    }

    private CompletionStage<TransactionResult> outputTransactionResults(TransactionResult txResult) {

        return CompletableFuture.supplyAsync(() -> {

            System.out.println(txResult);

            return txResult;

        });

    }
}
