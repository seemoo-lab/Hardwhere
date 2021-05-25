package de.tu_darmstadt.seemoo.HardWhere.ui.lib

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import de.tu_darmstadt.seemoo.HardWhere.R
import de.tu_darmstadt.seemoo.HardWhere.Utils
import de.tu_darmstadt.seemoo.HardWhere.Utils.Companion.hideKeyboardContext
import de.tu_darmstadt.seemoo.HardWhere.data.model.Asset
import de.tu_darmstadt.seemoo.HardWhere.ui.APIFragment
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import io.sentry.core.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Fragment for continuous scanning assets.
 * Uses the [EditorListViewModel]
 */
abstract class AbstractScannerFragment : APIFragment() {
    private var lastText: String? = null
    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var beepManager: BeepManager
    private val assetPattern: Regex = Regex("^http.*/([0-9]+)$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        beepManager = BeepManager(requireActivity())
    }

    /**
     * Called if [assetList] deduplication passed
     */
    abstract fun addToList(asset: Asset)
    abstract fun decreaseLoading()
    abstract fun increaseLoading()

    /**
     * Used for deduplication checks
     */
    abstract fun assetList(): ArrayList<Asset>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_scanner, container, false)
        barcodeView = rootView.findViewById(R.id.barcode_scanner)
        hideKeyboardContext(requireContext(),rootView)
        val api = getAPI()
        barcodeView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                result?.run {
                    if (result.text == null || result.text == lastText) {
                        return;
                    }
                    lastText = result.text

                    Utils.vibrate(context!!,100)

                    Log.d(this::class.java.name, "Scanned: $result")
                    assetPattern.find(lastText!!, 0)?.groupValues?.run {
                        this.forEach { item -> Log.d(this::class.java.name, "Item: $item") }
                        val id = Integer.valueOf(this[1])
                        if (assetList().any { asset: Asset -> asset.id == id }) {
                            Utils.displayToastUp(context!!,R.string.duplicate_scan,Toast.LENGTH_SHORT)
                            Utils.playErrorBeep()
                        } else {
                            try {
                                beepManager.playBeepSound()
                            } catch (e: Exception) {
                                Log.e(this@AbstractScannerFragment::class.java.name,"Failed to beep")
                                Sentry.captureException(e)
                            }

                            increaseLoading()
                            api.getAsset(id).enqueue(object : Callback<Asset> {
                                override fun onFailure(call: Call<Asset>?, t: Throwable?) {
                                    Log.e(this::class.java.name, "Error resolving $id: $t")
                                    Toast.makeText(
                                        requireContext(),
                                        "Can't request: $t",
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                                    decreaseLoading()
                                }

                                override fun onResponse(
                                    call: Call<Asset>?,
                                    response: Response<Asset>?
                                ) {
                                    response?.run {
                                        if (this.isSuccessful && this.body()!!.id == id) {
                                            addToList(this.body()!!)
                                        } else {
                                            Toast.makeText(
                                                requireContext(),
                                                R.string.invalid_scan_id,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } ?: Utils.logResponseVerbose(
                                        this@AbstractScannerFragment::class.java,
                                        response
                                    )
                                    decreaseLoading()
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
}