package org.j3lsmp.chessengine;

import java.util.HashSet;
import java.util.Random;

/**
 * Abstract representation of a chess board, using bitboards to store piece
 * locations.
 * 
 * @author Jaden
 * @since 0.0.1
 */
public class ChessBoard implements BoardFields {

	/**
	 * Whether white is up to play, in this board state
	 */
	boolean isWhiteTurn;

	/**
	 * Bitboards for each piece type. Bits 0-63 represent each square of the board,
	 * 0=a1, 7=h1, 56=a8, 63=h8
	 */
	long whitePawns, blackPawns, whiteKnights, blackKnights, whiteBishops, blackBishops, whiteRooks, blackRooks,
			whiteQueens, blackQueens, whiteKing, blackKing, whitePieces, blackPieces;

	/**
	 * 4-bit integer detailing who is allowed to castle and how 1st bit is white
	 * kingside 2nd bit is white queenside 3rd bit is black kingside 4th bit is
	 * black queenside
	 */
	int castlingRights; // TODO implement castling

	/**
	 * where, if anywhere, the current player can capture en passant. 0-7 to specify
	 * file, otherwise -1
	 */
	int enPassantFile; // TODO implement en passant

	/**
	 * Initialize various fields from {@link BoardFields}
	 */
	static {
		for (int i = 0; i < 64; i++) {
			long pos = 1L << i;
			KNIGHT_MOVES[i] = computeKnightMoves(pos);
			RAYS_NORTH[i] = computeRayNorth(i);
			RAYS_EAST[i] = computeRayEast(i);
			RAYS_SOUTH[i] = computeRaySouth(i);
			RAYS_WEST[i] = computeRayWest(i);
			RAYS_NORTHEAST[i] = computeRayNorthEast(i);
			RAYS_NORTHWEST[i] = computeRayNorthWest(i);
			RAYS_SOUTHEAST[i] = computeRaySouthEast(i);
			RAYS_SOUTHWEST[i] = computeRaySouthWest(i);
			KING_MOVES[i] = computeKingMoves(pos);
		}

		Random random = new Random(199923);
		for (int piece = 0; piece < 12; piece++)
			for (int square = 0; square < 64; square++)
				PIECE_HASHES[piece][square] = random.nextLong();
		for (int i = 0; i < 16; i++)
			CASTLING_HASHES[i] = random.nextLong();
		for (int i = 0; i < 8; i++)
			EN_PASSANT_HASHES[i] = random.nextLong();
		TURN_HASH[0] = random.nextLong();
	}

	/**
	 * Creates a bitboard mask of where a knight could move from the given location
	 * 
	 * @param pos the location, a bitboard with 1 bit true
	 * @return a bitboard of all legal knight moves from pos
	 */
	private static long computeKnightMoves(long pos) {
		long moves = 0l;
		moves |= (pos & ~FILEA) << 15; // U2L1
		moves |= (pos & ~FILEH) << 17; // U2R1
		moves |= (pos & ~FILEA) >>> 17; // D2L1
		moves |= (pos & ~FILEH) >>> 15; // D2R1
		moves |= (pos & ~FILEAB) << 6; // U1L2
		moves |= (pos & ~FILEGH) << 10; // U1R2
		moves |= (pos & ~FILEAB) >>> 10;// D1L2
		moves |= (pos & ~FILEGH) >>> 6; // D1R2
		return moves;
	}

	/**
	 * Creates a bitboard mask of a ray going north from the given location
	 * 
	 * @param square the location, an integer 0-63
	 * @return a bitboard of a ray going north from the given location
	 */
	private static long computeRayNorth(int square) {
		long ray = 0l;
		for (int i = square + 8; i < 64; i += 8)
			ray |= 1l << i;
		return ray;
	}

	/**
	 * Creates a bitboard mask of a ray going south from the given location
	 * 
	 * @param square the location, an integer 0-63
	 * @return a bitboard of a ray going south from the given location
	 */
	private static long computeRaySouth(int square) {
		long ray = 0l;
		for (int i = square - 8; i >= 0; i -= 8)
			ray |= 1l << i;
		return ray;
	}

	/**
	 * Creates a bitboard mask of a ray going east from the given location
	 * 
	 * @param square the location, an integer 0-63
	 * @return a bitboard of a ray going east from the given location
	 */
	private static long computeRayEast(int square) {
		long ray = 0l;
		long current = 1l << square;

		while ((current & FILEH) == 0) {
			current <<= 1;
			ray |= current;
		}

		return ray;
	}

	/**
	 * Creates a bitboard mask of a ray going west from the given location
	 * 
	 * @param square the location, an integer 0-63
	 * @return a bitboard of a ray going west from the given location
	 */
	private static long computeRayWest(int square) {
		long ray = 0l;
		long current = 1l << square;

		while ((current & FILEA) == 0) {
			current >>>= 1;
			ray |= current;
		}

		return ray;
	}

	/**
	 * Creates a bitboard mask of a ray going northeast from the given location
	 * 
	 * @param square the location, an integer 0-63
	 * @return a bitboard of a ray going northeast from the given location
	 */
	private static long computeRayNorthEast(int square) {
		long ray = 0l;
		long current = 1l << square;

		while ((current & FILEH) == 0 && current != 0) {
			current <<= 9;
			if (current != 0)
				ray |= current;
		}

		return ray;
	}

	/**
	 * Creates a bitboard mask of a ray going northwest from the given location
	 * 
	 * @param square the location, an integer 0-63
	 * @return a bitboard of a ray going northwest from the given location
	 */
	private static long computeRayNorthWest(int square) {
		long ray = 0l;
		long current = 1l << square;

		while ((current & FILEA) == 0 && current != 0) {
			current <<= 7;
			if (current != 0)
				ray |= current;
		}

		return ray;
	}

	/**
	 * Creates a bitboard mask of a ray going southeast from the given location
	 * 
	 * @param square the location, an integer 0-63
	 * @return a bitboard of a ray going southeast from the given location
	 */
	private static long computeRaySouthEast(int square) {
		long ray = 0l;
		long current = 1l << square;

		while ((current & FILEH) == 0 && current != 0) {
			current >>>= 7;
			if (current != 0)
				ray |= current;
		}

		return ray;
	}

	/**
	 * Creates a bitboard mask of a ray going southwest from the given location
	 * 
	 * @param square the location, an integer 0-63
	 * @return a bitboard of a ray going southwest from the given location
	 */
	private static long computeRaySouthWest(int square) {
		long ray = 0l;
		long current = 1l << square;

		while ((current & FILEA) == 0 && current != 0) {
			current >>>= 9;
			if (current != 0)
				ray |= current;
		}

		return ray;
	}

	/**
	 * Creates a bitboard mask of where a king could move from the given location
	 * 
	 * @param pos the location, a bitboard with 1 bit true
	 * @return a bitboard of all legal king moves from pos
	 */
	private static long computeKingMoves(long pos) {
		long moves = 0l;
		moves |= (pos & ~FILEA) >>> 1; // L
		moves |= (pos & ~FILEH) << 1; // R
		moves |= pos << 8; // U
		moves |= pos >>> 8; // D
		moves |= (pos & ~FILEA) >>> 9; // DL
		moves |= (pos & ~FILEH) >>> 7; // DR
		moves |= (pos & ~FILEA) << 7; // UL
		moves |= (pos & ~FILEH) << 9; // UR
		return moves;
	}

	/**
	 * Set the board to its default state. That is, how a game of chess begins
	 */
	void resetBoard() {
		isWhiteTurn = true;

		whitePawns = 0x000000000000FF00l;
		blackPawns = 0x00FF000000000000l;
		whiteKnights = 0x0000000000000042l;
		blackKnights = 0x4200000000000000l;
		whiteBishops = 0x0000000000000024l;
		blackBishops = 0x2400000000000000l;
		whiteRooks = 0x0000000000000081l;
		blackRooks = 0x8100000000000000l;
		whiteQueens = 0x0000000000000008l;
		blackQueens = 0x0800000000000000l;
		whiteKing = 0x0000000000000010l;
		blackKing = 0x1000000000000000l;
		whitePieces = 0x000000000000FFFFl;
		blackPieces = 0xFFFF000000000000l;

		castlingRights = 15; // binary 1111
		enPassantFile = -1; // no en passant
	}

	/**
	 * Returns a copy of the given board with the specified move performed
	 * 
	 * @param oldBoard the board to copy
	 * @param move     the move to perform on the copy
	 */
	ChessBoard(ChessBoard oldBoard, Move move) {
		this.whitePawns = oldBoard.whitePawns;
		this.blackPawns = oldBoard.blackPawns;
		this.whiteKnights = oldBoard.whiteKnights;
		this.blackKnights = oldBoard.blackKnights;
		this.whiteBishops = oldBoard.whiteBishops;
		this.blackBishops = oldBoard.blackBishops;
		this.whiteRooks = oldBoard.whiteRooks;
		this.blackRooks = oldBoard.blackRooks;
		this.whiteQueens = oldBoard.whiteQueens;
		this.blackQueens = oldBoard.blackQueens;
		this.whiteKing = oldBoard.whiteKing;
		this.blackKing = oldBoard.blackKing;
		this.whitePieces = oldBoard.whitePieces;
		this.blackPieces = oldBoard.blackPieces;

		this.castlingRights = oldBoard.castlingRights;
		this.enPassantFile = oldBoard.enPassantFile;
		this.isWhiteTurn = oldBoard.isWhiteTurn;

		this.performMove(move);
	}

	/**
	 * Returns a completely empty board, with all values default (NOT a
	 * start-of-game board, use {@link #resetBoard()})
	 */
	ChessBoard() {
	}

	/**
	 * Generates a HashSet of all moves that can be taken from the current board
	 * state
	 * 
	 * @return a HashSet of all legal moves
	 */
	public HashSet<Move> generateMoves() {
		HashSet<Move> moves = new HashSet<>();

		long opponentPieces = isWhiteTurn ? blackPieces : whitePieces;
		long emptySquares = ~(whitePieces | blackPieces);

		long pawns = isWhiteTurn ? whitePawns : blackPawns;
		generatePawnMoves(moves, pawns, emptySquares, opponentPieces, isWhiteTurn);

		long knights = isWhiteTurn ? whiteKnights : blackKnights;
		generateKnightMoves(moves, knights, emptySquares, opponentPieces, isWhiteTurn);

		long bishops = isWhiteTurn ? whiteBishops : blackBishops;
		generateSlidingMoves(moves, bishops, emptySquares, opponentPieces, false, true, isWhiteTurn, (byte) 3);

		long rooks = isWhiteTurn ? whiteRooks : blackRooks;
		generateSlidingMoves(moves, rooks, emptySquares, opponentPieces, true, false, isWhiteTurn, (byte) 4);

		long queens = isWhiteTurn ? whiteQueens : blackQueens;
		generateSlidingMoves(moves, queens, emptySquares, opponentPieces, true, true, isWhiteTurn, (byte) 5);

		long king = isWhiteTurn ? whiteKing : blackKing;
		generateKingMoves(moves, king, emptySquares, opponentPieces, isWhiteTurn);

		return moves;

		// TODO: disallow moves as to do with check
	}

	/**
	 * Computes all legal moves with pawns
	 * 
	 * @param moves          the set to add the moves to
	 * @param pawns          locations of all pawns of the active player
	 * @param emptySquares   empty squares on the board
	 * @param opponentPieces locations of opponents pieces
	 * @param isWhite        whether white is moving
	 */
	private static void generatePawnMoves(HashSet<Move> moves, long pawns, long emptySquares, long opponentPieces,
			boolean isWhite) {
		while (pawns != 0) {
			int sourceSquare = Long.numberOfTrailingZeros(pawns);
			long sourceBit = 1L << sourceSquare;
			byte value = (byte) (isWhite ? 1 : -1);

			// Move 1 square
			long destination = isWhite ? (sourceBit << 8) : (sourceBit >>> 8);
			if ((destination & emptySquares) != 0) {
				moves.add(new Move(sourceBit, destination, value));
			}

			// Move 2 squares if on rank 2/7
			if (isWhite && (sourceBit & RANK2) != 0) {
				long doubleMove = destination << 8;
				if ((doubleMove & emptySquares) != 0 && (destination & emptySquares) != 0)
					moves.add(new Move(sourceBit, doubleMove, value));
			} else if (!isWhite && (sourceBit & RANK7) != 0) {
				long doubleMove = destination >>> 8;
				if ((doubleMove & emptySquares) != 0 && (destination & emptySquares) != 0)
					moves.add(new Move(sourceBit, doubleMove, value));
			}

			// Capture diagonally
			long leftCapture = isWhite ? (sourceBit << 7) : (sourceBit >>> 9),
					rightCapture = isWhite ? (sourceBit << 9) : (sourceBit >>> 7);

			if ((leftCapture & opponentPieces) != 0 && (sourceBit & FILEA) == 0)
				moves.add(new Move(sourceBit, leftCapture, value));
			if ((rightCapture & opponentPieces) != 0 && (sourceBit & FILEH) == 0)
				moves.add(new Move(sourceBit, rightCapture, value));

			pawns &= ~sourceBit;
		}
	}

	/**
	 * Computes all legal moves with knights
	 * 
	 * @param moves          the set to add the moves to
	 * @param knights        locations of all knights of the active player
	 * @param emptySquares   empty squares on the board
	 * @param opponentPieces locations of opponents pieces
	 * @param isWhite        whether white is moving
	 */
	private static void generateKnightMoves(HashSet<Move> moves, long knights, long emptySquares, long opponentPieces,
			boolean isWhite) {
		while (knights != 0) {
			int sourceSquare = Long.numberOfTrailingZeros(knights);
			long sourceBit = 1L << sourceSquare;

			long possibleMoves = KNIGHT_MOVES[sourceSquare] & (emptySquares | opponentPieces);
			while (possibleMoves != 0) {
				int destinationSquare = Long.numberOfTrailingZeros(possibleMoves);
				long destinationBit = 1l << destinationSquare;
				moves.add(new Move(sourceBit, destinationBit, (byte) (isWhite ? 2 : -2)));

				possibleMoves &= ~destinationBit;
			}

			knights &= ~sourceBit;
		}
	}

	/**
	 * Computes all legal moves for sliding pieces - rooks, bishops, queens
	 * 
	 * @param moves          the set to add the moves to
	 * @param pieces         locations of the relevant pieces of the active player
	 * @param emptySquares   empty squares on the board
	 * @param opponentPieces locations of opponents pieces
	 * @param rookMoves      whether the piece can move like a rook (true for rooks,
	 *                       queens)
	 * @param bishopMoves    whether the piece can move like a bishop (true for
	 *                       bishops, queens)
	 * @param isWhite        whether white is moving
	 * @param value          the value of the piece, 3=bishop, 4=rook, 5=queen
	 */
	private static void generateSlidingMoves(HashSet<Move> moves, long pieces, long emptySquares, long opponentPieces,
			boolean rookMoves, boolean bishopMoves, boolean isWhite, byte value) {
		while (pieces != 0) {
			int sourceSquare = Long.numberOfTrailingZeros(pieces);
			long sourceBit = 1l << sourceSquare;
			byte signedPieceValue = (byte) (isWhite ? value : -value);

			if (rookMoves) {
				// NORTH
				long northMask = RAYS_NORTH[sourceSquare];
				long potentialNorthMoves = northMask & (emptySquares | opponentPieces);
				while (potentialNorthMoves != 0) {
					long targetBit = Long.lowestOneBit(potentialNorthMoves);
					long confirmBit = Long.lowestOneBit(northMask); // Make sure our own piece isn't in the way

					if (targetBit == confirmBit) // Don't take own piece
						moves.add(new Move(sourceBit, targetBit, signedPieceValue));
					else
						break;

					if ((opponentPieces & targetBit) != 0)
						break;

					northMask &= ~targetBit;
					potentialNorthMoves &= ~targetBit;
				}

				// SOUTH
				long southMask = RAYS_SOUTH[sourceSquare];
				long potentialSouthMoves = southMask & (emptySquares | opponentPieces);
				while (potentialSouthMoves != 0) {
					long targetBit = Long.highestOneBit(potentialSouthMoves);
					long confirmBit = Long.highestOneBit(southMask);

					if (targetBit == confirmBit)
						moves.add(new Move(sourceBit, targetBit, signedPieceValue));
					else
						break;

					if ((opponentPieces & targetBit) != 0)
						break;

					southMask &= ~targetBit;
					potentialSouthMoves &= ~targetBit;
				}

				// EAST
				long eastMask = RAYS_EAST[sourceSquare];
				long potentialEastMoves = eastMask & (emptySquares | opponentPieces);
				while (potentialEastMoves != 0) {
					long targetBit = Long.lowestOneBit(potentialEastMoves);
					long confirmBit = Long.lowestOneBit(eastMask);

					if (targetBit == confirmBit)
						moves.add(new Move(sourceBit, targetBit, signedPieceValue));
					else
						break;

					if ((opponentPieces & targetBit) != 0)
						break;

					eastMask &= ~targetBit;
					potentialEastMoves &= ~targetBit;
				}

				// WEST

				long westMask = RAYS_WEST[sourceSquare];
				long potentialWestMoves = westMask & (emptySquares | opponentPieces);
				while (potentialWestMoves != 0) {
					long targetBit = Long.highestOneBit(potentialWestMoves);
					long confirmBit = Long.highestOneBit(westMask);

					if (targetBit == confirmBit)
						moves.add(new Move(sourceBit, targetBit, signedPieceValue));
					else
						break;

					if ((opponentPieces & targetBit) != 0)
						break;

					westMask &= ~targetBit;
					potentialWestMoves &= ~targetBit;
				}
			}

			if (bishopMoves) {
				// NW
				long northWestMask = RAYS_NORTHWEST[sourceSquare];
				long potentialNorthWestMoves = northWestMask & (emptySquares | opponentPieces);
				while (potentialNorthWestMoves != 0) {
					long targetBit = Long.lowestOneBit(potentialNorthWestMoves);
					long confirmBit = Long.lowestOneBit(northWestMask);

					if (targetBit == confirmBit)
						moves.add(new Move(sourceBit, targetBit, signedPieceValue));
					else
						break;

					if ((opponentPieces & targetBit) != 0)
						break;

					northWestMask &= ~targetBit;
					potentialNorthWestMoves &= ~targetBit;
				}

				// NE
				long northEastMask = RAYS_NORTHEAST[sourceSquare];
				long potentialNorthEastMoves = northEastMask & (emptySquares | opponentPieces);
				while (potentialNorthEastMoves != 0) {
					long targetBit = Long.lowestOneBit(potentialNorthEastMoves);
					long confirmBit = Long.lowestOneBit(northEastMask);

					if (targetBit == confirmBit)
						moves.add(new Move(sourceBit, targetBit, signedPieceValue));
					else
						break;

					if ((opponentPieces & targetBit) != 0)
						break;

					northEastMask &= ~targetBit;
					potentialNorthEastMoves &= ~targetBit;
				}

				// SW
				long southWestMask = RAYS_SOUTHWEST[sourceSquare];
				long potentialSouthWestMoves = southWestMask & (emptySquares | opponentPieces);
				while (potentialSouthWestMoves != 0) {
					long targetBit = Long.highestOneBit(potentialSouthWestMoves);
					long confirmBit = Long.highestOneBit(southWestMask);

					if (targetBit == confirmBit)
						moves.add(new Move(sourceBit, targetBit, signedPieceValue));
					else
						break;

					if ((opponentPieces & targetBit) != 0)
						break;

					southWestMask &= ~targetBit;
					potentialSouthWestMoves &= ~targetBit;
				}

				// SE
				long southEastMask = RAYS_SOUTHEAST[sourceSquare];
				long potentialSouthEastMoves = southEastMask & (emptySquares | opponentPieces);
				while (potentialSouthEastMoves != 0) {
					long targetBit = Long.highestOneBit(potentialSouthEastMoves);
					long confirmBit = Long.highestOneBit(southEastMask);

					if (targetBit == confirmBit)
						moves.add(new Move(sourceBit, targetBit, signedPieceValue));
					else
						break;

					if ((opponentPieces & targetBit) != 0)
						break;

					southEastMask &= ~targetBit;
					potentialSouthEastMoves &= ~targetBit;
				}
			}

			pieces &= ~sourceBit;
		}
	}

	/**
	 * Computes all legal moves with king
	 * 
	 * @param moves          the set to add the moves to
	 * @param king           the location of the king of the active player
	 * @param emptySquares   empty squares on the board
	 * @param opponentPieces locations of opponents pieces
	 * @param isWhite        whether white is moving
	 */
	private static void generateKingMoves(HashSet<Move> moves, long king, long emptySquares, long opponentPieces,
			boolean isWhite) {
		int sourceSquare = Long.numberOfTrailingZeros(king);
		long sourceBit = 1l << sourceSquare; // This should just be king but for consistency and just in case, we
												// redefine it
		if (sourceSquare == 64) //TODO remove when determine win/check/mate is implemented, but for now we need it.
			return;
		
		long possibleMoves = KING_MOVES[sourceSquare] & (emptySquares | opponentPieces);
		while (possibleMoves != 0) {
			int destinationSquare = Long.numberOfTrailingZeros(possibleMoves);
			long destinationBit = 1l << destinationSquare;
			moves.add(new Move(sourceBit, destinationBit, isWhite ? (byte) 6 : (byte) -6));

			possibleMoves &= ~destinationBit;
		}
	}

	/**
	 * Debugging method, prints all found legal moves from this board state
	 */
	void printMoves() {
		HashSet<Move> moves = generateMoves();

		System.out.println(moves.size());
		for (Move move : moves) {
			System.out.println(Long.toHexString(move.from()) + " to " + Long.toHexString(move.to()));
		}
	}

	/**
	 * Returns a byte-array representation of the board state, used for interfacing
	 * and debugging.
	 * 
	 * The returned array has the following properties: array[0][0] represents a1
	 * array[0][7] represents h1 array[7][0] represents a8 array[7][7] represents h8
	 * positive values represent white pieces negative values represent black pieces
	 * 0 values represent empty squares pieces have the following magnitudes:
	 * 1=pawn, 2=knight, 3=bishop, 4=rook, 5=queen, 6=king
	 * 
	 * @return the board-state as a byte array
	 */
	byte[][] boardAsArray() {
		byte[][] ret = new byte[8][8];
		for (int square = 0; square < 64; square++) {
			int rank = square / 8;
			int file = square % 8;

			if ((whitePawns & (1L << square)) != 0)
				ret[rank][file] = 1;
			else if ((blackPawns & (1L << square)) != 0)
				ret[rank][file] = -1;
			else if ((whiteKnights & (1L << square)) != 0)
				ret[rank][file] = 2;
			else if ((blackKnights & (1L << square)) != 0)
				ret[rank][file] = -2;
			else if ((whiteBishops & (1L << square)) != 0)
				ret[rank][file] = 3;
			else if ((blackBishops & (1L << square)) != 0)
				ret[rank][file] = -3;
			else if ((whiteRooks & (1L << square)) != 0)
				ret[rank][file] = 4;
			else if ((blackRooks & (1L << square)) != 0)
				ret[rank][file] = -4;
			else if ((whiteQueens & (1L << square)) != 0)
				ret[rank][file] = 5;
			else if ((blackQueens & (1L << square)) != 0)
				ret[rank][file] = -5;
			else if ((whiteKing & (1L << square)) != 0)
				ret[rank][file] = 6;
			else if ((blackKing & (1L << square)) != 0)
				ret[rank][file] = -6;
			else
				ret[rank][file] = 0;
		}

		return ret;
	}

	/**
	 * Prints the current board state as an array. Used for debugging.
	 * 
	 * @see #boardAsArray()
	 */
	void printBoardAsArray() {
		byte[][] boardArray = boardAsArray();
		for (int rank = 7; rank >= 0; rank--) {
			for (int file = 0; file < 8; file++)
				System.out.printf("%2d ", boardArray[rank][file]);
			System.out.println();
		}
	}

	/**
	 * Performs the specified move on this board, updating all relevant bitboards
	 * 
	 * @param m the move to perform
	 */
	void performMove(Move m) {
		isWhiteTurn = !isWhiteTurn;
		switch (m.piece()) {
		case -6:
			blackKing &= ~m.from();
			blackKing |= m.to();
			break;
		case -5:
			blackQueens &= ~m.from();
			blackQueens |= m.to();
			break;
		case -4:
			blackRooks &= ~m.from();
			blackRooks |= m.to();
			break;
		case -3:
			blackBishops &= ~m.from();
			blackBishops |= m.to();
			break;
		case -2:
			blackKnights &= ~m.from();
			blackKnights |= m.to();
			break;
		case -1:
			blackPawns &= ~m.from();
			blackPawns |= m.to();
			break;
		case 1:
			whitePawns &= ~m.from();
			whitePawns |= m.to();
			break;
		case 2:
			whiteKnights &= ~m.from();
			whiteKnights |= m.to();
			break;
		case 3:
			whiteBishops &= ~m.from();
			whiteBishops |= m.to();
			break;
		case 4:
			whiteRooks &= ~m.from();
			whiteRooks |= m.to();
			break;
		case 5:
			whiteQueens &= ~m.from();
			whiteQueens |= m.to();
			break;
		case 6:
			whiteKing &= ~m.from();
			whiteKing |= m.to();
			break;
		}
		if (m.piece() > 0) {
			whitePieces &= ~m.from();
			whitePieces |= m.to();
			blackPieces &= ~m.to();
			blackPawns &= ~m.to();
			blackKnights &= ~m.to();
			blackBishops &= ~m.to();
			blackRooks &= ~m.to();
			blackQueens &= ~m.to();
			blackKing &= ~m.to();
		} else {
			blackPieces &= ~m.from();
			blackPieces |= m.to();
			whitePieces &= ~m.to();
			whitePawns &= ~m.to();
			whiteKnights &= ~m.to();
			whiteBishops &= ~m.to();
			whiteRooks &= ~m.to();
			whiteQueens &= ~m.to();
			whiteKing &= ~m.to();
		}
	}

	/**
	 * Calculate the value of the current board state. Higher values mean white is
	 * favored, lower values mean black is favored.
	 * 
	 * Uses a combination of the quantity of each type of piece, and the location of
	 * each piece.
	 * 
	 * @return An integer evaluating the current board state.
	 * @see BoardFields#BISHOP_TABLE
	 * @see BoardFields#KING_TABLE
	 * @see BoardFields#KING_TABLE_ENDGAME
	 * @see BoardFields#KNIGHT_TABLE
	 * @see BoardFields#PAWN_TABLE
	 * @see BoardFields#PIECE_VALUES
	 * @see BoardFields#QUEEN_TABLE
	 * @see BoardFields#ROOK_TABLE
	 */
	int calculateBoardValue() {
		MoveSearcher.boardsEvaluated++;
		int value = 0;
		value += Long.bitCount(whitePawns) * PIECE_VALUES[0];
		value -= Long.bitCount(blackPawns) * PIECE_VALUES[0];
		value += Long.bitCount(whiteKnights) * PIECE_VALUES[1];
		value -= Long.bitCount(blackKnights) * PIECE_VALUES[1];
		value += Long.bitCount(whiteBishops) * PIECE_VALUES[2];
		value -= Long.bitCount(blackBishops) * PIECE_VALUES[2];
		value += Long.bitCount(whiteRooks) * PIECE_VALUES[3];
		value -= Long.bitCount(blackRooks) * PIECE_VALUES[3];
		value += Long.bitCount(whiteQueens) * PIECE_VALUES[4];
		value -= Long.bitCount(blackQueens) * PIECE_VALUES[4];

		value += getValueForPieceType(whitePawns, PAWN_TABLE);
		value -= getValueForPieceType(blackPawns, PAWN_TABLE);
		value += getValueForPieceType(whiteKnights, KNIGHT_TABLE);
		value -= getValueForPieceType(blackKnights, KNIGHT_TABLE);
		value += getValueForPieceType(whiteBishops, BISHOP_TABLE);
		value -= getValueForPieceType(blackBishops, BISHOP_TABLE);
		value += getValueForPieceType(whiteRooks, ROOK_TABLE);
		value -= getValueForPieceType(blackRooks, ROOK_TABLE);
		value += getValueForPieceType(whiteQueens, QUEEN_TABLE);
		value -= getValueForPieceType(blackQueens, QUEEN_TABLE);
		value += getValueForPieceType(whiteKing, KING_TABLE);
		value -= getValueForPieceType(blackKing, KING_TABLE);
		// TODO implement endgame king location valuation

		return value;
	}

	/**
	 * Determine the locational value that the specified piece contributes to this board's valuation
	 * @param pieces the bitboard of the piecetype
	 * @param table the locational valuation table for this piece type
	 * @return the computed value
	 */
	int getValueForPieceType(long pieces, int[] table) {
		int ret = 0;
		while (pieces != 0) {
			long piece = Long.lowestOneBit(pieces);
			int square = Long.numberOfTrailingZeros(piece);
			ret += table[square];
			pieces &= ~piece;
		}
		return ret;
	}

	/**
	 * Compute a zobrist hash on the current board state
	 * @return the zobrist hash
	 */
	long computeZobristHash() {
		long hash = addBitBoardToHash(0l, whitePawns, 0);
		hash = addBitBoardToHash(hash, blackPawns, 1);
		hash = addBitBoardToHash(hash, whiteKnights, 2);
		hash = addBitBoardToHash(hash, blackKnights, 3);
		hash = addBitBoardToHash(hash, whiteBishops, 4);
		hash = addBitBoardToHash(hash, blackBishops, 5);
		hash = addBitBoardToHash(hash, whiteRooks, 6);
		hash = addBitBoardToHash(hash, blackRooks, 7);
		hash = addBitBoardToHash(hash, whiteQueens, 8);
		hash = addBitBoardToHash(hash, blackQueens, 9);
		hash = addBitBoardToHash(hash, whiteKing, 10);
		hash = addBitBoardToHash(hash, blackKing, 11);

		hash ^= CASTLING_HASHES[castlingRights];

		if (enPassantFile != -1)
			hash ^= EN_PASSANT_HASHES[enPassantFile];

		if (isWhiteTurn)
			hash ^= TURN_HASH[0];

		return hash;
	}

	/**
	 * Add the specified piece bitboard to the specified hash using xor of each piece by square
	 * @param currentHash the current hash
	 * @param bitboard the bitboard to add
	 * @param pieceIndex the index of the piecetype within {@link BoardFields#PIECE_HASHES}
	 * @return the updated hash
	 */
	long addBitBoardToHash(long currentHash, long bitboard, int pieceIndex) {
		while (bitboard != 0) {
			int square = Long.numberOfTrailingZeros(bitboard);
			currentHash ^= PIECE_HASHES[pieceIndex][square];
			bitboard &= ~(1l << square);
		}
		return currentHash;
	}
}