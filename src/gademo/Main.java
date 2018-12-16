/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gademo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.TreeSet;
import java.util.Vector;

/**
 *
 * @author Yasser
 */
public class Main extends JFrame implements ActionListener {

    /**
     * @param args the command line arguments
     */
    private static final int INIT_NODES = 20;
    JButton addNode, addEdge, random, start, restart;
    JPanel cPanel;
    DrawPane dPanel;
    JTextField value;
    JLabel title, status;
    float[] busy;
    int[] x, y;
    float[][] distances;
    int count;

    public Main() {
        setTitle("Genetic algorithm simulator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 600);
        initArrays();

        value = new JTextField(15);
        value.setText("10");
        title = new JLabel("Node busy [0-100]");
        title.setForeground(Color.WHITE);
        status = new JLabel("Ready...");

        cPanel = new JPanel();
        cPanel.setBackground(Color.BLACK);
        getContentPane().add(cPanel, BorderLayout.NORTH);
        addNode = new JButton("Add node");
        addEdge = new JButton("Add edge");
        random = new JButton("Random Graph");
        start = new JButton("Start GA");
        restart = new JButton("Restart");

        addNode.addActionListener(this);
        addEdge.addActionListener(this);
        random.addActionListener(this);
        start.addActionListener(this);
        restart.addActionListener(this);

        cPanel.add(title);
        cPanel.add(value);
        cPanel.add(addNode);
        cPanel.add(addEdge);
        cPanel.add(random);
        cPanel.add(start);
        cPanel.add(restart);
        dPanel = new DrawPane(this);
        getContentPane().add(dPanel);
        getContentPane().add(status, BorderLayout.SOUTH);
        GraphicsEnvironment env =
     GraphicsEnvironment.getLocalGraphicsEnvironment();

        this.setMaximizedBounds(env.getMaximumWindowBounds());
       this.setExtendedState(this.getExtendedState() | this.MAXIMIZED_BOTH);


        setVisible(true);
    }

    

    public static void main(String[] args) {
        // TODO code application logic here
        
            new Main();
        
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addNode) {
            title.setText("Node busy [0-100]");
            addNode();
        } else if (e.getSource() == addEdge) {
            title.setText("Edge cost ");
            dPanel.status = DrawPane.ADD_EDGE;
        } else if (e.getSource() == random) {
            int nodes, edges;
            try {
                nodes = Integer.parseInt(JOptionPane.showInputDialog(this, "Number of nodes"));
                edges = Integer.parseInt(JOptionPane.showInputDialog(this, "Number of edges (" +
                        (int) (nodes * 1.5) + " recommended)"));
            } catch (Exception err) {
                nodes = 25;
                edges = 50;
            }

            dPanel.generateRandomGraph(nodes, edges);
        } else if (e.getSource() == start) {
            dPanel.startGenetic();
        } else if(e.getSource() ==  restart){
            this.dispose();
            new Main();
        }
    }

    void initArrays() {
        //setResizable(false);
        count = 0;
        busy = new float[INIT_NODES];
        x = new int[INIT_NODES];
        y = new int[INIT_NODES];
        distances = new float[INIT_NODES][INIT_NODES];
        count = 0;
        for (int i = 0; i < distances.length; i++) {
            for (int j = 0; j < distances.length; j++) {
                if (i != j) {
                    distances[i][j] = Float.POSITIVE_INFINITY;
                }
            }
        }
        if (dPanel != null) {
            dPanel.firstNode = dPanel.lastNode = -1;
        }
    }

    private void addNode() {
        dPanel.status = DrawPane.ADD_NODE;
    }

    private class DrawPane extends JPanel implements MouseListener, MouseMotionListener {

        static final int IDLE = 0;
        static final int ADD_NODE = 1;
        static final int ADD_EDGE = 2;
        int RADIUS = 25;
        Main parent;
        int status = ADD_NODE;
        static final int SET_SOURCE = 3;
        static final int SET_DEST = 4;
        static final int RUNNING = 5;
        

        public DrawPane(Main m) {
            parent = m;
            addMouseMotionListener(this);
            addMouseListener(this);
        }

        @Override
        public void paint(Graphics g) {
            g.clearRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.white);
            g.fillRect(0, 0, getWidth(), getHeight());


            for (int i = 0; i < parent.count; i++) {
                for (int j = 0; j < parent.count; j++) {
                    if (!Float.isInfinite(parent.distances[i][j])) {
                        int x1, y1, x2, y2;
                        x1 = parent.x[i];
                        x2 = parent.x[j];
                        y1 = parent.y[i];
                        y2 = parent.y[j];
                        g.setColor(Color.red);

                        g.drawLine(x1, y1, x2, y2);
                        if (drawText) {
                            g.drawString("" + parent.distances[i][j], (x2 + x1) / 2, (y2 + y1) / 2);
                        }

                    }
                }
            }

            if (GAWorking && current != null) {
                for (int[] path : current) {
                    if (path == current.first()) {
                        drawColoredEdge(path, Color.black, g);
                    } else {
                        drawColoredEdge(path, Color.blue, g);
                    }
                }

            }

            if (shortestPath != null) {
                drawColoredEdge(shortestPath, Color.black, g);
            }

            for (int i = 0; i < parent.count; i++) {
                g.setColor(Color.blue);


                if (shortestPath != null && contains(shortestPath, i)) {
                    g.setColor(Color.green);
                }

                if (i == firstNode || i == lastNode) {
                    g.setColor(Color.black);
                }
                
                g.fillOval(parent.x[i] - RADIUS / 2, parent.y[i] - RADIUS / 2,
                        RADIUS, RADIUS);
                if (drawText) {
                    g.setColor(Color.black);
                    g.drawString(busy[i] + "%", parent.x[i] - RADIUS / 2, parent.y[i] - RADIUS / 2);
                    g.setColor(Color.white);
                    g.drawString("" + i, parent.x[i] - RADIUS / 4, parent.y[i] + RADIUS / 4);
                }
            }

            g.setColor(Color.black);

            if (dragging) {
                g.drawLine(x1, y1, x2, y2);
            }
        }

        private boolean contains(int[] array, int x) {
            for (int xx : array) {
                if (xx == x) {
                    return true;
                }
            }
            return false;
        }

        private void drawColoredEdge(int[] path, Color c, Graphics g) {
            if (path == null) {
                return;
            }

            Color old = g.getColor();
            g.setColor(c);
            Graphics2D gg = (Graphics2D) g;
            gg.setStroke(new BasicStroke(3));
            for (int i = 0; i < path.length - 1; i++) {

                gg.drawLine(parent.x[path[i]], parent.y[path[i]],
                        parent.x[path[i + 1]], parent.y[path[i + 1]]);
            }
            g.setColor(old);
        }

        public void generateRandomGraph(int nodes, int edges) {
            parent.initArrays();
            for (int i = 0; i < nodes; i++, parent.count++) {
                checkArrays();
                int b = (int) (Math.random() * 100);
                parent.busy[i] = b;
                int x = (int) (Math.random() * (getWidth() - RADIUS)) + RADIUS;
                int y = (int) (Math.random() * (getHeight() - RADIUS)) + RADIUS;

                while (checkCollision(x, y)) {
                    x = (int) (Math.random() * (getWidth() - RADIUS)) + RADIUS;
                    y = (int) (Math.random() * (getHeight() - RADIUS)) + RADIUS;
                }

                parent.x[i] = x;
                parent.y[i] = y;
            }

            int maxWeight = edges / nodes;

            int e = 0;
            DecimalFormat f = new DecimalFormat("#.#");
            while (e < edges) {
                int node1 = (int) (Math.random() * nodes);
                int node2 = (int) (Math.random() * nodes);
                if (node1 != node2 &&
                        Float.isInfinite(parent.distances[node1][node2]) &&
                        weight(node1) < maxWeight && weight(node2) < maxWeight) {
                    float cost = (float) Math.random() * 100;
                    cost = Float.valueOf(f.format(cost));
                    parent.distances[node1][node2] = cost;
                    parent.distances[node2][node1] = cost;
                    e++;
                }
            }


            this.repaint();
        }

        private int weight(int node) {
            if (node < parent.count) {
                return 0;
            }
            int c = 0;
            for (int i = 0; i < parent.count; i++) {
                if (!Float.isInfinite(parent.distances[node][i])) {
                    c++;
                }
            }
            return c;
        }
        boolean drawText = true;

        private void checkArrays() {

            if (count < 500) {
                RADIUS = 25;
                drawText = true;
            } else if (count < 750) {
                RADIUS = 20;
                drawText = false;
            } else {
                RADIUS = 10;
                drawText = false;
            }

            if (parent.count == parent.busy.length) {
                float[] nb = new float[parent.count * 2];
                for (int i = 0; i < parent.count; i++) {
                    nb[i] = parent.busy[i];
                }
                parent.busy = nb;

                float[][] nd = new float[count * 2][count * 2];
                for (int i = 0; i < parent.count; i++) {
                    for (int j = 0; j < parent.count; j++) {
                        nd[i][j] = parent.distances[i][j];
                    }
                }
                for (int i = 0; i < nd.length; i++) {
                    for (int j = parent.count; j < nd.length; j++) {
                        if (i != j) {
                            nd[i][j] = Float.POSITIVE_INFINITY;
                            nd[j][i] = Float.POSITIVE_INFINITY;
                        }
                    }
                }
                parent.distances = nd;

                int[] nx, ny;
                nx = new int[count * 2];
                ny = new int[count * 2];
                for (int i = 0; i < parent.x.length; i++) {
                    nx[i] = parent.x[i];
                    ny[i] = parent.y[i];
                }
                parent.x = nx;
                parent.y = ny;
            }
        }

        private boolean checkCollision(int x, int y) {

            for (int i = 0; i < parent.count; i++) {
                if (Math.abs(parent.x[i] - x) < RADIUS &&
                        Math.abs(parent.y[i] - y) < RADIUS) {
                    return true;
                }
            }

            return false;
        }

        public void mouseClicked(MouseEvent e) {

            if (status == ADD_NODE) {

                this.getGraphics().setColor(Color.blue);
                if (checkCollision(e.getX(), e.getY())) {
                    parent.status.setText("Collision: cannot add node here");
                    return;
                }

                checkArrays();
                String input = parent.value.getText();
                float b;

                try {
                    b = Float.parseFloat(input);
                } catch (Exception err) {
                    return;
                }

                if (b < 0) {
                    b = 0;
                } else if (b > 100) {
                    b = 100;
                }
                parent.busy[count] = b;
                parent.x[count] = e.getX();
                parent.y[count] = e.getY();
                parent.count++;
                parent.status.setText("Node " + count + " at (" + e.getX() + ", " + e.getY() + ")");
            } else if (status == SET_SOURCE) {
                int n1 = getNodeAt(e.getX(), e.getY());
                if (n1 != -1) {
                    firstNode = n1;
                    status = SET_DEST;
                    parent.status.setText("Select destination node to start");
                }
            } else if (status == SET_DEST) {
                int n2 = getNodeAt(e.getX(), e.getY());
                if (n2 != -1) {
                    lastNode = n2;
                    status = RUNNING;
                    startGA();
                }
            }

            this.repaint();
        }

        private int getNodeAt(int x, int y) {
            int index = -1;

            for (int i = 0; i < count; i++) {
                if (x > parent.x[i] - RADIUS / 2 && x < parent.x[i] + RADIUS / 2 &&
                        y > parent.y[i] - RADIUS / 2 && y < parent.y[i] + RADIUS / 2) {
                    index = i;
                    break;
                }
            }

            return index;
        }
        boolean dragging = false;
        int x1, y1, x2, y2, startIndex;
        int firstNode = -1, lastNode = -1;

        public void mousePressed(MouseEvent e) {
            if (status == ADD_EDGE) {
                this.getGraphics().setColor(Color.red);
                x1 = e.getX();
                y1 = e.getY();
                for (int i = 0; i < count; i++) {
                    if (x1 > parent.x[i] - RADIUS / 2 && x1 < parent.x[i] + RADIUS / 2 &&
                            y1 > parent.y[i] - RADIUS / 2 && y1 < parent.y[i] + RADIUS / 2) {
                        dragging = true;
                        parent.status.setText("drawing edge");
                        startIndex = i;
                        break;
                    }
                }
            }
        }

        public void mouseReleased(MouseEvent e) {
            if (!dragging) {
                return;
            }
            dragging = false;
            if (status == ADD_EDGE) {
                x2 = e.getX();
                y2 = e.getY();
                for (int i = 0; i < parent.count; i++) {
                    if (x2 > parent.x[i] - RADIUS / 2 && x2 < parent.x[i] + RADIUS / 2 &&
                            y2 > parent.y[i] - RADIUS / 2 && y2 < parent.y[i] + RADIUS / 2) {
                        if (i != startIndex) {
                            String input = parent.value.getText();
                            float distance;
                            try {
                                distance = Float.parseFloat(input);
                            } catch (Exception err) {
                                distance = 1;
                            }

                            parent.distances[i][startIndex] = distance;
                            parent.distances[startIndex][i] = distance;
                            parent.status.setText("Done edge between " + startIndex + " and " + i);
                            break;
                        }
                    }
                }
                this.repaint();
            }
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
            if (dragging) {
                x2 = e.getX();
                y2 = e.getY();
                this.repaint();
            }
        }

        public void mouseMoved(MouseEvent e) {
        }

        private void startGenetic() {
            status = SET_SOURCE;
            parent.status.setText("Click source node...");
        }

        private float cost(int node1, int node2) {
            return parent.distances[node1][node2];
        }
        ////////////////////////////////////////////////////////////////////////
        //                                                                    //
        //                    GENETIC ALGORITHM STARTS HERE                   //
        //                                                                    //
        ////////////////////////////////////////////////////////////////////////
        private int[] shortestPath;
        private boolean GAWorking = false;
        private TreeSet<int[]> current;

        private float avgCost, avgBusy;
        private void startGA() {
            GAWorking = true;
            parent.cPanel.setEnabled(false);
            removeMouseListener(this);
            removeMouseMotionListener(this);



            parent.distances[firstNode][lastNode] = Float.POSITIVE_INFINITY;
            parent.distances[lastNode][firstNode] = Float.POSITIVE_INFINITY;

            TreeSet<int[]> firstGeneration = new TreeSet<int[]>(new PathComparer(parent));

            int genSize;
            genSize = Integer.parseInt(JOptionPane.showInputDialog("Initial population"));

            for (int i = 0; i < genSize * 10; i++) {
                int[] path = getRandomPath(firstNode, lastNode);
                if (path != null) {
                    firstGeneration.add(path);
                    if(firstGeneration.size() == genSize / 2)
                        break;
                }
            }

            for (int i = 0; i < genSize * 10; i++) {
                int[] path = getRandomPath(lastNode, firstNode);
                if (path != null) {
                    firstGeneration.add(revert(path));
                    if(firstGeneration.size() == genSize)
                        break;
                }
            }

            if (firstGeneration.size() < 2) {
                JOptionPane.showMessageDialog(this, "No enough candidates to start");
                GAWorking = false;
                return;
            }


            for(int i = 0 ;i < parent.count; i++){
                avgBusy += parent.busy[i] * 2;
                for(int j = i; j < parent.count; j++){
                    if(!Float.isInfinite(parent.distances[i][j]))
                        avgCost += parent.distances[i][j];
                }
            }

            avgCost /= parent.count;
            avgBusy /= parent.count;

            MAX_LEVEL = 75/firstGeneration.size();//(int)((50 / Math.log(parent.count))/2);

            JOptionPane.showMessageDialog(this,"Working with population of " + firstGeneration.size()
                                                + "\n******\nMax level = " + MAX_LEVEL
                                                + "\nAverage cost = " + (avgCost+avgBusy));

            

            shortestPath = firstGeneration.first();
            shortest = getTotalCost(shortestPath);

            GA(firstGeneration, 0, genSize);

            parent.cPanel.setEnabled(true);
            addMouseListener(this);
            addMouseMotionListener(this);
            JOptionPane.showMessageDialog(this, "*******************************************************" +
                    "\nGA Finished: shortest path = " + shortest);
            String stat = "GA finished:";
            for(int x : shortestPath){
                stat += x + "->";
            }
            stat += " COST=" + shortest;
            parent.status.setText(stat);
            GAWorking = false;
            breakLoop = false;
        }

        private int[] revert(int[] x){
            int[] y = new int[x.length];

            for(int i = 0; i < y.length; i++){
                y[i] = x[(x.length - 1) - i];
            }

            return y;
        }


        int MAX_LEVEL = 10;
        boolean breakLoop = false;

        float shortest;
        private void GA(TreeSet<int[]> population, int level, int size) {
            current = population;
            if (level == MAX_LEVEL) {
                return;
            }

            if (population.size() == 0) {
                return;
            }
            if(level <= MAX_LEVEL / 10){
                this.repaint();
                JOptionPane.showMessageDialog(this, "Click OK for next step \n" +
                                            "current shortest path cost: " +
                                            shortest);
            }

            float cost = getTotalCost(population.first());
         
            if (cost < shortest) {
                this.repaint();
                JOptionPane.showMessageDialog(this, "Replacing " + shortest + " with " + cost);
                shortestPath = population.first();
                shortest = cost;
            }
/*
            if (cost <= avgCost + avgBusy) {
                //shortestPath = population.first();
                breakLoop = true;
                return;
            }
*/
            while (population.size() > size) {
                population.remove(population.last());
            }

            if (population.size() % 2 == 1) {
                population.remove(population.last());
            }

            System.out.println("GENERATION ***************************** " + level);
            for (int[] path : population) {
                printPath(path, false);
            }

            while (!population.isEmpty() && !breakLoop) {
                int[] a = population.first();
                population.remove(a);
                int[] b = population.first();
                population.remove(b);
                GA(crossOver(a, b), level + 1, size);
            }

            return;
        }

        private void printPath(int[] path, boolean err) {
            for (int i = 0; i < path.length; i++) {
                if (!err) {
                    System.out.print(path[i] + (i + 1 == path.length ? "" : "->"));
                } else {
                    System.err.print(path[i] + (i + 1 == path.length ? "" : "->"));
                }
            }
            if (!err) {
                System.out.println("\n : COST=" + getTotalCost(path));
                System.out.println("****************LENGTH = " + path.length + "***********");
            } else {
                System.err.println("\n : COST=" + getTotalCost(path));
                System.err.println("****************LENGTH = " + path.length + "***********");
            }
        }

        private int[] getRandomPath(int node1, int node2) {
            Vector<Integer> path = new Vector<Integer>();

            path.add(node1);
            for (int i = 0; i < parent.count; i++) {
                int x = getRandomNgbr(path.lastElement());
                if (x == -1) {
                    return null;
                }
                if (path.contains(x)) {
                    continue;
                }
                path.add(x);
                if (x == node2) {
                    break;
                }
            }

            if (path.size() == 0 || path.lastElement() != node2) {
                return null;
            }
            int[] result = new int[path.size()];
            for (int i = 0; i < result.length; i++) {
                result[i] = path.elementAt(i);
            }

            return result;

        }

        private int getRandomNgbr(int node) {
            Vector<Integer> nodes = new Vector<Integer>();
            for (int i = 0; i < parent.count; i++) {
                if (i != node && !Float.isInfinite(cost(node, i))) {
                    nodes.add(i);
                }
            }
            if (nodes.size() == 0) {
                return -1;
            }
            int random = (int) (Math.random() * nodes.size());
            return nodes.elementAt(random);
        }

        private float getTotalCost(int[] path) {
            float cost = 0;

            for (int i = 0; i < path.length - 1; i++) {
                cost += parent.distances[path[i]][path[i + 1]];
            }

            for (int i = 0; i < path.length; i++) {
                cost += parent.busy[path[i]] * 2;
            }

            return cost;
        }

        private TreeSet<int[]> crossOver(int[] path1, int[] path2) {

            TreeSet<int[]> children = new TreeSet<int[]>(new PathComparer(parent));

            for (int i = 1; i < path1.length - 1; i++) {
                for (int j = 1; j < path2.length - 1; j++) {
                    if (path1[i] == path2[j]) {
                        children.add(combine(path1, path2, i, j));
                        children.add(combine(path2, path1, j, i));
                    }
                }
            }

            return children;

        }

        private int[] combine(int[] x, int[] y, int i, int j) {
            int length = (i + 1) + y.length - (j + 1);
            int[] result = new int[length];

            int index = 0;
            for (int k = 0; k <= i; k++) {
                result[index++] = x[k];
            }

            for (int k = j + 1; k < y.length; k++) {
                result[index++] = y[k];
            }
            return result;
        }
    }

    private class PathComparer implements java.util.Comparator<int[]> {

        private Main parent;

        public PathComparer(Main parent) {
            this.parent = parent;
        }

        public int compare(int[] o1, int[] o2) {
            float c1, c2;
            c1 = getTotalCost(o1);
            c2 = getTotalCost(o2);

            float result = c1 - c2;
            if(result < 0) return -1;
            else if(result > 0) return 1;
            return 0;
        }

        private float getTotalCost(int[] path) {
            float cost = 0;

            for (int i = 0; i < path.length - 1; i++) {
                cost += parent.distances[path[i]][path[i + 1]];
            }

            for (int i = 0; i < path.length; i++) {
                cost += parent.busy[path[i]] * 2;
            }

            return cost;
        }
    }
}
