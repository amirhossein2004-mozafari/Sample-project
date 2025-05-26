package com.cleancode.tetris;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Random;

public class MessyTetris extends JFrame {

    private final ConfigurationManager config = ConfigurationManager.getInstance();

    boolean[][] boardCells;

    int[] shapePosition = new int[2];
    String shape;
    int angle;
    boolean newShapeGenerated = false;
    boolean paused = false;
    boolean started = false;
    JLabel labelScoreInfo;
    JLabel labelScoreValue;
    JButton startButton;
    JButton pauseButton;
    int panelScore = 0;
    Timer timer;
    KeyListener gameKeyListener;
    GamePanel gamePanel;
    JPanel sidePanel;

    public MessyTetris() {
        boardCells = new boolean[config.getRows()][config.getCols()];

        setTitle("Tetris Clean Code");
        int gamePanelWidth = config.getCols() * config.getCellSize();
        int gamePanelHeight = config.getRows() * config.getCellSize();
        int sidePanelWidth = 180;
        int padding = 10;
        int frameWidth = gamePanelWidth + sidePanelWidth + 3 * padding;
        int frameHeight = gamePanelHeight + 2 * padding + 30;

        setSize(frameWidth, frameHeight);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setResizable(false);

        gameKeyListener = new CustomKeyListener();
        gamePanel = new GamePanel();
        gamePanel.setBounds(padding, padding, gamePanelWidth, gamePanelHeight);
        add(gamePanel);

        sidePanel = new JPanel();
        sidePanel.setLayout(null);
        sidePanel.setBackground(new Color(20, 20, 80));
        sidePanel.setBounds(gamePanelWidth + 2 * padding, padding, sidePanelWidth, gamePanelHeight);
        sidePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.CYAN),
                "Game Info", TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                Color.CYAN));
        add(sidePanel);

        labelScoreInfo = new JLabel("Score", SwingConstants.CENTER);
        labelScoreInfo.setForeground(Color.WHITE);
        labelScoreInfo.setFont(new Font("Arial", Font.BOLD, 18));
        labelScoreInfo.setBounds(10, 30, sidePanelWidth - 20, 24);
        sidePanel.add(labelScoreInfo);

        labelScoreValue = new JLabel("0", SwingConstants.CENTER);
        labelScoreValue.setOpaque(true);
        labelScoreValue.setBackground(new Color(50, 50, 120));
        labelScoreValue.setForeground(Color.WHITE);
        labelScoreValue.setFont(new Font("Digital-7 Mono", Font.BOLD, 28));
        labelScoreValue.setBounds(10, 60, sidePanelWidth - 20, 38);
        sidePanel.add(labelScoreValue);

        startButton = new JButton("Start Game");
        startButton.setBounds(20, 120, sidePanelWidth - 40, 35);
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        sidePanel.add(startButton);

        pauseButton = new JButton("Pause");
        pauseButton.setBounds(20, 165, sidePanelWidth - 40, 35);
        pauseButton.setFont(new Font("Arial", Font.BOLD, 14));
        pauseButton.setEnabled(false);
        sidePanel.add(pauseButton);

        startButton.addActionListener(e -> {
            if (!started) {
                startGame();
            } else {
                resetGame();
            }
        });

        pauseButton.addActionListener(e -> {
            if (started && !isGameOver()) {
                if (!paused) {
                    timer.stop();
                    pauseButton.setText("Resume");
                    paused = true;
                } else {
                    timer.start();
                    pauseButton.setText("Pause");
                    paused = false;
                }
                gamePanel.requestFocusInWindow();
            }
        });

        timer = new Timer(config.getGameSpeed(), e -> {
            if (!paused && started && !isGameOver()) {
                dropShape();
            }
            gamePanel.repaint();
            labelScoreValue.setText(String.valueOf(panelScore));
        });

        gamePanel.addKeyListener(gameKeyListener);
        setVisible(true);
        gamePanel.requestFocusInWindow();
    }

    void resetBoard() {
        for (int i = 0; i < config.getRows(); i++) {
            for (int j = 0; j < config.getCols(); j++) {
                boardCells[i][j] = false;
            }
        }
        panelScore = 0;
        labelScoreValue.setText("0");
        newShapeGenerated = false;
        gamePanel.repaint();
    }

    void startGame() {
        resetBoard();
        generateNewShape();
        started = true;
        paused = false;
        timer.start();
        startButton.setText("Reset Game");
        pauseButton.setEnabled(true);
        pauseButton.setText("Pause");
        gamePanel.requestFocusInWindow();
    }

    void resetGame() {
        timer.stop();
        resetBoard();
        started = false;
        paused = false;
        startButton.setText("Start Game");
        pauseButton.setText("Pause");
        pauseButton.setEnabled(false);
        gamePanel.repaint();
    }

    void generateNewShape() {
        shapePosition[0] = config.getInitialShapeX();
        shapePosition[1] = config.getInitialShapeY();
        angle = new Random().nextInt(4);

        List<String> shapeNames = config.getShapeNames();
        if (shapeNames == null || shapeNames.isEmpty()) {
            timer.stop();
            JOptionPane.showMessageDialog(this, "Error: No tetromino shapes defined in config!", "Configuration Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }
        shape = shapeNames.get(new Random().nextInt(shapeNames.size()));
        newShapeGenerated = true;

        if (checkCollision(shapePosition[0], shapePosition[1], angle)) {
            timer.stop();
            started = false;
            gamePanel.repaint();
        }
    }

    boolean isGameOver() {
        for (int j = 0; j < config.getCols(); j++) {
            if (boardCells[0][j]) {
                return true;
            }
        }
        return false;
    }

    private boolean checkCollision(int newX, int newY, int targetAngle) {
        int[] xs = config.getShapeXCoordinates(this.shape, targetAngle);
        int[] ys = config.getShapeYCoordinates(this.shape, targetAngle);
        for (int i = 0; i < 4; i++) {
            int x = newX + xs[i];
            int y = newY + ys[i];
            if (x < 0 || x >= config.getCols() || y >= config.getRows()) {
                return true;
            }
            if (y < 0) continue;
            if (boardCells[y][x]) {
                return true;
            }
        }
        return false;
    }

    void dropShape() {
        if (!checkCollision(shapePosition[0], shapePosition[1] + 1, angle)) {
            shapePosition[1]++;
        } else {
            fixShapeToBoard();
            clearFullLines();
            if (!isGameOver()) {
                generateNewShape();
            } else {
                timer.stop();
                started = false;
            }
        }
    }

    void fixShapeToBoard() {
        int[] xs = config.getShapeXCoordinates(this.shape, this.angle);
        int[] ys = config.getShapeYCoordinates(this.shape, this.angle);
        for (int i = 0; i < 4; i++) {
            int boardX = shapePosition[0] + xs[i];
            int boardY = shapePosition[1] + ys[i];
            if (boardY >= 0 && boardY < config.getRows() && boardX >= 0 && boardX < config.getCols()) {
                boardCells[boardY][boardX] = true;
            }
        }
    }

    void moveRight() {
        if (!checkCollision(shapePosition[0] + 1, shapePosition[1], angle)) {
            shapePosition[0]++;
        }
    }

    void moveLeft() {
        if (!checkCollision(shapePosition[0] - 1, shapePosition[1], angle)) {
            shapePosition[0]--;
        }
    }

    void rotate() {
        int newAngle = (angle + 1) % 4;
        if (!checkCollision(shapePosition[0], shapePosition[1], newAngle)) {
            angle = newAngle;
        }
    }

    void hardDrop() {
        while (!checkCollision(shapePosition[0], shapePosition[1] + 1, angle)) {
            shapePosition[1]++;
        }
        fixShapeToBoard();
        clearFullLines();
        if (!isGameOver()) {
            generateNewShape();
        } else {
            timer.stop();
            started = false;
        }
    }

    void clearFullLines() {
        int linesCleared = 0;
        for (int i = config.getRows() - 1; i >= 0; i--) {
            boolean full = true;
            for (int j = 0; j < config.getCols(); j++) {
                if (!boardCells[i][j]) {
                    full = false;
                    break;
                }
            }
            if (full) {
                linesCleared++;
                for (int k = i; k > 0; k--) {
                    System.arraycopy(boardCells[k - 1], 0, boardCells[k], 0, config.getCols());
                }
                boardCells[0] = new boolean[config.getCols()];
                i++;
            }
        }
        if (linesCleared > 0) {
            switch (linesCleared) {
                case 1: panelScore += 100; break;
                case 2: panelScore += 300; break;
                case 3: panelScore += 500; break;
                case 4: panelScore += 800; break;
            }
            labelScoreValue.setText(String.valueOf(panelScore));
        }
    }

    class GamePanel extends JPanel {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setColor(new Color(30, 30, 100));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            int cellSize = config.getCellSize();
            for (int i = 0; i < config.getRows(); i++) {
                for (int j = 0; j < config.getCols(); j++) {
                    if (boardCells[i][j]) {
                        g2d.setColor(Color.CYAN);
                        g2d.fillRect(j * cellSize, i * cellSize, cellSize -1 , cellSize -1);
                    }
                }
            }

            if (started && newShapeGenerated && shape != null) {
                int[] xs = config.getShapeXCoordinates(shape, angle);
                int[] ys = config.getShapeYCoordinates(shape, angle);
                g2d.setColor(Color.ORANGE);
                for (int i = 0; i < 4; i++) {
                    int x = (shapePosition[0] + xs[i]) * cellSize;
                    int y = (shapePosition[1] + ys[i]) * cellSize;
                    if (y >= 0) { // Draw only if the piece is on or below the top of the board
                        g2d.fillRect(x, y, cellSize -1 , cellSize -1);
                    }
                }
            }

            g2d.setColor(Color.DARK_GRAY);
            for (int i = 0; i <= config.getCols(); i++) {
                g2d.drawLine(i * cellSize, 0, i * cellSize, config.getRows() * cellSize);
            }
            for (int i = 0; i <= config.getRows(); i++) {
                g2d.drawLine(0, i * cellSize, config.getCols() * cellSize, i * cellSize);
            }

            if (isGameOver() && !started) {
                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setColor(Color.RED);
                g2d.setFont(new Font("Arial", Font.BOLD, 40));
                String gameOverMsg = "GAME OVER";
                FontMetrics fm = g2d.getFontMetrics();
                int msgWidth = fm.stringWidth(gameOverMsg);
                g2d.drawString(gameOverMsg, (getWidth() - msgWidth) / 2, getHeight() / 2 - 20);

                g2d.setFont(new Font("Arial", Font.BOLD, 24));
                String scoreMsg = "Final Score: " + panelScore;
                fm = g2d.getFontMetrics();
                msgWidth = fm.stringWidth(scoreMsg);
                g2d.drawString(scoreMsg, (getWidth() - msgWidth) / 2, getHeight() / 2 + 20);
            }
        }
    }

    private class CustomKeyListener implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {}

        @Override
        public void keyPressed(KeyEvent e) {
            if (!started || paused || isGameOver()) return;

            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP: case KeyEvent.VK_W:
                    rotate();
                    break;
                case KeyEvent.VK_DOWN: case KeyEvent.VK_S:
                    dropShape();
                    break;
                case KeyEvent.VK_RIGHT: case KeyEvent.VK_D:
                    moveRight();
                    break;
                case KeyEvent.VK_LEFT: case KeyEvent.VK_A:
                    moveLeft();
                    break;
                case KeyEvent.VK_SPACE:
                    hardDrop();
                    break;
            }
            gamePanel.repaint();
        }

        @Override
        public void keyReleased(KeyEvent e) {}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MessyTetris::new);
    }
}