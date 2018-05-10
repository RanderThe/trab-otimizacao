package net.pucminas.otimizacao;

import java.util.ArrayList;

public class Problem  {
	
	private ArrayList<Constraint> constraints;
	private ArrayList<Column> columns;	
	private double z;

	public ArrayList<Constraint> getConstraints() {
		return constraints;
	}

	public void setConstraints(ArrayList<Constraint> constraints) {
		this.constraints = constraints;
	}

	public ArrayList<Column> getColumns() {
		return columns;
	}

	public void setColumns(ArrayList<Column> columns) {
		this.columns = columns;
	}
	
	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}
}