package com.janny.jannycoin;
/**
 * @Description:   Prototype code for Lib blockchain project 
 * 
 * @author          Janny (yonglin_guo@hotmail.com)
 * @version         V1.0  
 * @Date            04/08/2020
 */
public class TransactionInput {
	
	
	public String transactionOutputId; //Reference to TransactionOutputs -> transactionId
	//所有未使用的交易输出称为UTXO
	public TransactionOutput UTXO; //Contains the Unspent transaction output
	
	public TransactionInput(String transactionOutputId) {
		this.transactionOutputId = transactionOutputId;
	}
}