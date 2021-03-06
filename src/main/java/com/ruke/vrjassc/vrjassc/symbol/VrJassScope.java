package com.ruke.vrjassc.vrjassc.symbol;

public class VrJassScope extends ScopeSymbol {

	public VrJassScope() {
		super(null, null, null);
		this.defineNativeTypes();
	}
	
	@Override
	public Symbol define(Symbol symbol) {
		if (symbol instanceof VrJassScope) {
			for (Symbol s : ((VrJassScope) symbol).getChilds().values()) {
				this.define(s);
			}
			return symbol;
		}
		
		symbol.setModifier(Modifier.PUBLIC, true);
		return super.define(symbol);
	}
	
	private void defineNativeTypes() {
		this.define(new BuiltInTypeSymbol("integer", this, null));
		this.define(new BuiltInTypeSymbol("real", this, null));
		this.define(new BuiltInTypeSymbol("boolean", this, null));
		this.define(new BuiltInTypeSymbol("string", this, null));
		this.define(new BuiltInTypeSymbol("handle", this, null));
		this.define(new BuiltInTypeSymbol("code", this, null));
		
		// pseudo-types
		this.define(new BuiltInTypeSymbol("nothing", this, null));
		this.define(new BuiltInTypeSymbol("null", this, null));
	}

}
