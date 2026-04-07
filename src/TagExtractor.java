import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class TagExtractor extends JFrame {
    private JButton openTextBtn, openStopWordsBtn, extractBtn, saveBtn;
    private JTextArea resultArea;
    private JLabel fileLabel, stopWordsLabel;
    private Set<String> stopWords;
    private Map<String, Integer> tagFrequencies;
    private File textFile, stopWordsFile;

    public TagExtractor() {
        setTitle("Tag/Keyword Extractor");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        stopWords = new TreeSet<>();
        tagFrequencies = new TreeMap<>();

        JPanel topPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        openTextBtn = new JButton("1. Select Text File");
        openStopWordsBtn = new JButton("2. Select Stop Words File");
        fileLabel = new JLabel("Text File: None selected");
        stopWordsLabel = new JLabel("Stop Words: None selected");

        topPanel.add(openTextBtn);
        topPanel.add(fileLabel);
        topPanel.add(openStopWordsBtn);
        topPanel.add(stopWordsLabel);
        add(topPanel, BorderLayout.NORTH);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        extractBtn = new JButton("3. Extract Tags");
        saveBtn = new JButton("4. Save Results");
        saveBtn.setEnabled(false); // Disabled until tags are extracted

        bottomPanel.add(extractBtn);
        bottomPanel.add(saveBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        openTextBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                textFile = chooser.getSelectedFile();
                fileLabel.setText("Text File: " + textFile.getName());
            }
        });

        openStopWordsBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                stopWordsFile = chooser.getSelectedFile();
                stopWordsLabel.setText("Stop Words: " + stopWordsFile.getName());
                loadStopWords(stopWordsFile);
            }
        });

        extractBtn.addActionListener(e -> extractTags());

        saveBtn.addActionListener(e -> saveResults());
    }

    private void loadStopWords(File file) {
        stopWords.clear();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                // Ensure words are lowercase and trimmed of whitespace
                stopWords.add(scanner.nextLine().trim().toLowerCase());
            }
            JOptionPane.showMessageDialog(this, "Loaded " + stopWords.size() + " stop words.");
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Error loading stop words: " + ex.getMessage());
        }
    }

    private void extractTags() {
        if (textFile == null || stopWords.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select both a text file and a stop words file first.");
            return;
        }

        tagFrequencies.clear();
        resultArea.setText("");

        try (Scanner scanner = new Scanner(textFile)) {
            scanner.useDelimiter("[\\p{Punct}\\s]+");

            while (scanner.hasNext()) {
                String word = scanner.next().toLowerCase().replaceAll("[^a-z]", "");

                if (!word.isEmpty() && !stopWords.contains(word)) {
                    tagFrequencies.put(word, tagFrequencies.getOrDefault(word, 0) + 1);
                }
            }

            // Display the results in the text area
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Integer> entry : tagFrequencies.entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            resultArea.setText(sb.toString());

            // Enable saving now that we have data
            saveBtn.setEnabled(true);

        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Error reading text file: " + ex.getMessage());
        }
    }

    private void saveResults() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter out = new PrintWriter(chooser.getSelectedFile())) {
                out.print(resultArea.getText());
                JOptionPane.showMessageDialog(this, "Results saved successfully.");
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TagExtractor().setVisible(true);
        });
    }
}