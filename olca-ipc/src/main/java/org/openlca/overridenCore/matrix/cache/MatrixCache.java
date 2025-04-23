package org.openlca.overridenCore.matrix.cache;

import org.openlca.core.database.IDatabase;

public class MatrixCache extends org.openlca.core.matrix.cache.MatrixCache {


	public static MatrixCache lazyInstance; // singleton instance
	public static MatrixCache instance; // Singleton instance

	// changed with singleton pattern
	protected FlowTable flowTypeTable;
	protected ProcessTable processTable;


	public static MatrixCache create(IDatabase database) {
		if (instance == null) {
			instance = new MatrixCache(database, false);
		}
		instance.exchangeCache = null;
		return instance;
	}

	public static MatrixCache createLazy(IDatabase database) {
		if (lazyInstance == null) {
			lazyInstance = new MatrixCache(database, true);
		}
		return lazyInstance;
	}

	protected MatrixCache(IDatabase database, boolean lazy) {
		super(database, lazy);
	}

	public FlowTable getFlowTypeTable() {
		long startTime = System.nanoTime();
		if (flowTypeTable == null)
			flowTypeTable = FlowTable.create(database);
		long endTime = System.nanoTime();
		System.out.println("FlowTypeTable retrieve time: " + (endTime - startTime) / 1_000_000 + " ms");
		return flowTypeTable;
	}

	public ProcessTable getProcessTable() {
		System.out.println("ProcessTable creation started...");
		long startTime = System.nanoTime();
		if (processTable == null)
			processTable = ProcessTable.create(database);
		long endTime = System.nanoTime();
		System.out.println("ProcessTable creation time: " + (endTime - startTime) / 1_000_000 + " ms");
		return processTable;
	}

}
