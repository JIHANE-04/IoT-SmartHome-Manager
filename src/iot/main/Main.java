package iot.main;

import iot.view.DashboardIoT;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DashboardIoT().setVisible(true);
        });
    }
}
