package com.kxnst.bugsgame.presentation.rules

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment

import com.kxnst.bugsgame.R
import com.kxnst.bugsgame.databinding.FragmentRulesBinding

class RulesFragment : Fragment(R.layout.fragment_rules) {
    private var _binding: FragmentRulesBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentRulesBinding.bind(view)
        binding.tvRules.text = HtmlCompat.fromHtml(
            getString(R.string.rules_html_content),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        binding.tvRules.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}
