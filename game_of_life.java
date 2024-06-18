import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class game_of_life {
    private static int offsetX = 0;
    private static int offsetY = 0;
    private static int lastMouseX;
    private static int lastMouseY;
    private static int tailleCellule = 20;
    private static ArrayList<Point> cellsColorWhite = new ArrayList<>();
    private static boolean isPlaying = false;
    private static Timer timer;
    private static int vitesse = 500;
    private static int tour = 0;
    private static JFrame fenetre;
    private static JButton playPauseButton;
    private static JButton eraseButton;
    private static JTextField vitesseField;
    private static JLabel vitesseLabel;
    private static JLabel tourLabel;
    private static JButton nextButton;
    // private static JButton prevButton;
    private static JButton openButton;
    private static JButton saveBoutton;
    private static JPanel panel;
    // private static LinkedList<ArrayList<Point>> previousStates = new LinkedList<>();

    public static JPanel createPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.WHITE);
                int firstCellX = -offsetX / tailleCellule;
                int firstCellY = -offsetY / tailleCellule;
                int lastCellX = (getWidth() - offsetX) / tailleCellule + 1;
                int lastCellY = (getHeight() - offsetY) / tailleCellule + 1;
                for (int x = firstCellX; x < lastCellX; x++) {
                    for (int y = firstCellY; y < lastCellY; y++) {
                        g.setColor(Color.BLACK);
                        g.fillRect(x * tailleCellule + offsetX, y * tailleCellule + offsetY, tailleCellule, tailleCellule);
                    }
                }
                g.setColor(Color.WHITE);
                for (Point point : cellsColorWhite) {
                    int cellX = (int) point.getX();
                    int cellY = (int) point.getY();
                    g.fillRect(cellX * tailleCellule + offsetX, cellY * tailleCellule + offsetY, tailleCellule, tailleCellule);
                }
            }
        };

        panel.setBackground(Color.BLACK);
        return panel;
    }

    public static JPanel createMenu() {
        JPanel menuPanel = new JPanel();

        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(Color.GRAY);
        menuPanel.setPreferredSize(new Dimension(150, fenetre.getHeight()));
        playPauseButton = new JButton("Play");
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(playPauseButton);
        eraseButton = new JButton("Erase");
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(eraseButton);
        nextButton = new JButton("Next loop");
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(nextButton);
        // prevButton = new JButton("Prev loop");
        // menuPanel.add(Box.createVerticalStrut(10));
        // menuPanel.add(prevButton);
        openButton = new JButton("open file");
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(openButton);
        saveBoutton = new JButton("Save");
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(saveBoutton);
        vitesseLabel = new JLabel("Speed (ms): ");
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(vitesseLabel);
        vitesseField = new JTextField(5);
        vitesseField.setText(Integer.toString(vitesse));
        vitesseField.setMaximumSize(new Dimension(100, 25));
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(vitesseField);
        tourLabel = new JLabel("Loop: " + tour);
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(tourLabel);
        return menuPanel;
    }

    public static JPanel zoomInZoomOut(JPanel panel) {
        panel.addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int wheelRotation = e.getWheelRotation();
                if (wheelRotation < 0) {
                    int mouseGridX = (e.getX() - offsetX) / tailleCellule;
                    int mouseGridY = (e.getY() - offsetY) / tailleCellule;
                    tailleCellule += 5;
                    offsetX = e.getX() - (mouseGridX * tailleCellule);
                    offsetY = e.getY() - (mouseGridY * tailleCellule);
                } else {
                    if (tailleCellule > 5) {
                        int mouseGridX = (e.getX() - offsetX) / tailleCellule;
                        int mouseGridY = (e.getY() - offsetY) / tailleCellule;
                        tailleCellule -= 5;
                        offsetX = e.getX() - (mouseGridX * tailleCellule);
                        offsetY = e.getY() - (mouseGridY * tailleCellule);
                    }
                }
                panel.repaint();
            }
        });
        return panel;
    }

    public static int countLiveNeighbors(double x, double y, ArrayList<Point> cells) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i != 0 || j != 0) {
                    Point neighbor = new Point((int) x + i, (int) y + j);
                    if (cells.contains(neighbor)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public static void addNeighbors(int x, int y, ArrayList<Point> neighbors) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i != 0 || j != 0) {
                    Point neighbor = new Point(x + i, y + j);
                    if (!neighbors.contains(neighbor)) {
                        neighbors.add(neighbor);
                    }
                }
            }
        }
    }

    public static void updateTourLabel() {
        tourLabel.setText("Tours: " + tour);
    }

    public static void updateCellState(JPanel panel) {
        ArrayList<Point> currentState = new ArrayList<>(cellsColorWhite);
        // previousStates.add(currentState);

        ArrayList<Point> nextGenCells = new ArrayList<>();
        ArrayList<Point> currentCells = cellsColorWhite;
        ArrayList<Point> allNeighbors = new ArrayList<>();

        for (Point cell : currentCells) {
            int x = (int) cell.getX();
            int y = (int) cell.getY();
            int liveNeighbors = countLiveNeighbors(x, y, currentCells);
            if (liveNeighbors == 2 || liveNeighbors == 3) {
                nextGenCells.add(cell);
            }
            addNeighbors(x, y, allNeighbors);
        }
        for (Point neighbor : allNeighbors) {
            int x = (int) neighbor.getX();
            int y = (int) neighbor.getY();
            int liveNeighbors = countLiveNeighbors(x, y, currentCells);
            if (liveNeighbors == 3 && !nextGenCells.contains(neighbor)) {
                nextGenCells.add(neighbor);
            }
        }
        cellsColorWhite = nextGenCells;
        tour++;
        updateTourLabel();
        panel.repaint();
    }

    public static JPanel changeCelluleColor(JPanel panel) {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMouseX = e.getX();
                lastMouseY = e.getY();
                int cellX = (lastMouseX - offsetX) / tailleCellule;
                int cellY = (lastMouseY - offsetY) / tailleCellule;
                if (SwingUtilities.isRightMouseButton(e)) {
                    Point clickedPoint = new Point(cellX, cellY);
                    if (cellsColorWhite.contains(clickedPoint)) {
                        cellsColorWhite.remove(clickedPoint);
                    } else {
                        cellsColorWhite.add(clickedPoint);
                    }
                }
                panel.repaint();
            }
        });
        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int cellX = (e.getX() - offsetX) / tailleCellule;
                    int cellY = (e.getY() - offsetY) / tailleCellule;
                    Point clickedPoint = new Point(cellX, cellY);
                    if (!cellsColorWhite.contains(clickedPoint)) {
                        cellsColorWhite.add(clickedPoint);
                    }
                    panel.repaint();
                }
            }
        });

        return panel;
    }

    public static JPanel deplaceGrid(JPanel panel) {
        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    offsetX += e.getX() - lastMouseX;
                    offsetY += e.getY() - lastMouseY;
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                    panel.repaint();
                }
            }
        });
        return panel;
    }

    public static void togglePlayPause(JButton playPauseButton, JButton eraseButton, JPanel panel) {
        isPlaying = !isPlaying;
        if (isPlaying) {
            playPauseButton.setText("Pause");
            eraseButton.setEnabled(false);
            nextButton.setEnabled(false);
            // prevButton.setEnabled(false);
            openButton.setEnabled(false);
            saveBoutton.setEnabled(false);
            timer = new Timer(vitesse, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateCellState(panel);
                }
            });
            timer.start();
        } else {
            playPauseButton.setText("Play");
            eraseButton.setEnabled(true);
            nextButton.setEnabled(true);
            // prevButton.setEnabled(true);
            openButton.setEnabled(true);
            saveBoutton.setEnabled(true);
            if (timer != null) {
                timer.stop();
            }
        }
    }

    public static void clearWhiteCells(JPanel panel) {
        cellsColorWhite.clear();
        // previousStates.clear();
        panel.repaint();
    }

    public static JButton checkEraseButton(JButton eraseButton, JPanel finalPanel) {
        eraseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearWhiteCells(finalPanel);
            }
        });
        eraseButton.setEnabled(true);
        return eraseButton;
    }

    public static JButton playEvent(JButton playPauseButton, JPanel panel, JButton finalPlayPauseButton, JButton finalEraseButton, JTextField vitesseField) {
        playPauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                togglePlayPause(finalPlayPauseButton, finalEraseButton, panel);
            }
        });
        return playPauseButton;
    }

    public static JTextField changeVitesse(JTextField vitesseField) {
        vitesseField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int nouvelleVitesse = Integer.parseInt(vitesseField.getText());
                    if (nouvelleVitesse > 0) {
                        vitesse = nouvelleVitesse;
                        if (isPlaying && timer != null) {
                            timer.setDelay(vitesse);
                        }
                    } else {
                        vitesseField.setText(Integer.toString(vitesse));
                    }
                } catch (NumberFormatException ex) {
                    vitesseField.setText(Integer.toString(vitesse));
                }
            }
        });
        return vitesseField;
    }

    public static JPanel ecouteurMouse(JPanel panel, JButton playPauseButton, JButton eraseButton, JTextField vitesseField, JButton openButton, JButton nextButton, JButton saveBoutton) {
        final JPanel finalPanel = panel;
        final JButton finalPlayPauseButton = playPauseButton;
        final JButton finalEraseButton = eraseButton;

        panel = zoomInZoomOut(panel);
        panel = changeCelluleColor(panel);
        panel = deplaceGrid(panel);
        eraseButton = checkEraseButton(eraseButton, finalPanel);
        playPauseButton = playEvent(playPauseButton, finalPanel, finalPlayPauseButton, finalEraseButton, vitesseField);
        nextButton = nextButtonAction(nextButton);
        vitesseField = changeVitesse(vitesseField);
        openButton = chargingFile(openButton);
        saveBoutton = savingfile(saveBoutton);
        return panel;
    }

    // public static void restorePreviousState() {
    //     if (!previousStates.isEmpty()) {
    //         cellsColorWhite = previousStates.removeLast();
    //         tour--;
    //         updateTourLabel();
    //         panel.repaint();
    //     }
    // }

    // public static JButton prevButtonAction(JButton prevButton) {
    //     prevButton.addActionListener(new ActionListener() {
    //         @Override
    //         public void actionPerformed(ActionEvent e) {
    //             if (!isPlaying && tour > 0) {
    //                 restorePreviousState();
    //             }
    //         }
    //     });
    //     return prevButton;
    // }

    public static JButton nextButtonAction(JButton nextButton) {
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isPlaying) {
                    updateCellState(panel);
                }
            }
        });
        return nextButton;
    }

    public static JButton savingfile(JButton saveBoutton) {
        saveBoutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showSaveDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileChooser.getSelectedFile()))) {
                        int firstCellX = Integer.MAX_VALUE;
                        int firstCellY = Integer.MAX_VALUE;
                        int lastCellX = Integer.MIN_VALUE;
                        int lastCellY = Integer.MIN_VALUE;
                        for (Point point : cellsColorWhite) {
                            int x = (int) point.getX();
                            int y = (int) point.getY();
                            if (x < firstCellX) {
                                firstCellX = x;
                            }
                            if (y < firstCellY) {
                                firstCellY = y;
                            }
                            if (x > lastCellX) {
                                lastCellX = x;
                            }
                            if (y > lastCellY) {
                                lastCellY = y;
                            }
                        }
                        for (int y = firstCellY; y <= lastCellY; y++) {
                            for (int x = firstCellX; x <= lastCellX; x++) {
                                Point point = new Point(x, y);
                                if (cellsColorWhite.contains(point)) {
                                    bw.write("1");
                                } else {
                                    bw.write("0");
                                }
                            }
                            bw.newLine();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        return saveBoutton;
    }

    public static JButton chargingFile(JButton openButton) {
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    try (BufferedReader br = new BufferedReader(new FileReader(fileChooser.getSelectedFile()))) {
                        String line;
                        int y = 0;
                        while ((line = br.readLine()) != null) {
                            for (int x = 0; x < line.length(); x++) {
                                if (line.charAt(x) == '1') {
                                    cellsColorWhite.add(new Point(x, y));
                                }
                            }
                            y++;
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    panel.repaint();
                }
            }
        });
        return openButton;
    }

    public static void window() {
        JPanel menuPanel;

        fenetre = new JFrame("Game of Life");
        playPauseButton = new JButton();
        eraseButton = new JButton();
        vitesseField = new JTextField();
        menuPanel = createMenu();
        fenetre.add(menuPanel, BorderLayout.WEST);
        // prevButton = prevButtonAction(prevButton);
        panel = createPanel();
        panel = ecouteurMouse(panel, playPauseButton, eraseButton, vitesseField, openButton, nextButton, saveBoutton);
        fenetre.add(panel);
        fenetre.setSize(400, 300);
        fenetre.setExtendedState(JFrame.MAXIMIZED_BOTH);
        fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fenetre.setVisible(true);
    }

    public static void main(String[] args) {
        window();
    }
}
