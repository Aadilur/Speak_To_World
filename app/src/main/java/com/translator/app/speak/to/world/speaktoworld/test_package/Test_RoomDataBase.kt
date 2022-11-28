package com.translator.app.speak.to.world.speaktoworld.test_package

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.translator.app.speak.to.world.speaktoworld.R
import com.translator.app.speak.to.world.speaktoworld.databinding.FragmentTestRoomDataBaseBinding


class Test_RoomDataBase : Fragment() {

    lateinit var binding: FragmentTestRoomDataBaseBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentTestRoomDataBaseBinding = FragmentTestRoomDataBaseBinding
            .inflate(inflater,container,false)
        binding = fragmentTestRoomDataBaseBinding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}