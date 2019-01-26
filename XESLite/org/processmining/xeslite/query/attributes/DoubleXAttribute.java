package org.processmining.xeslite.query.attributes;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;

abstract public class DoubleXAttribute<T> extends AbstractXAttribute<T, Double> {

	public DoubleXAttribute(Class<T> objectType, String attributeKey) {
		super(objectType, Double.class, attributeKey);
	}

	protected Double getValue(XAttribute attr) {
		if (attr instanceof XAttributeContinuous) {
			return ((XAttributeContinuous) attr).getValue();
		} else {
			return Long.valueOf(((XAttributeDiscrete) attr).getValue()).doubleValue();
		}
	}

	protected boolean correctType(XAttribute attr) {
		return attr instanceof XAttributeContinuous || attr instanceof XAttributeDiscrete;
	}

}