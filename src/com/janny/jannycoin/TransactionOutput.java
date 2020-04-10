package com.janny.jannycoin;
/**
 * @Description:   Prototype code for Lib blockchain project 
 * 
 * @author          Janny (yonglin_guo@hotmail.com)
 * @version         V1.0  
 * @Date            03/19/2020
 */
 
import java.security.PublicKey;


public class TransactionOutput {
	
	//交易输出ID
	public String id;
	//接收人对的公钥地址
	public PublicKey reciepient; 
	//本次交易转给该账户金额
	public float value; //the amount of coins they own
	//父交易ID。通常一个父交易要做拆分才能完成转账，因为没有正好的金额（别人转给自己的金额）
	public String parentTransactionId; //the id of the transaction this output was created in

	public String reciepientStr; // senders address/public key.

	
	//交易输出构造方法
	public TransactionOutput(PublicKey reciepient, float value, String parentTransactionId) {
		this.reciepient = reciepient;
		this.value = value;
		this.parentTransactionId = parentTransactionId;
		this.id = JannyUtil.applySha256(JannyUtil.getStringFromKey(reciepient)+Float.toString(value)+parentTransactionId);
		
		this.reciepientStr = JannyUtil.getStringFromKey(reciepient);

	}
	
	//检查当前转账是否是给我的（通过判断转账的公钥等于我的公钥，即确定是的）
	public boolean isMine(PublicKey publicKey) {
		return (publicKey == reciepient);
	}
	
}