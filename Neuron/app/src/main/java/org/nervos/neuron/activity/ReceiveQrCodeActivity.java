package org.nervos.neuron.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import org.nervos.neuron.R;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.remote.response.TransactionInfo;
import org.nervos.neuron.service.EthRpcService;
import org.nervos.neuron.util.Blockies;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.db.DBWalletUtil;

import java.math.BigInteger;

import de.hdodenhof.circleimageview.CircleImageView;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ReceiveQrCodeActivity extends BaseActivity {

    private static final int REQUEST_CODE = 0x01;

    private ImageView qrCodeImage;
    private WalletItem walletItem;
    private TextView amountText;
    private TransactionInfo transactionInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_qrcode);

        walletItem = DBWalletUtil.getCurrentWallet(mActivity);
        amountText = findViewById(R.id.receive_qrcode_amount);

        qrCodeImage = findViewById(R.id.receive_qrcode_image);
        TextView walletNameText = findViewById(R.id.qrcode_wallet_name);
        TextView walletAddressText = findViewById(R.id.qrcode_wallet_address);
        CircleImageView walletPhotoImage = findViewById(R.id.wallet_photo);
        walletPhotoImage.setImageBitmap(Blockies.createIcon(walletItem.address));
        findViewById(R.id.receive_qrcode_set_value).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(mActivity, SetAmountActivity.class), REQUEST_CODE);
            }
        });
        findViewById(R.id.receive_qrcode_copy_address).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("qrCode", walletItem.address);
                if (cm != null) {
                    cm.setPrimaryClip(mClipData);
                    Toast.makeText(mActivity, R.string.copy_success, Toast.LENGTH_SHORT).show();
                }
            }
        });

        walletNameText.setText(walletItem.name);
        walletAddressText.setText(walletItem.address);

        setQrCodeImage(walletItem.address);

    }

    private void setQrCodeImage(String value) {
        Bitmap bitmap = CodeUtils.createImage(value, 400, 400, null);
        qrCodeImage.setImageBitmap(bitmap);
    }

    private void handleEthTransaction() {
        transactionInfo.setGasLimit(ConstUtil.GAS_LIMIT.toString());
        showProgressCircle();
        EthRpcService.getEthGasPrice()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<BigInteger>() {
                @Override
                public void onCompleted() {
                    dismissProgressCircle();
                }
                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                    dismissProgressBar();
                }
                @Override
                public void onNext(BigInteger gasPrice) {
                    transactionInfo.setGasPrice(gasPrice.toString());
                    setQrCodeImage(new Gson().toJson(transactionInfo));
                }
            });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == SetAmountActivity.RESULT_CODE) {
            String value = data.getStringExtra(SetAmountActivity.EXTRA_VALUE);
            String category = data.getStringExtra(SetAmountActivity.EXTRA_CATEGORY);
            amountText.setText(String.format("%s %s", value, category));
            transactionInfo = new TransactionInfo(walletItem.address, value);
            if (ConstUtil.ETH.equalsIgnoreCase(category)) {
                handleEthTransaction();
            } else {
                transactionInfo.setQuota(ConstUtil.DEFAULT_QUATO);
                setQrCodeImage(new Gson().toJson(transactionInfo));
            }
        }
    }
}
