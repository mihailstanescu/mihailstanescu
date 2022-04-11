package io.almer.server.repository

import io.almer.api.User
import io.almer.api.UserPayload
import io.almer.db.BlogDB
import io.almer.server.toApi

/**
 * Handles access to the User resource. Hides the db implementation
 */
class UserRepository(db: BlogDB, repository: Repository) : RepositoryBase(db, repository) {
    /**
     * Select all users
     */
    fun selectAll(): List<User> {
        // the db interface is generated automatically by Sqldelight
        // we convert to an API object to hide the underlying implementation of the db
        return db.userQueries.selectAll().executeAsList().map { it.toApi() }
    }

    /**
     * Select one user
     */
    fun selectOne(id: Long): User {
        return db.userQueries.selectOne(id).executeAsOne().toApi()
    }

    /**
     * Delete one user
     */
    fun deleteOne(id: Long): Boolean {
        // also need the number of rows delete, to assess if we actually had the row,
        // so we use a transaction. The Sqldelight API is pretty nice, you just open the transaction
        // and give it a lambda, and it will handle the commit by itself
        val rows =
            db.userQueries.transactionWithResult<Long> {
                db.userQueries.deleteOne(id)
                val deletedRows = db.userQueries.selectChanges().executeAsOne()

                deletedRows
            }

        // see if any rows were affected
        return rows != 0L
    }

    fun createOne(userPayload: UserPayload): User {
        // we need the last insert ID, so we do another transaction
        // we know this is not the most efficient way
        val insertId =
            db.userQueries.transactionWithResult<Long> {
                db.userQueries.insertOne(userPayload.firstName, userPayload.lastName, userPayload.admin)
                val insertId = db.userQueries.lastInsertId().executeAsOne()

                insertId
            }

        return User(insertId, userPayload.firstName, userPayload.lastName, userPayload.admin)
    }
}