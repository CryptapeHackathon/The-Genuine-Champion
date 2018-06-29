package org.nervos.neuron.remote.response;

import com.google.gson.annotations.SerializedName;

public class TransactionInfoResponse {

    @SerializedName("chain")
    public String chain;
    @SerializedName("expire")
    public int expire;
    @SerializedName("uuid")
    public String uuid;
    @SerializedName("transaction")
    public TransactionInfo transaction;

    public String error;

}
