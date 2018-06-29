// We have to specify what version of compiler this code will compile with
pragma solidity ^0.4.19;

contract WorldCupGaming {

  uint32 [] private teamOdds;

  mapping(bytes32 => uint32) private teamMap;
  bytes32 [] private teamList;

  /* mapping field below is equivalent to an associative array or hash.
  The key of the mapping is candidate name stored as type bytes32 and value is
  an unsigned integer to store the vote count
  */
  struct BetInfo {
    bytes32[] list;
    mapping(bytes32 => uint) map;
  }

  mapping(bytes32 => address[]) public betMap;
  bytes32[] public betList;
  mapping(address => BetInfo) private betInfoMap;
  // for iterate over `betInfoMap`
  address[] public betInfoList;

  bytes32 winnerTeam;
  /* need a owner to handle the rewards*/
  address owner;


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

  function getOddes() public view returns (uint32 []) {
    return teamOdds;
  }

  function getTeams(address sender) public view returns (bytes32 []) {
    return betInfoMap[sender].list;
  }

  function getBetList() public view returns (bytes32 []) {
      return betList;
  }

  function getBetAddrs(bytes32 teamName) public view returns (address []) {
    return betMap[teamName];
  }

  function getBetValue(address sender, bytes32 teamName) public view returns (uint) {
      return betInfoMap[sender].map[teamName];
  }


  function betForTeam(bytes32 teamName) public payable {
    require(msg.value > 0);
    require(teamMap[teamName] > 0);

    if (betMap[teamName].length == 0) {
        betList.push(teamName);
    }
    if (betInfoMap[msg.sender].list.length == 0) {
      betMap[teamName].push(msg.sender);
      betInfoList.push(msg.sender);
    }
    if (betInfoMap[msg.sender].map[teamName] == 0) {
        betInfoMap[msg.sender] = BetInfo(new bytes32[](0));
       betInfoMap[msg.sender].map[teamName] = 0;
       betInfoMap[msg.sender].list.push(teamName);
    }
    betInfoMap[msg.sender].map[teamName] += msg.value;
  }

  /* After set the winer, the contract dispatchs the rewards automatically */
  /* It is a big problem that how to set the winner! */
  function setWinner(bytes32 teamName) public {
    require(msg.sender == owner);
    require(teamMap[teamName] > 0);

    winnerTeam = teamName;

    address[] storage betAddressList = betMap[teamName];
    for (uint i = 0; i < betAddressList.length; i++) {
      address betAddress = betAddressList[i];
      BetInfo storage betInfo = betInfoMap[betAddress];
      betAddress.transfer(betInfo.map[teamName]);
    }

    // Clear state
    for (uint j = 0; j < betList.length; j++) {
      delete betMap[betList[j]];
    }
    for (uint k = 0; k < betInfoList.length; k++) {
      delete betInfoMap[betInfoList[k]];
    }
    delete betList;
    delete betInfoList;
  }
}
