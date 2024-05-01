/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package quizserver;

/**
 *
 * @author Ivan Milin
 */
public class QuizMember {
    
    private String username;
    private String password;
    private String role;
    private int points;
    private int answeredSets;

    public QuizMember(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.points = 0;
    }

    public String getUsername() {
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

    public int getAnsweredSets() {
        return answeredSets;
    }
    
    public void setPoints(int points) {
        this.points = points;
    }

    public void setAnsweredSets(int answeredSets) {
        this.answeredSets = answeredSets;
    }
    
    public String toString(){
            return "    " + username + ":" + password + ":" + role;
    }
}
