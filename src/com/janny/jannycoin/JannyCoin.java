package com.janny.jannycoin;
/**
 * @Description:   Prototype code for Lib blockchain project 
 * 
 * @author          Janny (yonglin_guo@hotmail.com)
 * @version         V1.0  
 * @Date            03/19/2020
 */
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

import com.janny.jannycoin.JannyBlock;
import com.janny.jannycoin.JannyUtil;

/**
 * 创建钱包
 * 开始多笔转账
 * 打印区块链JSON格式数据，分析结果
  */
public class JannyCoin {
	public static ArrayList<JannyBlock> blockchain = new ArrayList<JannyBlock>();
	
	public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();

	public static int difficulty = 0;
	public static float minimumTransaction = 0.1f;
	public static Wallet walletA;
	public static Wallet walletB;
	public static Transaction genesisTransaction;
	public static void main(String[] args) {
		
		//Setup Bouncey castle as a Security Provider
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); 
		
		//创建用户A和用户B的两个钱包:
		walletA = new Wallet();
		walletB = new Wallet();		
		Wallet mywallet = new Wallet();
		
		
		//创建区块链的创世块，并给A钱包初始为10个jannycoin币
		genesisTransaction = new Transaction(mywallet.publicKey, walletA.publicKey, 10f, null);
		genesisTransaction.generateSignature(mywallet.privateKey);	
		genesisTransaction.transactionId = "0"; 
		
		//将10元的交易放置在用户A的数组中
		genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.reciepient, genesisTransaction.value, genesisTransaction.transactionId)); 
		UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); 
		
		JannyBlock genesis = new JannyBlock("0");
		genesis.addTransaction(genesisTransaction);
		addBlock(genesis);
		System.out.println("block0 added......"+isChainValid());
		
		
		//A 转给 B 4元
		JannyBlock myblock;
		myblock = new JannyBlock(genesis.hash);
		myblock.addTransaction(walletA.sendFunds(walletB.publicKey, 4f));
		addBlock(myblock);

		//A 转给 B 1元
		myblock = new JannyBlock(genesis.hash);
		myblock.addTransaction(walletA.sendFunds(walletB.publicKey, 1f));
		addBlock(myblock);

		//B 转给 A 2元
		myblock = new JannyBlock(myblock.hash);
		myblock.addTransaction(walletB.sendFunds(walletA.publicKey, 2f));
		addBlock(myblock);


		/* 		
		尝试过错误的交易（金额不足）区块的测试案例，验证交易的有效性
		myblock = new JannyBlock(previousVaildBlockHash);
		myblock.addTransaction(walletA.sendFunds(walletB.publicKey, 20f));
		System.out.println("block valid？......"+isChainValid());
		System.out.println(JannyUtil.getJson(myblock));
		*/		
		
		System.out.println("--------------");
		System.out.println(JannyUtil.getJson(blockchain));
		
	}

	public static Boolean isChainValid() {
		JannyBlock currentBlock; 
		JannyBlock previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
		HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); //a temporary working list of unspent transactions at a given block state.
		tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
		
		//loop through blockchain to check hashes:
		for(int i=1; i < blockchain.size(); i++) {
			
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i-1);
			//compare registered hash and calculated hash:
			if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
				System.out.println("#Current Hashes not equal");
				return false;
			}
			//compare previous hash and registered previous hash
			if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
				System.out.println("#Previous Hashes not equal");
				return false;
			}
			//check if hash is solved
			if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
				System.out.println("#This block hasn't been mined");
				return false;
			}
			
			//loop thru blockchains transactions:
			TransactionOutput tempOutput;
			for(int t=0; t <currentBlock.transactions.size(); t++) {
				Transaction currentTransaction = currentBlock.transactions.get(t);
				
				if(!currentTransaction.verifiySignature()) {
					System.out.println("#Signature on Transaction(" + t + ") is Invalid");
					return false; 
				}
				if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
					System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
					return false; 
				}
				
				for(TransactionInput input: currentTransaction.inputs) {	
					tempOutput = tempUTXOs.get(input.transactionOutputId);
					
					if(tempOutput == null) {
						System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
						return false;
					}
					
					if(input.UTXO.value != tempOutput.value) {
						System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
						return false;
					}
					
					tempUTXOs.remove(input.transactionOutputId);
				}
				
				for(TransactionOutput output: currentTransaction.outputs) {
					tempUTXOs.put(output.id, output);
				}
				
				if( currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
					System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
					return false;
				}
				if( currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
					System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
					return false;
				}
				
			}
			
		}
		System.out.println("Blockchain is valid");
		return true;
	}

	public static void addBlock(JannyBlock newBlock) {
		newBlock.mineBlock(difficulty);
		blockchain.add(newBlock);
	}
}
