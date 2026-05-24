
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

package org.bamboomy.c44.bot.piecez;

import java.util.ArrayList;
import java.util.Stack;

import org.bamboomy.c44.bot.board.Board;
import org.bamboomy.c44.bot.board.Place;
import org.bamboomy.c44.bot.move.Move;
import org.bamboomy.c44.bot.player.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class Piece {

	@Getter
	@Setter
	protected int color, oldColor;

	@Getter
	protected Player player;

	@Getter
	private final String identifier;

	@Getter
	@Setter
	protected Place currentPlace;

	@Getter
	private final String md5;

	@Getter
	protected ArrayList<Move> movez = new ArrayList<>();

	@Getter
	protected boolean pinned = false;

	protected boolean inited = false;

	protected ArrayList<Piece> capturedPieces = new ArrayList<>();

	@Getter
	@Setter
	protected boolean removed = false;

	@Getter
	protected final PieceValue value;

	@Getter
	protected Stack<Boolean> moved = new Stack<>();

	private static final boolean DEBUG = false;

	protected Board board;

	private int attached = 0;

	@Getter
	@Setter
	private ArrayList<Move> preventingMoves;

	public Piece(Place place, int color, Player player, String identifier, PieceValue value, boolean moved, Board board,
			String md5, int oldColor) {

		this.currentPlace = place;

		this.color = color;
		this.oldColor = oldColor;

		this.player = player;

		this.identifier = identifier;

		this.value = value;

		this.moved.push(moved);

		this.board = board;

		this.md5 = md5;

		if (player != null) {

			player.getPiecez().add(this);
		}
	}

	public void calculateMovez() {

		movez = new ArrayList<>();
	}

	public synchronized boolean handleOtherPlaceNE(Place otherPlace) {

		if (otherPlace != null && otherPlace.getPiece() != null) {

			if (DEBUG) {

				log.debug("attack (" + otherPlace.getPiece().getColor() + "(" + otherPlace.getPiece().getIdentifier()
						+ "))(" + color + ")");
			}

			/*
			 * 
			 * TODO: allicance
			 * 
			 * if (alliance != null) {
			 * 
			 * if (otherPlace.getPiece().getColor() != color &&
			 * otherPlace.getPiece().getColor() !=
			 * alliance.getOtherColor(Color.getBySeq(color)).getSeq()) {
			 * 
			 * movez.add(new Move(currentPlace, otherPlace, this)); }
			 * 
			 * } else {
			 * 
			 */

			if (otherPlace.getPiece().getColor() != color) {

				movez.add(new Move(currentPlace, otherPlace, this, "m"));
			}
			// }

			/*
			 * if (!isPinnable()) { //player.kingAttack(otherPlace); }
			 * 
			 */

			return true;

		} else if (otherPlace != null) {

			movez.add(new Move(currentPlace, otherPlace, this, "m"));

			if (DEBUG) {

				log.debug("continue");
			}

			// player.kingAttack(otherPlace);
		}

		return false;
	}

	public void setMovez(JSONArray jsonArray) {

		if (!jsonArray.isEmpty()) {

			System.out.println(jsonArray.toList().size());

			int size = jsonArray.toList().size();

			JSONArray jsonMovez = jsonArray;

			for (int i = 0; i < size; i++) {

				JSONObject from = jsonMovez.getJSONObject(i).getJSONObject("from");
				JSONObject to = jsonMovez.getJSONObject(i).getJSONObject("to");

				Move move = new Move(currentPlace, board.getPlacez()[to.getInt("x")][to.getInt("y")], this, "m");

				move.getTo().setMd5(to.getString("md5"));

				movez.add(move);

				System.out.println("(" + from.getInt("x") + ", " + from.getInt("y") + ") -> (" + to.getInt("x") + ", "
						+ to.getInt("y") + ")");
			}
		}
	}

	public boolean moveTo(Place placeTo, boolean unused) {

		currentPlace.setPiece(null);

		currentPlace = placeTo;

		if (currentPlace.getPiece() != null) {

			if (DEBUG) {

				log.debug("taken: (" + currentPlace.getPiece().getIdentifier() + ") -> (" + getIdentifier() + ")");
			}

			currentPlace.getPiece().setRemoved(true);

			capturedPieces.add(currentPlace.getPiece());
		}

		currentPlace.setPiece(this);

		moved.push(true);

		return true;
	}

	public void rollBackMoved() {

		moved.pop();
	}

	public abstract void propagateValues();

	protected boolean attachMe(Place otherPlace, Piece piece) {

		if (otherPlace == null) {

			return true;
		}

		if (DEBUG) {

			attached++;

			System.out.println(attached);
		}

		otherPlace.attach(piece);

		if (otherPlace.getPiece() != null) {

			return true;
		}

		return false;
	}

	public abstract void calculateDefence(Place to);

	public abstract void calculateOffence();

	public abstract boolean attacks(Place to);

	public abstract boolean defends(Place to);

	public abstract void updateChecks(Player player);

}
