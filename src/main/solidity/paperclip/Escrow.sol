//we need to know how the oracle contract works
pragma solidity ^0.4.11;
contract Escrow /*is oracle.sol*/ {
    //variables
    //uint public balance; //keeping track of the balance of the contract
    address public oracle; // the oracle contract
    address public user; //the kickback contract or address

    mapping(address => uint) public deposits; // mapping the deposits of the participants
    mapping(address => bytes32) public bqID; // mapping the address to the paypalID
    mapping(bytes32 => address) public reversebqID; // mapping the  paypalID to the address

    //functions
    function deposit(bytes32 _bqID) public payable {
        if (deposits[msg.sender] == 0) {
            bqID[msg.sender] = _bqID;
            reversebqID[_bqID]= msg.sender;
        }
        else require(bqID[msg.sender] == _bqID);
        deposits[msg.sender] = msg.value;
    }
    function AskWithdraw() public payable {
        require(deposits[msg.sender] != 0);
        //trigger oracle with parameter bqID[msg.sender]
    }
    function slash(bytes32 _bqID) public {
        withdraw(user, reversebqID[_bqID]);
    }
    function withdraw(address _to, address _from) internal {
        _to.transfer(deposits[_from]);
        deposits[_from] = 0;
    }
    //check compatibility with oracle
    function callBack(uint _oracleValue, bytes32 _bqID) {
        require(msg.sender == oracle);
        require(_oracleValue ==0);
    withdraw(reversebqID[_bqID], reversebqID[_bqID]);
    }
}
