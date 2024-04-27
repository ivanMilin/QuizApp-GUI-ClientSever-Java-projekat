/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package quizserver;

/**
 *
 * @author Ivan Milin
 */
public class QuestionAndAnswers {
    
    private String question;
    private String answers;

    public QuestionAndAnswers(String question, String answers) {
        this.question = question;
        this.answers = answers;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswers() {
        return answers;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setAnswers(String answers) {
        this.answers = answers;
    }
    
    @Override
    public String toString()
    {
        return question + answers;
    }
}
