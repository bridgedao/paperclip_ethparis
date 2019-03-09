package org.web3j.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.quorum.Quorum;
import org.web3j.quorum.tx.ClientTransactionManager;
import org.web3j.sample.contracts.generated.Consumer;
import org.web3j.sample.contracts.generated.Escrow;
import org.web3j.sample.contracts.generated.ExampleDapp;
import org.web3j.sample.contracts.generated.LinkToken;
import org.web3j.sample.contracts.generated.Oracle;
import org.web3j.sample.contracts.generated.Paperclip;
import org.web3j.tx.Contract;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;

import java.math.BigInteger;

/**
 * A simple web3j application that demonstrates a number of core features of web3j:
 *
 * <ol>
 *     <li>Connecting to a node on the Ethereum network</li>
 *     <li>Loading an Ethereum wallet file</li>
 *     <li>Sending Ether from one address to another</li>
 *     <li>Deploying a smart contract to the network</li>
 *     <li>Reading a value from the deployed smart contract</li>
 *     <li>Updating a value in the deployed smart contract</li>
 *     <li>Viewing an event logged by the smart contract</li>
 * </ol>
 *
 * <p>To run this demo, you will need to provide:
 *
 * <ol>
 *     <li>Ethereum client (or node) endpoint. The simplest thing to do is
 *     <a href="https://infura.io/register.html">request a free access token from Infura</a></li>
 *     <li>A wallet file. This can be generated using the web3j
 *     <a href="https://docs.web3j.io/command_line.html">command line tools</a></li>
 *     <li>Some Ether. This can be requested from the
 *     <a href="https://www.rinkeby.io/#faucet">Rinkeby Faucet</a></li>
 * </ol>
 *
 * <p>For further background information, refer to the project README.
 */
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        new Application().run();
    }

    private void run() throws Exception {
        Quorum quorum = Quorum.build(new HttpService(
                "http://localhost:22000"));
        log.info("Connected to Ethereum client version: "
                + quorum.web3ClientVersion().send().getWeb3ClientVersion());

        ClientTransactionManager transactionManager = new ClientTransactionManager(
                quorum,
                "0xed9d02e382b34818e88b88a309c7fe71e65f419d",
                null, null);

        // Now lets deploy a smart contract
        log.info("Deploying smart contract");
        ContractGasProvider contractGasProvider = new StaticGasProvider(BigInteger.ZERO, BigInteger.valueOf(100000000));

//        LinkToken linkToken = LinkToken.deploy(
//                quorum,
//                transactionManager,
//                contractGasProvider).send();
//        Oracle oracle = Oracle.deploy(
//                quorum,
//                transactionManager,
//                contractGasProvider,
//                linkToken.getContractAddress()).send();
//        Consumer consumer = Consumer.deploy(
//                quorum,
//                transactionManager,
//                contractGasProvider,
//                oracle.getContractAddress(),
//                linkToken.getContractAddress()).send();
        Oracle oracle = Oracle.load("0xa501afd7d6432718daf4458cfae8590d88de818e",
                quorum,
                transactionManager,
                contractGasProvider);
        LinkToken linkToken = LinkToken.load("0x4d3bfd7821e237ffe84209d8e638f9f309865b87",
                quorum,
                transactionManager,
                contractGasProvider);

        Consumer consumer = Consumer.load("0x3950943d8d86267c04a4bba804f9f0b8a01c2fb8",
                quorum,
                transactionManager,
                contractGasProvider);

//        linkToken.transfer(consumer.getContractAddress(), Convert.toWei("5", Convert.Unit.ETHER).toBigInteger()).send();

//        log.info("Transferring ownership");
//        oracle.transferOwnership("0x31B298846eab0A93800a20dDCb1A1de1640Cbba2").send();

        String oracleAddress = oracle.getContractAddress();
        String linkTokenAddress = linkToken.getContractAddress();
        String consumerAddress = consumer.getContractAddress();
        log.info("Chainlink oracle " + oracleAddress);
        log.info("Chainlink token " + linkTokenAddress);
        log.info("Chainlink consumer " + consumerAddress);

        consumer.requestEthereumPriceFulfilledEventFlowable(DefaultBlockParameterName.LATEST,
                DefaultBlockParameterName.LATEST).subscribe(event -> {
            log.info("Current Ethereum Price: {}", event.price);
        });

        log.info("Chainlink Loaded ");
        log.info("Requesting Price of Ether");
        consumer.requestEthereumPrice("c31f7b0707f54e42a23f709e26109730", "USD").send();



        log.info("Previous Ethereum Price: 13851");

//        // Events enable us to log specific events happening during the execution of our smart
//        // contract to the blockchain. Index events cannot be logged in their entirety.
//        // For Strings and arrays, the hash of values is provided, not the original value.
//        // For further information, refer to https://docs.web3j.io/filters.html#filters-and-events
//        for (Greeter.ModifiedEventResponse event : contract.getModifiedEvents(transactionReceipt)) {
//            log.info("Modify event fired, previous value: " + event.oldGreeting
//                    + ", new value: " + event.newGreeting);
//            log.info("Indexed event previous value: " + Numeric.toHexString(event.oldGreetingIdx)
//                    + ", new value: " + Numeric.toHexString(event.newGreetingIdx));
//        }

        ExampleDapp exampleDapp = ExampleDapp.deploy(
                quorum,
                transactionManager,
                contractGasProvider).send();

        log.info("Deploying Paperclip");
        Paperclip paperclip = Paperclip.deploy(
                quorum,
                transactionManager,
                contractGasProvider,
                consumerAddress,
                exampleDapp.getContractAddress(),
                "c31f7b0707f54e42a23f709e26109730",
                "c31f7b0707f54e42a23f709e26109730").send();
    }
}
