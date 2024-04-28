/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package quizclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ivan Milin
 */
public class RecieveMessageFromServer implements Runnable{
    
    QuizClient parent;
    ContestantGUI contestantGUII;
    BufferedReader br;
    ArrayList<QuizMemberClient> loadedClientsFromFile;
    
    public RecieveMessageFromServer(QuizClient parent)
    {
        this.contestantGUII = new ContestantGUI(parent); 
        this.parent = parent;
        this.br     = parent.getBr();
        this.loadedClientsFromFile = new ArrayList<>();
        this.contestantGUII = null;
    }

    public void setContestantGUII(ContestantGUI contestantGUII) {
        this.contestantGUII = contestantGUII;
    }

    @Override
    public void run()
    {
        /*
            OVDE BI TREBALO DA PRIMIS OD SERVERA KOJI SU SVE UCITANI
            KORISNICI IZ FAJLA users.txt
        */
        while(true)
        {
            String line;
            try
            {
                line = this.br.readLine();
                
                if(line.startsWith("Users ="))
                {
                    String[] dataFromServer = line.split("=");
                    String[] username_password_role_points = dataFromServer[1].split(",");
                    
                    for (String user : username_password_role_points) 
                    {
                        String[] userDataParts = user.split(":");
                        
                        if (userDataParts.length == 3) 
                        {
                            //int points = Integer.parseInt(userDataParts[3].trim());
                            
                            QuizMemberClient client = new QuizMemberClient(userDataParts[0].trim(), // Username
                                                                           userDataParts[1].trim(), // Password
                                                                           userDataParts[2].trim()); // Role
                                                                           //points); // Points
                            loadedClientsFromFile.add(client);
                        } 
                        else 
                        {
                            System.out.println("Invalid user data format: " + user);
                        }
                    }
                    
                    int a = 5;
                    System.out.println("Trenutno prisutni : ");
                    
                    for(QuizMemberClient member : loadedClientsFromFile)
                    {
                        System.out.println(member);
                    }
                    
                    //System.out.println(line);
                    parent.updateActiveQuizMembers(loadedClientsFromFile);
                     
                }
                else if(line.startsWith("RecievingQuestionSet ="))
                {
                    String[] beforeUsername_username_QuestionsAndAnswers = (line.split("="));
                    String username_QuestionsAndAnswers = beforeUsername_username_QuestionsAndAnswers[1];
                    String[] parts = username_QuestionsAndAnswers.split("%");
                    String username = parts[0];
                    
                    if(username.equals(parent.getUsernameFromTextField()))
                    {
                        String[] qa_parts  = parts[1].split("#");
                        ArrayList<String> questionAndAnswers = new ArrayList<>();

                        for(String qa : qa_parts)
                        {
                            questionAndAnswers.add(qa);
                        }
                        
                        parent.setQuestionAndAnswers(questionAndAnswers);

                        // Print the results
                        /*
                        System.out.println("username = " + username);
                        System.out.println("questionsAndAnswers:");
                        for (String qa : questionAndAnswers) {
                            System.out.println(qa);
                        }
                        */
                    }
                }
                else if(line.startsWith("HelpMeFriend ="))
                {
                    //HelpMeFried =ivan:admin|1. Ukoliko za nekoga ka�emo da ima demenciju, on je:
                    String[] string = line.split("=");
                    String[] fromWho_toWho_questionForFriend = string[1].split(":");
                    
                    String fromWho = fromWho_toWho_questionForFriend[0];
                    String[] toWho_questionForFriend = fromWho_toWho_questionForFriend[1].split("\\|");
                    String toWho = toWho_questionForFriend[0];
                    String questionForFriend = toWho_questionForFriend[1];
                    
                    if(parent.getUsernameFromTextField().equals(toWho))
                    {
                        System.out.println(string[1]);
                        contestantGUII.setjTextField_helpMeFriend(fromWho + ":" + questionForFriend);
                    }
                }
                else if(line.startsWith("AnswerForFriend ="))
                {
                    //AnswerForFriend = jovan|ivan:zaboravan
                    String[] string = line.split("=");
                    String[] toWho_fromWho_answer = string[1].split("\\|");

                    String toWho = toWho_fromWho_answer[0];
                    String[] fromWho_answer = toWho_fromWho_answer[1].split(":");
                    String fromWho = fromWho_answer[0];
                    String answer = fromWho_answer[1];
                    
                    if(parent.getUsernameFromTextField().equals(toWho))
                    {
                        System.out.println(string[1]);
                        contestantGUII.setjTextField_helpMeFriend(fromWho + ":" + answer);
                    }
                }
                
            }
            catch(IOException ex)
            {
                Logger.getLogger(RecieveMessageFromServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
