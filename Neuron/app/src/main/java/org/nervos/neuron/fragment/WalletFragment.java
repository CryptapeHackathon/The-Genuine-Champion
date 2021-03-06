package org.nervos.neuron.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.nervos.neuron.R;
import org.nervos.neuron.activity.AddWalletActivity;
import org.nervos.neuron.activity.PayTokenActivity;
import org.nervos.neuron.activity.QrCodeActivity;
import org.nervos.neuron.activity.ReceiveQrCodeActivity;
import org.nervos.neuron.activity.TransferActivity;
import org.nervos.neuron.activity.WalletManageActivity;
import org.nervos.neuron.custom.TitleBar;
import org.nervos.neuron.dialog.DialogUtil;
import org.nervos.neuron.dialog.TokenTransferDialog;
import org.nervos.neuron.event.TokenRefreshEvent;
import org.nervos.neuron.item.ChainItem;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.remote.response.TransactionInfo;
import org.nervos.neuron.remote.response.TransactionInfoResponse;
import org.nervos.neuron.service.QrCodeService;
import org.nervos.neuron.service.WalletService;
import org.nervos.neuron.util.Blockies;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.db.SharePrefUtil;
import org.nervos.neuron.util.permission.PermissionUtil;
import org.nervos.neuron.util.permission.RuntimeRationale;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Response;
import rx.Subscriber;

public class WalletFragment extends BaseFragment {

    public static final String TAG = WalletFragment.class.getName();
    private static final int REQUEST_CODE = 0x01;

    private TextView walletNameText;
    private TextView addressText;
    private FrameLayout receiveLayout;
    private FrameLayout qrCodeScanLayout;
    private TitleBar titleBar;
    private RecyclerView tokenRecycler;
    private CircleImageView photoImage;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TokenAdapter tokenAdapter = new TokenAdapter();

    private List<TokenItem> tokenItemList = new ArrayList<>();
    private List<String> walletNameList = new ArrayList<>();
    private WalletItem walletItem;
    private TransactionInfo transactionInfo;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);
        walletNameText = view.findViewById(R.id.wallet_name);
        addressText = view.findViewById(R.id.wallet_address);
        tokenRecycler = view.findViewById(R.id.token_list);
        receiveLayout = view.findViewById(R.id.wallet_receive_layout);
        qrCodeScanLayout = view.findViewById(R.id.wallet_qr_scan_layout);
        titleBar = view.findViewById(R.id.title);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        photoImage = view.findViewById(R.id.wallet_photo);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().register(this);
        initWalletData(true);
        initAdapter();
        initListener();
        initTitleBarListener();
        initRefresh();
    }


    private void initWalletData(boolean showProgress) {
        if ((walletItem = DBWalletUtil.getCurrentWallet(getContext())) != null) {
            if (showProgress) showProgressBar();
            walletNameList = DBWalletUtil.getAllWalletName(getContext());
            walletNameText.setText(walletItem.name);
            addressText.setText(walletItem.address);
            photoImage.setImageBitmap(Blockies.createIcon(walletItem.address));
            WalletService.getWalletTokenBalance(getContext(), walletItem, walletItem ->
                walletNameText.post(() -> {
                    if (showProgress) dismissProgressBar();
                    swipeRefreshLayout.setRefreshing(false);
                    if (walletItem.tokenItems != null) {
                        tokenItemList = walletItem.tokenItems;
                        tokenAdapter.notifyDataSetChanged();
                    }
                })
            );
        } else {
            startActivity(new Intent(getActivity(), AddWalletActivity.class));
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWalletSaveEvent(TokenRefreshEvent event) {
        initWalletData(true);
    }

    private void initListener() {
        receiveLayout.setOnClickListener(v -> startActivity(new Intent(getActivity(), ReceiveQrCodeActivity.class)));
        qrCodeScanLayout.setOnClickListener(v -> {
            AndPermission.with(getContext())
                .runtime().permission(Permission.Group.CAMERA)
                .rationale(new RuntimeRationale())
                .onGranted(permissions -> {
                    Intent intent = new Intent(getActivity(), QrCodeActivity.class);
                    startActivityForResult(intent, REQUEST_CODE);
                })
                .onDenied(permissions -> PermissionUtil.showSettingDialog(getContext(), permissions))
                .start();
        });
        photoImage.setOnClickListener(v -> gotoWalletManagePage());
        addressText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("qrCode", walletItem.address);
                if (cm != null) {
                    cm.setPrimaryClip(mClipData);
                    Toast.makeText(getContext(), R.string.copy_success, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void handlePayQrCode(String value) {
        try {
            transactionInfo = new Gson().fromJson(value, TransactionInfo.class);
            startPayTokenPage(transactionInfo);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            showProgressCircle();
            QrCodeService.getTransactionInfo(value)
                .subscribe(new Subscriber<Response>() {
                    @Override
                    public void onCompleted() {
                        dismissProgressCircle();
                    }
                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        dismissProgressCircle();
                        Toast.makeText(getContext(), R.string.qr_code_error, Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onNext(Response response) {
                        try {
                            String result = response.body().string();
                            if (TextUtils.isEmpty(result)) {
                                Toast.makeText(getContext(), R.string.qr_code_error, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            TransactionInfoResponse infoResponse = new Gson().fromJson(result,
                                    TransactionInfoResponse.class);
                            if (infoResponse == null) {
                                Toast.makeText(getContext(), R.string.qr_code_error, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (!TextUtils.isEmpty(infoResponse.error)) {
                                Toast.makeText(getContext(), infoResponse.error, Toast.LENGTH_SHORT).show();
                            } else if (infoResponse.transaction != null) {
                                transactionInfo = infoResponse.transaction;
                                transactionInfo.uuid = infoResponse.uuid;
                                startPayTokenPage(transactionInfo);
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                            Toast.makeText(getContext(), R.string.qr_code_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }
    }

    private void startPayTokenPage(TransactionInfo transactionInfo) {
        ChainItem chainItem;
        if (transactionInfo.isEthereum()) {
            chainItem = new ChainItem(ConstUtil.ETH_CHAIN_ID,
                    ConstUtil.ETH_MAIN_NET, ConstUtil.ETH_NODE_IP);
        } else {
            chainItem = new ChainItem(ConstUtil.DEFAULT_NERVOS_DEFAULT_CHAIN_ID,
                    ConstUtil.NERVOS_CHAIN_NAME, ConstUtil.NERVOS_NODE_IP);
        }
        Intent intent = new Intent(getActivity(), PayTokenActivity.class);
        intent.putExtra(ConstUtil.EXTRA_PAYLOAD, new Gson().toJson(transactionInfo));
        intent.putExtra(ConstUtil.EXTRA_CHAIN, chainItem);
        startActivity(intent);
    }


    private void gotoWalletManagePage() {
        startActivity(new Intent(getActivity(), WalletManageActivity.class));
    }

    private void initRefresh() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initWalletData(false);
            }
        });
    }


    private void initAdapter() {
        tokenRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        decoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.recycle_line));
        tokenRecycler.addItemDecoration(decoration);
        tokenRecycler.setAdapter(tokenAdapter);

        tokenAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                TokenTransferDialog dialog = new TokenTransferDialog(getContext(), tokenItemList.get(position));
                dialog.setOnReceiveClickListener(v -> {
                    startActivity(new Intent(getActivity(), ReceiveQrCodeActivity.class));
                    dialog.dismiss();
                });
                dialog.setOnTransferClickListener(v -> {
                    Intent intent = new Intent(getActivity(), TransferActivity.class);
                    intent.putExtra(TransferActivity.EXTRA_TOKEN, tokenItemList.get(position));
                    startActivity(intent);
                    dialog.dismiss();
                });
                dialog.show();
            }
        });
    }

    private void initTitleBarListener() {
        titleBar.setOnRightClickListener(() -> startActivity(new Intent(getActivity(), AddWalletActivity.class)));

        titleBar.setOnLeftClickListener(() -> DialogUtil.showListDialog(getContext(),
                R.string.switch_current_wallet, walletNameList, walletItem.name, which -> {
            SharePrefUtil.putCurrentWalletName(walletNameList.get(which));
            initWalletData(true);
        }));
    }


    class TokenAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public static final int VIEW_TYPE_ITEM = 1;
        public static final int VIEW_TYPE_EMPTY = 0;

        public OnItemClickListener onItemClickListener;

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            if (viewType == VIEW_TYPE_EMPTY) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_empty_view, parent, false);
                ((TextView)view.findViewById(R.id.empty_text)).setText(R.string.empty_no_token_data);
                return new RecyclerView.ViewHolder(view){};
            }
            TokenViewHolder holder = new TokenViewHolder(LayoutInflater.from(
                    getActivity()).inflate(R.layout.item_token_list, parent,
                    false));
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof TokenViewHolder) {
                TokenViewHolder viewHolder = (TokenViewHolder)holder;
                if (TextUtils.isEmpty(tokenItemList.get(position).avatar)) {
                    viewHolder.tokenImage.setImageResource(tokenItemList.get(position).chainId < 0?
                            R.drawable.ether_small : R.mipmap.ic_launcher);
                } else {
                    viewHolder.tokenImage.setImageURI(tokenItemList.get(position).avatar);
                }
                if (tokenItemList.get(position) != null) {
                    viewHolder.tokenName.setText(tokenItemList.get(position).symbol);
                    viewHolder.tokenAmount.setText(NumberUtil.getDecimal_4(tokenItemList.get(position).balance));
                }
                viewHolder.itemView.setTag(position);
            }
        }

        @Override
        public int getItemCount() {
            if (tokenItemList.size() == 0) {
                return 1;
            }
            return tokenItemList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (tokenItemList.size() == 0) {
                return VIEW_TYPE_EMPTY;
            }
            return VIEW_TYPE_ITEM;
        }

        class  TokenViewHolder extends RecyclerView.ViewHolder {
            SimpleDraweeView tokenImage;
            TextView tokenName;
            TextView tokenAmount;

            public TokenViewHolder (View view) {
                super(view);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onItemClick(v, (int)v.getTag());
                        }
                    }
                });
                tokenImage = view.findViewById(R.id.token_image);
                tokenName = view.findViewById(R.id.token_name);
                tokenAmount = view.findViewById(R.id.token_amount);
            }
        }
    }

    private interface OnItemClickListener{
        void onItemClick(View view, int position);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (data != null && data.getExtras() != null) {
                Bundle bundle = data.getExtras();
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    handlePayQrCode(bundle.getString(CodeUtils.RESULT_STRING));
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(getContext(), R.string.qrcode_handle_fail, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}
