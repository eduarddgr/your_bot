
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

import org.bamboomy.c44.bot.board.gui.GuiPlace;
import org.bamboomy.c44.bot.move.Move;
import org.bamboomy.c44.bot.piecez.Piece;
import org.bamboomy.c44.bot.player.Alliance;
import org.bamboomy.c44.bot.player.Color;
import org.bamboomy.c44.bot.player.Player;
import org.bamboomy.c44.rest.Poller;
import org.json.JSONArray;

import lombok.Getter;

public class Board {

	@Getter
	private Place[][] placez = new Place[12][12];

	@Getter
	private Player[] players = new Player[4];

	@Getter
	private final Alliance alliance;

	@Getter
	private final Color color;

	@Getter
	private final Poller poller;

	@Getter
	private Player ownPlayer;

	private static final boolean DEBUG = false;

	public Board(String json, Alliance alliance, String ownColor, Poller poller) {

		this.alliance = alliance;

		color = Color.getByName(ownColor);

		this.poller = poller;

		for (int i = 0; i < 4; i++) {

			players[i] = new Player(i, this);
		}

		ownPlayer = players[color.getSeq()];

		JSONArray columns = new JSONArray(json);

		for (int i = 0; i < columns.length(); i++) {

			JSONArray row = columns.getJSONArray(i);

			for (int j = 0; j < row.length(); j++) {

				if (!row.isNull(j)) {

					placez[i][j] = new Place(row.getJSONObject(j), this, i, j);
				}
			}
		}

		for (int i = 0; i < columns.length(); i++) {

			JSONArray row = columns.getJSONArray(i);

			for (int j = 0; j < row.length(); j++) {

				if (!row.isNull(j)) {

					placez[i][j].parsePiece(row.getJSONObject(j));
				}
			}
		}

		for (int i = 0; i < 4; i++) {

			if (players[i].getKing() == null && !players[i].isDead()) {

				throw new RuntimeException("I don't have a king (" + i + ")");
			}
		}
	}

	public Board(Board board) {

		alliance = board.alliance;
		color = board.color;
		poller = board.poller;

		for (int i = 0; i < 4; i++) {

			players[i] = new Player(i, this);
		}

		for (int i = 0; i < placez.length; i++) {

			System.out.print(i + ":");

			for (int j = 0; j < placez.length; j++) {

				System.out.print(j + "|");

				if (board.getPlacez()[i][j] != null) {

					placez[i][j] = new Place(board.getPlacez()[i][j], this, i, j, players);
				}
			}

			System.out.println();
		}

		for (int i = 0; i < 4; i++) {

			if (players[i].getKing() == null && !players[i].isDead()) {

				throw new RuntimeException("I don't have a king (" + i + ")");
			}
		}
	}

	public void execute(Move move) {

		System.out.println("Move executed :-D :-D :-D");

		Place from = placez[move.getFrom().getX()][move.getFrom().getY()];
		Place to = placez[move.getTo().getX()][move.getTo().getY()];
		Piece piece = players[color.getSeq()].getPieceByIdentifier(move.getPiece().getIdentifier());

		(new Move(from, to, piece, "m")).execute(false);
	}

	public void clear() {

		for (int i = 0; i < placez.length; i++) {

			for (int j = 0; j < placez.length; j++) {

				if (placez[i][j] != null) {

					placez[i][j].clear();
				}
			}
		}
	}

	public double value(Alliance alliance) {

		double result = 0;

		for (int i = 0; i < placez.length; i++) {

			for (int j = 0; j < placez.length; j++) {

				if (placez[i][j] != null) {

					result += placez[i][j].value(alliance);
				}
			}
		}

		if (DEBUG) {

			System.out.println(result);
		}

		result *= 1000;

		result = Math.round(result);

		result /= 1000;

		return result;
	}

	public void output() {

		for (int i = 0; i < placez.length; i++) {

			for (int j = 0; j < placez.length; j++) {

				if (placez[i][j] != null && placez[i][j].getAttached().size() > 0) {

					System.out.print("(" + i + "," + j + ") -> ");

					for (Piece attached : placez[i][j].getAttached()) {

						System.out.print(attached.getValue().getName() + "(" + attached.getColor() + "), ");
					}

					System.out.println();
				}
			}
		}
	}

	public GuiPlace[][] getGuiArray(String color, boolean currentPlayer) {

		GuiPlace[][] result = new GuiPlace[12][12];

		for (int i = 0; i < 12; i++) {
			for (int j = 0; j < 12; j++) {

				if (placez[i][j] != null) {

					result[i][j] = placez[i][j].toGuiPlace(color, currentPlayer);
				}
			}
		}

		return result;
	}

}
