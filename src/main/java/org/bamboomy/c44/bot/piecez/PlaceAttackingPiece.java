
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
import org.bamboomy.c44.bot.player.Player;

public abstract class PlaceAttackingPiece extends Piece {

	protected final ArrayList<Place> reach = new ArrayList<Place>();

	public PlaceAttackingPiece(Place place, int color, Player player, String identifier, PieceValue value,
			boolean moved, Board board, String md5, int oldColor) {

		super(place, color, player, identifier, value, moved, board, md5, oldColor);
	}

	@Override
	public void calculateDefence(Place to) {

		calculateReach(to);
	}

	@Override
	public void calculateOffence() {

		calculateReach(null);
	}

	protected abstract void calculateReach(Place to);

	@Override
	public boolean attacks(Place to) {

		return reach.contains(to);
	}

	@Override
	public boolean defends(Place to) {

		return reach.contains(to);
	}

	@Override
	public void updateChecks(Player kingPlayer) {

		calculateReach(null);

		for (Place place : reach) {

			if (place != null && place.getPiece() == kingPlayer.getKing()) {

				kingPlayer.setCheck(this);
			}
		}
	}

}
