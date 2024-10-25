package com.summitcodeworks.apptesters.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.summitcodeworks.apptesters.fragments.AddFragment
import com.summitcodeworks.apptesters.fragments.HomeFragment
import com.summitcodeworks.apptesters.fragments.ProfileFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3 // Total number of fragments

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> AddFragment()
            2 -> ProfileFragment()
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}