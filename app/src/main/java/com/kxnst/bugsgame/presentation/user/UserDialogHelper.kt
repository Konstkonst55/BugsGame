package com.kxnst.bugsgame.presentation.user

import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import com.kxnst.bugsgame.R
import com.kxnst.bugsgame.domain.user.UserProfile

object UserDialogHelper {
    fun showUserChoiceDialog(
        fragment: Fragment,
        onRegisterNew: () -> Unit,
        onSelectExisting: () -> Unit
    ) {
        AlertDialog.Builder(fragment.requireContext())
            .setTitle(R.string.user_switch_title)
            .setMessage(R.string.user_switch_message)
            .setPositiveButton(R.string.user_register_new) { _, _ ->
                onRegisterNew()
            }
            .setNegativeButton(R.string.user_select_existing) { _, _ ->
                onSelectExisting()
            }
            .setNeutralButton(R.string.user_cancel, null)
            .show()
    }

    fun showUserSelectionDialog(
        fragment: Fragment,
        users: List<UserProfile>,
        onSelected: (UserProfile) -> Unit
    ) {
        if (users.isEmpty()) {
            AlertDialog.Builder(fragment.requireContext())
                .setTitle(R.string.user_select_title)
                .setMessage(R.string.user_select_empty)
                .setPositiveButton(android.R.string.ok, null)
                .show()
            return
        }

        AlertDialog.Builder(fragment.requireContext())
            .setTitle(R.string.user_select_title)
            .setItems(users.map { it.name }.toTypedArray()) { _, position ->
                onSelected(users[position])
            }
            .setNegativeButton(R.string.user_cancel, null)
            .show()
    }
}
