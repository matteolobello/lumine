package it.matteolobello.lumine.ui.fragment.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import it.matteolobello.lumine.R
import it.matteolobello.lumine.data.bundle.BundleKeys
import it.matteolobello.lumine.ui.activity.OnboardingActivity
import kotlinx.android.synthetic.main.fragment_onboarding.*

class OnboardingFragment : Fragment() {

    companion object {

        fun newInstance(
                title: String,
                description: String,
                imageRes: Int,
                lastElement: Boolean = false
        ): OnboardingFragment = OnboardingFragment().apply {
            arguments = Bundle().apply {
                putString(BundleKeys.EXTRA_ONBOARDING_TITLE, title)
                putString(BundleKeys.EXTRA_ONBOARDING_DESCRIPTION, description)
                putInt(BundleKeys.EXTRA_ONBOARDING_IMAGE_RES, imageRes)
                putBoolean(BundleKeys.EXTRA_ONBOARDING_LAST_ELEMENT, lastElement)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.fragment_onboarding, container, false)!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val title = it.getString(BundleKeys.EXTRA_ONBOARDING_TITLE)
            val description = it.getString(BundleKeys.EXTRA_ONBOARDING_DESCRIPTION)
            val imageRes = it.getInt(BundleKeys.EXTRA_ONBOARDING_IMAGE_RES)
            val lastElement = it.getBoolean(BundleKeys.EXTRA_ONBOARDING_LAST_ELEMENT)

            onboardingTitleTextView.text = title
            onboardingDescriptionTextView.text = description
            onboardingImageView.setImageResource(imageRes)

            if (lastElement) {
                onboardingButton.text = getString(R.string.get_started)
                onboardingButton.setOnClickListener {
                    if (activity is OnboardingActivity) {
                        (activity as OnboardingActivity).onGetStartedButtonClick()
                    }
                }
            } else {
                onboardingButton.text = getString(R.string.skip)
                onboardingButton.setOnClickListener {
                    if (activity is OnboardingActivity) {
                        (activity as OnboardingActivity).onSkipButtonClick()
                    }
                }
            }
        }
    }
}
