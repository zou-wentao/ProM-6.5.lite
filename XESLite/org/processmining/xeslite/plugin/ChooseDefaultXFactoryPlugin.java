package org.processmining.xeslite.plugin;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.util.ui.widgets.helper.ProMUIHelper;
import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;
import org.processmining.xeslite.external.XFactoryExternalStore;
import org.xeslite.lite.factory.XFactoryLiteImpl;

public class ChooseDefaultXFactoryPlugin {

	public interface XFactoryFactory {
		XFactory newInstance();
	}

	public enum XFactoryImplementation {
		NAIVE("OpenXES Naive", new XFactoryFactory() {

			public XFactory newInstance() {
				return new XFactoryNaiveImpl();
			}

		}), LITE("XESLite - Lite", new XFactoryFactory() {

			public XFactory newInstance() {
				return new XFactoryLiteImpl();
			}

		}), MAPDB_DISK("XESLite - MapDB ", new XFactoryFactory() {

			public XFactory newInstance() {
				return new XFactoryExternalStore.MapDBDiskImpl();
			}

		}), MAPDB_DISK_NOCACHE("XESLite - MapDB (no cache)", new XFactoryFactory() {

			public XFactory newInstance() {
				return new XFactoryExternalStore.MapDBDiskWithoutCacheImpl();
			}

		}), MAPDB_DISK_SEQUENTIAL("XESLite - MapDB (sequential)", new XFactoryFactory() {

			public XFactory newInstance() {
				return new XFactoryExternalStore.MapDBDiskSequentialAccessImpl();
			}

		}), IN_MEMORY("XESLite - In Memory", new XFactoryFactory() {

			public XFactory newInstance() {
				return new XFactoryExternalStore.InMemoryStoreImpl();
			}

		});

		private final XFactoryFactory factory;
		private final String desc;

		private XFactoryImplementation(String desc, XFactoryFactory factory) {
			this.desc = desc;
			this.factory = factory;
		}

		public XFactory newFactory() {
			return factory.newInstance();
		}

		@Override
		public String toString() {
			return desc;
		}

	}

	@Plugin(name = "Switch Default XES Implementation (Default XFactory)", parameterLabels = {}, //			
			level = PluginLevel.Regular, returnLabels = {}, returnTypes = {}, userAccessible = true, mostSignificantResult = -1)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = " F. Mannhardt", email = "f.mannhardt@tue.nl")
	public void switchXFactory(UIPluginContext context) {

		try {
			XFactoryImplementation userChoice = ProMUIHelper.queryForObject(context,
					"Please choose one of the following available XFactory implementations:",
					XFactoryImplementation.values());
			XFactoryRegistry.instance().setCurrentDefault(userChoice.newFactory());
			context.log("Setting your default XFactory to " + userChoice.toString(), MessageLevel.NORMAL);

		} catch (UserCancelledException e) {
			context.log("XFactory remains unchanged: "
					+ XFactoryRegistry.instance().currentDefault().getClass().getSimpleName());
		}

	}

}
