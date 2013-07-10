package com.ansfederal.netvistra.datagenerator;

import java.util.ArrayList;

public enum NormalDistVals {
	NORMAL_HIGH (0.68), // 68 percent of the sample pop
	NORMAL_MED (0.27),
	NORMAL_LOW (0.5);
	
	private final double weight;
	
	NormalDistVals(double weight) {
		this.weight = weight;
	}
	
	public double weight() {
		return weight;
	}
	
	private static int[] sampleVals ( int sampleCount ) {
		int[] vals = new int[NormalDistVals.values().length];
		int pos=0;
		for(NormalDistVals n: NormalDistVals.values()) { // enum guarantees order as items are declared.
			vals[pos] = (int) Math.round(n.weight() * sampleCount);
		}
		return vals;
	}
	
	// Returns a list (l) of numbers (x1...xN)
	// 	where l.length = sampleCount
	//	and x = random number
	//	the distribution of x...n in l is based on:
	//	68% for 1 stdev
	//	27% for 2 stdev
	//	5% for 3 stdev
	//	where all numbers are randomly positioned in (l)
	//	Example samplecount = 5, mean = 10, stdev = 1, l = [7,10,9,11,8]
	public static ArrayList<Double> sampleVals ( double mean, double stdev, int sampleCount ) {
		int[] vals = sampleVals(sampleCount);
		ArrayList<Double> l = new ArrayList<Double>();
		int valSize = vals.length;
		for(int i=0;i<=valSize;i++) {
			// HIGH
			for(int j=0;i==0 && j<=vals[i];j++) {
				double val = mean-stdev + (int) (Math.random() * ( 2*stdev ) + 1);  // produce a random number within a specified range (max-min or 2*stdev)
				l.add(val);
			}
			// MED
			if(i==1) {
				 
				int halfMed = Math.round(vals[i]/2); // half the values are low 13% and other half is in the top 13%
				for(int j=0;j<=halfMed;j++) {
					double val = mean-2*stdev + (int) (Math.random() * ( stdev ) + 1);
					l.add(val);
				}
				if((sampleCount & 1) == 0) halfMed++; // if odd, we get odd items, if even we get even items - 1..
				for(int j=0;j<=halfMed;j++) {
					double val = mean+2*stdev + (int) (Math.random() * ( stdev ) + 1);  
					l.add(val);
				}
			}
			// LOW
			if(i==2) {
				int halfLow = Math.round(vals[i]/2); // half the values are low 5% and other half is in the top 5%
				for(int j=0;i==2 && j<=halfLow;j++) {
					double val = mean-3*stdev + (int) (Math.random() * ( stdev ) + 1);
					l.add(val);
				}
				for(int j=0;i==2 && j<=halfLow;j++) {
					double val = mean+3*stdev + (int) (Math.random() * ( stdev ) + 1);  
					l.add(val);
				}
			}
		}
		//System.out.println(l);
		return distributedVals(l);
	}
	
	private static ArrayList<Double> distributedVals(ArrayList<Double> sampleVals) {
		ArrayList<Double> l = new ArrayList<Double>();
		while(!sampleVals.isEmpty()) {
			int idx = (int) (Math.random()*sampleVals.size());
			l.add(sampleVals.remove(idx));
		}
		return l;
	}
	
	public static void main(String[] args) {
		ArrayList<Double> l = sampleVals ( 719.77, 61.98, 12 );
		System.out.println(l);
	}
}

