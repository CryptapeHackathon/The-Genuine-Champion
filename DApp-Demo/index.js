const server = 'http://47.97.108.229:1337'

function init() {

    // const div = document.getElementById('info')

    // check if the web3 is injected
    // console.log('Web3 exist :' + (typeof web3 !== 'undefined'))
    // if (typeof web3 !== 'undefined') {  
    //   web3 = NervosWeb3(web3.currentProvider)
    //   console.log('Web Injected')
    //   div.innerHTML = 'Web Injected';
    // } else {
    //   web3 = NervosWeb3(server)
    //   console.log('Web3 Initialized')
    //   div.innerHTML = 'Web Initialized';
    // }

    // bind send_bet events with functions
    const sendBetButton = document.getElementById('send_bet')
    sendBetButton.addEventListener('click', sendBet)

    // bind send_result events with functions
    const sendResultButton = document.getElementById('send_result')
    sendResultButton.addEventListener('click', sendResult)

    // hide set blocks
    document.getElementById('set_div').style.display = 'none'

}

function sendBet() {
    var betValue = $('input[name=team_bet]:checked').val()

    console.log(betValue);

    // disable button and change text
    $('#send_bet').prop('disabled', true);
    $('#send_bet').html('请扫码并确认交易');

    // plot qr code
    $('#qrcode').qrcode({
        size: 150,
        text: betValue
    });

}

function qrScanCallBack(status) {
    //delay time
    var delayInMilliseconds = 1500;

    switch (status) {

        // if user rejected
        case 0:
            $('#send_bet').prop('disabled', true);
            $('#send_bet').html('用户拒绝交易！');
            setTimeout(function () {
                $('#send_bet').prop('disabled', false);
                $('#send_bet').html('下注！');
            }, delayInMilliseconds);
            break;

            //if user accepted
        case 1:
            // change to loading status
            $('#send_bet').prop('disabled', true);
            $('#send_bet').html('交易发送成功！');
            setTimeout(function () {
                // hide bet blocks and show set blocks
                document.getElementById('bet_div').style.display = 'none'
                document.getElementById('set_div').style.display = 'block'
            }, delayInMilliseconds);
            break;
    }
}

//construct AppChain transaction structure
var tx_appchain = {
    to: '0x2cc18375F32a98EfC017D1dDEBCEBD6F9Ee75152',
    nonce: 100,
    quota: 100,
    data: '0x2cc18375F32a98EfC017D1dDEBCEBD6F9Ee75152',
    value: "10000000000000000000",
    chainId: 1,
    version: 0
}

// send AppChain transaction
function sendAppChainTransaction() {
    web3.eth.sendTransaction(tx_appchain, function (err, res) {
        console.log(res);
    })
}

function sendResult() {
    console.log($('input[name=team_set]:checked').val())

}

window.onload = init()