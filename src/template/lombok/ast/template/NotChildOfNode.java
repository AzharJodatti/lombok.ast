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
package lombok.ast.template;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a certain field in a {@code lombok.ast.Node} template, as generated by {@link GenerateAstNode} is not itself a child of {@code lombok.ast.Node},
 * but something else, such as a raw string or an enum. This is generally used for the most specific nodes, such as {@link lombok.ast.Identifier} and
 * {@link lombok.at.IntegralLiteral}.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface NotChildOfNode {
	/**
	 * If you'd like to allow a 'raw form' concept, set both {@code rawFormParser} and {@code rawFormGenerator} to a method name that takes care of parsing/generating
	 * the raw form to/from its value form. For example, for character literals, The actual string {@code '\n'}, including a literal backslash and quote symbols,
	 * is the raw form, but the single character 'newline' is the value form. To signal that a raw form is malformed, throw a {@code IllegalArgumentException}
	 * with the reason as the message.
	 *
	 * Either leave both {@code rawFormParser} and {@code rawFormGenerator} blank as they are by default, or set them both.
	 * 
	 * @see #rawFormGenerator()
	 */
	String rawFormParser() default "";
	
	/**
	 * @see #rawFormParser()
	 */
	String rawFormGenerator() default "";
	
	/**
	 * Tells the template generator to generate only a getter for this field. Intended primarily fields of mutable types.
	 */
	boolean suppressSetter() default false;
	
	/**
	 * Supply some raw java code here that copies the field when the entire node is {@link lombok.ast.Node#copy()}ied.
	 * By default this is just the {@code this.fieldName} (resulting in {@code copy.fieldName = this.fieldName}).
	 * <p>
	 * Example for a list: {@code new java.util.ArrayList<Type>(this.fieldName)}
	 */
	String codeToCopy() default "";
}
