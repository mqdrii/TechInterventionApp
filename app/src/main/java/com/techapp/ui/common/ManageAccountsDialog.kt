package com.techapp.ui.common

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.techapp.R
import com.techapp.data.api.ApiClient
import com.techapp.data.api.UpdateUserRequest
import com.techapp.data.api.UserDto
import com.techapp.data.db.AppDatabase
import com.techapp.data.model.User
import com.techapp.databinding.DialogEditUserBinding
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

        lateinit var adapter: AccountsAdapter
        adapter = AccountsAdapter(
            users = usersList,
            currentUserId = currentUserId,
            onEditClick = { userToEdit ->
                showEditUserDialog(
                    context = context,
                    user = userToEdit,
                    scope = scope,
                    currentUserId = currentUserId,
                    onUpdated = { updatedUser ->
                        val index = usersList.indexOfFirst { it.id == updatedUser.id }
                        if (index != -1) {
                            usersList[index] = updatedUser
                            adapter.notifyItemChanged(index)
                        }
                        onAccountsChanged()
                    }
                )
            },
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

            val localUsers = AppDatabase.getInstance(context).userDao().getAllUsersList()
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

    private fun showEditUserDialog(
        context: Context,
        user: UserDto,
        scope: CoroutineScope,
        currentUserId: Long,
        onUpdated: (UserDto) -> Unit
    ) {
        val editBinding = DialogEditUserBinding.inflate(LayoutInflater.from(context))
        val editDialog = AlertDialog.Builder(context)
            .setView(editBinding.root)
            .create()

        editDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        editBinding.tvEditUserEmail.text = user.email
        editBinding.etFirstName.setText(user.firstName)
        editBinding.etLastName.setText(user.lastName)
        editBinding.etDepartment.setText(user.department)

        val isAdmin = user.role.equals("admin", true)
        if (isAdmin) {
            editBinding.rbRoleAdmin.isChecked = true
        } else {
            editBinding.rbRoleTech.isChecked = true
        }

        editBinding.btnCancel.setOnClickListener { editDialog.dismiss() }

        editBinding.btnSaveUser.setOnClickListener {
            val newFirst = editBinding.etFirstName.text?.toString()?.trim() ?: ""
            val newLast = editBinding.etLastName.text?.toString()?.trim() ?: ""
            val newDept = editBinding.etDepartment.text?.toString()?.trim() ?: "Generale"
            val newRole = if (editBinding.rbRoleAdmin.isChecked) "ADMIN" else "TECHNICIAN"
            val newPass = editBinding.etNewPassword.text?.toString()?.trim()?.takeIf { it.isNotBlank() }

            if (newFirst.isBlank() || newLast.isBlank()) {
                Toast.makeText(context, "Nome e Cognome sono obbligatori", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            editBinding.btnSaveUser.isEnabled = false

            scope.launch {
                val updatedDto = try {
                    val api = ApiClient.getService(context)
                    val response = api.updateUser(
                        id = user.id,
                        req = UpdateUserRequest(
                            firstName = newFirst,
                            lastName = newLast,
                            department = newDept,
                            role = newRole,
                            password = newPass
                        )
                    )
                    if (response.isSuccessful && response.body()?.user != null) {
                        response.body()!!.user!!
                    } else {
                        null
                    }
                } catch (_: Exception) {
                    null
                } ?: UserDto(
                    id = user.id,
                    email = user.email,
                    firstName = newFirst,
                    lastName = newLast,
                    fullName = "$newFirst $newLast",
                    role = newRole,
                    department = newDept
                )

                // Salva nel DB locale Room
                try {
                    val db = AppDatabase.getInstance(context)
                    db.userDao().upsert(
                        User(
                            id = updatedDto.id,
                            email = updatedDto.email,
                            firstName = updatedDto.firstName,
                            lastName = updatedDto.lastName,
                            role = updatedDto.role.lowercase(),
                            department = updatedDto.department,
                            passwordHash = ""
                        )
                    )
                } catch (_: Exception) {}

                // Se l'utente ha modificato se stesso, aggiorna la sessione
                if (user.id == currentUserId) {
                    val session = SessionManager(context)
                    session.saveSession(
                        userId = updatedDto.id,
                        fullName = "${updatedDto.firstName} ${updatedDto.lastName}",
                        email = updatedDto.email,
                        role = updatedDto.role,
                        department = updatedDto.department,
                        token = session.getAuthToken() ?: ""
                    )
                }

                withContext(Dispatchers.Main) {
                    editDialog.dismiss()
                    Toast.makeText(context, "Account di ${updatedDto.firstName} personalizzato con successo!", Toast.LENGTH_SHORT).show()
                    onUpdated(updatedDto)
                }
            }
        }

        editDialog.show()
    }

    private fun confirmDeleteUser(
        context: Context,
        user: UserDto,
        scope: CoroutineScope,
        currentUserId: Long,
        onSuccess: (wasSelf: Boolean) -> Unit
    ) {
        val isSelf = (user.id == currentUserId || user.email.equals(SessionManager(context).getUserEmail(), true))
        val roleLabel = if (user.role.equals("admin", true)) "Amministratore" else "Tecnico"
        val message = if (isSelf) {
            "Stai per eliminare il tuo stesso account (${user.fullName ?: user.firstName}). Verrai disconnesso immediatamente e l'account sarà rimosso dal server.\n\nVuoi procedere?"
        } else {
            "Sei sicuro di voler eliminare l'account di \"${user.fullName ?: user.firstName}\" ($roleLabel)?\n\nL'operazione eliminerà l'account sia dal server cloud che dal dispositivo."
        }

        MaterialAlertDialogBuilder(context)
            .setTitle("Elimina Account")
            .setMessage(message)
            .setIcon(R.drawable.ic_delete)
            .setNegativeButton("Annulla", null)
            .setPositiveButton("Elimina") { _, _ ->
                scope.launch {
                    try {
                        val api = ApiClient.getService(context)
                        api.deleteUser(user.id)
                    } catch (_: Exception) {}

                    try {
                        val db = AppDatabase.getInstance(context)
                        db.userDao().deleteById(user.id)
                        val u = db.userDao().getUserByEmail(user.email)
                        if (u != null) db.userDao().delete(u)
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
        private val onEditClick: (UserDto) -> Unit,
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
                holder.binding.ivUserIcon.setColorFilter(Color.parseColor("#E24C4A"))
                holder.binding.tvUserRoleBadge.text = "AMMINISTRATORE"
                holder.binding.tvUserRoleBadge.setTextColor(Color.parseColor("#E24C4A"))
                holder.binding.tvUserRoleBadge.setBackgroundColor(Color.parseColor("#FCF0F0"))
            } else {
                holder.binding.ivUserIcon.setImageResource(R.drawable.ic_precision_tech)
                holder.binding.ivUserIcon.setColorFilter(Color.parseColor("#1F1F1F"))
                val dept = if (user.department.isNotBlank()) user.department else "Generale"
                holder.binding.tvUserRoleBadge.text = "TECNICO • $dept"
                holder.binding.tvUserRoleBadge.setTextColor(Color.parseColor("#1F1F1F"))
                holder.binding.tvUserRoleBadge.setBackgroundColor(Color.parseColor("#F4F5F7"))
            }

            holder.binding.btnEditUser.setOnClickListener {
                onEditClick(user)
            }

            holder.binding.btnDeleteUser.setOnClickListener {
                onDeleteClick(user)
            }
        }

        override fun getItemCount(): Int = users.size
    }
}
