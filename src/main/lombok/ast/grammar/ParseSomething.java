/*
 * Copyright © 2010 Reinier Zwitserloot and Roel Spilker.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lombok.ast.grammar;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import lombok.ast.Node;

import org.parboiled.common.Function;
import org.parboiled.common.StringUtils;
import org.parboiled.support.ParseTreeUtils;
import org.parboiled.support.ParsingResult;
import org.parboiled.trees.Printability;

public class ParseSomething {
	public static void main(String[] args) throws IOException {
		String src = null;
		if (args.length > 0) {
			src = readFile(args);
		}
		
		ParserGroup group = new ParserGroup();
		if (src == null) {
			Scanner s = new Scanner(System.in);
			System.out.println("Write something to parse, then hit enter to print the node graph of what the parser made of it.");
			System.out.println("All expressions supported except parens, field selects, and method calls. Also supported:\n" +
					"'(any legal type).class' (but not in expressions, by itself).\n");
			while (true) {
				String line = s.nextLine();
				if (line.isEmpty()) return;
				parse(group.java, line);
			}
		} else {
			parse(group.java, src);
		}
	}
	
	private static void parse(JavaParser parser, String input) {
		long now = System.nanoTime();
		ParsingResult<Node> result = parser.parse(parser.testRules(), input + "\n");
		long taken = System.nanoTime() - now;
		System.out.println(ParseTreeUtils.printNodeTree(result, new Function<org.parboiled.Node<Node>, Printability>() {
			@Override public Printability apply(org.parboiled.Node<Node> from) {
				return from.getValue() != null ? Printability.PrintAndDescend : Printability.Descend;
			}
		}));
		if (result.hasErrors()) {
			System.out.println(StringUtils.join(result.parseErrors, "---\n"));
		}
		
		long msec = taken / 1000000;
		long nano = taken % 1000000;
		
		System.out.printf("Time taken: %d msec(+ %d nanos)\n", msec, nano);
	}
	
	private static String readFile(String[] args) throws FileNotFoundException, IOException {
		FileInputStream fis = new FileInputStream(args[0]);
		ByteArrayOutputStream fos = new ByteArrayOutputStream();
		byte[] b = new byte[65536];
		while (true) {
			int r = fis.read(b);
			if (r == -1) break;
			fos.write(b, 0, r);
		}
		fos.write('\n');
		return new String(fos.toByteArray(), "UTF-8");
	}
}
