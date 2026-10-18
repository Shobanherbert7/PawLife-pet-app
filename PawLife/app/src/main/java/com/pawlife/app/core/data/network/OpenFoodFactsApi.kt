package com.pawlife.app.core.data.network

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path

@Serializable
data class OpenFoodFactsResponse(
    val code: String? = null,
    val status_verbose: String? = null,
    val product: ProductData? = null
)

@Serializable
data class ProductData(
    val product_name: String? = null,
    val brands: String? = null,
    val nutriments: NutrimentsData? = null
)

@Serializable
data class NutrimentsData(
    val energy_kcal_100g: Double? = null
)

interface OpenFoodFactsApi {
    @GET("api/v3/product/{barcode}.json")
    suspend fun getProductInfo(@Path("barcode") barcode: String): OpenFoodFactsResponse
}
