import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.sql.*;

public class MathQuizApp {
    private static int score = 0;
    private static int totalQuestions = 5;
    private static int currentQuestion = 0;
    private static int num1, num2, correctAnswer;
    private static String operation;
    private static JFrame frame;
    private static JTextArea questionArea;
    private static JTextField answerField;
    private static JLabel scoreLabel;
    private static JButton submitButton, additionButton, subtractionButton, multiplicationButton;
    private static Connection conn;

    public static void main(String[] args) {
        initializeUI();
        connectDatabase();
    }

    private static void initializeUI() {
        frame = new JFrame("Math Quiz");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        questionArea = new JTextArea(2, 20);
        questionArea.setEditable(false);
        questionArea.setText("Welcome to the Math Quiz! Choose an operation.");

        scoreLabel = new JLabel("Score: 0");
        answerField = new JTextField(10);
        submitButton = new JButton("Submit Answer");

        additionButton = new JButton("Addition");
        subtractionButton = new JButton("Subtraction");
        multiplicationButton = new JButton("Multiplication");

        frame.add(questionArea);
        frame.add(additionButton);
        frame.add(subtractionButton);
        frame.add(multiplicationButton);
        frame.add(new JLabel("Your Answer:"));
        frame.add(answerField);
        frame.add(submitButton);
        frame.add(scoreLabel);

        frame.setSize(300, 300);
        frame.setVisible(true);

        additionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startQuiz(1); // Addition
            }
        });

        subtractionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startQuiz(2); // Subtraction
            }
        });

        multiplicationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startQuiz(3); // Multiplication
            }
        });

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkAnswer();
            }
        });
    }

    private static void connectDatabase() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:quiz_scores.db");
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS scores (id INTEGER PRIMARY KEY, score INTEGER)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void startQuiz(int operationChoice) {
        currentQuestion = 0;
        score = 0;
        updateScore();
        nextQuestion(operationChoice);
    }

    private static void nextQuestion(int operationChoice) {
        Random random = new Random();
        num1 = random.nextInt(100) + 1;
        num2 = random.nextInt(100) + 1;

        switch (operationChoice) {
            case 1: // Addition
                correctAnswer = num1 + num2;
                operation = "+";
                break;
            case 2: // Subtraction
                correctAnswer = num1 - num2;
                operation = "-";
                break;
            case 3: // Multiplication
                correctAnswer = num1 * num2;
                operation = "*";
                break;
        }

        questionArea.setText("What is " + num1 + " " + operation + " " + num2 + "?");
    }

    private static void checkAnswer() {
        try {
            int userAnswer = Integer.parseInt(answerField.getText());
            if (userAnswer == correctAnswer) {
                score++;
                JOptionPane.showMessageDialog(frame, "Correct!", "Answer", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Incorrect! The correct answer was " + correctAnswer, "Answer", JOptionPane.ERROR_MESSAGE);
            }

            currentQuestion++;
            updateScore();

            if (currentQuestion < totalQuestions) {
                nextQuestion(new Random().nextInt(3) + 1); // Randomize operation
            } else {
                endQuiz();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Please enter a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void updateScore() {
        scoreLabel.setText("Score: " + score);
    }

    private static void endQuiz() {
        JOptionPane.showMessageDialog(frame, "Quiz Finished! Final Score: " + score + "/" + totalQuestions, "Quiz Over", JOptionPane.INFORMATION_MESSAGE);
        saveScore();
        score = 0;
        updateScore();
    }

    private static void saveScore() {
        try {
            String query = "INSERT INTO scores (score) VALUES (?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, score);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void displayHistoricalScores() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM scores");
            StringBuilder sb = new StringBuilder("Historical Scores:\n");
            while (rs.next()) {
                sb.append("Score: ").append(rs.getInt("score")).append("\n");
            }
            JOptionPane.showMessageDialog(frame, sb.toString(), "Past Scores", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
