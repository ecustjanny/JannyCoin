package com.janny.jannycoin;
/**
 * @Description:   Prototype code for Lib blockchain project 
 * 
 * @author          Janny (yonglin_guo@hotmail.com)
 * @version         V1.0  
 * @Date            04/06/2020
 */


import java.util.ArrayList;
import java.util.Date;

public class JannyBlock {

	/** 
	“区块类”中存放每次区块的信息，我们在该类中存放6个值：
	hash - 当前区块的哈希值
	previousHash - 前一个区块的hash值	
	merkleRoot -merkle树根。	
	ArrayList<Transaction> - 区块的所有交易
	timeStamp - 当前时间戳
	previousHash - 前一个区块的hash值 
	nonce - 记录当前区块的工作量(即通过多少次hash运算最后得到了符合条件的当前区块的哈希值)
	
	区块类主要的功能:
	1,计算区块Hash
	2,挖矿：找到符合条件的Hash值（Hash值前面几位为0）
	3,将交易加入到当前区块	
	*/

	public String hash;
	public String previousHash; 
	public String merkleRoot;
	public ArrayList<Transaction> transactions = new ArrayList<Transaction>(); 
	public long timeStamp; //as number of milliseconds since 1/1/1970.
	public int nonce;
	
	public JannyBlock(String previousHash ) {
		this.previousHash = previousHash;
		this.timeStamp = new Date().getTime();
		
		this.hash = calculateHash(); 
		
	}
	
	//计算当前区块内容的Hash值
	public String calculateHash() {
		String calculatedhash = JannyUtil.applySha256( 
				previousHash +
				Long.toString(timeStamp) +
				Integer.toString(nonce) + 
				merkleRoot
				);
		return calculatedhash;
	}
	
	//挖矿过程：每计算一次Hash值，nonce增加1
	public void mineBlock(int difficulty) {
		merkleRoot = JannyUtil.getMerkleRoot(transactions);
		String target = JannyUtil.getDificultyString(difficulty); //Create a string with difficulty * "0" 
		while(!hash.substring( 0, difficulty).equals(target)) {
			nonce ++;
			hash = calculateHash();
		}
		System.out.println("Block Mined!!! : " + hash);
	}
	
	//将交易加入到当前的区块
	public boolean addTransaction(Transaction transaction) {
		//process transaction and check if valid, unless block is genesis block then ignore.
		if(transaction == null) return false;		
		if((previousHash != "0")) {
			if((transaction.processTransaction() != true)) {
				System.out.println("Transaction failed to process. Discarded.");
				return false;
			}
		}
		transactions.add(transaction);
		System.out.println("Transaction Successfully added to Block");
		return true;
	}
	
}