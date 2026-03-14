package org.example.project

import java.sql.Connection
import java.sql.DriverManager

object DatabaseConfig {
    private const val URL = "jdbc:postgresql://localhost:5432/postgres"
    private const val USER = "postgres"
    private const val PASS = "stefan"

    fun getConnection(): Connection {
        return DriverManager.getConnection(URL, USER, PASS)
    }
}