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

    public QuizMember(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
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
    
    public String toString(){
            return "    " + username + ":" + password + ":" + role;
    }
}
