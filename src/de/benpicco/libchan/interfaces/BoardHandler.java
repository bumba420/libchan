package de.benpicco.libchan.interfaces;

import de.benpicco.libchan.imageboards.Board;

public interface BoardHandler {
	public void onAddBoard(Board board);

	public void onBoardParsingDone();
}
