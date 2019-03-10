pragma solidity ^0.4.24;

import "./Paperclipable.sol";

contract ConsumerInterface {
    function requestPaypalDeposit(string _jobId, bytes32 _email, bytes32 _eventId) public;
    function requestPaypalRefund(string _jobId, bytes32 _txId) public;
}

contract Paperclip {

    ConsumerInterface consumer;
    Paperclipable paperclipable;

    string depositJobId;
    string withdrawJobId;

    uint32 public balance;
    bytes32 fiatHolderId;
    uint32 valueInsured;

    event TokensBackingEscrow(uint32 value);
    event Slash();
    event Withdraw(uint32 value);

    event Deposit(bytes32 email);
    event DepositComplete(bytes32 txId);
    event Refund(bytes32 email, bytes32 txId);
    event ClaimStake(uint256 amount);

    constructor(
        address _consumerAddress,
        address _dapp,
        string _depositJobId,
        string _withdrawJobId) public {
        consumer = ConsumerInterface(_consumerAddress);
        paperclipable = Paperclipable(_dapp);
        depositJobId  = _depositJobId;
        withdrawJobId = _withdrawJobId;
    }

    function deposit(bytes32 email, bytes32 txId) public {
        require(balance < valueInsured);
        //paperclipable.deposit(email, txId);
        balance += 5;
        emit Deposit(email);
    }

    function refund(bytes32 email, bytes32 txId) public {
        require(balance <= valueInsured);
        // consumer.requestPaypalRefund(withdrawJobId, txId);
        balance -= 5;
        emit Refund(email, txId);
    }

    //functions
    function escrow(uint32 value) public payable {
        emit TokensBackingEscrow(value);
        valueInsured = value;
    }
    function slash() public {
        //send token to dapp
        emit Slash();
    }
    function withdraw(address _to) public {
        require(balance == 0);
        emit Withdraw(valueInsured);
        valueInsured = 0;

    }
//    //check compatibility with oracle
//    function callBack(uint _oracleValue, bytes32 _bqID) {
//        require(msg.sender == oracle);
//        require(_oracleValue ==0);
//        withdraw(reversebqID[_bqID], reversebqID[_bqID]);
//    }

}
