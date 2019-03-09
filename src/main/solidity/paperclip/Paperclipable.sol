pragma solidity ^0.4.0;

interface Paperclipable {
    function deposit(bytes32 email, bytes32 txId) public;
    function refund(bytes32 email) public;
}
