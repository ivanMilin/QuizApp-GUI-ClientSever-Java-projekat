package quizclient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ScoreTableGUI extends JFrame {
    private JTable scoreTable;

    public ScoreTableGUI(Object[][] data, String[] columnNames) {
        super("Scoreboard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create a table model
        DefaultTableModel model = new DefaultTableModel(data, columnNames);

        scoreTable = new JTable(model);

        Font font = scoreTable.getFont().deriveFont(Font.PLAIN, 14f);
        scoreTable.setFont(font);

        // Add the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(scoreTable);
        getContentPane().add(scrollPane);

        // Set frame size and visibility
        setSize(400, 300);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
