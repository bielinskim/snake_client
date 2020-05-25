package projekttest;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ProjektTest {

    JFrame gameFrame;
    Rendering gamePanel;

    String dir = "";
    Key key;
    Reading r;
    Sending s;

    Socket socket;
    InputStream in;
    OutputStream out;
    BufferedReader fromKeyboard;

    List<Fields> fields = new ArrayList();

    public class Fields {

        int x;

        int y;

        public Fields(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public class Rendering extends JPanel {

        @Override
        public void paintComponent(Graphics g) {

            g.fillRect(0, 0, 495, 495);

            //fields.forEach((n) -> g.clearRect(n.x * 5, n.y * 5, 5, 5));
            for (int i = 0; i < fields.size(); i++) {
                g.clearRect(fields.get(i).x * 5, fields.get(i).y * 5, 5, 5);
            }
        }

    }

    class Reading extends Thread {

        int i = 0;
        boolean xory = true;
        int x = 0;
        int y = 0;

        @Override
        public void run() {
            try {

                while (true) {

                    int k = 0;
                    StringBuilder sb = new StringBuilder();
                    while ((k = in.read()) != -1 && k != '\n') {

                        sb.append((char) k);

                    }

                    String data = sb.toString().trim();
                    if (data.charAt(0) == 'i') {

                        for (int j = 1; j < data.length(); j++) {
                            i = Character.getNumericValue(data.charAt(j));

                            if (xory) {
                                x = i;
                                xory = false;
                            } else {
                                y = i;
                                xory = true;

                                fields.add(new Fields(x, y));
                                gamePanel.repaint();

                            }
                        }
                    } else {

                        x = Integer.parseInt(data.substring(0, 2));
                        y = Integer.parseInt(data.substring(2, 4));
                        fields.add(new Fields(x, y));
                        fields.remove(0);
                        gamePanel.repaint();
                    }

                }

            } catch (IOException ex) {
                //System.err.println(ex);
//                in.close();
//                out.close();
//                socket.close();
//                System.exit(0);
            }

        }

    }

    class Sending extends Thread {

        @Override
        public void run() {

//            try {
//                    
//                StringBuilder posToSend = new StringBuilder();
//                
//                Thread.sleep(50);
//                
//
//                    while (true) {
//                        
//                        
//                        posToSend = new StringBuilder();
//                        if (x.length() == 1) {
//                            posToSend.append(Integer.toString(0));
//                        }
//                        posToSend.append(x);
//                        if (y.length() == 1) {
//                            posToSend.append(Integer.toString(0));
//                        }
//                        posToSend.append(y);
//                        String xy = posToSend.toString();
//                        out.write(xy.getBytes());
//                        out.write("\r\n".getBytes());
//
//                        Thread.sleep(20);
//                    }
//                } catch (IOException ex) {
//                    System.err.println(ex);
//                    //in.close();
//                    //out.close();
//                    //socket.close();
//                    //server.close();
//                    //System.exit(0);
//                } catch (InterruptedException ex) {
//
//                }
        }

        public void sendDir(String dir) {
            try {
                out.write(dir.getBytes());
                out.write("\r\n".getBytes());
            } catch (IOException ex) {

            }
        }

    }

    public class GameMenu implements ActionListener {

        JFrame menuFrame;
        JPanel menuPanel;
        JButton createGame, joinGame;
        JTextField typeName, gameName;

        public void gameMenu() {

            menuFrame = new JFrame("Snake");
            menuFrame.setSize(400, 250);
            menuFrame.setResizable(false);
            menuFrame.setLayout(null);
            menuFrame.setLocationRelativeTo(null);
            menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            menuPanel = new JPanel();
            menuPanel.setSize(400, 250);
            menuPanel.setLayout(null);
            menuPanel.setBackground(Color.DARK_GRAY);
            menuFrame.add(menuPanel);

            typeName = new JTextField("Nazwa gry");
            typeName.setEditable(false);
            typeName.setBorder(null);
            typeName.setBackground(Color.DARK_GRAY);
            typeName.setFont(new Font("Lato", Font.BOLD, 20));
            typeName.setBounds(150, 0, 200, 50);
            menuPanel.add(typeName);

            gameName = new JTextField();
            gameName.setEditable(true);
            gameName.setFont(new Font("Lato", Font.BOLD, 20));
            gameName.setBounds(100, 50, 200, 50);
            menuPanel.add(gameName);

            createGame = new JButton("Stwórz grę");
            createGame.setFont(new Font("Lato", Font.BOLD, 15));
            createGame.setBounds(50, 120, 150, 50);
            createGame.addActionListener(this);
            menuPanel.add(createGame);

            joinGame = new JButton("Dołącz do gry");
            joinGame.setFont(new Font("Lato", Font.BOLD, 15));
            joinGame.setBounds(200, 120, 150, 50);
            joinGame.addActionListener(this);
            menuPanel.add(joinGame);

            menuFrame.setVisible(true);
        }

        @Override
        public void actionPerformed(ActionEvent s) {
            Object source = s.getSource();
            if (source == createGame) {
                //new NewGame().init();
                connectWithServer(gameName.getText(), "create");
                menuFrame.dispose();
                
            }
            if (source == joinGame) {
                //new NewGame().init();
                gameName.getText();
                connectWithServer(gameName.getText(), "join");
                menuFrame.dispose();
            }
        }
        
        public void connectWithServer(String name, String type) {
            
            String gNameToSend;
       
             try {

                socket = new Socket("localhost", 2006);
                System.out.println("Połączono z serwerem ...");
                in = socket.getInputStream();
                out = socket.getOutputStream();
                fromKeyboard = new BufferedReader(new InputStreamReader(System.in));

            
            if(type.equals("create") && !name.equals("")) {
                gNameToSend = "c" + name;
                out.write(gNameToSend.getBytes());
                out.write("\r\n".getBytes());
            }
            if(type.equals("join") && !name.equals("")) {
                gNameToSend = "j" + name;
                out.write(gNameToSend.getBytes());
                out.write("\r\n".getBytes());
            } else {
                this.gameMenu();
            } 
            new NewGame().init();
            } catch (IOException e) {

            }
        }

    }

    public class NewGame {

        public void init() {

            gameFrame = new JFrame();
            gamePanel = new Rendering();

            gameFrame.add(gamePanel);
            gameFrame.setSize(500, 500);
            gameFrame.setResizable(false);
            gameFrame.setLocationRelativeTo(null);
            gameFrame.setBackground(Color.LIGHT_GRAY);
            gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            gameFrame.setVisible(true);
            key = new Key();
            gameFrame.addKeyListener(key);
            gameFrame.setFocusable(true);

           

            r = new Reading();
            r.start();
            s = new Sending();
            s.start();
        }

    }

    private class Key extends KeyAdapter {

        String oldDir = "dup";

        @Override
        public void keyPressed(KeyEvent e) {

            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                dir = "ddown";
            }
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                dir = "dleft";
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                dir = "dright";
            }
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                dir = "dup";
            }

            if (oldDir != dir) {
                oldDir = dir;
                s.sendDir(dir);
            }
        }

    }

    public static void main(String[] args) {

        new ProjektTest().new GameMenu().gameMenu();

    }
}
