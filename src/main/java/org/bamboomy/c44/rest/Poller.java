
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

package org.bamboomy.c44.rest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.bamboomy.c44.bot.board.Board;
import org.bamboomy.c44.bot.player.Alliance;
import org.bamboomy.c44.bot.player.Color;
import org.bamboomy.c44.bot.player.Player;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class Poller implements Runnable {

	private String serverUrl;
	private String robotHash;
	private String[] playerHashesArray;
	private String ownColor;

	private Alliance alliance;

	private Player player;

	private boolean thinking = false;

	private static final boolean OUTPUT_POUNDER = true;

	public Poller(String serverUrl, String robotHash, String colors) {

		this.serverUrl = serverUrl;
		this.robotHash = robotHash;

		playerHashesArray = getPath(serverUrl, "/register/" + robotHash + "/" + colors).toString().split(",");
	}

	@Override
	public void run() {

		boolean done = false;

		System.out.println(ownColor);

		System.out.print("waiting...");

		Board board = null;

		int index = 0;

		while (!done) {

			System.out.print(".");

			try {

				Thread.sleep(3_000);

			} catch (InterruptedException e) {

				e.printStackTrace();
			}

			StringBuffer robotBoard = getPath(serverUrl,
					"/getRobotBoard/" + robotHash + "/" + playerHashesArray[index]);

			int augment = 0;

			if (robotBoard == null && augment < playerHashesArray.length) {

				index = (index + 1) % playerHashesArray.length;

				augment++;

				robotBoard = getPath(serverUrl, "/getRobotBoard/" + robotHash + "/" + playerHashesArray[index]);
			}

			if (robotBoard != null && !robotBoard.toString().isEmpty()) {

				System.out.println("I'm supposed to play now...");

				ownColor = getPath(serverUrl, "/getColor/" + robotHash + "/" + playerHashesArray[index]).toString();

				String allianceColor = getPath(serverUrl,
						"/getAllianceColor/" + robotHash + "/" + playerHashesArray[index]).toString();

				alliance = new Alliance(Color.getByName(ownColor), Color.getByName(allianceColor));

				System.out.println("(with color: " + ownColor + "; in alliance: " + allianceColor + ")...");

				board = new Board(robotBoard.toString(), alliance, ownColor, this);

				player = board.getOwnPlayer();

				player.startCalculation();

				thinking = true;
			}

			long start = System.currentTimeMillis();

			int pointz = 0, meta = 0;

			long finish;

			while (thinking) {

				if (OUTPUT_POUNDER) {

					System.out.print("Poundering[" + meta + "]...");

					for (int i = 0; i < pointz; i++) {

						System.out.print(".");
					}

					finish = System.currentTimeMillis();

					System.out.println("(" + (finish - start) / 1_000 + "s)");
				}

				try {

					Thread.sleep(7_000);

				} catch (InterruptedException e) {

					e.printStackTrace();
				}

				pointz++;

				if (pointz % 10 == 0) {

					meta++;
					pointz = 0;
				}
			}

			finish = System.currentTimeMillis();

			System.out.println("I poundered for: " + (finish - start) / 1_000 + " secondz...");

			index = (index + 1) % playerHashesArray.length;

			System.out.print("waiting to play again...");
		}

		System.out.println("I'm done...");
	}

	private StringBuffer getPath(String serverUrl, String path) {

		StringBuffer result = null;

		URL url;

		try {

			url = new URL(serverUrl + "/react" + path);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			result = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				result.append(inputLine);
			}
			in.close();

			con.disconnect();

			System.out.print(result.toString());

		} catch (ConnectException e) {

			System.out.println("could not connect :(");

			return null;

		} catch (IOException e) {

			e.printStackTrace();
		}

		return result;
	}

	public void movePiece(String pieceMd5, String moveMd5) {

		getPath(serverUrl, "/playBot/" + robotHash + "/" + pieceMd5 + "/" + moveMd5);

		System.out.println("moving piece...");

		thinking = false;
	}

	private void post(String output, String contentType, String urlPart) {

		StringBuffer result = null;

		URL url;

		try {

			url = new URL(serverUrl + "/react/" + urlPart + "/" + robotHash);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setRequestProperty("Content-Type", contentType);
			con.connect();

			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
			out.write(output);
			out.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			result = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				result.append(inputLine);
			}

			in.close();

			con.disconnect();

			System.out.print(result.toString() + " -- ");

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public void output(String output) {

		post(output, "text/html; charset=utf-8", "updateOutput");
	}

	public void sendBoard(Board board) {

		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = "doesn't work :(";

		try {

			json = ow.writeValueAsString(board.getGuiArray(ownColor, true));

		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		post(json, "application/json", "postBoard");
	}
}
