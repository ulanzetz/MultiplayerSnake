package com.snakegame.gui;

import com.snakegame.model.GameMode;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StartForm extends JFrame {

    private JTextField nameBox;
    private JTextField widthBox;
    private JTextField heightBox;
    private JTextField delayBox;
    private JTextField modeBox;

    public StartForm() {
        setTitle("Choose settings");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 300);
        setResizable(false);
        JFrameExtentions.SetLocationToCenter(this);
        setVisible(true);
        JPanel panel = new JPanel(null);
        add(panel);

        JLabel nameLabel = new JLabel("Name:");
        panel.add(nameLabel);
        nameLabel.setLocation(5, 10);
        nameLabel.setSize(50, 30);

        nameBox = new JTextField("Player");
        panel.add(nameBox);
        nameBox.setLocation(85, 10);
        nameBox.setSize(80, 30);

        JLabel sizeLabel = new JLabel("Field Size:");
        panel.add(sizeLabel);
        sizeLabel.setLocation(5, 50);
        sizeLabel.setSize(80, 30);

        widthBox = new JTextField("20");
        panel.add(widthBox);
        widthBox.setLocation(85, 50);
        widthBox.setSize(30, 30);

        heightBox = new JTextField("20");
        panel.add(heightBox);
        heightBox.setLocation(125, 50);
        heightBox.setSize(30, 30);

        JLabel delayLabel = new JLabel("Delay:");
        panel.add(delayLabel);
        delayLabel.setLocation(5, 90);
        delayLabel.setSize(50, 30);

        delayBox = new JTextField("100");
        panel.add(delayBox);
        delayBox.setLocation(85, 90);
        delayBox.setSize(30, 30);

        JLabel modeLabel = new JLabel("Game Mode:");
        panel.add(modeLabel);
        modeLabel.setLocation(5, 120);
        modeLabel.setSize(80, 30);

        modeBox = new JTextField("classic");
        panel.add(modeBox);
        modeBox.setLocation(85, 120);
        modeBox.setSize(80, 30);


        JButton startButton = new JButton("Start");
        panel.add(startButton);
        startButton.setLocation(200, 230);
        startButton.setSize(80, 30);


        startButton.addActionListener(e -> {
            try {
                int width = Integer.parseInt(widthBox.getText());
                int height = Integer.parseInt(heightBox.getText());
                int delay = Integer.parseInt(delayBox.getText());
                String modeName = modeBox.getText().toLowerCase();
                GameMode.loadGameMods();
                GameMode mode = GameMode.gameMods.get(modeName);
                if(width < 3 || height < 3) throw new IllegalArgumentException();

                GameForm form = new GameForm();
                form.setSize(width * 30 + 20, height * 30 + 30);
                JFrameExtentions.SetLocationToCenter(form);
                Panel panel1 = new Panel(width, height, delay, mode, null);
                form.add(panel1);
                setVisible(false);
                dispose();
            } catch (Exception exp) {
                JFrameExtentions.infoBox("Incorrect format of data", "Error");
            }
        });

    }

    public static void main(String[] args) {
        new StartForm();
    }
}
