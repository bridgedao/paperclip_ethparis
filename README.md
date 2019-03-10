# Paperclip Lite
Trustless access to bank account for dapps.

Users of Paperclip are dapps that offer simple services who want to be accessible to users who don't hold any cryptocurrency.This concept is a result of long iterative development of the idea of a decentralized bank.

**It is not cryptocurrency onramp.**

## What it does
It provides simple and trustless bank account integration to dapps. Dapp builder can easily integrate credit card payments without having to sacrifice privacy or decentralization (as is the case when hooking up personal bank account) or spend money and time on setting up business and a business bank account.

### Steps
1. Dapp customer interacts with payment gateway (through a widget) directly on dapp fronted.
2. When a fiat transaction is received to the bank account, webhook send notification to API.
3. API communicates this to Chainlink node.
4. Chainlink node updates the fiat balance of dapp in Paperclip smart contract.
5. Dapp can then spend its fiat balance by sending a private transaction to Paperclip dapp and including important payment details (receiver, amount, invoice details).
6. Paperclip checks the requrements and sends the transaction instruction to Chainlink which triggers the transaction using Paypal API.
7. Paypal account owner has to add stake into Paperclip in cryptocurrency.
8. If spend (refund) transaction is successful, the fiat balance in Paperclip is updated. And Paypal account owner gets to keep a fee for every successful transaction.
9. If spend (refund) transaction is unsuccessful, stake is automatically taken away and sent to dapp address.

## Transaction flow diagram:
1. Deposit
2. Purchase
![alt text][flow1]

[flow1]: https://raw.githubusercontent.com/bridgedao/paperclip_ethparis/master/paperclip-flows1.png "Paperclip flow diagram 1"

3. Refund (spend successful)
4. Slash (spend failed)
![alt text][flow2]

[flow2]: https://raw.githubusercontent.com/bridgedao/paperclip_ethparis/master/paperclip-flows2.png "Paperclip flow diagram 2"

## How we built it
We made a solidity smart contract that manages all the interactions and keeps the stake. We deployed it together with Chainlink node onto Quorum network. We used Chainlink existing Paypal API sandbox.
