package com.web.app.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder

val Json: Gson = GsonBuilder()
    .setPrettyPrinting() // Optional: Enables pretty printing for JSON
    .create()