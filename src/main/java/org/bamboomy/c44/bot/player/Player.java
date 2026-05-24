
/**

BSD 3-Clause License

Copyright (c) 2026, Sander Theetaert

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.


**/

package org.bamboomy.c44.bot.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.Semaphore;

import org.bamboomy.c44.bot.board.Board;
import org.bamboomy.c44.bot.board.Place;
import org.bamboomy.c44.bot.move.Check;
import org.bamboomy.c44.bot.move.Move;
import org.bamboomy.c44.bot.piecez.King;
import org.bamboomy.c44.bot.piecez.Pawn;
import org.bamboomy.c44.bot.piecez.Piece;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Player {

	private static final int CORES;

	static {

		CORES = Runtime.getRuntime().availableProcessors();
	}

	private static int NB_OF_THREADS = (int) (CORES * 1.5);

	private static int NB_OF_BOARDZ;

	private final Semaphore available = new Semaphore(NB_OF_THREADS);

	private ArrayList<Move> movez = new ArrayList<Move>();

	private Move bestMove;

	private double maxValue;

	// WARNING!!! -> as a king can only take 2 steps (with this value set to 7),
	// if you change this value
	// you should also adapt the check algorithm...
	// also (if you use a search tree) a depth of 7 is probably the deepest you want
	// to go for...
	// take into account that this includes 2 moves of a human (or other bot with a
	// different algorithm)
	// so their choice will differ from the one you calculate either way...
	private static final int MAX_DEPTH = 7;

	private int totalNodes = 0;

	private String[] movezStringz;

	private String output = "";

	private static final boolean DEBUG = true;

	@Getter
	private final Color color;

	@Getter
	private final Board board;

	@Getter
	protected final ArrayList<Piece> piecez = new ArrayList<Piece>();

	@Setter
	@Getter
	private King king;

	private int calculated = 0;

	@Getter
	private final Alliance alliance;

	@Getter
	private HashSet<Piece> preventPieces = new HashSet<Piece>();

	@Setter
	@Getter
	private boolean dead;

	public Player(int i, Board board) {

		System.out.println("I have: " + NB_OF_THREADS + " threads, jej :)");

		color = Color.getBySeq(i);

		this.board = board;

		if (board.getAlliance().isInAlliance(color)) {

			alliance = board.getAlliance();

		} else {

			alliance = board.getAlliance().getOtherAlliance();
		}
	}

	public void startCalculation() {

		calculated = 0;

		for (Piece piece : piecez) {

			if (piece.getMovez() != null) {

				movez.addAll(piece.getMovez());

				System.out.println(piece.getMovez().size() + " movez added...");
			}
		}

		NB_OF_BOARDZ = Math.min(NB_OF_THREADS, movez.size());

		(new Thread(new Calculator(movez, board))).start();
	}

	class Calculator implements Runnable {

		private ArrayList<Move> movez;

		private ArrayList<Integer> availableBoardIndexes = new ArrayList<Integer>();
		private ArrayList<Integer> busyBoardIndexes = new ArrayList<Integer>();

		private Board[] boardz = new Board[NB_OF_BOARDZ];

		private Board board;

		public Calculator(ArrayList<Move> movez, Board board) {
			this.movez = new ArrayList<>(movez);
			this.board = board;

			init();
		}

		public void init() {

			bestMove = null;
			maxValue = -Integer.MAX_VALUE;

			for (int i = 0; i < NB_OF_BOARDZ; i++) {

				boardz[i] = new Board(board);
			}
		}

		public synchronized void releaseIndex(int index) {

			if (DEBUG) {

				// log.debug("releasing:" + index);
			}

			for (int i = 0; i < busyBoardIndexes.size(); i++) {

				if (busyBoardIndexes.get(i) == index) {

					boardz[busyBoardIndexes.get(i)] = new Board(board);

					availableBoardIndexes.add(busyBoardIndexes.remove(i));

					return;
				}
			}

			throw new RuntimeException("couldn't find busy index");
		}

		@Override
		public void run() {

			ArrayList<Thread> threadz = new ArrayList<>();

			for (int i = 0; i < NB_OF_BOARDZ; i++) {

				availableBoardIndexes.add(i);
			}

			int numberOfMovez = 0;

			// ArrayList<Move> filteredMoves = filterMovez(movez, color.getSeq());

			movezStringz = new String[movez.size()];

			try {

				int counter = 0;

				while (!movez.isEmpty()) {

					log.debug("waiting for next calculator...");

					available.acquire();

					log.debug("going forth with:" + availableBoardIndexes.get(0));

					MoveCalculator calculator = new MoveCalculator(boardz[availableBoardIndexes.get(0)],
							movez.remove(0), board.getColor(), board.getAlliance(), availableBoardIndexes.get(0), this,
							counter++);

					busyBoardIndexes.add(availableBoardIndexes.remove(0));

					Thread thread = new Thread(calculator);
					threadz.add(thread);
					thread.start();

					numberOfMovez++;
				}

				for (Thread thread : threadz) {
					thread.join();
				}

				System.out.println("maxValue is: " + maxValue);

				board.getPoller().movePiece(bestMove.getPiece().getMd5(), bestMove.getTo().getMd5());

				// bestMove.execute(false);

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	class MoveCalculator implements Runnable {

		private static final int CHECK_MATE = 100_000;

		private Board board;
		private Color color;
		private Move move;
		private Alliance alliance;
		private int index;
		private Calculator calculator;

		private Move[] movesToDepth = new Move[MAX_DEPTH];

		private int nodes = 0;

		private int moveCounter;

		private int toBeMatedIndex = -1;

		public MoveCalculator(Board board, Move move, Color color, Alliance alliance, int index, Calculator calculator,
				int moveCounter) {
			this.board = board;
			this.color = color;
			this.alliance = alliance;
			this.move = move;
			this.index = index;
			this.calculator = calculator;
			this.moveCounter = moveCounter;
		}

		@Override
		public void run() {

			System.out.println(index);

			board.execute(move);

			boolean mateInOne = false;

			double evaluation = 0;

			Alliance otherAlliance = Player.this.alliance.getOtherAlliance();

			int[] toBeMatedIndices = new int[2];

			int indexCounter = 0;

			int start = color.getSeq() + 1;

			for (int i = 0; i < 3; i++) {

				if (otherAlliance.isInAlliance(Color.getBySeq((start + i) % 4))) {

					toBeMatedIndices[indexCounter++] = (start + i) % 4;
				}
			}

			for (int toBeMatedIndex : toBeMatedIndices) {

				if (board.getPlayers()[toBeMatedIndex].getKing() != null) {

					board.getPlayers()[toBeMatedIndex].calculateMovez();
					board.getPlayers()[toBeMatedIndex].calculateKingMovez();

					if (board.getPlayers()[toBeMatedIndex].getKing().isCheck()
							&& !board.getPlayers()[toBeMatedIndex].canPrevent(board.getPlayers()[toBeMatedIndex])) {

						evaluation = CHECK_MATE * 777;

						mateInOne = true;

						break;
					}
				}
			}

			if (!mateInOne) {

				evaluation = evaluate(1, -Integer.MAX_VALUE, Integer.MAX_VALUE, color.getSeq(),
						alliance.isInAlliance(board.getPlayers()[(color.getSeq() + 1) % 4].getColor()),
						alliance.isInAlliance(board.getPlayers()[(color.getSeq() + 2) % 4].getColor()));

				move.setMovesToDepth(movesToDepth);
			}

			recieveMove(mapMove(move), evaluation, moveCounter, nodes);

			board = null;// nullify board so it can be garbage collected...

			calculator.releaseIndex(index);

			available.release();
		}

		double evaluate(int depth, double alpha, double beta, int playerIndex, boolean isMax, boolean isNextMax) {

			double finalEvaluation;

			int currentPlayerIndex = (playerIndex + 1) % 4;

			while (board.getPlayers()[currentPlayerIndex].isDead()) {

				currentPlayerIndex = (currentPlayerIndex + 1) % 4;
			}

			ArrayList<Move> moves = new ArrayList<Move>();

			board.getPlayers()[currentPlayerIndex].getKing().clearChecks();
			board.getPlayers()[currentPlayerIndex].calculateChecks(board.getPlayers()[currentPlayerIndex].alliance);

			if (board.getPlayers()[currentPlayerIndex].getKing().isCheck() && depth < MAX_DEPTH) {

				if (board.getPlayers()[currentPlayerIndex].canPrevent(board.getPlayers()[currentPlayerIndex])) {

					Piece[] preventPieces;

					preventPieces = board.getPlayers()[currentPlayerIndex].getPreventPieces().toArray(new Piece[0]);

					for (Piece piece : preventPieces) {

						moves.addAll(piece.getPreventingMoves());
					}

					return calculateFinalEvaluation(moves, depth, alpha, beta, playerIndex, isMax, isNextMax,
							currentPlayerIndex);

				} else {

					// I am dead :(

					// handleDeath();

					if (board.getPlayers()[currentPlayerIndex].color == this.color) {

						return -100_000;

					} else if (board.getPlayers()[currentPlayerIndex].alliance.isInAlliance(this.color)) {

						return -50_000;

					} else {

						toBeMatedIndex = currentPlayerIndex;

						return CHECK_MATE;
					}
				}

			} else if (depth < MAX_DEPTH) {

				for (Player player : board.getPlayers()) {

					if (board.getPlayers()[currentPlayerIndex].alliance.isInAlliance(player.getColor())
							&& !player.isDead() && player.getKing().isCheck()
							&& board.getPlayers()[currentPlayerIndex].canPrevent(player)) {

						Piece[] preventPieces;

						preventPieces = board.getPlayers()[currentPlayerIndex].getPreventPieces().toArray(new Piece[0]);

						for (Piece piece : preventPieces) {

							moves.addAll(piece.getPreventingMoves());
						}

						return calculateFinalEvaluation(moves, depth, alpha, beta, playerIndex, isMax, isNextMax,
								currentPlayerIndex);
					}
				}
			}

			if (depth >= MAX_DEPTH) {

				return calculateBoardValue(color, board.getPlayers()[currentPlayerIndex].alliance);

			} else {

				// hier...
				board.getPlayers()[currentPlayerIndex].calculateMovez();
				board.getPlayers()[currentPlayerIndex].calculateKingMovez();

				for (Piece piece : board.getPlayers()[currentPlayerIndex].piecez) {

					moves.addAll(piece.getMovez());
				}

				// ArrayList<Move> filteredMoves = filterMovez(moves, currentPlayerIndex);

				finalEvaluation = calculateFinalEvaluation(moves, depth, alpha, beta, playerIndex, isMax, isNextMax,
						currentPlayerIndex);
			}

			return finalEvaluation;
		}

		private double calculateFinalEvaluation(ArrayList<Move> filteredMoves, int depth, double alpha, double beta,
				int playerIndex, boolean isMax, boolean isNextMax, int currentPlayerIndex) {

			double finalEvaluation;

			if (isMax) {

				finalEvaluation = -Integer.MAX_VALUE;

			} else {

				finalEvaluation = Integer.MAX_VALUE;
			}

			Collections.shuffle(filteredMoves);

			for (Move move : filteredMoves) {

				move.execute(false);

				double evaluation;

				try {

					evaluation = evaluate(depth + 1, alpha, beta, currentPlayerIndex,
							alliance.isInAlliance(board.getPlayers()[(currentPlayerIndex + 1) % 4].getColor()),
							alliance.isInAlliance(board.getPlayers()[(currentPlayerIndex + 2) % 4].getColor()));

				} finally {

					move.rollback();
				}

				if (DEBUG && evaluation < 742 && evaluation > 741) {

					System.out.println("-172");

					board.getPoller().sendBoard(board);
				}

				movesToDepth[depth] = move;

				if (evaluation == CHECK_MATE) {

					return CHECK_MATE;
				}

				if (isMax) {

					finalEvaluation = Math.max(finalEvaluation, evaluation);

					if (finalEvaluation >= beta) {

						break;
					}

					alpha = Math.max(finalEvaluation, alpha);

				} else {

					finalEvaluation = Math.min(finalEvaluation, evaluation);

					if (finalEvaluation <= alpha) {

						break;
					}

					beta = Math.min(finalEvaluation, beta);
				}
			}

			return finalEvaluation;
		}

		public double calculateBoardValue(Color color, Alliance alliance) {

			nodes++;

			board.clear();

			for (Player player : board.getPlayers()) {

				player.propagateValues();
			}

			return board.value(alliance);
		}

	}

	public void calculateMovez() {

		for (Piece piece : piecez) {

			if (!(piece instanceof King) && !piece.isRemoved()) {

				piece.calculateMovez();
			}
		}
	}

	public void calculateChecks(Alliance alliance) {

		ArrayList<Player> others = new ArrayList<Player>();

		for (Player player : board.getPlayers()) {

			if (!alliance.isInAlliance(player.getColor())) {

				others.add(player);
			}
		}

		updateChecks(others.toArray(new Player[0]));
	}

	public boolean canPrevent(Player otherPlayer) {

		ArrayList<Move> moves = new ArrayList<Move>();

		preventPieces = new HashSet<Piece>();

		calculateMovez();
		calculateKingMovez();

		for (Piece piece : piecez) {

			piece.setPreventingMoves(new ArrayList<Move>());

			moves.addAll(piece.getMovez());
		}

		for (Move move : moves) {

			move.execute(false);

			otherPlayer.getKing().clearChecks();
			otherPlayer.calculateChecks(otherPlayer.alliance);

			if (!otherPlayer.getKing().isCheck()) {

				move.getPiece().getPreventingMoves().add(move);

				preventPieces.add(move.getPiece());
			}

			move.rollback();
		}

		return preventPieces.size() != 0;
	}

	public void calculateKingMovez() {

		king.prepareMovez();

		ArrayList<Player> others = new ArrayList<>();

		for (Player player : board.getPlayers()) {

			if (!alliance.isInAlliance(player.getColor())) {

				others.add(player);
			}
		}

		ArrayList<Move> movez = new ArrayList<>();

		for (Place possiblePlace : king.getPossibleMoves()) {

			Move move = new Move(king.getCurrentPlace(), possiblePlace, king, "m");

			move.execute(false);

			king.clearChecks();

			for (Player player : others) {

				for (Piece piece : player.getPiecez()) {

					piece.updateChecks(this);
				}
			}

			if (!king.isCheck()) {

				movez.add(move);
			}

			move.rollback();
		}

		king.setMovez(movez);
	}

	private synchronized void recieveMove(Move move, double value, int moveCounter, int nodes) {

		calculated++;

		totalNodes += nodes;

		if (value > maxValue) {

			bestMove = move;

			maxValue = value;
		}

		movezStringz[moveCounter] = "(" + value + "(" + nodes + ")" + move.getTo().getMd5().substring(0, 4) + ")";

		output = "total: " + totalNodes + "\n\n";

		for (int i = 0; i < movezStringz.length; i++) {

			output += movezStringz[i] + " ";

			for (int j = 0; j < 3; j++) {

				i++;

				if (i >= movezStringz.length) {

					break;
				}

				output += movezStringz[i] + " ";
			}

			output += "\n";
		}

		output += "\n";

		output += calculated + "/" + movez.size() + " moves calculated...\n\n";

		System.out.println(output);

		board.getPoller().output(output);
	}

	public void propagateValues() {

		for (Piece piece : piecez) {

			if (!piece.isRemoved()) {

				piece.propagateValues();
			}
		}
	}

	public boolean defends(Place to) {

		for (Piece piece : piecez) {

			if (piece.defends(to)) {

				return true;
			}
		}

		return false;
	}

	public boolean attacks(Place to) {

		for (Piece piece : piecez) {

			if (piece.attacks(to)) {

				return true;
			}
		}

		return false;
	}

	public void calculateOffence() {

		for (Piece piece : piecez) {

			piece.calculateOffence();
		}
	}

	public void calculateDefence(Place to) {

		for (Piece piece : piecez) {

			piece.calculateDefence(to);
		}
	}

	public Piece getPieceByIdentifier(String identifier) {

		for (Piece piece : piecez) {

			if (piece.getIdentifier().equalsIgnoreCase(identifier)) {

				return piece;
			}
		}

		return null;
	}

	public void updateChecks(Player[] others) {

		king.clearChecks();

		for (Player player : others) {

			for (Piece piece : player.getPiecez()) {

				if (!piece.isRemoved()) {

					piece.updateChecks(this);
				}
			}
		}
	}

	public void setCheck(Piece piece) {

		king.addCheck(new Check(piece));
	}

	private ArrayList<Move> filterMovez(ArrayList<Move> moves, int currentPlayerIndex) {

		ArrayList<Move> result = new ArrayList<Move>();

		ArrayList<Player> own = new ArrayList<>(), others = new ArrayList<>();

		for (Player player : board.getPlayers()) {

			if (board.getPlayers()[currentPlayerIndex].alliance.isInAlliance(player.getColor())) {

				player.calculateDefence(null);

				own.add(player);

			} else {

				player.calculateOffence();

				others.add(player);
			}
		}

		for (Move move : moves) {

			if (move.getPiece() instanceof Pawn) {

				result.add(move);

			} else {

				boolean attacked = false;

				for (Player player : others) {

					if (player.attacks(move.getTo())) {

						move.setAttacked(true);

						attacked = true;

						break;
					}
				}

				if (!attacked) {

					result.add(move);

				} else {

					board.getPlayers()[currentPlayerIndex].calculateDefence(move.getTo());

					for (Player player : own) {

						if (player.defends(move.getTo())) {

							result.add(move);

							move.setDefended(true);

							break;
						}
					}
				}
			}
		}

		return result;
	}

	private synchronized Move mapMove(Move move) {

		for (Move gottenMove : movez) {

			if (gottenMove.getPiece().getMd5().equalsIgnoreCase(move.getPiece().getMd5())
					&& gottenMove.getTo().getMd5().equalsIgnoreCase(move.getTo().getMd5())) {

				return gottenMove;
			}

			if (gottenMove.getPiece().getMd5().equalsIgnoreCase(move.getPiece().getMd5())
					&& gottenMove.getTo().getX() == move.getTo().getX()
					&& gottenMove.getTo().getY() == move.getTo().getY()) {

				return gottenMove;
			}
		}

		throw new RuntimeException("didn't find move...");
	}

}
