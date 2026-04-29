package com.oncoreminder.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private static MyDataBase instance;
    private final String URL = "jdbc:mysql://localhost:3306/oncoreminder?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private final String USERNAME = "root";
    private final String PASSWORD = "123456";
    private Connection cnx;

    private MyDataBase() {
        try {
            cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connexion à oncoreminder réussie !");
        } catch (SQLException e) {
            System.out.println("Erreur de connexion : " + e.getMessage());
        }
    }

    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    public Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("Reconnexion à oncoreminder réussie !");
            }
        } catch (SQLException e) {
            System.out.println("Erreur de reconnexion : " + e.getMessage());
        }
        return cnx;
    }
}