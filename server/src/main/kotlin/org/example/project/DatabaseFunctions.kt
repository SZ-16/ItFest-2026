package org.example.project

import java.sql.Timestamp

fun saveScanToSupabase(imageName: String, hasAnomaly: Boolean, description: String) {
    val sql = """
        INSERT INTO site_scans (image_name, has_anomaly, ai_description, scan_time, lat, lng) 
        VALUES (?, ?, ?, ?, ?, ?)
    """.trimIndent()

    try {
        DatabaseConfig.getConnection().use { conn ->
            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setString(1, imageName)
                pstmt.setBoolean(2, hasAnomaly)
                pstmt.setString(3, description)
                pstmt.setTimestamp(4, Timestamp(System.currentTimeMillis()))
                pstmt.setDouble(5, 45.7489) // Default Lat (Timișoara)
                pstmt.setDouble(6, 21.2087) // Default Lng (Timișoara)
                pstmt.executeUpdate()
            }
        }
        println("💾 Data saved to Supabase for $imageName")
    } catch (e: Exception) {
        println("❌ Error saving to Supabase: ${e.message}")
    }
}