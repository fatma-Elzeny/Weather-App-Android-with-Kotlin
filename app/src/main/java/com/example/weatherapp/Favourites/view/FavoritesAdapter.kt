package com.example.weatherapp.Favourites.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.data.model.FavoriteLocation

class FavoritesAdapter(
    private val onItemClick: (FavoriteLocation) -> Unit,
    private val onDeleteClick: (FavoriteLocation) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    private val places = mutableListOf<FavoriteLocation>()

    fun submitList(newList: List<FavoriteLocation>) {
        places.clear()
        places.addAll(newList)
        notifyDataSetChanged()
    }

    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val placeName: TextView = itemView.findViewById(R.id.tvPlaceName)
        private val btnOptions: ImageButton = itemView.findViewById(R.id.btnOptions)

        fun bind(place: FavoriteLocation) {
            placeName.text = place.name

            itemView.setOnClickListener {
                onItemClick(place)
            }

            btnOptions.setOnClickListener {
                showDeleteDialog(place)
            }
        }

        private fun showDeleteDialog(place: FavoriteLocation) {
            AlertDialog.Builder(itemView.context)
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete this place?")
                .setPositiveButton("Yes") { _, _ -> onDeleteClick(place) }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_place, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(places[position])
    }

    override fun getItemCount(): Int = places.size
}