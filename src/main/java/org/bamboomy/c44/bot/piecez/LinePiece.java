
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

import org.bamboomy.c44.bot.board.Board;
import org.bamboomy.c44.bot.board.Place;
import org.bamboomy.c44.bot.move.CheckLine;
import org.bamboomy.c44.bot.move.Line;
import org.bamboomy.c44.bot.player.Player;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class LinePiece extends Piece {

	private static final boolean DEBUG = false;

	@Getter
	protected ArrayList<Line> linez = new ArrayList<>();

	@Getter
	protected ArrayList<CheckLine> checkLinez = new ArrayList<>();

	public LinePiece(Place place, int color, Player player, String identifier, PieceValue value, boolean moved,
			Board board, String md5, int oldColor) {

		super(place, color, player, identifier, value, moved, board, md5, oldColor);
	}

	protected void addBisshopMovez() {

		int counter = 1;

		for (int i = currentPlace.getX() + 1; i < 12; i++) {

			if (currentPlace.getY() - counter < 0) {

				break;
			}

			Place otherPlace = currentPlace.getBoard().getPlacez()[i][currentPlace.getY() - counter];

			if (handleOtherPlaceNE(otherPlace)) {

				break;
			}

			counter++;
		}

		counter = 1;

		for (int i = currentPlace.getY() + 1; i < 12; i++) {

			if (currentPlace.getX() + counter > 11) {

				break;
			}

			Place otherPlace = currentPlace.getBoard().getPlacez()[currentPlace.getX() + counter][i];

			if (handleOtherPlaceNE(otherPlace)) {

				break;
			}

			counter++;
		}

		counter = 1;

		for (int i = currentPlace.getX() - 1; i >= 0; i--) {

			if (currentPlace.getY() + counter > 11) {

				break;
			}

			Place otherPlace = currentPlace.getBoard().getPlacez()[i][currentPlace.getY() + counter];

			if (handleOtherPlaceNE(otherPlace)) {

				break;
			}

			counter++;
		}

		counter = 1;

		for (int i = currentPlace.getY() - 1; i >= 0; i--) {

			if (currentPlace.getX() - counter < 0) {

				break;
			}

			Place otherPlace = currentPlace.getBoard().getPlacez()[currentPlace.getX() - counter][i];

			if (handleOtherPlaceNE(otherPlace)) {

				break;
			}

			counter++;
		}
	}

	protected void addTowerMovez() {

		for (int i = currentPlace.getX() + 1; i < 12; i++) {

			Place otherPlace = currentPlace.getBoard().getPlacez()[i][currentPlace.getY()];

			if (DEBUG) {

				log.debug("i: " + i + ", y: " + currentPlace.getY() + " null?" + otherPlace);
			}

			if (handleOtherPlaceNE(otherPlace)) {

				if (DEBUG) {

					log.debug("i: " + i + ", y: " + currentPlace.getY() + " null?" + otherPlace + "(true)");
				}

				break;
			}
		}

		for (int i = currentPlace.getY() + 1; i < 12; i++) {

			Place otherPlace = currentPlace.getBoard().getPlacez()[currentPlace.getX()][i];

			if (DEBUG) {

				log.debug("x: " + currentPlace.getX() + " + i: " + i + " null?" + otherPlace);
			}

			if (handleOtherPlaceNE(otherPlace)) {

				if (DEBUG) {

					log.debug("x: " + currentPlace.getX() + " + i: " + i + " null?" + otherPlace + "(true)");
				}

				break;
			}
		}

		for (int i = currentPlace.getX() - 1; i >= 0; i--) {

			Place otherPlace = currentPlace.getBoard().getPlacez()[i][currentPlace.getY()];

			if (DEBUG) {

				log.debug("i: " + i + ", y: " + currentPlace.getY() + " null?" + otherPlace);
			}

			if (handleOtherPlaceNE(otherPlace)) {

				if (DEBUG) {

					log.debug("i: " + i + ", y: " + currentPlace.getY() + " null?" + otherPlace + "(true)");
				}

				break;
			}
		}

		for (int i = currentPlace.getY() - 1; i >= 0; i--) {

			Place otherPlace = currentPlace.getBoard().getPlacez()[currentPlace.getX()][i];

			if (DEBUG) {

				log.debug("x: " + currentPlace.getX() + " + i: " + i + " null?" + otherPlace);
			}

			if (handleOtherPlaceNE(otherPlace)) {

				if (DEBUG) {

					log.debug("x: " + currentPlace.getX() + " + i: " + i + " null?" + otherPlace + "(true)");
				}

				break;
			}
		}
	}

	@Override
	public abstract void propagateValues();

	protected void propagateValuesForBissop() {

		int counter = 1;

		for (int i = currentPlace.getX() + 1; i < 12; i++) {

			if (currentPlace.getY() - counter < 0) {

				break;
			}

			if (attachMe(currentPlace.getBoard().getPlacez()[i][currentPlace.getY() - counter], this)) {

				break;
			}

			counter++;
		}

		counter = 1;

		for (int i = currentPlace.getY() + 1; i < 12; i++) {

			if (currentPlace.getX() + counter > 11) {

				break;
			}

			if (attachMe(currentPlace.getBoard().getPlacez()[currentPlace.getX() + counter][i], this)) {

				break;
			}

			counter++;
		}

		counter = 1;

		for (int i = currentPlace.getX() - 1; i >= 0; i--) {

			if (currentPlace.getY() + counter > 11) {

				break;
			}

			if (attachMe(currentPlace.getBoard().getPlacez()[i][currentPlace.getY() + counter], this)) {

				break;
			}

			counter++;
		}

		counter = 1;

		for (int i = currentPlace.getY() - 1; i >= 0; i--) {

			if (currentPlace.getX() - counter < 0) {

				break;
			}

			if (attachMe(currentPlace.getBoard().getPlacez()[currentPlace.getX() - counter][i], this)) {

				break;
			}

			counter++;
		}
	}

	protected void propagateValuesForRook() {

		for (int i = currentPlace.getX() + 1; i < 12; i++) {

			if (attachMe(currentPlace.getBoard().getPlacez()[i][currentPlace.getY()], this)) {

				break;
			}
		}

		for (int i = currentPlace.getY() + 1; i < 12; i++) {

			if (attachMe(currentPlace.getBoard().getPlacez()[currentPlace.getX()][i], this)) {

				break;
			}
		}

		for (int i = currentPlace.getX() - 1; i >= 0; i--) {

			if (attachMe(currentPlace.getBoard().getPlacez()[i][currentPlace.getY()], this)) {

				break;
			}
		}

		for (int i = currentPlace.getY() - 1; i >= 0; i--) {

			if (attachMe(currentPlace.getBoard().getPlacez()[currentPlace.getX()][i], this)) {

				break;
			}
		}
	}

	protected void addBisshopLinez(Board board) {

		linez.add(new Line(1, 1, board, this));
		linez.add(new Line(1, -1, board, this));
		linez.add(new Line(-1, 1, board, this));
		linez.add(new Line(-1, -1, board, this));
	}

	protected void addRookLinez(Board board) {

		linez.add(new Line(0, 1, board, this));
		linez.add(new Line(1, 0, board, this));
		linez.add(new Line(0, -1, board, this));
		linez.add(new Line(-1, 0, board, this));
	}

	@Override
	public void calculateDefence(Place to) {

		for (Line line : linez) {

			line.calculateRange(to);
		}
	}

	@Override
	public void calculateOffence() {

		for (Line line : linez) {

			line.calculateRange(null);
		}
	}

	@Override
	public boolean attacks(Place to) {

		for (Line line : linez) {

			if (line.includes(to)) {

				return true;
			}
		}

		return false;
	}

	@Override
	public boolean defends(Place to) {

		for (Line line : linez) {

			if (line.includes(to)) {

				return true;
			}
		}

		return false;
	}

	protected void createBisshopCheckLinez(Board board) {

		checkLinez.add(new CheckLine(1, 1, board, this));
		checkLinez.add(new CheckLine(1, -1, board, this));
	}

	protected void createRookCheckLinez(Board board) {

		checkLinez.add(new CheckLine(1, 0, board, this));
		checkLinez.add(new CheckLine(0, 1, board, this));
	}

	@Override
	public void updateChecks(Player kingPlayer) {

		for (CheckLine checkLine : checkLinez) {

			checkLine.update();
		}

		for (CheckLine checkLine : checkLinez) {

			checkLine.checkCheck(kingPlayer);
		}
	}

}
