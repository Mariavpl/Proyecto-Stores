package com.example.stores

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.stores.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.jetbrains.anko.act
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.uiThread

class MainActivity : AppCompatActivity(), OnClickListener, MainAux {

    private lateinit var mBinding: ActivityMainBinding

    private lateinit var mAdapter: StoreAdapter
    private lateinit var mGridLayout: GridLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

    /*    mBinding.btnSave.setOnClickListener {
            val store = StoreEntity(name = mBinding.edtName.text.toString().trim())

            Thread{
                StoreApplication.dataBase.storeDao().addStore(store)
            }.start()

            mAdapter.add(store)
        }*/
        mBinding.fab.setOnClickListener { launchEditFragment() }

        setupRecylcerView()
    }

    private fun launchEditFragment(args: Bundle? = null) {
        val fragment = EditStoreFragment ()
        if (args != null) fragment.arguments = args

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        fragmentTransaction.add(R.id.containerMain, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

        hideFab()
    }

    private fun setupRecylcerView() {
        mAdapter = StoreAdapter(mutableListOf(), this)
        mGridLayout = GridLayoutManager(this, resources.getInteger(R.integer.main_columns))
        getStores()

        mBinding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = mGridLayout
            adapter = mAdapter
        }
    }
    private fun getStores(){
        doAsync {
            val stores = StoreApplication.dataBase.storeDao().getAllStores()
          uiThread {

              mAdapter.setStores(stores)
          }
        }
    }

    /*
    onClickListener
     */
    override fun onClick(storeId: Long) {
        val args = Bundle()
        args.putLong(getString(R.string.arg_id), storeId)

        launchEditFragment(args)
    }

    override fun onFavoriteStore(storeEntity: StoreEntity) {
        storeEntity.isFavorite = !storeEntity.isFavorite
        doAsync {
            StoreApplication.dataBase.storeDao().updateStore(storeEntity)
            uiThread {
                updateStore(storeEntity)
            }
        }
    }

    override fun onDeleteStore(storeEntity: StoreEntity) {
        val items = resources.getStringArray(R.array.array_options_item)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_options_title)
            .setItems(items, { dialogInterface, i ->
                when(i){
                    0 -> confirmDelet(storeEntity)

                    1 -> dial(storeEntity.phone)

                    2-> goToWebsite(storeEntity.website)
                }
            })
            .show()
    }

    private fun confirmDelet(storeEntity: StoreEntity){
        MaterialAlertDialogBuilder(this)
         .setTitle(R.string.dialog_delet_title)
         .setPositiveButton(R.string.dialog_delet_confirm, { dialogInterface, i ->
             doAsync {
                StoreApplication.dataBase.storeDao().deleteStore(storeEntity)
               uiThread {
                mAdapter.delete(storeEntity)
            }
        }
    })
    .setNegativeButton(R.string.dialog_delet_cancel, null)
    .show()
    }

    private fun dial(phone: String){
        val callIntent = Intent().apply {
            action = Intent.ACTION_DIAL
            data = Uri.parse("tel:$phone")
        }
        startIntent(callIntent)
    }

    private fun goToWebsite(website: String){
        if (website.isEmpty()){
            Toast.makeText(this, R.string.main_error_no_website, Toast.LENGTH_LONG).show()

        }else{
        val websiteIntent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(website)
        }
          startIntent(websiteIntent)
        }
    }

    private fun startIntent(intent: Intent){
        if (intent.resolveActivity(packageManager) != null)
            startActivity(intent)
        else
            Toast.makeText(this, R.string.main_error_no_resolve, Toast.LENGTH_LONG)

    }

    /*
    MainAux
     */
    override fun hideFab(iVisible: Boolean) {
        if (iVisible) mBinding.fab.show() else mBinding.fab.hide()

    }

    override fun addStore(storeEntity: StoreEntity) {
        mAdapter.add(storeEntity)

    }

    override fun updateStore(storeEntity: StoreEntity) {
        mAdapter.update(storeEntity)

    }
}