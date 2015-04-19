package visitor;

import java.util.HashMap;

import exception.AlreadyDefinedVariableException;
import symbol.FunctionSymbol;
import symbol.VariableSymbol;
import antlr4.vrjassBaseVisitor;
import antlr4.vrjassParser.FunctionDefinitionContext;
import antlr4.vrjassParser.LocalVariableStatementContext;
import antlr4.vrjassParser.ParameterContext;
import antlr4.vrjassParser.ParametersContext;

public class VariableFinder extends vrjassBaseVisitor<Void> {

	protected MainVisitor main;
	
	protected HashMap<String, VariableSymbol> globalVariables;
	protected HashMap<String, HashMap<String, VariableSymbol>> localVariables;
	
	protected String funcName;
	
	public VariableFinder(MainVisitor main) {
		this.main = main;
		this.globalVariables = new HashMap<String, VariableSymbol>();
		this.localVariables = new HashMap<String, HashMap<String, VariableSymbol>>();
	}
	
	public VariableSymbol get(String funcName, String variableName) {
		VariableSymbol variable = null;
		
		if (funcName == null) {
			variable = this.globalVariables.get(variableName);
		} else {
			if (this.localVariables.containsKey(funcName)) {
				variable = this.localVariables.get(funcName).get(variableName);
			} else {
				variable = this.globalVariables.get(variableName);
			}
		}
		
		return variable;
	}
	
	public VariableSymbol get(FunctionSymbol function, String variableName) {
		String funcName = null;
		
		if (function != null) {
			funcName = function.getName();
		}
		
		return this.get(funcName, variableName);
	}
	
	protected VariableSymbol put(String funcName, VariableSymbol variable) {
		VariableSymbol alreadyDefined = null;
		
		if (funcName == null) {
			alreadyDefined = this.globalVariables.get(variable.getName());
		} else {
			alreadyDefined = this.localVariables.get(funcName).get(variable.getName());
		}
		
		if (alreadyDefined != null) {
			throw new AlreadyDefinedVariableException(variable.getToken(), alreadyDefined);
		}
		
		if (funcName == null) {
			this.globalVariables.put(variable.getName(), variable);
		} else {
			this.localVariables.get(funcName).put(variable.getName(), variable);
		}
		
		return variable;
	}
	
	@Override
	public Void visitLocalVariableStatement(LocalVariableStatementContext ctx) {
		String variableName = ctx.varName.getText();
		String variableType = ctx.variableType().getText();
		boolean isArray = ctx.array != null;
		
		VariableSymbol variable = new VariableSymbol(
			variableName,
			variableType,
			isArray,
			null,
			ctx.varName
		);
		
		this.put(this.funcName, variable);
		
		return null;
	}
	
	@Override
	public Void visitParameters(ParametersContext ctx) {
		String variableName;
		String variableType;
		VariableSymbol variable;
		
		for (ParameterContext param : ctx.parameter()) {
			variableName = param.ID().getText();
			variableType = param.variableType().getText();
			variable = new VariableSymbol(
				variableName,
				variableType,
				false,
				null,
				param.ID().getSymbol()
			);
			
			this.put(this.funcName, variable);
		}
		
		return null;
	}
	
	@Override
	public Void visitFunctionDefinition(FunctionDefinitionContext ctx) {
		String prevFuncName = this.funcName;
		
		this.funcName = ctx.functionName.getText();
		
		if (!this.localVariables.containsKey(this.funcName)) {
			this.localVariables.put(
				this.funcName,
				new HashMap<String, VariableSymbol>()
			);
		}
		
		this.visit(ctx.parameters());
		this.visit(ctx.statements());
		
		this.funcName = prevFuncName;
		
		return null;
	}
	
}
