package com.cleancode.tetris;

public class Board {

    private final boolean[][] cells;
    private final int numRows;
    private final int numCols;
    private final ConfigurationManager config;

    public Board(ConfigurationManager configManager) {
        this.config = configManager;
        this.numRows = config.getRows();
        this.numCols = config.getCols();
        this.cells = new boolean[numRows][numCols];
        clearBoard();
    }

    public void clearBoard() {
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                cells[i][j] = false;
            }
        }
    }

    public int getNumRows() {
        return numRows;
    }

    public int getNumCols() {
        return numCols;
    }

    public boolean isCellOccupied(int row, int col) {
        if (row >= 0 && row < numRows && col >= 0 && col < numCols) {
            return cells[row][col];
        }
        return true; // Consider out-of-bounds as occupied to prevent piece going out
    }

    public void placePiece(int pieceCol, int pieceRow, String pieceName, int angle) {
        int[] xs = config.getShapeXCoordinates(pieceName, angle);
        int[] ys = config.getShapeYCoordinates(pieceName, angle);

        for (int i = 0; i < 4; i++) {
            int boardX = pieceCol + xs[i];
            int boardY = pieceRow + ys[i];
            if (boardY >= 0 && boardY < numRows && boardX >= 0 && boardX < numCols) {
                cells[boardY][boardX] = true;
            }
        }
    }

    public int clearFullLines() {
        int linesCleared = 0;
        for (int i = numRows - 1; i >= 0; i--) {
            boolean full = true;
            for (int j = 0; j < numCols; j++) {
                if (!cells[i][j]) {
                    full = false;
                    break;
                }
            }
            if (full) {
                linesCleared++;
                for (int k = i; k > 0; k--) {
                    System.arraycopy(cells[k - 1], 0, cells[k], 0, numCols);
                }
                cells[0] = new boolean[numCols]; // Clears the top line
                i++; // Re-check the current line as it now contains the line from above
            }
        }
        return linesCleared;
    }

    public boolean[][] getCells() {
        // Return a copy to prevent external modification if needed,
        // or the direct reference if performance is critical and trust is high.
        // For now, returning direct reference as GamePanel will only read.
        return cells;
    }
}