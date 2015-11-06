package com.ruke.vrjassc.translator.expression;

import com.ruke.vrjassc.vrjassc.util.VariableTypeDetector;

public class BooleanExpression extends Expression {

	public static enum Operator {
		EQUAL_EQUAL		{ public String toString() { return "=="; } },
		NOT_EQUAL		{ public String toString() { return "!="; } },
		GREATER			{ public String toString() { return ">"; } },
		GREATER_EQUAL	{ public String toString() { return ">="; } },
		LESS			{ public String toString() { return "<"; } },
		LESS_EQUAL		{ public String toString() { return "<="; } },
	}
	
	protected Expression a;
	protected Operator operator;
	protected Expression b;
	
	public BooleanExpression(Expression a, Operator operator, Expression b) {
		this.a = a;
		this.operator = operator;
		this.b = b;
	}
	
	public BooleanExpression(Expression a) {
		this.a = a;
	}

	@Override
	public String translate() {
		if (this.b == null) {
			if (this.a.translate().equals("true") || this.a.translate().equals("false")) {
				return this.a.translate();
			}
			
			String type = this.a.getSymbol().getType().getName();
			
			if (VariableTypeDetector.isHandle(type)) {
				return this.a.translate() + "!=null";
			} else if (type.equals("string")) {
				return "StringLength(" + this.a.translate() + ")!=0";
			} else if (type.equals("integer")) {
				return this.a.translate() + "!=0";
			}
		}
		
		return this.a.translate() + this.operator + this.b.translate();
	}

}