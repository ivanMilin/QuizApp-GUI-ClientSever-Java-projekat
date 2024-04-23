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
    
    private String username;
    private String password;
    private String role;
    private int points;

    public QuizMemberClient(String username, String password, String role, int points) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.points = points;
    }

    public String getUserName() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public int getPoints() {
        return points;
    }
    
    @Override
    public String toString(){
            return "    " + username + " " + password + " " + role + " " + points;

    }
}