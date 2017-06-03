/**
 * Copyright 2017 Isentropy LLC (http://isentropy.com)
 * Written by Jonathan Wolff (jwolff@isentropy.com)
 * Licensed under LGPL v3 (https://www.gnu.org/licenses/lgpl-3.0.en.html) 
 */

package com.isentropy.ethcrack;

import java.io.Reader;
import java.util.concurrent.ExecutorService;

import org.web3j.crypto.WalletFile;

public class EthcrackerOptions {
	//threads,pwqueueSize  < 1 means unspecified. they will be automatically set
	public int threads = -1;
	public int pwqueueSize = -1;
	
	public int updateProgressCount = 10;
	public String walletFile;
	public Reader passwords;
	public String foundPasswordFile;
	
	@Override
	public String toString(){
		return "walletFile = "+ walletFile+
				(foundPasswordFile == null ? "":"\nfoundPasswordFile = "+ foundPasswordFile)+
				"\nthreads = "+ threads+
				"\nqueueSize = "+ pwqueueSize;
	}
	
}
