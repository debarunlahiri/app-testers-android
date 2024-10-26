package com.summitcodeworks.apptesters.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import com.summitcodeworks.apptesters.activities.CreditsActivity
import com.summitcodeworks.apptesters.activities.DetailActivity
import com.summitcodeworks.apptesters.activities.RegisterActivity.Companion.TAG
import com.summitcodeworks.apptesters.adapter.HomeAdapter
import com.summitcodeworks.apptesters.apiClient.RetrofitClient
import com.summitcodeworks.apptesters.databinding.FragmentHomeBinding
import com.summitcodeworks.apptesters.models.userApps.UserApps
import com.summitcodeworks.apptesters.models.userApps.UserAppsResponse
import com.summitcodeworks.apptesters.models.userDetails.UserDetails
import com.summitcodeworks.apptesters.utils.CommonUtils.Companion.applyBoldStyle
import com.summitcodeworks.apptesters.utils.SharedPrefsManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment(), HomeAdapter.OnHomeAdapterListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var mContext: Context

    private lateinit var viewBinding: FragmentHomeBinding

    private lateinit var homeAdapter: HomeAdapter
    private var userAppsList: ArrayList<UserAppsResponse> = ArrayList<UserAppsResponse>()
    private lateinit var linearLayoutManager: LinearLayoutManager

    private var currentPage = 1
    private val itemsPerPage = 10
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mContext = requireContext()

        homeAdapter = HomeAdapter(mContext, userAppsList, this@HomeFragment)
        linearLayoutManager = LinearLayoutManager(mContext)
        viewBinding.rvHome.layoutManager = linearLayoutManager
        viewBinding.rvHome.adapter = homeAdapter
        viewBinding.rvHome.setHasFixedSize(true)
        viewBinding.rvHome.isNestedScrollingEnabled = false

        viewBinding.srlHome.setOnRefreshListener {
            fetchAppList(currentPage, itemsPerPage)
            viewBinding.srlHome.isRefreshing = false
        }


        fetchAppList(currentPage, itemsPerPage)

        viewBinding.cvHomeCredits.setOnClickListener {
            val creditsIntent = Intent(mContext, CreditsActivity::class.java)
            startActivity(creditsIntent)
        }

        setupScrollListener()
    }

    private fun setupScrollListener() {
        viewBinding.nsvHome.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            if (!isLoading && scrollY == (viewBinding.nsvHome.getChildAt(0).measuredHeight - viewBinding.nsvHome.height)) {
                // Load more data
                currentPage++
                fetchAppListMore(currentPage, itemsPerPage)
            }
        })
    }

    private fun fetchAppList(page: Int, perPage: Int) {
        isLoading = true
        authenticateUser()
        userAppsList.clear()
        homeAdapter.setUserAppList(userAppsList)
        homeAdapter.notifyDataSetChanged()

        RetrofitClient.apiInterface.getAppList(page, perPage).enqueue(object : Callback<UserApps> {
            override fun onResponse(call: Call<UserApps>, response: Response<UserApps>) {
                isLoading = false
                if (response.isSuccessful) {
                    if (response.body()?.header?.responseCode == 200) {
                        response.body()?.response?.let { apps ->
                            userAppsList.addAll(apps)
                            homeAdapter.setUserAppList(userAppsList)
                            homeAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<UserApps>, t: Throwable) {
                isLoading = false
                Log.e(TAG, "Network request failed", t)
                if (t is IOException) {
                    Toast.makeText(requireContext(), "Network error. Please try again.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "An unexpected error occurred. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun fetchAppListMore(page: Int, perPage: Int) {
        isLoading = true
        RetrofitClient.apiInterface.getAppList(page, perPage).enqueue(object : Callback<UserApps> {
            override fun onResponse(call: Call<UserApps>, response: Response<UserApps>) {
                isLoading = false
                if (response.isSuccessful) {
                    if (response.body()?.header?.responseCode == 200) {
                        response.body()?.response?.let { apps ->
                            userAppsList.addAll(apps)
                            homeAdapter.setUserAppList(userAppsList)
                            homeAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<UserApps>, t: Throwable) {
                isLoading = false
                Log.e(TAG, "Network request failed", t)
                if (t is IOException) {
                    Toast.makeText(requireContext(), "Network error. Please try again.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "An unexpected error occurred. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }



    private fun authenticateUser() {
        RetrofitClient.apiInterface.authenticateUser().enqueue(object : Callback<UserDetails> {
            override fun onResponse(call: Call<UserDetails>, response: Response<UserDetails>) {
                if (response.isSuccessful) {
                    val userDetails = response.body()?.response
                    if (userDetails != null) {
                        SharedPrefsManager.saveUserDetails(requireContext(), userDetails)
                        val postedOnText = "Available Credits: ${userDetails.userCredits}/60"
                        viewBinding.tvUserCredits.text = SharedPrefsManager.getUserDetails(mContext).userCredits.toString()

                    } else {
                        Log.e(TAG, "User details are null")
                        Toast.makeText(requireContext(), "User details are null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(TAG, "Login failed with code: ${response.code()} - ${response.message()}")
                    Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(p0: Call<UserDetails>, p1: Throwable) {
                Log.e(TAG, "Network request failed", p1)
                if (p1 is IOException) {
                    Toast.makeText(requireContext(), "Network error. Please try again.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "An unexpected error occurred. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onHomeAdapterClick(userApps: UserAppsResponse) {
        val detailIntent = Intent(mContext, DetailActivity::class.java)
        detailIntent.putExtra("app_id", userApps.appId)
        startActivity(detailIntent)
    }
}