package com.example.madlevel4task1

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AlertDialogLayout
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madlevel4task1.Model.Product
import com.example.madlevel4task1.Repository.ProductRepository
import com.example.madlevel4task1.databinding.FragmentShoppinglistBinding
import kotlinx.android.synthetic.main.fragment_product.*
import kotlinx.android.synthetic.main.fragment_shoppinglist.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ShoppingListFragment : Fragment() {

    private lateinit var productRepository: ProductRepository
    private val products = arrayListOf<Product>()
    private lateinit var binding: FragmentShoppinglistBinding
    private val productAdapter = ProductAdapter(products)
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private lateinit var viewManager: RecyclerView.LayoutManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentShoppinglistBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productRepository = ProductRepository(requireContext())
        getShoppingListFromDatabase()
        initRv()

        fabAdd.setOnClickListener {
            showAddProductDialog();
        }


        fabDelete.setOnClickListener {
            removeAllProducts()
        }

    }


    private fun initRv() {
        viewManager = LinearLayoutManager(activity)
        rvProducts.addItemDecoration(
            DividerItemDecoration(
                activity,
                DividerItemDecoration.VERTICAL
            )
        )

        createItemTouchHelper().attachToRecyclerView(rvProducts)

        rvProducts.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = productAdapter
        }
    }

    private fun getShoppingListFromDatabase() {
        mainScope.launch {
            val products = withContext(Dispatchers.IO) {
                productRepository.getAllProducts()
            }
            this@ShoppingListFragment.products.clear()
            this@ShoppingListFragment.products.addAll(products)
            this@ShoppingListFragment.productAdapter.notifyDataSetChanged()
        }
    }

    private fun createItemTouchHelper(): ItemTouchHelper {

        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition



                mainScope.launch {
                    withContext(Dispatchers.IO) {
                        productRepository.deleteProduct(products[position])
                    }
                    getShoppingListFromDatabase()
                }
            }
        }
        return ItemTouchHelper(callback)
    }

    @SuppressLint("InflateParams")
    private fun showAddProductDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.title))
        val dialogLayout = layoutInflater.inflate(R.layout.fragment_add_product, null)
        val productName = dialogLayout.findViewById<EditText>(R.id.etProductName)
        val amount = dialogLayout.findViewById<EditText>(R.id.etAmount)

        builder.setView(dialogLayout)
        builder.setPositiveButton(R.string.add_btn) { _: DialogInterface, _: Int ->
            addProduct(productName, amount)
        }
        builder.show()
    }

    private fun addProduct(txtProductName: EditText, txtAmount: EditText) {
        if (validateFields(txtProductName, txtAmount)) {
            mainScope.launch {
                val product = Product(
                    productName = txtProductName.text.toString(),
                    quantity = txtAmount.text.toString().toLong()
                )

                withContext(Dispatchers.IO) {
                    productRepository.insertProduct(product)
                }

                getShoppingListFromDatabase()
            }
        }
    }

    private fun validateFields(
        txtProductName: EditText
        , txtAmount: EditText
    ): Boolean {
        return if (txtProductName.text.toString().isNotBlank()
            && txtAmount.text.toString().isNotBlank()
        ) {
            true
        } else {

            Toast.makeText(activity, "Please fill in the fields", Toast.LENGTH_LONG).show()
            false
        }
    }

    private fun removeAllProducts() {
        mainScope.launch {
            withContext(Dispatchers.IO) {
                productRepository.deleteAllProducts()
            }
            getShoppingListFromDatabase()
        }
    }

}
