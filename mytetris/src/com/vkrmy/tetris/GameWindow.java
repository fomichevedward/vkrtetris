package com.vkrmy.tetris;

import java.awt.GridLayout;
import javax.swing.JFrame;

public class GameWindow extends JFrame {

    public GameWindow() {
        setTitle("Tetris");
        setSize(420, 805);
        setResizable(false);

        setLayout(new GridLayout(1, 2));

        GameBoardPanel gameBoard = new GameBoardPanel(this, 375);
        add(gameBoard);
        gameBoard.start();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
