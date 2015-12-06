package com.ruke.vrjassc.vrjassc.util;

import java.util.List;
import java.util.Stack;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import com.ruke.vrjassc.vrjassc.antlr4.vrjassParser;
import com.ruke.vrjassc.vrjassc.antlr4.vrjassParser.ElseIfStatementContext;
import com.ruke.vrjassc.vrjassc.antlr4.vrjassParser.StatementContext;
import com.ruke.vrjassc.vrjassc.exception.AlreadyDefinedException;
import com.ruke.vrjassc.vrjassc.exception.CompileException;
import com.ruke.vrjassc.vrjassc.exception.IncompatibleTypeException;
import com.ruke.vrjassc.vrjassc.exception.IncorrectArgumentCountException;
import com.ruke.vrjassc.vrjassc.exception.InvalidExtendTypeException;
import com.ruke.vrjassc.vrjassc.exception.InvalidImplementTypeException;
import com.ruke.vrjassc.vrjassc.exception.InvalidMathException;
import com.ruke.vrjassc.vrjassc.exception.InvalidStatementException;
import com.ruke.vrjassc.vrjassc.exception.InvalidStringConcatenationException;
import com.ruke.vrjassc.vrjassc.exception.InvalidTypeException;
import com.ruke.vrjassc.vrjassc.exception.MissReturnException;
import com.ruke.vrjassc.vrjassc.exception.NoAccessException;
import com.ruke.vrjassc.vrjassc.exception.StaticNonStaticTypeException;
import com.ruke.vrjassc.vrjassc.exception.UndefinedSymbolException;
import com.ruke.vrjassc.vrjassc.symbol.CastSymbol;
import com.ruke.vrjassc.vrjassc.symbol.ClassSymbol;
import com.ruke.vrjassc.vrjassc.symbol.FunctionSymbol;
import com.ruke.vrjassc.vrjassc.symbol.InterfaceSymbol;
import com.ruke.vrjassc.vrjassc.symbol.Modifier;
import com.ruke.vrjassc.vrjassc.symbol.Scope;
import com.ruke.vrjassc.vrjassc.symbol.Symbol;
import com.ruke.vrjassc.vrjassc.symbol.Type;

/**
 * Maintain all the validation (type compatible, amount of arguments
 * passed to a function, etc.) logic in one class
 * 
 * @author Ruke
 */
public class Validator {

	private CompileException exception;
	
	private Symbol validated;
	
	public Symbol getValidatedSymbol() {
		return this.validated;
	}
	
	public CompileException getException() {
		return this.exception;
	}
	
	/**
	 * Verifiy that the variable is declared before being used in token
	 * 
	 * @param variable
	 * @param token Where we are using the variable
	 * @return
	 */
	public boolean mustBeDeclaredBeforeUsed(Symbol variable, Token token) {
		this.validated = variable;
		
		if (variable.hasModifier(Modifier.LOCAL)) {
			if (variable.getToken().getLine() > token.getLine()) {
				this.exception = new InvalidStatementException(
					"Variables must be declared before use", token
				);
				
				return false;
			}
		}
		
		return true;
	}
	
	public boolean mustBeInsideOfLoop(ParserRuleContext ctx, Token token) {
		this.validated = null;
		
		ParserRuleContext parent = ctx.getParent();
		boolean insideOfLoop = false;
		
		// iterate until reach the function definition
		while (parent.getRuleIndex() != vrjassParser.RULE_functionDefinition) {
			if (parent.getRuleIndex() == vrjassParser.RULE_loopStatement) {
				insideOfLoop = true;
				break;
			}
			
			parent = parent.getParent();
		}
		
		if (!insideOfLoop) {
			this.exception = new InvalidStatementException("Can only be used inside of loops", token);
			return false;
		}
		
		return true;
	}
	
	public boolean mustBeValidStringConcatenation(Symbol a, Symbol b, Token token) {
		this.validated = b;
		
		if (!this.mustBeTypeCompatible(a, b, token)) {
			this.exception = new InvalidStringConcatenationException(token);
			return false;
		}
		
		return true;
	}
	
	public boolean mustBeValidMathOperation(Symbol a, Symbol b, Token token) {
		this.validated = b;
		
		if (!this.mustBeTypeCompatible(a, b, token)) {
			this.exception = new InvalidMathException(token);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Validates that a symbol must not be defined
	 * @param scope
	 * @param name
	 * @param token
	 * @return
	 */
	public boolean mustNotBeDefined(Scope scope, String name, Token token) {
		this.validated = scope.resolve(scope, name);
		
		if (this.validated != null) {
			this.exception = new AlreadyDefinedException(token, this.validated);
			return false;
		}
		
		return true;
	}

	/**
	 * 
	 * @param scope
	 * @param name
	 * @param token
	 * @return
	 */
	public boolean mustBeDefined(Scope scope, String name, Token token) {
		this.validated = scope.resolve(scope, name);
		
		if (this.validated == null) {
			this.exception = new UndefinedSymbolException(token, name);
			return false;
		}
		
		return true;
	}

	/**
	 * Symbol's type b must be compatible with symbol's a
	 * It will also verify that both types are defined
	 * @param a
	 * @param b
	 * @param token
	 * @return
	 */
	public boolean mustBeTypeCompatible(Symbol a, Symbol b, Token token) {
		this.validated = b;
		
		if (a.getType() == null) {
			this.exception = new InvalidTypeException(token, a);
			return false;
		}
		
		if (b.getType() == null) {
			this.exception = new InvalidTypeException(token, b);
			return false;
		}
		
		if (!a.isTypeCompatible(b)) {
			this.exception = new IncompatibleTypeException(token, a, b.getType());
			return false;
		}
		
		return true;
	}

	public boolean mustMatchArguments(FunctionSymbol function, Stack<Symbol> arguments, Token token) {
		this.validated = function;

		if (function.getParams().size() == arguments.size()) {
			int i = 0;
			
			for (Symbol argument : arguments) {
				if (!this.mustBeTypeCompatible(function.getParams().get(i), argument, token)) {
					return false;
				}
				
				i++;
			}
		} else {
			this.exception = new IncorrectArgumentCountException(token, function);
			return false;
		}
		
		return true;
	}

	public boolean mustHaveAccess(Scope scope, Symbol symbol, Token token) {
		this.validated = symbol;
		
		if (symbol instanceof CastSymbol == false && !scope.hasAccess(symbol)) {
			if (!symbol.getName().equals("allocate")) {
				this.exception = new NoAccessException(token, scope, symbol);
				return false;
			}
		}
		
		return true;
	}

	public boolean mustBeValidType(Symbol type, Token token) {
		this.validated = type;
		
		if (type instanceof Type == false) {
			this.exception = new InvalidTypeException(token, type);
			return false;
		}
		
		return true;
	}

	public boolean mustBeExtendableValid(Symbol symbol, Token token) {
		this.validated = symbol;
		
		if (symbol instanceof ClassSymbol == false) {
			this.exception = new InvalidExtendTypeException(token, symbol);
			return false;
		}
		
		return true;
	}

	public boolean mustBeImplementableTypeValid(Symbol symbol, Token token) {
		this.validated = symbol;
		
		if (symbol instanceof InterfaceSymbol == false) {
			this.exception = new InvalidImplementTypeException(token, symbol);
			return false;
		}
		
		return true;
	}

	public boolean mustBeValidMember(Symbol prevSymbol, Symbol member, Token token) {
		this.validated = member;
		
		if (member.hasModifier(Modifier.STATIC)) {
			if (prevSymbol instanceof ClassSymbol == false) {
				this.exception = new StaticNonStaticTypeException(token, member);
				return false;
			}
		} else {
			if (prevSymbol instanceof ClassSymbol) {
				this.exception = new StaticNonStaticTypeException(token, member);
				return false;
			}
		}
		
		return true;
	}
	
	public boolean mustReturn(Symbol function, List<StatementContext> statements) {
		this.validated = function;
		
		for (StatementContext stat : statements) {
			if (stat.returnStatement() != null) {
				return true;
			} else if (stat.ifStatement() != null) {
				if (stat.ifStatement().elseStatement() != null) {
					boolean ifReturns = this.mustReturn(function, stat.ifStatement().statement());
					boolean elseIfReturns = true;
					boolean elseReturns = this.mustReturn(function, stat.ifStatement().elseStatement().statement());
					
					for (ElseIfStatementContext elseif : stat.ifStatement().elseIfStatement()) {
						if (!this.mustReturn(function, elseif.statement())) {
							elseIfReturns = false;
							break;
						}
					}
					
					if (ifReturns && elseIfReturns && elseReturns) {
						return true;
					}
				}
			}
		}
		
		this.exception = new MissReturnException(function.getToken(), function);
		return false;
	}

	public boolean mustBeValidCode(Symbol func, Token token) {
		this.validated = func;
		
		if (!func.hasModifier(Modifier.STATIC)) {
			this.exception = new InvalidStatementException("Functions/methods must be static to be used as code", token);			
			return false;
		}
		
		return true;
	}

}
