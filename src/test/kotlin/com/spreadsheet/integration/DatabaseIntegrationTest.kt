package com.spreadsheet.integration

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class DatabaseIntegrationTest {

    object Users : Table() {
        val id = varchar("id", 10)
        val name = varchar("name", length = 50)
        override val primaryKey = PrimaryKey(id)
    }

    @Test
    fun `can connect to and interact with H2 database`() {
        // Connect to in-memory H2 database
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

        transaction {
            // Create the table
            SchemaUtils.create(Users)

            // Insert a user
            Users.insert {
                it[id] = "testuser"
                it[name] = "Test User"
            }

            // Query the user
            val user = Users.select { Users.id eq "testuser" }.single()

            assertEquals("Test User", user[Users.name])

            // Drop the table
            SchemaUtils.drop(Users)
        }
    }
}
