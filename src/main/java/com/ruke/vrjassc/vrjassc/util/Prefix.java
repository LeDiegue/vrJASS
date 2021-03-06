package com.ruke.vrjassc.vrjassc.util;

import com.ruke.vrjassc.vrjassc.symbol.ClassSymbol;
import com.ruke.vrjassc.vrjassc.symbol.Modifier;
import com.ruke.vrjassc.vrjassc.symbol.Symbol;

import java.util.LinkedList;

public class Prefix {

	public static String build(Symbol symbol) {
		if (!symbol.hasModifier(Modifier.LOCAL)) {
			LinkedList<String> e = new LinkedList<String>();
			Symbol parent = symbol;
			
			while (parent.getParentScope() != null) {
				e.addFirst(parent.getName());
				parent = (Symbol) parent.getParentScope();
			}

			if (symbol.getParentScope() instanceof ClassSymbol) {
				e.addFirst("struct");
			}
			
			if (!e.isEmpty()) {
				return String.join("_", e).replace("[]=", "bracket_set_op").replace("[]", "bracket_op");
			}
		}
		
		return symbol.getName();
	}

}
