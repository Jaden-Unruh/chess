package org.j3lsmp.chessengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest Controller for api - handles front-end interactions with {@link ChessEngineApplication#board}
 * 
 * @author Jaden
 * @since 0.0.1
 */
@RestController
@RequestMapping("/api")
public class APIController {
	
	/**
	 * Accepts get requests to `/getMoves/{square}` where square is an integer between 0 and 63. Returns a set of all legal moves that can be made with the piece on that square
	 * @param square an integer between 0 and 63
	 * @return the moves that can be made
	 */
	@GetMapping("/getMoves/{square}")
	public ResponseEntity<HashSet<Integer>> getMoves(@PathVariable String square) {
		try {
			HashSet<Move> moves = ChessEngineApplication.board.generateMoves();
			HashSet<Integer> ret = new HashSet<>();
			long bitboard = ChessUtilities.squareToBitboard(Integer.parseInt(square));
			for (Move move : moves)
				if (move.from() == bitboard)
					ret.add(ChessUtilities.bitboardtoSquare(move.to()));
			return ResponseEntity.ok(ret);
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}
	
	/**
	 * Accepts get requests to `/getBoard`. Returns the current board in array form (specifically a List of Lists of Bytes)
	 * @return the board as a list of byte lists
	 */
	@GetMapping("/getBoard")
	public ResponseEntity<List<List<Byte>>> getBoard() {
		try {
			byte[][] byteboard = ChessEngineApplication.board.boardAsArray();
			List<List<Byte>> response = new ArrayList<>();
			
			for (int i = 7; i >= 0; i--) {
				List<Byte> row = new ArrayList<>();
				for (int j = 0; j < 8; j++)
					row.add(byteboard[i][j]);
				response.add(row);
			}
			
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}
	
	/**
	 * Accepts post requests to `/makeMove` with a body detailing a move in the form of a {@link MoveRequest}
	 * @param moveRequest the move to perform
	 * @return ok if move was successfully performed
	 */
	@PostMapping("/makeMove")
	public ResponseEntity<String> makeMove(@RequestBody MoveRequest moveRequest) {
		try {
			ChessEngineApplication.board.performMove(new Move(ChessUtilities.squareToBitboard(moveRequest.from()), ChessUtilities.squareToBitboard(moveRequest.to()), (byte) moveRequest.piece()));
			return ResponseEntity.ok("Move executed");
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Something went wrong");
		}
	}
	
	/**
	 * Accepts get requests to `/isComputerTurn`
	 * @return true if it's the computer's turn
	 */
	@GetMapping("/isComputerTurn")
	public ResponseEntity<Boolean> isComputerTurn() {
		try {
			return ResponseEntity.ok(!ChessEngineApplication.board.isWhiteTurn);
		} catch(Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}
	
	/**
	 * Accepts post requests to `/computerMove` with a body detailing how long to give the computer
	 * @param computerTime the time for the computer in milliseconds
	 * @return data about the computer's move search
	 */
	@PostMapping("/computerMove")
	public ResponseEntity<HashMap<String, Integer>> computerMove(@RequestBody Integer computerTime) {
		try {
			ChessEngineApplication.board.performMove(MoveSearcher.findBestMove(ChessEngineApplication.board, (long) computerTime));
			HashMap<String, Integer> ret = new HashMap<>();
			ret.put("boardsEvaluated", MoveSearcher.boardsEvaluated);
			ret.put("futuresConsidered", MoveSearcher.futuresConsidered);
			ret.put("movesAhead", MoveSearcher.movesAhead);
			return ResponseEntity.ok(ret);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
		}
	}
}