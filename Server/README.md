
# Server API 文档

### 说明:

1. 字段名命名建议以驼峰风格命名，因为 DApp, Neuron 中的变量名也是驼峰风格的。


## DApp <=> Server

1. 将交易信息存储到 Server

    ``` javascript
    // POST /tx/info/
    // [Request]
    {
      // [必填] 链的类型，可选值: "ethereum", "bitcoin", "cita", "ckb"
      "chain": String,
      // [可选] 过期时间, 单位(秒), 不能超过 1 小时(3600秒)，默认值是 1 小时 
      "expire": Int,
      // [必填] 需要签名的交易信息(各个链的结构会有所不同)
      "transaction": {
        "to": Address,
        "value": BitInt,
        ...
      }
    }
    // [Response.200] 发送成功
    {
      // 交易信息的 ID (一个 uuid 值)
      "uuid": String,
    }
    // [Response.400] 参数有误
    {
      "uuid": String,
      // 出错时的错误信息
      "error": String,
    }
    ```

2. 从 Server 查询交易状态 (轮询)

    ``` javascript
    // GET /tx/status/{uuid}
    // [Response.200] 与 PUT /tx/status/{uuid} 中的 [Response.200] 结构一致
    // [Response.404] 可能的情况: 交易已经过期, 没有该交易
    ```

## Neuron <=> Server

1. Neuron 从 Server 查询交易信息

    ```javascript
    // GET /tx/info/{uuid}
    // [Response.200] 与 POST /tx/info/ 中的 [Request.200] 结构一致
    // [Response.404] 可能的情况: 交易已经过期, 没有该交易
    ```
    
2. 将交易结果发送到 Server

    ```javascript
    // PUT /tx/status/{uuid}
    // [Request]
    {
      // [必填] 交易状态, 可能的值: 
      //   "pending" => 交易进行中 (默认状态, 不允许 Neuron 填的值)
      //   "success" => 交易成功
      //   "denied"  => 用户拒绝交易
      //   "faied"   => 往区块链发送交易失败
      "status": String,
      // [出错时必填] 出错时(deined, failed)的错误信息  
      "error": String,
      // [成功时必填] 成功时(success)的交易信息
      "transaction": {
        // [必要字段] 交易发送成功后的交易 hash
        "hash": String, 
      }
    }
    // [Response.200]
    {
      "uuid": String,
    }
    // [Response.400] 参数有误
    {
      "uuid": String,
      // 出错时的错误信息
      "error": String,
    }
    ```
    
