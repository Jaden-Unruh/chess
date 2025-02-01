package org.j3lsmp.chessengine;

/**
 * Used to hold data for move evaluation tasks scheduled to be executed but awaiting a thread, kind of...
 * 
 * @param task the task to execute
 * @param move the move that yielded the board state of the task
 * 
 * @author Jaden
 * @since 0.0.1
 */
record UpcomingTask(IterativeDeepeningTask task, Move move) {}