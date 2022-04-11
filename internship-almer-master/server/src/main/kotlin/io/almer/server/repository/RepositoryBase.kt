package io.almer.server.repository

import io.almer.db.BlogDB

/**
 * Base class to grant access the Repository and db API
 */
abstract class RepositoryBase(protected val db: BlogDB, protected val repository: Repository) {
}