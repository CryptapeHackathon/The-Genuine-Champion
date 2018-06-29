package org.nervos.neuron.remote.response;

import android.text.TextUtils;

import org.web3j.utils.Numeric;

import java.math.BigInteger;

public class TransactionInfo {

    private static final BigInteger TOKENDecimal = new BigInteger("1000000000000000000");

    /**
     * from : 0x627306090abaB3A6e1400e9345bC60c78a8BEf57
     * nonce : 100
     * quota : 100
     * data : 0x627306090abaB3A6e1400e9345bC60c78a8BEf57
     * value : 0
     * chainId : 1
     * version : 0
     */

    public String from;
    public String to;
    public long nonce;
    private long quota = -1;
    public String data;
    private String value;
    public long chainId;
    public int version;
    private String gasLimit;
    private String gasPrice;

    public TransactionInfo(String to, String value) {
        this.to = to;
        this.value = BigInteger.valueOf((long)(Double.parseDouble(value)*10000))
                .multiply(TOKENDecimal).divide(BigInteger.valueOf(10000)).toString();
    }

    public double getValue() {
        return new BigInteger(value).multiply(BigInteger.valueOf(10000))
                .divide(TOKENDecimal).doubleValue()/10000.0;
    }

    public double getQuota() {
        return BigInteger.valueOf(quota).multiply(BigInteger.valueOf(10000))
                .divide(TOKENDecimal).doubleValue()/10000.0;
    }

    public double getGas() {
        return Numeric.toBigInt(gasLimit).multiply(Numeric.toBigInt(gasPrice))
                .multiply(BigInteger.valueOf(10000))
                .divide(TOKENDecimal).doubleValue()/10000.0;
    }

    public String getGasLimit() {
        return gasLimit;
    }

    public String getGasPrice() {
        return gasPrice;
    }

    public void setGasLimit(String gasLimit) {
        this.gasLimit = gasLimit;
    }

    public void setQuota(long quota) {
        this.quota = quota;
    }

    public void setGasPrice(String gasPrice) {
        this.gasPrice = gasPrice;
    }

    public boolean isEthereum() {
        return !TextUtils.isEmpty(gasLimit);
    }
}
