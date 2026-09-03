package com.example.orgamam.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orgamam.model.User
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedOut: Boolean = false
)

class AuthViewModel : ViewModel() {

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            loadUserData(firebaseUser.uid)
        } else {
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }

    fun signIn(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                auth.signInWithEmailAndPassword(email, pass)
                loadUserData(auth.currentUser?.uid ?: "")
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun signUp(email: String, pass: String, displayName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                auth.createUserWithEmailAndPassword(email, pass)
                val uid = auth.currentUser?.uid ?: ""
                val randomColor = generateRandomHexColor()
                val newUser = User(
                    uid = uid, 
                    email = email, 
                    displayName = displayName,
                    hexColor = randomColor
                )
                db.collection("users").document(uid).set(newUser)
                _uiState.update { it.copy(user = newUser, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun generateRandomHexColor(): String {
        val colors = listOf(
            "#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5",
            "#2196F3", "#03A9F4", "#00BCD4", "#009688", "#4CAF50",
            "#8BC34A", "#CDDC39", "#FFEB3B", "#FFC107", "#FF9800", "#FF5722"
        )
        return colors.random()
    }

    fun updateColor(newColor: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("users").document(uid).update("hexColor" to newColor)
                _uiState.update { it.copy(user = it.user?.copy(hexColor = newColor)) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun joinGroup(groupId: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("users").document(uid).update("groupId" to groupId)
                _uiState.update { it.copy(user = it.user?.copy(groupId = groupId)) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateDisplayName(newName: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("users").document(uid).update("displayName" to newName)
                _uiState.update { it.copy(user = it.user?.copy(displayName = newName)) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private fun loadUserData(uid: String) {
        viewModelScope.launch {
            try {
                val doc = db.collection("users").document(uid).get()
                val user = doc.data<User>()
                _uiState.update { it.copy(user = user, isLoading = false, isLoggedOut = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            auth.signOut()
            _uiState.update { it.copy(user = null, isLoggedOut = true) }
        }
    }
}
