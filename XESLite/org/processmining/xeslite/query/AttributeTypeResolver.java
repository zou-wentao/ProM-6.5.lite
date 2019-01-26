package org.processmining.xeslite.query;

/**
 * Resolves the type (i.e., Java class) of attributes that might be queried for.
 * 
 * @author F. Mannhardt
 *
 */
public interface AttributeTypeResolver {
	
	/**
	 * @param attributeName
	 * @return the type of the attribute
	 */
	Class<?> getAttributeType(String attributeName);
	
}