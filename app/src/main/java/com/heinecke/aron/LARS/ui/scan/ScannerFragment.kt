package com.heinecke.aron.LARS.ui.scan

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.heinecke.aron.LARS.R
import com.heinecke.aron.LARS.Utils
import com.heinecke.aron.LARS.data.model.Asset
import com.heinecke.aron.LARS.ui.APIFragment
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ScannerFragment : APIFragment() {
    private var lastText: String? = null
    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var beepManager: BeepManager
    private lateinit var viewModel: ScanViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        beepManager = BeepManager(requireActivity())
        viewModel = ViewModelProviders.of(requireActivity())[ScanViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_scanner, container, false)
        barcodeView = rootView.findViewById(R.id.barcode_scanner)
        val api = getAPI()
        barcodeView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                result?.run {
                    if (result.text == null || result.text == lastText) {
                        return;
                    }
                    lastText = result.text

                    beepManager.playBeepSound()

                    Log.d(this::class.java.name, "Scanned: $result")
                    viewModel.assetPattern.find(lastText!!, 0)?.groupValues?.run {
                        this.forEach { item -> Log.d(this::class.java.name, "Item: $item") }
                        val id = Integer.valueOf(this[1])
                        if (viewModel.scanList.value!!.any { asset: Asset -> asset.id == id }) {
                            Toast.makeText(context, R.string.duplicate_scan, Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            viewModel.incLoading()
                            api.getAsset(id).enqueue(object : Callback<Asset> {
                                override fun onFailure(call: Call<Asset>?, t: Throwable?) {
                                    Log.e(this::class.java.name, "Error resolving $id: $t")
                                    Toast.makeText(
                                        requireContext(),
                                        "Can't request: $t",
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                                    viewModel.decLoading()
                                }

                                override fun onResponse(
                                    call: Call<Asset>?,
                                    response: Response<Asset>?
                                ) {
                                    response?.run {
                                        if (this.isSuccessful && this.body()!!.id == id) {
                                            viewModel.scanList.value!!.add(0, this.body()!!)
                                        } else {
                                            Toast.makeText(
                                                requireContext(),
                                                R.string.invalid_scan_id,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } ?: Utils.logResponseVerbose(
                                        this@ScannerFragment::class.java,
                                        response
                                    )
                                    viewModel.decLoading()
                                }
                            })
                        }

                        mainViewModel.scanData.value = Integer.valueOf(this[1])
                    } ?: Toast.makeText(
                        requireContext(),
                        R.string.invalid_asset_code,
                        Toast.LENGTH_LONG
                    ).show()
                }


            }

            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {

            }
        })
        return rootView
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (barcodeView != null) {
            if (isVisibleToUser) {
                barcodeView.resume()
            } else {
                barcodeView.pauseAndWait()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pauseAndWait()
    }

    override fun onResume() {
        super.onResume()
        barcodeView.resume()
    }


    companion object {
        /**
         * Returns a new instance pair to use on a NavController
         */
        @JvmStatic
        fun newInstance(): Int {
            return R.id.scannerFragment
        }
    }
}