// We have to specify what version of compiler this code will compile with
pragma solidity ^0.4.19;

contract WorldCupGaming {
  bytes32 [] private teamList;

  struct BetInfo {
    bytes32 team;
    address sender;
    uint value;
  }
  BetInfo[] public betInfoList;

  address owner;

  modifier onlyOwner() {
    require(msg.sender == owner);
    _;
  }

  function WorldCupGaming(bytes32[] names) public {
    teamList = names;
    owner = msg.sender;
  }

  function getNames() public view returns (bytes32 []) {
    return teamList;
  }

  function betForTeam(bytes32 teamName) public payable {
    require(msg.value > 0);

    BetInfo memory info = BetInfo(teamName, msg.sender, msg.value);
    betInfoList.push(info);
  }

  /* After set the winer, the contract dispatchs the rewards automatically */
  /* It is a big problem that how to set the winner! */
  function setWinner(bytes32 teamName) public onlyOwner {

    for (uint i = 0; i < betInfoList.length; i++) {
      BetInfo storage info = betInfoList[i];
      if (info.team == teamName) {
        info.sender.transfer(info.value);

      }
    }
    delete betInfoList;
  }
}
