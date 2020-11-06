package com.funlocks.beaconhandson_android

import android.Manifest
import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.altbeacon.beacon.*
import permissions.dispatcher.*

@RuntimePermissions
class MainActivity : AppCompatActivity(R.layout.activity_main), BeaconConsumer {

    private val UNIQUE_ID: String? = "iBeacon"
    private val UUID: String? = null
    private val MAJOR_ID: String? = null
    private val MINOR_ID: String? = null

    /**
     * 受信するビーコンの設定
     */
    private val region = Region(
        UNIQUE_ID,
        UUID?.let { Identifier.parse(it) },
        MAJOR_ID?.let { Identifier.parse(it) },
        MINOR_ID?.let { Identifier.parse(it) })

    private val IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"
    private lateinit var beaconManager: BeaconManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        beaconManager = BeaconManager.getInstanceForApplication(this)
        beaconManager.isRegionStatePersistenceEnabled = false
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(IBEACON_FORMAT))

        start.setOnClickListener {
            startScanWithPermissionCheck()
        }

        stop.setOnClickListener {
            stopScan()
        }
    }

    override fun onResume() {
        super.onResume()

        /**
         * altbeacon開始
         */
        beaconManager.bind(this)
    }

    override fun onPause() {
        super.onPause()

        /**
         * altbeacon終了
         */
        beaconManager.unbind(this)
    }

    override fun onBeaconServiceConnect() {
        beaconManager.addMonitorNotifier(object : MonitorNotifier {

            /**
             * ビーコンの範囲内に入った時の処理
             */
            override fun didEnterRegion(region: Region?) {
                Log.d("iBeacon", "Enter Region:${region}")
            }

            /**
             * ビーコンの範囲内から出た時の処理
             */
            override fun didExitRegion(region: Region?) {
                Log.d("iBeacon", "Exit Region")
            }

            override fun didDetermineStateForRegion(state: Int, region: Region?) {
                Log.d("iBeacon", "Determine State$state, Region$region")
            }
        })

        beaconManager.addRangeNotifier { beacon, region ->
            Log.d("iBeacon", "range:${beacon}")
        }
    }

    /**
     * Permissionを許可した際の処理
     */
    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    fun startScan() {
        try {
            /**
             * Beacon情報の監視を開始
             */
            beaconManager.startMonitoringBeaconsInRegion(region)

            beaconManager.startRangingBeaconsInRegion(region)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private fun stopScan() {
        try {
            /**
             * Beacon情報の監視を終了
             */
            beaconManager.stopMonitoringBeaconsInRegion(region)

            beaconManager.stopRangingBeaconsInRegion(region)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    /**
     * Permissionを許可しなかった際の処理
     */
    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    fun onDefineFineLocation() {
        Toast.makeText(this, "位置情報が許可されていません", Toast.LENGTH_SHORT).show()
    }

    /**
     * Permissionを今後表示しない際の処理
     */
    @OnNeverAskAgain(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    fun onFineLocationNeverAskAgain() {
        Toast.makeText(this, "位置情報が許可されていません、設定から許可してください。", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        this.onRequestPermissionsResult(requestCode, grantResults)
    }
}