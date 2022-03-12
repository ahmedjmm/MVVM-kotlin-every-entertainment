package com.example.everyentertainment.views.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.*
import androidx.lifecycle.ViewModelProvider
import com.example.everyentertainment.R
import com.example.everyentertainment.databinding.ActivityMainBinding
import com.example.everyentertainment.viewModels.MemoryViewModel
import com.example.everyentertainment.viewModels.MemoryViewModelProvider
import com.example.everyentertainment.views.fragments.storageFragments.MemoryFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var memoryViewModel: MemoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val memoryViewModelProvider = MemoryViewModelProvider()
        memoryViewModel = ViewModelProvider(this, memoryViewModelProvider).get(MemoryViewModel::class.java)

        supportFragmentManager.commit {
            replace<MemoryFragment>(R.id.fragment_container_view)
            setReorderingAllowed(true)
            addToBackStack("stack")
        }

        val bottomNavigationView = binding.bottomNavigation
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_menu_storage -> {
                    supportFragmentManager.commit {
                        replace<MemoryFragment>(R.id.fragment_container_view)
                        setReorderingAllowed(true)
                        addToBackStack("stack")
                    }
                    true
                }
                R.id.navigation_menu_gallery -> {
                    supportFragmentManager.commit {
                        replace<MemoryFragment>(R.id.fragment_container_view)
                        setReorderingAllowed(true)
                        addToBackStack("stack")
                    }
                    true
                }
                R.id.navigation_menu_music -> {
                    supportFragmentManager.commit {
                        replace<MemoryFragment>(R.id.fragment_container_view)
                        setReorderingAllowed(true)
                        addToBackStack("stack")
                    }
                    true
                }
                R.id.navigation_menu_browser -> {
                    supportFragmentManager.commit {
                        replace<MemoryFragment>(R.id.fragment_container_view)
                        setReorderingAllowed(true)
                        addToBackStack("stack")
                    }
                    true
                }
                else -> false
            }
        }
    }
}
