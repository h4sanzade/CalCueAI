package com.hasanzade.calcueai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch

class ThirdOnBoardingFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(
            FirebaseModule.provideAuthRepository(requireContext())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_third_on_boarding, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListener(view)
    }

    private fun setupClickListener(view: View) {
        view.findViewById<View>(R.id.next_marker).setOnClickListener {
            completeOnboardingAndNavigate()
        }
    }

    private fun completeOnboardingAndNavigate() {
        lifecycleScope.launch {
            try {
                viewModel.completeOnboarding()

                // Navigate to login screen
                findNavController().navigate(
                    R.id.action_thirdOnBoardingFragment_to_loginFragment
                )
            } catch (e: Exception) {
                // Handle any potential errors
                findNavController().navigate(
                    R.id.action_thirdOnBoardingFragment_to_loginFragment
                )
            }
        }
    }
}