package org.processmining.xeslite.query;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.query.Query;

interface QueryBuilder<A> {
	
	<T extends Comparable<T>> Query<A> build(Attribute<A, T> attr, T value);

}