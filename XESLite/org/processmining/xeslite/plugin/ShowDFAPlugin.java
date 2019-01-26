package org.processmining.xeslite.plugin;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.plugins.graphviz.dot.Dot;
import org.xeslite.dfa.XLogDFABuilder;

public class ShowDFAPlugin {

	@Plugin(name = "Build Numbered DAFA based on Log (Numbered States)", parameterLabels = { "Log" }, //			
			level = PluginLevel.Regular, returnLabels = { "DFA Dot" }, returnTypes = {
					Dot.class }, userAccessible = true, mostSignificantResult = 1)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = " F. Mannhardt", email = "f.mannhardt@tue.nl")
	public Dot showDFAStates(UIPluginContext context, XLog log) {

		try {
			return createDFADot(log, true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	@Plugin(name = "Build Numbered DAFA based on XLog (Numbered Transitions)", parameterLabels = { "Log" }, //			
			level = PluginLevel.Local, returnLabels = { "DFA Dot" }, returnTypes = {
					Dot.class }, userAccessible = true, mostSignificantResult = 1)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = " F. Mannhardt", email = "f.mannhardt@tue.nl")
	public Dot showDFATransitions(UIPluginContext context, XLog log) {

		try {
			return createDFADot(log, false);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private Dot createDFADot(XLog log, boolean useSuffixes) throws IOException {
		XLogDFABuilder builder = new XLogDFABuilder(useSuffixes);
		builder.addLog(log);
		return new Dot(IOUtils.toInputStream(builder.build().toDot()));
	}

}
