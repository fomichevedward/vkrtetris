package com.vkrmy.tetris;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;

import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.JOptionPane;
import  javax.swing.UIManager;

import com.vkrmy.tetris.Tetri.Tetrominoes;

public class GameBoardPanel extends JPanel implements ActionListener {
    private static final int BoardWidth = 10;    // размер игровой доски по x(10)
    private static final int BoardHeight = 24;    // размер игровой доски по y(22)

    // игровой статус & таймер
    private Timer timer;
    private boolean isFallingDone = false;
    private boolean isStarted = false;
    private boolean isPaused = false;
    private int currentScore = 0; // удаленная линия == score

    // положение текущего блока
    private int curX = 0;
    private int curY = 0;

    // текущее тетромино
    private Tetri curBlock;

    // logical игровой блок
    private Tetrominoes[] gameBoard;
    private Color[] colorTable;

    // настройка статуса игры
    private String currentStatus;
    private String currentLevel;
    private int currentTimerResolution;

    private GameWindow tetrisFrameD;


    public GameBoardPanel(GameWindow tetrisFrame, int timerResolution) {

        setFocusable(true);
        setBackground(new Color(0, 30, 30));
        curBlock = new Tetri();
        timer = new Timer(timerResolution, this);
        timer.start();    // актвиация таймера
        currentTimerResolution = timerResolution;

        gameBoard = new Tetrominoes[BoardWidth * BoardHeight];

        // цвет тетромино блоков
        colorTable = new Color[]{
                new Color(0, 0, 0), new Color(150, 110, 255),
                new Color(255, 128, 0), new Color(255, 0, 0),
                new Color(32, 128, 255), new Color(255, 0, 255),
                new Color(255, 255, 0), new Color(0, 255, 0)
        };

        // добавление клавиш управления
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!isStarted || curBlock.getShape() == Tetrominoes.NO_BLOCK) {
                    return;
                }

                int keycode = e.getKeyCode();

                if (keycode == 'p' || keycode == 'P') {
                    pause();
                    return;
                }

                if (isPaused) {
                    return;
                }

                switch (keycode) {
                    case 'a':
                    case 'A':
                    case KeyEvent.VK_LEFT:
                        isMovable(curBlock, curX - 1, curY);
                        break;
                    case 'd':
                    case 'D':
                    case KeyEvent.VK_RIGHT:
                        isMovable(curBlock, curX + 1, curY);
                        break;
                    case 'w':
                    case 'W':
                    case KeyEvent.VK_UP:
                        isMovable(curBlock.rotateRight(), curX, curY);
                        break;
                    case 's':
                    case 'S':
                    case KeyEvent.VK_DOWN:
                        advanceOneLine();
                        break;
                    case KeyEvent.VK_SPACE:
                        advanceToEnd();
                        break;
                    case 'p':
                    case 'P':
                        pause();
                        break;
                }

            }
        });

        tetrisFrameD = tetrisFrame;
        initBoard();
    }

    // натсройка уровня игры
    private void setResolution() {

        switch (currentScore / 10) {
            case 10:
                currentTimerResolution = 100;
                break;
            case 9:
                currentTimerResolution = 130;
                break;
            case 8:
                currentTimerResolution = 160;
                break;
            case 7:
                currentTimerResolution = 190;
                break;
            case 6:
                currentTimerResolution = 220;
                break;
            case 5:
                currentTimerResolution = 250;
                break;
            case 4:
                currentTimerResolution = 280;
                break;
            case 3:
                currentTimerResolution = 310;
                break;
            case 2:
                currentTimerResolution = 340;
                break;
            case 1:
                currentTimerResolution = 370;
                break;
            case 0:
                currentTimerResolution = 370;
                break;
        }

        timer.setDelay(currentTimerResolution);

    }

    // инициализация игровго поля
    private void initBoard() {
        for (int i = 0; i < BoardWidth * BoardHeight; i++) {
            gameBoard[i] = Tetrominoes.NO_BLOCK;
        }
    }

    // таймер обратного вызова
    @Override
    public void actionPerformed(ActionEvent e) {
        if (isFallingDone) {
            isFallingDone = !isFallingDone; // перекл статус
            newTetromino();
        } else {
            advanceOneLine();
        }
    }

    public void start() {
        if (isPaused) {
            return;
        }

        isStarted = true;
        isFallingDone = false;
        currentScore = 0;
        initBoard();

        newTetromino();
        timer.start();
    }

    public void pause() {
        if (!isStarted) {
            return;
        }

        isPaused = !isPaused;
        if (isPaused) {
            timer.stop();
        } else {
            timer.start();
        }

        repaint();
    }

    // вычисляет фактический размер тетрамино на экране
    private int blockWidth() {
        return (int) getSize().getWidth() / BoardWidth;
    }

    private int blockHeight() {
        return (int) getSize().getHeight() / BoardHeight;
    }

    // текущее пололдение тетрамино в массиве (atom)
    Tetrominoes curTetrominoPos(int x, int y) {
        return gameBoard[(y * BoardWidth) + x];
    }

    @Override
    public void paint(Graphics g) {

        super.paint(g);

        if (!isPaused) {
            currentStatus = "Score: " + currentScore;
            currentLevel = "Level: " + (currentScore / 10 + 1);
        } else {
            currentStatus = "PAUSED";
            currentLevel = "";
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.PLAIN, 28));
        g.drawString(currentStatus, 15, 35);
        g.drawString(currentLevel, 15, 70);

        Dimension size = getSize();
        int boardTop = (int) size.getHeight() - BoardHeight * blockHeight();

        // рендеринг - тень тетрамино
        int tempY = curY;
        while (tempY > 0) {
            if (!atomIsMovable(curBlock, curX, tempY - 1, false))
                break;
            tempY--;
        }
        for (int i = 0; i < 4; i++) {
            int x = curX + curBlock.getX(i);
            int y = tempY - curBlock.getY(i);
            drawTetromino(g, 0 + x * blockWidth(), boardTop + (BoardHeight - y - 1) * blockHeight(), curBlock.getShape(),
                    true);
        }

        // рендеринг - игровая панель
        for (int i = 0; i < BoardHeight; i++) {
            for (int j = 0; j < BoardWidth; j++) {
                Tetrominoes shape = curTetrominoPos(j, BoardHeight - i - 1);
                if (shape != Tetrominoes.NO_BLOCK)
                    drawTetromino(g, 0 + j * blockWidth(), boardTop + i * blockHeight(), shape, false);
            }
        }


        // рендеринг - текущее тетрамино
        if (curBlock.getShape() != Tetrominoes.NO_BLOCK) {
            for (int i = 0; i < 4; i++) {
                int x = curX + curBlock.getX(i);
                int y = curY - curBlock.getY(i);
                drawTetromino(g, 0 + x * blockWidth(), boardTop + (BoardHeight - y - 1) * blockHeight(),
                        curBlock.getShape(), false);
            }
        }

    }

    private void drawTetromino(Graphics g, int x, int y, Tetrominoes bs, boolean isShadow) {
        Color curColor = colorTable[bs.ordinal()];

        if (!isShadow) {
            g.setColor(curColor);
            g.fillRect(x + 1, y + 1, blockWidth() - 2, blockHeight() - 2);
        } else {
            g.setColor(curColor.darker().darker());
            g.fillRect(x + 1, y + 1, blockWidth() - 2, blockHeight() - 2);
        }
    }

    private void removeFullLines() {
        int fullLines = 0;

        for (int i = BoardHeight - 1; i >= 0; i--) {
            boolean isFull = true;

            for (int j = 0; j < BoardWidth; j++) {
                if (curTetrominoPos(j, i) == Tetrominoes.NO_BLOCK) {
                    isFull = false;
                    break;
                }
            }

            if (isFull) {
                ++fullLines;
                for (int k = i; k < BoardHeight - 1; k++) {
                    for (int l = 0; l < BoardWidth; ++l)
                        gameBoard[(k * BoardWidth) + l] = curTetrominoPos(l, k + 1);
                }
            }
        }

        if (fullLines > 0) {
            currentScore += fullLines;
            isFallingDone = true;
            curBlock.setShape(Tetrominoes.NO_BLOCK);
            setResolution();
            repaint();
        }

    }

    // true - actual tetromino pos
    // false - shadow pos
    private boolean atomIsMovable(Tetri chkBlock, int chkX, int chkY, boolean flag) {
        for (int i = 0; i < 4; i++) {
            int x = chkX + chkBlock.getX(i);
            int y = chkY - chkBlock.getY(i);
            if (x < 0 || x >= BoardWidth || y < 0 || y >= BoardHeight)
                return false;
            if (curTetrominoPos(x, y) != Tetrominoes.NO_BLOCK) {
                return false;
            }
        }

        if (flag) {
            curBlock = chkBlock;
            curX = chkX;
            curY = chkY;
            repaint();
        }

        return true;
    }

    private boolean isMovable(Tetri chkBlock, int chkX, int chkY) {
        return atomIsMovable(chkBlock, chkX, chkY, true);
    }

    private void newTetromino() {
        curBlock.setRandomShape();
        curX = BoardWidth / 2 + 1;
        curY = BoardHeight - 1 + curBlock.minY();

        if (!isMovable(curBlock, curX, curY)) {
            curBlock.setShape(Tetrominoes.NO_BLOCK);
            timer.stop();
            isStarted = false;
            GameOver(currentScore);
        }
    }

    private void tetrominoFixed() {
        for (int i = 0; i < 4; i++) {
            int x = curX + curBlock.getX(i);
            int y = curY - curBlock.getY(i);
            gameBoard[(y * BoardWidth) + x] = curBlock.getShape();
        }

        removeFullLines();

        if (!isFallingDone) {
            newTetromino();
        }
    }

    private void advanceOneLine() {
        if (!isMovable(curBlock, curX, curY - 1)) {
            tetrominoFixed();
        }
    }

    private void advanceToEnd() {
        int tempY = curY;
        while (tempY > 0) {
            if (!isMovable(curBlock, curX, tempY - 1))
                break;
            --tempY;
        }
        tetrominoFixed();
    }

    private void GameOver(int dbScore) {
        int maxScore = readDB();
        String showD = "";
        if (dbScore > maxScore) {
          // writeDB(dbScore);//
            showD = String.format("%nCongratulations! %nNew max score: %d", dbScore);
        } else {
            showD = String.format("Score: %d %nMax score: %d", dbScore, maxScore);
        }
        UIManager.put("OptionPane.okButtonText", "new game");
        JOptionPane.showMessageDialog(null, showD, "Game Over!", JOptionPane.OK_OPTION);
        setResolution();
        start();
    }

    private int readDB() {
        try {
            BufferedReader inputStream = new BufferedReader(new FileReader("Tetris.score"));
            String dbMaxScore = inputStream.readLine();
            inputStream.close();
            return Integer.parseInt(dbMaxScore);
        } catch (IOException e) {
            return -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

}

