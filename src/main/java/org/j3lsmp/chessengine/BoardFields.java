package org.j3lsmp.chessengine;

/**
 * Various final fields used by {@link ChessBoard} that I decided were too long for the main class. Some are empty until initialized in {@link ChessBoard}
 * 
 * @author Jaden
 * @since 0.0.1
 */
interface BoardFields {
	/**
	 * Various bitboard masks
	 */
	long RANK2 = 0x000000000000FF00l, RANK7 = 0x00FF000000000000l, FILEA = 0x0101010101010101l,
			FILEAB = 0x0303030303030303l, FILEGH = 0xC0C0C0C0C0C0C0C0l, FILEH = 0x8080808080808080l;

	/**
	 * Bitboards of where a knight could move from each board square
	 */
	long[] KNIGHT_MOVES = new long[64];

	/**
	 * Bitboards of linear movement from every square in cardinal directions and
	 * diagonals
	 */
	long[] RAYS_NORTH = new long[64], RAYS_SOUTH = new long[64], RAYS_EAST = new long[64],
			RAYS_WEST = new long[64], RAYS_NORTHEAST = new long[64], RAYS_NORTHWEST = new long[64],
			RAYS_SOUTHEAST = new long[64], RAYS_SOUTHWEST = new long[64];

	/**
	 * Bitboards of where a king could move from each board square
	 */
	long[] KING_MOVES = new long[64];
	
	/**
	 * Values of each piece, used for board evaluation
	 */
	int[] PIECE_VALUES = {100, 320, 330, 500, 900, 20000};
	
	/**
	 * Table weighting pawn locational priority, used for board evaluation
	 * Note that this is kinda backwards, array index 0 is a1. Black uses inversed valuations.
	 * @see <a href="https://www.chessprogramming.org/Simplified_Evaluation_Function#Piece-Square_Tables">Reference</a>
	 */
	int[] PAWN_TABLE = {
			 0,  0,  0,  0,  0,  0,  0,  0,
			 0,  5,  5,  0,  0,  5,  5,  0,
			 5, 10, 10, 20, 20, 10, 10,  5,
			10, 10, 20, 25, 25, 20, 10, 10,
			 5, 15, 15, 30, 30, 15, 15,  5,
			 0, 10, 20, 20, 20, 20, 10,  0,
			 0, 10, 10,  0,  0, 10, 10,  0,
			 0,  0,  0,  0,  0,  0,  0,  0
	};
	
	/**
	 * Table weighting knight locational priority, used for board evaluation
	 * Note that this is kinda backwards, array index 0 is a1. Black uses inversed valuations.
	 * @see <a href="https://www.chessprogramming.org/Simplified_Evaluation_Function#Piece-Square_Tables">Reference</a>
	 */
	int[] KNIGHT_TABLE = {
			-50, -40, -30, -30, -30, -30, -40, -50,
			-40, -20,   0,   5,   5,   0, -20, -40,
			-30,   5,  10,  15,  15,  10,   5, -30,
			-30,   0,  15,  20,  20,  15,   0, -30,
			-30,   5,  15,  20,  20,  15,   5, -30,
			-30,   0,  10,  15,  15,  10,   0, -30,
			-40, -20,   0,   0,   0,   0, -20, -40,
			-50, -40, -30, -30, -30, -30, -40, -50
	};
	
	/**
	 * Table weighting bishop locational priority, used for board evaluation
	 * Note that this is kinda backwards, array index 0 is a1. Black uses inversed valuations.
	 * @see <a href="https://www.chessprogramming.org/Simplified_Evaluation_Function#Piece-Square_Tables">Reference</a>
	 */
	int[] BISHOP_TABLE = {
			-20, -10, -10, -10, -10, -10, -10, -20,
			-10,   5,   0,   0,   0,   0,   5, -10,
			-10,  10,  10,  10,  10,  10,  10, -10,
			-10,   0,  10,  10,  10,  10,   0, -10,
			-10,   5,   5,  10,  10,   5,   5, -10,
			-10,   0,   5,  10,  10,   5,   0, -10,
			-10,   0,   0,   0,   0,   0,   0, -10,
			-20, -10, -10, -10, -10, -10, -10, -20
	};
	
	/**
	 * Table weighting rook locational priority, used for board evaluation
	 * Note that this is kinda backwards, array index 0 is a1. Black uses inversed valuations.
	 * @see <a href="https://www.chessprogramming.org/Simplified_Evaluation_Function#Piece-Square_Tables">Reference</a>
	 */
	int[] ROOK_TABLE = {
		 0,  0,  0,  5,  5,  0,  0,  0,
		-5,  0,  0,  0,  0,  0,  0, -5,
		-5,  0,  0,  0,  0,  0,  0, -5,
		-5,  0,  0,  0,  0,  0,  0, -5,
		-5,  0,  0,  0,  0,  0,  0, -5,
		-5,  0,  0,  0,  0,  0,  0, -5,
		 5, 10, 10, 10, 10, 10, 10,  5,
		 0,  0,  0,  0,  0,  0,  0,  0
	};
	
	/**
	 * Table weighting queen locational priority, used for board evaluation
	 * Note that this is kinda backwards, array index 0 is a1. Black uses inversed valuations.
	 * @see <a href="https://www.chessprogramming.org/Simplified_Evaluation_Function#Piece-Square_Tables">Reference</a>
	 */
	int[] QUEEN_TABLE = {
		-20, -10, -10, -5, -5, -10, -10, -20,
		-10,   0,   0,  0,  0,   0,   0, -10,
		-10,   0,   5,  5,  5,   5,   0, -10,
		 -5,   0,   5,  5,  5,   5,   0,  -5,
		  0,   0,   5,  5,  5,   5,   0,  -5,
		-10,   0,   5,  5,  5,   5,   0, -10,
		-10,   0,   0,  0,  0,   0,   0, -10,
		-20, -10, -10, -5, -5, -10, -10, -20
	};
	

	/**
	 * Table weighting king locational priority until the endgame, used for board evaluation
	 * Note that this is kinda backwards, array index 0 is a1. Black uses inversed valuations.
	 * @see <a href="https://www.chessprogramming.org/Simplified_Evaluation_Function#Piece-Square_Tables">Reference</a>
	 */
	int[] KING_TABLE = {
		 20,  30,  10,   0,   0,  10,  30,  20,
		 20,  20,   0,   0,   0,   0,  20,  20,
		-10, -20, -20, -20, -20, -20, -20, -10,
		-20, -30, -30, -40, -40, -30, -30, -20,
		-30, -40, -40, -50, -50, -40, -40, -30,
		-30, -40, -40, -50, -50, -40, -40, -30,
		-30, -40, -40, -50, -50, -40, -40, -30,
		-30, -40, -40, -50, -50, -40, -40, -30
	};
	

	/**
	 * Table weighting king locational priority in the endgame, used for board evaluation
	 * Note that this is kinda backwards, array index 0 is a1. Black uses inversed valuations.
	 * @see <a href="https://www.chessprogramming.org/Simplified_Evaluation_Function#Piece-Square_Tables">Reference</a>
	 */
	int[] KING_TABLE_ENDGAME = {
		-50, -30, -30, -30, -30, -30, -30, -50,
		-30, -30,   0,   0,   0,   0, -30, -30,
		-30, -10,  20,  30,  30,  20, -10, -30,
		-30, -10,  30,  40,  40,  30, -10, -30,
		-30, -10,  30,  40,  40,  30, -10, -30,
		-30, -10,  20,  30,  30,  20, -10, -30,
		-30, -20, -10,   0,   0, -10, -20, -30,
		-50, -40, -30, -20, -20, -30, -40, -50
	};
	
	//Hashes for zobrist hashing method
	/**
	 * Hashes for various aspects of the board for zobrist hashing method
	 */
	long[][] PIECE_HASHES = new long[12][64];
	/**
	 * Hashes for various aspects of the board for zobrist hashing method
	 */
	long[] CASTLING_HASHES = new long[16], EN_PASSANT_HASHES = new long[8], TURN_HASH = new long[1];
}
