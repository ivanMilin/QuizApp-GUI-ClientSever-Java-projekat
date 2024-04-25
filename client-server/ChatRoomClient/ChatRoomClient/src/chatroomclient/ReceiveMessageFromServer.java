/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatroomclient;

import java.io.BufferedReader;
import java.io.IOException;
//import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

//import javax.swing.JComboBox;
//import javax.swing.JOptionPane;
/**
 * Ova klasa se koristi za prijem poruka od strane servera jer ce one stizati
 * asinhrono (ne znamo u kom trenutku ce se novi korisnik ukljuciti u Chat room,
 * kao ni kada ce nam poslati poruku)
 *
 */
public class ReceiveMessageFromServer implements Runnable {

    ChatRoomClient parent;
    BufferedReader br;      // IVAN : (1:23:27) treba nam samo BufferedReader jer samo primamo poruke, ne saljemo ih ka serveru 
                            // Sa klientske strane postoje dva thread-a    
                            //      1. Jedan thread koji ceka poruke od servera
                            //      2. Jedan thread GUI koji salje poruke ka serveru
    
    public ReceiveMessageFromServer(ChatRoomClient parent) {
        //parent ce nam trebati da bismo mogli iz ovog thread-a da menjamo sadrzaj 
        //komponenti u osnovnom GUI prozoru (npr da popunjavamo Combo Box sa listom
        //korisnika
        this.parent = parent;
        //BufferedReader koristimo za prijem poruka od servera, posto su sve
        //poruke u formi Stringa i linija teksta, BufferedReader je zgodniji nego
        //da citamo poruke iz InputStream objekta
        this.br = parent.getBr();
    }

    @Override
    public void run() {
        //Beskonacna petlja
        while (true) {
            String line;
            try {
                /* 
                   Cekaj da ti stigne linija teksta od servera. Postoje dve poruke koje nam server salje:
                1. spisak korisnika koji je uvek u formatu Users: Milan Dusan Dragan Dimitrije
                2. poruka koja nam stize od nekog drugog korisnika iz Chat room-a, koja je uvek u formatu
                    Ime korisnika koji salje poruku: Tekst poruke
                 */

                line = this.br.readLine();

                if (line.startsWith("Users: ")) {
                    /* 
                    1. parsiraj pristiglu poruku, 
                    2. prepoznaj korisnike koji su trenutno u Chat roomu
                    3. azuriraj ComboBox sa spiskom korisnika koji su trenutno u Chat Room-u
                    4. Prikazi prozor sa obavestenjem da je novi clan dosao/izasao iz Chat room-a (npr JOptionPane.showMessageDialog)
                     */

                    String[] imena = line.split(":")[1].split(" ");

                    parent.getCbUsers().removeAllItems();

                    for (String ime : imena) {
                        if (!ime.equals("")) {
                            parent.getCbUsers().addItem(ime.trim());
                        }
                    }
                    parent.setTaReceivedMessages("Novi clan se prikljucio ili je postojeci napustio sobu! Tretnutni clanovi su: " + line.split(": ")[1].toString());

                } else {
                    //prikazi poruku koja je primljena u polju za prijem poruka
                    parent.setTaReceivedMessages(line);

                }
            } catch (IOException ex) {
                Logger.getLogger(ReceiveMessageFromServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
