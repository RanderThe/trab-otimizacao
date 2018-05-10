package net.pucminas.otimizacao;

import java.util.LinkedHashMap;
import java.util.Map;

public class Constraint {
	
	private double bound;
	public Map<String,Double> items = new LinkedHashMap<String,Double>();
	
	public Constraint(Map<String,Double> items, double bound) {
		this.items = items;
		this.bound = bound;
	}	

	public Map<String, Double> getItems() {
		return items;
	}

	public void setItems(Map<String, Double> items) {
		this.items = items;
	}

	public double getBound() {
		return bound;
	}
	public void setBound(double bound) {
		this.bound = bound;
	}
}
