package jdbc.crud;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    private static final List<String> userOperations = List.of("Exit", "Registration", "Update", "Display data", "Delete");
    public static final String URL = "jdbc:mysql://localhost:3306/classicmodels";
    public static final String USER = "root";
    public static final String PASSWORD = "pluralsight";

    public static void main(String[] args) {

        final Scanner sc = new Scanner(System.in);

        boolean exit = false;
        while (!exit) {
            int option = askUserForOperationToPerform(sc);
            if (option == 0) {
                exit = true;
            } else {
                switch (option) {
                    case 1:
                        performRegistration(sc);
                        break;
                    case 2:
                        performUpdate(sc);
                        break;
                    case 3:
                        performDisplayData();
                        break;
                    case 4:
                        performDelete(sc);
                        break;
                    default:
                        break;
                }
            }
            System.out.println();
        }
        System.out.println("Exited successfully");
    }

    private static int askUserForOperationToPerform(Scanner sc) {
        boolean isValidOptionEntered = false;
        int option = 0;

        System.out.println("!!!! Welcome to user CRUD services !!!!");
        System.out.println("-----------------------------------------");
        System.out.println("Enter the option that you want to perform");
        while (!isValidOptionEntered) {
            userOperations
                    .forEach(operation -> System.out.println(userOperations.indexOf(operation) + ") " + operation));
            System.out.println("-----------------------------------------");
            option = sc.nextInt();
            if (option < 0 || option > 4) {
                System.out.println("Please enter valid option!");
            } else {
                isValidOptionEntered = true;
            }
        }

        return option;
    }

    private static void performRegistration(Scanner sc) {
        System.out.println("Enter user details:");
        System.out.println("userId: ");
        int id = sc.nextInt();
        System.out.println("firstName: ");
        String firstName = sc.next();
        System.out.println("lastName: ");
        String lastName = sc.next();
        System.out.println("email: ");
        String email = sc.next();

        String query = "insert into user values (?, ?, ?, ?)";
        try (
                Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, firstName);
            preparedStatement.setString(3, lastName);
            preparedStatement.setString(4, email);

            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("User registered successfully!");
    }

    private static void performUpdate(Scanner sc) {
        List<User> users = new ArrayList<>();

        System.out.println("Enter user details:");

        String query = "update user set firstName = ?, lastName = ?, email = ? where userId = ?";
        try (
                Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                Statement statement = connection.createStatement();
                PreparedStatement preparedStatement = connection.prepareStatement(query)
        ) {
            fetchAllUsers(users, statement);
            List<Integer> userIds = users.stream().map(User::getUserId).collect(Collectors.toList());

            if (users.isEmpty()) {
                System.out.println("No users to update!");
                return;
            } else {
                System.out.println("Enter userId to update among: ");
                userIds.forEach(id -> System.out.print(id + " "));
                System.out.println();

                System.out.println("userId: ");
                int id = sc.nextInt();
                if (!userIds.contains(id)) {
                    System.out.println("User not available!");
                    return;
                }
                System.out.println("firstName: ");
                String firstName = sc.next();
                System.out.println("lastName: ");
                String lastName = sc.next();
                System.out.println("email: ");
                String email = sc.next();

                preparedStatement.setString(1, firstName);
                preparedStatement.setString(2, lastName);
                preparedStatement.setString(3, email);
                preparedStatement.setInt(4, id);

                preparedStatement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("User updated successfully!");
    }

    private static void performDisplayData() {
        List<User> users = new ArrayList<>();
        try (
                Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                Statement statement = connection.createStatement()
        ) {
            fetchAllUsers(users, statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        users.forEach(System.out::println);
    }

    private static void fetchAllUsers(List<User> users, Statement statement) throws SQLException {
        ResultSet resultSet = statement.executeQuery("select * from user");
        while (resultSet.next()) {
            User user = User.builder()
                    .userId(resultSet.getInt("userId"))
                    .firstName(resultSet.getString("firstName"))
                    .lastName(resultSet.getString("lastName"))
                    .email(resultSet.getString("email"))
                    .build();

            users.add(user);
        }
    }

    private static void performDelete(Scanner sc) {
        System.out.println("Enter the Id to delete");
        int id = sc.nextInt();
        try (
                Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                Statement statement = connection.createStatement()
        ) {
            int count = statement.executeUpdate("delete from user where userId = " + id);
            System.out.println("deleted: " + count + " records successfully");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

@Data
@ToString
@Builder
class User {
    private int userId;
    private String firstName;
    private String lastName;
    private String email;
}
