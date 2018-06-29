package org.nervos.neuron.remote.request;

import org.nervos.neuron.remote.response.TransactionInfo;

public class TransactionInfoRequest {

    public String chain;
    public int expire;
    public TransactionInfo transaction;

}
