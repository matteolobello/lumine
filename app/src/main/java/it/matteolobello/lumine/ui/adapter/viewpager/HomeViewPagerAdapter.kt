package it.matteolobello.lumine.ui.adapter.viewpager

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentPagerAdapter
import it.matteolobello.lumine.ui.fragment.home.NotesFragment
import it.matteolobello.lumine.ui.fragment.home.RemindersFragment
import it.matteolobello.lumine.ui.fragment.home.TasksFragment
import it.matteolobello.lumine.ui.fragment.home.TrashFragment

class HomeViewPagerAdapter(activity: AppCompatActivity) : FragmentPagerAdapter(activity.supportFragmentManager) {

    private val fragments = arrayListOf(NotesFragment(), TasksFragment(), RemindersFragment(), TrashFragment())

    override fun getItem(position: Int) = fragments[position]

    override fun getCount() = fragments.size
}