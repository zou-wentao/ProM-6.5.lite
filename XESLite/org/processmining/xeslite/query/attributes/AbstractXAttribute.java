package org.processmining.xeslite.query.attributes;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;

import com.googlecode.cqengine.attribute.MultiValueAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;

public abstract class AbstractXAttribute<E, T> extends MultiValueAttribute<E, T> {

	private final String attributeKey;

	protected AbstractXAttribute(Class<E> objectType, Class<T> attributeType, String attributeKey) {
		super(objectType, attributeType, "attribute." + attributeKey);
		this.attributeKey = attributeKey;
	}

	public Iterable<T> getValues(E object, QueryOptions queryOptions) {
		List<T> values = new ArrayList<>();
		for (XAttributable attributable: getAttributables(object)) {
			final XAttribute attr = attributable.getAttributes().get(attributeKey);
			if (attr != null && correctType(attr)) {
				values.add(getValue(attr));
			}
		}
		return values;
	}

	abstract protected Iterable<? extends XAttributable> getAttributables(E object);

	abstract protected T getValue(XAttribute attr);

	abstract protected boolean correctType(XAttribute attr);

}