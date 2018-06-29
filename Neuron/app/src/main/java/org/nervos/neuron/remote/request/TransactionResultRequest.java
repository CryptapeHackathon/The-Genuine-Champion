package org.nervos.neuron.remote.request;

public class TransactionResultRequest {

    public String status;
    public String error;
    public TransactionResult transaction;

    public class TransactionResult {
        public String hash;
    }

}
