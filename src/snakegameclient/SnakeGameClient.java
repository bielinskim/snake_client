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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Klient SnakeGame
 * @author Mateusz Bieliński
 */
public class SnakeGameClient {
    
 
    /**
     * adres serwera
     */
    public static final String SERVER_ADDRESS = "localhost"; // 77.55.223.193    localhost

    /**
     * port tcp serwera
     */
    public static final int SERVER_TCP_PORT = 2006;

    /**
     * port udp klienta, na ktore beda przesylane datagramy z serwera, ustalany automatycznie
     */
    public int CLIENT_UDP_PORT;  // domyslnie dobierany

    public NewGame game;
    public JFrame gameFrame;
    public Rendering gamePanel;
    public JPanel infoPanel;

    public String dir = "";
    public Key key;
    public Reading r;
    public DatagramReading dr;
    public Sending s;
    
    public Socket socket;
    public InputStream in;
    public OutputStream out;
    public BufferedReader fromKeyboard;
    public DatagramSocket dgsocket;

    public List<Fields> snakes = new ArrayList();
    public Fields fruit;
    public Color playerOneColor = new Color(255, 0, 0, 128);
    public Color playerTwoColor = new Color(0, 0, 255, 128);

    /**
     *  Fields - klasa ktora przechowuje wspolrzedne pojedynczego pola z planszy gry w postaci x, y - liczby od 0 do 99,
     * + ewentualnie informacje o graczu w przypadku pol odwzorowujacych ciala wezy
     */
    public class Fields {

        public int x;
        public int y;
        public String player;

        /**
         * Pierwszy konstruktor
         * @param x - wspolrzedna x
         * @param y - wspolrzedna y
         */
        public Fields(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Drugi konstruktor
         * @param x - wpolrzedna x
         * @param y - wspolrzedna y
         * @param player - pierwszy/drugi gracz
         */
        public Fields(int x, int y, String player) {
            this.x = x;
            this.y = y;
            this.player = player;
        }
    }

    /**
     * Rendering - klasa w ktorej renderowana i odswiezana jest plansza gry po kazdym zaktualizowaniu danych
     */
    public class Rendering extends JPanel {

        @Override
        public void paintComponent(Graphics g) {
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, 500, 500);

            //snakes.forEach((n) -> g.clearRect(n.x * 5, n.y * 5, 5, 5));
            for (int i = 0; i < snakes.size(); i++) {
                if (snakes.get(i).player.equals("o")) {
                    g.setColor(playerOneColor);
                } else {
                    g.setColor(playerTwoColor);
                }
                g.fillRect(snakes.get(i).x * 5, snakes.get(i).y * 5, 5, 5);
            }
            g.clearRect(fruit.x * 5, fruit.y * 5, 5, 5);
        }

    }

    /**
     * Reading - klasa dzialajaca jako watek i sluzaca do odczytwania danych przesylanych z serwera z wykorzystaniem protokolu TCP
     */
    public class Reading extends Thread {

        public int i = 0;
        public boolean xory = true;
        public int x = 0;
        public int y = 0;
        public JTextField field;
        public String nick;

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
                                if (x < 90) {
                                    snakes.add(new Fields(x, y, "o"));
                                } else {
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
                        default:
                            break;
                    }

                }

            } catch (IOException ex) {

            }

        }
    }

    /**
     * DatagramReading - klasa dzialajaca jako watek i sluzaca do odczytwania danych przesylanych z serwera z wykorzystaniem protokolu UDP
     */
    public class DatagramReading extends Thread {

        public DatagramPacket dgpacket;
        
        public String data;
        public int x = 0;
        public int y = 0;

        /**
         * inicjalizacja buf jako bufora na dane i dgpacket do odebrania DatagramPacket
         */
        public DatagramReading() {
            byte[] buf = new byte[255];
            dgpacket = new DatagramPacket(buf, buf.length);
        }
        
        @Override
        public void run() {
            try {
                while (true) {
                dgsocket.receive(dgpacket);
                data = new String(dgpacket.getData(), 0, dgpacket.getLength());
                
                switch (data.charAt(0)) {
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
                }
                }
            } catch (IOException ex) {

            }
        }

    }

    /**
     * Sending - klasa do wysylania danych do serwera
     */
    public class Sending extends Thread {

        @Override
        public void run() {
        }

        /**
         *
         * @param dir - aktualny kierunek gracza przesylany do serwera
         */
        public void sendDir(String dir) {
            try {
                out.write(dir.getBytes());
                out.write("\r\n".getBytes());
            } catch (IOException ex) {

            }
        }

    }

    /**
     * GameMenu - klasa tworzaca menu gry
     */
    public class GameMenu implements ActionListener {

        public JFrame menuFrame;
        public JPanel menuPanel;
        public JButton createGame, joinGame;
        public JTextField typeName, gameName, typeNick, nickName;

        /**
         * inicjalizacja menu i elementow wchodzacych w jego sklad
         */
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

        /**
         *
         * @param name - nazwa gry
         * @param nick - nick gracza
         * @param type - stworz gre/dolacz do gry
         */
        public void connectWithServer(String name, String nick, String type) {

            String gNameToSend;
            String gNickToSend;
            String gPortToSend;

            try {

                socket = new Socket(SERVER_ADDRESS, SERVER_TCP_PORT);
                System.out.println("Połączono z serwerem ...");
                in = socket.getInputStream();
                out = socket.getOutputStream();
                fromKeyboard = new BufferedReader(new InputStreamReader(System.in));
                dgsocket = new DatagramSocket();
                CLIENT_UDP_PORT = dgsocket.getLocalPort();

                if (type.equals("create") && !name.equals("") && !nick.equals("")) {
                    gNameToSend = "c" + name;
                    gNickToSend = "a" + nick;
                    gPortToSend = "p" + String.valueOf(CLIENT_UDP_PORT);
                    out.write(gNameToSend.getBytes());
                    out.write("\r\n".getBytes());
                    out.write(gNickToSend.getBytes());
                    out.write("\r\n".getBytes());
                    out.write(gPortToSend.getBytes());
                    out.write("\r\n".getBytes());
                    game = new NewGame();
                    game.init();
                }
                if (type.equals("join") && !name.equals("") && !nick.equals("")) {
                    gNameToSend = "j" + name;
                    gNickToSend = "b" + nick;
                    gPortToSend = "p" + String.valueOf(CLIENT_UDP_PORT);
                    out.write(gNameToSend.getBytes());
                    out.write("\r\n".getBytes());
                    out.write(gNickToSend.getBytes());
                    out.write("\r\n".getBytes());
                    out.write(gPortToSend.getBytes());
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

    /**
     *  NewGame - glowna klasa sterujaca gra, utworzenie planszy gry, bocznego panelu
     */
    public class NewGame {

        public JTextField playersField, pointsField, playerOneName, playerTwoName, playerOnePoints, playerTwoPoints;

        /**
         *
         * @return - zwraca obiekt JTextField przechowujacy punkty pierwszego gracza 
         */
        public JTextField getPlayerOnePoints() {
            return playerOnePoints;
        }

        /**
         *
         * @return - zwraca obiekt JTextField przechowujacy punkty drugiego gracza 
         */
        public JTextField getPlayerTwoPoints() {
            return playerTwoPoints;
        }

        /**
         *
         * @return - zwraca obiekt JTextField przechowujacy nick pierwszego gracza 
         */
        public JTextField getPlayerOneName() {
            return playerOneName;
        }

        /**
         *
         * @return - zwraca obiekt JTextField przechowujacy nick drugiego gracza 
         */
        public JTextField getPlayerTwoName() {
            return playerTwoName;
        }

        /**
         * inicjalizacja okna gry, planszy gry i bocznego panelu, inicjalizacja obiektow odbierajacych i wysylajych dane od/do serwera, 
         * inicjalizacja ustalania kierunku za pomoca strzalek na klawiaturze
         */
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
            dr = new DatagramReading();
            dr.start();
            s = new Sending();
            s.start();
        }

        /**
         * metoda rozpoczynajaca gre na znak serwera
         */
        public void startGame() {
            gameFrame.setVisible(true);
            gameFrame.setFocusable(true);
            gameFrame.addKeyListener(key);
        }

        /**
         *  inicjalizacja bocznego panelu z nickami i punktami graczy
         */
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

    /**
     * Key - klasa odpowiadajaca za wczytywanie kierunku z klawiatury
     */
    public class Key extends KeyAdapter {

        public String oldDir = "";

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

            if (!oldDir.equals(dir)) {
                oldDir = dir;
                s.sendDir(dir);
            }
        }
    }

    /**
     * MessageWindow - klasa tworzaca okienko powiadomien
     */
    public class MessageWindow implements ActionListener {

        public JFrame messageFrame;
        public JPanel messagePanel;
        public JTextField messageField;
        public JButton acceptButton;

        /**
         *
         * @param message - tresc wiadomosci do wyswietlenia
         */
        public MessageWindow(String message) {
            this.init(message);
        }

        /**
         *
         * @param message - tresc wiadomosci do wyswietlenia przekazana z konstruktora
         */
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

    /**
     *
     * @param args metoda main inicjalizujaca calosc
     */
    public static void main(String[] args) {

        new SnakeGameClient().new GameMenu().gameMenu();

    }
}
