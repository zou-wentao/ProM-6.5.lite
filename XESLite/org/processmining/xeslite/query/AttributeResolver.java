package org.processmining.xeslite.query;

import java.util.Date;

import com.googlecode.cqengine.attribute.Attribute;

/**
 * Resolves the actual attribute value of a query  
 * 
 * @author F. Mannhardt
 *
 * @param <A>
 */
public interface AttributeResolver<A> {

	/**
	 * Returns the actual class of the attribute
	 * 
	 * @param attributeName
	 * @return
	 */
	Class<?> getAttributeType(String attributeName);

	/**
	 * String attribute within event, also handles booleans
	 * 
	 * @param eventName
	 * @param attributeName
	 * @return
	 */
	Attribute<A, String> stringAttribute(String eventName, String attributeName);

	/**
	 * String attribute also handles booleans
	 * 
	 * @param attributeName
	 * @return
	 */
	Attribute<A, String> stringAttribute(String attributeName);

	/**
	 * Double attribute within event, also handles longs/integers
	 * 
	 * @param eventName
	 * @param attributeName
	 * @return
	 */
	Attribute<A, Double> doubleAttribute(String eventName, String attributeName);

	/**
	 * Double attribute also handles longs/integers
	 * 
	 * @param attributeName
	 * @return
	 */
	Attribute<A, Double> doubleAttribute(String attributeName);

	/**
	 * Date attribute
	 * 
	 * @param eventName
	 * @param attributeName
	 * @return
	 */
	Attribute<A, Date> dateAttribute(String eventName, String attributeName);

	/**
	 * Date attribute
	 * 
	 * @param attributeName
	 * @return
	 */
	Attribute<A, Date> dateAttribute(String attributeName);

}
