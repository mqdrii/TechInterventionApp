package com.techapp.ui.common

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.techapp.R
import com.techapp.data.api.ApiClient
import com.techapp.data.api.UserDto
import com.techapp.data.db.AppDatabase
import com.techapp.databinding.DialogManageAccountsBinding
import com.techapp.databinding.ItemUserAccountBinding
import com.techapp.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ManageAccountsDialog {

    fun show(
        context: Context,
        scope: CoroutineScope,
        onAccountsChanged: () -> Unit = {},
        onSelfDeleted: () -> Unit = {}
    ) {
        val binding = DialogManageAccountsBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val sessionManager = SessionManager(context)
        val currentUserId = sessionManager.getUserId()
        val usersList = mutableListOf<UserDto>()

        val adapter = AccountsAdapter(
            users = usersList,
            currentUserId = currentUserId,
            onDeleteClick = { userToDelete ->
                confirmDeleteUser(
                    context = context,
                    user = userToDelete,
                    scope = scope,
                    currentUserId = currentUserId,
                    onSuccess = { wasSelf ->
                        if (wasSelf) {
                            dialog.dismiss()
                            sessionManager.clearSession()
                            Toast.makeText(context, "Il tuo account è stato eliminato.", Toast.LENGTH_LONG).show()
                            onSelfDeleted()
                        } else {
                            val index = usersList.indexOfFirst { it.id == userToDelete.id }
                            if (index != -1) {
                                usersList.removeAt(index)
                                binding.rvAccounts.adapter?.notifyItemRemoved(index)
                            }
                            binding.tvAccountsSubtitle.text = "${usersList.size} Account registrati"
                            binding.tvEmpty.visibility = if (usersList.isEmpty()) View.VISIBLE else View.GONE
                            Toast.makeText(context, "Account di ${userToDelete.firstName} eliminato.", Toast.LENGTH_SHORT).show()
                            onAccountsChanged()
                        }
                    }
                )
            }
        )

        binding.rvAccounts.layoutManager = LinearLayoutManager(context)
        binding.rvAccounts.adapter = adapter

        binding.btnClose.setOnClickListener { dialog.dismiss() }
        binding.btnDismiss.setOnClickListener { dialog.dismiss() }

        // Carica la lista degli utenti
        binding.pbLoading.visibility = View.VISIBLE
        scope.launch {
            try {
                // 1) Prova prima dal server
                val api = ApiClient.getService(context)
                val response = api.getAllUsers()
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!
                    withContext(Dispatchers.Main) {
                        usersList.clear()
                        usersList.addAll(list)
                        adapter.notifyDataSetChanged()
                        binding.pbLoading.visibility = View.GONE
                        binding.tvAccountsSubtitle.text = "${usersList.size} Account registrati"
                        binding.tvEmpty.visibility = if (usersList.isEmpty()) View.VISIBLE else View.GONE
                    }
                    return@launch
                }
            } catch (_: Exception) {}

            // 2) Fallback dal DB locale se il server non risponde
            val localUsers = AppDatabase.getInstance(context).userDao().getTechniciansList()
            withContext(Dispatchers.Main) {
                usersList.clear()
                usersList.addAll(localUsers.map { u ->
                    UserDto(
                        id = u.id,
                        email = u.email,
                        firstName = u.firstName,
                        lastName = u.lastName,
                        fullName = u.fullName,
                        role = u.role,
                        department = u.department
                    )
                })
                adapter.notifyDataSetChanged()
                binding.pbLoading.visibility = View.GONE
                binding.tvAccountsSubtitle.text = "${usersList.size} Account registrati"
                binding.tvEmpty.visibility = if (usersList.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        dialog.show()
    }

    private fun confirmDeleteUser(
        context: Context,
        user: UserDto,
        scope: CoroutineScope,
        currentUserId: Long,
        onSuccess: (wasSelf: Boolean) -> Unit
    ) {
        val isSelf = (user.id == currentUserId)
        val roleLabel = if (user.role.equals("admin", true)) "Amministratore" else "Tecnico"
        val message = if (isSelf) {
            "Stai per eliminare il tuo stesso account (${user.fullName}). Verrai disconnesso immediatamente e l'account sarà rimosso dal server.\n\nVuoi procedere?"
        } else {
            "Sei sicuro di voler eliminare l'account di \"${user.fullName}\" ($roleLabel)?\n\nL'operazione eliminerà l'account sia dal server cloud che dal dispositivo."
        }

        MaterialAlertDialogBuilder(context)
            .setTitle("Elimina Account")
            .setMessage(message)
            .setIcon(R.drawable.ic_delete)
            .setNegativeButton("Annulla", null)
            .setPositiveButton("Elimina") { _, _ ->
                scope.launch {
                    // 1) Elimina dal server API
                    try {
                        val api = ApiClient.getService(context)
                        api.deleteUser(user.id)
                    } catch (_: Exception) {}

                    // 2) Elimina dal DB locale
                    try {
                        AppDatabase.getInstance(context).userDao().deleteById(user.id)
                    } catch (_: Exception) {}

                    withContext(Dispatchers.Main) {
                        onSuccess(isSelf)
                    }
                }
            }
            .show()
    }

    private class AccountsAdapter(
        private val users: List<UserDto>,
        private val currentUserId: Long,
        private val onDeleteClick: (UserDto) -> Unit
    ) : RecyclerView.Adapter<AccountsAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemUserAccountBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemUserAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val user = users[position]
            val isSelf = (user.id == currentUserId)
            val isAdmin = user.role.equals("admin", true)

            holder.binding.tvUserName.text = if (isSelf) "${user.fullName ?: "${user.firstName} ${user.lastName}"} (Tu)" else (user.fullName ?: "${user.firstName} ${user.lastName}")
            holder.binding.tvUserEmail.text = user.email

            if (isAdmin) {
                holder.binding.ivUserIcon.setImageResource(R.drawable.ic_shield_admin)
                holder.binding.ivUserIcon.setColorFilter(Color.parseColor("#D97706"))
                holder.binding.tvUserRoleBadge.text = "AMMINISTRATORE"
                holder.binding.tvUserRoleBadge.setTextColor(Color.parseColor("#B45309"))
                holder.binding.tvUserRoleBadge.setBackgroundColor(Color.parseColor("#FEF3C7"))
            } else {
                holder.binding.ivUserIcon.setImageResource(R.drawable.ic_precision_tech)
                holder.binding.ivUserIcon.setColorFilter(Color.parseColor("#059669"))
                val dept = if (user.department.isNotBlank()) user.department else "Generale"
                holder.binding.tvUserRoleBadge.text = "TECNICO • $dept"
                holder.binding.tvUserRoleBadge.setTextColor(Color.parseColor("#047857"))
                holder.binding.tvUserRoleBadge.setBackgroundColor(Color.parseColor("#D1FAE5"))
            }

            holder.binding.btnDeleteUser.setOnClickListener {
                onDeleteClick(user)
            }
        }

        override fun getItemCount(): Int = users.size
    }
}
