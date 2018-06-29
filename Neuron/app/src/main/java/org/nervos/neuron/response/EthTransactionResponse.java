package org.nervos.neuron.response;

import org.nervos.neuron.item.TransactionItem;
import org.web3j.protocol.core.methods.request.Transaction;

import java.util.List;

public class EthTransactionResponse {

    public String status;
    public String message;
    public List<TransactionItem> result;

}
