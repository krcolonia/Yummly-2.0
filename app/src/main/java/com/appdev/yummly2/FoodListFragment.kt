package com.appdev.yummly2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appdev.yummly2.databinding.FragmentFoodListBinding
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase

class FoodListFragment : Fragment() {

  private lateinit var listBinding: FragmentFoodListBinding
  private lateinit var recyclerView: RecyclerView
  private lateinit var adapter: FoodAdapter

  private var userId: String? = null

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    listBinding = FragmentFoodListBinding.inflate(inflater, container, false)
    val view = listBinding.root

    recyclerView = listBinding.foodRecycler
    recyclerView.layoutManager = LinearLayoutManager(requireContext())

    val dbRef = FirebaseDatabase.getInstance().reference.child("foods")

    val options: FirebaseRecyclerOptions<Food> = FirebaseRecyclerOptions.Builder<Food>()
      .setQuery(dbRef, Food::class.java)
      .build()

    adapter = FoodAdapter(options)
    recyclerView.adapter = adapter

    val spinner = listBinding.filterSpinner
    val listAdapter = ArrayAdapter.createFromResource(
      listBinding.root.context,
      R.array.filterOrigin,
      android.R.layout.simple_spinner_item
    )
    listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    spinner.adapter = listAdapter

    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
      override fun onItemSelected(
        parentView: AdapterView<*>,
        selectedItemView: View?,
        position: Int,
        id: Long
      ) {
        val selectedFilter = if (position == 0) {
          "" // Filter nothing, so that all items show up
        } else {
          parentView.selectedItem.toString()
        }

        if(position != 0) {
          Toast.makeText(listBinding.root.context, "Filtering by: $selectedFilter Cuisine", Toast.LENGTH_SHORT).show()
        }

        filterOrigin(selectedFilter)
      }

      override fun onNothingSelected(p0: AdapterView<*>) {
        // do nothing...
      }
    }
    // Inflate the layout for this fragment
    return view
  }

  override fun onStart() {
    super.onStart()
    adapter.startListening()
    adapter.notifyDataSetChanged()
  }

  override fun onStop() {
    super.onStop()
    adapter.stopListening()
  }

  fun searchFood(str: String?) {
    if (::recyclerView.isInitialized) {
      val options: FirebaseRecyclerOptions<Food> = FirebaseRecyclerOptions.Builder<Food>()
        .setQuery(
          FirebaseDatabase.getInstance().reference.child("foods")
            .orderByChild("b_name")
            .startAt(str)
            .endAt("$str~"), Food::class.java
        )
        .build()

      adapter = FoodAdapter(options)
      adapter.startListening()
      recyclerView.adapter = adapter
    }
  }

  fun filterOrigin(str: String?) {
    if (::recyclerView.isInitialized) {
      val options: FirebaseRecyclerOptions<Food> = FirebaseRecyclerOptions.Builder<Food>()
        .setQuery(
          FirebaseDatabase.getInstance().reference.child("foods")
            .orderByChild("d_origin")
            .startAt(str)
            .endAt("$str~"), Food::class.java
        )
        .build()

      adapter = FoodAdapter(options)
      adapter.startListening()
      recyclerView.adapter = adapter
    }
  }

  fun filterUserRecipe(str: String) {
    userId = str
    if (::recyclerView.isInitialized) {
      val options: FirebaseRecyclerOptions<Food> = FirebaseRecyclerOptions.Builder<Food>()
        .setQuery(
          FirebaseDatabase.getInstance().reference.child("foods")
            .orderByChild("b_authid")
            .startAt(userId)
            .endAt("$userId~"), Food::class.java
        )
        .build()

      adapter = FoodAdapter(options)
      adapter.startListening()
      recyclerView.adapter = adapter
    }
  }

  companion object {
    @JvmStatic
    fun newInstance() = FoodListFragment()
  }
}