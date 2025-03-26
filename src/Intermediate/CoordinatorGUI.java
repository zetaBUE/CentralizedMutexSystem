/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Intermediate;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import javax.swing.SwingUtilities;

/**
 *
 * @author zeta440
 */
public class CoordinatorGUI extends javax.swing.JFrame {
    private static final int PORT = 2000;
    private static Map<Integer, Socket> currentClients = new HashMap<>();
    private static Map<Integer, Queue<ClientRequest>> requestQueues = new HashMap<>();
    private static Map<Integer, String> currentOperations = new HashMap<>();
    private static ServerSocket coordinatorSocket;
    private static boolean isRunning = true;

    private static class ClientRequest {
        Socket socket;
        String operation;
        String timestamp;

        ClientRequest(Socket socket, String operation) {
            this.socket = socket;
            this.operation = operation;
            this.timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        }

        @Override
        public String toString() {
            return String.format("[%s] %s from %s",
                    timestamp,
                    operation,
                    socket.getInetAddress().getHostAddress());
        }
    }

    /**
     * Creates new form CoordinatorGUI
     */
    public CoordinatorGUI() {
        initComponents();
        new Thread(() -> startServer()).start();
    }

    private void startServer() {
        try {
            coordinatorSocket = new ServerSocket(PORT);
            appendToLog("Coordinator is running on port " + PORT);

            while (isRunning) {
                Socket clientSocket = coordinatorSocket.accept();
                new Thread(() -> handleRequest(clientSocket)).start();
            }
        } catch (IOException e) {
            appendToLog("Server error: " + e.getMessage());
        }
    }

    private synchronized void handleRequest(Socket clientSocket) {
        try {
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

            String request = in.readUTF();
            if (request == null) {
                appendToLog("Null request received from " + clientSocket.getInetAddress());
                clientSocket.close();
                return;
            }

            String[] parts = request.split(" ");
            String requestType = parts[0];
            int serverPort = Integer.parseInt(parts[1]);

            appendToLog("Received " + requestType + " request for server " + serverPort + " from "
                    + clientSocket.getInetAddress());

            synchronized (requestQueues) {
                requestQueues.putIfAbsent(serverPort, new LinkedList<>());
                requestQueues.get(serverPort).add(new ClientRequest(clientSocket, requestType));
                updateQueueDisplay();
                processQueue(serverPort);
            }

        } catch (IOException e) {
            appendToLog("Error handling request from " + clientSocket.getInetAddress() + ": " + e.getMessage());
            try {
                clientSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void processQueue(int serverPort) {
        if (currentClients.get(serverPort) == null && !requestQueues.get(serverPort).isEmpty()) {
            ClientRequest nextRequest = requestQueues.get(serverPort).poll();
            currentClients.put(serverPort, nextRequest.socket);
            currentOperations.put(serverPort, nextRequest.operation);
            appendToLog("Processing next client for server " + serverPort + " from: "
                    + nextRequest.socket.getInetAddress());
            new Thread(() -> grantAccess(nextRequest.socket, serverPort)).start();
        }
    }

    private void grantAccess(Socket clientSocket, int serverPort) {
        if (clientSocket == null || clientSocket.isClosed()) {
            currentClients.remove(serverPort);
            currentOperations.remove(serverPort);
            processQueue(serverPort);
            updateQueueDisplay();
            return;
        }

        try {
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());

            out.writeUTF("ALLOWED");

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                appendToLog("Processing interrupted for " + clientSocket.getInetAddress());
            }

            String message;
            while ((message = in.readUTF()) != null) {
                if ("DONE".equals(message)) {
                    appendToLog("Releasing lock from: " + clientSocket.getInetAddress() + " for server " + serverPort);
                    break;
                }
            }

            in.close();
            out.close();
            clientSocket.close();

            currentClients.remove(serverPort);
            currentOperations.remove(serverPort);
            processQueue(serverPort);
            updateQueueDisplay();

        } catch (IOException e) {
            appendToLog("Error in grantAccess for " + clientSocket.getInetAddress() + " on server " + serverPort + ": "
                    + e.getMessage());
            currentClients.remove(serverPort);
            currentOperations.remove(serverPort);
            processQueue(serverPort);
            updateQueueDisplay();
        }
    }

    private void updateQueueDisplay() {
        SwingUtilities.invokeLater(() -> {
            StringBuilder display = new StringBuilder();
            display.append("Current Queue Status:\n\n");

            for (int serverPort : requestQueues.keySet()) {
                display.append("Server ").append(serverPort).append(":\n");

                Queue<ClientRequest> queue = requestQueues.get(serverPort);
                if (queue.isEmpty()) {
                    display.append("  No requests waiting\n");
                } else {
                    for (ClientRequest request : queue) {
                        display.append("  ").append(request.toString()).append("\n");
                    }
                }

                if (currentClients.containsKey(serverPort)) {
                    Socket currentSocket = currentClients.get(serverPort);
                    String operation = currentOperations.get(serverPort);
                    display.append("  Currently processing: ").append(currentSocket.getInetAddress())
                            .append(" (").append(operation).append(")\n");
                }
                display.append("\n");
            }

            jTextArea1.setText(display.toString());
        });
    }

    private void appendToLog(String message) {
        SwingUtilities.invokeLater(() -> {
            jTextArea1.append(message + "\n");
            jTextArea1.setCaretPosition(jTextArea1.getDocument().getLength());
        });
    }

    @Override
    public void dispose() {
        isRunning = false;
        try {
            if (coordinatorSocket != null && !coordinatorSocket.isClosed()) {
                coordinatorSocket.close();
            }
        } catch (IOException e) {
            appendToLog("Error closing server: " + e.getMessage());
        }
        super.dispose();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel1.setText("Coordinator");
        jLabel1.setName(""); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(47, Short.MAX_VALUE)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 400,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(46, 46, 46))
                        .addGroup(layout.createSequentialGroup()
                                .addGap(182, 182, 182)
                                .addComponent(jLabel1)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addComponent(jLabel1)
                                .addGap(26, 26, 26)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 312,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(58, Short.MAX_VALUE)));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        // <editor-fold defaultstate="collapsed" desc=" Look and feel setting code
        // (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default
         * look and feel.
         * For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(CoordinatorGUI.class.getName()).log(java.util.logging.Level.SEVERE, null,
                    ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CoordinatorGUI.class.getName()).log(java.util.logging.Level.SEVERE, null,
                    ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CoordinatorGUI.class.getName()).log(java.util.logging.Level.SEVERE, null,
                    ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CoordinatorGUI.class.getName()).log(java.util.logging.Level.SEVERE, null,
                    ex);
        }
        // </editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CoordinatorGUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables
}
