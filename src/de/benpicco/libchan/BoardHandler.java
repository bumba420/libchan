package de.benpicco.libchan;

import de.benpicco.libchan.imageboards.Board;

public interface BoardHandler {
	public void onAddBoard(Board board);

	public void onBoardParsingDone();
}
