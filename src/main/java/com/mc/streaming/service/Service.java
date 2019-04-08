package com.mc.streaming.service;

import com.mc.streaming.model.Transaction;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface Service {

    CompletionStage<Optional<String>> processTransaction(Transaction tx);

}
