
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

package org.bamboomy.c44.bot.move;

import java.util.ArrayList;

import org.bamboomy.c44.bot.board.Board;
import org.bamboomy.c44.bot.board.Place;
import org.bamboomy.c44.bot.piecez.LinePiece;
import org.bamboomy.c44.bot.player.Player;

public class CheckLine {

	private int xDelta, yDelta;
	private Board board;
	private LinePiece linePiece;

	private ArrayList<Place> oneDirection = new ArrayList<Place>(), otherDirection = new ArrayList<Place>();

	public CheckLine(int xDelta, int yDelta, Board board, LinePiece linePiece) {

		this.xDelta = xDelta;
		this.yDelta = yDelta;
		this.board = board;
		this.linePiece = linePiece;

		update();
	}

	public void update() {

		oneDirection = new ArrayList<Place>();

		Place otherPlace = linePiece.getCurrentPlace();

		while (otherPlace != null) {

			if (otherPlace.getX() + xDelta < 11 && otherPlace.getY() + yDelta < 11 && otherPlace.getY() + yDelta > 0) {

				if (otherPlace != linePiece.getCurrentPlace()) {

					oneDirection.add(otherPlace);
				}

				otherPlace = board.getPlacez()[otherPlace.getX() + xDelta][otherPlace.getY() + yDelta];

			} else {

				otherPlace = null;
			}
		}

		otherDirection = new ArrayList<Place>();

		otherPlace = linePiece.getCurrentPlace();

		while (otherPlace != null) {

			if (otherPlace.getX() - xDelta > 0 && otherPlace.getY() - yDelta < 11 && otherPlace.getY() - yDelta > 0) {

				if (otherPlace != linePiece.getCurrentPlace()) {

					otherDirection.add(otherPlace);
				}

				otherPlace = board.getPlacez()[otherPlace.getX() - xDelta][otherPlace.getY() - yDelta];

			} else {

				otherPlace = null;
			}
		}
	}

	public void checkCheck(Player player) {

		for (Place place : oneDirection) {

			if (place.getPiece() != null && place.getPiece() != player.getKing()) {

				break;
			}

			if (place.getPiece() == player.getKing()) {

				player.setCheck(linePiece);
			}
		}

		for (Place place : otherDirection) {

			if (place.getPiece() != null && place.getPiece() != player.getKing()) {

				break;
			}

			if (place.getPiece() == player.getKing()) {

				player.setCheck(linePiece);
			}
		}
	}

}
