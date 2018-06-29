package org.nervos.neuron.util;

import android.text.TextUtils;

import org.web3j.utils.Numeric;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.DecimalFormat;

import static org.nervos.neuron.util.ConstUtil.ETHDecimal;

public class NumberUtil {

    public static String getDecimal_6(Double value) {
        DecimalFormat fmt = new DecimalFormat("0.######");
        return fmt.format(value);
    }

    public static String getDecimal_4(Double value) {
        DecimalFormat fmt = new DecimalFormat("0.####");
        return fmt.format(value);
    }

    public static String getDecimal_2(Double value) {
        DecimalFormat fmt = new DecimalFormat("0.##");
        return fmt.format(value);
    }

    public static String hexToUtf8(String hex) {
        hex = Numeric.cleanHexPrefix(hex);
        ByteBuffer buff = ByteBuffer.allocate(hex.length()/2);
        for (int i = 0; i < hex.length(); i+=2) {
            buff.put((byte)Integer.parseInt(hex.substring(i, i+2), 16));
        }
        buff.rewind();
        Charset cs = Charset.forName("UTF-8");
        CharBuffer cb = cs.decode(buff);
        return cb.toString();
    }

    public static String utf8ToHex(String value) {
        byte[] bytes = new byte[0];
        try {
            bytes = value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        for (byte x: bytes) {
            sb.append(Integer.toHexString(x & 0xFF));
        }
        return sb.toString();
    }


    public static BigInteger getWeiFromEth(double value) {
        return BigInteger.valueOf((int)(value * ConstUtil.LONG_6)).multiply(ConstUtil.ETHDecimal)
                .divide(BigInteger.valueOf(ConstUtil.LONG_6));
    }

    public static String getEthFromWeiForStringDecimal6(String value) {
        return getDecimal_6(getEthFromWeiForDoubleDecimal6(value));
    }

    public static String getEthFromWeiForStringDecimal6(BigInteger value) {
        return getDecimal_6(getEthFromWei(value));
    }

    public static double getEthFromWeiForDoubleDecimal6(String value) {
        if (TextUtils.isEmpty(value)) return 0.0;
        if (Numeric.containsHexPrefix(value)) {
            return getEthFromWei(Numeric.toBigInt(value));
        } else {
            return getEthFromWei(new BigInteger(value));
        }
    }

    public static double getEthFromWei(BigInteger value) {
        return value.multiply(BigInteger.valueOf(ConstUtil.LONG_6))
                .divide(ETHDecimal).doubleValue()/ConstUtil.DOUBLE_6;
    }


    public static boolean isPasswordOk(String password) {
        int len = password.length();
        if (len < 8) return false;
        int flag = 0;
        for (int i = 0; i < len; i++) {
            char c = password.charAt(i);
            if (c >= 'a' & c <= 'z') {
                flag |= 0b0001;
            } else if (c >= 'A' & c <= 'Z') {
                flag |= 0b0010;
            } else if (c >= '0' & c <= '9') {
                flag |= 0b0100;
            } else if ((c >= '!' & c <= '/') || (c >= ':' & c <= '@')
                    || (c >= '[' & c <= '`') || (c >= '{' & c <= '~')) {
                flag |= 0b1000;
            } else {
                return false;
            }
        }
        return Integer.bitCount(flag) >= 3;
    }

}
