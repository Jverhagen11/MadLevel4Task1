package com.example.madlevel4task1.Repository

import android.content.Context
import com.example.madlevel4task1.Dao.ProductDao
import com.example.madlevel4task1.Database.ShoppingListRoomDatabase
import com.example.madlevel4task1.Model.Product


class ProductRepository(context: Context) {

    private val productDao: ProductDao

    init {
        val database = ShoppingListRoomDatabase.getDatabase(context)
        productDao = database!!.productDao()
    }

    suspend fun getAllProducts(): List<Product> {
        return productDao.getAllProducts()
    }

    suspend fun insertProduct(product: Product) {
        productDao.insertProduct(product)
    }

    suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product)
    }

    suspend fun deleteAllProducts() {
        productDao.deleteAllProducts()
    }

}
