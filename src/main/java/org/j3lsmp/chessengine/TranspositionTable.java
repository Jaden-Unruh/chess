package org.j3lsmp.chessengine;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A table mapping zobrist hashes of board states to previously computed evaluations of them
 * 
 * @author Jaden
 * @since 0.0.1
 */
public class TranspositionTable {
	/**
	 * Hashmap of hashes to evaluations
	 */
	private final ConcurrentHashMap<Long, TranspositionEntry> table = new ConcurrentHashMap<>();
	
	/**
	 * Add the computed valuation to the table, unless an existing entry exists that was evaluated to a greater depth
	 * @param zobristKey Zobrist hash of the board state
	 * @param newEntry table entry to be (potentially) added
	 */
	void put(long zobristKey, TranspositionEntry newEntry) {
		table.compute(zobristKey, (key, existingEntry) -> {
			if (existingEntry == null || newEntry.depth > existingEntry.depth) {
				return newEntry;
			}
			return existingEntry;
		});
	}
	
	/**
	 * Get the valuation associated with the given hash
	 * @param zobristKey the hash
	 * @return the associated valuation (or null if none exists)
	 */
	TranspositionEntry get(long zobristKey) {
		return table.get(zobristKey);
	}
	
	/**
	 * Returns the number of entries in the table
	 * @return the entries in the table
	 */
	int size() {
		return table.size();
	}
}

/**
 * An entry in {@link TranspositionTable}
 * 
 * @author Jaden
 * @since 0.0.1
 */
class TranspositionEntry {
	/**
	 * Values of this entry
	 */
	int value, depth, flag;
	/**
	 * Potential values for {@link flag}, whether this table entry is an exact valuation or a bound on potential valuations
	 */
	static final int EXACT = 1, UPPER_BOUND = 2, LOWER_BOUND = 3;
	
	/**
	 * Constructs a transposition table entry with the specified values
	 * @param value the valuation of the table with a particular hash
	 * @param depth the depth to which the valuation was calculated
	 * @param flag what we know about the valuation
	 */
	TranspositionEntry(int value, int depth, int flag) {
		this.value = value;
		this.depth = depth;
		this.flag = flag;
	}
}