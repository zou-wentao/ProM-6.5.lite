package org.processmining.xeslite.query;

import java.util.Collections;
import java.util.Date;

import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XEvent;
import org.processmining.xeslite.query.attributes.DateXAttribute;
import org.processmining.xeslite.query.attributes.DoubleXAttribute;
import org.processmining.xeslite.query.attributes.LiteralXAttribute;

import com.googlecode.cqengine.attribute.Attribute;

abstract public class AttributeResolverEvent implements AttributeResolver<XEvent> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.xeslite.query.AttributeResolver#stringAttribute(java
	 * .lang.String, java.lang.String)
	 */
	public Attribute<XEvent, String> stringAttribute(String eventName, String attributeName) {
		// Ignore event parameter
		return stringAttribute(attributeName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.xeslite.query.AttributeResolver#doubleAttribute(java
	 * .lang.String, java.lang.String)
	 */
	public Attribute<XEvent, Double> doubleAttribute(String eventName, String attributeName) {
		// Ignore event parameter
		return doubleAttribute(attributeName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.xeslite.query.AttributeResolver#dateAttribute(java.
	 * lang.String, java.lang.String)
	 */
	public Attribute<XEvent, Date> dateAttribute(String eventName, String attributeName) {
		// Ignore event parameter
		return dateAttribute(attributeName);
	}


	/* (non-Javadoc)
	 * @see org.processmining.xeslite.query.AttributeResolver#stringAttribute(java.lang.String)
	 */
	public Attribute<XEvent, String> stringAttribute(String attributeName) {
		return new LiteralXAttribute<XEvent>(XEvent.class, attributeName) {

			protected Iterable<XAttributable> getAttributables(XEvent event) {
				return Collections.<XAttributable>singleton(event);
			}

		};
	}

	/* (non-Javadoc)
	 * @see org.processmining.xeslite.query.AttributeResolver#doubleAttribute(java.lang.String)
	 */
	public Attribute<XEvent, Double> doubleAttribute(String attributeName) {
		return new DoubleXAttribute<XEvent>(XEvent.class, attributeName) {

			protected Iterable<XAttributable> getAttributables(XEvent event) {
				return Collections.<XAttributable>singleton(event);
			}

		};
	}

	/* (non-Javadoc)
	 * @see org.processmining.xeslite.query.AttributeResolver#dateAttribute(java.lang.String)
	 */
	public Attribute<XEvent, Date> dateAttribute(String attributeName) {
		return new DateXAttribute<XEvent>(XEvent.class, attributeName) {

			protected Iterable<XAttributable> getAttributables(XEvent event) {
				return Collections.<XAttributable>singleton(event);
			}

		};
	}

}
