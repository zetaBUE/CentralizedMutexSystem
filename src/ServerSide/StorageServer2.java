/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ServerSide;

/**
 *
 * @author zeta440
 */
import java.io.*;
import java.net.*;
import java.util.*;

public class StorageServer2 {
    private static final int PORT = 3001;
    private static final Map<String, Product> products = new HashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Storage Server 2 is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
        try (DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            String command = in.readUTF();

            if ("VIEW".equals(command)) {
                out.writeUTF(products.values().toString());
            } else if (command.startsWith("ADD ")) {
                String[] parts = command.split(" ");
                String productName = parts[1].trim();
                if (productName.isEmpty()) {
                    out.writeUTF("Product name cannot be empty");
                    return;
                }
                try {
                    int quantity = Integer.parseInt(parts[2]);
                    if (quantity <= 0) {
                        out.writeUTF("Quantity must be positive");
                        return;
                    }

                    Product pd = products.get(productName);
                    if (pd == null) {
                        products.put(productName, new Product(productName, quantity));
                    } else {
                        pd.increaseQuantity(quantity);
                    }

                    out.writeUTF("Added " + quantity + " to " + productName);
                } catch (NumberFormatException e) {
                    out.writeUTF("Invalid quantity format");
                }
            } else if (command.startsWith("DELETE ")) {
                String[] parts = command.split(" ");
                String productName = parts[1].trim();
                if (productName.isEmpty()) {
                    out.writeUTF("Product name cannot be empty");
                    return;
                }
                try {
                    int quantity = Integer.parseInt(parts[2]);
                    if (quantity <= 0) {
                        out.writeUTF("Quantity must be positive");
                        return;
                    }

                    Product pd = products.get(productName);
                    if (pd != null) {
                        pd.decreaseQuantity(quantity);
                        if (pd.getQuantity() <= 0) {
                            products.remove(productName);
                        }
                    }

                    if (!products.containsKey(productName)) {
                        out.writeUTF("Product " + productName + " removed from inventory.");
                    } else {
                        out.writeUTF("Reduced " + quantity + " from " + productName);
                    }
                } catch (NumberFormatException e) {
                    out.writeUTF("Invalid quantity format");
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }
}