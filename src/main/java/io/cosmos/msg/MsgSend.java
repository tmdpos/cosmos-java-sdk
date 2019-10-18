package io.cosmos.msg;

import io.cosmos.crypto.Crypto;
import io.cosmos.msg.delegate.BoardcastTx;
import io.cosmos.msg.delegate.Messages;
import io.cosmos.msg.delegate.MsgDelegateValue;
import io.cosmos.msg.delegate.TxValue;
import io.cosmos.types.*;
import io.cosmos.util.EncodeUtils;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

public class MsgSend extends MsgBase {

    public static void main(String[] args) {
        MsgSend msg = new MsgSend();

        msg.setMsgType("cosmos-sdk/MsgSend");
        msg.setRestServerUrl("http://localhost:1317");

        msg.submit("stake",
                "6",
                "200000",
                "testchain",
                "stake",
                "16",
                "cosmos1geyy4wtak2q9effnfhze9u4htd8yxxmagdw3q0",
                "cosmos transfer",
                "2c999c5afe7f0c902846e1b286fed29c5c5914998655d469568560955abe0d5d");
    }

    public void submit(String feeDenom, String feeAmount, String gas,
                         String chainId,
                         String transferDenom,
                         String transferAmount,
                         String to,
                         String memo,
                         String privateKey) {
        try {
            init(privateKey);
            List<Token> amountList = new ArrayList<>();
            Token amount = new Token();
            amount.setDenom(feeDenom);
            amount.setAmount(feeAmount);
            amountList.add(amount);

            //组装待签名交易结构
            TransferMessage transferMessage = newMsgSend(transferDenom, transferAmount, address, to);
            Messages[] messages = newSendMsgs(transferDenom, transferAmount, address, to);

            Fee fee = new Fee();
            fee.setAmount(amountList);
            fee.setGas(gas);

            SignData signData = new SignData();
            signData.setAccountNumber(accountNum);
            signData.setChainId(chainId);
            signData.setFee(fee);
            signData.setMemo(memo);
            signData.setMsgs(new TransferMessage[] {transferMessage});
            signData.setSequence(sequenceNum);

            System.out.println("Msg to be signed:");
            System.out.println(signData.toJson());

            List<Signature> signatureList = new ArrayList<>();
            Signature signature = MsgBase.sign(signData, privateKey);
            signatureList.add(signature);

            BoardcastTx cosmosTransaction = new BoardcastTx();
            cosmosTransaction.setMode("block");

            TxValue cosmosTx = new TxValue();

            cosmosTx.setMsgs(messages);
            cosmosTx.setFee(fee);
            cosmosTx.setMemo(memo);
            cosmosTx.setSignatures(signatureList);

            cosmosTransaction.setTx(cosmosTx);

            boardcast(cosmosTransaction.toJson());
        } catch (Exception e) {
            System.out.println("serialize transfer msg failed");
        }
    }


    private Messages[] newSendMsgs(String denom, String amountDenom, String from, String to) {

        List<Token> amountList = new ArrayList<>();
        Token amount = new Token();
        amount.setDenom(denom);
        amount.setAmount(amountDenom);
        amountList.add(amount);

        SendMessage value = new SendMessage();
        value.setFromAddress(from);
        value.setToAddress(to);
        value.setAmount(amountList);

        Messages<SendMessage> msg = new Messages<>();

        msg.setType(msgType);
        msg.setValue(value);
        Messages[] msgs = new Messages[1];
        msgs[0]=msg;
        return msgs;
    }

    private TransferMessage newMsgSend(String denom, String amountDenom, String from, String to) {
        TransferMessage transferMessage = new TransferMessage();

        List<Token> amountList = new ArrayList<>();
        Token amount = new Token();
        amount.setDenom(denom);
        amount.setAmount(amountDenom);
        amountList.add(amount);

        Value value = new Value();
        value.setFromAddress(from);
        value.setToAddress(to);
        value.setAmount(amountList);

        transferMessage.setType(this.msgType);
        transferMessage.setValue(value);
        return transferMessage;
    }

}