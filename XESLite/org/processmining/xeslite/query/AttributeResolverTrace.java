package org.processmining.xeslite.query;

import java.util.Date;

import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.log.utils.XUtils;
import org.processmining.xeslite.query.attributes.DateXAttribute;
import org.processmining.xeslite.query.attributes.DoubleXAttribute;
import org.processmining.xeslite.query.attributes.LiteralXAttribute;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.googlecode.cqengine.attribute.Attribute;

abstract public class AttributeResolverTrace implements AttributeResolver<XTrace> {

	public Attribute<XTrace, String> stringAttribute(final String eventName, String attributeName) {
		return new LiteralXAttribute<XTrace>(XTrace.class, attributeName) {

			protected Iterable<? extends XAttributable> getAttributables(XTrace trace) {
				return extractAttributables(eventName, trace);
			}

		};
	}

	public Attribute<XTrace, Double> doubleAttribute(final String eventName, String attributeName) {
		return new DoubleXAttribute<XTrace>(XTrace.class, attributeName) {

			protected Iterable<? extends XAttributable> getAttributables(XTrace trace) {
				return extractAttributables(eventName, trace);
			}

		};
	}

	public Attribute<XTrace, Date> dateAttribute(final String eventName, String attributeName) {
		return new DateXAttribute<XTrace>(XTrace.class, attributeName) {

			protected Iterable<? extends XAttributable> getAttributables(XTrace trace) {
				return extractAttributables(eventName, trace);
			}

		};
	}

	/**
	 * String attribute for {@link XAttributeLiteral} and
	 * {@link XAttributeBoolean}
	 * 
	 * @param attributeName
	 * @return
	 */
	public Attribute<XTrace, String> stringAttribute(String attributeName) {
		return new LiteralXAttribute<XTrace>(XTrace.class, attributeName) {

			protected Iterable<? extends XAttributable> getAttributables(XTrace trace) {
				return extractAttributables(trace);
			}

		};
	}

	/**
	 * Double attribute for {@link XAttributeContinuous} and
	 * {@link XAttributeDiscrete}
	 * 
	 * @param attributeName
	 * @return
	 */
	public Attribute<XTrace, Double> doubleAttribute(String attributeName) {
		return new DoubleXAttribute<XTrace>(XTrace.class, attributeName) {

			protected Iterable<? extends XAttributable> getAttributables(XTrace trace) {
				return extractAttributables(trace);
			}

		};
	}

	/**
	 * Date attribute
	 * 
	 * @param attributeName
	 * @return
	 */
	public Attribute<XTrace, Date> dateAttribute(String attributeName) {
		return new DateXAttribute<XTrace>(XTrace.class, attributeName) {

			protected Iterable<? extends XAttributable> getAttributables(XTrace trace) {
				return extractAttributables(trace);
			}

		};
	}

	protected Iterable<? extends XAttributable> extractAttributables(final String eventName, XTrace trace) {
		if (eventName != null) {
			return Iterables.filter(trace, new Predicate<XEvent>() {

				public boolean apply(XEvent event) {
					return eventName.equals(XUtils.getConceptName(event));
				}
			});
		} else {
			return extractAttributables(trace);
		}
	}

	private Iterable<? extends XAttributable> extractAttributables(XTrace trace) {
		return Iterables.concat(ImmutableList.of(trace), trace);
	}

}
