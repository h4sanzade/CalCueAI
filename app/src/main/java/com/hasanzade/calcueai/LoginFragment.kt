package com.hasanzade.calcueai

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(
            FirebaseModule.provideAuthRepository(requireContext())
        )
    }

    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var loginButton: MaterialButton
    private lateinit var forgotPasswordText: TextView
    private lateinit var signUpText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupSignUpText()
        setupClickListeners()
        observeViewModel()
    }

    private fun initViews(view: View) {
        emailInput = view.findViewById(R.id.emailEditText)
        passwordInput = view.findViewById(R.id.passwordEditText)
        emailLayout = view.findViewById(R.id.emailInputLayout)
        passwordLayout = view.findViewById(R.id.passwordInputLayout)
        loginButton = view.findViewById(R.id.login_button)
        forgotPasswordText = view.findViewById(R.id.forgot_password_text)
        signUpText = view.findViewById(R.id.sign_up_text)
    }

    private fun setupSignUpText() {
        val fullText = "Don't have an account? Sign Up"
        val spannableString = SpannableString(fullText)
        val orangeColor = ContextCompat.getColor(requireContext(), R.color.orange)

        val startIndex = fullText.indexOf("Sign Up")
        val endIndex = startIndex + "Sign Up".length

        spannableString.setSpan(
            ForegroundColorSpan(orangeColor),
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        signUpText.text = spannableString
    }

    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            viewModel.signIn(email, password)
        }

        forgotPasswordText.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }

        signUpText.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpDefaultFragment)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.authState.collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        loginButton.isEnabled = false
                        loginButton.text = "Logging in..."
                    }
                    is AuthResult.Success -> {
                        loginButton.isEnabled = true
                        loginButton.text = "Log In"
                        // Navigate to main app when implemented
                        // For now, just show success
                    }
                    is AuthResult.Error -> {
                        loginButton.isEnabled = true
                        loginButton.text = "Log In"
                        // Error handling is done by validation observer
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.loginValidation.collect { validation ->
                // Handle email validation
                if (validation.emailError != null) {
                    emailLayout.error = validation.emailError
                    emailLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.red)
                } else {
                    emailLayout.error = null
                    emailLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.gray)
                }

                // Handle password validation
                if (validation.passwordError != null) {
                    passwordLayout.error = validation.passwordError
                    passwordLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.red)
                } else {
                    passwordLayout.error = null
                    passwordLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.gray)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearValidationErrors()
    }
}