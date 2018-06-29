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
    mapping(bytes32 => uint) map;
    bytes32[] list;
  }

  mapping(bytes32 => address[]) public betMap;
  mapping(address => BetInfo) private betInfoMap;
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

  function getBets(bytes32 teamName) public view returns (address []) {
    return betMap[teamName];
  }

  function betForTeam(bytes32 teamName) public payable {
    require(msg.value > 0);
    if (betInfoMap[msg.sender].list.length == 0) {
      betMap[teamName].push(msg.sender);
      betInfoList.push(msg.sender);
    }
    if (betInfoMap[msg.sender].map[teamName] == 0) {
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

    var betAddressList = betMap[teamName];
    for (uint i = 0; i < betAddressList.length; i++) {
      var betAddress = betAddressList[i];
      var betInfo = betInfoMap[betAddress];
      betAddress.transfer(betInfo.map[teamName] * 2);
    }
  }
}
