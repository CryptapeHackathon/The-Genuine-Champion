// We have to specify what version of compiler this code will compile with
pragma solidity ^0.4.19;

contract WorldCupGaming {

  mapping(bytes32 => uint32) private teamMap;
  bytes32 [] private teamList;

  struct BetInfo {
    bytes32 team;
    address sender;
    uint value;
  }
  BetInfo[] public betInfoList;

  bytes32 winnerTeam;
  /* need a owner to handle the rewards*/
  address owner;

  /* address[] winAddrs; */
  /* uint[] winValues; */

  function WorldCupGaming(bytes32[] names) public {
    teamList = names;
    for (uint i = 0; i < names.length; i++) {
      teamMap[names[i]] = 1;
    }
    owner = msg.sender;
  }

  function getNames() public view returns (bytes32 []) {
    return teamList;
  }

  function betForTeam(bytes32 teamName) public payable {
    require(msg.value > 0);
    require(teamMap[teamName] > 0);

    BetInfo memory info = BetInfo(teamName, msg.sender, msg.value);
    betInfoList.push(info);
  }

  /* After set the winer, the contract dispatchs the rewards automatically */
  /* It is a big problem that how to set the winner! */
  function setWinner(bytes32 teamName) public {
    require(msg.sender == owner);
    require(teamMap[teamName] > 0);

    winnerTeam = teamName;

    /* uint totalValue = 0; */
    /* uint totalWinValue = 0; */

    for (uint i = 0; i < betInfoList.length; i++) {
      BetInfo storage info = betInfoList[i];
      if (info.team == teamName) {
        info.sender.transfer(info.value);
        /* winAddrs.push(info.sender); */
        /* winValues.push(info.value); */
        /* totalWinValue += info.value; */
      }
      /* totalValue += info.value; */
    }

    /* uint rate = totalValue * 1000 / totalWinValue; */
    /* for (uint j = 0; j < winAddrs.length; j++) { */
    /*   uint value = winValues[j] * rate / 1000; */
    /*   winAddrs[j].transfer(value); */
    /* } */
    delete betInfoList;
    /* delete winAddrs; */
    /* delete winValues; */
  }
}
