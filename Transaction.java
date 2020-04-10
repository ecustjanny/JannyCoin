/**
 * @Description:   Prototype code for Lib blockchain project 
 * 
 * @author          Janny (yonglin_guo@hotmail.com)
 * @version         V1.0  
 * @Date            03/19/2020
 */
 
package com.janny.jannycoin;

import java.security.*;
import java.util.ArrayList;

/**
 	交易类中包含下列信息。
    1、付款人的公钥信息
    2、收款人的公钥信息
    3、转移的资金数量
    4、输入，之前发生的交易，用来查询付款人的当前余额
	5、输出，显示了在这次交易中接受的相关地址（这些输出被参考为新的交易中的输入）       	  6、这笔交易的数字签名
	
	交易类中功能：
   	1、对交易信息做Hash摘要计算
    2、对交易做数字签名
    3、验证交易的数字签名
    4、处理交易：验证有足够的余额，并进行实际的转账

  */

public class Transaction {
	
	//交易ID，我们用交易的Hash值来作为交易标识ID
	public String transactionId; 
	//转账发起方的地址，通常是他的公钥	
	public PublicKey sender; 
	//转账接受方的地址，通常是他的公钥	
	public PublicKey reciepient; 

	//下面是字符串格式的公钥，便于打印和分析用。	
	public String senderStr; 
	public String reciepientStr; 
	
	//转账金额	
	public float value;
	//数字签名	
	private byte[] signature; 
	
	//我的所有交易的输入列表（转账给我的所有交易）	
	public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
	//我的所有交易的输出列表（本次转账转出的交易，包括给自己的）	
	public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
	//交易数量记录
	private static int sequence = 0; 

	public Transaction(PublicKey from, PublicKey to, float value,  ArrayList<TransactionInput> inputs) {
		this.sender = from;
		this.reciepient = to;
		this.value = value;
		this.inputs = inputs;
		
		//Janny added for debug
		this.senderStr = JannyUtil.getStringFromKey(from);
		this.reciepientStr = JannyUtil.getStringFromKey(to);
	
		
	}
	
	// 计算当前交易的Hash值，并用作交易的ID.
	private String calulateHash() {
		sequence++; //increase the sequence to avoid 2 identical transactions having the same hash
		return JannyUtil.applySha256(
				JannyUtil.getStringFromKey(sender) +
				JannyUtil.getStringFromKey(reciepient) +
				Float.toString(value) + sequence
				);
	}
	
	//对当前交易做数字签名，以便确定是发送者自己发出的交易.
	public void generateSignature(PrivateKey privateKey) {
		String data = JannyUtil.getStringFromKey(sender) + JannyUtil.getStringFromKey(reciepient) + Float.toString(value)	;
		signature = JannyUtil.applyECDSASig(privateKey,data);
	}
	//验证当前交易签名
	public boolean verifiySignature() {
		String data = JannyUtil.getStringFromKey(sender) + JannyUtil.getStringFromKey(reciepient) + Float.toString(value)	;
		return JannyUtil.verifyECDSASig(sender, data, signature);
	}
	
	
	//Returns true if new transaction could be created.	
	public boolean processTransaction() {
		
		if(verifiySignature() == false) {
			System.out.println("#Transaction Signature failed to verify");
			return false;
		}
				
		//gather transaction inputs (Make sure they are unspent):
		for(TransactionInput i : inputs) {
			i.UTXO = JannyCoin.UTXOs.get(i.transactionOutputId);
		}

		//check if transaction is valid:
		if(getInputsValue() < JannyCoin.minimumTransaction) {
			System.out.println("#Transaction Inputs to small: " + getInputsValue());
			return false;
		}
		
		//generate transaction outputs:
		float leftOver = getInputsValue() - value; //get value of inputs then the left over change:
		transactionId = calulateHash();
		outputs.add(new TransactionOutput( this.reciepient, value,transactionId)); //send value to recipient
		outputs.add(new TransactionOutput( this.sender, leftOver,transactionId)); //send the left over 'change' back to sender		
				
		//add outputs to Unspent list
		for(TransactionOutput o : outputs) {
			JannyCoin.UTXOs.put(o.id , o);
		}
		
		//remove transaction inputs from UTXO lists as spent:
		for(TransactionInput i : inputs) {
			if(i.UTXO == null) continue; //if Transaction can't be found skip it 
			JannyCoin.UTXOs.remove(i.UTXO.id);
		}
		
		return true;
	}
	
	//计算所有Input交易的总和（也就是计算自己的可用余额）
	public float getInputsValue() {
		float total = 0;
		for(TransactionInput i : inputs) {
			if(i.UTXO == null) continue; //if Transaction can't be found skip it 
			total += i.UTXO.value;
		}
		return total;
	}

	//计算所有Output交易的总和（也就是计算自己本次转出去的金额数量，可能存在多笔）
	public float getOutputsValue() {
		float total = 0;
		for(TransactionOutput o : outputs) {
			total += o.value;
		}
		return total;
	}

}