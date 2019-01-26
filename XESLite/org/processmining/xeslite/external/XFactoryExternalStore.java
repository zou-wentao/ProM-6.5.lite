package org.processmining.xeslite.external;

/**
 * Class moved to {@link org.xeslite.external.XFactoryExternalStore}!
 *
 */
public abstract class XFactoryExternalStore extends org.xeslite.external.XFactoryExternalStore {

	public static class MapDBDiskImpl extends org.xeslite.external.XFactoryExternalStore.MapDBDiskImpl {
	}
	
	public static class MapDBDiskWithoutCacheImpl extends org.xeslite.external.XFactoryExternalStore.MapDBDiskWithoutCacheImpl {
	}
	
	public static class MapDBDiskSequentialAccessImpl extends org.xeslite.external.XFactoryExternalStore.MapDBDiskSequentialAccessImpl {
	}
	
	public static class MapDBDiskSequentialAccessWithoutCacheImpl extends org.xeslite.external.XFactoryExternalStore.MapDBDiskSequentialAccessWithoutCacheImpl {
	}
	
	public static class InMemoryStoreImpl extends org.xeslite.external.XFactoryExternalStore.InMemoryStoreImpl {
	}
	
	public static class InMemoryStoreAlignmentAwareImpl extends org.xeslite.external.XFactoryExternalStore.InMemoryStoreAlignmentAwareImpl {
	}
	
}