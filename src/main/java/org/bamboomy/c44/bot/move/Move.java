
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

import org.bamboomy.c44.bot.board.Place;
import org.bamboomy.c44.bot.piecez.Piece;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class Move {

	protected Place from, to;

	protected Piece piece;

	private Piece takenPiece;

	private static final boolean DEBUG = false;

	protected String identifier = "m";
	
	@Setter
	@Getter
	private Move[] movesToDepth;

	@Setter
	private Boolean attacked = null, defended = null;

	public Move(Place from, Place to, Piece piece, String identifier) {

		this.from = from;
		this.to = to;

		if (to == null) {

			throw new RuntimeException("to is null");
		}

		this.piece = piece;

		this.identifier = identifier;
	}

	public boolean execute(boolean fromGameMaster) {

		if (to.getPiece() != null) {

			takenPiece = to.getPiece();

			takenPiece.setRemoved(true);
		}

		return piece.moveTo(to, fromGameMaster);
	}

	public void rollback() {

		resetPiece();

		if (takenPiece != null) {

			to.setPiece(takenPiece);

			takenPiece.setRemoved(false);
		}

		/*
		 * if (enPassant != null) {
		 * 
		 * EnPassant result = enPassant.release();
		 * 
		 * enPassant = result;
		 * 
		 * piece.getPlayer().setEnPassant(result); }
		 */
	}

	protected void resetPiece() {

		piece.moveTo(from, false);

		piece.rollBackMoved();
	}

}
