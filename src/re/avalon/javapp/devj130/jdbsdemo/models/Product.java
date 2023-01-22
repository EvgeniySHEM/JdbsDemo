package re.avalon.javapp.devj130.jdbsdemo.models;

public class Product {
    private String article;
    private String name;
    private String color;
    private int price;
    private int quantity;


    public Product(String article, String name, String color, int price, int quantity) {
        this.article = article;
        this.name = name;
        this.color = color;
        this.price = price;
        this.quantity = quantity;
    }

    public String getArticle() {
        return article;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public int getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return "Prodact{" +
                "article='" + article + '\'' +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                '}';
    }
}
