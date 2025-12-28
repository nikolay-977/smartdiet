package ru.smartdiet.products.dto

data class ProductResponse(
    val barcode: String,
    val name: String,
    val nameRu: String? = null,
    val brand: String? = null,
    val quantity: String? = null,

    // КБЖУ
    val calories: Double? = null,
    val proteins: Double? = null,
    val carbohydrates: Double? = null,
    val fats: Double? = null,

    // Важные нутриенты
    val fiber: Double? = null,
    val calcium: Double? = null,
    val omega3: Double? = null,
    val saturatedFat: Double? = null,
    val iron: Double? = null,
    val sodium: Double? = null,
    val potassium: Double? = null,
    val choline: Double? = null,
    val caffeine: Double? = null,

    // Дополнительная информация
    val ingredients: String? = null,
    val ingredientsRu: String? = null,
    val categories: String? = null,
    val categoriesRu: String? = null,
    val nutriscore: String? = null,
    val ecoscore: String? = null,

    val source: String = "unknown"
)