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
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ProjektTest {

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
            g.setColor(new Color(0, 255, 0, 128));
            g.fillRect(0, 0, 500, 500);

            //snakes.forEach((n) -> g.clearRect(n.x * 5, n.y * 5, 5, 5));
            for (int i = 0; i < snakes.size(); i++) {
                g.setColor(new Color(255, 255, 255, 128));
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
                                snakes.add(new Fields(x, y));
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
                                    new MessageWindow("Gra o podanej nazwie juz istnieje");
                                    break;
                                case 'n':
                                    new GameMenu().gameMenu();
                                    new MessageWindow("Gra o podanej nazwie nie istnieje");
                                    break;
                                case 'o':
                                    game = new NewGame();
                                    game.init();
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
                        default:
                            // pos

                            x = Integer.parseInt(data.substring(0, 2));
                            y = Integer.parseInt(data.substring(2, 4));
                            snakes.add(new Fields(x, y));
                            snakes.remove(0);
                            gamePanel.repaint();
                            break;
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
                }
                if (type.equals("join") && !name.equals("") && !nick.equals("")) {
                    gNameToSend = "j" + name;
                    gNickToSend = "b" + nick;
                    out.write(gNameToSend.getBytes());
                    out.write("\r\n".getBytes());
                    out.write(gNickToSend.getBytes());
                    out.write("\r\n".getBytes());
                }
                if (name.equals("") || nick.equals("")) {
                    new MessageWindow("Wprowadz dane");
                    this.gameMenu();
                }

            } catch (IOException e) {
                new MessageWindow("Nie udało sie połączyć z serwerem");
            }

        }

    }

    public class NewGame {

        JTextField playerOneName, playerTwoName, playerOnePoints, playerTwoPoints;

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
            gameFrame.setVisible(true);

            gamePanel.setBounds(0, 0, 516, 539);
            gameFrame.add(gamePanel);
            this.initInfoPanel();

            key = new Key();
            gameFrame.addKeyListener(key);
            gameFrame.setFocusable(true);
            fruit = new Fields(100, 100);

            r = new Reading();
            r.start();
            s = new Sending();
            s.start();
        }

        public void initInfoPanel() {

            infoPanel = new JPanel();
            infoPanel.setBounds(516, 0, 300, 539);
            infoPanel.setLayout(null);
            infoPanel.setBackground(Color.WHITE);
            infoPanel.setIgnoreRepaint(true);
            gameFrame.add(infoPanel);

            playerOneName = new JTextField("");
            playerOneName.setEditable(false);
            playerOneName.setBorder(null);
            playerOneName.setBackground(Color.WHITE);
            playerOneName.setFont(new Font("Lato", Font.BOLD, 20));
            playerOneName.setBounds(516, 20, 150, 50);
            infoPanel.add(playerOneName);

            playerTwoName = new JTextField("");
            playerTwoName.setEditable(false);
            playerTwoName.setBorder(null);
            playerTwoName.setBackground(Color.WHITE);
            playerTwoName.setFont(new Font("Lato", Font.BOLD, 20));
            playerTwoName.setBounds(666, 20, 100, 50);
            infoPanel.add(playerTwoName);

            playerOnePoints = new JTextField("0");
            playerOnePoints.setEditable(false);
            playerOnePoints.setBorder(null);
            playerOnePoints.setBackground(Color.WHITE);
            playerOnePoints.setFont(new Font("Lato", Font.BOLD, 20));
            playerOnePoints.setBounds(536, 60, 100, 50);
            infoPanel.add(playerOnePoints);

            playerTwoPoints = new JTextField("0");
            playerTwoPoints.setEditable(false);
            playerTwoPoints.setBorder(null);
            playerTwoPoints.setBackground(Color.WHITE);
            playerTwoPoints.setFont(new Font("Lato", Font.BOLD, 20));
            playerTwoPoints.setBounds(686, 60, 100, 50);
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

    public class MessageWindow {

        JFrame messageFrame;
        JPanel messagePanel;
        JTextField messageField;

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
            messageField.setBounds(50, 50, 400, 50);
            messagePanel.add(messageField);

        }
    }

    public static void main(String[] args) {

        new ProjektTest().new GameMenu().gameMenu();

    }
}
