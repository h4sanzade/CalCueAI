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

class SignUpDefaultFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels()

    private lateinit var fullNameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var fullNameLayout: TextInputLayout
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var signUpButton: MaterialButton
    private lateinit var backButton: View
    private lateinit var loginText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up_default, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupLoginText()
        setupClickListeners()
        observeViewModel()
    }

    private fun initViews(view: View) {
        fullNameInput = view.findViewById(R.id.full_name_edit_text)
        emailInput = view.findViewById(R.id.email_signup_edit_text)
        passwordInput = view.findViewById(R.id.password_signup_edit_text)
        fullNameLayout = view.findViewById(R.id.full_name_layout)
        emailLayout = view.findViewById(R.id.email_signup_layout)
        passwordLayout = view.findViewById(R.id.password_signup_layout)
        signUpButton = view.findViewById(R.id.sign_up_button)
        backButton = view.findViewById(R.id.back_button)
        loginText = view.findViewById(R.id.login_text)
    }

    private fun setupLoginText() {
        val fullText = "Already have an account? Log in"
        val spannableString = SpannableString(fullText)
        val orangeColor = ContextCompat.getColor(requireContext(), R.color.orange)

        val startIndex = fullText.indexOf("Log in")
        val endIndex = startIndex + "Log in".length

        spannableString.setSpan(
            ForegroundColorSpan(orangeColor),
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        loginText.text = spannableString
    }

    private fun setupClickListeners() {
        signUpButton.setOnClickListener {
            val fullName = fullNameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            viewModel.signUp(email, password, fullName)
        }

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        loginText.setOnClickListener {
            findNavController().navigate(R.id.action_signUpDefaultFragment_to_loginFragment)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.authState.collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        signUpButton.isEnabled = false
                        signUpButton.text = "Creating Account..."
                    }
                    is AuthResult.Success -> {
                        signUpButton.isEnabled = true
                        signUpButton.text = "Sign Up"

                        val email = emailInput.text.toString().trim()
                        val action = SignUpDefaultFragmentDirections
                            .actionSignUpDefaultFragmentToVerificationFragment(
                                email = email,
                                isFromSignUp = true
                            )
                        findNavController().navigate(action)
                    }
                    is AuthResult.Error -> {
                        signUpButton.isEnabled = true
                        signUpButton.text = "Sign Up"
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.signUpValidation.collect { validation ->
                // Handle full name validation
                if (validation.nameError != null) {
                    fullNameLayout.error = validation.nameError
                    fullNameLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.red)
                } else {
                    fullNameLayout.error = null
                    fullNameLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.gray)
                }

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