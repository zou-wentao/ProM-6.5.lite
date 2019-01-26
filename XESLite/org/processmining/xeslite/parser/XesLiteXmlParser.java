package org.processmining.xeslite.parser;

import org.deckfour.xes.factory.XFactory;

/**
 * Use {@link org.xeslite.parser.XesLiteXmlParser}.
 *
 */
@Deprecated
public class XesLiteXmlParser extends org.xeslite.parser.XesLiteXmlParser {

	public XesLiteXmlParser(boolean isLenient) {
		super(isLenient);
	}

	public XesLiteXmlParser(XFactory factory, boolean isLenient) {
		super(factory, isLenient);
	}

}