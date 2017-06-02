/**
 * Copyright 2017 Isentropy LLC (http://isentropy.com)
 * Written by Jonathan Wolff
 * Licensed under LGPL v3 (https://www.gnu.org/licenses/lgpl-3.0.en.html) 
 */


package com.isentropy.ethcrack;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.Charsets;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.protocol.ObjectMapperFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Ethcracker {
	
	public static final String INFO = "Ethcracker v0.1 - Copyright 2017 Isentropy LLC (https://isentropy.com)\n"+
									  "----------------------------------------------------------------------";

	
	protected int waitTimeMs = 1000;
	protected EthcrackerOptions opts;
	protected WalletFile wallet;
	protected ExecutorService executor;
	protected ArrayBlockingQueue<String> pws;
	protected String foundPassword = null;
	protected AtomicInteger triedPasswds = new AtomicInteger(0);
	
	class Consumer implements Runnable {  
		public void run() {  
			try {
				while(foundPassword == null){
					String pw = pws.poll(waitTimeMs, TimeUnit.MILLISECONDS);
					if(pw == null)
						continue;
					if(test(wallet, pw)){
						foundPassword = pw;
						executor.shutdown();
					}
					else{
						int n = triedPasswds.incrementAndGet();
						if(opts.updateProgressCount > 0 && n % opts.updateProgressCount == 0)
							System.out.print("Tried passwords: "+n + "\r");

						//System.out.println("Tried: "+ pw);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				executor.shutdown();
			}
		}  
	}  

	class Producer implements Runnable {  
		public void run() {  
			int n=0;
			BufferedReader br = new BufferedReader(opts.passwords);
			String l;
			try{
				while((l = br.readLine()) != null){
					n++;

					String pw = l.trim();
					while(!pws.offer(pw, waitTimeMs, TimeUnit.MILLISECONDS)){
						if(foundPassword != null)
							return;
					}
				}
			}
			catch(Exception e){
				e.printStackTrace();
				executor.shutdown();
			}
		}  
	}  

	public Ethcracker(EthcrackerOptions o) throws JsonParseException, JsonMappingException, IOException {
		opts = o;
		executor  = Executors.newFixedThreadPool(opts.threads+1);
		pws = new ArrayBlockingQueue<String>(opts.pwqueueSize);
		ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
		wallet = objectMapper.readValue(new File(opts.walletFile), WalletFile.class);
	}

	public void start() throws IOException{
		executor.execute(new Producer());
		for(int i=0; i< opts.threads; i++){
			executor.execute(new Consumer());
		}
		while(!executor.isTerminated()){
			try {
				Thread.sleep(waitTimeMs);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(foundPassword != null){
			System.out.println("\nFOUND PASSWORD:\t" + foundPassword);
			if(opts.foundPasswordFile != null){
				FileOutputStream fos = new FileOutputStream(opts.foundPasswordFile);
				fos.write(foundPassword.getBytes(Charsets.UTF_8));
				fos.flush();
				fos.close();
			}
		}
	}

	public boolean test(WalletFile wf, String pw) throws IOException, InterruptedException{
		try{
			Wallet.decrypt(pw, wf);
		}
		catch(Exception e){
			return false;
		}
		return true;
	}

	public static EthcrackerOptions parseArgs(String[] args) throws JsonParseException, JsonMappingException, IOException{
		EthcrackerOptions o =  new EthcrackerOptions();

		for(int i=0;i<args.length;i++){
			if(args[i].equals("--pwfile")){
				o.passwords = new InputStreamReader(new FileInputStream(args[++i]),Charsets.UTF_8);
			}
			else if(args[i].equals("--wallet")){
				o.walletFile = args[++i];
			}
			else if(args[i].equals("--threads")){
				o.threads = Integer.parseInt(args[++i]);
			}
			else if(args[i].equals("--out")){
				o.foundPasswordFile = args[++i];
			}		
		}
		if(o.threads < 1){
			o.threads = Runtime.getRuntime().availableProcessors()*2;
		}
		if(o.pwqueueSize < 1){
			o.pwqueueSize = o.threads*1000;
		}

		// use stdin for password file by default
		if(o.passwords == null){
			o.passwords = new InputStreamReader(System.in,Charsets.UTF_8);
		}
		return o;
	}

	public static void main(String[] args){
		try {
			EthcrackerOptions opts = parseArgs(args);
			System.out.println('\n'+INFO);
			if(opts.walletFile == null){
				System.err.println("Must pass a wallet file with --wallet. Exiting.");
				System.exit(2);
			}
			System.out.println("Options:\n"+opts+"\n");
			Ethcracker ec = new Ethcracker(opts);
			ec.start();
			if(ec.foundPassword == null)
				System.exit(1);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(3);			
		}
	}
}
