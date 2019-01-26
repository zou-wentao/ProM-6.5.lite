package org.processmining.xeslite.query;

import static com.googlecode.cqengine.query.QueryFactory.all;
import static com.googlecode.cqengine.query.QueryFactory.equal;
import static com.googlecode.cqengine.query.QueryFactory.matchesRegex;
import static com.googlecode.cqengine.query.QueryFactory.noQueryOptions;
import static com.googlecode.cqengine.query.QueryFactory.or;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XLogImpl;
import org.processmining.log.utils.XUtils;
import org.processmining.xeslite.query.attributes.LiteralXAttribute;
import org.processmining.xeslite.query.syntax.ParseException;
import org.processmining.xeslite.query.syntax.QueryParser;
import org.processmining.xeslite.query.syntax.QueryParserVisitor;
import org.processmining.xeslite.query.syntax.QueryRoot;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.MultiValueNullableAttribute;
import com.googlecode.cqengine.attribute.SimpleNullableAttribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.query.option.QueryOptions;

/**
 * Static methods to work with queries and indexed traces/events. For building
 * queries manually look at {@link QueryFactory} and the documentation of
 * cqengine.
 * 
 * @author F. Mannhardt
 *
 */
public final class XIndex {

	private XIndex() {
	}

	public static XIndexedLog newLog(XLog log) {
		return new XIndexedLogImpl(log);
	}

	public static XIndexedLog newLog(Collection<XTrace> traces) {
		if (traces instanceof XLog) {
			return new XIndexedLogImpl((XLog) traces);
		} else {
			XLogImpl logWrapper = new XLogImpl(new XAttributeMapImpl());
			logWrapper.addAll(traces);
			return new XIndexedLogImpl(logWrapper);
		}
	}

	public static XIndexedTraces newTraces(Collection<XTrace> traces) {
		return new XIndexedTracesImpl(traces);
	}

	public static XIndexedTraces newTraces() {
		return new XIndexedTracesImpl();
	}

	public static XIndexedEvents newEvents(Collection<XEvent> events) {
		return new XIndexedEventsImpl(events);
	}

	public static XIndexedEvents newEvents() {
		return new XIndexedEventsImpl();
	}

	public static <A> Iterable<A> filter(Iterable<A> unfiltered, final Query<A> query) {
		return Iterables.filter(unfiltered, new Predicate<A>() {

			public boolean apply(A a) {
				return query.matches(a, noQueryOptions());
			}
		});
	}

	public static <A> Iterable<A> filter(Iterable<A> unfiltered, String queryString,
			AttributeResolver<A> attributeResolver, Class<A> type) throws ParseException {
		Query<A> query = buildQuery(queryString, attributeResolver, type);
		return Iterables.filter(unfiltered, convertToPredicate(query));
	}

	public static <A> Predicate<A> newPredicate(String queryString, AttributeResolver<A> attributeResolver,
			Class<A> type) throws ParseException {
		Query<A> query = buildQuery(queryString, attributeResolver, type);
		return convertToPredicate(query);
	}

	public static Iterable<XTrace> filterTracesOnAttributes(Iterable<XTrace> unfiltered, String queryString,
			AttributeTypeResolver attributeTypeResolver) throws ParseException {
		final Query<XTrace> query = buildAttributesQueryForTrace(queryString, attributeTypeResolver);
		return filter(unfiltered, query);
	}

	public static Iterable<XEvent> filterEventsOnAttributes(Iterable<XEvent> unfiltered, String queryString,
			AttributeTypeResolver attributeTypeResolver) throws ParseException {
		final Query<XEvent> query = buildAttributesQueryForEvent(queryString, attributeTypeResolver);
		return filter(unfiltered, query);
	}

	public static Iterable<XTrace> filterTracesOnAttributesOrNames(Iterable<XTrace> unfiltered, String queryString,
			AttributeTypeResolver attributeTypeResolver) throws ParseException {
		final Query<XTrace> query = buildAttributesOrNamesQueryForTrace(queryString, attributeTypeResolver);
		return filter(unfiltered, query);
	}

	public static Iterable<XEvent> filterEventsOnAttributesOrNames(Iterable<XEvent> unfiltered, String queryString,
			AttributeTypeResolver attributeTypeResolver) throws ParseException {
		Query<XEvent> query = buildAttributesOrNamesQueryForEvent(queryString, attributeTypeResolver);
		return filter(unfiltered, query);
	}

	public static Predicate<XTrace> newTracePredicateOnAttributes(String queryString,
			AttributeTypeResolver attributeTypeResolver) throws ParseException {
		final Query<XTrace> query = buildAttributesQueryForTrace(queryString, attributeTypeResolver);
		return convertToPredicate(query);
	}

	public static Predicate<XTrace> newTracePredicateOnAttributesOrNames(String queryString,
			AttributeTypeResolver attributeTypeResolver) throws ParseException {
		final Query<XTrace> query = buildAttributesOrNamesQueryForTrace(queryString, attributeTypeResolver);
		return convertToPredicate(query);
	}

	public static Predicate<XEvent> newEventPredicateOnAttributes(String queryString,
			AttributeTypeResolver attributeTypeResolver) throws ParseException {
		final Query<XEvent> query = buildAttributesQueryForEvent(queryString, attributeTypeResolver);
		return convertToPredicate(query);
	}

	public static Predicate<XEvent> newEventPredicateOnAttributesOrNames(String queryString,
			AttributeTypeResolver attributeTypeResolver) throws ParseException {
		final Query<XEvent> query = buildAttributesOrNamesQueryForEvent(queryString, attributeTypeResolver);
		return convertToPredicate(query);
	}

	public static <A> Predicate<A> convertToPredicate(final Query<A> query) {
		return new Predicate<A>() {

			public boolean apply(A trace) {
				return query.matches(trace, noQueryOptions());
			}
		};
	}

	public static Query<XTrace> buildAttributesOrNamesQueryForTrace(String queryString,
			final AttributeTypeResolver attributeTypeResolver) throws ParseException {
		if (!queryString.isEmpty() && queryString.charAt(0) == '~') {
			if (queryString.length() > 1) {
				queryString = queryString.substring(1);
				return or(QueryFactory.matchesRegex(traceName(), queryString),
						QueryFactory.matchesRegex(XIndex.allEventNames(), queryString));
			} else {
				throw new ParseException("Missing regular expression after '~'");
			}
		} else if (!queryString.isEmpty() && queryString.charAt(0) == '%') {
			if (queryString.length() > 1) {
				queryString = queryString.substring(1);
				return or(QueryFactory.contains(traceName(), queryString),
						QueryFactory.contains(XIndex.allEventNames(), queryString));
			} else {
				throw new ParseException("Missing query after '%'");
			}
		} else if (!queryString.isEmpty()) {
			try {
				return XIndex.buildAttributesQueryForTrace(queryString, attributeTypeResolver);
			} catch (ParseException e) {
				Pattern queryPattern = Pattern.compile("(=|>|<|~|<=|>=|\\!=)", Pattern.CASE_INSENSITIVE);
				if (!queryPattern.matcher(queryString).find()) {
					// Simple query without any operator -> search in names
					return QueryFactory.or(equal(traceName(), queryString), equal(XIndex.allEventNames(), queryString));
				} else {
					throw e;
				}
			}
		} else {
			return all(XTrace.class);
		}
	}

	public static Query<XEvent> buildAttributesOrNamesQueryForEvent(String queryString,
			final AttributeTypeResolver attributeTypeResolver) throws ParseException {
		if (!queryString.isEmpty() && queryString.charAt(0) == '~') {
			if (queryString.length() > 1) {
				queryString = queryString.substring(1);
				return matchesRegex(eventName(), queryString);
			} else {
				throw new ParseException("Missing regular expression after '~'");
			}
		} else if (!queryString.isEmpty() && queryString.charAt(0) == '%') {
			if (queryString.length() > 1) {
				queryString = queryString.substring(1);
				return QueryFactory.contains(eventName(), queryString);
			} else {
				throw new ParseException("Missing query after '%'");
			}
		} else if (!queryString.isEmpty()) {
			try {
				return buildAttributesQueryForEvent(queryString, attributeTypeResolver);
			} catch (ParseException e) {
				Pattern queryPattern = Pattern.compile("(=|>|<|~|<=|>=|\\!=|and|or)", Pattern.CASE_INSENSITIVE);
				if (!queryPattern.matcher(queryString).find()) {
					// Simple query without any operator -> search in names
					return equal(eventName(), queryString);
				} else {
					throw e;
				}
			}
		} else {
			// Fall back empty query
			return all(XEvent.class);
		}
	}

	public static Query<XTrace> buildAttributesQueryForTrace(String queryString,
			final AttributeTypeResolver attributeTypeResolver) throws ParseException {
		return buildQuery(queryString, new AttributeResolverTrace() {

			public Class<?> getAttributeType(String attributeName) {
				return attributeTypeResolver.getAttributeType(attributeName);
			}

		}, XTrace.class);
	}

	public static Query<XEvent> buildAttributesQueryForEvent(String queryString,
			final AttributeTypeResolver attributeTypeResolver) throws ParseException {
		return buildQuery(queryString, new AttributeResolverEvent() {

			public Class<?> getAttributeType(String attributeName) {
				return attributeTypeResolver.getAttributeType(attributeName);
			}

		}, XEvent.class);
	}

	@SuppressWarnings("unchecked")
	public static <A> Query<A> buildQuery(String queryString, final AttributeResolver<A> attributeResolver,
			Class<A> type) throws ParseException {
		if (!queryString.isEmpty()) {
			QueryParser queryParser = new QueryParser(queryString);
			QueryRoot rootNode = queryParser.parse();
			QueryParserVisitor visitor = new QueryBuilderVisitor<A>(attributeResolver);
			return (Query<A>) rootNode.jjtAccept(visitor, null);
		} else {
			return QueryFactory.all(type);
		}
	}

	/**
	 * @return Attribute concept:name of the event
	 */
	public static final Attribute<XEvent, String> eventName() {
		return new LiteralXAttribute<XEvent>(XEvent.class, "concept:name") {

			protected Iterable<XAttributable> getAttributables(XEvent event) {
				return Collections.<XAttributable>singleton(event);
			}

		};
	}

	/**
	 * @return Attribute concept:name on the trace
	 */
	public static final SimpleNullableAttribute<XTrace, String> traceName() {
		return new SimpleNullableAttribute<XTrace, String>("trace.concept:name") {

			public String getValue(XTrace trace, QueryOptions queryOptions) {
				return XUtils.getConceptName(trace);
			}
		};
	}

	/**
	 * @return Attribute concept:name of events contained in traces
	 */
	public static final Attribute<XTrace, String> allEventNames() {
		return new MultiValueNullableAttribute<XTrace, String>("event.concept:name", true) {

			public Iterable<String> getNullableValues(XTrace trace, QueryOptions queryOptions) {
				return Iterables.transform(trace, new Function<XEvent, String>() {

					public String apply(XEvent event) {
						return XUtils.getConceptName(event);
					}
				});
			}

		};
	}

}
