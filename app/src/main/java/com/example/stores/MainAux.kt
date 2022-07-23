package com.example.stores

interface MainAux {
    fun hideFab(iVisible: Boolean = false)

    fun addStore(storeEntity: StoreEntity)
    fun updateStore(storeEntity: StoreEntity)
}