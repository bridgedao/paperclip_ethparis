pragma solidity 0.4.24;

contract Agent {
    address oracleAddress;
    address dapp;

    event Deposit(bytes32 email);
    event DepositComplete(bytes32 txId);
    event Refund(bytes32 txId);
    event ClaimStake(uint256 amount);
}
