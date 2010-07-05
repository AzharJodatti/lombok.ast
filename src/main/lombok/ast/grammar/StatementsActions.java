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

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import lombok.ast.AlternateConstructorInvocation;
import lombok.ast.Assert;
import lombok.ast.Block;
import lombok.ast.Break;
import lombok.ast.Case;
import lombok.ast.Catch;
import lombok.ast.Continue;
import lombok.ast.Default;
import lombok.ast.DoWhile;
import lombok.ast.EmptyStatement;
import lombok.ast.ExpressionStatement;
import lombok.ast.For;
import lombok.ast.ForEach;
import lombok.ast.If;
import lombok.ast.LabelledStatement;
import lombok.ast.Node;
import lombok.ast.Position;
import lombok.ast.Return;
import lombok.ast.SuperConstructorInvocation;
import lombok.ast.Switch;
import lombok.ast.Synchronized;
import lombok.ast.Throw;
import lombok.ast.Try;
import lombok.ast.VariableDeclaration;
import lombok.ast.VariableDefinition;
import lombok.ast.VariableDefinitionEntry;
import lombok.ast.While;

public class StatementsActions extends SourceActions {
	public StatementsActions(Source source) {
		super(source);
	}
	
	public Node createBlock(List<Node> statements) {
		Block block = new Block();
		if (statements != null) for (Node s : statements) {
			if (s != null) block.rawContents().addToEnd(s);
		}
		
		return posify(block);
	}
	
	public Node createEmptyStatement() {
		return posify(new EmptyStatement());
	}
	
	public Node createLabelledStatement(List<Node> labelNames, Node statement) {
		Node current = statement;
		if (labelNames != null) {
			labelNames = Lists.newArrayList(labelNames);
			Collections.reverse(labelNames);
			for (Node n : labelNames) {
				if (n != null) {
					Position pos = current == null ? null : new Position(n.getPosition().getStart(), current.getPosition().getEnd());
					current = new LabelledStatement().astLabel(createIdentifierIfNeeded(n, currentPos())).rawStatement(current);
					current.setPosition(pos);
				}
			}
		}
		return current;
	}
	
	public Node createIfStatement(Node condition, Node statement, Node elseStatement) {
		return posify(new If().rawCondition(condition).rawStatement(statement).rawElseStatement(elseStatement));
	}
	
	public Node createAssertStatement(Node assertion, Node message) {
		return posify(new Assert().rawAssertion(assertion).rawMessage(message));
	}
	
	public Node createSwitchStatement(Node condition, Node body) {
		return posify(new Switch().rawCondition(condition).rawBody(body));
	}
	
	public Node createCaseStatement(Node condition) {
		return posify(new Case().rawCondition(condition));
	}
	
	public Node createDefaultStatement(org.parboiled.Node<Node> defaultStatement) {
		Default node = new Default();
		source.registerStructure(node, defaultStatement);
		return posify(node);
	}
	
	public Node createWhileStatement(Node condition, Node statement) {
		return posify(new While().rawCondition(condition).rawStatement(statement));
	}
	
	public Node createDoStatement(Node condition, Node statement) {
		return posify(new DoWhile().rawCondition(condition).rawStatement(statement));
	}
	
	public Node createStatementExpressionList(Node head, List<Node> tail) {
		TemporaryNode.StatementExpressionList result = new TemporaryNode.StatementExpressionList();
		if (head != null) result.expressions.add(head);
		if (tail != null) for (Node n : tail) if (n != null) result.expressions.add(n);
		return posify(result);
	}
	
	public Node createBasicFor(Node init, Node condition, Node update, Node statement) {
		For result = new For().rawCondition(condition).rawStatement(statement);
		List<Node> updates;
		
		if (update instanceof TemporaryNode.StatementExpressionList) {
			updates = ((TemporaryNode.StatementExpressionList)update).expressions;
		} else {
			updates = Collections.singletonList(update);
		}
		
		if (init instanceof TemporaryNode.StatementExpressionList) {
			for (Node n : ((TemporaryNode.StatementExpressionList)init).expressions) result.rawExpressionInits().addToEnd(n);
		} else {
			result.rawVariableDeclaration(init);
		}
		
		for (Node n : updates) if (n != null) result.rawUpdates().addToEnd(n);
		return posify(result);
	}
	
	public Node createEnhancedFor(
			org.parboiled.Node<Node> modifiers, Node type,
			org.parboiled.Node<Node> varDefEntry, Node iterable, Node statement) {
		
		VariableDefinition decl = new VariableDefinition().rawTypeReference(type).rawVariables()
				.addToEnd(varDefEntry.getValue());
		positionSpan(decl, modifiers, varDefEntry);
		decl.astModifiers(createModifiersIfNeeded(modifiers.getValue(), decl.getPosition().getStart()));
		return posify(new ForEach().rawVariable(decl).rawIterable(iterable).rawStatement(statement));
	}
	
	public Node createBreak(Node label) {
		Break b = new Break();
		if (label != null) b.astLabel(createIdentifierIfNeeded(label, currentPos()));
		return posify(b);
	}
	
	public Node createContinue(Node label) {
		Continue c = new Continue();
		if (label != null) c.astLabel(createIdentifierIfNeeded(label, currentPos()));
		return posify(c);
	}
	
	public Node createReturn(Node value) {
		return posify(new Return().rawValue(value));
	}
	
	public Node createThrow(Node throwable) {
		return posify(new Throw().rawThrowable(throwable));
	}
	
	public Node createSynchronizedStatement(Node lock, Node body) {
		return posify(new Synchronized().rawLock(lock).rawBody(body));
	}
	
	public Node createCatch(Node modifiers, Node type, Node varName, Node body) {
		VariableDefinitionEntry varNameEntry = new VariableDefinitionEntry().astName(createIdentifierIfNeeded(varName, currentPos()));
		if (varName != null) varNameEntry.setPosition(varName.getPosition());
		VariableDefinition decl = new VariableDefinition().rawTypeReference(type).rawVariables().addToEnd(
				varNameEntry);
		if (type != null && varName != null) decl.setPosition(new Position(type.getPosition().getStart(), varName.getPosition().getEnd()));
		if (modifiers != null) decl.astModifiers(createModifiersIfNeeded(modifiers, currentPos()));
		return posify(new Catch().rawExceptionDeclaration(decl).rawBody(body));
	}
	
	public Node createTryStatement(Node body, List<Node> catches, Node finallyBody) {
		Try result = new Try().rawBody(body).rawFinally(finallyBody);
		if (catches != null) for (Node c : catches) if (c != null) result.rawCatches().addToEnd(c);
		return posify(result);
	}
	
	public Node addLocalVariableModifiers(Node variableDefinition, Node modifiers) {
		if (modifiers != null && variableDefinition instanceof VariableDefinition) {
			((VariableDefinition)variableDefinition).astModifiers(createModifiersIfNeeded(modifiers, currentPos()));
		}
		
		return posify(variableDefinition);
	}
	
	public Node createAlternateConstructorInvocation(Node typeArguments, Node arguments) {
		AlternateConstructorInvocation result = new AlternateConstructorInvocation();
		
		if (typeArguments instanceof TemporaryNode.TypeArguments) {
			for (Node arg : ((TemporaryNode.TypeArguments)typeArguments).arguments) {
				result.rawConstructorTypeArguments().addToEnd(arg);
			}
		}
		
		if (arguments instanceof TemporaryNode.MethodArguments) {
			for (Node arg : ((TemporaryNode.MethodArguments)arguments).arguments) {
				result.rawArguments().addToEnd(arg);
			}
		}
		return posify(result);
	}
	
	public Node createSuperConstructorInvocation(org.parboiled.Node<Node> dot, Node qualifier, Node typeArguments, Node arguments) {
		SuperConstructorInvocation result = new SuperConstructorInvocation().rawQualifier(qualifier);
		
		if (typeArguments instanceof TemporaryNode.TypeArguments) {
			for (Node arg : ((TemporaryNode.TypeArguments)typeArguments).arguments) {
				result.rawConstructorTypeArguments().addToEnd(arg);
			}
		}
		
		if (arguments instanceof TemporaryNode.MethodArguments) {
			for (Node arg : ((TemporaryNode.MethodArguments)arguments).arguments) {
				result.rawArguments().addToEnd(arg);
			}
		}
		if (dot != null) source.registerStructure(result, dot);
		return posify(result);
	}
	
	public Node createExpressionStatement(Node expression) {
		return posify(new ExpressionStatement().rawExpression(expression));
	}
	
	public Node createVariableDeclaration(Node definition) {
		return posify(new VariableDeclaration().rawDefinition(definition));
	}
}
