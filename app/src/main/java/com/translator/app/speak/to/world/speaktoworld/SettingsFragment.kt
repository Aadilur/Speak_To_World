package com.translator.app.speak.to.world.speaktoworld

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.translator.app.speak.to.world.speaktoworld.databinding.FragmentSettingsBinding
import com.translator.app.speak.to.world.speaktoworld.view_model.ShareViewModel


class SettingsFragment : Fragment() {


    private val shareViewModel: ShareViewModel by activityViewModels()

    lateinit var binding: FragmentSettingsBinding

    private val packageName = "com.translator.app.speak.to.world.speaktoworld"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentSettingsBinding = FragmentSettingsBinding.inflate(inflater, container, false)
        binding = fragmentSettingsBinding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        themeItem()

        binding.changeVoice.setOnClickListener {
            changeVoice()
        }
        binding.changeVoiceSpeed.setOnClickListener {
            changeVoiceSpeed()
        }

        binding.actionSettingsFragmentToCamera.setOnClickListener {
//            findNavController().apply {
//                navigate(R.id.action_settingsFragment_to_camera)
////                backQueue.clear()
//            }

            val shareData = Intent(Intent.ACTION_SEND)
            shareData.type = "text/plain"
            val dataToShare =
                "Speak To World is an offline Translation & Voice Narration application with beautiful ui. " +
                        "\n\nDownload the app from Google Play Store : \nhttps://play.google.com/store/apps/details?id=com.translator.app.speak.to.world.speaktoworld"
            shareData.putExtra(Intent.EXTRA_SUBJECT, "Speak To World")
            shareData.putExtra(Intent.EXTRA_TEXT, dataToShare)
            startActivity(Intent.createChooser(shareData, "Speak To World"))
        }


        binding.rateUs.setOnClickListener {
            rateUs()
        }

        binding.changeTheme.setOnClickListener {
            chooseThemeDialog()
        }


    }

    private fun changeVoice() {
        val intent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setPackage("com.google.android.tts") /*replace with the package name of the target TTS engine*/
        try {
            this.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
        }
    }

    private fun changeVoiceSpeed() {
        val intent = Intent("com.android.settings.TTS_SETTINGS")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            this.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
        }
    }

    private fun rateUs(){
        val uri: Uri = Uri.parse("market://details?id=$packageName")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    private fun chooseThemeDialog() {

        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(getString(R.string.change_theme))
        val styles = arrayOf("Light", "Dark", "System default")
        val checkedItem = shareViewModel.theme

        val sharedPrefTheme = requireActivity().getSharedPreferences("sharedPrefTheme",
            Context.MODE_PRIVATE
        )

        builder.setSingleChoiceItems(styles, checkedItem) { dialog, which ->
            when (which) {
                0 -> {

                    sharedPrefTheme.edit().apply {
                        putInt("theme", which)
                    }.apply()

                    shareViewModel.theme = 0
                    val sunIcon = ContextCompat.getDrawable(requireContext(), R.drawable.light_mode)
                    binding.themeTxt.setCompoundDrawablesWithIntrinsicBounds(null, null, sunIcon, null)

                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    dialog.dismiss()

                }
                1 -> {

                    sharedPrefTheme.edit().apply {
                        putInt("theme", 1)
                    }.apply()

                    shareViewModel.theme = 1
                    val moonIcon = ContextCompat.getDrawable(requireContext(), R.drawable.dark_mode)
                    binding.themeTxt.setCompoundDrawablesWithIntrinsicBounds(null,null,moonIcon,null)

                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    dialog.dismiss()
                }
                2 -> {

                    sharedPrefTheme.edit().apply {
                        putInt("theme", 2)
                    }.apply()

                    shareViewModel.theme = 2
                    val autoIcon = ContextCompat.getDrawable(requireContext(), R.drawable.auto_mod)
                    binding.themeTxt.setCompoundDrawablesWithIntrinsicBounds(null, null, autoIcon, null)

                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    dialog.dismiss()
                }

            }

        }

        val dialog = builder.create()
        dialog.show()
    }


    private fun themeItem (){

        val moonIcon = ContextCompat.getDrawable(requireContext(), R.drawable.dark_mode)
        val sunIcon = ContextCompat.getDrawable(requireContext(), R.drawable.light_mode)
        val autoIcon = ContextCompat.getDrawable(requireContext(), R.drawable.auto_mod)


        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO ) {
            binding.themeTxt.setCompoundDrawablesWithIntrinsicBounds(null, null, sunIcon, null)
        }

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            binding.themeTxt.setCompoundDrawablesWithIntrinsicBounds(null,null,moonIcon,null)
        }

        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            binding.themeTxt.setCompoundDrawablesWithIntrinsicBounds(null, null, autoIcon, null)
        }
    }


}