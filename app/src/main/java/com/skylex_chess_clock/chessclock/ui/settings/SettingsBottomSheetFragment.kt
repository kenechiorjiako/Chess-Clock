package com.skylex_chess_clock.chessclock.ui.settings

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skylex_chess_clock.chessclock.*
import com.skylex_chess_clock.chessclock.databinding.RecyclerViewAlertDialogLayoutBinding
import com.skylex_chess_clock.chessclock.databinding.SettingsBottomSheetBinding
import com.skylex_chess_clock.chessclock.ui.adapters.SelectorRVAdapter
import com.skylex_chess_clock.chessclock.util.TimeHelper
import com.skylex_chess_clock.chessclock.util.TopLevelFiles
import com.skylex_chess_clock.chessclock.util.TopLevelFiles.Companion.ClockMode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.parceler.Parcels

class SettingsBottomSheetFragment: BottomSheetDialogFragment() {

    private lateinit var binding: SettingsBottomSheetBinding
    private lateinit var alertDialogLayoutBinding: RecyclerViewAlertDialogLayoutBinding
    private lateinit var mAlertDialog: AlertDialog

    private var selectedTime: MutableLiveData<TimeHelper> = MutableLiveData()
    private var selectedIncrement: MutableLiveData<TimeHelper> = MutableLiveData()
    private var selectedClockMode: MutableLiveData<ClockMode> = MutableLiveData()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = SettingsBottomSheetBinding.inflate(inflater, container, false)

        setupViewHelperObjects()
        setupViewListeners()
        loadPage()
        setupViews()

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog : BottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        return dialog
    }

    private fun setupViewHelperObjects() {
        val alertDialogView: View = layoutInflater.inflate(R.layout.recycler_view_alert_dialog_layout, null)
        alertDialogLayoutBinding = RecyclerViewAlertDialogLayoutBinding.bind(alertDialogView)
        mAlertDialog = AlertDialog.Builder(requireContext()).create()
        mAlertDialog.setView(alertDialogLayoutBinding.root)
        mAlertDialog.setCanceledOnTouchOutside(true)
        mAlertDialog.setCancelable(true)

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = RecyclerView.VERTICAL
        alertDialogLayoutBinding.recyclerView.layoutManager = layoutManager
    }
    private fun setupViewListeners() {
        binding.apply {
            clockTextView.setOnClickListener {
                val listItems = ClockMode.getAllModes()

                val selectorRVAdapter = SelectorRVAdapter(eventHandler = object : SelectorRVAdapter.EventHandler<ClockMode> {
                    override fun onItemClicked(item: ClockMode) {
                        selectedClockMode.value = item
                        mAlertDialog.dismiss()
                    }
                })
                selectorRVAdapter.setItems(listItems)
                selectorRVAdapter.setSelectedItem(selectedClockMode.value)

                alertDialogLayoutBinding.recyclerView.adapter = selectorRVAdapter

                mAlertDialog.show()
            }
            timeTextView.setOnClickListener {

                val listItems = TopLevelFiles.clockTimes

                val selectorRVAdapter = SelectorRVAdapter(eventHandler = object : SelectorRVAdapter.EventHandler<TimeHelper> {
                    override fun onItemClicked(item: TimeHelper) {
                        selectedTime.value = item
                        mAlertDialog.dismiss()
                    }
                })
                selectorRVAdapter.setItems(listItems)
                selectorRVAdapter.setSelectedItem(selectedTime.value)

                alertDialogLayoutBinding.recyclerView.adapter = selectorRVAdapter

                mAlertDialog.show()
            }
            incrementTextView.setOnClickListener {

                val listItems = TopLevelFiles.incrementTimes

                val selectorRVAdapter = SelectorRVAdapter(eventHandler = object : SelectorRVAdapter.EventHandler<TimeHelper> {
                    override fun onItemClicked(item: TimeHelper) {
                        selectedIncrement.value = item
                        mAlertDialog.dismiss()
                    }
                })
                selectorRVAdapter.setItems(listItems)
                selectorRVAdapter.setSelectedItem(selectedIncrement.value)

                alertDialogLayoutBinding.recyclerView.adapter = selectorRVAdapter

                mAlertDialog.show()
            }
            actionButton.setOnClickListener {
                setFragmentResult()
                dismiss()
            }
        }
    }
    private fun loadPage() {
        selectedTime.observe(viewLifecycleOwner, Observer<TimeHelper> {
            binding.timeTextView.text = it.toString()
        })
        selectedIncrement.observe(viewLifecycleOwner, Observer<TimeHelper> {
            binding.incrementTextView.text = it.toString()
        })
        selectedClockMode.observe(viewLifecycleOwner, Observer<ClockMode> {
            binding.clockTextView.text = it.toString()

            if (it == ClockMode.Sudden_Death || it == ClockMode.Hourglass) {
                binding.incrementTextView.visibility = View.GONE
                binding.incrementTextViewLabel.visibility = View.GONE
            } else {
                binding.incrementTextView.visibility = View.VISIBLE
                binding.incrementTextViewLabel.visibility = View.VISIBLE
            }
        })

        selectedTime.value = arguments?.getParcelable(TIME_RESULT_KEY)
        selectedIncrement.value = arguments?.getParcelable(INCREMENT_RESULT_KEY)
        selectedClockMode.value = arguments?.getSerializable(CLOCK_MODE_RESULT_KEY) as ClockMode
    }
    private fun setupViews() {}

    private fun setFragmentResult() {
        val outBundle = Bundle()

        outBundle.putSerializable(CLOCK_MODE_RESULT_KEY, selectedClockMode.value)
        outBundle.putParcelable(TIME_RESULT_KEY, Parcels.wrap(selectedTime.value))
        outBundle.putParcelable(INCREMENT_RESULT_KEY, Parcels.wrap(selectedIncrement.value))

        parentFragmentManager.setFragmentResult(RESULT_KEY, outBundle)
    }

    companion object {
        const val RESULT_KEY = "SettingsBottomSheetResultKey"
        private const val TAG = "SettingsBottomSheetFrag"
    }
}