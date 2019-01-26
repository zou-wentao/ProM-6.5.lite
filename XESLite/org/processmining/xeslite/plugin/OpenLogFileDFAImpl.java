package org.processmining.xeslite.plugin;

import java.io.File;
import java.io.InputStream;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.xeslite.dfa.XLogDFAXmlParser;

@Plugin(name = "Open XES Log File (XESLite - Automaton (read only, concept:name only))", //
		level = PluginLevel.Regular, parameterLabels = { "Filename" }, returnLabels = {
				"Log (single process)" }, returnTypes = { XLog.class })
@UIImportPlugin(description = "ProM log files (XESLite - Automaton (read only, concept:name only))", //
		extensions = { "gz", "zip", "xes", "xez" }) // does not support MXML parser
public class OpenLogFileDFAImpl extends AbstractOpenXESFilePlugin {

	@Override
	protected Object importFromFile(PluginContext context, File file) throws Exception {
		InputStream inputStream = getInputStream(file);
		XLogDFAXmlParser dfaParser = new XLogDFAXmlParser();
		XLog log = dfaParser.parse(inputStream);
		if (context != null) {
			context.getFutureResult(0).setLabel(XConceptExtension.instance().extractName(log));
		}		
		return log;
	}

	@Override
	protected Object importFromStreamWithFactory(PluginContext context, InputStream input, String fileName,
			long fileSizeInBytes, XFactory factory) throws Exception {
		throw new UnsupportedOperationException();
	}

	protected void beforeParse(XFactory factory) {
	}

	protected void afterParse(XFactory factory) {
	}

}