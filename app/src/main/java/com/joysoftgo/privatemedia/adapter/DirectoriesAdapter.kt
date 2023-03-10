package com.joysoftgo.privatemedia.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.base.common.extensions.ImageViewType
import com.base.common.extensions.loadImageFile
import com.joysoftgo.privatemedia.databinding.ItemDirectoriesBinding
import com.joysoftgo.privatemedia.model.Directory
import java.io.File

class DirectoriesAdapter(val context : Context) : RecyclerView.Adapter<DirectoriesAdapter.DirectoriesViewHolder>() {

    var dirs = mutableListOf<Directory>()

    fun setData(list : ArrayList<Directory>){
        val directories =list.clone() as ArrayList<Directory>
        this.dirs = directories
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DirectoriesViewHolder {
        return DirectoriesViewHolder(ItemDirectoriesBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun getItemCount(): Int {
        return dirs.size
    }

    override fun onBindViewHolder(holder: DirectoriesViewHolder, position: Int) {
        val dir = dirs[position]
        holder.mBinding.imgDirectoryCover.loadImageFile(File(dir.path),ImageViewType.SQUARE)
        holder.mBinding.tvItemCount.text = dir.mediaCnt.toString()
        holder.mBinding.tvDirectoryName.text = dir.name
    }

    inner class DirectoriesViewHolder(val mBinding : ItemDirectoriesBinding) : RecyclerView.ViewHolder(mBinding.root)
}