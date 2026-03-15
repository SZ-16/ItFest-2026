package org.example.project

import java.sql.Connection
import java.sql.DriverManager

object DatabaseConfig {
    private const val URL = "jdbc:postgresql://aws-1-eu-central-1.pooler.supabase.com:6543/postgres"

    private const val USER = "postgres.muwxbbhtzoqjfdfwrwaw"
    private const val PASS = "stefan"

    fun getConnection(): Connection {
        return DriverManager.getConnection(URL, USER, PASS)
    }

    fun testConnection() {
        try {
            getConnection().use {
                println("✅ Successfully connected to Supabase!")
            }
        } catch (e: Exception) {
            println("❌ Connection failed: ${e.message}")
        }
    }
}