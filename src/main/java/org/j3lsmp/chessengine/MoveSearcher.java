package org.j3lsmp.chessengine;

import java.util.concurrent.ForkJoinPool;

/**
 * Class containing various components to find the best move on a board
 * 
 * @author Jaden
 * @since 0.0.1
 */
public class MoveSearcher {
	/**
	 * The maximum depth that {@link #findBestMove(ChessBoard, long)} will search to. Typically will not reach this depth, especially on board states with many possible moves
	 */
	private static final int MAX_DEPTH = 10;
	
	/**
	 * Data about move search to be returned by {@link APIController#computerMove(Integer)}
	 */
	static int boardsEvaluated = 0, futuresConsidered = 0, movesAhead = 0;
	
	/**
	 * Pool of threads for {@link #findBestMove(ChessBoard, long)}
	 */
	static final ForkJoinPool threadPool = new ForkJoinPool();
	
	/**
	 * Transposition table holding evaluations of already-evaluated boards, mapped from zobrist hashes of board states
	 */
	static final TranspositionTable transpositionTable = new TranspositionTable();
	
	/**
	 * Find the best move on the specified board
	 * @param board the board to use
	 * @param timeLimitMs time limit on execution, in milliseconds
	 * @return the best move found
	 */
	public static Move findBestMove(ChessBoard board, long timeLimitMs) {
		Move bestMove = null;
		long startTime = System.currentTimeMillis(); //Yes, we'll be liable to leap seconds, etc., but a nanoTime() call takes much longer
		
		boardsEvaluated = 0;
		futuresConsidered = 0;
		movesAhead = 0;
		
		for (int depth = 1; depth <= MAX_DEPTH; depth++) {
			movesAhead = depth;
			IterativeDeepeningTask task = new IterativeDeepeningTask(board, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, startTime, timeLimitMs);
			@SuppressWarnings("unused")
			int score = threadPool.invoke(task);
			if (task.bestMove != null)
				bestMove = task.bestMove;
			if (System.currentTimeMillis() - startTime >= timeLimitMs)
				break;
		}
		
		return bestMove;
	}
}