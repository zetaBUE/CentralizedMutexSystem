package ServerSide;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author zeta440
 */
import java.io.Serializable;

public class Product implements Serializable {
    private String name;
    private int quantity;

    public Product(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void increaseQuantity(int amount) {
        this.quantity += amount;
    }

    public void decreaseQuantity(int amount) {
        if (this.quantity >= amount) {
            this.quantity -= amount;
        }
    }

    @Override
    public String toString() {
        return "\n" + name + "(Qty: " + quantity + ")\n";
    }
}
