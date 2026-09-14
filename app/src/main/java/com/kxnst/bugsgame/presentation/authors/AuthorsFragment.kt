package com.kxnst.bugsgame.presentation.authors

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

import com.kxnst.bugsgame.R
import com.kxnst.bugsgame.databinding.FragmentAuthorsBinding

class AuthorsFragment : Fragment(R.layout.fragment_authors) {
    private var _binding: FragmentAuthorsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentAuthorsBinding.bind(view)
        binding.lvAuthors.adapter = AuthorAdapter(
            listOf(
                Author(
                    name = getString(R.string.authors_name),
                    imageResId = R.drawable.ph_author_first
                ),
                Author(
                    name = getString(R.string.authors_name_bugs_game_team),
                    imageResId = R.drawable.ph_author_second
                )
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}
