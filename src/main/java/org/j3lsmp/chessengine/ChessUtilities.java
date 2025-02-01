package org.j3lsmp.chessengine;

/**
 * A few utility methods that didn't fit anywhere else
 * 
 * @author Jaden
 * @since 0.0.1
 */
public class ChessUtilities {
	
	/**
	 * Converts an int from 0-63 to the corresponding bitboard, with just the specified square flipped
	 * @param square the 'on' square
	 * @return the corresponding bitboard
	 */
	static long squareToBitboard(int square) {
		return 0x1l << square;
	}
	
	/**
	 * Converts a bitboard with one bit flipped to the corresponding integer from 0-63
	 * @param bitboard the bitboard representing one square on a chess board
	 * @return the value of the square
	 */
	static int bitboardtoSquare(long bitboard) {
		return Long.numberOfTrailingZeros(bitboard);
	}
}