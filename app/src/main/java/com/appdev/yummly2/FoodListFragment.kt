package com.appdev.yummly2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appdev.yummly2.databinding.FragmentFoodListBinding
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase
class FoodListFragment : Fragment() {

  private lateinit var listBinding: FragmentFoodListBinding
  private lateinit var recyclerView: RecyclerView
  private lateinit var adapter: FoodAdapter

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
    if(::recyclerView.isInitialized) {
      val options: FirebaseRecyclerOptions<Food> = FirebaseRecyclerOptions.Builder<Food>()
        .setQuery(FirebaseDatabase.getInstance().reference.child("foods")
          .orderByChild("b_name")
          .startAt(str)
          .endAt("$str~"), Food::class.java)
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