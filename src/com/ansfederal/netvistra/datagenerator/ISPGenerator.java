package com.ansfederal.netvistra.datagenerator;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import netcat.NetCat;
import au.com.bytecode.opencsv.CSVReader;

public class ISPGenerator implements Runnable {

	private static final String BASH_PRE_PROC = "#!/bin/bash";
	private static final String BASH_DASH_C = "bash -c ";
	private static final String CURL_PART1 = "\"curl -k -u ";
	private static final String CURL_LOGIN_SEP = ":";
	private static final String CURL_PART2 = " -d \"name=";
	private static final String CURL_PART3 = "\" -d \"=\" ";
	private static final String CURL_PART4 = "http://";
	private static final String CURL_PART5 = "/servicesNS/admin/netvistra/data/inputs/tcp/raw\"";
	private static final String NC_PART1 = "echo \"$1\" | nc localhost ";
	private static final String EXIT = "exit";
	private static String SCRIPT = "";
	
	// reference
	private static final String CURL = 
			"bash -c \"curl -k -u admin:Tester123 -d \"name=3001\" " +
			"-d \"=\" "+
			"http://localhost:8000/servicesNS/admin/netvistra/data/inputs/tcp/raw\"";

	private static final String RESOURCE_DIR = "/resources/";
	
	private static final String HOUR = "hour";
	
	private HashMap<String,String> map;
	static final int sampleCount = 2;
	private int rowCount;
	static final Runtime r = Runtime.getRuntime();
	private int fileCount;
	private String user;
	private String password;
	private String host;
	private int genport;
	private int webport;
	
	// x, 3001, admin, Tester123, localhost, 8000
	public ISPGenerator(int fileCount, int genport, String user, String password, String host, int webport) {
		this.fileCount = fileCount;
		this.genport = genport;
		this.user = user;
		this.password = password;
		this.webport = webport;
		this.host = host;
		
		SCRIPT = 
			//BASH_PRE_PROC + "\n" +
			BASH_DASH_C +
			CURL_PART1 + getUser() +
			CURL_LOGIN_SEP + getPassword() +
			CURL_PART2 + getGenport() +
			CURL_PART3 + 
			CURL_PART4 + getHost() + ":" + getWebport() +
			CURL_PART5;
		
		System.out.println(SCRIPT);
		System.out.println(CURL);
	}
	
	private int getFileCount() {
		return this.fileCount;
	}
	
	private int getGenport() {
		return this.genport;
	}
	
	private int getWebport() {
		return this.webport;
	}
	
	private String getUser() {
		return this.user;
	}
	
	private String getPassword() {
		return this.password;
	}
	
	private String getHost() {
		return this.host;
	}
	
	private void createMap(String filename) {
		
		// read csv
		String csvFilename = RESOURCE_DIR+filename;
		Reader paramReader = new InputStreamReader(getClass().getResourceAsStream(csvFilename));
		//InputStreamReader isr = ResourceLoader.getResourceISR(MODIFIED_DATA_DIR+filename);
		CSVReader csvReader = null;
		try {
			//System.out.println(FileUtils.toFile(getClass().getResource(csvFilename)));
			csvReader = new CSVReader(paramReader);
		} catch (Exception e) {

			e.printStackTrace();
		}
		String[] tRow = null;
		map = new HashMap<String,String>();
		
		// loop through each hourX file
		rowCount = 0;

		try {
			while((tRow = csvReader.readNext()) != null) {
				
				final String[] row = tRow;
				String line = null;
				//this.sampleCount=Integer.parseInt(row[11]);
							
				ArrayList<Double> in = NormalDistVals.sampleVals (Double.parseDouble(row[5]), Double.parseDouble(row[6]), sampleCount);
				ArrayList<Double> out = NormalDistVals.sampleVals (Double.parseDouble(row[9]), Double.parseDouble(row[10]), sampleCount);

				// Create new lines. Instead avgIn/avgOut - we'll have MbpsIn/MbpsOut
				for(int i=0;i<sampleCount;i++) {

					double iValue = in.get(i);
					double oValue = out.get(i);

					line = 
							row[0]+","+row[1]+","+row[2]+","+
							row[3]+","+row[4]+","+iValue+","+
							row[6]+","+row[7]+","+row[8]+","+
							oValue+","+row[10]+","+row[11];
					
					map.put("reporter"+rowCount+"t"+i, line);
					
				}
				rowCount++;
			}
		} catch (NumberFormatException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
			
			
	}

	
	public void printLines() {
		//System.out.println(this.map.toString());
		for(int i=0;i<rowCount;i++) {
			for(int j=0;j<sampleCount;j++) {
				System.out.println(map.get("reporter"+i+"t"+j));
			}
		}
	}
	
	@Override
	public void run() {
		
		Process p=null;
		String command = CURL;
		try {
			//System.out.println(CURL);
			p = r.exec(command);

		} catch (IOException e1) {

			e1.printStackTrace();
		}
		
		this.createMap(HOUR+this.getFileCount());
		for(int i=0;i<sampleCount;i++) {
			for(int j=0;j<rowCount;j++) {
				try {
					NetCat.push(getHost(), getGenport(), map.get("reporter"+j+"t"+i));
					Thread.sleep(500);
				
				} catch (Exception e) {

					e.printStackTrace();
				}
			}
			try {
				Thread.sleep((60*1000)/sampleCount);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		
		int genport = 3001,webport=8000;
		String username="admin",password="Tester123",host="localhost";
		if (args.length > 0) {
		    try {
		    	// 3001, admin, Tester123, localhost, 8000
		        genport = Integer.parseInt(args[0]);
		        username = args[1];
		        password = args[2];
		        host = args[3];
		        webport = Integer.parseInt(args[4]);
		        
		    } catch (NumberFormatException e) {
		        System.err.println("Arguments 1 and 5" + " must be an integer. Example: java -jar datagen 3001 admin password 8000");
		        System.exit(1);
		    }
		}
		
		if (args.length > 1 && args.length < 5) {
			System.err.println("Missing an argument. Example: java -jar datagen 3001 admin password localhost 8000");
	        System.exit(1);
		}
		
		DateFormat dateFormat = new SimpleDateFormat("HH");
		Date date = new Date();
		
		int start_time = Integer.parseInt(dateFormat.format(date));
		
		boolean debug = false;
		if(debug) {
			ISPGenerator isp = new ISPGenerator(1,genport,username,password,host,webport);
		}
		while(true && !debug) {
			for(int i=start_time;i<24;i++) {
				ISPGenerator isp = new ISPGenerator(i,genport,username,password,host,webport);
				try {
					Thread t = new Thread(isp);
					t.start();
					Thread.sleep(1000*60*60);
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
		
			}
		}
	}


	
}
	
