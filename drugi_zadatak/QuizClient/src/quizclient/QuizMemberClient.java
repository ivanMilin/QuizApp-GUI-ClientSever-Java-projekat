/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package quizclient;

/**
 *
 * @author Ivan Milin
 */
public class QuizMemberClient {
    
    private String userName;
    private String password;
    private String role;
    private int points;

    public QuizMemberClient(String userName, String password, String role, int points) {
        this.userName = userName;
        this.password = password;
        this.role = role;
        this.points = points;
    }
    
    
}
