package org.j3lsmp.chessengine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.RecursiveTask;

/**
 * A recursive task used in {@link MoveSearcher#threadPool} to evaluate the best move on a board state
 * 
 * @author Jaden
 * @since 0.0.1
 */
@SuppressWarnings("serial")
class IterativeDeepeningTask extends RecursiveTask<Integer> {
	/**
	 * The board to evaluate
	 */
	private final ChessBoard board;
	
	/**
	 * Values used for iterative deepening and alpha-beta pruning
	 */
	private final int depth, alpha, beta;
	
	/**
	 * Values used to terminate after specified time
	 */
	private final long startTime, timeLimitMs;
	
	/**
	 * The current best-move-found
	 */
	public Move bestMove;
	
	/**
	 * Initializes an iterative task with the given parameters
	 * @param board the board to execute on
	 * @param depth the current remaining depth
	 * @param alpha the current alpha value for pruning
	 * @param beta the current beta value for pruning
	 * @param startTime the time this move calculation started
	 * @param timeLimitMs the allocated time for this move calculation in milliseconds
	 */
	IterativeDeepeningTask(ChessBoard board, int depth, int alpha, int beta, long startTime, long timeLimitMs) {
		this.board = board;
		this.depth = depth;
		this.alpha = alpha;
		this.beta = beta;
		this.startTime = startTime;
		this.timeLimitMs = timeLimitMs;
	}
	
	/**
	 * Execute this thread.
	 */
	@Override
	protected Integer compute() {
		if (System.currentTimeMillis() - startTime >= timeLimitMs) {
			return 0;
		}
		return alphaBeta(board, depth, alpha, beta);
	}
	
	/**
	 * Recursive minimax algorithm with alpha-beta pruning for move evaluation
	 * 
	 * @param board the board to evaluate moves on
	 * @param depth the iterations remaining on the board, used for iterative deepening to fulfill a time restriction
	 * @param alpha the current alpha value for pruning
	 * @param beta the current beta value for pruning
	 * @return the numeric evaluation of the board state reached, negative favors black, positive favors white
	 */
	private int alphaBeta(ChessBoard board, int depth, int alpha, int beta) {
		if (depth == 0 /* TODO || board.isGameOver()*/)
			return board.calculateBoardValue();
		
		long zobristKey = board.computeZobristHash();
		TranspositionEntry entry = MoveSearcher.transpositionTable.get(zobristKey);
		if (entry != null && entry.depth >= depth) {
			if (entry.flag == TranspositionEntry.EXACT)
				return entry.value;
			else if (entry.flag == TranspositionEntry.LOWER_BOUND)
				alpha = Math.max(alpha,  entry.value);
			else if (entry.flag == TranspositionEntry.UPPER_BOUND)
				beta = Math.min(beta, entry.value);		

			if (alpha >= beta)
				return entry.value;
		}
		
		HashSet<Move> moves = board.generateMoves();
		if (moves.isEmpty())
			return board.isWhiteTurn ? Integer.MIN_VALUE : Integer.MAX_VALUE; //TODO: add check for stalemate, return 0 if so
		
		Move bestMoveLocal = null;
		int bestValue = board.isWhiteTurn ? Integer.MIN_VALUE : Integer.MAX_VALUE;
		
		List<UpcomingTask> tasks = new ArrayList<>();
		
		for (Move move : moves) {
			MoveSearcher.futuresConsidered++;
			ChessBoard nextBoard = new ChessBoard(board, move);
			IterativeDeepeningTask task = new IterativeDeepeningTask(nextBoard, depth - 1, alpha, beta, startTime, timeLimitMs);
			
			if (depth >= 3) {
				task.fork();
				tasks.add(new UpcomingTask(task, move));
			} else {
				int eval = task.compute();
			
				if ((board.isWhiteTurn && eval > bestValue) || (!board.isWhiteTurn && eval < bestValue)) {
					bestValue = eval;
					bestMoveLocal = move;
				}
				
				if (board.isWhiteTurn)
					alpha = Math.max(alpha,  eval);
				else
					beta = Math.min(beta, eval);
				
				if (alpha >= beta)
					break;
			}
		}
		
		for (UpcomingTask task : tasks) {
			int eval = task.task().join();
			if ((board.isWhiteTurn && eval > bestValue) || (!board.isWhiteTurn && eval < bestValue)) {
				bestValue = eval;
				bestMoveLocal = task.move();
			}
			
			if (board.isWhiteTurn)
				alpha = Math.max(alpha, eval);
			else
				beta = Math.min(beta, eval);
			
			if (alpha >= beta)
				break;
		}
		
		int flag = (bestValue <= alpha) ? TranspositionEntry.UPPER_BOUND
				: (bestValue >= beta) ? TranspositionEntry.LOWER_BOUND
				: TranspositionEntry.EXACT;
		MoveSearcher.transpositionTable.put(zobristKey, new TranspositionEntry(bestValue, depth, flag));

		this.bestMove = bestMoveLocal;
		return bestValue;
	}
}