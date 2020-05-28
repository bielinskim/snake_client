package snakegameclient;

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

public class SnakeGameClient {

    NewGame game;
    JFrame gameFrame;
    Rendering gamePanel;
    JPanel infoPanel;

    String dir = "";
    Key key;
    Reading r;
    Sending s;

    Socket socket;
    InputStream in;
    OutputStream out;
    BufferedReader fromKeyboard;

    List<Fields> snakes = new ArrayList();
    Fields fruit;
    Color playerOneColor = new Color(255, 0, 0, 128);
    Color playerTwoColor = new Color(0, 0, 255, 128);

    public class Fields {

        int x;
        int y;
        String player;
        
        public Fields(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Fields(int x, int y, String player) {
            this.x = x;
            this.y = y;
            this.player = player;
        }
    }

    public class Rendering extends JPanel {

        @Override
        public void paintComponent(Graphics g) {
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, 500, 500);

            //snakes.forEach((n) -> g.clearRect(n.x * 5, n.y * 5, 5, 5));
            for (int i = 0; i < snakes.size(); i++) {
                if(snakes.get(i).player.equals("o")) {
                    g.setColor(playerOneColor);
                } else {
                    g.setColor(playerTwoColor);
                }      
                g.fillRect(snakes.get(i).x * 5, snakes.get(i).y * 5, 5, 5);
            }
            g.clearRect(fruit.x * 5, fruit.y * 5, 5, 5);
        }

    }

    class Reading extends Thread {

        int i = 0;
        boolean xory = true;
        int x = 0;
        int y = 0;
        JTextField field;
        String nick;

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
                    switch (data.charAt(0)) {
                        case 'i':
                            // init
                            for (int j = 1; j < data.length(); j += 4) {
                                x = Integer.parseInt(data.substring(j, j + 2));
                                y = Integer.parseInt(data.substring(j + 2, j + 4));
                                if(x<90) {
                                    snakes.add(new Fields(x, y, "o"));
                                }
                                else {
                                    snakes.add(new Fields(x, y, "t"));
                                }
                            }
                            gamePanel.repaint();
                            break;
                        case 'a':
                            nick = data.substring(1, data.length());
                            field = game.getPlayerOneName();
                            field.setText(nick);
                            break;
                        case 'b':
                            nick = data.substring(1, data.length());
                            field = game.getPlayerTwoName();
                            field.setText(nick);
                            break;
                        case 'f':
                            // fruit

                            x = Integer.parseInt(data.substring(1, 3));
                            y = Integer.parseInt(data.substring(3, 5));
                            fruit = new Fields(x, y);
                            gamePanel.repaint();
                            break;
                        case 'm':
                            // message
                            switch (data.charAt(1)) {
                                case 'e':
                                    new GameMenu().gameMenu();
                                    new MessageWindow("Gra o podanej nazwie już istnieje");
                                    break;
                                case 'n':
                                    new GameMenu().gameMenu();
                                    new MessageWindow("Gra o podanej nazwie nie istnieje");
                                    break;
                                case 'o':
                                    game.startGame();
                                    break;
                                default:
                                    break;
                            }
                            break;
                        case 'p':
                            // points
                            if (data.charAt(1) == 'o') {
                                field = game.getPlayerOnePoints();
                                field.setText(String.valueOf(Integer.parseInt(field.getText()) + 1));
                            } else if (data.charAt(1) == 't') {
                                field = game.getPlayerTwoPoints();
                                field.setText(String.valueOf(Integer.parseInt(field.getText()) + 1));
                            }
                            break;
                        case 'o':
                            // playerOnePos
                            x = Integer.parseInt(data.substring(1, 3));
                            y = Integer.parseInt(data.substring(3, 5));
                            snakes.add(new Fields(x, y, "o"));
                            snakes.remove(0);
                            gamePanel.repaint();
                            break;
                        case 't':
                            // playerTwoPos
                            x = Integer.parseInt(data.substring(1, 3));
                            y = Integer.parseInt(data.substring(3, 5));
                            snakes.add(new Fields(x, y, "t"));
                            snakes.remove(0);
                            gamePanel.repaint();
                            break;
                        default:   
                            break;
                    }

                }

            } catch (IOException ex) {
    
            }

        }

    }

    class Sending extends Thread {

        @Override
        public void run() {
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
        JTextField typeName, gameName, typeNick, nickName;

        public void gameMenu() {

            menuFrame = new JFrame("Snake");
            menuFrame.setSize(400, 350);
            menuFrame.setResizable(false);
            menuFrame.setLayout(null);
            menuFrame.setLocationRelativeTo(null);
            menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            menuPanel = new JPanel();
            menuPanel.setSize(400, 350);
            menuPanel.setLayout(null);
            menuPanel.setBackground(Color.DARK_GRAY);
            menuFrame.add(menuPanel);

            typeNick = new JTextField("Nick");
            typeNick.setEditable(false);
            typeNick.setBorder(null);
            typeNick.setBackground(Color.DARK_GRAY);
            typeNick.setFont(new Font("Lato", Font.BOLD, 20));
            typeNick.setBounds(175, 0, 200, 50);
            menuPanel.add(typeNick);

            nickName = new JTextField();
            nickName.setEditable(true);
            nickName.setFont(new Font("Lato", Font.BOLD, 20));
            nickName.setBounds(100, 50, 200, 50);
            menuPanel.add(nickName);

            typeName = new JTextField("Nazwa gry");
            typeName.setEditable(false);
            typeName.setBorder(null);
            typeName.setBackground(Color.DARK_GRAY);
            typeName.setFont(new Font("Lato", Font.BOLD, 20));
            typeName.setBounds(150, 100, 200, 50);
            menuPanel.add(typeName);

            gameName = new JTextField();
            gameName.setEditable(true);
            gameName.setFont(new Font("Lato", Font.BOLD, 20));
            gameName.setBounds(100, 150, 200, 50);
            menuPanel.add(gameName);

            createGame = new JButton("Stwórz grę");
            createGame.setFont(new Font("Lato", Font.BOLD, 15));
            createGame.setBounds(50, 220, 150, 50);
            createGame.addActionListener(this);
            menuPanel.add(createGame);

            joinGame = new JButton("Dołącz do gry");
            joinGame.setFont(new Font("Lato", Font.BOLD, 15));
            joinGame.setBounds(200, 220, 150, 50);
            joinGame.addActionListener(this);
            menuPanel.add(joinGame);

            menuFrame.setVisible(true);
        }

        @Override
        public void actionPerformed(ActionEvent s) {
            Object source = s.getSource();
            if (source == createGame) {
                connectWithServer(gameName.getText(), nickName.getText(), "create");
                menuFrame.dispose();

            }
            if (source == joinGame) {
                gameName.getText();
                connectWithServer(gameName.getText(), nickName.getText(), "join");
                menuFrame.dispose();
            }
        }

        public void connectWithServer(String name, String nick, String type) {

            String gNameToSend;
            String gNickToSend;

            try {

                socket = new Socket("localhost", 2006);
                System.out.println("Połączono z serwerem ...");
                in = socket.getInputStream();
                out = socket.getOutputStream();
                fromKeyboard = new BufferedReader(new InputStreamReader(System.in));

                if (type.equals("create") && !name.equals("") && !nick.equals("")) {
                    gNameToSend = "c" + name;
                    gNickToSend = "a" + nick;
                    out.write(gNameToSend.getBytes());
                    out.write("\r\n".getBytes());
                    out.write(gNickToSend.getBytes());
                    out.write("\r\n".getBytes());
                    game = new NewGame();
                    game.init();
                }
                if (type.equals("join") && !name.equals("") && !nick.equals("")) {
                    gNameToSend = "j" + name;
                    gNickToSend = "b" + nick;
                    out.write(gNameToSend.getBytes());
                    out.write("\r\n".getBytes());
                    out.write(gNickToSend.getBytes());
                    out.write("\r\n".getBytes());
                    game = new NewGame();
                    game.init();
                }
                if (name.equals("") || nick.equals("")) {
                    in.close();
                    out.close();
                    socket.close();
                    new GameMenu().gameMenu();
                    new MessageWindow("Wprowadź dane");
                }

            } catch (IOException e) {
                new GameMenu().gameMenu();
                new MessageWindow("Nie udało sie połączyć z serwerem");
            }

        }

    }

    public class NewGame {

        JTextField playersField, pointsField, playerOneName, playerTwoName, playerOnePoints, playerTwoPoints;

        public JTextField getPlayerOnePoints() {
            return playerOnePoints;
        }

        public JTextField getPlayerTwoPoints() {
            return playerTwoPoints;
        }

        public JTextField getPlayerOneName() {
            return playerOneName;
        }

        public JTextField getPlayerTwoName() {
            return playerTwoName;
        }

        public void init() {
            gameFrame = new JFrame("Snake");
            gamePanel = new Rendering();
            infoPanel = new JPanel();
            
            gameFrame.setSize(816, 539);
            gameFrame.setResizable(false);
            gameFrame.setLocationRelativeTo(null);
            gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            gamePanel.setBounds(0, 0, 516, 539);
            gameFrame.add(gamePanel);
            this.initInfoPanel();

            key = new Key();
            
           
            fruit = new Fields(100, 100);
            
            r = new Reading();
            r.start();
            s = new Sending();
            s.start();
        }
        
        public void startGame() {
            gameFrame.setVisible(true);
            gameFrame.setFocusable(true);
            gameFrame.addKeyListener(key);
        }
        public void initInfoPanel() {

            infoPanel = new JPanel();
            infoPanel.setBounds(516, 0, 300, 539);
            infoPanel.setLayout(null);
            infoPanel.setBackground(Color.WHITE);
            infoPanel.setIgnoreRepaint(true);
            gameFrame.add(infoPanel);
            
            playersField = new JTextField("Gracze");
            playersField.setEditable(false);
            playersField.setBorder(null);
            playersField.setBackground(Color.WHITE);
            playersField.setFont(new Font("Lato", Font.BOLD, 20));
            playersField.setBounds(500, 25, 300, 50);
            playersField.setHorizontalAlignment(JTextField.CENTER);
            infoPanel.add(playersField);

            playerOneName = new JTextField("");
            playerOneName.setEditable(false);
            playerOneName.setBorder(null);
            playerOneName.setBackground(Color.WHITE);
            playerOneName.setForeground(playerOneColor);
            playerOneName.setFont(new Font("Lato", Font.BOLD, 20));
            playerOneName.setBounds(500, 75, 150, 50);
            playerOneName.setHorizontalAlignment(JTextField.CENTER);
            infoPanel.add(playerOneName);

            playerTwoName = new JTextField("");
            playerTwoName.setEditable(false);
            playerTwoName.setBorder(null);
            playerTwoName.setBackground(Color.WHITE);
            playerTwoName.setForeground(playerTwoColor);
            playerTwoName.setFont(new Font("Lato", Font.BOLD, 20));
            playerTwoName.setBounds(650, 75, 150, 50);
            playerTwoName.setHorizontalAlignment(JTextField.CENTER);
            infoPanel.add(playerTwoName);
            
            pointsField = new JTextField("Punkty");
            pointsField.setEditable(false);
            pointsField.setBorder(null);
            pointsField.setBackground(Color.WHITE);
            pointsField.setFont(new Font("Lato", Font.BOLD, 20));
            pointsField.setBounds(500, 150, 300, 50);
            pointsField.setHorizontalAlignment(JTextField.CENTER);
            infoPanel.add(pointsField);

            playerOnePoints = new JTextField("0");
            playerOnePoints.setEditable(false);
            playerOnePoints.setBorder(null);
            playerOnePoints.setBackground(Color.WHITE);
            playerOnePoints.setFont(new Font("Lato", Font.BOLD, 20));
            playerOnePoints.setBounds(500, 200, 150, 50);
            playerOnePoints.setHorizontalAlignment(JTextField.CENTER);
            infoPanel.add(playerOnePoints);

            playerTwoPoints = new JTextField("0");
            playerTwoPoints.setEditable(false);
            playerTwoPoints.setBorder(null);
            playerTwoPoints.setBackground(Color.WHITE);
            playerTwoPoints.setFont(new Font("Lato", Font.BOLD, 20));
            playerTwoPoints.setBounds(650, 200, 150, 50);
            playerTwoPoints.setHorizontalAlignment(JTextField.CENTER);
            infoPanel.add(playerTwoPoints);
            infoPanel.setVisible(true);
        }

    }

    private class Key extends KeyAdapter {

        String oldDir = "";

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
    public class MessageWindow implements ActionListener {

        JFrame messageFrame;
        JPanel messagePanel;
        JTextField messageField;
        JButton acceptButton;

        public MessageWindow(String message) {
            this.init(message);
        }

        public void init(String message) {
            messageFrame = new JFrame("Message");
            messagePanel = new JPanel();

            messageFrame.setSize(500, 200);
            messageFrame.setResizable(false);
            messageFrame.setLocationRelativeTo(null);
            messageFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            messageFrame.setVisible(true);

            messagePanel = new JPanel();
            messagePanel.setSize(500, 200);
            messagePanel.setLayout(null);
            messagePanel.setBackground(Color.DARK_GRAY);
            messageFrame.add(messagePanel);

            messageField = new JTextField(message);
            messageField.setEditable(false);
            messageField.setBorder(null);
            messageField.setBackground(Color.DARK_GRAY);
            messageField.setForeground(Color.WHITE);
            messageField.setFont(new Font("Lato", Font.BOLD, 20));
            messageField.setBounds(25, 20, 450, 50);
            messageField.setHorizontalAlignment(JTextField.CENTER);
            messagePanel.add(messageField);
            
            acceptButton = new JButton("Ok");
            acceptButton.setFont(new Font("Lato", Font.BOLD, 15));
            acceptButton.setBounds(200, 85, 100, 50);
            acceptButton.addActionListener(this);
            messagePanel.add(acceptButton);
        }
        
        @Override
        public void actionPerformed(ActionEvent s) {
            Object source = s.getSource();
            if (source == acceptButton) {
                messageFrame.dispose();
            }
        }
    }

    public static void main(String[] args) {

        new SnakeGameClient().new GameMenu().gameMenu();

    }
}
