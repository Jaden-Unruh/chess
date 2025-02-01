package org.j3lsmp.chessengine;

/**
 * A more 'primitive' version of {@link Move}, using numbered squares from 0-63 instead of bitboards
 * 
 * @param from initial square, int 0-63
 * @param to destination square, int 0-63
 * @param piece piece type, int -6 to 6
 * 
 * @author Jaden
 * @since 0.0.1
 */
record MoveRequest(int from, int to, int piece) {}