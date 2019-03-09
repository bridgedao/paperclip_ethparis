pragma solidity ^0.4.0;

import "./Paperclipable.sol";
import "./Paperclip.sol";

contract ExampleDapp is Paperclipable{

    mapping(bytes32 => bytes32) public transactions;

    function deposit(bytes32 email, bytes32 txId) {
        transactions[email] = txId;
    }

    function refund(bytes32 email, address paperclipAddress) {
        Paperclip paperclip = Paperclip(paperclipAddress);
    }

}
