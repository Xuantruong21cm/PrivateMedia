package com.example.privategallery.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.base.common.extensions.ImageViewType
import com.base.common.extensions.loadImageFile
import com.base.common.extensions.loadImageFilePath
import com.base.common.extensions.loadImageUrl
import com.example.privategallery.databinding.ItemDirectoryBinding
import com.simplemobiletools.gallery.pro.models.Directory
import java.io.File

class DirectoriesAdapter : RecyclerView.Adapter<DirectoriesAdapter.DirectoryViewHolder>() {

    var dirs = mutableListOf<Directory>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DirectoryViewHolder {
        return DirectoryViewHolder(ItemDirectoryBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    fun setData(list : ArrayList<Directory>){
        this.dirs.clear()
        val directories =list.clone() as ArrayList<Directory>
        this.dirs.addAll(directories)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return dirs.size
    }

    override fun onBindViewHolder(holder: DirectoryViewHolder, position: Int) {
        val dir = dirs[position]
        holder.mBinding.imgDirectoryCover.loadImageFile(File(dir.tmb),ImageViewType.SQUARE)
        holder.mBinding.tvDirectoryName.text = dir.name
        holder.mBinding.tvItemCount.text = dir.mediaCnt.toString()
    }

    inner class DirectoryViewHolder(val mBinding : ItemDirectoryBinding) : RecyclerView.ViewHolder(mBinding.root)
}