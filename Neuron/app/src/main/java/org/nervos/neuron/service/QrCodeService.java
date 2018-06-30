package org.nervos.neuron.service;

import com.google.gson.Gson;

import org.nervos.neuron.remote.request.TransactionResultRequest;
import org.nervos.neuron.util.ConstUtil;

import java.io.IOException;
import java.util.concurrent.Callable;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class QrCodeService {

    public static Observable<Response> getTransactionInfo(String url) {
        Request request = new Request.Builder().url(url).build();
        return Observable.fromCallable(new Callable<Response>() {
            @Override
            public Response call() throws IOException {
                return NervosHttpService.getHttpClient().newCall(request).execute();
            }
        }).subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<Response> submitTransactionStatus(
            TransactionResultRequest resultRequest, String uuid) {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody requestBody = RequestBody.create(mediaType, new Gson().toJson(resultRequest));
        Request request = new Request.Builder()
                .put(requestBody)
                .url(String.format(ConstUtil.SERVER_SUBMIT_STATUS_URL, uuid))
                .build();
        return Observable.fromCallable(new Callable<Response>() {
            @Override
            public Response call() throws IOException {
                return NervosHttpService.getHttpClient().newCall(request).execute();
            }
        }).subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread());
    }

}
