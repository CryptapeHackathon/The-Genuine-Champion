# 二维码生成方案

应用端在将交易信息通过API发送给服务端后，服务端会立即返回一个相应的URL。这时应用端可以使用任意二维码库将该URL转化为一个二维码，供手机端扫码获取交易信息。在我们的Demo中我们使用了`jquery-qrcode.js`这个库。

```
<script src="https://cdnjs.cloudflare.com/ajax/libs/lrsjng.jquery-qrcode/0.14.0/jquery-qrcode.min.js"></script>
```

在HTML中设置一个DIV来放置二维码，这里也支持`canvas`或者`table`元素生成二维码。

```
<div id="qrcode"></div>
```

之后在js中可以随时生成二维码

```
$('#qrcode').qrcode({
    width: 120,
    height: 120,
    text: "Sample QR"
    });
```

其他需要设置的字段请参考：https://stackoverflow.com/questions/37803884/qr-code-using-jquery