package com.example.addharold

data class Ciudades(
    var id: String,
    var cod_pais: String,
    var nombre: String,
    var poblacion: String,
)

data class pais(
    var id: String,
    var nombre: String,
    var poblacion: String,
)
data class monumento(
    var id: String,
    var cod_poblacion: String,
    var nombre: String
)


