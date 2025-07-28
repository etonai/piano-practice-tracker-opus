/*
 * Timeline Tab Temporarily Disabled for Evaluation
 * 
 * DevCycle 2025-0005 Phase 4: Timeline tab has been temporarily removed to evaluate
 * user preference for Calendar Activities Detail Mode vs. separate Timeline functionality.
 * 
 * RESTORATION PROCESS:
 * 1. In setupTabs() TabLayoutMediator: Uncomment line 65 and move line 66 to position 5
 * 2. In ViewProgressPagerAdapter: Change getItemCount() back to (6/5) and uncomment line 106
 * 3. Restore data loading in TimelineViewModel.kt and TimelineFragment.kt
 * 
 * All Timeline code is preserved via comments for quick restoration.
 */
package com.pseddev.playstreak.ui.progress

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
import com.pseddev.playstreak.databinding.FragmentViewProgressBinding
import com.pseddev.playstreak.utils.ProUserManager
import com.google.android.material.tabs.TabLayoutMediator

class ViewProgressFragment : Fragment() {
    
    private var _binding: FragmentViewProgressBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var proUserManager: ProUserManager
    
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
        
        // Initialize ProUserManager
        proUserManager = ProUserManager.getInstance(requireContext())
        
        // Enable options menu for this fragment
        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)
        
        setupTabs()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh tabs when returning to fragment (in case Pro status changed)
        setupTabs()
    }
    
    private fun setupTabs() {
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
                // 4 -> "Timeline"  // Timeline tab temporarily disabled for evaluation
                4 -> if (proUserManager.isProUser()) "Inactive" else ""
                else -> ""
            }
        }.attach()
    }
    
    @Suppress("DEPRECATION")
    @Deprecated("Using deprecated Fragment menu API")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(com.pseddev.playstreak.R.menu.progress_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    
    @Suppress("DEPRECATION")
    @Deprecated("Using deprecated Fragment menu API")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            com.pseddev.playstreak.R.id.action_settings -> {
                findNavController().navigate(com.pseddev.playstreak.R.id.action_progressFragment_to_mainFragment)
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
        // Timeline tab temporarily disabled - reduced from 6/5 to 5/4 tabs
        override fun getItemCount(): Int = if (proUserManager.isProUser()) 5 else 4
        
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> DashboardFragment()
                1 -> CalendarFragment()
                2 -> SuggestionsFragment()
                3 -> PiecesFragment()
                // 4 -> TimelineFragment()  // Timeline fragment temporarily disabled for evaluation
                4 -> if (proUserManager.isProUser()) AbandonedFragment() else throw IllegalArgumentException("Invalid position: $position")
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }
    }
}