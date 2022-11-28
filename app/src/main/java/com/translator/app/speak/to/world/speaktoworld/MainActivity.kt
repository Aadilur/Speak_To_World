package com.translator.app.speak.to.world.speaktoworld

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.auth.api.phone.SmsRetrieverClient
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import com.translator.app.speak.to.world.speaktoworld.data.DataAppList
import com.translator.app.speak.to.world.speaktoworld.databinding.ActivityMainBinding
import com.translator.app.speak.to.world.speaktoworld.view_model.ShareViewModel
import com.translator.app.speak.to.world.speaktoworld.view_model.ViewModelAppList


class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navHostFragment: NavHostFragment
    lateinit var binding: ActivityMainBinding



    private val viewModelAppList: ViewModelAppList by viewModels()
    private val shareViewModel: ShareViewModel by viewModels()

    private var appList = ArrayList<DataAppList>()

    private var _langModelList = MutableLiveData<ArrayList<DataAppList>>()
    val langModelList: LiveData<ArrayList<DataAppList>>
        get() = _langModelList

    private var home: Boolean = false
    private var translate: Boolean = false
    private var settings: Boolean = false

//    var otpReceiver: OTPReceiver? = null
//    var stOtp : String? = null
//    private lateinit var smsClient: SmsRetrieverClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
//
//        startSmsRetriever()
//        val otpReceiver= OTPReceiver(viewModelAppList)
//        val filter = IntentFilter()
//        filter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION)
//        registerReceiver(otpReceiver,filter)
//
//        viewModelAppList.otpCode.observe(this){
//            Log.d("data7", "Your OTP for G Loan app is: $it")
//            startSmsRetriever()
//        }
        val sharedPrefLangTheme = getSharedPreferences("sharedPrefTheme",
            Context.MODE_PRIVATE
        )

        shareViewModel.theme = sharedPrefLangTheme.getInt("theme",0)

        when (shareViewModel.theme) {
            0 -> {



                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            1 -> {



                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            2 -> {



                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }

        }


        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController


        bottomNavigationView = binding.bottomNavigationView
        setupWithNavController(bottomNavigationView, navController)
        bottomNavigationView.labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_SELECTED

        navController.addOnDestinationChangedListener{ _, destination, _ ->

            if (destination.id == R.id.homeFragment || destination.id == R.id.settingsFragment){
                binding.bottomNavigationView.visibility = View.VISIBLE
            }else binding.bottomNavigationView.visibility = View.GONE

        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.exit_nav -> {
                    dialogue()
                    true
                }
                R.id.back_nav -> {
                    translate =
                        navController.backQueue.last().destination.id == R.id.translateSpeakFragment
                    settings =
                        navController.backQueue.last().destination.id == R.id.settingsFragment
                    if (translate || settings) {

                        callFun2()

                        onNavDestinationSelected(
                            item,
                            navController
                        )

                    } else true

                }
                R.id.translateSpeakFragment -> {
                    onNavDestinationSelected(
                        item,
                        navController
                    )
                }
                R.id.homeFragment -> {
                    onNavDestinationSelected(
                        item,
                        navController
                    )
                }
                R.id.settingsFragment -> {
                    onNavDestinationSelected(
                        item,
                        navController
                    )
                }
                else -> {
                    // Do the default behavior
                    onNavDestinationSelected(
                        item,
                        navController
                    )

                }
            }
        }


        getResponseUsingLiveData()

    }

//    private fun startSmsRetriever() {
//        val client = SmsRetriever.getClient(this /* context */)
//        val task: Task<Void> = client.startSmsRetriever()
//        task.addOnSuccessListener {
//            // Successfully started retriever, expect broadcast intent
//            // ...
//
//        }
//
//        task.addOnFailureListener {
//            // Failed to start retriever, inspect Exception for more details
//            // ...
//        }
//    }







    fun callFun() {

        bottomNavigationView.selectedItemId = R.id.translateSpeakFragment
        bottomNavigationView.menu.getItem(1).isVisible = true
        bottomNavigationView.menu.getItem(2).isVisible = true
        bottomNavigationView.menu.getItem(3).isVisible = false
        bottomNavigationView.menu.getItem(0).isVisible = false

    }

    private fun callFun2() {

        bottomNavigationView.selectedItemId = R.id.homeFragment
        bottomNavigationView.menu.getItem(1).isVisible = false
        bottomNavigationView.menu.getItem(2).isVisible = false
        bottomNavigationView.menu.getItem(3).isVisible = true
        bottomNavigationView.menu.getItem(0).isVisible = true

    }




    private fun dialogue() {
        val a = MaterialAlertDialogBuilder(this)
            .setTitle("Exit?")
            .setMessage("Press 'Yes' to exit the app.")
            .setPositiveButton(R.string.yes) { _, _ -> finishAffinity() }
            .setNegativeButton(R.string.no) {_,_ ->
                bottomNavigationView.selectedItemId = R.id.homeFragment
            }.setOnDismissListener {
                bottomNavigationView.selectedItemId = R.id.homeFragment
            }

        a.show()


    }

    private fun getResponseUsingLiveData() {
        viewModelAppList.getResponseUsingLiveData().observe(this) {
            if (it==null)return@observe
            appList.clear()
            try {

                it.items?.let { it1 -> appList.addAll(it1) }
                _langModelList.value = appList
                Log.d("data1", "error: ${it.error}")
                Log.d("data1", "exception: ${it.exception}")
                Log.d("data1", "items: ${it.items}")
            }catch (exception:Exception){
                Toast.makeText(this, "Error: $exception", Toast.LENGTH_SHORT).show()
            }

            if (it.exception!=null) Toast.makeText(this, "Error: ${it.exception}", Toast.LENGTH_SHORT).show()

        }
    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }




    override fun onBackPressed() {
        super.onBackPressed()
        home = navController.backQueue.last().destination.id == R.id.homeFragment
        if (home) {
            callFun2()
        }


    }
}
