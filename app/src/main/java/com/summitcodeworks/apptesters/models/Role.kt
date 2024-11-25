package com.summitcodeworks.apptesters.models

data class Role(
    val roleId: Long,
    val appPkg: String,
    val userId: Int,
    val roleType: String,
    val roleTimestamp: String,
    val useFlag: Boolean
)