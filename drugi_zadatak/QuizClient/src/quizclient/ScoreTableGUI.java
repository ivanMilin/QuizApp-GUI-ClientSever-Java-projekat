/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package quizclient;

/**
 *
 * @author Ivan Milin
 */
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ScoreTableGUI extends JFrame 
{
    private JTable scoreTable;

    public ScoreTableGUI(Object[][] data, String[] columnNames) 
    {
        super("Score Table");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Create a table model
        DefaultTableModel model = new DefaultTableModel(data, columnNames);

        // Create the JTable
        scoreTable = new JTable(model);

        // Add the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(scoreTable);
        getContentPane().add(scrollPane);

        // Set frame size and visibility
        setSize(400, 300);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
