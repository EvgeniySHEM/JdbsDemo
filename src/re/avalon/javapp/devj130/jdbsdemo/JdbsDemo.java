package re.avalon.javapp.devj130.jdbsdemo;

import re.avalon.javapp.devj130.jdbsdemo.models.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbsDemo {
    private static final String dbURL = "jdbc:mysql://localhost:3306/JdbsDemo";
    private static final String dbUserName = "root";
    private static final String dbPassword = "12345678";

    public static void main(String[] args) throws SQLException {
        readProducts().forEach(System.out::println);
        System.out.println("------------------------");
        readProductsFromOrder(1).forEach(System.out::println);
//        registerOrder("John Smith", "+7(999)123-45-67", null,
//                "На Кудыкину гору", new String[] {"3251616", "3251620"},
//                new int[] {2, 8});
    }

    static List<Product> readProducts() throws SQLException {
        try (Connection connection = DriverManager.getConnection(dbURL, dbUserName, dbPassword);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT ARTICLE, NAME, COLOR, PRICE, QUANTITY FROM PRODUCTS")) {

            return readProductsFromResultSet(resultSet);
        }
    }

    static List<Product> readProductsFromOrder(int orderId) throws SQLException {
        try (Connection connection = DriverManager.getConnection(dbURL, dbUserName, dbPassword);
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT ARTICLE, NAME, COLOR," +
                     "PRICE, QUANTITY FROM PRODUCTS WHERE ARTICLE IN " +
                     "(SELECT ARTICLE FROM ORDER_DETAILS WHERE ORDER_ID = ?)")) {

            preparedStatement.setInt(1, orderId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return readProductsFromResultSet(resultSet);
            }
        }
    }

    static List<Product> readProductsFromResultSet(ResultSet resultSet) throws SQLException {
        List<Product> res = new ArrayList<>();
        while (resultSet.next()) {
            res.add(new Product(resultSet.getString(1), resultSet.getString(2),
                    resultSet.getString(3), resultSet.getInt(4),
                    resultSet.getInt(5)));
        }
        return res;
    }

    static void registerOrder(String customerName, String phone, String email, String delivery_address,
                              String[] articles, int[] quantities) throws SQLException {
        if(articles.length != quantities.length)
            throw new IllegalArgumentException("articles and quantities must have the same length");
        if(articles.length == 0)
            throw new IllegalArgumentException("Order details must be provided");

        try(Connection connection = DriverManager.getConnection(dbURL, dbUserName, dbPassword)) {
            int orderId;

            try(Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT COALESCE(MAX(ID), 0) FROM ORDERS")) {
                resultSet.next();
                orderId = resultSet.getInt(1) + 1;
            }

            try(PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " +
                    "ORDERS (ID, REG_DATE,CUSTOMER_NAME, PHONE, EMAIL,DELIVERY_ADDRESS, STATUS) " +
                    "VALUES(?, CURRENT_DATE, ?,?,?,?, 'P')")) {
                preparedStatement.setInt(1, orderId);
                preparedStatement.setString(2, customerName);
                preparedStatement.setString(3, phone);
                preparedStatement.setString(4, email);
                preparedStatement.setString(5, delivery_address);
                preparedStatement.executeUpdate();
            }

            try(PreparedStatement pricePreparedStatement = connection.prepareStatement("SELECT PRICE " +
                    "FROM PRODUCTS WHERE ARTICLE = ?");
                    PreparedStatement insetPreparedStatement = connection.prepareStatement("INSERT INTO " +
                    "ORDER_DETAILS (ORDER_ID, ARTICLE, PRICE, QUANTITY) " +
                    "VALUES (?,?,?,?)")) {

                insetPreparedStatement.setInt(1, orderId);

                for (int i = 0; i < articles.length; i++) {
                    pricePreparedStatement.setString(1, articles[i]);
                    int price;
                    try(ResultSet resultSet = pricePreparedStatement.executeQuery()) {
                        resultSet.next();
                        price = resultSet.getInt(1);
                    }
                    insetPreparedStatement.setString(2, articles[i]);
                    insetPreparedStatement.setInt(3, price);
                    insetPreparedStatement.setInt(4, quantities[i]);

                    insetPreparedStatement.addBatch();
                }
                insetPreparedStatement.executeBatch();
            }
        }

    }
}
