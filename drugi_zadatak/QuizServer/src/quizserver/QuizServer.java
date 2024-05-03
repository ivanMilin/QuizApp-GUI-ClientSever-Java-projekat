/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package quizserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;


/**
 *
 * @author Ivan Milin
 */
public class QuizServer {

    private ServerSocket ssocket;
    private int port;
    private ArrayList<ConnectedQuizClient> clients;
    private ArrayList<String> presentMembers;
    
    private static final String AES = "AES";
    private static final String AES_CIPHER_ALGORITHM = "AES/CBC/PKCS5PADDING";
    
    private SecretKey symmetricKey = null;                // OVO DODAJ KAD NAMESTIS METODU
    private static byte[] initializationVector = createInitializationVector();   // OVO DODAJ KAD NAMESTIS METODU
    
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

    public SecretKey getSymmetricKey() {
        return symmetricKey;
    }

    public byte[] getInitializationVector() {
        return initializationVector;
    }
    
    public void acceptClients() throws Exception
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
        symmetricKey = createAESKey();
        initializationVector = createInitializationVector();
        
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
    
    public static SecretKey createAESKey()
    {
        try
        {
            SecureRandom securerandom = new SecureRandom("RSZEOS2024".getBytes());
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES);
            keyGenerator.init(128, securerandom);
            SecretKey key = keyGenerator.generateKey();

            return key;
        }
        catch(NoSuchAlgorithmException ex)
        {
            Logger.getLogger(ConnectedQuizClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static byte[] createInitializationVector() 
    {
        byte[] initializationVector = new byte[16];
        for (int i = 0; i < 16; i++) 
        {
            initializationVector[i] = (byte) (i + 1);
        }
        return initializationVector;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        QuizServer server = new QuizServer(6001);
        
        System.out.println("Server pokrenut, slusam na portu 6001");
        server.acceptClients();
    }    

}
