/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package quizserver;

import java.io.BufferedReader;
import java.io.FileReader;
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
            try
            {
                if(this.userName.equals(""))
                {
                    this.userName = this.br.readLine();
                    if(this.userName != null)
                    {
                        System.out.println("Connected :" + this.userName);
                        connectedClientsUpdateStatus();
                    }
                    else
                    {
                        System.out.println("Disconnected user: " + this.userName);
                        for(ConnectedQuizClient cl : this.allClients)
                        {
                            if(cl.getUserName().equals(this.userName))
                            {
                                this.allClients.remove(cl);
                                break;
                            }
                            connectedClientsUpdateStatus();
                            break;
                        }
                    }
                }
                else
                {
                    System.out.println("Cekam poruku");
                    String line = this.br.readLine();
                    System.out.println(line);
                    System.out.println("Stigla poruka");
                    
                    if(line != null)
                    {
                        String[] informacija = line.split(": ");
                        String primacKorisnik = informacija[0].trim();
                        System.out.println(primacKorisnik);
                        String poruka = informacija[1];
                        System.out.println(poruka);
                        
                        for(ConnectedQuizClient clnt : this.allClients)
                        {
                            if(clnt.getUserName().equals(primacKorisnik))
                            {
                                System.out.println(clnt.getUserName());
                                clnt.pw.println(this.userName + ": " + poruka);
                                System.out.println(primacKorisnik + ": " + poruka);
                            }
                            else
                            {
                                if(primacKorisnik.equals(""))
                                {
                                    this.pw.println("Korisnik " + primacKorisnik + " je odsutan!");
                                }
                            }
                        }
                    }
                    else
                    {
                        System.out.println("Disconnected user: " + this.userName);
                        
                        Iterator<ConnectedQuizClient> it = this.allClients.iterator();
                        
                        while(it.hasNext())
                        {
                            if(it.next().getUserName().equals(this.userName))
                            {
                                it.remove();
                            }
                        }
                        connectedClientsUpdateStatus();
                        
                        this.socket.close();
                        break;
                    }
                } 
            }
            catch(IOException ex)
            {
                System.out.println("Disconnected user: " + this.userName);
                
                for(ConnectedQuizClient cl : this.allClients)
                {
                    if(cl.getUserName().equals(this.userName))
                    {
                        this.allClients.remove(cl);
                        connectedClientsUpdateStatus();
                        return;
                    }
                }
            }
        }
    }
}
