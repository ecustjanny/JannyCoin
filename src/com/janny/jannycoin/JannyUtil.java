/**
 * @Description:   Prototype code for JannyCoin project 
 * 
 * @author          Janny (yonglin_guo@hotmail.com)
 * @version         V1.0  
 * @Date            04/06/2020
 */ 
package com.janny.jannycoin;

import java.util.ArrayList;
import java.util.Date;
import java.util.Base64;

import java.text.SimpleDateFormat;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.Key;
import java.security.PublicKey;
import java.security.PrivateKey;
 
import com.google.gson.GsonBuilder;
import com.janny.jannycoin.Transaction;

/**
 * 工具类
 * 创建数字签名、验证数字签名、获得Merkle根
 * 返回JSON格式数据、返回难度字符串目标
  */
public class JannyUtil {
	
	/*计算一个字符串的的Hash值，将Sha256应用到一个字符串并返回结果; 
	 * 	MessageDigest.getInstance("MD5"); MD5 产生一个32位字符串
	 * 	MessageDigest.getInstance("SHA-1"); SHA-1 产生一个40位字符串
	 */	
	public static String applySha256(String input){
		
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
 
			byte[] hash = digest.digest(input.getBytes("UTF-8"));
	        
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if(hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	//返回JSON格式的数据，用来打印区块或者整个区块链内容
	public static String getJson(Object o) {
		return new GsonBuilder().setPrettyPrinting().create().toJson(o);
	}
	
	//返回难度字符串目标，与散列比较。例如难度5将返回“00000”  
	public static String getDificultyString(int difficulty) {
		return new String(new char[difficulty]).replace('\0', '0');
	}
	
	//对于字符串做数字签名 ECDSA Signature.
	public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
		Signature dsa;
		byte[] output = new byte[0];
		try {
			dsa = Signature.getInstance("ECDSA", "BC");
			dsa.initSign(privateKey);
			byte[] strByte = input.getBytes();
			dsa.update(strByte);
			byte[] realSig = dsa.sign();
			output = realSig;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return output;
	}
	
	//验证一个字符串的数字签名 
	public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
		try {
			Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
			ecdsaVerify.initVerify(publicKey);
			ecdsaVerify.update(data.getBytes());
			return ecdsaVerify.verify(signature);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	//将公钥或者私钥转换成字符串，便于打印查看
	public static String getStringFromKey(Key key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}
	
	//遍历数组，获得Merkle根。
	public static String getMerkleRoot(ArrayList<Transaction> transactions) {
		int count = transactions.size();
		ArrayList<String> previousTreeLayer = new ArrayList<String>();
		for(Transaction transaction : transactions) {
			previousTreeLayer.add(transaction.transactionId);
		}
		ArrayList<String> treeLayer = previousTreeLayer;
		while(count > 1) {
			treeLayer = new ArrayList<String>();
			for(int i=1; i < previousTreeLayer.size(); i++) {
				treeLayer.add(applySha256(previousTreeLayer.get(i-1) + previousTreeLayer.get(i)));
			}
			count = treeLayer.size();
			previousTreeLayer = treeLayer;
		}
		String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
		String myObject = JannyUtil.getJson(treeLayer);
		System.out.println("treeLayer:" +myObject);

		return merkleRoot;  
	}

	//取得当前时期和时间的字符串格式。
	public static String getCurrentDateStr() { 
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(new Date());
		return dateString;
	}	
	
	//简单测试程序
	public static void main(String[] args) {	

		System.out.println("Sha265 of Janny....... "+applySha256("Janny"));
		System.out.println("getDificultyString of 6....... "+getDificultyString(6));
		System.out.println("Datetime....... "+getCurrentDateStr());

		byte[] bytes1 = "Hello Janny".getBytes();
 		byte[] bytes2 = "上海".getBytes();		
		String string = new String(bytes1);
		System.out.println("Hello Janny....... "+bytes1);
		System.out.println("Hello Janny....... "+new String(bytes1));
		
		 
	}
}