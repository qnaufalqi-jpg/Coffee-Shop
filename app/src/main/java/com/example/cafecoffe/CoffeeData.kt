package com.example.cafecoffe

data class CoffeeItem(
    val id: Int,
    val name: String,
    val category: String,
    val subtitle: String,
    val description: String,
    val price: Double,
    val rating: Double,
    val reviewsCount: Int,
    val colorRes: Long // Placeholder color for UI
)

val sampleCoffees = listOf(
    CoffeeItem(
        id = 1,
        name = "Caffe Mocha",
        category = "Macchiato",
        subtitle = "Deep Foam",
        description = "Coffee mocha is a chocolate-flavored warm beverage that is a variant of a caffè latte, commonly held to be the American equivalent of mochaccino.",
        price = 4.53,
        rating = 4.8,
        reviewsCount = 230,
        colorRes = 0xFFC67C4E
    ),
    CoffeeItem(
        id = 2,
        name = "Flat White",
        category = "Latte",
        subtitle = "Espresso",
        description = "A flat white is a coffee drink consisting of espresso with microfoam (steamed milk with small, fine bubbles and a glossy or velvety consistency).",
        price = 3.53,
        rating = 4.5,
        reviewsCount = 128,
        colorRes = 0xFF8B5A2B
    ),
    CoffeeItem(
        id = 3,
        name = "Boiled Espresso",
        category = "Americano",
        subtitle = "Ice/Hot",
        description = "A bold shot of pure espresso boiled and prepared with precision, delivering a robust punch of caffeine and deep rich aromas.",
        price = 2.89,
        rating = 4.7,
        reviewsCount = 95,
        colorRes = 0xFF313131
    ),
    CoffeeItem(
        id = 4,
        name = "Mocha Frap",
        category = "Macchiato",
        subtitle = "Chocolate",
        description = "Blended iced mocha frappuccino topped with whipped cream and rich chocolate drizzle. Perfect for warm afternoons.",
        price = 5.20,
        rating = 4.9,
        reviewsCount = 310,
        colorRes = 0xFFD29064
    ),
    CoffeeItem(
        id = 5,
        name = "Caramel Macchiato",
        category = "Latte",
        subtitle = "Caramel drizzle",
        description = "Freshly steamed milk with vanilla-flavored syrup marked with espresso and finished with caramel drizzle.",
        price = 4.80,
        rating = 4.8,
        reviewsCount = 420,
        colorRes = 0xFFED9D59
    )
)

val categories = listOf("All Coffee", "Macchiato", "Latte", "Americano", "Expresso")
