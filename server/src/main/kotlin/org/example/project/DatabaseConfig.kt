package org.example.project

import java.sql.Connection
import java.sql.DriverManager
import java.util.Properties

object DatabaseConfig {
    private const val URL = "jdbc:postgresql://aws-1-eu-central-1.pooler.supabase.com:6543/postgres"
    private const val USER = "postgres.muwxbbhtzoqjfdfwrwaw"
    private const val PASS = "&W8%7pwN*K@RE!A"

    fun getConnection(): Connection {
        val props = Properties()
        props.setProperty("user", USER)
        props.setProperty("password", PASS)
        props.setProperty("ssl", "true")
        props.setProperty("sslmode", "require")

        return DriverManager.getConnection(URL, props)
    }
}