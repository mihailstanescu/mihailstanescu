package io.almer.server

import io.almer.api.User
import io.almer.server.repository.Repository

fun io.almer.db.User.toApi() = User(this.id, this.firstName, this.lastName, this.admin)

// todo add the rest of the db to API mapping, if needed