package org.processmining.xeslite.query;

import java.util.Collection;

import org.processmining.xeslite.query.syntax.ParseException;

import com.googlecode.cqengine.index.Index;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.resultset.ResultSet;

public interface XIndexedAttributable<T> {

	/**
	 * @param index
	 */
	void addIndex(Index<T> index);

	/**
	 * @param query
	 * @return
	 */
	ResultSet<T> retrieve(Query<T> query);

	/**
	 * @param query
	 * @return
	 * @throws ParseException
	 */
	Query<T> parseQuery(String query) throws ParseException;

	/**
	 * @param query
	 * @param attributeTypeResolver
	 * @return
	 * @throws ParseException
	 */
	Query<T> parseQuery(String query, AttributeTypeResolver attributeTypeResolver) throws ParseException;
	
	/**
	 * @param query
	 * @return
	 * @throws ParseException
	 */
	ResultSet<T> retrieve(String query) throws ParseException;

	/**
	 * Add an element
	 *
	 * @param element
	 */
	void add(T element);

	/**
	 * Adds all elements
	 *
	 * @param element
	 */
	void addAll(Collection<T> element);
}