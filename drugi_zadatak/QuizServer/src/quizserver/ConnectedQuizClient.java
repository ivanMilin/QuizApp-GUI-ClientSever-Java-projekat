/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package quizserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import static quizserver.QuizServer.createInitializationVector;

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
    
    private static final String AES = "AES";
    private static final String AES_CIPHER_ALGORITHM = "AES/CBC/PKCS5PADDING";
    private static Scanner message;
    
    private SecretKey symmetricKey = null;                
    private byte[] initializationVector;  

    public String getUserName()
    {
        return userName;
    }
    
    public void setUserName(String userName)
    {
        this.userName = userName;
    }
    
    public ConnectedQuizClient(Socket socket, ArrayList<ConnectedQuizClient> allClients, QuizServer parent) throws Exception
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
        
        this.symmetricKey = parent.getSymmetricKey();                
        this.initializationVector = parent.getInitializationVector();
        
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
        //saveEncyptedAdmin();
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
                
                if(line.startsWith("SendQuestionSetTo ="))
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
                    removeUserFromEncyptedFile(removeUser, "./users_encrypted.txt");

                }
                else if(line.startsWith("AddNewUser ="))
                {
                    String[] string = line.split("=");
                    String addNewUser = string[1];
                    addUserToFile(addNewUser,"./users.txt");
                    addUserToEncrpytedFile(addNewUser,"./users_encrypted.txt");
                    
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
                else if(line.startsWith("MemberLeft ="))
                {
                    String[] string = line.split("=");
                    String removeMember = string[1];

                    for(String qm : parent.getPresentMembers())
                    {
                        parent.getPresentMembers().remove(removeMember);
                    }
                    
                    String porukaZaSlanje = "ActiveUsers =";
                    for(String qm : parent.getPresentMembers())
                    {
                        porukaZaSlanje += qm + ":";
                    }
                    System.out.println(porukaZaSlanje);
                    broadcastMessage(porukaZaSlanje);
                    this.socket.close();
                    break;
                }
                else if(line.startsWith("SetPoints ="))
                {
                    setPointsIfMemberHasAlready();
                }
            }
            catch(IOException ex)
            {
                //ex.printStackTrace();
            }
        }
    }
    //=> Enkripcija
    public static byte[] do_AESEncryption(String plainText, SecretKey secretKey, byte[] initializationVector) throws Exception 
    {
        Cipher cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(initializationVector);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
        return cipher.doFinal(plainText.getBytes());
    }
    
    public static String do_AESDecryption(byte[] cipherText, SecretKey secretKey, byte[] initializationVector) throws Exception 
    {
        Cipher cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(initializationVector);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

        byte[] result = cipher.doFinal(cipherText);

        return new String(result);
    }
    //<= Enkripcija
    
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
    public static void addUserToFile(String newUser, String filePath) 
    {
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

        usersFromFile.add(newUser);

        // Write the updated ArrayList back to the file
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
            String filePath_to_users = "./users_encrypted.txt";
            
            try(DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath_to_users)))) 
            {
                byte[] encryptedData = new byte[dis.available()];
                dis.readFully(encryptedData);
                String decryptedData = do_AESDecryption(encryptedData, symmetricKey, initializationVector);
                System.out.println("checkLoginFormat : " + decryptedData);
                String[] allUsersData = decryptedData.split("\n");

                for(String file_line : allUsersData) 
                {
                    String[] usernamePasswordRole_line = (file_line.trim()).split(":");
                    quizMembers.add(new QuizMember(usernamePasswordRole_line[0],usernamePasswordRole_line[1],usernamePasswordRole_line[2]));
                }
            } 
            catch (Exception ex) 
            {
                ex.printStackTrace();
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
        quizMembers.clear();
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
    // ===================================================================================
    private void sendPointsToScoreboard(String filename)
    {
        String porukaZaSlanje = "UpdateScoreboard =";
        ArrayList<String> sortedArray = new ArrayList<>();
        
        try(BufferedReader reader = new BufferedReader(new FileReader(filename)))
        {
            String line;
            
            while((line = reader.readLine()) != null)
            {
                sortedArray.add(line);
                //porukaZaSlanje += line + "#";
            }
            
            Collections.sort(sortedArray, new Comparator<String>(){
            @Override
            public int compare(String s1, String s2)
            {
                int num1 = Integer.parseInt(s1.split(":")[1].split("/")[0]);
                int num2 = Integer.parseInt(s2.split(":")[1].split("/")[0]);
                return Integer.compare(num2, num1);
            }
            });
            
            for(String s: sortedArray)
            {    
                porukaZaSlanje += s + "#";
            }
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        
        broadcastMessage(porukaZaSlanje);
    }
    // ===================================================================================
    public void setPointsIfMemberHasAlready()
    {
        String porukaZaSlanje = "SetPoints =";
        try(BufferedReader reader = new BufferedReader(new FileReader("./scoreboard.txt")))
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
        System.out.println(porukaZaSlanje);
        this.pw.println(porukaZaSlanje);
    }
    // ===================================================================================
    public void saveEncyptedAdmin() throws Exception
    {
        try
        {
            String admin_encrypted = "admin1:Adminn#1:admin" + "\n";
            byte[] encyptedData = do_AESEncryption(admin_encrypted, symmetricKey, initializationVector);
            
            try(DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("./users_encrypted.txt"))))
            {
                dos.write(encyptedData);
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
            }
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }        
    }

    public void addUserToEncrpytedFile(String addNewUser, String fileName)
    {
        String[] linesInFile;
        
        ArrayList<String> loadedUsersFromEncrypted = new ArrayList<>();
        loadedUsersFromEncrypted.clear();
        
        try(DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(fileName)))) 
        {
            byte[] encryptedData = new byte[dis.available()];
            dis.readFully(encryptedData);
            String decryptedData = do_AESDecryption(encryptedData, symmetricKey, initializationVector);
            String[] allUsersData = decryptedData.split("\n");

            for(String user : allUsersData)
            {
                loadedUsersFromEncrypted.add(user);
            }
            loadedUsersFromEncrypted.add(addNewUser);

            String allUsersInOneString = "";

            for(String user : loadedUsersFromEncrypted)
            {
                allUsersInOneString += user + "\n";
            }
            System.out.println("After adding in addUserToEncrpytedFile : " + allUsersInOneString);
            
            
            byte[] newEncryptedData = do_AESEncryption(allUsersInOneString, parent.getSymmetricKey(), parent.getInitializationVector()); 

            try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)))) 
            {
                dos.write(newEncryptedData);
            } 
            catch (IOException ex) 
            {
                ex.printStackTrace();
            }
        } 
        catch (Exception ex) 
        {
            ex.printStackTrace();
        }
    }

    public void removeUserFromEncyptedFile(String userToRemoveFromEncrypted, String fileName)
    {
        ArrayList<String> loadedUsersFromEncrypted = new ArrayList<>();
        loadedUsersFromEncrypted.clear();
        
        try(DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(fileName)))) 
        {
            byte[] encryptedData = new byte[dis.available()];
            dis.readFully(encryptedData);
            String decryptedData = do_AESDecryption(encryptedData, symmetricKey, initializationVector);
            String[] allUsersData = decryptedData.split("\n");

            for(String user : allUsersData)
            {
                loadedUsersFromEncrypted.add(user);
            }

            loadedUsersFromEncrypted.removeIf(user -> user.startsWith(userToRemoveFromEncrypted + ":"));

            String allUsersInOneString = "";

            System.out.println("After removing in encripted : ");
            for(String user : loadedUsersFromEncrypted)
            {
                System.out.print(user);
                allUsersInOneString += user + "\n";
            }
            System.out.println("After encription : \n" + allUsersInOneString);

            byte[] newEncryptedData = do_AESEncryption(allUsersInOneString, parent.getSymmetricKey(), parent.getInitializationVector()); 

            try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)))) 
            {
                dos.write(newEncryptedData);
            } 
            catch (IOException ex) 
            {
                ex.printStackTrace();
            }
        } 
        catch (Exception ex) 
        {
            ex.printStackTrace();
        }
    }
}