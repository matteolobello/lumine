package it.matteolobello.lumine.ui.fragment.home

import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import it.matteolobello.lumine.R
import it.matteolobello.lumine.ui.adapter.recyclerview.RemindersRecyclerViewAdapter
import it.matteolobello.lumine.ui.fragment.base.HomeFragment
import kotlinx.android.synthetic.main.fragment_reminders.*

class RemindersFragment : HomeFragment() {

    private val remindersAdapter = RemindersRecyclerViewAdapter()

    override fun getLayoutRes() = R.layout.fragment_reminders

    override fun setupUi(rootView: View) {
        remindersRecyclerView.layoutManager = LinearLayoutManager(context)
        remindersRecyclerView.adapter = remindersAdapter

        homeActivity.viewModel.reminders.observe(this, Observer {
            remindersAdapter.setReminders(it)

            if (remindersAdapter.itemCount == 0) {
                remindersContentWrapper.visibility = View.INVISIBLE
                emptyRemindersWrapper.visibility = View.VISIBLE
            } else {
                remindersContentWrapper.visibility = View.VISIBLE
                emptyRemindersWrapper.visibility = View.INVISIBLE
            }
        })
    }
}