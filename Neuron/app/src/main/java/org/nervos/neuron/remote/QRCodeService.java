package org.nervos.neuron.remote;

import org.nervos.neuron.remote.request.TransactionInfoRequest;
import org.nervos.neuron.remote.request.TransactionResultRequest;
import org.nervos.neuron.remote.response.TransactionInfoResponse;
import org.nervos.neuron.remote.response.TransactionStatusResponse;

import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import rx.Observable;

public interface QRCodeService {

    @GET("/tx/info/{uuid}")
    Observable<Response<TransactionInfoResponse>> getTransactionInfo(@Path("uuid") String uuid);

    @FormUrlEncoded
    @PUT("/tx/status/{uuid}")
    Observable<Response<TransactionStatusResponse>> submitTransactionStatus(
            @Path("uuid") String uuid, @Body TransactionResultRequest request);

    @FormUrlEncoded
    @POST("/tx/info/")
    Observable<Response<TransactionStatusResponse>> submitTransactionInfo(
            @Body TransactionInfoRequest request);

}
