package com.nutrilogic.product.exception

class ProductNotFoundException(id: Long) : RuntimeException("Product not found with id: $id")