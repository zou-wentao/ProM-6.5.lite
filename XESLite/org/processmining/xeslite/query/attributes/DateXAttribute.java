package org.processmining.xeslite.query.attributes;

import java.util.Date;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeTimestamp;

abstract public class DateXAttribute<T> extends AbstractXAttribute<T, Date> {

	public DateXAttribute(Class<T> objectType, String attributeName) {
		super(objectType, Date.class, attributeName);
	}

	protected Date getValue(XAttribute attr) {
		return ((XAttributeTimestamp) attr).getValue();
	}

	protected boolean correctType(XAttribute attr) {
		return attr instanceof XAttributeTimestamp;
	}

}