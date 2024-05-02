/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package quizserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import java.util.HashMap;
import java.util.Map;

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
                else if(line.startsWith("RemoveUserFromFile ="))
                {
                    String[] string = line.split("=");
                    String removeUser = string[1];
                    removeUserFromFile(removeUser, "./users.txt");

                }
                else if(line.startsWith("AddNewUser ="))
                {
                    String[] string = line.split("=");
                    String addNewUser = string[1];
                    addUserToFile(addNewUser,"./users.txt");
                    
                    String[] user = addNewUser.split(":");
                    if(user[2].equals("contestant"))
                    {
                        String usernamePoints = user[0] + ":0/0";
                        addUserToFile(usernamePoints,"./scoreboard.txt");
                    }
                    
                }
                else if(line.startsWith("CheckFormat ="))
                {
                    String[] string = line.split("=");
                    String[] usernamePassword = string[1].split(":");

                    voidCheckFormatForNewMembers(usernamePassword[0],usernamePassword[1]);

                }
                else if (line.startsWith("IncrementPoints =")) 
                {
                    final String[] string = line.split("=");
                    final String[] username_numberOfPoints = string[1].split(":");
                    final String username = username_numberOfPoints[0];
                    final int newPoints = Integer.parseInt(username_numberOfPoints[1]);
                    final int answeredSets = Integer.parseInt(username_numberOfPoints[2]);

                    // Load existing scoreboard data into a map
                    Map<String, String> scoreboardData = loadScoreboardData();

                    // Update points for the specified user
                    String userData = scoreboardData.getOrDefault(username, "0/0");
                    String[] userPoints = userData.split("/");
                    int currentPoints = Integer.parseInt(userPoints[0]);
                    int currentAnsweredSets = Integer.parseInt(userPoints[1]);

                    // Increment points and answered sets
                    currentPoints = newPoints;
                    currentAnsweredSets = answeredSets;

                    // Update scoreboard data
                    scoreboardData.put(username, currentPoints + "/" + currentAnsweredSets);

                    // Rewrite the scoreboard file
                    rewriteScoreboardFile(scoreboardData);
                    sendPointsToScoreboard("./scoreboard.txt");
                }
            }
            catch(IOException ex)
            {
                //System.out.println("Disconnected user: " + this.userName);
            }
        }
    }
    
    // ===================================================================================
    public void broadcastMessage(String message)
    {
        for(ConnectedQuizClient client : allClients)
        {
            client.sendMessage(message);
        }
    }
    // ===================================================================================
    public static void removeUserFromFile(String usernameToRemove, String filePath) {
        // Read the file and load its content into an ArrayList
        ArrayList<String> usersFromFile = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) 
        {
            String line;
            while ((line = reader.readLine()) != null) 
            {
                usersFromFile.add(line);
            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            return;
        }

        usersFromFile.removeIf(user -> user.startsWith(usernameToRemove + ":"));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) 
        {
            for (String user : usersFromFile)
            {
                writer.write(user);
                writer.newLine();
            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }

        System.out.println("User '" + usernameToRemove + "' removed from the file.");
    }
    // ===================================================================================
    public static void addUserToFile(String newUser, String filePath) {
        // Read the file and load its content into an ArrayList
        ArrayList<String> usersFromFile = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                usersFromFile.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Add the new user to the end of the ArrayList
        usersFromFile.add(newUser);

        // Write the updated ArrayList back to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String user : usersFromFile) {
                writer.write(user);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("New user added to the file : " + newUser);
    }
    // ===================================================================================
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
        } 
        catch (IOException ex) 
        {
            ex.printStackTrace();
            System.out.println("Problem pri radu metode 'loadQuestionAndAnswersFromFile'");
        }
    }
    // ===================================================================================
    public static String concatenate_QuestionsAndAnswersFromSet_inOneString(ArrayList<String> set)
    {
        StringBuilder builder = new StringBuilder();
        for(String s : set)
        {
            builder.append(s);
        }
        return builder.toString();
    }
    // ===================================================================================
    public void checkLoginFormat(ArrayList<QuizMember> quizMembers, PrintWriter pw, String username, String password, String role)
    {
        if(!username.matches("^\\d.*") && username.matches("^[a-zA-Z0-9]*$") && 
            password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,}$") && 
            role.matches("^(admin|contestant)$"))
        {
            String filePath_to_users = "./users.txt";
            
            try (BufferedReader players_from_file = new BufferedReader(new FileReader(filePath_to_users))) {
                String file_line;
                while ((file_line = players_from_file.readLine()) != null) 
                {
                    String[] usernamePasswordRole_line = file_line.split(":");
                    quizMembers.add(new QuizMember(usernamePasswordRole_line[0],usernamePasswordRole_line[1],usernamePasswordRole_line[2]));
                }
            } 
            catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("Nisam uspeo da ucitam fajl 'users.txt' ");
            } 
            
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
    // ===================================================================================
    public void voidCheckFormatForNewMembers(String username, String password)
    {

        if(!username.matches("^\\d.*") && username.matches("^[a-zA-Z0-9]*$") && 
            password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,}$"))
        {
            this.pw.println("ApprovedFormat =");
        }
        else
        {
            this.pw.println("NotApprovedFormat =");
        }
    }
    // ===================================================================================
    private Map<String, String> loadScoreboardData() {
        Map<String, String> scoreboardData = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("./scoreboard.txt"))) 
        {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                scoreboardData.put(parts[0], parts[1]);
            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        return scoreboardData;
    }
    // ===================================================================================
    private void rewriteScoreboardFile(Map<String, String> scoreboardData) 
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("./scoreboard.txt"))) 
        {
            for (Map.Entry<String, String> entry : scoreboardData.entrySet()) 
            {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    private void sendPointsToScoreboard(String filename)
    {
        String porukaZaSlanje = "UpdateScoreboard =";
        
        try(BufferedReader reader = new BufferedReader(new FileReader(filename)))
        {
            String line;
            while((line = reader.readLine()) != null)
            {
                porukaZaSlanje += line + "#";
            }
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        
        broadcastMessage(porukaZaSlanje);
    }
}