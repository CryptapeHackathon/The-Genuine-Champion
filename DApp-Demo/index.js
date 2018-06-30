const contractAddress = '0xb0770fdef4ea010b952181b54c1cd7d4ade3eaaa';
const functionAddress = {
  getNames: "cd838f0f",
  betForTeam: "e2093a69",
  setWinner: "2b39bc46",
};
const voteFor = {
  argentina: "6a6170616e000000000000000000000000000000000000000000000000000000",
  france: "6368696e61000000000000000000000000000000000000000000000000000000",
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
  //   document.getElementById('set_div').style.display = 'none';
  $('input[name=team_bet]').click(function() {
    $('#send_bet').removeAttr('disabled');
  });
}

function sendBet() {
  var betValue = $('input[name=team_bet]:checked').val();

  console.log(betValue, voteFor[betValue]);

  // disable button and change text
  $('#send_bet').prop('disabled', true);
  $('#send_bet').html('请扫码并确认交易');

  let txInfo = {
    chain: 'cita',
    transaction: {
      to: contractAddress,
      quota: 100000,
      data: functionAddress.betForTeam + voteFor[betValue],
      // 10.0 ether
      value: "0x8ac7230489e80000",
    }
  };

  sendTx(txInfo, function() {
    $('#send_bet').html('交易发送成功！');
    $('input[name=team_bet]').prop('checked', false);
    $('input[name=team_bet]').click(function() {
      $('#send_result').prop('disabled', false);
    });
    setTimeout(function () {
      // hide bet blocks and show set blocks
      $('#send_bet').hide();
      $('#send_result').show();
    }, 1500);
  });
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
          text: txInfoUrl,
          background: "#FFFFFF",
        });
      $('#qrcode-wrapper').show();
      checkStatusTimer = setInterval(function() {
        axios.get(storeServer + '/tx/status/' + response.data.uuid)
          .then(function(response) {
            switch(response.data.status) {
            case "success":
              if (!!callback) {
                callback(response.data);
              }
              $('#qrcode').empty();
              $('#qrcode-wrapper').hide();
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

function sendResult() {
  let teamValue = $('input[name=team_bet]:checked').val();
  console.log(teamValue, voteFor[teamValue]);

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
    // alert(teamValue + " is the winner.");
    $('#send_result').prop('disabled', true);
    $('#send_result').html('获胜球队是：' + teamValue + ' !');
  });
}

window.onload = init();

$('.box').backgroundMove();
