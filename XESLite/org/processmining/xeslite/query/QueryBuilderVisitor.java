package org.processmining.xeslite.query;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.processmining.xeslite.query.extension.NotEqual;
import org.processmining.xeslite.query.syntax.Node;
import org.processmining.xeslite.query.syntax.QueryAtom;
import org.processmining.xeslite.query.syntax.QueryBinaryLogicalExpression;
import org.processmining.xeslite.query.syntax.QueryParserVisitor;
import org.processmining.xeslite.query.syntax.QueryRoot;
import org.processmining.xeslite.query.syntax.QuerySimpleExpression;
import org.processmining.xeslite.query.syntax.SimpleNode;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.QueryFactory;

final class QueryBuilderVisitor<A> implements QueryParserVisitor {

	private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	private final AttributeResolver<A> attributeResolver;

	public QueryBuilderVisitor(AttributeResolver<A> attributeResolver) {
		this.attributeResolver = attributeResolver;
	}

	public Object visit(SimpleNode node, Object data) {
		throw new UnsupportedOperationException("There should be no unamed node");
	}

	public Object visit(QueryRoot node, Object data) {
		assert node.jjtGetNumChildren() == 1 : "Invalid root!";
		return node.jjtGetChild(0).jjtAccept(this, data);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object visit(QueryBinaryLogicalExpression node, Object data) {
		if (node.jjtGetNumChildren() == 2) {
			Node left = node.jjtGetChild(0);
			Node right = node.jjtGetChild(1);
			switch (node.op.toLowerCase()) {
				case "and" :
					return QueryFactory.and((Query) left.jjtAccept(this, data), (Query) right.jjtAccept(this, data));

				case "or" :
					return QueryFactory.or((Query) left.jjtAccept(this, data), (Query) right.jjtAccept(this, data));
			}
		}
		return node.jjtGetChild(0).jjtAccept(this, data);
	}

	public Object visit(QueryAtom node, Object data) {
		return node.val;
	}
	
	private String getValue(QuerySimpleExpression node, Object data) {
		if (node.jjtGetNumChildren() == 2) {
			return ((String) node.jjtGetChild(1).jjtAccept(this, data)).trim();
		} else {			
			return ((String) node.jjtGetChild(2).jjtAccept(this, data)).trim();
		}
	}

	private String getAttributeName(QuerySimpleExpression node, Object data) {
		if (node.jjtGetNumChildren() == 2) {
			return ((String) node.jjtGetChild(0).jjtAccept(this, data)).trim();
		} else {			
			return ((String) node.jjtGetChild(1).jjtAccept(this, data)).trim();
		}
	}
	
	private String getEventName(QuerySimpleExpression node, Object data) {
		if (node.jjtGetNumChildren() == 2) {
			return null;
		} else {			
			return ((String) node.jjtGetChild(0).jjtAccept(this, data)).trim();
		}
	}

	public Object visit(QuerySimpleExpression node, Object data) {
		final String attributeName = getAttributeName(node, data);
		final String value = getValue(node, data);
		String eventName = getEventName(node, data);

		switch (node.op.toLowerCase()) {
			case "=" :
				return createQuery(eventName, attributeName, value, new QueryBuilder<A>() {

					public <T extends Comparable<T>> Query<A> build(Attribute<A, T> attr, T value) {
						return QueryFactory.equal(attr, value);
					}

				});

			case "!=" :
				return createQuery(eventName, attributeName, value, new QueryBuilder<A>() {

					public <T extends Comparable<T>> Query<A> build(Attribute<A, T> attr, T value) {
						return new NotEqual<>(attr, value);
					}

				});

			case ">" :
				return createQuery(eventName, attributeName, value, new QueryBuilder<A>() {

					public <T extends Comparable<T>> Query<A> build(Attribute<A, T> attr, T value) {
						return QueryFactory.greaterThan(attr, value);
					}

				});

			case "<" :
				return createQuery(eventName, attributeName, value, new QueryBuilder<A>() {

					public <T extends Comparable<T>> Query<A> build(Attribute<A, T> attr, T value) {
						return QueryFactory.lessThan(attr, value);
					}

				});

			case "<=" :
				return createQuery(eventName, attributeName, value, new QueryBuilder<A>() {

					public <T extends Comparable<T>> Query<A> build(Attribute<A, T> attr, T value) {
						return QueryFactory.lessThanOrEqualTo(attr, value);
					}

				});

			case ">=" :
				return createQuery(eventName, attributeName, value, new QueryBuilder<A>() {

					public <T extends Comparable<T>> Query<A> build(Attribute<A, T> attr, T value) {
						return QueryFactory.greaterThanOrEqualTo(attr, value);
					}

				});

			case "%" :
				// Only for literals
				return QueryFactory.contains(attributeResolver.stringAttribute(attributeName), value);

			case "~" :
				// Only for literals
				return QueryFactory.matchesRegex(attributeResolver.stringAttribute(attributeName), value);

			default :
				throw new UnsupportedOperationException("Unsupported operator " + node.op);
		}
	}

	private final Query<A> createQuery(String eventName, String attributeName, String value,
			QueryBuilder<A> queryBuilder) {
		Class<?> type = attributeResolver.getAttributeType(attributeName);
		try {
			if ((type == XAttributeContinuous.class || type == XAttributeDiscrete.class || type == Double.class || type == Float.class)) {
				Double doubleValue = Double.parseDouble(value);
				return queryBuilder.build(attributeResolver.doubleAttribute(eventName, attributeName), doubleValue);
			} else if (type == XAttributeTimestamp.class || type == Date.class) {
				Date dateValue = DATE_FORMATTER.parse(value);
				return queryBuilder.build(attributeResolver.dateAttribute(eventName, attributeName), dateValue);
			}
		} catch (NumberFormatException | java.text.ParseException e) {
			// fall through with string
		}
		return queryBuilder.build(attributeResolver.stringAttribute(eventName, attributeName), value);
	}

}