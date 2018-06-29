const contractAddress = '0x44c0005df0cd86ea556a1c3c944a9383482c338c';
const functionAddress = {
  getNames: "cd838f0f",
  betForTeam: "e2093a69",
  setWinner: "2b39bc46",
};
const voteFor = {
  japan: "6a6170616e000000000000000000000000000000000000000000000000000000",
  china: "6368696e61000000000000000000000000000000000000000000000000000000",
};
let checkStatusTimer = {};

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
  const sendBetButton = document.getElementById('send_bet');
  sendBetButton.addEventListener('click', sendBet);


  // bind send_result events with functions
  const sendResultButton = document.getElementById('send_result');
  sendResultButton.addEventListener('click', sendResult);

  // hide set blocks
  document.getElementById('set_div').style.display = 'none';

}

function sendBet() {
  var betValue = $('input[name=team_bet]:checked').val();

  console.log(betValue);

  // disable button and change text
  $('#send_bet').prop('disabled', true);
  $('#send_bet').html('请扫码并确认交易');

  let txInfo = {
    chain: 'cita',
    transaction: {
      to: contractAddress,
      quota: 100000,
      data: functionAddress.betForTeam + voteFor[betValue],
      value: "0x2386f26fc10000",
    }
  };

  sendTx(txInfo, function() { qrScanCallBack(1); });
}


function clearTimer() {
  if (checkStatusTimer !== null) {
    clearInterval(checkStatusTimer);
    checkStatusTimer = null;
  }
}

function sendTx(txInfo, callback) {
  clearTimer();
  let url = storeServer + '/tx/info/';
  axios.post(url, txInfo)
    .then(function (response) {
      console.log(response);
      let txInfoUrl = url + response.data.uuid;
      console.log('txInfoUrl:', txInfoUrl);
      // plot qr code
      $('#qrcode')
        .empty()
        .qrcode({
          size: 150,
          text: txInfoUrl
        });
      checkStatusTimer = setInterval(function() {
        axios.get(storeServer + '/tx/status/' + response.data.uuid)
          .then(function(response) {
            switch(response.data.status) {
            case "success":
              if (!!callback) {
                callback(response.data);
              }
            case "denied":
            case "faied":
              clearTimer();
              console.log('status=' + response.data.status);
              break;
            default:
              console.log('status=pending:' + response.data.uuid);
            }
          });
      }, 1000);
    })
    .catch(function (error) {
      console.log(error);
      clearTimer();
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
      document.getElementById('bet_div').style.display = 'none';
      document.getElementById('set_div').style.display = 'block';
    }, delayInMilliseconds);
    break;
  }
}

function sendResult() {
  let teamValue = $('input[name=team_set]:checked').val();
  console.log(teamValue);

  let txInfo = {
    chain: 'cita',
    transaction: {
      to: contractAddress,
      quota: 100000,
      data: functionAddress.setWinner + voteFor[teamValue],
      value: "0x0",
    }
  };

  sendTx(txInfo, function() {
    alert(teamValue + " is the winner.");
  });
}

window.onload = init();
