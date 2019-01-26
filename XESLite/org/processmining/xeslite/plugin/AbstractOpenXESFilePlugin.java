package org.processmining.xeslite.plugin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.in.XMxmlParser;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.log.XContextMonitoredInputStream;
import org.xeslite.common.XESLiteException;
import org.xeslite.external.XFactoryExternalStore;
import org.xeslite.parser.XesLiteXmlParser;

import com.google.common.collect.Iterators;

abstract public class AbstractOpenXESFilePlugin {

	@PluginVariant(requiredParameterLabels = { 0 })
	public Object importFile(PluginContext context, String filename) throws Exception {
		File file = new File(filename);
		return importFile(context, file);
	}

	@PluginVariant(requiredParameterLabels = { 0 })
	public Object importFile(PluginContext context, File file) throws Exception {
		return importFromFile(context, file);
	}

	@PluginVariant(requiredParameterLabels = { 0 })
	public Object importFile(PluginContext context, URI uri) throws Exception {
		File file = new File(uri);
		return importFromFile(context, file);
	}

	@PluginVariant(requiredParameterLabels = { 0 })
	public Object importFile(PluginContext context, URL url) throws Exception {
		File file = new File(url.toURI());
		return importFromFile(context, file);
	}

	abstract protected Object importFromFile(PluginContext context, File file) throws Exception;

	abstract protected void beforeParse(XFactory factory);

	abstract protected void afterParse(XFactory factory);

	protected InputStream getInputStream(File file) throws Exception {
		if (file.getName().endsWith(".zip")) {
			final ZipFile zip = new ZipFile(file);
			Enumeration<? extends ZipEntry> entries = zip.entries();
			ZipEntry zipEntry = entries.nextElement();
			if (entries.hasMoreElements()) {
				throw new XESLiteException("Zipped log files should not contain more than one entry.");
			}
			final InputStream is = zip.getInputStream(zipEntry);
			return new InputStream() {

				public int read() throws IOException {
					return is.read();
				}

				public int read(byte[] b) throws IOException {
					return is.read(b);
				}

				public int read(byte[] b, int off, int len) throws IOException {
					return is.read(b, off, len);
				}

				public long skip(long n) throws IOException {
					return is.skip(n);
				}

				public int available() throws IOException {
					return is.available();
				}

				public synchronized void mark(int readlimit) {
					is.mark(readlimit);
				}

				public synchronized void reset() throws IOException {
					is.reset();
				}

				public boolean markSupported() {
					return is.markSupported();
				}

				public void close() throws IOException {
					is.close();
					// also close the zip file
					zip.close();
				}

			};
		}
		FileInputStream stream = new FileInputStream(file);
		if (file.getName().endsWith(".gz") || file.getName().endsWith(".xez")) {
			return new GZIPInputStream(stream, 65536);
		}
		return new BufferedInputStream(stream, 65536);
	}

	protected Object importFromStreamWithFactory(PluginContext context, InputStream input, String fileName,
			long fileSizeInBytes, XFactory factory) throws Exception {

		beforeParse(factory);
		Collection<XLog> logs = parseFile(fileName,
				new XContextMonitoredInputStream(input, fileSizeInBytes, context.getProgress()), factory);
		afterParse(factory);

		if (logs.size() == 0) {
			throw new Exception("No processes contained in log! Parser could did not find any log.");
		}

		XLog log = Iterators.getOnlyElement(logs.iterator());
		if (XConceptExtension.instance().extractName(log) == null) {
			XConceptExtension.instance().assignName(log, "Anonymous log imported from " + fileName);
		}

		if (context != null) {
			context.getFutureResult(0).setLabel(XConceptExtension.instance().extractName(log));
		}

		return log;
	}

	protected void log(String msg) {
		System.out.println(msg);
	}

	private final Collection<XLog> parseFile(String fileName, XContextMonitoredInputStream input, XFactory factory)
			throws Exception {

		XParser parser;
		if (isXES(fileName)) {
			parser = new XesLiteXmlParser(factory, true);
		} else {
			if (!(factory instanceof XFactoryExternalStore)) {
				parser = new XMxmlParser(factory);
			} else {
				throw new Exception("This importer does not support parsing MXML! Please use a different importer!");
			}
		}

		return parser.parse(input);
	}

	private boolean isXES(String filename) {
		return filename.toLowerCase().endsWith(".gz") 
				|| filename.toLowerCase().endsWith(".xes") 
				|| filename.toLowerCase().endsWith(".xez")
				|| filename.toLowerCase().endsWith(".xes.gz")
				|| (filename.toLowerCase().endsWith("zip") && !filename.toLowerCase().contains("mxml")); // just assume it is a XES file in there
	}

}