package com.ruke.vrjassc.vrjassc.compiler;

import com.ruke.vrjassc.vrjassc.util.TestHelper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InitializatorTest extends TestHelper {

	@Test
	public void autoDeleteInitTrig() {
		String code =
			"function InitTrig_bar takes nothing returns nothing\n"
				+ "call InitTrig_bar()\n"
			+ "endfunction\n"
			+ "function main takes nothing returns nothing\n"
				+ "call InitTrig_bar()\n"
				+ "call InitTrig_foo()\n"
			+ "endfunction";
		
		String expected =
			"globals\n"
			+ "endglobals\n"
			+ "function InitTrig_bar takes nothing returns nothing\n"
				+ "call InitTrig_bar()\n"
			+ "endfunction\n"
			+ "function main takes nothing returns nothing\n"
				+ "call InitTrig_bar()\n"
			+ "endfunction";
		
		assertEquals(expected, this.run(code));
	}
	
	@Test
	public void order() {
		String code =
			"library foo initializer b requires bar\n"
				+ "function b takes nothing returns nothing\n"
				+ "endfunction\n"
			+ "endlibrary\n"
			+ "library bar initializer a\n"
				+ "function a takes nothing returns nothing\n"
				+ "endfunction\n"
			+ "endlibrary\n"
			+ "function main takes nothing returns nothing\n"
			+ "endfunction";
			
		String expected =
			"globals\n"
			+ "endglobals\n"
			+ "function foo_b takes nothing returns nothing\n"
			+ "endfunction\n"
			+ "function bar_a takes nothing returns nothing\n"
			+ "endfunction\n"
			+ "function main takes nothing returns nothing\n"
				+ "call ExecuteFunc(\"bar_a\")\n"
				+ "call ExecuteFunc(\"foo_b\")\n"
			+ "endfunction";
			
		assertEquals(expected, this.run(code));
	}
	
	@Test
	public void test() {
		String code =
			"library foo initializer bar\n"
				+ "function bar takes nothing returns nothing\n"
				+ "endfunction\n"
			+ "endlibrary\n"
			+ "function main takes nothing returns nothing\n"
			+ "endfunction";
		
		String expected =
			"globals\n"
			+ "endglobals\n"
			+ "function foo_bar takes nothing returns nothing\n"
			+ "endfunction\n"
			+ "function main takes nothing returns nothing\n"
				+ "call ExecuteFunc(\"foo_bar\")\n"
			+ "endfunction";
		
		assertEquals(expected, this.run(code));
	}

}
