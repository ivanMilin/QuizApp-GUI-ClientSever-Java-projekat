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
    BufferedReader br;
    ArrayList<QuizMemberClient> loadedClientsFromFile;
    
    public RecieveMessageFromServer(QuizClient parent)
    {
        this.parent = parent;
        this.br     = parent.getBr();
        this.loadedClientsFromFile = new ArrayList<>();
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
                    
                    System.out.println("Trenutno prisutni : ");
                    
                    for(QuizMemberClient member : loadedClientsFromFile)
                    {
                        System.out.println(member);
                    }
                    //System.out.println(line);
                     parent.updateActiveQuizMembers(loadedClientsFromFile);
                }
            }
            catch(IOException ex)
            {
                Logger.getLogger(RecieveMessageFromServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
