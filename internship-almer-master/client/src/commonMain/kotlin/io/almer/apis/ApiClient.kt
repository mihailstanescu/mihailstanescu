package io.almer.apis

import io.almer.Requester

// we don't want anybody to use this class outside this package
// sealed is a very powerful construct, this is just the tip of the iceberg
sealed class ApiClient(
    protected val requester: Requester
)