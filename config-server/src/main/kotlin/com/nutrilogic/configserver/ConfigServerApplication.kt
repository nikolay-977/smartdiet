package com.nutrilogic.configserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.config.server.EnableConfigServer
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableConfigServer
@EnableDiscoveryClient
class ConfigServerApplication

fun main(args: Array<String>) {
    runApplication<ConfigServerApplication>(*args)
}