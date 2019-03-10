# Paperclip Lite
Trustless access to bank account for dapps.

## Transaction flow diagram:
1. Deposit
2. Purchase
![alt text][flow1]

[logo]: https://raw.githubusercontent.com/bridgedao/paperclip_ethparis/master/paperclip-flows1.png "Paperclip flow diagram 1"

3. Refund (spend successful)
4. Slash (spend failed)
![alt text][flow1]

[logo]: https://raw.githubusercontent.com/bridgedao/paperclip_ethparis/master/paperclip-flows2.png "Paperclip flow diagram 2"
## Inspiration
The concept is a result of long iterative development of the idea of a decentralized bank.

## What it does
It provides simple and trustless bank account integration to dapps. Dapp builder can as easily integrate credit card payments as token payment, without having to sacrifice privacy or decentralization (as is the case when hooking up personal bank account) or spend money and time on setting up business and a business bank account.

It has a Paypal account which can take all the credit card payments directly from dapp front end. Dapp can then spend it’s fiat balance by sending a transaction to Paperclip dapp. Paypal account owner has to add stake into Paperclip (in cryptocurrency) which gets taken away in case he tries to run away with money. Paypal account owner gets to keep a fee for every successful transaction.

## How we built it
We made a solidity smart contract that manages all the interactions and keeps the stake. We deployed it together with Chainlink node onto Quorum network. We used Chainlink existing Paypal API sandbox.
