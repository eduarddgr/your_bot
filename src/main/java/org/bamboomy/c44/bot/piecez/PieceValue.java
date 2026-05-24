
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

import lombok.Getter;

@Getter
public enum PieceValue {

	KING(0, "king", "k", 1000, King.class), PAWN(5, "pawn", "p", 1, Pawn.class),
	BISSHOP(3, "bisshop", "b", 3, Bisshop.class), TOWER(2, "tower", "t", 5, Tower.class),
	HORSE(4, "horse", "h", 3, Horse.class), QUEEN(1, "queen", "q", 10, Queen.class);

	private final int ordinal, value;
	private final String name, letter;
	private final Class pieceClass;

	private PieceValue(int ordinal, String name, String letter, int value, Class pieceClass) {
		this.ordinal = ordinal;
		this.name = name;
		this.letter = letter;
		this.value = value;
		this.pieceClass = pieceClass;
	}

	public static PieceValue getByName(String name) {

		for (PieceValue pv : PieceValue.values()) {

			if (pv.getName().equalsIgnoreCase(name)) {

				return pv;
			}
		}

		return null;
	}
}
