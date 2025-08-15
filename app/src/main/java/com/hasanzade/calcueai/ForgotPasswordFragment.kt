package com.hasanzade.calcueai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class ForgotPasswordFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(
            FirebaseModule.provideAuthRepository(requireContext())
        )
    }

    private lateinit var emailInput: TextInputEditText
    private lateinit var emailLayout: TextInputLayout
    private lateinit var sendResetButton: MaterialButton
    private lateinit var backButton: View
    private lateinit var backToLoginText: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupClickListeners()
        observeViewModel()
    }

    private fun initViews(view: View) {
        emailInput = view.findViewById(R.id.emailEditText)
        emailLayout = view.findViewById(R.id.emailInputLayout)
        sendResetButton = view.findViewById(R.id.reset_link_button)
        backButton = view.findViewById(R.id.back_button)
        backToLoginText = view.findViewById(R.id.back_to_login_text)
    }

    private fun setupClickListeners() {
        sendResetButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            viewModel.sendPasswordReset(email)
        }

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        backToLoginText.setOnClickListener {
            findNavController().navigate(R.id.action_forgotPasswordFragment_to_loginFragment)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.authState.collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        sendResetButton.isEnabled = false
                        sendResetButton.text = "Sending..."
                    }
                    is AuthResult.Success -> {
                        sendResetButton.isEnabled = true
                        sendResetButton.text = "Send Reset Link"

                        val email = emailInput.text.toString().trim()
                        val action = ForgotPasswordFragmentDirections
                            .actionForgotPasswordFragmentToVerificationFragment(
                                email = email,
                                isFromSignUp = false
                            )
                        findNavController().navigate(action)
                    }
                    is AuthResult.Error -> {
                        sendResetButton.isEnabled = true
                        sendResetButton.text = "Send Reset Link"

                        if (result.message.contains("email")) {
                            emailLayout.error = result.message
                            emailLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.red)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.resetAuthState()
    }
}