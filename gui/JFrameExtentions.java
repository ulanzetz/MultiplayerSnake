package com.snakegame.gui;

import javax.swing.*;
import java.awt.*;

public class JFrameExtentions {
    public static void SetLocationToCenter(JFrame frame) {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width / 2 - frame.getSize().width / 2,
                dim.height / 2 - frame.getSize().height / 2);
    }

    public static void infoBox(String infoMessage, String titleBar) {
        JOptionPane.showMessageDialog(null, infoMessage, titleBar, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void closeInfoBox(String infoMessage, String titleBar) {
        JOptionPane.showMessageDialog(null, infoMessage, titleBar, JOptionPane.OK_OPTION);
        System.exit(0);
    }
}
