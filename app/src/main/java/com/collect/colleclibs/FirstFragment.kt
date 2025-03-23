package com.collect.colleclibs

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.collect.colleclibs.databinding.FragmentFirstBinding
import com.sc.collectlibs.AppInfoUtil
import com.sc.collectlibs.AppInfoUtil.GetDeviceInfo
//import com.tbruyelle.rxpermissions3.RxPermissions

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.buttonFirst.setOnClickListener {
//            RxPermissions(this)
//                .requestEach(
//                    Manifest.permission.READ_SMS,
//                    Manifest.permission.READ_CONTACTS,
//                    Manifest.permission.READ_CALL_LOG,
//                    Manifest.permission.READ_PHONE_NUMBERS,
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                ).subscribe {
//
//                }
//        }
        binding.buttonAppList.setOnClickListener {
            val data = AppInfoUtil.getAppList(requireContext())
            binding.textviewFirst.text = data
        }
        binding.buttonContact.setOnClickListener {
            val data = AppInfoUtil.getContactString(requireContext())
            binding.textviewFirst.text = data
        }
        binding.buttonCallLog.setOnClickListener {
            val data = AppInfoUtil.getRecord(requireContext())
            binding.textviewFirst.text = data
        }
        binding.buttonSms.setOnClickListener {
            val data = AppInfoUtil.getSmsListType(requireContext())
            binding.textviewFirst.text = data
        }
        binding.buttonDeviceInfo.setOnClickListener {
            AppInfoUtil.getDeviceInfo(
                activity,
                { data ->
                    run {
                        activity?.runOnUiThread {
                            binding.textviewFirst.text = data
                        }
                    }
                    //切换到主线程更新ui

                }, 123,
                true
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}