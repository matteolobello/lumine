package it.matteolobello.lumine.ui.adapter.viewpager

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentPagerAdapter
import it.matteolobello.lumine.R
import it.matteolobello.lumine.ui.fragment.base.OnboardingFragment

class OnboardingViewPagerAdapter(activity: AppCompatActivity) : FragmentPagerAdapter(activity.supportFragmentManager) {

    private val fragments = arrayListOf(
            OnboardingFragment.newInstance(
                    title = activity.getString(R.string.onboarding_first_title),
                    description = activity.getString(R.string.onboarding_first_description),
                    imageRes = R.drawable.onboarding_1
            ),
            OnboardingFragment.newInstance(
                    title = activity.getString(R.string.onboarding_second_title),
                    description = activity.getString(R.string.onboarding_second_description),
                    imageRes = R.drawable.onboarding_2
            ),
            OnboardingFragment.newInstance(
                    title = activity.getString(R.string.onboarding_third_title),
                    description = activity.getString(R.string.onboarding_third_description),
                    imageRes = R.drawable.onboarding_3,
                    lastElement = true
            )
    )

    override fun getItem(position: Int) = fragments[position]

    override fun getCount() = fragments.size
}