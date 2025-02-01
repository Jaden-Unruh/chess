<script>
	import NavBar from './components/NavBar.svelte';
	import { onMount } from 'svelte';
	import { Range, Label } from 'flowbite-svelte';
	
	let stepValue = 10000;
	
	const pieces = {
		0: "♚",  // Black king
		1: "♛",  // Black queen
		2: "♜",  // Black rook
		3: "♝",  // Black bishop
		4: "♞",  // Black knight
		5: "♟",  // Black pawn
		6: "",    // Empty square
		7: "♙",   // White pawn
		8: "♘",   // White knight
		9: "♗",   // White bishop
		10: "♖",  // White rook
		11: "♕",  // White queen
		12: "♔"  // White king
	};
	
	let board = new Array(8).fill().map(() => new Array(8).fill(0));
	let selectedSquare = null;
	let legalMoves = new Set();
	let isComputerTurn = false;
	
	let computerDialog = "";
	
	async function fetchBoard() {
		try {
			const response = await fetch('/api/getBoard');
			if (response.ok) {
				board = await response.json();
				console.log(board);
			} else {
				console.error("Failed to fetch board state");
			}
			
			const iCTresponse = await fetch(`/api/isComputerTurn`);
			if (iCTresponse.ok) {
				isComputerTurn = await iCTresponse.json();
				console.log(isComputerTurn);
			} else {
				console.error("Failed to fetch game turn");
			}
		} catch (error) {
			console.error("Error fetching board state:", error);
		}
	}
	
	async function selectSquare(rank, file) {
		const square = (7 - rank) * 8 + file;
		if (selectedSquare != square && !isComputerTurn) {
			selectedSquare = square;
		
			try {
				const response = await fetch(`/api/getMoves/${square}`);
				if (response.ok)
					legalMoves = new Set(await response.json());
				else
					legalMoves.clear();
			} catch (error) {
				console.error("Error fetching legal moves:", error);
				legalMoves.clear();
			}
		} else
			resetSelection(); 
	}
	
	async function handleMove(rank, file, square) {
		const targetSquare = (7 - rank) * 8 + file;
		console.log("piece: " + square + "; from: " + selectedSquare + "; to: " + targetSquare);
		if (!legalMoves.has(targetSquare)) return;
		
		try {
			const response = await fetch (`/api/makeMove`, {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ from: selectedSquare, to: targetSquare, piece: board[7 - Math.floor(selectedSquare / 8)][selectedSquare % 8] })
			});
			
			if (response.ok) {
				await fetchBoard();
			} else {
				console.error("Move failed:", await response.text());
			}
		} catch (error) {
			console.error("Error making move:", error);
		}
		
		resetSelection();
	}
	
	function resetSelection() {
		selectedSquare = null;
		legalMoves.clear();
	}
	
	async function getComputerMove() {
		const computerTime = stepValue;
		console.log("Getting computer move for time " + computerTime);
		computerDialog = "Getting computer move for time " + computerTime;
		try {
			const response = await fetch (`/api/computerMove`, {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify(computerTime)
			});
			
			if (response.ok) {
				const data = await response.json();
				computerDialog = "Computer evaluated " + data["boardsEvaluated"] + " boards and considered " + data["futuresConsidered"] + " possible game states to a depth of at least " + data["movesAhead"] + " moves ahead.";
				console.log("bE: " + data["boardsEvaluated"] + "fC: " + data["futuresConsidered"] + "; mA: " + data["movesAhead"]); 
				await fetchBoard();
			} else {
				console.error("Computer failed:", await response.text());
			}
		} catch (error) {
			console.error("Computer errored: ", error);
		}
	}
	
	onMount(fetchBoard);
</script>

<style>
	table {
		border-collapse: collapse;
		margin: 20px auto;
	}
	
	td {
		width: 60px;
		height: 60px;
		text-align: center;
		vertical-align: middle;
		font-size: 2rem;
		font-family: 'Arial', sans-serif;
		cursor: pointer;
	}
	
	.light { background-color: #f0d9b5; }
	
	.dark { background-color: #b58863; }
	
	.selected { background-color: blue !important; }
	
	.highlight { background-color: red !important; }
</style>

<svelte:head>
	<title>Chess</title>
</svelte:head>

<NavBar />
<table>
	{#each board as row, rank}
		<tr>
			{#each row as square, file}
				<td class="{(rank + file) % 2 === 0 ? 'light' : 'dark'} {selectedSquare === (7 - rank) * 8 + file ? 'selected' : ''} {legalMoves.has((7 - rank) * 8 + file) ? 'highlight' : ''}"
					on:click={() => (legalMoves.has((7 - rank) * 8 + file) ? handleMove(rank, file) : selectSquare(rank, file))}>
					{pieces[square + 6]}
				</td>
			{/each}
		</tr>
	{/each}
</table>
<Label>Computer evaluation time limit (ms)</Label>
<Range id="range" min="1000" max="120000" bind:value={stepValue} step="100" />
<p>Value: {stepValue}</p>
<button on:click={fetchBoard}>Update Board</button>
<button on:click={getComputerMove}>Get Computer Move</button>
<p>{computerDialog}</p>