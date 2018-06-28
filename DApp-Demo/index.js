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

    document.getElementById('set_div').style.display = 'none'

}

function sendBet() {
    var betValue=$('input[name=team_bet]:checked').val()
    var jq = window.jQuery;

    console.log(betValue);
    document.getElementById('bet_div').style.display = 'none'
    document.getElementById('set_div').style.display = 'block'
    $('#qrcode').qrcode(betValue);
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