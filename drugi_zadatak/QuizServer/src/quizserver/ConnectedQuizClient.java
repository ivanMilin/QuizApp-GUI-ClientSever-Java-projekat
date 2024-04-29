/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package quizserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author Ivan Milin
 */
public class ConnectedQuizClient implements Runnable{
 
    QuizServer parent;
    private Socket socket;
    private String userName;
    private BufferedReader br;
    private PrintWriter pw;
    private ArrayList<ConnectedQuizClient> allClients;

    private ArrayList<String> questionSet1;
    private ArrayList<String> questionSet2;
    private ArrayList<String> questionSet3;
    private ArrayList<String> questionSet4;
    
    private ArrayList<QuizMember> quizMembers;
    private ArrayList<QuizMember> presentMembers;
    
    public String getUserName()
    {
        return userName;
    }
    
    public void setUserName(String userName)
    {
        this.userName = userName;
    }
    
    public ConnectedQuizClient(Socket socket, ArrayList<ConnectedQuizClient> allClients, QuizServer parent)
    {
        this.socket = socket;
        this.allClients = allClients;
        this.parent = parent;
        
        questionSet1 = new ArrayList<>();
        questionSet2 = new ArrayList<>();
        questionSet3 = new ArrayList<>();
        questionSet4 = new ArrayList<>();
        quizMembers = new ArrayList<>();
        presentMembers = new ArrayList<>();
        
        loadQuestionAndAnswersFromFile(questionSet1, "./set1.txt");
        loadQuestionAndAnswersFromFile(questionSet2, "./set2.txt");
        loadQuestionAndAnswersFromFile(questionSet3, "./set3.txt");
        loadQuestionAndAnswersFromFile(questionSet4, "./set4.txt");
       
        try
        {
            this.br = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
            this.pw = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()), true);
            this.userName = "";
        }
        catch(IOException ex)
        {
            Logger.getLogger(ConnectedQuizClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String filePath_to_users = "./users.txt";

        try (BufferedReader players_from_file = new BufferedReader(new FileReader(filePath_to_users))) {
            String line;
            while ((line = players_from_file.readLine()) != null) 
            {
                String[] usernamePasswordRole = line.split(":");
                quizMembers.add(new QuizMember(usernamePasswordRole[0],usernamePasswordRole[1],usernamePasswordRole[2]));
            }
        } 
        catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Nisam uspeo da ucitam fajl 'users.txt' ");
        }  
        
        //Provera da li sam dobro ucitao fajl
        //for(QuizMember qm : quizMembers)
        //{
        //    System.out.println(qm);
        //}
    }
    
    void connectedClientsUpdateStatus()
    {
        String connectedUsers = "stariKOD_Users:";

        for(ConnectedQuizClient c : this.allClients)
        {
            connectedUsers += " " + c.getUserName();
        }
        
        for(ConnectedQuizClient svimaUpdateCB : this.allClients)
        {
            svimaUpdateCB.pw.println(connectedUsers);
        }
        System.out.println("Korisnici :" + connectedUsers); 
    }
    
    public void broadcastMessage(String message)
    {
        for(ConnectedQuizClient client : allClients)
        {
            client.sendMessage(message);
        }
    }
    
    public void sendMessage(String message)
    {
        pw.println(message);
    }
    @Override
    public void run()
    {
        while(true)
        {
            String line;
            
            try
            {
                line = this.br.readLine();
                
                if(line.startsWith("New user ="))
                {
                    String[] newUser = line.split("=");
                    System.out.println("New user = " + newUser[1].trim());
                }
                else if(line.startsWith("SendQuestionSetTo ="))
                {
                    String[] questionSetRequest = line.split("=");
                    System.out.println("SendQuestionSetTo =" + questionSetRequest[1].trim());
                    
                    String[] username_QuestionSet = (questionSetRequest[1].trim()).split(":");
                    int questionSet = Integer.parseInt(username_QuestionSet[1]);
                    
                    // definisi string poruka za slanje;
                    if(questionSet == 1)
                    {
                        System.out.println("questionSet == 1");
                        String porukaZaSlanje = "RecievingQuestionSet =" + username_QuestionSet[0] +"%";
                        porukaZaSlanje += concatenate_QuestionsAndAnswersFromSet_inOneString(questionSet1);
                        System.out.println(porukaZaSlanje);
                        broadcastMessage(porukaZaSlanje);
                        
                    }
                    else if(questionSet == 2)
                    {
                        System.out.println("questionSet == 2");
                        String porukaZaSlanje = "RecievingQuestionSet =" + username_QuestionSet[0] +"%";
                        porukaZaSlanje += concatenate_QuestionsAndAnswersFromSet_inOneString(questionSet2);
                        System.out.println(porukaZaSlanje);
                        broadcastMessage(porukaZaSlanje);
                    }
                    else if(questionSet == 3)
                    {
                        System.out.println("questionSet == 3");
                        String porukaZaSlanje = "RecievingQuestionSet =" + username_QuestionSet[0] +"%";
                        porukaZaSlanje += concatenate_QuestionsAndAnswersFromSet_inOneString(questionSet3);
                        System.out.println(porukaZaSlanje);
                        broadcastMessage(porukaZaSlanje);
                    }
                    else if(questionSet == 4)
                    {
                        System.out.println("questionSet == 4");
                        String porukaZaSlanje = "RecievingQuestionSet =" + username_QuestionSet[0] +"%";
                        porukaZaSlanje += concatenate_QuestionsAndAnswersFromSet_inOneString(questionSet4);
                        System.out.println(porukaZaSlanje);
                        broadcastMessage(porukaZaSlanje);
                    }  
                }
                else if(line.startsWith("UsersAfterRemove ="))
                {
                    String[] usersAfterRemove = line.split("=");
                    removeFile_createFile((usersAfterRemove[1].trim()).split(" "));
                    System.out.println("UsersAfterRemove =" + usersAfterRemove[1].trim());
                }
                else if(line.startsWith("UsersAfterAdding ="))
                {
                    String[] usersAfterAdding = line.split("=");
                    removeFile_createFile((usersAfterAdding[1].trim()).split(" "));
                    System.out.println("UsersAfterAdding =" + usersAfterAdding[1].trim());
                }
                else if(line.startsWith("HelpMeFriend ="))
                {
                    //HelpMeFried =ivan:admin|1. Ukoliko za nekoga ka�emo da ima demenciju, on je:
                    String porukaZaSlanje = line;
                    broadcastMessage(porukaZaSlanje);
                }
                else if(line.startsWith("AnswerForFriend ="))
                {
                    //AnswerForFriend =jovan|ivan:zaboravan
                    String porukaZaSlanje = line;
                    broadcastMessage(line);
                }
                else if(line.startsWith("Login ="))
                {
                    String[] string = line.split("=");
                    String[] usernamePasswordRole = string[1].split(":");

                    String username = usernamePasswordRole[0];
                    String password = usernamePasswordRole[1];
                    String role = usernamePasswordRole[2];
                    
                    checkLoginFormat(quizMembers, pw, username, password, role);
                    
                }
                else if(line.startsWith("ActiveUsers ="))
                {
                    String porukaZaSlanje = "ActiveUsers =";
                    for(String qm : parent.getPresentMembers())
                    {
                        porukaZaSlanje += qm + ":";
                    }
                    System.out.println(porukaZaSlanje);
                    broadcastMessage(porukaZaSlanje);
                }
            }
            catch(IOException ex)
            {
                //System.out.println("Disconnected user: " + this.userName);
            }
        }
    }
    
    // Ova metoda se poziva kad admin hoce da doda ili obrise korisnika iz fajla
    public static void removeFile_createFile(String[] usersAfterRemove)
    {
        File existingFile = new File("users.txt");
        
        if(existingFile.exists())
        {
            existingFile.delete();
        }
        
        File newFile = new File("users.txt");
        try(FileWriter writer = new FileWriter(newFile))
        {
            for(String line : usersAfterRemove)
            {
                writer.write(line);
                writer.write(System.lineSeparator());
            }
            System.out.println("Podaci upisani u fajl " + newFile);
        }
        catch(IOException ex)
        {
            System.out.println("An error occurred while creating file users.txt");
            ex.printStackTrace();
        }
    }
     
    public static void loadQuestionAndAnswersFromFile(ArrayList<String> set, String filePath) 
    {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) 
        {
            String questionAndAnswers = "";

            String line;
            while ((line = br.readLine()) != null) 
            {
                if ((line.trim()).matches("^\\d+\\..*")) 
                {
                    if (!questionAndAnswers.isEmpty()) {
                        set.add(questionAndAnswers); 
                        questionAndAnswers = "";
                    }
                    questionAndAnswers += line.trim()+";";
                } 
                else if ((line.trim()).matches("^[ \t]*[a-c]\\)\\s+.*")) 
                {
                    String answerText = line.substring(line.indexOf(")") + 1).trim();
                    questionAndAnswers += answerText + ",false" + "|";
                }
                else if ((line.trim()).matches("^[ \t]*d\\)\\s+.*")) 
                {
                    String answerText = line.substring(line.indexOf(")") + 1).trim();
                    questionAndAnswers += answerText + ",true" + "#";
                }
            }

            if (!questionAndAnswers.isEmpty()) 
            {
                set.add(questionAndAnswers);
            }
            
            /*
            System.out.println("Loaded questions from file:");
            for (String item : set) 
            {
                System.out.println(item);
            }
            System.out.println("=============================");
            */
        } 
        catch (IOException ex) 
        {
            ex.printStackTrace();
            System.out.println("Problem pri radu metode 'loadQuestionAndAnswersFromFile'");
        }
    }

    public static String concatenate_QuestionsAndAnswersFromSet_inOneString(ArrayList<String> set)
    {
        StringBuilder builder = new StringBuilder();
        for(String s : set)
        {
            builder.append(s);
        }
        return builder.toString();
    }
    
    public void checkLoginFormat(ArrayList<QuizMember> quizMembers, PrintWriter pw, String username, String password, String role)
    {
        if(!username.matches("^\\d.*") && username.matches("^[a-zA-Z0-9]*$") && 
            password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,}$") && 
            role.matches("^(admin|contestant)$"))
        {
            for(QuizMember qm : quizMembers)
            {   
                if(qm.getUsername().equals(username) && qm.getPassword().equals(password) && qm.getRole().equals(role))
                {
                    String porukaZaSlanje = "LoginApproved =";
                    this.pw.println(porukaZaSlanje);
                    parent.addPresentUsers(qm.getUsername());
                    break;
                }
                else
                {
                    String porukaZaSlanje = "NotLoginApproved =";
                    this.pw.println(porukaZaSlanje);
                }
            }
        }
        else
        {   
            String porukaZaSlanje = "WrongLoginFormat =";
            this.pw.println(porukaZaSlanje);
        }
    }
}