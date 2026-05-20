package com.epam.gym.context;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TransactionContext {

    private static final ThreadLocal<String> transactionId = new ThreadLocal<>();

    public static void setTransactionId(String id) {
        transactionId.set(id);
    }

    public static String getTransactionId() {
        String id = transactionId.get();
        if (id == null) {
            id = generateTransactionId();
            transactionId.set(id);
        }
        return id;
    }

    public static void clear() {
        transactionId.remove();
    }

    private static String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8);
    }
}