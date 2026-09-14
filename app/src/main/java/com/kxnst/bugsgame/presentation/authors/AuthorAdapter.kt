package com.kxnst.bugsgame.presentation.authors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

import com.kxnst.bugsgame.R
import com.kxnst.bugsgame.databinding.ItemAuthorBinding

class AuthorAdapter(
    private val authors: List<Author>
) : BaseAdapter() {
    override fun getCount(): Int = authors.size

    override fun getItem(position: Int): Author = authors[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val binding = if (convertView == null) {
            ItemAuthorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        } else {
            ItemAuthorBinding.bind(convertView)
        }

        val author = getItem(position)

        binding.ivAuthor.setImageResource(author.imageResId)
        binding.ivAuthor.contentDescription = parent.context.getString(
            R.string.authors_cd_author_photo
        )
        binding.tvAuthorName.text = author.name

        return binding.root
    }
}
