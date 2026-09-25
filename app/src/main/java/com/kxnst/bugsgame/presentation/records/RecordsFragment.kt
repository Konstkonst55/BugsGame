package com.kxnst.bugsgame.presentation.records

import android.os.Bundle
import android.view.View

import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager

import com.kxnst.bugsgame.R
import com.kxnst.bugsgame.databinding.FragmentRecordsBinding
import com.kxnst.bugsgame.presentation.user.UserListState
import com.kxnst.bugsgame.presentation.user.UserSessionViewModel

import kotlinx.coroutines.launch

import org.koin.androidx.viewmodel.ext.android.activityViewModel

class RecordsFragment : Fragment(R.layout.fragment_records) {
    private var _binding: FragmentRecordsBinding? = null
    private val binding get() = _binding!!
    private val userSessionViewModel: UserSessionViewModel by activityViewModel()
    private lateinit var adapter: RecordAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentRecordsBinding.bind(view)
        adapter = RecordAdapter()
        binding.rvRecords.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecords.adapter = adapter

        observeState()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    userSessionViewModel.usersState.collect { state ->
                        renderState(state)
                    }
                }

                launch {
                    userSessionViewModel.currentUser.collect { user ->
                        adapter.setCurrentUser(user?.name)
                    }
                }
            }
        }
    }

    private fun renderState(state: UserListState) {
        binding.pbRecords.isVisible = state is UserListState.Loading
        binding.tvEmptyRecords.isVisible = state is UserListState.Empty || state is UserListState.Error
        binding.rvRecords.isVisible = state is UserListState.Content

        when (state) {
            UserListState.Loading -> Unit

            UserListState.Empty -> {
                binding.tvEmptyRecords.text = getString(R.string.records_empty)
            }

            is UserListState.Error -> {
                binding.tvEmptyRecords.text = getString(R.string.records_error)
            }

            is UserListState.Content -> {
                adapter.submitList(state.users)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null

        super.onDestroyView()
    }
}
