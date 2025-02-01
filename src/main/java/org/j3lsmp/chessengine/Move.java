package org.j3lsmp.chessengine;

/**
 * A single move, defined by its origin, destination, and piece type
 * 
 * @param from a bitboard with one bit true representing the move origin
 * @param to   a bitboard with one bit true representing the move destination
 * @param piece the type of piece being moved. Negative values are black,
 *             positive are white.
 * @see ChessBoard#boardAsArray() for more detailed piece typing
 * 
 * @author Jaden
 * @since 0.0.1
 */
record Move(long from, long to, byte piece) {}