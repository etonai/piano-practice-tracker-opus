package com.pseddev.practicetracker.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pseddev.practicetracker.databinding.FragmentViewProgressBinding
import com.google.android.material.tabs.TabLayoutMediator

class ViewProgressFragment : Fragment() {
    
    private var _binding: FragmentViewProgressBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewProgressBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Enable options menu for this fragment
        setHasOptionsMenu(true)
        
        val pagerAdapter = ViewProgressPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        
        // Improve nested scrolling for RecyclerViews inside ViewPager2
        binding.viewPager.isUserInputEnabled = true
        
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Dashboard"
                1 -> "Calendar"
                2 -> "Suggestions"
                3 -> "Pieces"
                4 -> "Timeline"
                5 -> "Inactive"
                else -> ""
            }
        }.attach()
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(com.pseddev.practicetracker.R.menu.progress_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            com.pseddev.practicetracker.R.id.action_settings -> {
                findNavController().navigate(com.pseddev.practicetracker.R.id.action_progressFragment_to_mainFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    private inner class ViewProgressPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 6
        
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> DashboardFragment()
                1 -> CalendarFragment()
                2 -> SuggestionsFragment()
                3 -> PiecesFragment()
                4 -> TimelineFragment()
                5 -> AbandonedFragment()
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }
    }
}