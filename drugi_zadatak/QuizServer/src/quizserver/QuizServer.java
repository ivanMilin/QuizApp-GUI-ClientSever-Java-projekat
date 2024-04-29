/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package quizserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Ivan Milin
 */
public class QuizServer {

    private ServerSocket ssocket;
    private int port;
    private ArrayList<ConnectedQuizClient> clients;
    private ArrayList<String> presentMembers;
    
    public ServerSocket getSsocket()
    {
        return ssocket;
    }
    
    public void setSsocket(ServerSocket ssocket)
    {
        this.ssocket = ssocket;
    }
    
    public int getPort()
    {
        return port;
    }
    
    public void setPort(int port)
    {
        this.port = port;
    }
    
    public void addPresentUsers(String user)
    {
        presentMembers.add(user);
    }

    public ArrayList<String> getPresentMembers() {
        return presentMembers;
    }
    
    public void acceptClients()
    {
        Socket client = null;
        Thread thr;
        while(true)
        {
            try
            {
                System.out.println("Waiting for new clients");
                client = this.ssocket.accept();
            }
            catch(IOException ex)
            {
                Logger.getLogger(QuizServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if(client != null)
            {
                ConnectedQuizClient clnt = new ConnectedQuizClient(client, clients,this);
                clients.add(clnt) ;
                thr = new Thread(clnt);
                thr.start();
            }
            else
            {
                return;
            }
        }
    }
    
    public QuizServer(int port)
    {
        this.clients = new ArrayList<>();
        this.presentMembers = new ArrayList<>();
        
        try
        {
            this.port = port;
            this.ssocket = new ServerSocket(port);
        }
        catch(IOException ex)
        {
            Logger.getLogger(QuizServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        QuizServer server = new QuizServer(6001);
        
        System.out.println("Server pokrenut, slusam na portu 6001");
        server.acceptClients();
    }    

}
