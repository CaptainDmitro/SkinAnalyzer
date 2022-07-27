package ru.captaindmitro.skinanalyzer.ui

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ru.captaindmitro.skinanalyzer.databinding.FragmentImageClassificationBinding

class LesionFragment : Fragment() {
    private lateinit var binding: FragmentImageClassificationBinding
    private val args: LesionFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImageClassificationBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backImageButton.setOnClickListener {
            findNavController().navigate(LesionFragmentDirections.actionLesionFragmentToCameraFragment())
        }
        binding.classificationTextView.text = args.lesions

        val uri = Uri.parse(args.imageUri)

        val source = ImageDecoder.createSource(activity?.contentResolver!!, uri)
        val drawable = ImageDecoder.decodeDrawable(source)
        binding.lesionImage.setImageDrawable(drawable)
    }
}