/**
 * @Description:   Prototype code for Lib blockchain project 
 * 
 * @author          Janny (yonglin_guo@hotmail.com)
 * @version         V1.0  
 * @Date            03/19/2020
 */
 
package com.janny.jannycoin;
 
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/* 	
钱包类
生成公钥和私钥
计算自己的余额
执行转账
*/	
public class Wallet {
	//当前钱包持有人的私钥
	public PrivateKey privateKey;
	//当前钱包持有人的公钥	
	public PublicKey publicKey;
	//当前钱包持有人的UTXOs：所以转给自己的交易中还没有转给别人的交易。	
	public HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();	
	
	public Wallet(){
		generateKeyPair();	
	}
	
	// 产生公钥和私钥。
	public void generateKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
			// Initialize the key generator and generate a KeyPair
			keyGen.initialize(ecSpec, random);   //256 bytes provides an acceptable security level
	        	KeyPair keyPair = keyGen.generateKeyPair();
	        	// Set the public and private keys from the keyPair
	        	privateKey = keyPair.getPrivate();
	        	publicKey = keyPair.getPublic();
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	//得到自己的余额，就是将所有自己的UTXOs相加。
	public float getBalance() {
		float total = 0;	
        for (Map.Entry<String, TransactionOutput> item: JannyCoin.UTXOs.entrySet()){
        	TransactionOutput UTXO = item.getValue();
            if(UTXO.isMine(publicKey)) { //if output belongs to me ( if coins belong to me )
            	UTXOs.put(UTXO.id,UTXO); //add it to our list of unspent transactions.
            	total += UTXO.value ; 
            }
        }  
		return total;
	}

	//给某人转账。
	public Transaction sendFunds(PublicKey _recipient,float value ) {
		if(getBalance() < value) { //gather balance and check funds.
			System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
			return null;
		}
    //create array list of inputs
		ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
    
		float total = 0;
		for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()){
			TransactionOutput UTXO = item.getValue();
			total += UTXO.value;
			inputs.add(new TransactionInput(UTXO.id));
			if(total > value) break;
		}
		
		Transaction newTransaction = new Transaction(publicKey, _recipient , value, inputs);
		newTransaction.generateSignature(privateKey);
		
		for(TransactionInput input: inputs){
			UTXOs.remove(input.transactionOutputId);
		}
		return newTransaction;
	}
	
	public static void main(String[] args) {	
	
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Setup Bouncey castle as a 
		Wallet wallet = new Wallet();
		
		//生成公钥和私钥
		wallet.generateKeyPair();

		//装换成字符创打印出来		
		System.out.println("公钥....... "+JannyUtil.getStringFromKey(wallet.publicKey));
		System.out.println("私钥....... "+JannyUtil.getStringFromKey(wallet.privateKey));
		
		//用我的私钥对于字符串“JannyDocs”签名
		byte[] bytes = JannyUtil.applyECDSASig(wallet.privateKey, "JannyDocs");
		System.out.println("签名结果....... " + bytes);
		
		System.out.println("My Wallet....... " + JannyUtil.getJson(wallet));
		
	
		//System.out.println("getBalance....... "+wallet.getBalance());
		
		
		/* privateKey....... MHsCAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQEEYTBfAgEBBBjZ47PPgbI9ZJi/gZR7LjnBhQKGzFm2+fygCgYIKoZIzj0DAQGhNAMyAAQlXjTEk4olZy6KLOv0AVhC2hFg7TGvz+LaXTXWSD2AjeRJoTnBVFZH30Uf8nmNewI=
		publicKey....... MEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAEJV40xJOKJWcuiizr9AFYQtoRYO0xr8/i2l011kg9gI3kSaE5wVRWR99FH/J5jXsC		
		*/

		
		
	}
	
}