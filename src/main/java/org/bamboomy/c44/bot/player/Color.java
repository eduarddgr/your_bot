
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

import lombok.Getter;

import java.util.function.BiFunction;

@Getter
public enum Color {

    GREEN(0, "Green", "green", 'g', 1, 0, (x, y) -> 11 - x, (x, y) -> 11 - y),
    BLUE(1, "Blue", "blue", 'b', 0, -1, (x, y) -> 11 - y, (x, y) -> x),
    RED(2, "Red", "red", 'r', -1, 0, (x, y) -> x, (x, y) -> y),
    YELLOW(3, "Yellow", "yellow", 'y', 0, 1, (x, y) -> y, (x, y) -> 11 - x),
    DEAD(4, "Dead", "grey", 'd', 0, 0, (x, y) -> x, (x, y) -> y),
    NO_COLOR(-1, "", "", 'n', 0, 0, (x, y) -> x, (x, y) -> y);

    private final int seq, xDirection, yDirection;
    private final String name;
    private final String cssColor;
    private final char shortDebug;
    private final BiFunction<Integer, Integer, Integer> xMapping;
    private final BiFunction<Integer, Integer, Integer> yMapping;

    Color(int seq, String name, String cssColor, char shortDebug,
          int xDirection, int yDirection,
          BiFunction<Integer, Integer, Integer> xMapping, BiFunction<Integer, Integer, Integer> yMapping) {
        this.seq = seq;
        this.name = name;
        this.cssColor = cssColor;
        this.shortDebug = shortDebug;
        this.xDirection = xDirection;
        this.yDirection = yDirection;
        this.xMapping = xMapping;
        this.yMapping = yMapping;
    }

    public static Color getByName(String name) {

        for (Color p : Color.values()) {

            if (p.name.equalsIgnoreCase(name)) {

                return p;
            }
        }

        return null;
    }

    public static Color getBySeq(int seq) {

        for (Color p : Color.values()) {

            if (p.seq == seq) {

                return p;
            }
        }

        return null;
    }

    public static Color[] getAll() {

        Color[] colorz = new Color[4];

        for (int i = 0; i < 4; i++) {

            colorz[i] = Color.getBySeq(i);
        }

        return colorz;
    }

}
