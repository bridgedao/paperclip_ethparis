pragma solidity ^0.4.0;

import "./Paperclipable.sol";

interface PaperclipInterface {
    function deposit(bytes32 email, bytes32 txId) public;
    function refund(bytes32 email, bytes32 txId) public;
}

contract ExampleDapp is Paperclipable {

    mapping(bytes32 => bytes32) public transactions;
    PaperclipInterface paperclip;

    event Deposit(bytes32 email, bytes32 txId);
    event Refund(bytes32 email, bytes32 txId);
    event EscrowReceived(uint value);

    constructor() {
        //construct stuff?
    }

    function attachPaperclip(address paperclipAddress) {
        paperclip = PaperclipInterface(paperclipAddress);
    }

    function deposit(bytes32 email, bytes32 txId) {
        transactions[email] = txId;
        emit Deposit(email, txId);
    }

    function refund(bytes32 email) {
        bytes32 txId = transactions[email];
        transactions[email] = "";
        //paperclip.refund(email, transactions[email]);
        emit Refund(email, txId);
    }

    function receiveEscrow(uint value) {
        emit EscrowReceived(value);
    }

}
