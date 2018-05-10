package net.pucminas.otimizacao;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_prob;
import org.gnu.glpk.glp_smcp;

@ManagedBean(name = "problemBO")
@SessionScoped
public class ProblemBO implements Serializable { 

	private static final long serialVersionUID = 1L;
	Problem problem;
    int max =  2; //GLPKConstants.GLP_MAX;
    int min = 1; //GLPKConstants.GLP_MIN;
    int typeProblem;
	
	@PostConstruct
    public void init() {
		problem = new Problem();
    }

	public Problem getProblem() {
		return problem;
	}

	public void setProblem(Problem problem) {
		this.problem = problem;
	}	

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}
	
	public int getTypeProblem() {
		return typeProblem;
	}

	public void setTypeProblem(int typeProblem) {
		this.typeProblem = typeProblem;
	}

	public ProblemBO() {
		// Carrega as dll's do GLPK
		loadDLL("glpk_4_63.dll");
		loadDLL("glpk_4_63_java.dll");
	}

	private static void loadDLL(String location) {
		try {
			File dll = new File(location);
			System.load(dll.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String start(int columns, int constraints) {		
		startColumns(columns);
		startConstraints(columns, constraints);
		return "index2";
	}
	
	public String calculate() {		
		
		solveProblem(problem.getConstraints(), problem.getColumns());		
		return "solution";
	}

	private void startColumns(int numberOfColumns) {
		ArrayList<Column> columns = new ArrayList<Column>();

		for(int i=1; i <= numberOfColumns; i++) {
			columns.add(new Column("x" + i, 0));	
		}
		
		problem.setColumns(columns);
	}

	private void startConstraints(int numberOfColumns, int numberOfConstraints) {
		ArrayList<Constraint> constraints = new ArrayList<Constraint>();	
		
		for(int i=1; i <=numberOfConstraints; i++) {			
			Map<String,Double> items = new LinkedHashMap<String,Double>();			
			for(int j=1; j<= numberOfColumns; j++) {
				items.put("x"+j, 0.0);
			}				
			constraints.add(new Constraint(items, 0));	
		}		
		
		problem.setConstraints(constraints);
	}
	
	public String solveProblem(ArrayList<Constraint> constraints, ArrayList<Column> columns) {
		glp_prob mip;
		glp_smcp parm;
		SWIGTYPE_p_int indexes;
		SWIGTYPE_p_double values;
		int ret;

		// Criação do problema
		mip = GLPK.glp_create_prob();
		GLPK.glp_set_prob_name(mip, "Production Mix Problem");
		
		
		// Definição de variáveis
		GLPK.glp_add_cols(mip, columns.size());
		for(int i=0; i < columns.size(); i++) {
			GLPK.glp_set_col_name(mip, i+1, columns.get(i).getLabel());
			GLPK.glp_set_col_kind(mip, i+1, GLPKConstants.GLP_IV);
			GLPK.glp_set_col_bnds(mip, i+1, GLPKConstants.GLP_LO, 0, 0);
		}
		
		// Definição de restrições
		GLPK.glp_add_rows(mip, constraints.size()); 
		int numberOfConstraints;
		
		for(int i=0; i< constraints.size(); i++) {
			GLPK.glp_set_row_name(mip, i+1, "c" + (i+1));
			
			if(typeProblem == max)
				GLPK.glp_set_row_bnds(mip, i+1, GLPKConstants.GLP_UP, 0.0, constraints.get(i).getBound());
			else if(typeProblem == min)
				GLPK.glp_set_row_bnds(mip, i+1, GLPKConstants.GLP_LO, constraints.get(i).getBound(), 0.0);	
			
			numberOfConstraints = constraints.get(i).getItems().size();
			
			indexes = GLPK.new_intArray(numberOfConstraints);			
			for(int j=1; j <= constraints.get(i).getItems().size(); j++)
				GLPK.intArray_setitem(indexes, j, j);			
			
			values = GLPK.new_doubleArray(numberOfConstraints);
			List<Double> valueList = new ArrayList<Double>(constraints.get(i).getItems().values());
			
			for(int j=0; j < valueList.size(); j++)
				GLPK.doubleArray_setitem(values, j +1, Double.parseDouble(String.valueOf(valueList.get(j))));		
			
			GLPK.glp_set_mat_row(mip, i + 1, columns.size(), indexes, values);
		}		
		
		// Definição de função objetiva
		GLPK.glp_set_obj_name(mip, "z");
		GLPK.glp_set_obj_dir(mip, typeProblem);
		
		for(int i=0; i < problem.getColumns().size(); i++) {
			GLPK.glp_set_obj_coef(mip, i + 1, problem.getColumns().get(i).getValue());
		}
		
		parm = new glp_smcp();
		GLPK.glp_init_smcp(parm);
		ret = GLPK.glp_simplex(mip, parm);
		// Recupera solução
		if (ret == 0) {
			writeSolution(mip);
		} else {
			System.out.println("O problema não pôde ser resolvido");
		}
		// Deleta o problema para liberar memória
		GLPK.glp_delete_prob(mip);

		return "solution";
	}

	public void writeSolution(glp_prob mip) {
		double value;
		value = GLPK.glp_get_obj_val(mip);
		problem.setZ(value);
	}


}
