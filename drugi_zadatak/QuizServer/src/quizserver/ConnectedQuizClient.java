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
import java.util.Iterator;

/**
 *
 * @author Ivan Milin
 */
public class ConnectedQuizClient implements Runnable{
 
    private Socket socket;
    private String userName;
    private BufferedReader br;
    private PrintWriter pw;
    private ArrayList<ConnectedQuizClient> allClients;
    
    public String getUserName()
    {
        return userName;
    }
    
    public void setUserName(String userName)
    {
        this.userName = userName;
    }
    
    public ConnectedQuizClient(Socket socket, ArrayList<ConnectedQuizClient> allClients)
    {
        this.socket = socket;
        this.allClients = allClients;
        
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
        ArrayList<String> dataFrom_userTxt = new ArrayList<>();

        try (BufferedReader players_from_file = new BufferedReader(new FileReader(filePath_to_users))) {
            String line;
            while ((line = players_from_file.readLine()) != null) {
                dataFrom_userTxt.add(line);
                dataFrom_userTxt.add(",");

            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Nisam uspeo da ucitam fajl 'users.txt' ");
        }

        StringBuilder oneString_dataFrom_userTxt = new StringBuilder();
        for (String user : dataFrom_userTxt) {
            oneString_dataFrom_userTxt.append(user);
        }

        // Remove the trailing newline character if needed
        if (oneString_dataFrom_userTxt.length() > 0) {
            oneString_dataFrom_userTxt.setLength(oneString_dataFrom_userTxt.length() - 1);
        }

        String porukaZaSlanje = "Users =" + oneString_dataFrom_userTxt.toString();
        System.out.println("iz fajla " + porukaZaSlanje);
        this.pw.println(porukaZaSlanje);
        
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
        System.out.println(connectedUsers); 
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
}
