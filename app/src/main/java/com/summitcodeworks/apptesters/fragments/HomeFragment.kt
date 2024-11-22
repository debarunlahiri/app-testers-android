package com.summitcodeworks.apptesters.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.summitcodeworks.apptesters.utils.CommonUtils
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

        viewBinding.ibSearchClear.visibility = View.INVISIBLE
        viewBinding.tvHomeNoData.visibility = View.GONE

        homeAdapter = HomeAdapter(mContext, userAppsList, this@HomeFragment)
        linearLayoutManager = LinearLayoutManager(mContext)
        viewBinding.rvHome.layoutManager = linearLayoutManager
        viewBinding.rvHome.adapter = homeAdapter
        viewBinding.rvHome.setHasFixedSize(true)
        viewBinding.rvHome.isNestedScrollingEnabled = false

        viewBinding.srlHome.setOnRefreshListener {
            currentPage = 1
            userAppsList.clear()
            homeAdapter.setUserAppList(userAppsList)
            homeAdapter.notifyDataSetChanged()
            fetchAppList(currentPage, itemsPerPage)
            viewBinding.srlHome.isRefreshing = false
        }


        fetchAppList(currentPage, itemsPerPage)

        viewBinding.cvHomeCredits.setOnClickListener {
            val creditsIntent = Intent(mContext, CreditsActivity::class.java)
            startActivity(creditsIntent)
        }

        viewBinding.etHomeSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val searchText = p0.toString().trim()
                if (searchText.isNotEmpty()) {
                    searchApps(searchText)
                }
                if (searchText.length >= 3) {
                    viewBinding.ibSearchClear.visibility = View.VISIBLE
                } else {
                    viewBinding.ibSearchClear.visibility = View.INVISIBLE
                }
            }

        })

        viewBinding.ibSearchClear.setOnClickListener {
            viewBinding.etHomeSearch.text.clear()
            fetchAppList(currentPage, itemsPerPage)
        }

        setupScrollListener()
    }

    private fun searchApps(searchText: String) {
        RetrofitClient.apiInterface(mContext).searchApps(searchText).enqueue(object : Callback<UserApps> {
            override fun onResponse(p0: Call<UserApps>, p1: Response<UserApps>) {
                if (p1.isSuccessful) {
                    if (p1.body()?.header?.responseCode == 200) {
                        val searchResponse = p1.body()?.response
                        if (searchResponse != null) {
                            userAppsList.clear()
                            userAppsList.addAll(searchResponse)
                            homeAdapter.setUserAppList(userAppsList)
                            homeAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }

            override fun onFailure(p0: Call<UserApps>, p1: Throwable) {
                CommonUtils.apiRequestFailedToast(mContext, p1)
            }


        })
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
        viewBinding.pbHome.visibility = View.VISIBLE
        viewBinding.tvHomeNoData.visibility = View.GONE
        viewBinding.llNoDataView.visibility = View.GONE
        isLoading = true
        authenticateUser()
        userAppsList.clear()
        homeAdapter.setUserAppList(userAppsList)
        homeAdapter.notifyDataSetChanged()

        RetrofitClient.apiInterface(mContext).getAppList(page, perPage).enqueue(object : Callback<UserApps> {
            override fun onResponse(call: Call<UserApps>, response: Response<UserApps>) {
                isLoading = false
                if (response.isSuccessful) {
                    viewBinding.pbHome.visibility = View.GONE
                    if (response.body()?.header?.responseCode == 200) {
                        response.body()?.response?.let { apps ->
                            userAppsList.addAll(apps)
                            homeAdapter.setUserAppList(userAppsList)
                            homeAdapter.notifyDataSetChanged()
                            if (userAppsList.isEmpty()) {
                                viewBinding.tvHomeNoData.visibility = View.GONE
                            } else {
                                viewBinding.tvHomeNoData.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<UserApps>, t: Throwable) {
                viewBinding.pbHome.visibility = View.GONE
                viewBinding.tvHomeNoData.visibility = View.VISIBLE
                viewBinding.tvHomeNoData.text = "There is a problem loading apps. Please try again later."
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
        RetrofitClient.apiInterface(mContext).getAppList(page, perPage).enqueue(object : Callback<UserApps> {
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
                CommonUtils.apiRequestFailedToast(mContext, t)
            }
        })
    }



    private fun authenticateUser() {
        RetrofitClient.apiInterface(mContext).authenticateUser().enqueue(object : Callback<UserDetails> {
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
                CommonUtils.apiRequestFailedToast(mContext, p1)

            }

        })
    }


    override fun onResume() {
        super.onResume()
        authenticateUser()
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