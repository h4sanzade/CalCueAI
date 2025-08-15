package com.hasanzade.calcueai

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class VerificationFragment : Fragment() {

    private val args: VerificationFragmentArgs by navArgs()
    private val viewModel: VerificationViewModel by viewModels()

    private lateinit var otpInputs: Array<EditText>
    private lateinit var verifyButton: MaterialButton
    private lateinit var backButton: View
    private lateinit var resendCodeText: TextView
    private lateinit var didntReceiveText: TextView
    private lateinit var timerText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login_verification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupOtpInputs()
        setupClickListeners()
        observeViewModel()
        updateEmailDisplay()
    }

    private fun initViews(view: View) {
        otpInputs = arrayOf(
            view.findViewById(R.id.otp_1),
            view.findViewById(R.id.otp_2),
            view.findViewById(R.id.otp_3),
            view.findViewById(R.id.otp_4),
            view.findViewById(R.id.otp_5),
            view.findViewById(R.id.otp_6)
        )
        verifyButton = view.findViewById(R.id.verify_button)
        backButton = view.findViewById(R.id.back_button)
        resendCodeText = view.findViewById(R.id.resend_code_text)
        didntReceiveText = view.findViewById(R.id.didnt_receive_code_text)
        timerText = view.findViewById(R.id.timer_text)
    }

    private fun setupOtpInputs() {
        otpInputs.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && index < otpInputs.size - 1) {
                        otpInputs[index + 1].requestFocus()
                    }
                    updateOtpCode()
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            editText.setOnKeyListener { _, keyCode, _ ->
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL && editText.text.isEmpty() && index > 0) {
                    otpInputs[index - 1].requestFocus()
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun updateOtpCode() {
        val code = otpInputs.joinToString("") { it.text.toString() }
        viewModel.updateOtpCode(code)
    }

    private fun setupClickListeners() {
        verifyButton.setOnClickListener {
            viewModel.verifyOtp()
        }

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        resendCodeText.setOnClickListener {
            viewModel.resendCode()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.verificationState.collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        verifyButton.isEnabled = false
                        verifyButton.text = "Verifying..."
                    }
                    is AuthResult.Success -> {
                        verifyButton.isEnabled = true
                        verifyButton.text = "Verify"

                        if (args.isFromSignUp) {
                            findNavController().navigate(
                                R.id.action_verificationFragment_to_loginFragment
                            )
                        } else {
                            val action = VerificationFragmentDirections
                                .actionVerificationFragmentToResetPasswordFragment(args.email)
                            findNavController().navigate(action)
                        }
                    }
                    is AuthResult.Error -> {
                        verifyButton.isEnabled = true
                        verifyButton.text = "Verify"

                        if (result.message == "Wrong") {
                            // Show wrong OTP indication
                            otpInputs.forEach { it.setBackgroundResource(R.drawable.otp_box_error) }
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.resendTimer.collect { seconds ->
                if (seconds > 0) {
                    timerText.visibility = View.VISIBLE
                    timerText.text = "00:${String.format("%02d", seconds)}"
                } else {
                    timerText.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.canResend.collect { canResend ->
                resendCodeText.isEnabled = canResend
                resendCodeText.alpha = if (canResend) 1.0f else 0.5f
            }
        }
    }

    private fun updateEmailDisplay() {
        view?.findViewById<TextView>(R.id.email_display)?.text =
            "Enter the code we've sent by text to \n${args.email}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.resetVerificationState()
    }
}