// We have to specify what version of compiler this code will compile with
pragma solidity ^0.4.19;

contract worldCupGaming {

  struct betInfo {
    address betMen;
    uint32 candidate;
    uint32 deposit;
  }

  uint32 [] private oddsList;

  /* mapping field below is equivalent to an associative array or hash.
  The key of the mapping is candidate name stored as type bytes32 and value is
  an unsigned integer to store the vote count
  */
  betInfo [] public bets;

  /* Solidity doesn't let you pass in an array of strings in the constructor (yet).
  We will use an array of bytes32 instead to store the list of candidates
  */
  bytes32[] public candidateList;

  /* need a owner to handle the rewards*/
  address owner;


  function worldCupGaming(uint32[] odds) public {

    /* Set odds at deploy time, so that no one can change the odds after This
     * contract deployed.
     */
    oddsList = odds;

    /* Set owner to deployer */
    owner = msg.sender;
  }

  function getOddes() public view returns (uint32 []) {
    return oddsList;
  }

  function betForCandidate(uint32 candidate, uint32 deposit) public payable {

    // one candidate point to an exactly odd.
    require(candidate < (oddsList.length - 1));
    bets.push(betInfo(msg.sender, candidate, deposit));
  }

  /* After set the winer, the contract dispatchs the rewards automatically */
  /* It is a big problem that how to set the winner! */
  function setWinner(uint32 winner) public {

    for (uint i = 0; i < bets.length; i++) {

      /* betMan get the rewards */
      if (winner == bets[i].candidate) {
        uint32 reward = bets[i].deposit * oddsList[bets[i].candidate];

        bets[i].betMen.transfer(reward);
      }
    }
  }
}
