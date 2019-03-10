package org.web3j.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.http.HttpService;
import org.web3j.quorum.Quorum;
import org.web3j.quorum.tx.ClientTransactionManager;
import org.web3j.sample.contracts.generated.Consumer;
import org.web3j.sample.contracts.generated.ExampleDapp;
import org.web3j.sample.contracts.generated.LinkToken;
import org.web3j.sample.contracts.generated.Oracle;
import org.web3j.sample.contracts.generated.Paperclip;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    //TODO:
    // Use wyre to subscribe to account balance
    // we get the email and txId from wyre
    private byte[] emailBytes = Arrays.copyOf("user@test.com".getBytes(), 32);
    private byte[] txIdBytes = Arrays.copyOf("mockId".getBytes(), 32);


    public static void main(String[] args) throws Exception {
        new Application().run();
    }

    private Quorum quorum;
    private ClientTransactionManager transactionManager;
    private ContractGasProvider contractGasProvider;

    private Oracle oracle;
    private LinkToken linkToken;
    private Consumer consumer;

    private ExampleDapp exampleDapp;
    private Paperclip paperclip;

    static byte[] trim(byte[] bytes)
    {
        int i = bytes.length - 1;
        while (i >= 0 && bytes[i] == 0)
        {
            --i;
        }

        return Arrays.copyOf(bytes, i + 1);
    }

    private void run() throws Exception {
        setUpBlockchainConection();

        deployChainLink();
//        loadChainlink();

        String oracleAddress = oracle.getContractAddress();
        String linkTokenAddress = linkToken.getContractAddress();
        String consumerAddress = consumer.getContractAddress();
        log.info("Chainlink oracle " + oracleAddress);
        log.info("Chainlink token " + linkTokenAddress);
        log.info("Chainlink consumer " + consumerAddress);
        log.info("Chainlink Loaded ");

        deployPaperclip(consumerAddress);
//        loadPaperclip();

        String exampleDappAddress = exampleDapp.getContractAddress();
        String paperclipAddress = paperclip.getContractAddress();
        log.info("Example Dapp " + exampleDappAddress);
        log.info("Paperclip " + paperclipAddress);
        log.info("Paperclip Loaded ");

        registerEventListeners();

        performDeposits();

        // artificial wait
        TimeUnit.SECONDS.sleep(10);

        performRefund();

        performDeposits();
        performDeposits();
        try {
            performDeposits();
        } catch (Exception e) {
            log.info("Cannot perform deposit - Too little stake");
        }

        try {
            performWithdraw();
        } catch (Exception e) {
            log.info("Current Paperclip balance: " + paperclip.balance().send());
            log.info("Cannot withdraw stake while fiat is being escrowed");
        }

        performRefund();
        performRefund();

        performWithdraw();

        try {
            performDeposits();
        } catch (Exception e) {
            log.info("Cannot perform deposit - Too little stake");
        }

    }

    private void performWithdraw() throws Exception {
        log.info("Attempting to withraw escrow stake");
        paperclip.withdraw("0xd8dba507e85f116b1f7e231ca8525fc9008a6966").send();
    }

    private void performRefund() throws Exception {
        log.info("Performing refund");
        exampleDapp.refund(emailBytes).send();
        paperclip.refund(emailBytes, txIdBytes).send();
        consumer.requestPaypalRefund("c31f7b0707f54e42a23f709e26109730", txIdBytes).send();
        log.info("Current Paperclip balance: " + paperclip.balance().send());
    }

    private void performDeposits() throws Exception {
        log.info("Performing deposit");
        consumer.fulfillPaypalDeposit(emailBytes, txIdBytes).send();
        paperclip.deposit(emailBytes, txIdBytes).send();
        exampleDapp.deposit(emailBytes, txIdBytes).send();
        log.info("Current Paperclip balance: " + paperclip.balance().send());
    }

    private void fundPaperclipEscrow() throws Exception {
        log.info("Preparing paperclip escrow");
        paperclip.escrow(BigInteger.TEN, BigInteger.ZERO).send();
    }

    private void registerEventListeners() {
        log.info("Registering Event Listeners");
        exampleDapp.depositEventFlowable(
                DefaultBlockParameterName.LATEST,
                DefaultBlockParameterName.LATEST).subscribe(event -> {
                    log.info("Dapp accepts deposit from: {} txId: {}",
                            new String(trim(event.email), UTF_8), new String(trim(event.txId), UTF_8));
                }
        );
        exampleDapp.refundEventFlowable(
                DefaultBlockParameterName.LATEST,
                DefaultBlockParameterName.LATEST).subscribe(event -> {
                    log.info("Dapp initiating refund for: {} txId: {}",
                            new String(trim(event.email), UTF_8), new String(trim(event.txId), UTF_8));
                }
        );

        paperclip.refundEventFlowable(
                DefaultBlockParameterName.LATEST,
                DefaultBlockParameterName.LATEST).subscribe(event -> {
                    log.info("Paperclip initiating refund: {} txId: {}",
                            new String(trim(event.email), UTF_8), new String(trim(event.txId), UTF_8));
                }
        );

        paperclip.depositEventFlowable(
                DefaultBlockParameterName.LATEST,
                DefaultBlockParameterName.LATEST).subscribe(event -> {
                    log.info("Paperclip accepts deposit from: {}",
                            new String(trim(event.email), UTF_8));
                }
        );

        paperclip.withdrawEventFlowable(
                DefaultBlockParameterName.LATEST,
                DefaultBlockParameterName.LATEST).subscribe(event -> {
                    log.info("Escrow withdrawn");
                }
        );
        paperclip.tokensBackingEscrowEventFlowable(
                DefaultBlockParameterName.LATEST,
                DefaultBlockParameterName.LATEST).subscribe(event -> {
                    log.info("Escrow was backed by: ${} worth of crypto", event.value);
                }
        );

        consumer.requestPaypalRefundFulfilledEventFlowable(
                DefaultBlockParameterName.LATEST,
                DefaultBlockParameterName.LATEST).subscribe(event -> {
            log.info("Chainlink -> Paypal refunding {}", new String(trim(event.email), UTF_8));
        });

        consumer.requestPaypalDepositFulfilledEventFlowable(
                DefaultBlockParameterName.LATEST,
                DefaultBlockParameterName.LATEST).subscribe(event -> {
            log.info("API -> Chainlink registering fiat transaction {} from {}",
                    new String(trim(event.txId), UTF_8),
                    new String(trim(event.email), UTF_8));
        });
    }

    private void setUpBlockchainConection() throws IOException {
        quorum = Quorum.build(new HttpService(
                "http://localhost:22000"));
        log.info("Connected to Ethereum client version: "
                + quorum.web3ClientVersion().send().getWeb3ClientVersion());

        transactionManager = new ClientTransactionManager(
                quorum,
                "0xed9d02e382b34818e88b88a309c7fe71e65f419d",
                null, null);

        contractGasProvider = new StaticGasProvider(BigInteger.ZERO, BigInteger.valueOf(100000000));
    }
    private void deployPaperclip(String consumerAddress) throws Exception {
        log.info("Deploying paperpclipable dapp");
        exampleDapp = ExampleDapp.deploy(
                quorum,
                transactionManager,
                contractGasProvider).send();

        log.info("Deploying Paperclip");
        paperclip = Paperclip.deploy(
                quorum,
                transactionManager,
                contractGasProvider,
                consumerAddress,
                exampleDapp.getContractAddress(),
                "c31f7b0707f54e42a23f709e26109730",
                "c31f7b0707f54e42a23f709e26109730").send();
        exampleDapp.attachPaperclip(paperclip.getContractAddress());
        fundPaperclipEscrow();
    }

    private void deployChainLink() throws Exception {
        log.info("Deploying Chainlink");
        linkToken = LinkToken.deploy(
                quorum,
                transactionManager,
                contractGasProvider).send();
        oracle = Oracle.deploy(
                quorum,
                transactionManager,
                contractGasProvider,
                linkToken.getContractAddress()).send();
        consumer = Consumer.deploy(
                quorum,
                transactionManager,
                contractGasProvider,
                oracle.getContractAddress(),
                linkToken.getContractAddress()).send();

        linkToken.transfer(consumer.getContractAddress(), Convert.toWei("5", Convert.Unit.ETHER).toBigInteger()).send();
        oracle.transferOwnership("0x31B298846eab0A93800a20dDCb1A1de1640Cbba2").send();
    }

    private void loadPaperclip() throws Exception {
        log.info("Loading paperclip");
        exampleDapp = ExampleDapp.load(
                "0xdcb6d1a6fdd31f29caa728ef3151f3c74df2e8ef",
                quorum,
                transactionManager,
                contractGasProvider);
        paperclip = Paperclip.load(
                "0x9e000d8b2ba13b40b9a56561d51288dc29573d12",
                quorum,
                transactionManager,
                contractGasProvider);
    }

    private void loadChainlink() {
        log.info("Loading Chainlik");
        oracle = Oracle.load("0x3dc0a01887b562412a933d6f81a29bf079a538f8",
                quorum,
                transactionManager,
                contractGasProvider);
        linkToken = LinkToken.load("0x23a47bc614cbae0e073aaff99d0bb8ec361248c3",
                quorum,
                transactionManager,
                contractGasProvider);

        consumer = Consumer.load("0xb2fd44503513d7578f9f08f8177b6b7909d48701",
                quorum,
                transactionManager,
                contractGasProvider);
    }
}
