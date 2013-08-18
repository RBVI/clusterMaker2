package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.main;
import java.util.Hashtable;


public class Args {
	
	String[] args;
	String optionIndicator = "-";
	Hashtable<String, String> options = new Hashtable<String, String>();
	
	public Args(String[] args) {
		this.args = args;
		
	}
	
	public Args(String[] args, String optionIndicator) throws ArgsParseException {
		this(args);
		this.optionIndicator = optionIndicator;
		makeOptionsHash();
	}
	
	public void makeOptionsHash() throws ArgsParseException {
		
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith(this.optionIndicator)) {
				
				String key = args[i].substring(this.optionIndicator.length());
				
				try{
					String value = args[i+1];
					if (value.startsWith(this.optionIndicator)) {
						throw new ArgsParseException("Value " + value + " invalid for parameter " + key + ".");
					}
					options.put(key, args[i+1]);
					i++;
				}catch (Exception e) {
					options.put(key, "");
				}		
				
			}
		}	
	}
	
	public String getStringValue(String key) throws ArgsParseException {
		
		String value = this.options.get(key);
		if (value == null) {
			throw new ArgsParseException("Key " + key + " unknown.");
		} else {
			return value;
		}
		
	}
	
	public int getIntValue(String key) throws ArgsParseException {
		
		String value = this.options.get(key);
		if (value == null) {
			throw new ArgsParseException("Key " + key + " unknown.");
		} else {
			return Integer.parseInt(value);
		}
		
	}
	
	public double getDoubleValue(String key) throws ArgsParseException {
		
		String value = this.options.get(key);
		if (value == null) {
			throw new ArgsParseException("Key " + key + " unknown.");
		} else {
			return Double.parseDouble(value);
		}
		
	}
	
	public float getFloatValue(String key) throws ArgsParseException {
		
		String value = this.options.get(key);
		if (value == null) {
			throw new ArgsParseException("Key " + key + " unknown.");
		} else {
			return Float.parseFloat(value);
		}
		
	}
	
	public boolean getBoolValue(String key) throws ArgsParseException {
		
		String value = this.options.get(key);
		if (value == null) {
			throw new ArgsParseException("Key " + key + " unknown.");
		} else {
			return Boolean.parseBoolean(value);
		}
		
	}

	public Hashtable<String, String> getOptions() {
		return options;
	}
	
}















