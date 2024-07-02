import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;
import java.net.*;
import java.io.*;

public class Server implements ActionListener {

    JTextField text;
    JPanel a1;
    static Box vertical = Box.createVerticalBox();
    static JFrame f = new JFrame();
    static DataOutputStream dout;

    Server() {

        f.setLayout(null);

        JPanel p1 = new JPanel();
        p1.setBackground(new Color(0, 0, 0));
        p1.setBounds(0, 0, 450, 70);
        p1.setLayout(null);
        f.add(p1);

        ImageIcon i1 = new ImageIcon(new File("imgg.JPEG").getAbsolutePath());
        Image i2 = i1.getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT);
        ImageIcon i3 = new ImageIcon(i2);
        JLabel back = new JLabel(i3);
        back.setBounds(5, 20, 25, 25);
        p1.add(back);

        back.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent ae) {
                System.exit(0);
            }
        });


        JLabel name = new JLabel("Mahnoor Zafar");
        name.setBounds(110, 15, 150, 18);
        name.setForeground(Color.yellow);
        name.setFont(new Font("SAN_SERIF", Font.BOLD, 18));
        p1.add(name);

        JLabel status = new JLabel("last seen sep 6, 2023");
        status.setBounds(110, 35, 150, 18);
        status.setForeground(Color.yellow);
        status.setFont(new Font("SAN_SERIF", Font.BOLD, 12));
        p1.add(status);

        a1 = new JPanel();
        a1.setBounds(5, 75, 440, 570);
        f.add(a1);

        text = new JTextField();
        text.setBounds(5, 655, 310, 40);
        text.setFont(new Font("SAN_SERIF", Font.PLAIN, 16));
        f.add(text);

        JButton send = new JButton("Send");
        send.setBounds(320, 655, 123, 40);
        send.setBackground(new Color(0, 0, 0));
        send.setForeground(Color.yellow);
        send.addActionListener(this);
        send.setFont(new Font("SAN_SERIF", Font.PLAIN, 16));
        f.add(send);

        f.setSize(450, 700);
        f.setLocation(200, 50);
        f.setUndecorated(true);
        f.getContentPane().setBackground(Color.black);

        f.setVisible(true);
    }

    public void actionPerformed(ActionEvent ae) {
        try {
            String originalText = text.getText();
    
            // Encrypt the message using Playfair
            String encryptedText = playfairEncrypt(originalText);
    
            // Display the encrypted message on the sending side
            JPanel encryptedPanel = formatLabel("Encrypted: " + encryptedText);
            a1.setLayout(new BorderLayout());
            JPanel right = new JPanel(new BorderLayout());
            right.add(encryptedPanel, BorderLayout.LINE_END);
            vertical.add(right);
    
            vertical.add(Box.createVerticalStrut(15));
            a1.add(vertical, BorderLayout.PAGE_START);
    
            // Send the encrypted message
            dout.writeUTF(encryptedText);
    
            text.setText("");
    
            f.repaint();
            f.invalidate();
            f.validate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    public static JPanel formatLabel(String out) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel output = new JLabel("<html><p style=\"width: 150px\">" + out + "</p></html>");
        output.setFont(new Font("Tahoma", Font.PLAIN, 16));
        output.setBackground(new Color(255, 255, 0));
        output.setOpaque(true);
        output.setBorder(new EmptyBorder(15, 15, 15, 50));

        panel.add(output);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        JLabel time = new JLabel();
        time.setText(sdf.format(cal.getTime()));

        panel.add(time);

        return panel;
    }

    // Playfair Encryption
    private String playfairEncrypt(String plaintext) {
        // Convert the key to uppercase and remove duplicate letters
        String key = "MAHNOORZAFAR";
        key = key.toUpperCase().replaceAll("[^A-Z]", "");

        // Generate the Playfair matrix
        char[][] matrix = generatePlayfairMatrix(key);

        // Format the plaintext
        plaintext = plaintext.toUpperCase().replaceAll("[^A-Z]", "");

        // Adjust plaintext by adding an 'X' between repeated letters and at the end if needed
        plaintext = adjustPlaintext(plaintext);

        // Perform encryption
        StringBuilder encryptedText = new StringBuilder();
        for (int i = 0; i < plaintext.length(); i += 2) {
            char first = plaintext.charAt(i);
            char second = plaintext.charAt(i + 1);
            int[] firstPos = findPosition(matrix, first);
            int[] secondPos = findPosition(matrix, second);

            if (firstPos[0] == secondPos[0]) { // Same row
                encryptedText.append(matrix[firstPos[0]][(firstPos[1] + 1) % 5]);
                encryptedText.append(matrix[secondPos[0]][(secondPos[1] + 1) % 5]);
            } else if (firstPos[1] == secondPos[1]) { // Same column
                encryptedText.append(matrix[(firstPos[0] + 1) % 5][firstPos[1]]);
                encryptedText.append(matrix[(secondPos[0] + 1) % 5][secondPos[1]]);
            } else { // Different row and column
                encryptedText.append(matrix[firstPos[0]][secondPos[1]]);
                encryptedText.append(matrix[secondPos[0]][firstPos[1]]);
            }
        }

        return encryptedText.toString();
    }

    // Helper method to generate the Playfair matrix
    private char[][] generatePlayfairMatrix(String key) {
        char[][] matrix = new char[5][5];
        String keyAlphabet = key + "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        int index = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                while (matrix[i][j] == 0) {
                    char currentChar = keyAlphabet.charAt(index);
                    if (keyAlphabet.substring(0, index).indexOf(currentChar) == -1) {
                        matrix[i][j] = currentChar;
                    }
                    index++;
                }
            }
        }

        return matrix;
    }

    // Helper method to adjust plaintext
    private String adjustPlaintext(String plaintext) {
        StringBuilder adjustedText = new StringBuilder(plaintext);
        for (int i = 0; i < adjustedText.length() - 1; i += 2) {
            if (adjustedText.charAt(i) == adjustedText.charAt(i + 1)) {
                adjustedText.insert(i + 1, 'X');
            }
        }

        if (adjustedText.length() % 2 != 0) {
            adjustedText.append('X');
        }

        return adjustedText.toString();
    }

    // Helper method to find the position of a character in the Playfair matrix
    private int[] findPosition(char[][] matrix, char target) {
        int[] position = new int[2];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (matrix[i][j] == target) {
                    position[0] = i;
                    position[1] = j;
                    return position;
                }
            }
        }
        return position;
    }

    // Playfair Decryption
    private String playfairDecrypt(String ciphertext) {
        // Convert the key to uppercase and remove duplicate letters
        String key = "MAHNOORZAFAR";
        key = key.toUpperCase().replaceAll("[^A-Z]", "");

        // Generate the Playfair matrix
        char[][] matrix = generatePlayfairMatrix(key);

        // Perform decryption
        StringBuilder decryptedText = new StringBuilder();
        for (int i = 0; i < ciphertext.length(); i += 2) {
            char first = ciphertext.charAt(i);
            char second = ciphertext.charAt(i + 1);
            int[] firstPos = findPosition(matrix, first);
            int[] secondPos = findPosition(matrix, second);

            if (firstPos[0] == secondPos[0]) { // Same row
                decryptedText.append(matrix[firstPos[0]][(firstPos[1] - 1 + 5) % 5]);
                decryptedText.append(matrix[secondPos[0]][(secondPos[1] - 1 + 5) % 5]);
            } else if (firstPos[1] == secondPos[1]) { // Same column
                decryptedText.append(matrix[(firstPos[0] - 1 + 5) % 5][firstPos[1]]);
                decryptedText.append(matrix[(secondPos[0] - 1 + 5) % 5][secondPos[1]]);
            } else { // Different row and column
                decryptedText.append(matrix[firstPos[0]][secondPos[1]]);
                decryptedText.append(matrix[secondPos[0]][firstPos[1]]);
            }
        }

        // Remove padding 'X' characters
        removePaddingX(decryptedText);

        return decryptedText.toString();
    }

    // Helper method to remove padding 'X' characters
    private void removePaddingX(StringBuilder text) {
        int index = text.length() - 1;
        while (index > 0 && text.charAt(index) == 'X') {
            text.deleteCharAt(index);
            index--;
        }
    }

    public static void main(String[] args) {
    Server server = new Server(); // Create an instance of Server

    try (ServerSocket skt = new ServerSocket(6001)) {
        while (true) {
            Socket s = skt.accept();
            DataInputStream din = new DataInputStream(s.getInputStream());
            dout = new DataOutputStream(s.getOutputStream());

            while (true) {
                String encryptedMsg = din.readUTF();

                // Decrypt the received message using Playfair
                String decryptedMsg = server.playfairDecrypt(encryptedMsg);

                // Display the decrypted message on the receiving side
                JPanel panel = Server.formatLabel("Decrypted: " + decryptedMsg);

                JPanel left = new JPanel(new BorderLayout());
                left.add(panel, BorderLayout.LINE_START);
                vertical.add(left);
                f.validate();
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}}
