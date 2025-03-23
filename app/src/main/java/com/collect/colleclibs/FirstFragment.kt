package com.collect.colleclibs

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.collect.colleclibs.databinding.FragmentFirstBinding
import com.permissionx.guolindev.PermissionX
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
        binding.buttonFirst.setOnClickListener {
            PermissionX.init(this)
                .permissions(
                    Manifest.permission.READ_SMS,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_PHONE_NUMBERS,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
                .request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
                        Toast.makeText(requireActivity(), "All permissions are granted", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(requireActivity(), "These permissions are denied: $deniedList", Toast.LENGTH_LONG).show()
                    }
                }
        }
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