pragma solidity ^0.4.24;

import "./Escrow.sol";
import "./Agent.sol";
import "./Paperclipable.sol";

contract ConsumerInterface {
    function requestPaypalDeposit(string _jobId, bytes32 _email, bytes32 _eventId) public;
    function requestPaypalRefund(string _jobId, bytes32 _txId) public;
}

contract Paperclip is Escrow, Agent {

    ConsumerInterface consumer;
    Paperclipable paperclipable;

    string depositJobId;
    string withdrawJobId;

    constructor(address _consumerAddress, address _dapp, string _depositJobId, string _withdrawJobId) public {
        consumer = ConsumerInterface(_consumerAddress);
        paperclipable = Paperclipable(_dapp);
        depositJobId  = _depositJobId;
        withdrawJobId = _withdrawJobId;
    }

    function deposit(bytes32 eventId, bytes32 email) public {
        consumer.requestPaypalDeposit(depositJobId, email, eventId);
        emit Deposit(email);
    }

    function depositCallback(bytes32 email, bytes32 txId) public {
        paperclipable.deposit(email, txId);
        emit DepositComplete(txId);
    }

    function refund(bytes32 txId) public {
        consumer.requestPaypalRefund(withdrawJobId, txId);
        emit Refund(txId);
    }

}
