package com.ansfederal.netvistra.datagenerator;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;

import au.com.bytecode.opencsv.CSVReader;

public class DataGenerator {
	static Runtime r = Runtime.getRuntime();
	
	public static int threadCount = 0;

	public class OneSiteThread implements Runnable {
		String filename;
		Process p=null;
		int sampleCount;
		
		OneSiteThread(String filename) {
			this.filename = filename;
		}

		public void run() {
			try {
				
				// read csv
				String csvFilename = "resources/hourly/"+filename;
				CSVReader csvReader;
				csvReader = new CSVReader(new FileReader(csvFilename));
				String[] tRow = null;
				//while((tRow = csvReader.readNext()) != null && threadCount<=1500) {
				while((tRow = csvReader.readNext()) != null) {
					//System.out.println(threadCount++);
					final String[] row = tRow;
					Thread t = new Thread() {
						
						public void run() {
							
							try {
								//Discard the original line 
//								System.out.println(
//								row[0]+","+row[1]+","+row[2]+","+
//								row[3]+","+row[4]+","+row[5]+","+
//								row[6]+","+row[7]+","+row[8]+","+
//								row[9]+","+row[10]+","+row[11]);
								sampleCount=Integer.parseInt(row[11]);
								ArrayList<Double> in = 
										NormalDistVals.sampleVals (Double.parseDouble(row[5]), Double.parseDouble(row[6]), sampleCount);
								ArrayList<Double> out = 
										NormalDistVals.sampleVals (Double.parseDouble(row[9]), Double.parseDouble(row[10]), sampleCount);
								//System.out.println("inbound: "+in);
								//System.out.println("outbound: "+out);
								Iterator<Double> it1 = in.iterator();
								Iterator<Double> it2 = out.iterator();

								//while(it1.hasNext() && it2.hasNext()) {
									String line = null;
									double iValue = it1.next();
									double oValue = it2.next();
									for(int i=0;i<=row.length;i++) {
										line = 
											row[0]+","+row[1]+","+row[2]+","+
											row[3]+","+row[4]+","+iValue+","+
											row[6]+","+row[7]+","+row[8]+","+
											oValue+","+row[10]+","+row[11];
									}

									p = r.exec("resources/./netchunks "+line);
									//Thread.sleep((1000*60*60)/sampleCount); // We're holding off on this for a more powerful machine
									//if(p != null) p.destroy();
									
									//System.out.println(line);
								//}

							} catch (Exception e) {
								System.err.println(e.toString());
							}

						}
					};
					t.start();
					Thread.sleep(10);
				}
				csvReader.close();
				
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}
	
	}
	
	public static void main(String[] args) {
		System.out.println("Main thread starting.");
		DataGenerator dg = new DataGenerator();
		while(true) {
			for(int i=1;i<=24;i++) {
				DataGenerator.OneSiteThread ost = dg.new OneSiteThread("hour"+i);
				try {
					Thread newThrd = new Thread(ost);
					newThrd.start();
					Thread.sleep(1000*60*60);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
			}
		}
	}
	
}
	
