package org.processmining.xeslite.query.attributes;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeLiteral;

abstract public class LiteralXAttribute<T> extends AbstractXAttribute<T, String> {

	public LiteralXAttribute(Class<T> objectType, String attributeKey) {
		super(objectType, String.class, attributeKey);
	}

	protected String getValue(XAttribute attr) {
		return attr.toString();
	}

	protected boolean correctType(XAttribute attr) {
		return attr instanceof XAttributeLiteral || attr instanceof XAttributeBoolean;
	}
	
}