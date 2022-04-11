package io.almer.server.repository

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import io.almer.db.BlogDB

class Repository(driver: SqlDriver) {
    init {
        BlogDB.Schema.create(driver)
    }

    private val db = BlogDB(driver)

    val userRepository = UserRepository(db, this)
    // todo Add the rest of the repositories
    //    create a separate repository class for each resource (eg ThreadRepository)

    constructor(): this(JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY))
}
