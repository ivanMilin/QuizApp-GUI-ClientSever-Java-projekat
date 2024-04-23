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
    
    public RecieveMessageFromServer(QuizClient parent)
    {
        this.parent = parent;
        this.br     = parent.getBr();
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
                
                    /*
                        OVO NE MOZE OVAKO, MORAS DRUGACIJE DA SMISLIS 
                        MORAS SVE PRISUTNE DA PRIMIS I ONDA DA SVAKI SPLITUJES
                    */
                    
                    String[] dataFromServer = line.split("=");
                    String[] username_password_role_points = dataFromServer[1].split(",");
                    //String[] username_password_role_points = line.split(":");
                    
                    for(String user : username_password_role_points)
                    {
                        System.out.println(user);
                    }
                  
                    
                    //System.out.println(line);
                }
            }
            catch(IOException ex)
            {
                Logger.getLogger(RecieveMessageFromServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        }
    }
}
