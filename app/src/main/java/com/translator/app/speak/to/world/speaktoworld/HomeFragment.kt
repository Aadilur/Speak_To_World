package com.translator.app.speak.to.world.speaktoworld

import android.annotation.SuppressLint
import android.os.Build
import android.os.Build.VERSION_CODES.N
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks.await
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.translator.app.speak.to.world.speaktoworld.adapter.AdapterAppList
import com.translator.app.speak.to.world.speaktoworld.data.DataAppList
import com.translator.app.speak.to.world.speaktoworld.databinding.FragmentHomeBinding
import com.translator.app.speak.to.world.speaktoworld.view_model.ViewModelAppList
import kotlinx.coroutines.*
import kotlinx.coroutines.NonDisposableHandle.parent
import org.checkerframework.checker.units.qual.s
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.resumeWithException


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModelAppList: ViewModelAppList by activityViewModels()

    private var list = ArrayList<DataAppList>()


    val TAG = "CameraXApp"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentHomeBinding = FragmentHomeBinding.inflate(inflater, container, false)
        binding = fragmentHomeBinding
        return binding.root
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        with(binding) {

            toTranslate.setOnClickListener {
                (activity as MainActivity).callFun()
            }

            toImageToText.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_imageToTextFragment2)
            }
        }


        getResponseUsingLiveData()




    }



    private fun getResponseUsingLiveData() {

        (activity as MainActivity).langModelList.observe(viewLifecycleOwner) {
            list = it
            try {
                binding.recyclerView.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                binding.recyclerView.adapter = AdapterAppList(requireContext(), list)
            } catch (exception: Exception) {
                Toast.makeText(context, "Error: $exception", Toast.LENGTH_SHORT).show()
            }

        }


    }




}


