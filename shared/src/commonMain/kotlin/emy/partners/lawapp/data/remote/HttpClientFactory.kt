package emy.partners.lawapp.data.remote

import io.ktor.client.HttpClient

expect fun createHttpClient(): HttpClient
