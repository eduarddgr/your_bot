
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

package org.bamboomy.c44.bot.board;

import java.util.ArrayList;

import org.bamboomy.c44.bot.board.gui.GuiPlace;
import org.bamboomy.c44.bot.piecez.Bisshop;
import org.bamboomy.c44.bot.piecez.Horse;
import org.bamboomy.c44.bot.piecez.King;
import org.bamboomy.c44.bot.piecez.Pawn;
import org.bamboomy.c44.bot.piecez.Piece;
import org.bamboomy.c44.bot.piecez.Queen;
import org.bamboomy.c44.bot.piecez.Tower;
import org.bamboomy.c44.bot.player.Alliance;
import org.bamboomy.c44.bot.player.Color;
import org.bamboomy.c44.bot.player.Player;
import org.json.JSONObject;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class Place {

	private Board board;

	@Setter
	private Piece piece;

	private int x, y;

	@Setter
	private String md5;

	private ArrayList<Piece> attached = new ArrayList<>();

	private static final boolean DEBUG = false;

	public Place(JSONObject jsonObject, Board board, int x, int y) {

		this.board = board;

		this.x = x;
		this.y = y;
	}

	public Place(Place place, Board board, int i, int j, Player[] players) {

		this.board = board;

		this.x = i;
		this.y = j;

		if (place.piece != null) {

			System.out.print("p");

			piece = copyPiece(place.piece, players);
		}
	}

	private void parseColor(JSONObject jsonObject) {

		log.debug(jsonObject.getInt("color") + "");
		
		int oldColor = jsonObject.getInt("color");

		if (!jsonObject.isNull("oldColor")) {

			oldColor = jsonObject.getInt("oldColor");
		}

		switch (jsonObject.getInt("color")) {

		case 0:

			parsePiece(jsonObject, Color.GREEN, oldColor);

			break;

		case 1:

			parsePiece(jsonObject, Color.BLUE, oldColor);

			break;

		case 2:

			parsePiece(jsonObject, Color.RED, oldColor);

			break;

		case 3:

			parsePiece(jsonObject, Color.YELLOW, oldColor);

			break;

		case 4:

			parsePiece(jsonObject, Color.DEAD, oldColor);

			break;

		default:
			throw new IllegalArgumentException("Unexpected value: identifier");

		}

	}

	private void parsePiece(JSONObject jsonObject, Color color, int oldColor) {

		Player player = null;

		if (color.getSeq() != 4) {

			player = board.getPlayers()[color.getSeq()];

		} else {

			board.getPlayers()[oldColor].setDead(true);
		}

		if (jsonObject.getString("identifier").startsWith("p")) {

			Pawn pawn = new Pawn(this, color.getSeq(), color.getXDirection(), color.getYDirection(), player,
					jsonObject.getString("identifier"), jsonObject.getBoolean("moved"), board,
					jsonObject.getString("md5"), oldColor);

			pawn.setMovez(jsonObject.getJSONArray("movez"));

			piece = pawn;

			log.debug("adding pawn: " + jsonObject.getString("identifier") + " for color: " + color.getName());
		}

		if (jsonObject.getString("identifier").startsWith("b")) {

			Bisshop bisshop = new Bisshop(this, color.getSeq(), player, jsonObject.getString("identifier"),
					jsonObject.getBoolean("moved"), board, jsonObject.getString("md5"), oldColor);

			bisshop.setMovez(jsonObject.getJSONArray("movez"));

			piece = bisshop;

			log.debug("adding bisshop: " + jsonObject.getString("identifier") + " for color: " + color.getName());
		}

		if (jsonObject.getString("identifier").startsWith("t")) {

			Tower tower = new Tower(this, color.getSeq(), player, jsonObject.getString("identifier"),
					jsonObject.getBoolean("moved"), board, jsonObject.getString("md5"), oldColor);

			tower.setMovez(jsonObject.getJSONArray("movez"));

			piece = tower;

			log.debug("adding tower: " + jsonObject.getString("identifier") + " for color: " + color.getName());
		}

		if (jsonObject.getString("identifier").startsWith("h")) {

			Horse horse = new Horse(this, color.getSeq(), player, jsonObject.getString("identifier"),
					jsonObject.getBoolean("moved"), board, jsonObject.getString("md5"), oldColor);

			horse.setMovez(jsonObject.getJSONArray("movez"));

			piece = horse;

			log.debug("adding horse: " + jsonObject.getString("identifier") + " for color: " + color.getName());
		}

		if (jsonObject.getString("identifier").equalsIgnoreCase("q")) {

			Queen queen = new Queen(this, color.getSeq(), player, jsonObject.getString("identifier"),
					jsonObject.getBoolean("moved"), board, jsonObject.getString("md5"), oldColor);

			queen.setMovez(jsonObject.getJSONArray("movez"));

			piece = queen;

			log.debug("adding queen: " + jsonObject.getString("identifier") + " for color: " + color.getName());
		}

		if (jsonObject.getString("identifier").equalsIgnoreCase("k")) {

			King king = new King(this, color.getSeq(), color.getXDirection(), color.getYDirection(), player,
					jsonObject.getString("identifier"), jsonObject.getBoolean("moved"), board,
					jsonObject.getString("md5"), oldColor);

			king.setMovez(jsonObject.getJSONArray("movez"));

			player.setKing(king);

			piece = king;

			log.debug("adding king: " + jsonObject.getString("identifier") + " for color: " + color.getName());
		}
	}

	private Piece copyPiece(Piece otherPiece, Player[] players) {

		Player player = null;

		if (otherPiece.getColor() != 4) {

			player = board.getPlayers()[otherPiece.getColor()];

		} else {

			board.getPlayers()[otherPiece.getOldColor()].setDead(true);
		}

		if (otherPiece instanceof Pawn) {

			return new Pawn(this, otherPiece.getColor(), ((Pawn) otherPiece).getXDelta(),
					((Pawn) otherPiece).getYDelta(), player, otherPiece.getIdentifier(),
					otherPiece.getMoved().peek().booleanValue(), board, otherPiece.getMd5(), otherPiece.getOldColor());
		}

		if (otherPiece instanceof Bisshop) {

			return new Bisshop(this, otherPiece.getColor(), player, otherPiece.getIdentifier(),
					otherPiece.getMoved().peek().booleanValue(), board, otherPiece.getMd5(), otherPiece.getOldColor());
		}

		if (otherPiece instanceof Horse) {

			return new Horse(this, otherPiece.getColor(), player, otherPiece.getIdentifier(),
					otherPiece.getMoved().peek().booleanValue(), board, otherPiece.getMd5(), otherPiece.getOldColor());
		}

		if (otherPiece instanceof Tower) {

			return new Tower(this, otherPiece.getColor(), player, otherPiece.getIdentifier(),
					otherPiece.getMoved().peek().booleanValue(), board, otherPiece.getMd5(), otherPiece.getOldColor());
		}

		if (otherPiece instanceof Queen) {

			return new Queen(this, otherPiece.getColor(), player, otherPiece.getIdentifier(),
					otherPiece.getMoved().peek().booleanValue(), board, otherPiece.getMd5(), otherPiece.getOldColor());
		}

		if (otherPiece instanceof King) {

			King king = new King(this, otherPiece.getColor(), ((King) otherPiece).getXDelta(),
					((King) otherPiece).getYDelta(), player, otherPiece.getIdentifier(),
					otherPiece.getMoved().peek().booleanValue(), board, otherPiece.getMd5(), otherPiece.getOldColor());

			if (player != null) {

				player.setKing(king);
			}

			return king;
		}

		throw new RuntimeException("unknown piece :(");
	}

	public void parsePiece(JSONObject jsonObject) {

		if (!jsonObject.isNull("guiPiece")) {

			parseColor(jsonObject.getJSONObject("guiPiece"));
		}
	}

	public void attach(Piece attchingPiece) {

		attached.add(attchingPiece);
	}

	public void clear() {

		attached = new ArrayList<Piece>();
	}

	public double value(Alliance alliance) {

		double result = 0;

		if (DEBUG) {

			System.out.println(attached.size());
		}

		if (piece != null) {

			if (alliance.isInAlliance(Color.getBySeq(piece.getColor()))) {

				if (!piece.isRemoved()) {

					result += piece.getValue().getValue();
				}

			} else {

				if (!piece.isRemoved()) {

					result -= piece.getValue().getValue();
				}
			}
		}

		/*
		 * int intermediate = 0;
		 * 
		 * for (Piece attachedPiece : attached) {
		 * 
		 * if (alliance.isInAlliance(Color.getBySeq(attachedPiece.getColor()))) {
		 * 
		 * intermediate += 1;
		 * 
		 * } else {
		 * 
		 * intermediate -= 1; } }
		 * 
		 * result += (intermediate / 10.0);
		 */

		if (DEBUG) {

			System.out.println(result);
		}

		return result;
	}

	public GuiPlace toGuiPlace(String color, boolean currentPlayer) {

		return new GuiPlace(piece, color, currentPlayer, x, y);
	}

}
